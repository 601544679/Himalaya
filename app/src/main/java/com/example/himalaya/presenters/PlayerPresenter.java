package com.example.himalaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.himalaya.api.XimalayaApi;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.interfaces.IPlayerPresenter;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
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
    public static final int DEFAULT_PLAY_INDEX = 0;
    private int mCurrentIndex = DEFAULT_PLAY_INDEX;
    private SharedPreferences mPlayModeSp;
    private XmPlayListControl.PlayMode mCurrentPlayMode = PLAY_MODEL_LIST;
    private boolean mIsReverse = false;
    private static final int PLAY_MODEL_LIST_INT = 0;
    private static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    private static final int PLAY_MODEL_RANDOM_INT = 2;
    private static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;
    //sp key and name
    public final static String PLAY_MODE_SP_NAME = "PlayMode";
    public final static String PLAY_MODE_SP_KEY = "currentPlayMode";
    private int mCurrentProgressPosition=0;
    private int mProgressDuration=0;

    private PlayerPresenter() {
        LogUtil.d(TAG, "private PlayerPresenter()");
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
        LogUtil.d(TAG, " public static PlayerPresenter getPlayerPresenter()");
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
        LogUtil.d(TAG, "setPlayList list: " + list + " playIndex= " + playIndex);
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list, playIndex);
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);
            mCurrentIndex = playIndex;
        } else {
            LogUtil.d(TAG, "mPlayerManager is null");
        }
    }

    @Override
    public void play() {
        LogUtil.d(TAG, "play");
        if (isPlayListSet) {
            mPlayerManager.play();
            LogUtil.d(TAG, "getPlayerStatus--- " + mPlayerManager.getPlayerStatus());
        }
    }

    @Override
    public void pause() {
        LogUtil.d(TAG, "pause");
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        LogUtil.d(TAG, "playPre");
        //播放前一首
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }
    }

    @Override
    public void playNext() {
        LogUtil.d(TAG, "playNext");
        //播放下一首
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        LogUtil.d(TAG, "switchPlayMode :" + mode);
        if (mPlayerManager != null) {
            mCurrentPlayMode = mode;
            mPlayerManager.setPlayMode(mode);
            //通知UI更新播放模式
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onPlayModeChange(mode);
                LogUtil.d(TAG, "switchPlayMode : iPlayerCallback.onPlayModeChange--> " + mode);
            }
            //保存到sp
            SharedPreferences.Editor editor = mPlayModeSp.edit();
            editor.putInt(PLAY_MODE_SP_KEY, getIntByPlayMode(mode));
            editor.commit();
        }
    }

    private int getIntByPlayMode(XmPlayListControl.PlayMode mode) {
        LogUtil.d(TAG, "getIntByPlayMode");
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
        LogUtil.d(TAG, "getModeByInt");
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
        LogUtil.d(TAG, "getPlayList");
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                LogUtil.d(TAG, "getPlayList--iPlayerCallback.onListLoaded -- " + playList);
                iPlayerCallback.onListLoaded(playList);
            }
        }
    }

    @Override
    public void playByIndex(int index) {
        LogUtil.d(TAG, "playByIndex -- " + index);
        //根据index切换播放内容
        mPlayerManager.play(index);
    }

    @Override
    public void seekTo(int progress) {
        LogUtil.d(TAG, "seekTo progress-- " + progress);
        //更新播放器进度
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        LogUtil.d(TAG, "isPlaying");
        //返回当前是否正在播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversePlayList() {
        LogUtil.d(TAG, "reversePlayList");
        //popwindow播放器列表翻转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        //反转成功改变boolean
        mIsReverse = !mIsReverse;
        //第一个参数，播放列表，第二个参数，开始播放的下标
        //新下标等于size()-1-position
        mCurrentIndex = playList.size() - 1 - mCurrentIndex;
        mPlayerManager.setPlayList(playList, mCurrentIndex);
        //更新UI
        //getCurrSound()获取当前的播放model，可能是track、radio或者schedule中的节目,要根据类型做强转
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();//节目有很多种，获取我们要的Track
        LogUtil.d(TAG, "mCurrentTrack" + mCurrentTrack);
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            //播放列表加载完成
            iPlayerCallback.onListLoaded(playList);
            //更新PlayerActivity标题,图片，内容
            iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            //通知UI更新播放列表的顺序和图标
            iPlayerCallback.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long id) {
        LogUtil.d(TAG, "playByAlbumId " + id);
        //1.要获取专辑的内容
        XimalayaApi ximalayaApi = XimalayaApi.getXimalayaApi();
        ximalayaApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //2.把内容设置给播放器
                List<Track> tracks = trackList.getTracks();
                if (tracks != null && tracks.size() > 0) {
                    mPlayerManager.setPlayList(tracks, DEFAULT_PLAY_INDEX);
                    //设置UI更新的信息
                    isPlayListSet = true;
                    mCurrentTrack = tracks.get(DEFAULT_PLAY_INDEX);
                    mCurrentIndex = DEFAULT_PLAY_INDEX;
                    for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                        LogUtil.d(TAG, "playByAlbumId  " +
                                "iPlayerCallback.onTrackUpdate---" + "mCurrentTrack " + mCurrentTrack
                                + "  --- " + mCurrentIndex);
                        iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(BaseApplication.getAppContext(), "请求失败", Toast.LENGTH_SHORT).show();
            }
        }, (int) id, 1);
        //3.播放
    }

    @Override
    public void registerViewCallback(IPlayerCallback iPlayerCallback) {
        LogUtil.d(TAG, "registerViewCallback");
        //解决第一次进PlayerActivity标题没有设置
        LogUtil.d(TAG, "registerViewCallback  " +
                "iPlayerCallback.onTrackUpdate---" + "mCurrentTrack " + mCurrentTrack
                + "  --- " + mCurrentIndex);
        iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
        iPlayerCallback.onProgressChange(mCurrentProgressPosition,mProgressDuration);
        //MainActivity点击播放后，改变PlayerActivity的播放状态
        handlePlayState(iPlayerCallback);
        //sp里面取
        int modeIndex = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        mCurrentPlayMode = getModeByInt(modeIndex);
        iPlayerCallback.onPlayModeChange(mCurrentPlayMode);
        if (!mIPlayerCallbacks.contains(iPlayerCallback)) {
            mIPlayerCallbacks.add(iPlayerCallback);
        }
    }

    private void handlePlayState(IPlayerCallback iPlayerCallback) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        //根据状态调用
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerCallback.onPlayStart();
        } else {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void unregisterViewCallback(IPlayerCallback iPlayerCallback) {
        LogUtil.d(TAG, "unregisterViewCallback");
        mIPlayerCallbacks.remove(iPlayerCallback);
    }

    //=========================================广告相关回调 start==================================//
    @Override
    public void onStartGetAdsInfo() {
        //开始获取广告
        LogUtil.d(TAG, "onStartGetAdsInfo");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        //获取广告成功
        LogUtil.d(TAG, "onGetAdsInfo  " + advertisList);
    }

    @Override
    public void onAdsStartBuffering() {
        //广告开始缓冲
        LogUtil.d(TAG, "onAdsStartBuffering");
    }

    @Override
    public void onAdsStopBuffering() {
        //广告结束缓冲
        LogUtil.d(TAG, "onAdsStopBuffering");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        //开始播放广告,advertis是当前播放广告，i是当前播放广告在广告列表的position，索引
        LogUtil.d(TAG, "onStartPlayAds  " + advertis + "  :  " + i);
    }

    @Override
    public void onCompletePlayAds() {
        //广告播放完毕
        LogUtil.d(TAG, "onCompletePlayAds");
    }

    @Override
    public void onError(int i, int i1) {
        //广告播放错误,i错误类型，i1 错误的额外信息
        LogUtil.d(TAG, "onError" + i + "  i1 " + i1);
    }
    //=========================================广告相关回调 end==================================//


    //=========================================播放器状态相关回调 start==================================//
    @Override
    public void onPlayStart() {
        //开始播放
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStart();
        }
        LogUtil.d(TAG, "开始播放+onPlayStart");
    }

    @Override
    public void onPlayPause() {
        //	暂停播放
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayPause();
        }
        LogUtil.d(TAG, "暂停播放+onPlayPause");
    }

    @Override
    public void onPlayStop() {
        //停止播放
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStop();
        }
        LogUtil.d(TAG, "停止播放+onPlayStop");
    }

    @Override
    public void onSoundPlayComplete() {
        //播放完成
        LogUtil.d(TAG, "播放完成+onSoundPlayComplete");
    }

    @Override
    public void onSoundPrepared() {
        //播放器准备完毕
        //获取播放模式
        mPlayerManager.setPlayMode(mCurrentPlayMode);
        if (mPlayerManager != null) {
            if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
                //播放器准备好可以播放
                mPlayerManager.play();

            }
        }
        LogUtil.d(TAG, "播放器准备完毕+onSoundPrepared");
    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        //切歌
        //lastModel：上一首model,可能为空
        //curModel：下一首model
        //请通过model中的kind字段来判断是track、radio和schedule；
        //上一首的播放时间请通过lastPlayedMills字段来获取;
        LogUtil.d(TAG, "切歌+onSoundSwitch ： PlayableModel 》");
        if (lastModel != null) {
            LogUtil.d(TAG, " lastModel 。kind> " + lastModel.getKind());

        }
        LogUtil.d(TAG, " curModel 。kind> " + curModel.getKind());
        //curModel,当前播放内容,通过getKind()获取数据类型
        //track表示是track类型
        //第一种写法,不推荐,如果后台把字段"track"改为其他字段，直接GG
        // if ("track".equals(curModel.getKind())) {
        //     Track currentTrack = (Track) curModel;
        //     LogUtil.d(TAG, "currentTrack title" + currentTrack.getTrackTitle());
        // }
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track) curModel;
            mCurrentTrack = currentTrack;
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            }
            LogUtil.d(TAG, "currentTrack title" + mCurrentTrack);
        }

    }

    @Override
    public void onBufferingStart() {
        //开始缓冲
        LogUtil.d(TAG, "开始缓冲+onBufferingStart");
    }

    @Override
    public void onBufferingStop() {
        //结束缓冲
        LogUtil.d(TAG, "结束缓冲+onBufferingStop");
    }

    @Override
    public void onBufferProgress(int i) {
        //缓冲进度回调
        LogUtil.d(TAG, "缓冲进度回调+onBufferProgress");
    }

    @Override
    public void onPlayProgress(int currPos, int duration) {
        //播放进度回调,current当前进度,duration总时长
        //这2个值要保存，因为从MainActivity播放栏跳转到PlayerActivity时，没有保存值，时长是00:00
        this.mCurrentProgressPosition=currPos;
        this.mProgressDuration=duration;
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onProgressChange(currPos, duration);
        }
        LogUtil.d(TAG, "播放进度回调+onPlayProgress currPos: " + currPos + "  duration: " + duration);
    }

    @Override
    public boolean onError(XmPlayerException e) {
        //播放器错误(这里面返回的错误code没有参考价值,如果配置正确但是却播放不了,
        // 最大的可能就是网络导致的,请注意log中的"PlayError"字段 ,
        // 如果responseCode != 200 说明就是网络问题,请换个网络重试下看看) code=612 表示没有播放地址
        LogUtil.d(TAG, "播放器错误+onError  " + e);
        return false;
    }


    //=========================================播放器状态相关回调 end==================================//
    //判断是否有播放列表
    public boolean hasPlayList() {
        //List<Track> playList = mPlayerManager.getPlayList();
        //return playList == null || playList.size() == 0;
        return isPlayListSet;


    }
}
