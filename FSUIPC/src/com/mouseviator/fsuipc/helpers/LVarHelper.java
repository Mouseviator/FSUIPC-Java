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
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_DOUBLE;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_FLOAT;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_INT;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_SHORT;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;
import com.mouseviator.fsuipc.datarequest.IWriteOnlyRequest;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * This class implements a helper to manipulate LVars using FSUIPC. LVars are gauge variables. This class allows you to
 * read, write and create LVars.</p>
 *
 * <p>
 * Manipulating LVars via FSUIPC requires 3 data requests, because it works with 3 offsets. Many functions in this class
 * return all of them in an instance of {@link LVarResult}. The main offset in use is 0D70, in which you write a string
 * telling FSUIPC what to do with what LVar. I call it here a "control request" and it is the {@link LVarResult#getControlRequest()
 * }. The next offset is 0D6C and I reference it as "parameter request" here. Because it is the parameter for requested
 * LVar operation, telling FSUIPC at which offset and in what format to place the result of LVar read operation, or
 * where it will find the value for write/create operation. You will find this request in the result object as {@link LVarResult#getParamRequest()
 * }. The last data request is the result request (even thought that for write and create it is actually kind of source
 * data request). It is the data request that will hold the result of the LVar read operation, or will hold the value to
 * write or init new LVar with (create operation). This is the {@link LVarResult#getResultRequest() } in the result
 * object.</p>
 *
 * <p>
 * There are various functions for writing, creating LVar, which mostly differ just by the value type they accept.</p>
 *
 * <p>
 * Some functions also accepts an instance of {@link FSUIPC} class and a boolean parameter. These functions will also
 * register the generated requests with given FSUIPC instance for one-time or continual processing (based on that
 * boolean parameter)</p>
 *
 * <h2>Reading an LVar example</h2>
 *
 * <p>
 * Here is an example how you would read an LVar value without this helper class. It first creates a "parameter
 * request", which tells FSUIPC that we want to store the value of LVar being read at offset 0x66C0 as double value.
 * Than we create "control request", which will tell FSUIPC to read the LVar. The third request is the one that Lvar
 * value will be store in. Than we register all 3 request for one-time processing, process all the requests and print
 * the result.</p>
 *
 * <pre><code>
 * // Construct the control param request. We will be reading the LVar, this param means:
 * // Store an LVar value at offset 0x66C0 as 64-bit float value (It is the 0x00000 + 0x66C0. More about this in FSUIPC documentation)
 * IntRequest paramReq = new IntRequest(0x0D6C);
 * paramReq.setValue(0x066C0);
 * paramReq.setType(IDataRequest.RequestType.WRITE);
 * fsuipc.addOneTimeRequest(paramReq);
 *
 * //Construct the control request. The ":" tells FSUIPC to read the LVar, whose name follows
 * StringRequest controlReq = new StringRequest(0x0D70, ":FSDT_GSX_DEBOARDING_STATE");
 * fsuipc.addOneTimeRequest(controlReq);
 *
 * //The result request where the resulting 64-bit float (double) will be
 * DoubleRequest resultReq = new DoubleRequest(0x66C0);
 * fsuipc.addOneTimeRequest(resultReq);
 *
 * //Read the LVar value
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *       System.out.println("Read Okay");
 *       System.out.println("The value of lvar: FSDT_GSX_DEBOARDING_STATE is: " + resultReq.getValue());
 * }
 * </code></pre>
 *
 * <p>
 * The same done using this helper class. You can see it si much simpler, as all of the code is hidden in the function.
 * The function that takes an {@link FSUIPC} instance is used here, so not even needed to register the requests. Just
 * call the helper function and process the requests.</p>
 *
 * <pre><code>
 * //Let the helper function construct all 3 requests and register them with FSUIPC for one-time processing
 * LVarHelper.LVarResult lvarreq = lvarHelper.readLVar("FSDT_GSX_DEBOARDING_STATE", 0x66C0,LVarHelper.LVarValueFormat.DOUBLE , fsuipc, false);
 *
 * //Read the LVar value
 * if (fsuipc.processRequestsOnce() == FSUIPC.PROCESS_RESULT_OK) {
 *       System.out.println("Read Okay");
 *       System.out.println("The value of lvar: FSDT_GSX_DEBOARDING_STATE is: " + lvarreq.getResultRequest().getValue());
 * }
 * </code></pre>
 *
 * <p>
 * Writing an LVar or creating an LVar follows the same logic. Only in these cases, all generated requests will be write
 * only. If you use the function that that registers the requests with FSUIPC, you may not need the resulting object at
 * all. Unless you registered them for continual processing and need to remove them from processing later on.</p>
 *
 * <p>
 * If you will be registering the requests yourself, make sure that in the case of reading LVar you register them in the
 * order:</p>
 * <ul><li>1. Parameter request ({@link LVarResult#getParamRequest() })</li>
 * <li>2. Control request ({@link LVarResult#getControlRequest() })</li>
 * <li>3. Result request ({@link LVarResult#getResultRequest() })</li>
 * </ul>
 *
 * <p>
 * and in the case of write/create:</p>
 * <ul><li>1. Parameter request ({@link LVarResult#getParamRequest() })</li>
 * <li>2. Result request ({@link LVarResult#getResultRequest() })</li>
 * <li>3. Control request ({@link LVarResult#getControlRequest() })</li>
 * </ul>
 *
 * <p>
 * So in the case of write/read, the control request needs to be registered last! So it has all required data when it
 * comes to it.</p>
 *
 * <p>
 * You can also just use the request and other supporting classes defined in this helper class and write the "functions"
 * by yourself.</p>
 *
 * @author Mouseviator
 */
public class LVarHelper {
    /**
     * Offset for parameter passing for the control offset below
     */
    public static final int PARAMETER_OFFSET = 0x0D6C;

    /**
     * Offset to tell FSUIPC what to do with lua script, LVar or Macro
     */
    public static final int CONTROL_OFFSET = 0x0D70;
    
    
    /**
     * This is helper function to read Lvar. Reading an Lvar value actually requires 3 requests. One to tell FSUIPC what
     * Lvar to read, one to tell FSUIPC where to store the result and one to read the result - the Lvar value. This
     * function will construct all of them, but return only the one to read the resulting (Lvar) value. It will also
     * register all of the request for one-time or continual processing with given FSUIPC instance.
     *
     * The returned request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>.
     *
     *
     * @param lvar Name of the Lvar to read.
     * @param targetOffset An offset at which to store the value of the Lvar.
     * @param lvarValueFormat The format of the value to be stored at the specified offset.
     * @param fsuipc An {@link FSUIPC} instance to register requests with.
     * @param continual Whether to register created requests for continual or one-time processing. True for continual
     * processing.
     * @return Null if <code>fsuipc</code> is null. An instance of {@link LVarResult} class with all 3 requests required
     * to read an Lvar. Do not register the request for processing by FSUIPC, as it will be done by this function.
     */
    public LVarResult readLVar(String lvar, int targetOffset, LVarValueFormat lvarValueFormat, FSUIPC fsuipc, boolean continual) {
        //check that fsuipc si not null
        if (fsuipc == null) {
            return null;
        }

        LVarResult result = readLVar(lvar, targetOffset, lvarValueFormat);

        if (continual) {
            fsuipc.addContinualRequest(result.getParamRequest());
            fsuipc.addContinualRequest(result.getControlRequest());
            fsuipc.addContinualRequest(result.getResultRequest());
        } else {
            fsuipc.addOneTimeRequest(result.getParamRequest());
            fsuipc.addOneTimeRequest(result.getControlRequest());
            fsuipc.addOneTimeRequest(result.getResultRequest());
        }

        return result;
    }

    /**
     * This is helper function to read Lvar. Reading an Lvar value actually requires 3 requests. One to tell FSUIPC what
     * Lvar to read, one to tell FSUIPC where to store the result and one to read the result - the Lvar value. This
     * function will construct all of them and return them in an instance of {@link LVarResult} class. You can then
     * register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getParamRequest() } first, {@link LVarResult#getControlRequest()
     * } second, and {@link LVarResult#getResultRequest() } last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>.
     *
     * @param lvar Name of the Lvar to read.
     * @param targetOffset An offset at which to store the value of the Lvar.
     * @param lvarValueFormat The format of the value to be stored at the specified offset.
     * @return An instance of {@link LVarResult} class with all 3 requests required to read an Lvar.
     */
    public LVarResult readLVar(String lvar, int targetOffset, LVarValueFormat lvarValueFormat) {
        LVarResult result;

        //construct read lvar command request
        LVarControlRequest controlRequest = new LVarControlRequest(lvar);

        //now will construct the param request and request to read resulting value
        LVarParamRequest paramRequest = new LVarParamRequest(lvarValueFormat.getValue() + targetOffset);
        IDataRequest resultRequest;
        switch (lvarValueFormat) {
            case FLOAT:
                resultRequest = new FloatLVarReadRequest(targetOffset);
                break;
            case SIGNED_INTEGER:
                resultRequest = new IntegerLVarReadRequest(targetOffset);
                break;
            case UNSIGNED_INTEGER:
                resultRequest = new IntegerLVarReadRequest(targetOffset);
                break;
            case SIGNED_SHORT:
                resultRequest = new ShortLVarReadRequest(targetOffset);
                break;
            case UNSIGNED_SHORT:
                resultRequest = new ShortLVarReadRequest(targetOffset);
                break;
            case SIGNED_BYTE:
                resultRequest = new ByteLVarReadRequest(targetOffset);
                break;
            case UNSIGNED_BYTE:
                resultRequest = new ByteLVarReadRequest(targetOffset);
                break;
            default:
                //double requets by default                
                resultRequest = new DoubleLVarReadRequest(targetOffset);
        }

        result = new LVarResult(controlRequest, paramRequest, resultRequest);
        return result;
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and return them in an instance of {@link LVarResult} class. You
     * can then register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the value to be written.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult writeLVar(String lvar, int lvarValueOffset, double lvarValue) {
        return writeLVar(lvar, lvarValueOffset, (Double) lvarValue, LVarValueFormat.DOUBLE);
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and return them in an instance of {@link LVarResult} class. You
     * can then register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the value to be written.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult writeLVar(String lvar, int lvarValueOffset, float lvarValue) {
        return writeLVar(lvar, lvarValueOffset, (Float) lvarValue, LVarValueFormat.FLOAT);
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and return them in an instance of {@link LVarResult} class. You
     * can then register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the value to be written.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult writeLVar(String lvar, int lvarValueOffset, short lvarValue) {
        return writeLVar(lvar, lvarValueOffset, (Short) lvarValue, LVarValueFormat.SIGNED_SHORT);
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and return them in an instance of {@link LVarResult} class. You
     * can then register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the value to be written.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult writeLVar(String lvar, int lvarValueOffset, int lvarValue) {
        return writeLVar(lvar, lvarValueOffset, (Integer) lvarValue, LVarValueFormat.SIGNED_INTEGER);
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and return them in an instance of {@link LVarResult} class. You
     * can then register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the value to be written.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult writeLVar(String lvar, int lvarValueOffset, byte lvarValue) {
        return writeLVar(lvar, lvarValueOffset, (Byte) lvarValue, LVarValueFormat.SIGNED_BYTE);
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and return them in an instance of {@link LVarResult} class. You
     * can then register them for continual or one-time processing as you are used to. Note that the requests should be
     * registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the value to be written.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @param lvarValueFormat The format (data type) of the value to be written to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult writeLVar(String lvar, int lvarValueOffset, Object lvarValue, LVarValueFormat lvarValueFormat) {
        LVarResult result;

        //construct read lvar command request
        LVarControlRequest controlRequest = new LVarControlRequest(LVarControlRequestCommand.WRITE, lvar);

        //now will construct the param request and request to read resulting value
        LVarParamRequest paramRequest = new LVarParamRequest(lvarValueFormat.getValue() + lvarValueOffset);
        IDataRequest resultRequest;

        try {
            switch (lvarValueFormat) {
                case FLOAT:
                    resultRequest = new FloatLVarWriteRequest(lvarValueOffset, (Float) lvarValue);
                    break;
                case SIGNED_INTEGER:
                    resultRequest = new IntegerLVarWriteRequest(lvarValueOffset, (Integer) lvarValue);
                    break;
                case UNSIGNED_INTEGER:
                    resultRequest = new IntegerLVarWriteRequest(lvarValueOffset, (Integer) lvarValue);
                    break;
                case SIGNED_SHORT:
                    resultRequest = new ShortLVarWriteRequest(lvarValueOffset, (Short) lvarValue);
                    break;
                case UNSIGNED_SHORT:
                    resultRequest = new ShortLVarWriteRequest(lvarValueOffset, (Short) lvarValue);
                    break;
                case SIGNED_BYTE:
                    resultRequest = new ByteLVarWriteRequest(lvarValueOffset, (Byte) lvarValue);
                    break;
                case UNSIGNED_BYTE:
                    resultRequest = new ByteLVarWriteRequest(lvarValueOffset, (Byte) lvarValue);
                    break;
                default:
                    //double requets by default                    
                    resultRequest = new DoubleLVarWriteRequest(lvarValueOffset, (Double) lvarValue);
            }
        } catch (Exception ex) {
            Logger.getLogger(LVarHelper.class.getName()).log(Level.SEVERE, MessageFormat.format("There was an error while preparing param and result requests to write to Lvar: {0}, value: {1}, value type: {2}, offset: {3}", lvar, lvarValue, lvarValueFormat, lvarValueOffset), ex);
            return null;
        }

        result = new LVarResult(controlRequest, paramRequest, resultRequest);
        return result;
    }

    /**
     * This is helper function to write Lvar. Writing a value an Lvar actually requires 3 requests. One to tell FSUIPC
     * what Lvar to write to, one to tell FSUIPC at which offset to find the value to be written and one with the value
     * itself. This function will construct all of them and will register them with given FSUIPC instance for one-time
     * or continual processing based on the <code>continual</code> parameter.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @param lvarValueFormat The format (data type) of the value to be written to Lvar.
     * @param fsuipc An {@link FSUIPC} instance to register requests with.
     * @param continual Whether to register created requests for continual or one-time processing. True for continual
     * processing.
     * @return True if requests were created and registered for processing. False if <code>fsuipc</code> is null. Note
     * that the True does not mean that the Lvar was actually written to. There is no way to detect if the write
     * succeeded. First, it happens after call to process the requests, and this function only registers the requests.
     * Than there is still no other way to tell if the write succeeded other than reading back the value of the Lvar.
     */
    public boolean writeLVar(String lvar, int lvarValueOffset, Object lvarValue, LVarValueFormat lvarValueFormat, FSUIPC fsuipc, boolean continual) {
        //check that fsuipc si not null
        if (fsuipc == null) {
            return false;
        }

        LVarResult result = writeLVar(lvar, lvarValueOffset, lvarValue, lvarValueFormat);

        if (continual) {
            fsuipc.addContinualRequest(result.getResultRequest());
            fsuipc.addContinualRequest(result.getParamRequest());
            fsuipc.addContinualRequest(result.getControlRequest());
        } else {
            fsuipc.addOneTimeRequest(result.getResultRequest());
            fsuipc.addOneTimeRequest(result.getParamRequest());
            fsuipc.addOneTimeRequest(result.getControlRequest());
        }

        return true;
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize the Lvar with and one with the
     * value itself. This function will construct all of them and return them in an instance of {@link LVarResult}
     * class. You can then register them for continual or one-time processing as you are used to. Note that the requests
     * should be registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the initial value of the Lvar.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @param lvarValueFormat The format (data type) of the value to be written to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult createLVar(String lvar, int lvarValueOffset, Object lvarValue, LVarValueFormat lvarValueFormat) {
        LVarResult result;

        //construct read lvar command request
        LVarControlRequest controlRequest = new LVarControlRequest(LVarControlRequestCommand.CREATE, lvar);

        //now will construct the param request and request to read resulting value
        LVarParamRequest paramRequest = new LVarParamRequest(lvarValueFormat.getValue() + lvarValueOffset);
        IDataRequest resultRequest;

        try {
            switch (lvarValueFormat) {
                case FLOAT:
                    resultRequest = new FloatLVarWriteRequest(lvarValueOffset, (Float) lvarValue);
                    break;
                case SIGNED_INTEGER:
                    resultRequest = new IntegerLVarWriteRequest(lvarValueOffset, (Integer) lvarValue);
                    break;
                case UNSIGNED_INTEGER:
                    resultRequest = new IntegerLVarWriteRequest(lvarValueOffset, (Integer) lvarValue);
                    break;
                case SIGNED_SHORT:
                    resultRequest = new ShortLVarWriteRequest(lvarValueOffset, (Short) lvarValue);
                    break;
                case UNSIGNED_SHORT:
                    resultRequest = new ShortLVarWriteRequest(lvarValueOffset, (Short) lvarValue);
                    break;
                case SIGNED_BYTE:
                    resultRequest = new ByteLVarWriteRequest(lvarValueOffset, (Byte) lvarValue);
                    break;
                case UNSIGNED_BYTE:
                    resultRequest = new ByteLVarWriteRequest(lvarValueOffset, (Byte) lvarValue);
                    break;
                default:
                    //double requets by default                    
                    resultRequest = new DoubleLVarWriteRequest(lvarValueOffset, (Double) lvarValue);
            }
        } catch (Exception ex) {
            Logger.getLogger(LVarHelper.class.getName()).log(Level.SEVERE, MessageFormat.format("There was an error while preparing param and result requests to create Lvar: {0}, value: {1}, value type: {2}, offset: {3}", lvar, lvarValue, lvarValueFormat, lvarValueOffset), ex);
            return null;
        }

        result = new LVarResult(controlRequest, paramRequest, resultRequest);
        return result;
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize the Lvar with and one with the
     * value itself. This function will construct all of them and return them in an instance of {@link LVarResult}
     * class. You can then register them for continual or one-time processing as you are used to. Note that the requests
     * should be registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the initial value of the Lvar.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult createLVar(String lvar, int lvarValueOffset, double lvarValue) {
        return createLVar(lvar, lvarValueOffset, (Double) lvarValue, LVarValueFormat.DOUBLE);
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize the Lvar with and one with the
     * value itself. This function will construct all of them and return them in an instance of {@link LVarResult}
     * class. You can then register them for continual or one-time processing as you are used to. Note that the requests
     * should be registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the initial value of the Lvar.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult createLVar(String lvar, int lvarValueOffset, float lvarValue) {
        return createLVar(lvar, lvarValueOffset, (Float) lvarValue, LVarValueFormat.FLOAT);
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize the Lvar with and one with the
     * value itself. This function will construct all of them and return them in an instance of {@link LVarResult}
     * class. You can then register them for continual or one-time processing as you are used to. Note that the requests
     * should be registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the initial value of the Lvar.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult createLVar(String lvar, int lvarValueOffset, int lvarValue) {
        return createLVar(lvar, lvarValueOffset, (Integer) lvarValue, LVarValueFormat.SIGNED_INTEGER);
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize the Lvar with and one with the
     * value itself. This function will construct all of them and return them in an instance of {@link LVarResult}
     * class. You can then register them for continual or one-time processing as you are used to. Note that the requests
     * should be registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the initial value of the Lvar.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult createLVar(String lvar, int lvarValueOffset, short lvarValue) {
        return createLVar(lvar, lvarValueOffset, (Short) lvarValue, LVarValueFormat.SIGNED_SHORT);
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize the Lvar with and one with the
     * value itself. This function will construct all of them and return them in an instance of {@link LVarResult}
     * class. You can then register them for continual or one-time processing as you are used to. Note that the requests
     * should be registered in the order: {@link LVarResult#getResultRequest() } first, {@link LVarResult#getParamRequest()
     * } second, {@link LVarResult#getControlRequest()} last.
     *
     * The result request will be an instance of
     * {@link DoubleLVarReadRequest}, {@link FloatLVarReadRequest}, {@link IntegerLVarReadRequest}, {@link ShortLVarReadRequest}
     * or {@link ByteLVarReadRequest} depending on the value of <code>lvarValueFormat</code>. The result request in this
     * case is the request containing the initial value of the Lvar.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @return An instance of {@link LVarResult} class with all 3 requests required to write an Lvar.
     */
    public LVarResult createLVar(String lvar, int lvarValueOffset, byte lvarValue) {
        return createLVar(lvar, lvarValueOffset, (Byte) lvarValue, LVarValueFormat.SIGNED_BYTE);
    }

    /**
     * This is helper function to create Lvar. Creating an Lvar actually requires 3 requests. One to tell FSUIPC what
     * Lvar to create, one to tell FSUIPC at which offset to find the value to initialize Lvar with and one with the
     * value itself. This function will construct all of them and will register them with given FSUIPC instance for
     * one-time processing.
     *
     * @param lvar An Lvar to write to.
     * @param lvarValueOffset Offset at which FSUIPC will find the value to write to the Lvar.
     * @param lvarValue Value to write to Lvar.
     * @param lvarValueFormat The format (data type) of the value to be written to Lvar.
     * @param fsuipc An {@link FSUIPC} instance to register requests with.
     * @return True if requests were created and registered for one-time processing. False if <code>fsuipc</code> is
     * null. Note that the True does not mean that the Lvar was actually created. There is no way to detect if the
     * create succeeded. First, it happens after call to process the requests, and this function only registers the
     * requests. Than there is still no other way to tell if the create succeeded other than reading back the value of
     * the Lvar.
     */
    public boolean createLVar(String lvar, int lvarValueOffset, Object lvarValue, LVarValueFormat lvarValueFormat, FSUIPC fsuipc) {
        //check that fsuipc si not null
        if (fsuipc == null) {
            return false;
        }

        LVarResult result = createLVar(lvar, lvarValueOffset, lvarValue, lvarValueFormat);

        fsuipc.addOneTimeRequest(result.getResultRequest());
        fsuipc.addOneTimeRequest(result.getParamRequest());
        fsuipc.addOneTimeRequest(result.getControlRequest());

        return true;
    }

    /**
     * This class implements LVar read (read-only) request. The returned value will be Double (64-bit float value). The
     * only parameter for the only available constructor this class have is the offset at which the value of Lvar will
     * be found (where you told FSUIPC to store it). This class is best used with {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat) } function
     * or {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat, com.mouseviator.fsuipc.FSUIPC, boolean) } function.
     */
    public static class DoubleLVarReadRequest extends DataRequest implements IReadOnlyRequest<Double> {

        /**
         * Construct new LVar read request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         */
        public DoubleLVarReadRequest(int offset) {
            this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
            this.offset = offset;
        }

        /**
         * Returns the double value stored in this read data request.
         *
         * @return The read request value.
         */
        @Override
        public Double getValue() {
            return getDouble();
        }        
    };

    /**
     * This class implements LVar read (read-only) request. The returned value will be Float (32-bit float value). The
     * only parameter for the only available constructor this class have is the offset at which the value of Lvar will
     * be found (where you told FSUIPC to store it). This class is best used with {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat) } function
     * or {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat, com.mouseviator.fsuipc.FSUIPC, boolean) } function.
     */
    public static class FloatLVarReadRequest extends DataRequest implements IReadOnlyRequest<Float> {

        /**
         * Construct new LVar read request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         */
        public FloatLVarReadRequest(int offset) {
            this.dataBuffer = new byte[BUFFER_LENGTH_FLOAT];
            this.offset = offset;
        }

        /**
         * Returns the float value stored in this read data request.
         *
         * @return The read request value.
         */
        @Override
        public Float getValue() {
            return getFloat();
        }
    };

    /**
     * This class implements LVar read (read-only) request. The returned value will be Integer (32-bit integer). The
     * only parameter for the only available constructor this class have is the offset at which the value of Lvar will
     * be found (where you told FSUIPC to store it). This class is best used with {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat) } function
     * or {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat, com.mouseviator.fsuipc.FSUIPC, boolean) } function.
     */
    public static class IntegerLVarReadRequest extends DataRequest implements IReadOnlyRequest<Integer> {

        /**
         * Construct new LVar read request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         */
        public IntegerLVarReadRequest(int offset) {
            this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            this.offset = offset;
        }

        /**
         * Returns the integer value stored in this read data request.
         *
         * @return The read request value.
         */
        @Override
        public Integer getValue() {
            return getInt();
        }
    };

    /**
     * This class implements LVar read (read-only) request. The returned value will be Short (16 bit integer). The only
     * parameter for the only available constructor this class have is the offset at which the value of Lvar will be
     * found (where you told FSUIPC to store it). This class is best used with {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat) } function
     * or {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat, com.mouseviator.fsuipc.FSUIPC, boolean) } function.
     */
    public static class ShortLVarReadRequest extends DataRequest implements IReadOnlyRequest<Short> {

        /**
         * Construct new LVar read request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         */
        public ShortLVarReadRequest(int offset) {
            this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            this.offset = offset;
        }

        /**
         * Returns the short value stored in this read data request.
         *
         * @return The read request value.
         */
        @Override
        public Short getValue() {
            return getShort();
        }
    };

    /**
     * This class implements LVar read (read-only) request. The returned value will be Short (16 bit integer). The only
     * parameter for the only available constructor this class have is the offset at which the value of Lvar will be
     * found (where you told FSUIPC to store it). This class is best used with {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat) } function
     * or {@link LVarHelper#readLVar(java.lang.String, int, com.mouseviator.fsuipc.helpers.LVarHelper.LVarValueFormat, com.mouseviator.fsuipc.FSUIPC, boolean) } function.
     */
    public static class ByteLVarReadRequest extends DataRequest implements IReadOnlyRequest<Byte> {

        /**
         * Construct new LVar read request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         */
        public ByteLVarReadRequest(int offset) {
            this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            this.offset = offset;
        }

        /**
         * Returns the byte value stored in this read data request.
         *
         * @return The read request value.
         */
        @Override
        public Byte getValue() {
            return dataBuffer[0];
        }
    };

    /**
     * This class implements LVar write (write-only) request. It will accept Double (64-bit float value) value. The only
     * one available constructor takes 2 arguments. The first one is an offset at which FSUIPC will look for that value
     * to write to the Lvar. The second one is the value itself.
     */
    public static class DoubleLVarWriteRequest extends DataRequest implements IWriteOnlyRequest<Double> {

        /**
         * Construct new LVar write request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         * @param value Value to be stored at the offset.
         */
        public DoubleLVarWriteRequest(int offset, Double value) {
            this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
            putDouble(value);
            this.offset = offset;
            this.type = RequestType.WRITE;
        }

        /**
         * Sets the value for this data request.
         *
         * @param value The value.
         */
        @Override
        public void setValue(Double value) {
            putDouble(value);
        }
    };

    /**
     * This class implements LVar write (write-only) request. It will accept Float (32-bit float value) value. The only
     * one available constructor takes 2 arguments. The first one is an offset at which FSUIPC will look for that value
     * to write to the Lvar. The second one is the value itself.
     */
    public static class FloatLVarWriteRequest extends DataRequest implements IWriteOnlyRequest<Float> {

        /**
         * Construct new LVar write request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         * @param value Value to be stored at the offset.
         */
        public FloatLVarWriteRequest(int offset, Float value) {
            this.dataBuffer = new byte[BUFFER_LENGTH_FLOAT];
            putFloat(value);
            this.offset = offset;
            this.type = RequestType.WRITE;
        }

        /**
         * Sets the value for this data request.
         *
         * @param value The value.
         */
        @Override
        public void setValue(Float value) {
            putFloat(value);
        }
    };

    /**
     * This class implements LVar write (write-only) request. It will accept Integer (32-bit integer value) value. The
     * only one available constructor takes 2 arguments. The first one is an offset at which FSUIPC will look for that
     * value to write to the Lvar. The second one is the value itself.
     */
    public static class IntegerLVarWriteRequest extends DataRequest implements IWriteOnlyRequest<Integer> {

        /**
         * Construct new LVar write request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         * @param value Value to be stored at the offset.
         */
        public IntegerLVarWriteRequest(int offset, Integer value) {
            this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            putInt(value);
            this.offset = offset;
            this.type = RequestType.WRITE;
        }

        /**
         * Sets the value for this data request.
         *
         * @param value The value.
         */
        @Override
        public void setValue(Integer value) {
            putInt(value);
        }
    };

    /**
     * This class implements LVar write (write-only) request. It will accept Short (16-bit integer value) value. The
     * only one available constructor takes 2 arguments. The first one is an offset at which FSUIPC will look for that
     * value to write to the Lvar. The second one is the value itself.
     */
    public static class ShortLVarWriteRequest extends DataRequest implements IWriteOnlyRequest<Short> {

        /**
         * Construct new LVar write request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         * @param value Value to be stored at the offset.
         */
        public ShortLVarWriteRequest(int offset, Short value) {
            this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            putShort(value);
            this.offset = offset;
            this.type = RequestType.WRITE;
        }

        /**
         * Sets the value for this data request.
         *
         * @param value The value.
         */
        @Override
        public void setValue(Short value) {
            putShort(value);
        }
    };

    /**
     * This class implements LVar write (write-only) request. It will accept Byte (8-bit integer value) value. The only
     * one available constructor takes 2 arguments. The first one is an offset at which FSUIPC will look for that value
     * to write to the Lvar. The second one is the value itself.
     */
    public static class ByteLVarWriteRequest extends DataRequest implements IWriteOnlyRequest<Byte> {

        /**
         * Construct new LVar write request.
         *
         * @param offset The offset at which the LVar value will be stored by FSUIPC.
         * @param value Value to be stored at the offset.
         */
        public ByteLVarWriteRequest(int offset, Byte value) {
            this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            this.dataBuffer[0] = value;
            this.offset = offset;
            this.type = RequestType.WRITE;
        }

        /**
         * Sets the value for this data request.
         *
         * @param value The value.
         */
        @Override
        public void setValue(Byte value) {
            this.dataBuffer[0] = value;
        }
    };

    /**
     * This class implements Lvar parameter request. It is an Integer type request, with offset 0x0D6C and is WRITE
     * type. The 0x0D6C offset is being used a parameter for FSUIPC Lvar read/write/create operations (requests). The
     * value of this request is expected to be the another offset. When we want to read LVar value, it is the offset at
     * which we want FSUIPC to store the LVar value for us. If we are writing/creating Lvar, it is the offset at which
     * FSUIPC will find out the value to write to the LVar / init the new Lvar with. The offset value is just the low 2
     * bytes if integer. The high 2 bytes specifies the format. There are predefined formats in the
     * {@link LVarValueFormat} enumeration. You can get the correct value to place in this request for your combination
     * of offset and format, when you just add the value from the enumeration to the offset. Like this:
     *
     * <pre><code>
     * //we will read LVar and want FSUIPC to store it at offset 0x66C0 as float.
     *
     * LVarParamRequest paramRequest = new LVarParamRequest(LVarValueFormat.FLOAT.getValue() + 0x66C0);
     * </code></pre>
     */
    public static class LVarParamRequest extends DataRequest implements IWriteOnlyRequest<Integer> {

        {
            this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            this.offset = PARAMETER_OFFSET;
            this.type = RequestType.WRITE;
        }

        /**
         * Creates a new LVar parameter request with initial value. Usually an offset and format at which to find the
         * read value or the value to write/init new LVar with combined with value format. Read the description of this
         * class for more info.
         *
         * @param value LVar parameter value.
         */
        public LVarParamRequest(Integer value) {
            putInt(value);
        }

        /**
         * Sets the LVar parameter value. Usually an offset and format at which to find the read value or the value to
         * write/init new LVar with combined with value format. Read the description of this class for more info.
         *
         * @param value The LVar parameter value.
         */
        @Override
        public void setValue(Integer value) {
            putInt(value);
        }
    }

    /**
     * This class implements Lvar control offset. It is a WRITE type request, writing a string at offset 0x0D70. This
     * string than identifies what should FSUIPC do - read Lvar, write Lvar or create Lvar. By default the control
     * request will tell FSUIPC to read the given Lvar. The Lvar name can be set via constructor or via the {@link #setValue(java.lang.String)
     * } method. The command - what to do, is being set by constructor or by {@link #setCommand(com.mouseviator.fsuipc.helpers.LVarHelper.LVarControlRequestCommand)
     * } method.
     */
    public static class LVarControlRequest extends DataRequest implements IWriteOnlyRequest<String> {

        private LVarControlRequestCommand command;
        private Charset charset = Charset.forName("UTF-8");

        {
            this.offset = CONTROL_OFFSET;
            this.type = RequestType.WRITE;
            this.command = LVarControlRequestCommand.READ;
        }

        /**
         * Constructs new Lvar control request to read given Lvar.
         *
         * @param lvar Lvar to read.
         */
        public LVarControlRequest(String lvar) {
            convertStringToByteArray(this.command.getValue() + lvar, 0, charset);
        }

        /**
         * Constructs new Lvar control request to read given Lvar.
         *
         * @param lvar Lvar to read.
         * @param charset Charset to use when encoding Lvar name and command to the byte array fro FSUIPC.
         */
        public LVarControlRequest(String lvar, Charset charset) {
            if (charset != null) {
                this.charset = charset;
            }
            convertStringToByteArray(this.command.getValue() + lvar, 0, charset);
        }

        /**
         * Constructs new Lvar control request to read/write to/create given Lvar.
         *
         * @param command A command - whether to read/write to or create given Lvar.
         * @param lvar An Lvar to read/write to/create.
         */
        public LVarControlRequest(LVarControlRequestCommand command, String lvar) {
            this.command = command;
            convertStringToByteArray(this.command.getValue() + lvar, 0, charset);
        }

        /**
         * Constructs new Lvar control request to read/write to/create given Lvar.
         *
         * @param command A command - whether to read/write to or create given Lvar.
         * @param lvar An Lvar to read/write to/create.
         * @param charset Charset to use when encoding Lvar name and command to the byte array for FSUIPC.
         */
        public LVarControlRequest(LVarControlRequestCommand command, String lvar, Charset charset) {
            if (charset != null) {
                this.charset = charset;
            }
            this.command = command;
            convertStringToByteArray(this.command.getValue() + lvar, 0, charset);
        }

        /**
         * Will set the value - the name of the lvar to be read/write to/created by this control request. Note that
         * command is {@link LVarControlRequestCommand#READ} by default, unless other command was set by the
         * {@link LVarControlRequest} constructor or {@link #setCommand(com.mouseviator.fsuipc.helpers.LVarHelper.LVarControlRequestCommand)
         * }. 
         *
         * @param value An Lvar name this control command is referencing.
         */
        @Override
        public void setValue(String value) {
            convertStringToByteArray(this.command.getValue() + value, 0, charset);
        }

        /**
         * Returns the command this control request is set to perform. This is, what should FSUIPC do with the Lvar this
         * control request is referencing to.
         *
         * @return The command.
         */
        public LVarControlRequestCommand getCommand() {
            return this.command;
        }

        /**
         * This method will set the command - what should FSUIPC do with the Lvar this control request is referencing.
         *
         * @param command The command.
         */
        public void setCommand(LVarControlRequestCommand command) {
            //get lvar name
            String currValue = new String(this.dataBuffer, this.charset).trim();
            String lvar = currValue.replace(this.command.getValue(), "").trim();
            //make new value
            this.command = command;
            convertStringToByteArray(this.command.getValue() + lvar, 0, charset);
        }

        /**
         * Returns the charset that is used to convert string containing Lvar name and command to the byte array for
         * FSUIPC.
         *
         * @return The charset used.
         */
        public Charset getCharset() {
            return charset;
        }

        /**
         * Sets the charset to use when converting string value that should contain Lvar name and command to the byte
         * array for FSUIPC. Call this before {@link #setValue(java.lang.String) }, otherwise the change will have no
         * effect.
         *
         * @param charset The charset to use.
         */
        public void setCharset(Charset charset) {
            this.charset = charset;
        }
    }

    /**
     * An enumeration representing a command for {@link LVarControlRequest}.
     */
    public static enum LVarControlRequestCommand {
        /**
         * A constant being used by {@link LVarControlRequest} request internally for creating string telling FSUIPC to
         * read given Lvar.
         */
        READ(":"),
        /**
         * A constant being used by {@link LVarControlRequest} request internally for creating string telling FSUIPC to
         * write to given Lvar.
         */
        WRITE("::"),
        /**
         * A constant being used by {@link LVarControlRequest} request internally for creating string telling FSUIPC to
         * create given Lvar.
         */
        CREATE(":::");

        private final String value;
        private static final Map<String, LVarControlRequestCommand> lookupTable = new HashMap<>();

        static {
            for (LVarControlRequestCommand type : EnumSet.allOf(LVarControlRequestCommand.class)) {
                lookupTable.put(type.getValue(), type);
            }
        }

        private LVarControlRequestCommand(String value) {
            this.value = value;
        }

        /**
         * @return Integer value of this type.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Returns {@link LVarControlRequestCommand} by corresponding int value.
         *
         * @param value String value corresponding to one of enumeration constants.
         * @return {@link LVarControlRequestCommand} by corresponding string value.
         * @throws InvalidParameterException if value not corresponding to any enumeration value is passed.
         */
        public static LVarControlRequestCommand get(String value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("Command value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }

    /**
     * An enumeration specifying a value format for LVar read and write functions.
     */
    public static enum LVarValueFormat {
        /**
         * 64-bit double
         */
        DOUBLE(0x00000),
        /**
         * 32-bit float
         */
        FLOAT(0x10000),
        /**
         * 32-bit signed integer
         */
        SIGNED_INTEGER(0x20000),
        /**
         * 32-bit unsigned integer
         */
        UNSIGNED_INTEGER(0x30000),
        /**
         * 16-bit signed integer (short)
         */
        SIGNED_SHORT(0x40000),
        /**
         * 16-bit unsigned integer (short)
         */
        UNSIGNED_SHORT(0x50000),
        /**
         * 8-bit signed integer (byte)
         */
        SIGNED_BYTE(0x60000),
        /**
         * 8-bit unsigned integer (byte)
         */
        UNSIGNED_BYTE(0x70000);

        private final int value;
        private static final Map<Integer, LVarValueFormat> lookupTable = new HashMap<>();

        static {
            for (LVarValueFormat type : EnumSet.allOf(LVarValueFormat.class)) {
                lookupTable.put(type.getValue(), type);
            }
        }

        private LVarValueFormat(int value) {
            this.value = value;
        }

        /**
         * @return Integer value of this type.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Returns {@link LVarValueFormat} by corresponding int value.
         *
         * @param value Integer value corresponding to one of enumeration constants.
         * @return {@link LVarValueFormat} by corresponding string value.
         * @throws InvalidParameterException if value not corresponding to any enumeration value is passed.
         */
        public static LVarValueFormat get(int value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("Value fromat: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }

    /**
     * A class representing a result fo many Lvar helper functions in the class {@link LVarHelper}. Reading/writing
     * to/creating an Lvar requires 3 data requests. One request to tell FSUIPC what to do with what Lvar - the {@link LVarResult#getControlRequest()
     * }, one request which is parameter fro the control request - the {@link LVarResult#getParamRequest() } and the
     * last one, which tells FSUIPC where to store the read result or where to find the value to write to the Lvar, or
     * initialize the new one with - the {@link LVarResult#getResultRequest() }. This class is just an wrapper to return
     * all of them from one function.
     */
    public static class LVarResult {

        private IDataRequest resultRequest = null;
        private LVarParamRequest paramRequest = null;
        private LVarControlRequest controlRequest = null;

        /**
         * Construts new uninitialzed LVarResult.
         */
        public LVarResult() {

        }

        /**
         * Constructs new LVarResult with reference to the data requests for Lvar manipulation.
         *
         * @param controlRequest A control request telling FSUIPC what to do whit what Lvar.
         * @param paramRequest A parameter data request for the control request.
         * @param resultRequest A request to read the result fo read, or holding the value for writing to/creating an
         * Lvar.
         */
        public LVarResult(LVarControlRequest controlRequest, LVarParamRequest paramRequest, IDataRequest resultRequest) {
            this.controlRequest = controlRequest;
            this.paramRequest = paramRequest;
            this.resultRequest = resultRequest;
        }

        /**
         *
         * @return The parameter request.
         */
        public LVarParamRequest getParamRequest() {
            return paramRequest;
        }

        /**
         * Sets the parameter request.
         *
         * @param paramRequest The parameter request to set.
         */
        public void setParamRequest(LVarParamRequest paramRequest) {
            this.paramRequest = paramRequest;
        }

        /**
         *
         * @return The control request.
         */
        public LVarControlRequest getControlRequest() {
            return controlRequest;
        }

        /**
         * Sets the control request.
         *
         * @param controlRequest The control request to set.
         */
        public void setControlRequest(LVarControlRequest controlRequest) {
            this.controlRequest = controlRequest;
        }

        /**
         *
         * @return The result request.
         */
        public IDataRequest getResultRequest() {
            return resultRequest;
        }

        /**
         * Sets the result request.
         * 
         * @param resultRequest The result request.
         */
        public void setResultRequest(IDataRequest resultRequest) {
            this.resultRequest = resultRequest;
        }
    }
}
