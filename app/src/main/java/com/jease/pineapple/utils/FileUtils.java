package com.jease.pineapple.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    private static final String BASE_DIR = "BlackPineapple";

    private static final String VIDEO_DIR = "Video";

    private static final String PHOTO_DIR = "Photo";



    public static String getMediaDir(String dirName) {
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        sb.append(sdcard);
        sb.append(File.separator);
        sb.append(BASE_DIR);
        sb.append(File.separator);
        sb.append(dirName);
        File file = new File(sb.toString());
        if (!file.exists())
            file.mkdirs();
        return sb.toString();
    }

    public static String getVideoPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMediaDir(VIDEO_DIR));
        sb.append(File.separator);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String videoFileName = format.format(new Date());
        sb.append(videoFileName);
        sb.append(".mp4");
        return sb.toString();
    }

    public static String getPhotoPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMediaDir(PHOTO_DIR));
        sb.append(File.separator);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String videoFileName = format.format(new Date());
        sb.append(videoFileName);
        sb.append(".jpg");
        return sb.toString();
    }
}
