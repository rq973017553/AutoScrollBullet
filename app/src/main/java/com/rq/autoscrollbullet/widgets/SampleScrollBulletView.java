package com.rq.autoscrollbullet.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.rq.autoscrollbullet.data.Bullet;
import com.rq.floatingbullet.widgets.AutoScrollBulletView;

public class SampleScrollBulletView extends AutoScrollBulletView<Bullet> {

    public SampleScrollBulletView(Context context) {
        super(context);
    }

    public SampleScrollBulletView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SampleScrollBulletView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
