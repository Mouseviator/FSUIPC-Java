// fsuipc_java.cpp : Defines the entry point for the DLL application.
//

#include <windows.h>
#include <iostream>
#include "fsuipc_java64.h"
#include "com_mouseviator_fsuipc_FSUIPCWrapper.h"
#include "fsuipc_user64.h"
#include "FSUIPCDataRequest.h"
#include "FSUIPCDataRequestManager.h"
#include <boost/log/core.hpp>
#include <boost/log/trivial.hpp>
#include <boost/log/expressions.hpp>
#include <boost/format.hpp>
#include <boost/log/utility/setup/file.hpp>
#include <boost/log/utility/setup/common_attributes.hpp>
#include <boost/log/sinks/text_file_backend.hpp>
#include <boost/lambda/lambda.hpp>


namespace logging = boost::log;
namespace keywords = boost::log::keywords;
namespace sinks = boost::log::sinks;
namespace bll = boost::lambda;

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
			//init logging upon DLL entry
			logging::register_simple_formatter_factory<logging::trivial::severity_level, char>("Severity");			
			setup_logging(false, "fsuipc_java64.log", LOGGING_SL_INFO, DEFAULT_ROTATION_SIZE);
			break;
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
			break;    
	}	
	
    return TRUE;
}


/**********************************************************************************************************************************************
* Variable definitions start
**********************************************************************************************************************************************/


// Stores the result of last FSUIPC function call
static DWORD iResult;

//A manager to store requests made to FSUIPC between calls o process function
static FSUIPCDataRequestManager* oFSUIPCDataRequestManager = new FSUIPCDataRequestManager();

// Whether file logging has already been enabled
static boolean bFileLoggingEnabled = false;

//Reference to file logging sink
boost::shared_ptr <sinks::synchronous_sink< sinks::text_file_backend>> file_logging_sink = NULL;

//A reference to cached JavaVirtualMachine
JavaVM* cachedJVM;

/**********************************************************************************************************************************************
* Variable definitions end
**********************************************************************************************************************************************/


/**********************************************************************************************************************************************
* Library (helper) functions implementation start
**********************************************************************************************************************************************/

void setup_logging(boolean bEnableFileLogging, const char* pFileName, byte severityLevel, DWORD rotationSize)
{
	BOOST_LOG_TRIVIAL(trace) << "setup_logging called with params EnableFileLogging=" << (int)bEnableFileLogging << " , FileName=" << pFileName << ", severity level=" << (int)severityLevel << " ,rotation size=" << rotationSize;

	if (bEnableFileLogging && !bFileLoggingEnabled) {		
		
		//add logging to file		
		file_logging_sink = logging::add_file_log(
			keywords::file_name = pFileName,
			keywords::rotation_size = rotationSize,
			keywords::format = "[%TimeStamp%] [%LineID%] [%ProcessID%] [%ThreadID%] [%Severity%] %Message%"
		);

		
		// Set header and footer writing functors
		file_logging_sink->locked_backend()->set_open_handler
		(
			bll::_1 << "Started logging to file: " << pFileName << " with severity level: " << (int)severityLevel << " and rotation size: " << rotationSize << " .This is " << DLL_NAME << " ,version " << DLL_VERSION << "\n"
		);
		file_logging_sink->locked_backend()->set_close_handler
		(
			bll::_1 << "This is the last line of the log. Good Bye.\n"
		);

		BOOST_LOG_TRIVIAL(info) << "Enabled logging to file: " << pFileName;

		bFileLoggingEnabled = true;
	}
	else if (!bEnableFileLogging  && file_logging_sink != NULL) {
		//remove the file_logging_sink from boost core

		logging::core::get()->remove_sink(file_logging_sink);
		file_logging_sink = NULL;

		bFileLoggingEnabled = false;

		BOOST_LOG_TRIVIAL(info) << "Disabled logging to file: " << pFileName;
	}	

	//setup severity level
	logging::trivial::severity_level loggingSeverityLevel;
	switch (severityLevel) {
	case LOGGING_SL_TRACE :
		loggingSeverityLevel = logging::trivial::trace;
		break;
	case LOGGING_SL_DEBUG :
		loggingSeverityLevel = logging::trivial::debug;
		break;
	case LOGGING_SL_WARNING :
		loggingSeverityLevel = logging::trivial::warning;
		break;
	case LOGGING_SL_ERROR :
		loggingSeverityLevel = logging::trivial::error;
		break;
	case LOGGING_SL_FATAL :
		loggingSeverityLevel = logging::trivial::fatal;
		break;
	default :
		loggingSeverityLevel = logging::trivial::info;
	}

	BOOST_LOG_TRIVIAL(info) << "Logging severity changed to level: " << loggingSeverityLevel;

	logging::core::get()->set_filter(
		logging::trivial::severity >= loggingSeverityLevel
	);

	logging::add_common_attributes();
}

