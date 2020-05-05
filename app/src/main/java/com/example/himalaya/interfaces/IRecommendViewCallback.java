package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface IRecommendViewCallback {
    /**
     * 获取推荐内容的结果
     *
     * @param result
     */
    void onRecommendListLoaded(List<Album> result);

    /**
     * 加载更多
     */
    void onLoaderMore(List<Album> result);

    /**
     * 下拉加载更多
     */
    void onRefreshMore(List<Album> result);
}