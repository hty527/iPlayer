package com.android.videoplayer.ui.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.adapter.BaseMultiItemAdapter;
import com.android.videoplayer.base.adapter.widget.BaseViewHolder;
import com.android.videoplayer.bean.Menu;
import com.android.videoplayer.bean.Version;
import java.util.List;

/**
 * created by hty
 * 2022/7/4
 * Desc:首页Menu
 */
public class MainMenuAdapter  extends BaseMultiItemAdapter<Menu, BaseViewHolder> {

    public MainMenuAdapter(List<Menu> data) {
        super(data);
        addItemType(0,R.layout.item_main_menu);
        addItemType(1,R.layout.item_main_menu2);
    }

    @Override
    protected void initItemView(BaseViewHolder viewHolder, int position, Menu data) {
        switch (viewHolder.getItemViewType()) {
            case 0:
                setItemData(viewHolder,position,data);
                break;
            case 1:
                setItemData2(viewHolder,position,data);
                break;
        }
    }

    /**
     * 常规
     * @param viewHolder
     * @param position
     * @param data
     */
    private void setItemData(BaseViewHolder viewHolder, int position, Menu data) {
        if(null!=data){
            viewHolder.itemView.setTag(data);
            View item_sub = viewHolder.findViewById(R.id.item_sub);
            TextView subTitle = (TextView) viewHolder.itemView.findViewById(R.id.item_sub_title);
            subTitle.setText(data.getSub_title());
            item_sub.setVisibility(TextUtils.isEmpty(data.getSub_title())? View.GONE:View.VISIBLE);
            ((TextView) viewHolder.getView(R.id.item_title)).setText(data.getTitle());
            View item_root = viewHolder.findViewById(R.id.item_root);
            View item_line = viewHolder.findViewById(R.id.item_line);
            item_root.setBackgroundResource(0==data.getGravity()?R.drawable.bg_main_item_top:1==data.getGravity()?R.drawable.bg_main_item_buttom:3==data.getGravity()?R.drawable.bg_main_item_full:R.drawable.bg_main_item_center);
            item_line.setVisibility(0==data.getGravity()?View.VISIBLE:1==data.getGravity()?View.GONE:3==data.getGravity()?View.GONE:View.VISIBLE);
        }
    }

    /**
     * 版本预告
     * @param viewHolder
     * @param position
     * @param data
     */
    private void setItemData2(BaseViewHolder viewHolder, int position, Menu data) {
        if(null!=data){
            viewHolder.itemView.setTag(data);
            View item_sub = viewHolder.findViewById(R.id.item_sub);
            TextView subTitle = (TextView) viewHolder.itemView.findViewById(R.id.item_sub_title);
            subTitle.setText(data.getSub_title());
            item_sub.setVisibility(TextUtils.isEmpty(data.getSub_title())? View.GONE:View.VISIBLE);
            Version version = data.getVersion();
            if(null!=version){
                ((TextView) viewHolder.getView(R.id.item_version)).setText(version.getCode());
                ((TextView) viewHolder.getView(R.id.item_descript)).setText(version.getDescript());
                ((TextView) viewHolder.getView(R.id.item_time)).setText(version.getTime());
            }
        }
    }
}