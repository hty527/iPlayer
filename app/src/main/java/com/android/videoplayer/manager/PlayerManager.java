package com.android.videoplayer.manager;

import com.android.iplayer.widget.VideoPlayer;
import com.android.videoplayer.video.bean.OpenEyesIndexItemBean;
import com.android.videoplayer.video.bean.VideoParams;

/**
 * created by hty
 * 2022/7/2
 * Desc:转场无缝播放,需要一个中间管理人
 */
public class PlayerManager {

    private volatile static PlayerManager mInstance;
    private boolean isChangeIng=false;
    private VideoPlayer mVideoPlayer;//无缝转场临时对象

    public static synchronized PlayerManager getInstance(){
        synchronized (PlayerManager.class){
            if(null==mInstance){
                mInstance=new PlayerManager();
            }
        }
        return mInstance;
    }

    public VideoPlayer getVideoPlayer() {
        return mVideoPlayer;
    }

    public void setVideoPlayer(VideoPlayer videoPlayer) {
        mVideoPlayer = videoPlayer;
        setChangeIng(null!=mVideoPlayer);
    }

    public boolean isChangeIng() {
        return isChangeIng;
    }

    public void setChangeIng(boolean changeIng) {
        isChangeIng = changeIng;
    }

    /**
     * 返回实体类型的播放地址
     * @param itemBean
     * @return
     */
    public String[] getVideoPath(OpenEyesIndexItemBean itemBean){
        String[] source =new String[2];
        if(null!=itemBean){
            switch (itemBean.getItemType()) {
                case OpenEyesIndexItemBean.ITEM_FOLLOW:
                    if(null!=itemBean.getData()&&null!=itemBean.getData().getContent()){
                        OpenEyesIndexItemBean indexItemBean = itemBean.getData().getContent().getData();
                        source[0]=indexItemBean.getPlayUrl();
                        source[1]=indexItemBean.getTitle();
                    }
                    break;
                case OpenEyesIndexItemBean.ITEM_VIDEO:
                    if(null!=itemBean.getData()) {
                        source[0]=itemBean.getData().getPlayUrl();
                        source[1]=itemBean.getData().getTitle();
                    }
                    break;
                default:
                    source[0]=itemBean.getPlayUrl();
                    source[1]=itemBean.getTitle();
            }
        }
        return source;
    }

    /**
     * 返回实体类型的java bean
     * @param itemBean
     * @return
     */
    public OpenEyesIndexItemBean getItemData(OpenEyesIndexItemBean itemBean){
        if(null!=itemBean){
            switch (itemBean.getItemType()) {
                case OpenEyesIndexItemBean.ITEM_FOLLOW:
                    if(null!=itemBean.getData()&&null!=itemBean.getData().getContent()){
                        OpenEyesIndexItemBean indexItemBean = itemBean.getData().getContent().getData();
                        return indexItemBean;
                    }
                case OpenEyesIndexItemBean.ITEM_VIDEO:
                    if(null!=itemBean.getData()) {
                        return itemBean.getData();
                    }
                case OpenEyesIndexItemBean.ITEM_CARD_VIDEO:
                case OpenEyesIndexItemBean.ITEM_NOIMAL:
                    return itemBean;
            }
            return itemBean;
        }
        return null;
    }

    /**
     * 根据ITEM解析落地页需要的参数
     * @param itemData
     * @return
     */
    public VideoParams parseParams(OpenEyesIndexItemBean itemData) {
        if(null==itemData) return null;
        VideoParams params=new VideoParams();
        params.setDate(itemData.getDate());
        params.setNickname(null!=itemData.getAuthor()?itemData.getAuthor().getName():itemData.getTitle());
        params.setUser_cover(null!=itemData.getAuthor()?itemData.getAuthor().getIcon():"");
        params.setDescription(itemData.getDescription());
        params.setId(itemData.getId());
        params.setTitle(itemData.getTitle());
        params.setPlayUrl(itemData.getPlayUrl());
        params.setVideoCover(null!=itemData.getCover()?itemData.getCover().getFeed():"");
        return params;
    }
}