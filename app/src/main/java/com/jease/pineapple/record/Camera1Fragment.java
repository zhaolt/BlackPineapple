package com.jease.pineapple.record;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jease.pineapple.R;
import com.jease.pineapple.gles.GLController;
import com.jease.pineapple.gles.filters.LookupFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Camera1Fragment extends Fragment implements SurfaceHolder.Callback, View.OnClickListener {

    private SurfaceView mSurfaceView;

    private GLController mGLController;

    private TextView mCameraSwitchBtn;

    private TextView mCameraFlashBtn;

    private int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;

    public static Camera1Fragment newInstance() {
        return new Camera1Fragment();
    }

    private RenderCallback mRenderCallback = new RenderCallback() {
        @Override
        public void onSurfaceDestroyed() {
            CameraHelper.getInstance().stopPreview();
            CameraHelper.getInstance().release();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            CameraHelper.getInstance().prepareCameraThread();
            CameraHelper.getInstance().openCamera(mCameraIndex);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            CameraHelper.getInstance().initCamera(rotation, 9.0 / 16.0);
            SurfaceTexture surfaceTexture = mGLController.getSurfaceTexture();
            if (surfaceTexture == null)
                return;
            CameraHelper.getInstance().setPreviewTexture(surfaceTexture);
            surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mGLController.requestRender();
                }
            });
            CameraHelper.getInstance().startPreview();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
        }

        @Override
        public void onDrawFrame(GL10 gl) {
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera1, container, false);
        mCameraSwitchBtn = view.findViewById(R.id.tv_camera_switch);
        mCameraFlashBtn = view.findViewById(R.id.tv_camera_flash);
        mSurfaceView = view.findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
        mGLController = new GLController(getContext());
        onFilterSet(mGLController);
        mCameraSwitchBtn.setOnClickListener(this);
        mCameraFlashBtn.setOnClickListener(this);
        return view;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null != mGLController) {
            mGLController.surfaceCreated(holder);
            mGLController.setRenderCallback(mRenderCallback);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (null != mGLController)
            mGLController.surfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != mRenderCallback)
            mRenderCallback.onSurfaceDestroyed();
        if (null != mGLController)
            mGLController.surfaceDestroyed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mGLController)
            mGLController.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mGLController)
            mGLController.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mGLController) {
            mGLController.release();
            mGLController = null;
        }
    }

    private void onFilterSet(GLController controller) {
        LookupFilter lutFilter = new LookupFilter(getResources());
        controller.addFilter(lutFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_camera_switch:
                SurfaceTexture surfaceTexture = mGLController.getSurfaceTexture();
                if (surfaceTexture == null)
                    return;
                mCameraIndex = ++mCameraIndex % Camera.getNumberOfCameras();
                CameraHelper.getInstance().switchCamera(
                        getActivity().getWindowManager().getDefaultDisplay().getRotation(),
                        mCameraIndex, 9.0 / 16.0);
                CameraHelper.getInstance().setPreviewTexture(surfaceTexture);
                CameraHelper.getInstance().startPreview();
                break;
            case R.id.tv_camera_flash:
                break;
        }
    }
}
