package com.jease.pineapple.gles.filters;

import android.content.res.Resources;

public class GrayFilter extends GLFilter {

    public GrayFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.glsl",
                "shader/gray_fragment.glsl");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
