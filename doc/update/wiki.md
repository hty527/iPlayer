## 播放器框架结构图
![iPlayer架构关系图](https://amuse-1259486925.cos.ap-hongkong.myqcloud.com/image/iPlayer%E6%9E%B6%E6%9E%84%E5%85%B3%E7%B3%BB%E5%9B%BE.png)
* 如图所示，通过架构图可以直观的看到面向用户层的模块和交互都支持自定义，也包括视频解码器在内。
## 常用文档
### 一、常用api使用说明
#### 1、播放器API
* 1.1、请阅读[IPlayerControl][1]
* 1.2、常用API
```
    mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);//设置画面缩放|裁剪模式为居中显示(原始大小),定宽等高 (更多缩放模式请参考IMediaPlayer设置)，默认为IMediaPlayer.MODE_ZOOM_TO_FIT
    mVideoPlayer.setLandscapeWindowTranslucent(true);//全屏播放下是否启用沉浸样式，默认关闭。辅以setZoomModel为IMediaPlayer.MODE_ZOOM_CROPPING效果最佳，默认为false
    mVideoPlayer.setLoop(true);//是否循环播放，，默认为false
    mVideoPlayer.setProgressCallBackSpaceMilliss(300);//设置进度条回调间隔时间(毫秒)，默认300毫秒
    mVideoPlayer.setSoundMute(false);//是否开启静音播放，默认为false
    mVideoPlayer.setSpeed(1.0f);//设置播放倍速(默认正常即1.0f，区间：0.5f-2.0f)
    mVideoPlayer.setMirror(false);//是否镜像显示，默认为false
    mVideoPlayer.setVolume(1.0f,1.0f);//设置左右声道，0.0f(小)-1.0f(大)
    mVideoPlayer.setMobileNetwork(false);//移动网络下是否允许播放网络视频,如需网络提示交互需要声明权限：<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    mVideoPlayer.setInterceptTAudioFocus(true);//是否监听音频焦点状态，设置为true后SDK在监听焦点丢失时自动暂停播放，，默认为true
    mVideoPlayer.setPlayCompletionRestoreDirection(true);//横屏状态下播放完成是否自动还原到竖屏状态,默认为true
    mVideoPlayer.setAutoChangeOrientation(true);//是否开启重力旋转。当系统"自动旋转"开启+正在播放生效
```
#### 2、控制器API
* 2.1、请阅读[IVideoController][2]
* 2.2、常用API
```
    mController.setTitle("测试地址播放");//视频标题(默认控制器横屏状态下可见),所有UI交互组件都会收到setTitle回调
    mController.setPreViewTotalDuration("3600");//注意:设置虚拟总时长(一旦设置播放器内部走片段试看流程)，试看结束回调至OnControllerEventListener的onCompletion()方法，启用试看流程时播放器必须设置为mVideoPlayer.setLoop(true);
    mController.setPlayerScene(IVideoController.SCENE_NOIMAL);//设置控制器应用场景
    mController.addControllerWidget(IControllerView controllerView);//给控制器添加UI交互组件
    //默认控制器独有api
    //mController.showLocker(true);//横屏状态下是否启用屏幕锁功能,默认开启
    //以下常用api的控制器需继承GestureController
    //mController.setCanTouchInPortrait(true);//竖屏状态下是否开启手势交互,默认允许
    //mController.setCanTouchPosition(true);//设置是否可以滑动调节进度，默认可以
    //mController.setGestureEnabled(true);//是否开启手势控制，默认关闭，关闭之后，手势调节进度，音量，亮度功能将关闭
    //mController.setDoubleTapTogglePlayEnabled(true);//是否开启双击播放/暂停，默认关闭
```
#### 3、交互组件API
* 3.1、请阅读[IControllerView][3]

[1]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/interfaces/IPlayerControl.java "IPlayerControl"
[2]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/interfaces/IVideoController.java "IVideoController"
[3]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/interfaces/IControllerView.java "IControllerView"
#### 4、自定义解码器
* 4.1、为方便开发者和减少工作量，特意封装了ijk和exo第三方解码器SDK：
```
    //ijk音视频解码器,根据自己需要实现
    implementation 'com.github.hty527.iPlayer:ijk:lastversion'
    //exo音视频解码器,根据自己需要实现
    implementation 'com.github.hty527.iPlayer:exo:lastversion'
```
* 4.2、SDK默认使用MediaPlayer解码器，自定义解码器的使用,请参考：[IJkMediaPlayer][4]和[ExoMediaPlayer][5]

[4]:https://github.com/hty527/iPlayer/blob/main/ijk/src/main/java/com/android/iplayer/media/core/IJkMediaPlayer.java "IJkMediaPlayer"
[5]:https://github.com/hty527/iPlayer/blob/main/exo/src/main/java/com/android/iplayer/media/core/ExoMediaPlayer.java "ExoMediaPlayer"
* 4.3、更换解码器或应用自定义解码器
```
    int MEDIA_CORE=1;
    /**
     * 如果使用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
     */
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {

        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            if(1==MEDIA_CORE){
                return IjkPlayerFactory.create().createPlayer(VideoPlayerActivity.this);//IJK解码器，需引用库：implementation 'com.github.hty527.iPlayer:ijk:lastversion'
            }else if(2==MEDIA_CORE){
                return ExoPlayerFactory.create().createPlayer(VideoPlayerActivity.this);//EXO解码器，需引用库：implementation 'com.github.hty527.iPlayer:exo:lastversion'
            }else{
                return null;//返回null时,SDK内部会自动使用系统MediaPlayer解码器,自定义解码器请参考Demo中ExoMediaPlayer类或ijk中的IJkMediaPlayer类
            }
        }
    });
```

#### 5、自定义UI交互
##### 5.1、自定义Controller
* 5.1.1、继承[BaseController][6]实现自己的控制器，如需手势交互，请继承[GestureController][7]
* 5.1.2、设置控制器到播放器

[6]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/base/BaseController.java "BaseController"
[7]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/controller/GestureController.java "GestureController"
```
    VideoController controller=new VideoController(videoPlayer.getContext());
    mVideoPlayer.setController(controller);//将控制器绑定到播放器
    //或使用SDK内置的
    mVideoPlayer.createController();
```
##### 5.2、自定义UI交互组件
* 5.2.1、为什么有自定义Controller还要整个"自定义UI交互组件"出来？<br>
因为Controller在处理交互比较复杂或功能比较多的场景下耦合性太高，于是Controller就拓展了自定义UI交互组件能力，可根据需要来添加自己的UI交互组件和任意自定义单个交互模块的UI组件。
* 5.2.2、SDK提供了一套标题栏、底部控制栏、播放器状态(网络提示、播放失败)、播放完成、手势交互相应处理、Window窗口、列表模式 等UI交互组件。Controller的任意UI交互组件均支持自定义。
* 5.2.3、自定义UI交互组件需要继承[BaseControlWidget][8]，参考[IControllerView][9]接口回调来实现自己的交互：

[8]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/base/BaseControlWidget.java "BaseControlWidget"
[9]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/interfaces/IControllerView.java "IControllerView"
```
    /**
     * 1、给播放器设置一个控制器
     */
    mController = new VideoController(mVideoPlayer.getContext());
    //mController.showLocker(true);//横屏状态下是否启用屏幕锁功能,默认开启
    //mController.setCanTouchInPortrait(true);//竖屏状态下是否开启手势交互,内部默认允许
    //mController.setCanTouchPosition(true);//设置是否可以滑动调节进度，默认可以
    //mController.setGestureEnabled(true);//是否开启手势控制，默认关闭，关闭之后，手势调节进度，音量，亮度功能将关闭
    //mController.setDoubleTapTogglePlayEnabled(true);//是否开启双击播放/暂停，默认关闭
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
    //将自定义交互组件添加到控制器
    mController.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
```
##### 5.3、使用自定义组件对象
* 5.3.1、 假设播放器在A界面初始化，如何在B界面找到此前在A界面设置的自定义交互组件并使用此组件对象？
```
    //1、在给控制器设置自定义交互组件时，可为自定义组件设置target
    ControlToolBarView controlToolBarView=new ControlToolBarView(this);
    controlToolBarView.setTarget("target");
    mController.addControllerWidget(controlToolBarView);//添加到控制器
    //或者：
    mController.addControllerWidget(new ControlToolBarView(this),"target");//添加到控制器

    //2、根据target寻找自定义交互组件
    IControllerView controllerView = mController.findControlWidgetByTag("target");
    if(null!=controllerView&&controllerView instanceof ControlToolBarView){
        ControlToolBarView controlToolBarView= (ControlToolBarView) controllerView;
        controlToolBarView.showMenus(true,true,true);
        controlToolBarView.setOnToolBarActionListener(new ControlToolBarView.OnToolBarActionListener() {
            @Override
            public void onWindow() {
                
            }

            @Override
            public void onMenu() {
               
            }
        });
    }
```
##### 5.4、使用SDK默认UI组件
* 5.4.1、为方便开发者快速实现完整的播放器交互功能，特针对播放器封装UI交互组件SDK，请集成：
```
    //UI交互组件,可根据需要使用
    implementation 'implementation 'com.github.hty527.iPlayer:widget:lastversion'
```
* 5.4.2、使用UI交互组件
```
    //创建一个默认控制器
    VideoController controller = new VideoController(mVideoPlayer.getContext());
    //将播放器绑定到控制器
    mVideoPlayer.setController(controller);
    //一键使用默认UI交互组件绑定到控制器
    WidgetFactory.bindDefaultControls(controller);
```
* 5.4.3、UI交互SDK支持一句话为播放器设置默认控制器和UI交互组件
```
    //创建一个控制器绑定到播放器，然后将所有UI交互组件绑定到控制器
    WidgetFactory.bindDefaultControls(mVideoPlayer.createController());
```
* 5.4.4、使用默认Controller+自定义交互组件时，SDK内部会为添加的每一个自定义UI组件绑定一个target，默认target请阅读[IVideoController][10]类。
* 根据默认target寻找组件示例：

[10]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/interfaces/IVideoController.java "IVideoController"
```
    //这里以标题栏为示例
    IControllerView controllerView = controller.findControlWidgetByTag(IVideoController.TARGET_CONTROL_TOOL);
    if(null!=controllerView&&controllerView instanceof ControlToolBarView){
        ControlToolBarView controlToolBarView= (ControlToolBarView) controllerView;
        controlToolBarView.showMenus(true,true,true);
        controlToolBarView.setOnToolBarActionListener(new ControlToolBarView.OnToolBarActionListener() {
            @Override
            public void onWindow() {
                
            }

            @Override
            public void onMenu() {
               
            }
        });
    }
```
##### 5.5、控制器场景
###### 5.5.1、场景介绍
* SDK控制器内置5种场景，分别是：0：常规状态(包括竖屏、横屏)，1：Activity级别悬浮窗，2：全局悬浮窗，3：画中画悬浮窗，4：列表模式， 
###### 5.5.2、场景使用
* 当场景变化时会回调到[IVideoController][10]的onPlayerScene和[IControllerView][9]的onPlayerScene方法。开发者可在控制器或UI交互组件中根据场景的变化做相应的UI交互变更。

###### 5.5.3、自定义场景
* 为方便播放器应用于不同场景，SDK支持自定义场景设置，用户可根据自己的场景来实现不同的UI交互。
```
    //更新播放器\控制器所在场景,调用此方法后控制器和所有UI组件都会收到onPlayerScene(int playerScene)回调
    controller.setPlayerScene(IVideoController.SCENE_NOIMAL);
```
#### 6、自定义画面渲染器
###### 6.1、画面渲染器自定义
* SDK内部的视频画面渲染器使用的是TextureView,TextureView和SurfaceView推荐使用TextureView。SurfaceView在横竖屏切换时会有短暂黑屏及镜像(setScaleX)失效。
* 自定义视频画面渲染器组件View需要实现[IRenderView][19]接口并实现所有接口方法，在getView中返回你的TextureView或SurfaceView
```
    //1、继承TextureView并设置监听
    setSurfaceTextureListener(this);
    //2、绑定到播放器
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        //ILogger.d(TAG,"onSurfaceTextureAvailable-->width:"+width+",height:"+height);
        if(null==mMediaPlayer) return;
        if(null!=mSurfaceTexture){
            setSurfaceTexture(mSurfaceTexture);
        }else{
            mSurfaceTexture = surfaceTexture;
            mSurface =new Surface(surfaceTexture);
            mMediaPlayer.setSurface(mSurface);
        }
    }
```
###### 6.2、应用自定义渲染器
```
    //自定义画面渲染器，在开始播放前设置监听并返回自定义画面渲染器后生效
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {

        @Override
        public IRenderView createRenderView() {
            return new CoustomSurfaceView(MainActivity.this);//返回null时,SDK内部会自动使用自定义的MediaTextureView渲染器,自定义渲染器请参考Demo中CoustomSurfaceView类
        }
    });
```
#### 7、全屏播放
##### 7.1、横竖屏切换
* 7.1.1、全屏播放api
```
    mVideoPlayer.startFullScreen();//开启全屏播放
```
* 7.1.2、如需支持横竖屏切换播放，需在AndroidManifest中所在的Activity申明如下属性：
```
    android:configChanges="orientation|screenSize"
```
* 7.1.3、SDK支持全屏播放下全屏幕沉浸效果
```
    //全屏沉浸样式
    mVideoPlayer.setLandscapeWindowTranslucent(true);
    //设置缩放模式为裁剪缩放铺满全屏
    mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
```
##### 7.2、直接启动全屏播放
* 7.2.1、SDK支持在任意位置直接启动全屏播放：
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
    videoPlayer.setLoop(false);
    videoPlayer.setProgressCallBackSpaceMilliss(300);
    videoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
    videoPlayer.setDataSource(MP4_URL2);//播放地址设置
    videoPlayer.startFullScreen();//开启全屏播放
    videoPlayer.prepareAsync();//开始异步准备播放
```
#### 8、窗口播放
##### 8.1、Activity级别悬浮窗
* 8.1.1、Activity级别悬浮窗无需悬浮窗权限直接开启：
```
    //开启Activity级别窗口播放并启用拖拽窗口松手后自动吸附至屏幕边缘
    mVideoPlayer.startWindow(true);

    //startWindow支持多参传入，示例参数1：给窗口设置一个圆角，参数2：给窗口设置一个背景色，其它更多参数请阅读api。
    mVideoPlayer.startWindow(ScreenUtils.getInstance().dpToPxInt(3f), Color.parseColor("#99000000"));
```
* 8.1.2、SDK支持在任意位置直接启动Activity级别悬浮窗播放：
```
    private void startMiniWindowPlayer() {
        //播放器内部在收到返回事件时，检查到没有宿主ViewGroup时会自动销毁播放器，此时应该在收到PlayerState.STATE_DESTROY状态时清除播放器变量
        if(null==mVideoPlayer){
            mVideoPlayer = new VideoPlayer(this);
            VideoController controller=new VideoController(this);
            //给播放器设置控制器
            mVideoPlayer.setController(controller);
            //给播放器控制器绑定需要的自定义UI交互组件
            ControWindowView controWindowView=new ControWindowView(this);//加载中、开始播放
            controller.addControllerWidget(controWindowView);
            //播放完成时内部会关闭window，在销毁播放器时清除变量
            mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
         
                @Override
                public void onPlayerState(PlayerState state, String message) {
                    Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
                    if(PlayerState.STATE_DESTROY==state){
                        mVideoPlayer=null;
                    }
                }
            });
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(MP4_URL2);//播放地址设置
            //自定义窗口播放的宽,高,起始X轴,起始Y轴属性,这里示例将播放器添加到标题栏下方右侧
            //mVideoPlayer.startWindow();
            int[] screenLocation=new int[2];
            TitleView titleView = findViewById(R.id.title_view);
            titleView.getLocationInWindow(screenLocation);
            int width = (PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2)+ScreenUtils.getInstance().dpToPxInt(30f);
            int height = width*9/16;
            float startX=PlayerUtils.getInstance().getScreenWidth(MainActivity.this)/2-PlayerUtils.getInstance().dpToPxInt(45f);//开始位置
            float startY=screenLocation[1]+titleView.getHeight()+PlayerUtils.getInstance().dpToPxInt(15f);
            //启动窗口播放
            mVideoPlayer.startWindow(width,height,startX,startY,ScreenUtils.getInstance().dpToPxInt(3f),Color.parseColor("#99000000"));//初始显示的位置并添加窗口颜色和圆角大小
            //mVideoPlayer.startWindow(ScreenUtils.getInstance().dpToPxInt(3f),Color.parseColor("#99000000"));//也可以使用内部默认的窗口宽高和位置属性
            mVideoPlayer.prepareAsync();//开始异步准备播放
        }
    }
