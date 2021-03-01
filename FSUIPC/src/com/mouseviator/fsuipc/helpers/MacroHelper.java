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
import com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType;
import com.mouseviator.fsuipc.datarequest.IWriteOnlyRequest;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This class implements a helper to execute FSUIPC macros using FSUIPC. </p>
 *
 * <p>
 * Executing FSUIPC macro requires 2 data requests, because it works with 2 offsets. Many functions in this class return
 * both of them in an instance of {@link MacroResult}. The main offset in use is 0D70, in which you write a string
 * telling FSUIPC macro from what macro file to execute. I call it here a "macro execute request" and it is the {@link MacroResult#getMacroExecuteRequest()
 * }. The next offset is 0D6C and I reference it as "parameter request" here. Because it is the parameter for the macro.
 * You will find this request in the result object as {@link MacroResult#getParamRequest() }.
 *
 *
 * <p>
 * There are various functions for executing FSUIPC macros using FSUIPC. They differ in the amount of parameters - in the way you
 * supply the name of the macro file, the name of the macro and the parameter (if any). Some functions also accepts an instance of {@link FSUIPC} class and a boolean parameter. These functions will also register the generated requests
 * with given FSUIPC instance for one-time or continual processing (based on that boolean parameter)</p>
 *
 * <h2>Running a macro example</h2>
 *
 * <p>The code below uses helper function that will build both the required request. Then they are registered for one-time processing and then they are processed.
 * Whether it worked or not needs to be looked in the sim. Did the Pause mode toggled? (also, the macro must be correct).
 * </p>
 * 
 * <pre><code>
 * //
 * // Running macro
 * //
 * MacroHelper macroHelper = new MacroHelper();
 *
 * System.out.println("Trying to execute macro: \"fsuipctest:PauseToggle\" which should toggle the pause in the sim.");
 * MacroHelper.MacroResult macroreq = macroHelper.executeMacro("fsuipctest", "PauseToggle");
 * fsuipc.addOneTimeRequest(macroreq.getParamRequest());
 * fsuipc.addOneTimeRequest(macroreq.getMacroExecuteRequest());
 *
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *     System.out.println("The macro: \"fsuipctest:PauseToggle\" should have execute. The sim pause should toggle.");
 * }
 * </code></pre>
 *
 * <h3>Running a macro with parameter example</h3>
 * 
 * <p>The code below uses helper function to build requests to set the parking brake on using a macro. Then they are registered for one-time processing and then they are processed.
 * Whether it worked or not needs to be looked in the sim. Is the parking brake set? (also, the macro must be correct). This example also uses
 * another variant of the function which accepts the name of the macro file and name of the macro as one string.
 * </p>
 * 
 * <pre><code>
 * MacroHelper macroHelper = new MacroHelper();
 *
 * System.out.println("Trying to execute macro: \"fsuipctest:ParkBrake\" with param 1, which should set parking brake.");
 * macroreq = macroHelper.executeMacro("fsuipctest:ParkBrake", 1);
 * fsuipc.addOneTimeRequest(macroreq.getParamRequest());
 * fsuipc.addOneTimeRequest(macroreq.getMacroExecuteRequest());
 *
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *    System.out.println("The macro: \"fsuipctest:ParkBrake\" should have execute. The parking brake should be set.");
 * }       
 * </code></pre>
 *
 * <h3>Running a macro with parameter example 2</h3>
 * 
 * <p>The code below uses helper function to build requests to set the parking brake on using a macro. But this time we use the function
 * that accepts also an instance of {@link FSUIPC} as parameter, so it will also register the requests for processing. Then we only tell FSUIPC to
 * process the requests. Whether it worked or not needs to be looked in the sim. Is the parking brake unset? (also, the macro must be correct). This example also uses
 * another variant of the function which accepts the name of the macro file and name of the macro as one string.
 * </p>
 * 
 * <pre><code>
 * MacroHelper macroHelper = new MacroHelper();
 * 
 * System.out.println("Trying to execute macro: \"fsuipctest:ParkBrake\" with param 0, which should unset parking brake.");
 * macroreq = macroHelper.executeMacro("fsuipctest:ParkBrake", 0, fsuipc, false);
 * 
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *   System.out.println("The macro: \"fsuipctest:ParkBrake\" should have execute. The parking brake should be unset.");
 * }
 * </code></pre>
 * 
 * <p>
 * If you use the function that registers the requests with FSUIPC, you may not need the resulting object at all. Unless you
 * registered them for continual processing and need to remove them from processing later on.</p>
 *
 * <p>
 * If you will be registering the requests yourself, make sure that you register them in this order</p>
 * <ul><li>1. Parameter request ({@link MacroResult#getParamRequest() })</li>
 * <li>2. Execute request ({@link MacroResult#getMacroExecuteRequest()  })</li>
 * </ul>
 *
 * <p>You can also just use the request and other supporting classes defined in this helper class and write the "functions" by yourself.</p>
 *
 * @author Mouseviator
 */
public class MacroHelper {
    /**
     * Offset for parameter passing for the control offset below
     */
    public static final int PARAMETER_OFFSET = 0x0D6C;

    /**
     * Offset to tell FSUIPC what to do with lua script, LVar or Macro
     */
    public static final int CONTROL_OFFSET = 0x0D70;
    
    /**
     * This function will create requests required to execute a macro by FSUIPC. Executing a macro requires 2 requests.
     * One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro execute
     * request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The "macro
     * file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro within
     * that macro file. Both parts can be up to 16 chars long. This function will generate parameter data request with
     * value 0.
     *
     * @param macro The macro to execute. In the format: "macro file:macro name"
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned.
     */
    public MacroResult executeMacro(String macro) {
        return executeMacro(macro, 0);
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC. Executing a macro requires 2 requests.
     * One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro execute
     * request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The "macro
     * file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro within
     * that macro file. Both parts can be up to 16 chars long. This function will generate parameter data request with
     * value 0 and will register the generated requests for continual or one-time processing.
     *
     * @param macro The macro to execute. In the format: "macro file:macro name"
     * @param fsuipc An instance of {@link FSUIPC} to register requests with.
     * @param continual Whether to register requests for continual processing or one-time processing. True for
     * continual.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned. Null will be returned also if
     * <code>fsuipc</code> is null.
     */
    public MacroResult executeMacro(String macro, FSUIPC fsuipc, boolean continual) {
        return executeMacro(macro, 0, fsuipc, continual);
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC.Executing a macro requires 2 requests.
     * One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro execute
     * request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The "macro
     * file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro within
     * that macro file. Both parts can be up to 16 chars long.
     *
     * @param macro The macro to execute. In the format: "macro file:macro name"
     * @param macroParam The parameter for the macro.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned.
     */
    public MacroResult executeMacro(String macro, int macroParam) {
        MacroResult result = null;

        //create zero param request
        MacroParamRequest paramRequest = new MacroParamRequest(macroParam);

        //create execute request
        MacroExecuteRequest executeRequest;
        try {
            executeRequest = new MacroExecuteRequest(macro);

            result = new MacroResult(executeRequest, paramRequest);
        } catch (Exception ex) {
            Logger.getLogger(MacroHelper.class.getName()).log(Level.SEVERE, "Failed to create macro execute request!", ex);
        }

        return result;
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC.Executing a macro requires 2
     * requests.One is the macro parameter request - integer value written at offset 0x0D6C.The other is the macro
     * execute request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The
     * "macro file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro
     * within that macro file. Both parts can be up to 16 chars long. This function and will register the generated
     * requests for continual or one-time processing.
     *
     * @param macro The macro to execute. In the format: "macro file:macro name"
     * @param macroParam The parameter for the macro.
     * @param fsuipc An instance of {@link FSUIPC} to register requests with.
     * @param continual Whether to register requests for continual processing or one-time processing. True for
     * continual.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned. Null will be returned also if
     * <code>fsuipc</code> is null.
     */
    public MacroResult executeMacro(String macro, int macroParam, FSUIPC fsuipc, boolean continual) {
        MacroResult result = null;
        if (fsuipc == null) {
            return null;
        }

        //create zero param request
        MacroParamRequest paramRequest = new MacroParamRequest(macroParam);

        //create execute request
        MacroExecuteRequest executeRequest;
        try {
            executeRequest = new MacroExecuteRequest(macro);

            result = new MacroResult(executeRequest, paramRequest);

            if (continual) {
                fsuipc.addContinualRequest(result.paramRequest);
                fsuipc.addContinualRequest(result.macroExecuteRequest);
            } else {
                fsuipc.addOneTimeRequest(result.paramRequest);
                fsuipc.addOneTimeRequest(result.macroExecuteRequest);
            }
        } catch (Exception ex) {
            Logger.getLogger(MacroHelper.class.getName()).log(Level.SEVERE, "Failed to create macro execute request!", ex);
        }

        return result;
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC.Executing a macro requires 2 requests.
     * One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro execute
     * request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The "macro
     * file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro within
     * that macro file. Both parts can be up to 16 chars long. This function will generate parameter data request with
     * value 0. This function accepts macro file and macro name separately.
     *
     * @param macroFile The name of the macro file.
     * @param macroName The name of the macro within the macro file.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned.
     */
    public MacroResult executeMacro(String macroFile, String macroName) {
        return executeMacro(macroFile, macroName, 0);
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC.Executing a macro requires 2 requests.
     * One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro execute
     * request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The "macro
     * file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro within
     * that macro file. Both parts can be up to 16 chars long. This function will generate parameter data request with
     * value 0. This function accepts macro file and macro name separately and will register the generated requests for
     * continual or one-time processing.
     *
     * @param macroFile The name of the macro file.
     * @param macroName The name of the macro within the macro file.
     * @param fsuipc An instance of {@link FSUIPC} to register requests with.
     * @param continual Whether to register requests for continual processing or one-time processing. True for
     * continual.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned. Null will be returned also if
     * <code>fsuipc</code> is null.
     */
    public MacroResult executeMacro(String macroFile, String macroName, FSUIPC fsuipc, boolean continual) {
        return executeMacro(macroFile, macroName, 0, fsuipc, continual);
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC.Executing a macro requires 2
     * requests.One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro
     * execute request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The
     * "macro file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro
     * within that macro file. Both parts can be up to 16 chars long. This function accepts macro file and macro name
     * separately.
     *
     * @param macroFile The name of the macro file.
     * @param macroName The name of the macro within the macro file.
     * @param macroParam The parameter for the macro.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned.
     */
    public MacroResult executeMacro(String macroFile, String macroName, int macroParam) {
        MacroResult result = null;

        //create zero param request
        MacroParamRequest paramRequest = new MacroParamRequest(macroParam);

        //create execute request
        MacroExecuteRequest executeRequest;
        try {
            executeRequest = new MacroExecuteRequest(macroFile, macroName);

            result = new MacroResult(executeRequest, paramRequest);
        } catch (Exception ex) {
            Logger.getLogger(MacroHelper.class.getName()).log(Level.SEVERE, "Failed to create macro execute request!", ex);
        }

        return result;
    }

    /**
     * This function will create requests required to execute a macro by FSUIPC.Executing a macro requires 2
     * requests.One is the macro parameter request - integer value written at offset 0x0D6C. The other is the macro
     * execute request - written at offset 0x0D70 and it contains the String in the format "macro file:macro name". The
     * "macro file" is the name of the macro file without the .mcro extension. The "macro name" is name of the macro
     * within that macro file. Both parts can be up to 16 chars long. This function accepts macro file and macro name
     * separately and will register the generated requests for continual or one-time processing.
     *
     * @param macroFile The name of the macro file.
     * @param macroName The name of the macro within the macro file.
     * @param macroParam The parameter for the macro.
     * @param fsuipc An instance of {@link FSUIPC} to register requests with.
     * @param continual Whether to register requests for continual processing or one-time processing. True for
     * continual.
     * @return Will return an instance of {@link MacroResult} when both required data requests are successfully created.
     * The creation of execute request may fail if the <code>macro</code> violates the format or either macro file or
     * macro name parts exceeds length of 16 bytes... In that case, null will be returned. Null will be returned also if
     * <code>fsuipc</code> is null.
     */
    public MacroResult executeMacro(String macroFile, String macroName, int macroParam, FSUIPC fsuipc, boolean continual) {
        MacroResult result = null;
        if (fsuipc == null) {
            return null;
        }

        //create zero param request
        MacroParamRequest paramRequest = new MacroParamRequest(macroParam);

        //create execute request
        MacroExecuteRequest executeRequest;
        try {
            executeRequest = new MacroExecuteRequest(macroFile, macroName);

            result = new MacroResult(executeRequest, paramRequest);

            if (continual) {
                fsuipc.addContinualRequest(result.paramRequest);
                fsuipc.addContinualRequest(result.macroExecuteRequest);
            } else {
                fsuipc.addOneTimeRequest(result.paramRequest);
                fsuipc.addOneTimeRequest(result.macroExecuteRequest);
            }
        } catch (Exception ex) {
            Logger.getLogger(MacroHelper.class.getName()).log(Level.SEVERE, "Failed to create macro execute request!", ex);
        }

        return result;
    }

    /**
     * This class implements macro execute request. It is a WRITE type request, writing a string at offset 0x0D70. This
     * string than identifies what macro from what macro file should FSUIPC execute.
     * 
     * Note that the string format is: "&lt;macro file name&gt;:&lt;macro_name&gt;". The maximum length of the &lt;macro file name&gt; is
     * 16 characters. The maximum length of the &lt;macro_name&gt; is 16 characters. The separator char must be ":". 
     * The constructors as well as the {@link MacroExecuteRequest#setValue(java.lang.String) } will check for these limitations and will
     * raise the {@link InvalidParameterException} if not met.
     */
    public static class MacroExecuteRequest extends DataRequest implements IWriteOnlyRequest<String> {

        public static final char MACRO_SEPARATOR = ':';
        private static final byte MAX_MACRO_FILE_LENGHT = 16;
        private static final byte MAX_MACRO_NAME_LENGTH = 16;

        private Pattern macroValuePattern = Pattern.compile("(.{1," + MAX_MACRO_FILE_LENGHT + "})" + MACRO_SEPARATOR + "(.{1," + MAX_MACRO_NAME_LENGTH + "})");
        private Charset charset = Charset.forName("UTF-8");

        {
            this.offset = CONTROL_OFFSET;
            this.type = RequestType.WRITE;

        }

        /**
         * Constructs new macro execute request to execute given macro.
         * 
         * @param macro A macro to execute.
         */
        public MacroExecuteRequest(String macro) throws InvalidParameterException {
            constructMacroString(macro);
        }

        /**
         * Constructs new macro execute request to execute given macro.
         * 
         * @param macro A macro to execute.
         * @param charset Charset to use during conversion of <code>macro</code> to the
         * byte array for FSUIPC.
         */
        public MacroExecuteRequest(String macro, Charset charset) throws InvalidParameterException {
            if (charset != null) {
                this.charset = charset;
            }
            constructMacroString(macro);
        }

        /**
         * Constructs new macro execute request to execute given macro from given macro file.
         *
         * @param macroFile Macro file name.
         * @param macroName Name of the macro inside the macro file.
         */
        public MacroExecuteRequest(String macroFile, String macroName) throws InvalidParameterException {
            constructMacroString(macroFile, macroName);
        }

        /**
         * Constructs new macro execute request to execute given macro from given macro file.
         *
         * @param macroFile Macro file name.
         * @param macroName Name of the macro inside the macro file.
         * @param charset Charset to use during conversion of <code>macroFile</code> and <code>macroName</code> to the
         * byte array for FSUIPC.
         */
        public MacroExecuteRequest(String macroFile, String macroName, Charset charset) throws InvalidParameterException {
            if (charset != null) {
                this.charset = charset;
            }
            constructMacroString(macroFile, macroName);
        }

        /**
         * (The name of the macro file, the ":" char and the name of macro within that file).
         *
         * @param value A macro to execute.
         */
        @Override
        public void setValue(String value) {
            constructMacroString(value);
        }

        private void constructMacroString(String macro) throws InvalidParameterException {
            final Matcher matcher = macroValuePattern.matcher(macro);
            if (matcher.matches()) {
                //if matces the pattern, it should be impossible the following code to raise an exception, so we will skip checks
                String[] macroParts = macro.split(Character.toString(MACRO_SEPARATOR));
                constructMacroString(macroParts[0], macroParts[1]);
            } else {
                throw new InvalidParameterException(MessageFormat.format("Failed to match given macro: {0} against macro regex: {1}", macro, macroValuePattern.toString()));
            }
        }

        private void constructMacroString(String macroFile, String macroName) {
            byte[] macroFileBytes = macroFile.getBytes(charset);
            if (macroFileBytes.length > MAX_MACRO_FILE_LENGHT) {
                throw new InvalidParameterException(MessageFormat.format("The length of the byte array of the converted macro file name is too long. It's length is: {0} and maximum is: {1}", macroFileBytes.length, MAX_MACRO_FILE_LENGHT));
            }

            byte[] macroNameBytes = macroName.getBytes(charset);
            if (macroNameBytes.length > MAX_MACRO_NAME_LENGTH) {
                throw new InvalidParameterException(MessageFormat.format("The length of the byte array of the converted macro name is too long. It's length is: {0} and maximum is: {1}", macroNameBytes.length, MAX_MACRO_NAME_LENGTH));
            }

            //now create and fill the byte buffer
            this.dataBuffer = new byte[macroFileBytes.length + macroNameBytes.length + 2];

            //copy macro file name
            System.arraycopy(macroFileBytes, 0, this.dataBuffer, 0, macroFileBytes.length);

            //put separator char
            this.dataBuffer[macroFileBytes.length] = (byte) MACRO_SEPARATOR;

            //copy macro name
            System.arraycopy(macroNameBytes, 0, this.dataBuffer, macroFileBytes.length + 1, macroNameBytes.length);

            //add terminating 0
            this.dataBuffer[this.dataBuffer.length - 1] = (byte) 0;
        }

        /**
         * Returns the charset that is used to convert string containing macro file and macro name to the byte array for
         * FSUIPC.
         *
         * @return The charset used.
         */
        public Charset getCharset() {
            return charset;
        }

        /**
         * Sets the charset to use when converting string value that should contain macro file name and macro name to
         * the byte array for FSUIPC. Call this before {@link #setValue(java.lang.String) }, otherwise the change will
         * have no effect.
         *
         * @param charset The charset to use.
         */
        public void setCharset(Charset charset) {
            this.charset = charset;
        }
    }

    /**
     * This class implements macro parameter request. It is an Integer type request, with offset 0x0D6C and is WRITE
     * type. The 0x0D6C offset is being used as parameter for FSUIPC macro execute requests. The value of this request
     * is expected to be an integer in the range of -32768 to 32767 or 0 to 65535 for unsigned values.
     */
    public static class MacroParamRequest extends DataRequest implements IWriteOnlyRequest<Integer> {

        {
            this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            this.offset = PARAMETER_OFFSET;
            this.type = RequestType.WRITE;
        }

        /**
         * Creates a new macro parameter request with initial value.
         *  
         * @param value The parameter for the macro.
         */
        public MacroParamRequest(Integer value) {
            putInt(value);
        }

        /**
         * Sets the macro parameter.
         * 
         * @param value The parameter for the macro.
         */
        @Override
        public void setValue(Integer value) {
            putInt(value);
        }
    }

    /**
     * A class representing a result fo macro execute helper functions in the class {@link MacroHelper}. Executing macro
     * requires 2 data requests. One request to tell FSUIPC what macro to execute - the {@link MacroResult#getMacroExecuteRequest()
     * }, and the other request - {@link MacroResult#getParamRequest() } as the parameter to the macro, if any.
     */
    public static class MacroResult {

        private MacroExecuteRequest macroExecuteRequest = null;
        private MacroParamRequest paramRequest = null;

        /**
         * Constructs new uninitialized MacroResult.
         */
        public MacroResult() {

        }

        /**
         * Constructs new MacroResult with reference to the data requests for macro execution.
         *
         * @param macroExecuteRequest A macro execute request telling FSUIPC to execute the macro.
         * @param paramRequest A parameter data request for the control request.
         */
        public MacroResult(MacroExecuteRequest macroExecuteRequest, MacroParamRequest paramRequest) {
            this.macroExecuteRequest = macroExecuteRequest;
            this.paramRequest = paramRequest;
        }

        /**
         *
         * @return The macro execute request.
         */
        public MacroExecuteRequest getMacroExecuteRequest() {
            return macroExecuteRequest;
        }

        /**
         * Sets the macro execute request.
         * 
         * @param macroExecuteRequest The macro execute request.
         */
        public void setMacroExecuteRequest(MacroExecuteRequest macroExecuteRequest) {
            this.macroExecuteRequest = macroExecuteRequest;
        }

        /**
         *
         * @return The macro parameter request.
         */
        public MacroParamRequest getParamRequest() {
            return paramRequest;
        }

        /**
         * Sets the macro parameter request.
         * 
         * @param paramRequest The macro parameter request.
         */
        public void setParamRequest(MacroParamRequest paramRequest) {
            this.paramRequest = paramRequest;
        }
    }
}
