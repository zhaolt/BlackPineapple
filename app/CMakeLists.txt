# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(PATH_TO_VIDEOSTUDIO ${CMAKE_SOURCE_DIR}/src/main/cpp)
set(PATH_TO_JNI_LAYER ${CMAKE_SOURCE_DIR}/src/main/jni)
set(PATH_TO_THIRDPARTY ${PATH_TO_VIDEOSTUDIO}/thirdparty)
set(PATH_TO_PRE_BUILT ${PATH_TO_THIRDPARTY}/prebuilt/${ANDROID_ABI})

include_directories(BEFORE ${PATH_TO_THIRDPARTY}/ffmpeg/include/)
include_directories(${PATH_TO_VIDEOSTUDIO}/)
include_directories(${PATH_TO_VIDEOSTUDIO}/common/)

file(GLOB FILES_JNI_LAYER "${PATH_TO_JNI_LAYER}/*.cpp")
file(GLOB FILES_LIB_COMMON_MESSAGE "${PATH_TO_VIDEOSTUDIO}/common/message/*.cpp")
file(GLOB FILES_LIB_RECORD_ENCODE "${PATH_TO_VIDEOSTUDIO}/video_record/encode/*.cpp")
file(GLOB FILES_LIB_RECORD_COMMON "${PATH_TO_VIDEOSTUDIO}/video_record/common/*.cpp")
file(GLOB FILES_LIB_EGL_CORE "${PATH_TO_VIDEOSTUDIO}/common/egl_core/*.cpp")

add_library( # Sets the name of the library.
        video-studio

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${FILES_JNI_LAYER}
        ${FILES_LIB_RECORD_ENCODE}
        ${FILES_LIB_RECORD_COMMON}
        ${FILES_LIB_COMMON_MESSAGE}
        ${FILES_LIB_EGL_CORE}
        ${PATH_TO_VIDEOSTUDIO}/cmd_line/cmdutils.c
        ${PATH_TO_VIDEOSTUDIO}/cmd_line/ffmpeg.c
        ${PATH_TO_VIDEOSTUDIO}/cmd_line/ffmpeg_filter.c
        ${PATH_TO_VIDEOSTUDIO}/cmd_line/ffmpeg_hw.c
        ${PATH_TO_VIDEOSTUDIO}/cmd_line/ffmpeg_opt.c)

add_library(ffmpeg SHARED IMPORTED)
set_target_properties(
        ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${PATH_TO_PRE_BUILT}/libffmpeg.so
)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        video-studio
        ffmpeg
        log
        android
        EGL
        GLESv2)