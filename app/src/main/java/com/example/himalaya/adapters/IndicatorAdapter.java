package com.example.himalaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.example.himalaya.R;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

public class IndicatorAdapter extends CommonNavigatorAdapter {
    private final Context mContext;
    private final String[] mTitles;
    private OnIndicatorTabClickListener mOnIndicatorTabClickListener = null;

    public IndicatorAdapter(Context context) {
        mTitles = context.getResources().getStringArray(R.array.indicator_title);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        if (mTitles != null) {
            return mTitles.length;
        }
        return 0;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {
        //创建View
        ColorTransitionPagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
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
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        linePagerIndicator.setColors(Color.WHITE);
        return linePagerIndicator;
    }

    public void setOnIndicatorTabClickListener(OnIndicatorTabClickListener listener) {
        this.mOnIndicatorTabClickListener = listener;
    }

    public interface OnIndicatorTabClickListener {
        void onTabClick(int position);
    }
}
