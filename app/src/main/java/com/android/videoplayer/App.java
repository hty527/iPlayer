package com.android.videoplayer;

import android.content.Context;
import android.content.Intent;
import androidx.multidex.MultiDexApplication;
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
public class App extends MultiDexApplication {

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
        /**
         * SDK内部会在使用缓存相关功能时自动初始化。如果需要自行定义缓存目录、缓存目录最大长度大小可自行调用初始化。必须在使用缓存功能之前初始化
         */
        //返回的路径是SD卡包名下的内部缓存路径，无需存储权限。位于/storage/emulated/0/Android/data/包名/files/video/cache下，会随着应用卸载被删除
        //其它路径请注意申请动态权限！！！
//        File cachePath = getExternalFilesDir("video/cache/");
        //参数2：缓存大小(单位：字节),参数3：缓存路径,不设置默认在sd_card/Android/data/[app_package_name]/cache中
//        VideoCache.getInstance().initCache(getApplicationContext(),1024*1024*1024,cachePath);//缓存大小为1024M，路径为SD卡下的cachePath。请注意SD卡权限状态。
    }

    public static App getInstance(){
        return mInstance;
    }

    public Context getContext(){
        return mInstance.getApplicationContext();
    }
}