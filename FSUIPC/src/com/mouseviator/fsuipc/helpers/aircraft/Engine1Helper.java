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
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.ShortRequest;

/**
 * This class provides methods that return data requests to gather various info about aircraft engine number 1.
 * The returned requests are usually modified to return user-friendly data type, even thought the underlying data type may be different.
 * 
 * @author Mouseviator
 */
public class Engine1Helper {
    protected int throttleLeverOffset = 0x088C;
    protected int propLeverOffset = 0x088E;
    protected int mixtureLeverOffset = 0x0890;
    protected int fuelFlowOffset = 0x0918;      //pounds per hour, float64 (double)
    protected int oilTempOffset = 0x08B8;       //140C = 16384
    protected int oilPressureOffset = 0x08BA;   //16384 = 55 psi, 65535 = 220 psi
    protected int oilQuantityOffset = 0x08D0;         //16384 = 100%
    protected int manifoldPressureOffset = 0x08C0;
    protected int fuelUsedOffset = 0x090C;
    protected int elapsedTimeOffset = 0x0910;
    
    /**
     * This will return a request to read engine throttle lever position. Value range from -4096 to 16384. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine throttle lever position.
     */
    public ShortRequest getThrottleLever() {
        return new ShortRequest(throttleLeverOffset);
    }
    
    /**
     * This will return a request to read engine propeller lever position. Value range from -4096 to 16384. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine propeller lever position.
     */
    public ShortRequest getPropellerLever() {
        return new ShortRequest(propLeverOffset);
    }
    
    /**
     * This will return a request to read engine mixture lever position. Value range from 0 to 16384. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine mixture lever position.
     */
    public ShortRequest getMixtureLever() {
        return new ShortRequest(mixtureLeverOffset);
    }
    
    /**
     * This will return a request to read engine fuel flow. Pounds per hour as double value. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine fuel flow.
     */
    public DoubleRequest getFuelFlow() {
        return new DoubleRequest(fuelFlowOffset);
    }
    
    /**
     * This will return a request to read engine Oil Temperature. The result will  in degrees, float value, max value is 140C.
     * The setValue method is also overridden. Expects oil temperature value in degrees C, max 140. However, not guaranteed to work. See
     * description of offset 0x08B8 in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine oil temperature.
     */
    public FloatRequest getOilTemperature() {
        return new FloatRequest(oilTempOffset) {
            {
                this.dataBuffer = new byte[2];
            }
            
            @Override           
            public Float getValue() {
                short oilTemp = getShort();
                
                return oilTemp / 16384.0f * 140.0f;
            } 

            @Override
            public void setValue(Float value) {
                short oilTemp = (short)(value / 140.0f * 16384.0f);
                putShort(oilTemp);
            }                        
        };
    }
    
    /**
     * This will return a request to read engine Oil quantity. The result will  in percent, float value.
     * The setValue method is also overridden. Expects oil quantity in percent. However, not guaranteed to work. See
     * description of offset 0x08D0 in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine oil quantity.
     */
    public FloatRequest getOilQuantity() {
        return new FloatRequest(oilQuantityOffset) {
            @Override
            public Float getValue() {
                int quantity = getInt();
                
                return quantity / 16384.0f * 100.0f;
            }

            @Override
            public void setValue(Float value) {
                int quantity = (int)(value / 100.0f * 16384.0f);
                putInt(quantity);
            }           
        };
    }
    
    /**
     * This will return a request to read engine Oil pressure. The result will  be in psi, max 220, float value.
     * The setValue method is also overridden. Expects oil quantity in percent. However, not guaranteed to work. See
     * description of offset 0x08D0 in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine oil pressure.
     */
    public FloatRequest getOilPressure() {
        return new FloatRequest(oilPressureOffset) {
            @Override
            public Float getValue() {
                short pressure = getShort();
                
                return pressure / 65535.0f * 220.0f;
            }

            @Override
            public void setValue(Float value) {
                short pressure = (short)(value * 220.0f / 65535.0f);
                putInt(pressure);
            }           
        };
    }
    
    /**
     * This will return a request to read engine manifold pressure. The result will be in Inches Hg, float value.
     * The setValue method is also overridden. Expects manifold pressure in Inches Hg. However, not guaranteed to work. See
     * description of offset 0x08C0 in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine manifold pressure.
     */
    public FloatRequest getManifoldPressure() {
        return new FloatRequest(manifoldPressureOffset) {
            {
                this.dataBuffer = new byte[2];
            }

            @Override
            public Float getValue() {
                short manPressure = getShort();
                
                return manPressure / 1024.0f;
            }; 

            @Override
            public void setValue(Float value) {
                short manPressure = (short)(value * 1024);
                putShort(manPressure);
            }                        
        };
    }
    
    /**
     * This will return a request to read engine fuel used since start. Value in pounds as float value. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#getType() } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set engine fuel used.
     */
    public FloatRequest getFuelUsed() {
        return new FloatRequest(fuelUsedOffset);
    }
    
    /**
     * This will return a request to read engine elapsed time. Value in hours as float value. READ ONLY. 
     * 
     * @return Data request to get engine elapsed time.
     */
    public IDataRequest<Float> getElapsedTime() {        

        class ElapsedTimeRequest extends DataRequest implements IReadOnlyRequest<Float> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_FLOAT];
                this.offset = elapsedTimeOffset;
            }
            
            @Override
            public Float getValue() {
                return getFloat();
            }
        } 
        return new ElapsedTimeRequest();        
    }
}
