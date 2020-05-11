package com.example.himalaya.presenters;

import com.example.himalaya.data.XimalayaApi;
import com.example.himalaya.interfaces.ISearchCallback;
import com.example.himalaya.interfaces.ISearchPresenter;
import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {
    //保存搜索集合
    private List<Album> mSearchResult = new ArrayList<>();
    private List<ISearchCallback> mCallbacks = new ArrayList<>();
    //当前的搜索关键字
    private String mCurrentKeyWord = null;
    private final XimalayaApi mXimalayaApi;
    private static final int DEFAULT_PAGE = 1;
    private int mCurrentPage = DEFAULT_PAGE;
    private static final String TAG = "SearchPresenter";

    private SearchPresenter() {
        LogUtil.d(TAG, "  private SearchPresenter() ");
        mXimalayaApi = XimalayaApi.getXimalayaApi();
    }

    public static SearchPresenter sSearchPresenter = null;

    public static SearchPresenter getSearchPresenter() {
        LogUtil.d(TAG, " public static SearchPresenter getSearchPresenter()");
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class) {
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }
        }
        return sSearchPresenter;
    }

    @Override
    public void doSearch(String keyword) {
        mCurrentPage = DEFAULT_PAGE;
        mSearchResult.clear();
        LogUtil.d(TAG, "doSearch");
        //保存变量,用于重新搜索，比如网络不好,用户点击重试
        this.mCurrentKeyWord = keyword;
        search(keyword);

    }

    private void search(String keyword) {
        mXimalayaApi.searchByKeyWord(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                LogUtil.d(TAG, "onSuccess");
                List<Album> albums = searchAlbumList.getAlbums();
                mSearchResult.addAll(albums);
                if (albums != null) {
                    LogUtil.d(TAG, "albums size --> " + albums.size());
                    if (mIsLoadMore) {
                        for (ISearchCallback callback : mCallbacks) {
                            //不等于0才表示有更多数据
                            callback.onLoadMoreResult(mSearchResult, albums.size() != 0);
                        }
                        mIsLoadMore = false;
                    } else {
                        for (ISearchCallback callback : mCallbacks) {
                            callback.onSearchResultLoader(mSearchResult);
                        }
                    }
                } else {
                    LogUtil.d(TAG, "albums is null--> ");
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                LogUtil.d(TAG, "errorCode--> " + errorCode + " errorMessage--> " + errorMessage);
                //加载更多失败
                for (ISearchCallback callback : mCallbacks) {
                    if (mIsLoadMore) {
                        callback.onLoadMoreResult(mSearchResult, false);
                        mIsLoadMore = false;
                        mCurrentPage--;//因为数据不超过1页
                    } else {
                        callback.Error(errorCode, errorMessage);
                    }
                }
            }
        });
    }

    @Override
    public void reSearch() {
        search(mCurrentKeyWord);
        LogUtil.d(TAG, "reSearch");

    }

    private boolean mIsLoadMore = false;

    @Override
    public void loadMore() {
        //先判断有没有必要加载更多
        if (mSearchResult.size() < Constans.COUNT_DEFAULT) {
            //少于默认数量没有必要加载更多
            for (ISearchCallback callback : mCallbacks) {
                callback.onLoadMoreResult(mSearchResult, false);
            }
        } else {
            mIsLoadMore = true;
            mCurrentPage++;//加多一页
            search(mCurrentKeyWord);
        }
        LogUtil.d(TAG, "loadMore");
    }

    @Override
    public void getHotWord() {
        //做一个热词缓存
        mXimalayaApi.getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    LogUtil.d(TAG, "hotWords size--> " + hotWords.size());
                    for (ISearchCallback callback : mCallbacks) {
                        //传递数据
                        callback.onHotWordLoader(hotWords);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                LogUtil.d(TAG, "errorCode--> " + errorCode + " errorMessage--> " + errorMessage);

            }
        });
        LogUtil.d(TAG, "hotWord");
    }

    /**
     * 获取联想词
     *
     * @param keyword
     */
    @Override
    public void getRecommendWord(String keyword) {
        mXimalayaApi.getSuggestWords(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    for (ISearchCallback callback : mCallbacks) {
                        //通知UI，联想词获取成功
                        callback.onRecommendWordLoaded(keyWordList);
                    }
                    LogUtil.d(TAG, "keyWordList size--> " + keyWordList.size());
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                LogUtil.d(TAG, "errorCode--> " + errorCode + " errorMessage--> " + errorMessage);

            }
        });
        LogUtil.d(TAG, "getRecommendWord");
    }

    @Override
    public void registerViewCallback(ISearchCallback iSearchCallback) {
        LogUtil.d(TAG, "registerViewCallback");
        if (!mCallbacks.contains(iSearchCallback)) {
            mCallbacks.add(iSearchCallback);
        }
    }

    @Override
    public void unregisterViewCallback(ISearchCallback iSearchCallback) {
        LogUtil.d(TAG, "unregisterViewCallback");
        mCallbacks.remove(iSearchCallback);
    }
}
