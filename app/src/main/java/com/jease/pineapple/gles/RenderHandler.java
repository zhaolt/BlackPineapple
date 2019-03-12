package com.jease.pineapple.gles;

import android.os.Handler;
import android.os.Message;

public class RenderHandler extends Handler {


    public RenderHandler(RenderThread thread) {
        super(thread.getLooper());

    }

    @Override
    public void handleMessage(Message msg) {

    }
}
