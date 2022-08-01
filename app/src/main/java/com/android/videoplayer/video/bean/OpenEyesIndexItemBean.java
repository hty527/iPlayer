package com.android.videoplayer.video.bean;

import android.text.TextUtils;
import com.android.videoplayer.base.adapter.interfaces.MultiItemEntity;
import java.util.List;

/**
 * created by hty
 * 2022/7/7
 * Desc:
 */
public class OpenEyesIndexItemBean implements MultiItemEntity {

    //Item类型
    public static final int ITEM_UNKNOWN    =0;//未知
    public static final int ITEM_TITLE      =1;//标题
    public static final int ITEM_FOLLOW     =2;//收藏视频
    public static final int ITEM_VIDEO      =3;//视频
    public static final int ITEM_CARD_VIDEO =4;//card视频
    //视频
    public static final int ITEM_NOIMAL     =5;//普通视频
    public static final int ITEM_HEADER     =6;//头部

    //条目类型
    //squareCardCollection
    private String type;
    //本地区分条目类型
    private int itemType;

    //详细元素
    private long id;
    private long date;
    private long duration;
    private int count;
    private OpenEyesContent content;
    private OpenEyesIndexItemBean data;
    private List<OpenEyesIndexItemBean> itemList;
    private String text;
    private String description;
    private String playUrl;
    private String title;
    private OpenEyesAuthor author;
    private Consumption consumption;
    private Cover cover;
    //头部数据
    private VideoParams headers;

    @Override
    public int getItemType() {
        if(TextUtils.isEmpty(type)){
            itemType=ITEM_UNKNOWN;
        }else if("textCard".equals(type)){
            itemType=ITEM_TITLE;
        }else if("followCard".equals(type)){
            itemType=ITEM_FOLLOW;
        }else if("videoSmallCard".equals(type)){
            itemType=ITEM_VIDEO;
        }else if("video".equals(type)){
            itemType=ITEM_CARD_VIDEO;
        }else if("NORMAL".equals(type)){
            itemType=ITEM_NOIMAL;
        }else if("header".equals(type)){
            itemType=ITEM_HEADER;
        }else{
            itemType=ITEM_UNKNOWN;
        }
        return itemType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public OpenEyesContent getContent() {
        return content;
    }

    public void setContent(OpenEyesContent content) {
        this.content = content;
    }

    public OpenEyesIndexItemBean getData() {
        return data;
    }

    public void setData(OpenEyesIndexItemBean data) {
        this.data = data;
    }

    public List<OpenEyesIndexItemBean> getItemList() {
        return itemList;
    }

    public void setItemList(List<OpenEyesIndexItemBean> itemList) {
        this.itemList = itemList;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OpenEyesAuthor getAuthor() {
        return author;
    }

    public void setAuthor(OpenEyesAuthor author) {
        this.author = author;
    }

    public Consumption getConsumption() {
        return consumption;
    }

    public void setConsumption(Consumption consumption) {
        this.consumption = consumption;
    }

    public Cover getCover() {
        return cover;
    }

    public void setCover(Cover cover) {
        this.cover = cover;
    }

    public static class Consumption{

        /**
         * collectionCount : 213
         * replyCount : 4
         * shareCount : 51
         */

        private int collectionCount;
        private int replyCount;
        private int shareCount;

        public int getCollectionCount() {
            return collectionCount;
        }

        public void setCollectionCount(int collectionCount) {
            this.collectionCount = collectionCount;
        }

        public int getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(int replyCount) {
            this.replyCount = replyCount;
        }

        public int getShareCount() {
            return shareCount;
        }

        public void setShareCount(int shareCount) {
            this.shareCount = shareCount;
        }
    }


    public static class Cover{

        /**
         * blurred : http://img.kaiyanapp.com/3caf5628a7f4ea525949225715376182.png?imageMogr2/quality/60/format/jpg
         * detail : http://img.kaiyanapp.com/7b6399dbc663ac20dcaab9fe58b140ff.png?imageMogr2/quality/60/format/jpg
         * feed : http://img.kaiyanapp.com/7b6399dbc663ac20dcaab9fe58b140ff.png?imageMogr2/quality/60/format/jpg
         * homepage : http://img.kaiyanapp.com/7b6399dbc663ac20dcaab9fe58b140ff.png?imageView2/1/w/720/h/560/format/jpg/q/75|watermark/1/image/aHR0cDovL2ltZy5rYWl5YW5hcHAuY29tL2JsYWNrXzMwLnBuZw==/dissolve/100/gravity/Center/dx/0/dy/0|imageslim
         */

        private String blurred;
        private String detail;
        private String feed;

        public String getBlurred() {
            return blurred;
        }

        public void setBlurred(String blurred) {
            this.blurred = blurred;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getFeed() {
            return feed;
        }

        public void setFeed(String feed) {
            this.feed = feed;
        }

    }

    public VideoParams getHeaders() {
        return headers;
    }

    public void setHeaders(VideoParams headers) {
        this.headers = headers;
    }
}