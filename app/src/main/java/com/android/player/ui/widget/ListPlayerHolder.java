package com.android.player.ui.widget;

import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.android.player.R;
import com.android.player.base.adapter.widget.BaseViewHolder;

/**
 * created by hty
 * 2022/7/2
 * Desc:列表播放ViewHolder
 */
public class ListPlayerHolder extends BaseViewHolder {

    private FrameLayout mPlayerContainer;

    public ListPlayerHolder(@NonNull View itemView) {
        super(itemView);
        if(null!=itemView&&null!=itemView.findViewById(R.id.item_player_container)){
            mPlayerContainer=itemView.findViewById(R.id.item_player_container);
        }
        itemView.setTag(this);
    }

    public FrameLayout getPlayerContainer() {
        return mPlayerContainer;
    }
}