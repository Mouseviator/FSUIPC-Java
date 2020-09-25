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
package com.mouseviator.fsuipc.datarequest.advanced;

import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IWriteOnlyRequest;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class implements special data request to send any FS control to flight simulator. This works via the 0x3110 FSUIPC offset. You do not need to specify this offset,
 * it is set by default. Use the {@link #setControl(int) } method to specify which control to send, and the {@link #setValue(java.lang.Integer) } method to set any control
 * parameter.
 * 
 * @author Murdock
 */
public class FSControlRequest extends DataRequest implements IWriteOnlyRequest<Integer> {        
    
    {
        //Offset 3110 operates facility to send any FS conrol to Flight Simulator
        this.offset = 0x3110;
        //will need 8 bytes, that is the same as LONG buffer length
        this.dataBuffer = new byte[BUFFER_LENGTH_LONG]; 
        this.type = RequestType.WRITE;
    }
    
    /**
     * Creates a new FS control data request associated with given FS control.
     * 
     * @param control A FS control to be sent by this data request.
     */
    public FSControlRequest(int control) {       
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(control);
        buf.rewind();
        buf.get(this.dataBuffer, 0, BUFFER_LENGTH_INT);  
    }
    
    /**
     * Creates a new FS control data request associated with given FS control.
     * 
     * @param control A FS control to be sent by this data request.
     * @param value A value to be sent along with the control.
     */
    public FSControlRequest(int control, int value) {
        final ByteBuffer ctrl_buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        ctrl_buf.order(ByteOrder.LITTLE_ENDIAN);
        ctrl_buf.putInt(control);
        ctrl_buf.rewind();
        ctrl_buf.get(this.dataBuffer, 0, BUFFER_LENGTH_INT);  
        
        final ByteBuffer value_buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        value_buf.order(ByteOrder.LITTLE_ENDIAN);
        value_buf.putInt(value);
        value_buf.rewind();
        //we want to write the parameter the higher int
        value_buf.get(dataBuffer, BUFFER_LENGTH_INT, value_buf.capacity());
    }
    
    /**
     * Sets the FS control to be sent by this data request.
     * 
     * @param control A FS control to be sent by this data request.
     */
    public void setControl(int control) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(control);
        buf.rewind();
        buf.get(dataBuffer, 0, BUFFER_LENGTH_INT);  
    }

    @Override
    public void setValue(Integer value) {
        final ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH_INT);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(value);
        buf.rewind();
        //we want to write the parameter the higher int
        buf.get(dataBuffer, BUFFER_LENGTH_INT, buf.capacity());
    }    
}
