package com.android.videoplayer.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.IjkPlayerFactory;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.iplayer.widget.controls.ControWindowView;
import com.android.iplayer.widget.controls.ControlCompletionView;
import com.android.iplayer.widget.controls.ControlFunctionBarView;
import com.android.iplayer.widget.controls.ControlGestureView;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.iplayer.widget.controls.ControlToolBarView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.base.adapter.interfaces.OnItemClickListener;
import com.android.videoplayer.bean.Menu;
import com.android.videoplayer.pager.activity.PagerPlayerActivity;
import com.android.videoplayer.ui.adapter.MainMenuAdapter;
import com.android.videoplayer.ui.widget.ProjectDialog;
import com.android.videoplayer.ui.widget.TitleView;
import com.android.videoplayer.utils.DataFactory;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.ScreenUtils;
import com.android.videoplayer.utils.WindowPermission;
import com.android.videoplayer.video.ui.activity.PagerListActivity;

/**
 * created by hty
 * 2022/6/28
 * Desc:示例入口
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getResources().getString(R.string.app_name));
        titleView.enableTitleBack(false);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(),LinearLayoutManager.VERTICAL,false));
        MainMenuAdapter adapter=new MainMenuAdapter(DataFactory.getInstance().getMenus());
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                boolean isForbidAnimation=false;
                if(null!=view&&view.getTag() instanceof Menu){
                    Menu menu= (Menu) view.getTag();
                    Intent intent=null;
                    switch (menu.getId()) {
                        case 1://SDK默认播放器
                            intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_default,"SDK默认播放器"));
                            break;
                        case 2://直播拉流
                            intent = new Intent(MainActivity.this, LivePlayerActivity.class);
                            intent.putExtra("islive",true);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_live,"直播拉流"));
                            break;
                        case 3://多播放器同时播放
                            intent=new Intent(MainActivity.this, VideosPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_videos,"多播放器同时播放"));
                            break;
                        case 4://全屏播放
                            startFullScreen();
                            break;
                        case 5://收费试看模式
                            intent=new Intent(MainActivity.this, PerviewPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_perview,"收费试看模式"));
                            break;
                        case 6://raw和assets资源播放
                            intent=new Intent(MainActivity.this, AssetsPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_resouce,"Raw和Assets资源播放"));
                            break;
                        case 7://连续播放一个列表示例
                            intent=new Intent(MainActivity.this, VideoListPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_list,"连续播放一个列表示例"));
                            break;
                        case 8://列表自动播放(无缝转场)
                            intent = new Intent(MainActivity.this, PagerListActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_auto,"列表自动播放"));
                            intent.putExtra("auto_play","1");
                            break;
                        case 9://列表点击播放(无缝转场)
                            intent = new Intent(MainActivity.this, PagerListActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_click,"列表点击播放"));
                            intent.putExtra("auto_play","0");
                            break;
                        case 10://Activity小窗口
                            intent = new Intent(MainActivity.this, WindowPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_window,"Activity局部悬浮窗"));
                            break;
                        case 11://全局悬浮窗
                            intent = new Intent(MainActivity.this, WindowGlobalPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_goable_window,"全局悬浮窗"));
                            break;
                        case 12://任意界面开启窗口播放器
                            startMiniWindowPlayer();
                            break;
                        case 13://任意界面开启全局悬浮窗播放器
                            checkedPermission();
                            break;
                        case 14://画中画
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                intent=new Intent(MainActivity.this, PiPPlayerActivity.class);
                                intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_dip,"画中画"));
                            }
                            break;
                        case 15://类抖音垂直滚动播放
                            intent=new Intent(MainActivity.this, PagerPlayerActivity.class);
                            break;
                        case 16://自定义弹幕控制器功能示例
                            intent = new Intent(MainActivity.this, DanmuPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_danmu,"自定义弹幕控制器"));
                            break;
                        case 17://预缓存
                            intent = new Intent(MainActivity.this, VideoCacheActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_cache,"视频预缓存"));
                            break;
                        case 18://项目主页 https://gitee.com/hy_Yuye/iPlayer
                            ProjectDialog dialog=new ProjectDialog(MainActivity.this);
                            dialog.setOnMenuActionListener(new ProjectDialog.OnMenuActionListener() {
                                @Override
                                public void onSelected(String url) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    intent.setData(Uri.parse(url));
                                    startActivity(intent);
                                }
                            });
                            dialog.show();
                            break;
                    }
                    if(null!=intent){
                        startActivity(intent);
                        if(isForbidAnimation){
                            overridePendingTransition(0, 0);
                        }
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    private void checkedPermission() {
        boolean hasPermission = PlayerUtils.getInstance().checkWindowsPermission(this);//检查是否获取了悬浮窗权限
        if(hasPermission){
            startGlobalWindowPlayer();
        }else{
            WindowPermission.getInstance().startRequstPermission(this, new WindowPermission.OnRuntimePermissionListener() {
                @Override
                public void onRequstPermissionResult(boolean success) {
                    if(success){
                        startGlobalWindowPlayer();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),DataFactory.getInstance().getString(R.string.text_permission_window,"开启失败,需要悬浮窗权限!"),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 任意界面创建一个全局悬浮窗窗口播放器并开始播放
     * 点击悬浮窗跳转到Activity示例，请参考App中的IWindowManager.getInstance().setOnWindowActionListener(OnWindowActionListener listener);
     */
    private void startGlobalWindowPlayer() {
        VideoPlayer videoPlayer = new VideoPlayer(MainActivity.this);
        videoPlayer.setLoop(false);
        videoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.setDataSource(MP4_URL2);//播放地址设置
        VideoController controller = videoPlayer.createController();//初始化一个默认的控制器(内部适用默认的一套交互UI控制器组件)
        WidgetFactory.bindDefaultControls(controller,false,true);
        controller.setTitle("任意界面开启一个悬浮窗窗口播放器");//视频标题(默认视图控制器横屏可见)
        //悬浮窗口播放默认开启自动吸附悬停，如需禁用请阅读多参方法参数说明
        boolean globalWindow = videoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
        Logger.d(TAG,"startGoableWindow-->globalWindow:"+globalWindow);
        if(globalWindow) {
            IWindowManager.getInstance().setCoustomParams(null);//给悬浮窗口播放器绑定自定义参数，在点击窗口播放器跳转至Activity时有用
            videoPlayer.prepareAsync();//开始异步准备播放,注意界面关闭不要销毁播放器实例
        }
    }

    /**
     * 任意界面创建一个Activity Window级别窗口播放器并开始播放
     */
    private void startMiniWindowPlayer() {
        if(null==mVideoPlayer){
            mVideoPlayer = new VideoPlayer(this);
            VideoController controller=new VideoController(this);
            //给播放器设置控制器
            mVideoPlayer.setController(controller);
            //给播放器控制器绑定需要的自定义UI交互组件
            ControWindowView controWindowView=new ControWindowView(this);//加载中、开始播放
            controller.addControllerWidget(controWindowView);
            //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
            mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
                /**
                 * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
                 * @return
                 */
                @Override
                public AbstractMediaPlayer createMediaPlayer() {
                    return IjkPlayerFactory.create().createPlayer(MainActivity.this);
                }

                @Override
                public void onPlayerState(PlayerState state, String message) {
                    Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
                    if(PlayerState.STATE_DESTROY==state){
                        mVideoPlayer=null;
                    }
                }
            });
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(MP4_URL2);//播放地址设置
            //自定义窗口播放的宽,高,起始X轴,起始Y轴属性,这里示例将播放器添加到标题栏下方右侧，距离顶部及右侧编剧12dp
