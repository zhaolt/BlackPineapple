package com.jease.pineapple.base;

import android.app.Application;

import com.jease.pineapple.utils.DeviceInfoUtils;

public class App extends Application {

    public static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        DeviceInfoUtils.init(this);
    }
}
