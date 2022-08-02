# **iPlayer**

### 免责声明：
此项目中视频列表数据使用了开眼API，纯属Demo演示，无任何商业用途，禁止任何人将示例项目中的第三方API应用于商业用途，如果使用了，连带的法律责任与本作者无关!

### 项目介绍：
一个封装基础视频播放器功能交互的SDK。

### Apk下载：
[Demo apk下载]: https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/apk/iPlayer-1.0.0.apk "Download"
**[Demo apk下载]**

### 功能支持:
* 基础的网络地址视频播放、本地Raw和Assets资源视频播放</br>
* 直播拉流</br>
* 内置基础UI控制器交互、横屏、静音、画面镜像、缩放模式等设置</br>
* 支持自定义播放器解码器内核</br>
* 多播放器同时播放</br>
* 支持完全自定义UI控制器</br>
* 无权限Activity窗口播放</br>
* 全局悬浮窗窗口播放,Demo内置悬浮窗和Activity无缝转场示例<br/>
* 支持列表播放转场到落地页无缝衔接播放</br>
* Android 8.0画中画</br>
* Demo内置仿抖音滚动播放示例(支持秒播)</br>
* Demo内置列表滑动自动播放示例</br>
* Demo内置自定义弹幕交互控制器示例</br>
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
        implementation 'com.android.support:appcompat-v7:+'
        //或androidX环境：implementation 'androidx.appcompat:appcompat:1.0.2'
        implementation 'com.github.hty527:iPlayer:1.0.0'//播放器SDK
    }
```
* 2.在需要播放视频的xml中添加如下代码</br>
```
    <com.android.player.media.VideoPlayer
        android:id="@+id/video_player"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        app:initController="true"/>
```
* 3.播放器准备工作</br>
```
    mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);//或者直接 new 一个VideoPlayer(context);对象
    mVideoPlayer.getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//固定播放器高度
    //监听播放器状态(如果需要自定义解码器必须设置此监听,在createMediaPlayer中初始化自己的解码器)
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {

        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            return null;//返回null时,SDK内部会自动使用系统MediaPlayer解码器,自定义解码器请参考Demo中的JkMediaPlayer类
        }

        @Override
        public void onPlayerState(PlayerState state, String message) {
            //播放器内部工作状态
        }
        //更多回调请查阅OnPlayerEventListener
    });
```
* 4.在需要播放的地方</br>
```
    mVideoPlayer.setTitle("测试地址播放");//视频标题(默认控制器横屏状态下可见)
    mVideoPlayer.setDataSource("https://upload.dongfeng-nissan.com.cn/nissan/video/202204/4cfde6f0-bf80-11ec-95c3-214c38efbbc8.mp4");//设置播放源
    mVideoPlayer.prepareAsync();//异步开始准备播放
```
* 5.生命周期处理</br>
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
* 6.清单文件配置：如果播放器需要横屏播放,需要在Activity清单文件配置如下属性：</br>
```
    //在需要全屏播放的Activity清单文件中添加属性：android:configChanges="orientation|screenSize"，防止横屏时Activity重绘
    <activity
        android:name="xxx.xxx.PlayerActivity"
        android:launchMode="singleTask"
        android:screenOrientation="portrait"
        android:configChanges="orientation|screenSize">
