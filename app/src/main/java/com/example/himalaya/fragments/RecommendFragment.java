package com.example.himalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.DetailActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;


import java.util.List;


public class RecommendFragment extends BaseFragment implements IRecommendViewCallback {
    private static final String TAG = "RecommendFragment";
    private RecyclerView mRecommendRv;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;
    private AlbumListAdapter mAlbumListAdapter;

    @Override
    protected View onSubViewLoaded(final LayoutInflater inflater, final ViewGroup container) {
        LogUtil.d(TAG, "onSubViewLoaded");
        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(inflater, container);
            }
        };
        //获取数据
        //getRecommend();用mvp模式
        //获取到逻辑层对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先设置通知接口的注册
        mRecommendPresenter.registerViewCallback(this);//选第四个方法make，然后相关方法实现在下面,就是实现接口
        //获取推荐类,但是内容怎么返回，注册一个接口,就是上面的
        mRecommendPresenter.getRecommendList();//调用这个方法
        //调用UILoader，可以直接返回mUiLoader，但是要先跟父类解绑,因为不允许重复绑定
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                //网络不佳点击重试
                if (mRecommendPresenter != null) {
                    mRecommendPresenter.getRecommendList();
                }
            }
        });

        return mUiLoader;
        // return view;
    }

    private View createSuccessView(LayoutInflater inflater, ViewGroup container) {
        LogUtil.d(TAG, "createSuccessView");
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);
        mRecommendRv = view.findViewById(R.id.recommend_list);
        TwinklingRefreshLayout twinklingRefreshLayout = view.findViewById(R.id.over_scroll_view);
        twinklingRefreshLayout.setPureScrollModeOn();
        mRecommendRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAlbumListAdapter = new AlbumListAdapter();
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
        mAlbumListAdapter.setAlbumListItemClickListener(new AlbumListAdapter.onAlbumListItemClickListener() {
            @Override
            public void onItemClick(int position, Album album) {
                AlbumDetailPresenter.getInstance().setTargetAlbum(album);
                //设置数据，然后registerViewCallback里用 detailViewCallback.onAlbumLoader(mTargetAlbum);传递这个数据
                //在DetailActivity实现DetailViewCallBack接口，实现onAlbumLoader方法，获取到数据，并更新UI
                Intent intent = new Intent(getContext(), DetailActivity.class);
                startActivity(intent);

            }
        });
        mRecommendRv.setAdapter(mAlbumListAdapter);
        return view;
    }

    //IRecommendViewCallback接口回调方法
    @Override
    public void onRecommendListLoaded(List<Album> result) {
        LogUtil.d(TAG, "onRecommendListLoaded");
        //获取推荐内容后，这个方法会被调用
        mAlbumListAdapter.setData(result);
        mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
    }

    //接口回调方法
    @Override
    public void onNetWorkError() {
        LogUtil.d(TAG, "onNetWorkError");
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    //接口回调方法
    @Override
    public void onEmpty() {
        LogUtil.d(TAG, "onEmpty");
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    //接口回调方法
    @Override
    public void onLoading() {
        LogUtil.d(TAG, "onLoading");
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
    }

    @Override
    public void onDestroyView() {
        LogUtil.d(TAG, "onDestroyView");
        super.onDestroyView();
        //取消接口注册,避免内存泄漏,泄露就是无法回收，泄露多了就是内存溢出
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unregisterViewCallback(this);
        }

    }
}
