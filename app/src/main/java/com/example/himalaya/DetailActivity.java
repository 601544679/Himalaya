package com.example.himalaya;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.himalaya.adapters.DetailListAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IAlbumDetailViewCallback;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.utils.ImageBlur;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.RoundRectImageView;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener, IPlayerCallback {

    private static final String TAG = "DetailActivity";
    private ImageView mLargeCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private int mCurrentPage = 1;
    private long mCurrentId = -1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Drawable drawable = mLargeCover.getDrawable();
                    LogUtil.d(TAG, "drawable null?  " + drawable);
                    if (drawable != null) {
                        ImageBlur.makeBlur(mLargeCover, DetailActivity.this);

                    }
                    break;
            }
        }
    };
    private RecyclerView mDetailList;
    private DetailListAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UILoader mUiLoader;
    private ImageView mPlayControlBtn;
    private TextView mPlayControlTips;
    private PlayerPresenter mPlayerPresenter;
    private LinearLayout mPlayControlContainer;
    private List<Track> mCurrentTracks = null;
    private final static int DEFAULT_PLAY_INDEX = 0;
    private TwinklingRefreshLayout mRefreshLayout;
    private boolean mIsLoaderMore = false;
    private String mCurrentTrackTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);//占满状态栏
        initView();
        //专辑详情的Presenter
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);
        //播放器的Presenter
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);
        //记入详情界面后获取播放状态，更改图标，文本
        updatePlayState(mPlayerPresenter.isPlaying());
        initEvent();
    }

    private void initEvent() {
        LogUtil.d(TAG, "initEvent");
        mPlayControlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断播放器是否有播放列表，如果我们退出程序在进入，如果之前是在播放，那么文本就显示停止
                //但是我们重新进入程序，此时没有播放列表，点击文本无法更改播放状态
                if (mPlayerPresenter != null) {
                    //判断播放器是否有列表
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (hasPlayList) {
                        handlePlayControl();
                    } else {
                        handleNoPlayList();
                    }

                }

            }
        });
    }

    /**
     * 播放器没有播放内容,点击按钮就从当前列表，第一首开始播放
     */
    private void handleNoPlayList() {
        LogUtil.d(TAG, "handleNoPlayList");
        mPlayerPresenter.setPlayList(mCurrentTracks, DEFAULT_PLAY_INDEX);
    }

    private void handlePlayControl() {
        LogUtil.d(TAG, "handlePlayControl");
        //控制播放器状态
        if (mPlayerPresenter.isPlaying()) {
            //正在播放就暂停
            mPlayerPresenter.pause();
        } else {
            //暂停就播放
            mPlayerPresenter.play();
        }
    }

    private void initView() {
        LogUtil.d(TAG, "initView");
        mDetailListContainer = findViewById(R.id.detail_list_container);
        //设置UILoader
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
        }
        //布局添加内容前先移除所有
        mDetailListContainer.removeAllViews();
        mDetailListContainer.addView(mUiLoader);
        //设置网络错误，点击重试
        mUiLoader.setOnRetryClickListener(this);

        mLargeCover = findViewById(R.id.iv_large_cover);
        mSmallCover = findViewById(R.id.iv_small_cover);
        mAlbumTitle = findViewById(R.id.tv_album_title);
        mAlbumAuthor = findViewById(R.id.tv_album_author);
        //播放控制图标
        mPlayControlBtn = findViewById(R.id.detail_play_control);
        mPlayControlTips = findViewById(R.id.play_control_tv);
        mPlayControlContainer = findViewById(R.id.play_control_container);

    }

    private View createSuccessView(ViewGroup container) {
        LogUtil.d(TAG, "createSuccessView");
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        //找到刷新控件
        mRefreshLayout = detailListView.findViewById(R.id.refresh_layout);
        mDetailList = detailListView.findViewById(R.id.album_detail_list);
        mDetailList.setLayoutManager(new LinearLayoutManager(this));
        mDetailListAdapter = new DetailListAdapter();
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mDetailList.setAdapter(mDetailListAdapter);
        mDetailListAdapter.setItemClickListener(new DetailListAdapter.ItemClickListener() {
            @Override
            public void ItemClick(List<Track> list, int position) {
                //跳转到播放器界面
                //设置播放器的数据
                PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
                playerPresenter.setPlayList(list, position);
                startActivity(new Intent(DetailActivity.this, PlayerActivity.class));
                LogUtil.d(TAG, "jump to Player");
            }
        });
        //试试开源框架的属性
        BezierLayout headerView = new BezierLayout(this);
        mRefreshLayout.setHeaderView(headerView);
        mRefreshLayout.setOverScrollBottomShow(false);
        mRefreshLayout.setMaxHeadHeight(140);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                Toast.makeText(DetailActivity.this, "开始刷新", Toast.LENGTH_SHORT).show();
                BaseApplication.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.finishRefreshing();
                    }
                }, 2000);

            }

            //使用finishRefreshing()方法结束刷新，finishLoadmore()方法结束加载更多。此处OnRefreshListener还有其它方法，可以选择需要的来重写。
            //如果你想进入到界面的时候主动调用下刷新，可以调用startRefresh()/startLoadmore()方法。
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                //加载更多内容
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                    mIsLoaderMore = true;
                }
                Toast.makeText(DetailActivity.this, "加载更多", Toast.LENGTH_SHORT).show();
