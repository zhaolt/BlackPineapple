//
// Created by ZhaoLiangtai on 2019/1/23.
//

#ifndef BLACKPINEAPPLE_VIDEOENCODER_H
#define BLACKPINEAPPLE_VIDEOENCODER_H

#include "video_record/common/VideoFrameQueue.h"

class VideoEncoder {
public:
    VideoEncoder();
    virtual ~VideoEncoder() {};
    virtual int Init(int width, int height, int bitRate, float frameRate) = 0;
    virtual int Encode(VideoFrame *videoFrame) = 0;
    virtual void Release() = 0;
};
#endif //BLACKPINEAPPLE_VIDEOENCODER_H
