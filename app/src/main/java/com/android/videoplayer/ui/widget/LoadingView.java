package com.android.videoplayer.ui.widget;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.videoplayer.R;

/**
 * created by hty
 * 2022/7/1
 * Desc:处理中、处理失败、重试等状态View
 */
public class LoadingView extends LinearLayout {

    private ProgressBar mProgressBar;
    private ImageView mIvLoading;
    private TextView mTvContent;

    public LoadingView(Context context) {
        this(context,null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_loading_view,this);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);
        mIvLoading = (ImageView) findViewById(R.id.iv_loading);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        findViewById(R.id.lv_root).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mRefreshListener){
                    mRefreshListener.onRefresh();
                }
            }
        });
    }

    public void setHeight(int height){
        findViewById(R.id.lv_root).getLayoutParams().height=height;
    }

    public void showLoadingView() {
        showLoadingView("正在加载中...");
    }

    public void showLoadingView(String message) {
        if(null!=mIvLoading) mIvLoading.setVisibility(GONE);
        if(null!=mProgressBar) mProgressBar.setVisibility(VISIBLE);
        if(null!=mTvContent) mTvContent.setText(message);
    }

    public void showEmptyView(String message) {
        showEmptyView(message, R.mipmap.ic_list_empty);
    }

    public void showEmptyView(String message, @DrawableRes int resId) {
        if(null!=mProgressBar) mProgressBar.setVisibility(GONE);
        if(null!=mTvContent) mTvContent.setText(message);
        if(null!=mIvLoading){
            mIvLoading.setVisibility(VISIBLE);
            mIvLoading.setImageResource(resId);
        }
    }

    public void showErrorView(String message) {
        showErrorView(message, R.mipmap.ic_list_error);
    }

    public void showErrorView(String message, @DrawableRes int resId) {
        if(null!=mProgressBar) mProgressBar.setVisibility(GONE);
        if(null!=mTvContent) mTvContent.setText(message);
        if(null!=mIvLoading){
            mIvLoading.setVisibility(VISIBLE);
            mIvLoading.setImageResource(resId);
        }
    }

    public void reset() {
        if(null!=mIvLoading) mIvLoading.setVisibility(GONE);
        if(null!=mProgressBar) mProgressBar.setVisibility(GONE);
        if(null!=mIvLoading) mIvLoading.setVisibility(GONE);
    }

    private OnRefreshListener mRefreshListener;

    public void setRefreshListener(OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public void setTextColor(int parseColor) {
        if(null!=mTvContent){
            mTvContent.setTextColor(parseColor);
        }
    }

    public interface OnRefreshListener{
        void onRefresh();
    }
}