```
* 8.1.3、Activity级别悬浮窗默认是开启松手后自定吸附悬停至最近的X轴方向的屏幕边缘功能的，如需关闭，请查阅：startWindow()多参方法参数说明。

##### 8.2、全局悬浮窗
###### 8.2.1、悬浮窗开启
* 8.2.1.1、全局悬浮窗需要SYSTEM_ALERT_WINDOW权限，SDK内部会自动检测和申请SYSTEM_ALERT_WINDOW权限。
``` 
    //1、申明SYSTEM_ALERT_WINDOW权限
    <!--如您的播放器需要支持全局悬浮窗窗口播放请申明此权限-->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

    //2、开启全局悬浮窗窗口播放并启用拖拽窗口松手后自动吸附至屏幕边缘
    boolean globalWindow = mVideoPlayer.startGlobalWindow(true);

    //startGlobalWindow支持多参传入，示例参数1：给窗口设置一个圆角，参数2：给窗口设置一个背景色，其它更多参数请阅读api。
    boolean globalWindow = mVideoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
```
* 8.2.1.2、SDK支持在任意位置直接启动全局悬浮窗窗口播放：
```
    VideoPlayer videoPlayer = new VideoPlayer(MainActivity.this);
    videoPlayer.setLoop(false);
    videoPlayer.setProgressCallBackSpaceMilliss(300);
    videoPlayer.setDataSource(MP4_URL2);//播放地址设置
    //初始化一个默认的控制器(内部适用默认的一套交互UI控制器组件)
    VideoController controller = new VideoController(mVideoPlayer.getContext());
    mVideoPlayer.setController(controller);
    WidgetFactory.bindDefaultControls(controller);//一键使用默认UI交互组件绑定到控制器
    controller.setTitle("任意界面开启一个悬浮窗窗口播放器");//视频标题(默认视图控制器横屏可见)
    boolean globalWindow = videoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
    if(globalWindow) {
        IWindowManager.getInstance().setCoustomParams(null);//给悬浮窗口播放器绑定自定义参数，在点击窗口播放器跳转至Activity时有用
        videoPlayer.prepareAsync();//开始异步准备播放,注意界面关闭不要销毁播放器实例
    }
