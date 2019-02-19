package com.jease.pineapple.gles.filters;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.jease.pineapple.utils.MGLUtils;
import com.jease.pineapple.utils.MatrixUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public abstract class GLFilter {

    private static final String TAG = GLFilter.class.getSimpleName();

    public static final float[] OM = MatrixUtils.getOriginalMatrix();

    private static final float[] VERTEX = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    private static final float[] TEX_COORD = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    protected int mProgram = -1;

    protected int mPositionLoc;

    protected int mTexCoordLoc;

    protected int mMvpMatrixLoc;

    protected int mTextureLoc;

    protected Resources mRes;

    protected FloatBuffer mVertexBuf;

    protected FloatBuffer mTexCoordBuf;

    private float[] mMvpMatrix = Arrays.copyOf(OM, 16);

    private int mTextureId;


    public GLFilter(Resources res) {
        mRes = res;
        initBuffer();
    }

    public final void setSize(int width, int height) {
        onSizeChanged(width, height);
    }

    public final void setTextureId(int textureId) {
        mTextureId = textureId;
    }

    public final int getTextureId() {
        return mTextureId;
    }

    public float[] getMvpMatrix() {
        return mMvpMatrix;
    }

    protected void initBuffer() {
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
    }

    public final void create() {
        onCreate();
    }

    protected abstract void onCreate();

    protected abstract void onSizeChanged(int width, int height);

    public void draw() {
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    public void release() {
        if (mProgram >= 0) {
            GLES20.glDeleteProgram(mProgram);
        }
        mProgram = -1;
    }

    protected void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void onUseProgram() {
        GLES20.glUseProgram(mProgram);
    }

    public int getOutputTexture() {
        return -1;
    }

    protected void onSetExpandData() {
        GLES20.glUniformMatrix4fv(mMvpMatrixLoc, 1, false, mMvpMatrix, 0);
    }

    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
        GLES20.glUniform1i(mTextureLoc, 0);
    }

    protected void onDraw() {
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        GLES20.glVertexAttribPointer(mPositionLoc, 2, GLES20.GL_FLOAT, false,
                0, mVertexBuf);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false,
                0, mTexCoordBuf);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }

    protected final void createProgramByAssetsFile(String vertex, String fragment) {
        createProgram(readResShader(mRes, vertex), readResShader(mRes, fragment));
    }

    protected final void createProgram(String vertex, String fragment) {
        mProgram = MGLUtils.createProgram(vertex, fragment);
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mMvpMatrixLoc = GLES20.glGetUniformLocation(mProgram, "aMvpMatrix");
        mTextureLoc = GLES20.glGetUniformLocation(mProgram, "sTexture");
    }

    public static String readResShader(Resources res, String path) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = res.getAssets().open(path);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString().replaceAll("\\r\\n", "\n");
    }
}
