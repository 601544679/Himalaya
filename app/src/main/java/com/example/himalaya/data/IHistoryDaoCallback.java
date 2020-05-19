package com.example.himalaya.data;

import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IHistoryDaoCallback {
    /**
     * 添加历史结果
     *
     * @param isSuccess
     */
    void onHistoryAdd(boolean isSuccess);

    /**
     * 删除历史结果
     *
     * @param isSuccess
     */
    void onHistoryDel(boolean isSuccess);

    /**
     * 加载历史
     *
     * @param tracks
     */
    void onHistoriesLoaded(List<Track> tracks);

    /**
     * 清楚历史
     *
     * @param isSuccess
     */
    void onHistoriesClean(boolean isSuccess);
}
