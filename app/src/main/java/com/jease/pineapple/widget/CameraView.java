package com.jease.pineapple.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.jease.pineapple.utils.DensityUtils;

public class CameraView extends FrameLayout {

    private static final String TAG = CameraView.class.getSimpleName();

    private CameraShutter mCameraShutter;

    private boolean isInited = false;

    private Paint mPaint;

    private LinearGradient mTopGradient;

    private LinearGradient mBottomGradient;

    private Rect mTopRect;

    private Rect mBottomRect;

    private static final String SHAPE_START_COLOR = "#90000000";

    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mCameraShutter = new CameraShutter(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    public void startShutterVideoAnim() {
        mCameraShutter.startShutterVideoAnim();
    }

    public void stopShutterVideoAnim() {
        mCameraShutter.stopShutterVideoAnim();
    }

    public void setOnShutterClickListener(CameraShutter.OnClickListener listener) {
        if (null != mCameraShutter)
            mCameraShutter.setOnClickListener(listener);
    }

    public void startTakePhoto() {
        if (null != mCameraShutter)
            mCameraShutter.startTakePhoto();
    }

    public void stopTakePhoto() {
        if (null != mCameraShutter)
            mCameraShutter.stopTakePhoto();
    }

    public void changeToVideoMode() {
        if (null != mCameraShutter)
            mCameraShutter.changeToVideoMode();
    }

    public void changeToPhotoMode() {
        if (null != mCameraShutter)
            mCameraShutter.changeToPhotoMode();
    }

    public void setShutterVisiblity(boolean isVisible) {
        if (null != mCameraShutter)
            mCameraShutter.setVisible(isVisible);
    }

    private void restoration(int width, int height) {
        int offsetX = width / 2 - mCameraShutter.getMiddleRadius();
        int offsetY = (int) (height - DensityUtils.dp2px(48) - mCameraShutter.getMiddleRadius() * 2);
        mCameraShutter.setCenterPoint(offsetX + mCameraShutter.getMiddleRadius(),
                offsetY + mCameraShutter.getMiddleRadius());
        mCameraShutter.getMatrix().reset();
        mCameraShutter.getMatrix().postTranslate(offsetX, offsetY);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isInited && getWidth() != 0 && getHeight() != 0) {
            restoration(getWidth(), getHeight());
            int shapeWidth = getWidth();
            int shapeHeight = getHeight() / 5;
            mTopGradient = new LinearGradient(shapeWidth, 0, shapeWidth, shapeHeight,
                    Color.parseColor(SHAPE_START_COLOR), Color.TRANSPARENT, Shader.TileMode.CLAMP);
            mBottomGradient = new LinearGradient(shapeWidth, getHeight(), shapeWidth,
                    getHeight() - shapeHeight, Color.parseColor(SHAPE_START_COLOR),
                    Color.TRANSPARENT, Shader.TileMode.CLAMP);
            mPaint.setShader(mTopGradient);
            mTopRect = new Rect(0, 0, shapeWidth, shapeHeight);
            mBottomRect = new Rect(0, getHeight() - shapeHeight, shapeWidth, getHeight());
            isInited = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mCameraShutter.onTouch(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(mTopRect, mPaint);
        mPaint.setShader(mBottomGradient);
        canvas.drawRect(mBottomRect, mPaint);
        mCameraShutter.draw(canvas);
    }
}
