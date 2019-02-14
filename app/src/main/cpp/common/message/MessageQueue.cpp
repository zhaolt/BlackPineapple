//
// Created by ZhaoLiangtai on 2019/1/31.
//


#include "MessageQueue.h"
MessageQueue::MessageQueue() {
    Init();
}

MessageQueue::MessageQueue(const char *queueName) {
    Init();
    mQueueName = queueName;
}

MessageQueue::~MessageQueue() {
    Flush();
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

void MessageQueue::Init() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
    mNbPackets = 0;
    mFirst = NULL;
    mLast = NULL;
    mAbortRequest = false;
}

int MessageQueue::Size() {
    pthread_mutex_lock(&mLock);
    int size = mNbPackets;
    pthread_mutex_unlock(&mLock);
    return size;
}

void MessageQueue::Flush() {
    MessageNode *curNode, *nextNode;
    Message *msg;
    pthread_mutex_lock(&mLock);
    for (curNode = mFirst; curNode != NULL; curNode = nextNode) {
        nextNode = curNode->next;;
        msg = curNode->msg;
        if (NULL != msg)
            delete msg;
        delete curNode;
    }
    mLast = NULL;
    mFirst = NULL;
    mNbPackets = 0;
    pthread_mutex_unlock(&mLock);
}

int MessageQueue::EnqueueMessage(Message *msg) {
    if (mAbortRequest) {
        delete msg;
        return -1;
    }
    MessageNode *node = new MessageNode();
    node->msg = msg;
    node->next = NULL;
    pthread_mutex_lock(&mLock);
    if (mLast == NULL)
        mFirst = node;
    else
        mLast->next = node;
    mLast = node;
    mNbPackets++;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
    return 0;
}

int MessageQueue::DequeueMessage(Message **msg, bool block) {
    MessageNode *node;
    int ret;
    pthread_mutex_lock(&mLock);
    for (;;) {
        if (mAbortRequest) {
            ret = -1;
            break;
        }
        node = mFirst;
        if (node) {
            mFirst = node->next;
            if (!mFirst)
                mLast = NULL;
            mNbPackets--;
            *msg = node->msg;
            delete node;
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
            pthread_cond_wait(&mCondition, &mLock);
        }
    }
    pthread_mutex_unlock(&mLock);
    return ret;
}

void MessageQueue::Abort() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = true;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

/************************** Message class ***********************/

Message::Message() {
    handler = NULL;
}

Message::Message(int what) {
    handler = NULL;
    this->what = what;
}

Message::Message(int what, void *obj) {
    handler = NULL;
    this->what = what;
    this->obj = obj;
}

Message::Message(int what, int arg1, int arg2) {
    handler = NULL;
    this->what = what;
    this->arg1 = arg1;
    this->arg2 = arg2;
}

Message::Message(int what, int arg1, int arg2, void *obj) {
    handler = NULL;
    this->what = what;
    this->arg1 = arg1;
    this->arg2 = arg2;
    this->obj = obj;
}

Message::~Message() {
}

int Message::Execute() {
    if (MESSAGE_QUEUE_LOOP_QUIT_FLAG == what) {
        return MESSAGE_QUEUE_LOOP_QUIT_FLAG;
    } else if (handler) {

        return 1;
    }
    return 0;
}