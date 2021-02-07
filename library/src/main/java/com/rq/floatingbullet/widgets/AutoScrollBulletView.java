package com.rq.floatingbullet.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

/**
 * 自动上浮弹幕View
 * @param <T>
 */
public class AutoScrollBulletView<T> extends FrameLayout {

    /**
     * 轮询线程
     */
    private HandlerEngine<T> handlerEngine;

    public AutoScrollBulletView(Context context) {
        this(context, null);
    }

    public AutoScrollBulletView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScrollBulletView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(){
        handlerEngine = new HandlerEngine<>(this);
        handlerEngine.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopScroll();
    }

    /**
     * 暂停滚动
     */
    public void pauseScroll(){
        if (handlerEngine != null){
            handlerEngine.pauseTask();
        }
        removeAllViews();
    }

    /**
     * 继续滚动
     */
    public void resumeScroll(){
        if (handlerEngine != null){
            handlerEngine.resumeTask();
        }
    }

    /**
     * 停止滚动
     */
    public void stopScroll(){
        if (handlerEngine != null){
            handlerEngine.quit();
        }
        removeAllViews();
    }

    public void addAllData(List<T> list, IBulletHelper<T> helper){
        handlerEngine.addAllData(list, helper);
    }

    public void addData(T data, IBulletHelper<T> helper){
        handlerEngine.addData(data, helper);
    }

    @Override
    protected void onLayout(boolean change, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++){
            View childView = getChildAt(i);
            if (childView.getVisibility() == VISIBLE){
                int viewGroupWidth = getMeasuredWidth();
                int viewGroupHeight = getMeasuredHeight();
                MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                //得到子view的测量建议高度
                int viewMeasuredWidth = childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                int viewMeasuredHeight = childView.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                int viewWidth = Math.min(viewMeasuredWidth, viewGroupWidth);
                int viewHeight = Math.min(viewMeasuredHeight, viewGroupHeight);
                int l = -viewWidth;
                int t = viewGroupHeight - viewHeight;
                int r = l + viewWidth;
                int b = t + viewHeight;
                childView.layout(l, t, r, b);
            }
        }
    }
}
