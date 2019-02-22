package com.jease.pineapple.record;

import com.jease.pineapple.common.Constants;
import com.jease.pineapple.gles.GLController;

import java.lang.ref.WeakReference;

public class VideoRecorder {

    private static final String TAG = VideoRecorder.class.getSimpleName();

    private WeakReference<GLController> mWeakGLController;

    private float mBpp = Constants.Video.BPP;

    private int mFrameRate = Constants.Video.FRAME_RATE;

    private int mSampleRate = Constants.Video.SAMPLE_RATE;

    private int mChannels = Constants.Video.CHANNELS;

    private int mAudioBitRate = Constants.Video.AUDIO_BIT_RATE;

    private String mOutputPath;

    private int mVideoWidth;

    private int mVideoHeight;

    private static final class Singleton {
        private static final VideoRecorder INSTANCE = new VideoRecorder();
    }

    private VideoRecorder() {}

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

    public VideoRecorder frameRate(int frameRate) {
        mFrameRate = frameRate;
        return this;
    }

    public VideoRecorder audioParams(int sampleRate, int channels, int bitRate) {
        mSampleRate = sampleRate;
        mChannels = channels;
        mAudioBitRate = bitRate;
        return this;
    }

}
