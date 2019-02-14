//
// Created by ZhaoLiangtai on 2019/1/23.
//
#include <cstdlib>
#include <cstring>

#include <jni.h>
#include "cmd_line/ffmpeg.h"

#ifdef __cplusplus
extern "C" {
#endif
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#ifdef __cplusplus
}
#endif
#include "common/CommonTools.h"

#define JNI_REG_CLASS "com/jease/pineapple/media/VideoStudio"

JNIEXPORT jstring JNICALL showFFmpegInfo(JNIEnv *env, jobject) {
    char *info = (char *) malloc(40000);
    memset(info, 0, 40000);
    av_register_all();
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            strcat(info, "[Decoder]");
        } else {
            strcat(info, "[Encoder]");
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                strcat(info, "[Video]");
                break;
            case AVMEDIA_TYPE_AUDIO:
                strcat(info, "[Audio]");
                break;
            default:
                strcat(info, "[Other]");
                break;
        }
        sprintf(info, "%s %10s\n", info, c_temp->name);
        c_temp = c_temp->next;
    }
    puts(info);
    jstring result = env->NewStringUTF(info);
    free(info);
    return result;
}

JNIEXPORT jint JNICALL executeFFmpegCmd(JNIEnv *env, jobject, jobjectArray commands) {
    int argc = env->GetArrayLength(commands);
    char **argv = (char **) malloc(sizeof(char *) * argc);
//    av_log_set_callback(custom_log);
    for (int i = 0; i < argc; i++) {
        jstring string = (jstring) env->GetObjectArrayElement(commands, i);
        const char *tmp = env->GetStringUTFChars(string, 0);
        argv[i] = (char *) malloc(sizeof(char) * 1024);
        strcpy(argv[i], tmp);
    }
    try {
        ffmpeg_exec(argc, argv);
    } catch (int i) {
        LOGE("ffmpeg_exec error: %d", i);
    }
    for (int i = 0; i < argc; i++) {
        free(argv[i]);
    }
    free(argv);
    return 0;
}

const JNINativeMethod g_methods[] = {
        "showFFmpegInfo", "()Ljava/lang/String;", (void *) showFFmpegInfo,
        "executeFFmpegCmd", "([Ljava/lang/String;)I", (void *) executeFFmpegCmd
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env = NULL;
    jclass clazz = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK)
        return JNI_ERR;
    clazz = env->FindClass(JNI_REG_CLASS);
    if (clazz == NULL)
        return JNI_ERR;
    if (env->RegisterNatives(clazz, g_methods, NELEM(g_methods)) != JNI_OK)
        return JNI_ERR;
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *) {
    JNIEnv *env = NULL;
    jclass clazz = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK)
        return;
    clazz = env->FindClass(JNI_REG_CLASS);
    if (clazz == NULL)
        return;
    env->UnregisterNatives(clazz);
}