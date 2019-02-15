package com.jease.pineapple.gles.filters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.jease.pineapple.utils.MGLUtils;

import java.io.IOException;

public class LookupFilter extends GLFilter {

    private int mMaskTexLoc;

    private int mLutTexId;

    private Bitmap mLutBitmap;

    public LookupFilter(Resources res) {
        super(res);
        try {
            mLutBitmap = BitmapFactory.decodeStream(res.getAssets().open("lut/res_plant_1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/lookup_vertex.glsl",
                "shader/lookup_fragment.glsl");
        mMaskTexLoc = GLES20.glGetUniformLocation(mProgram, "maskTexture");
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mLutTexId = MGLUtils.createTexture(width, height);
        if (mLutBitmap != null && !mLutBitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mLutBitmap, 0);
            mLutBitmap.recycle();
            mLutBitmap = null;
        }
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
    }

    @Override
    protected void onBindTexture() {
        super.onBindTexture();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLutTexId);
        GLES20.glUniform1i(mMaskTexLoc, 1);
    }
}
