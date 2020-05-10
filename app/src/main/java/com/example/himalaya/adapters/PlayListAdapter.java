package com.example.himalaya.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.InnerHolder> {
    private static final String TAG = "PlayListAdapter";
    private List<Track> mTrackList = new ArrayList<>();
    private int playingIndex = 0;
    private SobPopWindow.PlayListItemClickListener mOnItemClickListener = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LogUtil.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        LogUtil.d(TAG, "onBindViewHolder");
        TextView trackTitleTv = holder.itemView.findViewById(R.id.track_title_tv);
        trackTitleTv.setText(mTrackList.get(position).getTrackTitle() + "");
        trackTitleTv.setTextColor(playingIndex == position ?
                BaseApplication.getAppContext().getResources().getColor(R.color.second_color) :
                BaseApplication.getAppContext().getResources().getColor(R.color.play_list_text_color));
        //显示当前播放状态
        ImageView playingIconView = holder.itemView.findViewById(R.id.play_icon_iv);
        playingIconView.setVisibility(playingIndex == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "holder.itemView.setOnClickListener  >  " + position + " -title- " + mTrackList.get(position).getTrackTitle());
                if (mOnItemClickListener != null) {
                    //返回数据给SobPopWindow里的PlayListItemClickListener接口里的onItemClick(int position);
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        LogUtil.d(TAG, "getItemCount");
        return mTrackList.size();
    }

    public void setData(List<Track> data) {
        LogUtil.d(TAG, "setData");
        mTrackList.clear();
        mTrackList.addAll(data);
        notifyDataSetChanged();
    }

    public void setCurrentPlayPosition(int playIndex) {
        LogUtil.d(TAG, "setCurrentPlayPosition");
        this.playingIndex = playIndex;
        notifyDataSetChanged();
    }


    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            LogUtil.d(TAG, "InnerHolder");
        }
    }

    public void setOnItemClickListener(SobPopWindow.PlayListItemClickListener listItemClickListener) {
        LogUtil.d(TAG, "setOnItemClickListener");
        this.mOnItemClickListener = listItemClickListener;
    }

}
