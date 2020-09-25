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
package com.mouseviator.fsuipc;

import java.security.InvalidParameterException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for fsuipc_java64.dll and fsuipc_java32.dll libraries. <strong>This class is based upon SDK written by Mark Burton, later amended by Paul Henty for 64 bit environment.</strong>
 *
 * 
 * <p>Why the rewrite? Well, the main reason for me was performance considerations. After studying FSUIPC C SDK, the wrapper library and the Java SDK by Mark, I found out
 * that the {@link FSUIPCWrapper#readData(int, int, byte[]) } and {@link FSUIPCWrapper#writeData(int, int, byte[]) } functions calls internally FSUIPC_Process() function
 * (within that C++ fsuipc_java64.dll, fsuipc_java32.dll). I have read on the forums that this function is quite heavy on processing time - thus, calling it after each data request is at least
 * not optimal. And I think it might cause trouble in bigger project where you need to update multiple data several times per second. Thus, I decided that I will try to write
 * my own wrapper, that will allow to store multiple read/write requests for FSUIPC and then process them all via one call to the process function. Following the approach, we
 * would use writing the code in C++.</p>
 * 
 * <p>I kept the above functions for those who are used to use them.... But there are new functions:</p>
 * 
 * <ul>
 * <li>{@link FSUIPCWrapper#read(int, int, byte[]) }</li>
 * <li>{@link FSUIPCWrapper#write(int, int, byte[]) }</li>
 * </ul>
 * 
 * <p>which will do the same as their counterparts within the C++ - store the read / write request, but will NOT immediately process them. To process them,
 * you have to call:</p>
 * 
 * <p>{@link FSUIPCWrapper#process() }</p>
 * 
 * <p>The other functions, that were present in the old SDK API, are the same with the same functionality:</p>
 * 
 * <ul>
 * <li>{@link FSUIPCWrapper#open(int) }</li>
 * <li>{@link FSUIPCWrapper#close()  }</li>
 * </ul>
 * 
 * <p>Oh, OK. The naming convention changed... the method/function names started with a BIG letter and returned no value, most of the time, now they start with
 * small letter and returns the value.... Just tried to make them the same as in FSUIPC C SDK. Also, some new functions were added:</p>
 * 
 * <ul>
 * <li>{@link FSUIPCWrapper#getVersion() }</li>
 * <li>{@link FSUIPCWrapper#getFSVersion() }</li>
 * <li>{@link FSUIPCWrapper#getLibVersion() }</li>
 * </ul>
 * 
 * <p>The above functions returns the respective variables defined within FSUIPC C library... Last, for now least, are functions:</p>
 * 
 * <ul>
 * <li>{@link FSUIPCWrapper#setupLogging(boolean, byte) }</li>
 * <li>{@link FSUIPCWrapper#setupLogging(boolean, java.lang.String, byte)  }</li>
 * <li>{@link FSUIPCWrapper#setupLogging(boolean, java.lang.String, byte, int)  }</li>
 * </ul>
 * 
 * <p>which enables logging to file - if you specify that by parameters. Then, a log file called fsuipc_java64.log is created by functions within the C++ library, so
 * we have at least some idea, what happens inside there...</p>
 * 
 * <p><strong>Please note that this class will NOT load either the fsuipc_java64.dll or fsuipc_java32.dll libraries.</strong> You have to do it before the first usage
 * of this class! But, the direct usage of this class is sort of cumbersome. Rather, use the {@link FSUIPC} class. It uses more object oriented approach, uses this class
 * internally and also provides functions, such as {@link FSUIPC#load() }, {@link FSUIPC#load32() } and {@link FSUIPC#load64() } to load the dll libraries.</p>
 * 
 * @author Mouseviator
 */
public class FSUIPCWrapper {
              
    /**
     * Connect to FS.
     *
     * @param aFlightSim Version of flight simulator to try to connect to.
     * @return False (0) if connection failed. True (non-zero) if connection opened.
     */
    public static synchronized native int open(int aFlightSim);

    /**
     * Close the connection to FSUIPC.
     */
    public static synchronized native void close();

    /**
     * Stores read request to read data from flight simulator.
     * 
     * @param aOffset An FSUIPC offset to read data from.
     * @param aSize A size of the data to read (in bytes).
     * @param aData A buffer to store the read data into.
     * @return Returns True (request successfully saved), or False (failed to save request). If request failed, we can get last error code by {@link #getResult() }
     */
    public static synchronized native int read(int aOffset, int aSize, byte[] aData);

    /**
     * Reads data from flight simulator. This function will internally call FSUIPC_Read to store read request for given data, and immediately will call FSUPC_Process to force
     * FSUIPC to process the request. The result will be available within aData right after function call. Note that this approach is suitable for small programs reading a couple
     * of values in not so short intervals. The IPC communication , which happens every time FSUP_Process is called...is quite time consuming. Calling this with every read/write request
     * is very UNEFFECTIVE. Use {@link #read(int, int, byte[]) } function to store multiple data read requests and than get all values using {@link #process() } function.
     * 
     * @param aOffset An FSUIPC offset to read data from.
     * @param aSize A size of the data to read (in bytes).
     * @param aData A buffer to store the read data into.
     * @return Returns True if data was read, or False in case of failure. If request failed, we can get last error code by {@link #getResult() } - in this case, it will be result of the FSUIPC_Process function (called internally by implementation).
     */
    public static synchronized native int readData(int aOffset, int aSize, byte[] aData);
    
    
    /**
     * Stores write request to write data to flight simulator.
     * 
     * @param aOffset An FSUIPC offset to write data to.
     * @param aSize A size of the data to write (in bytes).
     * @param aData A data to write.
     * @return Returns True (request successfully saved), or False (failed to save request). If request failed, we can get last error code by {@link #getResult() }
     */
    public static synchronized native int write(int aOffset, int aSize, byte[] aData);
    
    /**
     * Writes data to flight simulator.
     * 
     * @param aOffset An FSUIPC offset to write data to.
     * @param aSize A size of the data to write (in bytes).
     * @param aData A data to write.
     * @return Returns True if data write succeeded, or False in case of failure. If request failed, we can get last error code by {@link #getResult() } - in this case, it will be result of the FSUIPC_Process function (called internally by implementation).
     */
    public static synchronized native int writeData(int aOffset, int aSize, byte[] aData);

    /**
     * This function instructs FSUIPC to process all stored read/write requests. 
     * 
     * @return True if processing succeeded, False otherwise. In case of failure, get the last result by {@link #getResult() }.
     */
    public static synchronized native int process();        

    /**
     * Returns the last result code. It is integer value. All the known values are defined within {@link FSUIPCResult }
     * This value is changed in the calls of either one of {@link #read(int, int, byte[]) }, {@link #write(int, int, byte[]) }, {@link #process() }.
     * 
     * @return Integer value representing the last result.
     */
    public static synchronized native int getResult();

    /**
     * Returns the FSUIPC_FS_Version variable.
     * 
     * @return Integer value. Should match one of the {@link FSUIPCSimVersion} ... so, if you use {@link FSUIPCSimVersion#get(int) }, where parameter will be the number returned
     * by this function, it should not end up with exception and you should know what sim FSUIPC is connected to.
     */
    public static synchronized native int getFSVersion();

    /**
     * Returns the FSUIPC_Version variable.
     * @return Integer value. HIWORD is 1000 x Version Number, minimum 1998. LOWORD is build letter, with a = 1 etc. For 1998 this must be at least 5 (1998e).
     */
    public static synchronized native int getVersion();

    /**
     * Returns the FSUIPC_Lib_Version variable.
     * @return Integer value. HIWORD is 1000 x version, LOWORD is build letter, a = 1 etc.
     */
    public static synchronized native int getLibVersion();
    
    /**
     * This function will enable logging from the dll library... Can be useful when something does not work and you want to see
     * what happens inside the library. The <b>bEnableFileLogging</b>, when set to true, will enable logging to file. 
     * The <b>severityLevel</b> sets severity level - ie. how much log messages you want to see. See {@link FSUIPCWrapper.LogSeverity}
     * for possible values. With this function the log file name will be fsuipc_java64.log for 64bit library and fsuipc_java32.log for 32bit library. And the log
     * rotation file size will be 10MB.
     * 
     * 
     * @param bEnableFileLogging Whether to enable logging to file or not.
     * @param severityLevel Logging severity level.
     */
    public static synchronized native void setupLogging(boolean bEnableFileLogging, byte severityLevel);
    /**
     * This function will enable logging from the dll library...Can be useful when something does not work and you want to see
     * what happens inside the library. The <b>bEnableFileLogging</b>, when set to true, will enable logging to file. 
     * The <b>severityLevel</b> sets severity level - ie. how much log messages you want to see. See {@link FSUIPCWrapper.LogSeverity}
     * for possible values. The log rotation file size will be 10MB.
     * 
     * 
     * @param bEnableFileLogging Whether to enable logging to file or not.
     * @param fileName The name of the log file.
     * @param severityLevel Logging severity level.
     */
    public static synchronized native void setupLogging(boolean bEnableFileLogging, String fileName, byte severityLevel);
    /**
     * This function will enable logging from the dll library...Can be useful when something does not work and you want to see
     * what happens inside the library.The <b>bEnableFileLogging</b>, when set to true, will enable logging to file. 
     * The <b>severityLevel</b> sets severity level - ie. how much log messages you want to see. See {@link FSUIPCWrapper.LogSeverity}
     * for possible values. 
     * 
     * 
     * @param bEnableFileLogging Whether to enable logging to file or not.
     * @param fileName The name of the log file.
     * @param severityLevel Logging severity level.
     * @param rotationSize The maximum size of the log file in bytes.
     */
    public static synchronized native void setupLogging(boolean bEnableFileLogging, String fileName, byte severityLevel, int rotationSize);
    /**
     * Private constructor to prevent direct instantiation
     */
    private FSUIPCWrapper() {
    }       

    /**
     * An enumeration of known FSUIPC sim versions for passing as parameter to {@link #open(int) } function.
     */
    public static enum FSUIPCSimVersion {

        /**
         * Any simulator
         */
        SIM_ANY(0),

        /**
         * Flight Simulator 98
         */
        SIM_FS98(1),

        /**
         * Flight Simulator 2000
         */
        SIM_FS2K(2),

        /**
         * Combat Flight Simulator 2
         */
        SIM_CFS2(3),

        /**
         * Combat Flight Simulator 1
         */
        SIM_CFS1(4),

        /**
         * Fly!
         */
        SIM_FLY(5),

        /**
         * Flight Simulator 2002
         */
        SIM_FS2K2(6),

        /**
         * Flight Simulator 2004
         */
        SIM_FS2K4(7),

        /**
         * Flight Simulator X
         */
        SIM_FSX(8),

        /**
         * ESP
         */
        SIM_ESP(9),

        /**
         * Prepar3D
         */
        SIM_P3D(10),

        /**
         * Flight Simulator X 64bit
         */
        SIM_FSX64(11),

        /**
         * Prepar3D 64 bit
         */
        SIM_P3D64(12),
        
        /**
         * Flight Simulator 2020
         */
        SIM_MSFS(13);

        private final int value;

        private static final Map<Integer, FSUIPCSimVersion> lookupTable = new HashMap<>();

        static {
            for (FSUIPCSimVersion version : EnumSet.allOf(FSUIPCSimVersion.class)) {
                lookupTable.put(version.getValue(), version);
            }
        }

        private FSUIPCSimVersion(int value) {
            this.value = value;
        }

        /**
         * @return Integer value of this type.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Returns {@link FSUIPCSimVersion} by corresponding int value.
         *
         * @param value String value corresponding to one of enumeration
         * constants.
         * @return {@link FSUIPCSimVersion} by corresponding int value.
         * @throws InvalidParameterException if value not corresponding to any
         * enumeration value is passed.
         */
        public static FSUIPCSimVersion get(int value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("FSUIPC Sim version value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }

    /**
     * An enumeration of known result codes, that can be returned by calls to {@link #read(int, int, byte[]) }, {@link #write(int, int, byte[]) },
     * {@link #process() }. Use {@link #getResult() } to get the last result value.
     */
    public static enum FSUIPCResult {
        /**
         * No error
         */
        FSUIPC_ERR_OK(0), 
        /**
         * Attempt to Open() when connection is already open.
         */
        FSUIPC_ERR_OPEN(1), 	   
        /**
         * Cannot link to FSUIPC or WideClient
         */
        FSUIPC_ERR_NOFS(2), 
        /**
         * Failed to Register common message with Windows
         */
        FSUIPC_ERR_REGMSG(3), 
        /**
         * Failed to create Atom for mapping filename
         */
        FSUIPC_ERR_ATOM(4), 
        /**
         * Failed to create a file mapping object
         */
        FSUIPC_ERR_MAP(5), 
        /**
         * Failed to open a view to the file map
         */
        FSUIPC_ERR_VIEW(6), 
        /**
         * Incorrect version of FSUIPC, or not FSUIPC.
         */
        FSUIPC_ERR_VERSION(7), 
        /**
         * Flight Sim is not version requested by this application.
         */
        FSUIPC_ERR_WRONGFS(8), 
        /**
         * Attempted to call Process() but the FSUIPC link has not been opened.
         */
        FSUIPC_ERR_NOTOPEN(9), 
        /**
         * Call cannot execute: no requests accumulated
         */
        FSUIPC_ERR_NODATA(10), 
        /**
         * IPC SendMessage timed out (all retries)
         */
        FSUIPC_ERR_TIMEOUT(11), 
        /**
         * IPC SendMessage failed (all retries)
         */
        FSUIPC_ERR_SENDMSG(12), 
        /**
         * IPC request contains bad data        
         */
        FSUIPC_ERR_DATA(13), 
        /**
         * Wrong version of FSUIPC.  Can also occur if running on WideClient but FSUIPC is not running on server.
         */
        FSUIPC_ERR_RUNNING(14), 
        /**
         * Read or Write request cannot be added to the shared memory file as the file is full. 
         */
        FSUIPC_ERR_SIZE(15); 

        private final int value;

        private static final Map<Integer, FSUIPCResult> lookupTable = new HashMap<>();

        static {
            for (FSUIPCResult result : EnumSet.allOf(FSUIPCResult.class)) {
                lookupTable.put(result.getValue(), result);
            }
        }

        private FSUIPCResult(int value) {
            this.value = value;
        }

        /**
         * @return Integer value of this type.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Returns {@link FSUIPCResult} by corresponding int value.
         *
         * @param value String value corresponding to one of enumeration
         * constants.
         * @return {@link FSUIPCResult} by corresponding int value.
         * @throws InvalidParameterException if value not corresponding to any
         * enumeration value is passed.
         */
        public static FSUIPCResult get(int value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("FSUIPC Result value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }
    
    /**
     * <p>An enumeration of logging severity that can be used in {@link #setupLogging(boolean, byte) } function. The severity levels are:</p>
     * 
     * <ul>
     * <li>TRACE</li>
     * <li>DEBUG</li>
     * <li>INFO</li>
     * <li>WARNING</li>
     * <li>ERROR</li>
     * <li>FATAL</li>
     * </ul>
     * 
     * <p>when TRACE will give you all the log messages, and FATAL only the fatal log messages (when the Earth is about to stop turning...)</p>
     */
    public static enum LogSeverity {

        /**
         *
         */
        TRACE((byte)0),

        /**
         *
         */
        DEBUG((byte)1),

        /**
         *
         */
        INFO((byte)2),

        /**
         *
         */
        WARNING((byte)3),

        /**
         *
         */
        ERROR((byte)4),

        /**
         *
         */
        FATAL((byte)5);
        
        private final byte value;

        private static final Map<Byte, LogSeverity> lookupTable = new HashMap<>();

        static {
            for (LogSeverity severity : EnumSet.allOf(LogSeverity.class)) {
                lookupTable.put(severity.getValue(), severity);
            }
        }

        private LogSeverity(byte value) {
            this.value = value;
        }

        /**
         * @return Integer value of this type.
         */
        public byte getValue() {
            return this.value;
        }

        /**
         * Returns {@link LogSeverity} by corresponding int value.
         *
         * @param value String value corresponding to one of enumeration
         * constants.
         * @return {@link LogSeverity} by corresponding int value.
         * @throws InvalidParameterException if value not corresponding to any
         * enumeration value is passed.
         */
        public static LogSeverity get(byte value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("Log severity value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }
}
