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
public class DoubleRequestTest {

    private static final double DELTA = 1e-32;

    public DoubleRequestTest() {
    }

    /**
     * Test of all available constructors of class DoubleRequest.
     */
    @Test
    public void testConstructors() {
        DoubleRequest request;
        final int offset = 0x66C4;
        final double value = 3.14159;

        System.out.println("JUnit test: Test of all available DoubleRequest constructors.");

        try {
            request = new DoubleRequest();
            assertEquals(0x66C0, request.getOffset());
            assertEquals(Double.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Double.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: DoubleRequest() should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new DoubleRequest(offset);
            assertEquals(offset, request.getOffset());
            assertEquals(Double.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Double.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: DoubleRequest(int offset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new DoubleRequest(offset, Double.valueOf(value));
            assertEquals(offset, request.getOffset());
            assertEquals(Double.valueOf(value), request.getValue());
            assertEquals(request.getDataBuffer().length, Double.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: DoubleRequest(int offset, Double value) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new DoubleRequest(offset, value);
            assertEquals(offset, request.getOffset());
            assertEquals(value, request.getValue().doubleValue(), DELTA);
            assertEquals(request.getDataBuffer().length, Double.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: DoubleRequest(int offset, double value) should not raise exception other than InvalidParameterException!");
        }
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: DoubleRequest.setValue and DoubleRequest.getValue");

        DoubleRequest request = new DoubleRequest();

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        double value = 3.14159d;
        request.setValue(value);
        double requestValue = request.getValue();

        //if we store 255 to the byte, it is actaully -1
        assertEquals(value, requestValue, DELTA);
    }
}
