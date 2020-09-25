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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

/**
 * This class implements integer data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. The internal byte data buffer length will be 4 bytes (32 bit), matching
 * FSUIPC 32bit signed/unsigned DWORD value. If you are familiar with FSUIPC lua functions, or other data type marking, this will match data that you can
 * read in lua with readUD and readSD function.
 * 
 * @author Mouseviator
 */
public class IntRequest extends DataRequest implements IDataRequest<Integer> {    
    /**
     * Creates a new integer data request.     
     */
    public IntRequest() {
        dataBuffer = new byte[BUFFER_LENGTH_INT];
    }
    
    /**
     * Creates a new integer data request associated with given offset.
     * 
     * @param offset An offset to associate this data request with.
     */ 
    public IntRequest(int offset) {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;            
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }    
    
    /**
     * Creates a new integer data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public IntRequest(int offset, int value) {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(value);
            buf.get(dataBuffer);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    /**
     * Creates a new integer data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public IntRequest(int offset, Integer value) {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(value);
            buf.get(dataBuffer);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    @Override
    public Integer getValue() {
        return getInt();
    }

    @Override
    public void setValue(Integer value) {
        putInt(value);
    }    
}
