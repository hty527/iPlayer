package com.android.iplayer.manager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import com.android.iplayer.R;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.WindowPlayerFloatView;

/**
 * created by hty
 * 2022/7/4
 * Desc:全局悬浮窗口播放管理者,开发者可自行调用api来添加自定义的VideoPlayer到窗口中
 */
public class IWindowManager {

    private static final String TAG = "IWindowManager";
    private volatile static IWindowManager mInstance;
    private static WindowManager mWindowManager;
    private WindowPlayerFloatView mPlayerContainer;
    private OnWindowActionListener mWindowActionListener;
    private WindowManager.LayoutParams mLayoutParams;
    private Object mCoustomParams;//自定义参数

    public static synchronized IWindowManager getInstance() {
        synchronized (IWindowManager.class) {
            if (null == mInstance) {
                mInstance = new IWindowManager();
            }
        }
        return mInstance;
    }

    private WindowManager getWindowManager() {
        return getWindowManager(PlayerUtils.getInstance().getContext());
    }

    private WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 将播放器添加到全局窗口中
     * @param context context 上下文
     * @param basePlayer 继承自BasePlayer的播放器实例
     * @param width 窗口播放器的宽
     * @param height 窗口播放器的高
     * @param startX 窗口位于屏幕中的X轴起始位置
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     */
    public boolean addGolbalWindow(Context context, BasePlayer basePlayer, int width, int height, float startX, float startY, float radius, int bgColor) {
        ILogger.d(TAG,"addGolbalWindow-->width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY+",radius:"+radius+",bgColor:"+bgColor);
        quitGlobaWindow();//清除可能存在的窗口播放器
        try {
            //悬浮窗口准备
            WindowManager windowManager = getWindowManager(context);
            mLayoutParams = new WindowManager.LayoutParams();
            //WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            //不拦截焦点、使焦点穿透到底层
            mLayoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //背景透明
            mLayoutParams.format = PixelFormat.RGBA_8888;
            //需要默认位于屏幕的左上角，具体定位用x,y轴
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            mLayoutParams.width = width;
            mLayoutParams.height = height;
            mLayoutParams.x= (int) startX;
            mLayoutParams.y= (int) startY;
            mLayoutParams.windowAnimations = R.style.WindowAnimation;//悬浮窗开启动画
            mPlayerContainer = new WindowPlayerFloatView(context);
            mPlayerContainer.setOnWindowActionListener(new OnWindowActionListener() {
                @Override
                public void onMovie(float x, float y) {
                    ILogger.d(TAG,"onMovie-->x:"+x+",y:"+y);
                    if(null!=mLayoutParams){
                        mLayoutParams.x= (int) x;
                        mLayoutParams.y= (int) y;
                        getWindowManager().updateViewLayout(mPlayerContainer,mLayoutParams);
                    }
                }

                @Override
                public void onClick(BasePlayer basePlayer, Object coustomParams) {
                    ILogger.d(TAG,"onClick-->coustomParams:"+coustomParams);
                    if(null!=mWindowActionListener){
                        mWindowActionListener.onClick(basePlayer,mCoustomParams);
                    }
                }

                @Override
                public void onClose() {
                    ILogger.d(TAG,"addGolbalWindow-->onClose");
                    if(null!=mWindowActionListener){
                        mWindowActionListener.onClose();
                    }else{
                        quitGlobaWindow();
                    }
                }
            });
            mPlayerContainer.addPlayerView(basePlayer,width,height,radius,bgColor);//先将播放器包装到可托拽的容器中
            windowManager.addView(mPlayerContainer, mLayoutParams);
            return true;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 清除悬浮窗所有View
     */
    public void quitGlobaWindow() {
        ILogger.d(TAG,"quitGlobaWindow-->");
        if(null!=mPlayerContainer){
            getWindowManager(mPlayerContainer.getContext()).removeViewImmediate(mPlayerContainer);
            //销毁此前的播放器
            mPlayerContainer.onReset();
            mPlayerContainer=null;
        }
        mLayoutParams=null;mWindowManager=null;mCoustomParams=null;
    }

    public BasePlayer getBasePlayer() {
        if(null!=mPlayerContainer){
            return mPlayerContainer.getBasePlayer();
        }
        return null;
    }

    public Object getCoustomParams() {
        return mCoustomParams;
    }

    /**
     * 设置自定义参数，在收到
     * @param coustomParams
     */
    public IWindowManager setCoustomParams(Object coustomParams) {
        mCoustomParams = coustomParams;
        return mInstance;
    }

    /**
     * 注册监听器,监听点击悬浮窗口播放器事件
     * @param listener
     */
    public void setOnWindowActionListener(OnWindowActionListener listener) {
        mWindowActionListener = listener;
    }

    public OnWindowActionListener getWindowActionListener() {
        return mWindowActionListener;
    }

    /**
     * 提供给窗口控制器调用的,方便将点击事件抛给开发者
     * 为什么要这样写:因为悬浮窗播放器的宿主界面已经不存在了,只能抛给全局的关心点击悬浮窗的监听器来处理
     */
    public void onClickWindow() {
        if(null!=mWindowActionListener&&null!=mPlayerContainer){
            BasePlayer basePlayer = mPlayerContainer.getBasePlayer();
            if(null!=basePlayer){
                mWindowActionListener.onClick(basePlayer,mCoustomParams);
            }
        }
    }

    /**
     * 恢复播放
     */
    public void onResume(){
        if(null!=mPlayerContainer) mPlayerContainer.onResume();
    }

    /**
     * 暂停播放
     */
    public void onPause(){
        if(null!=mPlayerContainer) mPlayerContainer.onPause();
    }

    /**
     * 清除悬浮窗播放器及其容器&&将其还原到常规模式,但不销毁播放器
     */
    public void onClean(){
        BasePlayer basePlayer = getBasePlayer();
        PlayerUtils.getInstance().removeViewFromParent(basePlayer);//从原有全局悬浮窗口移除
        if(null!=basePlayer){
            basePlayer.onRecover();//播放器还原到普通容器请还原播放器内部状态
        }
        if(null!=mPlayerContainer){
            mPlayerContainer.removeAllViews();
            getWindowManager(mPlayerContainer.getContext()).removeViewImmediate(mPlayerContainer);
            mPlayerContainer=null;
        }
        mLayoutParams=null;
    }

    /**
     * 清除悬浮窗
     */
    public void onReset(){
        quitGlobaWindow();
    }
}