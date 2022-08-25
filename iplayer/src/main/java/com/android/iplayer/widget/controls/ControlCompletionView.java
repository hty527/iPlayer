package com.android.iplayer.widget.controls;

import android.content.Context;
import android.view.View;
import com.android.iplayer.R;
import com.android.iplayer.base.BaseControllerWidget;
import com.android.iplayer.model.PlayerState;

/**
 * created by hty
 * 2022/8/22
 * Desc:UI控制器-播放完成
 */
public class ControlCompletionView extends BaseControllerWidget {

    public ControlCompletionView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_control_completion;
    }

    @Override
    public void initViews() {
        hide();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=mControlWrapper) mControlWrapper.togglePlay();
            }
        });
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        switch (state) {
            case STATE_COMPLETION://播放结束
                if(!isWindowScene()&&!isPreViewScene()){//窗口播放模式/试看模式不显示
                    show();
                }
                break;
            default:
                hide();
        }
    }

    @Override
    public void onOrientation(int direction) {}
}