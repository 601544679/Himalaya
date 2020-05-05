package com.example.himalaya.presenters;

import android.os.Message;
import android.util.Log;

import com.example.himalaya.interfaces.IRecommendRresenter;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.utils.Constans;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendPresenter implements IRecommendRresenter {
    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private static final String TAG = "RecommendPresenter";

    /**
     * 单例模式
     * 首先构造方法私有化
     */

    private RecommendPresenter() {
    }

    private static RecommendPresenter sImstance = null;

    /**
     * 获取单例对象,下面是懒汉式写法
     *
     * @return
     */
    public static RecommendPresenter getInstance() {
        if (sImstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sImstance == null) {
                    sImstance = new RecommendPresenter();
                }
            }
        }
        return sImstance;
    }

    @Override
    public void getRecommendList() {
        //获取推荐内容
        getRecommend();
    }

    @Override
    public void pull2RefreshMode() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    //通过IRecommendViewCallback去更新UI
    public void registerViewCallback(IRecommendViewCallback callback) {
        //集合没有这个callback就添加进去
        if (mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unregisterViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }

    //获取推荐内容,猜你喜欢
    private void getRecommend() {
        //封装参数
        Map<String, String> map = new HashMap<>();
        //表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, String.valueOf(Constans.RECOMMAND_COUNT));
        CommonRequest.getGuessLikeAlbum(map, new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                if (gussLikeAlbumList != null) {
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    //获取数据，通知UI更新
                    handlerRecommendResult(albumList);
                }

            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "i " + i + "error message -- " + s);
            }
        });
    }

    private void handlerRecommendResult(List<Album> albumList) {
        if (mCallbacks != null) {
            //mCallbacks在RecommendFragment注册接口是添加到了mCallbacks集合
            for (IRecommendViewCallback callback : mCallbacks) {
                callback.onRecommendListLoaded(albumList);//传值
            }
        }
    }
}
