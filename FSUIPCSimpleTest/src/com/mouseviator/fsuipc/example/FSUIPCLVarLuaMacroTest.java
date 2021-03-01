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
package com.mouseviator.fsuipc.example;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.datarequest.primitives.StringRequest;
import com.mouseviator.fsuipc.helpers.LVarHelper;
import com.mouseviator.fsuipc.helpers.LuaHelper;
import com.mouseviator.fsuipc.helpers.MacroHelper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This si simple test program for methods and functions in the {@link LVarHelper}, {@link LuaHelper} and
 * {@link MacroHelper}. To properly test this, you should run your simulator with FSUIPC console window opened, so you
 * can see if respective actions done by this program recall the expected actions within FSUIPC.
 *
 * You should find all the used lua scripts and macros inside the lua_macro folder of this project.
 * 
 * @author Mouseviator
 */
public class FSUIPCLVarLuaMacroTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //1) get FSUIPC instance
        FSUIPC fsuipc = FSUIPC.getInstance();

        //2) First of all, load the native library. The default load function will try to determine if we are running under 32 or 64 bit JVM
        // and load 32/64 bit native library respectively
        byte result = FSUIPC.load();
        if (result != FSUIPC.LIB_LOAD_RESULT_OK) {
            System.out.println("Failed to load native library. Quiting...");
            return;
        }

        //enable fsuipc library file logging
        FSUIPCWrapper.setupLogging(true, FSUIPCWrapper.LogSeverity.DEBUG.getValue());

        System.out.println("Running tests...");
        int ret = fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
        System.out.println("FSUIPC connect return value =" + ret);
        if (ret == 0) {
            System.out.println("Flight sim not found");
        } else {
            System.out.println("Flight Sim found!");

            System.out.println("FSUIPC Version: " + String.valueOf(FSUIPCWrapper.getVersion()));
            System.out.println("FSUIPC Version (string): " + fsuipc.getVersion());
            System.out.println("FSUIPC FS Version: " + String.valueOf(FSUIPCWrapper.getFSVersion()));
            System.out.println("FSUIPC FS Version (string): " + fsuipc.getFSVersion());
            System.out.println("FSUIPC Lib Version: " + String.valueOf(FSUIPCWrapper.getLibVersion()));
            System.out.println("FSUIPC Lib Version (string): " + fsuipc.getLibVersion());

            //Helepers to create data requests
            final int LVAR_REQUEST_OFFSET = 0x0D70;
            final int LVAR_REQUEST_PARAM_OFFSET = 0x0D6C;
            final int LVAR_RESULT_OFFSET = 0x66C0;
            final String lvar1 = "FSDT_GSX_DEBOARDING_STATE";
            final String lvar2 = "FSDT_GSX_BOARDING_STATE";
            final String lvar3 = "FSDT_GSX_NUMPASSENGERS";
            final String customLvar = "MOUSEVIATOR_CUSTOM_LVAR";

            /**
             * Non-helper way of reading an LVar
             */
            System.out.println("\n\n>>> LVar Tests...");
            System.out.println("Will read value for LVar : " + lvar1 + " using standard approach and value for LVar: " + lvar2 + " using helper functions...");

            IntRequest integer = new IntRequest(LVAR_REQUEST_PARAM_OFFSET);
            integer.setValue(0x066C0);
            integer.setType(IDataRequest.RequestType.WRITE);
            fsuipc.addOneTimeRequest(integer);

            StringRequest var = new StringRequest(LVAR_REQUEST_OFFSET, ":" + lvar1);
            fsuipc.addOneTimeRequest(var);

            DoubleRequest erg = new DoubleRequest(LVAR_RESULT_OFFSET);
            //not necessary. All requests are read by default, unless constructor specifies otherwise
            //erg.setType(IDataRequest.RequestType.READ);
            fsuipc.addOneTimeRequest(erg);

            /**
             * Reading LVar using helper
             */
            LVarHelper lvarHelper = new LVarHelper();

            LVarHelper.LVarResult lvarreq = lvarHelper.readLVar(lvar2, 0x66C4, LVarHelper.LVarValueFormat.UNSIGNED_BYTE, fsuipc, false);

            //Actual processing of the read LVar requests
            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("Read Okay");
                System.out.println("The value of lvar: " + lvar1 + " is: " + erg.getValue());
                System.out.println("The value of lvar: " + lvar2 + " is: " + lvarreq.getResultRequest().getValue());
            }

            /**
             * Writing and creating LVar using helper
             */
            System.out.println("Will write value to LVar: " + lvar3 + " and will create custom LVar: " + customLvar);

            //try to write num of passangers to gsx
            lvarHelper.writeLVar(lvar3, 0x66C0, (short) 124, LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc, false);
            //create custom lvar
            lvarHelper.createLVar(customLvar, 0x66C2, (int) 84, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc);
            //process the requests
            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("Write/Create Okay");
            }

            /**
             * Reading back the value of written/created LVar
             */
            //construct the read requests
            System.out.println("Will read back the value od LVar: " + lvar3 + " and custom created LVar: " + customLvar);
            lvarreq = lvarHelper.readLVar(lvar3, 0x66C0, LVarHelper.LVarValueFormat.UNSIGNED_SHORT, fsuipc, false);
            LVarHelper.LVarResult lvarreq2 = lvarHelper.readLVar(customLvar, 0x66C2, LVarHelper.LVarValueFormat.SIGNED_INTEGER, fsuipc, false);
            //process the requests
            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("Read Okay");
                System.out.println("The value of lvar: " + lvar3 + " is: " + lvarreq.getResultRequest().getValue());
                System.out.println("The value of lvar: " + customLvar + " is: " + lvarreq2.getResultRequest().getValue());
            }

            System.out.println("\n\n>>> Lua Tests...");
            /**
             * Running lua test
             */
            LuaHelper luaHelper = new LuaHelper();

            //hello wordls cript
            System.out.println("Will try to run the \"hello world.lua\" and \"blocking script.lua\"");
            LuaHelper.LuaResult helloworldreq = luaHelper.lua("hello world");
            fsuipc.addOneTimeRequest(helloworldreq.getParamRequest());
            fsuipc.addOneTimeRequest(helloworldreq.getControlRequest());

            //blocking script
            LuaHelper.LuaResult blockingscrreq = luaHelper.lua("blocking script", fsuipc);

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("The \"hello world.lua\" should have run.");
                System.out.println("The \"blocking script.lua\" should be running.");
            }

            System.out.println("Will sleep for 5 seconds now...");
            //wait 5 seconds
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FSUIPCLVarLuaMacroTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            /**
             * try to kill the blocking script
             */
            System.out.println("Will try to to kill the \"blocking script.lua\"");
            LuaHelper.LuaResult blockingkillreq = luaHelper.luaKill("blocking script");
            fsuipc.addOneTimeRequest(blockingkillreq.getParamRequest());
            fsuipc.addOneTimeRequest(blockingkillreq.getControlRequest());

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("The \"blocking script.lua\" script should have been terminated!");
            }

            /**
             * Running param and flag test
             */
            System.out.println("Will try to run the \"flag_param.lua\" for testing the sending of param and setting the flags...");
            final String LUA_FLAG_PARAM_PROGRAM = "flag_param";
            LuaHelper.LuaResult paramscrreq = luaHelper.lua(LUA_FLAG_PARAM_PROGRAM, 25);
            fsuipc.addOneTimeRequest(paramscrreq.getParamRequest());
            fsuipc.addOneTimeRequest(paramscrreq.getControlRequest());

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("The \"flag_param.lua\" should be running...");

                int flag = 10;

                //Test set flag
                System.out.println("Will try to set flag: " + flag + " for program: " + LUA_FLAG_PARAM_PROGRAM);
                LuaHelper.LuaResult flagreq = luaHelper.luaSet(LUA_FLAG_PARAM_PROGRAM, flag);
                fsuipc.addOneTimeRequest(flagreq.getParamRequest());
                fsuipc.addOneTimeRequest(flagreq.getControlRequest());

                if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                    System.out.println("Flag " + flag + " should be set for program: " + LUA_FLAG_PARAM_PROGRAM);
                }

                //Test clear flag
                System.out.println("Will try to clear flag: " + flag + " for program: " + LUA_FLAG_PARAM_PROGRAM);
                flagreq = luaHelper.luaClear(LUA_FLAG_PARAM_PROGRAM, flag);
                fsuipc.addOneTimeRequest(flagreq.getParamRequest());
                fsuipc.addOneTimeRequest(flagreq.getControlRequest());

                if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                    System.out.println("Flag " + flag + " should be cleared for program: " + LUA_FLAG_PARAM_PROGRAM);
                }

                //Test toggle flag
                System.out.println("Will try to toggle flag: " + flag + " for program: " + LUA_FLAG_PARAM_PROGRAM);
                flagreq = luaHelper.luaToggle(LUA_FLAG_PARAM_PROGRAM, flag, fsuipc);

                if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                    System.out.println("Flag " + flag + " should be toggled for program: " + LUA_FLAG_PARAM_PROGRAM);
                }

                //Test toggle flag
                System.out.println("Will try to send param of value: 40 to the program: " + LUA_FLAG_PARAM_PROGRAM);
                LuaHelper.LuaResult paramreq = luaHelper.luaValue(LUA_FLAG_PARAM_PROGRAM, 40);
                fsuipc.addOneTimeRequest(paramreq.getParamRequest());
                fsuipc.addOneTimeRequest(paramreq.getControlRequest());

                if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                    System.out.println("Parameter should be 40 for program: " + LUA_FLAG_PARAM_PROGRAM);
                }
            }

            /**
             * Kill all scripts test
             */
            IDataRequest killallreq = luaHelper.luaKillAll();
            fsuipc.addOneTimeRequest(killallreq);

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("All scripts should be terminated!");
            }

            
            //macro tests
            System.out.println("\n\n>>> Macro Tests....");

            MacroHelper macroHelper = new MacroHelper();

            System.out.println("Trying to execute macro: \"fsuipctest:PauseToggle\" which should toggle the pause in the sim.");
            MacroHelper.MacroResult macroreq = macroHelper.executeMacro("fsuipctest", "PauseToggle");
            fsuipc.addOneTimeRequest(macroreq.getParamRequest());
            fsuipc.addOneTimeRequest(macroreq.getMacroExecuteRequest());

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("The macro: \"fsuipctest:PauseToggle\" should have execute. The sim pause should toggle.");
            }
            
            //try macro with parameter - unset parking brake
            System.out.println("Trying to execute macro: \"fsuipctest:ParkBrake\" with param 0, which should unset parking brake.");
            macroreq = macroHelper.executeMacro("fsuipctest:ParkBrake", 0, fsuipc, false);

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("The macro: \"fsuipctest:ParkBrake\" should have execute. The parking brake should be unset.");
            }
            
            //try macro with parameter - set parking brake
            System.out.println("Trying to execute macro: \"fsuipctest:ParkBrake\" with param 1, which should set parking brake.");
            macroreq = macroHelper.executeMacro("fsuipctest:ParkBrake", 1);
            fsuipc.addOneTimeRequest(macroreq.getParamRequest());
            fsuipc.addOneTimeRequest(macroreq.getMacroExecuteRequest());

            if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("The macro: \"fsuipctest:ParkBrake\" should have execute. The parking brake should be set.");
            }                        

            //Disconnect FSUIPC and free used resources
            fsuipc.disconnect();
            System.out.println("FSUIPC disconnected. Good bye.");
        }
    }
}
