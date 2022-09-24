package com.android.videoplayer.video.ui.fragment;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.interfaces.IControllerView;
import com.android.iplayer.interfaces.IRenderView;
import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.controls.ControWindowView;
import com.android.iplayer.widget.controls.ControlCompletionView;
import com.android.iplayer.widget.controls.ControlFunctionBarView;
import com.android.iplayer.widget.controls.ControlGestureView;
import com.android.iplayer.widget.controls.ControlListView;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.iplayer.widget.controls.ControlToolBarView;
import com.android.iplayer.widget.view.MediaTextureView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseFragment;
import com.android.videoplayer.base.adapter.interfaces.OnItemChildClickListener;
import com.android.videoplayer.base.adapter.widget.OnLoadMoreListener;
import com.android.videoplayer.manager.PlayerManager;
import com.android.videoplayer.net.BaseEngin;
import com.android.videoplayer.ui.widget.ListPlayerHolder;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.SharedPreferencesUtil;
import com.android.videoplayer.video.adapter.ListPlayerAdapter;
import com.android.videoplayer.video.bean.OpenEyesAuthor;
import com.android.videoplayer.video.bean.OpenEyesIndexItemBean;
import com.android.videoplayer.video.contract.VideoListContract;
import com.android.videoplayer.video.presenter.VideoListPersenter;
import com.android.videoplayer.video.ui.widget.PlayerNewbieView;
import java.util.List;

/**
 * created by hty
 * 2022/7/1
 * Desc:处理列表视频播放、转场播放后接收播放器等示例
 */
