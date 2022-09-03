package com.android.videoplayer.pager.adapter;

import android.view.View;
import androidx.annotation.NonNull;

import com.android.iplayer.video.cache.VideoCache;
import com.android.videoplayer.R;
import com.android.videoplayer.base.adapter.BaseNoimalAdapter;
import com.android.videoplayer.base.adapter.widget.BaseViewHolder;
import com.android.videoplayer.pager.bean.VideoBean;
import com.android.videoplayer.pager.widget.PagerVideoController;
import com.android.videoplayer.utils.Logger;
import java.util.List;

/**
 * created by hty
 * 2022/7/1
 * Desc:片段播放器适配器示例
 */
public class PagerPlayerAdapter extends BaseNoimalAdapter<VideoBean, PagerPlayerAdapter.VideoViewHolder> {

    public PagerPlayerAdapter(List<VideoBean> data) {
        super(R.layout.item_pager_player,data);
    }

    @Override
    protected void initItemView(VideoViewHolder viewHolder, int position, VideoBean data) {
        viewHolder.mPagerVideoController.initMediaData( getItemData(position),position);
        //开始预加载
        VideoCache.getInstance().startPreloadTask(data.getVideoDownloadUrl(), position,1024*1024);
    }

    public class VideoViewHolder extends BaseViewHolder{

        private PagerVideoController mPagerVideoController;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mPagerVideoController =itemView.findViewById(R.id.item_video_pager);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        Logger.d(TAG,"adapterPosition:"+adapterPosition);
        if(adapterPosition>-1){
            VideoBean itemData = getItemData(adapterPosition);
            //取消预加载
            VideoCache.getInstance().removePreloadTask(itemData.getVideoDownloadUrl());
        }
        super.onViewDetachedFromWindow(holder);
    }
}