package com.android.player.base.adapter;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.android.player.base.adapter.interfaces.MultiItemEntity;
import com.android.player.base.adapter.widget.BaseViewHolder;
import java.util.List;

/**
 * created by hty
 * 2022/7/7
 * Desc:这是一个支持多布局类型的扩展，多条目布局类型适配器请继承此类,无比在构造方法里调用addItemType()添加你的布局类型和布局ID
 */
public abstract class BaseMultiItemAdapter<T extends MultiItemEntity,VH extends BaseViewHolder> extends BaseAdapter <T,VH> {

    private SparseIntArray layouts = new SparseIntArray();//存储多条目类型和布局

    public BaseMultiItemAdapter(List data) {
        super(data);
    }

    @Override
    protected View getItemTypeView(@NonNull ViewGroup viewGroup, int itemType) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(layouts.get(itemType), viewGroup, false);
    }

    @Override
    public int getItemViewType(int position) {
        if(null!=getData()&&getData().size()>position){
            T data = (T) getData().get(position);
            if(null!=data) {
                return data.getItemType();
            }
        }
        return 0;
    }

    /**
     * 子适配器调用此方法添加自己的不同条目View ID
     * @param itemType
     * @param layoutResId
     */
    protected void addItemType(int itemType, @LayoutRes int layoutResId){
        layouts.put(itemType,layoutResId);
    }
}