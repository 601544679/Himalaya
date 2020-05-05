package com.example.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.himalaya.R;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class RecommendListAdapter extends RecyclerView.Adapter<RecommendListAdapter.InnerHolder> {
    private List<Album> mData = new ArrayList<>();

    @NonNull
    @Override
    public RecommendListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendListAdapter.InnerHolder holder, int position) {
        //设置数据
        holder.itemView.setTag(position);//大佬教的
        Glide.with(holder.itemView.getContext()).load(mData.get(position).getCoverUrlMiddle()).into(holder.mAlbumCoverIv);
        holder.mAlbumTitleTv.setText(mData.get(position).getAlbumTitle() + "");
        holder.mAlbumDescriptionTv.setText(mData.get(position).getAlbumIntro() + "");
        holder.mAlbumPlayCountTv.setText(mData.get(position).getPlayCount() + "");
        holder.mAlbumContentCountTv.setText(mData.get(position).getIncludeTrackCount()+"");
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setData(List<Album> albumList) {
        if (mData != null) {
            mData.clear();
            mData.addAll(albumList);
        }
        notifyDataSetChanged();
    }


    public class InnerHolder extends RecyclerView.ViewHolder {
        private final ImageView mAlbumCoverIv;
        private final TextView mAlbumTitleTv;
        private final TextView mAlbumDescriptionTv;
        private final TextView mAlbumPlayCountTv;
        private final TextView mAlbumContentCountTv;


        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mAlbumCoverIv = itemView.findViewById(R.id.album_cover);
            mAlbumTitleTv = itemView.findViewById(R.id.album_title_tv);
            mAlbumDescriptionTv = itemView.findViewById(R.id.album_description_tv);
            mAlbumPlayCountTv = itemView.findViewById(R.id.album_play_count);
            mAlbumContentCountTv = itemView.findViewById(R.id.album_content_size);
        }
    }

}
