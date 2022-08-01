package com.android.videoplayer.pager.interfaces;

import android.view.View;

/**
 * created by hty
 * 2022/7/4
 * Desc:RecyclerView仿ViewPager的片段选择器器监听
 */
public interface OnViewPagerListener {
    /**
     * @param view 当前正在被释放的itemView
     * @param isNext 是否还有下一条视频 true:有 false:没有
     * @param position 当前被释放的item位置
     */
    void onPageRelease(View view,boolean isNext, int position);

    /**
     * @param view 当前选中的itemView
     * @param position 当前选中的item位置
     * @param isBottom 是否滑动到底部 true:当前position已经到<=(最后一条数据-2)的位置了,在这里加载更多数据 flase:否
     */
    void onPageSelected(View view, int position, boolean isBottom);
}