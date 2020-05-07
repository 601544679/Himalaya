package com.example.himalaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.interfaces.IPlayerPresenter;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {

    private static final String TAG = "PlayerPresenter";
    private final XmPlayerManager mPlayerManager;
    private List<IPlayerCallback> mIPlayerCallbacks = new ArrayList<>();
    private Track mCurrentTrack;
    private int mCurrentIndex = 0;
    private SharedPreferences mPlayModeSp;
    private XmPlayListControl.PlayMode mCurrentplayMode = PLAY_MODEL_LIST;

    private static final int PLAY_MODEL_LIST_INT = 0;
    private static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    private static final int PLAY_MODEL_RANDOM_INT = 2;
    private static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;
    //sp key and name
    public final static String PLAY_MODE_SP_NAME = "PlayMode";
    public final static String PLAY_MODE_SP_KEY = "currentPlayMode";

    private PlayerPresenter() {
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());
        //广告物料相关接口的注册
        mPlayerManager.addAdsStatusListener(this);//当前界面实现addAdsStatusListener里的方法
        //注册播放器状态相关接口
        mPlayerManager.addPlayerStatusListener(this);
        //记录当前播放模式
        mPlayModeSp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);
    }

    private static PlayerPresenter sPlayerPresenter;

    public static PlayerPresenter getPlayerPresenter() {
        if (sPlayerPresenter == null) {
            synchronized (PlayerPresenter.class) {
                if (sPlayerPresenter == null) {
                    sPlayerPresenter = new PlayerPresenter();
                }
            }
        }
        return sPlayerPresenter;
    }

    //点击节目跳转到播放器界面后，传递节目内容，position
    private boolean isPlayListSet = false;

    public void setPlayList(List<Track> list, int playIndex) {
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list, playIndex);
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);
            mCurrentIndex = playIndex;
        } else {
            Log.d(TAG, "mPlayerManager is null");
        }
    }

    @Override
    public void play() {
        if (isPlayListSet) {
            mPlayerManager.play();
            Log.d(TAG, "getPlayerStatus--- " + mPlayerManager.getPlayerStatus());
        }
    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        //播放前一首
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }
    }

    @Override
    public void playNext() {
        //播放下一首
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentplayMode = mode;
            mPlayerManager.setPlayMode(mode);
            //通知UI更新播放模式
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onPlayModeChange(mode);
            }
            //保存到sp
            SharedPreferences.Editor editor = mPlayModeSp.edit();
            editor.putInt(PLAY_MODE_SP_KEY, getIntByPlayMode(mode));
            editor.commit();
        }
    }

    private int getIntByPlayMode(XmPlayListControl.PlayMode mode) {
        switch (mode) {
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
        }

        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModeByInt(int index) {
        switch (index) {
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
        }
        return PLAY_MODEL_LIST;
    }


    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onListLoaded(playList);
            }
        }
    }

    @Override
    public void playByIndex(int index) {
        //根据index切换播放内容
        mPlayerManager.play(index);
    }

    @Override
    public void seekTo(int progress) {
        //更新播放器进度
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlay() {
        //返回当前是否正在播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void registerViewCallback(IPlayerCallback iPlayerCallback) {
        //解决第一次进PlayerActivity标题没有设置
        iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
        int modeIndex = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        mCurrentplayMode = getModeByInt(modeIndex);
        iPlayerCallback.onPlayModeChange(mCurrentplayMode);
        if (!mIPlayerCallbacks.contains(iPlayerCallback)) {
            mIPlayerCallbacks.add(iPlayerCallback);
        }
    }

    @Override
    public void unregisterViewCallback(IPlayerCallback iPlayerCallback) {
        mIPlayerCallbacks.remove(iPlayerCallback);
    }

    //=========================================广告相关回调 start==================================//
    @Override
    public void onStartGetAdsInfo() {
        //开始获取广告
        Log.d(TAG, "onStartGetAdsInfo");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        //获取广告成功
        Log.d(TAG, "onGetAdsInfo  " + advertisList);
    }

    @Override
    public void onAdsStartBuffering() {
        //广告开始缓冲
        Log.d(TAG, "onAdsStartBuffering");
    }

    @Override
    public void onAdsStopBuffering() {
        //广告结束缓冲
        Log.d(TAG, "onAdsStopBuffering");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        //开始播放广告,advertis是当前播放广告，i是当前播放广告在广告列表的position，索引
        Log.d(TAG, "onStartPlayAds  " + advertis + "  :  " + i);
    }

    @Override
    public void onCompletePlayAds() {
        //广告播放完毕
        Log.d(TAG, "onCompletePlayAds");
    }

    @Override
    public void onError(int i, int i1) {
        //广告播放错误,i错误类型，i1 错误的额外信息
        Log.d(TAG, "onError" + i + "  i1 " + i1);
    }
    //=========================================广告相关回调 end==================================//


    //=========================================播放器状态相关回调 start==================================//
    @Override
    public void onPlayStart() {
        //开始播放
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStart();
        }
        Log.d(TAG, "开始播放+onPlayStart");
    }

    @Override
    public void onPlayPause() {
        //	暂停播放
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayPause();
        }
        Log.d(TAG, "暂停播放+onPlayPause");
    }

    @Override
    public void onPlayStop() {
        //停止播放
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStop();
        }
        Log.d(TAG, "停止播放+onPlayStop");
    }

    @Override
    public void onSoundPlayComplete() {
        //播放完成
        Log.d(TAG, "播放完成+onSoundPlayComplete");
    }

    @Override
    public void onSoundPrepared() {
        //播放器准备完毕
        //获取播放模式
        mPlayerManager.setPlayMode(mCurrentplayMode);
        if (mPlayerManager != null) {
            if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
                //播放器准备好可以播放
                mPlayerManager.play();

            }
        }
        Log.d(TAG, "播放器准备完毕+onSoundPrepared");
    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        //切歌
        //lastModel：上一首model,可能为空
        //curModel：下一首model
        //请通过model中的kind字段来判断是track、radio和schedule；
        //上一首的播放时间请通过lastPlayedMills字段来获取;
        Log.d(TAG, "切歌+onSoundSwitch ： PlayableModel 》");
        if (lastModel != null) {
            Log.d(TAG, " lastModel 。kind> " + lastModel.getKind());

        }
        Log.d(TAG, " curModel 。kind> " + curModel.getKind());
        //curModel,当前播放内容,通过getKind()获取数据类型
        //track表示是track类型
        //第一种写法,不推荐,如果后台把字段"track"改为其他字段，直接GG
        // if ("track".equals(curModel.getKind())) {
        //     Track currentTrack = (Track) curModel;
        //     Log.d(TAG, "currentTrack title" + currentTrack.getTrackTitle());
        // }
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track) curModel;
            mCurrentTrack = currentTrack;
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            }
            Log.d(TAG, "currentTrack title" + mCurrentTrack);
        }

    }

    @Override
    public void onBufferingStart() {
        //开始缓冲
        Log.d(TAG, "开始缓冲+onBufferingStart");
    }

    @Override
    public void onBufferingStop() {
        //结束缓冲
        Log.d(TAG, "结束缓冲+onBufferingStop");
    }

    @Override
    public void onBufferProgress(int i) {
        //缓冲进度回调
        Log.d(TAG, "缓冲进度回调+onBufferProgress");
    }

    @Override
    public void onPlayProgress(int current, int duration) {
        //播放进度回调,current当前进度,duration总时长
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onProgressChange(current, duration);
        }
        Log.d(TAG, "播放进度回调+onPlayProgress current: " + current + "  duration: " + duration);
    }

    @Override
    public boolean onError(XmPlayerException e) {
        //播放器错误(这里面返回的错误code没有参考价值,如果配置正确但是却播放不了,
        // 最大的可能就是网络导致的,请注意log中的"PlayError"字段 ,
        // 如果responseCode != 200 说明就是网络问题,请换个网络重试下看看) code=612 表示没有播放地址
        Log.d(TAG, "播放器错误+onError  " + e);
        return false;
    }
    //=========================================播放器状态相关回调 end==================================//
}
