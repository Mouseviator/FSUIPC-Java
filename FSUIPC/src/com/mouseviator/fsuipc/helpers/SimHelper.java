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

import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;
import com.mouseviator.fsuipc.datarequest.IWriteOnlyRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import java.text.DecimalFormat;
import java.time.LocalTime;

/**
 * This class provides methods that return data requests to gather various info
 * about flight simulator. The returned requests are usually modified to return
 * user-friendly data type, even thought the underlying data type may be
 * different.
 *
 * @author Mouseviator
 */
public class SimHelper {

    /**
     * This will return request to read current simulator situation file. Result
     * will be a string, full or relative path. READ ONLY!
     *
     * @return Data request to get currently loaded situation file.
     */
    public IDataRequest<String> getSituationFile() {

        class SituationFileRequest extends DataRequest implements IReadOnlyRequest<String> {

            {
                this.dataBuffer = new byte[256];
                this.offset = 0x0024;
            }

            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }
        }
        return new SituationFileRequest();
    }

    /**
     * This will return request to read simulator local time. Result will be as
     * seconds since midnight. When using <b>setValue</b> method on returned
     * object, again, pass value that is seconds from midnight. Note that the
     * returned object will be READ request by default. To make it write
     * request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType)
     * } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set simulator local time.
     */
    public IntRequest getLocalTime() {
        return new IntRequest(0x0238) {
            {
                this.dataBuffer = new byte[3];
            }

            @Override
            public void setValue(Integer value) {
                LocalTime time = LocalTime.ofSecondOfDay(value);
                this.dataBuffer[0] = (byte) time.getHour();
                this.dataBuffer[1] = (byte) time.getMinute();
                this.dataBuffer[2] = (byte) time.getSecond();
            }

            @Override
            public Integer getValue() {
                LocalTime time = LocalTime.of(dataBuffer[0], dataBuffer[1], dataBuffer[2]);

                return time.toSecondOfDay();
            }
        };
    }

    /**
     * Return request to read sim pause indicator. READ ONLY!
     *
     * @return Data request to get whether the sim is paused or not. The request
     * will return 0 if sim is not paused, 1 if sim is paused.
     */
    public IDataRequest<Short> getPauseIndicator() {
        class PauseIndicatorRequest extends DataRequest implements IReadOnlyRequest<Short> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x0264;
            }

            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new PauseIndicatorRequest();
    }

    /**
     * Return request to pause / un-pause the sim.WRITE ONLY!
     *
     * @param bPause True to pause, false to un-pause.
     * @return Data request to set - pause/un-pause the flight simulator.
     */
    public IDataRequest<Short> setPause(boolean bPause) {
        class PauseRequest extends DataRequest implements IWriteOnlyRequest<Short> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x0262;
                this.type = RequestType.WRITE;
            }

            @Override
            public void setValue(Short value) {
                putShort(value);
            }
        }
        PauseRequest request = new PauseRequest();
        if (bPause) {
            request.setValue((short) 1);
        } else {
            request.setValue((short) 0);
        }
        return request;
    }

    /**
     * Return request to read simulator season. READ ONLY!
     *
     * @return Data request to get flight simulator season. Return 0 = Winter, 1
     * = Spring, 2 = Summer, 3 = Fall
     */
    public IDataRequest<Short> getSeason() {

        class SeasonRequest extends DataRequest implements IReadOnlyRequest<Short> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x0248;
            }

            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new SeasonRequest();
    }

    /**
     * Returns request to get ground altitude. READ ONLY!
     *
     * @param bFeet True to get result in feet, False for meters.
     * @return Data request to get ground altitude.
     */
    public IDataRequest<Double> getGroundAltitude(boolean bFeet) {
        if (!bFeet) {
            //return in meters
            class GroundAltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                    this.offset = 0x0020;
                }

                @Override
                public Double getValue() {
                    int altitude = getInt();
                    return altitude / 256.0d;
                }
            }
            return new GroundAltitudeRequest();
        } else {
            //return in feet
            class GroundAltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                    this.offset = 0x0020;
                }

                @Override
                public Double getValue() {
                    int altitude = getInt();
                    return altitude / 256.0d * 3.2808d;
                }
            }
            return new GroundAltitudeRequest();
        }
    }
    
    /**
     * Returns request to get the size of the memory currently assigned to FSUIPC. In bytes. READ ONLY! 
     * 
     * @return Data request to get the size of memory currently assigned to FSUIPC.
     */
    public IDataRequest<Integer> getFSUIPCAssignedMemorySize() {
        class FSUIPCMemRequest extends DataRequest implements IReadOnlyRequest<Integer> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                this.offset = 0x0258;
            }

            @Override
            public Integer getValue() {
                return getInt();
            }
        }
        return new FSUIPCMemRequest();
    }
    
    /**
     * Returns request to get simulator actual frame rate. READ ONLY! 
     * 
     * @return Data request to get the actual frame rate.
     */
    public IDataRequest<Float> getFrameRate() {
        class FrameRateRequest extends DataRequest implements IReadOnlyRequest<Float> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x0274;
            }

            @Override
            public Float getValue() {
                return 32768 / (float)getShort();
            }
        }
        return new FrameRateRequest();
    }
    
    /**
     * <p>This returns data request that returns FSX/P3D version that FSUIPC is connected to. Internally, offset 0x3124 is used. The returned data request will return some string
     * as following:</p>
     * 
     * <ul>
     *  <li><i>N/A</i> - If returned returned value does not match any documented value.</li>
     *  <li><i>FSX RTM, FSX SP1, FSX SP2 or FSX Acceleration</i> - For respective version of FSX.</li>
     *  <li><i>FSX (Unknown version)</i> - For FSX, but again, returned value does not match any documented value.</li>
     *  <li><i>FSX Steam Edition, build &lt;build number&gt;</i> - For FSX Steam Edition.</li>
     *  <li><i>Prepar3D &lt;version number&gt;</i> - For respective Prepar3D version.</li>
     *  <li><i>Microsoft Flight Simulator (2020)</i> - For Microsoft Flight Simulator , first released at 2020.</li>
     * </ul>
     * 
     * @return Data request that returns string with FSX/P3D version.
     */
    public IDataRequest<String> getFSXP3DVersion() {
        class FSXP3DVersionRequest extends DataRequest implements IReadOnlyRequest<String> {
            {
                this.offset = 0x3124;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public String getValue() {
                final byte version = this.dataBuffer[0];
                final DecimalFormat decimalFormat1 = new DecimalFormat("#.#");
                
                if (version < 10) {
                    //FSX
                    switch (version) {
                        case 1:
                            return "FSX RTM";
                        case 2:
                            return "FSX SP1";
                        case 3:
                            return "FSX SP2";
                        case 4: 
                            return "FSX Acceleration";
                        default:
                            return "FSX (Unknown version)";
                    }                                    
                } else if (version >= 10 && version <= 100) {
                    //Preapr3D versions
                    return "Prepar3D " + decimalFormat1.format(version / 10.0f);
                } else if (version >= 101 && version <= 109) {
                    //FSX Steam Edition
                    return "FSX Steam Edition, build: " + String.valueOf(62607 + ((int)version - 100));
                } else if (version >= 110) {
                    //Microsoft Flight Simulator
                    return "Microsoft Flight Simulator (2020)";
                }
                
                return "N/A";
            }
        }
        return new FSXP3DVersionRequest();
    }
}
