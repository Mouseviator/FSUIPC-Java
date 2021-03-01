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
public class LongRequestTest {

    public LongRequestTest() {
    }

    /**
     * Test of all available constructors of class LongRequest.
     */
    @Test
    public void testConstructors() {
        LongRequest request;
        final int offset = 0x66C4;
        final long value = Long.MIN_VALUE + 9843;

        System.out.println("JUnit test: Test of all available LongRequest constructors.");

        try {
            request = new LongRequest();
            assertEquals(0x66C0, request.getOffset());
            assertEquals(Long.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Long.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: LongRequest() should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new LongRequest(offset);
            assertEquals(offset, request.getOffset());
            assertEquals(Long.valueOf(0), request.getValue());
            assertEquals(request.getDataBuffer().length, Long.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: LongRequest(int offset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new LongRequest(offset, Long.valueOf(value));
            assertEquals(offset, request.getOffset());
            assertEquals(Long.valueOf(value), request.getValue());
            assertEquals(request.getDataBuffer().length, Long.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: LongRequest(int offset, Long value) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new LongRequest(offset, value);
            assertEquals(offset, request.getOffset());
            assertEquals(value, request.getValue().longValue());
            assertEquals(request.getDataBuffer().length, Long.BYTES);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: LongRequest(int offset, long value) should not raise exception other than InvalidParameterException!");
        }
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: LongRequest.setValue and LongRequest.getValue");

        LongRequest request = new LongRequest();

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        long value = Long.MIN_VALUE + 9843;
        request.setValue(value);
        long requestValue = request.getValue();

        //if we store 255 to the byte, it is actaully -1
        assertEquals(value, requestValue);
    }
}
