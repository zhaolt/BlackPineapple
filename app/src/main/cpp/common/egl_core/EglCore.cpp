//
// Created by ZhaoLiangtai on 2019/2/12.
//

#include "EglCore.h"

EglCore::EglCore() {
    mPfneglPresentationTimeANDROID = 0;
    mDisplay = EGL_NO_DISPLAY;
    mContext = EGL_NO_CONTEXT;
}

EglCore::~EglCore() {
}

void EglCore::Release() {
    if (EGL_NO_DISPLAY != mDisplay && EGL_NO_CONTEXT != mContext) {
        eglMakeCurrent(mDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(mDisplay, mContext);
    }
    mDisplay = EGL_NO_DISPLAY;
    mContext = EGL_NO_CONTEXT;
}

void EglCore::ReleaseSurface(EGLSurface eglSurface) {
    eglDestroySurface(mDisplay, eglSurface);
    eglSurface = EGL_NO_SURFACE;
}

EGLContext EglCore::GetContext() {
    return mContext;
}

EGLDisplay EglCore::GetDisplay() {
    return mDisplay;
}

EGLConfig EglCore::GetConfig() {
    return mConfig;
}

EGLSurface EglCore::CreateWindowSurface(ANativeWindow *_window) {
    EGLSurface surface = NULL;
    EGLint format;
    if (_window == NULL) {
        LOGE("EGLCore::createWindowSurface _window is NULL");
        return NULL;
    }
    if (!eglGetConfigAttrib(mDisplay, mConfig, EGL_NATIVE_VISUAL_ID, &format)) {
        Release();
        return surface;
    }
    ANativeWindow_setBuffersGeometry(_window, 0, 0, format);
    if (!(surface = eglCreateWindowSurface(mDisplay, mConfig, _window, 0))) {
        LOGE("eglCreateWindowSurface() returned error %d", eglGetError());
    }
    return surface;
}

EGLSurface EglCore::CreateOffscreenSurface(int width, int height) {
    EGLSurface surface;
    EGLint pBufferAttributes[] = {EGL_WIDTH, width, EGL_HEIGHT, height, EGL_NONE, EGL_NONE};
    if (!(surface = eglCreatePbufferSurface(mDisplay, mConfig, pBufferAttributes))) {
        LOGE("eglCreatePbufferSurface() returned error %d", eglGetError());
    }
    return surface;
}

int EglCore::SetPresentationTime(EGLSurface surface, khronos_stime_nanoseconds_t nsecs) {
    mPfneglPresentationTimeANDROID(mDisplay, surface, nsecs);
    return 1;
}

int EglCore::QuerySurface(EGLSurface eglSurface, int what) {
    int value = -1;
    eglQuerySurface(mDisplay, eglSurface, what, &value);
    return value;
}

bool EglCore::SwapBuffers(EGLSurface eglSurface) {
    return eglSwapBuffers(mDisplay, eglSurface);
}

bool EglCore::MakeCurrent(EGLSurface eglSurface) {
    return eglMakeCurrent(mDisplay, eglSurface, eglSurface, mContext);
}

void EglCore::DoneCurrent() {
    eglMakeCurrent(mDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
}

bool EglCore::Init() {
    return this->Init(NULL);
}

bool EglCore::InitWithSharedContext() {
    return false;
}

bool EglCore::Init(EGLContext sharedContext) {
    EGLint numConfigs;
    const EGLint attribs[] = {
            EGL_BUFFER_SIZE, 32,
            EGL_ALPHA_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };
    if ((mDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay() returned error %d", eglGetError());
        return false;
    }
    if (!eglInitialize(mDisplay, 0, 0)) {
        LOGE("eglInitialize() returned error %d", eglGetError());
        return false;
    }
    if (!eglChooseConfig(mDisplay, attribs, &mConfig, 1, &numConfigs)) {
        LOGE("eglChooseConfig() returned error %d", eglGetError());
        Release();
        return false;
    }
    EGLint eglContextAttributes[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    if (!(mContext = eglCreateContext(mDisplay, mConfig,
            NULL == sharedContext ? EGL_NO_CONTEXT : sharedContext,
            eglContextAttributes))) {
        LOGE("eglCreateContext() returned error %d", eglGetError());
        Release();
        return false;
    }
    mPfneglPresentationTimeANDROID = (PFNEGLPRESENTATIONTIMEANDROIDPROC) eglGetProcAddress("eglPresentationTimeANDROID");
    if (!mPfneglPresentationTimeANDROID)
        LOGE("eglPresentationTimeANDROID is not available!");
    return true;
}