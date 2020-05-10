package com.example.himalaya.api;

import com.example.himalaya.utils.Constans;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

public class XimalayaApi {
    private static final String TAG = "XimalayaApi";

    private XimalayaApi() {
        LogUtil.d(TAG, "private XimalayaApi()");
    }

    public static XimalayaApi sXimalayaApi;

    public static XimalayaApi getXimalayaApi() {
        LogUtil.d(TAG, "public static XimalayaApi getXimalayaApi()");
        if (sXimalayaApi == null) {
            synchronized (XimalayaApi.class) {
                if (sXimalayaApi == null) {
                    sXimalayaApi = new XimalayaApi();
                }
            }
        }
        return sXimalayaApi;
    }

    /**
     * 获取推荐内容
     *
     * @param callBack
     */
    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callBack) {
        LogUtil.d(TAG, "getRecommendList");
        Map<String, String> map = new HashMap<>();
        //表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, String.valueOf(Constans.COUNT_RECOMMEND));
        CommonRequest.getGuessLikeAlbum(map, callBack);
    }

    /**
     * 根据页码和专辑id获取列表
     *
     * @param callBack  获取专辑详情的回调接口
     * @param albumId   专辑ID
     * @param pageIndex 第几页
     */
    public void getAlbumDetail(IDataCallBack<TrackList> callBack, long albumId, int pageIndex) {
        LogUtil.d(TAG, "getAlbumDetail   --" + "albumId " + albumId + "  pageIndex: " + pageIndex);
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.ALBUM_ID, String.valueOf(albumId));
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, String.valueOf(pageIndex));
        map.put(DTransferConstants.PAGE_SIZE, String.valueOf(Constans.COUNT_DEFAULT));
        CommonRequest.getTracks(map, callBack);
    }

    //根据关键词进行搜索
    public void searchByKeyWord(String keyword, int page, IDataCallBack<SearchAlbumList> callBack) {
        LogUtil.d(TAG, "searchByKeyWord   --" + "keyword " + keyword + "  page: " + page);
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        map.put(DTransferConstants.PAGE, page + "");
        map.put(DTransferConstants.PAGE_SIZE, Constans.COUNT_DEFAULT + "");
        CommonRequest.getSearchedAlbums(map, callBack);
    }

    /**
     * 获取推荐的热词
     *
     * @param callBack
     */
    public void getHotWords(IDataCallBack<HotWordList> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.TOP, Constans.COUNT_HOT_WORD + "");
        CommonRequest.getHotWords(map, callBack);
    }

    /**
     * 根据关键字获取联想词
     *
     * @param keyWord  关键字
     * @param callBack 回调
     */
    public void getSuggestWords(String keyWord, IDataCallBack<SuggestWords> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyWord);
        CommonRequest.getSuggestWord(map, callBack);
    }
}
