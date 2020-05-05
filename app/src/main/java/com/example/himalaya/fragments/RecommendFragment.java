package com.example.himalaya.fragments;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.adapters.RecommendListAdapter;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.utils.Constans;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallback {
    private static final String TAG = "RecommendFragment";
    private RecyclerView mRecommendRv;
    private RecommendPresenter mRecommendPresenter;

    @Override
    protected View onSubViewLoaded(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);
        mRecommendRv = view.findViewById(R.id.recommend_list);
        //获取数据
        //getRecommend();用mvp模式
        //获取到逻辑层对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先设置通知接口的注册
        mRecommendPresenter.registerViewCallback(this);//选第四个方法make，然后相关方法实现在下面,就是实现接口
        //获取推荐类,但是内容怎么返回，注册一个接口,就是上面的
        mRecommendPresenter.getRecommendList();//调用这个方法
        return view;
    }


    @Override
    public void onRecommendListLoaded(List<Album> result) {
        //获取推荐内容后，这个方法会被调用
        mRecommendRv.setLayoutManager(new LinearLayoutManager(getContext()));
        RecommendListAdapter adapter = new RecommendListAdapter();
        adapter.setData(result);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            //设置间距
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mRecommendRv.setAdapter(adapter);
    }

    @Override
    public void onLoaderMore(List<Album> result) {

    }

    @Override
    public void onRefreshMore(List<Album> result) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口注册,避免内存泄漏,泄露就是无法回收，泄露多了就是内存溢出
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unregisterViewCallback(this);
        }

    }
}
