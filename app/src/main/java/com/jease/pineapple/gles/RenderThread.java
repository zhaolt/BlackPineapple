package com.jease.pineapple.gles;

import android.graphics.SurfaceTexture;
import android.os.HandlerThread;
import android.view.SurfaceHolder;

public class RenderThread extends HandlerThread {

    private Renderer mRenderer;

    private EGLBase mEgl;

    private EGLBase.EglSurface mEglSurface;

    public RenderThread(String name) {
        super(name);
    }

    void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    void surfaceCreated(Object surface) {
        if (!(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder))
            throw new IllegalArgumentException("must set a SurfaceHolder or SurfaceTexture");
        mEgl = new EGLBase(null, false, false);
        mEglSurface = mEgl.createFromSurface(surface);
        mEglSurface.makeCurrent();
        if (null != mRenderer)
            mRenderer.onSurfaceCreated();
    }

    void surfaceChanged(int width, int height) {
        if (null != mRenderer)
            mRenderer.onSurfaceChanged(width, height);
    }

    void surfaceDestroy() {
        if (null != mRenderer)
            mRenderer.onSurfaceDestroyed();
    }

    void release() {
        if (null != mRenderer)
            mRenderer = null;
        if (null != mEglSurface) {
            mEglSurface.release();
            mEglSurface = null;
        }
        if (null != mEgl) {
            mEgl.release();
            mEgl = null;
        }
        getLooper().quit();
    }

    void drawFrame() {
        mEglSurface.makeCurrent();
        if (null != mRenderer)
            mRenderer.onDrawFrame();
        mEglSurface.swap();
    }
}