```
* 8.2.1.3、全局悬浮窗默认是开启松手后自定吸附悬停至最近的X轴方向的屏幕边缘功能的，如需关闭，请查阅：startGlobalWindow()多参方法参数说明。

###### 8.2.2、悬浮窗点击
* 8.2.2.1、SDK内部提供了监听悬浮窗窗口播放器的关闭、点击回调监听器
```
    1、在Application中注册监听器
    IWindowManager.getInstance().setOnWindowActionListener(new OnWindowActionListener() {
        
        //悬浮窗扣拖动事件
        @Override
        public void onMovie(float x, float y) {

        }

        //点击了悬浮窗口播放器
        @Override
        public void onClick(BasePlayer basePlayer, Object coustomParams) {//设置的自定义参数会回调至此
            Logger.d(TAG,"onClick-->coustomParams:"+coustomParams);
            
        }

        //点击了悬浮窗口播放器的关闭按钮
        @Override
        public void onClose() {
            Logger.d(TAG,"onClose-->");
            IWindowManager.getInstance().quitGlobaWindow();//关闭悬浮窗播放器窗口
        }
    });

    2、在启动全局悬浮窗窗口播放器播放成功后，设置自定义参数(在点击悬浮窗时转场到Activity继续播放时需要)
    IWindowManager.getInstance().setCoustomParams(coustomParams);

    3、点击悬浮窗的全屏按钮后会回调至第1步的onClick中。
