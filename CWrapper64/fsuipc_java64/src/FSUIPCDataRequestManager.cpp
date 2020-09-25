#include "FSUIPCDataRequestManager.h"
#include <boost/log/core.hpp>
#include <boost/log/trivial.hpp>

void FSUIPCDataRequestManager::addRequest(FSUIPCDataRequest* dataRequest)
{
	if (dataRequest != NULL) {
		m_arRequests.push_back(dataRequest);
		BOOST_LOG_TRIVIAL(debug) << "Stored new FSUIPC data request! The current count of requests is: " << m_arRequests.size();
	}	
}

void FSUIPCDataRequestManager::releaseAll()
{
	size_t iSize = m_arRequests.size();
	
	BOOST_LOG_TRIVIAL(debug) << "Releasing all stored FSUIPC data requests! There is: " << iSize << " requests stored!";

	for (size_t i = 0; i < iSize; i++) {
		FSUIPCDataRequest* dataRequest = m_arRequests[i];
		dataRequest->release();
		delete (dataRequest);
	}

	m_arRequests.clear();

	BOOST_LOG_TRIVIAL(info) << "All FSUIPC data requests released!";
}

FSUIPCDataRequestManager::FSUIPCDataRequestManager()
{
}

FSUIPCDataRequestManager::~FSUIPCDataRequestManager()
{
	//Will delete all requests that we store
	BOOST_LOG_TRIVIAL(debug) << "FSUIPCDataRequestManager destructor called! Will release all stored data requests...";

	releaseAll();
}
