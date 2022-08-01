package com.android.videoplayer.video.bean;

/**
 * created by hty
 * 2022/7/9
 * Desc:头部参数等
 */
public class VideoParams {

    private long id;//视频ID
    private String nickname;//用户昵称
    private String user_cover;//用户头像
    private long date;//最后更新时间
    private String title;//视频标题
    private String playUrl;//播放地址
    private String description;//视频描述
    private String videoCover;//视频封面

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUser_cover() {
        return user_cover;
    }

    public void setUser_cover(String user_cover) {
        this.user_cover = user_cover;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }
}