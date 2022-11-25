package com.android.iplayer.interfaces;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import com.android.iplayer.base.BaseController;
import com.android.iplayer.listener.OnPlayerEventListener;
import java.io.File;

/**
 * created by hty
 * 2022/7/3
 * Desc:控制器持有的播放器代理人,也提供给宿主调用
 */
public interface IPlayerControl<V extends BaseController> {

    /**
     * 设置是否循环播放
     * @param loop 设置是否循环播放 true:循环播放 flase:禁止循环播放
     */
    void setLoop(boolean loop);

    /**
     * 设置播放进度回调间隔时间
     * @param callBackSpaceMilliss 设置播放进度回调间隔时间 单位：毫秒,数字越大性能越好,越小回调越频繁
     */
    void setProgressCallBackSpaceMilliss(int callBackSpaceMilliss);

    /**
     * 设置播放状态监听
     * @param listener 设置播放状态监听,如需自定义解码器,必须实现此监听
     */
    void setOnPlayerActionListener(OnPlayerEventListener listener);

    /**
     * 设置String类型播放地址
     * @param dataSource 设置String类型播放地址  网络地址:http://或https://,aw目录下地址:android.resource://" + getPackageName() + "/" + R.raw.xxx
     */
    void setDataSource(String dataSource);

    /**
     * 设置Assets类型的播放地址
     * @param dataSource 设置Assets类型的播放地址
     */
    void setDataSource(AssetFileDescriptor dataSource);

    /**
     * 设置本地File路劲的播放地址
     * @param dataSource 设置本地File路劲的播放地址,请注意先申请"存储"权限
     */
    void setDataSource(File dataSource);

    /**
     * 设置缩放模式
     * @param zoomModel 设置缩放模式 请适用IMediaPlayer类中定义的常量值
     */
    void setZoomModel(int zoomModel);

    /**
     * 是否开启重力旋转。当系统"自动旋转"开启+正在播放生效
     * @param enable 是否开启重力旋转。当系统"自动旋转"开启+正在播放生效
     */
    void setAutoChangeOrientation(boolean enable);

    /**
     * 设置视频旋转角度
     * @param degree 设置视频画面旋转角度
     */
    void setDegree(int degree);

    /**
     * 设置播放速度
     * @param speed 设置播放速度，仅在播放中设置生效。 从0.5f-2.0f
     */
    void setSpeed(float speed);

    /**
     * 设置左右声道音量，从0.0f-1.0f
     * @param leftVolume 设置左声道音量，1.0f-1.0f
     * @param rightVolume 设置右声道音量，1.0f-1.0f
     */
    void setVolume(float leftVolume, float rightVolume);

    /**
     * @param mute 设置是否静音,true:无声 false:跟随系统音量
     * @return 是否静音,true:无声 false:跟随系统音量
     */
    boolean setSoundMute(boolean mute);

    /**
     * 是否启用了静音
     * @return true:启用了静音 false:未启用静音
     */
    boolean isSoundMute();

    /**
     * 开启、关闭静音
     * @return 是否静音,true:静音 false:跟随系统音量
     */
    boolean toggleMute();

    /**
     * 设置画面镜像旋转
     * @param mirror 设置画面镜像旋转 true:画面翻转 false:正常
     * @return true:画面翻转 false:正常
     */
    boolean setMirror(boolean mirror);

    /**
     * 开启、关闭画面镜像旋转
     * @return 是否镜像,true:镜像音 false:正常
     */
    boolean toggleMirror();

    /**
     * @param restoreDirection 设置当播放器在横屏状态下收到播放完成事件时是否自动还原到竖屏状态,true:自动还原到竖屏 false:保留当前屏幕方向状态
     */
    void setPlayCompletionRestoreDirection(boolean restoreDirection);

    /**
     * @param landscapeWindowTranslucent 开始全屏前设置生效，设置当播放器在开启横屏状态下播放时是否启用全屏沉浸样式，true:启用沉浸式全屏 false:保留状态栏及菜单栏位置(隐藏状态栏及菜单栏图标及按钮)，使用标准的全屏样式
     */
    void setLandscapeWindowTranslucent(boolean landscapeWindowTranslucent);

