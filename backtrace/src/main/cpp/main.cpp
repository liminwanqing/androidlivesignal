//
// Created by Administrator on 2020/9/4.
//

#include <jni.h>
#include "jnihelper.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    CRASH_LOG("jni onload");

    JniHelper::setJavaVM(vm);

    CRASH_LOG("jni loaded JNI_VERSION_1_4");

    return JNI_VERSION_1_4; // version;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
CRASH_LOG("jni onunload");
}

#ifdef __cplusplus
}
#endif