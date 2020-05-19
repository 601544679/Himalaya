package com.example.himalaya.presenters;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.data.HistoryDao;
import com.example.himalaya.data.IHistoryDao;
import com.example.himalaya.data.IHistoryDaoCallback;
import com.example.himalaya.interfaces.IHistoryCallback;
import com.example.himalaya.interfaces.IHistoryPresenter;
import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 历史记录最多100条，超过了就删除最前面的，再添加
 */
public class HistoryPresenter implements IHistoryPresenter, IHistoryDaoCallback {
    private static final String TAG = "HistoryPresenter";
    private List<IHistoryCallback> mCallbacks = new ArrayList<>();

    private final IHistoryDao mIHistoryDao;
    private List<Track> mCurrentHistories = null;
    private Track mCurrentAddTrack = null;

    private HistoryPresenter() {
        //多态
        LogUtil.d(TAG, "private HistoryPresenter()");
        mIHistoryDao = HistoryDao.getInstance();
        mIHistoryDao.setCallback(this);
        //一开始就获取记录
        listHistories();
    }

    private static HistoryPresenter sHistoryPresenter = null;

    public static HistoryPresenter getHistoryPresenter() {
        LogUtil.d(TAG, "public static HistoryPresenter getHistoryPresenter()");
        if (sHistoryPresenter == null) {
            synchronized (HistoryPresenter.class) {
                if (sHistoryPresenter == null) {
                    sHistoryPresenter = new HistoryPresenter();
                }
            }
        }
        return sHistoryPresenter;
    }

    @Override
    public void listHistories() {
        LogUtil.d(TAG, "listHistories");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                //被观察的内容
                if (mIHistoryDao != null) {
                    mIHistoryDao.listHistories();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private boolean isDoDelAsOutOfSize = false;

    @Override
    public void addHistory(Track track) {
        //判断历史记录是否》=100条
        if (mCurrentHistories != null && mCurrentHistories.size() >= Constans.MAX_HISTORY_COUNT) {
            //先不能添加,先删除最前面的记录
            this.mCurrentAddTrack = track;
            delHistory(mCurrentHistories.get(mCurrentHistories.size() - 1));
            isDoDelAsOutOfSize = true;
        }
        doAddHistory(track);

        LogUtil.d(TAG, "addHistory");
    }

    private void doAddHistory(Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                //被观察的内容
                if (mIHistoryDao != null) {
                    mIHistoryDao.addHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void delHistory(Track track) {
        LogUtil.d(TAG, "delHistory");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                //被观察的内容
                if (mIHistoryDao != null) {
                    mIHistoryDao.delHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void cleanHistories() {
        LogUtil.d(TAG, "cleanHistories");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                //被观察的内容
                if (mIHistoryDao != null) {
                    mIHistoryDao.clearHistory();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void registerViewCallback(IHistoryCallback iHistoryCallback) {
        LogUtil.d(TAG, "registerViewCallback");
        //ui注册过来
        if (!mCallbacks.contains(iHistoryCallback)) {
            mCallbacks.add(iHistoryCallback);
        }
    }

    @Override
    public void unregisterViewCallback(IHistoryCallback iHistoryCallback) {
        //删除
        LogUtil.d(TAG, "unregisterViewCallback");
        mCallbacks.remove(iHistoryCallback);
    }

    @Override
    public void onHistoryAdd(boolean isSuccess) {
        listHistories();
        LogUtil.d(TAG, "onHistoryAdd");
    }

    @Override
    public void onHistoryDel(boolean isSuccess) {
        if (isDoDelAsOutOfSize && mCurrentAddTrack != null) {
            //添加当前数据到数据库
            addHistory(mCurrentAddTrack);
            isDoDelAsOutOfSize = false;
        } else {
            listHistories();
        }
        LogUtil.d(TAG, "onHistoryDel");
    }

    @Override
    public void onHistoriesLoaded(List<Track> tracks) {
        this.mCurrentHistories = tracks;
        //通知UI更新数据
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (IHistoryCallback callback : mCallbacks) {
                    callback.onHistoriesLoaded(tracks);
                }
            }
        });
        LogUtil.d(TAG, "onHistoriesLoaded size  " + tracks.size());
    }

    @Override
    public void onHistoriesClean(boolean isSuccess) {
        listHistories();
        LogUtil.d(TAG, "onHistoriesClean");
    }
}
