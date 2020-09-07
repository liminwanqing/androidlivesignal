
#include "jnihelper.h"

namespace {
    JavaVM*         g_vm = NULL;
    JniHelper*      g_jniHelper = NULL;
	JNIEnv*         g_env = NULL;
}

#define JAVA_CRASHHANDLER_CLASS "com/yy/sdk/crashreport/CrashHandler"

jclass    JniHelper::m_stringClass = 0;
jmethodID JniHelper::m_stringCtor = 0;

void JniHelper::setJavaVM(JavaVM* vm) {
    g_vm = vm;
}

JavaVM* JniHelper::getJavaVM(){
    return g_vm;
}

JniHelper* JniHelper::defaultHelper() {
    if (g_jniHelper != NULL) {
        return g_jniHelper;
    }

    g_jniHelper = new JniHelper(JAVA_CRASHHANDLER_CLASS);

    return g_jniHelper;
}

std::string JniHelper::convert2String(JNIEnv* env, const jstring& val) {
    if (env == 0) {
        return "";
    }

    std::string res;
    const char* str = env->GetStringUTFChars(val, 0);
    res.append(str);
    env->ReleaseStringUTFChars(val, str);

    return res;
}

jstring JniHelper::convert2JString(JNIEnv* env, const std::string& val) {
    if (env == 0) {
        return 0;
    }

    if (m_stringClass == NULL) {
        CRASH_LOG("[Error] GlobalRef failed...");
        return NULL;
    }

    jbyteArray tmpText = env->NewByteArray(val.length());
    env->SetByteArrayRegion(tmpText, 0, val.length(), (const jbyte*)val.c_str());

    jstring charsetName = env->NewStringUTF("UTF-8");
    jstring res = reinterpret_cast<jstring>(env->NewObject(m_stringClass, m_stringCtor, tmpText, charsetName));

    if (res == NULL) {
        CRASH_LOG("[Error]create string object failed...");
    }

    env->DeleteLocalRef(tmpText);
    env->DeleteLocalRef(charsetName);

	return res;
}

JniHelper::JniHelper(const char *cls)
: m_curJaveCls(0) {
	init(cls);
}

JniHelper::~JniHelper() {
	m_curJaveCls = 0;
}

void JniHelper::init(const char *cls) {
    JEnvLock lock;
    JNIEnv *env = lock.env();
    if (env == 0) {
    	return;
    }

    jclass tmpCls = env->FindClass(cls);
    if (tmpCls == 0) {
    	return;
    }

    m_curJaveCls = (jclass)env->NewGlobalRef(tmpCls);
    env->DeleteLocalRef(tmpCls);

    if (m_stringClass == NULL) {
         jclass c = env->FindClass("java/lang/String");

         if (c == NULL) {
             CRASH_LOG("[Error]String FindClass failed. Maybe is not main thread?");
             return;
         }

         m_stringCtor = env->GetMethodID(c, "<init>", "([BLjava/lang/String;)V");
         m_stringClass = reinterpret_cast<jclass>(env->NewGlobalRef(c));
    }
}

void JniHelper::callVoid(jmethodID id, ...) {
    JEnvLock lock;
    JNIEnv *env = lock.env();
    if (env == 0) {
        return ;
    }
    va_list arg;
    va_start(arg, id);
    env->CallStaticVoidMethodV(m_curJaveCls, id, arg);
    va_end(arg);
}

jmethodID JniHelper::javaFunctionID(const char *name, const char *signature) {
    if (m_curJaveCls == 0) {
        CRASH_LOG("JniHelper::registerNativeImplementation javacls is null, name=%s", name);

    	return 0;
    }

    JEnvLock lock;
    JNIEnv *env = lock.env();
	jmethodID mID = env->GetStaticMethodID(m_curJaveCls, name, signature);

    return mID;
}


bool JniHelper::registerNativeImplementation(const char *name, const char *signature, void *func) {
    JNINativeMethod method = {name, signature, func};
    JEnvLock lock;
    JNIEnv *env = lock.env();
    if (env == NULL) {
    	CRASH_LOG("JniHelper::registerNativeImplementation env is null");

        return false;
    }
    if (m_curJaveCls == 0) {
    	CRASH_LOG("JniHelper::registerNativeImplementation javacls is null, name=%s", name);

    	return false;
    }

    bool bRet = ( 0 == env->RegisterNatives(m_curJaveCls, &method, 1));

	return bRet;
}



void* JEnvLock::operator new(size_t bytes) {
	return (void*)0;
}

void* JEnvLock::operator new[](size_t bytes) {
	return (void*)0;
}

void JEnvLock::operator delete(void* ptr, size_t bytes) {

}

void JEnvLock::operator delete[](void* ptr, size_t bytes) {

} 


JEnvLock::JEnvLock() {
    m_env = NULL;
    
    int env_status = g_vm->GetEnv((void **)&m_env, JNI_VERSION_1_4);
	if(env_status == JNI_EDETACHED) {
        jint attachResult = g_vm->AttachCurrentThread(&m_env,NULL);
        if(attachResult >= 0) {
		} else {
            m_env = NULL;
        }
	} else if(JNI_OK != env_status){
        m_env = NULL;
    }
}

void JEnvLock::detachJVM() {
    JNIEnv *env = NULL;
	int env_status = g_vm->GetEnv((void **)&env, JNI_VERSION_1_4);
	if(env_status == JNI_OK) {
		g_vm->DetachCurrentThread();
	}
}

JEnvLock::~JEnvLock() {
    /*if (m_shouldDetach) {
        g_vm->DetachCurrentThread();
    }*/
}

JNIEnv *JEnvLock::env() {
    return m_env;
}
