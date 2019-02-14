//
// Created by ZhaoLiangtai on 2019/1/23.
//

#ifndef BLACKPINEAPPLE_COMMONTOOLS_H
#define BLACKPINEAPPLE_COMMONTOOLS_H
#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE, "VideoEditFramework", format, ##__VA_ARGS__)
#define LOGD(format, ...)  __android_log_print(ANDROID_LOG_DEBUG,  "VideoEditFramework", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "VideoEditFramework", format, ##__VA_ARGS__)
#define LOGW(format, ...)  __android_log_print(ANDROID_LOG_WARN,  "VideoEditFramework", format, ##__VA_ARGS__)
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR,  "VideoEditFramework", format, ##__VA_ARGS__)
#define NELEM(x) ((int) sizeof(x) / sizeof(x[0]))
#define MAX(a, b) (((a) > (b)) ? (a) : (b))
#define MIN(a, b) (((a) < (b)) ? (a) : (b))
#endif //BLACKPINEAPPLE_COMMONTOOLS_H
