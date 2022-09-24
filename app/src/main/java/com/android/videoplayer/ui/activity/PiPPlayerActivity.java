package com.android.videoplayer.ui.activity;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.Logger;
import java.util.ArrayList;

/**
 * created by hty
 * 2022/7/12
 * Desc:画中画示例
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class PiPPlayerActivity extends BaseActivity {

    //画中画交互事件桥梁
    private final PictureInPictureParams.Builder mBuilder = new PictureInPictureParams.Builder();
    private static final String ACTION_MEDIA_CONTROL = "media_control";//intent的事件
    private static final String EXTRA_CONTROL_TYPE = "control_type";//事件类型
    private static final int CONTROL_TYPE_PLAY = 1;
    private static final int CONTROL_TYPE_PAUSE = 2;
    private static final int CONTROL_TYPE_REPLAY = 3;
    private static final int REQUEST_PLAY = 1;//播放事件
    private static final int REQUEST_PAUSE = 2;
    private static final int REQUEST_REPLAY = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip_player);
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

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        mVideoPlayer.getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;
        VideoController controller = mVideoPlayer.createController();//绑定默认的控制器
        WidgetFactory.bindDefaultControls(controller);
        controller.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return ExoPlayerFactory.create().createPlayer(PiPPlayerActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
                switch (state) {
                    case STATE_PREPARE://播放器准备中
                    case STATE_BUFFER://播放过程缓冲中
                        break;
                    case STATE_START://缓冲结束、准备结束 后的开始播放
                    case STATE_PLAY://恢复播放
                    case STATE_ON_PLAY:
                        updatePictureInPictureActions(R.mipmap.ic_player_pause, "播放", CONTROL_TYPE_PLAY, REQUEST_PLAY);
                        break;
                    case STATE_MOBILE://移动网络环境下播放
                        break;
                    case STATE_ON_PAUSE: //手动暂停生命周期暂停
                    case STATE_PAUSE://手动暂停
                        updatePictureInPictureActions(R.mipmap.ic_player_play, "暂停", CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
                        break;
                    case STATE_RESET: //重置
                    case STATE_STOP://停止
                    case STATE_DESTROY://销毁
                    case STATE_COMPLETION://正常的播放器结束
                    case STATE_ERROR://失败
                        updatePictureInPictureActions(R.mipmap.ic_player_window_replay, "重新播放", CONTROL_TYPE_REPLAY, REQUEST_REPLAY);
                        break;
                }
            }
        });
        mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);
        mVideoPlayer.setMobileNetwork(true);
        mVideoPlayer.setLoop(false);
        mVideoPlayer.setProgressCallBackSpaceMilliss(300);
        mVideoPlayer.setDataSource(MP4_URL3);//播放地址设置
        mVideoPlayer.prepareAsync();//开始异步准备播放
    }

    /**
     * 准备开启画中画
     * @param view
     */
    public void enterPicture(View view){
        if(null==mVideoPlayer) return;
        //判断当前设备是否支持画中画
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)){
            Toast.makeText(getApplicationContext(), "当前设备不支持画中画功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//>= Build.VERSION_CODES.N的设备进入画中画提高性能的API支持
            //这边是对画中画时添加一些按钮，如果不需要可以直接使用else里面的enterPictureInPictureMode方法直接进入画中画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Rational aspectRatio = new Rational(16, 9);
                mBuilder.setAspectRatio(aspectRatio).build();
//                builder.setAutoEnterEnabled(true);//提升动画流畅性,Android 12支持
                forbidCycle();//告诉父类忽视生命周期
                hideAllView();
                enterPictureInPictureMode(mBuilder.build());
            }else{
                forbidCycle();//告诉父类忽视生命周期
                hideAllView();
                enterPictureInPictureMode();
            }
        }
    }

    private void hideAllView() {
        findViewById(R.id.title_view).setVisibility(View.GONE);
        findViewById(R.id.btn_enter_picture).setVisibility(View.GONE);
    }

    /**
     * 更新PendingIntent意图
     * @param iconId
     * @param title
     * @param controlType
     * @param requestCode
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final ArrayList<RemoteAction> actions = new ArrayList<>();
            final PendingIntent intent =
                    PendingIntent.getBroadcast(PiPPlayerActivity.this, requestCode,
                            new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType), 0);
            final Icon icon;
            icon = Icon.createWithResource(PiPPlayerActivity.this, iconId);
            actions.add(new RemoteAction(icon, title, title, intent));
            mBuilder.setActions(actions);
            setPictureInPictureParams(mBuilder.build());
        }
    }

    /**
     * 监听进入画中画状态
     * @param isInPictureInPictureMode true:进入画中画 false:退出画中画
     * @param newConfig
     */
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        Logger.d(TAG, "onPictureInPictureModeChanged isPip = " + isInPictureInPictureMode+",newConfig:"+(null!=newConfig?newConfig.toString():""));
        if(!isInPictureInPictureMode){
            findViewById(R.id.title_view).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_enter_picture).setVisibility(View.VISIBLE);
        }
        if(null!=mVideoPlayer){
            if(isInPictureInPictureMode){
                forbidCycle();//告诉父类忽视生命周期
                mVideoPlayer.enterPipWindow();//告诉播放器进入画中画场景
                mVideoPlayer.requestLayout();
                registerActionReceiver();//注册广播事件
            }else{
                if(null!=mReceiver){
                    unregisterReceiver(mReceiver);
                }
                enableCycle();//告诉父类关心生命周期
                mVideoPlayer.quitPipWindow();//告诉播放器退出画中画场景
                mVideoPlayer.requestLayout();
            }
        }
    }

    private BroadcastReceiver mReceiver =new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                return;
            }
            final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
            Logger.d(TAG,"controlType:"+controlType);
            switch (controlType) {
                case CONTROL_TYPE_PLAY:
                case CONTROL_TYPE_PAUSE:
                case CONTROL_TYPE_REPLAY:
                    if(null!=mVideoPlayer) mVideoPlayer.togglePlay();
                    break;
            }
        }
    };

    /**IVideoPlayer
     * 注册广播
     */
    private void registerActionReceiver() {
        registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
    }

    /**
     * 主义:画中画模式直接点击系统悬浮窗关闭按钮时,此界面不会回调onDestroy方法
     */
    @Override
    protected void onStop() {
        super.onStop();
//        Logger.d(TAG,"onStop");
        try {
            unregisterReceiver(mReceiver);
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            mReceiver=null;
            if(null!=mVideoPlayer){
                mVideoPlayer.onDestroy();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Logger.d(TAG,"onDestroy");
        try {
            unregisterReceiver(mReceiver);
        }catch (Throwable e){
            e.printStackTrace();
        }
        mReceiver=null;
    }
}