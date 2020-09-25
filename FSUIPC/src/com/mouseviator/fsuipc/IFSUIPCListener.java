/**
 * <pre>
 * ##########################################################################################################
 * ######                            This file is part of Java FSUIPC SDK                              ######
 * ######                                        Version: 1.0                                          ######
 * ######         Based upon 64 bit Java SDK by Paul Henty who amended 32 bit SDK by Mark Burton       ######
 * ######                                   ©2020, Radek Henys                                         ######
 * ######                         All rights .... well, this will be LGPL or so                        ######
 * ######                                   http:\\mouseviator.com                                     ######
 * ##########################################################################################################
 * </pre>
 */
package com.mouseviator.fsuipc;

import com.mouseviator.fsuipc.datarequest.IDataRequest;
import java.util.AbstractQueue;

/**
 * This interface to be implemented to receive notifications from {@link FSUIPC} library.
 * Use {@link FSUIPC#addListener(com.mouseviator.fsuipc.IFSUIPCListener) } to add implemented listener.
 * 
 * @author Mouseviator
 */
public interface IFSUIPCListener {
    /**
     * This function will be called once FSUIPC is connected.
     */
    public void onConnected();
    /**
     * This function will be called once FSUIPC is disconnected.
     */
    public void onDisconnected();
    /**
     * This function will be called every time the FSUIPC_Process function is being called.Ie. every time the library actually instructs FSUIPC to exchange data.
     * @param arRequests A queue of requests that has been processed. This will be only requests stored int the continual requests queue. The "one-time" requests queue is discarded after every successful processing.
     */
    public void onProcess(AbstractQueue<IDataRequest> arRequests);
    /**
     * This function will be called when FSUIPC functions return something other than OK value....
     * 
     * @param lastResult The last result code indicating what went wrong.
     */
    public void onFail(int lastResult);
}
