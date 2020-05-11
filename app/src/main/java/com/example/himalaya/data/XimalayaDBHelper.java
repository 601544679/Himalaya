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
                Constans.SUB_AUTHORNAME + " varchar ," +
                Constans.SUB_ALBUMID + " integer)";
        db.execSQL(subTbSql);
       /*
        for (int i = 0; i < 1000; i++) {
            db.insert(Constans.SUB_TB_NAME, null, values);
            //db.execSQL(aa);
        }*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