```
* 8.2.2.2、如果设置了setOnWindowActionListener需要在收到onClose回调后关闭悬浮窗，未设置SDK内部会自定关闭。
* 8.2.2.3、自定义悬浮窗UI交互组件全屏点击事件请回调至播放器！！！
```
    IWindowManager.getInstance().onClickWindow();
```
#### 9、多播放器同时播放
* 9.1、SDK内部默认不支持并发播放，如需支持多播放器同时工作，需要设置允许播放器同时播放。
```
    //告诉播放器忽略音频焦点丢失事件，播放器内部即可支持多播放器同时播放
    IVideoManager.getInstance().setInterceptTAudioFocus(false);
```
#### 10、直播拉流
* 10.1、SDK内部自带的系统MediaPlayer对直播流的拓展仅限于.m3u8格式，如需支持更多的直播流视频格式，请使用ijk或exo,或自定义解码器拓展。直播流相关请参考[LivePlayerActivity][11]类
```
    //ijk音视频解码器,根据自己需要实现
    implementation 'com.github.hty527.iPlayer:ijk:lastversion'
    //exo音视频解码器,根据自己需要实现
    implementation 'com.github.hty527.iPlayer:exo:lastversion'
```
* 10.2、更多格式支持：
```
    int MEDIA_CORE=1;
    /**
     * 可使用受支持的视频流格式的解码器
     */
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {

        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            return IjkPlayerFactory.create().createPlayer(VideoPlayerActivity.this);
            //return MediaPlayerFactory.create().createPlayer(VideoPlayerActivity.this);
            //return ExoPlayerFactory.create().createPlayer(VideoPlayerActivity.this);
            //或其它受支持的解码器
        }
    });
