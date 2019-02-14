//
// Created by ZhaoLiangtai on 2019/1/25.
//

#ifndef BLACKPINEAPPLE_AUDIOFRAMEPOOL_H
#define BLACKPINEAPPLE_AUDIOFRAMEPOOL_H


#include "FramePool.h"

class AudioFramePool: public FramePool {
public:
    ~AudioFramePool();
    void InitFrameQueue();
    void AbortFrameQueue();
    void DestroyFrameQueue();

    int GetAudioFrame(AudioFrame **pAudioFrame, bool block);
    void PushAudioFrameToQueue(AudioFrame *audioFrame);

    static AudioFramePool *GetInstance();

private:
    AudioFramePool();
    static AudioFramePool *sInstance;
    AudioFrameQueue *mAudioFrameQueue;
};


#endif //BLACKPINEAPPLE_AUDIOFRAMEPOOL_H
