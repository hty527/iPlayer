package com.android.videoplayer.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.VideoPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.media.JkMediaPlayer;
import com.android.videoplayer.utils.Logger;

/**
 * created by hty
 * 2022/7/4
 * Desc:直接开启全屏播放
 */
public class FullScreenPlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPlayer();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer() {
        mVideoPlayer = new VideoPlayer(this);
        mVideoPlayer.setBackgroundColor(Color.parseColor("#000000"));
        VideoController controller = mVideoPlayer.initController();//绑定默认的控制器
        controller.enableFullScreen(false);
        controller.setOnControllerListener(new BaseController.OnControllerEventListener() {

            @Override
            public void onBack() {//竖屏的返回事件
                Logger.d(TAG,"onBack");
                finish();
            }

            @Override
            public void onCompletion() {//试播结束或播放完成
                Logger.d(TAG,"onCompletion");
            }

        });
        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return new JkMediaPlayer(FullScreenPlayerActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
//        mVideoPlayer.setPreViewTotalDuration("3600");//注意:设置虚拟总时长(一旦设置播放器内部走片段试看流程)
        mVideoPlayer.setLoop(false);
        mVideoPlayer.setProgressCallBackSpaceMilliss(300);
        mVideoPlayer.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer.setDataSource(URL2);//播放地址设置
        mVideoPlayer.startFullScreen();//开启全屏播放
        mVideoPlayer.playOrPause();//开始异步准备播放
    }
}