//
// Created by ZhaoLiangtai on 2019/1/24.
//

#include "AudioFrameQueue.h"

AudioFrameQueue::AudioFrameQueue() {
    init();
}

AudioFrameQueue::~AudioFrameQueue() {
    flush();
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

int AudioFrameQueue::Put(AudioFrame *audioFrame) {
    if (mAbortRequest) {
        delete audioFrame;
        return -1;
    }
    AudioFrameList *list = new AudioFrameList();
    list->frame = audioFrame;
    list->next = NULL;
    pthread_mutex_lock(&mLock);
    if (mLast == NULL)
        mFirst = list;
    else
        mLast->next = list;
    mNbFrames++;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
    return 0;
}

int AudioFrameQueue::Get(AudioFrame **pAudioFrame, bool block) {
    AudioFrameList *frameList;
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
            *pAudioFrame = frameList->frame;
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

void AudioFrameQueue::Abort() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = true;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

void AudioFrameQueue::init() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
    mNbFrames = 0;
    mFirst = NULL;
    mLast = NULL;
    mAbortRequest = false;
}

void AudioFrameQueue::flush() {
    AudioFrameList *flt0, *flt1;
    AudioFrame *frame;
    pthread_mutex_lock(&mLock);
    for (flt0 = mFirst; flt0 != NULL; flt0 = flt1) {
        flt1 = flt0->next;
        frame = flt0->frame;
        if (NULL != frame)
            delete frame;
        delete flt0;
    }
    mLast = NULL;
    mFirst = NULL;
    mNbFrames = 0;
    pthread_mutex_unlock(&mLock);
}