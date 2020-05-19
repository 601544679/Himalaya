package com.example.himalaya.interfaces;


import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IHistoryCallback {
    /**
     * 加载历史内容
     *
     * @param tracks
     */
    void onHistoriesLoaded(List<Track> tracks);
}
