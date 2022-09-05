package com.android.videoplayer.pager.controller;

import android.content.Context;
import android.view.View;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.videoplayer.R;
import com.android.videoplayer.utils.Logger;

/**
 * Created by TinyHung@Outlook.com
 * 2020/9/23
 * 全屏短视频视频播放场景控制器
 */
public class ShortControllerControl extends BaseController {

    private View mControlloerStart;
    private SeekBar mSeekBar;
    private boolean isTouchSeekBar;

    public ShortControllerControl(@NonNull Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.controller_short_video;
    }

    @Override
    public void initViews() {
        findViewById(R.id.controller_root_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=mVideoPlayerControl) mVideoPlayerControl.prepareAsync();
            }
        });
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
        mControlloerStart = findViewById(R.id.video_start);
        mControlloerStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();
            }
        });
        //默认停止状态
        changedUIState(View.INVISIBLE);
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        super.onPlayerState(state,message);
        Logger.d(TAG,"onState-->state:"+state);
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
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                changedUIState(View.INVISIBLE);
                break;
            case STATE_ERROR://播放失败
            case STATE_PAUSE://暂停中
            case STATE_ON_PAUSE://生命周期暂停
                changedUIState(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion) {
        super.onProgress(currentDurtion,totalDurtion);
        if(null!= mSeekBar){
            if(mSeekBar.getMax()==0){
                mSeekBar.setMax((int) totalDurtion);
            }
            if(!isTouchSeekBar) mSeekBar.setProgress((int) currentDurtion);
        }
    }

    @Override
    public void onBuffer(int bufferPercent) {
        super.onBuffer(bufferPercent);
        if(null!= mVideoPlayerControl){
            int percent = PlayerUtils.getInstance().formatBufferPercent(bufferPercent, mVideoPlayerControl.getDuration());
            if(null!= mSeekBar&&mSeekBar.getSecondaryProgress()!=percent) {
                mSeekBar.setSecondaryProgress(percent);
            }
        }
    }

    /**
     * 改变控制器UI状态
     * @param playStatus 播放状态
     */
    private void changedUIState(int playStatus) {
        if(null!= mControlloerStart) mControlloerStart.setVisibility(playStatus);
    }

    @Override
    public void onReset() {
        super.onReset();
        if(null!= mSeekBar){
            mSeekBar.setSecondaryProgress(0);
            mSeekBar.setProgress(0);
            mSeekBar.setMax(0);
        }
        changedUIState(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onReset();
        mControlloerStart =null;
    }
}