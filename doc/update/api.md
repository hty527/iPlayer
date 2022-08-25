#### 常用功能使用说明
##### 播放器的所有api
* 请阅读IVideoPlayerControl
##### 自定义Controller及UI交互组件
###### 自定义Controller
* 所有自定义Controller需继承BaseController，若您的Controll需要支持手势交互，请继承GestureController，GestureController内部处理了手势事件。
* 自定义Controller的使用
```
        VideoController controller=new VideoController(videoPlayer.getContext());
        BasePlayer.setController(controller);
        //或使用SDK内置的
        BasePlayer.initController();
```
###### 自定义UI交互组件
* SDK提供了一套标题栏、底部控制栏、播放器状态(网络提示、播放失败)、播放完成、手势交互相应处理、Window窗口、列表模式 等UI交互组件。Controller的任意UI交互组件均支持自定义。
如需自定义请继承BaseControllerWidget，请阅读IControllerView提供的API来实现自己的交互。
* 自定义UI交互组件的使用(请参考Demo中的VideoPlayerActivity类)
```
        /**
         * 1、给播放器设置一个控制器
         */
        mController = new VideoController(mVideoPlayer.getContext());
        //mController.setCanTouchInPortrait(false);//竖屏状态下是否开启手势交互,内部默认允许
        //mController.showLocker(true);//横屏状态下是否启用屏幕锁功能,默认开启
        mVideoPlayer.setController(mController);//绑定控制器到播放器

        /**
         * 2、给控制器添加各UI交互组件
         */
        //给播放器控制器绑定自定义UI交互组件，也可调用initControlComponents()一键使用SDK内部提供的所有UI交互组件
        ControlToolBarView toolBarView=new ControlToolBarView(this);//标题栏，返回按钮、视频标题、功能按钮、系统时间、电池电量等组件
        toolBarView.setTarget(IVideoController.TARGET_CONTROL_TOOL);
        toolBarView.showBack(false);//是否显示返回按钮,仅限竖屏情况下，横屏模式下强制显示
        toolBarView.showMenus(true,true,true);//是否显示投屏\悬浮窗\功能等按钮，仅限竖屏情况下，横屏模式下强制不显示
        //监听标题栏的功能事件
        toolBarView.setOnToolBarActionListener(new ControlToolBarView.OnToolBarActionListener() {
            @Override
            public void onBack() {//仅当设置showBack(true)后并且竖屏情况下才会有回调到此
                Logger.d(TAG,"onBack");
                onBackPressed();
            }

            @Override
            public void onTv() {
                Logger.d(TAG,"onTv");
            }

            @Override
            public void onWindow() {
                Logger.d(TAG,"onWindow");
                startGoableWindow(null);
            }

            @Override
            public void onMenu() {
                Logger.d(TAG,"onMenu");
                showMenuDialog();
            }
        });
        ControlFunctionBarView functionBarView=new ControlFunctionBarView(this);//底部时间、seek、静音、全屏功能栏
        functionBarView.showSoundMute(true,false);//启用静音功能交互\默认不静音
        ControlGestureView gestureView=new ControlGestureView(this);//手势控制屏幕亮度、系统音量、快进、快退UI交互
        ControlCompletionView completionView=new ControlCompletionView(this);//播放完成、重试
        ControlStatusView statusView=new ControlStatusView(this);//移动网络播放提示、播放失败、试看完成
        ControlLoadingView loadingView=new ControlLoadingView(this);//加载中、开始播放
        ControWindowView windowView=new ControWindowView(this);//悬浮窗窗口播放器的窗口样式
        mController.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
```

##### 全屏播放

###### 横竖屏切换
* 支持横竖屏切换播放，需在AndroidManifest中所在的Activity申明如下属性：
```
        android:configChanges="orientation|screenSize"
```
###### 直接启动全屏播放
* SDK支持在任意位置直接启动全屏播放,只需要：
```
        VideoPlayer videoPlayer = new VideoPlayer(this);
        videoPlayer.setBackgroundColor(Color.parseColor("#000000"));
        VideoController controller=new VideoController(videoPlayer.getContext());
        videoPlayer.setController(controller);
        /**
         * 给播放器控制器绑定需要的自定义UI交互组件
         */
        ControlToolBarView toolBarView=new ControlToolBarView(this);//标题栏，返回按钮、视频标题、功能按钮、系统时间、电池电量等组件
        ControlFunctionBarView functionBarView=new ControlFunctionBarView(this);//底部时间、seek、静音、全屏功能栏
        functionBarView.showSoundMute(true,false);//启用静音功能交互\默认不静音
        ControlStatusView statusView=new ControlStatusView(this);//移动网络播放提示、播放失败、试看完成
        ControlGestureView gestureView=new ControlGestureView(this);//手势控制屏幕亮度、系统音量、快进、快退UI交互
        ControlCompletionView completionView=new ControlCompletionView(this);//播放完成、重试
        ControlLoadingView loadingView=new ControlLoadingView(this);//加载中、开始播放
        controller.addControllerWidget(toolBarView,functionBarView,statusView,gestureView,completionView,loadingView);

        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        videoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                return new JkMediaPlayer(MainActivity.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            }
        });
        videoPlayer.setLoop(false);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
        videoPlayer.setDataSource(MP4_URL2);//播放地址设置
        videoPlayer.startFullScreen();//开启全屏播放
        videoPlayer.playOrPause();//开始异步准备播放
```
#### 注意点

