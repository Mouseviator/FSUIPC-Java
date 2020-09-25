#include "FSUIPCDataRequest.h"

BOOL FSUIPCDataRequest::alloc(jbyteArray jData)
{
    if (m_oJavaVM == NULL || jData == NULL) {
        return FALSE;
    }

    //get java env
    JNIEnv* env;
    if (m_oJavaVM->GetEnv((void**)&env, JNI_VERSION_1_6)) {
        //failed to get Java Env
        return FALSE;
    }

    //create global reference to the byte array passed as container to store read values into
    //because otherwise it would not survive between JNI calls and would cause memory exception
    m_oData = env->NewGlobalRef(jData);
    //convert our java byte array to C++ byte array
    m_cData = env->GetByteArrayElements((jbyteArray)m_oData, 0);
    if (m_cData == NULL) {
        //if failed to convert to c byte array, we will not need that global ref
        env->DeleteGlobalRef(m_oData);        
        return FALSE;
    }
    else {
        return TRUE;
    }
}

BOOL FSUIPCDataRequest::release()
{
    if (m_oJavaVM == NULL || m_oData == NULL || m_cData == NULL) {
        return FALSE;
    }

    //get java env
    JNIEnv* env;
    if (m_oJavaVM->GetEnv((void**)&env, JNI_VERSION_1_6)) {
        //failed to get Java Env
        return FALSE;
    }

    //copy back data from C++ byte array to Java byte array, release the C++ byte array
    env->ReleaseByteArrayElements((jbyteArray)m_oData, m_cData, 0);
    //release the global reference to the Java byte array
    env->DeleteGlobalRef(m_oData);
    m_cData = NULL;
    
    return TRUE;
}

FSUIPCDataRequest::FSUIPCDataRequest()
{
    m_dwOffset = 0;
    m_dwSize = 0;
    m_cData = NULL;
    m_oData = NULL;
    m_oJavaVM = NULL;
}

FSUIPCDataRequest::FSUIPCDataRequest(JavaVM* oJavaVM)
{
    m_dwOffset = 0;
    m_dwSize = 0;
    m_cData = NULL;
    m_oData = NULL;
    m_oJavaVM = oJavaVM;
}


FSUIPCDataRequest::~FSUIPCDataRequest()
{    
    release();
}
