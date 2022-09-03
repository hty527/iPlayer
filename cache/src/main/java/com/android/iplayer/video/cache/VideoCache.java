package com.android.iplayer.video.cache;

import android.content.Context;
import com.android.iplayer.video.cache.task.PreloadTask;
import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.StorageUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by hty
 * 2022/9/3
 * Desc:视频文件预缓存预缓存、缓存
 */
public class VideoCache {

//    private static final String TAG = "VideoCache";
    private volatile static VideoCache mInstance;
    private HttpProxyCacheServer mCacheServer;//本地缓存服务器代理人
    //缓存目录下的最大缓存长度，单位：字节,默认为512M
    private long mMaxSize =512 * 1024 * 1024;
    //缓存目录,不设置则默认在sd_card/Android/data/[app_package_name]/cache中
    private File mCacheDirectory;
    //单个视频文件的预缓存大小，单位:字节，默认1M
    private int mPreloadLength = 1024 * 1024;
    //标识是否需要预加载
    private boolean mIsStartPreload = true;
    //单线程池，按照添加顺序依次执行#startPreloadTask
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    //保存正在预加载的#PreloadTask
    private LinkedHashMap<String, PreloadTask> mPreloadTasks = new LinkedHashMap<>();

    //调用入口
    public static synchronized VideoCache getInstance() {
        synchronized (VideoCache.class) {
            if (null == mInstance) {
                mInstance = new VideoCache();
            }
        }
        return mInstance;
    }

