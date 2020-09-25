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
 * This class implements short data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. The internal byte data buffer length will be 2 bytes (16 bit), matching
 * FSUIPC 16bit signed/unsigned WORD value. If you are familiar with FSUIPC lua functions, or other data type marking, this will match data that you can
 * read in lua with readUW and readSW function.
 * 
 * @author Mouseviator
 */
public class ShortRequest extends DataRequest implements IDataRequest<Short> {            
    /**
     * Creates a new short data request.     
     */
    public ShortRequest() {
        dataBuffer = new byte[BUFFER_LENGTH_SHORT];
    }
    
    /**
     * Creates a new short data request associated with given offset.
     * 
     * @param offset An offset to associate this data request with.
     */ 
    public ShortRequest(int offset) {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;                        
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    /**
     * Creates a new short data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public ShortRequest(int offset, short value) {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_SHORT);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.putShort(value);
            buf.get(dataBuffer);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    /**
     * Creates a new short data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public ShortRequest(int offset, Short value) {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_SHORT);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.putShort(value);
            buf.get(dataBuffer);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    @Override
    public Short getValue() {
        return getShort();
    }

    @Override
    public void setValue(Short value) {
        putShort(value);
    }    
}
