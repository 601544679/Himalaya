package com.example.himalaya.presenters;

import android.util.Log;

import com.example.himalaya.interfaces.IAlbumDetailPresenter;
import com.example.himalaya.interfaces.IAlbumDetailViewCallback;
import com.example.himalaya.utils.Constans;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {
    private Album mTargetAlbum = null;
    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();
    private static final String TAG = "AlbumDetailPresenter";

    //懒汉模式,构造方法私有化
    private AlbumDetailPresenter() {
    }

    private static AlbumDetailPresenter sInstance = null;

    public static AlbumDetailPresenter getInstance() {
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

    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        //根据页码和专辑id获取列表
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.ALBUM_ID, String.valueOf(albumId));
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, String.valueOf(page));
        map.put(DTransferConstants.PAGE_SIZE, String.valueOf(Constans.COUNT_DEFAULT));
        CommonRequest.getTracks(map, new IDataCallBack<TrackList>() {


            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null) {
                    List<Track> tracks = trackList.getTracks();
                    Log.d(TAG, "trackList -->" + tracks);
                    handlerAlbumDetailResult(tracks);
                }

            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.d(TAG, "errorCode --> " + errorCode + "  errorMessage --> " + errorMessage);
                handlerError(errorCode, errorMessage);
            }
        });

    }

    /**
     * 发生网络错误，通知UI
     *
     * @param errorCode
     * @param errorMessage
     */
    private void handlerError(int errorCode, String errorMessage) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onNetWorkError(errorCode, errorMessage);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.OnDetailListLoader(tracks);
        }
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        if (!mCallbacks.contains(detailViewCallback)) {
            mCallbacks.add(detailViewCallback);
            if (mTargetAlbum != null) {
                detailViewCallback.onAlbumLoader(mTargetAlbum);
            }

        }
    }

    @Override
    public void unregisterViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        mCallbacks.remove(detailViewCallback);
    }


    public void setTargetAlbum(Album targetAlbum) {
        this.mTargetAlbum = targetAlbum;
    }
}
