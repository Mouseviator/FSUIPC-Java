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
import com.mouseviator.fsuipc.helpers.MacroHelper.MacroExecuteRequest;
import com.mouseviator.fsuipc.testing.InteractiveTest;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import javax.swing.JOptionPane;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the {@link MacroHelper} class.
 * By default it will perform an interactive test if there is also simulator running (and therefore, FSUIPC connection can be made).
 * 
 * @author Murdock
 */
public class MacroHelperTest extends InteractiveTest {

    private static FSUIPC fsuipc;

    private final int MACRO_CONTROL_OFFSET = 0x0D70;
    private final int MACRO_REQUEST_PARAM_OFFSET = 0x0D6C;

    private static final String MACRO_FILE = "fsuipctest";
    private static final String MACRO = "PauseToggle";
    private static final String TEST_MACRO = MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO;

    private static final String MACRO_FILE2 = "fsuipctest";
    private static final String MACRO2 = "ParkBrake";
    private static final String TEST_MACRO2 = MACRO_FILE2 + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2;

    private static final String MACRO_FILE_INVALID = "MacroFileViolateMaxLength";
    private static final String MACRO_INVALID = "MacroViolateMaxLength";
    private static final String TEST_MACRO_INVALID = MACRO_FILE_INVALID + MacroExecuteRequest.MACRO_SEPARATOR + MACRO_INVALID;

    private static int parkBrakeValue = 0;

