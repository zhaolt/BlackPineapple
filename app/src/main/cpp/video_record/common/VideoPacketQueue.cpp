//
// Created by ZhaoLiangtai on 2019/1/25.
//


#include "VideoPacketQueue.h"

VideoPacketQueue::VideoPacketQueue() {
    Init();
}

VideoPacketQueue::~VideoPacketQueue() {
    Flush();
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

void VideoPacketQueue::Init() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
    mNbPackets = 0;
    mFirst = NULL;
    mLast = NULL;
    mAbortRequest = false;
}

void VideoPacketQueue::Flush() {
    VideoPacketList *vpl0, *vpl1;
    VideoPacket *packet;
    pthread_mutex_lock(&mLock);
    for (vpl0 = mFirst; vpl0 != NULL; vpl0 = vpl1) {
        vpl1 = vpl0->next;
        packet = vpl0->pkt;
        if (NULL != packet)
            delete packet;
        delete vpl0;
    }
    mLast = NULL;
    mFirst = NULL;
    mNbPackets = 0;
    pthread_mutex_unlock(&mLock);
}

int VideoPacketQueue::Put(VideoPacket *videoPacket) {
    if (mAbortRequest) {
        delete videoPacket;
        return -1;
    }
    VideoPacketList *list = new VideoPacketList();
    list->pkt = videoPacket;
    list->next = NULL;
    pthread_mutex_lock(&mLock);
    if (mLast == NULL)
        mFirst = list;
    else
        mLast->next = list;
    mLast = list;
    mNbPackets++;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
    return 0;
}

int VideoPacketQueue::Get(VideoPacket **pVideoPacket, bool block) {
    VideoPacketList *list;
    int ret;
    pthread_mutex_lock(&mLock);
    for (;;) {
        if (mAbortRequest) {
            ret = -1;
            break;
        }
        list = mFirst;
        if (list) {
            mFirst = list->next;
            if (!mFirst)
                mLast = NULL;
            mNbPackets--;
            *pVideoPacket = list->pkt;
            delete list;
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

void VideoPacketQueue::Abort() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = true;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}