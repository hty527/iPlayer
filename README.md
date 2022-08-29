# **iPlayer**

### 免责声明：
此项目中视频列表数据使用了开眼API，纯属Demo演示，无任何商业用途，禁止任何人将示例项目中的第三方API应用于商业用途，如果使用了，连带的法律责任与本作者无关!

### 项目介绍：
一个封装基础视频播放器功能交互的SDK。

### 强烈推荐集成前先下载体验Apk，找到对应的功能后再开始集成：
[Demo apk下载]: https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/apk/iPlayer-2.0.0.apk?version=2.0.0 "Download"
**[Demo apk下载]**

### 历史版本
[查看历史版本]: https://github.com/hty527/iPlayer/wiki/Version "历史版本"
**[查看历史版本]**

### SDK功能支持:
* 支持网络地址、直播流、本地Assets和Raw资源文件播放</br>
* 支持播放倍速、缩放模式、静音、镜像等功能设置</br>
* 支持多播放器同时播放</br>
* 支持自定义视频解码器、自定义控制器、自定义UI交互组件</br>
* SDK自带默认解码器+控制器+UI交互组件(任意局部UI组件也支持自定义)、支持片段试看交互</br>
* 支持任意界面开启无权限Activity级别可拖拽小窗口播放</br>
* 支持任意界面开启可拖拽全局悬浮窗窗口播放</br>
* 支持任意界面直接启动全屏播放</br>
* 支持连续播放视频列表</br>
* Demo列表或组件之间无缝转场播放示例</br>
* Demo悬浮窗窗口播放带参无缝跳转Activity示例</br>
* Demo支持MediaPlayer、IjkPlayer、ExoPlayer三种解码器切换示例</br>
* Demo仿抖音播放示例，支持视频缓存、秒播</br>
* Demo弹幕交互示例</br>
* Demo Android8.0+画中画示例</br>
***
### Android SDK集成：
```
    集成前请下载体验Demo,复杂场景集成请参考Demo
```
#### 一、SDK配置及基础功能:
* 1.项目根build.gradle及模块build.gradle配置</br>
```
    //1.在你的根build.gradle中添加：
    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }

    //2.在你的模块build.gradle中添加：   
    dependencies {
        implementation 'androidx.appcompat:appcompat:1.2.0' // 或 implementation 'com.android.support:appcompat-v7:+'

        //播放器SDK
        implementation 'com.github.hty527:iPlayer:2.0.0.4'
    }
```
* 2.在需要播放视频的xml中添加如下代码,或在适合的位置new VideoPlayer()</br>
```
    <com.android.iplayer.widget.VideoPlayer
        android:id="@+id/video_player"
        android:layout_width="match_parent"
        android:layout_height="200dp"/>
```
* 3.播放器准备及开始播放</br>
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
* 4.生命周期处理</br>
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
### 自定义解码器、自定义UI交互组件、悬浮窗口播放等功能请阅读：**[接入文档]**
[接入文档]: https://github.com/hty527/iPlayer/wiki/api "接入文档"

### SDK及Demo部分功能快照：
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot1.jpg?q=1" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot2.jpg?q=2" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot3.jpg?q=3" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot4.jpg?q=4" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot5.jpg?q=5" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot6.jpg?q=6" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot7.jpg?q=7" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/shot8.jpg?q=8" width="270",height="585">
</div>