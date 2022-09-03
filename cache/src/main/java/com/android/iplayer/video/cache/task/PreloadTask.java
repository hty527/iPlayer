package com.android.iplayer.video.cache.task;

import android.util.Log;
import com.android.iplayer.video.cache.VideoCache;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * created by hty
 * 2022/7/11
 * Desc:预缓存Task
 * 原理：主动去请求VideoCache生成的代理地址，触发VideoCache缓存机制
 * 缓存到 PreloadManager.PRELOAD_LENGTH 的数据之后停止请求，完成预加载
 * 播放器去播放VideoCache生成的代理地址的时候，VideoCache会直接返回缓存数据，
 * 从而提升播放速度
 */
public class PreloadTask implements Runnable {

    private static final String TAG = "PreloadTask";
    //原始视频地址
    private String mRawUrl;
    //列表中的位置,如果是非列表场景，则是-1
    private int mPosition=-1;
    //是否被取消
    private boolean mIsCanceled;
    //是否正在预加载
    private boolean mIsExecuted;
    //预缓存大小
    private int mPreloadLength=1024*1024;
    //暂停池子
    private final static List<String> sBlackList = new ArrayList<>();

    private PreloadTask(){}

    public PreloadTask(String rawUrl){
        this(rawUrl,-1);
    }

    public PreloadTask(String rawUrl,int position){
        this.mRawUrl=rawUrl;
        this.mPosition=position;
    }

    public PreloadTask(String rawUrl,int position,int preloadLength){
        this.mRawUrl=rawUrl;
        this.mPosition=position;
        this.mPreloadLength=preloadLength;
    }

    @Override
    public void run() {
        if (!mIsCanceled) {
            start();
        }
        mIsExecuted = false;
        mIsCanceled = false;
    }

    /**
     * 开始预加载
     */
    private void start() {
        // 如果在小黑屋里不加载
        if (sBlackList.contains(mRawUrl)) return;
//        Log.d(TAG,"start-->mPosition:"+mPosition);
        HttpURLConnection connection = null;
        try {
            //获取HttpProxyCacheServer的代理地址
            String proxyUrl = VideoCache.getInstance().getProxy().getProxyUrl(mRawUrl);
            URL url = new URL(proxyUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            int length;
            int read = -1;
            byte[] bytes = new byte[8 * 1024];
            while ((length = in.read(bytes)) != -1) {
                read += length;
                //预加载完成或者取消预加载
                if (mIsCanceled || read >= mPreloadLength) {
                    if (mIsCanceled) {
                        Log.d(TAG,"预缓存取消:position:"+mPosition+",Byte:"+read);
                    } else {
                        Log.d(TAG,"预缓存成功:position:"+mPosition+",Byte:"+read);
                    }
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG,"预缓存异常:position:"+mPosition+",error:"+ e.getMessage());
            // 关入小黑屋
            sBlackList.add(mRawUrl);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            Log.d(TAG,"预缓存结束:position:"+mPosition);
        }
    }

    /**
     * 将预加载任务提交到线程池，准备执行
     */
    public void executeOn(ExecutorService executorService) {
        if (mIsExecuted) return;
        mIsExecuted = true;
        executorService.submit(this);
    }

    public String getRawUrl() {
        return mRawUrl;
    }

    public int getPosition() {
        return mPosition;
    }

    /**
     * 取消预加载任务
     */
    public void cancel() {
        if (mIsExecuted) {
            mIsCanceled = true;
        }
    }
}
