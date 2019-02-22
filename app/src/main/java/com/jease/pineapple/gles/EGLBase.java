package com.jease.pineapple.gles;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * a, 选择Display
 * b, 选择Config
 * c, 创建Surface
 * d, 创建Context
 * e, 指定当前的环境为绘制环境
 */
public class EGLBase {
    private static final String TAG = EGLBase.class.getSimpleName();

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mEglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mDefaultContext = EGL14.EGL_NO_CONTEXT;

    public EGLBase(EGLContext sharedContext, boolean withDepthBuffer, boolean isRecordable) {
        init(sharedContext, withDepthBuffer, isRecordable);
    }

    private void init(EGLContext sharedContext, boolean withDepthBuffer, boolean isRecordable) {
        Log.i(TAG, "init");
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            throw new IllegalStateException("EGL already set up!");
        }
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed!");
        }
        final int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            mEglDisplay = null;
            throw new RuntimeException("eglInitialize failed");
        }
        sharedContext = sharedContext != null ? sharedContext : EGL14.EGL_NO_CONTEXT;
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            mEglConfig = getConfig(withDepthBuffer, isRecordable);
            if (mEglConfig == null) {
                throw new RuntimeException("chooseConfig failed!");
            }
            mEglContext = createContext(sharedContext);
        }
        final int[] values = new int[1];
        EGL14.eglQueryContext(mEglDisplay, mEglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);
        makeDefault();
    }

    private EGLConfig getConfig(boolean withDepthBuffer, boolean isRecordable) {
        final int[] attribList = {
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE, EGL14.EGL_NONE,    //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_NONE, EGL14.EGL_NONE,    //EGL_RECORDABLE_ANDROID, 1,	// this flag need to recording of MediaCodec
                EGL14.EGL_NONE, EGL14.EGL_NONE,    //	with_depth_buffer ? EGL14.EGL_DEPTH_SIZE : EGL14.EGL_NONE,
                // with_depth_buffer ? 16 : 0,
                EGL14.EGL_NONE
        };
        int offset = 10;
        if (withDepthBuffer) {
            attribList[offset++] = EGL14.EGL_DEPTH_SIZE;
            attribList[offset++] = 16;
        }
        if (isRecordable && (Build.VERSION.SDK_INT >= 18)) { // 配合MediaCodec InputSurface
            attribList[offset++] = EGL_RECORDABLE_ANDROID;
            attribList[offset++] = 1;
        }
        for (int i = attribList.length - 1; i >= offset; i--) {
            attribList[i] = EGL14.EGL_NONE;
        }
        final EGLConfig[] configs = new EGLConfig[1];
        final int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEglDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            // XXX it will be better to fallback to RGB565
            Log.w(TAG, "unable to find RGBA8888 / " + " EGLConfig");
            return null;
        }
        return configs[0];
    }

    private EGLContext createContext(EGLContext sharedContext) {
        final int[] attributeList = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        EGLContext context = EGL14.eglCreateContext(mEglDisplay, mEglConfig, sharedContext,
                attributeList, 0);
        return context;
    }
    private void makeDefault() {
        if (!EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)) {
            Log.w("TAG", "makeDefault" + EGL14.eglGetError());
        }
    }

    private void destroyContext() {
        Log.i(TAG, "destroyContext");
        if (!EGL14.eglDestroyContext(mEglDisplay, mEglContext)) {
            Log.e("destroyContext", "display:" + mEglDisplay + " context: "
                    + mEglContext);
            Log.e(TAG, "eglDestroyContex:" + EGL14.eglGetError());
        }
        mEglContext = EGL14.EGL_NO_CONTEXT;
        if (mDefaultContext != EGL14.EGL_NO_CONTEXT) {
            if (!EGL14.eglDestroyContext(mEglDisplay, mDefaultContext)) {
                Log.e("destroyContext", "display:" + mEglDisplay + " context: "
                        + mDefaultContext);
                Log.e(TAG, "eglDestroyContex:" + EGL14.eglGetError());
            }
            mDefaultContext = EGL14.EGL_NO_CONTEXT;
        }
    }

    private EGLSurface createWindowSurface(Object nativeWindow) {
        final int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        EGLSurface result = null;
        try {
            result = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, nativeWindow,
                    surfaceAttribs, 0);
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "eglCreateWindowSurface", e);
        }
        return result;
    }

    private EGLSurface createOffscreenSurface(final int width, final int height) {
        Log.v(TAG, "createOffscreenSurface:");
        final int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };
        EGLSurface result = null;
        try {
            result = EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, surfaceAttribs, 0);
            if (result == null) {
                throw new RuntimeException("surface was null");
            }
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "createOffscreenSurface", e);
        } catch (final RuntimeException e) {
            Log.e(TAG, "createOffscreenSurface", e);
        }
        return result;
    }

    private boolean makeCurrent(final EGLSurface surface) {
        if (mEglDisplay == null) {
            Log.w(TAG, "makeCurrent: eglDisplay not initialized");
        }
        if (surface == null || surface == EGL14.EGL_NO_SURFACE) {
            final int error = EGL14.eglGetError();
            if (error == EGL14.EGL_BAD_NATIVE_WINDOW) {
                Log.e(TAG, "makeCurrent:returned EGL_BAD_NATIVE_WINDOW.");
            }
            return false;
        }
        // attach EGL renderring context to specific EGL window surface
        if (!EGL14.eglMakeCurrent(mEglDisplay, surface, surface, mEglContext)) {
            Log.w(TAG, "eglMakeCurrent:" + EGL14.eglGetError());
            return false;
        }
        return true;
    }

    private void destroyWindowSurface(EGLSurface surface) {
        Log.v(TAG, "destroySurface:");

        if (surface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEglDisplay,
                    EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(mEglDisplay, surface);
        }
        surface = EGL14.EGL_NO_SURFACE;
        Log.v(TAG, "destroySurface: finished");
    }

    private int swap(final EGLSurface surface) {
        if (!EGL14.eglSwapBuffers(mEglDisplay, surface)) {
            final int err = EGL14.eglGetError();
            Log.w(TAG, "swap: err=" + err);
            return err;
        }
        return EGL14.EGL_SUCCESS;
    }

    public EglSurface createFromSurface(final Object surface) {
        final EglSurface eglSurface = new EglSurface(this, surface);
        eglSurface.makeCurrent();
        return eglSurface;
    }

    public EglSurface createOffScreen(int width, int height) {
        final EglSurface eglSurface = new EglSurface(this, width, height);
        eglSurface.makeCurrent();
        return eglSurface;
    }

    public EGLContext getContext() {
        return mEglContext;
    }

    public int querySurface(final EGLSurface eglSurface, final int what) {
        final int[] value = new int[1];
        EGL14.eglQuerySurface(mEglDisplay, eglSurface, what, value, 0);
        return value[0];
    }

    public void release() {
        Log.i(TAG, "release");
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            destroyContext();
            EGL14.eglTerminate(mEglDisplay);
            EGL14.eglReleaseThread();
        }
        mEglDisplay = EGL14.EGL_NO_DISPLAY;
        mEglContext = EGL14.EGL_NO_CONTEXT;
    }

    public static class EglSurface {
        private final EGLBase mEgl;
        private final int mWidth, mHeight;
        private EGLSurface mEglSurface = EGL14.EGL_NO_SURFACE;

        EglSurface(EGLBase egl, final Object surface) {
            if (!(surface instanceof SurfaceView)
                    && !(surface instanceof Surface)
                    && !(surface instanceof SurfaceHolder)
                    && !(surface instanceof SurfaceTexture))
                throw new IllegalArgumentException("unsupported surface");
            mEgl = egl;
            mEglSurface = mEgl.createWindowSurface(surface);
            mWidth = mEgl.querySurface(mEglSurface, EGL14.EGL_WIDTH);
            mHeight = mEgl.querySurface(mEglSurface, EGL14.EGL_HEIGHT);
            Log.v(TAG, String.format("EglSurface:size(%d,%d)", mWidth, mHeight));
        }

        EglSurface(final EGLBase egl, final int width, final int height) {
            Log.v(TAG, "EglSurface:");
            mEgl = egl;
            mEglSurface = mEgl.createOffscreenSurface(width, height);
            mWidth = width;
            mHeight = height;
        }

        public void makeCurrent() {
            mEgl.makeCurrent(mEglSurface);
        }

        public void swap() {
            mEgl.swap(mEglSurface);
        }

        public EGLContext getContext() {
            return mEgl.getContext();
        }

        public void release() {
            Log.v(TAG, "EglSurface:release:");
            mEgl.makeDefault();
            mEgl.destroyWindowSurface(mEglSurface);
            mEglSurface = EGL14.EGL_NO_SURFACE;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }
    }
}
