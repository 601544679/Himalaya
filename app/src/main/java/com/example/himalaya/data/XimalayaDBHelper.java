package com.example.himalaya.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;


public class XimalayaDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "XimalayaDBHelper";

    public XimalayaDBHelper(Context context) {
        super(context, Constans.DB_NAME, null, Constans.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.d(TAG, "onCreate");
        //创建数据表
        String subTbSql = "create table " + Constans.SUB_TB_NAME + " (" +
                Constans.SUB_ID + " integer primary key autoincrement , " +
                Constans.SUB_COVER_URL + " varchar," +
                Constans.SUB_TITLE + " varchar ," +
                Constans.SUB_DESCRIPTION + " varchar," +
                Constans.SUB_PLAY_COUNT + " integer," +
                Constans.SUB_TRACKS_COUNT + " integer," +
                Constans.SUB_AUTHOR_NAME + " varchar ," +
                Constans.SUB_ALBUM_ID + " integer)";
        db.execSQL(subTbSql);
        //创建历史记录表
        String histoTbSql = "create table " + Constans.HISTORY_TB_NAME + " (" +
                Constans.HISTORY_ID + " integer primary key autoincrement , " +
                Constans.HISTORY_TRACK_ID + " integer," +
                Constans.HISTORY_TITLE + " varchar ," +
                Constans.HISTORY_PLAY_COUNT + " integer," +
                Constans.HISTORY_DURATION + " integer," +
                Constans.HISTORY_COVER + " varchar," +
                Constans.HISTORY_AUTHOR + " varchar," +
                Constans.HISTORY_UPDATE_TIME + " integer)";
        db.execSQL(histoTbSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
