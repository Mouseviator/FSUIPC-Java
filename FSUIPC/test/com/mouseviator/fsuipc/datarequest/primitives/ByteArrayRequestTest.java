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
public class ByteArrayRequestTest {
    private final byte[] TEST_BYTE_ARRAY = new byte[] {5,8,6,126,-112};
    
    public ByteArrayRequestTest() {
    }

    /**
     * Test of all available constructors of class ByteRequest.
     */
    @Test
    public void testConstructors() {
        ByteArrayRequest request;
        final int offset = 0x66C4;        

        System.out.println("JUnit test: Test of all available ByteArrayRequest constructors.");

        try {
            request = new ByteArrayRequest(10);
            assertEquals(0x66C0, request.getOffset());
            assertEquals(10, request.getDataBuffer().length);
            assertArrayEquals(new byte[10], request.getValue());
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteArrayRequest() should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new ByteArrayRequest(offset, TEST_BYTE_ARRAY);
            assertEquals(offset, request.getOffset());
            assertEquals(TEST_BYTE_ARRAY.length, request.getValue().length);
            assertArrayEquals(TEST_BYTE_ARRAY, request.getValue());            
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteArrayRequest(int offset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new ByteArrayRequest(offset, 24);
            assertEquals(offset, request.getOffset());
            assertEquals(24, request.getDataBuffer().length);
            assertArrayEquals(new byte[24], request.getValue());
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: ByteArrayRequest(int offset, Byte value) should not raise exception other than InvalidParameterException!");
        }       
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: ByteArrayRequest.setValue and ByteArrayRequest.getValue");

        ByteArrayRequest request = new ByteArrayRequest(0);

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests        
        request.setValue(TEST_BYTE_ARRAY);
        byte[] requestValue = request.getValue();

        //if we store 255 to the byte, it is actaully -1
        assertArrayEquals(TEST_BYTE_ARRAY, requestValue);
    }    
}
