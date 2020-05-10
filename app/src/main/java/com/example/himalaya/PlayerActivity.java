package com.example.himalaya;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.himalaya.adapters.PlayerTrackPagerAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
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

    private ImageView mPlayListBtn;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterBgAnimator;
    private ValueAnimator mOutBgAnimator;
    public final int BG_ANIMATOR_DURATION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate");
        setContentView(R.layout.activity_player);
        initView();
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        //注册
        mPlayerPresenter.registerViewCallback(this);
        //界面初始化后才获取数据
        mPlayerPresenter.getPlayList();
        /**
         *   看Log当我们从MainActivity下面播放栏点击跳转到PlayerActivity时，
         *   为什么获取的数据是对的，代码也只是一个简单的跳转没有传数据，因为之前我们一级一级进入时，从MainActivity
         *   到DetailActity再到PlayerActivity，进入到PlayerActivity后调用onSoundSwitch方法，获取到了Track和position,
         *   并作为PlayerPresenter的成员变量赋值，保存起来，上面我们注册接口时，直接读取成员变量的值，同时调用onTrackUpdate方法,
         *   简单来说前面进入播放界面是已经保存了值，并且进入播放界面后，同步更新了MainActivity的播放栏状态，所以直接Intent就可以进入对应PlayerActivity
         */

        initEvent();
        //这里调用startPlay()播放器可能没准备好导致进入不会播放，因此在PlayerPresenter里的onSoundPrepared() 调用startPlay();
        /* startPlay();*/
        //设置背景透明度改变时的动画
        initBgAnimation();
    }

    private void initBgAnimation() {
        LogUtil.d(TAG, "initBgAnimation");
        //设置变化范围
        mEnterBgAnimator = ValueAnimator.ofFloat(1.0f, 0.7f);
        mEnterBgAnimator.setDuration(BG_ANIMATOR_DURATION);
        mEnterBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //上面的变化范围1~0.7，每次变化是会回调这个方法
                LogUtil.d(TAG, "value -- > " + animation.getAnimatedValue());
                //打开popupwindow时背景透明
                updateBgAlpha((Float) animation.getAnimatedValue());
            }
        });
        //退出
        mOutBgAnimator = ValueAnimator.ofFloat(0.7f, 1.0f);
        mOutBgAnimator.setDuration(BG_ANIMATOR_DURATION);
        mOutBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //上面的变化范围1~0.7，每次变化是会回调这个方法
                LogUtil.d(TAG, "value -- > " + animation.getAnimatedValue());
                //打开popupwindow时背景透明
                updateBgAlpha((Float) animation.getAnimatedValue());
            }
        });
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
        LogUtil.d(TAG, "startPlay");
        if (mPlayerPresenter != null) {
            mPlayerPresenter.play();
        }
        //我们注册了接口mPlayerPresenter.registerViewCallback(this);
        //在onPlayStart（）实现对应的UI变化
    }*/
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        LogUtil.d(TAG, "initEvent");
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
                //页面选中时，切换播放内容,通过判断mIsUserSlidePager，是用户滑动才执行这个方法，避免点击按钮切换也调用这个方法
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
        mPlayListBtn.setOnClickListener(this);
        //popupWindow消失时更改为原来的透明度
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mOutBgAnimator.start();
            }
        });
        //播放列表里的item被点击
        mSobPopWindow.setPlayListItemClickListener(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mPlayerPresenter.playByIndex(position);
                LogUtil.d(TAG, "mSobPopWindow.setPlayListItemClickListener   --- " + position);
            }
        });
        //点击popupwindow切换播放模式
        mSobPopWindow.setPlayListActionListener(new SobPopWindow.PlayListActionListener() {
            @Override
            public void onPlayModeClick() {
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //切换顺序，逆序
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();
                }
            }
        });
    }


    private void initView() {
        LogUtil.d(TAG, "initView");
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
        mPlayListBtn = findViewById(R.id.player_list);
        mSobPopWindow = new SobPopWindow();
    }

    @Override
    public void onClick(View v) {
        if (v == mControlBtn) {
            //如果现在是正在播放就暂停，暂停就继续播放
            if (mPlayerPresenter.isPlaying()) {
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
            switchPlayMode();
        } else if (v == mPlayListBtn) {
            //以全屏为基准，从底部弹出来
            mSobPopWindow.showAtLocation(mPlayListBtn, Gravity.BOTTOM, 0, 0);
            //修改背景透明度时增加动画效果
            mEnterBgAnimator.start();
        }
    }

    private void switchPlayMode() {
        LogUtil.d(TAG, "switchPlayMode");
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        //修改播放模式
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(playMode);
            // mCurrentMode = playMode;
        }
    }

    //修改背景透明度
    public void updateBgAlpha(float alpha) {
        LogUtil.d(TAG, "updateBgAlpha");
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        //设置透明度
        window.setAttributes(attributes);
    }

    private void updatePlayModeBtnImg() {
        LogUtil.d(TAG, "updatePlayModeBtnImg");
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
        LogUtil.d(TAG, "onPlayStart");
        //修改UI为暂停样式
        //控件判空，有可能步骤走在控件初始化前
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_stop);
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause");
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop");
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
        LogUtil.d(TAG, "onListLoaded");
        //LogUtil.d(TAG, "list--> " + list);
        //设置数据到适配器
        if (mTrackPagerAdapter != null) {
            mTrackPagerAdapter.setData(list);
        }
        if (mSobPopWindow != null) {
            //数据回来后给到popupwindow
            mSobPopWindow.setListData(list);
        }
    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {
        LogUtil.d(TAG, "onPlayModeChange");
        mCurrentMode = playMode;
        //更新popwindow里的播放模式
        mSobPopWindow.updatePlayMode(playMode);
        updatePlayModeBtnImg();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        LogUtil.d(TAG, "onProgressChange");
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
        LogUtil.d(TAG, "onTrackUpdate: " + "track: " + track + " index - " + playIndex);
        mIsUserSlidePager = false;
        //第一次进PlayerActivity标题没有设置,因为控件是null
        //此时数据已有，定义一个String，给他赋值title，在initView时设置TextView
        if (track == null) {
            LogUtil.d(TAG, "onTrackUpdate --> track null");
            return;//退出该方法
        }
        this.mTrackTitleText = track.getTrackTitle();
        if (mTrackTitle != null) {
            mTrackTitle.setText(mTrackTitleText + "");
        }
        //当节目改变时，我们获取当前播放中节目的position
        //当前节目改变以后。要修改页面的图片
        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(playIndex, true);//点击按钮使ViewPager切换
        }
        //设置播放列表的当前播放位置
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(playIndex);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {
        LogUtil.d(TAG, "updateListOrder");
        //不用！第一次点击不变
        mSobPopWindow.updateOrderIcon(!isReverse);
    }
}
