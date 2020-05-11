package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;

/**
 * 订阅一般有上限,比如不能超过100
 */
public interface ISubscriptionPresenter extends IBasePresenter<ISubscriptionCallback> {
    /**
     * 添加订阅
     *
     * @param album
     */
    void addSubscription(Album album);

    /**
     * 删除订阅
     *
     * @param album
     */
    void delSubscription(Album album);

    /**
     * 获取订阅列表
     */
    void getSubscription();

    /**
     * 判断当前专辑是否已经收藏订阅
     *
     * @param album
     */
    boolean isSub(Album album);
}
