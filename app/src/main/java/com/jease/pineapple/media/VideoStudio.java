package com.jease.pineapple.media;

public class VideoStudio {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("video-studio");
    }

    private static final class SingleHandle {
        private static final VideoStudio INSTANCE = new VideoStudio();
    }

    public static VideoStudio getInstance() {
        return SingleHandle.INSTANCE;
    }

    private VideoStudio() {
    }

    public native String showFFmpegInfo();
}