```
* 7.更多基础设置</br>
```
    mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);//居中显示,定宽等高 (更多缩放模式请参考IMediaPlayer设置)
    mVideoPlayer.setLoop(false);//是否循环播放
    mVideoPlayer.setProgressCallBackSpaceMilliss(300);//设置进度条回调间隔时间(毫秒)
    //..更多设置或功能API请参阅IVideoPlayerControl

    //在声明播放器添加了属性app:initController="true 或mVideoPlayer.initController();之后
    BaseController controller = mVideoPlayer.getController();
    if(null!=controller){
        controller.showBackBtn(false);//竖屏状态下是否显示返回按钮
        controller.showMenus(false,true,true);//竖屏状态是否显示控制器右上角的功能菜单按钮,默认是显示的(横屏不显示所有菜单按钮)
        //监听控制器交互事件(设置开启了返回showBackBtn\菜单栏showMenus等功能后可设置监听,回调方法请参阅：OnControllerEventListener)
        controller.setOnControllerListener(new BaseController.OnControllerEventListener(){

            @Override
            public void onMenu() {
                //菜单点击
            }

            @Override
            public void onGobalWindow() {
                //可参考WindowGlobalPlayerActivity
                boolean globalWindow = mVideoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));//支持多参,这里给悬浮窗设置一个圆角和背景颜色
                if(globalWindow){
                    finish();
                }
            }
        });
    }
```
* 8.其它播放器全局设置</br>
```
    //设置播放器是否拦截音频焦点丢失事件,如果设置了检测到音频焦点丢失会自动暂停播放
    IVideoManager.getInstance().setInterceptTAudioFocus(true);
    //设置移动网络下是否允许播放
    IVideoManager.getInstance().setMobileNetwork(true);
    
    //更多API文档使用，请参阅目录文件/iPlayer/doc/docs/javadoc/index.html
```
#### 二、权限:
* 1.请根据需要在您的AndroidManifest中声明权限</br>
```
    <!--播放网络视频需要声明此权限-->
    <uses-permission android:name="android.permission.INTERNET" />

    <!--如您的播放器需要支持移动网路提示用户的交互,需要申明此权限,SDK用来检测网络状态-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!--如您的播放器需要支持全局悬浮窗窗口播放请申明此权限-->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
#### 三、高级功能:
* 1.无权限悬浮窗播放(Activity级别)</br>
```
    //开启小窗播放,此方法支持多参传入,请根据多参参数选择您的实际需求
    mVideoPlayer.startWindow(ScreenUtils.getInstance().dpToPxInt(3f), Color.parseColor("#99000000"));//参数1:给窗口一个圆角,参数2:给窗口一个背景颜色
```
* 2.全局悬浮窗窗口播放</br>
```
    //1.开启全局悬浮窗窗口播放,请根据多参参数选择您的实际需求,SDK内部会检测悬浮窗窗口权限
    boolean globalWindow = mVideoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));//参数1:给窗口一个圆角,参数2:给窗口一个背景颜色

```
* 3.处理全局悬浮窗播放器的点击\跳转\参数传递
```
    //1.在你的Application中设置全局悬浮窗窗口点击监听
    IWindowManager.getInstance().setOnWindowActionListener(new OnWindowActionListener() {

        @Override
        public void onMovie(float x, float y) {

        }

        @Override
        public void onClick(BasePlayer basePlayer, Object coustomParams) {
            //点击了悬浮窗口播放器,coustomParams为自定义参数
        }

        @Override
        public void onClose() {
            //点击了悬浮窗口播放器的关闭按钮
            IWindowManager.getInstance().quitGlobaWindow();//关闭悬浮窗播放器窗口
        }
    });

    //2.在开启悬浮窗播放后用户点击悬浮窗之前,设置自定义参数
    IWindowManager.getInstance().setCoustomParams(coustomParams);

    //3.如何完美实现悬浮窗到Activity转场播放无感无缝衔接？
    参考VideoDetailsActivity中的代码示例
```

* 4.list列表播放转场无缝衔接播放</br>
```
    参考ListPlayerChangeFragment中的示例代码
```
* 5.类似抖音功能,支持秒播</br>
```
    参考PagerPlayerFragment中的示例代码
```
### SDK及Demo快照：
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/1.jpg" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/2.jpg" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/3.jpg" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/4.jpg" width="270",height="585">
</div>
<div align="center">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/5.jpg" width="270",height="585">
    <img src="https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/6.jpg" width="270",height="585">
</div>