```

[11]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/ui/activity/LivePlayerActivity.java "LivePlayerActivity"
#### 11、收费试看模式
* 11.1、SDK默认Controller支持试看模式，请参考[PerviewPlayerActivity][18]分两步实现：
```
    VideoController controller = new VideoController(mVideoPlayer.getContext());//创建一个默认控制器
    WidgetFactory.bindDefaultControls(controller);//一键使用默认UI交互组件绑定到控制器
    //1、设置虚拟的视频总时长,即可开启试看模式,试看模式下不能开启循环播放，否则无法回调试看完成状态。
    controller.setPreViewTotalDuration(DURATION+"");//注意:设置虚拟总时长(一旦设置控制器部走片段试看流程)
    mVideoPlayer.setLoop(false);//关闭循环播放
    //2、添加自己的试看播放完成的UI交互组件
    ControPerviewView controPerviewView=new ControPerviewView(controller.getContext());
    controPerviewView.setOnEventListener(new ControPerviewView.OnEventListener() {
        @Override
        public void onBuy() {
            Logger.d(TAG,"单片购买");
            //如果设置了setPlayCompletionRestoreDirection(false),需先退出横屏
            mVideoPlayer.isBackPressed();
        }

        @Override
        public void onVipBuy() {
            Logger.d(TAG,"会员价格购买");
            //如果设置了setPlayCompletionRestoreDirection(false),需先退出横屏
            mVideoPlayer.isBackPressed();
        }
    });
    controller.addControllerWidget(controPerviewView);
```
#### 12、连续播放
* 可参考Demo中的[VideoListPlayerActivity][12]类

[12]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/ui/activity/VideoListPlayerActivity.java "VideoListPlayerActivity"
```
    //1、mVideoPlayer.setLoop(false);//连续播放模式下只能设置为false
    //2、实现连续播放可在收到播放完成时切换视频流
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            return null;
        }

        @Override
        public void onPlayerState(PlayerState state, String message) {
            Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
            if(null!=mVideoPlayer){
                if(PlayerState.STATE_COMPLETION==state){
                    //尝试播放下一个视频
                    String url = getUrl(mPosition);
                    Logger.d(TAG,"onPlayerState-->url:"+url);
                    if(null!=url){
                        mVideoPlayer.onReset();
                        mVideoPlayer.setDataSource(url);
                        mPosition+=1;
                        mVideoPlayer.prepareAsync();
                    }else{
                        Logger.d(TAG,"onPlayerState-->播放到最后一个了");
                    }
                }
            }
        }
    });
```
#### 13、转场衔接播放
###### 13.1、列表转场
* 13.1.1、列表转场衔接继续播放原理：
```
    1、点击跳转到新的界面时将播放器从父容器中移除，并保存到全局变量
    2、将全局变量播放器对象添加到新的ViewGroup容器
    3、回到列表界面时如果播放的视频源没有被切换,关闭当前Activity不要销毁播放器,将播放器从当前父容器中移除
    4、重新添加到列表界面的此前正在播放的item中的ViewGroup中
