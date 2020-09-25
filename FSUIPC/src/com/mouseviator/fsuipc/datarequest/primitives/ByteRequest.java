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
import java.security.InvalidParameterException;

/**
 * This class implements byte data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. The internal byte data buffer length will be 1 bytes (8 bit), matching
 * FSUIPC 8bit signed/unsigned byte values. If you are familiar with FSUIPC lua functions, or other data type marking, this will match data that you can
 * read in lua with readUB or readSB function.
 * 
 * @author Mouseviator
 */
public class ByteRequest extends DataRequest implements IDataRequest<Byte> {
        
    /**
     * Creates a new byte data request.     
     */
    public ByteRequest() {
        dataBuffer = new byte[BUFFER_LENGTH_BYTE];
    }
    
    /**
     * Creates a new byte data request associated with given offset.
     * 
     * @param offset An offset to associate this data request with.
     */    
    public ByteRequest(int offset) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;                        
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }       
    
    /**
     * Creates a new byte data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */    
    public ByteRequest(int offset, byte value) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
            dataBuffer[0] = value;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    /**
     * Creates a new byte data request associated with given offset and with given value.This also sets data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value The actual value.
     */        
    public ByteRequest(int offset, Byte value) throws InvalidParameterException {
        this();
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;        
            dataBuffer[0] = value;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    @Override
    public Byte getValue() {
        return dataBuffer[0];
    }

    @Override
    public void setValue(Byte value) {
        dataBuffer[0] = value;
    }        
}
