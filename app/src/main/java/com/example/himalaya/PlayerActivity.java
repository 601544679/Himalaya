package com.example.himalaya;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.himalaya.adapters.PlayerTrackPagerAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.presenters.PlayerPresenter;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerActivity extends BaseActivity implements View.OnClickListener, IPlayerCallback {

    private static final String TAG = "PlayerActivity";
    private ImageView mControlBtn;
    private PlayerPresenter mPlayerPresenter;
    private TextView mCurrentPosition;
    private TextView mTotalDuration;
    //最好在逻辑层处理单位转换
    SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private SeekBar mDurationBar;
    private int mCurrentProgress = 0;
    private boolean mIsUserTouchProgressBar = false;
    private ImageView mPlayNextBtn;
    private ImageView mPlayPreBtn;
    private TextView mTrackTitle;
    private String mTrackTitleText;
    private ViewPager mTrackPagerView;
    private PlayerTrackPagerAdapter mTrackPagerAdapter;
    private boolean mIsUserSlidePager = false;//slide滑动的意思,这个变量的作用，
    private ImageView mPlayModeSwitchBtn;
    // 由于ViewPager的addOnPageChangeListener监听选中页面时切换播放内容,而我们用点击下一首进行切换时，会更改播放内容
    //此时会调用addOnPageChangeListener的onPageSelected方法造成二次调用,可能死循环,所以加个判断，是用户切换才调用onPageSelected
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();


    //PLAY_MODEL_SINGLE_LOOP
    //PLAY_MODEL_LIST
    //PLAY_MODEL_LIST_LOOP
    //PLAY_MODEL_RANDOM
    static {
        //根据当前播放模式获取下一个播放模式
        sPlayModeRule.put(PLAY_MODEL_LIST, PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP, PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM, PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP, PLAY_MODEL_LIST);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        //注册
        mPlayerPresenter.registerViewCallback(this);
        //界面初始化后才获取数据
        mPlayerPresenter.getPlayList();
        initEvent();
        //这里调用播放器可能没准备好导致进入不会播放，因此在PlayerPresenter里的onSoundPrepared() 调用startPlay();

    }

    @Override
    protected void onDestroy() {
        //取消注册
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unregisterViewCallback(this);
            mPlayerPresenter = null;
        }
    }

    /**
     * 开始播放
     */
   /* private void startPlay() {
        Log.d(TAG, "startPlay");
        if (mPlayerPresenter != null) {
            mPlayerPresenter.play();
        }
        //我们注册了接口mPlayerPresenter.registerViewCallback(this);
        //在onPlayStart（）实现对应的UI变化
    }*/
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        Log.d(TAG, "initEvent");
        mControlBtn.setOnClickListener(this);
        mPlayPreBtn.setOnClickListener(this);
        mPlayNextBtn.setOnClickListener(this);
        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //fromUser是否用户触摸
                if (fromUser) {
                    mCurrentProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //用户触摸
                mIsUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //手离开，更新进度
                mIsUserTouchProgressBar = false;
                mPlayerPresenter.seekTo(mCurrentProgress);

            }
        });
        mTrackPagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //页面选中时，切换播放内容
                if (mPlayerPresenter != null && mIsUserSlidePager) {
                    mPlayerPresenter.playByIndex(position);
                }
                mIsUserSlidePager = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTrackPagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mIsUserSlidePager = true;
                        break;
                }
                return false;
            }
        });
        mPlayModeSwitchBtn.setOnClickListener(this);
    }

    private void initView() {
        Log.d(TAG, "initView");
        mControlBtn = findViewById(R.id.play_or_stop);
        mCurrentPosition = findViewById(R.id.current_position);
        mTotalDuration = findViewById(R.id.track_duration);
        mDurationBar = findViewById(R.id.track_seek_bar);
        mPlayNextBtn = findViewById(R.id.play_next);
        mPlayPreBtn = findViewById(R.id.play_pre);
        mTrackTitle = findViewById(R.id.track_title);
        if (!TextUtils.isEmpty(mTrackTitleText)) {
            mTrackTitle.setText(mTrackTitleText);
        }
        mTrackPagerView = findViewById(R.id.track_pager_view);
        //创建适配器
        mTrackPagerAdapter = new PlayerTrackPagerAdapter();
        mTrackPagerView.setAdapter(mTrackPagerAdapter);
        mPlayModeSwitchBtn = findViewById(R.id.play_mode_switch_btn);
    }

    @Override
    public void onClick(View v) {
        if (v == mControlBtn) {
            //如果现在是正在播放就暂停，暂停就继续播放
            if (mPlayerPresenter.isPlay()) {
                mPlayerPresenter.pause();
            } else {
                mPlayerPresenter.play();
            }
        } else if (v == mPlayPreBtn) {
            if (mPlayerPresenter != null) {
                mPlayerPresenter.playPre();
            }
        } else if (v == mPlayNextBtn) {
            if (mPlayerPresenter != null) {
                mPlayerPresenter.playNext();
            }
        } else if (v == mPlayModeSwitchBtn) {
            //切换播放模式
            // 默认PLAY_MODEL_LIST列表播放
            //设置播放器模式，mode取值为PlayMode中的下列之一：
            //PLAY_MODEL_SINGLE单曲播放
            //PLAY_MODEL_SINGLE_LOOP 单曲循环播放
            //PLAY_MODEL_LIST列表播放
            //PLAY_MODEL_LIST_LOOP列表循环
            //PLAY_MODEL_RANDOM 随机播放

            //根据当前播放模式，获取下一个播放模式
            XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
            //修改播放模式
            if (mPlayerPresenter != null) {
                mPlayerPresenter.switchPlayMode(playMode);
                // mCurrentMode = playMode;
            }

        }
    }

    private void updatePlayModeBtnImg() {
        switch (mCurrentMode) {
            case PLAY_MODEL_LIST:
                mPlayModeSwitchBtn.setImageResource(R.drawable.selector_play_mode_list_order);
                break;
            case PLAY_MODEL_RANDOM:
                mPlayModeSwitchBtn.setImageResource(R.drawable.selector_play_mode_random);
                break;
            case PLAY_MODEL_LIST_LOOP:
                mPlayModeSwitchBtn.setImageResource(R.drawable.selector_play_mode_list_order_looper);
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                mPlayModeSwitchBtn.setImageResource(R.drawable.selector_play_mode_single_looper);
                break;
        }
    }

    @Override
    public void onPlayStart() {
        //修改UI为暂停样式
        //控件判空，有可能步骤走在控件初始化前
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_stop);
        }
    }

    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.mipmap.play_press);
        }
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
        //Log.d(TAG, "list--> " + list);
        //设置数据到适配器
        if (mTrackPagerAdapter != null) {
            mTrackPagerAdapter.setData(list);
        }
    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {
        mCurrentMode = playMode;
        updatePlayModeBtnImg();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        mDurationBar.setMax(total);
        String totalDuration;
        String currentPosition;
        if (total > 1000 * 60 * 60) {
            //时长超过1小时
            currentPosition = mHourFormat.format(currentProgress);
            totalDuration = mHourFormat.format(total);
        } else {
            currentPosition = mMinFormat.format(currentProgress);
            totalDuration = mMinFormat.format(total);
        }
        //设置数据
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentPosition + "");
        }
        if (mTotalDuration != null) {
            mTotalDuration.setText(totalDuration + "");
        }
        //更新进度seekBar,首先换算百分比
        if (!mIsUserTouchProgressBar) {
            mDurationBar.setProgress(currentProgress);
        }

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        mIsUserSlidePager = false;
        //第一次进PlayerActivity标题没有设置,因为控件是null
        //此时数据已有，定义一个String，给他赋值title，在initView时设置TextView
        this.mTrackTitleText = track.getTrackTitle();
        if (mTrackTitle != null) {
            mTrackTitle.setText(mTrackTitleText + "");
        }
        //当节目改变时，我们获取当前播放中节目的position
        //当前节目改变以后。要修改页面的图片
        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(playIndex, true);//点击按钮使ViewPager切换
        }

    }
}
