package com.example.himalaya.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionDao implements ISubDao {

    private static final String TAG = "SubscriptionDao";
    private final XimalayaDBHelper mXimalayaDBHelper;
    private ISubDaoCallback mCallback = null;

    private SubscriptionDao() {
        mXimalayaDBHelper = new XimalayaDBHelper(BaseApplication.getAppContext());
    }

    public static SubscriptionDao sSubscriptionDao = null;

    public static SubscriptionDao getInstance() {
        if (sSubscriptionDao == null) {
            synchronized (SubscriptionDao.class) {
                if (sSubscriptionDao == null) {
                    sSubscriptionDao = new SubscriptionDao();
                }
            }
        }
        return sSubscriptionDao;
    }

    @Override
    public void addAlbum(Album album) {
        SQLiteDatabase db = null;
        boolean isAddSuccess = false;
        try {
            db = mXimalayaDBHelper.getWritableDatabase();
            //开启事务
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(Constans.SUB_COVER_URL, album.getCoverUrlLarge());
            values.put(Constans.SUB_TITLE, album.getAlbumTitle());
            values.put(Constans.SUB_DESCRIPTION, album.getAlbumIntro());
            values.put(Constans.SUB_PLAY_COUNT, album.getPlayCount());
            values.put(Constans.SUB_TRACKS_COUNT, album.getIncludeTrackCount());
            values.put(Constans.SUB_AUTHORNAME, album.getAnnouncer().getNickname());
            values.put(Constans.SUB_ALBUMID, album.getId());
            //插入数据
            db.insert(Constans.SUB_TB_NAME, null, values);
            db.setTransactionSuccessful();
            //由于没结束事务，没有通知更新,所以放到finally里面
           /* if (mCallback != null) {
                mCallback.onAddResult(true);
            }*/
            isAddSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isAddSuccess = false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (mCallback != null) {
                mCallback.onAddResult(isAddSuccess);
            }
        }


    }

    /**
     * 删除
     *
     * @param album
     */
    @Override
    public void delAlbum(Album album) {
        SQLiteDatabase db = null;
        boolean isDeleteSuccess = false;
        try {
            db = mXimalayaDBHelper.getWritableDatabase();
            //开启事务
            db.beginTransaction();
            //删除数据
            int delete = db.delete(Constans.SUB_TB_NAME, Constans.SUB_ALBUMID + "=?", new String[]{album.getId() + ""});
            LogUtil.d(TAG, "delete: " + delete);
            db.setTransactionSuccessful();
            isDeleteSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isDeleteSuccess = false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (mCallback != null) {
                mCallback.onDelResult(isDeleteSuccess);
            }
        }
    }

    /**
     * 获取订阅内容
     */
    @Override
    public void listAlbum() {
        SQLiteDatabase db = null;
        List<Album> result = new ArrayList<>();
        try {
            db = mXimalayaDBHelper.getWritableDatabase();
            //开启事务
            db.beginTransaction();
            //删除数据
            Cursor query = db.query(Constans.SUB_TB_NAME, null, null, null, null, null, "_id desc");
            while (query.moveToNext()) {
                Album album = new Album();
                //封面图片
                String coverUrl = query.getString(query.getColumnIndex(Constans.SUB_COVER_URL));
                album.setCoverUrlLarge(coverUrl);
                //标题
                String title = query.getString(query.getColumnIndex(Constans.SUB_TITLE));
                album.setAlbumTitle(title);
                //描述
                String description = query.getString(query.getColumnIndex(Constans.SUB_DESCRIPTION));
                album.setAlbumIntro(description);
                //专辑里面有多少集
                int tracksCount = query.getInt(query.getColumnIndex(Constans.SUB_TRACKS_COUNT));
                album.setIncludeTrackCount(tracksCount);
                //播放数量
                int playCount = query.getInt(query.getColumnIndex(Constans.SUB_PLAY_COUNT));
                album.setPlayCount(playCount);
                //作者
                String authorName = query.getString(query.getColumnIndex(Constans.SUB_AUTHORNAME));
                Announcer announcer = new Announcer();
                announcer.setNickname(authorName);
                album.setAnnouncer(announcer);
                //专辑ID
                int albumId = query.getInt(query.getColumnIndex(Constans.SUB_ALBUMID));
                album.setId(albumId);
                result.add(album);
            }
            //把数据通知出去
            //LogUtil.d(TAG, "delete: " + delete);
            db.setTransactionSuccessful();
            if (mCallback != null) {
                mCallback.onSubListLoaded(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    @Override
    public void registerViewCallback(ISubDaoCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void unregisterViewCallback(ISubDaoCallback callback) {

    }
}
