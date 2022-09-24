package com.android.videoplayer.video.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.interfaces.IControllerView;
import com.android.iplayer.interfaces.IRenderView;
import com.android.iplayer.interfaces.IVideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.model.PlayerState;
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
import com.android.videoplayer.base.adapter.interfaces.OnItemClickListener;
import com.android.videoplayer.bean.Params;
import com.android.videoplayer.manager.PlayerManager;
import com.android.videoplayer.utils.GlideModel;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;
import com.android.videoplayer.video.adapter.ListDetailsAdapter;
import com.android.videoplayer.video.bean.OpenEyesIndexItemBean;
import com.android.videoplayer.video.bean.VideoParams;
import com.android.videoplayer.video.contract.VideoListContract;
import com.android.videoplayer.video.listener.OnMenuActionListener;
import com.android.videoplayer.video.presenter.VideoListPersenter;
import com.android.videoplayer.video.ui.widget.PlayerMenuDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/**
 * created by hty
 * 2022/7/9
 * Desc:这个界面处理：转场继续播放\接收全局悬浮窗继续播放\传入ID新开界面播放
 * 专场播放需紧密配合ListPlayerChangeFragment和ListPlayerFragment的onPause
 * 接收全局悬浮窗窗口播放器需紧密配合
 1.IWindowManager.getInstance().onClean();//清除现有悬浮窗窗口播放器组件但不销毁
 * 2.ViewGroup.addView(IWindowManager.getInstance().getBasePlayer(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));//将播放器添加到你自己的ViewGroup中
 * 当关闭了Activity时不用处理可见、不可见的生命周期,不然播放器状态会不停的切换,做不到流畅效果
 * 而宿主界面在转场过程中能够也不要调用生命周期事件-ListPlayerFragment的onPause
 */
public class VideoDetailsActivity extends AppCompatActivity implements VideoListContract.View {

