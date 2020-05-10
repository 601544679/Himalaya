package com.example.himalaya.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;


public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";
    private static Handler sHandler = null;
    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        CommonRequest mXimalaya = CommonRequest.getInstanse();
        if (DTransferConstants.isRelease) {
            String mAppSecret = "8646d66d6abe2efd14f2891f9fd1c8af";
            mXimalaya.setAppkey("9f9ef8f10bebeaa83e71e62f935bede8");
            mXimalaya.setPackid("com.app.test.android");
            mXimalaya.init(this, mAppSecret);
        } else {
            String mAppSecret = "0a09d7093bff3d4947a5c4da0125972e";
            mXimalaya.setAppkey("f4d8f65918d9878e1702d49a8cdf0183");
            mXimalaya.setPackid("com.ximalaya.qunfeng");
            mXimalaya.init(this, mAppSecret);
        }
        //初始化播放器
        XmPlayerManager.getInstance(BaseApplication.this).init();
        LogUtil.init(this.getPackageName(), false);//初始化,输入包名确定是这个包的Log,true就不显示Log
        sHandler = new Handler();
        sContext = getBaseContext();
        LogUtil.d(TAG, "onCreate");
    }

    public static Context getAppContext() {
        LogUtil.d(TAG, "getAppContext");
        return sContext;
    }

    //线程
    public static Handler getHandler() {
        LogUtil.d(TAG, "getHandler");
        return sHandler;
    }
}
