package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback {
    /**
     * 专辑详情内容加载
     *
     * @param tracks
     */
    void OnDetailListLoader(List<Track> tracks);

    /**
     * 网络错误
     */
    void onNetWorkError(int errorCode, String errorMessage);

    /**
     * 传递Album
     */
    void onAlbumLoader(Album album);

    /**
     * 上拉加载更多的结果
     * size>0加载成功
     * 否则加载失败
     *
     * @param size
     */
    void onLoaderMoreFinished(int size);


    /**
     * 上拉加载更多的结果
     * size>0加载成功
     * 否则加载失败
     *
     * @param size
     */
    void onRefreshFinished(int size);
}
