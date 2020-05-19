package com.example.himalaya.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class HistoryDao implements IHistoryDao {

    private static final String TAG = "HistoryDao";
    private final XimalayaDBHelper mHelper;
    private IHistoryDaoCallback mCallback = null;
    //解决,attempt to re-open an already-closed object: SQLiteDatabase,加锁
    private Object mLock = new Object();

    private HistoryDao() {
        mHelper = new XimalayaDBHelper(BaseApplication.getAppContext());
        LogUtil.d(TAG, "HistoryDao");
    }

    public static HistoryDao sHistoryDao = null;

    public static HistoryDao getInstance() {
        if (sHistoryDao == null) {
            synchronized (SubscriptionDao.class) {
                if (sHistoryDao == null) {
                    sHistoryDao = new HistoryDao();
                }
            }
        }
        return sHistoryDao;
    }

    @Override
    public void setCallback(IHistoryDaoCallback callback) {
        this.mCallback = callback;
        LogUtil.d(TAG, "setCallback");
    }

    @Override
    public void addHistory(Track track) {
        synchronized (mLock) {
            LogUtil.d(TAG, "addHistory");
            //若有将要添加的历史记录已经存在，就删除原来的
            SQLiteDatabase db = null;
            boolean isSuccess = false;
            try {
                db = mHelper.getWritableDatabase();
                //删除
                int delete = db.delete(Constans.HISTORY_TB_NAME, Constans.HISTORY_TRACK_ID + "=?", new String[]{track.getDataId() + ""});
                LogUtil.d(TAG, "delete result -- " + delete);
                db.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(Constans.HISTORY_TRACK_ID, track.getDataId());
                values.put(Constans.HISTORY_TITLE, track.getTrackTitle());
                values.put(Constans.HISTORY_PLAY_COUNT, track.getPlayCount());
                values.put(Constans.HISTORY_DURATION, track.getDuration());
                values.put(Constans.HISTORY_COVER, track.getCoverUrlLarge());
                values.put(Constans.HISTORY_UPDATE_TIME, track.getUpdatedAt());
                values.put(Constans.HISTORY_AUTHOR, track.getAnnouncer().getNickname());
                db.insert(Constans.HISTORY_TB_NAME, null, values);
                db.setTransactionSuccessful();
                isSuccess = true;
            } catch (Exception e) {
                isSuccess = false;
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoryAdd(isSuccess);
                }
            }
        }
    }

    @Override
    public void delHistory(Track track) {
        synchronized (mLock) {
            LogUtil.d(TAG, "delHistory");
            SQLiteDatabase db = null;
            boolean isDeleteSuccess = false;
            try {
                db = mHelper.getWritableDatabase();
                db.beginTransaction();
                db.delete(Constans.HISTORY_TB_NAME, Constans.HISTORY_TRACK_ID + "=?", new String[]{track.getDataId() + ""});
                db.setTransactionSuccessful();
                isDeleteSuccess = true;
            } catch (Exception e) {
                isDeleteSuccess = false;
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoryDel(isDeleteSuccess);
                }
            }

        }
    }

    @Override
    public void clearHistory() {
        synchronized (mLock) {
            LogUtil.d(TAG, "clearHistory");
            SQLiteDatabase db = null;
            boolean isDeleteSuccess = false;
            try {
                db = mHelper.getWritableDatabase();
                db.beginTransaction();
                db.delete(Constans.HISTORY_TB_NAME, null, null);
                db.setTransactionSuccessful();
                isDeleteSuccess = true;
            } catch (Exception e) {
                isDeleteSuccess = false;
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
                if (mCallback != null) {
                    mCallback.onHistoriesClean(isDeleteSuccess);
                }
            }
        }
    }

    @Override
    public void listHistories() {
        synchronized (mLock) {
            LogUtil.d(TAG, "listHistories");
            SQLiteDatabase db = null;
            List<Track> history = new ArrayList<>();
            try {
                db = mHelper.getWritableDatabase();
                db.beginTransaction();
                Cursor query = db.query(Constans.HISTORY_TB_NAME, null, null, null, null, null, "_id desc");
                while (query.moveToNext()) {
                    Track track = new Track();
                    int trackID = query.getInt(query.getColumnIndex(Constans.HISTORY_TRACK_ID));
                    track.setDataId(trackID);
                    String title = query.getString(query.getColumnIndex(Constans.HISTORY_TITLE));
                    track.setTrackTitle(title);
                    int duration = query.getInt(query.getColumnIndex(Constans.HISTORY_DURATION));
                    track.setDuration(duration);
                    int playCount = query.getInt(query.getColumnIndex(Constans.HISTORY_PLAY_COUNT));
                    track.setPlayCount(playCount);
                    long updateTime = query.getLong(query.getColumnIndex(Constans.HISTORY_UPDATE_TIME));
                    track.setUpdatedAt(updateTime);
                    String historyCover = query.getString(query.getColumnIndex(Constans.HISTORY_COVER));
                    track.setCoverUrlLarge(historyCover);
                    track.setCoverUrlSmall(historyCover);
                    track.setCoverUrlMiddle(historyCover);
                    String historyAuthor = query.getString(query.getColumnIndex(Constans.HISTORY_AUTHOR));
                    Announcer announcer = new Announcer();
                    announcer.setNickname(historyAuthor);
                    track.setAnnouncer(announcer);
                    history.add(track);
                }
                query.close();
                db.setTransactionSuccessful();
                //通知出去,
                //因此把回调操作放到db.close()之后
          /*  if (mCallback != null) {
                mCallback.onHistoriesLoaded(history);
            }*/
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();

                }
                if (mCallback != null) {
                    mCallback.onHistoriesLoaded(history);
                }
            }
        }
    }
}
