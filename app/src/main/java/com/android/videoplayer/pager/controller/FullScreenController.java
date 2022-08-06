package com.android.videoplayer.pager.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.ControllerStatusView;
import com.android.videoplayer.R;
import com.android.videoplayer.utils.Logger;

/**
 * Created by TinyHung@Outlook.com
 * 2020/9/23
 * 全屏视频播放器控制器
 */
public class FullScreenController extends BaseController {

    public static final int SCENE_MOBILE     =1;//移动网络播放提示
    public static final int SCENE_COMPLETION =2;//试看结束
    public static final int SCENE_ERROR      =3;//播放失败
    private View mBtnStart;
    private ControllerStatusView mMobileLayout;
    private SeekBar mSeekBar;
    private boolean isTouchSeekBar;
    private TextView mError;

    public FullScreenController(@NonNull Context context) {
        this(context,null);
    }

    public FullScreenController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FullScreenController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_pager_controller_layout;
    }

    @Override
    public void initViews() {
        mSeekBar = (SeekBar) findViewById(R.id.video_bottom_progress);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * 用户持续拖动进度条,视频总长为虚拟时长时，用户不得滑动阈值超过限制
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Logger.d(TAG,"onProgressChanged-->progress:"+progress+",fromUser:"+fromUser+getOrientationStr());
            }

            /**
             * 获得焦点-按住了
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar=true;
            }

            /**
             * 失去焦点-松手了
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar=false;
                if(null!= mVideoPlayerControl){
                    int seekBarProgress = seekBar.getProgress();
                    mVideoPlayerControl.seekTo(seekBarProgress);//真实时长,允许跳转
                }
            }
        });
        mSeekBar.setMax(0);
        mSeekBar.setProgress(0);
        mBtnStart = findViewById(R.id.video_start);
        mMobileLayout = findViewById(R.id.video_mobile);
        mMobileLayout.setOnStatusListener(new ControllerStatusView.OnStatusListener() {
            @Override
            public void onEvent(int event) {
                if(SCENE_MOBILE==event){//移动网络播放
                    IVideoManager.getInstance().setMobileNetwork(true);
                    if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
                }
            }
        });
        mBtnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
            }
        });
        mError=findViewById(R.id.video_error);
        //默认停止状态
        changedUIState(View.INVISIBLE, View.INVISIBLE,0,null);
    }

    @Override
    public void onState(PlayerState state, String message) {
        Logger.d(TAG,"onState-->state:"+state);
        if(null!=mError){//将有些过期的视频不能播放的原因提示给观众
            mError.setText(state==PlayerState.STATE_ERROR?message:"");
        }
        switch (state) {
            case STATE_RESET://初始状态\播放器还原重置
            case STATE_STOP://停止播放
            case STATE_COMPLETION://播放结束
            case STATE_DESTROY://播放器回收
                onReset();
                break;
            case STATE_PREPARE://准备中
            case STATE_BUFFER://缓冲中
            case STATE_START://首次播放
            case STATE_PLAY://恢复播放
            case STATE_ON_PLAY://生命周期恢复播放
                changedUIState(View.INVISIBLE, View.INVISIBLE,0,null);
                break;
            case STATE_ERROR://播放失败
            case STATE_PAUSE://暂停中
            case STATE_ON_PAUSE://生命周期暂停
                changedUIState(View.VISIBLE, View.INVISIBLE,0,null);
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                changedUIState(View.INVISIBLE, View.VISIBLE,SCENE_MOBILE,null);
                break;
        }
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion, int bufferPercent) {
        if(null!= mSeekBar){
            if(mSeekBar.getMax()==0){
                mSeekBar.setMax((int) totalDurtion);
            }
            if(!isTouchSeekBar) mSeekBar.setProgress((int) currentDurtion);
        }
    }

    @Override
    public void onBuffer(int bufferPercent) {
        Logger.d(TAG,"onBuffer-->bufferPercent:"+bufferPercent);
        if(null!=mVideoPlayerControl){
            int percent = PlayerUtils.getInstance().formatBufferPercent(bufferPercent, mVideoPlayerControl.getDuration());
            if(null!= mSeekBar&&mSeekBar.getSecondaryProgress()!=percent) {
                mSeekBar.setSecondaryProgress(percent);
            }
        }
    }

    @Override
    public void setScreenOrientation(int orientation) {}

    @Override
    public void setWindowProperty(boolean isWindowProperty, boolean isGlobalWindow) {

    }

    /**
     * 改变控制器UI状态
     * @param playStatus 播放状态
     * @param statuView 移动网络播放提示状态
     */
    private void changedUIState(int playStatus,int statuView,int scene,String errorMessage) {
        Logger.d(TAG,"changedUIState:playStatus："+playStatus+",mobileLayout:"+statuView+",errorMessage:"+errorMessage);
        if(null!=mBtnStart) mBtnStart.setVisibility(playStatus);
        if(null!=mMobileLayout){
            mMobileLayout.setVisibility(statuView);
            if(scene>0) mMobileLayout.setScene(scene,errorMessage);//仅当需要处理状态场景时才更新交互UI
        }
    }

    @Override
    public void onResume() {
        Logger.d(TAG,"onResume-->:");
    }

    @Override
    public void onPause() {
        Logger.d(TAG,"onPause-->:");
    }

    @Override
    public void onReset() {
        Logger.d(TAG,"onReset-->:");
        if(null!= mSeekBar){
            mSeekBar.setSecondaryProgress(0);
            mSeekBar.setProgress(0);
            mSeekBar.setMax(0);
        }
        changedUIState(View.INVISIBLE, View.INVISIBLE,0,null);
    }

    @Override
    public void onDestroy() {
        onReset();
        mBtnStart=null;
    }
}