    public MacroHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("JUnit test: MacroHelper class setUp - Will try to load FSUIPC and connect to the simulator...");

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
            System.out.println("*************************************************************************************");
            System.out.println("""
                               IF THIS TEST IS RUN IN INTRACTIVE MODE, THERE MUST BE A "fsuiptest.mcro" MACRO FILE INSIDE
                               THE FSUIPC FOLDER, AS THE TESTS FROMTHIS CLASS USES IT TO TRIGGER ACTIONS WITHIN THE SIMULATOR. THE MACRO FILE CAN BE FOUND INSIDE THE "lua_macro" FOLDER OF THE"FSUIPCSimpleTest" PROJECT.
                               YOU SHOULD ALSO ENABLE FSUIPC LOGGING AND DISPLAY THE CONSOLE WINDOW.""");
            System.out.println("*************************************************************************************");
        }

        System.out.println("JUnit test: MacroHelper class setUp - Done.");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("JUnit test: MacroHelper class tearDown - Will disconnect FSUIPC if connected...");

        if (fsuipc != null) {
            fsuipc.disconnect();
            fsuipc = null;
        }

        System.out.println("JUnit test: MacroHelper class tearDown - Done.");
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_String() {
        System.out.println("JUnit test: executeMacro 1 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO);
            testResult(result, MACRO_FILE, MACRO);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO + "\" macro.");
            
            showTestInfo("<p>This test tests the: MacroHelper.executeMacro(macro_file, macro_name)  function</p>"
                    + "<p>This test will try to run the: " + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + " macro.</br>Please watch if the simulator pauses/un-pauses.</p>");
            
            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO);
            testResult(result, MACRO_FILE, MACRO);
            registerAndProcessRequests(result);

            System.out.println("Macro should have execute. If it did, the sim should be paused/unpaused!");
            
            askTestResult("<p>Was the simulator paused/un-paused?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_3args_1() {
        System.out.println("JUnit test: executeMacro 3 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        toggleParkBrakeValue();

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE, MACRO2, parkBrakeValue);
            testResult(result, MACRO_FILE, MACRO2);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + "\" macro.");

            showParkingBrakeInfo(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2);

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE, MACRO2, parkBrakeValue);
            testResult(result, MACRO_FILE, MACRO2);
            registerAndProcessRequests(result);

            System.out.println("Macro should have execute. If it did, aircraft parking brake shoule be toggled!");

            askTestResult("<p>Was the aircraft parking brake <b>toggled</b>?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);          
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_String_int() {
        System.out.println("JUnit test: executeMacro 2 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        toggleParkBrakeValue();

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2, parkBrakeValue);
            testResult(result, MACRO_FILE, MACRO2);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + "\" macro.");

            showParkingBrakeInfo(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2);            

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2, parkBrakeValue);
            testResult(result, MACRO_FILE, MACRO2);
            registerAndProcessRequests(result);

            System.out.println("Macro should have execute. If it did, aircraft parking brake shoule be toggled!");

            askTestResult("<p>Was the aircraft parking brake <b>toggled</b>?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_4args_1() {
        System.out.println("JUnit test: executeMacro 4 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        toggleParkBrakeValue();

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2, parkBrakeValue, fsuipc, true);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + "\" macro.");

            showParkingBrakeInfo(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2);            

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2, parkBrakeValue, fsuipc, false);
            testResult(result, MACRO_FILE, MACRO2);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Macro should have execute. If it did, aircraft parking brake shoule be toggled!");

            askTestResult("<p>Was the aircraft parking brake <b>toggled</b>?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_String_String() {
        System.out.println("JUnit test: executeMacro 2 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE, MACRO);
            testResult(result, MACRO_FILE, MACRO);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO + "\" macro.");

            showTestInfo("<p>This test tests the: MacroHelper.executeMacro(macro_file, macro_name)  function</p>"
                    + "<p>This test will try to run the: " + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + " macro.</br>Please watch if the simulator pauses/un-pauses.</p>");

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE, MACRO);
            testResult(result, MACRO_FILE, MACRO);
            registerAndProcessRequests(result);

            System.out.println("Macro should have execute. If it did, the sim should be paused/unpaused!");

            askTestResult("<p>Was the simulator paused/un-paused?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_4args_2() {
        System.out.println("JUnit test: executeMacro 2 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE, MACRO, fsuipc, false);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO + "\" macro.");

            showTestInfo("<p>This test tests the: MacroHelper.executeMacro(macro_file, macro_name, fsuipc, continual)  function</p>"
                    + "<p>This test will try to run the: " + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + " macro.</br>Please watch if the simulator pauses/un-pauses.</p>");

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE, MACRO, fsuipc, false);
            testResult(result, MACRO_FILE, MACRO);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Macro should have execute. If it did, the sim should be paused/unpaused!");

            askTestResult("<p>Was the simulator paused/un-paused?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_3args_2() {
        System.out.println("JUnit test: executeMacro 2 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO, fsuipc, false);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO + "\" macro.");

            showTestInfo("<p>This test tests the: MacroHelper.executeMacro(macro_file_and_name, fsuipc, continual)  function</p>"
                    + "<p>This test will try to run the: " + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + " macro.</br>Please watch if the simulator pauses/un-pauses.</p>");

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO, fsuipc, false);
            testResult(result, MACRO_FILE, MACRO);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Macro should have execute. If it did, the sim should be paused/unpaused!");

            askTestResult("<p>Was the simulator paused/un-paused?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of executeMacro method, of class MacroHelper.
     */
    @Test
    public void testExecuteMacro_5args() {
        System.out.println("JUnit test: executeMacro 4 args");

        MacroHelper macroHelper = new MacroHelper();
        MacroHelper.MacroResult result = null;

        toggleParkBrakeValue();

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = macroHelper.executeMacro(MACRO_FILE, MACRO2, parkBrakeValue, fsuipc, true);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2 + "\" macro.");

            showParkingBrakeInfo(MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO2);

            //create requests and test them
            result = macroHelper.executeMacro(MACRO_FILE, MACRO2, parkBrakeValue, fsuipc, false);
            testResult(result, MACRO_FILE, MACRO2);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Macro should have execute. If it did, aircraft parking brake shoule be toggled!");

            askTestResult("<p>Was the aircraft parking brake <b>toggled</b>?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    @Test
    public void testParamRequestClass() {
        MacroHelper.MacroParamRequest paramRequest;
        final int controlNum = (int) (Math.random() * 255);

        System.out.println("JUnit test: Test MacroParamRequest class.");

        try {
            paramRequest = new MacroHelper.MacroParamRequest(null);
            fail("Passing null as parameter for MacroParamRequest constructor should cause NullPointerException");
        } catch (NullPointerException ex) {
            //Ok
        }

        try {
            paramRequest = new MacroHelper.MacroParamRequest(controlNum);
            paramRequest.setValue(controlNum + 10);
        } catch (Exception ex) {
            fail("Setting the value: " + controlNum + " as parameter for MacroParamRequest.setValue() should not cause any Exception");
        }

        try {
            paramRequest = new MacroHelper.MacroParamRequest(controlNum);
            paramRequest.getValue();
            fail("The MacroParamRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }

        paramRequest = new MacroHelper.MacroParamRequest(controlNum);
        assertEquals(MacroHelper.PARAMETER_OFFSET, paramRequest.getOffset());
        assertEquals(DataRequest.BUFFER_LENGTH_INT, paramRequest.getSize());
    }

    @Test
    public void testExecuteRequestClass() {
        MacroHelper.MacroExecuteRequest macroExecuteRequets;

        System.out.println("JUnit test: Test MacroExecuteRequest class.");

        try {
            macroExecuteRequets = new MacroHelper.MacroExecuteRequest(TEST_MACRO);
            macroExecuteRequets.getValue();
            fail("The LuaControlRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }

        //Try the constructors and setting the values, than verify that the constructed String is correct
        macroExecuteRequets = new MacroHelper.MacroExecuteRequest(TEST_MACRO);
        //it should be read by default
        Charset controlRequestCharset = macroExecuteRequets.getCharset();
        String strValue = new String(macroExecuteRequets.getDataBuffer(), controlRequestCharset).trim();
        String compareValue = new String(TEST_MACRO.getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.CONTROL_OFFSET, macroExecuteRequets.getOffset());

        //try change macro
        macroExecuteRequets.setValue(TEST_MACRO2);
        strValue = new String(macroExecuteRequets.getDataBuffer(), macroExecuteRequets.getCharset()).trim();
        compareValue = new String(TEST_MACRO2.getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);

        macroExecuteRequets = new MacroHelper.MacroExecuteRequest(MACRO_FILE, MACRO);
        strValue = new String(macroExecuteRequets.getDataBuffer()).trim();
        compareValue = new String((MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO).getBytes()).trim();
        assertEquals(compareValue, strValue);
        assertEquals(MacroHelper.CONTROL_OFFSET, macroExecuteRequets.getOffset());

        //Try another constructors        
        Charset charset = Charset.forName("UTF-8");
        macroExecuteRequets = new MacroHelper.MacroExecuteRequest(TEST_MACRO, charset);
        strValue = new String(macroExecuteRequets.getDataBuffer(), charset).trim();
        compareValue = new String(TEST_MACRO.getBytes(charset), charset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(MacroHelper.CONTROL_OFFSET, macroExecuteRequets.getOffset());
        assertEquals(charset, macroExecuteRequets.getCharset());

        macroExecuteRequets = new MacroHelper.MacroExecuteRequest(MACRO_FILE, MACRO, charset);
        strValue = new String(macroExecuteRequets.getDataBuffer(), charset).trim();
        compareValue = new String((MACRO_FILE + MacroExecuteRequest.MACRO_SEPARATOR + MACRO).getBytes(charset), charset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(MacroHelper.CONTROL_OFFSET, macroExecuteRequets.getOffset());
        assertEquals(charset, macroExecuteRequets.getCharset());

        //Test Invalid parameters
        try {
            macroExecuteRequets = new MacroHelper.MacroExecuteRequest(TEST_MACRO_INVALID);
            macroExecuteRequets = null;
            fail("Trying to create MacroExecuteRequest with macro file name or macro name exceeding 16 chars should cause InvalidParameterException");
        } catch (InvalidParameterException ex) {

        }

        try {
            macroExecuteRequets = new MacroHelper.MacroExecuteRequest(TEST_MACRO);
            macroExecuteRequets.setValue(TEST_MACRO_INVALID);
            fail("Trying to setValue for MacroExecuteRequest with macro file name or macro name exceeding 16 chars should cause InvalidParameterException");
        } catch (InvalidParameterException ex) {

        }

        try {
            macroExecuteRequets = new MacroHelper.MacroExecuteRequest(MACRO_FILE_INVALID, MACRO);
            macroExecuteRequets = null;
            fail("Trying to create MacroExecuteRequest with macro file exceeding 16 chars should cause InvalidParameterException");
        } catch (InvalidParameterException ex) {

        }

        try {
            macroExecuteRequets = new MacroHelper.MacroExecuteRequest(MACRO_FILE, MACRO_INVALID);
            macroExecuteRequets = null;
            fail("Trying to create MacroExecuteRequest with macro name exceeding 16 chars should cause InvalidParameterException");
        } catch (InvalidParameterException ex) {

        }

        try {
            macroExecuteRequets = new MacroHelper.MacroExecuteRequest(TEST_MACRO_INVALID, MACRO_INVALID);
            macroExecuteRequets = null;
            fail("Trying to create MacroExecuteRequest with macro file name or macro name exceeding 16 chars should cause InvalidParameterException");
        } catch (InvalidParameterException ex) {

        }
    }

    private void testResult(MacroHelper.MacroResult result, String macroFile, String macro) {
        //test result not null, param request corrext buffer length and offset
        assertNotNull(result);
        assertEquals(MACRO_REQUEST_PARAM_OFFSET, result.getParamRequest().getOffset());
        assertEquals(DataRequest.BUFFER_LENGTH_INT, result.getParamRequest().getDataBuffer().length);

        assertEquals(MACRO_CONTROL_OFFSET, result.getMacroExecuteRequest().getOffset());

        if (macroFile != null && macro != null) {
            //test that command string is ok
            Charset charset = result.getMacroExecuteRequest().getCharset();
            String controlValue = new String(result.getMacroExecuteRequest().getDataBuffer(), charset).trim();
            String compareValue = new String((macroFile + MacroExecuteRequest.MACRO_SEPARATOR + macro).getBytes(charset), charset).trim();
            assertEquals(compareValue, controlValue);
        }
    }

    private void registerAndProcessRequests(MacroHelper.MacroResult result) {
        fsuipc.addOneTimeRequest(result.getParamRequest());
        fsuipc.addOneTimeRequest(result.getMacroExecuteRequest());

        int iRet = fsuipc.processRequestsOnce();
        assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
    }

    private void toggleParkBrakeValue() {
        /*if (parkBrakeValue == 0) {
            parkBrakeValue = 1;            
        } else {
            parkBrakeValue = 0;            
        }*/
        //Parking brake control actually works as toggle. It does not really matter what value ve use, it just must be different than the one
        //that is used already (to fire the toggle)
        parkBrakeValue = (int)(Math.random() * 100);
        System.out.println("Parking brake value is now: " + parkBrakeValue);
    }

    private void showParkingBrakeInfo(String script) {
        showTestInfo("<p>This test tests the: MacroHelper.executeMacro(macro_file, macro_name, macro_parameter)  function</p>"
                    + "<p>This test will try to run the: " + script + " macro.</br>Please watch the simulator if aircraft parking brake was <b>toggled</b>.</p>");
    }
}
