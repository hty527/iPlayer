package com.android.videoplayer.video.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * 开眼视频
 */

public class OpenEyesIndexInfo {

    //一维数组
    private List<OpenEyesIndexItemBean> itemList;

    private List<OpenEyesIndexItemBean> videoList;

    public List<OpenEyesIndexItemBean> getItemList() {
        return itemList;
    }

    public void setItemList(List<OpenEyesIndexItemBean> itemList) {
        this.itemList = itemList;
    }

    public List<OpenEyesIndexItemBean> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<OpenEyesIndexItemBean> videoList) {
        this.videoList = videoList;
    }
}