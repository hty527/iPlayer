package com.android.videoplayer.video.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.iplayer.utils.PlayerUtils;
import com.android.videoplayer.R;
import com.android.videoplayer.base.adapter.BaseMultiItemAdapter;
import com.android.videoplayer.ui.widget.ListPlayerHolder;
import com.android.videoplayer.utils.DateParseUtil;
import com.android.videoplayer.utils.GlideModel;
import com.android.videoplayer.utils.ScreenUtils;
import com.android.videoplayer.video.bean.OpenEyesIndexItemBean;
import com.android.videoplayer.video.bean.VideoParams;
import java.util.List;

/**
 * created by hty
 * 2022/7/9
 * Desc:视频详情推荐数据
 */
public class ListDetailsAdapter extends BaseMultiItemAdapter<OpenEyesIndexItemBean,ListPlayerHolder> {

    private final int mVideoHeight;
    private boolean isSpread=false;

    public ListDetailsAdapter(List<OpenEyesIndexItemBean> data) {
        super(data);
        addItemType(OpenEyesIndexItemBean.ITEM_UNKNOWN,R.layout.item_unkonwn);
        addItemType(OpenEyesIndexItemBean.ITEM_TITLE,R.layout.item_video_list_title);//标题
        addItemType(OpenEyesIndexItemBean.ITEM_FOLLOW,R.layout.item_details_video_list);//收藏视频
        addItemType(OpenEyesIndexItemBean.ITEM_VIDEO,R.layout.item_details_video_list);//视频
        addItemType(OpenEyesIndexItemBean.ITEM_CARD_VIDEO,R.layout.item_details_video_list);//card视频
        addItemType(OpenEyesIndexItemBean.ITEM_NOIMAL,R.layout.item_details_video_list);//普通视频
        addItemType(OpenEyesIndexItemBean.ITEM_HEADER,R.layout.item_details_header);//普通视频
        mVideoHeight = ((ScreenUtils.getInstance().getScreenWidth() - ScreenUtils.getInstance().dpToPxInt(32f))/2) * 9 /16;
    }

    @Override
    protected void initItemView(ListPlayerHolder viewHolder, int position, OpenEyesIndexItemBean data) {
        switch (viewHolder.getItemViewType()) {
            case OpenEyesIndexItemBean.ITEM_TITLE:
                setItemTitle(viewHolder,data,position);
                break;
            case OpenEyesIndexItemBean.ITEM_FOLLOW:
                if(null!=data.getData()&&null!=data.getData().getContent()){
                    OpenEyesIndexItemBean indexItemBean = data.getData().getContent().getData();
                    setItemVideo(viewHolder,indexItemBean,position);
                }else{
                    setNullItem(viewHolder,position);
                }
                break;
            case OpenEyesIndexItemBean.ITEM_VIDEO:
                if(null!=data.getData()){
                    OpenEyesIndexItemBean indexItemBean = data.getData();
                    setItemVideo(viewHolder,indexItemBean,position);
                }else{
                    setNullItem(viewHolder,position);
                }
                break;
            case OpenEyesIndexItemBean.ITEM_CARD_VIDEO:
            case OpenEyesIndexItemBean.ITEM_NOIMAL:
                setItemVideo(viewHolder,data,position);
                break;
            case OpenEyesIndexItemBean.ITEM_HEADER:
                setHeaderData(viewHolder,data,position);
                break;
        }
    }

