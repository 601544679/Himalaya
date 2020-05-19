package com.example.himalaya.presenters;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.data.ISubDaoCallback;
import com.example.himalaya.data.SubscriptionDao;
import com.example.himalaya.interfaces.ISubscriptionCallback;
import com.example.himalaya.interfaces.ISubscriptionPresenter;
import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SubscriptionPresenter implements ISubscriptionPresenter, ISubDaoCallback {

    private static final String TAG = "SubscriptionPresenter";
    private final SubscriptionDao mSubscriptionDao;
    private Map<Long, Album> mData = new HashMap<>();
    private List<ISubscriptionCallback> mCallbacks = new ArrayList<>();

    private SubscriptionPresenter() {
        LogUtil.d(TAG, "private SubscriptionPresenter()");
        mSubscriptionDao = SubscriptionDao.getInstance();
        mSubscriptionDao.registerViewCallback(this);
    }

    private void listSubscriptions() {
        LogUtil.d(TAG, "listSubscriptions");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                //只调用,不处理结果
                // 获取订阅数据
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.listAlbum();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private static SubscriptionPresenter sInstance = null;

    public static SubscriptionPresenter getInstance() {
        LogUtil.d(TAG, " public static SubscriptionPresenter getInstance()");
        if (sInstance == null) {
            synchronized (SubscriptionPresenter.class) {
                if (sInstance == null) {
                    sInstance = new SubscriptionPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void addSubscription(final Album album) {
        LogUtil.d(TAG, " addSubscription");
        //判断当前订阅数量不能超过100个
        if (mData.size() >= Constans.MAX_SUB_COUNT) {
            //提示
            for (ISubscriptionCallback callback : mCallbacks) {
                callback.onSubFull();
            }
            return;
        }
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.addAlbum(album);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void delSubscription(final Album album) {
        LogUtil.d(TAG, " delSubscription");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                if (mSubscriptionDao != null) {
                    mSubscriptionDao.delAlbum(album);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void getSubscription() {
        LogUtil.d(TAG, " getSubscription");
        listSubscriptions();
    }

    @Override
    public boolean isSub(Album album) {
        LogUtil.d(TAG, " isSub");
        Album result = mData.get(album.getId());
        //不为空表示已订阅
        return result != null;//不等于空返回false
    }

    @Override
    public void registerViewCallback(ISubscriptionCallback iSubscriptionCallback) {
        LogUtil.d(TAG, " registerViewCallback");
        if (!mCallbacks.contains(iSubscriptionCallback)) {
            mCallbacks.add(iSubscriptionCallback);
        }
    }

    @Override
    public void unregisterViewCallback(ISubscriptionCallback iSubscriptionCallback) {
        LogUtil.d(TAG, " unregisterViewCallback");
        mCallbacks.remove(iSubscriptionCallback);
    }

    @Override
    public void onAddResult(final boolean isSuccess) {
        LogUtil.d(TAG, " onAddResult");
        //订阅成功回调
        listSubscriptions();
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionCallback callback : mCallbacks) {
                    callback.onAddResult(isSuccess);
                }
            }
        });
    }

    @Override
    public void onDelResult(final boolean isSuccess) {
        LogUtil.d(TAG, " onDelResult");
        //删除订阅回调
        listSubscriptions();
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionCallback callback : mCallbacks) {
                    callback.onDeleteResult(isSuccess);
                }
            }
        });
    }

    @Override
    public void onSubListLoaded(final List<Album> result) {
        LogUtil.d(TAG, " onSubListLoaded");
        //加载数据回调
        mData.clear();
        for (Album album : result) {
            mData.put(album.getId(), album);
        }
        //通知UI更新
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (ISubscriptionCallback callback : mCallbacks) {
                    callback.onSubscriptionsLoad(result);
                }
            }
        });
    }
}
