package com.android.iplayer.widget.controls;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.iplayer.R;
import com.android.iplayer.base.BaseControllerWidget;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.model.PlayerState;
import com.android.iplayer.utils.PlayerUtils;

/**
 * created by hty
 * 2022/6/30
 * Desc:播放器试看结束\移动网络提示\播放失败提示
 */
public class ControlStatusView extends BaseControllerWidget {

    public static final int SCENE_MOBILE                = 1;//移动网络播放提示
    public static final int SCENE_COMPLETION            = 2;//试看结束
    public static final int SCENE_ERROR                 = 3;//播放失败

    private int mScene;

    public ControlStatusView(Context context) {
        super(context,null);
    }

    public ControlStatusView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public ControlStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_control_status;
    }

    @Override
    public void initViews() {
        hide();
        View btnContinue = findViewById(R.id.player_status_btn);
        PlayerUtils.getInstance().setOutlineProvider(findViewById(R.id.player_status_btn),PlayerUtils.getInstance().dpToPxInt(18f));
        btnContinue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击事件优先回调给监听器处理
                if(null!=mOnStatusListener){
                    mOnStatusListener.onEvent(mScene);
                    return;
                }
                if(null!=mControlWrapper){
                    switch (mScene) {
                        case SCENE_MOBILE:
                            IVideoManager.getInstance().setMobileNetwork(true);
                            mControlWrapper.togglePlay();
                            break;
                        case SCENE_COMPLETION:
                            mControlWrapper.onCompletion();
                            break;
                        case SCENE_ERROR:
                            mControlWrapper.togglePlay();
                            break;
                    }
                }
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
            case SCENE_MOBILE:
                return getContext().getResources().getString(R.string.player_btn_continue_play);
            case SCENE_COMPLETION:
                return getContext().getResources().getString(R.string.player_btn_yes);
            case SCENE_ERROR:
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
            case SCENE_MOBILE:
                return getContext().getResources().getString(R.string.player_tips_mobile);
            case SCENE_COMPLETION:
                return getContext().getResources().getString(R.string.player_tips_preview_finish);
            case SCENE_ERROR:
                return getContext().getResources().getString(R.string.player_tips_play_error);
        }
        return getContext().getResources().getString(R.string.player_tips_unknown);
    }

    /**
     * 设置场景类型以改变交互样式来适应窗口模式
     * @param sceneType 0:默认的视频控制器场景 1:窗口模式场景
     */
    public void setSceneType(int sceneType) {
        int textSize14 = PlayerUtils.getInstance().dpToPxInt(14f);
        int textSize13 = PlayerUtils.getInstance().dpToPxInt(13f);
        int textSize16 = PlayerUtils.getInstance().dpToPxInt(16f);
        TextView tips = (TextView) findViewById(R.id.player_status_tips);
        tips.setTextSize(TypedValue.COMPLEX_UNIT_PX,1==sceneType?textSize14:textSize16);

        int paddingLeft12 = PlayerUtils.getInstance().dpToPxInt(12f);
        int paddingLeft22 = PlayerUtils.getInstance().dpToPxInt(22f);
        TextView btn = (TextView) findViewById(R.id.player_status_btn);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,1==sceneType?textSize13:textSize16);
        btn.setPadding(1==sceneType?paddingLeft12:paddingLeft22,0,1==sceneType?paddingLeft12:paddingLeft22,0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
        layoutParams.height=PlayerUtils.getInstance().dpToPxInt(1==sceneType?26f:36f);
        layoutParams.setMargins(0,PlayerUtils.getInstance().dpToPxInt(1==sceneType?15f:20f),0,0);
        btn.setLayoutParams(layoutParams);
        PlayerUtils.getInstance().setOutlineProvider(btn,PlayerUtils.getInstance().dpToPxInt(1==sceneType?13f:18f));
    }

    @Override
    public void show() {
        if(getVisibility()!=View.VISIBLE){
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hide() {
        if(getVisibility()!=View.GONE){
            setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        switch (state) {
            case STATE_COMPLETION://播放结束
                if(isPreViewScene()){//预览场景
                    show();
                    setScene(SCENE_COMPLETION);
                }else{
                    hide();
                }
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                show();
                setScene(SCENE_MOBILE);
                break;
            case STATE_ERROR://播放失败
                setScene(SCENE_ERROR,message);
                show();
                break;
            default:
                hide();
        }
    }

    @Override
    public void onOrientation(int direction) {}

    /**
     * 适配常规场景和窗口场景样式
     * @param playerScene
     */
    @Override
    public void onPlayerScene(int playerScene) {
//        if(IControllerView.SCENE_GLOBAL_WINDOW==scene||IControllerView.SCENE_WINDOW==scene){
//            setSceneType(1);
//        }else{
//            setSceneType(0);
//        }
    }

    public interface OnStatusListener{
        void onEvent(int event);
    }

    private OnStatusListener mOnStatusListener;

    public void setOnStatusListener(OnStatusListener onStatusListener) {
        mOnStatusListener = onStatusListener;
    }
}