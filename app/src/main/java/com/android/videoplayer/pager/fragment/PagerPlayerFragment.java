package com.android.videoplayer.pager.fragment;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.video.cache.VideoCache;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseFragment;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.pager.adapter.PagerPlayerAdapter;
import com.android.videoplayer.pager.base.BaseViewPager;
import com.android.videoplayer.pager.bean.VideoBean;
import com.android.videoplayer.pager.controller.ShortControllerControl;
import com.android.videoplayer.pager.interfaces.OnViewPagerListener;
import com.android.videoplayer.pager.widget.PagerVideoController;
import com.android.videoplayer.pager.widget.ViewPagerLayoutManager;
import com.android.videoplayer.utils.DataFactory;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/**
 * created by hty
 * 2022/7/1
 * Desc:垂直视频播放示例,只维护一个播放器实例
 */
public class PagerPlayerFragment extends BaseFragment {

    private PagerPlayerAdapter mAdapter;
    private boolean isVisible=false;
    private VideoPlayer mVideoPlayer;
    private ViewPagerLayoutManager mLayoutManager;
    private int mPosition;//准备待播放的角标位置

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_video_pager_player;
    }

    @Override
    protected void initViews() {
        findViewById(R.id.ll_bar_margin).getLayoutParams().height= ScreenUtils.getInstance().getStatusBarHeight(getContext())+ScreenUtils.getInstance().dpToPxInt(49f);
        //视频列表适配器准备
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        //LayoutManager内部已经过滤重复选中
        mLayoutManager = new ViewPagerLayoutManager(getContext(),ViewPagerLayoutManager.VERTICAL);
        mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {

            /**
             * @param view 当前正在被释放的itemView
             * @param isNext 是否还有下一条视频 true:有 false:没有
             * @param position 当前被释放的item位置
             */
            @Override
            public void onPageRelease(View view, boolean isNext, int position) {
                Logger.d(TAG,"onPageRelease-->isNext:"+isNext+",position:"+position+",mPosition:"+mPosition);
                resetPlayer(view,position);
            }

            /**
             * @param view 当前选中的itemView
             * @param position 当前选中的item位置
             * @param isBottom 是否滑动到底部 true:当前position已经到<=(最后一条数据-2)的位置了,在这里加载更多数据 flase:否
             */
            @Override
            public void onPageSelected(View view, int position, boolean isBottom) {
                Logger.d(TAG,"onPageSelected-->position:"+position+",isBottom:"+isBottom+",mPosition:"+mPosition);
                PagerPlayerFragment.this.mPosition=position;
                startPlayer(mPosition);
                if(isBottom){//是否要加载更多了,滑动到最后两条数据时触发加载更多
                    loadMoreData();
                }
            }
        });
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter= new PagerPlayerAdapter(null);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        isVisible=false;
        if(null!=mVideoPlayer) mVideoPlayer.onPause();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        isVisible=true;
        if(null!=mVideoPlayer) mVideoPlayer.onResume();
    }

    private View getItemView(int position) {
        if(null!=mLayoutManager){
            try {
                return mLayoutManager.findViewByPosition(position);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 根据position获取ItemView中的的PagerVideoController
     * @param view
     * @param position
     * @return
     */
    private BaseViewPager getItemPager(View view,int position) {
        if(null!=view){
            View videoPager = view.findViewById(R.id.item_video_pager);
            if(null!=videoPager){
                return (BaseViewPager) videoPager;
            }
        }
        View itemView = getItemView(position);
        if(null!=itemView){
            View videoPager = itemView.findViewById(R.id.item_video_pager);
            if(null!=videoPager){
                return (BaseViewPager) videoPager;
            }
        }
        return null;
    }

    /**
     * 播放器初始化
     */
    private void initVideoPlayer() {
        if(null==mVideoPlayer){
            mVideoPlayer = new VideoPlayer(getContext());
            //给播放器设置一个控制器
            ShortControllerControl controller = new ShortControllerControl(getContext());
            mVideoPlayer.setController(controller);
            //给控制器添加需要的UI交互组件
            controller.addControllerWidget(new ControlStatusView(getContext()));
            mVideoPlayer.setLoop(true);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
            //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
            mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
                /**
                 * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
                 * @return
                 */
                @Override
                public AbstractMediaPlayer createMediaPlayer() {
                    return ExoPlayerFactory.create().createPlayer(getContext());
                }

                @Override
                public void onPlayerState(PlayerState state, String message) {
//                    Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message+",mPosition:"+mPosition);
                    BaseViewPager itemPager = getItemPager(null,mPosition);
                    if(null!=itemPager){
                        switch (state) {
                            case STATE_PREPARE://播放器准备中
                            case STATE_BUFFER://播放过程缓冲中
                                itemPager.prepare();
                                break;
                            case STATE_START://缓冲结束、准备结束 后的开始播放
                            case STATE_PLAY://恢复播放
                            case STATE_ON_PLAY:
                                itemPager.resume();
                                break;
                            case STATE_MOBILE://移动网络环境下播放
                                break;
                            case STATE_ON_PAUSE: //手动暂停生命周期暂停
                            case STATE_PAUSE://手动暂停
                                itemPager.pause();
                                break;
                            case STATE_RESET: //重置
                            case STATE_STOP://停止
                            case STATE_DESTROY://销毁
                                itemPager.stop();
                                break;
                            case STATE_COMPLETION://正常的播放器结束
                                //先退出全屏控制器
                                break;
                            case STATE_ERROR://失败
                                itemPager.error();
                                break;
                        }
                    }
                }
            });
            mVideoPlayer.setLoop(true);
            mVideoPlayer.setProgressCallBackSpaceMilliss(200);
        }
    }

    /**
     * 开始启动播放
     * @param position
     */
    private void startPlayer(int position) {
        View itemView = getItemView(position);
        if(null!=itemView){
            View videoPager = itemView.findViewById(R.id.item_video_pager);
            if(null!=videoPager){
                PagerVideoController playerPager = (PagerVideoController) videoPager;
                ViewGroup playerContainer = playerPager.getPlayerContainer();
                VideoBean videoData = playerPager.getVideoData();
                if(null!=playerContainer&&null!=videoData){
                    initVideoPlayer();
                    PlayerUtils.getInstance().removeViewFromParent(mVideoPlayer);
                    playerContainer.addView(mVideoPlayer,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                    mVideoPlayer.getController().setTitle(videoData.getTitle());//视频标题(默认视图控制器横屏可见)
                    mVideoPlayer.setDataSource(VideoCache.getInstance().getPlayPreloadUrl(videoData.getVideoDownloadUrl()));//播放地址设置
                    mVideoPlayer.prepareAsync();//开始异步准备播放
                }
            }
        }
    }

    /**
     * 还原播放器
     */
    private synchronized void resetPlayer(View view,int position) {
        Logger.d(TAG,"resetPlayer-->position:"+position);
        if(null!=mVideoPlayer){
            mVideoPlayer.onReset();
            //将播放器从ITEM中移除
            PlayerUtils.getInstance().removeViewFromParent(mVideoPlayer);
        }
        //结束控制器唱片机的动画
        BaseViewPager itemPager = getItemPager(view,position);
        if(null!=itemPager){
            itemPager.onRelease();
        }
    }

    private void loadMoreData() {
        DataFactory.getInstance().getTikTopVideo(new DataFactory.OnCallBackListener() {
            @Override
            public void onList(List<VideoBean> data) {
                if(null!=mAdapter) mAdapter.addData(data);
            }
        });
    }

    /**
     * 播放入口,开始播放之前还原播放器状态
     * @param videoJson 视频数据
     * @param position 从第几个开始播放
     */
    public void navigationPlayer(String videoJson, int position) {
        Logger.d(TAG,"navigationPlayer-->videoJson:"+videoJson+",position:"+position);
        if(TextUtils.isEmpty(videoJson)) return;
        try {
            List<VideoBean> data = new Gson().fromJson(videoJson, new TypeToken<List<VideoBean>>() {}.getType());
            navigationPlayer(data,position);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 播放入口,开始播放之前还原播放器状态
     * @param data 视频数据
     * @param position 从第几个开始播放
     */
    public void navigationPlayer(List<VideoBean> data, int position) {
        Logger.d(TAG,"onVisible-->");
//        isVisible=true;
        if(null!=data&&data.size()>0){
            Logger.d(TAG,"navigationPlayer-->data:"+data.size()+",position:"+position);
            resetPlayer(null,mPosition);//这个入口会重复调用,必须先移除上一个播放器
            if(null==mAdapter) return;
            if(null!=mLayoutManager) mLayoutManager.onReset();
            if(null!=mAdapter) mAdapter.setNewData(null);
            mAdapter.setNewData(data);//第1+次打开次界面时
            this.mPosition=position;
            View input = findViewById(R.id.input);
            input.setVisibility(View.VISIBLE);
            input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(),"请下载抖音体验",Toast.LENGTH_SHORT).show();
                }
            });
            //定为到要预览的位置
            if(null!=mLayoutManager){
                mLayoutManager.scrollToPositionWithOffset(data.size()>position?position:0,0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume,isVisible:"+isVisible);
        if(isVisible){
            if(null!=mVideoPlayer) mVideoPlayer.onResume();
            getItemView(mPosition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG,"onPause");
        if(null!=mVideoPlayer) mVideoPlayer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG,"onDestroy");
        VideoCache.getInstance().removeAllPreloadTask();
        if(null!=mVideoPlayer) mVideoPlayer.onDestroy();
        if(null!=mAdapter){
            mAdapter.setNewData(null);
        }
    }
}