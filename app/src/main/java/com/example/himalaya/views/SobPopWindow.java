package com.example.himalaya.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.adapters.PlayListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class SobPopWindow extends PopupWindow {

    private static final String TAG = "SobPopWindow";
    private final View mPopView;
    private TextView mCloseBtn;
    private RecyclerView mTrackList;
    private PlayListAdapter mPlayListAdapter;
    private PlayListItemClickListener mPlayListItemClickListener = null;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private LinearLayout mPlayModeContainer;
    private PlayListActionListener mPlayListActionListener = null;
    private LinearLayout mOrderBtnContainer;
    private ImageView mOrderIcon;
    private TextView mOrderText;

    //设置属性在构造方法设置,外面可能无效
    public SobPopWindow() {
        //设置宽高
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置背景透明
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //点击外部消失
        setOutsideTouchable(true);//根据视频单独设置setOutsideTouchable没效果要先设置setBackgroundDrawable，但是不设置也可以点击关闭
        //载进来View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list, null);
        setContentView(mPopView);
        //设置进入，退出的动画
        setAnimationStyle(R.style.pop_animation);
        LogUtil.d(TAG, "SobPopWindow");
        initView();
        initEvent();
    }

    private void initView() {
        LogUtil.d(TAG, "initView");
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        mTrackList = mPopView.findViewById(R.id.play_list_rv);
        mTrackList.setLayoutManager(new LinearLayoutManager(BaseApplication.getAppContext()));
        mPlayListAdapter = new PlayListAdapter();
        mTrackList.setAdapter(mPlayListAdapter);
        //播放模式相关
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        mOrderBtnContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);
    }

    private void initEvent() {
        LogUtil.d(TAG, "initEvent");
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击关闭，popupwindow消失
                dismiss();
                //两种写法SobPopWindow.this.dismiss();
            }
        });
        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                if (mPlayListActionListener != null) {
                    mPlayListActionListener.onPlayModeClick();
                }
            }
        });
        mOrderBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换顺序，倒序播放
                if (mPlayListActionListener != null) {
                    mPlayListActionListener.onOrderClick();
                }
            }
        });
    }

    //设置一个方法传递数据进来
    public void setListData(List<Track> data) {
        LogUtil.d(TAG, "setListData");
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);
        }
    }

    public void setCurrentPlayPosition(int playIndex) {
        LogUtil.d(TAG, "setCurrentPlayPosition");
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(playIndex);
            mTrackList.scrollToPosition(playIndex);
        }
    }

    public void setPlayListItemClickListener(PlayListItemClickListener listItemClickListener) {
        LogUtil.d(TAG, "setPlayListItemClickListener");
        mPlayListAdapter.setOnItemClickListener(listItemClickListener);
        //设置接口，返回数据到接口，在PlayerActivity调用可以获得数据
        LogUtil.d(TAG, "setPlayListItemClickListener");
    }

    /**
     * 更新播放模式
     *
     * @param playMode
     */
    public void updatePlayMode(XmPlayListControl.PlayMode playMode) {
        LogUtil.d(TAG, "updatePlayMode");
        updatePlayModeBtnImg(playMode);
    }

    //isOrder顺序的意思
    public void updateOrderIcon(boolean isOrder) {
        LogUtil.d(TAG, "updateOrderIcon");
        mOrderIcon.setImageResource(isOrder ? R.drawable.selector_play_mode_list_order : R.drawable.selector_play_mode_list_revers);
        mOrderText.setText(BaseApplication.getAppContext().getString(isOrder ? R.string.order_text : R.string.revers_text));//前缀相同可以把判断isOrder放到不同的地方开始判断
    }

    private void updatePlayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        LogUtil.d(TAG, "updatePlayModeBtnImg");
        int textId = R.string.play_mode_order_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                mPlayModeIv.setImageResource(R.drawable.selector_play_mode_list_order);
                textId = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_RANDOM:
                mPlayModeIv.setImageResource(R.drawable.selector_play_mode_random);
                textId = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                mPlayModeIv.setImageResource(R.drawable.selector_play_mode_list_order_looper);
                textId = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                mPlayModeIv.setImageResource(R.drawable.selector_play_mode_single_looper);
                textId = R.string.play_mode_single_play_text;
                break;
        }
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemClickListener {
        void onItemClick(int position);
    }

    public void setPlayListActionListener(PlayListActionListener listModeClickListener) {
        LogUtil.d(TAG, "setPlayListActionListener");
        this.mPlayListActionListener = listModeClickListener;
    }

    public interface PlayListActionListener {
        //切换播放模式
        void onPlayModeClick();

        //设置列表顺序，逆序播放
        void onOrderClick();
    }
}
