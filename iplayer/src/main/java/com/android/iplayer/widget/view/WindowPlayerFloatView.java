package com.android.iplayer.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.iplayer.R;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/7/4
 * Desc:
 * 1、解决了Activity级别和全局悬浮窗级别的窗口手势冲突
 * 2、当前View范围内拦截了ACTION_MOVE事件，点击事件不拦截
 * 3、内部根据activity window窗口和全局的悬浮窗窗口最了区别处理
 */
public final class WindowPlayerFloatView extends FrameLayout {

    private static final String TAG="WindowPlayerFloatView";
    private int mGroupWidth,mGroupHeight;
    //手指按下X、Y坐标
    private static float xDownInScreen,yDownInScreen;
    //手指按下此View在屏幕中X、Y坐标,偏移量X,Y
    private float xInView,yInView, translationX,translationY;
    private ViewGroup mPlayerViewGroup;//Activity内的窗口模式下播放器父容器手势拖拽目标View
    private BasePlayer mBasePlayer;//当全局悬浮窗启用时,此播放器实例不为空
    private int mStatusBarHeight;

    public WindowPlayerFloatView(Context context) {
        this(context,null);
    }

    public WindowPlayerFloatView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WindowPlayerFloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.player_window_float,this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);//如果是继承的viewgroup比如linearlayout时，可以先计算
        int widthResult = 0;
        //view根据xml中layout_width和layout_height测量出对应的宽度和高度值，
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthSpecMode){
            //match_parent
            case MeasureSpec.UNSPECIFIED:
                widthResult = widthSpecSize;
                break;
            //wrap_content
            case MeasureSpec.AT_MOST:
                widthResult = getContentWidth();
                break;
            //写死的固定高度
            case MeasureSpec.EXACTLY:
                //当xml布局中是准确的值，比如200dp是，判断一下当前view的宽度和准确值,取两个中大的，这样的好处是当view的宽度本事超过准确值不会出界
                widthResult = Math.max(getContentWidth(), widthSpecSize);
                break;
        }
        int heightResult = 0;
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightSpecMode){
            case MeasureSpec.UNSPECIFIED:
                heightResult = heightSpecSize;
                break;
            case MeasureSpec.AT_MOST:
                heightResult = getContentHeight();
                break;
            case MeasureSpec.EXACTLY:
                heightResult = Math.max(getContentHeight(), heightSpecSize);
                break;
        }
        this.mGroupWidth=widthResult;
        this.mGroupHeight=heightResult;
        setMeasuredDimension(widthResult, heightResult);//测量宽度和高度
    }

    private int getContentWidth(){
        float contentWidth = getWidth()+getPaddingLeft()+getPaddingRight();
        return (int)contentWidth;
    }

    private int getParentViewHeight() {
        return null!=mPlayerViewGroup?mPlayerViewGroup.getHeight():getHeight();
    }

    private int getParentViewWidth() {
        return null!=mPlayerViewGroup?mPlayerViewGroup.getWidth():getWidth();
    }

    private int getContentHeight(){
        float contentHeight = getHeight()+getPaddingTop()+getPaddingBottom();
        return (int)contentHeight;
    }

    private float getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            mStatusBarHeight = PlayerUtils.getInstance().getStatusBarHeight(getContext());
        }
        return mStatusBarHeight;
    }

    // 这里的手势拖拽已被废弃,由新的onTouchEvent代替,解决了滑动手势和播放器的交互手势冲突问题
    // 这种模式是findViewById(R.id.player_window_handel).setOnTouchListener(this);实现接口:View.OnTouchListener 生效
    // 缺点：拦截了子控制器的所有手势事件，拦截移动事件(改由WindiwnGestureListener处理)，点击事件不拦截。
