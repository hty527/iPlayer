package com.android.iplayer.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created by hty
 * 2022/6/28
 * Desc:线程管理器
 */
public class ThreadPool {

    private volatile static ThreadPool mInstance;
    private ExecutorService mCachedThreadPool;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(CPU_COUNT, 5); // 至少允许5个线程
    private static final int THREAD_KEEP_LIVE_TIME = 30; // 线程如果30秒不用，允许超时
    private static final int TASK_QUEUE_MAX_COUNT = 128;

    private ThreadPoolExecutor mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
            CORE_POOL_SIZE,
            THREAD_KEEP_LIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(TASK_QUEUE_MAX_COUNT));

    public Handler mMainHandler = new Handler(Looper.getMainLooper());


    public static synchronized ThreadPool getInstance() {
        synchronized (ThreadPool.class) {
            if (null == mInstance) {
                mInstance = new ThreadPool();
            }
        }
        return mInstance;
    }

    public void run(Runnable runnable){
        if(null==mCachedThreadPool){
            //newCachedThreadPool：创建一个可缓存线程池，如果线程池长度超过处理需求，可以灵活回收空闲线程，若无可回收则新建线程
            //newFixedThreadPool：创建一个定长线程池，可以控制线程最大并发数，超过的线程会在队列中等待
            //newScheduledThreadPool：创建一个定长线程池，支持定时及周期性任务执行
            //newSingleThreadExecutor：创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序执行
            mCachedThreadPool = Executors.newSingleThreadExecutor();
        }
        mCachedThreadPool.execute(runnable);
    }

    public void stop(){
        try {
            if(null!=mCachedThreadPool){
                mCachedThreadPool.shutdown();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    public void reset(){
        try {
            if(null!=mMainHandler){
                mMainHandler.removeCallbacksAndMessages(null);
            }
            if(null!=mCachedThreadPool){
                mCachedThreadPool.shutdown();
                mCachedThreadPool=null;
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    /**
     * 在子线程中运行任务
     *
     * @param runnable
     */
    public void runOnThreadPool(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    /**
     * 在主线程中运行任务
     *
     * @param runnable
     */
    public void runOnUIThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
        }
    }

    /**
     * 在子线程中切主线程运行任务（切两次线程，先切子线程，再在子线程中切主线程）
     *
     * @param runnable
     */
    public void runOnUIThreadByThreadPool(final Runnable runnable) {
        if(null==runnable) return;
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mMainHandler.post(runnable);
            }
        });
    }

    /**
     * 在子线程中运行任务
     *
     * @param callable
     * @return
     */
    public <T> Future<T> runOnThreadPool(Callable<T> callable) {
        return mThreadPoolExecutor.submit(callable);
    }
}