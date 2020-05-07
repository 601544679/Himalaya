package com.example.himalaya.views;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.himalaya.R;

public class LoadingView extends AppCompatImageView {
    //旋转的角度
    private int rotateDegree = 0;
    private boolean mNeedRotate = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置图片
        setImageResource(R.mipmap.loading);
    }

    @Override
    protected void onAttachedToWindow() {
        //绑定到windows时，就是在主界面
        super.onAttachedToWindow();
        mNeedRotate = true;
        post(new Runnable() {
            @Override
            public void run() {
                rotateDegree += 25;
                rotateDegree = rotateDegree <= 360 ? rotateDegree : 0;
                invalidate();
                //invalidate方法和postInvalidate方法都是用于进行View的刷新，invalidate方法应用在UI线程中，
                // 而postInvalidate方法应用在非UI线程中，用于将线程切换到UI线程，postInvalidate方法最后调用的也是invalidate方法。

                if (mNeedRotate) {
                    postDelayed(this, 100);//当前runnable继续
                }

            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        //解绑时，就是不在主页面显示
        super.onDetachedFromWindow();
        mNeedRotate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         *  第一个参数是旋转角度
         *  第二个参数旋转的x坐标
         *  第三个参数旋转的y坐标
         *  getWidth() / 2, getHeight() / 2中心旋转
         */

        canvas.rotate(rotateDegree, getWidth() / 2, getHeight() / 2);
        super.onDraw(canvas);
    }
}