    private static final String TAG="VideoDetailsActivity";
    private VideoListPersenter mPersenter;
    protected VideoPlayer mVideoPlayer;
    private boolean isFinish=false;//是否正在关闭Activity
    private ListDetailsAdapter mAdapter;
    private VideoParams mParams;//视频及头部参数
    private boolean mIsChange,mIsGlobalWindow;//是否转场播放,是否窗口播放
    private boolean isForbidCycle=false;//是否开启全屏模式,是否禁止生命周期(悬浮窗必须设置)
    private int mCurrentPosition=-1;
    private PlayerMenuDialog mMenuDialog;
    private GridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);
        mPersenter = new VideoListPersenter();
        mPersenter.attachView(this);
        initViews();

        mIsChange = getIntent().getBooleanExtra("is_change",false);//是否是转场播放场景跳转过来的
        mIsGlobalWindow = getIntent().getBooleanExtra("is_global_window",false);//是否是全局悬浮窗窗口播放场景跳转过来的
        String params = getIntent().getStringExtra("params");//悬浮窗和转场播放都会传的视频属性数据
        if(!TextUtils.isEmpty(params)) mParams = new Gson().fromJson(params,new TypeToken<VideoParams>(){}.getType());

        Logger.d(TAG,"onCreate-->mIsChange："+mIsChange+",mIsGlobalWindow:"+mIsGlobalWindow);
        //接收转场\全局悬浮窗窗口播放器继续播放
        initPlayer();

        isFinish=false;
        //初始化界面数据
        initData();
    }

    //基本的界面初始化
    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //列表适配器初始化
        mLayoutManager = new GridLayoutManager(this,2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListDetailsAdapter(null);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                if(mAdapter.getData().size()>position){
                    OpenEyesIndexItemBean data = mAdapter.getData().get(position);
                    switch (mAdapter.getItemViewType(position)) {
                        case OpenEyesIndexItemBean.ITEM_FOLLOW:
                            if(null!=data.getData()&&null!=data.getData().getContent()){
                                OpenEyesIndexItemBean indexItemBean = data.getData().getContent().getData();
                                startNewPlayer(indexItemBean,position);
                            }
                            break;
                        case OpenEyesIndexItemBean.ITEM_VIDEO:
                            if(null!=data.getData()){
                                OpenEyesIndexItemBean indexItemBean = data.getData();
                                startNewPlayer(indexItemBean,position);
                            }
                            break;
                        case OpenEyesIndexItemBean.ITEM_CARD_VIDEO:
                        case OpenEyesIndexItemBean.ITEM_NOIMAL:
                            startNewPlayer(data,position);
                            break;
                    }
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 初始化界面数据 默认id:6059
     */
    private void initData() {
        //设置封面
        if(null!=mParams){
            GlideModel.getInstance().loadImage((ImageView) findViewById(R.id.video_cover),mParams.getVideoCover());
        }
        String listJson = getIntent().getStringExtra("list_json");//如果列表数据不为空,直接渲染,不用加载数据了
        if(!TextUtils.isEmpty(listJson)){
            List<OpenEyesIndexItemBean> list = new Gson().fromJson(listJson,new TypeToken<List<OpenEyesIndexItemBean>>(){}.getType());
            findViewById(R.id.tv_loading).setVisibility(View.GONE);
            mAdapter.setNewData(list);
        }else{
            if(null!=mParams){
                mPersenter.getVideosByVideo(mParams.getId()+"");
            }else{
                mPersenter.getVideosByVideo("6059");
            }
        }
    }

    /**
     * 处理item点击跳转/转场播放跳转/全局悬浮窗点击跳转
     * 一、转场播放
     * 1.将转PlayerManager.getInstance().getVideoPlayer()中的BasePlayer add到此界面的ViewGroup容器中
     * 2.调用BasePlayer的setTempContext(this);传入当前界面上下文
     * 3.如果用户在这个界面没有切换视频源,则需要在生命周期销毁时不要销毁和停止播放器。因为要将播放器还给上一个界面
     * 二、全局悬浮窗
     * 1.检测IWindowManager.getInstance().getBasePlayer()中的BasePlayer状态
     * 2.调用IWindowManager.getInstance().onClean();从窗口中移除播放器但播放器内部不销毁
     * 3.调用BasePlayer的setTempContext(this);传入当前界面上下文
     * 4.将播放器add到此界面的ViewGroup容器中
     * 5.如果想实现数据的流畅效果，可以在悬浮窗窗口播放器开启之前将此界面的数据设置到临时存储，跳转到此界面时直接取缓存数据渲染到界面上。
     * 三、非转场，新的播放事件
     * 1.直接创建播放器并添加到此ViewGroup中
     * 2.准备初始化和播放
     */
    private void initPlayer() {
        findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;
        FrameLayout playerParent = (FrameLayout) findViewById(R.id.player_container_parent);
        playerParent.removeAllViews();
        if(mIsChange){//接收转场播放任务
            VideoPlayer videoPlayer = PlayerManager.getInstance().getVideoPlayer();
            if(null!=videoPlayer){
                mVideoPlayer=videoPlayer;
                playerParent.addView(mVideoPlayer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                addListener();
            }else{
                defaultPlay(playerParent,true);
            }
        }else if(mIsGlobalWindow){//接收全局悬浮窗窗口播放任务
            BasePlayer basePlayer = IWindowManager.getInstance().getBasePlayer();
            if(null!=basePlayer){
                //1.清除现有悬浮窗窗口播放器组件但不销毁
                IWindowManager.getInstance().onClean();
                mVideoPlayer= (VideoPlayer) basePlayer;
                playerParent.addView(mVideoPlayer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                addListener();
            }else{
                defaultPlay(playerParent,false);
            }
        }else{//根据播放地址播放新的视频
            defaultPlay(playerParent,false);
        }
    }

    /**
     * 默认的初始播放
     * @param playerParent
     * @param addListController 是否添加列表专用交互组件
     */
    private void defaultPlay(FrameLayout playerParent,boolean addListController) {
        mIsGlobalWindow=false;
        if(null!=mParams){
            createPlayer(addListController);
            mVideoPlayer.getController().setTitle(mParams.getTitle());
            mVideoPlayer.setDataSource(mParams.getPlayUrl());
            playerParent.addView(mVideoPlayer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            /**
             * 重要:如果时需要专场的界面跳转到这里来，需要将播放器设置到转场管理者中，如果这个界面用户切换了视频源，则销毁重置官场播放状态
             */
            if(mIsChange){
                PlayerManager.getInstance().setVideoPlayer(mVideoPlayer);
            }
            mVideoPlayer.prepareAsync();//准备播放
        }else{
            Toast.makeText(getApplicationContext(),"不正确的调用,缺少params",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建播放器
     * @param addListController 是否添加列表专用交互组件
     */
    private void createPlayer(boolean addListController) {
        if(null==mVideoPlayer){
            mVideoPlayer=new VideoPlayer(this);
            mVideoPlayer.setLoop(true);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.setLandscapeWindowTranslucent(true);
            mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
            //为播放器添加控制器
            VideoController controller=new VideoController(mVideoPlayer.getContext());
            mVideoPlayer.setController(controller);
            //为控制器添加UI交互组件
            ControlToolBarView toolBarView=new ControlToolBarView(controller.getContext());//标题栏，返回按钮、视频标题、功能按钮、系统时间、电池电量等组件
            toolBarView.setTarget(IVideoController.TARGET_CONTROL_TOOL);
            ControlFunctionBarView functionBarView=new ControlFunctionBarView(controller.getContext());//底部时间、seek、静音、全屏功能栏
            functionBarView.showSoundMute(true,false);//启用静音功能交互\默认不静音
            ControlGestureView gestureView=new ControlGestureView(controller.getContext());//手势控制屏幕亮度、系统音量、快进、快退UI交互
            ControlCompletionView completionView=new ControlCompletionView(controller.getContext());//播放完成、重试
            ControlStatusView statusView=new ControlStatusView(controller.getContext());//移动网络播放提示、播放失败、试看完成
            ControlLoadingView loadingView=new ControlLoadingView(controller.getContext());//加载中、开始播放
            ControWindowView windowView=new ControWindowView(controller.getContext());//悬浮窗窗口UI交互
            if(addListController){
                ControlListView controlListView=new ControlListView(controller.getContext());//列表播放器场景专用交互组件
                controller.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView,controlListView);
            }else{
                controller.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
            }
        }
        addListener();
    }

    /**
     * 设置监听器
     */
    private void addListener(){
        if(null!=mVideoPlayer){
            /**
             * 重要！！！这里存在一种情况，假设用户在列表界面开始播放视频，点击列表后跳转到详情继续播放视频，这时候全屏功能是无效的。所以要更重置上下文为自己
             */
            mVideoPlayer.setParentContext(this);
            //无论是新创建的播放器还是转场过来的播放器,监听事件都必须在当前界面设置
            mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
                /**
                 * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
                 * @return
                 */
                @Override
                public AbstractMediaPlayer createMediaPlayer() {
                    return ExoPlayerFactory.create().createPlayer(VideoDetailsActivity.this);
                }

                @Override
                public IRenderView createRenderView() {
                    return new MediaTextureView(VideoDetailsActivity.this);
                }

                @Override
                public void onPlayerState(PlayerState state, String message) {
                    if(state==PlayerState.STATE_COMPLETION||state==PlayerState.STATE_RESET||state==PlayerState.STATE_STOP){
                        if(null!=mMenuDialog) mMenuDialog.onReset();
                    }
                }
            });
            VideoController controller = (VideoController) mVideoPlayer.getController();
            if(null!=controller){
                controller.setListPlayerMode(false);//从可能的列表模式转换为正常模式
                //启用多功能和设置菜单功能监听器
                IControllerView controllerView = controller.findControlWidgetByTag(IVideoController.TARGET_CONTROL_TOOL);
                if(null!=controllerView&&controllerView instanceof ControlToolBarView){
                    ControlToolBarView controlToolBarView= (ControlToolBarView) controllerView;
                    controlToolBarView.showMenus(false,true,true);
                    controlToolBarView.setOnToolBarActionListener(new ControlToolBarView.OnToolBarActionListener() {
                        @Override
                        public void onWindow() {
                            startGoableWindow();
                        }

                        @Override
                        public void onMenu() {
                            showMenuDialog();
                        }
                    });
                }
            }
        }
    }

    /**
     * 开始播放新的视频
     * @param data
     * @param position 准备播放的位置
     */
    private void startNewPlayer(OpenEyesIndexItemBean data, int position) {
        Logger.d(TAG,"startNewPlayer-->currentPosition:"+mCurrentPosition+",position:"+position);
        if(mCurrentPosition==position) return;
        mIsChange=false;
        PlayerManager.getInstance().setVideoPlayer(null);
        this.mCurrentPosition=position;
        if(null!=data.getCover()){
            GlideModel.getInstance().loadImage((ImageView) findViewById(R.id.video_cover),data.getCover().getFeed());
        }
        createPlayer(false);
        mVideoPlayer.onReset();
        //更新正在播放的视频头部,保存临时参数
        mParams = PlayerManager.getInstance().parseParams(data);
        if(null!=mAdapter&&mAdapter.getData().size()>0){
            mAdapter.getData().get(0).setHeaders(mParams);
            mAdapter.notifyDataSetChanged();
        }
        //开始播放
        mVideoPlayer.getController().setTitle(data.getTitle());
        mVideoPlayer.setDataSource(data.getPlayUrl());
        mVideoPlayer.prepareAsync();
        //获取最新的推荐数据
        if(null!=mPersenter){
            mPersenter.getVideosByVideo(mParams.getId()+"");
        }
    }

    /**
     * 开启全局悬浮窗模式播放,设个时候可以把界面数据缓存下来,
     */
    private void startGoableWindow() {
        if(null!=mVideoPlayer) {
            boolean globalWindow = mVideoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
            Logger.d(TAG, "startGoableWindow-->globalWindow:" + globalWindow);
            if (globalWindow) {
                if(null!=mAdapter){
                    Params params=new Params();
                    if(null!=mParams){
                        params.setId(mParams.getId());
                        params.setParamsJson(null!=mParams?new Gson().toJson(mParams):null);
                    }
                    params.setListJson(new Gson().toJson(mAdapter.getData()));
                    IWindowManager.getInstance().setCoustomParams(params);
                }
                forbidCycle();
                //跳转后设置自定义参数，将在onClick中回调
                close(false);//开启悬浮窗必须关闭Activity
            }
        }
    }

    /**
     * 功能菜单
     */
    private void showMenuDialog() {
        try {
            if(null==mMenuDialog){
                mMenuDialog = new PlayerMenuDialog(this);
                mMenuDialog.setOnMenuActionListener(new OnMenuActionListener() {
                    @Override
                    public void onSpeed(float speed) {
                        if(null!=mVideoPlayer) mVideoPlayer.setSpeed(speed);
                    }

                    @Override
                    public void onZoom(int zoomModel) {
                        if(null!=mVideoPlayer) mVideoPlayer.setZoomModel(zoomModel);
                    }

                    @Override
                    public void onScale(int scale) {

                    }

                    @Override
                    public void onMute(boolean mute) {
                        if(null!=mVideoPlayer) mVideoPlayer.setSoundMute(mute);
                    }

                    @Override
                    public void onMirror(boolean mirror) {
                        if(null!=mVideoPlayer) mVideoPlayer.setMirror(mirror);
                    }
                });
            }
            mMenuDialog.show();
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 禁止生命周期调用(提供给全局悬浮窗设置,悬浮窗需要一个播放器常驻内存)
     */
    protected void forbidCycle(){
        this.isForbidCycle=true;
    }

    @Override
    public void showVideos(List<OpenEyesIndexItemBean> data, boolean isRestart) {
        if(isFinishing()) return;
        findViewById(R.id.tv_loading).setVisibility(View.GONE);
        if(null!=mAdapter){
            //添加一条头部数据到顶部
            if(null!=mParams){
                OpenEyesIndexItemBean indexItemBean=new OpenEyesIndexItemBean();
                indexItemBean.setType("header");
                indexItemBean.setHeaders(mParams);
                data.add(0,indexItemBean);
            }
            mAdapter.setNewData(data);
            if(null!=mLayoutManager) mLayoutManager.scrollToPositionWithOffset(0,0);
        }
    }

    @Override
    public void showLoading() {
        if(null!=mAdapter&&mAdapter.getData().size()>0){

        }else{
            findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showError(int code, String errorMsg) {
        TextView error = (TextView) findViewById(R.id.tv_loading);
        error.setText(errorMsg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null!=mVideoPlayer) mVideoPlayer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isForbidCycle){

        }else{
            if(mIsChange){
                if(!isFinish){//转场播放生效中或全局悬浮窗生效中时,当界面不可见时不要调用生命周期，此时正在关闭activity并将播放器交给上一级界面
                    mVideoPlayer.onPause();
                }
            }else{
                mVideoPlayer.onPause();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if(null!=mVideoPlayer){
            mVideoPlayer.setParentContext(null);
            if(mVideoPlayer.isBackPressed()){
                isFinish=true;
                close(true);
            }
            return;
        }
        isFinish=true;
        close(true);
    }

    /**
     * 转场的播放返回时设置setResult,其它情况不用设置setResult
     * @param isChange
     */
    private void close(boolean isChange){
        Logger.d(TAG,"close-->isChange:"+isChange);
        if(isChange){
            Intent intent=new Intent();
            intent.putExtra("status","1");
            setResult(102,intent);
        }else{
            mIsChange=false;
            PlayerManager.getInstance().setVideoPlayer(null);
        }
        finish();
    }


    /**
     * 专场播放的Activity不要销毁播放器
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mMenuDialog){
            mMenuDialog.dismiss();
            mMenuDialog=null;
        }
        //重要:悬浮窗接收的Activity或其它组件中设置临时的Context为自己，在界面关闭时调用setTempContext(null)置空Context
        if(null!=mVideoPlayer){
            if(isForbidCycle){

            }else{
                if(!mIsChange){
                    mVideoPlayer.onDestroy();//非转场或非全局悬浮窗窗口播放时销毁播放器
                }
            }
        }
    }
}