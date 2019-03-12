package com.jease.pineapple.record.camera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;

public class CameraThread extends Thread {

    private static final String TAG = CameraThread.class.getSimpleName();

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Camera mCamera;

    private int mFacing;

    private boolean isReady = false;

    private WeakReference<CameraHelper> mWeakCameraHelper;

    private CameraHelper.CameraHandler mHandler;

    private final Object mReadyFence = new Object();

    public CameraThread(CameraHelper helper) {
        super(TAG);
        mWeakCameraHelper = new WeakReference<>(helper);
    }

    public void waitUnitReady() {
        synchronized (mReadyFence) {
            while (!isReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CameraHelper.CameraHandler getHandler() {
        return mHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new CameraHelper.CameraHandler(this);
            isReady = true;
            mReadyFence.notify();
        }
        Looper.loop();
        synchronized (mReadyFence) {
            release();
            isReady = false;
            mHandler = null;
        }
    }

    void createCamera(int cameraId) {
        synchronized (mReadyFence) {
            openCamera(cameraId);
        }
    }

    private void openCamera(int cameraId) {
        if (cameraId != Camera.CameraInfo.CAMERA_FACING_BACK
                && cameraId != Camera.CameraInfo.CAMERA_FACING_FRONT)
            throw new IllegalArgumentException("illegal camera id.");
        try {
            Log.i(TAG, "openCamera");
            mCamera = Camera.open(cameraId);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            mFacing = info.facing;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                    && info.canDisableShutterSound) {
                mCamera.enableShutterSound(false);  // 关闭快门声
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initCamera(int rotation, double ratio) {
        synchronized (mReadyFence) {
            setupCamera(rotation, ratio);
        }
    }

    private void setupCamera(int rotation, double ratio) {
        if (null == mCamera) {
            Log.e(TAG, "Camera reference is null");
            return;
        }
        try {
            setFlash(false);
            setAF(mCamera);
            setCAF(mCamera);
            Camera.Parameters params = mCamera.getParameters();
            Camera.Size previewSize = calculateMaxSize(params.getSupportedPreviewSizes(), ratio);
            params.setPreviewSize(previewSize.width, previewSize.height);
            Camera.Size pictureSize = calculateMaxSize(params.getSupportedPictureSizes(), ratio);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            // 适配nexus6系列拍摄预览倒置的问题
            if (Build.MODEL.equals("Nexus 6") && isFront()) {
                mCamera.setDisplayOrientation(0);
            } else if (Build.MODEL.equals("Nexus 6P") && isFront()) {
                mCamera.setDisplayOrientation(270);
            } else {
                mCamera.setDisplayOrientation(ORIENTATIONS.get(rotation));
            }
            mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void switchCamera(int rotation, int cameraIndex, double ratio) {
        synchronized (mReadyFence) {
            releaseCamera();
            openCamera(cameraIndex);
            setupCamera(rotation, ratio);
        }
    }

    void enableFlash(boolean enable) {
        synchronized (mReadyFence) {
            setFlash(enable);
        }
    }

    void handleFocus(FocusParams focusParams) {
        synchronized (mReadyFence) {
            if (null == mCamera) return;
            try {
                Camera.Parameters params = mCamera.getParameters();
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(mFacing, info);
                Rect focusRect = focusParams.getFocusRect();
                Rect meteringRect = focusParams.getMeteringRect();
                mCamera.cancelAutoFocus();
                if (mFacing != Camera.CameraInfo.CAMERA_FACING_FRONT
                        && params.getMaxNumFocusAreas() > 0) {
                    ArrayList<Camera.Area> focusAreas = new ArrayList<>();
                    focusAreas.add(new Camera.Area(focusRect, FocusParams.FOCUS_SIDE));
                    params.setFocusAreas(focusAreas);
                }
                if (params.getMaxNumMeteringAreas() > 0) {
                    ArrayList<Camera.Area> meteringAreas = new ArrayList<>();
                    meteringAreas.add(new Camera.Area(meteringRect, FocusParams.FOCUS_SIDE));
                    params.setMeteringAreas(meteringAreas);
                }
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(params);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void takePicture(final TakePictureCallback callback, int rotate) {
        synchronized (mReadyFence) {
            if (null == mCamera) {
                if (null != callback)
                    callback.onError(-1);
                return;
            }
            try {
                Camera.Parameters params = mCamera.getParameters();
                if (isFront())
                    rotate = Math.abs(360 - rotate) % 360;
                params.setRotation(rotate);
                mCamera.setParameters(params);
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (null != callback)
                            callback.onTakePicture(Observable.just(data));
                        try {
                            camera.startPreview();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void handleShrink(boolean shrink) {
        synchronized (mReadyFence) {
            if (null == mCamera) return;
            Camera.Parameters params = mCamera.getParameters();
            if (params == null) return;
            if (params.isZoomSupported()) {
                int maxZoom = params.getMaxZoom();
                int zoom = params.getZoom();
                if (!shrink && zoom < maxZoom) {
                    zoom++;
                } else if (zoom > 0) {
                    zoom--;
                }
                params.setZoom(zoom);
                mCamera.setParameters(params);
            }
        }
    }

    void startPreview() {
        synchronized (mReadyFence) {
            if (null != mCamera) {
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void stopPreview() {
        synchronized (mReadyFence) {
            releaseCamera();
        }
    }

    private void releaseCamera() {
        if (mCamera == null) return;
        try {
            Log.i(TAG, "release Camera");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setPreviewTexture(SurfaceTexture surfaceTexture) {
        synchronized (mReadyFence) {
            if (null != mCamera) {
                try {
                    mCamera.setPreviewTexture(surfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void setDisplay(SurfaceHolder holder) {
        synchronized (mReadyFence) {
            if (null != mCamera) {
                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setAF(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            applyParams(camera, params);
        }
    }

    private void setCAF(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            applyParams(camera, params);
        } else if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            applyParams(camera, params);
        }
    }

    private void applyParams(Camera camera, Camera.Parameters params) {
        try {
            camera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] calculatePreviewFps(List<int[]> supportedFps, int bestFps) {
        int bestMatchIndex = -1;
        float bestMatchFps0 = 0;
        float bestMatchFps1 = 0;
        for (int i = 0, len = supportedFps.size(); i < len; i++) {
            int[] fpsRange = supportedFps.get(i);
            float fps0 = (float) (fpsRange[0]) / 1000;
            float fps1 = (float) (fpsRange[1]) / 1000;
            if (bestMatchIndex < 0 || Math.abs(fps1 - bestFps) <= Math.abs(bestMatchFps1 - bestFps)) {
                if ((Math.abs(fps1 - bestFps) == Math.abs(bestMatchFps1 - bestFps))
                        && (Math.abs(fps0 - bestFps) < Math.abs(bestMatchFps0 - 25))) {
                    bestMatchIndex = i;
                    bestMatchFps0 = fps0;
                    bestMatchFps1 = fps1;
                } else {
                    bestMatchIndex = i;
                    bestMatchFps0 = fps0;
                    bestMatchFps1 = fps1;
                }
            }
        }
        if (bestMatchIndex >= 0) {
            return supportedFps.get(bestMatchIndex);
        } else {
            return supportedFps.get(0);
        }
    }

    private Camera.Size calculateMaxSize(List<Camera.Size> supportedSize, double ratio) {
        ArrayList<Camera.Size> targetRatioSize = new ArrayList<>();
        StringBuilder sizesString = new StringBuilder();
        for (Camera.Size size : supportedSize) {
            double r = size.height / (double) size.width;
            if (r == ratio) {
                sizesString.append(size.width).append('x')
                        .append(size.height).append(' ');
                targetRatioSize.add(size);
            }
        }
        Log.d(TAG, sizesString.toString());
        return Collections.max(targetRatioSize, new CameraSizeComparator());
    }

    private void setFlash(boolean enable) {
        if (mCamera == null || mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            return;
        try {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(enable ? Camera.Parameters.FLASH_MODE_ON
                    : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFront() {
        return mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    private void release() {
        if (mWeakCameraHelper != null) {
            mWeakCameraHelper.clear();
            mWeakCameraHelper = null;
        }
    }


    public class CameraSizeComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
