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

import com.example.himalaya.PlayerActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.TrackListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.IHistoryCallback;
import com.example.himalaya.presenters.HistoryPresenter;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.ConfirmCheckBoxDialog;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class HistoryFragment extends BaseFragment implements IHistoryCallback, TrackListAdapter.ItemClickListener, TrackListAdapter.ItemLongClickListener {

    private static final String TAG = "HistoryFragment";
    private UILoader mUiLoader;
    private RecyclerView mHistoryList;
    private TrackListAdapter mTrackListAdapter;
    private HistoryPresenter mHistoryPresenter;
    private Track mCurrentClickHistoryItem = null;

    @Override
    protected View onSubViewLoaded(LayoutInflater inflater, ViewGroup container) {
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_history, container, false);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(BaseApplication.getAppContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }

                @Override
                protected View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView textView = emptyView.findViewById(R.id.empty_view_tips_tv);
                    textView.setText("没有历史记录，快去收听节目吧！");
                    return emptyView;

                }
            };
        } else {
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }
        view.addView(mUiLoader);
        mHistoryPresenter = HistoryPresenter.getHistoryPresenter();
        //注册接口
        mHistoryPresenter.registerViewCallback(this);
        //界面改为加载中
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        //获取数据
        mHistoryPresenter.listHistories();
        LogUtil.d(TAG, "onSubViewLoaded");
        return view;
    }

    private View createSuccessView(ViewGroup container) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_history, container, false);
        mHistoryList = view.findViewById(R.id.history_list);
        TwinklingRefreshLayout twinklingRefreshLayout = view.findViewById(R.id.over_scroll_view);
        twinklingRefreshLayout.setEnableRefresh(false);
        twinklingRefreshLayout.setEnableLoadmore(false);
        twinklingRefreshLayout.setEnableOverScroll(true);
        mHistoryList.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mHistoryList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            //设置间距
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mTrackListAdapter = new TrackListAdapter();
        mHistoryList.setAdapter(mTrackListAdapter);
        mTrackListAdapter.setItemClickListener(this);
        mTrackListAdapter.setItemLongClickListener(this);
        LogUtil.d(TAG, "createSuccessView");
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHistoryPresenter != null) {
            mHistoryPresenter.unregisterViewCallback(this);
        }
        LogUtil.d(TAG, "onDestroyView");
    }

    @Override
    public void onHistoriesLoaded(List<Track> tracks) {
        if (tracks.size() == 0 || tracks == null) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        } else {
            //更新数据
            mTrackListAdapter.setData(tracks);
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
            LogUtil.d(TAG, "onHistoriesLoaded");
        }
    }

    @Override
    public void ItemClick(List<Track> list, int position) {
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(list, position);
        startActivity(new Intent(getContext(), PlayerActivity.class));
        LogUtil.d(TAG, "history jump to Player");
    }

    @Override
    public void ItemLongClick(Track track) {
        this.mCurrentClickHistoryItem = track;
        ConfirmCheckBoxDialog dialog = new ConfirmCheckBoxDialog(getActivity());
        // dialog.setContent("删除该历史记录吗？", "删除记录");
        Toast.makeText(getContext(), "长按", Toast.LENGTH_SHORT).show();
        dialog.show();
        dialog.setOnDialogActionClickListener(new ConfirmCheckBoxDialog.OnDialogActionClickListener() {
            @Override
            public void onCancelClick() {
                //不用做
            }

            @Override
            public void onConfirmClick(boolean checked) {
                //删除
                if (mHistoryPresenter != null && mCurrentClickHistoryItem != null) {
                    if (checked) {
                        mHistoryPresenter.cleanHistories();
                    } else {
                        mHistoryPresenter.delHistory(mCurrentClickHistoryItem);
                    }
                }
            }
        });
    }
}
