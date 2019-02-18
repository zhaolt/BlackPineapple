package com.jease.pineapple.gles.filters;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.jease.pineapple.utils.MGLUtils;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GroupFilter extends GLFilter {

    private static final String TAG = GroupFilter.class.getSimpleName();

    private Queue<GLFilter> mFilterQueue;

    private ArrayList<GLFilter> mFilters;

    private int mWidth;

    private int mHeight;

    private int mSize;

    private int mTexSize = 2;

    private int mFrameBufId;

    private int[] mTextures = new int[mTexSize];

    private int mTexIndex;


    public GroupFilter(Resources res) {
        super(res);
        mFilters = new ArrayList<>();
        mFilterQueue = new ConcurrentLinkedQueue<>();
    }

    public void addFilter(final GLFilter filter) {
        mFilterQueue.add(filter);
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        updateFilter();
        mFrameBufId = MGLUtils.createFrameBuffer();
        createTextures();
    }

    @Override
    public void draw() {
        updateFilter();
        mTexIndex = 0;
        if (mSize > 0) {
            for (GLFilter f : mFilters) {
                MGLUtils.bindFrameTexture(mFrameBufId, mTextures[mTexIndex % 2]);
                GLES20.glViewport(0, 0, mWidth, mHeight);
                if (mTexIndex == 0)
                    f.setTextureId(getTextureId());
                else
                    f.setTextureId(mTextures[(mTexIndex - 1) % 2]);
                f.draw();
                MGLUtils.unBindFrameBuffer();
                mTexIndex++;
            }
        }
    }

    public void clearAll() {
        mFilterQueue.clear();
        mFilters.clear();
        mSize = 0;
    }

    private void updateFilter() {
        GLFilter f;
        while ((f = mFilterQueue.poll()) != null) {
            f.create();
            f.setSize(mWidth, mHeight);
            mFilters.add(f);
            mSize++;
        }
    }

    @Override
    public int getOutputTexture() {
        return mSize == 0 ? getTextureId() : mTextures[(mTexIndex - 1) % 2];
    }

    private void createTextures() {
        for (int i = 0; i < mTextures.length; i++) {
            mTextures[i] = MGLUtils.createTexture(mWidth, mHeight);
        }
    }
}
