package com.jease.pineapple.gles.filters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.jease.pineapple.utils.MGLUtils;

public class LookupFilter extends GLFilter {

    private static final String TAG = LookupFilter.class.getSimpleName();

    private int mMaskTexLoc;

    private int mLutTexId;

    private Bitmap mLutBitmap;

    public LookupFilter(Resources res, Bitmap bitmap) {
        super(res);
        mLutBitmap = bitmap;
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.glsl",
                "shader/lookup_fragment.glsl");
        mMaskTexLoc = GLES20.glGetUniformLocation(mProgram, "maskTexture");
        mLutTexId = MGLUtils.createTexture(mLutBitmap.getWidth(), mLutBitmap.getHeight());
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        if (mLutTexId != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLutTexId);
            if (mLutBitmap != null && !mLutBitmap.isRecycled()) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mLutBitmap, 0);
                mLutBitmap.recycle();
            }
            GLES20.glUniform1i(mMaskTexLoc, 1);
        }
    }

    @Override
    protected void onBindTexture() {
        super.onBindTexture();
    }
}
