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
package com.mouseviator.fsuipc.helpers;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import java.nio.charset.Charset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This is a tester class for {@link LVarHelper}. It will test all LVar read ,write and create methods. If you have
 * simulator connected during the test, it will try to connect and in the create and write tests will also create custom
 * LVars and will try to read back values written using write. The read test will also create a custom LVar for the test
 * reads.
 *
 * @author Murdock
 */
public class LVarHelperTest {

    private static FSUIPC fsuipc;
    private static final String CUSTOM_LVAR_FOR_READ = "MOUSEVIATOR_CUSTOM_FOR_READ";
    private static final String CUSTOM_LVAR_FOR_WRITE = "MOUSEVIATOR_CUSTOM_FOR_WRITE";
    private static final String CUSTOM_LVAR_DOUBLE = "MOUSEVIATOR_CUSTOM_LVAR_DOUBLE";
    private static final String CUSTOM_LVAR_FLOAT = "MOUSEVIATOR_CUSTOM_LVAR_FLOAT";
    private static final String CUSTOM_LVAR_SSHORT = "MOUSEVIATOR_CUSTOM_LVAR_SSHORT";
    private static final String CUSTOM_LVAR_USHORT = "MOUSEVIATOR_CUSTOM_LVAR_USHORT";
    private static final String CUSTOM_LVAR_SINT = "MOUSEVIATOR_CUSTOM_LVAR_SINT";
    private static final String CUSTOM_LVAR_UINT = "MOUSEVIATOR_CUSTOM_LVAR_UINT";
    private static final String CUSTOM_LVAR_SBYTE = "MOUSEVIATOR_CUSTOM_LVAR_SBYTE";
    private static final String CUSTOM_LVAR_UBYTE = "MOUSEVIATOR_CUSTOM_LVAR_UBYTE";
    private final int LVAR_REQUEST_OFFSET = 0x0D70;
    private final int LVAR_REQUEST_PARAM_OFFSET = 0x0D6C;
    private final int LVAR_READ_VALUE_OFFSET = 0x66C0;
    private final int LVAR_WRITE_VALUE_OFFSET = 0x66C8;
    private final int LVAR_CREATE_VALUE_OFFSET = 0x66D0;
    private static final float FLOAT_DELTA = 1e-15f;
    private static final double DOUBLE_DELTA = 1e-24f;

    public LVarHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("JUnit test: LVarHelper class setUp - Will try to load FSUIPC and connect to the simulator...");

        //The libraries needs to be in the project folder in order the Java to find them
        byte result = FSUIPC.load();
        if (result != FSUIPC.LIB_LOAD_RESULT_OK && result != FSUIPC.LIB_LOAD_RESULT_ALREADY_LOADED) {
            System.out.println("Failed to load native library. Quiting...");
            return;
        }

        fsuipc = FSUIPC.getInstance();
        int ret = fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
        System.out.println("FSUIPC connect return value = " + ret);

        if (ret == 0) {
            System.out.println("Flight sim not found");
            fsuipc = null;
        } else {
            System.out.println("Flight Sim found!");
        }

        System.out.println("JUnit test: LVarHelper class setUp - Done.");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("JUnit test: LVarHelper class tearDown - Will disconnect FSUIPC if connected...");

        if (fsuipc != null) {
            fsuipc.disconnect();
            fsuipc = null;
        }

