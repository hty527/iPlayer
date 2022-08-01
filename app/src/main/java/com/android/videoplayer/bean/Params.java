package com.android.videoplayer.bean;

/**
 * created by hty
 * 2022/7/11
 * Desc:这个时视频落地页接收处理悬浮窗窗口播放器时的临时中转参数
 */
public class Params {

    private long id;
    private String listJson;
    private String paramsJson;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getListJson() {
        return listJson;
    }

    public void setListJson(String listJson) {
        this.listJson = listJson;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }
}