package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubscriptionCallback {
    /**
     * 调用添加的时候，通知UI更新结果
     */

    void onAddResult(boolean isSuccess);

    /**
     * 调用删除的时候，通知UI更新结果
     */
    void onDeleteResult(boolean isSuccess);

    /**
     * 加载订阅的专辑内容的结果回调
     *
     * @param albums
     */
    void onSubscriptionsLoad(List<Album> albums);
}
