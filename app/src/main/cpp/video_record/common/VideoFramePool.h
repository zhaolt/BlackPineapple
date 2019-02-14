//
// Created by ZhaoLiangtai on 2019/1/25.
//

#ifndef BLACKPINEAPPLE_VIDEOFRAMEPOOL_H
#define BLACKPINEAPPLE_VIDEOFRAMEPOOL_H


#include "FramePool.h"

class VideoFramePool: public FramePool {
public:
    ~VideoFramePool();
    void InitFrameQueue();
    void AbortFrameQueue();
    void DestroyFrameQueue();

    int GetVideoFrame(VideoFrame **pVideoFrame, bool block);
    void PushVideoFrameToQueue(VideoFrame *videoFrame);

    static VideoFramePool *GetInstance();

private:
    VideoFramePool();
    static VideoFramePool *sInstance;
    VideoFrameQueue *mVideoFrameQueue;
};


#endif //BLACKPINEAPPLE_VIDEOFRAMEPOOL_H
