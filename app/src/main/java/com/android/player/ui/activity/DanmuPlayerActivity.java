package com.android.player.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.WidgetFactory;
import com.android.player.R;
import com.android.player.base.BaseActivity;
import com.android.player.base.BasePresenter;
import com.android.player.controller.DanmuWidgetView;
import com.android.player.ui.widget.TitleView;
import com.android.player.utils.DataFactory;

/**
 * created by hty
 * 2022/6/22
 * Desc:这是一个支持带弹幕控制的常规视频播放器控件封装的示例
 */
public class DanmuPlayerActivity extends BaseActivity {

    private DanmuWidgetView mDanmuWidgetView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(new TitleView.OnTitleActionListener() {
            @Override
            public void onBack() {
                onBackPressed();
            }
        });
        initPlayer();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer() {
        initSetting();
        //播放器播放之前准备工作
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//给播放器固定一个高度
        //给播放器设置一个控制器
        VideoController controller = mVideoPlayer.initController();
        WidgetFactory.bindDefaultControls(controller);
        mDanmuWidgetView = new DanmuWidgetView(controller.getContext());
        //将弹幕组件添加到控制器最底层
        controller.addControllerWidget(mDanmuWidgetView,0);
        mDanmuWidgetView.setDanmuData(DataFactory.getInstance().getDanmus());//添加弹幕数据
        mVideoPlayer.getController().setTitle("弹幕视频测试播放地址");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer.setDataSource(MP4_URL1);//播放地址设置
        mVideoPlayer.prepareAsync();//开始异步准备播放
    }

    private void initSetting() {
        //如果是弹幕播放场景则添加弹幕组件
        Switch aSwitch = (Switch) findViewById(R.id.switch_danmu);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(null!= mDanmuWidgetView){
                    if(isChecked){
                        mDanmuWidgetView.openDanmu();
                        ((TextView) findViewById(R.id.tv_danmu)).setText("关闭弹幕");
                    }else{
                        mDanmuWidgetView.closeDanmu();
                        ((TextView) findViewById(R.id.tv_danmu)).setText("开启弹幕");
                    }
                }
            }
        });
        findViewById(R.id.danmu_content).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_send_danmu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!= mDanmuWidgetView){
                    mDanmuWidgetView.addDanmuItem("这是我发的有颜色的弹幕！",true);
                }
            }
        });
    }
}