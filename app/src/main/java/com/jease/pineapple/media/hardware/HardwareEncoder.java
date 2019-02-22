package com.jease.pineapple.media.hardware;

import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.jease.pineapple.BuildConfig;
import com.jease.pineapple.gles.EGLBase;
import com.jease.pineapple.gles.GLDrawer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class HardwareEncoder {

    private static final String TAG = HardwareEncoder.class.getSimpleName();

    private static final boolean VERBOSE = BuildConfig.DEBUG;

    private final Object mReadyFence = new Object();

    /**
     * 初始化录制器
     */
    static final int MSG_INIT_RECORDER = 0;
    /**
     * 帧可用
     */
    static final int MSG_FRAME_AVAILABLE = 1;
    /**
     * 渲染帧
     */
    static final int MSG_DRAW_FRAME = 2;
    /**
     * 停止录制
     */
    static final int MSG_STOP_RECORDING = 3;
    /**
     * 暂停录制
     */
    static final int MSG_PAUSE_RECORDING = 4;
    /**
     * 继续录制
     */
    static final int MSG_CONTINUE_RECORDING = 5;
    /**
     * 是否录制音频
     */
    static final int MSG_ENABLE_AUDIO = 6;
    /**
     * 退出
     */
    static final int MSG_QUIT = 7;

    static final int MSG_INIT_SWAYING = 8;

    private String mOutputPath;

    private RecordThread mRecordThread;

    private static final class SingleTon {
        public static final HardwareEncoder INSTANCE = new HardwareEncoder();
    }

    private HardwareEncoder() {}

    public static HardwareEncoder getInstance() {
        return SingleTon.INSTANCE;
    }

    public HardwareEncoder prepareRecorder() {
        synchronized (mReadyFence) {
            if (mRecordThread == null) {
                mRecordThread = new RecordThread(this);
                mRecordThread.start();
                mRecordThread.waitUntilReady();
            }
        }
        return this;
    }

    public void destroyRecorder() {
        synchronized (mReadyFence) {
            if (mRecordThread != null) {
                Handler handler = mRecordThread.getHandler();
                if (handler != null)
                    handler.sendMessage(handler.obtainMessage(MSG_QUIT));
                mRecordThread = null;
            }
        }
    }

    public void initRecorder(int width, int height, MediaEncoder.MediaEncoderListener listener) {
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_INIT_RECORDER, width, height, listener));
    }

    public void initSwaying(int width, int height, MediaEncoder.MediaEncoderListener listener) {
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_INIT_SWAYING, width, height, listener));
    }


    public void startRecording(final EGLContext sharedContext) {
        Handler handler = mRecordThread.getHandler();
        if (null != handler) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mRecordThread.startRecording(sharedContext);
                }
            });
        }
    }

    public void frameAvailable() {
        if (null == mRecordThread) return;
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_FRAME_AVAILABLE));
    }

    public void drawRecordFrame(int texture) {
        if (null == mRecordThread) return;
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_DRAW_FRAME, texture, 0));
    }

    public void stopRecording() {
        if (mRecordThread == null)
            return;
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_STOP_RECORDING));
    }

    public void pauseRecord() {
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_PAUSE_RECORDING));
    }

    public void continueRecord() {
        Handler handler = mRecordThread.getHandler();
        if (null != handler)
            handler.sendMessage(handler.obtainMessage(MSG_CONTINUE_RECORDING));
    }

    public HardwareEncoder enableHDMode(boolean enable) {

        return this;
    }

    public HardwareEncoder enableAudioRecord(boolean enable) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_ENABLE_AUDIO, enable));
        }
        return this;
    }

    public HardwareEncoder setOutputPath(String path) {
        mOutputPath = path;
        return this;
    }

    public String getOutputPath() {
        return mOutputPath;
    }

    private static class RecordThread extends Thread {

        private final Object mReadyFence = new Object();

        private boolean mReady;

        private int mVideoWidth, mVideoHeight;

        private EGLBase mEgl;

        private EGLBase.EglSurface mEglSurface;

        private MediaMuxerWrapper mMuxerWrapper;

        private boolean enableAudio = true;

        private boolean isRecording = false;

        private long mProcessTime = 0;

        private RecordHandler mHandler;

        private WeakReference<HardwareEncoder> mWeakRecorder;

        private GLDrawer mDrawer;

        RecordThread(HardwareEncoder manager) {
            mWeakRecorder = new WeakReference<>(manager);
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (mReadyFence) {
                mHandler = new RecordHandler(this);
                mReady = true;
                mReadyFence.notify();
            }
            Looper.loop();
            if (VERBOSE)
                Log.i(TAG, "Record thread exiting.");
            synchronized (mReadyFence) {
                release();
                mReady = false;
                mHandler = null;
            }
        }

        void waitUntilReady() {
            synchronized (mReadyFence) {
                while (!mReady) {
                    try {
                        mReadyFence.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        void initRecorder(int width, int height, MediaEncoder.MediaEncoderListener listener) {
            if (VERBOSE)
                Log.d(TAG, "init recorder.");
            synchronized (mReadyFence) {
                long time = System.currentTimeMillis();
                mVideoWidth = width;
                mVideoHeight = height;
                String filePath = mWeakRecorder.get().getOutputPath();
                if (TextUtils.isEmpty(filePath))
                    throw new IllegalArgumentException("file path must no be empty!");
                File file = new File(filePath);
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                try {
                    mMuxerWrapper = new MediaMuxerWrapper(file.getAbsolutePath());
                    new MediaVideoEncoder(mMuxerWrapper, listener, mVideoWidth, mVideoHeight);
                    if (enableAudio)
                        new MediaAudioEncoder(mMuxerWrapper, listener);
                    mMuxerWrapper.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mProcessTime += (System.currentTimeMillis() - time);
            }
        }

        void initSwaying(int width, int height, MediaEncoder.MediaEncoderListener listener) {
            if (VERBOSE)
                Log.d(TAG, "init recorder.");
            synchronized (mReadyFence) {
                long time = System.currentTimeMillis();
                mVideoWidth = width;
                mVideoHeight = height;
                String filePath = mWeakRecorder.get().getOutputPath();
                if (TextUtils.isEmpty(filePath))
                    throw new IllegalArgumentException("file path must no be empty!");
                File file = new File(filePath);
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                try {
                    mMuxerWrapper = new MediaMuxerWrapper(file.getAbsolutePath());
                    new MediaVideoEncoder(mMuxerWrapper, listener, mVideoWidth, mVideoHeight);
                    if (enableAudio)
                        new MediaAudioEncoder(mMuxerWrapper, listener);
                    mMuxerWrapper.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mProcessTime += (System.currentTimeMillis() - time);
            }
        }


        void startRecording(EGLContext sharedContext) {
            if (VERBOSE)
                Log.d(TAG, "start recording.");
            synchronized (mReadyFence) {
                if (mMuxerWrapper.getVideoEncoder() == null)
                    return;
                mEgl = new EGLBase(sharedContext, false, true);
                mEglSurface = mEgl.createFromSurface(
                        ((MediaVideoEncoder) mMuxerWrapper.getVideoEncoder()).getInputSurface());
                mEglSurface.makeCurrent();
                initRecordingFileter();
                if (mMuxerWrapper != null)
                    mMuxerWrapper.startRecording();
                isRecording = true;
            }
        }

        void frameAvailable() {
            if (VERBOSE)
                Log.v(TAG, "frame available");
            synchronized (mReadyFence) {
                if (mMuxerWrapper != null && mMuxerWrapper.getVideoEncoder() != null && isRecording)
                    mMuxerWrapper.getVideoEncoder().frameAvailableSoon();
            }
        }

        void drawRecordingFrame(int currentTexture) {
            if (VERBOSE)
                Log.v(TAG, "draw recording frame");
            synchronized (mReadyFence) {
                if (mEglSurface != null) {
                    mEglSurface.makeCurrent();
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                    mDrawer.draw(currentTexture);
                    mEglSurface.swap();
                }
            }
        }

        void stopRecording() {
            if (VERBOSE) Log.d(TAG, "stop recording.");
            synchronized (mReadyFence) {
                long time = System.currentTimeMillis();
                isRecording = false;
                if (mMuxerWrapper != null) {
                    mMuxerWrapper.stopRecording();
                    mMuxerWrapper = null;
                }
                if (mEglSurface != null) {
                    mEglSurface.release();
                    mEglSurface = null;
                }
                if (VERBOSE) {
                    mProcessTime += (System.currentTimeMillis() - time);
                    Log.d(TAG, "sum of init and release time: " + mProcessTime + "ms");
                    mProcessTime = 0;
                }
            }
        }

        void enableAudioRecording(boolean enable) {
            if (VERBOSE)
                Log.d(TAG, "enable audio recording ? " + enable);
            synchronized (mReadyFence) {
                enableAudio = enable;
            }
        }

        void pauseRecording() {
            if (VERBOSE)
                Log.d(TAG, "pause recording.");
            synchronized (mReadyFence) {
                if (mMuxerWrapper != null && isRecording)
                    mMuxerWrapper.pauseRecording();
            }
        }

        void continueRecording() {
            if (VERBOSE)
                Log.d(TAG, "continue recording.");
            synchronized (mReadyFence) {
                if (mMuxerWrapper != null && isRecording)
                    mMuxerWrapper.continueRecording();
            }
        }

        private void initRecordingFileter() {
            mDrawer = new GLDrawer();
        }


        public RecordHandler getHandler() {
            return mHandler;
        }

        public void release() {
            stopRecording();
            if (null != mDrawer) {
                mDrawer.release();
                mDrawer = null;
            }
            if (mEgl != null) {
                mEgl.release();
                mEgl = null;
            }
        }
    }

    private static class RecordHandler extends Handler {

        private WeakReference<RecordThread> mWeakRecordThread;

        public RecordHandler(RecordThread thread) {
            mWeakRecordThread = new WeakReference<>(thread);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            RecordThread thread = mWeakRecordThread.get();
            if (null == thread) {
                Log.w(TAG, "RecordHandler encoder is null!");
                return;
            }
            switch (what) {
                case MSG_INIT_RECORDER:
                    thread.initRecorder(msg.arg1, msg.arg2,
                            (MediaEncoder.MediaEncoderListener) msg.obj);
                    break;
                case MSG_FRAME_AVAILABLE:
                    thread.frameAvailable();
                    break;
                case MSG_DRAW_FRAME:
                    thread.drawRecordingFrame(msg.arg1);
                    break;
                case MSG_STOP_RECORDING:
                    thread.stopRecording();
                    break;
                case MSG_PAUSE_RECORDING:
                    thread.pauseRecording();
                    break;
                case MSG_CONTINUE_RECORDING:
                    thread.continueRecording();
                    break;
                case MSG_ENABLE_AUDIO:
                    thread.enableAudioRecording((Boolean) msg.obj);
                    break;
                case MSG_QUIT:
                    removeCallbacksAndMessages(null);
                    Looper.myLooper().quit();
                    break;
                case MSG_INIT_SWAYING:
                    thread.initSwaying(msg.arg1, msg.arg2,
                            (MediaEncoder.MediaEncoderListener) msg.obj);
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what = " + what);
            }
        }
    }
}
