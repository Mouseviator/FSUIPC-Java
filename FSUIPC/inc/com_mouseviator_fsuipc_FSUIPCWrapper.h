/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_mouseviator_fsuipc_FSUIPCWrapper */

#ifndef _Included_com_mouseviator_fsuipc_FSUIPCWrapper
#define _Included_com_mouseviator_fsuipc_FSUIPCWrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    open
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_open
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_close
  (JNIEnv *, jclass);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    read
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_read
  (JNIEnv *, jclass, jint, jint, jbyteArray);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    readData
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_readData
  (JNIEnv *, jclass, jint, jint, jbyteArray);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    write
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_write
  (JNIEnv *, jclass, jint, jint, jbyteArray);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    writeData
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_writeData
  (JNIEnv *, jclass, jint, jint, jbyteArray);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    process
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_process
  (JNIEnv *, jclass);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    getResult
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getResult
  (JNIEnv *, jclass);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    getFSVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getFSVersion
  (JNIEnv *, jclass);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    getVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getVersion
  (JNIEnv *, jclass);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    getLibVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getLibVersion
  (JNIEnv *, jclass);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    setupLogging
 * Signature: (ZB)V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_setupLogging__ZB
  (JNIEnv *, jclass, jboolean, jbyte);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    setupLogging
 * Signature: (ZLjava/lang/String;B)V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_setupLogging__ZLjava_lang_String_2B
  (JNIEnv *, jclass, jboolean, jstring, jbyte);

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    setupLogging
 * Signature: (ZLjava/lang/String;BI)V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_setupLogging__ZLjava_lang_String_2BI
  (JNIEnv *, jclass, jboolean, jstring, jbyte, jint);

#ifdef __cplusplus
}
#endif
#endif