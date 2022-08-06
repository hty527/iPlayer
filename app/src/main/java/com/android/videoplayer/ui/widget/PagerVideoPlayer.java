package com.android.videoplayer.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.listener.OnPlayerEventListener;
import com.android.videoplayer.media.JkMediaPlayer;
import com.android.videoplayer.pager.controller.FullScreenController;

/**
 * created by hty
 * 2022/7/1
 * Desc:Pager片段播放器
 */
public class PagerVideoPlayer extends BasePlayer {

    public PagerVideoPlayer(@NonNull Context context) {
        this(context,null);
    }

    public PagerVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PagerVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initViews() {
        //播放器事件监听
        setController(new FullScreenController(getContext()));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //暂停、开始
                playOrPause();
            }
        });
        setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return new JkMediaPlayer(getContext());
            }
        });
        setLoop(true);
        setProgressCallBackSpaceMilliss(300);
    }
}