public class ListPlayerFragment extends BaseFragment<VideoListPersenter> implements VideoListContract.View {

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ListPlayerAdapter mAdapter;
    protected VideoPlayer mVideoPlayer;
    protected int mCurrentPosition=-1;
    private ViewGroup mPlayerContainer;
    private SwipeRefreshLayout mRefreshLayout;
    private boolean mNewbie;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_list_player;
    }

    @Override
    protected void initViews() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiper_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(null!=mPresenter) mPresenter.getIndexVideos(true);
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //列表适配器初始化
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListPlayerAdapter(null,autoPlayer());//DataFactory.getInstance().getVideoList()
        //点击播放器区域
        mAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                if(null!=view&&null!=view.findViewById(R.id.item_player_container)){
                    startPlayer((ViewGroup) view.findViewById(R.id.item_player_container),position);
                }
            }
        });
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Logger.d(TAG,"onLoadMore");
                if(null!=mPresenter) {
                    mPresenter.getIndexVideos(false);
                }
            }
        },mRecyclerView);
        //监听子ITEM不可见状态
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
            }

            /**
             * 监听滚动状态销毁播放器
             * @param view
             */
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                changedPlayerIcon(view,View.VISIBLE);
                FrameLayout container = view.findViewById(R.id.item_player_container);
                if(null!=container&&container.getChildCount()>0){
                    View childAt = container.getChildAt(0);
                    if(null!=childAt&&childAt instanceof VideoPlayer){
                        Logger.d(TAG,"childAtReset-->");
                        mCurrentPosition=-1;//不管是自动播放还是点击播放,在销毁了播放器同时也要重置上一个播放器的角标位置
                        resetPlayer();
                    }
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mPresenter.getIndexVideos(true);

        mNewbie = SharedPreferencesUtil.getInstance().getBoolean("newbie", false);
    }

    @Override
    protected VideoListPersenter createPresenter() {
        return new VideoListPersenter();
    }

    @Override
    public void showVideos(List<OpenEyesIndexItemBean> data, boolean isRestart) {
        Logger.d(TAG, "showVideos-->data：" + data.size());
        findViewById(R.id.tv_loading).setVisibility(View.GONE);
        if(null!=mRefreshLayout) mRefreshLayout.setRefreshing(false);
        if(isRestart){
            //销毁此前存在的播放记录
            mCurrentPosition=-1;//不管是自动播放还是点击播放,在销毁了播放器同时也要重置上一个播放器的角标位置
            resetPlayer();
        }
        if (null != mAdapter) {
            if(isRestart){
                mAdapter.setNewData(data);
                if(null!=mLayoutManager) mLayoutManager.scrollToPositionWithOffset(0,0);//每次冲洗加载都滚动到第0个
                boolean autoPlayer = autoPlayer();
                if(autoPlayer){
                    autoPlayerVideo();
                }
            }else{
                mAdapter.addData(data);
            }
            mAdapter.onLoadComplete();
        }
    }

    @Override
    public void showError(int code, String errorMsg) {
        if(null!=mRefreshLayout) mRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(),errorMsg,Toast.LENGTH_SHORT).show();
        if(code== BaseEngin.API_RESULT_EMPTY){
            mAdapter.onLoadEnd();
        }else{
            mAdapter.onLoadError();
        }
    }

    protected boolean autoPlayer() {return false;}//交给子类决定是否自动播放

    /**
     * 自动开始播放第0个视频
     */
    private void autoPlayerVideo() {
        //添加一条CDN贼快的视频资源到顶部自动播放
        OpenEyesIndexItemBean itemBean=new OpenEyesIndexItemBean();
        itemBean.setType("NORMAL");
        itemBean.setId(7176);
        OpenEyesIndexItemBean.Cover cover=new OpenEyesIndexItemBean.Cover();
        cover.setFeed("https://inews.gtimg.com/newsapp_bt/0/13386414723/1000.jpg");
        itemBean.setCover(cover);
        OpenEyesAuthor author=new OpenEyesAuthor();
        author.setName("漫威影视");
        author.setIcon("https://5b0988e595225.cdn.sohucs.com/q_70,c_zoom,w_640/images/20180608/dd3be9e446694852b3eb02cfb434400c.jpeg");
        itemBean.setAuthor(author);
        OpenEyesIndexItemBean.Consumption consumption=new OpenEyesIndexItemBean.Consumption();
        consumption.setCollectionCount(44541);
        consumption.setReplyCount(1860223);
        consumption.setShareCount(58954);
        itemBean.setTitle("漫威惊奇队长");
        itemBean.setConsumption(consumption);
        itemBean.setDuration(31);
        itemBean.setDate(1657167889000L);
        itemBean.setPlayUrl("http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4");
        itemBean.setDescription("漫威惊奇队长，是什么让她成为复联最强战力，能力堪比六钻满配灭霸？据说惊奇2已开拍，2023年上映？");
        mAdapter.addData(0,itemBean);
        //开始播放第一个视频
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                View viewByPosition = mLayoutManager.findViewByPosition(0);
                if(null!=viewByPosition&&null!=viewByPosition.findViewById(R.id.item_player_container)){
                    startPlayer((ViewGroup) viewByPosition.findViewById(R.id.item_player_container),0);
                }
            }
        },200);
    }

    /**
     * 检测当前显示完整的第一个Item并尝试播放视频
     * @param view
     */
    protected void autoPlayVideo(RecyclerView view) {
        if (view == null) return;
        //遍历RecyclerView子控件,如果item_player_container完全可见就开始播放
        int count = view.getChildCount();
        Logger.d(TAG,"autoPlayVideo-->count:" + count);
        for (int i = 0; i < count; i++) {
            View itemView = view.getChildAt(i);
            if (itemView == null||null==itemView.getTag()) continue;
            ListPlayerHolder holder = (ListPlayerHolder) itemView.getTag();
            FrameLayout container = holder.getPlayerContainer();
            if(null!=container){
                Rect rect = new Rect();
                container.getLocalVisibleRect(rect);
                int height = container.getHeight();
                if (rect.top == 0 && rect.bottom == height) {
                    startPlayer(container,holder.getAbsoluteAdapterPosition());
                    break;
                }
            }else{
                //其它类型条目
            }
        }
    }

    /**
     * 改变item的播放按钮状态
     * @param view
     * @param visible
     */
    protected void changedPlayerIcon(View view,int visible) {
        if(null==view) return;
        View itemPlayer = view.findViewById(R.id.item_player);
        if(null!=itemPlayer) itemPlayer.setVisibility(visible);
    }

    /**
     * 开始播放
     * @param position
     */
    protected void startPlayer(ViewGroup playerContainer,int position) {
        Logger.d(TAG,"startPlayer-->position:" + position+",currentPosition:"+mCurrentPosition);
        IWindowManager.getInstance().quitGlobaWindow();//每次播放清除悬浮窗窗口播放器
        if(mCurrentPosition==position) return;//如果时尝试重复播放同一个item,拦截
        //恢复此前ITEM虚拟播放按钮
        if(null!=mLayoutManager&&mCurrentPosition>-1) changedPlayerIcon(mLayoutManager.findViewByPosition(mCurrentPosition),View.VISIBLE);//还原上一个播放器的播放按钮可见状态
        resetPlayer();
        if(null==mAdapter) return;
        if(null==playerContainer) return;
        this.mPlayerContainer=playerContainer;
        this.mCurrentPosition=position;
        initVideoPlayer();
        OpenEyesIndexItemBean itemData = getItemData(position);
        if(null!=itemData){
            Logger.d(TAG,"startPlayer-->开始播放:");
            //隐藏当前ITEM虚拟播放按钮
            if(null!=mLayoutManager) changedPlayerIcon(mLayoutManager.findViewByPosition(position),View.INVISIBLE);//隐藏播放按钮可见状态
            mPlayerContainer.addView(mVideoPlayer,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            String[] videoPath = PlayerManager.getInstance().getVideoPath(itemData);
            mVideoPlayer.getController().setTitle(videoPath[1]);//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(videoPath[0]);//播放地址设置
            mVideoPlayer.prepareAsync();//开始异步准备播放
        }
    }

    /**
     * 根据条目ID获取item数据
     * @param position
     * @return
     */
    protected OpenEyesIndexItemBean getItemData(int position) {
        if(null!=mAdapter){
            List<OpenEyesIndexItemBean> data = mAdapter.getData();
            if(data.size()>position){
                return data.get(position);
            }
        }
        return null;
    }

    /**
     * 开始转场前的处理,将播放器从父容器中移除
     * @param position 点击的item位置
     * @return true:转场播放 false:非转场播放
     */
    protected void resetPlayerParent(int position) {
        if(null!=mVideoPlayer){
            PlayerUtils.getInstance().removeViewFromParent(mVideoPlayer);
            PlayerManager.getInstance().setVideoPlayer(mVideoPlayer);//转场之前必须设置
            changedPlayerIcon(mLayoutManager.findViewByPosition(mCurrentPosition),View.VISIBLE);//恢复播放器的播放按钮可见状态
        }
        //如果点击的item不是正在播放的item，则停止播放后重新设置播放资源,并且重新指定item播放器宿主
        if(mCurrentPosition!=position){
            if(null!=mVideoPlayer){
                mVideoPlayer.onDestroy();//结束并销毁播放器
                mVideoPlayer=null;
            }
            PlayerManager.getInstance().setVideoPlayer(null);//转场之前必须设置
            mCurrentPosition=position;
            mPlayerContainer= mLayoutManager.findViewByPosition(position).findViewById(R.id.item_player_container);
        }
    }

    /**
     * 结束转场时调用,恢复转场播放的播放器容器到原item的父容器
     * @return
     */
    protected void recoverPlayerParent() {
        VideoPlayer videoPlayer = PlayerManager.getInstance().getVideoPlayer();
        if(null!=mPlayerContainer&&null!=videoPlayer){
            Logger.d(TAG,"recoverPlayerParent-->接收转场");
            //移除播放器的父容器
            PlayerUtils.getInstance().removeViewFromParent(videoPlayer);
            mVideoPlayer=videoPlayer;
            /**
             * 重要,还原到宿主后需要更换宿主上下文
             * 这里存在一种情况，假设用户在列表界面未开始播放，而是点击条目跳转到详情页开始播放，此时播放器会在详情页创建，上下文是详情页的，这是转场回到此界面开始播放视频时，全屏是无效的。所以要更重置上下文为自己
             */
            mVideoPlayer.setParentContext(getContext());
            /**
             * 还原到列表模式
             */
            BaseController videoController = mVideoPlayer.getController();
            if(null!=videoController&&autoPlayer()){
                videoController.setListPlayerMode(true);//改变播放器为列表模式
            }
            setListener(false);//绑定监听器到此界面
            if(null!=mLayoutManager) changedPlayerIcon(mLayoutManager.findViewByPosition(mCurrentPosition),View.INVISIBLE);//隐藏播放按钮的可见状态
            mPlayerContainer.addView(mVideoPlayer,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }else{
            Logger.d(TAG,"recoverPlayerParent-->重置");
            resetPlayer();
            mVideoPlayer=null;
            mCurrentPosition=-1;
        }
        PlayerManager.getInstance().setVideoPlayer(null);//清除转场对象,避免生命周期重复时重复调用
    }

    /**
     * 播放器初始化
     */
    private void initVideoPlayer() {
        if(null==mVideoPlayer){
            mVideoPlayer = new VideoPlayer(getContext());
            //为播放器添加控制器
            VideoController controller=new VideoController(mVideoPlayer.getContext());
            mVideoPlayer.setController(controller);
            //为控制器添加UI交互组件
            ControlToolBarView toolBarView=new ControlToolBarView(controller.getContext());//标题栏，返回按钮、视频标题、功能按钮、系统时间、电池电量等组件
            toolBarView.setTarget(IVideoController.TARGET_CONTROL_TOOL);
            ControlFunctionBarView functionBarView=new ControlFunctionBarView(controller.getContext());//底部时间、seek、静音、全屏功能栏
            ControlGestureView gestureView=new ControlGestureView(controller.getContext());//手势控制屏幕亮度、系统音量、快进、快退UI交互
            ControlCompletionView completionView=new ControlCompletionView(controller.getContext());//播放完成、重试
            ControlStatusView statusView=new ControlStatusView(controller.getContext());//移动网络播放提示、播放失败、试看完成
            ControlLoadingView loadingView=new ControlLoadingView(controller.getContext());//加载中、开始播放
            ControWindowView windowView=new ControWindowView(controller.getContext());//悬浮窗窗口UI交互
            //自动播放场景下添加列表专用UI交互组件
            if(autoPlayer()){
                ControlListView controlListView=new ControlListView(controller.getContext());//列表播放器场景专用交互组件
                controller.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView,controlListView);
            }else{
                controller.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
            }
            //启用静音功能交互\默认当自动播放时静音，非自动播放不静音 跟播放器交互的设置需要在addControllerWidget之后调用
            functionBarView.showSoundMute(true,autoPlayer());
            //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
            setListener(true);
        }
    }

    /**
     * 无论初始化、转场接收都重置监听
     * @param isInit 播放器是否是初始化后调用的
     */
    private void setListener(boolean isInit) {
        if(null==mVideoPlayer) return;
        VideoController controller = (VideoController) mVideoPlayer.getController();
        if(null!=controller){
            //找到此前添加\在详情页添加的titleBar组件
            IControllerView controllerView = controller.findControlWidgetByTag(IVideoController.TARGET_CONTROL_TOOL);
            if(null!=controllerView&&controllerView instanceof ControlToolBarView){
                ControlToolBarView controlToolBarView= (ControlToolBarView) controllerView;
                controlToolBarView.showMenus(false,false,false);
            }
            Logger.d(TAG,"setListener-->autoPlayer:"+autoPlayer()+",isInit:"+isInit);
            if(autoPlayer()){//自动播放场景启用列表模式,点击播放使用常规播放模式
                if(isInit){
                    controller.setListPlayerMode(true);//列表播放模式,默认静音
                    IControllerView controllerView1 = controller.findControlWidgetByTag(IVideoController.TARGET_CONTROL_FUNCTION);
                    if(null!=controllerView1&&controllerView1 instanceof ControlFunctionBarView){
                        ControlFunctionBarView functionBarView= (ControlFunctionBarView) controllerView;
                        functionBarView.showSoundMute(true);//显示静音按钮，这个按钮给无缝转场的播放器使用设置
                    }
                }else{
                    controller.setListPlayerMode(true);//列表播放模式
                }
            }
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
                public IRenderView createRenderView() {
                    return new MediaTextureView(getContext());
                }

                @Override
                public void onPlayerState(PlayerState state, String message) {
                    //首针渲染开始新手引导
                    if(state==PlayerState.STATE_START){
                        showNewbie();
                    }
                }
            });
            mVideoPlayer.setLoop(true);
            mVideoPlayer.setLandscapeWindowTranslucent(true);
            mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
        }
    }

    private void onBackPressed(){
        if(null!=getActivity()){
            getActivity().onBackPressed();
        }
    }

    /**
     * 还原播放器
     */
    private void resetPlayer() {
        Logger.d(TAG,"resetPlayer-->");
        if(null!=mVideoPlayer){
            mVideoPlayer.onReset();
        }
        //将播放器添加到ITEM中
        PlayerUtils.getInstance().removeViewFromParent(mVideoPlayer);
        if(null!=mPlayerContainer) mPlayerContainer.removeAllViews();
    }

    /**
     * 新手引导开始
     */
    private void showNewbie() {
        if(!mNewbie&&autoPlayer()&&null!=mPlayerContainer){
            ViewGroup viewGroup = (ViewGroup) getActivity().getWindow().getDecorView();
            if (null != viewGroup.findViewById(R.id.window_newbie)) {
                return;
            }
            PlayerNewbieView newbieWindow = new PlayerNewbieView(viewGroup.getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
            newbieWindow.setId(R.id.window_newbie);
            viewGroup.addView(newbieWindow,layoutParams);
            newbieWindow.setOnDismissListener(new PlayerNewbieView.OnDismissListener(){

                @Override
                public void onDismiss() {
                    mNewbie=true;
                    SharedPreferencesUtil.getInstance().putBoolean("newbie", true);
                    removeNewbie();
                }
            });
            newbieWindow.updateWindow(mPlayerContainer);
        }
    }

    private void removeNewbie() {
        ViewGroup viewGroup = (ViewGroup)getActivity().getWindow().getDecorView();
        View oldTinyVideo = viewGroup.findViewById(R.id.window_newbie);
        //移除新人窗口
        if (null != oldTinyVideo) {
            viewGroup.removeView(oldTinyVideo);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume");
        if(null!=mRefreshLayout&&mRefreshLayout.isRefreshing()&&null!=mPresenter&&!mPresenter.isRequsting()){
            mRefreshLayout.setRefreshing(false);
        }
        if(null!=mVideoPlayer) mVideoPlayer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG,"onPause-->isChangeIng："+PlayerManager.getInstance().isChangeIng());
        //转场播放的时候交给接手转场的宿主处理生命周期事件
        if(!PlayerManager.getInstance().isChangeIng()){
            if(null!=mVideoPlayer) mVideoPlayer.onPause();
        }
    }

    /**
     * 是否能返回
     * @return
     */
    public boolean isBackPressed() {
        if(null!=mVideoPlayer){
            return mVideoPlayer.isBackPressed();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG,"onDestroy");
        resetPlayer();
        removeNewbie();
        if(null!=mVideoPlayer){
            mVideoPlayer.onDestroy();
            mVideoPlayer=null;
        }
    }
}