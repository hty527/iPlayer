package com.android.iplayer.widget.controls;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.iplayer.R;
import com.android.iplayer.base.BaseControllerWidget;
import com.android.iplayer.interfaces.IMediaPlayer;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/8/22
 * Desc:UI控制器-列表播放器场景定制UI
 */
public class ControlListView extends BaseControllerWidget implements View.OnClickListener {

    private TextView mSurplusDuration;//剩余时间

    public ControlListView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_list_view;
    }

    @Override
    public void initViews() {
        hide();
        mSurplusDuration = findViewById(R.id.controller_surplus_duration);
        findViewById(R.id.controller_list_mute).setOnClickListener(this);
        findViewById(R.id.controller_list_fullscreen).setOnClickListener(this);
        updateMute();
    }

    /**
     * 更新静音状态
     */
    private void updateMute(){
        if(null!= mControlWrapper){
            boolean soundMute = mControlWrapper.isSoundMute();
            ImageView muteImge = (ImageView) findViewById(R.id.controller_list_mute);
            muteImge.setImageResource(soundMute?R.mipmap.ic_player_mute_true:R.mipmap.ic_player_mute_false);
        }
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        switch (state) {
            case STATE_RESET://初始状态\播放器还原重置
            case STATE_STOP://初始\停止
            case STATE_ERROR://播放失败
            case STATE_COMPLETION://播放结束
                onReset();
                break;
            case STATE_PREPARE://初次准备中不显示交互组件
                hide();
                break;
            case STATE_START://首次播放
                if(isListPlayerScene()){
                    show();
                }
                break;
        }
    }

    @Override
    public void onOrientation(int direction) {
        if(IMediaPlayer.ORIENTATION_LANDSCAPE==direction){
            hide();
        }else{
            if(isListPlayerScene()){
                show();
            }else{
                hide();
            }
        }
    }

    @Override
    public void onPlayerScene(int scene) {
        if(isOrientationPortrait()){
            if(isListPlayerScene(scene)){
                if(isPlaying()){
                    show();
                }
            }else{
                hide();
            }
        }else{
            hide();
        }
    }

    @Override
    public void onProgress(long currentDurtion, long totalDurtion) {
        try {
            if(null!=mSurplusDuration) mSurplusDuration.setText(PlayerUtils.getInstance().stringForAudioTime(totalDurtion-currentDurtion));
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMute(boolean isMute) {
        ImageView muteImage = (ImageView) findViewById(R.id.controller_list_mute);
        muteImage.setImageResource(isMute?R.mipmap.ic_player_mute_true:R.mipmap.ic_player_mute_false);
    }

    @Override
    public void onClick(View view) {
        if(null!=mControlWrapper){
            if (view.getId() == R.id.controller_list_mute) {
                mControlWrapper.toggleMute();
            }else if (view.getId() == R.id.controller_list_fullscreen) {
                mControlWrapper.toggleFullScreen();
            }
        }
    }

    /**
     * 是否显示静音按钮
     * @param showSound 是否显示静音按钮,true:显示 false:隐藏
     * @param soundMute 是否静音,true:静音 false:系统原声
     */
    public void showSoundMute(boolean showSound,boolean soundMute){
        ImageView muteImage = (ImageView) findViewById(R.id.controller_list_mute);
        muteImage.setVisibility(showSound?View.VISIBLE:View.GONE);
        if(null!=mControlWrapper) mControlWrapper.setSoundMute(soundMute);//UI状态将在onMute回调中处理
    }

    @Override
    public void onReset() {
        if(null!=mSurplusDuration) mSurplusDuration.setText(PlayerUtils.getInstance().stringForAudioTime(0));
    }
}