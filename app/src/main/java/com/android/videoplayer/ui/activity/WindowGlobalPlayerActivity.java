package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.interfaces.IControllerView;
import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.iplayer.widget.controls.ControlToolBarView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.Logger;

/**
 * created by hty
 * 2022/7/4
 * Desc:activity全局可拖拽悬浮窗窗口播放示例
 * 如何实现点击窗口播放器后的跳转事件：看App中的示例
 * 接收悬浮窗口播放器并继续工作只需要两步：
 * 1.IWindowManager.getInstance().onClean();//清除现有悬浮窗窗口播放器组件但不销毁
 * 2.ViewGroup.addView(IWindowManager.getInstance().getBasePlayer(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));//将播放器添加到你自己的ViewGroup中
 */
public class WindowGlobalPlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_global_window_player);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                onBackPressed();
            }
        });
        findViewById(R.id.btn_goable_window).setVisibility(View.VISIBLE);
        String is_global = getIntent().getStringExtra("is_global");
        String extra = getIntent().getStringExtra("extra");
        Logger.d(TAG,"onCreate-->is_global:"+is_global+",extra:"+extra);
        initPlayer("1".equals(is_global));
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer(boolean isGlobal) {
        BasePlayer basePlayer = IWindowManager.getInstance().getBasePlayer();
        FrameLayout playerParent = (FrameLayout) findViewById(R.id.player_container_parent);
        playerParent.removeAllViews();
        //接收并继续全局的悬浮窗窗口播放器
        if(isGlobal&&null!=basePlayer){
            //1.清除现有悬浮窗窗口播放器组件但不销毁
            IWindowManager.getInstance().onClean();
            mVideoPlayer= (VideoPlayer) basePlayer;
            /**
             * 重要:悬浮窗接收的Activity或其它组件中设置临时的Context为自己，在界面关闭时调用setTempContext(null)置空Context
             */
            mVideoPlayer.setParentContext(this);
            //2.将播放器添加到你自己的ViewGroup中
            playerParent.addView(mVideoPlayer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }else{
            mVideoPlayer = new VideoPlayer(this);
            findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.setDataSource(MP4_URL2);//播放地址设置
            playerParent.addView(mVideoPlayer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

            //为播放器添加控制器和Ui交互组件
            VideoController controller = mVideoPlayer.createController();
            WidgetFactory.bindDefaultControls(controller,false,true);
            controller.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)

            mVideoPlayer.prepareAsync();//开始异步准备播放
        }

        IControllerView controllerView = mVideoPlayer.getController().findControlWidgetByTag(IVideoController.TARGET_CONTROL_TOOL);
        if(null!=controllerView&&controllerView instanceof ControlToolBarView){
            ControlToolBarView toolBarView= (ControlToolBarView) controllerView;
            toolBarView.setOnToolBarActionListener(new ControlToolBarView.OnToolBarActionListener() {
                @Override
                public void onBack() {
                    onBackPressed();
                }

                @Override
                public void onWindow() {
                    WindowGlobalPlayerActivity.this.startGoableWindow(null);
                }
            });
        }

        /**
         * 从悬浮窗窗口跳转过来的播放器重新设置监听器(如果你关心的话)
         * 如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
         */
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return ExoPlayerFactory.create().createPlayer(WindowGlobalPlayerActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
    }

    /**
     * 开启全局悬浮窗播放
     * @param view
     */
    public void startGoableWindow(View view) {
        super.startGoableWindow(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //重要:悬浮窗接收的Activity或其它组件中设置临时的Context为自己，在界面关闭时调用setTempContext(null)置空Context
        if(null!=mVideoPlayer) mVideoPlayer.setParentContext(null);
    }
}