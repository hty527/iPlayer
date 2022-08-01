package com.android.iplayer.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.iplayer.R;
import com.android.iplayer.controller.VideoController;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/6/30
 * Desc:播放器试看结束\移动网络提示\播放失败提示
 */
public class ControllerStatusView extends LinearLayout {

    private int mScene;

    public ControllerStatusView(Context context) {
        this(context,null);
    }

    public ControllerStatusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ControllerStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.player_view_status,this);
        View btnContinue = findViewById(R.id.player_status_btn);
        PlayerUtils.getInstance().setOutlineProvider(findViewById(R.id.player_status_btn),PlayerUtils.getInstance().dpToPxInt(18f));
        btnContinue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=mOnStatusListener) mOnStatusListener.onEvent(mScene);
            }
        });
    }

    /**
     * 改变场景
     * @param scene 0:移动网络播放提示 1:试看结束 2:播放失败
     */
    public void setScene(int scene){
        setScene(scene,null);
    }

    /**
     * 改变场景
     * @param scene 1:移动网络播放提示 2:试看结束 3:播放失败
     * @param tipsStr 提示文字
     */
    public void setScene(int scene,String tipsStr){
        setScene(scene,tipsStr,null);
    }

    /**
     * 改变场景
     * @param scene 1:移动网络播放提示 2:试看结束 3:播放失败
     * @param tipsStr 提示文字
     * @param btnStr 按钮文字
     */
    public void setScene(int scene,String tipsStr,String btnStr){
        this.mScene=scene;
        TextView tips = (TextView) findViewById(R.id.player_status_tips);
        TextView btn = (TextView) findViewById(R.id.player_status_btn);
        tips.setText(PlayerUtils.getInstance().formatHtml(getTipsStr(scene,tipsStr)));
        btn.setText(PlayerUtils.getInstance().formatHtml(getBtnStr(scene,btnStr)));
    }

    /**
     * 返回按钮文案
     * @param scene
     * @param btnStr
     * @return
     */
    private String getBtnStr(int scene, String btnStr) {
        if(!TextUtils.isEmpty(btnStr)) return btnStr;
        switch (scene) {
            case VideoController.SCENE_MOBILE:
                return getContext().getResources().getString(R.string.player_btn_continue_play);
            case VideoController.SCENE_COMPLETION:
                return getContext().getResources().getString(R.string.player_btn_yes);
            case VideoController.SCENE_ERROR:
                return getContext().getResources().getString(R.string.player_btn_try);
        }
        return getContext().getResources().getString(R.string.player_btn_unknown);
    }

    /**
     * 返回提示文案
     * @param scene
     * @param tipsStr
     * @return
     */
    private String getTipsStr(int scene, String tipsStr) {
        if(!TextUtils.isEmpty(tipsStr)) return tipsStr;
        switch (scene) {
            case VideoController.SCENE_MOBILE:
                return getContext().getResources().getString(R.string.player_tips_mobile);
            case VideoController.SCENE_COMPLETION:
                return getContext().getResources().getString(R.string.player_tips_preview_finish);
            case VideoController.SCENE_ERROR:
                return getContext().getResources().getString(R.string.player_tips_play_error);
        }
        return getContext().getResources().getString(R.string.player_tips_unknown);
    }

    /**
     * 设置场景类型以改变交互样式来适应窗口模式
     * @param sceneType 0:默认的视频控制器场景 1:窗口模式场景
     */
    public void setSceneType(int sceneType) {
        int textSize13 = PlayerUtils.getInstance().dpToPxInt(13f);
        int textSize15 = PlayerUtils.getInstance().dpToPxInt(15f);
        TextView tips = (TextView) findViewById(R.id.player_status_tips);
        tips.setTextSize(TypedValue.COMPLEX_UNIT_PX,1==sceneType?textSize13:textSize15);

        int paddingLeft16 = PlayerUtils.getInstance().dpToPxInt(16f);
        int paddingLeft22 = PlayerUtils.getInstance().dpToPxInt(22f);
        TextView btn = (TextView) findViewById(R.id.player_status_btn);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,1==sceneType?textSize13:textSize15);
        btn.setPadding(1==sceneType?paddingLeft16:paddingLeft22,0,1==sceneType?paddingLeft16:paddingLeft22,0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
        layoutParams.height=PlayerUtils.getInstance().dpToPxInt(1==sceneType?26f:36f);
        layoutParams.setMargins(0,PlayerUtils.getInstance().dpToPxInt(1==sceneType?12f:18f),0,0);
        btn.setLayoutParams(layoutParams);
        PlayerUtils.getInstance().setOutlineProvider(btn,PlayerUtils.getInstance().dpToPxInt(1==sceneType?13f:18f));
    }

    public interface OnStatusListener{
        /**
         * 事件状态
         * @param event 1:移动网络播放提示 2:试看结束 3:播放失败
         */
        void onEvent(int event);
    }

    private OnStatusListener mOnStatusListener;

    public void setOnStatusListener(OnStatusListener onStatusListener) {
        mOnStatusListener = onStatusListener;
    }
}