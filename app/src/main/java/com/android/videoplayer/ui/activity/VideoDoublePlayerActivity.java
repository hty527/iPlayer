package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.media.VideoPlayer;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.media.JkMediaPlayer;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.Logger;

/**
 * created by hty
 * 2022/7/3
 * Desc:支持横竖屏切换\弹幕\试看\4G网络提示\失败交互的默认播放器
 * 默认的播放器是不包含视图控制器的,如需使用默认控制器,请调用initController方法
 */
public class VideoDoublePlayerActivity extends BaseActivity {

    private VideoPlayer mVideoPlayer2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_video_player);
        IVideoManager.getInstance().setInterceptTAudioFocus(false);//允许多播放器同时播放
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                onBackPressed();
            }
        });
        init1();
        init2();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 系统解码器播放器初始化
     */
    private void init1() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;
        VideoController controller = mVideoPlayer.initController();//绑定默认的控制器
        controller.setOnControllerListener(new BaseController.OnControllerEventListener() {
            @Override
            public void onMenu() {}

            @Override
            public void onBack() {//竖屏的返回事件
                Logger.d(TAG,"onBack1");
                onBackPressed();
            }

            @Override
            public void onCompletion() {//试播结束或播放完成
                Logger.d(TAG,"onCompletion1");
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
                Logger.d(TAG,"createMediaPlayer1");
                return null;
            }
        });
        mVideoPlayer.setLoop(false);
        mVideoPlayer.setProgressCallBackSpaceMilliss(300);
        mVideoPlayer.setTitle("测试播放地址1");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer.setDataSource(URL1);//播放地址设置
//        mVideoPlayer.playOrPause();//开始异步准备播放
    }

    /**
     * ijk解码器播放器初始化,开启试看功能
     */
    private void init2() {
        mVideoPlayer2 = (VideoPlayer) findViewById(R.id.video_player2);
        findViewById(R.id.player_container2).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;
        //绑定控制器
        VideoController controller = new VideoController(this);
        //实现回调getPreViewTotalDuration并传入时长(单位：秒)既表示用户看到的进度条是虚拟的进度条
        //横屏的控制器不会回调下列方法！！！
        controller.setOnControllerListener(new BaseController.OnControllerEventListener(){
            @Override
            public void onBack() {

            }

            @Override
            public void onCompletion() {

            }

            @Override
            public void onMenu() {}
        });
        controller.setPreViewTotalDuration("3600");//注意:设置虚拟总时长(一旦设置播放器内部走片段试看流程)
        controller.setOnControllerListener(new BaseController.OnControllerEventListener() {
            @Override
            public void onMenu() {}

            @Override
            public void onBack() {//竖屏的返回事件
                Logger.d(TAG,"onBack2");
            }

            @Override
            public void onCompletion() {//试播结束或播放完成
                Logger.d(TAG,"onCompletion2");
            }
        });
        mVideoPlayer2.setController(controller);
        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        mVideoPlayer2.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                Logger.d(TAG,"createMediaPlayer2");
                return new JkMediaPlayer(VideoDoublePlayerActivity.this);
            }
        });

        mVideoPlayer2.setLoop(false);
        mVideoPlayer2.setProgressCallBackSpaceMilliss(300);
        mVideoPlayer2.setTitle("测试播放地址2");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer2.setDataSource(URL2);//播放地址设置
//        mVideoPlayer2.playOrPause();//开始异步准备播放2
//        mVideoPlayer2.onCompletion();//拦截模式下不要设置播放地址,只有虚拟时间存在拦截模式才会生效
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null!=mVideoPlayer2) mVideoPlayer2.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(null!=mVideoPlayer2) mVideoPlayer2.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mVideoPlayer2) mVideoPlayer2.onDestroy();
        IVideoManager.getInstance().setInterceptTAudioFocus(true);//禁止多播放器同时播放
    }
}