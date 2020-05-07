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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.himalaya.interfaces.IAlbumDetailViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.utils.ImageBlur;
import com.example.himalaya.views.RoundRectImageView;
import com.example.himalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener {

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
                    Log.d(TAG, "drawable null?  " + drawable);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);//占满状态栏
        initView();
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);
    }

    private void initView() {
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

    }

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
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
                Log.d(TAG, "jump to Player");
            }
        });
        return detailListView;
    }


    @Override
    public void OnDetailListLoader(List<Track> tracks) {
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
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    @Override
    public void onAlbumLoader(Album album) {
        long id = album.getId();
        mCurrentId = id;
        Log.d(TAG, "album  id-- " + id);
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
    public void onRetryClick() {
        //设置网络错误，点击重试
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mCurrentId, mCurrentPage);
        }
    }
}
