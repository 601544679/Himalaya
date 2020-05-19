package com.example.himalaya.presenters;

import com.example.himalaya.data.XimalayaApi;
import com.example.himalaya.interfaces.IAlbumDetailPresenter;
import com.example.himalaya.interfaces.IAlbumDetailViewCallback;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {
    private Album mTargetAlbum = null;
    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();
    private static final String TAG = "AlbumDetailPresenter";
    private List<Track> mTracks = new ArrayList<>();
    //当前专辑ID
    private int mCurrentAlbumId = -1;
    //当前页码
    private int mCurrentPageIndex = 0;

    //懒汉模式,构造方法私有化
    private AlbumDetailPresenter() {
        LogUtil.d(TAG, " private AlbumDetailPresenter() ");
    }

    private static AlbumDetailPresenter sInstance = null;

    public static AlbumDetailPresenter getInstance() {
        LogUtil.d(TAG, " public static AlbumDetailPresenter getInstance() ");
        if (sInstance == null) {
            synchronized (AlbumDetailPresenter.class) {
                if (sInstance == null) {
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void pull2RefreshMode() {

    }

    @Override
    public void loadMore() {
        LogUtil.d(TAG, " loadMore ");
        //加载更多内容
        mCurrentPageIndex++;
        //true，结果会追加到列表后方
        doLoaded(true);

    }

    //boolean isLoadMore判断是否加载到源列表后面，就是上拉加载更多，跟下拉加载2中情况
    private void doLoaded(final boolean isLoadMore) {
        LogUtil.d(TAG, " doLoaded ");
        //根据页码和专辑id获取列表
        XimalayaApi ximalayaApi = XimalayaApi.getXimalayaApi();
        ximalayaApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null) {
                    List<Track> tracks = trackList.getTracks();
                    LogUtil.d(TAG, "trackList -->" + tracks);
                    if (isLoadMore) {
                        //上拉加载更多,结果放到后面
                        mTracks.addAll(mTracks.size(), tracks);
                        //mTracks.addAll(tracks);可以直接这样写，默认是加载到后面
                        int size = tracks.size();
                        handlerLoaderMoreResult(size);
                    } else {
                        //下拉加载更多，结果放到前面
                        mTracks.addAll(0, tracks);
                    }
                    handlerAlbumDetailResult(mTracks);
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                //加载失败，要回退
                if (isLoadMore) {
                    mCurrentPageIndex--;
                }
                LogUtil.d(TAG, "errorCode --> " + errorCode + "  errorMessage --> " + errorMessage);
                handlerError(errorCode, errorMessage);
            }
        }, mCurrentAlbumId, mCurrentPageIndex);

    }

    /**
     * 处理加载更多的结果
     *
     * @param size
     */
    private void handlerLoaderMoreResult(int size) {
        LogUtil.d(TAG, " handlerLoaderMoreResult ");
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onLoaderMoreFinished(size);
            //传数据，界面层实现接口取得数据
        }
    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        LogUtil.d(TAG, " getAlbumDetail ");
        mTracks.clear();
        //保存进入界面后传进来的albumId,page
        this.mCurrentAlbumId = albumId;
        this.mCurrentPageIndex = page;
        //根据页码和专辑id获取列表
        doLoaded(false);

    }

    /**
     * 发生网络错误，通知UI
     *
     * @param errorCode
     * @param errorMessage
     */
    private void handlerError(int errorCode, String errorMessage) {
        LogUtil.d(TAG, " handlerError ");
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onNetWorkError(errorCode, errorMessage);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        LogUtil.d(TAG, " handlerAlbumDetailResult ");
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.OnDetailListLoader(tracks);
        }
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        LogUtil.d(TAG, " registerViewCallback ");
        if (!mCallbacks.contains(detailViewCallback)) {
            mCallbacks.add(detailViewCallback);
            if (mTargetAlbum != null) {
                detailViewCallback.onAlbumLoader(mTargetAlbum);
            }

        }
    }

    @Override
    public void unregisterViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        LogUtil.d(TAG, " unregisterViewCallback ");
        mCallbacks.remove(detailViewCallback);
    }


    public void setTargetAlbum(Album targetAlbum) {
        //获取值，并赋值到当前的全局变量
        LogUtil.d(TAG, " setTargetAlbum ");
        this.mTargetAlbum = targetAlbum;
    }
}
