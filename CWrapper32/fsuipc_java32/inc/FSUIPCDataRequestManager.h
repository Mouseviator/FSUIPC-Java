#pragma once
#include "FSUIPCDataRequest.h"
#include <vector>

class FSUIPCDataRequestManager
{
private:
	std::vector<FSUIPCDataRequest*>		m_arRequests;

public:
	void addRequest(FSUIPCDataRequest* dataRequest);
	void releaseAll();

	FSUIPCDataRequestManager();
	~FSUIPCDataRequestManager();
};

