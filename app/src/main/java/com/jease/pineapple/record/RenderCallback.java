package com.jease.pineapple.record;

import android.opengl.GLSurfaceView;

public interface RenderCallback extends GLSurfaceView.Renderer {
    void onSurfaceDestroyed();
}
