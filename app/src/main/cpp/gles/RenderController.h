//
// Created by ZhaoLiangtai on 2019/2/20.
//

#ifndef BLACKPINEAPPLE_RENDERCONTROLLER_H
#define BLACKPINEAPPLE_RENDERCONTROLLER_H

#include <common/message/Handler.h>

enum RenderThreadMessage {
    MSG_RENDER_FRAME = 0,
    MSG_EGL_THREAD_CREATE,
    MSG_EGL_CREATE_PREVIEW_SURFACE,
    MSG_EGL_DESTROY_PREVIEW_SURFACE,

};

class RenderController {

};

class RenderHandler : public Handler {

private:
    RenderController *mRenderController;

public:
    RenderHandler(RenderController *controller, MessageQueue *queue) : Handler(queue) {
        mRenderController = controller;
    }

    void handleMessage(Message *msg) {

    }
};




#endif //BLACKPINEAPPLE_RENDERCONTROLLER_H