/**********************************************************************************************************************************************
* Library (helper) functions implementation end
**********************************************************************************************************************************************/


/**********************************************************************************************************************************************
* JNI functions implementation start
**********************************************************************************************************************************************/

/**
* This function will be called when the JVM calls System.loadLibrary to load this library.
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	cachedJVM = vm;
	
	BOOST_LOG_TRIVIAL(info) << "JNI_OnLoad called! Returning JNI version: " << JNI_VERSION_1_6;

	return JNI_VERSION_1_6;	
}

/**
* This function will be called when the library is being unloaded
*/
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
	BOOST_LOG_TRIVIAL(info) << "JNI_OnUnload called! Calling FSUIPCDataRequestManager to release all remaining requests!";
	//release all data requests in case it was not done 
	oFSUIPCDataRequestManager->releaseAll();

	BOOST_LOG_TRIVIAL(info) << "Calling FSUIPC_Close just in case client left connection opened!";
	//close FSUIPC connection (for the case user app forgot to do it)
	FSUIPC_Close();

	BOOST_LOG_TRIVIAL(info) << "Bye!";
}

/*
 * This function returns the last result of FSUIPC function calls.

 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    getResult
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getResult(JNIEnv*, jclass)
{
	return iResult;
}

/*
 * This function opens FSUIPC connection.
 *
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    Open
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_open(JNIEnv* env, jclass, jint aFlightSim)
{
	jint iRet = FSUIPC_Open(aFlightSim, &iResult);

	BOOST_LOG_TRIVIAL(debug) << "FSUIPC_Open called with parameter aFlightSim=" << aFlightSim << ". Returned value is: " << iRet;

	return iRet;
}

/*
 * This method closes FSUIPC connection
 *
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    Close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_close(JNIEnv* env, jclass)
{	
	FSUIPC_Close();
	BOOST_LOG_TRIVIAL(debug) << "FSUIPC_Close() called! Releasing all remaining data requests!";
	//relase all data request that might be still there
	oFSUIPCDataRequestManager->releaseAll();
}

/*
 * This function will process all stored FSUIPC read and write requests.
 *
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    Process
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_process(JNIEnv* env, jclass) {
	jint iRet = 0;
	
	// call FSUIPC_Process to process all waiting requests
	iRet = FSUIPC_Process(&iResult);
	BOOST_LOG_TRIVIAL(info) << "FSUIPC_Process() called! Will now call FSUIPCRequestManager->releaseAll() to process all returned data!";

	//instruct the FSUIPCDataRequestManager to release all requests - copy changed data back to JVM
	oFSUIPCDataRequestManager->releaseAll();

	return iRet;
}

/*
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    Read
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_read(JNIEnv* env, jclass, jint aOffset, jint aSize, jbyteArray aData)
{
	jint iRet = FALSE;

	/* We create a new FSUIPC data request, so that we store a reference to the data being modified. They will not be modified right away by the call
	* of FSUIPC_Read, it will only store the request. The data will be modified by calling the FSUIPC_Process and we need to have a reference to those byte
	* buffers to be able to copy them back to the Java Virtual Machine
	*/
	FSUIPCDataRequest* dataRequest = new FSUIPCDataRequest(cachedJVM);
	if (dataRequest->alloc(aData)) {
		iRet = FSUIPC_Read(aOffset, aSize, (void*)dataRequest->getCData(), &iResult);
		//add request to manager if the read function successfully stored the data read request
		if (iRet) {
			oFSUIPCDataRequestManager->addRequest(dataRequest);
			BOOST_LOG_TRIVIAL(debug) << "The FSUIPC_Read succesfully stored read request! The offset was: " << boost::format("0x%04X") % aOffset << " ,the data length was: " << aSize << " and value of Result variable is: " << iResult;
		}
		else {
			BOOST_LOG_TRIVIAL(fatal) << "The FSUIPC_Read FAILED to store read request! The offset was: " << boost::format("0x%04X") % aOffset << " ,the data length was: " << aSize << " and value of Result variable is: " << iResult;
		}
	}
	else {
		BOOST_LOG_TRIVIAL(warning) << "Failed to allocate data for FSUIPC read request!";
	}
			
	return iRet;
}

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    readData
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_readData(JNIEnv* env, jclass, jint aOffset, jint aSize, jbyteArray aData)
{
	BOOL bRet = TRUE;
	jbyte* data = env->GetByteArrayElements(aData, 0);
	bRet &= FSUIPC_Read(aOffset, aSize, (void*)data, &iResult);
	bRet &= FSUIPC_Process(&iResult);
	env->ReleaseByteArrayElements(aData, data, 0);
	return bRet;	//this only will stay TRUE unless none of the FSUIPC calls above fails!
}

