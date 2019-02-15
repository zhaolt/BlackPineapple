package com.jease.pineapple.gles.filters;

import android.content.res.Resources;

public class NoFilter extends GLFilter {

    public NoFilter(Resources res) {
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