* 5.全屏播放功能清单文件配置：如果播放器需要横屏播放,需要在Activity清单文件配置如下属性：</br>
```
    //在需要全屏播放的Activity清单文件中添加属性：android:configChanges="orientation|screenSize"，防止横屏时Activity重绘
    <activity
        android:name="xxx.xxx.PlayerActivity"
        android:launchMode="singleTask"
        android:screenOrientation="portrait"
        android:configChanges="orientation|screenSize">
```


* 4.交互控制器</br>
```
    //SDK内部提供了一套默认的交互控制器，功能支持包括但不限于：开始播放、暂停播放、全屏播放、seek调节播放位置、手势交互控制屏幕亮度、系统音量、快进、快退、window模式、列表模式、试看等。
    //给播放器设置一个继承自BaseController的控制器，VideoPlayer也封装了initController();方法方便快捷绑定控制器
    VideoController controller = new VideoController(mVideoPlayer.getContext());
    mVideoPlayer.setController(controller);
    controller.showBackBtn(true);//竖屏状态下是否显示返回按钮
    controller.showMenus(true,true,true);//参数1：投屏按钮，参数2：悬浮窗按钮，参数3：菜单按钮，竖屏状态是否显示控制器右上角的功能菜单按钮,默认是不显示的(横屏模式下不显示所有菜单按钮)
    controller.setCanTouchInPortrait(true);//竖屏状态下是否启用手势交互,默认不启用
    controller.showSoundMute(true,false);//参数1：是否显示右下角静音交互按钮，参数2:是否静音
    //controller.setListPlayerMode(false,false);//设置列表播放模式,参数1：启用列表模式，参数2：默认是否静音
    //controller.setPreViewTotalDuration("3600");//注意:设置虚拟总时长(一旦设置播放器内部走片段试看流程)，试看结束回调至OnControllerEventListener的onCompletion()方法
    //监听控制器交互事件(设置开启了返回showBackBtn\菜单栏showMenus等功能后可设置监听,回调方法请参阅：OnControllerEventListener)
    controller.setOnControllerListener(new VideoController.OnControllerEventListener(){

        @Override
        public void onBack() {//竖屏状态下且启用了返回按钮回调至此
            finish();
        }

        @Override
        public void onGobalWindow() {
            //可参考WindowGlobalPlayerActivity
            boolean globalWindow = binding.videoPlayer.startGlobalWindow(dpToPx(3f), Color.parseColor("#99000000"));//支持多参,这里给悬浮窗设置一个圆角和背景颜色
            if(globalWindow){
                finish();
            }
        }
    });
```
* 5.在需要播放的地方</br>
```

```


* 8.常用播放器设置</br>
```
    //监听播放器状态(如果需要自定义解码器必须设置此监听,在createMediaPlayer中初始化自己的解码器)

    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {

        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            return null;//返回null时,SDK内部会自动使用系统MediaPlayer解码器,自定义解码器请参考Demo中的JkMediaPlayer或ExoMediaPlayer类
        }

        @Override
        public void onPlayerState(PlayerState state, String message) {
            //播放器内部工作状态
        }
        //更多回调请查阅OnPlayerEventListener
    });


    mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);//居中显示,定宽等高 (更多缩放模式请参考IMediaPlayer设置)
    mVideoPlayer.setLoop(true);//是否循环播放
    mVideoPlayer.setProgressCallBackSpaceMilliss(300);//设置进度条回调间隔时间(毫秒)
    mVideoPlayer.setSoundMute(false);//是否开启静音播放
    mVideoPlayer.setSpeed(1.0f);//设置播放倍速(默认正常即1.0f，区间：0.5f-2.0f)
    mVideoPlayer.setMirror(false);//是否镜像显示
    mVideoPlayer.setMobileNetwork(true);//移动网络下是否允许播放网络视频,需要声明权限：<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    mVideoPlayer.setInterceptTAudioFocus(true);//是否监听音频焦点状态，设置为true后SDK在监听焦点丢失时自动暂停播放

    //..更多设置或功能请参阅IVideoPlayerControl API
```
* 9.其它播放器全局设置</br>
```
    //设置播放器是否拦截音频焦点丢失事件,如果设置了检测到音频焦点丢失会自动暂停播放
    IVideoManager.getInstance().setInterceptTAudioFocus(true);
    //设置移动网络下是否允许播放
    IVideoManager.getInstance().setMobileNetwork(true);
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


    <!--播放网络视频需要在application节点声明此属性-->
    <application
        android:usesCleartextTraffic="true">

    </application>
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
    参考VideoDetailsActivity中的initPlayer方法中的代码示例
```

* 4.list列表播放转场无缝衔接播放</br>
```
    参考ListPlayerChangeFragment中的onItemClick、onActivityResult方法和VideoDetailsActivity中的initPlayer、onBackPressed、close、onDestroy方法示例代码
```
* 5.类似抖音功能,支持缓存秒播</br>
```
    参考PagerPlayerFragment中的示例代码，缓存参考PagerPlayerAdapter中的initItemView方法
```