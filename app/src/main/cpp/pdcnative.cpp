//
// Created by Istvan Szilagyi on 03.04.21.
//
#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_pdc_main_parkdistancecontrol2_ParkSensorBackgroundService_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from Cplusplus";
    return env->NewStringUTF(hello.c_str());
}
