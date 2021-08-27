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
import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType;
import com.mouseviator.fsuipc.datarequest.IWriteOnlyRequest;
import com.mouseviator.fsuipc.helpers.LVarHelper.LVarControlRequestCommand;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * This class implements a helper to manipulate Lua scripts using FSUIPC. </p>
 *
 * <p>
 * Manipulating Lua via FSUIPC requires 2 data requests, because it works with 2 offsets. Many functions in this class
 * return both of them in an instance of {@link LuaResult}. The main offset in use is 0D70, in which you write a string
 * telling FSUIPC what to do with what lua script. I call it here a "control request" and it is the {@link LuaResult#getControlRequest()
 * }. The next offset is 0D6C and I reference it as "parameter request" here. Because it is the parameter for lua script
 * / operation with the lua script. You will find this request in the result object as {@link LuaResult#getParamRequest()
 * }.
 *
 *
 * <p>
 * There are various functions for all the things we can do with the lua programs via FSUIPC - start them, kill them,
 * send parameter to them and set,clear or toggle one of the 255 available flags.
 *
 * <p>
 * Some functions also accepts an instance of {@link FSUIPC} class. These functions will also register the generated
 * requests with given FSUIPC instance for one-time processing</p>
 *
 * <h2>Running a lua program example</h2>
 *
 * <p>
 * The code below uses helper function to build requests to run the "hello world.lua" program. The helper function will
 * build the required data requests and we just register them for processing and process them. Whether it worked or not
 * needs to be looked in the sim - in the FSUIPC console window or FSUIPC log file. There should be line saying Hello
 * world....
 * </p>
 *
 * <pre><code>
 * //
 * // Running lua test
 * //
 * LuaHelper luaHelper = new LuaHelper();
 *
 * //hello wordl script
 * LuaHelper.LuaResult helloworldreq = luaHelper.lua("hello world");
 * fsuipc.addOneTimeRequest(helloworldreq.getParamRequest());
 * fsuipc.addOneTimeRequest(helloworldreq.getControlRequest());
 *
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *    System.out.println("The requested script should have been run!");
 * }
 * </code></pre>
 *
 * <p>
 * or shorter way: </p>
 *
 * <pre><code>
 * LuaHelper luaHelper = new LuaHelper();
 *
 * LuaHelper.LuaResult helloworldreq = luaHelper.lua("hello world", fsuipc);
 *
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *    System.out.println("The requested script should have been run!");
 * }
 * </code></pre>
 *
 * <p>
 * The other operations - killing a lua program, sending a param to lua program or manipulation lua program flags,
 * follow the same logic. The only exception is the {@link LuaHelper#luaKillAll() } function, which return only one data
 * request and works like most of the functions in other helper classes. You just need to register the returned request
 * for one-time of continual processing (well, that actually makes no sense) with FSUIPC instance. If you use the
 * function that that registers the requests with FSUIPC, you may not need the resulting object at all. Unless you
 * registered them for continual processing and need to remove them from processing later on.</p>
 *
 * <h3>Sending parameter to lua program</h3>
 *
 * <p>
 * Ok, one more example using the short way:</p>
 *
 * <pre><code>
 * LuaHelper luaHelper = new LuaHelper();
 *
 * LuaHelper.LuaResult helloworldreq = luaHelper.luaValue("flag_param", 40, fsuipc);
 *
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *    System.out.println("Parameter should be 40 for program: flag_param.lua");
 * }
 * </code></pre>
 *
 * <p>
 * If you will be registering the requests yourself, make sure that you register them in this order</p>
 * <ul><li>1. Parameter request ({@link LuaResult#getParamRequest() })</li>
 * <li>2. Control request ({@link LuaResult#getControlRequest() })</li>
 * </ul>
 *
 * <p>
 * You can also skip the helper functions and use the most raw function to create the requests: {@link LuaHelper#luaRequest(java.lang.String, int, com.mouseviator.fsuipc.helpers.LuaHelper.LuaControlRequestCommand)
 * } or
 * {@link LuaHelper#luaRequest(java.lang.String, int, com.mouseviator.fsuipc.helpers.LuaHelper.LuaControlRequestCommand, com.mouseviator.fsuipc.FSUIPC, boolean) }.</p>
 *
 * <p>
 * Or even use request and other supporting classes defined in this helper class and write the create function by
 * yourself.</p>
 *
 * @author Mouseviator
 */
public class LuaHelper {
    /**
     * Offset for parameter passing for the control offset below
     */
    public static final int PARAMETER_OFFSET = 0x0D6C;

    /**
     * Offset to tell FSUIPC what to do with lua script, LVar or Macro
     */
    public static final int CONTROL_OFFSET = 0x0D70;
    
    /**
     * This function will construct data requests required by FSUIPC to perform given operation with given lua program.
     * Any lua program operation requires 2 data requests to be created. One is parameter for lua program or flag being
     * modified by the lua request. The other is the control request, which tells FSUIPC what to do with specified lua
     * program.
     *
     * @param luaProgram The name of the lua program. Without the .lua extension.
     * @param luaParam Parameter for the lua program or the number of the flag (0-255) being modified.
     * @param luaCommand What to do with the lua program.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaRequest(String luaProgram, int luaParam, LuaControlRequestCommand luaCommand) {
        LuaResult result = null;

        //create zero param request
        LuaParamRequest paramRequest = new LuaParamRequest(luaParam);

        //create execute request
        LuaControlRequest controlRequest;
        try {
            controlRequest = new LuaControlRequest(luaCommand, luaProgram);

            result = new LuaResult(controlRequest, paramRequest);
        } catch (Exception ex) {
            Logger.getLogger(MacroHelper.class.getName()).log(Level.SEVERE, "Failed to create Lua request!", ex);
        }

        return result;
    }

    /**
     * This function will construct data requests required by FSUIPC to perform given operation with given lua program.
     * Any lua program operation requires 2 data requests to be created. One is parameter for lua program or flag being
     * modified by the lua request. The other is the control request, which tells FSUIPC what to do with specified lua
     * program.
     *
     * @param luaProgram The name of the lua program. Without the .lua extension.
     * @param luaParam Parameter for the lua program or the number of the flag (0-255) being modified.
     * @param luaCommand What to do with the lua program.
     * @param fsuipc An instance of {@link FSUIPC} to register requests with.
     * @param continual Whether to register requests for continual processing or one-time processing. True for
     * continual.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null will be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaRequest(String luaProgram, int luaParam, LuaControlRequestCommand luaCommand, FSUIPC fsuipc, boolean continual) {
        LuaResult result = null;
        //quit if no fsuipc
        if (fsuipc == null) {
            return null;
        }

        //create zero param request
        LuaParamRequest paramRequest = new LuaParamRequest(luaParam);

        //create execute request
        LuaControlRequest controlRequest;
        try {
            controlRequest = new LuaControlRequest(luaCommand, luaProgram);

            result = new LuaResult(controlRequest, paramRequest);

            if (continual) {
                fsuipc.addContinualRequest(paramRequest);
                fsuipc.addContinualRequest(controlRequest);
            } else {
                fsuipc.addOneTimeRequest(paramRequest);
                fsuipc.addOneTimeRequest(controlRequest);
            }
        } catch (Exception ex) {
            Logger.getLogger(MacroHelper.class.getName()).log(Level.SEVERE, "Failed to create Lua request!", ex);
        }

        return result;
    }

    /**
     * This function will construct data requests required by FSUIPC to run specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA} command.
     *
     * @param luaProgram The name of the lua program to run. Without the .lua extension.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult lua(String luaProgram) {
        return luaRequest(luaProgram, 0, LuaControlRequestCommand.LUA);
    }

    /**
     * This function will construct data requests required by FSUIPC to run specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA} command. The function will also register the requests with FSUIPC for
     * one-time processing.
     *
     * @param luaProgram The name of the lua program to run. Without the .lua extension.
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult lua(String luaProgram, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, 0, LuaControlRequestCommand.LUA);
    }

    /**
     * This function will construct data requests required by FSUIPC to run specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA} command.
     *
     * @param luaProgram The name of the lua program to run. Without the .lua extension.
     * @param luaParam Parameter for the lua program.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult lua(String luaProgram, int luaParam) {
        return luaRequest(luaProgram, luaParam, LuaControlRequestCommand.LUA);
    }

    /**
     * This function will construct data requests required by FSUIPC to run specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA} command. The function will also register the requests with FSUIPC for
     * one-time processing.
     *
     * @param luaProgram The name of the lua program to run. Without the .lua extension.
     * @param luaParam Parameter for the lua program.
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult lua(String luaProgram, int luaParam, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, luaParam, LuaControlRequestCommand.LUA);
    }

    /**
     * This function will construct data requests required by FSUIPC to send parameter to specified lua program
     * (Performs the {@link LuaControlRequestCommand#LUA_VALUE} command. Only integer parameters are supported (because
     * FSUIPC simply expects 4 byte integer as the parameter).
     *
     * @param luaProgram The name of the lua program to send parameter to. Without the .lua extension.
     * @param luaParam Parameter for the lua program.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaValue(String luaProgram, int luaParam) {
        return luaRequest(luaProgram, luaParam, LuaControlRequestCommand.LUA_VALUE);
    }

    /**
     * This function will construct data requests required by FSUIPC to send parameter to specified lua program
     * (Performs the {@link LuaControlRequestCommand#LUA_VALUE} command. Only integer parameters are supported (because
     * FSUIPC simply expects 4 byte integer as the parameter). The function will also register the requests with FSUIPC
     * for one-time processing.
     *
     * @param luaProgram The name of the lua program to send parameter to. Without the .lua extension.
     * @param luaParam Parameter for the lua program.
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaValue(String luaProgram, int luaParam, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, luaParam, LuaControlRequestCommand.LUA_VALUE);
    }

    /**
     * This function will construct data requests required by FSUIPC to run and debug specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_DEBUG} command.
     *
     * @param luaProgram The name of the lua program to run and debug. Without the .lua extension.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaDebug(String luaProgram) {
        return luaRequest(luaProgram, 0, LuaControlRequestCommand.LUA_DEBUG);
    }

    /**
     * This function will construct data requests required by FSUIPC to run and debug specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_DEBUG} command. The function will also register the requests with FSUIPC
     * for one-time processing.
     *
     * @param luaProgram The name of the lua program to run and debug. Without the .lua extension.
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaDebug(String luaProgram, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, 0, LuaControlRequestCommand.LUA_DEBUG);
    }

    /**
     * This function will construct data requests required by FSUIPC to run and debug specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_DEBUG} command.
     *
     * @param luaProgram The name of the lua program to run and debug. Without the .lua extension.
     * @param luaParam Parameter for the lua program or the number of the flag (0-255) being modified.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaDebug(String luaProgram, int luaParam) {
        return luaRequest(luaProgram, luaParam, LuaControlRequestCommand.LUA_DEBUG);
    }

    /**
     * This function will construct data requests required by FSUIPC to run and debug specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_DEBUG} command. The function will also register the requests with FSUIPC
     * for one-time processing.
     *
     * @param luaProgram The name of the lua program to run and debug. Without the .lua extension.
     * @param luaParam Parameter for the lua program or the number of the flag (0-255) being modified.
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaDebug(String luaProgram, int luaParam, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, luaParam, LuaControlRequestCommand.LUA_DEBUG);
    }

    /**
     * This function will construct data requests required by FSUIPC to kill specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA_KILL} command.
     *
     * @param luaProgram The name of the lua program to kill. Without the .lua extension.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaKill(String luaProgram) {
        return luaRequest(luaProgram, 0, LuaControlRequestCommand.LUA_KILL);
    }

    /**
     * This function will construct data requests required by FSUIPC to kill specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA_KILL} command. The function will also register the requests with FSUIPC for
     * one-time processing.
     *
     * @param luaProgram The name of the lua program to kill. Without the .lua extension.
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaKill(String luaProgram, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, 0, LuaControlRequestCommand.LUA_KILL);
    }

    /**
     * This function will generate request to send FSUIPC control to kill all running lua programs. NOTE that this is
     * the only function in this helper that does return only one request!
     *
     * @return A data request to kill all running lua programs.
     */
    public IDataRequest luaKillAll() {
        class LuaKillAllRequest extends DataRequest implements IWriteOnlyRequest<Integer> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                putInt(1084);          //LuaKillAll control
                this.offset = 0x3110;   //send fs control offset
                this.type = RequestType.WRITE;
            }

            @Override
            public void setValue(Integer value) {
                putInt(value);
            }

        }
        return new LuaKillAllRequest();
    }

    /**
     * This function will construct data requests required by FSUIPC to set flag for specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA_SET} command.
     *
     * @param luaProgram The name of the lua program to set flag for. Without the .lua extension.
     * @param luaFlag A flag (0-255) to set for specified lua program. (Note that validity of this value is not
     * checked!)
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaSet(String luaProgram, int luaFlag) {
        return luaRequest(luaProgram, luaFlag, LuaControlRequestCommand.LUA_SET);
    }

    /**
     * This function will construct data requests required by FSUIPC to set flag for specified lua program (Performs the
     * {@link LuaControlRequestCommand#LUA_SET} command. The function will also register the requests with FSUIPC for
     * one-time processing.
     *
     * @param luaProgram The name of the lua program to set flag for. Without the .lua extension.
     * @param luaFlag A flag (0-255) to set for specified lua program. (Note that validity of this value is not
     * checked!)
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaSet(String luaProgram, int luaFlag, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, luaFlag, LuaControlRequestCommand.LUA_SET);
    }

    /**
     * This function will construct data requests required by FSUIPC to clear flag for specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_CLEAR} command.
     *
     * @param luaProgram The name of the lua program to clear flag for. Without the .lua extension.
     * @param luaFlag A flag (0-255) to clear for specified lua program. (Note that validity of this value is not
     * checked!)
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaClear(String luaProgram, int luaFlag) {
        return luaRequest(luaProgram, luaFlag, LuaControlRequestCommand.LUA_CLEAR);
    }

    /**
     * This function will construct data requests required by FSUIPC to clear flag for specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_CLEAR} command. The function will also register the requests with FSUIPC
     * for one-time processing.
     *
     * @param luaProgram The name of the lua program to clear flag for. Without the .lua extension.
     * @param luaFlag A flag (0-255) to clear for specified lua program. (Note that validity of this value is not
     * checked!)
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaClear(String luaProgram, int luaFlag, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, luaFlag, LuaControlRequestCommand.LUA_CLEAR);
    }

    /**
     * This function will construct data requests required by FSUIPC to toggle flag for specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_SET} command. The function will also register the requests with FSUIPC
     * for one-time processing.
     *
     * @param luaProgram The name of the lua program to toggle flag for. Without the .lua extension.
     * @param luaFlag A flag (0-255) to toggle for specified lua program. (Note that validity of this value is not
     * checked!)
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned.
     */
    public LuaResult luaToggle(String luaProgram, int luaFlag) {
        return luaRequest(luaProgram, luaFlag, LuaControlRequestCommand.LUA_TOGGLE);
    }

    /**
     * This function will construct data requests required by FSUIPC to toggle flag for specified lua program (Performs
     * the {@link LuaControlRequestCommand#LUA_SET} command.
     *
     * @param luaProgram The name of the lua program to toggle flag for. Without the .lua extension.
     * @param luaFlag A flag (0-255) to toggle for specified lua program. (Note that validity of this value is not
     * checked!)
     * @param fsuipc An instance of {@link FSUIPC} to register with for one time processing.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    public LuaResult luaToggle(String luaProgram, int luaFlag, FSUIPC fsuipc) {
        return runLuaCommandOnce(fsuipc, luaProgram, luaFlag, LuaControlRequestCommand.LUA_TOGGLE);
    }

    /**
     * A helper function to no-repeat code. This one is actually an implementation of all function that take FSUIPC
     * instance as argument.
     *
     * @param fsuipc An instance of FSUIPC.
     * @param luaProgram Lua program to run or modify by this command.
     * @param luaParam Lua program parameter or flag number.
     * @param command A command to run.
     * @return Will return an instance of {@link LuaResult} when both required data requests are successfully created.
     * The creation of control request may fail if the <code>luaProgram</code> is null or blank. In that case, null will
     * be returned. Null we be returned also if <code>fsuipc</code> is null.
     */
    private LuaResult runLuaCommandOnce(FSUIPC fsuipc, String luaProgram, int luaParam, LuaControlRequestCommand command) {
        if (fsuipc == null) {
            return null;
        }

        LuaResult result = luaRequest(luaProgram, luaParam, command);
        if (result != null) {
            fsuipc.addOneTimeRequest(result.getParamRequest());
            fsuipc.addOneTimeRequest(result.getControlRequest());
        }

        return result;
    }

    /**
     * This class implements Lua control request. It is a WRITE type request, writing a string at offset 0x0D70. This
     * string than identifies what should FSUIPC do with specified lua program. By default the control request will tell
     * FSUIPC to execute the given lua program. The lua program name can be set via constructor or via the {@link #setValue(java.lang.String)
     * } method. The command - what to do, is being set by constructor or by {@link #setCommand(com.mouseviator.fsuipc.helpers.LVarHelper.LuaControlRequestCommand)
     * } method.
     */
    public static class LuaControlRequest extends DataRequest implements IWriteOnlyRequest<String> {

        /**
         * This character si used to separate command and arguments when working with lua programs via FSUIPC
         */
        public static final String COMMAND_SEPARATOR = ":";

        private LuaControlRequestCommand command;
        private Charset charset = Charset.forName("UTF-8");

        {
            this.offset = CONTROL_OFFSET;
            this.type = RequestType.WRITE;
            this.command = LuaControlRequestCommand.LUA;
        }

        /**
         * Constructs new lua control request to execute given lua program.
         *
         * @param luaProgram Lua program to execute.
         */
        public LuaControlRequest(String luaProgram) throws InvalidParameterException {
            if (luaProgram != null && !luaProgram.isBlank()) {
                convertStringToByteArray(this.command.getValue() + COMMAND_SEPARATOR + luaProgram, 0, charset);
            } else {
                throw new InvalidParameterException("The luapProgram cannot be null or blank!");
            }
        }

        /**
         * Constructs new lua control request to execute given lua program.
         *
         * @param luaProgram Lua program to execute.
         * @param charset Charset to use when encoding lua program name and command to the byte array fro FSUIPC.
         */
        public LuaControlRequest(String luaProgram, Charset charset) throws InvalidParameterException {
            if (charset != null) {
                this.charset = charset;
            }
            if (luaProgram != null && !luaProgram.isBlank()) {
                convertStringToByteArray(this.command.getValue() + COMMAND_SEPARATOR + luaProgram, 0, charset);
            } else {
                throw new InvalidParameterException("The luapProgram cannot be null or blank!");
            }
        }

        /**
         * Constructs new lua control request to perform specified command with specified lua program.
         *
         * @param command A command - what to do with the given lua program.
         * @param luaProgram Lua program to perform command with.
         */
        public LuaControlRequest(LuaControlRequestCommand command, String luaProgram) throws InvalidParameterException {
            this.command = command;

            if (luaProgram != null && !luaProgram.isBlank()) {
                convertStringToByteArray(this.command.getValue() + COMMAND_SEPARATOR + luaProgram, 0, charset);
            } else {
                throw new InvalidParameterException("The luapProgram cannot be null or blank!");
            }

        }

        /**
         * Constructs new lua control request to perform specified command with specified lua program.
         *
         * @param command A command - whether to read/write to or create given Lvar.
         * @param luaProgram Lua program to perform command with.
         * @param charset Charset to use when encoding lua program name and command to the byte array for FSUIPC.
         */
        public LuaControlRequest(LuaControlRequestCommand command, String luaProgram, Charset charset) throws InvalidParameterException {
            if (charset != null) {
                this.charset = charset;
            }
            this.command = command;

            if (luaProgram != null && !luaProgram.isBlank()) {
                convertStringToByteArray(this.command.getValue() + COMMAND_SEPARATOR + luaProgram, 0, charset);
            } else {
                throw new InvalidParameterException("The luapProgram cannot be null or blank!");
            }

        }

        /**
         * Will set the value - the name of the lua program that should be controlled by this control request.
         * {@link LuaControlRequestCommand#LUA} by default, unless other command was set by the
         * {@link LuaControlRequest} constructor or {@link #setCommand(com.mouseviator.fsuipc.helpers.LVarHelper.LuaControlRequestCommand)
         * }. You must call the {@link #setCommand(com.mouseviator.fsuipc.helpers.LVarHelper.LuaControlRequestCommand)
         * } before this function in order for the command to be updated.
         *
         * @param value An lua program name this control command is referencing.
         */
        @Override
        public void setValue(String value) {
            if (value != null && !value.isBlank()) {
                convertStringToByteArray(this.command.getValue() + COMMAND_SEPARATOR + value, 0, charset);
            }
        }

        /**
         * Returns the command this control request is set to perform. This is, what should FSUIPC do with the specified
         * lua program this control request is referencing to.
         *
         * @return The command.
         */
        public LuaControlRequestCommand getCommand() {
            return this.command;
        }

        /**
         * This method will set the command - what should FSUIPC do with the Lvar this control request is referencing.
         *
         * @param command The command.
         */
        public void setCommand(LuaControlRequestCommand command) {
            String currValue = new String(this.dataBuffer, this.charset).trim();
            String luaProgram = currValue.replace(this.command.getValue(), "").replace(COMMAND_SEPARATOR, "").trim();
            //make new value
            this.command = command;
            convertStringToByteArray(this.command.getValue() + COMMAND_SEPARATOR + luaProgram, 0, charset);
        }

        /**
         * Returns the charset that is used to convert string containing lua program name and command to the byte array
         * for FSUIPC.
         *
         * @return The charset used.
         */
        public Charset getCharset() {
            return charset;
        }

        /**
         * Sets the charset to use when converting string value that should contain lua program name and command to the
         * byte array for FSUIPC. Call this before {@link #setValue(java.lang.String) }, otherwise the change will have
         * no effect.
         *
         * @param charset The charset to use.
         */
        public void setCharset(Charset charset) {
            this.charset = charset;
        }
    }

    /**
     * An enumeration representing a command for {@link LuaControlRequest}.
     */
    public static enum LuaControlRequestCommand {
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * start lua program.
         */
        LUA("Lua"),
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * set specific value at ipc param for specified lua program.
         */
        LUA_VALUE("LuaValue"),
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * debug lua program.
         */
        LUA_DEBUG("LuaDebug"),
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * kill lua program.
         */
        LUA_KILL("LuaKill"),
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * set flag for specified lua program.
         */
        LUA_SET("LuaSet"),
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * clear flag for specified lua program.
         */
        LUA_CLEAR("LuaClear"),
        /**
         * A constant being used by {@link LuaControlRequest} request internally for creating string telling FSUIPC to
         * toggle flag for specified lua program.
         */
        LUA_TOGGLE("LuaToggle");

        private final String value;
        private static final Map<String, LuaControlRequestCommand> lookupTable = new HashMap<>();

        static {
            for (LuaControlRequestCommand type : EnumSet.allOf(LuaControlRequestCommand.class)) {
                lookupTable.put(type.getValue(), type);
            }
        }

        private LuaControlRequestCommand(String value) {
            this.value = value;
        }

        /**
         * @return Integer value of this type.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Returns {@link LVarControlRequestCommand} by corresponding string value.
         *
         * @param value String value corresponding to one of enumeration constants.
         * @return {@link LVarControlRequestCommand} by corresponding string value.
         * @throws InvalidParameterException if value not corresponding to any enumeration value is passed.
         */
        public static LuaControlRequestCommand get(String value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("Command value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }

    /**
     * This class implements lua parameter request. It is an Integer type request, with offset 0x0D6C and is WRITE type.
     * The 0x0D6C offset is being used as parameter for FSUIPC lua program control request.
     */
    public static class LuaParamRequest extends DataRequest implements IWriteOnlyRequest<Integer> {

        {
            this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            this.offset = PARAMETER_OFFSET;
            this.type = RequestType.WRITE;
        }

        /**
         * Creates a new lua parameter request with initial value.
         * 
         * @param value The parameter or flag number for lua request.
         */
        public LuaParamRequest(Integer value) {
            putInt(value);
        }

        /**
         * Sets the parameter value or flag number for lua request.
         * 
         * @param value The parameter or flag number for lua request.
         */
        @Override
        public void setValue(Integer value) {
            putInt(value);
        }
    }

    /**
     * A class representing a result for lua control helper functions in the class {@link LuaHelper}. Controlling lua
     * program via FSUIPC requires 2 data requests. One request to tell FSUIPC what to do with specified lua program -
     * the {@link LuaResult#getControlRequest() ()
     * }, and the other request - {@link LuaResult#getParamRequest() } as the parameter to the lua program, if any.
     */
    public static class LuaResult {

        private LuaControlRequest controlRequest = null;
        private LuaParamRequest paramRequest = null;

        /**
         * Constructs new uninitialized MacroResult.
         */
        public LuaResult() {

        }

        /**
         * Constructs new LuaResult with reference to the data requests for lua program control.
         *
         * @param controlRequest A lua program control request telling FSUIPC what to do with given macro.
         * @param paramRequest A parameter data request for the control request.
         */
        public LuaResult(LuaControlRequest controlRequest, LuaParamRequest paramRequest) {
            this.controlRequest = controlRequest;
            this.paramRequest = paramRequest;
        }

        /**
         *
         * @return Return the parameter request.
         */
        public LuaParamRequest getParamRequest() {
            return paramRequest;
        }

        /**
         * Sets the parameter request.
         * 
         * @param paramRequest The parameter request to set.
         */
        public void setParamRequest(LuaParamRequest paramRequest) {
            this.paramRequest = paramRequest;
        }

        /**
         *
         * @return Returns the control request.
         */
        public LuaControlRequest getControlRequest() {
            return controlRequest;
        }

        /**
         * Sets the control request.
         * 
         * @param controlRequest The control request to set.
         */
        public void setControlRequest(LuaControlRequest controlRequest) {
            this.controlRequest = controlRequest;
        }
    }
}
