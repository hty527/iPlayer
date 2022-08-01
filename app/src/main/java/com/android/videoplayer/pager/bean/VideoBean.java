package com.android.videoplayer.pager.bean;

/**
 * created by hty
 * 2022/6/29
 * Desc:视频javaBean
 */
public class VideoBean {


    public VideoBean(){}

    private String title;
    private String cover;
    private String path;
    private String sub_title;
    private String describe;
    private String videoPlayUrl;
    private String videoDownloadUrl;
    private String videoWidth;
    private String videoHeight;
    private String musicAuthorName;
    private String musicName;
    private String likeCount;
    private String formatPlayCountStr;
    private String playCount;
    private String authorName;
    private String authorImgUrl="https://p9-dy.byteimg.com/aweme/100x100/bdf80017d3278f461445.jpeg";
    private String coverImgUrl;
    private String filterTitleStr;//简介标题
    private String musicImgUrl;//music封面

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVideoPlayUrl() {
        return videoPlayUrl;
    }

    public void setVideoPlayUrl(String videoPlayUrl) {
        this.videoPlayUrl = videoPlayUrl;
    }

    public String getVideoDownloadUrl() {
        return videoDownloadUrl;
    }

    public void setVideoDownloadUrl(String videoDownloadUrl) {
        this.videoDownloadUrl = videoDownloadUrl;
    }

    public String getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(String videoWidth) {
        this.videoWidth = videoWidth;
    }

    public String getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(String videoHeight) {
        this.videoHeight = videoHeight;
    }

    public String getMusicAuthorName() {
        return musicAuthorName;
    }

    public void setMusicAuthorName(String musicAuthorName) {
        this.musicAuthorName = musicAuthorName;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getFormatPlayCountStr() {
        return formatPlayCountStr;
    }

    public void setFormatPlayCountStr(String formatPlayCountStr) {
        this.formatPlayCountStr = formatPlayCountStr;
    }

    public String getPlayCount() {
        return playCount;
    }

    public void setPlayCount(String playCount) {
        this.playCount = playCount;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorImgUrl() {
        return authorImgUrl;
    }

    public void setAuthorImgUrl(String authorImgUrl) {
        this.authorImgUrl = authorImgUrl;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getFilterTitleStr() {
        return filterTitleStr;
    }

    public void setFilterTitleStr(String filterTitleStr) {
        this.filterTitleStr = filterTitleStr;
    }

    public String getMusicImgUrl() {
        return musicImgUrl;
    }

    public void setMusicImgUrl(String musicImgUrl) {
        this.musicImgUrl = musicImgUrl;
    }
}