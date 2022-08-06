package com.android.videoplayer.ui.activity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.VideoPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;
import java.io.IOException;

/**
 * created by hty
 * 2022/6/22
 * Desc:raw和assets资源播放
 */
public class AssetsPlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_assets_player);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                onBackPressed();
            }
        });
        init();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 播放器初始化及调用示例
     */
    private void init() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;
        VideoController controller = mVideoPlayer.initController();//绑定默认的控制器
        controller.setTitleTopOffset(ScreenUtils.getInstance().getStatusBarHeight(getApplicationContext()));
        controller.setOnControllerListener(new BaseController.OnControllerEventListener() {

            @Override
            public void onBack() {//竖屏的返回事件
                Logger.d(TAG,"onBack");
                onBackPressed();
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
//                return new JkMediaPlayer(AssetsPlayerActivity.this);//使用ijk解码器
                return null;//使用内部默认解码器
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
        mVideoPlayer.setLoop(false);
        mVideoPlayer.setProgressCallBackSpaceMilliss(300);
        mVideoPlayer.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
    }

    /**
     * 播放Raw目录下文件
     * @param view
     */
    public void raw(View view) {
        if(null!=mVideoPlayer){
            mVideoPlayer.onReset();
            String url = "android.resource://" + getPackageName() + "/" + R.raw.test;
            mVideoPlayer.setDataSource(url);
            mVideoPlayer.playOrPause();
        }
    }

    /**
     * 播放Assets目录下文件
     * @param view
     */
    public void assets(View view) {
        //EXC内核:mVideoPlayer.setUrl("file:///android_asset/" + "music.mp4");
        if(null!=mVideoPlayer){
            mVideoPlayer.onReset();
            AssetManager am = getResources().getAssets();
            AssetFileDescriptor afd = null;
            try {
                afd = am.openFd("test.mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(null!=afd){
                mVideoPlayer.setDataSource(afd);
                mVideoPlayer.playOrPause();
            }
        }
    }
}