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
import com.android.iplayer.media.core.ExoPlayerFactory;
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
 * Desc:????????????
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
                        case 1://SDK???????????????
                            intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_default,"SDK???????????????"));
                            break;
                        case 2://????????????
                            intent = new Intent(MainActivity.this, LivePlayerActivity.class);
                            intent.putExtra("islive",true);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_live,"????????????"));
                            break;
                        case 3://????????????????????????
                            intent=new Intent(MainActivity.this, VideosPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_videos,"????????????????????????"));
                            break;
                        case 4://????????????
                            startFullScreen();
                            break;
                        case 5://??????????????????
                            intent=new Intent(MainActivity.this, PerviewPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_perview,"??????????????????"));
                            break;
                        case 6://raw???assets????????????
                            intent=new Intent(MainActivity.this, AssetsPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_resouce,"Raw???Assets????????????"));
                            break;
                        case 7://??????????????????????????????
                            intent=new Intent(MainActivity.this, VideoListPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_list,"??????????????????????????????"));
                            break;
                        case 8://??????????????????(????????????)
                            intent = new Intent(MainActivity.this, PagerListActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_auto,"??????????????????"));
                            intent.putExtra("auto_play","1");
                            break;
                        case 9://??????????????????(????????????)
                            intent = new Intent(MainActivity.this, PagerListActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_click,"??????????????????"));
                            intent.putExtra("auto_play","0");
                            break;
                        case 10://Activity?????????
                            intent = new Intent(MainActivity.this, WindowPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_window,"Activity???????????????"));
                            break;
                        case 11://???????????????
                            intent = new Intent(MainActivity.this, WindowGlobalPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_goable_window,"???????????????"));
                            break;
                        case 12://?????????????????????????????????
                            startMiniWindowPlayer();
                            break;
                        case 13://??????????????????????????????????????????
                            checkedPermission();
                            break;
                        case 14://?????????
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                intent=new Intent(MainActivity.this, PiPPlayerActivity.class);
                                intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_dip,"?????????"));
                            }
                            break;
                        case 15://???????????????????????????
                            intent=new Intent(MainActivity.this, PagerPlayerActivity.class);
                            break;
                        case 16://????????????????????????????????????
                            intent = new Intent(MainActivity.this, DanmuPlayerActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_title_danmu,"????????????????????????"));
                            break;
                        case 17://?????????
                            intent = new Intent(MainActivity.this, VideoCacheActivity.class);
                            intent.putExtra("title",DataFactory.getInstance().getString(R.string.text_item_cache,"???????????????"));
                            break;
                        case 18://???????????? https://gitee.com/hy_Yuye/iPlayer
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
        boolean hasPermission = PlayerUtils.getInstance().checkWindowsPermission(this);//????????????????????????????????????
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
                    Toast.makeText(getApplicationContext(),DataFactory.getInstance().getString(R.string.text_permission_window,"????????????,?????????????????????!"),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     * ????????????????????????Activity??????????????????App??????IWindowManager.getInstance().setOnWindowActionListener(OnWindowActionListener listener);
     */
    private void startGlobalWindowPlayer() {
        VideoPlayer videoPlayer = new VideoPlayer(MainActivity.this);
        videoPlayer.setLoop(false);
        videoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.setDataSource(MP4_URL2);//??????????????????
        VideoController controller = videoPlayer.createController();//?????????????????????????????????(?????????????????????????????????UI???????????????)
        WidgetFactory.bindDefaultControls(controller,false,true);
        controller.setTitle("????????????????????????????????????????????????");//????????????(?????????????????????????????????)
        //????????????????????????????????????????????????????????????????????????????????????????????????
        boolean globalWindow = videoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
        Logger.d(TAG,"startGoableWindow-->globalWindow:"+globalWindow);
        if(globalWindow) {
            IWindowManager.getInstance().setCoustomParams(null);//?????????????????????????????????????????????????????????????????????????????????Activity?????????
            videoPlayer.prepareAsync();//????????????????????????,?????????????????????????????????????????????
        }
    }

    /**
     * ????????????????????????Activity Window????????????????????????????????????
     */
    private void startMiniWindowPlayer() {
        if(null==mVideoPlayer){
            mVideoPlayer = new VideoPlayer(this);
            VideoController controller=new VideoController(this);
            //???????????????????????????
            mVideoPlayer.setController(controller);
            //?????????????????????????????????????????????UI????????????
            ControWindowView controWindowView=new ControWindowView(this);//????????????????????????
            controller.addControllerWidget(controWindowView);
            //?????????????????????????????????????????????setOnPlayerActionListener?????????????????????????????????
            mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
                /**
                 * ?????????????????????????????????,??????null,?????????????????????????????????????????????
                 * @return
                 */
                @Override
                public AbstractMediaPlayer createMediaPlayer() {
                    return ExoPlayerFactory.create().createPlayer(MainActivity.this);
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
            mVideoPlayer.getController().setTitle("??????????????????");//????????????(?????????????????????????????????)
            mVideoPlayer.setDataSource(MP4_URL2);//??????????????????
            //???????????????????????????,???,??????X???,??????Y?????????,????????????????????????????????????????????????????????????????????????????????????12dp
//            mVideoPlayer.startWindow();
            int[] screenLocation=new int[2];
            TitleView titleView = findViewById(R.id.title_view);
            titleView.getLocationInWindow(screenLocation);
            int width = (PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2)+ScreenUtils.getInstance().dpToPxInt(30f);
            int height = width*9/16;
            float startX=PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2-PlayerUtils.getInstance().dpToPxInt(42f);//????????????
            float startY=screenLocation[1]+titleView.getHeight()+PlayerUtils.getInstance().dpToPxInt(15f);
            //??????????????????
//            mVideoPlayer.startWindow(PlayerUtils.getInstance().dpToPxInt(3f),Color.parseColor("#80000000"),true);
            //????????????????????????????????????????????????????????????????????????????????????????????????
            mVideoPlayer.startWindow(width,height,startX,startY,ScreenUtils.getInstance().dpToPxInt(3f),Color.parseColor("#99000000"));//?????????????????????????????????????????????????????????
            mVideoPlayer.prepareAsync();//????????????????????????
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    private void startFullScreen() {
        VideoPlayer videoPlayer = new VideoPlayer(this);
        videoPlayer.setBackgroundColor(Color.parseColor("#000000"));
        VideoController controller=new VideoController(videoPlayer.getContext());
        /**
         * ???????????????????????????
         */
        videoPlayer.setController(controller);
        /**
         * ?????????????????????????????????????????????UI????????????
         */
        ControlToolBarView toolBarView=new ControlToolBarView(this);//?????????????????????????????????????????????????????????????????????????????????????????????
        ControlFunctionBarView functionBarView=new ControlFunctionBarView(this);//???????????????seek???????????????????????????
        functionBarView.showSoundMute(true,false);//????????????????????????\???????????????
        ControlStatusView statusView=new ControlStatusView(this);//??????????????????????????????????????????????????????
        ControlGestureView gestureView=new ControlGestureView(this);//?????????????????????????????????????????????????????????UI??????
        ControlCompletionView completionView=new ControlCompletionView(this);//?????????????????????
        ControlLoadingView loadingView=new ControlLoadingView(this);//????????????????????????
        controller.addControllerWidget(toolBarView,functionBarView,statusView,gestureView,completionView,loadingView);
        //?????????????????????????????????????????????setOnPlayerActionListener?????????????????????????????????
        videoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * ?????????????????????????????????,??????null,?????????????????????????????????????????????
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return ExoPlayerFactory.create().createPlayer(MainActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
        videoPlayer.setLandscapeWindowTranslucent(true);//??????????????????
        videoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
        videoPlayer.setLoop(false);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.getController().setTitle("??????????????????");//????????????(?????????????????????????????????)
        videoPlayer.setDataSource(MP4_URL2);//??????????????????
        videoPlayer.startFullScreen();//??????????????????
        videoPlayer.prepareAsync();//????????????????????????
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