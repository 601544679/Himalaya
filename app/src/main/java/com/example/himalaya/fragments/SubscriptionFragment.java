package com.example.himalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.DetailActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.ISubscriptionCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.SubscriptionPresenter;
import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.ConfirmDialog;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionCallback, AlbumListAdapter.onAlbumListItemLongClickListener {

    private static final String TAG = "SubscriptionFragment";
    private SubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubList;
    private AlbumListAdapter mAlbumListAdapter;
    private TwinklingRefreshLayout mRefreshLayout;
    private Album mCurrentClickAlbum = null;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater inflater, ViewGroup container) {
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_subscription, container, false);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(container.getContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(inflater);
                }

                //复写getEmptyView换一个UI
                @Override
                protected View getEmptyView() {
                    //创建一个新的UI
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipsView = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tipsView.setText(R.string.no_sub_content_tips_text);
                    return emptyView;
                }
            };
        } else {
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }
        //因为用UILoader懒得再写一个FrameLayout，直接把根布局改为FrameLayout
        view.addView(mUiLoader);
        LogUtil.d(TAG, "onSubViewLoaded");
        return view;
    }

    private View createSuccessView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.item_subscription, null, false);
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
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        //实现点击事件
        mAlbumListAdapter.setAlbumListItemClickListener(new AlbumListAdapter.onAlbumListItemClickListener() {
            @Override
            public void onItemClick(int position, Album album) {
                //传值
                AlbumDetailPresenter.getInstance().setTargetAlbum(album);
                //跳转
                startActivity(new Intent(getContext(), DetailActivity.class));
            }
        });
        //实现长按事件
        mAlbumListAdapter.setonAlbumListItemLongClickListener(this);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unregisterViewCallback(this);
            mSubscriptionPresenter = null;
        }
        //取消适配器接口注册
        mAlbumListAdapter.setAlbumListItemClickListener(null);
    }

    @Override
    public void onAddResult(boolean isSuccess) {
        LogUtil.d(TAG, "onAddResult");
    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        //给出取消订阅提示
        Toast.makeText(BaseApplication.getAppContext(), isSuccess ? R.string.cancel_sub_success : R.string.cancel_sub_failed, Toast.LENGTH_SHORT).show();
        LogUtil.d(TAG, "onDeleteResult");
    }

    @Override
    public void onSubscriptionsLoad(List<Album> albums) {
        LogUtil.d(TAG, "onSubscriptionsLoad" + albums);
        if (albums.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        } else {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
        //更新UI
        if (mAlbumListAdapter != null) {
            //Collections.reverse(albums);倒序，不推荐,直接在数据库查询时降序 SubscriptionDao.listAlbum()
            mAlbumListAdapter.setData(albums);
        }
    }

    @Override
    public void onSubFull() {
        Toast.makeText(BaseApplication.getAppContext(), "订阅不能超过" + Constans.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(Album album) {
        this.mCurrentClickAlbum = album;
        Toast.makeText(getContext(), "长按；了", Toast.LENGTH_SHORT).show();
        ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());//必须用getActivity()
        confirmDialog.show();
        confirmDialog.setOnDialogActionClickListener(new ConfirmDialog.OnDialogActionClickListener() {
            @Override
            public void onCancelSubClick() {
                //取消订阅
                if (mCurrentClickAlbum != null || mSubscriptionPresenter != null) {
                    mSubscriptionPresenter.delSubscription(mCurrentClickAlbum);
                }
            }

            @Override
            public void onGiveUPClick() {
                //放弃取消

            }
        });
    }
}
