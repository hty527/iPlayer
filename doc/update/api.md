## 文档和功能介绍
### 一、常用功能使用说明
#### 1、播放器功能API
* 1.1、请阅读IVideoPlayerControl接口类
#### 2、控制器功能API
* 2.1、请阅读IVideoController接口类
#### 3、交互组件API
* 3.1、请阅读IControllerView接口类
#### 4、自定义解码器
* 4.1、SDK默认使用MediaPlayer解码器，Demo中示例了两套自定义解码器的使用,请参考：JkMediaPlayer\ExoMediaPlayer
```
    /**
     * 如果使用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
     */
    mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {

        @Override
        public AbstractMediaPlayer createMediaPlayer() {
            if(1==MEDIA_CORE){
                return new JkMediaPlayer(VideoPlayerActivity.this);//IJK解码器
            }else if(2==MEDIA_CORE){
                return new ExoMediaPlayer(VideoPlayerActivity.this);//EXO解码器
            }else{
                return null;//返回null时,SDK内部会自动使用系统MediaPlayer解码器,自定义解码器请参考Demo中的JkMediaPlayer或ExoMediaPlayer类
            }
        }
    });
```
#### 5、自定义UI交互组件
##### 5.1、自定义Controller
* 5.1.1、继承BaseController实现自己的控制器，如需手势交互，请继承GestureController
* 5.1.2、设置控制器到播放器

```
    VideoController controller=new VideoController(videoPlayer.getContext());
    mVideoPlayer.setController(controller);//将控制器绑定到播放器
    //或使用SDK内置的
    mVideoPlayer.initController();
```
##### 5.2、自定义UI交互组件
* 5.2.1、为什么有自定义Controller还要整个"自定义UI交互组件"出来？<br>
因为Controller在处理交互比较复杂或功能比较多的场景下耦合性太高，于是Controller就拓展了自定义UI交互组件能力，可根据需要来添加自己的UI交互组件和任意自定义单个交互模块的UI组件。
* 5.2.2、SDK提供了一套标题栏、底部控制栏、播放器状态(网络提示、播放失败)、播放完成、手势交互相应处理、Window窗口、列表模式 等UI交互组件。Controller的任意UI交互组件均支持自定义。
如需自定义请继承BaseControllerWidget，请阅读IControllerView提供的API来实现自己的交互。
* 5.2.3、代码示例：
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
    //将自定义交互组件添加到控制器
    mController.addControllerWidget(toolBarView,functionBarView,gestureView,completionView,statusView,loadingView,windowView);
```
##### 5.3、使用自定义组件对象
* 5.3.1、 在列表跳转到Activity转场播放、开启悬浮窗后转场到Activity等场景下，需要找到此前初始化播放器时设置的自定义交互UI组件。如下所示：
```
    //1、在添加控制器时，可调用BaseController的addControllerWidget(IControllerView controllerView,String target)两参方法
    //   或在addControllerWidget之前设置Target，如：
    ControlToolBarView controlToolBarView=new ControlToolBarView(this);
    controlToolBarView.setTarget(“target”);
    mController.addControllerWidget(controlToolBarView,"toolBar");
    //2、在需要使用此前添加的UI组件地方：
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
##### 5.4、找到SDK自带UI交互组件
* 5.4.1、在调用mVideoPlayer.initController();时，SDK内部会为添加的每一个自定义UI组件绑定一个target，所有的target请阅读：IVideoController类。找到某个组件对象方法如下：
```
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
* SDK控制器内置5种场景，分别是：0：常规状态(包括竖屏、横屏)，1：activity小窗口，2：全局悬浮窗窗口，3：列表，4：Android8.0的画中画。 
###### 5.5.2、场景使用
* 当场景变化时会回调到IVideoController的onPlayerScene(int playerScene)和IControllerView的onPlayerScene(int playerScene)方法。开发者可在控制器或UI交互组件中根据场景的变化做相应的UI交互变更。
###### 5.5.3、自定义场景
* 为方便播放器应用于不同场景，SDK支持自定义场景设置，用户可根据自己的场景来实现不同的UI交互。
```
    //更新播放器\控制器所在场景,调用此方法后控制器和所有UI组件都会收到onPlayerScene(int playerScene)回调
    controller.setPlayerScene(IVideoController.SCENE_NOIMAL);
```
#### 6、全屏播放
##### 6.1、横竖屏切换
* 6.1.1、支持横竖屏切换播放，需在AndroidManifest中所在的Activity申明如下属性：
```
    android:configChanges="orientation|screenSize"
```
##### 6.2、直接启动全屏播放
* 6.2.1、SDK支持在任意位置直接启动全屏播放,如：
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
#### 7、窗口播放
##### 7.1、Activity级别悬浮窗
* 7.1.1、Activity级别悬浮窗无需悬浮窗权限，可在任意位置启动Activity级别悬浮窗窗口播放。
```
    //startWindow支持多参传入，请阅读api。参数1：给窗口设置一个圆角，参数2：给窗口设置一个背景色
    mVideoPlayer.startWindow(ScreenUtils.getInstance().dpToPxInt(3f), Color.parseColor("#99000000"));
```
##### 7.2、全局悬浮窗
###### 7.2.1、悬浮窗开启
* 7.2.1.1、全局悬浮窗需要SYSTEM_ALERT_WINDOW权限，SDK内部会自动检测和申请SYSTEM_ALERT_WINDOW权限。可在任意位置开启悬浮窗窗口播放。
``` 
    //1、申明SYSTEM_ALERT_WINDOW权限
    <!--如您的播放器需要支持全局悬浮窗窗口播放请申明此权限-->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

    //2、开启全局悬浮窗窗口播放，startGlobalWindow支持多参传入，请阅读api。参数1：给窗口设置一个圆角，参数2：给窗口设置一个背景色
    boolean globalWindow = mVideoPlayer.startGlobalWindow(ScreenUtils.getInstance().dpToPxInt(3), Color.parseColor("#99000000"));
