package com.jease.pineapple.gles;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.jease.pineapple.gles.filters.CameraFilter;
import com.jease.pineapple.gles.filters.GLFilter;
import com.jease.pineapple.gles.filters.GroupFilter;
import com.jease.pineapple.gles.filters.NoFilter;
import com.jease.pineapple.media.hardware.HardwareRecorder;
import com.jease.pineapple.record.RenderCallback;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class GLController implements GLSurfaceView.Renderer {

    private static final String TAG = GLController.class.getSimpleName();

    private static final int OPENGL_ES_VERSION = 2;

    private Object mSurface;

    private GLView mGLView;

    private RenderCallback mRenderCallback;

    private CameraFilter mCameraFilter;

    private GroupFilter mGroupFilter;

    private NoFilter mShowFilter;

    private HardwareRecorder mHardwareRecorder;

    public GLController(Context context) {
        init(context);
    }

    private void init(Context context) {
        mGLView = new GLView(context);
        ViewGroup vg = new ViewGroup(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        vg.addView(mGLView);
        vg.setVisibility(View.GONE);
    }

    public void onResume() {
        mGLView.onResume();
    }

    public void onPause() {
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mCameraFilter != null) {
                    mCameraFilter.release();
                    mCameraFilter = null;
                }
                if (mGroupFilter != null) {
                    mGroupFilter.release();
                    mGroupFilter = null;
                }
            }
        });
        mGLView.onPause();
    }

    public void requestRender() {
        mGLView.requestRender();
    }

    public void commitTask(Runnable runnable) {
        mGLView.queueEvent(runnable);
    }

    public synchronized void setHardwareRecorder(HardwareRecorder recorder) {
        mHardwareRecorder = recorder;
    }

    public SurfaceTexture getSurfaceTexture() {
        if (mCameraFilter != null)
            return mCameraFilter.getSurfaceTexture();
        return null;
    }

    public void setRenderCallback(RenderCallback callback) {
        mRenderCallback = callback;
    }

    public void addFilter(GLFilter filter) {
        if (null != mGroupFilter)
            mGroupFilter.addFilter(filter);
    }

    public void surfaceCreated(Object nativeWindow) {
        mSurface = nativeWindow;
        mGLView.surfaceCreated(null);
    }

    public void surfaceChanged(int width, int height) {
        mGLView.surfaceChanged(null, 0, width, height);
    }

    public void surfaceDestroyed() {
        mGLView.surfaceDestroyed(null);
    }

    public void release() {
        if (null != mRenderCallback)
            mRenderCallback = null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraFilter = new CameraFilter(mGLView.getResources());
        mGroupFilter = new GroupFilter(mGLView.getResources());
        mShowFilter = new NoFilter(mGLView.getResources());
        mCameraFilter.create();
        mGroupFilter.create();
        mShowFilter.create();
        if (null != mRenderCallback)
            mRenderCallback.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraFilter.setSize(width, height);
        mGroupFilter.setSize(width, height);
        mShowFilter.setSize(width, height);
        if (null != mRenderCallback)
            mRenderCallback.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraFilter.draw();
        mGroupFilter.setTextureId(mCameraFilter.getOutputTexture());
        mGroupFilter.draw();
        mShowFilter.setTextureId(mGroupFilter.getOutputTexture());
        mShowFilter.draw();
        if (mHardwareRecorder != null) {
            mHardwareRecorder.frameAvailable();
            mHardwareRecorder.drawRecordFrame(mGroupFilter.getOutputTexture());
        }
        if (null != mRenderCallback)
            mRenderCallback.onDrawFrame(gl);
    }


    private final class GLView extends GLSurfaceView {

        public GLView(Context context) {
            super(context);
            init();
        }

        private void init() {
            getHolder().addCallback(null);
            setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory() {
                @Override
                public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                                      EGLConfig config, Object nativeWindow) {
                    return egl.eglCreateWindowSurface(display, config, mSurface, null);
                }

                @Override
                public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
                    egl.eglDestroySurface(display, surface);
                }
            });
            setEGLContextClientVersion(OPENGL_ES_VERSION);
            setRenderer(GLController.this);
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }
}
