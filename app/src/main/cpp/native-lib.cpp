#include <jni.h>
#include <string>
#include <signal.h>
#include <android/log.h>
#include <unistd.h>
#include <pthread.h>
#include <stdatomic.h>

#define TAG    "myhello-jni-test" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型

std::string hello = "hello";

struct pthread_rwlock_internal_t {
    atomic_int state;
    atomic_int writer_tid;

    bool pshared;
    bool writer_nonrecursive_preferred;
    uint16_t __pad;

// When a reader thread plans to suspend on the rwlock, it will add STATE_HAVE_PENDING_READERS_FLAG
// in state, increase pending_reader_count, and wait on pending_reader_wakeup_serial. After woken
// up, the reader thread decreases pending_reader_count, and the last pending reader thread should
// remove STATE_HAVE_PENDING_READERS_FLAG in state. A pending writer thread works in a similar way,
// except that it uses flag and members for writer threads.

    int pending_lock;  // All pending members below are protected by pending_lock.
    uint32_t pending_reader_count;  // Count of pending reader threads.
    uint32_t pending_writer_count;  // Count of pending writer threads.
    uint32_t pending_reader_wakeup_serial;  // Pending reader threads wait on this address by futex_wait.
    uint32_t pending_writer_wakeup_serial;  // Pending writer threads wait on this address by futex_wait.

#if defined(__LP64__)
    char __reserved[20];
#else
    char __reserved[4];
#endif
};

extern "C" JNIEXPORT jstring JNICALL
Java_com_zenmen_demo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
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

    return env->NewStringUTF(hello.c_str());
}

void signalHandle(int sig) {
    LOGD("limin setting signal number is %d", sig);
    switch (sig) {
        case SIGSTOP:
            hello = "signal stop";
            break;
        case SIGQUIT:
            hello = "signal quit";
            break;
        case SIGCONT:
            hello = "signal cont";
            break;
        case SIGKILL:
            hello = "signal kill";
            break;
        default:
            break;
    }
}


static void my_handler(int sig, siginfo_t *si, void *sc) {
    LOGD("limin signal: %d\n", sig);
    switch (sig) {
        case SIGSTOP:
            hello = "signal stop";
            break;
        case SIGQUIT:
            hello = "signal quit";
            break;
        case SIGCONT:
            hello = "signal cont";
            break;
        case SIGKILL:
            hello = "signal kill";
            break;
        default:
            break;
    }
}

static void* sig_handle(void *arg) {
    sigset_t* tmp = (sigset_t*) arg;
    pthread_sigmask(SIG_BLOCK, tmp, NULL);
    while (true) {
        int signal_number;
        auto rc = sigwait(tmp, &signal_number);
        if (rc == 0) {
            switch (signal_number) {
                case SIGQUIT:
                    LOGD("limin get signal SIGQUIT!!");
                    hello = "signal quit";
                    break;
                case SIGCONT:
                    LOGD("limin get signal SIGCONT!!");
                    hello = "signal SIGCONT";
                case SIGABRT:
                    LOGD("limin get signal SIGABRT!!");
                    hello = "signal SIGABRT";
                default:
                    break;
            }
        }
    }
    pthread_detach(pthread_self());
}

extern "C" JNIEXPORT void JNICALL
Java_com_zenmen_demo_MainActivity_signalFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    LOGD("limin setting signal");
//    signal(SIGSTOP, signalHandle);
//    signal(SIGQUIT, signalHandle);
//    signal(SIGCONT, signalHandle);
    struct sigaction act;
    memset(&act, 0, sizeof(act));
    sigemptyset(&act.sa_mask);

    sigaction(SIGQUIT, &act, nullptr);
    sigaction(SIGCONT, &act, nullptr);
    sigaction(SIGABRT, &act, nullptr);
//    sigaddset(&act.sa_mask, SIGQUIT);
//    sigaddset(&act.sa_mask, SIGCONT);
//    sigaddset(&act.sa_mask, SIGKILL);
//    act.sa_sigaction = my_handler;
//    act.sa_flags = 0;

    sigset_t set_;
    sigemptyset(&set_);
    sigaddset(&set_, SIGQUIT);
    sigaddset(&set_, SIGCONT);
    sigaddset(&set_, SIGABRT);
    sigaddset(&set_, SIGSEGV);

    pthread_t thread;
    pthread_create(&thread, NULL, &sig_handle, (void*)&set_);

//    sigaction(SIGQUIT, &act, nullptr);
//    sigaction(SIGCONT, &act, nullptr);
//    sigaction(SIGABRT, &act, nullptr);
//    sigprocmask(SIG_BLOCK, &act.sa_mask, NULL);
    pause();
    LOGD("limin setting signal end!!");
}