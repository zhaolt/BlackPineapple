package com.jease.pineapple.gles;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.jease.pineapple.utils.MGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class GLDrawer {

    protected static final String VERTEX_SOURCE =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "uniform mat4 aMVPMatrix;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    gl_Position = aMVPMatrix * aPosition;\n" +
            "    vTexCoord = aTexCoord;\n" +
            "}";

    protected static final String FRAGMENT_SOURCE =
            "precision mediump float;\n" +
            "varying vec2 vTexCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTexCoord);\n" +
            "}";

    protected static final float[] VERTEX = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    protected static final float[] TEX_COORD = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    protected FloatBuffer mVertexBuf;
    protected FloatBuffer mTexCoordBuf;

    protected static final float[] OM;

    static {
        OM = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    protected float[] mMvpMatrix = Arrays.copyOf(OM, 16);

    protected int mProgram;
    protected int mPositionLoc;
    protected int mTexCoordLoc;
    protected int mMVPMatrixLoc;

    public GLDrawer() {
        mVertexBuf = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuf.position(0);
        mTexCoordBuf = ByteBuffer.allocateDirect(TEX_COORD.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_COORD);
        mTexCoordBuf.position(0);
        mProgram = MGLUtils.createProgram(VERTEX_SOURCE, FRAGMENT_SOURCE);
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "aMVPMatrix");
    }

    public void scaleM(float scaleX, float scaleY) {
        Matrix.scaleM(mMvpMatrix, 0, scaleX, scaleY, 1.0f);
    }

    public void release() {
        if (mProgram >= 0)
            GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
    }

    protected void useProgram() {
        GLES20.glUseProgram(mProgram);
        MGLUtils.checkGlError("glUseProgram: " + mProgram);
    }

    protected void bindTexture(int texId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        MGLUtils.checkGlError("glActiveTexture: " + GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        MGLUtils.checkGlError("glBindTexture: " + texId);
    }

    protected void setExpandData() {
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMvpMatrix, 0);
    }

    public void draw(int textureID) {
        useProgram();
        bindTexture(textureID);
        setExpandData();
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        GLES20.glVertexAttribPointer(mPositionLoc, 2, GLES20.GL_FLOAT, false, 0, mVertexBuf);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuf);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }
}
