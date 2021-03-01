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

import com.mouseviator.fsuipc.datarequest.IDataRequest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Murdock
 */
public class ShortRequestTest {
    
    public ShortRequestTest() {
    }

    /**
     * Test of all available constructors of class ShortRequest.
     */
    @Test
    public void testConstructors() {
        ShortRequest request;
        final int offset = 0x66C4;
        final short value = 450;
        
        System.out.println("JUnit test: Test of all available ShortRequest constructors.");
        
        try {
            request = new ShortRequest();
            assertEquals(0x66C0, request.getOffset());
            assertEquals(Short.valueOf((short)0), request.getValue());
            assertEquals(request.getDataBuffer().length, Short.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (Exception ex) {
            fail("The constructor: ShortRequest() should not raise any Exception!");
        }
        
        try {
            request = new ShortRequest(offset);
            assertEquals(offset, request.getOffset());
            assertEquals(Short.valueOf((short)0), request.getValue());
            assertEquals(request.getDataBuffer().length, Short.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (Exception ex) {
            fail("The constructor: ShortRequest(int offset) should not raise any Exception!");
        }
        
        try {
            request = new ShortRequest(offset, Short.valueOf(value));
            assertEquals(offset, request.getOffset());
            assertEquals(Short.valueOf(value), request.getValue());
            assertEquals(request.getDataBuffer().length, Short.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (Exception ex) {
            fail("The constructor: ShortRequest(int offset, Short value) should not raise any Exception!");
        }
        
        try {
            request = new ShortRequest(offset, value);
            assertEquals(offset, request.getOffset());
            assertEquals(value, request.getValue().intValue());
            assertEquals(request.getDataBuffer().length, Short.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (Exception ex) {
            fail("The constructor: ShortRequest(int offset, short value) should not raise any Exception!");
        }
    }
        
    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: ShortRequest.setValue and ShortRequest.getValue");
        
        ShortRequest request = new ShortRequest();
        
        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        short value = 450;
        request.setValue(value);
        int requestValue = request.getValue();
        
        //if we store 255 to the byte, it is actaully -1
        assertEquals(value, requestValue);                                
    }        
}
