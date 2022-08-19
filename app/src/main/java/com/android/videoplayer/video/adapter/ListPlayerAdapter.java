package com.android.videoplayer.video.adapter;

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
import java.util.List;

/**
 * created by hty
 * 2022/7/2
 * Desc:多条目类型的视频列表适配器
 */
public class ListPlayerAdapter extends BaseMultiItemAdapter<OpenEyesIndexItemBean,ListPlayerHolder> {

    private final int mVideoHeight;
    private boolean mAutoPlay;//是否自动播放，自动播放模式下不显示item的播放按钮
    private View mFirstItemView;

    public ListPlayerAdapter(List<OpenEyesIndexItemBean> data) {
        this(data,false);
    }

    public ListPlayerAdapter(List<OpenEyesIndexItemBean> data,boolean autoPlay) {
        super(data);
        addItemType(OpenEyesIndexItemBean.ITEM_UNKNOWN,R.layout.item_unkonwn);
        addItemType(OpenEyesIndexItemBean.ITEM_TITLE,R.layout.item_unkonwn);//标题//R.layout.item_video_list_title
        addItemType(OpenEyesIndexItemBean.ITEM_FOLLOW,R.layout.item_video_list_player);//收藏视频
        addItemType(OpenEyesIndexItemBean.ITEM_VIDEO,R.layout.item_video_list_player);//视频
        addItemType(OpenEyesIndexItemBean.ITEM_CARD_VIDEO,R.layout.item_video_list_player);//card视频
        addItemType(OpenEyesIndexItemBean.ITEM_NOIMAL,R.layout.item_video_list_player);//普通视频
        mVideoHeight = (ScreenUtils.getInstance().getScreenWidth() - ScreenUtils.getInstance().dpToPxInt(32f)) * 9 /16;
        this.mAutoPlay=autoPlay;
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
            if(null==mFirstItemView){
                mFirstItemView=viewHolder.itemView.findViewById(R.id.item_container);
            }
            View itemContainer = viewHolder.getView(R.id.item_container);
            itemContainer.getLayoutParams().height=mVideoHeight;
            itemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!= mOnItemChildClickListener){
                        int adapterPosition = viewHolder.getAdapterPosition();
                        mOnItemChildClickListener.onItemClick(v,adapterPosition,getItemId(adapterPosition));
                    }
                }
            });
            PlayerUtils.getInstance().setOutlineProvider(itemContainer,ScreenUtils.getInstance().dpToPxInt(3f));
            TextView anchorName = ((TextView) viewHolder.getView(R.id.item_title));
            anchorName.setText(null!=data.getAuthor()?data.getAuthor().getName():data.getTitle());
            ((TextView) viewHolder.getView(R.id.item_sub_title)).setText(DateParseUtil.getNow(data.getDate()));//最后更新时间
            ((TextView) viewHolder.getView(R.id.item_describe)).setText(data.getDescription());
            //视频时长\点赞\分享等
            ((TextView) viewHolder.getView(R.id.item_durtion)).setText(PlayerUtils.getInstance().stringForAudioTime(data.getDuration()>0?data.getDuration()*1000:0));
            TextView item_count = ((TextView) viewHolder.getView(R.id.item_count));
            TextView item_collection = ((TextView) viewHolder.getView(R.id.item_collection));
            TextView item_comment = ((TextView) viewHolder.getView(R.id.item_comment));
            TextView item_give = ((TextView) viewHolder.getView(R.id.item_give));
            TextView item_share = ((TextView) viewHolder.getView(R.id.item_share));
            OpenEyesIndexItemBean.Consumption consumption = data.getConsumption();
            if(null!=consumption){
                item_count.setText(String.format("%s人观看",consumption.getReplyCount()));//观看人次
                item_collection.setText(ScreenUtils.getInstance().formatWan(consumption.getCollectionCount(),true));
                item_comment.setText((295+position)+"");
                item_give.setText((565+position)+"");
                item_share.setText(ScreenUtils.getInstance().formatWan(consumption.getShareCount(),true));
            }else{
                item_count.setText(String.format("%s人观看",0));
                item_collection.setText("987");
                item_comment.setText("476");
                item_give.setText("455");
                item_share.setText("25");
            }
            //封面+用户头像
            ImageView imageCover = (ImageView) viewHolder.getView(R.id.item_cover);
            ImageView itemUserAvatar = (ImageView) viewHolder.getView(R.id.item_user_avatar);
            ImageView itemPlayer = (ImageView) viewHolder.getView(R.id.item_player);
            itemPlayer.setImageResource(mAutoPlay?0:R.mipmap.ic_player_start);
            if(null!=data.getAuthor()){
                GlideModel.getInstance().loadCirImage(itemUserAvatar,data.getAuthor().getIcon());
            }else{
                imageCover.setImageResource(R.mipmap.ic_default_circle);
            }
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
//        TextView textView = ((TextView) viewHolder.getView(R.id.item_video_title));
//        if(null!=data&&null!=data.getData()){
//            textView.setText(data.getData().getText());
//        }else{
//            textView.setText("");
//        }
    }

    public View getFirstItemView() {
        return mFirstItemView;
    }
}