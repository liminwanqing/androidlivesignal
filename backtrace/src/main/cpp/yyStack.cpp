//
// Created by Administrator on 2020/9/4.
//
#include <vector>
#include <jni.h>

#include "yyStack.h"
#include "backtrace.h"
#include "debug_log.h"
#include "jnihelper.h"

extern std::string getJavaStack(JNIEnv* env);

yyStack::yyStack() {
    //TODO
}

yyStack::~yyStack() {
    //TODO
}

std::string yyStack::getJavaTrace() {
    return getJavaStack(reinterpret_cast<JNIEnv *>(JniHelper::getJavaVM()));
}

std::string yyStack::getNativeTarce(int num) {
    std::vector<uintptr_t> frames(num);

    size_t num_frames = backtrace_get(frames.data(), frames.size());
    if (num_frames == 0) {
        error_log("Backtrace failed to get any frames.");
    } else {
        error_log("Backtrace at time of num(%d):", num_frames);
        return backtrace_string(frames.data(), num_frames).c_str();
    }
    return "";
}