//                BaseApplication.getHandler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(DetailActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
//                        mRefreshLayout.finishLoadmore();
//                    }
//                }, 2000);

            }
        });
        return detailListView;
    }


    @Override
    public void OnDetailListLoader(List<Track> tracks) {
        LogUtil.d(TAG, "OnDetailListLoader");
        if (mIsLoaderMore && mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
            mIsLoaderMore = false;
        }
        this.mCurrentTracks = tracks;
        //判断数据结果，显示内容为空界面
        if (tracks == null && tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //更新或设置UI数据
        mDetailListAdapter.setData(tracks);
    }

    @Override
    public void onNetWorkError(int errorCode, String errorMessage) {
        LogUtil.d(TAG, "onNetWorkError");
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    @Override
    public void onAlbumLoader(Album album) {
        LogUtil.d(TAG, "onAlbumLoader");
        long id = album.getId();
        mCurrentId = id;
        LogUtil.d(TAG, "album  id-- " + id);
        //获取专辑详情内容
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) album.getId(), mCurrentPage);
        }
        //那数据显示正在加载
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        mAlbumTitle.setText(album.getAlbumTitle() + "");
        mAlbumAuthor.setText(album.getAnnouncer().getNickname() + "");
        //做毛玻璃效果
        Glide.with(DetailActivity.this).load(album.getCoverUrlLarge()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
                return false;
            }
        }).into(mLargeCover);
        Glide.with(DetailActivity.this).load(album.getCoverUrlSmall()).into(mSmallCover);

    }

    @Override
    public void onLoaderMoreFinished(int size) {
        LogUtil.d(TAG, "onLoaderMoreFinished");
        if (size > 0) {
            Toast.makeText(DetailActivity.this, "成功加载" + size + "条", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DetailActivity.this, "没有更多", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshFinished(int size) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unregisterViewCallback(this);
            mPlayerPresenter = null;
        }
    }

    @Override
    public void onRetryClick() {
        LogUtil.d(TAG, "onRetryClick");
        //设置网络错误，点击重试
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mCurrentId, mCurrentPage);
        }
    }

    //=======================PlayerPresenter方法实现start============================//
    @Override
    public void onPlayStart() {
        LogUtil.d(TAG, "onPlayStart");
        //修改图标,文字为暂停状态
        updatePlayState(true);
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause");
        updatePlayState(false);
    }

    //根据播放状态改变文本
    private void updatePlayState(boolean playing) {
        LogUtil.d(TAG, "updatePlayState " + playing);
        if (mPlayControlBtn != null && mPlayControlTips != null) {
            mPlayControlBtn.setImageResource(playing ? R.drawable.selector_play_control_pause : R.drawable.selector_play_control_play);
            if (!playing) {
                mPlayControlTips.setText(R.string.click_play_text);
            } else {
                if (!TextUtils.isEmpty(mCurrentTrackTitle)) {
                    mPlayControlTips.setText(mCurrentTrackTitle);
                }
            }
        }

    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop");
        updatePlayState(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void onNextPlay(Track track) {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        LogUtil.d(TAG, "onTrackUpdate");
        LogUtil.d(TAG, "track is null ? " + track);
        LogUtil.d(TAG, "mPlayControlTips is null ? " + mPlayControlTips);
        if (track != null) {
            mCurrentTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mCurrentTrackTitle) && mPlayControlTips != null) {
                mPlayControlTips.setText(mCurrentTrackTitle);
                LogUtil.d(TAG, "track.getTrackTitle()  " + track.getTrackTitle());
            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
    //=======================PlayerPresenter方法实现end============================//
}
