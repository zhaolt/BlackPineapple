package com.jease.pineapple.record;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.jease.pineapple.BuildConfig;

import java.lang.ref.WeakReference;

public class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static final int MSG_OPEN_CAMERA = 1;

    public static final int MSG_INIT_CAMERA = 2;

    public static final int MSG_HANDLE_FOCUS = 3;

    public static final int MSG_HANDLE_SHRINK = 4;

    public static final int MSG_TAKE_PICTURE = 5;

    public static final int MSG_START_PREVIEW = 6;

    public static final int MSG_STOP_PREVIEW = 7;

    public static final int MSG_SET_PREVIEW_TEXTURE = 8;

    public static final int MSG_ENABLE_FLASH = 9;

    public static final int MSG_SWITCH_CAMERA = 10;

    public static final int MSG_QUIT = 11;

    private CameraThread mCamera;

    private CameraHelper() {}

    private static final class SingleHandle {
        private static final CameraHelper INSTANCE = new CameraHelper();
    }

    public static CameraHelper getInstance() {
        return SingleHandle.INSTANCE;
    }

    public void prepareCameraThread() {
        mCamera = new CameraThread(this);
        mCamera.start();
        // 调用方为OpenGL线程 里面有线程等待，会在这里等一会。如果此时主线程将mCamera置空，会触发空指针
        if (null != mCamera)
            mCamera.waitUnitReady();
    }


    public void openCamera(int cameraId) {
        if (mCamera == null) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_OPEN_CAMERA, cameraId, 0));
    }

    public void switchCamera(int rotation, int cameraIndex, double ratio) {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_SWITCH_CAMERA, rotation, cameraIndex,
                    ratio));
    }

    public void release() {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendEmptyMessage(MSG_QUIT);
        mCamera = null;
    }

    public void initCamera(int rotation, double ratio) {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_INIT_CAMERA;
            msg.arg1 = rotation;
            msg.obj = ratio;
            handler.sendMessage(msg);
        }
    }

    public void handleFocus(FocusParams params) {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_HANDLE_FOCUS, params));
    }

    public void handleShrink(boolean shrink) {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_HANDLE_SHRINK, shrink));
    }


    public void startPreview() {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendEmptyMessage(MSG_START_PREVIEW);
    }

    public void stopPreview() {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendEmptyMessage(MSG_STOP_PREVIEW);
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_SET_PREVIEW_TEXTURE, surfaceTexture));
    }

    public void enableFlash(boolean enable) {
        if (null == mCamera) return;
        CameraHandler handler = mCamera.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_ENABLE_FLASH, enable));
    }


    public static final class CameraHandler extends Handler {

        private WeakReference<CameraThread> mWeakCamera1;

        public CameraHandler(CameraThread camera) {
            mWeakCamera1 = new WeakReference<>(camera);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraThread camera = mWeakCamera1.get();
            if (camera == null) return;
            switch (msg.what) {
                case MSG_OPEN_CAMERA:
                    camera.createCamera(msg.arg1);
                    break;
                case MSG_INIT_CAMERA:
                    camera.initCamera(msg.arg1, (Double) msg.obj);
                    break;
                case MSG_HANDLE_FOCUS:
                    camera.handleFocus((FocusParams) msg.obj);
                    break;
                case MSG_HANDLE_SHRINK:
                    camera.handleShrink((Boolean) msg.obj);
                    break;
                case MSG_TAKE_PICTURE:
                    break;
                case MSG_START_PREVIEW:
                    camera.startPreview();
                    break;
                case MSG_STOP_PREVIEW:
                    camera.stopPreview();
                    break;
                case MSG_SET_PREVIEW_TEXTURE:
                    camera.setPreviewTexture((SurfaceTexture) msg.obj);
                    break;
                case MSG_ENABLE_FLASH:
                    camera.enableFlash((Boolean) msg.obj);
                    break;
                case MSG_SWITCH_CAMERA:
                    camera.switchCamera(msg.arg1, msg.arg2, (Double) msg.obj);
                    break;
                case MSG_QUIT:
                    if (DEBUG)
                        Log.d(TAG, "quit loop");
                    Looper.myLooper().quit();
                    release();
                    break;
            }
        }

        private void release() {
            if (mWeakCamera1 != null) {
                mWeakCamera1.clear();
                mWeakCamera1 = null;
            }
        }
    }
}
