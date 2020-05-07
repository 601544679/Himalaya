package com.example.himalaya.base;

import com.example.himalaya.interfaces.IAlbumDetailViewCallback;

//T泛型,M一样
public interface IBasePresenter<T> {
    /**
     * 注册UI回调接口
     *
     * @param t
     */
    void registerViewCallback(T t);

    /**
     * 取消回调接口
     *
     *
     */
    void unregisterViewCallback(T t);
}