    private Context getThreadContext() {
        try {
            Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
            Method method = ActivityThread.getMethod("currentActivityThread");
            Object currentActivityThread = method.invoke(ActivityThread);
            Method method2 = currentActivityThread.getClass().getMethod("getApplication");
            return (Context)method2.invoke(currentActivityThread);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 设置缓存目录，在初始化|使用预缓存|调用{@link #getPlayPreloadUrl(String)}之前调用
     * @param cacheDirectory 路径，不设置默认在sd_card/Android/data/[app_package_name]/cache中
     */
    public VideoCache setCacheDirectory(File cacheDirectory) {
        this.mCacheDirectory = cacheDirectory;
        return mInstance;
    }

    /**
     * 设置mCacheDirectory目录下缓存总大小，在初始化|使用预缓存|调用{@link #getPlayPreloadUrl(String)}之前调用
     * @param maxSize 单位：字节,默认512M
     */
    public VideoCache setMaxSize(long maxSize) {
        this.mMaxSize = maxSize;
        return mInstance;
    }

    /**
     * 设置预加载的单个视频长度大小，在初始化|使用预缓存|调用{@link #getPlayPreloadUrl(String)}之前调用
     * @param preloadLength 单位：字节，默认每个视频预加载1M
     */
    public VideoCache setPreloadLength(int preloadLength) {
        this.mPreloadLength = preloadLength;
        return mInstance;
    }

    /**
     * 初始化缓存器，可在application中初始化
     * @param context
     */
    public void initCache(Context context){
        initCache(context, mMaxSize,mCacheDirectory);
    }

    /**
     * 初始化缓存器，可在application中初始化
     * @param context 全局上下文
     * @param maxSize 缓存目录的最大缓存大小，单位：字节
     * @param cacheDirectory 缓存目录，不设置默认在sd_card/Android/data/[app_package_name]/cache中
     */
    public void initCache(Context context, long maxSize, File cacheDirectory){
        if(null==mCacheServer){
            mCacheDirectory=cacheDirectory;
            creatProxy(context,maxSize,cacheDirectory);
        }
    }

    private HttpProxyCacheServer creatProxy(Context context,long maxSize,File cacheDirectory) {
        return mCacheServer == null ? (mCacheServer = newProxy(context,maxSize,cacheDirectory)) : mCacheServer;
    }

    private HttpProxyCacheServer newProxy(Context context,long maxSize,File cacheDirectory) {
        if(null!=cacheDirectory){
            return new HttpProxyCacheServer.Builder(context)
                    .maxCacheSize(maxSize>0?maxSize:512 * 1024 * 1024)       // 512MB for cache
                    //缓存路径，不设置默认在sd_card/Android/data/[app_package_name]/cache中
                    .cacheDirectory(cacheDirectory)
                    .build();
        }else{
            return new HttpProxyCacheServer.Builder(context)
                    .maxCacheSize(maxSize)       // 512MB for cache
                    .build();
        }
    }

    /**
     * 返回缓存代理人
     * @return
     */
    public HttpProxyCacheServer getProxy(){
        return creatProxy(getThreadContext(),mMaxSize,mCacheDirectory);
    }

    /**
     * 删除所有缓存文件
     * @return 返回缓存是否删除成功
     */
    public boolean clearAllCache() {
        return StorageUtils.deleteFiles(getProxy().getCacheRoot());
    }

    /**
     * 删除url对应默认缓存文件
     * @param rawUrl 原始视频文件地址
     * @return 返回缓存是否删除成功
     */
    public boolean clearDefaultCache(String rawUrl) {
        File pathTmp = getProxy().getTempCacheFile(rawUrl);
        File path = getProxy().getCacheFile(rawUrl);
        return StorageUtils.deleteFile(pathTmp.getAbsolutePath()) &&
                StorageUtils.deleteFile(path.getAbsolutePath());
    }

    /**
     * 根据源视频地址获取本地代理地址
     * @param rawUrl 源视频地址
     * @return 本地代理地址，以127.0.0.1开头的代理地址
     */
    public String getPlayUrl(String rawUrl) {
        return getPlayUrl(null,rawUrl);
    }

    /**
     * 根据源视频地址获取本地预缓存代理地址
     * @param context 上下文
     * @param rawUrl 源视频地址
     * @return 本地代理地址，以127.0.0.1开头的代理地址
     */
    public String getPlayUrl(Context context,String rawUrl) {
        if(null==mCacheServer&&null!=context){
            initCache(context);
        }
        PreloadTask task = mPreloadTasks.get(rawUrl);
        if (task != null) {
            task.cancel();
        }
        return getProxy().getProxyUrl(rawUrl);
    }

    //===========================================预缓存处理===========================================

    /**
     * 开始预加载，适合任何场景下调用
     * @param rawUrl 原始视频地址
     */
    public void startPreloadTask(String rawUrl) {
        startPreloadTask(rawUrl,mPreloadLength);
    }

    /**
     * 开始预加载，适合列表场景、类似抖音的连续片段播放场景
     * @param rawUrl 原始视频地址
     * @param preloadLength 预缓存此视频期望预加载的长度大小，单位字节，不指定的话默认是1024*1024=1M
     */
    public void startPreloadTask(String rawUrl, int preloadLength) {
        startPreloadTask(rawUrl,-1, preloadLength);
    }

    /**
     * 开始预加载
     * @param rawUrl 原始视频地址
     * @param position 当前item在列表中的position，当正在播放的item发生了变化，则应该根据这个position取消预缓存,默认是-1，非列表
     * @param preloadLength 预缓存此视频期望预加载的长度大小，单位字节，不指定的话默认是1024*1024=1M
     */
    public void startPreloadTask(String rawUrl, int position, int preloadLength) {
        if (isPreloaded(rawUrl)) return;
        if(preloadLength>0) this.mPreloadLength =preloadLength;
//        Log.d(TAG,"startPreloadTask-->position:" + position+",preloadLength:"+preloadLength);
        PreloadTask task = new PreloadTask(rawUrl,position,preloadLength);
        mPreloadTasks.put(rawUrl, task);
        if (mIsStartPreload) {
            //开始预加载
            task.executeOn(mExecutorService);
        }
    }

    /**
     * 判断该播放地址是否已经预加载
     * @param rawUrl 原始视频地址
     * @return true:预缓存成功,false:预缓存不存在
     */
    public boolean isPreloaded(String rawUrl) {
        //先判断是否有缓存文件，如果已经存在缓存文件，并且其大小大于1KB，则表示已经预加载完成了
        File cacheFile = getProxy().getCacheFile(rawUrl);
        if (cacheFile.exists()) {
            if (cacheFile.length() >= 1024) {
                return true;
            } else {
                //这种情况一般是缓存出错，把缓存删掉，重新缓存
                cacheFile.delete();
                return false;
            }
        }
        //再判断是否有临时缓存文件，如果已经存在临时缓存文件，并且临时缓存文件超过了预加载大小，则表示已经预加载完成了
        File tempCacheFile = getProxy().getTempCacheFile(rawUrl);
        if (tempCacheFile.exists()) {
            return tempCacheFile.length() >= mPreloadLength;
        }
        return false;
    }

    /**
     * 判断该播放地址是否已经全量缓存，这个和预缓存概念不一样，预缓存是缓存期望缓存的部视频长度大小
     * @param rawUrl 原始视频地址
     * @return true:缓存成功,false:缓存不存在
     */
    public boolean isCache(String rawUrl) {
        return getProxy().isCached(rawUrl);
    }

    /**
     * 暂停预加载，适合任何场景下调用
     */
    public void pausePreload() {
//        Log.d(TAG,"pausePreload-->");
        mIsStartPreload = false;
        try {
            for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
                PreloadTask task = next.getValue();
                task.cancel();
            }
        }catch (Throwable e){

        }
    }

    /**
     * 暂停预加载，适合列表场景、类似抖音的连续片段播放场景
     * 根据是否反向滑动取消在position之下或之上的PreloadTask
     * @param position 当前滑到的列表位置
     * @param isReverseScroll 列表是否反向滑动
     */
    public void pausePreload(int position, boolean isReverseScroll) {
//        Log.d(TAG,"pausePreload-->position:" + position+",isReverseScroll:"+isReverseScroll);
        mIsStartPreload = false;
        try {
            for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
                PreloadTask task = next.getValue();
                if (isReverseScroll) {
                    if (task.getPosition() >= position) {
                        task.cancel();
                    }
                } else {
                    if (task.getPosition() <= position) {
                        task.cancel();
                    }
                }
            }
        }catch (Throwable e){

        }
    }

    /**
     * 恢复预加载，适合任何场景下调用
     * 根据是否反向滑动开始在position之下或之上的PreloadTask
     */
    public void resumePreload() {
//        Log.d(TAG,"resumePreload-->");
        mIsStartPreload = true;
        try {
            for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
                PreloadTask task = next.getValue();
                if (!isPreloaded(task.getRawUrl())) {
                    task.executeOn(mExecutorService);
                }
            }
        }catch (Throwable e){

        }
    }

