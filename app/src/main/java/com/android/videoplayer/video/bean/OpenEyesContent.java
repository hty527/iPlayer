package com.android.videoplayer.video.bean;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 */

public class OpenEyesContent {

    private long id;
    //squareCardCollection
    private String type;
    private OpenEyesIndexItemBean data;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OpenEyesIndexItemBean getData() {
        return data;
    }

    public void setData(OpenEyesIndexItemBean data) {
        this.data = data;
    }
}
