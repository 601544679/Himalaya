package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;

public interface IAlbumDetailPresenter extends IBasePresenter<IAlbumDetailViewCallback> {

    /**
     * 下拉刷新
     */

    void pull2RefreshMode();


    /**
     * 上拉加载更多
     */

    void loadMore();

    /**
     * 获取专辑详情
     *
     * @param albumId
     * @param page
     */
    void getAlbumDetail(int albumId, int page);
    /*
     *//**
     * 注册UI通知的接口
     *
     * @param detailViewCallback
     *//*
    void registerViewCallback(IAlbumDetailViewCallback detailViewCallback);

    *//**
     * 删除UI通知接口
     *
     * @param detailViewCallback
     *//*
    void unregisterViewCallback(IAlbumDetailViewCallback detailViewCallback);*/
}
