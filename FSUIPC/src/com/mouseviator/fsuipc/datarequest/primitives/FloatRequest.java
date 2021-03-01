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
package com.mouseviator.fsuipc.datarequest.primitives;

import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import static com.mouseviator.fsuipc.datarequest.DataRequest.MAX_OFFSET_VALUE;
import static com.mouseviator.fsuipc.datarequest.DataRequest.MIN_OFFSET_VALUE;
import java.security.InvalidParameterException;

/**
 * This class implements short data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. The internal byte data buffer length will be 4 bytes (32 bit), matching
 * FSUIPC 32bit signed floating point value. If you are familiar with FSUIPC lua functions, or other data type marking, this will match data that you can
 * read in lua with readFLT function.
 * 
 * @author Mouseviator
 */
public class FloatRequest extends DataRequest implements IDataRequest<Float> {    
    /**
     * Creates a new float data request.     
     */
    public FloatRequest() {
        dataBuffer = new byte[BUFFER_LENGTH_FLOAT];
    }
    
    /**
     * Creates a new float data request associated with given offset.
     * 
     * @param offset An offset to associate this data request with.
     */ 
    public FloatRequest(int offset) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;                        
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    /**
     * Creates a new float data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public FloatRequest(int offset, float value) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            putFloat(value);            
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    /**
     * Creates a new float data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public FloatRequest(int offset, Float value) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            putFloat(value);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }    

    @Override
    public Float getValue() {
        return getFloat();
    }

    @Override
    public void setValue(Float value) {
        putFloat(value);
    }
}
