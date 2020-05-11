package com.example.himalaya.data;

import com.example.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface ISubDaoCallback {
    /**
     * 添加结果回调方法
     *
     * @param isSuccess
     */
    void onAddResult(boolean isSuccess);

    /**
     * 删除结果回调方法
     *
     * @param isSuccess
     */
    void onDelResult(boolean isSuccess);

    /**
     * 加载结果
     *
     * @param result
     */
    void onSubListLoaded(List<Album> result);
}
