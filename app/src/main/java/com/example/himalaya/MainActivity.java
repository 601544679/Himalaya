package com.example.himalaya;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.os.Bundle;

import com.example.himalaya.adapters.IndicatorAdapter;
import com.example.himalaya.adapters.MainContentAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.utils.LogUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtil.init(this.getPackageName(), true);//初始化,输入包名确定是这个包的Log,true就不显示Log
        initView();
        initEvent();
    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTabClickListener(new IndicatorAdapter.OnIndicatorTabClickListener() {
            @Override
            public void onTabClick(int position) {
                mContentPager.setCurrentItem(position);
            }
        });
    }

    private void initView() {
        mMagicIndicator = findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(getResources().getColor(R.color.main_color));
        //创建indicator适配器
        mIndicatorAdapter = new IndicatorAdapter(MainActivity.this);
        CommonNavigator commonNavigator = new CommonNavigator(MainActivity.this);
        commonNavigator.setAdjustMode(true);//平分布局
        commonNavigator.setAdapter(mIndicatorAdapter);
        //设置要显示的内容

        //Viewpager
        mContentPager = findViewById(R.id.content_pager);
        //创建内容适配器
        MainContentAdapter mainContentAdapter = new MainContentAdapter(getSupportFragmentManager());
        mContentPager.setAdapter(mainContentAdapter);
        //绑定导航栏很viewpager
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mContentPager);
    }
}