```
* 13.1.2、列表转场衔接继续播放实现：主要参考Demo中的[ListPlayerChangedFragment][13]、[ListPlayerFragment][14]、[VideoDetailsActivity][15]类

[13]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/video/ui/fragment/ListPlayerChangedFragment.java "ListPlayerChangedFragment"
[14]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/video/ui/fragment/ListPlayerFragment.java "ListPlayerFragment"
[15]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/video/ui/activity/VideoDetailsActivity.java "VideoDetailsActivity"
``` 
    1、开始播放：参考ListPlayerFragment类的startPlayer()方法，注意标记当前mCurrentPosition和mPlayerContainer
    2、点击item跳转：参考ListPlayerChangedFragment类的onItemClick()方法，跳转到新的Activity
    3、新的Activity接收播放器继续播放：参考VideoDetailsActivity类的initPlayer方法，根据mIsChange变量来确认是否处理转场播放。
    4、新的Activity销毁：新的Activity在关闭时如果播放器视频地址未被切换，则在onDestroy中不要销毁播放器，参考：VideoDetailsActivity类的onDestroy
    5、回到列表界面：如果处理了第4步，在回到列表界面时接收并处理播放器，参考：ListPlayerChangedFragment类的onActivityResult方法和ListPlayerFragment类的recoverPlayerParent方法
```
###### 13.2、悬浮窗转场
* 13.2.1、全局悬浮窗窗口播放器转场到新的Activity请参考：Demo [WindowGlobalPlayerActivity][16]类的initPlayer方法或[VideoDetailsActivity][15]类的initPlayer方法
#### 14、实现秒播功能
* 参考"视频预缓存"相关说明实现。
#### 15、视频本地缓存
* 注意预缓存和边播边存api，调用不一样！！！
* 15.1、为方便视频缓存的业务需求，已基于AndroidVideoCache封装了一个独立SDK，具体api请阅读[VideoCache][20]
```
    //音视频预缓存+边播边存,根据需要使用
    implementation 'com.github.hty527.iPlayer:cache:lastversion'
```
* 15.2、缓存配置,SDK内部会自动处初始化！如果需要自定义缓存大小、缓存目录，可自行调用初始化。
```
    /**
     * SDK内部会在使用缓存相关功能时自动初始化。如果需要自行定义缓存目录、缓存目录最大长度大小可自行调用初始化。必须在使用缓存功能之前初始化
     */
    //返回的路径是SD卡包名下的内部缓存路径，无需存储权限。位于/storage/emulated/0/Android/data/包名/files/video/cache下，会随着应用卸载被删除
    //其它路径请注意申请动态权限！！！
    File cachePath = context.getExternalFilesDir("video/cache/");
    //参数2：缓存大小(单位：字节),参数3：缓存路径,不设置默认在sd_card/Android/data/[app_package_name]/cache中
    VideoCache.getInstance().initCache(context,1024*1024*1024,cachePath);//缓存大小为1024M，路径为SD卡下的cachePath。请注意SD卡权限状态。
```
* 15.3、targetSdk=29时Android 10及以上机型创建本地目录失败？
```
    //在你的AndroidManifest中添加此属性配置
    <application
        android:requestLegacyExternalStorage="true">
    </application>
```
##### 15.1、视频预缓存
* 预缓存一般用于列表、类似抖音的播放场景，在渲染画面时，提前缓存好指定大小的视频文件，实现秒播的功能。参考Demo中的[PagerPlayerAdapter][17]和[VideoCacheActivity][21]用法
* 15.1.1、开始\结束预缓存
```
    //开始预缓存|缓存，此方法为多参方法，可选preloadLength(预缓存大小)和position(位于列表中的position)
    VideoCache.getInstance().startPreloadTask(rawUrl);//rawUrl为你的源http/https视频地址
    //暂停预缓存|缓存，此方法为多参方法，请参考方法参数注释
    VideoCache.getInstance().pausePreload();
    //根据原视频地址取消预缓存|缓存
    VideoCache.getInstance().removePreloadTask(rawUrl);//rawUrl为你的源http/https视频地址
    //取消所有预缓存任务
    VideoCache.getInstance().removeAllPreloadTask();
```
* 15.1.2、使用预缓存地址播放视频
```
    //开始播放时使用此播放地址来播放
    String cacheUrl = VideoPreloadManager.getInstance(getContext()).getPlayPreloadUrl(rawUrl);//rawUrl为你的源视频地址
    mVideoPlayer.setDataSource(cacheUrl);//播放地址设置
    mVideoPlayer.prepareAsync();//开始异步准备播放
```
##### 15.2、边播边存
```
    //边播边存api非常简单，只需要传入你的地址，内部会转换为本地代理地址，播放完成后，再次播放不再消耗流量(不删除缓存情况下)
    String playUrl = VideoCache.getInstance().getPlayUrl(rawUrl);//rawUrl为你的源视频地址
    mVideoPlayer.setDataSource(playUrl);//播放地址设置
    mVideoPlayer.prepareAsync();//开始异步准备播放
