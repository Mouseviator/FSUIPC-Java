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
import java.security.InvalidParameterException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Murdock
 */
public class IntRequestTest {

    public IntRequestTest() {
    }

    /**
     * Test of all available constructors of class IntRequest.
     */
    @Test
    public void testConstructors() {
        IntRequest request;
        final int offset = 0x66C4;
        final int value = 450;

        System.out.println("JUnit test: Test of all available IntRequest constructors.");

        try {
            request = new IntRequest();
            assertEquals(0x66C0, request.getOffset());
            assertEquals(Integer.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Integer.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: IntRequest() should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new IntRequest(offset);
            assertEquals(offset, request.getOffset());
            assertEquals(Integer.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Integer.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: IntRequest(int offset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new IntRequest(offset, Integer.valueOf(value));
            assertEquals(offset, request.getOffset());
            assertEquals(Integer.valueOf(value), request.getValue());
            assertEquals(request.getDataBuffer().length, Integer.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: IntRequest(int offset, Integer value) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new IntRequest(offset, value);
            assertEquals(offset, request.getOffset());
            assertEquals(value, request.getValue().intValue());
            assertEquals(request.getDataBuffer().length, Integer.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: IntRequest(int offset, int value) should not raise exception other than InvalidParameterException!");
        }
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: IntRequest.setValue and IntRequest.getValue");

        IntRequest request = new IntRequest();

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        int value = 450;
        request.setValue(value);
        int requestValue = request.getValue();

        //if we store 255 to the byte, it is actaully -1
        assertEquals(value, requestValue);
    }
}
