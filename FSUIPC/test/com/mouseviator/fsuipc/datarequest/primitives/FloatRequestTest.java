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
public class FloatRequestTest {

    private static final float DELTA = 1e-15f;

    public FloatRequestTest() {
    }

    /**
     * Test of all available constructors of class FloatRequest.
     */
    @Test
    public void testConstructors() {
        FloatRequest request;
        final int offset = 0x66C4;
        final float value = 3.14159f;

        System.out.println("JUnit test: Test of all available FloatRequest constructors.");

        try {
            request = new FloatRequest();
            assertEquals(0x66C0, request.getOffset());
            assertEquals(Float.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Float.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: FloatRequest() should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new FloatRequest(offset);
            assertEquals(offset, request.getOffset());
            assertEquals(Float.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Float.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: FloatRequest(int offset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new FloatRequest(offset, Float.valueOf(value));
            assertEquals(offset, request.getOffset());
            assertEquals(Float.valueOf(value), request.getValue());
            assertEquals(request.getDataBuffer().length, Float.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: FloatRequest(int offset, Float value) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new FloatRequest(offset, value);
            assertEquals(offset, request.getOffset());
            assertEquals(value, request.getValue().doubleValue(), DELTA);
            assertEquals(request.getDataBuffer().length, Float.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: FloatRequest(int offset, float value) should not raise exception other than InvalidParameterException!");
        }
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: FloatRequest.setValue and FloatRequest.getValue");

        FloatRequest request = new FloatRequest();

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        float value = 3.14159f;
        request.setValue(value);
        float requestValue = request.getValue();

        //if we store 255 to the byte, it is actaully -1
        assertEquals(value, requestValue, DELTA);
    }
}