```
[16]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/ui/activity/WindowGlobalPlayerActivity.java "WindowGlobalPlayerActivity"
[17]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/pager/adapter/PagerPlayerAdapter.java "PagerPlayerAdapter"
[18]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/ui/activity/PerviewPlayerActivity.java "PerviewPlayerActivity"
[19]:https://github.com/hty527/iPlayer/blob/main/iplayer/src/main/java/com/android/iplayer/interfaces/IRenderView.java "IRenderView"
[20]:https://github.com/hty527/iPlayer/tree/main/cache/src/main/java/com/android/iplayer/video/cache/VideoCache.java "VideoCache"
[21]:https://github.com/hty527/iPlayer/blob/main/app/src/main/java/com/android/videoplayer/ui/activity/VideoCacheActivity.java "VideoCacheActivity"

#### 16、本地SD卡视频播放
* 16.1、本地SD卡视频播放需要注意获取存储权限及File文件协议，设置本地播放地址如下：
```
    //本地file需要转称file://协议
    File file=new File(Environment.getExternalStorageDirectory(),"190204084208765161.mp4");
    String filePath = Uri.parse("file://" + file.getAbsolutePath()).toString();
    mVideoPlayer.setDataSource(filePath);
    mVideoPlayer.prepareAsync();//开始异步准备播放
```
* 16.2、Android9及以上设备提示：Permission denied？请先检查是否动态申请存储权限，若已申请添加如下配置
```
    //在application中加入
    android:requestLegacyExternalStorage="true"
```
* 16.3、或直接调用api
```
    File file=new File(Environment.getExternalStorageDirectory(),"190204084208765161.mp4");
    mVideoPlayer.setDataSource(file);
    mVideoPlayer.prepareAsync();//开始异步准备播放
```
#### 17、Log日志
```
    ILogger.DEBUG=true;//或 ILogger.setDebug(true);
```
### 二、第三方解码器
* SDK默认使用系统MediaPlayer作为音视频解码器，SDK支持自定义任意解码器实现音视频解码。请阅读《自定义解码器》部分。
#### 1、IJK解码器
##### 1.1、依赖
```
    //对IJK解码器的二次封装实现（版本号与iPlayer SDK一致）
    implementation 'com.github.hty527.iPlayer:ijk:lastversion'
```
* 请根据需要选择ABI平台
```
    defaultConfig {
            ndk {
                abiFilters 'arm64-v8a','armeabi-v7a','armeabi','x86','x86_64'
            }
    }
```
##### 1.2、使用
```
    //开始播放视频前设置实现生效
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            return IjkPlayerFactory.create().createPlayer(LivePlayerActivity.this);
        }
    });
```
##### 1.3、混淆
```
    # IjkPlayer
    -keep class tv.danmaku.ijk.** { *; }
    -dontwarn tv.danmaku.ijk.**
```
#### 2、EXO解码器
##### 2.1、依赖
* SDK内部引用的EXO SDK版本号为：2.18.1
```
    //对EXO解码器的二次封装实现，必须依赖（版本号与iPlayer SDK一致）
    implementation 'com.github.hty527.iPlayer:exo:lastversion'

    //SDK内部实现EXO解码器逻辑，必须依赖
    //以下为必须项，SDK内部已引用，集成时无需引用
    //implementation 'com.google.android.exoplayer:exoplayer:2.18.1'//（必需）
    //implementation 'com.google.android.exoplayer:exoplayer-core:2.18.1'//核心功能（必需）
    //implementation "com.google.android.exoplayer:extension-rtmp:2.18.1"//rtmp直播流解码协议//（必需）

    //以下为可选依赖，请根据需要实现
    //implementation 'com.google.android.exoplayer:exoplayer-dash:2.18.1'//支持DASH内容
    //implementation "com.google.android.exoplayer:exoplayer-hls:2.18.1"//支持HLS内容
    //implementation "com.google.android.exoplayer:exoplayer-rtsp:2.18.1"//rtsp直播流解码协议
    //以下为自定义解码器场景根据需要实现，直接使用com.github.hty527.iPlayer:exo SDK时无需引用
    //implementation "com.google.android.exoplayer:exoplayer-smoothstreaming:2.18.1"//支持SmoothStreaming内容
    //implementation "com.google.android.exoplayer:exoplayer-transformer:2.18.1"//媒体转换功能，需要minSdkVersion>=21
    //implementation 'com.google.android.exoplayer:exoplayer-ui:2.18.1'//用于ExoPlayer的UI组件和资源
```
##### 2.2、使用
```
    //开始播放视频前设置实现生效
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            return ExoPlayerFactory.create().createPlayer(LivePlayerActivity.this);
        }
    });
```
##### 2.3、混淆
```
    # ExoPlayer
    -keep class com.google.android.exoplayer2.** { *; }
    -dontwarn com.google.android.exoplayer2.**
```
### 三、异常现象及注意点
#### 1、网络地址无法播放
* 请检查AndroidManifest文件中是否声明INTERNET权限
```
    <!--播放网络视频需要声明此权限-->
    <uses-permission android:name="android.permission.INTERNET" />

    <!--信任http明文-->
    <application
        android:usesCleartextTraffic="true">
    </application>
