package com.example.himalaya.interfaces;

public interface IRecommendRresenter {
    /**
     * 获取推荐内容
     */
    void getRecommendList();

    /**
     * 下拉刷新
     */
    void pull2RefreshMode();

    /**
     * 上拉加载更多
     */
    void loadMore();

    /**
     * 这个方法用于注册UI的回调实现类,注册了就要取消，防止泄露
     */
    void registerViewCallback(IRecommendViewCallback callback);

    /**
     * 取消
     * @param callback
     */
    void unregisterViewCallback(IRecommendViewCallback callback);
}
