package com.jease.pineapple.record.album;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.jease.pineapple.R;
import com.jease.pineapple.base.FullScreenActivity;
import com.jease.pineapple.record.CameraActivity;
import com.jease.pineapple.record.camera.CameraHelper;
import com.jease.pineapple.utils.PermissionUtils;

public class AlbumActivity extends FullScreenActivity implements SurfaceHolder.Callback, View.OnClickListener {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, AlbumActivity.class);
    }

    private SurfaceView mSurfaceView;

    private View mVideoRecordFrame;

    private boolean isCameraInited = false;

    private ImageView mCloseBtn;

    private Runnable mLoadCameraPageTask = new Runnable() {
        @Override
        public void run() {
            releaseCamera();
            Intent intent = CameraActivity.getCallingIntent(AlbumActivity.this);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, 0);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_album);
        mSurfaceView = findViewById(R.id.surface_view);
        mVideoRecordFrame = findViewById(R.id.rl_video_record_frame);
        mCloseBtn = findViewById(R.id.iv_close);
        mCloseBtn.setOnClickListener(this);
        mSurfaceView.getHolder().addCallback(this);
        mVideoRecordFrame.setOnClickListener(this);
        loadAlbumFragment();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isCameraInited)
            releaseCamera();
    }

    private void loadAlbumFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.bottom_thumb_frame);
        if (fragment == null)
            fragment = AlbumFragment.newInstance();
        fm.beginTransaction().replace(R.id.bottom_thumb_frame, fragment).commitAllowingStateLoss();
    }

    private void initCamera(SurfaceHolder holder) {
        CameraHelper.getInstance().prepareCameraThread();
        CameraHelper.getInstance().openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        CameraHelper.getInstance().initCamera(getWindowManager().getDefaultDisplay().getRotation(),
                9.0 / 16.0);
        CameraHelper.getInstance().setDisplay(holder);
        CameraHelper.getInstance().startPreview();
        isCameraInited = true;
    }

    private void releaseCamera() {
        CameraHelper.getInstance().stopPreview();
        CameraHelper.getInstance().release();
        isCameraInited = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 101, grantResults,
                mLoadCameraPageTask, new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AlbumActivity.this, R.string.request_permission_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_video_record_frame:
                PermissionUtils.askPermission(AlbumActivity.this,
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        101,
                        mLoadCameraPageTask);
                break;
            case R.id.iv_close:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.slide_out_down);
    }
}
