package com.jease.pineapple.record;

import android.opengl.EGL14;
import android.text.TextUtils;

import com.jease.pineapple.gles.GLController;
import com.jease.pineapple.media.hardware.HardwareRecorder;
import com.jease.pineapple.media.hardware.MediaEncoder;
import com.jease.pineapple.media.hardware.MediaVideoEncoder;

import java.lang.ref.WeakReference;

public class VideoRecorder {

    private static final String TAG = VideoRecorder.class.getSimpleName();

    private WeakReference<GLController> mWeakGLController;

    private String mOutputPath;

    private int mVideoWidth;

    private int mVideoHeight;

    private HardwareRecorder mHardwareRecorder;

    private RecordStatusCallback mRecordStatusCallback;

    private HardwareEncoderStatusCallback mHardwareEncoderStatusCallback
            = new HardwareEncoderStatusCallback() {
        @Override
        public void onPrepared(MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder && null != mHardwareRecorder) {
                final GLController controller = mWeakGLController.get();
                if (null == controller) return;
                controller.commitTask(new Runnable() {
                    @Override
                    public void run() {
                        mHardwareRecorder.startRecording(EGL14.eglGetCurrentContext());
                        controller.setHardwareRecorder(mHardwareRecorder);
                    }
                });
                if (null != mRecordStatusCallback)
                    mRecordStatusCallback.onPrepared();
            }
        }

        @Override
        public void onStarted(MediaEncoder encoder) {
            if (null != mRecordStatusCallback)
                mRecordStatusCallback.onStarted();
        }

        @Override
        public void onStopped(MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder && null != mHardwareRecorder) {
                GLController controller = mWeakGLController.get();
                if (null == controller) return;
                controller.setHardwareRecorder(null);
                if (mRecordStatusCallback != null)
                    mRecordStatusCallback.onStopped();
            }
        }

        @Override
        public void onReleased(MediaEncoder encoder) {

        }

        @Override
        public void onError(int errorCode) {

        }
    };

    private static final class Singleton {
        private static final VideoRecorder INSTANCE = new VideoRecorder();
    }

    private VideoRecorder() {}

    public static VideoRecorder getInstance() {
        return Singleton.INSTANCE;
    }

    public VideoRecorder render(GLController controller) {
        mWeakGLController = new WeakReference<>(controller);
        return this;
    }

    public VideoRecorder outputPath(String path) {
        mOutputPath = path;
        return this;
    }

    public VideoRecorder resolution(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        return this;
    }

    public void setRecordStatusCallback(RecordStatusCallback callback) {
        mRecordStatusCallback = callback;
    }


    public void prepare() {
        if (mWeakGLController == null)
            throw new IllegalStateException("Must appoint a render!");
        initHardwareEncoder();
    }

    private void initHardwareEncoder() {
        mHardwareRecorder = new HardwareRecorder(mHardwareEncoderStatusCallback);
    }

    public boolean startRecording() {
        if (TextUtils.isEmpty(mOutputPath))
            throw new IllegalArgumentException("Output file path is empty");
        if (mVideoWidth <= 0 || mVideoHeight <= 0)
            throw new IllegalArgumentException("Illegal resolution");
        if (null == mHardwareRecorder)
            throw new IllegalStateException("Hardware encoder is not initialization");
        mHardwareRecorder.prepare(mOutputPath, true, mVideoWidth, mVideoHeight);
        return true;
    }

    public void stopRecording() {
        if (mHardwareRecorder == null)
            return;
        mHardwareRecorder.stopRecording();
    }


    public interface HardwareEncoderStatusCallback {
        void onPrepared(MediaEncoder encoder);
        void onStarted(MediaEncoder encoder);
        void onStopped(MediaEncoder encoder);
        void onReleased(MediaEncoder encoder);
        void onError(int errorCode);
    }

    public interface RecordStatusCallback {
        void onPrepared();
        void onStarted();
        void onStopped();
    }
}
