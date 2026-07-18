#include <jni.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <SDL3/SDL.h>
#include "port_ppu.h"
#include "port_touch_controls.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "TMC_JNI", __VA_ARGS__)

extern "C" void Port_PPU_SetSecondaryNativeWindow(struct ANativeWindow* window);

extern "C" {

JNIEXPORT void JNICALL
Java_com_matheovignaud_tmc_TmcTouchActivity_nativeSecondarySurfaceCreated(JNIEnv* env, jobject thiz, jobject surface) {
    LOGD("nativeSecondarySurfaceCreated entry");
    ANativeWindow* nativeWindow = ANativeWindow_fromSurface(env, surface);
    if (nativeWindow) {
        LOGD("nativeWindow acquired: %p", nativeWindow);
        SDL_SetPointerProperty(SDL_GetGlobalProperties(), "TMC.secondary_native_window", nativeWindow);
        Port_PPU_SetSecondaryNativeWindow(nativeWindow);
    } else {
        LOGD("nativeWindow acquisition failed!");
    }
}

JNIEXPORT void JNICALL
Java_com_matheovignaud_tmc_TmcTouchActivity_nativeSecondarySurfaceDestroyed(JNIEnv* env, jobject thiz) {
    SDL_SetPointerProperty(SDL_GetGlobalProperties(), "TMC.secondary_native_window", nullptr);
    Port_PPU_SetSecondaryNativeWindow(nullptr);
}

JNIEXPORT void JNICALL
Java_com_matheovignaud_tmc_TmcTouchActivity_nativeSecondaryTouchEvent(JNIEnv* env, jobject thiz, jint action, jfloat x, jfloat y, jint pointerId) {
    Port_TouchControls_HandleSecondaryEvent(action, x, y, pointerId);
}

void Port_Android_RequestSecondaryDisplay(void) {
    LOGD("Port_Android_RequestSecondaryDisplay entry");
    JNIEnv* env = (JNIEnv*)SDL_GetAndroidJNIEnv();
    if (!env) return;

    jobject activity = (jobject)SDL_GetAndroidActivity();
    if (!activity) return;

    jclass cls = env->GetObjectClass(activity);
    jmethodID mid = env->GetMethodID(cls, "requestSecondaryDisplay", "()V");
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        LOGD("requestSecondaryDisplay method not found or exception occurred");
    } else if (mid) {
        env->CallVoidMethod(activity, mid);
    }
}

}
