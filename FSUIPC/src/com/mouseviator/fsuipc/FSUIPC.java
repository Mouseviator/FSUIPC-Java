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

import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;
import java.security.InvalidParameterException;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h2>General description</h2>
 *
 * <p>
 * This is another FSUIPC wrapper class... This one uses {@link FSUIPCWrapper}
 * to perform stuff with FSUIPC, but sort of hides the low-level approach
 * required by {@link FSUIPCWrapper} and hides it behind more object oriented
 * Java approach - or at least, I tried to achieve that...</p>
 *
 * <p>
 * The {@link FSUIPCWrapper} uses native methods and therefore, needs a dll
 * library to be loaded for it to work. But since the JVM can be 32bit, or
 * 64bit, the library implementing the native functions must also be 32bit or
 * 64bit. The {@link FSUIPCWrapper} will not load any library by itself.
 * Therefore, it is up the programmer to do it before using any (actually some,
 * but rather stick to all) functions from this (and therefore, the
 * {@link FSUIPCWrapper} class) class. But you don't have to write them
 * yourself, you can use {@link #load() } function (which will try to detect
 * platform by itself), or either one of {@link #load32() } and
 * {@link #load64() } if you have your own mechanism on detecting platform, such
 * as JNI.</p>
 *
 * <p>
 * This class is build upon a concept of data requests. Data requests simply are
 * data that you want to read/write to/from the simulator via FSUIPC. Each data
 * request is associated with the offset (what data you want to read/write, you
 * know that from other programming with FSUIPC), has its type - READ or WRITE.
 * For data request to be supported by this class, it must implement the
 * {@link IDataRequest} interface. You can also use the {@link DataRequest}
 * class as a foundation for building new data request object, or use any
 * predefined ones from the
 * {@link com.mouseviator.fsuipc.datarequest.primitives} package (which defines
 * data request for all primitive data types supported by FSUIPC, such as byte,
 * integer, string etc.).
 * </p>
 *
 * <p>
 * Gathering data to/from the simulator via FSUIPC's inter process communication
 * is time consuming, that is why it would be bad to do it each time we need i
 * single information, like airspeed update. This class has two arrays (queues)
 * for storing data requests. One is for one-time requests - the data, that you
 * need to read/write once, since they do not change often, such as situation
 * file. You can add requests to this queue using the {@link #addOneTimeRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)
 * } function. Add multiple requests and then process them all via single FSUIPC
 * inter process communication call using the {@link #processRequestsOnce() }
 * function. Note that if the processing is successful, the one-time queue will
 * be emptied (you will need to add requests again for another data exchange).
 * </p>
 *
 * <p>
 * The second type of queue is the one for continual data requests. For data,
 * that you need to read/write continuously, such as airspeed. You can add
 * requests to this queue using the {@link #addContinualRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)
 * } function. The process them all using the {@link #processRequests(int, boolean)
 * } function. This function will start a thread that will trigger the FSUIPC
 * inter process communication periodically in the interval specified when
 * calling the function, and all registered data requests will be updated every
 * time. If there are any request in the one-time queue, they will be processed
 * to, so there is no need to call {@link #processRequestsOnce() } after
 * starting the processing thread using the {@link #processRequests(int, boolean)
 * } function. To stop the processing thread, call the {@link #cancelRequestsProcessing()
 * } function.
 * </p>
 *
 * <p>
 * Well, I know it is nice that the processing thread updates the data, but how
 * would you know that it actually happened? There is an event listener for
 * this. Implement your own {@link IFSUIPCListener} and register it using the {@link #addListener(com.mouseviator.fsuipc.IFSUIPCListener)
 * } function. Then, FSUIPC class will call {@link IFSUIPCListener#onProcess(java.util.AbstractQueue)
 * }
 * function after each FSUIPC inter process communication requests are done (in
 * another words, after each FSUIPC process() function call). See also another
 * callback functions of the {@link IFSUIPCListener} interface. I think they are
 * pretty self-explanatory
 * </p>
 *
 * <h3>Usage example</h3>
 *
 * <p>
 * This is basic example on using this class. First of all, we need to get an
 * instance of it.</p>
 *
 * <pre><code>
 *  FSUIPC fsuipc = FSUIPC.getInstance();
 * </code></pre>
 *
 * <p>
 * Than we need to load the native library. In the code below, we do that using
 * the {@link #load() } function, which will try to determine whether to load 32
 * bit, or 64 bit library automatically. But you can also use {@link #load32() }
 * or {@link #load64() } to load specific version if you have your own logic for
 * determining JVM platform. When the library is loaded, we can connect to
 * FSUIPC.</p>
 *
 * <pre><code>
 *   byte result = FSUIPC.load();
 *   if (result != FSUIPC.LIB_LOAD_RESULT_OK) {
 *       System.out.println("Failed to load native library. Quiting...");
 *       return;
 *   }
 *
 *   int ret = fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
 *   if (ret == 0) {
 *       System.out.println("Flight sim not found");
 *   } else {
 *       System.out.println("Flight Sim found!");
 *   }
 * </code></pre>
 *
 * <p>
 * Now, we can create some data request to read/write some data to/from
 * simulator. In the code below, we create data request to read airspeed and
 * register it for one time processing.</p>
 *
 * <pre><code>
 *  //Helper for gathering aircraft data
 *  AircraftHelper aircraftHelper = new AircraftHelper();
 *
 *  //Get IAS data request and register it for time processing
 *  FloatRequest ias = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getIAS());
 *
 *  //Let FSUIPC process all one-time requests
 *  int ret = fsuipc.processRequestsOnce();
 *
 *  //Later on, print the requested data
 *  if (ret == FSUIPC.PROCESS_RESULT_OK) {
 *      System.out.println("Aircraft IAS: " + String.valueOf(ias.getValue()));
 *  }
 * </code></pre>
 *
 * <p>
 * Note that in the code above, we are using the helper method {@link AircraftHelper#getIAS()
 * } to get the IAS data request. That helper provides modified request object,
 * that will return the IAS in Kts as float value, even though that FSUIPC
 * itself will return it as integer value * 128. The modified data request
 * {@link IDataRequest#getValue() } will perform the calculation for us. The
 * code above is the same as this: </p>
 *
 * <pre><code>
 *  //Get IAS data request and register it for time processing
 *  IntRequest ias = new IntRequest(0x02BC);
 *  fsuipc.addOneTimeRequest(ias);
 *
 *  //Let FSUIPC process all one-time requests
 *  int ret = fsuipc.processRequestsOnce();
 *
 *  //Later on, print the requested data
 *  if (ret == FSUIPC.PROCESS_RESULT_OK) {
 *      System.out.println("Aircraft IAS (method 2): " + String.valueOf(ias.getValue() / 128.0f));
 *  }
 * </code></pre>
 *
 * <p>
 * If we want to monitor the airspeed continuously, we would register it with {@link #addContinualRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)
 * } rather than with {@link #addOneTimeRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)
 * }. In this case, it would also be nice to define the listener so that we know
 * when processing happened. Note that the example uses logger variable, which
 * is for logging messages (if you don't know about logging in Java, just ignore
 * it) - kind of instead of System.out.println... Also notice, that inside the {@link IFSUIPCListener#onProcess(java.util.AbstractQueue)
 * } callback we use <code>SwingUtilities.invokeLater</code>. This is because
 * the processing happens in FSUIPC class processing thread - NOT EDT thread...
 * and all GUI updates should happen on EDT thread...that is how to do it. The
 * last line of code in this example, that cancels the requests processing, is
 * not really needed. The FSUIPC class check the last result code after each to
 * call to FSUIPC's processing function and it will cancel the processing thread
 * once it finds out that FSUIPC connection to sim has been lost. But you can of
 * course call it if you want to stop the processing for any reason.</p>
 *
 * <pre><code>
 * //Helper for gathering aircraft data
 * AircraftHelper aircraftHelper = new AircraftHelper();
 *
 * //Get IAS data request and register it for time processing
 * FloatRequest ias = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getIAS());
 *
 * //We will get results from continual request processing using the listener
 * IFSUIPCListener fsuipcListener = new IFSUIPCListener() {
 *       &#64;Override
 *       public void onConnected() {
 *          logger.info("FSUIPC connected!");
 *       }
 *
 *       &#64;Override
 *       public void onDisconnected() {
 *          logger.info("FSUIPC disconnected!");
 *       }
 *
 *       &#64;Override
 *       public void onProcess(AbstractQueue&lt;IDataRequest&gt; arRequests) {
 *          System.out.println("FSUIPC continual request processing callback!");
 *
 *          //GUI updates on EDT thread
 *          SwingUtilities.invokeLater(new Runnable() {
 *              &#64;Override
 *              public void run() {
 *                  lblIAS.setText(String.format("%d Kts", (int) Math.ceil(ias.getValue())));
 *              }
 *          }
 *       }
 *
 *       &#64;Override
 *       public void onFail(int lastResult) {
 *           logger.log(Level.INFO, "Last FSUIPC function call ended with error code: {0}, message: {1}",
 *                 new Object[]{lastResult,
 *                    FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult))});
 *       }
 * }
 *
 * //Add our listener to FSUIPC
 * fsuipc.addListener(fsuipcListener);
 *
 * //Start continual processing, every 250 miliseconds
 * fsuipc.processRequests(250, true);
 *
 * .
 * .
 * .
 * .
 *
 * //Later, stop processing
 * fsuipc.cancelRequestsProcessing();
 * </code></pre>
 *
 * <p>
 * When we are finished, we should disconnect the FSUIPC (this will also release
 * used resources).</p>
 *
 * <pre><code>
 *  fsuipc.disconnect();
 * </code></pre>
 *
 * @author Mouseviator
 */
public class FSUIPC {

    /**
     * A code returned by {@link #processRequests(int, boolean) } or {@link #processRequestsOnce()
     * } if no requests have been registered.
     */
    public static final int PROCESS_RESULT_REQUESTS_EMPTY = 512;

    /**
     * A code returned by {@link #processRequests(int, boolean) } or {@link #processRequestsOnce()
     * } if FSUIPC fails to register any of data request.
     */
    public static final int PROCESS_RESULT_REQUESTS_STORE_FAILED = 513;

    /**
     * A code returned by {@link #process() } if FSUIPC process function returns
     * any other value than OK.
     */
    public static final int PROCESS_RESULT_REQUESTS_PROCESS_FAILED = 514;

    /**
     * A code returned by {@link #processRequests(int, boolean) }, {@link #process()
     * } or {@link #processRequestsOnce() } if FSUIPC process function return no
     * errors.
     */
    public static final int PROCESS_RESULT_OK = 500;

    /**
     * A code returned by {@link #processRequests(int, boolean) } if it fails to
     * cancel already running processing thread.
     */
    public static final int PROCESS_RESULT_FAILED_TO_CANCEL_THREAD = 515;

    /**
     * A code returned by {@link #processRequests(int, boolean) } if it fails to
     * start processing thread.
     */
    public static final int PROCESS_RESULT_FAILTED_TO_START_THREAD = 516;

    /**
     * A code returned by {@link #processRequests(int, boolean) } if processing
     * thread is already running and you did not specify to stop it when calling
     * the function.
     */
    public static final int PROCESS_RESULT_THREAD_ALREADY_RUNNING = 517;
    /**
     * Name of 64 bit library implementation
     */
    public static final String LIBRARY_NAME64 = "fsuipc_java64";
    /**
     * Name of 32 bit library implementation
     */
    public static final String LIBRARY_NAME32 = "fsuipc_java32";
    /**
     * This result is returned by {@link #load() }, {@link #load32() } and {@link #load64()
     * } if loading of native library succeeds.
     */
    public static final byte LIB_LOAD_RESULT_OK = 0;
    /**
     * This result is returned by {@link #load() }, {@link #load32() } and {@link #load64()
     * } if loading of native library fails.
     */
    public static final byte LIB_LOAD_RESULT_FAILED = 1;
    /**
     * This result is returned by {@link #load() }, {@link #load32() } and {@link #load64()
     * } if native library is already loaded when calling these functions.
     */
    public static final byte LIB_LOAD_RESULT_ALREADY_LOADED = 3;

    private static boolean libraryLoaded = false;
    /**
     * Reference to this FSUIPC. For singleton approach.
     */
    private static FSUIPC INSTANCE = null;
    /**
     * A logger for this class
     */
    private static final Logger logger = Logger.getLogger(FSUIPC.class.getName());

    /**
     * A has map with error messages, giving some explanation to FSUIPC result
     * values. There is a maessage for each value of
     * {@link FSUIPCWrapper.FSUIPCResult}
     */
    public static final HashMap<FSUIPCWrapper.FSUIPCResult, String> FSUIPC_ERROR_MESSAGES = new HashMap<FSUIPCWrapper.FSUIPCResult, String>() {
        {
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_OK, "Okay");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_OPEN, "Attempt to Open when already Open");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_NOFS, "Cannot link to FSUIPC or WideClient");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_REGMSG, "Failed to Register common message with Windows");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_ATOM, "Failed to create Atom for mapping filename");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_MAP, "Failed to create a file mapping object");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_VIEW, "Failed to open a view to the file map");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_VERSION, "Incorrect version of FSUIPC, or not FSUIPC");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_WRONGFS, "Sim is not version requested");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_NOTOPEN, "Call cannot execute, link not Open");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_NODATA, "Call cannot execute: no requests accumulated");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_TIMEOUT, "IPC timed out all retries");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_SENDMSG, "IPC sendmessage failed all retries");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_DATA, "IPC request contains bad data");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_RUNNING, "Maybe running on WideClient, but FS not running on Server, or wrong FSUIPC");
            put(FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_SIZE, "Read or Write request cannot be added, memory for Process is full");
        }
    };

    /**
     * A has map with simulator names, giving some explanation to FSUIPC
     * supported simulator values. There is a message for each value of
     * {@link FSUIPCWrapper.FSUIPCSimVersion}
     */
    public static final HashMap<FSUIPCWrapper.FSUIPCSimVersion, String> FSUIPC_SIM_VERSION_TEXT = new HashMap<FSUIPCWrapper.FSUIPCSimVersion, String>() {
        {
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY, "Any");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FS98, "Flight Simulator 98");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FS2K, "Flight Simulator 2000");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_CFS2, "Combat Flight Simulator 2");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_CFS1, "Combat Flight Simulator 1");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FLY, "Fly!");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FS2K2, "Flight Simulator 2002");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FS2K4, "Flight Simulator 2004");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FSX, "Flight Simulator X");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_ESP, "ESP");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_P3D, "Prepar3D");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_FSX64, "Flight Simulator X (64bit)");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_P3D64, "Prepar3D (64bit)");
            put(FSUIPCWrapper.FSUIPCSimVersion.SIM_MSFS, "Microsoft Flight Simulator (2020)");
        }
    };

    /**
     * @return Instance of FSUIPC.
     */
    public static synchronized FSUIPC getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FSUIPC();
        }
        return INSTANCE;
    }

    /**
     * This function will try to load the 32bit/64bit native library that implements this
     * wrapper native methods. Note that it tries to determine the architecture
     * by reading the "sun.arch.data.model" system property and then call {@link #load32()
     * } or {@link #load64() } based on result. The result can also be
     * "unknown", by that case non of the functions will be called. Note that
     * this is not bulletproof, if you ae using JNI or similar API, use it and
     * call 32/64 bit load function by yourself.
     *
     * @return Will return {@link #LIB_LOAD_RESULT_OK} if the library is
     * successfully loaded, {@link #LIB_LOAD_RESULT_FAILED} if the loading of
     * the library fails and {@link #LIB_LOAD_RESULT_ALREADY_LOADED} if the
     * library is already loaded (either 32 or 64 bit one)
     */
    public static byte load() {
        byte result = LIB_LOAD_RESULT_FAILED;

        try {
            String arch = System.getProperty("sun.arch.data.model");
            if (arch.equals("32")) {
                result = load32();
            } else if (arch.equals("64")) {
                result = load64();
            } else {
                Logger.getLogger(FSUIPCWrapper.class.getName()).log(Level.SEVERE, "Failed to determine system architecture to decide what version of library to load!");
            }
        } catch (Exception ex) {
            Logger.getLogger(FSUIPCWrapper.class.getName()).log(Level.SEVERE, "Failed to load native implementation library!", ex);
        }

        return result;
    }

    /**
     * This function will try to load 32bit version of native library that
     * implements this wrapper native methods.
     *
     * @return Will return {@link #LIB_LOAD_RESULT_OK} if the library is
     * successfully loaded, {@link #LIB_LOAD_RESULT_FAILED} if the loading of
     * the library fails and {@link #LIB_LOAD_RESULT_ALREADY_LOADED} if the
     * library is already loaded (either 32 or 64 bit one)
     */
    public static byte load32() {
        byte result = LIB_LOAD_RESULT_FAILED;
        //if lib not loaded yet, try to load it
        if (!libraryLoaded) {
            try {
                System.loadLibrary(LIBRARY_NAME32);
                logger.info("Loaded library: " + LIBRARY_NAME32);
                libraryLoaded = true;
                result = LIB_LOAD_RESULT_OK;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to load native implementation library: " + LIBRARY_NAME32, ex);
                libraryLoaded = false;
            }
        } else {
            result = LIB_LOAD_RESULT_ALREADY_LOADED;
        }

        return result;
    }

    /**
     * This function will try to load 64bit version of native library that
     * implements this wrapper native methods.
     *
     * @return Will return {@link #LIB_LOAD_RESULT_OK} if the library is
     * successfully loaded, {@link #LIB_LOAD_RESULT_FAILED} if the loading of
     * the library fails and {@link #LIB_LOAD_RESULT_ALREADY_LOADED} if the
     * library is already loaded (either 32 or 64 bit one)
     */
    public static byte load64() {
        byte result = LIB_LOAD_RESULT_FAILED;
        //if lib not loaded yet, try to load it
        if (!libraryLoaded) {
            try {
                System.loadLibrary(LIBRARY_NAME64);
                logger.info("Loaded library: " + LIBRARY_NAME64);
                libraryLoaded = true;
                result = LIB_LOAD_RESULT_OK;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to load native implementation library: " + LIBRARY_NAME64, ex);
                libraryLoaded = false;
            }
        } else {
            result = LIB_LOAD_RESULT_ALREADY_LOADED;
        }

        return result;
    }

    /**
     * An array of one time data requests
     */
    private final AbstractQueue<IDataRequest> arOneTimeRequests = new ConcurrentLinkedQueue<>();

    /**
     * An array for holding repeated requests
     */
    private final AbstractQueue<IDataRequest> arContinualRequests = new ConcurrentLinkedQueue<>();

    /**
     * An array of FSUIPC event listeners
     */
    private final List<IFSUIPCListener> arListeners = new LinkedList<>();

    /**
     * Whether the lib is connected to FSUIPC or not
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);
    /**
     * Thread pool for running continuous requests
     */
    private ScheduledExecutorService scheduledESForCRPTask = null;
    /**
     * Thread pool for wait for connection task
     */
    private ScheduledExecutorService scheduledESForWfCTask = null;
    /**
     * A reference to task that waits for FSUIPC connection
     */
    private ScheduledFuture waitForConnectionThread = null;
    /**
     * A reference to task that process requests continually
     */
    private ScheduledFuture continualRequestProcessThread = null;
    /**
     * this variable stores last processing time. It is being updated in each
     * call of {@link #process() } function.
     */
    private long lastProcessingTime = 0;

    /**
     * This function will check last FSUIPC result and change library state if
     * desired.
     */
    private void checkLastResult() {
        int lastResult = FSUIPCWrapper.getResult();
        
        // in case of some error, inform all listeners
        if (lastResult != FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_OK.getValue()) {
            logger.log(Level.FINER, "Something did not go as planned! FSUIPC last result code is: {0}. Letting the listeners know!", lastResult);
            arListeners.forEach(listener -> {
                listener.onFail(lastResult);
            });
        }

        //check if disconnected
        if (lastResult == FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_NOFS.getValue()
                || lastResult == FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_NOTOPEN.getValue()
                || lastResult == FSUIPCWrapper.FSUIPCResult.FSUIPC_ERR_SENDMSG.getValue()) {
            logger.log(Level.FINER, "It seems that connection to FSUIPC has been lost! Last result code is: {0}. Will call disconnect!", lastResult);
            setConnected(false);
        }
    }

    /**
     * A function to set connected state. It will also call onConnected listener
     * if one defined.
     *
     * @param connected
     */
    private void setConnected(boolean connected) {
        boolean bChange = (this.connected.get() != connected); // did the value change?
        this.connected.set(connected);

        // if there was a change in the connected state, let all listeners know
        if (bChange) {
            if (connected) {
                //if the task waiting for success connection is running and connected, we will cancel it        
                logger.finer("FSUIPC connected! Canceling thread that waits for FSUIPC connection.");
                cancelWaitForConnectionTask();
            } else {
                //if disconnected, cancel processing tasks
                logger.finer("FSUIPC disconnected! Will cancel continual requests processing thread.");
                cancelRequestsProcessing();
            }

            //let all listeners know
            arListeners.forEach(listener -> {
                if (connected) {
                    listener.onConnected();
                } else {
                    listener.onDisconnected();
                }
            });
        }
    }

    /**
     * This will add listener. If the listener is already present, it will not be added.
     *
     * @param listener A listener to add.
     * @return True if listener was added, false otherwise (you passed null, or
     * what {@link Collection#add(java.lang.Object) } returns.).
     */
    public boolean addListener(IFSUIPCListener listener) {
        if (listener != null && !arListeners.contains(listener)) {
            return arListeners.add(listener);
        }
        return false;
    }

    /**
     * This will remove registered listener.
     *
     * @param listener A listener to remove.
     * @return True if listener was removed, false otherwise (you passed null,
     * or what {@link Collection#remove(java.lang.Object) } returns.).
     */
    public boolean removeListener(IFSUIPCListener listener) {
        if (listener != null) {
            return arListeners.remove(listener);
        }
        return false;
    }
    
    /**
     * This will remove all listeners
     */
    public void removeAllListeners() {
        arListeners.clear();
    }

    /**
     * Open FSUIPC connection to selected simulator version.
     *
     * @param simVersion A simulator to connect to.
     * @return 0 if not connected, 1 (or non-zero) when successfully connected.
     */
    public int connect(FSUIPCWrapper.FSUIPCSimVersion simVersion) {
        int iRet = FSUIPCWrapper.open(simVersion.getValue());
        if (iRet != 0) {
            logger.info("Connection to FSUIPC opened.");
            setConnected(true);
        } else {
            logger.finer("Failed to open connection to FSUIPC!");
            setConnected(false);
        }
        return iRet;
    }
    
    /**
     * Will return the value of the internal {@link AtomicBoolean} variable that is being updated when
     * FSUIPC is connected/disconnected.
     * 
     * @return True if FSUIPC is connected, false otherwise.
     */
    public boolean isConnected() {
        return this.connected.get();
    }

    /**
     * This method will init thread that will continuously try to connect to
     * simulator via FSUIPC till success.
     *           
     * <strong>WARNING:</strong> This functions will first try to stop any currently running waiting thread. This might take some time - thus may
     * block the current thread! Should not be called from main EDT thread.
     * 
     * @param simVersion A simulator version to which to connect.
     * @param repeatPeriod A time in seconds to repeat the connection attempts.
     * @return True if thread is successfully started, false otherwise.
     */
    public boolean waitForConnection(FSUIPCWrapper.FSUIPCSimVersion simVersion, int repeatPeriod) {
        try {
            if (cancelWaitForConnectionTask()) {
                //Init task executor, if not initialized yet
                if (scheduledESForWfCTask == null) {
                    //Creating thread pool with only one thread will make sure that no more than one thread will be waiting for the connection at one time
                    scheduledESForWfCTask = Executors.newScheduledThreadPool(1);
                }
                //start waiting thread
                waitForConnectionThread = scheduledESForWfCTask.scheduleAtFixedRate(new WaitForConnectionWorker(simVersion), 0, repeatPeriod, TimeUnit.SECONDS);
                logger.log(Level.FINER, "Started new task to wait to connection to sim via FSUIPC. Required sim version is: {0} and repeat period is: {1} seoonds.", new Object[]{getFSVersion(simVersion), repeatPeriod});
                return true;
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to schedule new task to wait for FSUIPC connection.", ex);
        }
        return false;
    }

    /**
     * Cancels the task that wait s for FSUIPC connection.
     *
     * @return True if Ok, False in case of any exception.
     */
    private boolean cancelWaitForConnectionTask() {
        //if not running we will return true also
        try {
            if (waitForConnectionThread != null) {
                //cancel any currently running task
                ((ScheduledThreadPoolExecutor) scheduledESForWfCTask).setRemoveOnCancelPolicy(true);
                waitForConnectionThread.cancel(true);
                //wait for the task to complete
                while (!waitForConnectionThread.isDone()) {
                    try {
                        /* It is said that using Thread.sleep in the loop is bad design pattern, but here, I do not know about
                           better solution for waiting for the thread to be completly finished.
                        */
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        //just continue on processing
                        logger.log(Level.WARNING, "Interrupted while waiting for wait for FSUIPC connection thread to finish!", ex);
                    }
                }
                waitForConnectionThread = null; //important so that the condition above works next time
                logger.finer("Thread to wait for FSUIPC connection was canceled!");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to cancel thread waiting for FSUIPC connection!", ex);
            return false;
        }
        return true;
    }

    /**
     * This function will cancel the thread that is running the continual
     * request processing.
     * 
     * <strong>WARNING:</strong> This functions will try to stop any currently running processing thread. This might take some time - thus may
     * block the current thread! Should not be called from main EDT thread.
     *
     * @return True if thread was canceled, false if there was some problem
     * (exception).
     */
    public boolean cancelRequestsProcessing() {
        try {
            if (continualRequestProcessThread != null) {
                //cancel any currently running task
                ((ScheduledThreadPoolExecutor) scheduledESForCRPTask).setRemoveOnCancelPolicy(true);
                //this one, we will let finish if already running - the false parameter
                continualRequestProcessThread.cancel(false);
                //wait for the task to complete
                while (!continualRequestProcessThread.isDone()) {
                    try {
                        /* It is said that using Thread.sleep in the loop is bad design pattern, but here, I do not know about
                           better solution for waiting for the thread to be completly finished.
                        */
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        //just continue on processing
                        logger.log(Level.WARNING, "Interrupted while waiting for continual request processing thread to finish!", ex);
                    }
                }
                continualRequestProcessThread = null;   //important so that the condition above works next time
                //clear array of requests
                arContinualRequests.clear();        //added as anothe call to start request processing in one session would add request (double them and so on)
                logger.finer("Thread for FSUIPC continual requests processing was canceled!");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to cancel thread for FSUIPC continual requests processing!", ex);
            return false;
        }               
        return true;
    }

    /**
     * Closes connection to FSUIPC.
     */
    public void disconnect() {
        logger.info("Called disconnect! Will close FSUIPC connection and release resoures.");
        setConnected(false);

        //cancel runnig  threads waiting for FSUIPC connection if any        
        if (scheduledESForWfCTask != null) {
            try {
                scheduledESForWfCTask.shutdown();
                while (!scheduledESForWfCTask.isShutdown()) {
                    //wait for tasks to shutdown
                    try {
                        /* It is said that using Thread.sleep in the loop is bad design pattern, but here, I do not know about
                           better solution for waiting for the executor to be completly shutdown. We could call awaitTermination,
                           but it has tim limit and if tasks does not finish within limit, than we still have to take another action,
                           so may not be sure the executor finished all and was shutdown
                        */
                        Thread.sleep(50);                        
                    } catch (InterruptedException ex) {
                        //just continue on processing
                        logger.log(Level.WARNING, "Interrupted while waiting for \"Wait for FSUIPC connection\" task executor to shutdown!", ex);
                    }
                }
                scheduledESForWfCTask = null;
                waitForConnectionThread = null;
                logger.finer("The \"Wait for FSUIPC connection\" task executor is shutdown. Thread waiting for connection is terminated.");
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to terminate the \"Wait for FSUIPC connection\" task executor! The waiting thread might still be running!", ex);
            }
        }

        //terminate all running continual request processing threads
        if (scheduledESForCRPTask != null) {
            try {
                scheduledESForCRPTask.shutdown();
                while (!scheduledESForCRPTask.isShutdown()) {
                    //wait for tasks to shutdown
                    try {
                        /* It is said that using Thread.sleep in the loop is bad design pattern, but here, I do not know about
                           better solution for waiting for the executor to be completly shutdown. We could call awaitTermination,
                           but it has tim limit and if tasks does not finish within limit, than we still have to take another action,
                           so may not be sure the executor finished all and was shutdown
                        */
                        Thread.sleep(50);                        
                    } catch (InterruptedException ex) {
                        //just continue on processing
                        logger.log(Level.WARNING, "Interrupted while waiting for \"Continual requests processing\" scheduled task executor to shutdown!", ex);
                    }
                }
                scheduledESForCRPTask = null;
                continualRequestProcessThread = null;
                arContinualRequests.clear();    //added as another call to start request processing in one session would add request (double them and so on)
                logger.finer("The \"Continual requests processing\" task executor is shutdown. Thread performing continual request processing is terminated.");
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to terminate the \"Continual requests processing\" executor service! The thread performing continual request processing might still be running!", ex);
            }
        }

        //close FSUIPC connection. We do it as last command, after cancelling all processing, as doing it as firts, may
        //cause many calls to onFail, while it is actaully not fail. This function is not called internally, so the disconnect
        //should be called only when we really want to
        FSUIPCWrapper.close();
    }

    /**
     * Return the last result from last FSUIPC operation. This should be one
     * from {@link FSUIPCWrapper.FSUIPCResult }
     *
     * @return {@link FSUIPCWrapper.FSUIPCResult } of last FSUIPC function call.
     * @throws InvalidParameterException if {@link FSUIPCWrapper#getResult() }
     * return value not supported by {@link FSUIPCWrapper.FSUIPCResult }. That
     * would mean this library is outdated!
     */
    public FSUIPCWrapper.FSUIPCResult getLastResult() throws InvalidParameterException {
        return FSUIPCWrapper.FSUIPCResult.get(FSUIPCWrapper.getResult());
    }

    /**
     * This function will return a message from {@link #FSUIPC_ERROR_MESSAGES}
     * for the last FSUIPC function call. It will call {@link #getLastResult() }
     * internally to get the last result code.
     *
     * @return A string error message for the last FSUIPC function call.
     * @throws InvalidParameterException if {@link #getLastResult() } throws the
     * same Exception.
     */
    public String getLastErrorMessage() throws InvalidParameterException {
        return FSUIPC_ERROR_MESSAGES.get(getLastResult());
    }

    /**
     * This function will return one text from {@link #FSUIPC_SIM_VERSION_TEXT}
     * based on the result of the call of the function {@link FSUIPCWrapper#getFSVersion()
     * }. If it fails to convert result of function {@link FSUIPCWrapper#getFSVersion()
     * } to {@link FSUIPCWrapper.FSUIPCSimVersion}, the returned value will be
     * an empty string.
     *
     * @return String value.
     */
    public String getFSVersion() {
        try {
            FSUIPCWrapper.FSUIPCSimVersion simVersion = FSUIPCWrapper.FSUIPCSimVersion.get(FSUIPCWrapper.getFSVersion());
            return FSUIPC_SIM_VERSION_TEXT.get(simVersion);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to get FS version!", ex);
        }
        return "";
    }

    /**
     * This function will return one text from {@link #FSUIPC_SIM_VERSION_TEXT}
     * based on the result of the call of the function {@link FSUIPCWrapper#getFSVersion()
     * }.If it fails to convert result of function
     * {@link FSUIPCWrapper#getFSVersion()} to
     * {@link FSUIPCWrapper.FSUIPCSimVersion}, the returned value will be an
     * empty string.
     *
     * @param simVersion A sim version to get string representation for.
     * @return String value.
     */
    public String getFSVersion(FSUIPCWrapper.FSUIPCSimVersion simVersion) {
        try {
            return FSUIPC_SIM_VERSION_TEXT.get(simVersion);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to get FS version!", ex);
        }
        return "";
    }

    /**
     * Returns string representation of FSUIPC version. Calls {@link FSUIPCWrapper#getVersion()
     * } to get FSUIPC version as number first.
     *
     * @return String representation of FSUIPC version.
     */
    public String getVersion() {
        int version = FSUIPCWrapper.getVersion();

        //Below code copied from SDK UIPCHello.c
        //hiword is FSUIPC version - BCD encoded
        //loword is build letter, 0 = none, 1-26 = a-z
        String sVersion = String.format("%c.%c%c%c%c",
                (char) ('0' + (0x0f & (version >> 28))),
                (char) ('0' + (0x0f & (version >> 24))),
                (char) ('0' + (0x0f & (version >> 20))),
                (char) ('0' + (0x0f & (version >> 16))),
                (version & 0xffff) > 0 ? (char) ('a' + (version & 0xff) - 1) : ' ');
        return sVersion;
    }

    /**
     * Returns string representation of FSUIPC library version. Calls {@link FSUIPCWrapper#getVersion()
     * } to get FSUIPC version as number first. Note that this version number is
     * hard-coded in FSUIPC C lib and was not updated for years :)
     *
     * @return String representation of FSUIPC library version.
     */
    public String getLibVersion() {
        //According to documentation the values hould be stored the same as the fsuipc vesrion, but it does not give meaningfull value
        int version = FSUIPCWrapper.getLibVersion();

        return String.format("%.3f", version / 1000.f);
    }

    /**
     * This method will add data request to the one-time requests array.This
     * array will be emptied once successfully processed via the
     * {@link #processRequestsOnce()} function.
     *
     * @param dataRequest Read or Write data request.
     * @return The passed <b>dataRequest</b> if not null, otherwise null.
     */
    public IDataRequest addOneTimeRequest(IDataRequest dataRequest) {
        if (dataRequest != null) {
            arOneTimeRequests.add(dataRequest);
            return dataRequest;
        }
        return null;
    }

    /**
     * This method will add data request to the continual requests array.
     *
     * @param dataRequest Read or Write data request.
     * @return The passed <b>dataRequest</b> if not null, otherwise null.
     */
    public IDataRequest addContinualRequest(IDataRequest dataRequest) {
        if (dataRequest != null) {
            arContinualRequests.add(dataRequest);
            return dataRequest;
        }
        return null;
    }

    /**
     * This function will remove data request from continual requests array.
     *
     * @param dataRequest A data request to remove.
     * @return True if continual requests array changed (the request was
     * removed).
     */
    public boolean removeContinualRequest(IDataRequest dataRequest) {
        return arContinualRequests.remove(dataRequest);
    }

    /**
     * This will clear an array of continual requests and stop continual request
     * processing thread.
     *
     * @return The result of call to {@link #cancelRequestsProcessing() }, which
     * is done before the requests array is cleared.
     */
    public boolean clearContinualRequests() {
        //stop continual request thread
        boolean bRet = cancelRequestsProcessing();
        //clear the requests queue
        arContinualRequests.clear();

        return bRet;
    }

    /**
     * This function returns an array of one time data requests.
     *
     * @return The queue of one-time requests.
     */
    public AbstractQueue<IDataRequest> getOneTimeRequests() {
        return arOneTimeRequests;
    }

    /**
     * This function registers given requests to FSUIPC
     *
     * @param arRequests
     * @return
     */
    private int registerRequests(AbstractQueue<IDataRequest> arRequests) {
        int iRet = 0;

        //if the array of requests is empty, nothing to do here
        if (arRequests.isEmpty()) {
            return PROCESS_RESULT_REQUESTS_EMPTY;
        }

        for (IDataRequest dataRequest : arRequests) {
            if (dataRequest.getType() == IDataRequest.RequestType.READ) {
                iRet = FSUIPCWrapper.read(dataRequest.getOffset(), dataRequest.getSize(), dataRequest.getDataBuffer());
            } else {
                iRet = FSUIPCWrapper.write(dataRequest.getOffset(), dataRequest.getSize(), dataRequest.getDataBuffer());
            }

            if (iRet == 0) {
                break;
            }
        }

        // check if storing requests went ok, if not, quit
        if (iRet == 0) {
            checkLastResult();  //check whether we are still connected
            return PROCESS_RESULT_REQUESTS_STORE_FAILED;
        }

        return PROCESS_RESULT_OK;
    }

    /**
     * This function will call FSUIPC process function. It also monitors
     * processing time, which can be returned by {@link #getLastProcessingTime()
     * }. If the processing function fails, this function calls the {@link #checkLastResult()
     * }, which will let registered listeners know and optionally disconnect
     * (close) FSUIPC connection, if the last result code indicates that the
     * connection has been lost or terminated, ie. no longer available.
     *
     * @return It will return {@link #PROCESS_RESULT_OK} if processing function
     * did not return error, or {@link #PROCESS_RESULT_REQUESTS_PROCESS_FAILED}
     * if it did. Check the last result code with {@link #getLastResult() }
     * then.
     */
    private int process() {
        //get time, for measurements
        final long startTime = System.nanoTime();
        //now, process all
        int iRet = FSUIPCWrapper.process();
        //compute elapsed time
        lastProcessingTime = System.nanoTime() - startTime;       
                   
        //return value based od process result
        if (iRet == 0) {
            checkLastResult();  //check whether we are still connected
            return PROCESS_RESULT_REQUESTS_PROCESS_FAILED;
        } else {
            return PROCESS_RESULT_OK;
        }
    }

    /**
     * This function will process all stored one-time data requests. To store
     * one time data request to the queue, use {@link #addContinualRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)
     * } method.
     *
     * @return This function will return {@link #PROCESS_RESULT_OK} if
     * everything went Ok. It will return {@link #PROCESS_RESULT_REQUESTS_EMPTY}
     * if the one-time requests array was empty before calling this function -
     * ie. nothing to process. The {@link #PROCESS_RESULT_REQUESTS_STORE_FAILED}
     * will be returned if it failed to store any of the request to FSUIPC
     * ({@link FSUIPCWrapper#read(int, int, byte[]) } and {@link FSUIPCWrapper#write(int, int, byte[])
     * } is being called in the background based on request type). And lastly,
     * the {@link #PROCESS_RESULT_REQUESTS_PROCESS_FAILED} if the call to {@link FSUIPCWrapper#process()
     * } returned error. In the case of last two scenarios, use {@link FSUIPCWrapper#getResult()
     * } to find out what did not work.
     */
    public int processRequestsOnce() {
        int iRet = registerRequests(arOneTimeRequests);

        //if registration was ok, process request
        if (iRet == PROCESS_RESULT_OK) {
            iRet = process();
            if (iRet == PROCESS_RESULT_OK) {
                arOneTimeRequests.clear();
            }
        }

        return iRet;
    }

    /**
     * This function will start thread for continual processing of FSUIPC data
     * requests. Specify time period in milliseconds of how often to process
     * requests. This will process all requests, either the ones stored via the {@link #addOneTimeRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)
     * } or
     * {@link #addContinualRequest(com.mouseviator.fsuipc.datarequest.IDataRequest) }
     * functions. If processing is completed without errors, the "one time"
     * requests array will be cleared.
     *
     * <strong>WARNING:</strong> This function will try to stop any currently running processing thread (if cancelRunning is true). This might take some time - thus may
     * block the current thread! Should not be called from main EDT thread.
     * 
     * @param repeatPeriod How often to process the requests. Milliseconds.
     * @param cancelRunning Whether to cancel the task if currently running and
     * start a new one.
     * @return This function will return {@link #PROCESS_RESULT_OK} if thread is
     * successfully started. It will return
     * {@link #PROCESS_RESULT_FAILED_TO_CANCEL_THREAD} if the thread is already
     * running, the <b>cancelRunning</b> parameter was true and it failed to
     * cancel the currently running thread. If the thread is already running and
     * the <b>cancelRunning</b>
     * parameter is false, {@link #PROCESS_RESULT_THREAD_ALREADY_RUNNING} will
     * be returned. The {@link #PROCESS_RESULT_FAILTED_TO_START_THREAD} will be
     * returned if it fails to start new thread.
     */
    public int processRequests(int repeatPeriod, boolean cancelRunning) {
        //if the thread is already running, try to stop it
        if (continualRequestProcessThread != null) {
            if (cancelRunning) {
                if (!cancelRequestsProcessing()) {
                    return PROCESS_RESULT_FAILED_TO_CANCEL_THREAD;
                }
            } else {
                //not cancel selected, nothing to do, return thread already running
                return PROCESS_RESULT_THREAD_ALREADY_RUNNING;
            }
        }

        try {
            //init task executor if not initialized yet
            if (scheduledESForCRPTask == null) {
                //Creating thread pool with only one thread will make sure that no more than one thread will be processing requests at one time
                //thus, listener functions that are called from processing thread will have to be completed before the thread can run next time
                //Hope this will prevent data incosistency that might would occur when the executor would run the same processing code again
                //before the completion of previous code (which could happen with more than one thread in pool). This would cause data issues where
                //data in instances that are read by FSUIPC would be overwritten before listener has time to process them, as FSUIPC lib writes the changes
                //directly into the respective variable memory
                scheduledESForCRPTask = Executors.newScheduledThreadPool(1);
            }
            //start our process continual request thread
            continualRequestProcessThread = scheduledESForCRPTask.scheduleAtFixedRate(new ContinualRequestsProcessWorker(), 0, repeatPeriod, TimeUnit.MILLISECONDS);
            logger.log(Level.FINER, "Started thread to process continual requests at period of: {0} miliseconds.", repeatPeriod);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to start thread to process continual requests!", ex);
            return PROCESS_RESULT_FAILTED_TO_START_THREAD;
        }

        return PROCESS_RESULT_OK;
    }

    /**
     * Returns the time in nanoseconds that the last call to FSUIPC process
     * function took. It will be updated by
     * {@link #processRequests(int, boolean) } and {@link #processRequestsOnce()
     * } functions.
     *
     * @return Time in milliseconds.
     */
    public long getLastProcessingTime() {
        return lastProcessingTime;
    }

    /**
     * This thread will is about to call FSUIPC open until it is successfully
     * opened.
     */
    private class WaitForConnectionWorker implements Runnable {

        FSUIPCWrapper.FSUIPCSimVersion simVersion = FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY;
        private int result = 0;

        public WaitForConnectionWorker(FSUIPCWrapper.FSUIPCSimVersion simVersion) {
            this.simVersion = simVersion;
        }

        @Override
        public void run() {
            //Try to open FSUIPC connection, if Ok, set connectef flag, othrwise, check for error
            result = FSUIPCWrapper.open(this.simVersion.getValue());
            if (result != 0) {
                setConnected(true);
            } else {
                //actually, do nothing, this function should wait until connected,
                //so checking the failure result would only result in calling unnecessary onFail function for all registered
                //listeners
                //checkLastResult();
            }
        }

        public int getResult() {
            return result;
        }
    }

    /**
     * A thread to process continual requests
     */
    private class ContinualRequestsProcessWorker implements Runnable {

        @Override
        public void run() {
            //process all requests, the continual ones and also the one-time ones
            registerRequests(arOneTimeRequests);
            registerRequests(arContinualRequests);

            int iRet = process();            
            //clear the one time requests
            if (iRet == PROCESS_RESULT_OK) {
                arOneTimeRequests.clear();
            }

            //Let all listeners know, if we are still connected, the disconnect might have been called while processing
            if (connected.get()) {
                arListeners.forEach(listener -> {
                    listener.onProcess(arContinualRequests);
                });
            }
        }
    }

}
