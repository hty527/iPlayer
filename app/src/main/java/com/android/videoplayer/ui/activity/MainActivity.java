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
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;
import com.android.iplayer.widget.VideoPlayer;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.base.adapter.interfaces.OnItemClickListener;
import com.android.videoplayer.bean.Menu;
import com.android.videoplayer.media.JkMediaPlayer;
import com.android.videoplayer.pager.activity.PagerPlayerActivity;
import com.android.videoplayer.ui.adapter.MainMenuAdapter;
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
                            intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
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
                        case 5://raw和assets资源播放
                            intent=new Intent(MainActivity.this, AssetsPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_resouce,"Raw和Assets资源播放"));
                            break;
                        case 6://连续播放一个列表示例
                            intent=new Intent(MainActivity.this, VideoListPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_list,"连续播放一个列表示例"));
                            break;
                        case 7://列表自动播放(无缝转场)
                            intent = new Intent(MainActivity.this, PagerListActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_auto,"列表自动播放"));
                            intent.putExtra("auto_play","1");
                            break;
                        case 8://列表点击播放(无缝转场)
                            intent = new Intent(MainActivity.this, PagerListActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_click,"列表点击播放"));
                            intent.putExtra("auto_play","0");
                            break;
                        case 9://Activity小窗口
                            intent = new Intent(MainActivity.this, WindowPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_window,"Activity局部悬浮窗"));
                            break;
                        case 10://全局悬浮窗
                            intent = new Intent(MainActivity.this, WindowGlobalPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_goable_window,"全局悬浮窗"));
                            break;
                        case 11://任意界面开启窗口播放器
                            startMiniWindowPlayer();
                            break;
                        case 12://任意界面开启全局悬浮窗播放器
                            checkedPermission();
                            break;
                        case 13://画中画
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                intent=new Intent(MainActivity.this, PiPPlayerActivity.class);
                                intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_dip,"画中画"));
                            }
                            break;
                        case 14://类抖音垂直滚动播放
                            intent=new Intent(MainActivity.this, PagerPlayerActivity.class);
                            break;
                        case 15://自定义弹幕控制器功能示例
                            intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                            intent.putExtra("danmu",true);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_danmu,"自定义弹幕控制器"));
                            break;
                        case 16://项目主页 https://gitee.com/hty_Yuye/iPlayer
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse("https://github.com/hty527/iPlayer"));
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
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.setTitle("任意界面开启一个悬浮窗窗口播放器");//视频标题(默认视图控制器横屏可见)
        videoPlayer.setDataSource(URL2);//播放地址设置
        videoPlayer.initController();//初始化一个默认的控制器
        boolean globalWindow = videoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
        Logger.d(TAG,"startGoableWindow-->globalWindow:"+globalWindow);
        if(globalWindow) {
            IWindowManager.getInstance().setCoustomParams(null);//给悬浮窗口播放器绑定自定义参数，在点击窗口播放器跳转至Activity时有用
            Logger.d(TAG, "startGoableWindow-->mVideoPlayer:" + videoPlayer);
            videoPlayer.playOrPause();//开始异步准备播放,注意界面关闭不要销毁播放器实例
        }
    }

    /**
     * 任意界面创建一个Activity Window级别窗口播放器并开始播放
     */
    private void startMiniWindowPlayer() {
        if(null==mVideoPlayer){
            mVideoPlayer = new VideoPlayer(this);
            mVideoPlayer.initController();//初始化一个默认的控制器
            //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
            mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
                /**
                 * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
                 * @return
                 */
                @Override
                public AbstractMediaPlayer createMediaPlayer() {
                    return new JkMediaPlayer(MainActivity.this);
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
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(URL2);//播放地址设置
            //自定义窗口播放的宽,高,起始X轴,起始Y轴属性,这里示例将播放器添加到标题栏下方右侧
//            mVideoPlayer.startWindow();
            int[] screenLocation=new int[2];
            TitleView titleView = findViewById(R.id.title_view);
            titleView.getLocationInWindow(screenLocation);
            int width = (PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2)+ScreenUtils.getInstance().dpToPxInt(30f);
            int height = width*9/16;
            float startX=PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2-PlayerUtils.getInstance().dpToPxInt(45f);//开始位置
            float startY=screenLocation[1]+titleView.getHeight()+PlayerUtils.getInstance().dpToPxInt(15f);
            //启动窗口播放
            mVideoPlayer.startWindow(width,height,startX,startY,ScreenUtils.getInstance().dpToPxInt(3f),Color.parseColor("#99000000"));//初始显示的位置并添加窗口颜色和圆角大小
//            mVideoPlayer.startWindow(ScreenUtils.getInstance().dpToPxInt(3f),Color.parseColor("#99000000"));//也可以使用内部默认的窗口宽高和位置属性
            mVideoPlayer.playOrPause();//开始异步准备播放
        }
    }

    /**
     * 任意界面创建一个全屏窗口播放器并开始播放
     */
    private void startFullScreen() {
        VideoPlayer videoPlayer = new VideoPlayer(this);
        videoPlayer.setBackgroundColor(Color.parseColor("#000000"));
        VideoController controller = videoPlayer.initController();//绑定默认的控制器
        controller.enableFullScreen(false);
        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        videoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return new JkMediaPlayer(MainActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
//        videoPlayer.setPreViewTotalDuration("3600");//注意:设置虚拟总时长(一旦设置播放器内部走片段试看流程)
        videoPlayer.setLoop(false);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
        videoPlayer.setDataSource(URL2);//播放地址设置
        videoPlayer.startFullScreen();//开启全屏播放
        videoPlayer.playOrPause();//开始异步准备播放
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