package com.jease.pineapple.widget;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CameraShutter {

    public static final int MODE_PHOTO_NORMAL = 0x101;

    public static final int MODE_PHOTO_WORK = 0x102;

    public static final int MODE_VIDEO_NORMAL = 0x103;

    public static final int MODE_VIDEO_TO_WORK = 0x104;

    public static final int MODE_VIDEO_WORK = 0x105;

    private static final String VIDEO_MODE_COLOR = "#FD415F";

    private static final String PHOTO_MODE_COLOR = "#FFFFFF";

    private static final String RING_COLOR = "#80FFFFFF";

    private View mParentView;

    private static final long ANIM_DURATION = 300L;

    private float mAnimFraction = 1f;

    private int mWorkingModeRingWidth;

    private int mWorkingModeRingMaxWidth;

    private ValueAnimator mValueAnimator;

    private Paint mPaint;

    private int mCenterRectMinSize;

    private int mCenterRectMaxSize;

    private int mCenterRectMinAngle;

    private int mCenterRectMaxAngle;

    private float mDownX;

    private float mDownY;

    private int mCenterX;

    private int mCenterY;

    public CameraShutter(View parent) {
        mParentView = parent;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor(VIDEO_MODE_COLOR));
    }
}