/*
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    Write
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_write(JNIEnv* env, jclass, jint aOffset, jint aSize, jbyteArray aData)
{	
	jint iRet = FALSE;

	/* We create a new FSUIPC data request, so that we store a reference to the data being modified. They will not be modified right away by the call
	* of FSUIPC_Write, it will only store the request. The data will be modified by calling the FSUIPC_Process and we need to have a reference to those byte
	* buffers to be able to copy them back to the Java Virtual Machine

	* NOTE: Probably not needed to hold references, as FSUIPC_Write will copy the data and nothinf should be returned
	*/
	FSUIPCDataRequest* dataRequest = new FSUIPCDataRequest(cachedJVM);
	if (dataRequest->alloc(aData)) {
		iRet = FSUIPC_Write(aOffset, aSize, (void*)dataRequest->getCData(), &iResult);
		if (iRet) {
			oFSUIPCDataRequestManager->addRequest(dataRequest);
			BOOST_LOG_TRIVIAL(debug) << "The FSUIPC_Read succesfully stored write request! The offset was: " << boost::format("0x%04X") % aOffset << " ,the data length was: " << aSize << " and value of Result variable is: " << iResult;
		}
		else {
			BOOST_LOG_TRIVIAL(fatal) << "The FSUIPC_Read FAILED to store write request! The offset was: " << boost::format("0x%04X") % aOffset << " ,the data length was: " << aSize << " and value of Result variable is: " << iResult;
		}
	}
	else {
		BOOST_LOG_TRIVIAL(warning) << "Failed to allocate data for FSUIPC write request!";
	}
		
	return iRet;
}

/*
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    writeData
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_writeData(JNIEnv* env, jclass, jint aOffset, jint aSize, jbyteArray aData)
{
	BOOL bRet = TRUE;
	jbyte* data = env->GetByteArrayElements(aData, 0);
	bRet &= FSUIPC_Write(aOffset, aSize, (void*)data, &iResult);
	bRet &= FSUIPC_Process(&iResult);
	env->ReleaseByteArrayElements(aData, data, 0);
	return bRet;	//this only will stay TRUE unless none of the FSUIPC calls above fails!
}

/*
 * This function returns the FSUIPC_Lib_Version variable from the FSUIPC library.
 *
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    getFSUIPC_FS_Version
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getLibVersion(JNIEnv*, jclass)
{
	return FSUIPC_Lib_Version;
}

/*
 * This function returns the FSUIPC_FS_Version variable from the FSUIPC library.
 *
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    getFSUIPC_FS_Version
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getFSVersion(JNIEnv*, jclass)
{
	return FSUIPC_FS_Version;
}

/*
 * This function returns the FSUIPC_Version variable from the FSUIPC library.
 *
 * Class:     com_mouseviator_fsuipc_fsuipc_wrapper
 * Method:    getFSUIPC_Version
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_getVersion(JNIEnv*, jclass)
{
	return FSUIPC_Version;
}

/*
 * This function will setup logging.
 *
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    setupLogging
 * Signature: (ZB)V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_setupLogging__ZB(JNIEnv* env, jclass, jboolean bEnableFileLogging, jbyte severityLevel)
{
	setup_logging(bEnableFileLogging, "fsuipc_java64.log", severityLevel, DEFAULT_ROTATION_SIZE);
}

/*
 * This function will setup logging.
 *
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    setupLogging
 * Signature: (ZLjava/lang/String;B)V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_setupLogging__ZLjava_lang_String_2B(JNIEnv* env, jclass, jboolean bEnableFileLogging, jstring fileName, jbyte severityLevel) 
{
	const char* pFileName = env->GetStringUTFChars(fileName, NULL);
	setup_logging(bEnableFileLogging, pFileName, severityLevel, DEFAULT_ROTATION_SIZE);
	env->ReleaseStringUTFChars(fileName, pFileName);
}

/*
 * This function will setup logging.
 *
 * Class:     com_mouseviator_fsuipc_FSUIPCWrapper
 * Method:    setupLogging
 * Signature: (ZLjava/lang/String;B)V
 */
JNIEXPORT void JNICALL Java_com_mouseviator_fsuipc_FSUIPCWrapper_setupLogging__ZLjava_lang_String_2BI(JNIEnv* env, jclass, jboolean bEnableFileLogging, jstring fileName, jbyte severityLevel, jint rotationSize)
{
	const char* pFileName = env->GetStringUTFChars(fileName, NULL);
	setup_logging(bEnableFileLogging, pFileName, severityLevel, rotationSize);
	env->ReleaseStringUTFChars(fileName, pFileName);
}


/**********************************************************************************************************************************************
* JNI functions implementation end
**********************************************************************************************************************************************/