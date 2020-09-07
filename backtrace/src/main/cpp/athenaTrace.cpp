#include <jni.h>
#include <string>
#include <signal.h>
#include <vector>
#include <android/log.h>
#include <unistd.h>
#include <pthread.h>
#include <stdatomic.h>
#include "backtrace.h"
#include "debug_log.h"
#include "jnihelper.h"

#define TAG    "myhello-jni-test" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG,__VA_ARGS__) // 定义LOGD类型
#define JNI_CLASS_NAME "athena/backtrace/backtrace"

std::string hello = "hello";
std::string java_stack;

void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg);
void trace(int);
void java_trace(JNIEnv* env);
void get_java_trace(JNIEnv* env);
std::string getJavaStack(JNIEnv* env);

jstring test(JNIEnv *env) {
    LOGD("limin setting native staring");
//    int res, ii;
//    pthread_t rthread[2], wthread;
//    int tno[2];
//    static pthread_rwlock_t arealock;
//    res = pthread_rwlock_init(&arealock,NULL);
//    pthread_rwlock_rdlock(&arealock);
//    if (res != 0) {
//        perror("arealock initialization failed!!\n");
//        exit(EXIT_FAILURE);
//    }

    java_trace(env);
    trace(10);
    get_java_trace(env);
    LOGD("%s", java_stack.c_str());

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_athena_backtrace_backtrace_getNativeFromJNI(JNIEnv *env, jobject /* this */, int num) {
    if(num > 128) {
        num = 128;
    }
    std::vector<uintptr_t> frames(num);

    size_t num_frames = backtrace_get(frames.data(), frames.size());
    if (num_frames != 0) {
        return env->NewStringUTF(backtrace_string(frames.data(), num).c_str());
    }
    return env->NewStringUTF("");
}

void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
    jclass cls = (*env).FindClass(name);
    /* if cls is NULL, an exception has already been thrown */
    if (cls != NULL) {
        (*env).ThrowNew(cls, msg);
    }
    /* free the local ref */
    (*env).DeleteLocalRef(cls);
}

void java_trace(JNIEnv* env) {
    jclass  throwable_class = env->FindClass("java/lang/Throwable");
    if (nullptr == throwable_class) {
        return;
    }

    jmethodID  throwable_init = env->GetMethodID(throwable_class, "<init>", "(Ljava/lang/String;)V");
    if (nullptr == throwable_init) {
        if (nullptr != throwable_class) {
            env->DeleteLocalRef(throwable_class);
        }
        return;
    }

    jobject string_obj = env->NewStringUTF("crashreport");
    jobject throwable_obj = env->NewObject(throwable_class, throwable_init, string_obj);
    if(nullptr == throwable_obj) {
        if (nullptr != throwable_class) {
            env->DeleteLocalRef(throwable_class);
        }
        if (nullptr != string_obj) {
            env->DeleteLocalRef(string_obj);
        }
        return;
    }

    if (nullptr != string_obj) {
        env->DeleteLocalRef(string_obj);
    }

    jmethodID throwable_mid = env->GetMethodID(throwable_class, "printStackTrace", "()V");
    if (nullptr == throwable_mid) {
        if (nullptr != throwable_class) {
            env->DeleteLocalRef(throwable_class);
        }
        if (nullptr != throwable_obj) {
            env->DeleteLocalRef(throwable_obj);
        }
    }

    env->CallVoidMethod(throwable_obj, throwable_mid);


    if (nullptr != throwable_class) {
        env->DeleteLocalRef(throwable_class);
    }
    if (nullptr != throwable_obj) {
        env->DeleteLocalRef(throwable_obj);
    }
}

void trace(int frameName) {
    std::vector<uintptr_t> frames(frameName);

    error_log("start");
    size_t num_frames = backtrace_get(frames.data(), frames.size());
    if (num_frames == 0) {
        error_log("Backtrace failed to get any frames.");
    } else {
        error_log("Backtrace at time of num(%d):", num_frames);
        backtrace_log(frames.data(), num_frames);
    }
    error_log("end");
}

char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

void get_java_trace(JNIEnv* env) {
    jclass  throwable_class = env->FindClass(JNI_CLASS_NAME);
    if (nullptr == throwable_class) {
        return;
    }

    jclass global_class = static_cast<jclass>(env->NewGlobalRef(throwable_class));
    if (nullptr == global_class) {
        if (nullptr != throwable_class) {
            env->DeleteLocalRef(throwable_class);
        }
        return;
    }

    static jmethodID   trace_cb_method = env->GetStaticMethodID(global_class, "getStackTrace",
                                                                "()Ljava/lang/String;");

    jobject job = env->CallStaticObjectMethod(global_class, trace_cb_method);

    java_stack = jstringToChar(env, (jstring)job);

    if (nullptr != throwable_class) {
        env->DeleteLocalRef(throwable_class);
    }
    if (nullptr != global_class) {
        env->DeleteGlobalRef(global_class);
    }

    if (nullptr != job) {
        env->DeleteLocalRef(job);
    }
}

std::string getJavaStack(JNIEnv* env){
    std::string stack = "";
    jclass  throwable_class = env->FindClass(JNI_CLASS_NAME);
    if (nullptr == throwable_class) {
        return stack;
    }

    jclass global_class = static_cast<jclass>(env->NewGlobalRef(throwable_class));
    if (nullptr == global_class) {
        if (nullptr != throwable_class) {
            env->DeleteLocalRef(throwable_class);
        }
        return stack;
    }

    static jmethodID   trace_cb_method = env->GetStaticMethodID(global_class, "getStackTrace",
                                                                "()Ljava/lang/String;");

    jobject job = env->CallStaticObjectMethod(global_class, trace_cb_method);
    stack = jstringToChar(env, (jstring)job);

    if (nullptr != throwable_class) {
        env->DeleteLocalRef(throwable_class);
    }
    if (nullptr != global_class) {
        env->DeleteGlobalRef(global_class);
    }

    if (nullptr != job) {
        env->DeleteLocalRef(job);
    }

    return stack;
}