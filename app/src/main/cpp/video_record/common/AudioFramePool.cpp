//
// Created by ZhaoLiangtai on 2019/1/25.
//

#include "AudioFramePool.h"

AudioFramePool::AudioFramePool() {
    mAudioFrameQueue = NULL;
}

AudioFramePool::~AudioFramePool() {

}

AudioFramePool *AudioFramePool::sInstance = new AudioFramePool();
AudioFramePool *AudioFramePool::GetInstance() {
    return sInstance;
}

void AudioFramePool::InitFrameQueue() {
    mAudioFrameQueue = new AudioFrameQueue();
}

void AudioFramePool::AbortFrameQueue() {
    if (NULL != mAudioFrameQueue)
        mAudioFrameQueue->Abort();
}

int AudioFramePool::GetAudioFrame(AudioFrame **pAudioFrame, bool block) {
    int ret = -1;
    if (NULL != mAudioFrameQueue)
        ret = mAudioFrameQueue->Get(pAudioFrame, block);
    return ret;
}

void AudioFramePool::DestroyFrameQueue() {
    if (NULL != mAudioFrameQueue) {
        delete mAudioFrameQueue;
        mAudioFrameQueue = NULL;
    }
}

void AudioFramePool::PushAudioFrameToQueue(AudioFrame *audioFrame) {
    if (NULL != mAudioFrameQueue)
        mAudioFrameQueue->Put(audioFrame);
}