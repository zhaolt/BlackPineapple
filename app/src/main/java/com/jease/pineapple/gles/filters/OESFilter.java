package com.jease.pineapple.gles.filters;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.util.Arrays;

public class OESFilter extends GLFilter {

    private int mStMatrixLoc;

    private float[] mStMatrix = Arrays.copyOf(OM, 16);

    public OESFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("camera/camera_vertex.glsl",
                "camera/camera_fragment.glsl");
        mStMatrixLoc = GLES20.glGetUniformLocation(mProgram, "aStMatrix");
    }

    public void setStMatrix(float[] matrix) {
        mStMatrix = matrix;
    }

    @Override
    protected void onSizeChanged(int width, int height) {
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        GLES20.glUniformMatrix4fv(mStMatrixLoc, 1, false, mStMatrix, 0);
    }

    @Override
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId());
        GLES20.glUniform1i(mTextureLoc, 0);
    }
}
