//
// Created by ZhaoLiangtai on 2019/1/24.
//

#ifndef BLACKPINEAPPLE_AUDIOFRAMEQUEUE_H
#define BLACKPINEAPPLE_AUDIOFRAMEQUEUE_H

#include <cstdint>
#include <pthread.h>

#include "common/CommonTools.h"

typedef struct AudioFrame {
    uint8_t *data;
    int size;
    AudioFrame() {
        data = NULL;
        size = 0;
    }
    ~AudioFrame() {
        if (data != NULL) {
            delete [] data;
            data = NULL;
        }
    }
} AudioFrame;

typedef struct AudioFrameList {
    AudioFrame *frame;
    struct AudioFrameList *next;
    AudioFrameList() {
        frame = NULL;
        next = NULL;
    }
} AudioFrameList;

class AudioFrameQueue {
public:
    AudioFrameQueue();
    ~AudioFrameQueue();
    int Put(AudioFrame *audioFrame);
    int Get(AudioFrame **pAudioFrame, bool block);
    void Abort();
private:
    void init();
    void flush();
    bool mAbortRequest;
    int mNbFrames;
    AudioFrameList *mFirst;
    AudioFrameList *mLast;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
};


#endif //BLACKPINEAPPLE_AUDIOFRAMEQUEUE_H
