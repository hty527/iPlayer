package com.android.videoplayer.video.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.adapter.interfaces.OnItemClickListener;
import com.android.videoplayer.manager.PlayerManager;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.video.bean.OpenEyesIndexItemBean;
import com.android.videoplayer.video.bean.VideoParams;
import com.android.videoplayer.video.ui.activity.VideoDetailsActivity;
import com.google.gson.Gson;

/**
 * created by hty
 * 2022/7/1
 * Desc:列表自动播放+点击转场播放示例播放示例,这个示例的列表是自动播放的,如果是非自动播放，转场处理前还需要调用protected void startPlayer(ViewGroup playerContainer,int position)方法先开始播放再转场
 * 无缝转场播放步骤：
 * 1.将播放器从原有Parent布局中移除
 * 2.跳转到新的Activity后将播放器添加到新的parent中
 */
public class ListAutoPlayerChangeFragment extends ListPlayerFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_player, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //点击ITEM全局区域,无缝跳转至详情页播放
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                //1.判断此Item类型是否存在播放器父容器
                if(null!=view&&null!=view.findViewById(R.id.item_player_container)){
                    //这个界面的适配器已经过滤不可见不支持的item类型，所以点击可以不用判断了
                    OpenEyesIndexItemBean itemData = PlayerManager.getInstance().getItemData(getItemData(position));
                    //2.将播放器从原有Parent布局中移除
                    resetPlayerParent(position);
                    //3.移除成功后，跳转到新的Activity后将播放器添加到新的parent中,如果点击的item并非当前正在播放的item,则直接销毁播放器到落地页播放
                    if(null!=itemData){
                        Intent intent=new Intent(getContext(), VideoDetailsActivity.class);
                        intent.putExtra("is_change",true);//是否转场播放
                        VideoParams videoParams = PlayerManager.getInstance().parseParams(itemData);
                        if(null!=videoParams){
                            intent.putExtra("params",new Gson().toJson(videoParams));
                        }
                        startActivityForResult(intent,101);
                    }
                }
            }
        });
        //监听子Item滚动可见状态
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //滚动停止
                    autoPlayVideo(recyclerView);
                }
            }
        });
    }

    /**
     * 告诉父类允许列表静止后自动播放
     * @return
     */
    @Override
    protected boolean autoPlayer() {
        return true;
    }

    /**
     * 在这里接收原播放器的转场播放逻辑
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(TAG,"onActivityResult-->requestCode："+requestCode+",resultCode:"+resultCode);
        if(101==requestCode&&102==resultCode){//不要误伤了悬浮窗播放
            recoverPlayerParent();//结束转场
        }else{
            mCurrentPosition=-1;
            mVideoPlayer=null;//无法转场的情况下重置播放器,避免界面销毁的时候销毁了正在窜浮窗口窗口播放的播放器
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlayerManager.getInstance().setVideoPlayer(null);//重置转场播放器对象
    }
}