    /**
     * 设置播放器在移动网络能否继续工作
     * @param mobileNetwork 设置播放器在移动网络能否继续工作 true:允许工作 flase:禁止
     */
    void setMobileNetwork(boolean mobileNetwork);

    /**
     * 设置是否监听并处理音频焦点事件
     * @param interceptTAudioFocus 设置是否监听并处理音频焦点事件 true:拦截，并在收到音频焦点失去后暂停播放 false:什么也不处理
     */
    void setInterceptTAudioFocus(boolean interceptTAudioFocus);

    /**
     * 设置当播放器遇到链接视频文件失败时自动重试的次数，内部自动重试次数为3次
     * @param reCatenationCount 设置当播放器遇到链接视频文件失败时自动重试的次数，内部自动重试次数为3次
     */
    void setReCatenationCount(int reCatenationCount);

    /**
     * 驾驶异步准备播放
     */
    void prepareAsync();

    void startPlay();

    void play();

    void pause();

    void rePlay();

    /**
     * 开始\暂停播放
     */
    void togglePlay();

    /**
     * 开始播放\暂停
     */
    void playOrPause();

    /**
     * @param dataSource 传入播放地址 开始播放\暂停
     * 在开始播放之前可调用IWindowManager.getInstance().quitGlobaWindow();结束并退出悬浮窗窗口播放
     */
    void playOrPause(Object dataSource);

    /**
     * 开启全屏模式播放
     */
    void startFullScreen();

    /**
     * @param bgColor 开启全屏模式播放:横屏时播放器的背景颜色,内部默认用黑色#000000
     */
    void startFullScreen(int bgColor);

    /**
     * 退出全屏播放
     */
    void quitFullScreen();

    /**
     *
     * 开启\退出全屏波发放
     */
    void toggleFullScreen();

    /**
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     */
    void startWindow();

    /**
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     */
    void startWindow(boolean isAutoSorption);

    /**
     * @param radius 窗口的圆角 单位:像素
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param bgColor 窗口的背景颜色
     */
    void startWindow(float radius,int bgColor);

