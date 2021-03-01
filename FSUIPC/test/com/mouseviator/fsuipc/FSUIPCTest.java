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

import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;
import java.util.AbstractQueue;
import java.util.EnumSet;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class test several methods and functions from the {@link FSUIPC} class. It needs one of the supported simulators to be running, because without a simulator
 * to connect to there is not much to test.
 * 
 * @author Murdock
 */
public class FSUIPCTest {

    private static final int USER_OFFSET1 = 0x66C0;

    private static FSUIPC fsuipc;

    public FSUIPCTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("JUnit test: FSUIPC class setUp - Will try to load FSUIPC and connect to the simulator...");

        //The libraries needs to be in the project folder in order the Java to find them
        byte result = FSUIPC.load();
        if (result != FSUIPC.LIB_LOAD_RESULT_OK && result != FSUIPC.LIB_LOAD_RESULT_ALREADY_LOADED) {
            System.out.println("Failed to load native library. Quiting...");
            return;
        }

        fsuipc = FSUIPC.getInstance();

        System.out.println("JUnit test: FSUIPC class setUp - Done.");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("JUnit test: FSUIPC class tearDown - Will disconnect FSUIPC if connected...");

        if (fsuipc.isConnected()) {
            fsuipc.disconnect();
            fsuipc = null;
        }

