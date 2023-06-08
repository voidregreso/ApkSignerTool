// dllmain.cpp : Defines the entry point for the DLL application.
#include "framework.h"
#include "zipalign.h"
#include "jni.h"
#include <stdlib.h>

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
                     )
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}

char* jcc(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = new char[alen + 1];
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

/*
 * Class:     ernesto_apksigner_util_Utils
 * Method:    zipAlign
 * Signature: (Ljava/lang/String;Ljava/lang/String;IZ)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_ernesto_apksigner_util_Utils_zipAlign
(JNIEnv* env, jclass cls, jstring inf, jstring outf, jint alg_bytes, jboolean force) {
    char* inc = jcc(env, inf);
    char* outc = jcc(env, outf);
    int res = zipalign(inc, outc, (int)alg_bytes, (force == JNI_TRUE) ? 1 : 0);
    if (res == 0) return JNI_TRUE;
    else return JNI_FALSE;
}

/*
 * Class:     ernesto_apksigner_util_Utils
 * Method:    isAligned
 * Signature: (Ljava/lang/String;I)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_ernesto_apksigner_util_Utils_isAligned
(JNIEnv* env, jclass cls, jstring path, jint alg_bytes) {
    char* cp = jcc(env, path);
    int res = zipalign_is_aligned(cp, (int)alg_bytes);
    if (res == 0) return JNI_TRUE;
    else return JNI_FALSE;
}