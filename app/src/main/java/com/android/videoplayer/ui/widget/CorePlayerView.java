package com.android.videoplayer.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.VideoPlayer;
import com.android.videoplayer.R;
import com.android.videoplayer.media.ExoMediaPlayer;
import com.android.videoplayer.media.JkMediaPlayer;

/**
 * created by hty
 * 2022/8/9
 * Desc:支持解码器交互的简单播放器封装交互组件
 */
public class CorePlayerView extends LinearLayout {

//    private static final String URL = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4";//惊奇队长
    private static final String URL = "http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4";
    protected VideoPlayer mVideoPlayer;
    private int mCurrentMediaCore;//当前正在使用的解码器类型 0:系统 1:ijk 2:exo

    public CorePlayerView(Context context) {
        this(context, null);
    }

    public CorePlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CorePlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_player_core, this);
        mVideoPlayer = findViewById(R.id.view_video_player);
        findViewById(R.id.view_player_container).getLayoutParams().height = getResources().getDisplayMetrics().widthPixels * 9 / 16;//给播放器固定一个高度
        VideoController controller = mVideoPlayer.initController();
        controller.showBackBtn(false);//竖屏下是否显示返回按钮
        controller.showMenus(false, false, false);//是否显示右上角菜单栏功能按钮
        controller.setCanTouchInPortrait(true);
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                if (1 == mCurrentMediaCore) {
                    return new JkMediaPlayer(getContext());//IJK解码器
                } else if (2 == mCurrentMediaCore) {
                    return new ExoMediaPlayer(getContext());//EXO解码器
                } else {
                    return null;//播放器内部默认的
                }
            }
        });
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.view_core_1:
                        if(0!=mCurrentMediaCore){
                            findViewById(R.id.view_core_1).setSelected(true);
                            findViewById(R.id.view_core_2).setSelected(false);
                            findViewById(R.id.view_core_3).setSelected(false);
                            mCurrentMediaCore = 0;
                            rePlay();
                        }
                        break;
                    case R.id.view_core_2:
                        if(1!=mCurrentMediaCore){
                            findViewById(R.id.view_core_1).setSelected(false);
                            findViewById(R.id.view_core_2).setSelected(true);
                            findViewById(R.id.view_core_3).setSelected(false);
                            mCurrentMediaCore = 1;
                            rePlay();
                        }
                        break;
                    case R.id.view_core_3:
                        if(2!=mCurrentMediaCore){
                            findViewById(R.id.view_core_1).setSelected(false);
                            findViewById(R.id.view_core_2).setSelected(false);
                            findViewById(R.id.view_core_3).setSelected(true);
                            mCurrentMediaCore = 2;
                            rePlay();
                        }
                        break;
                }

            }
        };
        mCurrentMediaCore=0;
        findViewById(R.id.view_core_1).setSelected(true);
        findViewById(R.id.view_core_1).setOnClickListener(onClickListener);
        findViewById(R.id.view_core_2).setOnClickListener(onClickListener);
        findViewById(R.id.view_core_3).setOnClickListener(onClickListener);
    }

    /**
     * 重新播放
     */
    private void rePlay() {
        if (null != mVideoPlayer) {
            mVideoPlayer.onReset();
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(URL);//播放地址设置
            mVideoPlayer.playOrPause();//开始异步准备播放
        }
    }

    public void onResume() {
        if (null != mVideoPlayer) mVideoPlayer.onResume();
    }

    public void onPause() {
        if (null != mVideoPlayer) mVideoPlayer.onPause();
    }

    public boolean isBackPressed() {
        if (null != mVideoPlayer) {
            if (mVideoPlayer.isBackPressed()) {
                return true;
            }
            return false;
        }
        return true;
    }

    public void onDestroy() {
        if (null != mVideoPlayer) mVideoPlayer.onDestroy();
    }

    public void start() {
        if (null != mVideoPlayer) {
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(URL);//播放地址设置
            mVideoPlayer.playOrPause();//开始异步准备播放
        }
    }
}