        System.out.println("JUnit test: LVarHelper class tearDown - Done.");
    }   

    /**
     * Test of readLVar method, of class LVarHelper.
     */
    @Test
    public void testReadLVar_5args() {
        System.out.println("JUnit test: ReadLVar 5 args");
        final String VAR_NUM = "5";

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE, fsuipc, false);
            assertNull(result);

            //float
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT, fsuipc, false);
            assertNull(result);

            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE, fsuipc, false);
            assertNull(result);

            //actually the same as signed byte internally
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE, fsuipc, false);
            assertNull(result);

            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT, fsuipc, false);
            assertNull(result);

            //actually the same as signed byte internally
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc, false);
            assertNull(result);

            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc, false);
            assertNull(result);

            //actually the same as signed byte internally
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER, fsuipc, false);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will also check the correctness of read LVar value.");

            //first create custom LVar so we have something to read
            final double controlNum = Math.ceil(Math.random() * 100);

            result = lvarHelper.createLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);

            //test all variant with fsuipc null
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum, LVarHelper.LVarValueFormat.FLOAT);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testLVarValue(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        }
    }

    /**
     * Test of readLVar method, of class LVarHelper.
     */
    @Test
    public void testReadLVar_3args() {
        System.out.println("JUnit test: ReadLVar 3 args");
        final String VAR_NUM = "3";

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);

            //actually the same as signed byte internally
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);

            //actually the same as signed byte internally
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            //actually the same as signed byte internally
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        } else {
            System.out.println("Connected to FSUIPC. Will also check the correctness of read LVar value.");

            //first create custom LVar so we have something to read
            final double controlNum = Math.ceil(Math.random() * 100);

            result = lvarHelper.createLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);

            //test read the value as double
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);
            assertEquals(controlNum, (double) result.getResultRequest().getValue(), DOUBLE_DELTA);

            //test read the value as float
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            registerAndProcessRequests(result);
            assertEquals((float) controlNum, (float) result.getResultRequest().getValue(), FLOAT_DELTA);

            //test read the value as signed byte
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            registerAndProcessRequests(result);
            assertEquals((byte) controlNum, result.getResultRequest().getValue());

            //test read the value as unsigned byte
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            registerAndProcessRequests(result);
            assertEquals((byte) controlNum, result.getResultRequest().getValue());

            //test read the value as signed short
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            registerAndProcessRequests(result);
            assertEquals((short) controlNum, result.getResultRequest().getValue());

            //test read the value as unsigned short
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            registerAndProcessRequests(result);
            assertEquals((short) controlNum, result.getResultRequest().getValue());

            //test read the value as signed integer
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            registerAndProcessRequests(result);
            assertEquals((int) controlNum, result.getResultRequest().getValue());

            //test read the value as unsigned integer
            result = lvarHelper.readLVar(CUSTOM_LVAR_FOR_READ + VAR_NUM, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            testResult(result, LVAR_READ_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            registerAndProcessRequests(result);
            assertEquals((int) controlNum, result.getResultRequest().getValue());
        }
    }

    /**
     * Test of writeLVar method, of class LVarHelper.
     */
    @Test
    public void testWriteLVar_3args() {
        System.out.println("JUnit test: WriteLVar 3 args");
        final String VAR_NUM = "3";

        final double controlNum = Math.ceil(Math.random() * 100);

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (float) controlNum);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) controlNum);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) controlNum);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
        } else {
            System.out.println("Connected to FSUIPC. Will also check the correctness of written LVar value by reading it back.");

            //first create custom LVar so we have something to read
            result = lvarHelper.createLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);

            //test all variant with fsuipc null
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum + 1.0d);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum + 1.0d, LVarHelper.LVarValueFormat.DOUBLE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (float) controlNum + 2.0f);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum + 2.0f, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) (controlNum + (byte) 3));
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.SIGNED_BYTE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) (controlNum + (short) 5));
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.SIGNED_SHORT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum + 7);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
        }
    }

    /**
     * Test of writeLVar method, of class LVarHelper.
     */
    @Test
    public void testWriteLVar_4args() {
        System.out.println("JUnit test: WriteLVar 4 args");
        final String VAR_NUM = "4";

        final double controlNum = Math.ceil(Math.random() * 100);

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (float) controlNum, LVarHelper.LVarValueFormat.FLOAT);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);

            //actually the same as signed byte internally
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);

            //actually the same as signed byte internally
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            //actually the same as signed byte internally
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        } else {
            System.out.println("Connected to FSUIPC. Will also check the correctness of written LVar value by reading it back.");

            //first create custom LVar so we have something to read
            result = lvarHelper.createLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);

            //test all variant with fsuipc null
            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum + 1.0d, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum + 1.0d, LVarHelper.LVarValueFormat.DOUBLE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (float) controlNum + 2.0f, LVarHelper.LVarValueFormat.FLOAT);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum + 2.0f, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.SIGNED_BYTE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) (controlNum + (byte) 4), LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 4), LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.SIGNED_SHORT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) (controlNum + (short) 6), LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 6), LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            result = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum + 8, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 8, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        }
    }

    /**
     * Test of writeLVar method, of class LVarHelper.
     */
    @Test
    public void testWriteLVar_6args() {
        System.out.println("JUnit test: WriteLVar 6 args");
        final String VAR_NUM = "6";

        final double controlNum = Math.ceil(Math.random() * 100);

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            boolean bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE, fsuipc, false);
            assertFalse(bret);

            //float
            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (float) controlNum, LVarHelper.LVarValueFormat.FLOAT, fsuipc, false);
            assertFalse(bret);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.SIGNED_BYTE, fsuipc, false);
            assertFalse(bret);

            //actually the same as signed byte internally
            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_BYTE, fsuipc, false);
            assertFalse(bret);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.SIGNED_SHORT, fsuipc, false);
            assertFalse(bret);

            //actually the same as signed byte internally
            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc, false);
            assertFalse(bret);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc, false);
            assertFalse(bret);

            //actually the same as signed byte internally
            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER, fsuipc, false);
            assertFalse(bret);
        } else {
            System.out.println("Connected to FSUIPC. Will also check the correctness of written LVar value by reading it back.");

            //first create custom LVar so we have something to read
            result = lvarHelper.createLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_WRITE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);

            //test all variant with fsuipc null
            boolean bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, controlNum + 1.0d, LVarHelper.LVarValueFormat.DOUBLE, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum + 1.0d, LVarHelper.LVarValueFormat.DOUBLE);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (float) controlNum + 2.0f, LVarHelper.LVarValueFormat.FLOAT, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum + 2.0f, LVarHelper.LVarValueFormat.FLOAT);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.SIGNED_BYTE, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.SIGNED_BYTE);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (byte) (controlNum + (byte) 4), LVarHelper.LVarValueFormat.UNSIGNED_BYTE, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 4), LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.SIGNED_SHORT, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.SIGNED_SHORT);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (short) (controlNum + (short) 6), LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 6), LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            bret = lvarHelper.writeLVar(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_WRITE_VALUE_OFFSET, (int) controlNum + 8, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER, fsuipc, false);
            assertTrue(bret);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FOR_WRITE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 8, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        }
    }

    /**
     * Test of createLVar method, of class LVarHelper.
     */
    @Test
    public void testCreateLVar_4args() {
        System.out.println("JUnit test: CreateLVar 4 args");
        final String VAR_NUM = "4";

        final double controlNum = Math.ceil(Math.random() * 100);

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = lvarHelper.createLVar(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.createLVar(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (float) controlNum, LVarHelper.LVarValueFormat.FLOAT);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);

            //actually the same as signed byte internally
            result = lvarHelper.createLVar(CUSTOM_LVAR_UBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);

            //actually the same as signed byte internally
            result = lvarHelper.createLVar(CUSTOM_LVAR_USHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            //actually the same as signed byte internally
            result = lvarHelper.createLVar(CUSTOM_LVAR_UINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        } else {
            System.out.println("Connected to FSUIPC. Will also read back the value of created LVar and test for match.");
            //test all variant with fsuipc null
            result = lvarHelper.createLVar(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.createLVar(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (float) controlNum + 1.0f, LVarHelper.LVarValueFormat.FLOAT);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum + 1.0f, LVarHelper.LVarValueFormat.FLOAT);

            //signed byte
            result = lvarHelper.createLVar(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) (controlNum + (byte) 2), LVarHelper.LVarValueFormat.SIGNED_BYTE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 2), LVarHelper.LVarValueFormat.SIGNED_BYTE);

            //actually the same as signed byte internally
            result = lvarHelper.createLVar(CUSTOM_LVAR_UBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_BYTE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_UBYTE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) (controlNum + (short) 4), LVarHelper.LVarValueFormat.SIGNED_SHORT);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 4), LVarHelper.LVarValueFormat.SIGNED_SHORT);

            //actually the same as signed byte internally
            result = lvarHelper.createLVar(CUSTOM_LVAR_USHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_SHORT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_USHORT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum + 6, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 6, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            //actually the same as signed byte internally
            result = lvarHelper.createLVar(CUSTOM_LVAR_UINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_UINT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        }
    }

    /**
     * Test of createLVar method, of class LVarHelper.
     */
    @Test
    public void testCreateLVar_3args() {
        System.out.println("JUnit test: CreateLVar 3 args");
        final String VAR_NUM = "3";

        final double controlNum = Math.ceil(Math.random() * 100);

        LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = lvarHelper.createLVar(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.createLVar(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (float) controlNum);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) controlNum);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) controlNum);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
        } else {
            System.out.println("Connected to FSUIPC. Will also read back the value of created LVar and test for match.");
            //test all variant with fsuipc null
            result = lvarHelper.createLVar(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.DOUBLE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            result = lvarHelper.createLVar(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (float) controlNum + 1.0f);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.FLOAT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum + 1.0f, LVarHelper.LVarValueFormat.FLOAT);

            //signed byte
            result = lvarHelper.createLVar(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) (controlNum + (byte) 2));
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_BYTE);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 2), LVarHelper.LVarValueFormat.SIGNED_BYTE);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) (controlNum + (short) 3));
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_SHORT);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 3), LVarHelper.LVarValueFormat.SIGNED_SHORT);

            result = lvarHelper.createLVar(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum + 4);
            testResult(result, LVAR_CREATE_VALUE_OFFSET, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
            registerAndProcessRequests(result);
            testLVarValue(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 4, LVarHelper.LVarValueFormat.SIGNED_INTEGER);
        }
    }

    /**
     * Test of createLVar method, of class LVarHelper.
     */
    @Test
    public void testCreateLVar_5args() {
        System.out.println("JUnit test: CreateLVar 5 args");
        final String VAR_NUM = "5";

        final double controlNum = Math.ceil(Math.random() * 100);

        LVarHelper lvarHelper = new LVarHelper();

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            boolean bres = lvarHelper.createLVar(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE, fsuipc);
            assertFalse(bres);

            //float
            bres = lvarHelper.createLVar(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (float) controlNum, LVarHelper.LVarValueFormat.FLOAT, fsuipc);
            assertFalse(bres);

            bres = lvarHelper.createLVar(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.SIGNED_BYTE, fsuipc);
            assertFalse(bres);
            //actually the same as signed byte internally
            bres = lvarHelper.createLVar(CUSTOM_LVAR_UBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_BYTE, fsuipc);
            assertFalse(bres);

            bres = lvarHelper.createLVar(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.SIGNED_SHORT, fsuipc);
            assertFalse(bres);
            //actually the same as signed byte internally
            bres = lvarHelper.createLVar(CUSTOM_LVAR_USHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc);
            assertFalse(bres);

            bres = lvarHelper.createLVar(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc);
            assertFalse(bres);
            //actually the same as signed byte internally
            bres = lvarHelper.createLVar(CUSTOM_LVAR_UINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER, fsuipc);
            assertFalse(bres);
        } else {
            System.out.println("Connected to FSUIPC. Will also read back the value of created LVar and test for match.");
            //test all variant with fsuipc null
            boolean bres = lvarHelper.createLVar(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_DOUBLE + VAR_NUM, LVAR_READ_VALUE_OFFSET, controlNum, LVarHelper.LVarValueFormat.DOUBLE);

            //float
            bres = lvarHelper.createLVar(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (float) controlNum + 1.0f, LVarHelper.LVarValueFormat.FLOAT, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_FLOAT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (float) controlNum + 1.0f, LVarHelper.LVarValueFormat.FLOAT);

            //signed byte
            bres = lvarHelper.createLVar(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) (controlNum + (byte) 2), LVarHelper.LVarValueFormat.SIGNED_BYTE, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_SBYTE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 2), LVarHelper.LVarValueFormat.SIGNED_BYTE);

            //actually the same as signed byte internally
            bres = lvarHelper.createLVar(CUSTOM_LVAR_UBYTE + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.UNSIGNED_BYTE, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_UBYTE + VAR_NUM, LVAR_READ_VALUE_OFFSET, (byte) (controlNum + (byte) 3), LVarHelper.LVarValueFormat.UNSIGNED_BYTE);

            bres = lvarHelper.createLVar(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) (controlNum + (short) 4), LVarHelper.LVarValueFormat.SIGNED_SHORT, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_SSHORT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 4), LVarHelper.LVarValueFormat.SIGNED_SHORT);

            //actually the same as signed byte internally
            bres = lvarHelper.createLVar(CUSTOM_LVAR_USHORT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_USHORT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (short) (controlNum + (short) 5), LVarHelper.LVarValueFormat.UNSIGNED_SHORT);

            bres = lvarHelper.createLVar(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum + 6, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_SINT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 6, LVarHelper.LVarValueFormat.SIGNED_INTEGER);

            //actually the same as signed byte internally
            bres = lvarHelper.createLVar(CUSTOM_LVAR_UINT + VAR_NUM, LVAR_CREATE_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER, fsuipc);
            assertTrue(bres);
            fsuipc.processRequestsOnce();
            testLVarValue(CUSTOM_LVAR_UINT + VAR_NUM, LVAR_READ_VALUE_OFFSET, (int) controlNum + 7, LVarHelper.LVarValueFormat.UNSIGNED_INTEGER);
        }
    }

    @Test
    public void testParamRequestClass() {               
        LVarHelper.LVarParamRequest paramRequest;
        final int controlNum = (int) (Math.random() * 100);
        
        System.out.println("JUnit test: testParamRequestClass");

        try {
            paramRequest = new LVarHelper.LVarParamRequest(null);
            fail("Passing null as parameter for LVarParamRequest constructor should cause NullPointerException");
        } catch (NullPointerException ex) {
            //Ok
        }

        try {
            paramRequest = new LVarHelper.LVarParamRequest(controlNum);
            paramRequest.setValue(controlNum + 10);
        } catch (Exception ex) {
            fail("Setting the value: " + controlNum + " as parameter for LVarParamRequest.setValue() should not cause any Exception");
        }

        try {
            paramRequest = new LVarHelper.LVarParamRequest(controlNum);
            paramRequest.getValue();
            fail("The LVarParamRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        paramRequest = new LVarHelper.LVarParamRequest(controlNum);
        assertEquals(LVarHelper.PARAMETER_OFFSET, paramRequest.getOffset());
        assertEquals(DataRequest.BUFFER_LENGTH_INT, paramRequest.getSize());
    }

    @Test
    public void testControlRequestClass() {
        LVarHelper.LVarControlRequest controlRequest;
        
        System.out.println("JUnit test: testControlRequestClass");
        
        try {
            controlRequest = new LVarHelper.LVarControlRequest(CUSTOM_LVAR_DOUBLE);
            controlRequest.getValue();
            fail("The LVarControlRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        } 

        //Try the constructors and setting the values, than verify that the constructed String is correct
        controlRequest = new LVarHelper.LVarControlRequest(CUSTOM_LVAR_DOUBLE);
        //it should be read by default
        Charset controlRequestCharset = controlRequest.getCharset();
        String strValue = new String(controlRequest.getDataBuffer(), controlRequestCharset).trim();
        String compareValue = new String((LVarHelper.LVarControlRequestCommand.READ.getValue() + CUSTOM_LVAR_DOUBLE).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LVarHelper.CONTROL_OFFSET, controlRequest.getOffset());
        
        //try change lvar
        controlRequest.setValue(CUSTOM_LVAR_FLOAT);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LVarHelper.LVarControlRequestCommand.READ.getValue() + CUSTOM_LVAR_FLOAT).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);       
        
        //try change command
        controlRequest.setCommand(LVarHelper.LVarControlRequestCommand.WRITE);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LVarHelper.LVarControlRequestCommand.WRITE.getValue() + CUSTOM_LVAR_FLOAT).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue); 
        assertEquals(LVarHelper.LVarControlRequestCommand.WRITE, controlRequest.getCommand());
        
        //second change of command
        controlRequest.setCommand(LVarHelper.LVarControlRequestCommand.CREATE);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LVarHelper.LVarControlRequestCommand.CREATE.getValue() + CUSTOM_LVAR_FLOAT).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);   
        assertEquals(LVarHelper.LVarControlRequestCommand.CREATE, controlRequest.getCommand());
        
        
        //Try another constructors
        controlRequest = new LVarHelper.LVarControlRequest(LVarHelper.LVarControlRequestCommand.WRITE, CUSTOM_LVAR_FOR_READ);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LVarHelper.LVarControlRequestCommand.WRITE.getValue() + CUSTOM_LVAR_FOR_READ).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);   
        assertEquals(LVarHelper.LVarControlRequestCommand.WRITE, controlRequest.getCommand());
        assertEquals(LVarHelper.CONTROL_OFFSET, controlRequest.getOffset());
        
        Charset charset = Charset.forName("UTF-8");
        controlRequest = new LVarHelper.LVarControlRequest(CUSTOM_LVAR_FOR_WRITE, charset);
        strValue = new String(controlRequest.getDataBuffer(), charset).trim();
        compareValue = new String((LVarHelper.LVarControlRequestCommand.READ.getValue() + CUSTOM_LVAR_FOR_WRITE).getBytes(charset), charset).trim();
        assertEquals(compareValue, strValue);   
        assertEquals(LVarHelper.LVarControlRequestCommand.READ, controlRequest.getCommand());
        assertEquals(LVarHelper.CONTROL_OFFSET, controlRequest.getOffset());
        assertEquals(charset, controlRequest.getCharset());
                
        controlRequest = new LVarHelper.LVarControlRequest(LVarHelper.LVarControlRequestCommand.CREATE, CUSTOM_LVAR_SBYTE, charset);
        strValue = new String(controlRequest.getDataBuffer(), charset).trim();
        compareValue = new String((LVarHelper.LVarControlRequestCommand.CREATE.getValue() + CUSTOM_LVAR_SBYTE).getBytes(charset), charset).trim();
        assertEquals(compareValue, strValue);   
        assertEquals(LVarHelper.LVarControlRequestCommand.CREATE, controlRequest.getCommand());
        assertEquals(LVarHelper.CONTROL_OFFSET, controlRequest.getOffset());
        assertEquals(charset, controlRequest.getCharset());
    }

    @Test
    public void testResultRequestClass() {
        IDataRequest resultRequest;
        final double controlNum = (int) (Math.random() * 100);
        
        System.out.println("JUnit test: testResultRequestClass");
        
        resultRequest = new LVarHelper.DoubleLVarReadRequest(LVAR_READ_VALUE_OFFSET);
        assertEquals(LVAR_READ_VALUE_OFFSET, resultRequest.getOffset());               
        try {
            resultRequest.setValue(controlNum);            
            fail("The DoubleLVarReadRequest.setValue() should cause the UnsupportedOperationException as it is the READ ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.FloatLVarReadRequest(LVAR_READ_VALUE_OFFSET);
        assertEquals(LVAR_READ_VALUE_OFFSET, resultRequest.getOffset());               
        try {
            resultRequest.setValue((float)controlNum);            
            fail("The FloatLVarReadRequest.setValue() should cause the UnsupportedOperationException as it is the READ ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
                
        resultRequest = new LVarHelper.ByteLVarReadRequest(LVAR_READ_VALUE_OFFSET);
        assertEquals(LVAR_READ_VALUE_OFFSET, resultRequest.getOffset());               
        try {
            resultRequest.setValue((byte)controlNum);            
            fail("The ByteLVarReadRequest.setValue() should cause the UnsupportedOperationException as it is the READ ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.ShortLVarReadRequest(LVAR_READ_VALUE_OFFSET);
        assertEquals(LVAR_READ_VALUE_OFFSET, resultRequest.getOffset());               
        try {
            resultRequest.setValue((short)controlNum);            
            fail("The ShortLVarReadRequest.setValue() should cause the UnsupportedOperationException as it is the READ ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.IntegerLVarReadRequest(LVAR_READ_VALUE_OFFSET);
        assertEquals(LVAR_READ_VALUE_OFFSET, resultRequest.getOffset());               
        try {
            resultRequest.setValue((int)controlNum);            
            fail("The IntegerLVarReadRequest.setValue() should cause the UnsupportedOperationException as it is the READ ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        
        //Write requests
        resultRequest = new LVarHelper.DoubleLVarWriteRequest(LVAR_WRITE_VALUE_OFFSET, controlNum);
        assertEquals(LVAR_WRITE_VALUE_OFFSET, resultRequest.getOffset());          
        try {
            resultRequest.getValue();            
            fail("The DoubleLVarWriteRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.FloatLVarWriteRequest(LVAR_WRITE_VALUE_OFFSET, (float)controlNum);
        assertEquals(LVAR_WRITE_VALUE_OFFSET, resultRequest.getOffset());          
        try {
            resultRequest.getValue();            
            fail("The FloatLVarWriteRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.ByteLVarWriteRequest(LVAR_WRITE_VALUE_OFFSET, (byte)controlNum);
        assertEquals(LVAR_WRITE_VALUE_OFFSET, resultRequest.getOffset());          
        try {
            resultRequest.getValue();            
            fail("The ByteLVarWriteRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.ShortLVarWriteRequest(LVAR_WRITE_VALUE_OFFSET, (short)controlNum);
        assertEquals(LVAR_WRITE_VALUE_OFFSET, resultRequest.getOffset());          
        try {
            resultRequest.getValue();            
            fail("The ShortLVarWriteRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
        
        resultRequest = new LVarHelper.IntegerLVarWriteRequest(LVAR_WRITE_VALUE_OFFSET, (int)controlNum);
        assertEquals(LVAR_WRITE_VALUE_OFFSET, resultRequest.getOffset());          
        try {
            resultRequest.getValue();            
            fail("The IntegerLVarWriteRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }
    }

    private void testResult(LVarHelper.LVarResult result, int resultOffset, LVarHelper.LVarValueFormat valueFormat) {
        assertNotNull(result);
        assertEquals(LVAR_REQUEST_PARAM_OFFSET, result.getParamRequest().getOffset());
        assertEquals(DataRequest.BUFFER_LENGTH_INT, result.getParamRequest().getDataBuffer().length);

        assertEquals(LVAR_REQUEST_OFFSET, result.getControlRequest().getOffset());

        assertEquals(resultOffset, result.getResultRequest().getOffset());
        switch (valueFormat) {
            case DOUBLE:
                assertEquals(DataRequest.BUFFER_LENGTH_DOUBLE, result.getResultRequest().getDataBuffer().length);
                break;
            case FLOAT:
                assertEquals(DataRequest.BUFFER_LENGTH_FLOAT, result.getResultRequest().getDataBuffer().length);
                break;
            case SIGNED_BYTE:
            case UNSIGNED_BYTE:
                assertEquals(DataRequest.BUFFER_LENGTH_BYTE, result.getResultRequest().getDataBuffer().length);
                break;
            case SIGNED_SHORT:
            case UNSIGNED_SHORT:
                assertEquals(DataRequest.BUFFER_LENGTH_SHORT, result.getResultRequest().getDataBuffer().length);
                break;
            case SIGNED_INTEGER:
            case UNSIGNED_INTEGER:
                assertEquals(DataRequest.BUFFER_LENGTH_INT, result.getResultRequest().getDataBuffer().length);
                break;
        }
    }

    private void testLVarValue(String lvar, int resultOffset, Object expectedValue, LVarHelper.LVarValueFormat valueFormat) {
        //read back the value
        final LVarHelper lvarHelper = new LVarHelper();
        LVarHelper.LVarResult result = lvarHelper.readLVar(lvar, resultOffset, valueFormat, fsuipc, false);
        testResult(result, resultOffset, valueFormat);
        int iRet = fsuipc.processRequestsOnce();
        assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
        if (expectedValue instanceof Float) {
            assertEquals((float) expectedValue, (float) result.getResultRequest().getValue(), FLOAT_DELTA);
        } else if (expectedValue instanceof Double) {
            assertEquals((double) expectedValue, (double) result.getResultRequest().getValue(), DOUBLE_DELTA);
        } else {
            assertEquals(expectedValue, result.getResultRequest().getValue());
        }
    }

    private void registerAndProcessRequests(LVarHelper.LVarResult result) {
        if (result.getControlRequest().getCommand() == LVarHelper.LVarControlRequestCommand.READ) {
            fsuipc.addOneTimeRequest(result.getParamRequest());
            fsuipc.addOneTimeRequest(result.getControlRequest());
            fsuipc.addOneTimeRequest(result.getResultRequest());
        } else {
            fsuipc.addOneTimeRequest(result.getResultRequest());
            fsuipc.addOneTimeRequest(result.getParamRequest());
            fsuipc.addOneTimeRequest(result.getControlRequest());
        }
        
        int iRet = fsuipc.processRequestsOnce();
        assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
    }
}
