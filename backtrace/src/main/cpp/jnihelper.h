
#ifndef __CRASHREPORT_JNIHELPER_H__
#define __CRASHREPORT_JNIHELPER_H__

#include <string>

#include <jni.h>
#include <android/log.h>

#define      CRASH_LOG(format, ...)    __android_log_print(ANDROID_LOG_WARN, "CrashReport", format, ##__VA_ARGS__)

class JniHelper {

public:
    static void         setJavaVM(JavaVM* vm);
    static JavaVM*      getJavaVM();
    static JniHelper*   defaultHelper();

    static std::string  convert2String(JNIEnv* env, const jstring& val);
    static jstring      convert2JString(JNIEnv* env, const std::string& val);

public:
    JniHelper(const char *cls);
    ~JniHelper();

public:
    void        init(const char *cls);
    void        callVoid(jmethodID id, ...);
    jmethodID   javaFunctionID(const char *function, const char *signature);
    bool        registerNativeImplementation(const char *name, const char *signature, void *func);
    
private:
    jclass      m_curJaveCls;


   static jclass    m_stringClass;
   static jmethodID m_stringCtor;
};  // JniHelper

class JEnvLock {
private:    //to disable allocate in heap!!
    static void* operator new(size_t bytes);
    static void* operator new[](size_t bytes);
    static void operator delete(void* ptr, size_t bytes);
    static void operator delete[](void* ptr, size_t bytes);
    
public:
    JEnvLock();
    ~JEnvLock();
    
    JNIEnv *env();
    
public:
    static void detachJVM();
    
private:
    JNIEnv *m_env;
};

#endif  // __CRASHREPORT_JNIHELPER_H__ 
