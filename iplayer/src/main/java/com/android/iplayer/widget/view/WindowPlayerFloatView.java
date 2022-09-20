package com.android.iplayer.widget.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.android.iplayer.R;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/7/4
 * Desc:Activity窗口和全局悬浮窗窗口播放器的容器包装，处理了手势操作
 * 1、解决了Activity级别和全局悬浮窗级别的窗口手势冲突
 * 2、当前View范围内拦截了ACTION_MOVE事件，点击事件不拦截
 * 3、内部根据activity window窗口和全局的悬浮窗窗口做了区别处理
 * 4、用户松手后自动吸附至屏幕最近的X轴边缘,距离边缘12dp位置悬停
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
    private int mStatusBarHeight,mHorMargin,mScreenWidth,mScreenHeight,mOldToPixelX;//状态栏高度,吸附至屏幕边缘的边距,屏幕宽,屏幕高,实时的上一次平移偏移量X轴像素点
    private boolean isAutoSorption=false;//是否自动吸附

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
                //activity内窗口
                if(null!=mPlayerViewGroup){
                    float toX = getX() + (e.getX() - xInView);
                    float toY = getY() + (e.getY() - yInView);
//                    ILogger.d(TAG,"onTouchEvent-->getX():"+getX()+",e.getX():"+e.getX()+",toX:"+toX+",toY:"+toY);
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
                        float xInScreen,yInScreen;
                        xInScreen = e.getRawX();
                        yInScreen = e.getRawY()-getStatusBarHeight();
                        mWindowActionListener.onMovie((int) (xInScreen - xInView),(int) (yInScreen - yInView));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                adsorptionDisplay();
                break;
        }
        return super.onTouchEvent(e);
    }

    /**
     * 检测是否需要自动吸附到屏幕边缘
     */
    private void adsorptionDisplay() {
        if(!isAutoSorption) return;
        this.mOldToPixelX=0;
//        xInView=0;yInView=0;
        int[] locations=new int[2];
        if(null!=mPlayerViewGroup){
            mPlayerViewGroup.getLocationInWindow(locations);//Activity悬浮窗口
        }else{
            getLocationOnScreen(locations);//全局悬浮窗口
        }
        int centerX=locations[0]+(getParentViewWidth()/2);
        scrollToPixel(locations[0],centerX,200);
    }

    /**
     * 自动滚动并吸附至屏幕边缘
     * @param startX 窗口当前在屏幕的X点
     * @param centerX 播放器位于屏幕的X中心点
     * @param scrollDurtion 滚动时间，单位：毫秒
     */
    private void scrollToPixel(final int startX, int centerX, long scrollDurtion) {
        try {
            boolean isLeft=true;
            int toPixelX=getHorMargin();//初始的默认停靠在左侧15dp处
            if(centerX>(getScreenWidth()/2)){//检测是否在屏幕右侧
                //右边停靠最大X：屏幕宽-自身宽-边距大小
                isLeft=false;
                toPixelX=(getScreenWidth()-getParentViewWidth()- getHorMargin());
            }
            if(scrollDurtion<=0){
                moveToX(startX,toPixelX,isLeft);
                return;
            }
//            ILogger.d(TAG,"scrollToPixel,startX:"+startX+",toPixelX:"+toPixelX+",centerX:"+centerX);
            @SuppressLint("ObjectAnimatorBinding") ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "number", startX, toPixelX);
            objectAnimator.setDuration(scrollDurtion);
            objectAnimator.setInterpolator(new LinearInterpolator());
            final boolean finalIsLeft = isLeft;
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int animatedValue = (int) valueAnimator.getAnimatedValue();
                    moveToX(startX,animatedValue, finalIsLeft);
                }
            });
            objectAnimator.start();
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 移动至某个位于屏幕的x点
     * @param startX 起点,位于屏幕的x点
     * @param toPixelX 终点,位于屏幕的x点
     * @param isLeft 是否往左边吸附悬停,true:往左边吸附悬停,false:往右边吸附悬停
     */
    private void moveToX(int startX,int toPixelX,boolean isLeft) {
        //Activity级别悬浮窗口
        if(null!=mPlayerViewGroup){
            //根据实时偏移量计算当前应当偏移多少像素点
            float translationX = getTranslationX();
            float toTranslationX=0;
//            ILogger.d(TAG,"moveToX-->translationX:"+translationX+",toPixelX:"+toPixelX+",startX:"+startX+",leLeft:"+isLeft);
            if(isLeft){//往左边越来越小，也就是TranslationX偏移量越来越大
                int offset=0==mOldToPixelX?startX-toPixelX:mOldToPixelX-toPixelX;//本次往左边偏移量
                toTranslationX=translationX-offset;//最终递减往左边偏移量
//                ILogger.d(TAG,"moveToLeftX-->offset:"+offset+",toTranslationX:"+toTranslationX);
            }else {
                int offset=0==mOldToPixelX?toPixelX-startX:toPixelX-mOldToPixelX;
                toTranslationX=translationX+offset;//最终累加往右边偏移量
//                ILogger.d(TAG,"moveToRightX-->offset:"+offset+",toTranslationX:"+toTranslationX);
            }
            setTranslationX(toTranslationX);
            this.mOldToPixelX=toPixelX;
        }else{
            //全局悬浮窗口，交给WindowManager更新位置
            if(null!= mWindowActionListener) mWindowActionListener.onMovie(toPixelX,-1);
        }
    }

    private int getHorMargin(){
        if(0==mHorMargin){
            mHorMargin = PlayerUtils.getInstance().dpToPxInt(12f);
        }
        return mHorMargin;
    }

    private int getScreenWidth(){
        if(0==mScreenWidth){
            mScreenWidth = PlayerUtils.getInstance().getScreenWidth(getContext());
        }
        return mScreenWidth;
    }

    private int getScreenHeight(){
        if(0==mScreenHeight){
            mScreenHeight = PlayerUtils.getInstance().getScreenHeight(getContext());
        }
        return mScreenHeight;
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
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘
     */
    public void addPlayerView(BasePlayer basePlayer,int width,int height,float startX,float startY, float radius, int bgColor,boolean isAutoSorption){
        if(null==basePlayer )return;
        this.isAutoSorption=isAutoSorption;
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
//        adsorptionDisplay();//防止参数调用意外，处理自动吸附
    }

    /**
     * 全局悬浮窗调用--将窗口播放器添加到可推拽的容器中,选举悬浮窗窗口的宽高被WindowManager.LayoutParams约束
     * @param basePlayer
     * @param width 窗口组件宽
     * @param height 窗口组件高
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘
     */
    public void addPlayerView(BasePlayer basePlayer, int width, int height,float radius, int bgColor,boolean isAutoSorption){
        if(null==basePlayer )return;
        this.isAutoSorption=isAutoSorption;
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
//        adsorptionDisplay();//防止参数调用意外，处理自动吸附
    }

    /**
     * 设置监听器
     */
    private void setListener(BasePlayer basePlayer) {
        /**
         * 关闭事件,优先通知给开发者处理,如果开发者未监听则直接销毁播放器
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