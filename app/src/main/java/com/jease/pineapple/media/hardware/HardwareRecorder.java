package com.jease.pineapple.media.hardware;

import android.opengl.EGLContext;

import com.jease.pineapple.record.VideoRecorder;

public class HardwareRecorder {

    private static final String TAG = HardwareRecorder.class.getSimpleName();

    private MediaEncoder.MediaEncoderListener mEncoderListener
            = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(MediaEncoder encoder) {
            if (null != mEncoderStatusCallback)
                mEncoderStatusCallback.onPrepared(encoder);
        }

        @Override
        public void onStarted(MediaEncoder encoder) {
            if (null != mEncoderStatusCallback)
                mEncoderStatusCallback.onStarted(encoder);
        }

        @Override
        public void onStopped(MediaEncoder encoder) {
            if (null != mEncoderStatusCallback)
                mEncoderStatusCallback.onStopped(encoder);
        }

        @Override
        public void onReleased(MediaEncoder encoder) {
            if (null != mEncoderStatusCallback)
                mEncoderStatusCallback.onReleased(encoder);
            HardwareEncoder.getInstance().destroyRecorder();
        }

        @Override
        public void onError(int errorCode) {
            if (null != mEncoderStatusCallback)
                mEncoderStatusCallback.onError(errorCode);
        }
    };

    private VideoRecorder.HardwareEncoderStatusCallback mEncoderStatusCallback;

    public HardwareRecorder(VideoRecorder.HardwareEncoderStatusCallback callback) {
        mEncoderStatusCallback = callback;
    }

    public void prepare(String outputPath, boolean enableAudio, int width, int height) {
        HardwareEncoder.getInstance()
                .prepareRecorder()
                .setOutputPath(outputPath)
                .enableAudioRecord(enableAudio)
                .initRecorder(width, height, mEncoderListener);
    }

    public void startRecording(EGLContext sharedContext) {
        HardwareEncoder.getInstance().startRecording(sharedContext);
    }

    public void frameAvailable() {
        HardwareEncoder.getInstance().frameAvailable();
    }

    public void drawRecordFrame(int texId) {
        HardwareEncoder.getInstance().drawRecordFrame(texId);
    }

    public void stopRecording() {
        HardwareEncoder.getInstance().stopRecording();
    }


}