        System.out.println("JUnit test: FSUIPC class tearDown - Done.");
    }

    /**
     * Test of getInstance method, of class FSUIPC.
     */
    @Test
    public void testGetInstance() {
        System.out.println("JUnit test: FSUIPC.getInstance");
        FSUIPC instance = FSUIPC.getInstance();
        FSUIPC instance2 = FSUIPC.getInstance();

        //test singleton
        assertEquals(instance, instance2);
    }

    /**
     * Test of load method, of class FSUIPC.
     */
    @Test
    public void testLoad() {
        System.out.println("JUnit test: FSUIPC.load");
        byte result = FSUIPC.load();
        //should return already loaded
        assertEquals(FSUIPC.LIB_LOAD_RESULT_ALREADY_LOADED, result);
    }

    /**
     * Test of load32 method, of class FSUIPC.
     */
    @Test
    public void testLoad32() {
        System.out.println("JUnit test: FSUIPC.load32");
        byte result = FSUIPC.load32();
        //should return already loaded
        assertNotEquals(FSUIPC.LIB_LOAD_RESULT_OK, result);
    }

    /**
     * Test of load64 method, of class FSUIPC.
     */
    @Test
    public void testLoad64() {
        System.out.println("JUnit test: FSUIPC.load64");
        byte result = FSUIPC.load64();
        //should return already loaded
        assertNotEquals(FSUIPC.LIB_LOAD_RESULT_OK, result);
    }

    /**
     * Test of connect method, of class FSUIPC.
     */
    @Test
    public void testConnect() {
        System.out.println("JUnit test: FSUIPC.connect");
        if (fsuipc.isConnected()) {
            //disconnet for the case other test method connected
            fsuipc.disconnect();
        }

        EnumSet.allOf(FSUIPCWrapper.FSUIPCSimVersion.class).forEach((simVersion)
                -> {
            System.out.println("Trying to connect to simulator version: " + simVersion);
            int result = fsuipc.connect(simVersion);
            System.out.println("Connection attempt to simulator: " + simVersion + " returned: " + result);
            if (result != 0) {
                fsuipc.disconnect();
            }
        });

        //connect back for other methods
        connectTOFSUIPC();
    }

    /**
     * Test of waitForConnection method, of class FSUIPC.
     */
    @Test    
    public void testWaitForConnection() {
        System.out.println("JUnit test: FSUIPC.waitForConnection");
        if (fsuipc.isConnected()) {
            //disconnet for the case other test method connected
            fsuipc.disconnect();
        }

        //add listener
        fsuipc.addListener(new IFSUIPCListener() {
            @Override
            public void onConnected() {
                System.out.println("FSUIPC connected!");
            }

            @Override
            public void onDisconnected() {
                System.out.println("FSUIPC disconnected!");
            }

            @Override
            public void onProcess(AbstractQueue<IDataRequest> arRequests) {
            }

            @Override
            public void onFail(int lastResult) {
            }
        });

        EnumSet.allOf(FSUIPCWrapper.FSUIPCSimVersion.class).forEach((simVersion)
                -> {
            System.out.println("Starting wait for connection task to connect to simulator version: " + simVersion);
            boolean result = fsuipc.waitForConnection(simVersion, 500);
            long startTime = System.currentTimeMillis();
            while (!fsuipc.isConnected()) {
                //wait for connection but max 5s
                System.out.println("Waiting for connection to simulator: " + simVersion);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    System.out.println("Interrupted while waiting for connection!");
                }
                long currTime = System.currentTimeMillis();
                if (currTime - startTime >= 5000) {
                    break;
                }
            }

            //this will close fsuipc conenction and also cancel all waiting tasks
            fsuipc.disconnect();
        });

        //remove all listeners
        fsuipc.removeAllListeners();

        //connect back for other methods
        connectTOFSUIPC();
    }

    /**
     * Test of disconnect method, of class FSUIPC.
     */
    @Test
    public void testDisconnect() {
        System.out.println("JUnit test: FSUIPC.disconnect");
        if (fsuipc.isConnected()) {
            fsuipc.disconnect();
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getLastResult method, of class FSUIPC.
     */
    @Test
    public void testGetLastResult() {
        System.out.println("JUnit test: FSUIPC.getLastResult");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            try {
                fsuipc.getLastResult();
            } catch (Exception ex) {
                fail("FSUIPC.getLastResult() returned value not supported by FSUIPCResult. Exception is: " + ex.getMessage());
            }
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getLastErrorMessage method, of class FSUIPC.
     */
    @Test
    public void testGetLastErrorMessage() {
        System.out.println("JUnit test: FSUIPC.getLastErrorMessage");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            try {
                fsuipc.getLastErrorMessage();
            } catch (Exception ex) {
                fail("FSUIPC.getLastResult() returned value not supported by FSUIPCResult. Thus, the FSUIPC.getLastErrorMessage() FAILED. Exception is: " + ex.getMessage());
            }
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getFSVersion method, of class FSUIPC.
     */
    @Test
    public void testGetFSVersion_0args() {
        System.out.println("JUnit test: FSUIPC.getFSVersion");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            assertNotEquals("", fsuipc.getFSVersion());
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getFSVersion method, of class FSUIPC.
     */
    @Test
    public void testGetFSVersion_FSUIPCWrapperFSUIPCSimVersion() {
        System.out.println("JUnit test: FSUIPC.getFSVersion(simVersion)");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            EnumSet.allOf(FSUIPCWrapper.FSUIPCSimVersion.class).forEach((simVersion) -> {
                assertNotEquals("", fsuipc.getFSVersion(simVersion));
            });
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getVersion method, of class FSUIPC.
     */
    @Test
    public void testGetVersion() {
        System.out.println("JUnit test: FSUIPC.getVersion");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            assertNotEquals("", fsuipc.getVersion());
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getLibVersion method, of class FSUIPC.
     */
    @Test
    public void testGetLibVersion() {
        System.out.println("JUnit test: FSUIPC.getLibVersion");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            assertNotEquals("", fsuipc.getLibVersion());
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of addOneTimeRequest method, of class FSUIPC.
     */
    @Test
    public void testAddOneTimeRequest() {
        System.out.println("JUnit test: FSUIPC.addOneTimeRequest");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            IntRequest request = new IntRequest(USER_OFFSET1, 60);
            fsuipc.addOneTimeRequest(request);
            assertTrue(fsuipc.getOneTimeRequests().contains(request));
            fsuipc.getOneTimeRequests().clear();
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getOneTimeRequests method, of class FSUIPC.
     */
    @Test
    public void testGetOneTimeRequests() {
        System.out.println("JUnit test: FSUIPC.getOneTimeRequests");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            assertNotNull(fsuipc.getOneTimeRequests());
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of processRequestsOnce method, of class FSUIPC.
     */
    @Test
    public void testProcessRequestsOnce() {
        System.out.println("JUnit test: FSUIPC.processRequestsOnce");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            //write value
            IntRequest writeRequest = new IntRequest(USER_OFFSET1, 64);
            assertNotNull(fsuipc.addOneTimeRequest(writeRequest));
            int result = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, result);

            //read back value
            IntRequest readRequest = new IntRequest(USER_OFFSET1);
            assertNotNull(fsuipc.addOneTimeRequest(readRequest));
            result = fsuipc.processRequestsOnce();
            assertEquals(FSUIPC.PROCESS_RESULT_OK, result);

            assertEquals(writeRequest.getValue(), readRequest.getValue());
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of processRequests method, of class FSUIPC.
     */
    @Test
    public void testProcessRequests() {
        System.out.println("JUnit test: FSUIPC.processRequests");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            System.out.println("This test will do 3 short continual request processing. During the test several stuff are tested, like adding continual requests, the work of the listeners and repeat execution of continual processing.");

            for (int i = 1; i < 3; i++) {
                System.out.println("Starting continual request processing round " + i + " of 3.");
                
                //create some requests
                AircraftHelper aircraftHelper = new AircraftHelper();

                IDataRequest iasRequest = fsuipc.addContinualRequest(aircraftHelper.getIAS());
                IDataRequest headingRequest = fsuipc.addContinualRequest(aircraftHelper.getHeading());
                IDataRequest latRequest = fsuipc.addContinualRequest(aircraftHelper.getLatitude());
                IDataRequest longRequest = fsuipc.addContinualRequest(aircraftHelper.getLongitude());

                //add listener
                fsuipc.addListener(new IFSUIPCListener() {
                    @Override
                    public void onConnected() {
                        System.out.println("FSUIPC connected!");
                    }

                    @Override
                    public void onDisconnected() {
                        System.out.println("FSUIPC disconnected!");
                    }

                    @Override
                    public void onProcess(AbstractQueue<IDataRequest> arRequests) {
                        System.out.println("Aircraft IAS: " + iasRequest.getValue());
                        System.out.println("Aircraft Heading: " + headingRequest.getValue());
                        System.out.println("Aircraft Lattitude: " + latRequest.getValue());
                        System.out.println("Aircraft Longitude: " + longRequest.getValue());
                    }

                    @Override
                    public void onFail(int lastResult) {
                        System.out.println("FSUIPC onFail called code: " + lastResult);
                    }
                });

                //start the processing                
                int result = fsuipc.processRequests(500, true);
                assertEquals(FSUIPC.PROCESS_RESULT_OK, result);
                
                System.out.println("Will now let the processing go for 10 seconds. You should see values being printed in twice a second.");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.out.println("Interrupted while waiting letting the processing go...");
                }

                //cancel processing
                assertTrue(fsuipc.cancelRequestsProcessing());
                fsuipc.removeAllListeners();
            }
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    /**
     * Test of getLastProcessingTime method, of class FSUIPC.
     */
    @Test
    public void testGetLastProcessingTime() {
        System.out.println("JUnit test: FSUIPC.getLastProcessingTime");
        connectTOFSUIPC();
        if (fsuipc.isConnected()) {
            assertTrue(fsuipc.getLastProcessingTime() >= 0);
        } else {
            System.out.println("Not connected to simulator! No test to perform!");
        }
    }

    private void connectTOFSUIPC() {
        if (!fsuipc.isConnected()) {
            int ret = fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
            System.out.println("FSUIPC connect return value = " + ret);

            if (ret == 0) {
                System.out.println("Flight sim not found");
            } else {
                System.out.println("Flight Sim found!");
            }
        }
    }
}
