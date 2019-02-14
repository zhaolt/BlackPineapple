//
// Created by ZhaoLiangtai on 2019/1/25.
//

#ifndef BLACKPINEAPPLE_VIDEOPACKETQUEUE_H
#define BLACKPINEAPPLE_VIDEOPACKETQUEUE_H

#define PTS_PARAM_NOT_SET_FLAG -1
#define DTS_PARAM_NOT_SET_FLAG -1

#include <cstdint>
#include <pthread.h>
#include <sys/types.h>

typedef struct VideoPacket {
    uint8_t *buf;
    int size;
    int timeMills;
    int duration;
    int64_t pts;
    int64_t dts;
    VideoPacket() {
        buf = NULL;
        size = 0;
        pts = PTS_PARAM_NOT_SET_FLAG;
        dts = DTS_PARAM_NOT_SET_FLAG;
    }
    ~VideoPacket() {
        if (NULL != buf) {
            delete [] buf;
            buf = NULL;
        }
    }
} VideoPacket;

typedef struct VideoPacketList {
    VideoPacket *pkt;
    struct VideoPacketList *next;
    VideoPacketList() {
        pkt = NULL;
        next = NULL;
    }
} VideoPacketList;

class VideoPacketQueue {
public:
    VideoPacketQueue();
    ~VideoPacketQueue();
    void Init();
    void Flush();
    int Put(VideoPacket *videoPacket);
    int Get(VideoPacket **pVideoPacket, bool block);
    void Abort();

private:
    bool mAbortRequest;
    int mNbPackets;
    VideoPacketList *mFirst;
    VideoPacketList *mLast;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
};


#endif //BLACKPINEAPPLE_VIDEOPACKETQUEUE_H
