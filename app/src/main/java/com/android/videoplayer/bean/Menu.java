package com.android.videoplayer.bean;

import com.android.videoplayer.base.adapter.interfaces.MultiItemEntity;

/**
 * created by hty
 * 2022/7/4
 * Desc:
 */
public class Menu implements MultiItemEntity {

    private int itemType;
    private String title;
    private int id;
    private String sub_title;
    private int gravity;//0:上 1:下 其它：中
    private Version version;

    public Menu(){

    }

    public Menu(String title, int id,String sub_title, int gravity) {
        this.title = title;
        this.id = id;
        this.sub_title = sub_title;
        this.gravity = gravity;
    }

    public Menu(String title, int id,String sub_title, int gravity,int itemType) {
        this.title = title;
        this.id = id;
        this.sub_title = sub_title;
        this.gravity = gravity;
        this.itemType=itemType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}