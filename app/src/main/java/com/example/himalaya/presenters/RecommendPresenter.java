package com.example.himalaya.presenters;

import com.example.himalaya.data.XimalayaApi;
import com.example.himalaya.interfaces.IRecommendPresenter;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.List;

public class RecommendPresenter implements IRecommendPresenter {
    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private static final String TAG = "RecommendPresenter";
    private List<Album> mCurrentRecommend = new ArrayList<>();
    private List<Album> mRecommendList;

    /**
     * 单例模式
     * 首先构造方法私有化
     */

    private RecommendPresenter() {
        LogUtil.d(TAG, " private RecommendPresenter()");
    }

    private static RecommendPresenter sInstance = null;

    /**
     * 获取单例对象,下面是懒汉式写法
     *
     * @return
     */
    public static RecommendPresenter getInstance() {
        LogUtil.d(TAG, "public static RecommendPresenter getInstance()");
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取当前推荐专辑
     * 使用前要判空
     *
     * @return
     */
    public List<Album> getCurrentRecommend() {
        LogUtil.d(TAG, "getCurrentRecommend()");
        return mCurrentRecommend;
    }

    @Override
    public void getRecommendList() {
        LogUtil.d(TAG, "getRecommendList()");
        //获取推荐内容
        getRecommend();
    }


    @Override
    //通过IRecommendViewCallback去更新UI
    public void registerViewCallback(IRecommendViewCallback callback) {
        LogUtil.d(TAG, "registerViewCallback");
        //集合没有这个callback就添加进去
        if (mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unregisterViewCallback(IRecommendViewCallback callback) {
        LogUtil.d(TAG, "unregisterViewCallback");
        if (mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }

    //获取推荐内容,猜你喜欢
    private void getRecommend() {
        LogUtil.d(TAG, "getRecommend");
        //如果内容不为空直接用
        if (mRecommendList != null && mRecommendList.size() > 0) {
            LogUtil.d(TAG, "mRecommendList -- > from memory");
            handlerRecommendResult(mRecommendList);
            return;//结束当前方法
        }
        //封装参数
        updateLoading();
        XimalayaApi ximalayaApi = XimalayaApi.getXimalayaApi();
        ximalayaApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                if (gussLikeAlbumList != null) {
                    LogUtil.d(TAG, "mRecommendList -- > from network");
                    mRecommendList = gussLikeAlbumList.getAlbumList();
                    //获取数据，通知UI更新
                    handlerRecommendResult(mRecommendList);
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "error " + i + "error message -- " + s);
                handlerError();
            }
        });

    }

    private void handlerError() {
        LogUtil.d(TAG, "handlerError");
        if (mCallbacks != null) {
            for (IRecommendViewCallback callback : mCallbacks) {
                callback.onNetWorkError();
            }
        }
    }

    private void handlerRecommendResult(List<Album> albumList) {
        LogUtil.d(TAG, "handlerRecommendResult");
        if (albumList != null) {
            //测试内容为空
            //albumList.clear();
            if (albumList.size() == 0) {
                if (mCallbacks != null) {
                    for (IRecommendViewCallback callback : mCallbacks) {
                        callback.onEmpty();
                    }
                }
            } else {
                //mCallbacks在RecommendFragment注册接口是添加到了mCallbacks集合
                for (IRecommendViewCallback callback : mCallbacks) {
                    //类似于接口传值，调用set方法
                    callback.onRecommendListLoaded(albumList);//传值
                }
                this.mCurrentRecommend = albumList;//给MainActivity获取专辑列表
            }
        }
    }

    //发起请求后调用显示正在加载
    private void updateLoading() {
        LogUtil.d(TAG, "updateLoading");
        for (IRecommendViewCallback callback : mCallbacks) {
            callback.onLoading();
        }
    }
}
