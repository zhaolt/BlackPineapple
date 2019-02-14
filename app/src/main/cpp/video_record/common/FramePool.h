//
// Created by ZhaoLiangtai on 2019/1/25.
//

#ifndef BLACKPINEAPPLE_FRAMEPOOL_H
#define BLACKPINEAPPLE_FRAMEPOOL_H


#include "VideoFrameQueue.h"
#include "AudioFrameQueue.h"

class FramePool {
public:
    virtual void InitFrameQueue() = 0;
    virtual void AbortFrameQueue() = 0;
    virtual void DestroyFrameQueue() = 0;

    virtual int GetVideoFrame(VideoFrame **pVideoFrame, bool block) {};
    virtual void PushVideoFrameToQueue(VideoFrame *videoFrame) {};
    virtual int GetAudioFrame(AudioFrame **pAudioFrame, bool block) {};
    virtual void PushAudioFrameToQueue(AudioFrame *audioFrame) {};
};


#endif //BLACKPINEAPPLE_FRAMEPOOL_H
