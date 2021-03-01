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
import com.mouseviator.fsuipc.helpers.LuaHelper.LuaControlRequest;
import com.mouseviator.fsuipc.testing.InteractiveTest;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the {@link LuaHelper} class.
 * By default it will perform an interactive test if there is also simulator running (and therefore, FSUIPC connection can be made).
 * 
 * @author Murdock
 */
public class LuaHelperTest extends InteractiveTest {

    private static FSUIPC fsuipc;

    private final int LUA_CONTROL_OFFSET = 0x0D70;
    private final int LUA_REQUEST_PARAM_OFFSET = 0x0D6C;
    private static final String LUA_PROGRAM_HELLOW = "hello world";
    private static final String LUA_PROGRAM_FP = "flag_param";
    private static final String LUA_PROGRAM_BS = "blocking script";

    public LuaHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("JUnit test: LuaHelper class setUp - Will try to load FSUIPC and connect to the simulator...");

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
                               IF THIS TEST IS RUN IN INTRACTIVE MODE, THERE MUST BE "hello world.lua", "flag_param.lua" and "blocking script.lua" SCRIPT FILES INSIDE
                               THE FSUIPC FOLDER, AS THE TESTS FROM THIS CLASS USES THEM TO TRIGGER ACTIONS WITHIN THE SIMULATOR. THE SCRIPT FILES CAN BE FOUND INSIDE THE "lua_macro" FOLDER OF THE"FSUIPCSimpleTest" PROJECT.
                               YOU SHOULD ALSO ENABLE FSUIPC LOGGING AND DISPLAY THE CONSOLE WINDOW.""");
            System.out.println("*************************************************************************************");
        }

        System.out.println("JUnit test: LuaHelper class setUp - Done.");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("JUnit test: LuaHelper class tearDown - Will disconnect FSUIPC if connected...");

        if (fsuipc != null) {
            fsuipc.disconnect();
            fsuipc = null;
        }

        System.out.println("JUnit test: LuaHelper class tearDown - Done.");
    }

    /**
     * Test of luaRequest method, of class LuaHelper.
     */
    @Test
    public void testLuaRequest_3args() {
        System.out.println("JUnit test: luaRequest 3 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;
        
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaRequest(LUA_PROGRAM_HELLOW, flagNum, LuaHelper.LuaControlRequestCommand.LUA);
            testResult(result, LUA_PROGRAM_HELLOW);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script.");

            showTestInfo("<p>This test tests the: LuaHelper.luaRequest(lua_program, parameter, command)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script.</br>Please watch the FSUIPC console for the \"Hello world...\" text.</p>");

            //create requests and test them
            result = luaHelper.luaRequest(LUA_PROGRAM_HELLOW, flagNum, LuaHelper.LuaControlRequestCommand.LUA);
            testResult(result, LUA_PROGRAM_HELLOW);
            registerAndProcessRequests(result);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window.");

            askTestResult("<p>Did the string starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaRequest method, of class LuaHelper.
     */
    @Test
    public void testLuaRequest_5args() {
        System.out.println("JUnit test: luaRequest 5 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;
        
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaRequest(LUA_PROGRAM_HELLOW, flagNum, LuaHelper.LuaControlRequestCommand.LUA, fsuipc, false);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script.");

            showTestInfo("<p>This test tests the: LuaHelper.luaRequest(lua_program, parameter, command)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script.</br>Please watch the FSUIPC console for the \"Hello world...\" text.</p>");

            //create requests and test them
            result = luaHelper.luaRequest(LUA_PROGRAM_HELLOW, flagNum, LuaHelper.LuaControlRequestCommand.LUA, fsuipc, false);
            testResult(result, LUA_PROGRAM_HELLOW);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window.");

            askTestResult("<p>Did the string starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of lua method, of class LuaHelper.
     */
    @Test
    public void testLua_String() {
        System.out.println("JUnit test: lua 1 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.lua(LUA_PROGRAM_HELLOW);
            testResult(result, LUA_PROGRAM_HELLOW);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script.");

            showTestInfo("<p>This test tests the: LuaHelper.lua(lua_program)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script.</br>Please watch the FSUIPC console for the \"Hello world...\" text.</p>");

            //create requests and test them
            result = luaHelper.lua(LUA_PROGRAM_HELLOW);
            testResult(result, LUA_PROGRAM_HELLOW);
            registerAndProcessRequests(result);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window.");

            askTestResult("<p>Did the string starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of lua method, of class LuaHelper.
     */
    @Test
    public void testLua_String_FSUIPC() {
        System.out.println("JUnit test: lua 2 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.lua(LUA_PROGRAM_HELLOW, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script.");

            showTestInfo("<p>This test tests the: LuaHelper.lua(lua_program, fsuipc)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script.</br>Please watch the FSUIPC console for the \"Hello world...\" text.</p>");

            //create requests and test them
            result = luaHelper.lua(LUA_PROGRAM_HELLOW, fsuipc);
            testResult(result, LUA_PROGRAM_HELLOW);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window.");

            askTestResult("<p>Did the string starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of lua method, of class LuaHelper.
     */
    @Test
    public void testLua_String_int() {
        System.out.println("JUnit test: lua 2 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.lua(LUA_PROGRAM_HELLOW, flagNum);
            testResult(result, LUA_PROGRAM_HELLOW);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script with param: " + flagNum);

            showTestInfo("<p>This test tests the: LuaHelper.lua(lua_program, parameter)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script with parameter: " + flagNum + ".</br>Please watch the FSUIPC console for the \"Hello world...\" text.</p>");

            //create requests and test them
            result = luaHelper.lua(LUA_PROGRAM_HELLOW, flagNum);
            testResult(result, LUA_PROGRAM_HELLOW);
            registerAndProcessRequests(result);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window.");

            askTestResult("<p>Did the string starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }

    }

    /**
     * Test of lua method, of class LuaHelper.
     */
    @Test    
    public void testLua_3args() {
        System.out.println("JUnit test: lua 3 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.lua(LUA_PROGRAM_HELLOW, flagNum, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script with param: " + flagNum);

            showTestInfo("<p>This test tests the: LuaHelper.lua(lua_program, parameter, fsuipc)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script with parameter: " + flagNum + ".</br>Please watch the FSUIPC console for the \"Hello world...\" text.</p>");

            //create requests and test them
            result = luaHelper.lua(LUA_PROGRAM_HELLOW, flagNum, fsuipc);
            testResult(result, LUA_PROGRAM_HELLOW);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window.");

            askTestResult("<p>Did the string starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaValue method, of class LuaHelper.
     */
    @Test    
    public void testLuaValue_String_int() {
        System.out.println("JUnit test: luaValue 2 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaValue(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
        } else {            
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will send it a parameter and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaDebug(lua_program)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will send parameter: " + flagNum + " to the script. This should result in text like:</br>"
                            + "\"Received param: " + flagNum + "\". Than the script will be killed.</p>");
            
            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaValue(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Received param: " + flagNum + "\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaValue method, of class LuaHelper.
     */
    @Test    
    public void testLuaValue_3args() {
        System.out.println("JUnit test: luaValue 3 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaValue(LUA_PROGRAM_FP, flagNum, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will send it a parameter and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaDebug(lua_program)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will send parameter: " + flagNum + " to the script. This should result in text like:</br>"
                            + "\"Received param: " + flagNum + "\". Than the script will be killed.</p>");
            
            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaValue(LUA_PROGRAM_FP, flagNum, fsuipc);
            testResult(result, LUA_PROGRAM_FP);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Received param: " + flagNum + "\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaDebug method, of class LuaHelper.
     */
    @Test    
    public void testLuaDebug_String() {
        System.out.println("JUnit test: luaDebug 1 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW);
            testResult(result, LUA_PROGRAM_HELLOW);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script in debug mode.");

            showTestInfo("<p>This test tests the: LuaHelper.luaDebug(lua_program)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script in debug mode.</br>Please watch the FSUIPC console for the \"Hello world...\" text.<br/></p>");

            //create requests and test them
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW);
            testResult(result, LUA_PROGRAM_HELLOW);
            registerAndProcessRequests(result);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window. Also the log file should be generated for the script.");

            askTestResult("<p>Did the test starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaDebug method, of class LuaHelper.
     */
    @Test
    public void testLuaDebug_String_FSUIPC() {
        System.out.println("JUnit test: luaDebug 2 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script in debug mode.");

            showTestInfo("<p>This test tests the: LuaHelper.luaDebug(lua_program, fsuipc)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script in debug mode.</br>Please watch the FSUIPC console for the \"Hello world...\" text.<br/></p>");

            //create requests and test them
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW, fsuipc);
            testResult(result, LUA_PROGRAM_HELLOW);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window. Also the log file should be generated for the script.");

            askTestResult("<p>Did the test starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaDebug method, of class LuaHelper.
     */
    @Test
    public void testLuaDebug_String_int() {
        System.out.println("JUnit test: luaDebug 2 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW, flagNum);
            testResult(result, LUA_PROGRAM_HELLOW);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script with parameter: " + flagNum + " in debug mode.");

            showTestInfo("<p>This test tests the: LuaHelper.luaDebug(lua_program, parameter)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script with parameter: " + flagNum + " in debug mode.</br>Please watch the FSUIPC console for the \"Hello world...\" text.<br/></p>");
            //create requests and test them
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW, flagNum);
            testResult(result, LUA_PROGRAM_HELLOW);
            registerAndProcessRequests(result);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window. Also the log file should be generated for the script.");

            askTestResult("<p>Did the test starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaDebug method, of class LuaHelper.
     */
    @Test    
    public void testLuaDebug_3args() {
        System.out.println("JUnit test: luaDebug 3 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW, flagNum, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_HELLOW + "\" lua script with parameter: " + flagNum + " in debug mode.");

            showTestInfo("<p>This test tests the: LuaHelper.luaDebug(lua_program, parameter)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_HELLOW + " script with parameter: " + flagNum + " in debug mode.</br>Please watch the FSUIPC console for the \"Hello world...\" text.<br/></p>");

            //create requests and test them
            result = luaHelper.luaDebug(LUA_PROGRAM_HELLOW, flagNum, fsuipc);
            testResult(result, LUA_PROGRAM_HELLOW);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            System.out.println("Script should have execute. If it did, you should see the text: \"Hellow world. This script was run via FSUIPC Java SDK!\" in FSUIPC console window. Also the log file should be generated for the script.");

            askTestResult("<p>Did the test starting: \"Hellow world\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaKill method, of class LuaHelper.
     */
    @Test    
    public void testLuaKill_String() {
        System.out.println("JUnit test: luaKill 1 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaKill(LUA_PROGRAM_BS);
            testResult(result, LUA_PROGRAM_BS);
        } else {
            System.out.println("Connected to FSUIPC. Will run the " + LUA_PROGRAM_BS + " lua script. Than will wait 5 seconds and will try to kill it.");

            showTestInfo("<p>This test tests the: LuaHelper.luaKill(lua_program)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_BS + " script.</br>The script prints \"This is blocking script!\" every 750 ms to FSUIPC console window.<br/> "
                    + "After 5 seconds the script will be killed and the text should no longer appear.</p>");

            //create requests and run the script first
            result = luaHelper.lua(LUA_PROGRAM_BS);
            testResult(result, LUA_PROGRAM_BS);
            registerAndProcessRequests(result);

            System.out.println("Letting the script work for 5 seconds...");
            try {
                Thread.sleep(5000);
                //now we should read that flag to verify....            
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            result = luaHelper.luaKill(LUA_PROGRAM_BS);
            testResult(result, LUA_PROGRAM_BS);
            registerAndProcessRequests(result);

            askTestResult("<p>Has the text \"This is blocking script!\" been repeatedly print into the console and did it stop after approximately 5 seconds?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaKill method, of class LuaHelper.
     */
    @Test    
    public void testLuaKill_String_FSUIPC() {
        System.out.println("JUnit test: luaKill 2 args");

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaKill(LUA_PROGRAM_BS, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the " + LUA_PROGRAM_BS + " lua script. Than will wait 5 seconds and will try to kill it.");

            showTestInfo("<p>This test tests the: LuaHelper.luaKill(lua_program)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_BS + " script.</br>The script prints \"This is blocking script!\" every 750 ms to FSUIPC console window.<br/> "
                    + "After 5 seconds the script will be killed and the text should no longer appear.</p>");

            //create requests and run the script first
            result = luaHelper.lua(LUA_PROGRAM_BS);
            testResult(result, LUA_PROGRAM_BS);
            registerAndProcessRequests(result);

            System.out.println("Letting the script work for 5 seconds...");
            try {
                Thread.sleep(5000);
                //now we should read that flag to verify....            
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            result = luaHelper.luaKill(LUA_PROGRAM_BS, fsuipc);
            testResult(result, LUA_PROGRAM_BS);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            askTestResult("<p>Has the text \"This is blocking script!\" been repeatedly print into the console and did it stop after approximately 5 seconds?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaKillAll method, of class LuaHelper.
     */
    @Test    
    public void testLuaKillAll() {
        LuaHelper luaHelper = new LuaHelper();
        IDataRequest killAllRequest = luaHelper.luaKillAll();

        assertEquals(DataRequest.BUFFER_LENGTH_INT, killAllRequest.getSize());
        assertEquals(0x3110, killAllRequest.getOffset());

        try {
            killAllRequest.getValue();
            fail("Calling getValue on the luaKillAll request should cause UnsupportedOperationException as it is WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //Ok
        }
        
        if (fsuipc != null) {
            System.out.println("Connected to FSUIPC. Will run the " + LUA_PROGRAM_BS + " lua script. Than will wait 5 seconds and will call killAll.");

            showTestInfo("<p>This test tests the: LuaHelper.luaKillAll  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_BS + " script.</br>The script prints \"This is blocking script!\" every 750 ms to FSUIPC console window.<br/> "
                    + "After 5 seconds the script will be killed and the text should no longer appear.</p>");

            //create requests and run the script first
            LuaHelper.LuaResult result = luaHelper.lua(LUA_PROGRAM_BS);
            testResult(result, LUA_PROGRAM_BS);
            registerAndProcessRequests(result);

            System.out.println("Letting the script work for 5 seconds...");
            try {
                Thread.sleep(5000);
                //now we should read that flag to verify....            
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            fsuipc.addOneTimeRequest(killAllRequest);            
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);

            askTestResult("<p>Has the text \"This is blocking script!\" been repeatedly print into the console and did it stop after approximately 5 seconds?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
        }
    }

    /**
     * Test of luaSet method, of class LuaHelper.
     */
    @Test    
    public void testLuaSet_String_int() {
        System.out.println("JUnit test: luaSet 2 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaSet(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will set a flag and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaSet(lua_program, flag_num)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will set flag: " + flagNum + " for the script. This should result in text like:</br>"
                            + "\"Flag: " + flagNum + " value is now: true\". Than the script will be killed.</p>");
            
            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaSet(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Flag: " + flagNum + " value is now: true\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);     
        }
    }

    /**
     * Test of luaSet method, of class LuaHelper.
     */
    @Test    
    public void testLuaSet_3args() {
        System.out.println("JUnit test: luaSet 3 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaSet(LUA_PROGRAM_FP, flagNum, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will set a flag and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaSet(lua_program, flag_num, fsuipc)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will set flag: " + flagNum + " for the script. This should result in text like:</br>"
                            + "\"Flag: " + flagNum + " value is now: true\". Than the script will be killed.</p>");
            
            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaSet(LUA_PROGRAM_FP, flagNum, fsuipc);
            testResult(result, LUA_PROGRAM_FP);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
                       
            //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Flag: " + flagNum + " value is now: true\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);          
        }
    }

    /**
     * Test of luaClear method, of class LuaHelper.
     */
    @Test    
    public void testLuaClear_String_int() {
        System.out.println("JUnit test: luaClear 2 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaClear(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will clear a flag and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaClear(lua_program, flag_num)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will clear flag: " + flagNum + " for the script. This should result in text like:</br>"
                            + "\"Flag: " + flagNum + " value is now: false\". Than the script will be killed.</p>");

            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            //create requests and test them
            result = luaHelper.luaClear(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Flag: " + flagNum + " value is now: false\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);                  
        }
    }

    /**
     * Test of luaClear method, of class LuaHelper.
     */
    @Test    
    public void testLuaClear_3args() {
        System.out.println("JUnit test: luaClear 3 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaClear(LUA_PROGRAM_FP, flagNum, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will clear a flag and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaClear(lua_program, flag_num, fsuipc)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will clear flag: " + flagNum + " for the script. This should result in text like:</br>"
                            + "\"Flag: " + flagNum + " value is now: false\". Than the script will be killed.</p>");

            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaClear(LUA_PROGRAM_FP, flagNum, fsuipc);
            testResult(result, LUA_PROGRAM_FP);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Flag: " + flagNum + " value is now: false\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);                    
        }
    }

    /**
     * Test of luaToggle method, of class LuaHelper.
     */
    @Test    
    public void testLuaToggle_String_int() {
        System.out.println("JUnit test: luaToggle 2 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaToggle(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will toggle a flag and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaToggle(lua_program, flag_num)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will toggle flag: " + flagNum + " for the script. This should result in text like:</br>"
                            + "\"Flag: " + flagNum + " value is now: true or false\". Than the script will be killed.</p>");

            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaToggle(LUA_PROGRAM_FP, flagNum);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

             //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Flag: " + flagNum + " value is now: true or false\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);         
        }
    }

    /**
     * Test of luaToggle method, of class LuaHelper.
     */
    @Test    
    public void testLuaToggle_3args() {
        System.out.println("JUnit test: luaToggle 3 args");
        final int flagNum = (int) Math.ceil(Math.random() * 255);

        LuaHelper luaHelper = new LuaHelper();
        LuaHelper.LuaResult result = null;

        if (fsuipc == null) {
            System.out.println("Not connected to FSUIPC. Will perform only test of created requests!");
            //test all variant with fsuipc null
            result = luaHelper.luaToggle(LUA_PROGRAM_FP, flagNum, fsuipc);
            assertNull(result);
        } else {
            System.out.println("Connected to FSUIPC. Will run the \"" + LUA_PROGRAM_FP + "\" lua script. Than will toggle a flag and then will kill the script.");
            
            showTestInfo("<p>This test tests the: LuaHelper.luaToggle(lua_program, flag_num, fsuipc)  function</p>"
                    + "<p>This test will try to run the: " + LUA_PROGRAM_FP + " script.</br>The text: \"*** Flag/Param test lua script started ***\" will appear in FSUIPC console.<br/> "
                    + "Than will toggle flag: " + flagNum + " for the script. This should result in text like:</br>"
                            + "\"Flag: " + flagNum + " value is now: true or false\". Than the script will be killed.</p>");

            //start the script
            result = luaHelper.lua(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //create requests and test them
            result = luaHelper.luaToggle(LUA_PROGRAM_FP, flagNum, fsuipc);
            testResult(result, LUA_PROGRAM_FP);
            int iRet = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LuaHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            }

             //kill the script
            result = luaHelper.luaKill(LUA_PROGRAM_FP);
            testResult(result, LUA_PROGRAM_FP);
            registerAndProcessRequests(result);
            
            askTestResult("<p>Did the string \"Flag: " + flagNum + " value is now: true or false\" appear in the FSUIPC console window?</p>", JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);        
        }
    }

    @Test
    public void testParamRequestClass() {
        LuaHelper.LuaParamRequest paramRequest;
        final int controlNum = (int) (Math.random() * 100);

        System.out.println("JUnit test: Test LuaParamRequest class.");

        try {
            paramRequest = new LuaHelper.LuaParamRequest(null);
            fail("Passing null as parameter for LuaParamRequest constructor should cause NullPointerException");
        } catch (NullPointerException ex) {
            //Ok
        }

        try {
            paramRequest = new LuaHelper.LuaParamRequest(controlNum);
            paramRequest.setValue(controlNum + 10);
        } catch (Exception ex) {
            fail("Setting the value: " + controlNum + " as parameter for LuaParamRequest.setValue() should not cause any Exception");
        }

        try {
            paramRequest = new LuaHelper.LuaParamRequest(controlNum);
            paramRequest.getValue();
            fail("The LuaParamRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }

        paramRequest = new LuaHelper.LuaParamRequest(controlNum);
        assertEquals(LuaHelper.PARAMETER_OFFSET, paramRequest.getOffset());
        assertEquals(DataRequest.BUFFER_LENGTH_INT, paramRequest.getSize());
    }

    @Test
    public void testControlRequestClass() {
        LuaHelper.LuaControlRequest controlRequest;

        System.out.println("JUnit test: Test LuaControlRequest class.");

        try {
            controlRequest = new LuaHelper.LuaControlRequest(LUA_PROGRAM_HELLOW);
            controlRequest.getValue();
            fail("The LuaControlRequest.getValue() should cause the UnsupportedOperationException as it is the WRITE ONLY request!");
        } catch (UnsupportedOperationException ex) {
            //OK
        }

        //Try the constructors and setting the values, than verify that the constructed String is correct
        controlRequest = new LuaHelper.LuaControlRequest(LUA_PROGRAM_HELLOW);
        //it should be read by default
        Charset controlRequestCharset = controlRequest.getCharset();
        String strValue = new String(controlRequest.getDataBuffer(), controlRequestCharset).trim();
        String compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_HELLOW).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.CONTROL_OFFSET, controlRequest.getOffset());

        //try change luaProgram
        controlRequest.setValue(LUA_PROGRAM_FP);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);

        //try change command to lua set
        controlRequest.setCommand(LuaHelper.LuaControlRequestCommand.LUA_SET);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_SET.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_SET, controlRequest.getCommand());

        //try change command to lua toggle
        controlRequest.setCommand(LuaHelper.LuaControlRequestCommand.LUA_TOGGLE);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_TOGGLE.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_TOGGLE, controlRequest.getCommand());

        //try change command to lua clear
        controlRequest.setCommand(LuaHelper.LuaControlRequestCommand.LUA_CLEAR);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_CLEAR.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_CLEAR, controlRequest.getCommand());

        //lua debuf command
        controlRequest.setCommand(LuaHelper.LuaControlRequestCommand.LUA_DEBUG);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_DEBUG.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_DEBUG, controlRequest.getCommand());

        //try change command to lua clear
        controlRequest.setCommand(LuaHelper.LuaControlRequestCommand.LUA_KILL);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_KILL.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_KILL, controlRequest.getCommand());

        //try change command to lua clear
        controlRequest.setCommand(LuaHelper.LuaControlRequestCommand.LUA_VALUE);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_VALUE.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_VALUE, controlRequest.getCommand());

        //Try another constructors
        controlRequest = new LuaHelper.LuaControlRequest(LuaHelper.LuaControlRequestCommand.LUA, LUA_PROGRAM_HELLOW);
        strValue = new String(controlRequest.getDataBuffer(), controlRequest.getCharset()).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_HELLOW).getBytes(controlRequestCharset), controlRequestCharset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA, controlRequest.getCommand());
        assertEquals(LuaHelper.CONTROL_OFFSET, controlRequest.getOffset());

        Charset charset = Charset.forName("UTF-8");
        controlRequest = new LuaHelper.LuaControlRequest(LUA_PROGRAM_FP, charset);
        strValue = new String(controlRequest.getDataBuffer(), charset).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(charset), charset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA, controlRequest.getCommand());
        assertEquals(LuaHelper.CONTROL_OFFSET, controlRequest.getOffset());
        assertEquals(charset, controlRequest.getCharset());

        controlRequest = new LuaHelper.LuaControlRequest(LuaHelper.LuaControlRequestCommand.LUA_KILL, LUA_PROGRAM_FP, charset);
        strValue = new String(controlRequest.getDataBuffer(), charset).trim();
        compareValue = new String((LuaHelper.LuaControlRequestCommand.LUA_KILL.getValue() + LuaControlRequest.COMMAND_SEPARATOR + LUA_PROGRAM_FP).getBytes(charset), charset).trim();
        assertEquals(compareValue, strValue);
        assertEquals(LuaHelper.LuaControlRequestCommand.LUA_KILL, controlRequest.getCommand());
        assertEquals(LuaHelper.CONTROL_OFFSET, controlRequest.getOffset());
        assertEquals(charset, controlRequest.getCharset());
    }

    private void testResult(LuaHelper.LuaResult result, String luaProgram) {
        //test killAllRequest not null, param request corrext buffer length and offset
        assertNotNull(result);
        assertEquals(LUA_REQUEST_PARAM_OFFSET, result.getParamRequest().getOffset());
        assertEquals(DataRequest.BUFFER_LENGTH_INT, result.getParamRequest().getDataBuffer().length);

        assertEquals(LUA_CONTROL_OFFSET, result.getControlRequest().getOffset());

        if (luaProgram != null) {
            //test that command string is ok
            Charset charset = result.getControlRequest().getCharset();
            String controlValue = new String(result.getControlRequest().getDataBuffer(), charset).trim();
            String compareValue = new String((result.getControlRequest().getCommand().getValue() + LuaHelper.LuaControlRequest.COMMAND_SEPARATOR + luaProgram).getBytes(charset), charset).trim();
            assertEquals(compareValue, controlValue);
        }
    }

    private void registerAndProcessRequests(LuaHelper.LuaResult result) {
        fsuipc.addOneTimeRequest(result.getParamRequest());
        fsuipc.addOneTimeRequest(result.getControlRequest());

        int iRet = fsuipc.processRequestsOnce();
        assertEquals(FSUIPC.PROCESS_RESULT_OK, iRet);
    }
}
