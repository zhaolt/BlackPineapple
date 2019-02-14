//
// Created by ZhaoLiangtai on 2019/1/24.
//

#include "VideoFrameQueue.h"

VideoFrameQueue::VideoFrameQueue() {
    init();
}

VideoFrameQueue::~VideoFrameQueue() {
    flush();
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

int VideoFrameQueue::Put(VideoFrame *videoFrame) {
    if (mAbortRequest) {
        delete videoFrame;
        return -1;
    }
    VideoFrameList *frameList = new VideoFrameList();
    frameList->frame = videoFrame;
    frameList->next = NULL;
    pthread_mutex_lock(&mLock);
    if (mLast == NULL)
        mFirst = frameList;
    else
        mLast = frameList;
    mLast = frameList;
    mNbFrames++;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
    return 0;
}

int VideoFrameQueue::Get(VideoFrame **pVideoFrame, bool block) {
    VideoFrameList *frameList;
    int ret;
    pthread_mutex_lock(&mLock);
    for (;;) {
        if (mAbortRequest) {
            ret = -1;
            break;
        }
        frameList = mFirst;
        if (frameList) {
            mFirst = frameList->next;
            if (!mFirst)
                mLast = NULL;
            mNbFrames--;
            *pVideoFrame = frameList->frame;
            delete frameList;
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
            pthread_cond_wait(&mCondition, &mLock);
        }
    }
    return ret;
}

void VideoFrameQueue::Abort() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = true;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

void VideoFrameQueue::init() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
    mNbFrames = 0;
    mFirst = NULL;
    mLast = NULL;
    mAbortRequest = false;
}

void VideoFrameQueue::flush() {
    VideoFrameList *flt0, *flt1;
    VideoFrame *frame;
    pthread_mutex_lock(&mLock);
    for (flt0 = mFirst; flt0 != NULL; flt0 = flt1) {
        flt1 = flt0->next;
        frame = flt0->frame;
        if (NULL != frame) {
            delete frame;
        }
        delete flt0;
    }
    mLast = NULL;
    mFirst = NULL;
    mNbFrames = 0;
    pthread_mutex_unlock(&mLock);
}