```
###### 7.2.2、悬浮窗点击
* 7.2.2.1、SDK内部提供了监听悬浮窗窗口播放器的关闭、点击回调监听器
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
* 7.2.2.2、如果设置了setOnWindowActionListener需要在收到onClose回调后关闭悬浮窗，未设置SDK内部会自定关闭。
#### 8、多播放器同时播放
* 8.1、SDK内部默认不支持并发播放，如需支持多播放器同时工作，需要设置允许播放器同时播放。
```
    //告诉播放器忽略音频焦点丢失事件，播放器内部即可支持多播放器同时播放
    IVideoManager.getInstance().setInterceptTAudioFocus(false);
```
#### 9、直播拉流
* 9.1、SDK内部自带的系统MediaPlayer对直播流的拓展仅限于.m3u8格式，如需支持更多的直播流视频格式，请自定义解码器拓展。直播流相关请参考LivePlayerActivity类
#### 10、收费试看模式
* 10.1、SDK默认Controller支持试看模式，分两步实现：
```
    VideoController controller = mVideoPlayer.initController();
    //1、设置虚拟的视频总时长,即可开启试看模式
    controller.setPreViewTotalDuration(DURATION+"");//注意:设置虚拟总时长(一旦设置控制器部走片段试看流程)
    //2、添加自己的试看播放完成的交互UI组件
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
#### 11、连续播放
* 可参考Demo中的VideoListPlayerActivity类
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
                        mVideoPlayer.playOrPause();
                    }else{
                        Logger.d(TAG,"onPlayerState-->播放到最后一个了");
                    }
                }
            }
        }
    });
```
#### 12、转场衔接播放
###### 12.1、列表转场
* 12.1.1、列表转场衔接继续播放原理：
```
    1、点击跳转到新的界面时将播放器从父容器中移除，并保存到全局变量
    2、将全局变量播放器对象添加到新的ViewGroup容器
    3、回到列表界面时如果播放的视频源没有被切换,关闭当前Activity不要销毁播放器,将播放器从当前父容器中移除
    4、重新添加到列表界面的此前正在播放的item中的ViewGroup中
```
* 12.1.2、列表转场衔接继续播放实现：主要参考Demo中的ListPlayerChangedFragment、ListPlayerFragment、VideoDetailsActivity类
``` 
    1、开始播放：参考ListPlayerFragment类的startPlayer()方法，注意标记当前mCurrentPosition和mPlayerContainer
    2、点击item跳转：参考ListPlayerChangedFragment类的onItemClick()方法，跳转到新的Activity
    3、新的Activity接收播放器继续播放：参考VideoDetailsActivity类的initPlayer方法，根据mIsChange变量来确认是否处理转场播放。
    4、新的Activity销毁：新的Activity在关闭时如果播放器视频地址未被切换，则在onDestroy中不要销毁播放器，参考：VideoDetailsActivity类的onDestroy
    5、回到列表界面：如果处理了第4步，在回到列表界面时接收并处理播放器，参考：ListPlayerChangedFragment类的onActivityResult方法和ListPlayerFragment类的recoverPlayerParent方法
```
###### 12.2、悬浮窗转场
* 12.2.1、全局悬浮窗窗口播放器转场到新的Activity请参考：Demo WindowGlobalPlayerActivity类的initPlayer方法或VideoDetailsActivity类的initPlayer方法
#### 13、视频缓存
* Demo的“防抖音”模块支持视频缓存和秒播，请参考PagerPlayerAdapter类的 //开始预加载 和 //结束预加载
### 二、异常现象及注意点
#### 1、全屏、小窗口播放无效
* 从列表或全局悬浮窗窗口转场跳转到Activity衔接继续播放场景下全屏、小窗口播放时全屏功能、手势设置屏幕亮度等和Activity相关的功能会失效。是因为播放器在上一个Activity创建并被添加到上一个Activity 的Window中，此时全屏、小窗口、屏幕亮度的结果会反馈给上一个Activity，
解决办法：
```
    //给播放器设置一个ParentConttext 上下文
    mVideoPlayer.setParentContext(context);上下文传递当前Activity Context
```
#### 2、播放器还原到竖屏位置
* 2.1、播放器在开启全屏、小窗口播放后还原到竖屏状态时，可能会因为下面这种布局方式而产生错位问题
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
#### 3、混合语言兼容方案
##### 3.1、Flutter
* 3.1.1、将此播放器SDK应用到Android原生和Flutter混合开发使用时，会出现全屏功能、Activity级别小窗、屏幕亮度设置功能无法使用，解决办法：
```
    //给播放器设置一个ParentConttext 上下文
    mVideoPlayer.setParentContext(context);上下文传递当前混合语言所在的宿主Activity Context
```
#### 4、VideoController屏幕锁
##### 4.1、屏幕锁禁用
* 4.1.1、SDK内部提供的VideoController默认开启了屏幕锁交互，关闭方法：
```
    VideoController controller = new VideoController(mVideoPlayer.getContext());
    controller.showLocker(false);//禁止屏幕锁功能
```
##### 4.2、自定义屏幕锁交互
* 4.2.1、自定义Controller实现自己的屏幕锁交互
#### 更多文档持续更新中...