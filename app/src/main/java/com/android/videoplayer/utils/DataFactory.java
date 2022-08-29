package com.android.videoplayer.utils;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.android.iplayer.utils.PlayerUtils;
import com.android.videoplayer.App;
import com.android.videoplayer.R;
import com.android.videoplayer.bean.Menu;
import com.android.videoplayer.bean.Version;
import com.android.videoplayer.pager.bean.VideoBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * created by hty
 * 2022/6/29
 * Desc:数据生产
 */
public class DataFactory {

    private static final String TAG="DataFactory";
    private volatile static DataFactory mInstance;
    private Handler mHandler=new Handler(Looper.getMainLooper());

    public static DataFactory getInstance() {
        if(null==mInstance){
            synchronized (DataFactory.class) {
                if (null == mInstance) {
                    mInstance = new DataFactory();
                }
            }
        }
        return mInstance;
    }

    /**
     * 从资源string中获取文字返回
     * @param id 源字符串ID
     * @param defaultStr 源字符串
     * @return
     */
    public String getString(int id,String defaultStr){
        Context context = PlayerUtils.getInstance().getContext();
        if(null!=context){
            return context.getResources().getString(id);
        }
        return defaultStr;
    }

    public List<Menu> getMenus() {
        List<Menu> menus=new ArrayList<>();
        menus.add(new Menu(getString(R.string.text_item_sdk,"SDK默认播放器示例"),1,getString(R.string.text_item_sub_noimal,"基础"),0));
        menus.add(new Menu(getString(R.string.text_item_live,"直播拉流"),2,null,2));
        menus.add(new Menu(getString(R.string.text_item_videos,"多播放器同时播放"),3,null,2));
        menus.add(new Menu(getString(R.string.text_item_full,"直接全屏播放"),4,null,2));
        menus.add(new Menu(getString(R.string.text_item_perview,"收费试看模式"),5,null,2));
        menus.add(new Menu(getString(R.string.text_item_resouce,"Assets/Raw本地资源播放"),6,null,2));
        menus.add(new Menu(getString(R.string.text_item_continuity,"连续播放一个视频列表"),7,null,1));
        menus.add(new Menu(getString(R.string.text_item_auto,"列表自动播放(无缝转场)"),8,getString(R.string.text_item_sub_list,"列表"),0));
        menus.add(new Menu(getString(R.string.text_item_click,"列表点击播放(无缝转场)"),9,null,1));
        menus.add(new Menu(getString(R.string.text_item_window,"Activity悬浮窗"),10,getString(R.string.text_item_sub_window,"窗口"),0));
        menus.add(new Menu(getString(R.string.text_item_goable_window,"全局悬浮窗"),11,null,2));
        menus.add(new Menu(getString(R.string.text_item_window_open,"任意界面开启窗口播放器"),12,null,2));
        menus.add(new Menu(getString(R.string.text_item_window_goable_open,"任意界面开启全局悬浮窗播放器"),13,null,2));
        menus.add(new Menu(getString(R.string.text_item_dip,"画中画(Android8.0+)"),14,null,1));
        menus.add(new Menu(getString(R.string.text_item_dy,"仿抖音(扩展示例)"),15,getString(R.string.text_item_sub_expand,"扩展"),0));
        menus.add(new Menu(getString(R.string.text_item_danmu,"自定义弹幕控制器(扩展示例)"),16,null,1));
        menus.add(new Menu(getString(R.string.text_item_home,"项目主页"),17,getString(R.string.text_item_sub_other,"其它"),3));
        Menu menu = new Menu("", 101, getString(R.string.text_item_version,"版本预告"), 3, 1);
        Version version=new Version();
        version.setCode("2.0.xx");
        version.setTime(getString(R.string.text_item_time,"待定,请持续关注"));
        version.setDescript(getString(R.string.text_item_desc,"1、新增重力旋转功能，支持重力旋转开关及阻尼设置"));
        menu.setVersion(version);
        menus.add(menu);
        return menus;
    }

