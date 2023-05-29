package com.android.player.ui.activity;

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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.player.R;
import com.android.player.utils.Logger;
import com.android.player.utils.ScreenUtils;
import java.util.ArrayList;

/**
 * created by hty
 * 2022/7/12
 * Desc:画中画示例（生命周期由此类控制）
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class PiPPlayerActivity extends AppCompatActivity {

    private static final String TAG = "PiPPlayerActivity";
    private VideoPlayer mVideoPlayer;
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
    private PipBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip_player);
        initPlayer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(TAG,"onNewIntent-->");
    }

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        mVideoPlayer.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.getInstance().getScreenWidth(), ScreenUtils.getInstance().getScreenWidth() * 9 /16));
        VideoController controller = mVideoPlayer.initController();//绑定默认的控制器
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
        mVideoPlayer.setProgressCallBackSpaceTime(300);
        mVideoPlayer.setDataSource("http://cdnxdc.tanzi88.com/XDC/dvideo/2017/11/29/15f22f48466180232ca50ec25b0711a7.mp4");//播放地址设置
        mVideoPlayer.prepareAsync();
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
                enterPictureInPictureMode(mBuilder.build());
            }else{
                enterPictureInPictureMode();
            }
        }
    }

    /**
     * 更新窗口UI及PendingIntent意图
     * @param iconId
     * @param title
     * @param controlType
     * @param requestCode
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final ArrayList<RemoteAction> actions = new ArrayList<>();
            final PendingIntent intent = PendingIntent.getBroadcast(PiPPlayerActivity.this, requestCode, new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType), 0);
            actions.add(new RemoteAction(Icon.createWithResource(PiPPlayerActivity.this, iconId), title, title, intent));
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
        if(null==mVideoPlayer) return;
        if(isInPictureInPictureMode){
            mVideoPlayer.enterPipWindow();//告诉播放器进入画中画场景(画中画场景时controller不可用)
            mVideoPlayer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mReceiver = new PipBroadcastReceiver();
            registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));//注册广播事件
        }else{
            release();
            mVideoPlayer.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.getInstance().getScreenWidth(), ScreenUtils.getInstance().getScreenWidth() * 9 /16));
            mVideoPlayer.quitPipWindow();//告诉播放器退出画中画场景(退出画中画场景时恢复controller可用)
        }
    }

    private class PipBroadcastReceiver extends BroadcastReceiver{

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
    }

    /**
     * 动态更新窗口宽高比例
     */
    private void updateLayoutParams(int widthScale,int heightScale){
        //设置param宽高比,根据快高比例调整初始参数
        Rational aspectRatio = new Rational(widthScale, heightScale);
        mBuilder.setAspectRatio(aspectRatio);
        //设置更新PictureInPictureParams
        setPictureInPictureParams(mBuilder.build());
    }

    private void release() {
        if(null!=mReceiver){
            try {
                unregisterReceiver(mReceiver);
                mReceiver=null;
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 离开画中画返回到全屏，
     * 全屏播放状态下下锁屏/解锁 onPause ,onStop /  onStart,onResume
     * 画中画状态下下锁屏/解锁 onStop /  onStart
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 离开全屏进入到画中画
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null!=mVideoPlayer) mVideoPlayer.togglePlay();
    }

    /**
     * 关闭画中画
     */
    @Override
    protected void onStop() {
        super.onStop();
        release();
    }

    @Override
    public void onBackPressed() {
        if(mVideoPlayer.isBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        release();
        if(null!=mVideoPlayer){
            mVideoPlayer.onDestroy();
        }
        super.onDestroy();
    }
}