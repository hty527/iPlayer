package com.android.videoplayer.base.adapter.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * created by hty
 * 2022/7/1
 * Desc:基础的BaseViewHolder
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public View findViewById(int id){
        return getView(id);
    }

    public  <T extends View> T getView(int id) {
        if (null == itemView) return null;
        return (T) itemView.findViewById(id);
    }

    public Context getContext(){
        if(null!=itemView){
            return itemView.getContext();
        }
        return null;
    }
}