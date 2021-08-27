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
import java.nio.charset.Charset;
import java.security.InvalidParameterException;

/**
 * This class implements string data request for use with {@link com.mouseviator.fsuipc.FSUIPC} class. Well, FSUIPC will
 * read/write string values as series of bytes, usually terminated as classic C string, by 0 byte. But with Strings we
 * always have this problem with encoding... so to keep the scalability, this data request allows you to set encoding
 * that will be used to convert the internal byte data buffer holding the string to the actual Java string. You can set
 * this encoding using various constructors or by the {@link #setCharset(java.nio.charset.Charset) } method. The charset
 * for encoding/decoding the string will be initialized to the system default, which, mostly, will be UTF-8.
 *
 *
 * @author Mouseviator
 */
public class StringRequest extends DataRequest implements IDataRequest<String> {

    private Charset charset = Charset.defaultCharset();
    private ValueRetrieveMethod valueRetrieveMethod = ValueRetrieveMethod.WHOLE_BUFFER;

    /**
     * Creates a new string data request associated with given offset and the byte data buffer initialized to given
     * size. Note that this is only useful for READ type request. If you use the {@link #setValue(java.lang.String)
     * } method later on this object, the byte array created by this constructor will be discarded.
     *
     * @param offset An offset to associate this data request with.
     * @param size The size of the byte data buffer to hold the string characters.
     */
    public StringRequest(int offset, int size) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE && size > 0) {
            dataBuffer = new byte[size];
            this.offset = offset;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * Creates a new string data request associated with given offset and initialized to given string value. The
     * conversion of the string to byte data buffer will happen using the system default charset. The data request type
     * will be set to WRITE.
     *
     * @param offset An offset to associate this data request with.
     * @param value A string to set for this data request value.
     */
    public StringRequest(int offset, String value) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            //convert to internal byte array
            convertStringToByteArray(value, 0, charset);
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * Creates a new string data request associated with given offset and initialized to given string value. If the byte
     * data buffer, that will be result of converting given string value to byte array, is longer than max_size, it will
     * be trimmed to the (max_size - 1) - the last byte will be set to 0. The conversion of the string to byte data
     * buffer will happen using the system default charset. The data request type will be set to WRITE.
     *
     * @param offset An offset to associate this data request with.
     * @param max_size Maximum size of the resulting byte data buffer.
     * @param value A string to set for this data request value.
     */
    public StringRequest(int offset, int max_size, String value) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            //convert to internal byte array
            convertStringToByteArray(value, max_size, charset);

            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * Creates a new string data request associated with given offset and initialized to given string value.The
     * conversion of the string to byte data buffer will happen using the provided charset. The data request type will
     * be set to WRITE.
     *
     * @param offset An offset to associate this data request with.
     * @param value A string to set for this data request value.
     * @param charset A charset to use for string conversion.
     */
    public StringRequest(int offset, String value, Charset charset) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            //convert to internal byte array
            convertStringToByteArray(value, 0, charset);

            this.charset = charset;
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * Creates a new string data request associated with given offset and initialized to given string value.If the byte
     * data buffer, that will be result of converting given string value to byte array, is longer than max_size, it will
     * be trimmed to the (max_size - 1) - the last byte will be set to 0. The conversion of the string to byte data
     * buffer will happen using the provided charset. The data request type will be set to WRITE.
     *
     * @param offset An offset to associate this data request with.
     * @param max_size Maximum size of the resulting byte data buffer.
     * @param value A string to set for this data request value.
     * @param charset A charset to use for string conversion.
     */
    public StringRequest(int offset, int max_size, String value, Charset charset) throws InvalidParameterException {
        if (offset >= MIN_OFFSET_VALUE && offset <= MAX_OFFSET_VALUE) {
            //convert to internal byte array
            convertStringToByteArray(value, max_size, charset);

            this.charset = charset;
            this.offset = offset;
            this.type = RequestType.WRITE;
        } else {
            throw new InvalidParameterException("Offset value out of supported range!");
        }
    }

    /**
     * This function will allocate the internat byte data buffer to specified size, creating new buffer, thus, throwing
     * away any data that this data request held before.
     *
     * @param size The required size of the byte data buffer.
     */
    public void allocate(int size) {
        if (size > 0) {
            dataBuffer = new byte[size];
        }
    }

    @Override
    /**
     * This function will return underlying byte data buffer as String. How the value will be retrieved depends on what value is set 
     * {@link #getValueRetrieveMethod()}. 
     *
     * @return String representation of the underlying byte data buffer.
     */
    public String getValue() {
        return getValue(valueRetrieveMethod);
    }

    /**
     * This function will return underlying byte data buffer as String. You have to specify how the value will be retrieved
     * by <code>valueRetrieveMethod</code> parameter.
     *
     * @param valueRetrieveMethod The method to retrieve the value.
     * @return String representation of the underlying byte data buffer.
     */
    public String getValue(ValueRetrieveMethod valueRetrieveMethod) {
        if (valueRetrieveMethod == ValueRetrieveMethod.WHOLE_BUFFER) {
            return new String(dataBuffer, charset).trim();
        } else {
            return getZeroTerminatedString(charset);
        }
    }

    @Override
    public void setValue(String value) {
        convertStringToByteArray(value, 0, charset);
    }

    /**
     * This function will set value of this StringRequest to given string, but the resulting length of the byte array of
     * the converted string will be max_size.
     *
     * @param value A string value to set.
     * @param max_size The maximum length of the byte buffer of the converted string.
     */
    public void setValue(String value, int max_size) {
        convertStringToByteArray(value, max_size, charset);
    }

    /**
     * Sets the charset that the {@link #getValue() } and {@link #setValue(java.lang.String)
     * } functions will use to convert to/from string/byte data buffer.
     *
     * @param charset A charset to use for string conversion.
     */
    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    /**
     * Return the charset that the {@link #getValue() } and {@link #setValue(java.lang.String)
     * } functions will use to convert to/from string/byte data buffer.
     *
     * @return The charset.
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Return the value retrieve method used by this String data request.
     *
     * @return Value retrieve method.
     */
    public ValueRetrieveMethod getValueRetrieveMethod() {
        return valueRetrieveMethod;
    }

    /**
     * Sets the value retrieve method for this String data request.
     *
     * @param valueRetrieveMethod Value retrieve method.
     */
    public void setValueRetrieveMethod(ValueRetrieveMethod valueRetrieveMethod) {
        this.valueRetrieveMethod = valueRetrieveMethod;
    }

    /**
     * This enumeration defines values retrieve method that will be used by {@link #getValue() } method.
     */
    public enum ValueRetrieveMethod {
        /**
         * Retrieve the content of whole underlying byte buffer when converting it to String.
         */
        WHOLE_BUFFER,
        /**
         * Retrieve the content of underlying byte buffer up to first zero byte when converting it to String.
         */
        TO_FIRST_ZERO_BYTE
    }
}
