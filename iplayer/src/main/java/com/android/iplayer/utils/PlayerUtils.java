package com.android.iplayer.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import com.android.iplayer.widget.view.LayoutProvider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * created by hty
 * 2022/6/28
 * Desc:工具类集合
 */
public class PlayerUtils {

    private static final String TAG = "PlayerUtils";
    private static PlayerUtils mInstance;

    public static synchronized PlayerUtils getInstance() {
        synchronized (PlayerUtils.class) {
            if (null == mInstance) {
                mInstance = new PlayerUtils();
            }
        }
        return mInstance;
    }

    public String stringForAudioTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 返回设备是否连接至WIFI网络
     * @param context context
     * @return if wifi is connected,return true
     */
    public boolean isWifiConnected(Context context) {
        if(existPermission(getContext(), Manifest.permission.ACCESS_NETWORK_STATE)){
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 返回设备是否连接至移动网络
     * @param context context
     * @return if wifi is connected,return true
     */
    public boolean isMobileConnected(Context context) {
        if(isCheckNetwork()&&!isWifiConnected(context)){
            return true;
        }
        return false;
    }

    /**
     * 检查设备是否已连接至可用网络
     * @return
     */
    public boolean isCheckNetwork() {
        Context context = getContext();
        if(null!=context&&existPermission(getContext(), Manifest.permission.ACCESS_NETWORK_STATE)){
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo == null) {
                    return false;
                }
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_WIFI) {
                    return true;
                }
                return false;
            }catch (Throwable e){
                e.printStackTrace();
            }
            return true;
        }
        return true;
    }

    public boolean mobileNetwork(boolean isMobileNetwork) {
        if(isMobileConnected(getContext())){
            return isMobileNetwork;
        }
        return true;
    }

    /**
     * 获取上下文所在的Activity
     * 写播放器时遇到的问题：播放器被转场时，获取播放器的上下文是上一个Activity的上下文。
     * 解决办法:在接收转场的Activityg或Fragment或ViewGroup中调用BasePlayer的setTempContext(Context context)手动设置上下文,并在界面销毁时释放TempContext
     * @param context
     * @return
     */
    public Activity getActivity(Context context) {
        try {
            if (context == null) return null;
            if (context instanceof Activity) {
                return (Activity) context;
            } else if (context instanceof ContextWrapper) {
                return getActivity(((ContextWrapper) context).getBaseContext());
            } else if (context instanceof ContextThemeWrapper) {
                return getActivity(((ContextThemeWrapper) context).getBaseContext());
            }
            return null;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    //设备屏幕宽度
    public int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    //设备屏幕高度
    public int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 将dp转换成px
     * @param dp
     * @return
     */
    public float dpToPx(Context context, float dp) {
        return dp * context.getApplicationContext().getResources().getDisplayMetrics().density;
    }

    public int dpToPxInt(float dp) {
        return (int) (dpToPx(PlayerUtils.getInstance().getContext(),dp) + 0.5f);
    }

    /**
     * 获取应用的包名
     * @param context
     * @return
     */
    public String getPackageName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取底部虚拟按键的高度
     * @param context
     * @return
     */
    public int getNavigationHeight(Context context){
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        if(result<=0){
            result=dpToPxInt(24f);
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else {
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 检查虚拟按键是否被重写
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }

    /**
     * 反射获取Context
     * @return
     */
    public Context getContext() {
        try {
            Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
            Method method = ActivityThread.getMethod("currentActivityThread");
            Object currentActivityThread = method.invoke(ActivityThread);//获取currentActivityThread 对象
            Method method2 = currentActivityThread.getClass().getMethod("getApplication");
            return (Context)method2.invoke(currentActivityThread);//获取 Context对象
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检测资源地址是否是直播流
     * @param dataSource
     * @return
     */
    public boolean isLiveStream(String dataSource) {
        if(dataSource.isEmpty()) return false;
        if(dataSource.startsWith("htpp")||dataSource.startsWith("htpps")){
            if(dataSource.endsWith(".m3u8")||dataSource.endsWith(".hks")||dataSource.endsWith(".rtmp")){
                return true;
            }
        }
        return false;
    }

    /**
     * 将自己从父Parent中移除
     * @param view
     */
    public void removeViewFromParent(View view) {
        if(null!=view&&null!=view.getParent() && view.getParent() instanceof ViewGroup){
            try {
                ((ViewGroup) view.getParent()).removeView(view);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    public int parseInt(String content){
        return parseInt(content,0);
    }

    public int parseInt(String content, int defaultValue){
        if(TextUtils.isEmpty(content)) return defaultValue;
        try {
            return Integer.parseInt(content);
        }catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }

    public long parseLong(String content){
        if(TextUtils.isEmpty(content)) return 0;
        try {
            return Long.parseLong(content);
        }catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }

    public float parseFloat(String content){
        try {
            float parseFloat = Float.parseFloat(content);
            return parseFloat;
        }catch (RuntimeException e){
            e.printStackTrace();
            return 0;
        }
    }

    public double parseDouble(String progressStr, double defaultvalue) {
        try {
            return Double.parseDouble(progressStr);
        }catch (RuntimeException e){
            e.printStackTrace();
            return defaultvalue;
        }
    }

    /**
     * 根据包名检测是否申明某个权限
     * @param context
     * @param permission
     * @return
     */
    public boolean existPermission(Context context, String permission){
        if(TextUtils.isEmpty(permission)){
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = packageInfo.requestedPermissions;
            if(null!=requestedPermissions&&requestedPermissions.length>0){
                for (String requestedPermission : requestedPermissions) {
                    if(requestedPermission.equals(permission)){
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }catch (Throwable e){
            e.printStackTrace();
        }
        return false;
    }

    public Spanned formatHtml(String content) {
//        Logger.d(TAG,"content"+content);
        if(TextUtils.isEmpty(content)) return new SpannableString("");
        try {
            return Html.fromHtml(content);
        }catch (Throwable e){
            e.printStackTrace();
        }
        return new SpannableString(content);
    }

    /**
     * 给View设置圆角
     * @param view
     * @param radius
     */
    public void setOutlineProvider(View view,float radius) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setOutlineProvider(new LayoutProvider(radius));
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 检查是否需要网络播放
     * @param dataSource
     * @param assetsSource
     * @return
     */
    public boolean hasNet(String dataSource, AssetFileDescriptor assetsSource) {
        if(null!=assetsSource){
            return false;
        }
        if(TextUtils.isEmpty(dataSource)){
            return false;
        }
        if(dataSource.startsWith("file")||dataSource.startsWith("android")){
            return false;
        }
        if(dataSource.contains("127.0.0.1")){
            return false;
        }
        return true;
    }

    /**
     * 获取当前系统时间
     * @return
     */
    public String getCurrentTimeStr() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    /**
     * 根据百分比计算出实际占总进度的进度值
     * @param bufferPercent 百分比
     * @param durtion 总数
     * @return
     */
    public int formatBufferPercent(int bufferPercent, long durtion) {
        if(bufferPercent<=0) return 0;
        if(durtion<=0) return 100;
        try {
            return (int) (durtion/100)*bufferPercent;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * @param context
     * @return true 允许  false禁止
     */
    public boolean checkWindowsPermission(Context context) {
        if(!existPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW)){
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    return true;
                }
                return false;
            }
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 用户手指在屏幕边缘检测
     * @param context 上下文
     * @param e 触摸事件
     * @return
     */
    public boolean isEdge(Context context, MotionEvent e) {
        float edgeSize = dpToPx(context, 40);
        ILogger.d(TAG,"isEdge-->eX:"+e.getRawX()+",eY:"+e.getRawY()+",screenWidt:"+getScreenWidth(context));
        return e.getRawX() < edgeSize
                || e.getRawX() > getScreenWidth(context) - edgeSize
                || e.getRawY() < edgeSize
                || e.getRawY() > getScreenHeight(context) - edgeSize;
    }
}