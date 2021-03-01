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
public class ByteRequestTest {

    public ByteRequestTest() {
    }

    /**
     * Test of all available constructors of class ByteRequest.
     */
    @Test
    public void testConstructors() {
        ByteRequest request;
        final int offset = 0x66C4;
        final byte value = 22;

        System.out.println("JUnit test: Test of all available ByteRequest constructors.");

        try {
            request = new ByteRequest();
            assertEquals(0x66C0, request.getOffset());
            assertEquals(Byte.valueOf((byte) 0), request.getValue());
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteRequest() should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new ByteRequest(offset);
            assertEquals(offset, request.getOffset());
            assertEquals(Byte.valueOf((byte) 0), request.getValue());
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteRequest(int offset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new ByteRequest(offset, Byte.valueOf(value));
            assertEquals(offset, request.getOffset());
            assertEquals(Byte.valueOf(value), request.getValue());
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteRequest(int offset, Byte value) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new ByteRequest(offset, value);
            assertEquals(offset, request.getOffset());
            assertEquals(value, request.getValue().byteValue());
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteRequest(int offset, byte value) should not raise exception other than InvalidParameterException!");
        }
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: ByteRequest.setValue and ByteRequest.getValue");

        ByteRequest request = new ByteRequest();

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        byte value = (byte) 255;
        request.setValue(value);
        byte requestValue = request.getValue();

        //if we store 255 to the byte, it is actaully -1
        assertEquals((byte) -1, requestValue);

        value = 97;
        request.setValue(value);
        requestValue = request.getValue();

        assertEquals(value, requestValue);
    }
}
