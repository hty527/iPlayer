package com.android.videoplayer.base;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.iplayer.listener.OnMenuActionListener;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.VideoPlayer;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;
import com.android.videoplayer.video.ui.widget.PlayerMenuDialog;

/**
 * created by hty
 * 2022/6/29
 * Desc:
 */
public abstract class BaseActivity <P extends BasePresenter> extends AppCompatActivity implements BaseContract.BaseView {

    protected static final String TAG = "BaseActivity";
    protected P mPresenter;
    protected static final String URL1="http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4";
    protected static final String URL2="https://upload.dongfeng-nissan.com.cn/nissan/video/202204/4cfde6f0-bf80-11ec-95c3-214c38efbbc8.mp4";
    protected static final String URL3="http://cdnxdc.tanzi88.com/XDC/dvideo/2017/11/29/15f22f48466180232ca50ec25b0711a7.mp4";
    protected static final String URL4="https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4";
//    protected static final String URL4="http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4";
    protected static final String M3U8="http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";//直播测试流
    protected PlayerMenuDialog mMenuDialog;//功能交互菜单
    private boolean isFullScreen=false,isForbidCycle=false;//是否开启全屏模式,是否禁止生命周期(悬浮窗必须设置)
    protected VideoPlayer mVideoPlayer;

    protected void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter=createPresenter();
        if(null!=mPresenter){
            mPresenter.attachView(this);
        }
        if(isFullScreen){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //系统版本大于19
                setTranslucentStatus(true);
            }
            //Android5.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 交由子类实现自己指定的Presenter,可以为空
     * @return 子类持有的继承自BasePresenter的Presenter
     */
    protected abstract P createPresenter();

    /**
     * 由子类调用,告诉父类忽视生命周期
     */
    protected void forbidCycle(){
        this.isForbidCycle=true;
    }

    /**
     * 由子类调用,告诉父类关心生命周期
     */
    protected void enableCycle(){
        this.isForbidCycle=false;
    }

    protected void destroyPlayer(){
        if(null!=mVideoPlayer){
            mVideoPlayer.onDestroy();
            mVideoPlayer=null;
        }
    }

    /**
     * 功能菜单
     */
    protected void showMenuDialog() {
        try {
            if(null==mMenuDialog){
                mMenuDialog = new PlayerMenuDialog(this);
                mMenuDialog.setOnMenuActionListener(new OnMenuActionListener() {
                    @Override
                    public void onSpeed(float speed) {
                        if(null!=mVideoPlayer) mVideoPlayer.setSpeed(speed);
                    }

                    @Override
                    public void onZoom(int zoomModel) {
                        if(null!=mVideoPlayer) mVideoPlayer.setZoomModel(zoomModel);
                    }

                    @Override
                    public void onScale(int scale) {

                    }

                    @Override
                    public void onMute(boolean mute) {
                        if(null!=mVideoPlayer) mVideoPlayer.setSoundMute(mute);
                    }

                    @Override
                    public void onMirror(boolean mirror) {
                        if(null!=mVideoPlayer) mVideoPlayer.setMirror(mirror);
                    }
                });
            }
            mMenuDialog.show();
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 开启全局悬浮窗播放,成功开启全局悬浮窗后当前界面将被finish界面
     * @param coustomParams 自定义参数
     */
    protected void startGoableWindow(Object coustomParams) {
        if(null!=mVideoPlayer){
            /**
             * 如需关心点击播放器事件,则需要在自己的Application中注册IWindowManager中的监听器setWindowActionListener
             * 如果点击悬浮窗口播放器打开自己的Activity时有自定义参数，可调用IWindowManager中的coustomParams传入自定义参数，方便在收到悬浮窗点击事件后的Activity跳转
             */
//            //示例代码：
//            IWindowManager.getInstance().setOnWindowActionListener(new OnWindowActionListener() {
//                @Override
//                public void onMovie(float x, float y) {
//
//                }
//
//                @Override
//                public void onClick(BasePlayer basePlayer, Object coustomParams) {
//                    Logger.d(TAG,"onClick-->coustomParams:"+coustomParams);
//                    Intent intent=new Intent(context, WindowGlobalPlayerActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("is_global","1");//是否接收并继续播放悬浮窗口的视频
//                    intent.putExtra("extra", (String) coustomParams);//示例GlobalWindowPlayerActivity中传入的是字符串,所以这里可以强转
//                    context.startActivity(intent);
//                }
//
//                @Override
//                public void onClose() {
//                    Logger.d(TAG,"onClose-->");
//                    IWindowManager.getInstance().quitGlobaWindow();//关闭悬浮窗播放器窗口
//                }
//            });
            boolean globalWindow = mVideoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
            Logger.d(TAG,"startGoableWindow-->globalWindow:"+globalWindow);
            if(globalWindow){
                IWindowManager.getInstance().setCoustomParams(coustomParams);
                forbidCycle();
                //跳转后设置自定义参数，将在onClick中回调
                finish();//开启悬浮窗必须关闭Activity
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isForbidCycle&&null!=mVideoPlayer) mVideoPlayer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isForbidCycle&&null!=mVideoPlayer) mVideoPlayer.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Logger.d(TAG,"onBackPressed-->"+isForbidCycle);
        if(!isForbidCycle&&null!=mVideoPlayer){
            if(mVideoPlayer.isBackPressed()){
                super.onBackPressed();
            }
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mMenuDialog){
            mMenuDialog.dismiss();
            mMenuDialog=null;
        }
        if(!isForbidCycle&&null!=mVideoPlayer) mVideoPlayer.onDestroy();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showError(int code, String errorMsg) {

    }
}