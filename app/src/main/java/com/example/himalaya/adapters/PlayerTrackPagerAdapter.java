package com.example.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.himalaya.R;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayerTrackPagerAdapter extends PagerAdapter {
    private static final String TAG = "PlayerTrackPagerAdapter";
    private List<Track> mTrackList = new ArrayList<>();

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LogUtil.d(TAG, "PlayerTrackPagerAdapter");
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_track_pager, container, false);
        container.addView(view);
        //设置数据
        ImageView item = view.findViewById(R.id.track_pager_item);
        Glide.with(container.getContext()).load(mTrackList.get(position).getCoverUrlLarge()).into(item);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        LogUtil.d(TAG, "destroyItem");
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        LogUtil.d(TAG, "getCount");
        return mTrackList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        LogUtil.d(TAG, "isViewFromObject");
        return view == object;
    }

    public void setData(List<Track> list) {
        LogUtil.d(TAG, "setData");
        mTrackList.clear();
        mTrackList.addAll(list);
        notifyDataSetChanged();
    }


}
