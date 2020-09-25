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
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.InvalidParameterException;

/**
 * This class implements string data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. Well, FSUIPC will read/write string values as series of bytes, usually terminated
 * as classic C string, by 0 byte. But with Strings we always have this problem with encoding...  so to keep the scalability, this data request allows you to set 
 * encoding that will be used to convert the internal byte data buffer holding the string to the actual Java string. You can set this encoding using various constructors
 * or by the {@link #setCharset(java.nio.charset.Charset) } method. The charset for encoding/decoding the string will be initialized to the system default, which, mostly, will
 * be UTF-8.
 * 
 * 
 * @author Mouseviator
 */
public class StringRequest extends DataRequest implements IDataRequest<String> {

    private Charset charset = Charset.defaultCharset();
    
    /**
     * Creates a new string data request associated with given offset and the byte data buffer initialized to given size.
     * 
     * @param offset An offset to associate this data request with.
     * @param size The size of the byte data buffer to hold the string characters.
     */
    public StringRequest(int offset, int size) {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE && size > 0) {            
            dataBuffer = new byte[size];            
            this.offset = offset;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    /**
     * Creates a new string data request associated with given offset and initialized to given string value. The conversion
     * of the string to byte data buffer will happen using the system default charset. The data request type will be set to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value A string to set for this data request value.
     */
    public StringRequest(int offset, String value) {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) { 
            this.dataBuffer = value.getBytes(charset);
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }
    
    /**
     * Creates a new string data request associated with given offset and initialized to given string value. If the byte data buffer, that
     * will be result of converting given string value to byte array, is longer than max_size, it will be trimmed to the (max_size - 1) - the last
     * byte will be set to 0. The conversion of the string to byte data buffer will happen using the system default charset. The data request type will be set to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param max_size Maximum size of the resulting byte data buffer.
     * @param value A string to set for this data request value.
     */
    public StringRequest(int offset, int max_size, String value) {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) { 
            //get charset encoder to encode string
            CharsetEncoder enc = charset.newEncoder();
            ByteBuffer byteBuffer = ByteBuffer.allocate(max_size);
            enc.encode(CharBuffer.wrap(value), byteBuffer, true);
            
            //test if we ae at the end of the buffer, move one byte back
            if (!byteBuffer.hasRemaining()) {
                byteBuffer.position(byteBuffer.position() - 1);
            }
            //Add zero fo C zero terminated string
            byteBuffer.put((byte)0);
            
            //Copy the byte buffer into the byte array holding data
            dataBuffer = new byte[byteBuffer.position()];
            byteBuffer.get(dataBuffer,0, byteBuffer.position());
            
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * Creates a new string data request associated with given offset and initialized to given string value.The conversion
     * of the string to byte data buffer will happen using the provided charset. The data request type will be set to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param value A string to set for this data request value.
     * @param charset A charset to use for string conversion.
     */
    public StringRequest(int offset, String value, Charset charset) {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) { 
            this.dataBuffer = value.getBytes(charset);
            this.charset = charset;
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }    
    
    /**
     * Creates a new string data request associated with given offset and initialized to given string value.If the byte data buffer, that
     * will be result of converting given string value to byte array, is longer than max_size, it will be trimmed to the (max_size - 1) - the last
     * byte will be set to 0. The conversion of the string to byte data buffer will happen using the provided charset. The data request type will be set to WRITE.
     * 
     * @param offset An offset to associate this data request with.
     * @param max_size Maximum size of the resulting byte data buffer.
     * @param value A string to set for this data request value.
     * @param charset A charset to use for string conversion.
     */    
    public StringRequest(int offset, int max_size, String value, Charset charset) {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {                        
            
            //get charset encoder to encode string
            CharsetEncoder enc = charset.newEncoder();
            ByteBuffer byteBuffer = ByteBuffer.allocate(max_size);
            enc.encode(CharBuffer.wrap(value), byteBuffer, true);
            
            //test if we ae at the end of the buffer, move one byte back
            if (!byteBuffer.hasRemaining()) {
                byteBuffer.position(byteBuffer.position() - 1);
            }
            //Add zero fo C zero terminated string
            byteBuffer.put((byte)0);
            
            //Copy the byte buffer into the byte array holding data
            dataBuffer = new byte[byteBuffer.position()];
            byteBuffer.get(dataBuffer,0, byteBuffer.position());
            
            this.charset = charset;            
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * This function will allocate the internat byte data buffer to specified size, creating new buffer, thus,
     * throwing away any data that this data request held before.
     * 
     * @param size The required size of the byte data buffer.
     */
    public void allocate(int size) {
        if (size > 0) {
            dataBuffer = new byte[size];
        }
    }

    @Override
    public String getValue() {
        return new String(dataBuffer, charset).trim();
    }

    @Override
    public void setValue(String value) {
        dataBuffer = value.getBytes(charset);
    }
    
    /**
     * Sets the charset that the {@link #getValue() } and {@link #setValue(java.lang.String) } functions will use
     * to convert to/from string/byte data buffer.
     * 
     * @param charset A charset to use for string conversion.
     */
    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }
}