//            mVideoPlayer.startWindow();
            int[] screenLocation=new int[2];
            TitleView titleView = findViewById(R.id.title_view);
            titleView.getLocationInWindow(screenLocation);
            int width = (PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2)+ScreenUtils.getInstance().dpToPxInt(30f);
            int height = width*9/16;
            float startX=PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2-PlayerUtils.getInstance().dpToPxInt(42f);//开始位置
            float startY=screenLocation[1]+titleView.getHeight()+PlayerUtils.getInstance().dpToPxInt(15f);
            //启动窗口播放
//            mVideoPlayer.startWindow(PlayerUtils.getInstance().dpToPxInt(3f),Color.parseColor("#80000000"),true);
            //悬浮窗口播放默认开启自动吸附悬停，如需禁用请阅读多参方法参数说明
            mVideoPlayer.startWindow(width,height,startX,startY,ScreenUtils.getInstance().dpToPxInt(3f),Color.parseColor("#99000000"));//初始显示的位置并添加窗口颜色和圆角大小
            mVideoPlayer.prepareAsync();//开始异步准备播放
        }
    }

    /**
     * 任意界面创建一个全屏窗口播放器并开始播放
     */
    private void startFullScreen() {
        VideoPlayer videoPlayer = new VideoPlayer(this);
        videoPlayer.setBackgroundColor(Color.parseColor("#000000"));
        VideoController controller=new VideoController(videoPlayer.getContext());
        /**
         * 给播放器设置控制器
         */
        videoPlayer.setController(controller);
        /**
         * 给播放器控制器绑定需要的自定义UI交互组件
         */
        ControlToolBarView toolBarView=new ControlToolBarView(this);//标题栏，返回按钮、视频标题、功能按钮、系统时间、电池电量等组件
        ControlFunctionBarView functionBarView=new ControlFunctionBarView(this);//底部时间、seek、静音、全屏功能栏
        functionBarView.showSoundMute(true,false);//启用静音功能交互\默认不静音
        ControlStatusView statusView=new ControlStatusView(this);//移动网络播放提示、播放失败、试看完成
        ControlGestureView gestureView=new ControlGestureView(this);//手势控制屏幕亮度、系统音量、快进、快退UI交互
        ControlCompletionView completionView=new ControlCompletionView(this);//播放完成、重试
        ControlLoadingView loadingView=new ControlLoadingView(this);//加载中、开始播放
        controller.addControllerWidget(toolBarView,functionBarView,statusView,gestureView,completionView,loadingView);
        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        videoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return IjkPlayerFactory.create().createPlayer(MainActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
        videoPlayer.setLandscapeWindowTranslucent(true);//全屏沉浸样式
        videoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
        videoPlayer.setLoop(false);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
        videoPlayer.setDataSource(MP4_URL2);//播放地址设置
        videoPlayer.startFullScreen();//开启全屏播放
        videoPlayer.prepareAsync();//开始异步准备播放
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
            if(mVideoPlayer.isBackPressed()){
                super.onBackPressed();
            }
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IWindowManager.getInstance().quitGlobaWindow();
    }
}