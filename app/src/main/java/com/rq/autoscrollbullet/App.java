package com.rq.autoscrollbullet;

import android.app.Application;

import com.rq.autoscrollbullet.utils.ImageLoader;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.initialize(this);
    }
}
