package com.example.himalaya.interfaces;

import com.example.himalaya.base.IBasePresenter;

public interface ISearchPresenter extends IBasePresenter<ISearchCallback> {
    /**
     * 进行搜索
     *
     * @param keyword
     */
    void doSearch(String keyword);

    /**
     * 重新搜索
     */
    void reSearch();

    /**
     * 加载更多搜索结果
     */
    void loadMore();

    /**
     * 获取热词,就是搜索框下面的热词提示
     */
    void getHotWord();

    /**
     * 获取推荐的关键字，就是联想搜索
     *
     * @param keyword
     */
    void getRecommendWord(String keyword);
}
