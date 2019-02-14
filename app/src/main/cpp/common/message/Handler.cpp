//
// Created by ZhaoLiangtai on 2019/1/31.
//

#include "Handler.h"

Handler::Handler(MessageQueue *queue) {
    mQueue = queue;
}

Handler::~Handler() {
}

int Handler::PostMessage(Message *msg) {
    msg->handler = this;
    return mQueue->EnqueueMessage(msg);
}

int Handler::GetQueueSize() {
    return mQueue->Size();
}