package com.android.player.ui.activity;

import android.os.Bundle;
import android.view.View;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.player.R;
import com.android.player.base.BaseActivity;
import com.android.player.base.BasePresenter;
import com.android.player.ui.widget.TitleView;
import com.android.player.utils.ScreenUtils;

/**
 * created by hty
 * 2023/5/26
 * Desc:Pad和Tv
 */
public class PadActivity extends BaseActivity {

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                onBackPressed();
            }
        });
        findViewById(R.id.fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**！！！重点！！！：必须在启动全屏前调用下列方法禁止旋转Activity方向**/
                //mVideoPlayer.shutFullScreenOrientation();
                mVideoPlayer.toggleFullScreen();
            }
        });
        initPlayer();
    }

    private void initPlayer() {
        findViewById(R.id.player_container).getLayoutParams().height= ScreenUtils.getInstance().dpToPxInt(300f) * 10 /16;//给播放器固定一个高度
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        VideoController controller = mVideoPlayer.createController();//绑定默认的控制器
        WidgetFactory.bindDefaultControls(controller);
        controller.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public void onPlayerState(PlayerState state, String message) {
                if(PlayerState.STATE_START==state){
                    findViewById(R.id.fullscreen).setVisibility(View.VISIBLE);
                }
            }
        });
        mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);
        mVideoPlayer.setLoop(true);
        mVideoPlayer.setProgressCallBackSpaceTime(300);
        /**！！！重点！！！：关闭、退出全屏时禁止旋转Activity方向，默认开启**/
        mVideoPlayer.shutFullScreenOrientation();
        mVideoPlayer.setDataSource(MP4_URL3);//播放地址设置
        mVideoPlayer.prepareAsync();//开始异步准备播放
    }
}