    /**
     * 返回测试的弹幕数据
     * @return
     */
    public List<String> getDanmus(){
        List<String> danmu=new ArrayList<>();
        danmu.add("又把我们当外人了");
        danmu.add("哪里会有这样的人");
        danmu.add("为什么帅哥都在理发店，而美女却都在红灯区?");
        danmu.add("叫你兄弟武松来啊");
        danmu.add("爽了！");
        danmu.add("女人的腰，夺命的刀");
        danmu.add("兄弟想必是练家子");
        danmu.add("好看…好看….");
        danmu.add("保安 ！保安 ！保安哪？");
        danmu.add("你现在不努力当你四五十岁的时候户口本都翻不了页");
        danmu.add("如果是有腰子之前，我一定给你好好说说");
        danmu.add("这个操作也是厉害了，实在是我想不到的");
        danmu.add("对面收到的永远和这边给出的不一样");
        danmu.add("我一看这个姿态就是日本的宅男吧，太可爱了");
        danmu.add("你懂我的图谋不轨，我懂你的故作矜持。");
        danmu.add("夏日有迟暮的霞光，却没有她晚来的微笑");
        danmu.add("把屏幕关闭，你会发现一个帅气的脸");
        danmu.add("看封面我就已经知道结局了");
        danmu.add("女朋友可以，夫人不能");
        danmu.add("来啦٩(๑òωó๑)۶");
        danmu.add("经常都来看你~");
        danmu.add("虽然没看懂，还是很赞的感觉");
        danmu.add("学习学习");
        danmu.add("这个好，不错！");
        danmu.addAll(danmu);
        return danmu;
    }

    /**
     * 返回临时的视频列表
     * @return
     */
    public List<VideoBean> getVideoList() {
        String str = pasterAssetsByName(App.getInstance().getContext(), "videos");
        if(!TextUtils.isEmpty(str)){
            List<VideoBean> list = new Gson().fromJson(str,new TypeToken<List<VideoBean>>(){}.getType());
            return list;
        }
        return null;
    }

    /**
     * 返回临时的抖音视频列表数据
     * @return
     */
    public void getTikTopVideo(OnCallBackListener listener){
        this.mOnCallBackListener=listener;
        if(null==mOnCallBackListener) return;
        new Thread(){
            @Override
            public void run() {
                super.run();
                //String str = pasterAssetsByPath(App.getInstance().getContext(), "tiktok.json");
                String str = pasterAssetsByName(App.getInstance().getContext(), "tiktok");
                if(!TextUtils.isEmpty(str)){
                    final List<VideoBean> list = new Gson().fromJson(str,new TypeToken<List<VideoBean>>(){}.getType());
                    if(null!=mOnCallBackListener&&null!=mHandler){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnCallBackListener.onList(list);
                            }
                        });
                    }
                }else{
                    if(null!=mOnCallBackListener&&null!=mHandler){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnCallBackListener.onList(null);
                            }
                        });
                    }
                }
            }
        }.start();
    }

    public List<String> getDataSources(){
        List<String> list=new ArrayList<>();
        list.add("http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4");
        list.add("http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4");
//        list.add("http://vfx.mtime.cn/Video/2019/03/19/mp4/190319222227698228.mp4");
//        list.add("http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4");
        return list;
    }

    public interface OnCallBackListener{
        void onList(List<VideoBean> data);
    }

    private OnCallBackListener mOnCallBackListener;


    /**
     * 根据文件名称格式化资产目录下json文件,这个速度明显快于pasterAssetsByPath方法的速度
     * @param context
     * @param fileName
     * @return
     */
    private String pasterAssetsByName(Context context, String fileName){
        try {
            InputStream is = context.getAssets().open(fileName);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            is.close();
            String result = new String(buffer, Charset.forName("UTF-8"));
            return result;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据文件地址格式化资产目录下json文件
     * @param context
     * @param filePath
     * @return
     */
    private String pasterAssetsByPath(Context context, String filePath) {
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            InputStream inputStream = context.getAssets().open(filePath);
            inputReader = new InputStreamReader(inputStream);
            bufReader = new BufferedReader(inputReader);
            String line;
            String Result = "";
            while ((line = bufReader.readLine()) != null) {
                Result += line;
            }
            bufReader.close();
            inputReader.close();
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            if (null != bufReader) {
                try {
                    bufReader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (null != inputReader) {
                try {
                    inputReader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    public void writeLog(final String content) {
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    File filePath = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator,"Json");
//                    if(!filePath.exists()){
//                        filePath.mkdirs();
//                    }
//                    Logger.d(TAG,"writeLog-->"+filePath.exists());
//                    try {
//                        FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath,"list.txt"));
//                        byte[] bytes = content.getBytes();
//                        try {
//                            fileOutputStream.write(bytes);
//                            if (fileOutputStream != null) {
//                                fileOutputStream.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }catch (Throwable e){
//                    e.printStackTrace();
//                }
//            }
//        }.start();
    }

    public void copyString(Context context,String identify) {
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(identify);
        }catch (RuntimeException e){

        }
    }
}