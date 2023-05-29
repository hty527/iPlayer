package com.android.player.ui.adapter;

import android.content.Intent;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.iplayer.utils.ILogger;
import com.android.player.R;
import com.android.player.base.adapter.BaseMultiItemAdapter;
import com.android.player.base.adapter.widget.BaseViewHolder;
import com.android.player.bean.Menu;
import com.android.player.bean.Version;
import com.android.player.utils.DataFactory;
import java.util.List;

/**
 * created by hty
 * 2022/7/4
 * Desc:首页Menu
 */
public class MainMenuAdapter  extends BaseMultiItemAdapter<Menu, BaseViewHolder> {

    public static final String EMAIL_ADDRES="584312311@qq.com";

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
            ((TextView) viewHolder.itemView.findViewById(R.id.item_sdk_version)).setText(String.format(DataFactory.getInstance().getString(R.string.text_version_format,"SDK版本号：%s"), ILogger.getVersion()));
            item_sub.setVisibility(TextUtils.isEmpty(data.getSub_title())? View.GONE:View.VISIBLE);
            ((TextView) viewHolder.getView(R.id.item_version_title)).setText(DataFactory.getInstance().getString(R.string.item_version_title,"预更新版本："));
            ((TextView) viewHolder.getView(R.id.item_desc_title)).setText(DataFactory.getInstance().getString(R.string.item_desc_title,"预更新内容："));
            ((TextView) viewHolder.getView(R.id.item_time_title)).setText(DataFactory.getInstance().getString(R.string.item_time_title,"预更新时间："));
            ((TextView) viewHolder.getView(R.id.item_sdk_anchor)).setText(DataFactory.getInstance().getString(R.string.item_time_anchor,"联系作者："));
            TextView itemAnchor = (TextView) viewHolder.getView(R.id.item_anchor);
            itemAnchor.setText(EMAIL_ADDRES);
            itemAnchor.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            itemAnchor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataFactory.getInstance().copyString(viewHolder.getContext(), EMAIL_ADDRES);
                    Toast.makeText(viewHolder.getContext(),"已复制到粘贴板",Toast.LENGTH_SHORT).show();
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{ EMAIL_ADDRES});
//                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
                    email.putExtra(Intent.EXTRA_TEXT, "你好,我是来自iPlayer开源项目的电子邮件。");
                    email.setType("message/rfc822");
                    Intent chooser = Intent.createChooser(email, "请选择邮箱客户端:");
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    viewHolder.getContext().startActivity(chooser);
                }
            });
            Version version = data.getVersion();
            if(null!=version){
                ((TextView) viewHolder.getView(R.id.item_version)).setText(version.getCode());
                ((TextView) viewHolder.getView(R.id.item_descript)).setText(version.getDescript());
                ((TextView) viewHolder.getView(R.id.item_time)).setText(version.getTime());
            }
        }
    }
}