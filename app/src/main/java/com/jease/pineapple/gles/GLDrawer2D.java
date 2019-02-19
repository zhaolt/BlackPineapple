package com.jease.pineapple.gles;

import android.content.res.Resources;

import com.jease.pineapple.gles.filters.GLFilter;

/**
 * Helper class to draw to whole view using specific texture and texture matrix
 */
public class GLDrawer2D extends GLFilter {

    public GLDrawer2D(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.glsl",
                "shader/base_fragment.glsl");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
