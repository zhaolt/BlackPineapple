//
// Created by ZhaoLiangtai on 2019/1/25.
//

#include "VideoFramePool.h"

VideoFramePool::VideoFramePool() {
    mVideoFrameQueue = NULL;
}

VideoFramePool::~VideoFramePool() {

}

VideoFramePool *VideoFramePool::sInstance = new VideoFramePool();

VideoFramePool *VideoFramePool::GetInstance() {
    return sInstance;
}

void VideoFramePool::InitFrameQueue() {
    mVideoFrameQueue = new VideoFrameQueue();
}

void VideoFramePool::AbortFrameQueue() {
    if (NULL != mVideoFrameQueue)
        mVideoFrameQueue->Abort();
}

int VideoFramePool::GetVideoFrame(VideoFrame **pVideoFrame, bool block) {
    int ret = 1;
    if (NULL != mVideoFrameQueue)
        ret = mVideoFrameQueue->Get(pVideoFrame, block);
    return ret;
}

void VideoFramePool::DestroyFrameQueue() {
    if (NULL != mVideoFrameQueue) {
        delete mVideoFrameQueue;
        mVideoFrameQueue = NULL;
    }
}

void VideoFramePool::PushVideoFrameToQueue(VideoFrame *videoFrame) {
    if (NULL != mVideoFrameQueue)
        mVideoFrameQueue->Put(videoFrame);
}