```

#### 2、移动网络未出现交互提示
* 请检查AndroidManifest文件中是否声明ACCESS_NETWORK_STATE权限
```
    <!--如您的播放器需要支持移动网路提示用户的交互,需要申明此权限,SDK用来检测网络状态-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### 3、全局悬浮窗播放失败
* 请检查AndroidManifest文件中是否声明SYSTEM_ALERT_WINDOW权限
```
    <!--如您的播放器需要支持全局悬浮窗窗口播放请申明此权限-->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

#### 4、全屏\小窗口播放无效
* 从列表或全局悬浮窗窗口转场跳转到Activity衔接继续播放场景下全屏、小窗口播放时全屏功能、手势设置屏幕亮度等和Activity相关的功能会失效。是因为播放器在上一个Activity创建并被添加到上一个Activity 的Window中，此时全屏、小窗口、屏幕亮度的结果会反馈给上一个Activity，
解决办法：
```
    //给播放器设置一个ParentConttext 上下文
    mVideoPlayer.setParentContext(context);上下文传递当前Activity Context
```
#### 5、播放器还原到竖屏位置
* 5.1、播放器在开启全屏、小窗口播放后还原到竖屏状态时，可能会因为下面这种布局方式而产生错位问题
```
    <?xml version="1.0" encoding="utf-8"?>
    <FrameLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <com.android.iplayer.widget.VideoPlayer
            android:id="@+id/video_player"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:layout_gravity="center"
            android:background="#000000">
        </com.android.iplayer.widget.VideoPlayer>
    </FrameLayout>
```
如布局所示：播放器父容器宽高大于播放器时，播放器设置了居中显示，但SDK在从全屏还原竖屏时会丢失居中属性。解决办法是给播放器嵌套一层ViewGroup：
```
    <?xml version="1.0" encoding="utf-8"?>
    <FrameLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center">
            <com.android.iplayer.widget.VideoPlayer
                android:id="@+id/video_player"
                android:layout_width="match_parent"
                android:layout_height="200dp"
                android:background="#000000">
            </com.android.iplayer.widget.VideoPlayer>
        </FrameLayout>
    </FrameLayout>
```
#### 6、混合语言兼容方案
##### 6.1、Flutter
* 6.1.1、将此播放器SDK应用到Android原生和Flutter混合开发使用时，会出现全屏功能、Activity级别小窗、屏幕亮度设置功能无法使用，解决办法：
```
    //给播放器设置一个ParentConttext 上下文
    mVideoPlayer.setParentContext(context);上下文传递当前混合语言所在的宿主Activity Context
```
#### 7、VideoController屏幕锁
##### 7.1、屏幕锁禁用
* 7.1.1、SDK内部提供的VideoController默认开启了屏幕锁交互，关闭方法：
```
    VideoController controller = new VideoController(mVideoPlayer.getContext());
    controller.showLocker(false);//禁止屏幕锁功能
```
##### 7.2、自定义屏幕锁交互
* 7.2.1、自定义Controller实现自己的屏幕锁交互
#### 8、IJKPlayer库体积大小
* ijk支持5中CPU架构，需根据你的需要做筛选配置，在你使用IJKPlayer的模块的build.gradle中的defaultConfig中加入：
```
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a','armeabi-v7a','armeabi','x86','x86_64'
        }
    }
```
#### 9、横屏沉浸样式
* 在带有刘海、水滴、挖空的>=28（Android9）设备上，播放器在横屏时无法延申到状态栏，有两个解决方案：
##### 9.1、播放器开启沉浸模式

* 给播放器设置横屏状态下允许启用全屏沉浸样式式，再配合缩放模式，全屏画面比较震撼。SDK版本需>=2.0.2.2
```
    mVideoPlayer.setLandscapeWindowTranslucent(true);//全屏模式下是否启用沉浸样式，在开始全屏前设置生效，默认关闭。辅以setZoomModel为IMediaPlayer.MODE_ZOOM_CROPPING效果最佳
    //mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);//设置视频画面渲染模式为：全屏缩放模式
```
##### 9.2、Style配置启用沉浸模式
* 如果不给播放器设置setLandscapeWindowTranslucent(true),还可以给Activity的style设置属性来达到全屏沉浸效果。两种方法二选一
```
    //添加这个属性达到全屏沉浸效果<item name="android:windowLayoutInDisplayCutoutMode" tools:ignore="NewApi">shortEdges</item>

    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowTranslucentNavigation">true</item>
        <item name="android:windowTranslucentStatus">true</item>
        <item name="android:windowContentOverlay">@null</item>
        <item name="android:windowLayoutInDisplayCutoutMode" tools:ignore="NewApi">shortEdges</item>
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowActionBar">false</item>
    </style>
```
### 四、混淆
#### 1、IjkMediaPlayer
```
    # IjkPlayer
    -keep class tv.danmaku.ijk.** { *; }
    -dontwarn tv.danmaku.ijk.**
```
#### 2、ExoPlayer
```
    # ExoPlayer
    -keep class com.google.android.exoplayer2.** { *; }
    -dontwarn com.google.android.exoplayer2.**
```
#### 更多文档持续更新中...