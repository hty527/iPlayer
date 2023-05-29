package com.android.iplayer.manager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.iplayer.R;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.utils.ILogger;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.view.WindowPlayerFloatView;

/**
 * created by hty
 * 2022/7/4
 * Desc:全局悬浮窗口播放管理者,开发者可自行调用api来添加自定义的VideoPlayer到窗口中
 */
public final class IWindowManager {

    private static final String TAG = "IWindowManager";
    private volatile static IWindowManager mInstance;
    //以下二个变量将时常驻内存，只初始化一次
    private static WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private WindowPlayerFloatView mPlayerContainer;
    private OnWindowActionListener mWindowActionListener;
    private Object mCustomParams;//自定义参数

    public static synchronized IWindowManager getInstance() {
        synchronized (IWindowManager.class) {
            if (null == mInstance) {
                mInstance = new IWindowManager();
            }
        }
        return mInstance;
    }

    private IWindowManager(){}

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
     * 初始化全局悬浮窗初始化参数
     * @param context 播放器上下文
     * @param basePlayer 播放器
     * @param width 悬浮窗的宽，默认为：屏幕宽度/2+30dp
     * @param height 悬浮窗的高，默认为：width*9/16
     * @param startX 位于屏幕的X起始位置，如果为0第一次渲染全局悬浮窗时：屏幕宽度/2-30dp-12dp；非初次渲染全局悬浮窗：使用最后一次关闭窗口前的位置
     * @param startY 位于屏幕的Y起始位置，如果为0第一次渲染全局悬浮窗时：播放器位于屏幕的Y轴+播放器高度+边距(12dp)；非初次渲染全局悬浮窗：使用最后一次关闭窗口前的位置
     */
    private void initParams(Context context,BasePlayer basePlayer,int width,int height,float startX,float startY) {
        if(null==mLayoutParams){
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

            int[] screenLocation=new int[2];
            ViewGroup parent=null;
            //1.从原有竖屏窗口移除自己前保存自己的Parent,直接开启全屏是不存在宿主ViewGroup的,可直接窗口转场
            if(null!=basePlayer.getParent()&& basePlayer.getParent() instanceof ViewGroup){
                parent = (ViewGroup) basePlayer.getParent();
                parent.getLocationInWindow(screenLocation);
            }
            //2.获取宿主的View属性和startX、Y轴
            if(width<=0||height<=0){
                width = PlayerUtils.getInstance().getScreenWidth(context)/2+PlayerUtils.getInstance().dpToPxInt(30f);
                height = width*9/16;
//                ILogger.d(TAG,"initParams-->未传入宽或高,width:"+width+",height:"+height);
            }
            //如果传入的startX不存在，则startX起点位于屏幕宽度1/2-距离右侧15dp位置，startY起点位于宿主View的下方12dp处
            if(startX<=0&&null!=parent){
                startX=(PlayerUtils.getInstance().getScreenWidth(context)/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(12f);
                startY=screenLocation[1]+parent.getHeight()+PlayerUtils.getInstance().dpToPxInt(12f);
//                ILogger.d(TAG,"initParams-->未传入X,Y轴,取父容器位置,startX:"+startX+",startY:"+startY);
            }
            //如果宿主也不存在，则startX起点位于屏幕宽度1/2-距离右侧12dp位置，startY起点位于屏幕高度-Window View 高度+12dp位置处
            if(startX<=0){
                startX=(PlayerUtils.getInstance().getScreenWidth(context)/2-PlayerUtils.getInstance().dpToPxInt(30f))-PlayerUtils.getInstance().dpToPxInt(12f);
                startY=PlayerUtils.getInstance().dpToPxInt(60f);
//                ILogger.d(TAG,"initParams-->未传入X,Y轴或取父容器位置失败,startX:"+startX+",startY:"+startY);
            }
//            ILogger.d(TAG,"initParams-->final:width:"+width+",height:"+height+",startX:"+startX+",startY:"+startY);
            mLayoutParams.width = width;
            mLayoutParams.height = height;
            mLayoutParams.x= (int) startX;
            mLayoutParams.y= (int) startY;
            mLayoutParams.windowAnimations = R.style.WindowAnimation;//悬浮窗开启动画
        }else{
            if(width>0||height>0){
                mLayoutParams.width = width;
                mLayoutParams.height = height;
            }
        }
    }

    /**
     * 将播放器添加到全局窗口中
     * @param context context 上下文
     * @param basePlayer 继承自BasePlayer的播放器实例
     * @param width 悬浮窗的宽，默认为：屏幕宽度/2+30dp
     * @param height 悬浮窗的高，默认为：width*9/16
     * @param startX 位于屏幕的X起始位置，如果为0第一次渲染全局悬浮窗时：屏幕宽度/2-30dp-12dp；非初次渲染全局悬浮窗：使用最后一次关闭窗口前的位置
     * @param startY 位于屏幕的Y起始位置，如果为0第一次渲染全局悬浮窗时：播放器位于屏幕的Y轴+播放器高度+边距(12dp)；非初次渲染全局悬浮窗：使用最后一次关闭窗口前的位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘
     */
    public boolean addGlobalWindow(Context context, BasePlayer basePlayer, int width, int height, float startX, float startY, float radius, int bgColor, boolean isAutoSorption ) {
        quitGlobalWindow();//清除可能存在的窗口播放器
        try {
            //悬浮窗口准备
            WindowManager windowManager = getWindowManager(context);
            initParams(context,basePlayer,width,height,startX,startY);
            //从原宿主中移除播放器
            PlayerUtils.getInstance().removeViewFromParent(basePlayer);
            //初始化一个装载播放器的手势容器
            mPlayerContainer = new WindowPlayerFloatView(context);
            mPlayerContainer.setOnWindowActionListener(new OnWindowActionListener() {
                @Override
                public void onMovie(float x, float y) {
                    if(null!=mLayoutParams){
                        mLayoutParams.x= (int) x;
                        if(-1!=y){//过滤自动吸附事件
                            mLayoutParams.y= (int) y;
                        }
                        getWindowManager().updateViewLayout(mPlayerContainer,mLayoutParams);
                    }
                }

                @Override
                public void onClick(BasePlayer basePlayer, Object customParams) {
                    ILogger.d(TAG,"onClick-->customParams:"+customParams);
                    if(null!=mWindowActionListener){
                        mWindowActionListener.onClick(basePlayer, mCustomParams);
                    }
                }

                @Override
                public void onClose() {
                    if(null!=mWindowActionListener){
                        mWindowActionListener.onClose();
                    }else{
                        quitGlobalWindow();
                    }
                }
            });
            windowManager.addView(mPlayerContainer, mLayoutParams);
            mPlayerContainer.addPlayerView(basePlayer,width,height,radius,bgColor,isAutoSorption);//先将播放器包装到可托拽的容器中
            return true;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 清除悬浮窗所有View
     */
    public void quitGlobalWindow() {
        if(null!=mPlayerContainer){
            getWindowManager(mPlayerContainer.getContext()).removeViewImmediate(mPlayerContainer);
            //销毁此前的播放器
            mPlayerContainer.onReset();
            mPlayerContainer=null;
        }
        mCustomParams =null;
    }

    public BasePlayer getBasePlayer() {
        if(null!=mPlayerContainer){
            return mPlayerContainer.getBasePlayer();
        }
        return null;
    }

    public Object getCustomParams() {
        return mCustomParams;
    }

    /**
     * 设置自定义参数，在收到
     * @param coustomParams
     */
    public IWindowManager setCustomParams(Object coustomParams) {
        mCustomParams = coustomParams;
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
                mWindowActionListener.onClick(basePlayer, mCustomParams);
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
    }

    /**
     * 清除悬浮窗及所有设置
     */
    public void onReset(){
        quitGlobalWindow();
        mLayoutParams=null;mWindowManager=null;
    }
}