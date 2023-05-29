package com.android.player.base.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.player.base.adapter.interfaces.OnItemChildClickListener;
import com.android.player.base.adapter.interfaces.OnItemClickListener;
import com.android.player.base.adapter.widget.BaseViewHolder;
import com.android.player.base.adapter.widget.OnLoadMoreListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * created by hty
 * 2022/7/1
 * Desc:适配器封装基类,业务适配器请继承 BaseNoimalAdapter(单布局) 或 BaseMultiItemAdapter(多布局) 实现自己的适配器
 */
public abstract class BaseAdapter<T,VH extends BaseViewHolder> extends RecyclerView.Adapter<VH> {

    protected static final String TAG=BaseAdapter.class.getSimpleName();
    protected List<T> mData;
    protected OnItemClickListener mOnItemClickListener;
    protected OnItemChildClickListener mOnItemChildClickListener;

    private BaseAdapter(){}

    public BaseAdapter(List<T> data){
        this.mData=data;
    }

    protected abstract void initItemView(VH viewHolder,int position,T data);

    /**
     * 字适配器实现此方法返回ItemView
     * @param viewGroup
     * @param itemType
     */
    protected abstract View getItemTypeView(@NonNull ViewGroup viewGroup, int itemType);

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
        return createBaseViewHolder(getItemTypeView(viewGroup,itemType));
    }

    @Override
    public void onBindViewHolder(@NonNull final VH vh, final int position) {
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!= mOnItemClickListener){
                    int adapterPosition = vh.getAdapterPosition();
                    mOnItemClickListener.onItemClick(v,adapterPosition,getItemId(adapterPosition));
                }
            }
        });
        initItemView(vh,position,getItemData(position));
    }

    protected T getItemData(int position) {
        if(null!=mData&&mData.size()>position){
            return mData.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return null!=mData&&mData.size()>0?mData.size():0;
    }

    public List<T> getData(){
        if(null!=mData){
            return mData;
        }
        return new ArrayList<>();
    }

    public void setNewData(List<T> data) {
        this.mData=data;
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        if(null==mData) {
            mData=new ArrayList<>();
        }
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(T data) {
        if(null==mData) {
            mData=new ArrayList<>();
        }
        mData.add(data);
        notifyDataSetChanged();
    }

    public void addData(int index,T data) {
        if(null==mData) {
            mData=new ArrayList<>();
        }
        mData.add(index,data);
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    private VH createBaseViewHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        VH k;
        // 泛型擦除会导致z为null
        if (z == null) {
            k = (VH) new BaseViewHolder(view);
        } else {
            k = createGenericKInstance(z, view);
        }
        return k != null ? k : (VH) new BaseViewHolder(view);
    }

    /**
     * get generic parameter K
     *
     * @param z
     * @return
     */
    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseViewHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                } else if (temp instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) temp).getRawType();
                    if (rawType instanceof Class && BaseViewHolder.class.isAssignableFrom((Class<?>) rawType)) {
                        return (Class<?>) rawType;
                    }
                }
            }
        }
        return null;
    }

    /**
     * try to create Generic K instance
     *
     * @param z
     * @param view
     * @return
     */
    @SuppressWarnings("unchecked")
    private VH createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            // inner and unstatic class
            if (z.isMemberClass() && !Modifier.isStatic(z.getModifiers())) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                constructor.setAccessible(true);
                return (VH) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                return (VH) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    /**
     * LOAD MORE LISTSNER
     */
    private OnLoadMoreListener mLoadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener,RecyclerView recyclerView) {
        if(null==loadMoreListener){
            recyclerView.removeOnScrollListener(mLoadMoreListener);
        }
        mLoadMoreListener = loadMoreListener;
        if(null!=mLoadMoreListener){
            recyclerView.addOnScrollListener(mLoadMoreListener);
        }
    }

    /**
     * 加载成功
     */
    public void onLoadComplete() {
        if(null!=mLoadMoreListener){
            mLoadMoreListener.onLoadComplete();
        }
    }

    /**
     * 加载完成
     */
    public void onLoadEnd() {
        if(null!=mLoadMoreListener){
            mLoadMoreListener.onLoadEnd();
        }
    }

    /**
     * 加载失败
     */
    public void onLoadError() {
        if(null!=mLoadMoreListener){
            mLoadMoreListener.onLoadError();
        }
    }
}