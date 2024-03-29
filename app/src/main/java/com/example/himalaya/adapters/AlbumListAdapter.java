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
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.InnerHolder> {
    private List<Album> mData = new ArrayList<>();
    private static final String TAG = "AlbumListAdapter";
    private onAlbumListItemClickListener mAlbumListItemClickListener = null;
    private onAlbumListItemLongClickListener mAlbumListItemLongClickListener = null;

    @NonNull
    @Override
    public AlbumListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LogUtil.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumListAdapter.InnerHolder holder, final int position) {
        LogUtil.d(TAG, "onBindViewHolder");
        //设置数据
        holder.itemView.setTag(position);//大佬教的
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlbumListItemClickListener != null) {
                    mAlbumListItemClickListener.onItemClick(position, mData.get(position));
                }
                LogUtil.d(TAG, "holder.itemView onClick--> " + v.getTag());//v.getTag()代替position
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mAlbumListItemLongClickListener != null) {
                    mAlbumListItemLongClickListener.onItemLongClick(mData.get(position));
                }
                //true表示消费掉该事件,处理了
                return true;
            }
        });
        Glide.with(holder.itemView.getContext()).load(mData.get(position).getCoverUrlLarge()).into(holder.mAlbumCoverIv);
        LogUtil.d(TAG, "pic--> " + mData.get(position).getCoverUrlMiddle());
        holder.mAlbumTitleTv.setText(mData.get(position).getAlbumTitle() + "");
        holder.mAlbumDescriptionTv.setText(mData.get(position).getAlbumIntro() + "");
        holder.mAlbumPlayCountTv.setText(mData.get(position).getPlayCount() + "");
        holder.mAlbumContentCountTv.setText(mData.get(position).getIncludeTrackCount() + "");
    }

    @Override
    public int getItemCount() {
        LogUtil.d(TAG, "getItemCount");
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setData(List<Album> albumList) {
        LogUtil.d(TAG, "setData");
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
            LogUtil.d(TAG, "InnerHolder");
            mAlbumCoverIv = itemView.findViewById(R.id.album_cover);
            mAlbumTitleTv = itemView.findViewById(R.id.album_title_tv);
            mAlbumDescriptionTv = itemView.findViewById(R.id.album_description_tv);
            mAlbumPlayCountTv = itemView.findViewById(R.id.album_play_count);
            mAlbumContentCountTv = itemView.findViewById(R.id.album_content_size);
        }
    }

    public void setAlbumListItemClickListener(onAlbumListItemClickListener listener) {
        LogUtil.d(TAG, "setonRecommendItemClickListener");
        this.mAlbumListItemClickListener = listener;
    }

    public interface onAlbumListItemClickListener {
        void onItemClick(int position, Album album);
    }

    public void setonAlbumListItemLongClickListener(onAlbumListItemLongClickListener listener) {
        LogUtil.d(TAG, "setonRecommendItemClickListener");
        this.mAlbumListItemLongClickListener = listener;
    }

    /**
     * 长按接口
     */
    public interface onAlbumListItemLongClickListener {
        void onItemLongClick(Album album);
    }
}
