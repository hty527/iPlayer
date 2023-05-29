package com.android.player.pager.widget;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.player.pager.interfaces.OnViewPagerListener;
import com.android.player.utils.Logger;

/**
 * created by hty
 * 2022/7/4
 * Desc:这是一个适用于RecyclerView ITEM分页(item占满全屏)LayoutManager,回调抛出选中的项和正要被销毁的项
 */
public class ViewPagerLayoutManager extends LinearLayoutManager implements RecyclerView.OnChildAttachStateChangeListener {

    private static final String TAG = "ViewPagerLayoutManager";
    private PagerSnapHelper mPagerSnapHelper;
    private OnViewPagerListener mOnViewPagerListener;
    private int mDrift;//位移，用来判断移动方向
    private int mCurrentPostion;//当前选中的项
    private boolean haveSelect;//初次选中标识

    public ViewPagerLayoutManager(Context context) {
        this(context, LinearLayoutManager.VERTICAL);
    }

    public ViewPagerLayoutManager(Context context, int orientation) {
        this(context, orientation, false);
    }

    public ViewPagerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mPagerSnapHelper = new PagerSnapHelper();
    }

    @Override
    public void onAttachedToWindow(RecyclerView recyclerView) {
        super.onAttachedToWindow(recyclerView);
        if(null!=mPagerSnapHelper){
            mPagerSnapHelper.attachToRecyclerView(recyclerView);
        }
        recyclerView.addOnChildAttachStateChangeListener(this);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if(null!=mPagerSnapHelper){
                View viewIdle = mPagerSnapHelper.findSnapView(ViewPagerLayoutManager.this);
                if(null!=viewIdle){
                    int position = getPosition(viewIdle);
                    Logger.d(TAG,"onScrollStateChanged-->position:"+position+",currentPostion:"+mCurrentPostion);
                    //过滤重复选中
                    if (mOnViewPagerListener != null && this.mCurrentPostion != position) {
                        this.mCurrentPostion = position;
                        mOnViewPagerListener.onPageSelected(viewIdle, position,position==getItemCount()-2);
                    }
                }
            }
        }
    }

    /**
     * 监听竖直方向的相对偏移量
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.mDrift = dy;
        return super.scrollVerticallyBy(dy, recycler, state);
    }


    /**
     * 监听水平方向的相对偏移量
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.mDrift = dx;
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    /**
     * 设置监听
     */
    public void setOnViewPagerListener(OnViewPagerListener listener){
        this.mOnViewPagerListener = listener;
    }

    /**
     * 默认初始选中用户scrollToPositionWithOffset中的项,不调用scrollToPositionWithOffset默认选中第0个
     * @param view
     */
    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        if(!haveSelect){
            this.haveSelect = true;
            this.mCurrentPostion = this.getPosition(view);
            if(null!=mOnViewPagerListener){
                mOnViewPagerListener.onPageSelected(view,mCurrentPostion,mCurrentPostion==getItemCount()-2);
            }
        }
    }

    /**
     * 及时通知宿主销毁
     * @param view
     */
    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        int position = getPosition(view);
        Logger.d(TAG,"onChildViewDetachedFromWindow-->position:"+position+",mCurrentPostion:"+mCurrentPostion);
        if(mCurrentPostion==position){//只回调证在被选中的Item,宿主需要判断播放器播放时不能销毁正在播放的项
            if (mOnViewPagerListener != null) mOnViewPagerListener.onPageRelease(view,this.mDrift >= 0,position);
        }
    }

    public void onReset() {
        this.mCurrentPostion = 0;
        this.mDrift = 0;
        this.haveSelect=false;
    }
}