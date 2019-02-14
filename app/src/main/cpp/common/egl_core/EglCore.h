//
// Created by ZhaoLiangtai on 2019/2/12.
//

#ifndef BLACKPINEAPPLE_EGLCORE_H
#define BLACKPINEAPPLE_EGLCORE_H

#include "CommonTools.h"
#include <pthread.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <KHR/khrplatform.h>
#include <EGL/eglplatform.h>
#include <android/native_window.h>

typedef EGLBoolean (EGLAPIENTRYP PFNEGLPRESENTATIONTIMEANDROIDPROC)
(EGLDisplay display, EGLSurface surface, khronos_stime_nanoseconds_t time);

class EglCore {
public:
    EglCore();
    virtual ~EglCore();
    bool Init();
    bool Init(EGLContext sharedContext);
    bool InitWithSharedContext();
    EGLSurface CreateWindowSurface(ANativeWindow *_window);
    EGLSurface CreateOffscreenSurface(int width, int height);
    bool MakeCurrent(EGLSurface eglSurface);
    void DoneCurrent();
    bool SwapBuffers(EGLSurface eglSurface);
    int QuerySurface(EGLSurface eglSurface, int what);
    int SetPresentationTime(EGLSurface surface, khronos_stime_nanoseconds_t nsecs);
    void ReleaseSurface(EGLSurface eglSurface);
    void Release();
    EGLContext GetContext();
    EGLDisplay GetDisplay();
    EGLConfig GetConfig();

private:
    EGLDisplay mDisplay;
    EGLConfig mConfig;
    EGLContext mContext;
    PFNEGLPRESENTATIONTIMEANDROIDPROC mPfneglPresentationTimeANDROID;
};


#endif //BLACKPINEAPPLE_EGLCORE_H