    /**
     * 头部数据
     * @param viewHolder
     * @param data
     * @param position
     */
    private void setHeaderData(ListPlayerHolder viewHolder, OpenEyesIndexItemBean data, int position) {
        if(null!=data){
            VideoParams headers = data.getHeaders();
            if(null!=headers){
                TextView anchorName = ((TextView) viewHolder.getView(R.id.item_title));
                anchorName.setText(headers.getNickname());
                ((TextView) viewHolder.getView(R.id.item_sub_title)).setText(DateParseUtil.getNow(headers.getDate()));//最后更新时间
                final TextView tvDescribe = (TextView) viewHolder.getView(R.id.item_describe);
                tvDescribe.setText(headers.getDescription());
                ImageView itemUserAvatar = (ImageView) viewHolder.getView(R.id.item_user_avatar);
                GlideModel.getInstance().loadCirImage(itemUserAvatar,headers.getUser_cover());
                final View btnMore = viewHolder.findViewById(R.id.item_more);
                //描述展开、收起
                View.OnClickListener onClickListener=new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isSpread=!isSpread;
                        tvDescribe.setMaxLines(isSpread?6:1);
                        btnMore.setRotation(isSpread?180:0);
                    }
                };
                viewHolder.findViewById(R.id.item_btn_more).setOnClickListener(onClickListener);
            }
        }
    }

    /**
     * 视频类型ITEM数据填充
     * @param viewHolder
     * @param data
     * @param position
     */
    private void setItemVideo(final ListPlayerHolder viewHolder, OpenEyesIndexItemBean data, int position) {
        if(null!=data){
            View itemContainer = viewHolder.getView(R.id.item_container);
            itemContainer.getLayoutParams().height=mVideoHeight;
            PlayerUtils.getInstance().setOutlineProvider(viewHolder.findViewById(R.id.item_root_view),ScreenUtils.getInstance().dpToPxInt(5f));
            TextView itemDuration = ((TextView) viewHolder.getView(R.id.item_duration));
            itemDuration.setText(PlayerUtils.getInstance().stringForAudioTime(data.getDuration()>0?data.getDuration()*1000:0));
            TextView title = ((TextView) viewHolder.getView(R.id.item_title));
            title.setText(data.getTitle());
            //视频时长\点赞\分享等
            TextView item_count = ((TextView) viewHolder.getView(R.id.item_count));
            OpenEyesIndexItemBean.Consumption consumption = data.getConsumption();
            if(null!=consumption){
                item_count.setText(String.format("%s人观看",consumption.getReplyCount()));//观看人次
            }else{
                item_count.setText(String.format("%s人观看",0));
            }
            //封面+用户头像
            ImageView imageCover = (ImageView) viewHolder.getView(R.id.item_cover);
            if(null!=data.getCover()){
                GlideModel.getInstance().loadImage(imageCover,data.getCover().getFeed());
            }else{
                imageCover.setImageResource(R.mipmap.ic_player_cover);
            }
        }else{
            setNullItem(viewHolder,position);
        }
    }

    /**
     * 将Item置空
     * @param viewHolder
     * @param position
     */
    private void setNullItem(ListPlayerHolder viewHolder, int position) {
        ImageView imageCover = (ImageView) viewHolder.getView(R.id.item_cover);
        imageCover.setImageResource(R.mipmap.ic_player_cover);
    }

    /**
     * 标题类型ITEM数据填充
     * @param viewHolder
     * @param data
     * @param position
     */
    private void setItemTitle(ListPlayerHolder viewHolder, OpenEyesIndexItemBean data, int position) {
        TextView textView = ((TextView) viewHolder.getView(R.id.item_video_title));
        if(null!=data&&null!=data.getData()){
            textView.setText(data.getData().getText());
        }else{
            textView.setText("");
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(null!=layoutManager&&layoutManager instanceof GridLayoutManager){
            GridLayoutManager gridLayoutManager= (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int itemViewType = getItemViewType(position);
                    switch (itemViewType) {
                        //未知的布局和标题及头部栏权重占满
                        case OpenEyesIndexItemBean.ITEM_UNKNOWN:
                        case OpenEyesIndexItemBean.ITEM_TITLE:
                        case OpenEyesIndexItemBean.ITEM_HEADER:
                            return 2;
                    }
                    //其它类型条目享受1/2的权重
                    return 1;
                }
            });
        }
    }
}