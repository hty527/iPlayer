# **iPlayer**

#### 一、SDK基础功能:
* 支持网络地址、直播流、本地Assets和Raw资源文件播放</br>
* 支持播放倍速、缩放模式、静音、镜像等功能设置</br>
* 支持自定义视频解码器、控制器、UI交互组件、视频画面渲染器</br>
* 支持多播放器同时播放</br>
* SDK默认控制器(局部UI交互可自定义)支持手势控制、付费试看等交互</br>
* 支持任意位置启动Activity级别悬浮窗口播放</br>
* 支持任意位置启动全局悬浮窗口播放</br>
* 支持任意位置直接启动全屏播放</br>
* 支持连续播放视频列表</br>
* Demo：列表或组件之间无缝转场播放</br>
* Demo：全局悬浮窗播放转场跳转Activity</br>
* Demo：MediaPlayer、IjkPlayer、ExoPlayer三种解码器切换</br>
* Demo：仿抖音，支持视频缓存、秒播</br>
* Demo：弹幕交互</br>
* Demo：Android8.0+画中画示例</br>

#### 二、[历史版本][1]
[1]:https://github.com/hty527/iPlayer/wiki/Version "历史版本"

#### 三、SDK集成
* 建议集成前先[下载apk][2]体验，找到自己想要实现的功能模块，后续集成可参考demo源码。

[2]:https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/apk/iPlayer-2.0.2.apk?version=2.0.2 "下载apk"

##### 1、项目根build.gradle及模块build.gradle配置</br>
```
    //1.在你的根build.gradle中添加：
    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }

    //2.在你的模块build.gradle中添加：   
    dependencies {
        //播放器
        implementation 'com.github.hty527.iPlayer:iplayer:2.0.2.1'
            
        //可选，音视频解码器    
        //implementation 'com.github.hty527.iPlayer:ijk:2.0.2.1'
        //可选，音视频缓存
        //implementation 'com.github.hty527.iPlayer:videocache:2.0.2.1'
    }
```
##### 2、在需要播放视频的xml中添加如下代码,或在适合的位置new VideoPlayer()</br>
```
    <com.android.iplayer.widget.VideoPlayer
        android:id="@+id/video_player"
        android:layout_width="match_parent"
        android:layout_height="200dp"/>
```
##### 3、播放器准备及开始播放</br>
```
    mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
    mVideoPlayer.getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//固定播放器高度，或高度设置为:match_parent
    //使用SDK自带控制器+各UI交互组件
    VideoController controller = mVideoPlayer.initController();
    //设置视频标题(仅横屏状态可见)
    controller.setTitle("测试地址播放");
    //设置播放源
    mVideoPlayer.setDataSource("https://upload.dongfeng-nissan.com.cn/nissan/video/202204/4cfde6f0-bf80-11ec-95c3-214c38efbbc8.mp4");
    //异步开始准备播放
    mVideoPlayer.prepareAsync();
```
##### 4、生命周期处理</br>
```
    @Override
    protected void onResume() {
        super.onResume();
        mVideoPlayer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoPlayer.onPause();
    }

    @Override
    public void onBackPressed() {
        if(mVideoPlayer.isBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayer.onDestroy();
    }
```
##### 5、自定义解码器、UI交互组件和悬浮窗口播放等功能请阅读[wiki][3]</br>
[3]:https://github.com/hty527/iPlayer/wiki/api "wiki"
#### 四、遇到问题
* 1、阅读接入文档[wiki][3]
* 2、提交 [issue](https://github.com/hty527/iPlayer/issues)
* 3、联系作者：584312311@qq.com
* 4、项目无法下载或下载缓慢请至[码云][4]下载
* 5、播放器框架结构图[点击][5]查看

[4]:https://gitee.com/hty_Yuye/iPlayer "码云"
[5]:https://www.jianshu.com/p/39d8f824c2fb "点击"
#### 五、SDK及Demo部分功能预览
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen1.jpg?q=1" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen2.jpg?q=2" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen3.jpg?q=3" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen4.jpg?q=4" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen5.jpg?q=5" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen6.jpg?q=6" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen7.jpg?q=7" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/screen8.jpg?q=8" width="270",height="585">
</div>