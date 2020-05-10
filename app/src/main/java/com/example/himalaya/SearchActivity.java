package com.example.himalaya;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.adapters.SearchRecommendAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.interfaces.ISearchCallback;
import com.example.himalaya.presenters.SearchPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.FlowTextLayout;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends BaseActivity implements ISearchCallback {

    private static final String TAG = "SearchActivity";
    private ImageView mBackBtn;
    private EditText mInputBox;
    private TextView mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;
    private UILoader mUILoader;
    private RecyclerView mResultListView;
    private AlbumListAdapter mAlbumListAdapter;
    private FlowTextLayout mFlowTextLayout;
    private TwinklingRefreshLayout mTwinklingRefreshLayout;
    private InputMethodManager mInputMethodManager;
    private ImageView mDelBtn;
    public final static int TIME_SHOW = 500;
    private RecyclerView mSearchRecommendList;
    private SearchRecommendAdapter mSearchRecommendAdapter;
    //private FlowTextLayout mFlowTextLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        LogUtil.d(TAG, "onCreate ");
        initView();
        initEvent();
        initPresenter();
    }

    private void initPresenter() {
        LogUtil.d(TAG, "initPresenter ");
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchPresenter = SearchPresenter.getSearchPresenter();
        mSearchPresenter.registerViewCallback(this);
        //拿热词
        mSearchPresenter.getHotWord();
    }

    private void initEvent() {
        LogUtil.d(TAG, "initEvent ");
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行搜索
                String keyWord = mInputBox.getText().toString().trim();
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(keyWord);
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //输入前
                LogUtil.d(TAG, "beforeTextChanged: s  > " + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //输入内容
                //显示删除按钮
                if (TextUtils.isEmpty(s)) {
                    mDelBtn.setVisibility(View.GONE);
                    mSearchPresenter.getHotWord();
                    if (mUILoader != null) {
                        mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                    }
                } else {
                    //内容不为空触发联想查询
                    mDelBtn.setVisibility(View.VISIBLE);
                    getSuggestWord(s.toString());
                }
                LogUtil.d(TAG, "onTextChanged: s  > " + s);
                LogUtil.d(TAG, "before: s  > " + before);
                LogUtil.d(TAG, "count: s  > " + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
                LogUtil.d(TAG, "afterTextChanged");
            }
        });
        mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
            @Override
            public void onItemClick(String text) {
                //热词扔到输入框
                mInputBox.setText(text);
                //改变光标位置
                mInputBox.setSelection(text.length());
                //发起搜索
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(text);
                }
                //改变状态
                if (mUILoader != null) {
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
        mUILoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                if (mSearchPresenter != null) {
                    mSearchPresenter.reSearch();
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputBox.setText("");
            }
        });
    }

    /**
     * 获取联想的关键词
     *
     * @param keyWord
     */
    private void getSuggestWord(String keyWord) {
        LogUtil.d(TAG, "getSuggestWord " + keyWord);
        if (mSearchPresenter != null) {
            //大据哥起的名字不行
            mSearchPresenter.getRecommendWord(keyWord);
        }
    }

    private void initView() {
        LogUtil.d(TAG, "initView ");
        mBackBtn = findViewById(R.id.search_back);
        mInputBox = findViewById(R.id.search_input);
        //进入后显示键盘
        mInputBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                //获取焦点
                mInputBox.requestFocus();
                mInputMethodManager.showSoftInput(mInputBox, InputMethodManager.SHOW_IMPLICIT);
            }
        }, TIME_SHOW);
        mSearchBtn = findViewById(R.id.search_btn);
        mResultContainer = findViewById(R.id.search_container);
        //mFlowTextLayout = findViewById(R.id.flow_text_layout);
        if (mUILoader == null) {
            mUILoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
            };
            if (mUILoader.getParent() instanceof ViewGroup) {
                //添加之前把所有view干掉
                ((ViewGroup) mUILoader.getParent()).removeView(mUILoader);
            }
            mResultContainer.addView(mUILoader);
        }
        mDelBtn = findViewById(R.id.search_input_delete);
    }

    private View createSuccessView() {
        LogUtil.d(TAG, "createSuccessView ");
        View resultView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.search_result_layout, null);
        mTwinklingRefreshLayout = resultView.findViewById(R.id.tw);
        mTwinklingRefreshLayout.setPureScrollModeOn();
        mFlowTextLayout = resultView.findViewById(R.id.recommend_hot_word_view);
        mResultListView = resultView.findViewById(R.id.result_list_view);
        mSearchRecommendList = resultView.findViewById(R.id.search_recommend_list);
        mAlbumListAdapter = new AlbumListAdapter();
        mSearchRecommendAdapter = new SearchRecommendAdapter();
        aa(mResultListView, mAlbumListAdapter);
        aa(mSearchRecommendList, mSearchRecommendAdapter);
       /* mResultListView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mResultListView.setAdapter(mAlbumListAdapter);
        mResultListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(SearchActivity.this, 5);
                outRect.bottom = UIUtil.dip2px(SearchActivity.this, 5);
                outRect.left = UIUtil.dip2px(SearchActivity.this, 5);
                outRect.right = UIUtil.dip2px(SearchActivity.this, 5);
            }
        });*/
        //搜索联想词
        //mSearchRecommendList.setLayoutManager(new LinearLayoutManager(this));
        //mSearchRecommendList.setAdapter(mSearchRecommendAdapter);

        mSearchRecommendAdapter.setOnItemClickListener(new SearchRecommendAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(String keyword) {
                if (mSearchPresenter != null) {
                    mInputBox.setText(keyword);
                    mSearchPresenter.doSearch(keyword);
                    if (mUILoader != null) {
                        mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                    }
                }
            }
        });
        return resultView;
    }

    public void aa(RecyclerView rv, RecyclerView.Adapter adapter) {
        rv.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        rv.setAdapter(adapter);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(SearchActivity.this, 5);
                outRect.bottom = UIUtil.dip2px(SearchActivity.this, 5);
                outRect.left = UIUtil.dip2px(SearchActivity.this, 5);
                outRect.right = UIUtil.dip2px(SearchActivity.this, 5);
            }
        });
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy ");
        super.onDestroy();
        if (mSearchPresenter != null) {
            mSearchPresenter.unregisterViewCallback(this);
            mSearchPresenter = null;
        }
    }

    @Override
    public void onSearchResultLoader(List<Album> result) {
        LogUtil.d(TAG, "onSearchResultLoader ");
        if (result != null) {
            if (result.size() == 0) {
                //数据为空
                if (mUILoader != null) {
                    mUILoader.updateStatus(UILoader.UIStatus.EMPTY);
                }
            } else {
                //数据不为空，传递数据
                mAlbumListAdapter.setData(result);
                hideSuccessView();
                //mFlowTextLayout.setVisibility(View.GONE);有了hideSuccessView()不用写
                mTwinklingRefreshLayout.setVisibility(View.VISIBLE);
                mResultListView.setVisibility(View.VISIBLE);
                //隐藏键盘
                mInputMethodManager.hideSoftInputFromWindow(mInputBox.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                //getWindowToken()获取调用的view依附在哪个window的令牌。
                //WindowManager会给每一个window一个唯一的令牌。
                mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
    }

    @Override
    public void onHotWordLoader(List<HotWord> hotWordList) {
        if (hotWordList != null) {
            List<String> hotWords = new ArrayList<>();
            hotWords.clear();
            for (HotWord hotWord : hotWordList) {
                String searchWord = hotWord.getSearchword();
                hotWords.add(searchWord);
            }
            if (mUILoader != null) {
                //不加这个跳转到搜索是没有热词显示
                mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
            //更新UI
            Collections.sort(hotWords);//排序
            mFlowTextLayout.setTextContents(hotWords);
            hideSuccessView();
            mFlowTextLayout.setVisibility(View.VISIBLE);
            //mTwinklingRefreshLayout.setVisibility(View.GONE);
            LogUtil.d(TAG, "onHotWordLoader size--> " + hotWordList.size());
        }
    }

    @Override
    public void onLoadMoreResult(List<Album> result, boolean isOkay) {
        LogUtil.d(TAG, "onLoadMoreResult size--> " + result.size());
    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {
        LogUtil.d(TAG, "onRecommendWordLoaded size--> " + keyWordList.size());
        //更新联想词
        if (mSearchRecommendAdapter != null) {
            mSearchRecommendAdapter.setData(keyWordList);
        }
        //控制UI的状态，隐藏，显示
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //显示，隐藏
        hideSuccessView();
        mTwinklingRefreshLayout.setVisibility(View.VISIBLE);
        mSearchRecommendList.setVisibility(View.VISIBLE);
    }

    private void hideSuccessView() {
        mSearchRecommendList.setVisibility(View.GONE);
        mTwinklingRefreshLayout.setVisibility(View.GONE);
        mResultListView.setVisibility(View.GONE);
        mFlowTextLayout.setVisibility(View.GONE);
    }

    @Override
    public void Error(int errorCode, String errorMessage) {
        LogUtil.d(TAG, "Error size--> ");
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }
}
