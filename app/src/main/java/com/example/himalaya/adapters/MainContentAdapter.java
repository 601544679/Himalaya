package com.example.himalaya.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.example.himalaya.utils.FragmentCreator;
import com.example.himalaya.utils.LogUtil;

public class MainContentAdapter extends FragmentPagerAdapter {

    private static final String TAG = "MainContentAdapter";

    public MainContentAdapter(@NonNull FragmentManager fm) {
        super(fm);
        LogUtil.d(TAG, "setOnIndicatorTabClickListener");
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        LogUtil.d(TAG, "getItem");
        return FragmentCreator.getFragment(position);
    }

    @Override
    public int getCount() {
        LogUtil.d(TAG, "getCount");
        return FragmentCreator.PAGER_COUNT;
    }
}
