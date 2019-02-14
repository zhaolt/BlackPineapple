package com.jease.pineapple.record;

import android.graphics.Rect;
import android.graphics.RectF;

public class FocusParams {

    private static final float FOCUS_COEF = 1.0f;

    private static final float METER_COEF = 1.5f;

    public static final int FOCUS_SIDE = 1000;

    private float mX;

    private float mY;

    private int mWidth;

    private int mHeight;

    public FocusParams(float x, float y, int width, int height) {
        mX = x;
        mY = y;
        mWidth = width;
        mHeight = height;
    }

    public Rect getFocusRect(boolean isPortrait) {
        int width = mWidth;
        int height = mHeight;
        if (isPortrait) {
            width = mHeight;
            height = mWidth;
        }
        return calculateTapArea(mX, mY, FOCUS_COEF, width, height);
    }

    public Rect getMeteringRect(boolean isPortrait) {
        int width = mWidth;
        int height = mHeight;
        if (isPortrait) {
            width = mHeight;
            height = mWidth;
        }
        return calculateTapArea(mX, mY, METER_COEF, width, height);
    }

    /**
     * 这里的计算方向是以camera传感器方向(手机横向，机尾在右)
     * 左上角坐标为(-1000, -1000)右下角坐标为(1000, 1000)
     * 因为目前app是固定竖屏方向，所以x,y坐标要做转换
     * @param x
     * @param y
     * @param coefficient
     * @param width
     * @param height
     * @return
     */
    private Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        float halfW = width / 2f;
        float halfH = height / 2f;
        int centerX = (int) ((x - halfW) / halfW * FOCUS_SIDE);
        int centerY = (int) ((halfH - y) / halfH * FOCUS_SIDE);
        int left = clamp(centerX - areaSize / 2, -FOCUS_SIDE, FOCUS_SIDE);
        int top = clamp(centerY - areaSize / 2, -FOCUS_SIDE, FOCUS_SIDE);
        int right = left + areaSize > FOCUS_SIDE ? FOCUS_SIDE : left + areaSize;
        int bottom = top + areaSize > FOCUS_SIDE ? FOCUS_SIDE : top + areaSize;
        RectF rectF = new RectF(left, top, right, bottom);
        return new Rect(
                Math.round(rectF.left),
                Math.round(rectF.top),
                Math.round(rectF.right),
                Math.round(rectF.bottom)
        );
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
