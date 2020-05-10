package com.example.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class SearchRecommendAdapter extends RecyclerView.Adapter<SearchRecommendAdapter.InnerHolder> {
    private List<QueryResult> mData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_recommend, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        TextView text = holder.itemView.findViewById(R.id.search_recommend_item);
        text.setText(mData.get(position).getKeyword());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.OnItemClick(mData.get(position).getKeyword());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    //设置数据
    public void setData(List<QueryResult> keyWordList) {
        mData.clear();
        mData.addAll(keyWordList);
        notifyDataSetChanged();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    //点击自动搜索
    public interface OnItemClickListener {
        void OnItemClick(String keyword);
    }
}
