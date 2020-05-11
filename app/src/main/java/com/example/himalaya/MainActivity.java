package com.example.himalaya;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.himalaya.adapters.IndicatorAdapter;
import com.example.himalaya.adapters.MainContentAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.data.XimalayaDBHelper;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.RoundRectImageView;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends BaseActivity implements IPlayerCallback {

    private static final String TAG = "MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;
    private RoundRectImageView mRoundRectImageView;
    private TextView mHeaderTitle;
    private TextView mSubTitle;
    private ImageView mPlayControl;
    private PlayerPresenter mPlayerPresenter;
    private LinearLayout mPlayControlItem;
    private ImageView mSearchBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //LogUtil.init(this.getPackageName(), false);//初始化,输入包名确定是这个包的Log,true就不显示Log
        initView();
        initEvent();
        initPresenter();

        LogUtil.d(TAG, "onCreate");
    }



    private void initPresenter() {
        LogUtil.d(TAG, "initPresenter");
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);
    }

    private void initEvent() {
        LogUtil.d(TAG, "initEvent");
        mIndicatorAdapter.setOnIndicatorTabClickListener(new IndicatorAdapter.OnIndicatorTabClickListener() {
            @Override
            public void onTabClick(int position) {
                mContentPager.setCurrentItem(position);
            }
        });
        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                    //如果没有播放列表点击播放按钮默认播放第一个推荐专辑
                    //第一个推荐专辑每天都会变
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        playFirstRecommend();
                        mPlayerPresenter.playByIndex(0);
                    } else {
                        if (mPlayerPresenter.isPlaying()) {
                            mPlayerPresenter.pause();

                        } else {
                            mPlayerPresenter.play();
                        }
                    }

                }
            }
        });
        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //没有播放列表就默认跳转到第一个
                boolean hasPlayList = mPlayerPresenter.hasPlayList();
                if (!hasPlayList) {
                    playFirstRecommend();
                    mPlayerPresenter.playByIndex(0);
                }  //跳转到播放器界面
                startActivity(new Intent(MainActivity.this, PlayerActivity.class));
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
    }

    /**
     * 播放第一个推荐专辑
     */
    private void playFirstRecommend() {
        LogUtil.d(TAG, "playFirstRecommend");
        RecommendPresenter instance = RecommendPresenter.getInstance();
        List<Album> currentRecommend = instance.getCurrentRecommend();
        if (currentRecommend != null) {
            Album album = currentRecommend.get(0);
            long albumId = album.getId();
            mPlayerPresenter.playByAlbumId(albumId);
        }
    }

    private void initView() {
        LogUtil.d(TAG, "initView");
        mMagicIndicator = findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(getResources().getColor(R.color.main_color));
        //创建indicator适配器
        mIndicatorAdapter = new IndicatorAdapter(MainActivity.this);
        CommonNavigator commonNavigator = new CommonNavigator(MainActivity.this);
        commonNavigator.setAdjustMode(true);//平分布局
        commonNavigator.setAdapter(mIndicatorAdapter);
        //设置要显示的内容

        //Viewpager
        mContentPager = findViewById(R.id.content_pager);
        //创建内容适配器
        MainContentAdapter mainContentAdapter = new MainContentAdapter(getSupportFragmentManager());
        mContentPager.setAdapter(mainContentAdapter);
        //绑定导航栏很viewpager
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mContentPager);

        mRoundRectImageView = findViewById(R.id.main_track_cover);
        mHeaderTitle = findViewById(R.id.main_head_title);
        mHeaderTitle.setSelected(true);
        mSubTitle = findViewById(R.id.main_sub_title);
        mPlayControl = findViewById(R.id.main_play_control);
        mPlayControlItem = findViewById(R.id.main_play_control_item);
        mSearchBtn = findViewById(R.id.search_btn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unregisterViewCallback(this);
            mPlayerPresenter = null;
        }
    }

    private void updatePlayControl(boolean isPlaying) {
        LogUtil.d(TAG, "updatePlayControl");
        if (mPlayControl != null) {
            mPlayControl.setImageResource(isPlaying ? R.drawable.selector_player_stop : R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStart() {
        updatePlayControl(true);
    }

    @Override
    public void onPlayPause() {
        updatePlayControl(false);
    }

    @Override
    public void onPlayStop() {
        updatePlayControl(false);
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
        if (track != null) {
            if (mHeaderTitle != null) {
                mHeaderTitle.setText(track.getTrackTitle());
            }
            if (mSubTitle != null) {
                mSubTitle.setText(track.getAnnouncer().getNickname());
            }
            if (mRoundRectImageView != null) {
                Glide.with(MainActivity.this).load(track.getCoverUrlSmall()).into(mRoundRectImageView);
            }
        }

    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}
