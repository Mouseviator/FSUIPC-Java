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
 * This class implements long data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. The internal byte data buffer length will be 8 bytes (64 bit), matching
 * FSUIPC 64bit signed DOUBLE DWORD value. If you are familiar with FSUIPC lua functions, or other data type marking, this will match data that you can
 * read in lua with readDD function.
 * 
 * @author Mouseviator
 */
public class LongRequest extends DataRequest implements IDataRequest<Long> {    
    /**
     * Creates a new long data request.     
     */
    public LongRequest() {
        dataBuffer = new byte[BUFFER_LENGTH_LONG];
    }
    
    /**
     * Creates a new long data request associated with given offset.
     * 
     * @param offset An offset to associate this data request with.
     */ 
    public LongRequest(int offset) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }    
    
    /**
     * Creates a new long data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public LongRequest(int offset, long value) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            putLong(value);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    /**
     * Creates a new long data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */ 
    public LongRequest(int offset, Long value) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            
            putLong(value);
            
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }       
    }
    
    @Override
    public Long getValue() {
        return getLong();
    }

    @Override
    public void setValue(Long value) {
        putLong(value);
    }
    
}
