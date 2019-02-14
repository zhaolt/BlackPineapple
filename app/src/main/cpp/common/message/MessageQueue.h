//
// Created by ZhaoLiangtai on 2019/1/31.
//

#ifndef BLACKPINEAPPLE_MESSAGEQUEUE_H
#define BLACKPINEAPPLE_MESSAGEQUEUE_H

#define MESSAGE_QUEUE_LOOP_QUIT_FLAG  19940107
#include <cstdio>
#include <pthread.h>

class Handler;

class Message {
private:
    int what;
    int arg1;
    int arg2;
    void *obj;

public:
    Message();
    Message(int what);
    Message(int what, int arg1, int arg2);
    Message(int what, void *obj);
    Message(int what, int arg1, int arg2, void *obj);
    ~Message();

    int Execute();
    int GetWhat() {
        return what;
    }
    int GetArg1() {
        return arg1;
    }
    int GetArg2() {
        return arg2;
    }
    void *GetObj() {
        return obj;
    }
    Handler *handler;
};

typedef struct MessageNode {
    Message *msg;
    struct MessageNode *next;
    MessageNode() {
        msg = NULL;
        next = NULL;
    }
} MessageNode;

class MessageQueue {
private:
    MessageNode *mFirst;
    MessageNode *mLast;
    int mNbPackets;
    bool mAbortRequest;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    const char *mQueueName;

public:
    MessageQueue();
    MessageQueue(const char *queueName);
    ~MessageQueue();

    void Init();
    void Flush();
    int EnqueueMessage(Message *msg);
    int DequeueMessage(Message **msg, bool block);
    int Size();
    void Abort();
};


#endif //BLACKPINEAPPLE_MESSAGEQUEUE_H
