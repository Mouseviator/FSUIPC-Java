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
import com.mouseviator.fsuipc.FSUIPCWrapper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

/**
 * This is a base abstract class for FSUIPC data request. The data exchange between FSUIPC and the sim is done by storing read/write requests into memory file....
 * this goes down to bytes etc., that is why the exchange of data between Java wrapper and the FSUIPC library also goes down to bytes (byte arrays). This abstract
 * class is like a bridge between the primitive data types and the byte arrays, that {@link FSUIPCWrapper} uses when sending/receiving data with FSUIPC. It would 
 * like non-handy to always work with these arrays.
 * 
 * So, mainly, this class defines basic functions to manipulate that underlying byte array, that holds the actual data,
 * methods and functions such as {@link #putInt(java.lang.Integer) }, {@link #getInt() } to get primitive data to/from the buffer. Classes extending this one can use those
 * functions, so they don't have to write them again. Why reinvent the wheel, yes? One implementation for all.
 * 
 * What is this for? Well, the {@link FSUIPC} class is build around data requests. See also the {@link IDataRequest} interface, which every data request that should be
 * supported by the FSUIPC class must implement.
 * 
 * @author Mouseviator
 */
public abstract class DataRequest {

    /**
     *  Minimum offset value.
     */
    public static final int MIN_OFFSET_VALUE = 0;

    /**
     * Maximum FSUIPC offset value.
     */
    public static final int MAX_OFFSET_VALUE = Integer.MAX_VALUE;

    /**
     * Length of the byte buffer to store one byte.
     */
    public static final byte BUFFER_LENGTH_BYTE = 1;

    /**
     * Length of the byte buffer to store short value.
     */
    public static final byte BUFFER_LENGTH_SHORT = 2;

    /**
     * Length of the byte buffer to store integer value.
     */
    public static final byte BUFFER_LENGTH_INT = 4;

    /**
     * Length of the byte buffer to store long value.
     */
    public static final byte BUFFER_LENGTH_LONG = 8;

    /**
     * Length of the byte buffer to store float value.
     */
    public static final byte BUFFER_LENGTH_FLOAT = 4;

    /**
     * Length of the byte buffer to store double value.
     */
    public static final byte BUFFER_LENGTH_DOUBLE = 8;
    
    /**
     * The actual byte data buffer that will hold the value.
     */
    protected byte[]  dataBuffer;

    /**
     * Init offset to 66C0, which is for "general use", should not cause any trouble if someone forgets to set the value        
     */
    protected int offset = 0x66C0;      

    /**
     * Data request type, default to read
     */
    protected IDataRequest.RequestType type = IDataRequest.RequestType.READ;           
    
    /**
     * Returns the actual byte buffer that holds the request data.
     * 
     * @return Byte array.
     */
    public byte[] getDataBuffer() {
        return dataBuffer;
    }
    
    /**
     * Returns the offset the data request is associated with.
     * 
     * @return The offset.
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Returns the size of the byte buffer holding this request data.
     * 
     * @return Size of the byte data buffer.
     */
    public int getSize() {
        if (dataBuffer != null) {
            return dataBuffer.length;
        } else {
            return -1;
        }
    }    
    
    /**
     * Sets the offset this data request is associated with.
     * 
     * @param offset The offset.
     * @throws InvalidParameterException If offset is outside the {@link #MIN_OFFSET_VALUE} and {@link #MAX_OFFSET_VALUE}.
     */
    public void setOffset(int offset) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            this.offset = offset;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    /**
     * Returns the type of this data request (Whether READ or WRITE).
     * 
     * @return Type of this data request.
     */
    public IDataRequest.RequestType getType() {
        return this.type;
    }
    
    /**
     * Sets the type of this data request.
     * 
     * @param type Data request type.
     */
    public void setType(IDataRequest.RequestType type) {
        this.type = type;
    }
    
    /**
     * Returns data stored in this data request as short value.
     * 
     * @return Value stored within the internal byte data buffer as Short value.
     */
    protected final short getShort() {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_SHORT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(dataBuffer, 0, 2);
        return buf.getShort(0);
    }
    
    /**
     * Returns data stored in this data request as integer value.
     * 
     * @return Value stored within the internal byte data buffer as Integer value.
     */
    protected final int getInt() {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(dataBuffer, 0, 4);
        return buf.getInt(0);        
    }
    
    /**
     * Returns data stored in this data request as long value.
     * 
     * @return Value stored within the internal byte data buffer as Long value.
     */
    protected final long getLong() {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_LONG);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(dataBuffer, 0, 8);
        return buf.getLong(0);        
    }
    
    /**
     * Returns data stored in this data request as float value.
     * 
     * @return Value stored within the internal byte data buffer as Float value.
     */
    protected final float getFloat() {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_FLOAT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(dataBuffer, 0, 4);
        return buf.getFloat(0);
    }
    
    /**
     * Returns data stored in this data request as double value.
     * 
     * @return Value stored within the internal byte data buffer as Double value.
     */
    protected final double getDouble() {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_DOUBLE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(dataBuffer, 0, 8);        
        return buf.getDouble(0);
    }
    
    /**
     * Will store given short value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putShort(Short value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_SHORT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }
    
    /**
     * Will store given integer value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putInt(Integer value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }
    
    /**
     * Will store given long value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putLong(Long value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_LONG);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putLong(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }    
    
    /**
     * Will store given float value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putFloat(Float value) {
        final ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }    
    
    /**
     * Will store given double value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putDouble(Double value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_DOUBLE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putDouble(value);
        buf.rewind();
        buf.get(dataBuffer);        
    } 
    
    /**
     * Will store given short value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putShort(short value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_SHORT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }
    
    /**
     * Will store given integer value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putInt(int value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }
    
    /**
     * Will store given long value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putLong(long value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_LONG);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putLong(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }    
    
    /**
     * Will store given float value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putFloat(float value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_FLOAT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }    
    
    /**
     * Will store given double value in this data request byte buffer.
     * 
     * @param value A value to store in the underlying byte data buffer.
     */
    protected final void putDouble(double value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_DOUBLE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putDouble(value);
        buf.rewind();
        buf.get(dataBuffer);        
    }              
}
