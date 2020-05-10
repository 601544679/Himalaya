package com.example.himalaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.example.himalaya.R;
import com.example.himalaya.utils.LogUtil;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

public class IndicatorAdapter extends CommonNavigatorAdapter {
    private static final String TAG = "IndicatorAdapter";
    private final Context mContext;
    private final String[] mTitles;
    private OnIndicatorTabClickListener mOnIndicatorTabClickListener = null;

    public IndicatorAdapter(Context context) {
        LogUtil.d(TAG, "IndicatorAdapter");
        mTitles = context.getResources().getStringArray(R.array.indicator_title);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        LogUtil.d(TAG, "getCount");
        if (mTitles != null) {
            return mTitles.length;
        }
        return 0;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {
        LogUtil.d(TAG, "getTitleView");
        //创建View
        ColorTransitionPagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
        colorTransitionPagerTitleView.setTextSize(20);
        //设置一般状态下是灰色
        colorTransitionPagerTitleView.setNormalColor(Color.parseColor("#aaffffff"));
        //选中情况下是白色
        colorTransitionPagerTitleView.setSelectedColor(Color.parseColor("#ffffff"));
        colorTransitionPagerTitleView.setText(mTitles[index]);
        colorTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mViewPager.setCurrentItem(index);
                if (mOnIndicatorTabClickListener != null) {
                    mOnIndicatorTabClickListener.onTabClick(index);
                }
            }
        });
        return colorTransitionPagerTitleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LogUtil.d(TAG, "getIndicator");
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        linePagerIndicator.setColors(Color.WHITE);
        return linePagerIndicator;
    }

    public void setOnIndicatorTabClickListener(OnIndicatorTabClickListener listener) {
        LogUtil.d(TAG, "setOnIndicatorTabClickListener");
        this.mOnIndicatorTabClickListener = listener;
    }

    public interface OnIndicatorTabClickListener {
        void onTabClick(int position);
    }
}