    /**
     * @param radius 窗口的圆角 单位:像素
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     */
    void startWindow(float radius,int bgColor,boolean isAutoSorption);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部15dp,右边15dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     */
    void startWindow(int width,int height,float startX,float startY);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     */
    void startWindow(int width,int height,float startX,float startY,boolean isAutoSorption);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     */
    void startWindow(int width,int height,float startX,float startY,float radius);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     */
    void startWindow(int width,int height,float startX,float startY,float radius,boolean isAutoSorption);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     */
    void startWindow(int width,int height,float startX,float startY,float radius,int bgColor);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置)
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     */
    void startWindow(int width,int height,float startX,float startY,float radius,int bgColor,boolean isAutoSorption);

    /**
     * 关闭窗口播放
     */
    void quitWindow();

    /**
     * 开启\关闭可拖拽的窗口播放
     */
    void toggleWindow();

    /**
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow();

    /**
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */

    boolean startGlobalWindow(boolean isAutoSorption);

    /**
     * @param radius 窗口的圆角 单位:像素
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param bgColor 窗口的背景颜色
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(float radius,int bgColor);

    /**
     * @param radius 窗口的圆角 单位:像素
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(float radius,int bgColor,boolean isAutoSorption);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(int width,int height,float startX,float startY);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(int width,int height,float startX,float startY,boolean isAutoSorption);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param radius 窗口的圆角 单位:像素
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(int width,int height,float startX,float startY,float radius);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param radius 窗口的圆角 单位:像素
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(int width,int height,float startX,float startY,float radius,boolean isAutoSorption);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(int width,int height,float startX,float startY,float radius,int bgColor);

    /**
     * @param width 窗口播放器的宽,当小于=0时用默认
     * 开启可拖拽的全局悬浮窗口播放
     * 默认宽为屏幕1/2+30dp,高为1/2+30dp的16:9比例,X起始位置为:播放器原宿主的右下方,距离原宿主View顶部12dp,右边12dp(如果原宿主不存在,则位于屏幕右上角距离顶部60dp位置),
     * 全局悬浮窗口和局部小窗口不能同时开启
     * 横屏下不允许开启,需要在取得悬浮窗权限之后再调用
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     * @param height 窗口播放器的高,当小于=0时用默认
     * @param startX 窗口位于屏幕中的X轴起始位置,当小于=0时用默认
     * @param startY 窗口位于屏幕中的Y轴起始位置,当小于=0时用默认
     * @param radius 窗口的圆角 单位:像素
     * @param bgColor 窗口的背景颜色
     * @param isAutoSorption 触摸松手后是否自动吸附到屏幕边缘(悬停时距离屏幕边缘12dp),true:自动吸附,false:保持在床后的最后位置状态
     * @return true:开启悬浮窗成功 false:开启悬浮窗失败
     */
    boolean startGlobalWindow(int width,int height,float startX,float startY,float radius,int bgColor,boolean isAutoSorption);

    /**
     * 关闭全局悬浮窗口播放
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     */
    void quitGlobaWindow();

    /**
     * 开启\关闭可拖拽的全局悬浮窗口播放
     * 需要声明权限：
     *     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     *     <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
     */
    void toggleGlobaWindow();

    /**
     * 告诉播放器进入了画中画模式
     */
    void enterPipWindow();

    /**
     * 告诉播放器退出了画中画模式
     */
    void quitPipWindow();

    /**
     * 试看模式下的播放完成
     */
    void onCompletion();

    /**
     * @param continuityPlay 设置是否连续播放模式(需要在视频播放完成结束前调用),true:连续播放模式开启 false:关闭连续播放模式,播放器内部在收到continuityPlay为true的时候,不会自动退出全屏\小窗口\悬浮窗口等模式
     */
    void setContinuityPlay(boolean continuityPlay);

    /**
     * @return 返回视频分辨率-宽，单位：像素
     */
    int getVideoWidth();

    /**
     * @return 返回视频分辨率-高，单位：像素
     */
    int getVideoHeight();

    /**
     * @return 返回视频文件总时长,单位：毫秒
     */

    long getDuration();

    /**
     * @return 返回正在播放的位置,单位：毫秒
     */
    long getCurrentPosition();

    /**
     * @return 返回缓冲进度，单位：百分比
     */
    int getBuffer();

    /**
     * @param prepareTimeout 设置准备和读数据超时阈值,需在{@link #prepareAsync()}之前调用方可生效 准备超时阈值,即播放器在建立链接、解析流媒体信息的超时阈值
     * @param readTimeout    读数据超时阈值
     */
    void setTimeout(int prepareTimeout, int readTimeout);

    /**
     * 快进\快退
     * @param msec 毫秒进度条
     */
    void seekTo(long msec);

    /**
     * @param msec 快进\快退 毫秒进度条
     * @param accurate 是否精准快进快退
     */
    void seekTo(long msec,boolean accurate);

    /**
     * @return 播放器是否正处于工作状态(准备\开始播放\缓冲\手动暂停\生命周期暂停) true:工作中 false:空闲状态
     */
    boolean isWorking();

    /**
     * @return 是否正处于播放中(准备\开始播放\播放中\缓冲\) true:播放中 false:不处于播放中状态
     */
    boolean isPlaying();

    /**
     * @param context 当播放器开启转场、全局悬浮窗功能时,在业务层面设置一个当前的上下文,方便内部处理全屏、屏幕亮度调节逻辑
     */
    void setParentContext(Context context);

    /**
     * @return 返回临时的上下文
     */
    Context getParentContext();

    /**
     * @param videoController 设置视图控制器 继承VideoBaseController的控制器
     */
    void setController(V videoController);

    /**
     * @return 返回播放器控制器
     */
    BaseController getController();

    /**
     * @return 是否允许返回(横屏时先退出横屏,小窗口模式下先退出小窗口模式)
     */
    boolean isBackPressed();

    /**
     * 尝试恢复播放
     */
    void onResume();

    /**
     * 尝试暂停播放
     */
    void onPause();

    /**
     * 结束播放,鉴于停止播放比较耗时，在子线程中操作
     */
    void onStop();

    /**
     * 恢复播放器内部状态
     */
    void onRecover();

    /**
     * 还原播放器及controller内部所有状态
     */
    void onReset();

    /**
     * 销毁播放器
     */
    void onRelease();

    /**
     * 销毁播放器
     */
    void onDestroy();
}