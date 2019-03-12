package com.jease.pineapple.record;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jease.pineapple.R;
import com.jease.pineapple.common.Constants;
import com.jease.pineapple.gles.GLController;
import com.jease.pineapple.gles.filters.LookupFilter;
import com.jease.pineapple.record.camera.CameraHelper;
import com.jease.pineapple.record.camera.TakePictureCallback;
import com.jease.pineapple.record.filter.Filter;
import com.jease.pineapple.record.filter.FilterMenuFragment;
import com.jease.pineapple.utils.FileUtils;
import com.jease.pineapple.widget.CameraShutter;
import com.jease.pineapple.widget.CameraView;
import com.jease.pineapple.widget.ItemScrollView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback,
        View.OnClickListener, CameraShutter.OnClickListener, ItemScrollView.OnItemSelectedListener {

    private static final String TAG = CameraFragment.class.getSimpleName();

    private SurfaceView mSurfaceView;

    private GLController mGLController;

    private TextView mCameraSwitchBtn;

    private TextView mCameraFlashBtn;

    private TextView mCameraFilterBtn;

    private CameraView mCameraView;

    private ImageView mCloseBtn;

    private ItemScrollView mItemScrollView;

    private String[] mCameraMode;

    private FrameLayout mBottomFrame;

    private int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;

    private VideoRecorder.RecordStatusCallback mRecordStatusCallback
            = new VideoRecorder.RecordStatusCallback() {
        @Override
        public void onPrepared() {
        }

        @Override
        public void onStarted() {
            mCameraView.post(new Runnable() {
                @Override
                public void run() {
                    onStartRecording();
                }
            });
        }

        @Override
        public void onStopped() {
            mCameraView.post(new Runnable() {
                @Override
                public void run() {
                    onStopRecording();
                }
            });
        }
    };

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    private RenderCallback mRenderCallback = new RenderCallback() {
        @Override
        public void onSurfaceDestroyed() {
            releaseCamera();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            initCamera();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
        }

        @Override
        public void onDrawFrame(GL10 gl) {
        }
    };

    private void initCamera() {
        CameraHelper.getInstance().prepareCameraThread();
        CameraHelper.getInstance().openCamera(mCameraIndex);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        CameraHelper.getInstance().initCamera(rotation, 9.0 / 16.0);
        SurfaceTexture surfaceTexture = mGLController.getSurfaceTexture();
        if (surfaceTexture == null)
            return;
        CameraHelper.getInstance().setPreviewTexture(surfaceTexture);
        onFilterSet(mGLController);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mGLController.requestRender();
            }
        });
        CameraHelper.getInstance().startPreview();
    }

    private void releaseCamera() {
        CameraHelper.getInstance().stopPreview();
        CameraHelper.getInstance().release();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mCameraSwitchBtn = view.findViewById(R.id.tv_camera_switch);
        mCameraFlashBtn = view.findViewById(R.id.tv_camera_flash);
        mCameraFilterBtn = view.findViewById(R.id.tv_camera_filter);
        mSurfaceView = view.findViewById(R.id.surface_view);
        mCameraView = view.findViewById(R.id.camera_view);
        mCloseBtn = view.findViewById(R.id.iv_close);
        mItemScrollView = view.findViewById(R.id.item_scroll_view);
        mBottomFrame = view.findViewById(R.id.bottom_frame);
        mCameraMode = getActivity().getResources().getStringArray(R.array.camera_mode);
        mItemScrollView.addIndicator(Arrays.asList(mCameraMode));
        mSurfaceView.getHolder().addCallback(this);
        mGLController = new GLController(getContext());
        mCameraSwitchBtn.setOnClickListener(this);
        mCameraFlashBtn.setOnClickListener(this);
        mCameraFilterBtn.setOnClickListener(this);
        mCameraView.setOnShutterClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mItemScrollView.setOnItemSelectedListener(this);
        mCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return dismissPopMenu();
            }
        });
    }

    public boolean dismissPopMenu() {
        if (mCameraFilterBtn.isSelected()) {
            mCameraFilterBtn.setSelected(false);
            hideFilterMenu();
            return true;
        } else {
            return false;
        }
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

    private void onStartRecording() {
        mCameraView.startShutterVideoAnim();
        mCameraSwitchBtn.setVisibility(View.GONE);
        mCameraFlashBtn.setVisibility(View.GONE);
    }

    private void onStopRecording() {
        mCameraView.stopShutterVideoAnim();
        mCameraSwitchBtn.setVisibility(View.VISIBLE);
        mCameraFlashBtn.setVisibility(View.VISIBLE);
        VideoRecorder.getInstance().setRecordStatusCallback(null);
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
            case R.id.iv_close:
                getActivity().onBackPressed();
                break;
            case R.id.tv_camera_filter:
                if (mCameraFilterBtn.isSelected())
                    hideFilterMenu();
                else
                    showFilterMenu();
                mCameraFilterBtn.setSelected(!mCameraFilterBtn.isSelected());
                break;
        }
    }

    private void showFilterMenu() {
        ArrayList<Filter> filters = new ArrayList<>();
        for (int i = 0; i < Constants.Filter.FILTER_ASSETS_PATH.length; i++) {
            Filter f = new Filter();
            f.setCoverResId(Constants.Filter.FILTER_RES_IDS[i]);
            f.setLutPath(Constants.Filter.FILTER_ASSETS_PATH[i]);
            filters.add(f);
        }
        mBottomFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.bottom_frame);
        if (null == fragment)
            fragment = FilterMenuFragment.newInstance(filters);
        ((CameraActivity) getActivity()).addFragment(fm, fragment, R.id.bottom_frame);
        hideAllViews();
    }

    private void hideFilterMenu() {
        mBottomFrame.setOnTouchListener(null);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.bottom_frame);
        if (null == fragment) return;
        ((CameraActivity) getActivity()).removeFragment(fm, fragment);
        showAllViews();
    }

    private void hideAllViews() {
        mCloseBtn.setVisibility(View.GONE);
        mCameraSwitchBtn.setVisibility(View.GONE);
        mCameraFlashBtn.setVisibility(View.GONE);
        mCameraFilterBtn.setVisibility(View.GONE);
        mItemScrollView.setVisibility(View.GONE);
        mCameraView.setShutterVisiblity(false);
    }

    private void showAllViews() {
        mCloseBtn.setVisibility(View.VISIBLE);
        mCameraSwitchBtn.setVisibility(View.VISIBLE);
        mCameraFlashBtn.setVisibility(View.VISIBLE);
        mCameraFilterBtn.setVisibility(View.VISIBLE);
        mItemScrollView.setVisibility(View.VISIBLE);
        mCameraView.setShutterVisiblity(true);
    }


    @Override
    public void onPressed() {
        VideoRecorder.getInstance()
                .outputPath(FileUtils.getVideoPath())
                .resolution(Constants.Video.WIDTH, Constants.Video.HEIGHT)
                .render(mGLController)
                .prepare();
        VideoRecorder.getInstance().setRecordStatusCallback(mRecordStatusCallback);
        VideoRecorder.getInstance().startRecording();
        Log.i(TAG, "shutter be pressed, start recording video.");
    }

    @Override
    public void onRelease() {
        VideoRecorder.getInstance().stopRecording();
        Log.i(TAG, "shutter be released, stop recording video.");
    }

    @Override
    public void onClicked() {
        CameraHelper.getInstance().takePicture(new TakePictureCallback() {
            @Override
            public void onTakePicture(Observable<byte[]> observable) {
                observable.map(new Function<byte[], String>() {
                    @Override
                    public String apply(byte[] bytes) throws Exception {
                        String photoPath = FileUtils.getPhotoPath();
                        File file = new File(photoPath);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            fos.write(bytes);
                            fos.flush();
                        } finally {
                            if (fos != null) {
                                fos.close();
                            }
                        }
                        return photoPath;
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(String s) {
                                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                                mCameraView.stopTakePhoto();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "拍照失败", Toast.LENGTH_SHORT).show();
                                mCameraView.stopTakePhoto();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
            }

            @Override
            public void onError(int errorCode) {
                mCameraView.post(new Runnable() {
                    @Override
                    public void run() {
                        mCameraView.stopTakePhoto();
                    }
                });
            }
        }, 90);
    }

    @Override
    public void onItemSelected(View v, String value, int position) {
        if (value.equals(mCameraMode[0]))
            mCameraView.changeToPhotoMode();
        else if (value.equals(mCameraMode[1]))
            mCameraView.changeToVideoMode();
    }
}
