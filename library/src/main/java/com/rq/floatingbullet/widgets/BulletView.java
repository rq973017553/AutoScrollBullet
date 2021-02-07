package com.rq.floatingbullet.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class BulletView<T>{

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private OnBindViewListener listener;

    private long durationMillis;

    private View currentView;

    private ViewGroup container;

    public BulletView(ViewGroup container, T data, IBulletHelper<T> helper) {
        this.container = container;
        if (helper != null){
            currentView = helper.getView(data);
        }
    }

    void setDurationMillis(long durationMillis){
        this.durationMillis = durationMillis;
    }

    void setOnBindViewListener(OnBindViewListener listener){
        this.listener = listener;
    }

    void executeAddView(){
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (container != null && currentView != null){
                    container.addView(currentView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    if (listener != null){
                        listener.onView(currentView);
                    }
                }
            }
        });
    }

    void executeAnimation(){
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (container != null && currentView != null){
                    AnimatorSet animatorSet = getDefaultAnimator(container, currentView);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            sHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    currentView.clearAnimation();
                                    container.removeView(currentView);
                                }
                            });
                        }
                    });
                    animatorSet.start();
                }
            }
        });
    }

    /**
     * 默认动画
     * @param container 弹幕父容器
     * @param bulletView 弹幕
     * @return {@link AnimatorSet}
     */
    AnimatorSet getDefaultAnimator(ViewGroup container, View bulletView){
        int containerHeight = container.getHeight();
        int viewWidth = bulletView.getWidth();
        // 弹幕出来的时候alpha的变化
        ObjectAnimator outAlphaAnimator = ObjectAnimator.
                ofFloat(bulletView, "alpha", 0.2f, 1.0f);
        outAlphaAnimator.setDuration(1000);
        // 弹幕横移出来
        ObjectAnimator outTranslateAnimator = ObjectAnimator.
                ofFloat(bulletView, "translationX", -viewWidth, viewWidth);
        outTranslateAnimator.setDuration(0);
        // 弹幕上浮
        ObjectAnimator scrollAnimator = ObjectAnimator.
                ofFloat(bulletView, "translationY", 0, -containerHeight);
        scrollAnimator.setDuration(durationMillis);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(scrollAnimator, outTranslateAnimator, outAlphaAnimator);
        return animatorSet;
    }

    /**
     * 默认动画
     * @return {@link AnimationSet}
     */
    AnimationSet getDefaultAnimation(){
        AnimationSet animationSet = new AnimationSet(false);
        ScaleAnimation outScaleAnimation = new ScaleAnimation(0, 1.0f, 0, 1.0f,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f);
        outScaleAnimation.setDuration(1000);
        AlphaAnimation outAlphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        outAlphaAnimation.setDuration(1000);
        TranslateAnimation outTranslateAnimation =
                new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0);
        outTranslateAnimation.setDuration(0);
        TranslateAnimation scrollAnimation =
                new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
                        Animation.RELATIVE_TO_PARENT, 0,
                        Animation.RELATIVE_TO_PARENT, 0,
                        Animation.RELATIVE_TO_PARENT, -1.0f);
        scrollAnimation.setDuration(durationMillis);
        scrollAnimation.setInterpolator(new LinearInterpolator());
//        animationSet.addAnimation(outScaleAnimation);
        animationSet.addAnimation(scrollAnimation);
        animationSet.addAnimation(outTranslateAnimation);
        animationSet.addAnimation(outAlphaAnimation);
        return animationSet;
    }

    /**
     * 对外接口，用于获取自定义View
     */
    interface OnBindViewListener{
        void onView(View view);
    }
}
