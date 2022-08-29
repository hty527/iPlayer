package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.VideoPlayer;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.video.ui.widget.ControPerviewView;

/**
 * created by hty
 * 2022/8/25
 * Desc:自定义收费试看模式
 */
public class PerviewPlayerActivity extends BaseActivity {

    private long DURATION=3600;//虚拟总时长

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_perview);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
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
//        mVideoPlayer.setPlayCompletionRestoreDirection(false);
        VideoController controller = mVideoPlayer.initController();
        //1、试看需要现设置虚拟总时长
        controller.setPreViewTotalDuration(DURATION+"");//注意:设置虚拟总时长(一旦设置控制器部走片段试看流程)
        //2、添加自己的试看播放完成的交互UI组件
        ControPerviewView controPerviewView=new ControPerviewView(controller.getContext());
        controPerviewView.setOnEventListener(new ControPerviewView.OnEventListener() {
            @Override
            public void onBuy() {
                Logger.d(TAG,"单片购买");
                //如果设置了setPlayCompletionRestoreDirection(false),需先退出横屏
                Toast.makeText(getApplicationContext(),"单片购买",Toast.LENGTH_SHORT).show();
                mVideoPlayer.isBackPressed();
            }

            @Override
            public void onVipBuy() {
                Logger.d(TAG,"会员价格购买");
                Toast.makeText(getApplicationContext(),"会员价格购买",Toast.LENGTH_SHORT).show();
                //如果设置了setPlayCompletionRestoreDirection(false),需先退出横屏
                mVideoPlayer.isBackPressed();
            }
        });
        controller.addControllerWidget(controPerviewView);
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return null;
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                ((TextView) findViewById(R.id.tv_message)).setText("虚拟时长："+ PlayerUtils.getInstance().stringForAudioTime(DURATION*1000)+"\n真实时长："+PlayerUtils.getInstance().stringForAudioTime(mVideoPlayer.getDuration()));
            }
        });
        mVideoPlayer.setDataSource(MP4_URL2);
        mVideoPlayer.prepareAsync();//准备播放
    }
}