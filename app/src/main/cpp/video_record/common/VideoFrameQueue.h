//
// Created by ZhaoLiangtai on 2019/1/24.
//

#ifndef BLACKPINEAPPLE_VIDEOFRAMEQUEUE_H
#define BLACKPINEAPPLE_VIDEOFRAMEQUEUE_H

#include <cstdint>
#include <pthread.h>
#include "common/CommonTools.h"
typedef struct VideoFrame {
    uint8_t *data;
    int size;
    int64_t timestamp;
    VideoFrame() {
        data = NULL;
        size = 0;
        timestamp = 0;
    }
    ~VideoFrame() {
        if (NULL != data) {
            delete [] data;
            data = NULL;
        }
        size = 0;
        timestamp = 0;
    }
} VideoFrame;

typedef struct VideoFrameList {
    VideoFrame *frame;
    struct VideoFrameList *next;
    VideoFrameList() {
        frame = NULL;
        next = NULL;
    }
} VideoFrameList;

class VideoFrameQueue {
public:
    VideoFrameQueue();
    ~VideoFrameQueue();
    int Put(VideoFrame *videoFrame);
    int Get(VideoFrame **pVideoFrame, bool block);
    void Abort();

private:
    VideoFrameList *mFirst;
    VideoFrameList *mLast;
    int mNbFrames;
    bool mAbortRequest;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    void init();
    void flush();
};


#endif //BLACKPINEAPPLE_VIDEOFRAMEQUEUE_H
