package com.android.videoplayer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.android.iplayer.base.BasePlayer;
import com.android.iplayer.listener.OnWindowActionListener;
import com.android.iplayer.manager.IVideoManager;
import com.android.iplayer.manager.IWindowManager;
import com.android.iplayer.utils.ILogger;
import com.android.videoplayer.bean.Params;
import com.android.videoplayer.ui.activity.WindowGlobalPlayerActivity;
import com.android.videoplayer.utils.Logger;
import com.android.videoplayer.utils.SharedPreferencesUtil;
import com.android.videoplayer.video.ui.activity.VideoDetailsActivity;

/**
 * created by hty
 * 2022/7/1
 * Desc:
 */
public class App extends Application {

    private static final String TAG ="App";
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        SharedPreferencesUtil.init(this, getPackageName() + ".sp", Context.MODE_MULTI_PROCESS);
        ILogger.DEBUG=true;
        //设置播放器是否拦截音频焦点丢失事件,如果设置了检测到音频焦点丢失会自动暂停播放
        IVideoManager.getInstance().setInterceptTAudioFocus(true);

        /**
         * 1.想自己实现点击全局悬浮窗窗口播放器后跳转逻辑？
         * 在这里实现监听，并在onClick中处理自己的跳转事件
         * 2.点击全局悬浮窗窗口播放器跳转需要参数？
         * 在收到onClick点击事件之,调用IWindowManager.getInstance().setCoustomParams("我是自定义参数");传入自定义参数,最终会回调到onClick(BasePlayer basePlayer, Object coustomParams)中
         */
        IWindowManager.getInstance().setOnWindowActionListener(new OnWindowActionListener() {
            @Override
            public void onMovie(float x, float y) {

            }

            //点击了悬浮窗口播放器
            @Override
            public void onClick(BasePlayer basePlayer, Object coustomParams) {
                Logger.d(TAG,"onClick-->coustomParams:"+coustomParams);
                //如果参数为空则跳转到示例界面
                if(null==coustomParams){
                    Intent intent=new Intent(getContext(), WindowGlobalPlayerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("title","接收全局悬浮窗播放器");//是否接收并继续播放悬浮窗口的视频
                    intent.putExtra("is_global","1");//是否接收并继续播放悬浮窗口的视频
                    intent.putExtra("extra", (String) coustomParams);//示例GlobalWindowPlayerActivity中传入的是字符串,所以这里可以强转
                    getContext().startActivity(intent);
                //不为空跳转到视频落地页
                }else{
                    if(coustomParams instanceof Params){
                        Params params= (Params) coustomParams;
                        Intent intent=new Intent(getContext(), VideoDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("is_global_window",true);//是否时接收全局悬浮窗窗口播放器播放
                        intent.putExtra("params",params.getParamsJson());
                        intent.putExtra("list_json",params.getListJson());
                        getContext().startActivity(intent);
                    }
                }
            }

            //点击了悬浮窗口播放器的关闭按钮
            @Override
            public void onClose() {
                Logger.d(TAG,"onClose-->");
                IWindowManager.getInstance().quitGlobaWindow();//关闭悬浮窗播放器窗口
            }
        });
    }


    public static App getInstance(){
        return mInstance;
    }

    public Context getContext(){
        return mInstance.getApplicationContext();
    }
}