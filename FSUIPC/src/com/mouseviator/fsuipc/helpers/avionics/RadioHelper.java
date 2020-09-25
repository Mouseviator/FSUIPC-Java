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
package com.mouseviator.fsuipc.helpers.avionics;

import com.mouseviator.fsuipc.datarequest.DataRequest;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_SHORT;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IWriteOnlyRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;

/**
 * This class implements an abstract class with functionality common to COM and NAV radios. Classes implementing helpers for the
 * COM/NAV radios should use it as a base for the common functionality, as to read/write frequencies...
 * 
 * @author Murdock
 */
public abstract class RadioHelper {

    protected int frequencyOffset;
    protected int standbyFrequencyOffset;
    protected byte frequencySwapValue;

    /**
     * Returns data request to get/set active radio frequency. Note that this request will not support 8.33 Khz range - so the resulting
     * frequency will have 3 digits in units and 2 digits in decimals, like 123.45 frequency. This is important when writing the frequency! The data request will
     * not check for the validity of the frequency value, it expects it will be like this. And if not, unexpected behavior may occur. 
     * You can get/set the frequency with this data request.
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set active radio frequency.
     */
    public FloatRequest getFrequency() {
        return new FloatRequest(frequencyOffset) {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }

            @Override
            public void setValue(Float value) {
                putShort(encodeFrequency(value));
            }

            @Override
            public Float getValue() {
                return decodeFrequency(getShort());
            }
        };
    }
    
    /**
     * Returns data request to get/set standby radio frequency. Note that this request will not support 8.33 Khz range - so the resulting
     * frequency will have 3 digits in units and 2 digits in decimals, like 123.45 frequency. This is important when writing the frequency! The data request will
     * not check for the validity of the frequency value, it expects it will be like this. And if not, unexpected behavior may occur. 
     * You can get/set the frequency with this data request. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * @return Data request to get/set standby radio frequency.
     */
    public FloatRequest getStandByFrequency() {
        return new FloatRequest(standbyFrequencyOffset) {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }

            @Override
            public void setValue(Float value) {
                putShort(encodeFrequency(value));
            }

            @Override
            public Float getValue() {
                return decodeFrequency(getShort());
            }
        };
    }
    
    /**
     * Returns request to swap active/standby radio frequency. The request will be WRITE ONLY!
     * 
     * @return Data request to toggle between active and standby frequency.
     */
    public IDataRequest<Byte> swapFrequencies() {
        class SwapFreqiencyRequest extends DataRequest implements IWriteOnlyRequest<Byte> {
            {
                this.offset = 0x3123;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public void setValue(Byte value) {
                this.dataBuffer[0] = frequencySwapValue;
            }            
        }
        return new SwapFreqiencyRequest();
    }

    /**
     * This function will decode BCD stored frequency and return it as float value. The leading 1 should not be stored
     * in the number, it will be added automatically. For example, a frequency of 123.45, should be passed as BCD value 2345. Than,
     * the value returned will be 123.45 float.
     * 
     * @param value A frequency in BCD format.
     * @return Decoded frequency value as float number.
     */
    private float decodeFrequency(short value) {
        //frequencies are stored in 4 digit BCD format                                
        float frequency = 100.0f;

        //The digits will be stored in BCD, means each digit in its own byte binary formatted
        frequency += (((value & 0xff00) >> 12) * 10);
        frequency += (((value & 0x0f00) >> 8));
        frequency += (((value & 0x00f0) >> 4) / 10.0f);
        frequency += ((value & 0x000f) / 100.0f);

        return frequency;
    }
        
    /**
     * This function will encode given frequency, given as float value, to BCD format expected by FSUIPC. Note that flight simulator
     * radio frequencies are always in the range of 100-200, but this function does not check for correctness. It also expects the frequency will
     * be with two digit after decimal point (so, no 8.33 range support).
     * 
     * @param frequency A frequency to be encoded to BCD.
     * @return A BCD encoded frequency.
     */
    private short encodeFrequency(float frequency) {
        //convert from like 123.45 to 2345, we do not need the first 1, it there always with the frequency
        short freqValue = (short)Math.ceil((frequency - 100) * 100);
        short storeValue = 0;
        
        //We need to get separate values and store them in the buffer as LITTLE ENDIAN
        for (byte i = 0; i < 4; i++) {
            short reminder = (short)(freqValue % 10);
            storeValue += (reminder << (i * 4));
            freqValue = (short)(freqValue / 10);
        }
        
        return storeValue;
    }
}
