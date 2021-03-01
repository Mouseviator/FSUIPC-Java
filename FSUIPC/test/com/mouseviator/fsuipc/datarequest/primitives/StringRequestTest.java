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
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Murdock
 */
public class StringRequestTest {

    private static final int TEST_OFFSET = 0x66C4;
    private static final String TEST_STRING = "my test string";
    private static final String TEST_STRING2 = "můj mezinárdní vzorec!";

    public StringRequestTest() {
    }

    /**
     * Test of all available constructors of class StringRequest.
     */
    @Test
    public void testConstructors() {
        StringRequest request;        

        System.out.println("JUnit test: Test of all available StringRequest constructors.");

        try {
            request = new StringRequest(TEST_OFFSET, TEST_STRING);
            assertEquals(TEST_OFFSET, request.getOffset());
            testString(request, TEST_STRING);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: StringRequest(int offset, String value) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new StringRequest(TEST_OFFSET, 128);
            assertEquals(TEST_OFFSET, request.getOffset());
            assertEquals(request.getDataBuffer().length, 128);
            assertEquals(request.getType(), IDataRequest.RequestType.READ);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: StringRequest(int offset, int size) should not raise exception other than InvalidParameterException!");
        }

        try {
            final Charset charset = Charset.forName("UTF-8");
            request = new StringRequest(TEST_OFFSET, TEST_STRING, charset);
            assertEquals(TEST_OFFSET, request.getOffset());
            assertEquals(charset, request.getCharset());
            testString(request, TEST_STRING, 0, charset);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: StringRequest(int offset, String value, Charset charset) should not raise exception other than InvalidParameterException!");
        }

        try {
            request = new StringRequest(TEST_OFFSET, 10, TEST_STRING);
            assertEquals(TEST_OFFSET, request.getOffset());
            testString(request, TEST_STRING, 10, null);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: StringRequest(int offset, int max_size, String value) should not raise exception other than InvalidParameterException!");
        }

        try {
            final Charset charset = Charset.forName("UTF-8");
            request = new StringRequest(TEST_OFFSET, 10, TEST_STRING, charset);
            assertEquals(TEST_OFFSET, request.getOffset());
            testString(request, TEST_STRING, 10, charset);
            assertEquals(request.getType(), IDataRequest.RequestType.WRITE);
        } catch (InvalidParameterException ex) {
            fail("Impossible in current implementation! Only the offset parameter can throw that and it is allowed from Integer.MIN_VALUE to Integer.MAX_VALUE");
        } catch (Exception ex) {
            fail("The constructor: StringRequest(int offset, int max_size, String value) should not raise exception other than InvalidParameterException!");
        }
    }

    /**
     * Test of getValue and setValue method of class ByteRequest.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("JUnit test: StringRequest.setValue and StringRequest.getValue");

        StringRequest request = new StringRequest(TEST_OFFSET, TEST_STRING);

        //I know this test seems silly, but it should test that the conversion from primitive data type to byte array and back
        //works ok inside the data requests
        //this one using default charset
        testString(request, TEST_STRING, 0, null);

        //test limited length setValue
        request.setValue(TEST_STRING2, 10);
        testString(request, TEST_STRING2, 10, null);

        //test when max_size will not met
        request.setValue(TEST_STRING2, 40);
        testString(request, TEST_STRING2, 40, null);

        //test with 2byte encoding
        Charset charset = Charset.forName("UTF-16");
        //test limited length setValue
        request.setCharset(charset);
        request.setValue(TEST_STRING2, 10);

        testString(request, TEST_STRING2, 10, charset);
    }

    /**
     * Test of allocate method, of class StringRequest.
     */
    @Test
    public void testAllocate() {
        System.out.println("JUnit test: StringRequest.setAllocate");

        StringRequest request = new StringRequest(TEST_OFFSET, TEST_STRING);
        request.allocate(64);
        assertEquals(64, request.getDataBuffer().length);
    }

    /**
     * Test of setCharset method, of class StringRequest.
     */
    @Test
    public void testSetCharset() {
        System.out.println("JUnit test: StringRequest.setCharset");

        Charset testCharset = Charset.forName("UTF-8");

        StringRequest request = new StringRequest(TEST_OFFSET, TEST_STRING);
        request.setCharset(testCharset);
        assertEquals(testCharset, request.getCharset());
    }

    /**
     * A helper function to test that internal conversion of String to byte array and back works as it should. This also
     * test that the converted String is ended by the 0 byte.
     *
     * @param request A string request to test.
     * @param originalValue Original value.
     */
    private void testString(StringRequest request, String originalValue) {
        testString(request, originalValue, 0, Charset.defaultCharset());
    }

    /**
     * A helper function to test that internal conversion of String to byte array and back works as it should. This also
     * test that the converted String is ended by the 0 byte.
     *
     * @param request A string request to test.
     * @param originalValue Original value.
     * @param max_size Maximum size of the converted buffer.
     * @param charset Charset to use for encoding the string.
     */
    private void testString(StringRequest request, String originalValue, int max_size, Charset charset) {
        //make sure request will use specific charset for setValue and getValue
        if (charset != null) {
            request.setCharset(charset);
        }

        String requestValue;
        byte[] requestDataBuffer;

        //convert original str to byet array
        byte[] originalStrBytes;
        String compareValue;
        if (charset != null && charset.canEncode()) {
            originalStrBytes = originalValue.getBytes(charset);
            compareValue = new String(originalStrBytes, charset).trim();
        } else {
            originalStrBytes = originalValue.getBytes();
            compareValue = new String(originalStrBytes).trim();
        }

        //set value
        if (max_size > 0) {
            request.setValue(originalValue, max_size);
            requestValue = request.getValue();
            requestDataBuffer = request.getDataBuffer();

            //Convert the original String to byte array and back, so that we have the same String as should be in the data request
            int newLength = Math.min(max_size, originalStrBytes.length);
            //check if original string in our conversion gets shortened
            if (newLength < originalStrBytes.length) {
                //we have to copy the values to new string                
                //this is important, because in the request the last byte will be 0, which might chnge character if multi-byte charset is used
                originalStrBytes[newLength - 1] = (byte)0;
                if (charset != null && charset.canEncode()) {                    
                    compareValue = new String(originalStrBytes, 0, newLength, charset).trim();
                } else {                    
                    compareValue = new String(originalStrBytes, 0, newLength).trim();
                }

                //compare trimmed string
                assertEquals(compareValue, requestValue);
                //assertTrue(compareValue.compareTo(requestValue) == 0);

                //compare buffer lengths
                assertEquals(newLength, request.getDataBuffer().length);
            } else {
                //new length is lower or the same as original String bytes length, we can compare right away
                //compare trimmed string                                
                assertEquals(compareValue, requestValue);

                //match the size of the arrays (+1 because the encoded string has the additional 0 byte)
                assertEquals(requestDataBuffer.length, originalStrBytes.length + 1);
            }
        } else {
            request.setValue(originalValue);
            requestValue = request.getValue();
            requestDataBuffer = request.getDataBuffer();

            //new length is lower or the same as original String bytes length, we can compare right away
            //compare trimmed string
            //convert to original String back from bytes            
            assertEquals(compareValue, requestValue);

            //match the size of the arrays (+1 because the encoded string has the additional 0 byte)
            assertEquals(requestDataBuffer.length, originalStrBytes.length + 1);
        }

        //now test that the byte array ends with zero byte       
        assertTrue("The string converted to byte array must always end with 0 byte!", requestDataBuffer[requestDataBuffer.length - 1] == (byte) 0);
    }
}
