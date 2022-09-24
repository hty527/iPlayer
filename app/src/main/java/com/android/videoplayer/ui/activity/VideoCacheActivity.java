package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.video.cache.VideoCache;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.Logger;

/**
 * created by hty
 * 2022/9/3
 * Desc:视频文件预缓存示例
 * 边播边存直接使用api:VideoCache.getInstance().getPlayUrl(rawUrl)
 */
public class VideoCacheActivity extends BaseActivity {

    public static final String CACHE_URL=MP4_URL2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_cache);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                onBackPressed();
            }
        });
        initPlayer();
        ((TextView) findViewById(R.id.tv_state)).setText(String.format("缓存状态(若已缓存状态未更新可重新进入此界面)：%s",VideoCache.getInstance().isPreloaded(CACHE_URL)));
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer() {
        //播放器播放之前准备工作
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        WidgetFactory.bindDefaultControls(mVideoPlayer.createController());//使用默认的UI交互
        mVideoPlayer.setLandscapeWindowTranslucent(true);
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return ExoPlayerFactory.create().createPlayer(VideoCacheActivity.this);
            }
        });
        findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//给播放器固定一个高度
    }

    public void cacheStart(View view) {
        VideoCache.getInstance().startPreloadTask(CACHE_URL,50*1024*1024);//预缓存10M
    }

    public void cachePause(View view) {
        VideoCache.getInstance().pausePreload();
    }

    public void cacheResume(View view) {
        VideoCache.getInstance().resumePreload();
    }

    public void cacheStop(View view) {
        VideoCache.getInstance().stopllPreloadTask();
    }

    public void playerStart(View view) {
        String playUrl = VideoCache.getInstance().getPlayPreloadUrl(CACHE_URL);
        Logger.d(TAG,"getPlayPreloadUrl:"+playUrl);
        mVideoPlayer.getController().setTitle("弹幕视频测试播放地址");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer.setDataSource(playUrl);//播放地址设置
        mVideoPlayer.prepareAsync();//开始异步准备播放
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoCache.getInstance().removeAllPreloadTask();
    }
}