package com.example.himalaya.fragments;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.ISubscriptionCallback;
import com.example.himalaya.presenters.SubscriptionPresenter;
import com.example.himalaya.utils.LogUtil;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.Collections;
import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionCallback {

    private static final String TAG = "SubscriptionFragment";
    private SubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubList;
    private AlbumListAdapter mAlbumListAdapter;
    private TwinklingRefreshLayout mRefreshLayout;

    @Override
    protected View onSubViewLoaded(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_subscription, container, false);
        mSubList = view.findViewById(R.id.sub_list);
        mRefreshLayout = view.findViewById(R.id.over_scroll_view);
        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadmore(false);
        mSubList.setLayoutManager(new LinearLayoutManager(BaseApplication.getAppContext()));
        mAlbumListAdapter = new AlbumListAdapter();
        mSubList.setAdapter(mAlbumListAdapter);
        mSubList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(BaseApplication.getAppContext(), 5);
                outRect.bottom = UIUtil.dip2px(BaseApplication.getAppContext(), 5);
                outRect.left = UIUtil.dip2px(BaseApplication.getAppContext(), 5);
                outRect.right = UIUtil.dip2px(BaseApplication.getAppContext(), 5);
            }
        });
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
        //获取数据
        mSubscriptionPresenter.getSubscription();


        LogUtil.d(TAG, "onSubViewLoaded");
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unregisterViewCallback(this);
            mSubscriptionPresenter = null;
        }
    }

    @Override
    public void onAddResult(boolean isSuccess) {
        LogUtil.d(TAG, "onAddResult");
    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        LogUtil.d(TAG, "onDeleteResult");
    }

    @Override
    public void onSubscriptionsLoad(List<Album> albums) {
        LogUtil.d(TAG, "onSubscriptionsLoad" + albums);
        //更新UI
        if (mAlbumListAdapter != null) {
            //Collections.reverse(albums);倒序，不推荐,直接在数据库查询时降序 SubscriptionDao.listAlbum()
            mAlbumListAdapter.setData(albums);
        }
    }
}
