package com.android.videoplayer.ui.activity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.widget.VideoPlayer;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
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
        titleView.setTitle(getIntent().getStringExtra("title"));
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
        VideoController controller = mVideoPlayer.initController(false,false);//绑定默认的控制器
        mVideoPlayer.setLoop(false);
        mVideoPlayer.setProgressCallBackSpaceMilliss(300);
        mVideoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
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
            mVideoPlayer.prepareAsync();
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
                mVideoPlayer.prepareAsync();
            }
        }
    }
}