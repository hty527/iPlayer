package com.android.videoplayer.pager.adapter;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.adapter.BaseNoimalAdapter;
import com.android.videoplayer.base.adapter.widget.BaseViewHolder;
import com.android.videoplayer.pager.bean.VideoBean;
import com.android.videoplayer.utils.GlideModel;
import com.android.videoplayer.utils.ScreenUtils;
import java.util.List;

/**
 * created by hty
 * 2022/7/1
 * Desc:视频列表适配器
 */
public class VideoListAdapter extends BaseNoimalAdapter<VideoBean, BaseViewHolder> {

    private final int mItemHeight;

    public VideoListAdapter(List<VideoBean> data) {
        super(R.layout.item_video_list,data);
        mItemHeight = ((ScreenUtils.getInstance().getScreenWidth() - ScreenUtils.getInstance().dpToPxInt(3f)) / 2) * 16 /11;
    }

    @Override
    protected void initItemView(BaseViewHolder viewHolder, int position, VideoBean data) {
        ((TextView) viewHolder.getView(R.id.item_title)).setText(data.getFilterTitleStr());
        FrameLayout itemRootView = (FrameLayout) viewHolder.getView(R.id.item_root_content);
        itemRootView.getLayoutParams().height= mItemHeight;
        ImageView imageCover = (ImageView) viewHolder.getView(R.id.item_cover);
        GlideModel.getInstance().loadImage(imageCover,data.getCoverImgUrl());
    }
}