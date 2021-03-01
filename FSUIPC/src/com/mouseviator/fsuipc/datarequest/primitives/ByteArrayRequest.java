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

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import java.security.InvalidParameterException;

/**
 * This class implements byte array data request for use with {@link FSUIPC} class. It is for good use when reading/writing bigger chunks of data
 * which we cannot wrap into primitive data types.
 * 
 * @author Mouseviator
 */
public class ByteArrayRequest extends DataRequest implements IDataRequest<byte[]>{

    /**
     * Creates a new byte array data request of specified size.
     * 
     * @param size The number of bytes to allocate for this data request.
     */
    public ByteArrayRequest(int size) {
        this.dataBuffer = new byte[size];
    }
    
    /**
     * Creates a new byte array data request of specified size associated with given offset.
     * 
     * @param offset An offset to associate this data request with.
     * @param size The number of bytes to allocate for this data request.
     */
    public ByteArrayRequest(int offset, int size) throws InvalidParameterException {
        this(size);
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;                        
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    /**
     * Creates a new byte array data request associated with given offset and initialized with data from given byte array. This also sets the data request type to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param data An actual data. Note that the array will not be copied, will be used directly.
     */
    public ByteArrayRequest(int offset, byte[] data) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;       
            //rather copy the data than just assigning reference
            copyByteArray(data);
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    @Override
    public byte[] getValue() {
        return this.dataBuffer;
    }

    @Override
    public void setValue(byte[] value) {        
        copyByteArray(value);
    }            
}
