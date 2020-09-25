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
package com.mouseviator.fsuipc.helpers.aircraft;

import com.mouseviator.fsuipc.datarequest.DataRequest;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_SHORT;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;

/**
 * This class provides methods that return data requests to gather various info about aircraft gear.
 */
public class GearHelper {
    /**
     * Returns data request to get/set gear control. The value returned by this request / or to be set by this request, ranges from 0 to 16383. Where:
     * 0=full up, 16383=full down. 
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set nose gear position.
     */    
    public IntRequest getControlLever() {
        return new IntRequest(0x0BE8);
    }
    
    /**
     * Returns data request to get/set nose gear position. The value returned by this request / or to be set by this request, ranges from 0 to 16383. Where:
     * 0=full up, 16383=full down.
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set nose gear position.
     */
    public IntRequest getNosePosition() {
        return new IntRequest(0x0BEC);
    }
    
    /**
     * Returns data request to get/set right gear position. The value returned by this request / or to be set by this request, ranges from 0 to 16383. Where:
     * 0=full up, 16383=full down.
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set right gear position.
     */    
    public IntRequest getRightPosition() {
        return new IntRequest(0x0BF0);
    }
    
    /**
     * Returns data request to get/set left gear position. The value returned by this request / or to be set by this request, ranges from 0 to 16383. Where:
     * 0=full up, 16383=full down.
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set left gear position.
     */    
    public IntRequest getLefttPosition() {
        return new IntRequest(0x0BF4);
    }
    
    /**
     * Returns data request to get center (nose or tail) wheel RPM.
     * 
     * @return Data request to get center wheel RPM.
     */
    public IDataRequest<Short> getCenterWheelRPM() {
        class CenterWheelRPMRequest extends DataRequest implements IReadOnlyRequest<Short> {
            {
                this.offset = 0x0266;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }
            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new CenterWheelRPMRequest();
    }
    
    /**
     * Returns data request to get left wheel RPM.
     * 
     * @return Data request to get left wheel RPM.
     */
    public IDataRequest<Short> getLeftWheelRPM() {
        class LeftWheelRPMRequest extends DataRequest implements IReadOnlyRequest<Short> {
            {
                this.offset = 0x0268;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }
            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new LeftWheelRPMRequest();
    }
    
    /**
     * Returns data request to get right wheel RPM.
     * 
     * @return Data request to get right wheel RPM.
     */
    public IDataRequest<Short> getRightWheelRPM() {
        class RightWheelRPMRequest extends DataRequest implements IReadOnlyRequest<Short> {
            {
                this.offset = 0x026A;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }
            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new RightWheelRPMRequest();
    }
}
