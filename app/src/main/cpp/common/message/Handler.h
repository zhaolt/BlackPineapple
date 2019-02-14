//
// Created by ZhaoLiangtai on 2019/1/31.
//

#ifndef BLACKPINEAPPLE_HANDLER_H
#define BLACKPINEAPPLE_HANDLER_H


#include "MessageQueue.h"

class Handler {
private:
    MessageQueue *mQueue;

public:
    Handler(MessageQueue *queue);
    ~Handler();

    int PostMessage(Message *msg);
    int GetQueueSize();
    virtual void handleMessage(Message *msg) {};
};


#endif //BLACKPINEAPPLE_HANDLER_H
