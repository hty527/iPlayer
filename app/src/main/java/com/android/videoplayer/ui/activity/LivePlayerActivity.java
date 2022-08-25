package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.media.ExoMediaPlayer;
import com.android.videoplayer.media.JkMediaPlayer;
import com.android.videoplayer.pager.controller.LiveController;
import com.android.videoplayer.pager.widget.ControlLiveView;

/**
 * created by hty
 * 2022/8/25
 * Desc:这是一个直播拉流和简单的自定义直播组件交互的实例
 */
public class LivePlayerActivity extends BaseActivity {

    private int MEDIA_CORE=2;//这里默认用ExoPlayer解码器

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        findViewById(R.id.live_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initPlayer();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    private void initPlayer() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        mVideoPlayer.getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//给播放器固定一个高度
        mVideoPlayer.setLoop(true);
        mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);//设置视频画面渲染模式为：全屏缩放模式
        //给播放器设置一个控制器
        LiveController controller = new LiveController(mVideoPlayer.getContext());
        mVideoPlayer.setController(controller);
        //给控制器添加需要的UI交互组件
        ControlLoadingView controlLoadingView = new ControlLoadingView(controller.getContext());//加载中、开始播放按钮
        ControlStatusView controlStatusView = new ControlStatusView(controller.getContext());//播放失败、移动网络播放提示
        ControlLiveView controlLiveView = new ControlLiveView(controller.getContext());//自定义直播场景交互UI组件
        controller.addControllerWidget(controlLoadingView,controlStatusView,controlLiveView);
        //自定义解码器
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                if (1 == MEDIA_CORE) {
                    return new JkMediaPlayer(LivePlayerActivity.this);
                } else if (2 == MEDIA_CORE) {
                    return new ExoMediaPlayer(LivePlayerActivity.this);
                } else {
                    return null;
                }
            }
        });
        mVideoPlayer.setDataSource(LIVE_RTMP2);
        mVideoPlayer.prepareAsync();//准备播放
    }
}