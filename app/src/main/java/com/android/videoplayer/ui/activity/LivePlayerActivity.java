package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.iplayer.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.media.core.IjkPlayerFactory;
import com.android.iplayer.widget.VideoPlayer;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.videoplayer.R;
import com.android.videoplayer.base.BaseActivity;
import com.android.videoplayer.base.BasePresenter;
import com.android.videoplayer.controller.LiveControllerControl;
import com.android.videoplayer.pager.widget.ControlLiveView;

/**
 * created by hty
 * 2022/8/25
 * Desc:这是一个直播拉流和简单的自定义直播组件交互的实例
 */
public class LivePlayerActivity extends BaseActivity implements View.OnClickListener {

    private String mUrl=LIVE_M3U;
    private int MEDIA_CORE=2;//这里用IJkMediaPlayer作为初始解码器
    private View[] buttons=new View[3];
    private TextView mPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        findViewById(R.id.live_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initViews();
        initPlayer();
    }

    private void initViews() {
        buttons[0]=findViewById(R.id.btn_core_1);
        buttons[1]=findViewById(R.id.btn_core_2);
        buttons[2]=findViewById(R.id.btn_core_3);
        buttons[MEDIA_CORE].setSelected(true);
        buttons[0].setOnClickListener(this);
        buttons[1].setOnClickListener(this);
        buttons[2].setOnClickListener(this);
        buttons[0].setTag(0);
        buttons[1].setTag(1);
        buttons[2].setTag(2);
        //测试地址播放
        mPlay = (TextView) findViewById(R.id.play);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.input);
                String url = editText.getText().toString().trim();
                if(TextUtils.isEmpty(url)){
                    Toast.makeText(getApplicationContext(),"请粘贴或输入直播流地址后再播放!",Toast.LENGTH_SHORT).show();
                    return;
                }
                mUrl=url;
                reStartPlay();
            }
        });
        ((EditText) findViewById(R.id.input)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(null!=mPlay) mPlay.setSelected(!TextUtils.isEmpty(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    private void initPlayer() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        mVideoPlayer.getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//给播放器固定一个高度
        mVideoPlayer.setLoop(true);
        mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);//设置视频画面渲染模式为：原始大小模式
        //给播放器设置一个控制器
        LiveControllerControl controller = new LiveControllerControl(mVideoPlayer.getContext());
        mVideoPlayer.setController(controller);
        //给控制器添加需要的UI交互组件
        ControlLoadingView controlLoadingView = new ControlLoadingView(controller.getContext());//加载中、开始播放按钮
        ControlStatusView controlStatusView = new ControlStatusView(controller.getContext());//播放失败、移动网络播放提示
        ControlLiveView controlLiveView = new ControlLiveView(controller.getContext());//自定义直播场景交互UI组件
        controller.addControllerWidget(controlLoadingView,controlStatusView,controlLiveView);
        //自定义解码器
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                if (1 == MEDIA_CORE) {
                    return IjkPlayerFactory.create(true).createPlayer(LivePlayerActivity.this);
                } else if (2 == MEDIA_CORE) {
                    return ExoPlayerFactory.create().createPlayer(LivePlayerActivity.this);
                } else {
                    return null;
                }
            }
        });
        mVideoPlayer.setDataSource(mUrl);
        mVideoPlayer.prepareAsync();//准备播放
    }

    @Override
    public void onClick(View view) {
        if((int)view.getTag()==MEDIA_CORE) return;
        switch (view.getId()) {
            case R.id.btn_core_1:
                MEDIA_CORE =0;
                break;
            case R.id.btn_core_2:
                MEDIA_CORE =1;
                break;
            case R.id.btn_core_3:
                MEDIA_CORE =2;
                break;
        }
        for (View button : buttons) {
            button.setSelected(false);
        }
        buttons[MEDIA_CORE].setSelected(true);
        reStartPlay();
    }

    /**
     * 重新播放
     */
    private void reStartPlay() {
        if(null!=mVideoPlayer){
            mVideoPlayer.onReset();
            mVideoPlayer.setDataSource(mUrl);
            mVideoPlayer.prepareAsync();
        }
    }
}