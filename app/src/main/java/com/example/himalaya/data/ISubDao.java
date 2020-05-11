package com.example.himalaya.data;

import com.example.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;

public interface ISubDao extends IBasePresenter<ISubDaoCallback> {
    /**
     * 添加专辑订阅
     *
     * @param album
     */
    void addAlbum(Album album);

    /**
     * 删除专辑订阅
     *
     * @param album
     */
    void delAlbum(Album album);

    /**
     * 获取订阅内容
     */
    void listAlbum();
}
