//
// Created by ZhaoLiangtai on 2019/1/25.
//

#ifndef BLACKPINEAPPLE_FFMPEGVIDEOENCODER_H
#define BLACKPINEAPPLE_FFMPEGVIDEOENCODER_H

#ifdef __cplusplus
extern "C" {
#endif
#include "libavutil/frame.h"
#include "libavcodec/avcodec.h"
#ifdef __cplusplus
}
#endif


#include "VideoEncoder.h"

class FFmpegVideoEncoder: public VideoEncoder {
public:
    FFmpegVideoEncoder();
    ~FFmpegVideoEncoder() {};
    int Init(int width, int height, int bitRate, float frameRate);
    int Encode(VideoFrame *videoFrame);
    void Release();

private:
    AVFrame *mFrame;
    AVCodecContext *mCodecCtx;
    uint8_t *mVideoBuf;
    AVCodec *mCodec;
    uint8_t *mPicBuf;
    bool mParamSetUnWriteFlag;

    int allocVideoStream(int width, int height, int bitRate, float frameRate);
    void allocAVFrame();
    void pushToQueue(uint8_t *buf, int size, int timeMills);
};


#endif //BLACKPINEAPPLE_FFMPEGVIDEOENCODER_H
