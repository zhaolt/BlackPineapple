package com.jease.pineapple.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.jease.pineapple.utils.DensityUtils;

public class CameraShutter implements ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener {

    public static final int MODE_PHOTO_NORMAL = 0x101;

    public static final int MODE_PHOTO_WORK = 0x102;

    public static final int MODE_VIDEO_NORMAL = 0x103;

    public static final int MODE_VIDEO_TO_WORK = 0x104;

    public static final int MODE_VIDEO_WORK = 0x105;

    private static final String VIDEO_MODE_COLOR = "#FD415F";

    private static final String PHOTO_MODE_COLOR = "#FFFFFF";

    private static final String RING_COLOR = "#80FD415F";

    private View mParentView;

    private boolean mBreathingFlags;

    private static final long ANIM_DURATION = 300L;

    private float mAnimFraction = 1f;

    private int mDefaultRingWidth;

    private int mWorkingModeRingMaxWidth;

    private ValueAnimator mValueAnimator;

    private Paint mPaint;

    private int mCenterRectMinSize;

    private int mCenterRectMaxSize;

    private int mCenterRectMinAngle;

    private int mCenterRectMaxAngle;

    private Matrix mMatrix;

    private float mDownX;

    private float mDownY;

    private int mCenterX;

    private int mCenterY;

    private int mMinRadius;

    private int mMaxRadius;

    private int mMiddleRadius;

    private int mMode = MODE_VIDEO_NORMAL;

    public CameraShutter(View parent) {
        mParentView = parent;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor(VIDEO_MODE_COLOR));
        mMinRadius = (int) DensityUtils.dp2px(30f);
        mMiddleRadius = (int) DensityUtils.dp2px(40f);
        mMaxRadius = (int) DensityUtils.dp2px(60f);
        mDefaultRingWidth = (int) DensityUtils.dp2px(6f);
        mWorkingModeRingMaxWidth = (int) DensityUtils.dp2px(10f);
        mCenterRectMinAngle = (int) DensityUtils.dp2px(5f);
        mCenterRectMaxAngle = (int) DensityUtils.dp2px(35f);
        mCenterRectMinSize = (int) DensityUtils.dp2px(16f);
        mCenterRectMaxSize = (int) (Math.sqrt(Math.pow(mMinRadius * 2, 2)) / 2);
        mCenterX = mCenterY = mMiddleRadius;
        mMatrix = new Matrix();
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.concat(mMatrix);
        int radius;
        int alpha = 0xb2;
        int ringRadius;
        int tmpAngle;
        int rectSize;
        int ringColor = Color.parseColor(RING_COLOR);
        switch (mMode) {
            case MODE_PHOTO_NORMAL:
                break;
            case MODE_PHOTO_WORK:
                break;
            case MODE_VIDEO_NORMAL:
                radius = mMinRadius;
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mDefaultRingWidth);
                mPaint.setColor(ringColor);
                ringRadius = mMiddleRadius - mDefaultRingWidth;
                canvas.drawCircle(mCenterX, mCenterY, ringRadius, mPaint);
                drawBasicCircle(canvas, mCenterX, mCenterY, radius);
                mAnimFraction = 0;
                break;
            case MODE_VIDEO_TO_WORK:
                tmpAngle = (int) (mCenterRectMaxAngle - (mCenterRectMaxAngle - mCenterRectMinAngle)
                        * mAnimFraction);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.parseColor(VIDEO_MODE_COLOR));
                rectSize = (int) (mCenterRectMaxSize - (mCenterRectMaxSize - mCenterRectMinSize)
                        * mAnimFraction);
                drawSquare(canvas, mCenterX, tmpAngle, rectSize);
                radius = (int) (mMiddleRadius + (mMaxRadius - mMiddleRadius) * mAnimFraction);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mDefaultRingWidth);
                mPaint.setColor(Color.parseColor(RING_COLOR));
                ringRadius = radius - mDefaultRingWidth / 2;
                canvas.drawCircle(mCenterX, mCenterY, ringRadius, mPaint);
                if (mAnimFraction >= 1.0f) {
                    mMode = MODE_VIDEO_WORK;
                    mParentView.invalidate();
                }
                break;
            case MODE_VIDEO_WORK:
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.parseColor(VIDEO_MODE_COLOR));
                drawSquare(canvas, mCenterX, mCenterRectMinAngle, mCenterRectMinSize);
                radius = mMaxRadius;
                drawBreathingCircle(canvas, mCenterX, mCenterY, radius, Color.parseColor(RING_COLOR));
                mParentView.postInvalidateDelayed(50);
                break;
        }
        canvas.restore();
    }

    public int getMiddleRadius() {
        return mMiddleRadius;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    private void drawBreathingCircle(Canvas canvas, int centerX,
                                     int centerY, float radius, int color) {
        float ringRadius;
        float iS = DensityUtils.dp2px(1f);
        if (mDefaultRingWidth <= mWorkingModeRingMaxWidth && !mBreathingFlags) {
            mDefaultRingWidth += iS;
            if (mDefaultRingWidth >= mWorkingModeRingMaxWidth) {
                mBreathingFlags = true;
            }
        }
        if (mDefaultRingWidth >= DensityUtils.dp2px(5f) && mBreathingFlags) {
            mDefaultRingWidth -= iS;
            if (mDefaultRingWidth <= DensityUtils.dp2px(5f)) {
                mBreathingFlags = false;
            }
        }
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mDefaultRingWidth);
        ringRadius = radius - mDefaultRingWidth / 2;
        canvas.drawCircle(centerX, centerY, ringRadius, mPaint);
    }

    private void startAnimation() {
        if (null != mValueAnimator && mValueAnimator.isRunning())
            mValueAnimator.cancel();
        mValueAnimator = ValueAnimator.ofFloat(mAnimFraction, 1f);
        mValueAnimator.setDuration(ANIM_DURATION);
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.addListener(this);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.start();
    }

    private void drawSquare(Canvas canvas, int centerX, float tmpAngle, float tmpSquareC) {
        float lt = centerX - tmpSquareC;
        float rb = centerX + tmpSquareC;
        RectF rectF = new RectF(lt, lt, rb, rb);
        canvas.drawRoundRect(rectF, tmpAngle, tmpAngle, mPaint);
    }

    private void drawBasicCircle(Canvas canvas, int centerX, int centerY, float radius) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor(VIDEO_MODE_COLOR));
        canvas.drawCircle(centerX, centerY, radius, mPaint);
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {

    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mAnimFraction = (float) animation.getAnimatedValue();
        mParentView.invalidate();
    }

    public boolean onTouch(MotionEvent event) {
        return false;
    }
}
