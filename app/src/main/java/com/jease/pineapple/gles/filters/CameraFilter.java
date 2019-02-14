package com.jease.pineapple.gles.filters;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import com.jease.pineapple.utils.MGLUtils;

public class CameraFilter extends GLFilter {

    private OESFilter mOESFilter;

    private float[] mStMatrix = new float[16];

    private SurfaceTexture mSurfaceTexture;

    private int mOesTexId;

    public CameraFilter(Resources res) {
        super(res);
        mOESFilter = new OESFilter(mRes);
    }

    @Override
    protected void onCreate() {
        mOESFilter.create();
        mOesTexId = MGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mOesTexId);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mOESFilter.setSize(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void draw() {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mStMatrix);
            mOESFilter.setStMatrix(mStMatrix);
        }
        mOESFilter.setTextureId(mOesTexId);
        mOESFilter.draw();
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }
}