    /**
     * 恢复预加载，适合列表场景、类似抖音的连续片段播放场景
     * 根据是否反向滑动开始在position之下或之上的PreloadTask
     * @param position        当前滑到的位置
     * @param isReverseScroll 列表是否反向滑动
     */
    public void resumePreload(int position, boolean isReverseScroll) {
//        Log.d(TAG,"resumePreload-->position:" + position+",isReverseScroll:"+isReverseScroll);
//        mIsStartPreload = true;
        try {
            for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
                PreloadTask task = next.getValue();
                if (isReverseScroll) {
                    if (task.getPosition() < position) {
                        if (!isPreloaded(task.getRawUrl())) {
                            task.executeOn(mExecutorService);
                        }
                    }
                } else {
                    if (task.getPosition() > position) {
                        if (!isPreloaded(task.getRawUrl())) {
                            task.executeOn(mExecutorService);
                        }
                    }
                }
            }
        }catch (Throwable e){

        }
    }

    /**
     * 通过原始地址取消预加载
     * @param rawUrl 原始地址
     */
    public void removePreloadTask(String rawUrl) {
        PreloadTask task = mPreloadTasks.get(rawUrl);
        if (task != null) {
            task.cancel();
            mPreloadTasks.remove(rawUrl);
        }
    }

    /**
     * 结束所有的预加载
     */
    public void stopllPreloadTask() {
        removeAllPreloadTask();
    }

    /**
     * 取消所有的预加载
     */
    public void removeAllPreloadTask() {
        try {
            Iterator<Map.Entry<String, PreloadTask>> iterator = mPreloadTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PreloadTask> next = iterator.next();
                PreloadTask task = next.getValue();
                task.cancel();
                iterator.remove();
            }
        }catch (Throwable e){

        }
    }

    /**
     * 根据源视频地址获取本地代理地址
     * @param rawUrl 源视频地址
     * @return 本地代理地址，以127.0.0.1开头的代理地址
     */
    public String getPlayPreloadUrl(String rawUrl) {
        return getPlayPreloadUrl(null,rawUrl);
    }

    /**
     * 根据源视频地址获取本地预缓存代理地址
     * @param context 上下文
     * @param rawUrl 源视频地址
     * @return 本地代理地址，以127.0.0.1开头的代理地址
     */
    public String getPlayPreloadUrl(Context context,String rawUrl) {
        if(null==mCacheServer&&null!=context){
            initCache(context);
        }
        PreloadTask task = mPreloadTasks.get(rawUrl);
        if (task != null) {
            task.cancel();
        }
        if (isPreloaded(rawUrl)) {
            return getProxy().getProxyUrl(rawUrl);
        } else {
            return rawUrl;
        }
    }
}