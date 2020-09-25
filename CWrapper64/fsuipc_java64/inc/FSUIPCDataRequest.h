#pragma once

#include <jni.h>
#include <Windows.h>

class FSUIPCDataRequest
{
private:
	DWORD m_dwOffset;
	DWORD m_dwSize;
	jbyte* m_cData;				//C++ data representation	
	jobject m_oData;			//global reference to our java byte array
	JavaVM* m_oJavaVM;			//reference to JavaVM	

public:
	DWORD getOffset() { return m_dwOffset; };
	DWORD getSize() { return m_dwSize; };
	jbyte* getCData() { return m_cData; };		

	void setOffset(DWORD dwOffset) { m_dwOffset = dwOffset; };
	void setSize(DWORD dwSize) { m_dwSize = dwSize; };	

	BOOL alloc(jbyteArray jData);
	BOOL release();

	FSUIPCDataRequest();	
	FSUIPCDataRequest(JavaVM* oJavaVM);
	~FSUIPCDataRequest();
};