//    @Override
//    public boolean onTouch(View view, MotionEvent e) {
//        ILogger.d(TAG,"onTouch--"+e.getAction());
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                //记录手指按下时手在父View中的位置
//                xInView = e.getX();
//                yInView = e.getY();
//                xDownInScreen =e.getRawX();
//                yDownInScreen = e.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                //实时获取相对于屏幕X,Y位置刷新
//                //手指在屏幕上的实时X、Y坐标
//                float xInScreen,yInScreen;
//                if(null!=mPlayerViewGroup){
//                    xInScreen = e.getRawX();
//                    yInScreen = e.getRawY();
//                    float toX = xInScreen - xInView;
//                    float toY = yInScreen - yInView;
//                    if(toX< 0){
//                        toX= 0;
//                    }else if(toX>(mGroupWidth -getParentViewWidth())){
//                        toX= mGroupWidth -getParentViewWidth();
//                    }
//                    if(toY<0){
//                        toY=0;
//                    }else if(toY>(mGroupHeight -getParentViewHeight())){
//                        toY= mGroupHeight -getParentViewHeight();
//                    }
//                    mPlayerViewGroup.setX(toX);
//                    mPlayerViewGroup.setY(toY);
//                }else{
//                    xInScreen = e.getRawX();
//                    yInScreen = e.getRawY()-getStatusBarHeight();
//                    if(null!= mWindowActionListener){
//                        mWindowActionListener.onMovie((int) (xInScreen - xInView),(int) (yInScreen - yInView));
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                break;
//        }
//        if(null!=mGestureDetector){
//            return mGestureDetector.onTouchEvent(e);
//        }
//        return false;
//    }

    /**
     * 处理拦截事件,是否拦截滑动事件
     * @param e
     * @return true:拦截,播放器将收不到ACTION_MOVE事件 false:不拦截,播放器可接收并处理ACTION_MOVE事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean intercepted = false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                //记录手指按下时手在父View中的位置
                xInView = e.getX();
                yInView = e.getY();
                xDownInScreen = e.getRawX();
                yDownInScreen = e.getRawY();
                if(null!=mPlayerViewGroup){
                    translationX = mPlayerViewGroup.getTranslationX();
                    translationY = mPlayerViewGroup.getTranslationY();
                }
//                ILogger.d(TAG,"onInterceptTouchEvent-->xInView:"+xInView+",yInView:"+yInView+",translationX:"+translationX+",translationY"+translationY);
                break;
            case MotionEvent.ACTION_MOVE:
                float absDeltaX = Math.abs(e.getRawX() - xDownInScreen);
                float absDeltaY = Math.abs(e.getRawY() - yDownInScreen);
                intercepted = absDeltaX > ViewConfiguration.get(getContext()).getScaledTouchSlop() ||
                        absDeltaY > ViewConfiguration.get(getContext()).getScaledTouchSlop();
                break;
        }
        return intercepted;
    }

    /**
     * 当onInterceptTouchEvent返回为true:时,这里能接收到MotionEvent.ACTION_MOVE和ACTION_DOWN事件和
     * 当onInterceptTouchEvent返回false:时,这里将收不到任何手势事件
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float xInScreen,yInScreen;
                //activity内窗口
                if(null!=mPlayerViewGroup){
                    float toX = getX() + (e.getX() - xInView);
                    float toY = getY() + (e.getY() - yInView);
//                    ILogger.d(TAG,"onTouchEvent-->xInView:"+xInView+",yInView:"+yInView+",toX:"+toX+",toY:"+toY);
                    if(toX<=-translationX){//屏幕最左侧
                        toX=-translationX;
                    }else if(toX>=(mGroupWidth-(translationX+getParentViewWidth()))){//屏幕最右侧
                        toX=(mGroupWidth-(translationX+getParentViewWidth()));
                    }
                    if(toY<=-translationY){//屏幕最顶部
                        toY=-translationY;
                    }else if(toY>=(mGroupHeight-(translationY+getParentViewHeight()))){//屏幕最底部
                        toY=(mGroupHeight-(translationY+getParentViewHeight()));
                    }
                    setTranslationX(toX);
                    setTranslationY(toY);
                //全局悬浮窗口
                }else{
                    if(null!= mWindowActionListener){
                        xInScreen = e.getRawX();
                        yInScreen = e.getRawY()-getStatusBarHeight();
                        mWindowActionListener.onMovie((int) (xInScreen - xInView),(int) (yInScreen - yInView));
                    }
                }
                break;
        }
        return super.onTouchEvent(e);
    }

    /**
     * Activity类型窗口调用--将窗口View添加到可推拽的容器中
     * @param basePlayer
     * @param width 窗口组件宽
     * @param height 窗口组件高
     * @param startX 窗口X轴起始位置
     * @param startY 窗口Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     */
    public void addPlayerView(BasePlayer basePlayer,int width,int height,float startX,float startY, float radius, int bgColor){
        if(null==basePlayer )return;
        //被移动的View宽高确定
        mPlayerViewGroup = findViewById(R.id.player_window_group);
        FrameLayout.LayoutParams layoutParams = (LayoutParams) mPlayerViewGroup.getLayoutParams();
        layoutParams.width=width;
        layoutParams.height=height;
        mPlayerViewGroup.setLayoutParams(layoutParams);
        //将播放器添加到容器里
        FrameLayout playerContainer = (FrameLayout) findViewById(R.id.player_window_container);
        playerContainer.addView(basePlayer,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        //初始位置确定
        mPlayerViewGroup.setX(startX);
        mPlayerViewGroup.setY(startY);
        if(radius>0) PlayerUtils.getInstance().setOutlineProvider(mPlayerViewGroup,radius);
        if(bgColor!=0) mPlayerViewGroup.setBackgroundColor(bgColor);

        setListener(basePlayer);
    }

    /**
     * 全局悬浮窗调用--将窗口播放器添加到可推拽的容器中
     * @param basePlayer
     * @param width 窗口组件宽
     * @param height 窗口组件高
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     */
    public void addPlayerView(BasePlayer basePlayer, int width, int height,float radius, int bgColor){
        if(null==basePlayer )return;
        //将播放器添加到容器里
        FrameLayout windowGroup = findViewById(R.id.player_window_group);
        FrameLayout.LayoutParams layoutParams = (LayoutParams) windowGroup.getLayoutParams();
        layoutParams.width=LayoutParams.MATCH_PARENT;
        layoutParams.height=LayoutParams.MATCH_PARENT;
        windowGroup.setLayoutParams(layoutParams);
        FrameLayout playerContainer = (FrameLayout) findViewById(R.id.player_window_container);
        playerContainer.addView(basePlayer,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        if(radius>0) PlayerUtils.getInstance().setOutlineProvider(playerContainer,radius);
        if(bgColor!=0) playerContainer.setBackgroundColor(bgColor);

        setListener(basePlayer);
    }

    /**
     * 设置监听器
     */
    private void setListener(BasePlayer basePlayer) {
        //设置手势识别监听
//        findViewById(R.id.player_window_handel).setOnTouchListener(this);
        //回调给开发者处理跳转
//        if(null!= mWindowActionListener){
//            mWindowActionListener.onClick(mBasePlayer,null);
//        }
        /**
         * 关闭事件,优先通知给开发者处理,如果开发者未监听则直接销毁
         */
        findViewById(R.id.player_window_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!= mWindowActionListener) mWindowActionListener.onClose();
            }
        });
        this.mBasePlayer=basePlayer;
    }

    private OnWindowActionListener mWindowActionListener;

    public void setOnWindowActionListener(OnWindowActionListener listener) {
        mWindowActionListener = listener;
    }

    public BasePlayer getBasePlayer() {
        return mBasePlayer;
    }

    /**
     * 恢复播放
     */
    public void onResume() {
        if(null!=mBasePlayer){
            mBasePlayer.onResume();
        }
    }

    /**
     * 暂停播放
     */
    public void onPause() {
        if(null!=mBasePlayer){
            mBasePlayer.onPause();
        }
    }

    /**
     * 悬浮窗关闭需要调用此接口
     */
    public void onReset(){
        if(null!=mBasePlayer){
            PlayerUtils.getInstance().removeViewFromParent(mBasePlayer);
            mBasePlayer.onReset();
            mBasePlayer.onDestroy();
            mBasePlayer=null;
        }
        xDownInScreen=0;yDownInScreen=0;xInView=0;yInView=0;mStatusBarHeight=0;
    }

    /**
     * 这里一定要处理,可能存在开发者会在没有主动或者被动关闭悬浮窗窗口播放器的时候来添加一个播放器到窗口,所以必须释放此前的窗口播放器
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        xDownInScreen=0;yDownInScreen=0;xInView=0;yInView=0;translationX=0;translationY=0;mStatusBarHeight=0;
        if(null!=mPlayerViewGroup) mPlayerViewGroup.removeAllViews();
    }
}