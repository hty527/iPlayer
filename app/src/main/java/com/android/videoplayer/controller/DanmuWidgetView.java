package com.android.videoplayer.controller;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.iplayer.base.BaseControllerWidget;
import com.android.iplayer.model.PlayerState;
import com.android.videoplayer.R;
import com.android.videoplayer.danmu.DanmuPaserView;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;
import java.util.List;

/**
 * created by hty
 * 2022/7/3
 * Desc:弹幕功能交互的控制器
 */
public class DanmuWidgetView extends BaseControllerWidget {

    private DanmuPaserView mDanmuPaserView;//全局的弹幕

    public DanmuWidgetView(@NonNull Context context) {
        super(context);
    }

    public DanmuWidgetView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DanmuWidgetView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_controller_danmu;
    }

    @Override
    public void initViews() {
        mDanmuPaserView=findViewById(R.id.view_danmu);
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        Logger.d(TAG,"onPlayerState-->state："+state+",message:"+message);
        switch (state) {
            case STATE_PREPARE://准备中
                initDanmaku();
                break;
            case STATE_RESET://初始
            case STATE_STOP://停止
                onReset();
                break;
            case STATE_ERROR://播放失败
                break;
            case STATE_BUFFER://缓冲中
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                break;
            case STATE_START://开始首帧播放
            case STATE_PLAY://恢复播放
            case STATE_ON_PLAY://生命周期恢复播放
                onResumeDanmu();
                break;
            case STATE_PAUSE://手动暂停中
            case STATE_ON_PAUSE://生命周期暂停
                onPauseDanmu();
                break;
            case STATE_COMPLETION://播放结束
            case STATE_DESTROY://播放器回收
                onResetDanmu();
                break;
        }
    }

    @Override
    public void onOrientation(int direction) {}

    @Override
    public void onPlayerScene(int playerScene) {
        findViewById(R.id.view_tool_bar).getLayoutParams().height= isOrientationPortrait()?0:ScreenUtils.getInstance().getStatusBarHeight(getContext());
    }

    @Override
    public void onReset() {
        Logger.d(TAG,"onReset-->");
        onResetDanmu();
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG,"onDestroy-->");
        onDestroyDanmu();
    }

    private void initDanmaku() {
        Logger.d(TAG,"initDanmaku");
        if(null!=mDanmuPaserView) mDanmuPaserView.initDanmaku();
    }

    private void onResetDanmu(){
        Logger.d(TAG,"onResetDanmu");
        if(null!=mDanmuPaserView) mDanmuPaserView.releaseDanmaku();
    }

    private void onResumeDanmu(){
        Logger.d(TAG,"onResumeDanmu");
        if(null!=mDanmuPaserView&&mDanmuPaserView.getVisibility()==VISIBLE){
            mDanmuPaserView.onResume();
        }
    }

    private void onPauseDanmu(){
        Logger.d(TAG,"onPauseDanmu");
        if(null!=mDanmuPaserView) mDanmuPaserView.onPause();
    }

    private void onDestroyDanmu(){
        Logger.d(TAG,"onDestroyDanmu");
        if(null!=mDanmuPaserView){
            mDanmuPaserView.onDestroy();
            mDanmuPaserView=null;
        }
    }

    public void openDanmu(){
        if(null!=mDanmuPaserView) mDanmuPaserView.setVisibility(VISIBLE);
        onResumeDanmu();
    }

    public void closeDanmu(){
        onPauseDanmu();
        if(null!=mDanmuPaserView) mDanmuPaserView.setVisibility(INVISIBLE);
    }

    /**
     * 设置弹幕数据
     * @param barrage
     */
    public void setDanmuData(List<String> barrage){
        Logger.d(TAG,"setDanmuData");
        if(null!=mDanmuPaserView) mDanmuPaserView.addDanmuContent(barrage);
    }

    /**
     * 追加弹幕数据
     * @param content 弹幕文本内容
     * @param isOneself 是否是自己发送的
     */
    public void addDanmuItem(String content,boolean isOneself){
        if(null!=mDanmuPaserView) mDanmuPaserView.addDanmuItem(content,isOneself);
    }
}