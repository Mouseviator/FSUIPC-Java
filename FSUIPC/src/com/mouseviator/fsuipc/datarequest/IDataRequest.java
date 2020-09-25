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
package com.mouseviator.fsuipc.datarequest;

import com.mouseviator.fsuipc.FSUIPC;


/**
 * This interface is used by many functions in {@link FSUIPC} class. Every data request that should be supported by the FSUIPC class must implement this interface.
 * It provides basic functions that are common for all data requests no matter the data (data type) they hold internally. See also the {@link DataRequest} abstract class,
 * which provides many default functions useful when implementing data request.
 * 
 * @author Mouseviator
 * @param <DataType> The data type of this data request, such as Byte, Short...
 */
public interface IDataRequest<DataType> {
    /**
     * Returns the actual byte buffer that holds the request data.
     * 
     * @return Byte array.
     */
    public byte[] getDataBuffer();
        
    /**
     * Returns the offset the data request is associated with.
     * 
     * @return The offset.
     */
    public int getOffset();
    
    /**
     * Returns the size of the byte buffer holding this request data.
     * 
     * @return Size of the byte data buffer.
     */
    public int getSize();
            
    /**
     * Sets the offset this data request is associated with.
     * 
     * @param offset The offset.     
     */
    public void setOffset(int offset);
    
    /**
     * Returns the value stored within this data request byte data buffer.
     * 
     * @return The value stored within this data request.
     */
    public DataType getValue();
    
    /**
     * Sets the value to be stored in the byte data buffer of this data request.
     * 
     * @param value The value to store.
     */
    public void setValue(DataType value);
    
    /**
     * Returns the type of this data request (Whether READ or WRITE).
     * 
     * @return Type of this data request.
     */
    public RequestType getType();
    
    /**
     * Sets the type of this data request.
     * 
     * @param type Data request type.
     */
    public void setType(RequestType type);
    
    /**
     * This enumeration defines the possible types of data request.
     */
    public static enum RequestType {
        READ,
        WRITE
    }
}
