package com.jease.pineapple.record;

import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jease.pineapple.R;
import com.jease.pineapple.encoder.MediaAudioEncoder;
import com.jease.pineapple.encoder.MediaEncoder;
import com.jease.pineapple.encoder.MediaMuxerWrapper;
import com.jease.pineapple.encoder.MediaVideoEncoder;
import com.jease.pineapple.gles.GLController;
import com.jease.pineapple.gles.filters.LookupFilter;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.jease.pineapple.BuildConfig.DEBUG;

public class CameraTextureFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private static final String TAG = "Camera1Fragment";

    private Button mRecordBtn;

    private GLController mGLController;

    private MediaMuxerWrapper mMuxer;

    public static CameraTextureFragment newInstance() {
        return new CameraTextureFragment();
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
            CameraHelper.getInstance().openCamera(0);
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
            mGLController.addFilter(new LookupFilter(getResources()));
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
        View view = inflater.inflate(R.layout.fragment_camera_texture, container, false);
        TextureView textureView = view.findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);
        mRecordBtn = view.findViewById(R.id.bt_record);
        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMuxer == null) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
        mGLController = new GLController(getContext());
        return view;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (null != mGLController) {
            mGLController.surfaceCreated(surface);
            mGLController.setRenderCallback(mRenderCallback);
            mGLController.surfaceChanged(width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (null != mGLController) {
            mGLController.surfaceChanged(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (null != mGLController) {
            mGLController.surfaceDestroyed();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

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

    /**
     * start resorcing
     * This is a sample project and call this on UI thread to avoid being complicated
     * but basically this should be called on private thread because prepareing
     * of encoder is heavy work
     */
    private void startRecording() {
        if (DEBUG) Log.v(TAG, "startRecording:");
        try {
            mRecordBtn.setBackgroundColor(Color.parseColor("#ff0000"));	// turn red
            mMuxer = new MediaMuxerWrapper(".mp4");	// if you record audio only, ".m4a" is also OK.
            if (true) {
                // for video capturing
                new MediaVideoEncoder(mMuxer, mMediaEncoderListener, 720, 1080);
            }
            if (true) {
                // for audio capturing
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            mRecordBtn.setBackgroundResource(0);
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * request stop recording
     */
    private void stopRecording() {
        if (DEBUG) Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
        mRecordBtn.setBackgroundResource(0);
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder)
                mGLController.setVideoEncoder((MediaVideoEncoder)encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder)
                mGLController.setVideoEncoder(null);
        }
    };


}
