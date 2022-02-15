package com.rq.floatingbullet.widgets;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HandlerEngine<T> extends Thread{

    /**
     * 控制每个弹幕的间距
     */
    private static final int MAX_BULLET_SPACING = 200;

    /**
     * 每次循环间隔时间
     */
    private static final int MAX_LOOP_INTERVAL_TIME = 4000;

    /**
     * 弹幕执行一次所需的时间
     */
    private static final long DURATION_MILLIS = 7500;

    private static final Object LOCK = new Object();

    private volatile boolean mQuit = false;

    private volatile boolean mPause = false;

    private BlockingQueue<T> handleQueue = new LinkedBlockingQueue<>();

    private WeakReference<ViewGroup> weakContainer;

    private List<T> cacheDataList = new ArrayList<>();

    private IBulletHelper<T> helper;

    private long waitTime = 0;

    public HandlerEngine(ViewGroup container) {
        this.weakContainer = new WeakReference<>(container);
    }

    public void addAllData(List<T> currentDataList, IBulletHelper<T> helper){
        this.helper = helper;
        this.cacheDataList.addAll(currentDataList);
        this.handleQueue.addAll(currentDataList);
    }

    public void addData(T data, IBulletHelper<T> helper){
        this.helper = helper;
        this.cacheDataList.add(data);
        this.handleQueue.add(data);
    }

    @Override
    public void run() {
        while (true){
            if (mPause){
                continue;
            }
            try {
                if (handleQueue.isEmpty() && !cacheDataList.isEmpty()){
                    // 实现数据循环
                    handleQueue.addAll(cacheDataList);
                    Thread.sleep(MAX_LOOP_INTERVAL_TIME);
                }
                final ViewGroup container = weakContainer.get();
                if (container != null){
                    T data = handleQueue.take();
                    BulletView<T> bulletView = new BulletView<>(weakContainer, data, helper);
                    bulletView.setDurationMillis(DURATION_MILLIS);
                    bulletView.setOnBindViewListener(new BulletView.OnBindViewListener() {
                        @Override
                        public void onView(final View view) {
                            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    long currentViewSleepTime = getSleepTime(container, view);
                                    waitTime = currentViewSleepTime +  MAX_BULLET_SPACING;
                                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    synchronized (LOCK){
                                        LOCK.notify();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            synchronized (LOCK){
                                LOCK.notify();
                            }
                        }
                    });
                    bulletView.executeAddView();
                    long time = System.currentTimeMillis();
                    synchronized (LOCK){
                        LOCK.wait();
                    }
                    long diffTime = System.currentTimeMillis() - time;
                    waitTime = waitTime - diffTime;
                    Thread.sleep(waitTime <0? 0: waitTime);
                    waitTime = 0; // 重置waitTime
                    bulletView.executeAnimation();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (mQuit) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private long getSleepTime(ViewGroup container, View view){
        int containerHeight = container.getHeight();
        double speed = containerHeight*1.0f / DURATION_MILLIS;
        int viewHeight = view.getHeight();
        double time = Math.ceil(viewHeight / speed);
        if (time <= 0){
            time = 0;
        }
        return (long)time;
    }

    public void resumeTask(){
        mPause = false;
    }

    public void pauseTask(){
        mPause = true;
    }

    public void quit(){
        mQuit = true;
        interrupt();
        if (handleQueue != null){
            handleQueue.clear();
            handleQueue = null;
        }
        if (cacheDataList != null){
            cacheDataList.clear();
            cacheDataList = null;
        }
    }

    public boolean isPause(){
        return mPause;
    }

    public boolean isRunning(){
        return !mQuit;
    }
}
