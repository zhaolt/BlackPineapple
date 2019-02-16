package com.jease.pineapple.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.jease.pineapple.utils.DensityUtils;

public class CameraView extends FrameLayout {

    private static final String TAG = CameraView.class.getSimpleName();

    private CameraShutter mCameraShutter;

    private boolean isInited = false;

    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mCameraShutter = new CameraShutter(this);
    }

    private void restoration(int width, int height) {
        int offsetX = width / 2 - mCameraShutter.getMiddleRadius();
        int offsetY = (int) (height - DensityUtils.dp2px(48) - mCameraShutter.getMiddleRadius() * 2);
        mCameraShutter.getMatrix().reset();
        mCameraShutter.getMatrix().postTranslate(offsetX, offsetY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isInited && getWidth() != 0 && getHeight() != 0) {
            restoration(getWidth(), getHeight());
            isInited = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCameraShutter.draw(canvas);
    }
}
