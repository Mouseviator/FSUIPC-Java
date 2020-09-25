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
package com.mouseviator.fsuipc.helpers.aircraft;

import com.mouseviator.fsuipc.datarequest.DataRequest;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.StringRequest;
import java.security.InvalidParameterException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides methods that return data requests to gather various info about flight simulator.
 * The returned requests are usually modified to return user-friendly data type, even thought the underlying data type may be different.
 * For example, the {@link #getIAS() } returns an instance of {@link FloatRequest}, while the overloaded <b>getValue()</b> reads integer (4 bytes) value
 * from offset 0x02BC, divides it by 128 and returns the resulting float value, which is IAS in knots. Many of the methods here return data requests with
 * overloaded <b>getValue</b> function to return user-friendly value rather that raw FSUIPC value.
 * 
 * @author Mouseviator
 */
public class AircraftHelper {

    /**
     * Returns request to get aircraft IAS in knots. Expects IAS in Kts. However, not guaranteed to work. See
     * description of offset 0x02BC in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft IAS in Kts.
     */
    public FloatRequest getIAS() {
        /**
         *
         */   
        return new FloatRequest(0x02BC) {

            // We just ovveride the getValue method to retvrieve the true airspeed as float, as claculation is needed. 
            @Override
            public Float getValue() {
                int iIAS = getInt();

                return iIAS / 128.0f;
            }

            @Override
            public void setValue(Float value) {
                int iIAS = (int)(value / 128.0f);
                putInt(iIAS);
            }                        
        };
    }

    /**
     * Returns request to get aircraft TAS in knots. The setValue method is also overridden. Expects TAS in Kts. However, not guaranteed to work. See
     * description of offset 0x02B0 in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft TAS in Kts.
     */
    public FloatRequest getTAS() {
        /**
         *
         */
        return new FloatRequest(0x02B8) {

            // We just ovveride the getValue method to retvrieve the true airspeed as float, as claculation is needed. 
            @Override
            public Float getValue() {
                int iTAS = getInt();

                return iTAS / 128.0f;
            }

            @Override
            public void setValue(Float value) {
                int iTAS = (int)(value / 128.0f);
                putInt(iTAS);
            }                        
        };
    }

    /**
     * Returns request to get aircraft vertical speed in m/s or Feets per
     * minute.The setValue method is also overridden. Expects vertical speed in m/s of fpm. However, not guaranteed to work. See
     * description of offset 0x02B0 in FSUIPC documentation. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @param bFPM Whether to return value as fpm or m/s.
     * @return Data request to get/set aircraft vertical speed in fpm or m/s.
     */
    public FloatRequest getVerticalSpeed(boolean bFPM) {
        if (bFPM) {
            //Vertical speed in feets per minute
            return new FloatRequest(0x02C8) {

                // We just ovveride the getValue method to retvrieve the true airspeed as float, as claculation is needed. 
                @Override
                public Float getValue() {
                    int iVS = getInt();

                    return iVS / 256.0f * 60 * 3.28084f;
                }

                @Override
                public void setValue(Float value) {
                    int iVS = (int)(value * 256 / 60.0f / 3.28084f);
                    putInt(iVS);
                }                                
            };
        } else {
            //Vertical speed as meters per second
            return new FloatRequest(0x02C8) {

                // We just ovveride the getValue method to retvrieve the true airspeed as float, as claculation is needed. 
                @Override
                public Float getValue() {
                    int iVS = getInt();

                    return iVS / 256.0f;
                }
                
                @Override
                public void setValue(Float value) {
                    int iVS = (int)(value * 256);
                    putInt(iVS);
                }                                
            };
        }
    }

    /**
     * Returns request to get aircraft latitude in degrees. The setValue method is also overridden. Expects latitude in degrees. 
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft latitude in degrees.
     */
    public DoubleRequest getLatitude() {
        return new DoubleRequest(0x0560) {
            @Override
            public Double getValue() {
                //can do this, since double and long both have 8 bytes in Java
                long latitude = getLong();

                return (double) latitude * (90.0 / (10001750.0 * 65536.0 * 65536.0));
            }

            @Override
            public void setValue(Double value) {
                long latitude = (long)(value / (90.0 * (10001750.0 * 65536.0 * 65536.0)));
                putLong(latitude);
            }                        
        };
    }

    /**
     * Returns request to get aircraft longitude in degrees. The setValue method is also overridden. Expects longitude in degrees. 
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft longitude in degrees.
     */
    public DoubleRequest getLongitude() {
        return new DoubleRequest(0x0568) {
            @Override
            public Double getValue() {
                //can do this, since double and long both have 8 bytes in Java
                long longitude = getLong();

                return (double) longitude * (360.0 / (65536.0 * 65536.0 * 65536.0 * 65536.0));
            }

            @Override
            public void setValue(Double value) {
                long longitude = (long)(value / (360.0 / (65536.0 * 65536.0 * 65536.0 * 65536.0)));
                putLong(longitude);
            }                        
        };
    }
    
    /**
     * Returns request to get aircraft altitude in meters of feet. This reads from offset 0x0570. Due to conversions, some precision might be lost and this might
     * give slightly different result than {@link com.mouseviator.fsuipc.helpers.avionics.GPSHelper#getAltitude(boolean) }. But the difference should be within 1-2 meters.
     *
     * @param bFeet True to get result in feet, False for meters.
     * @return Data request to get aircraft altitude in meters or feet.
     */
    public DoubleRequest getAltitude(boolean bFeet) {
        if (!bFeet) {
            //meters result
            return new DoubleRequest(0x0570) {
                @Override
                public void setValue(Double value) {
                    double unit = Math.floor(value);
                    double fraction = value - unit;
                    
                    long altitude = (long)(fraction * 65536) + ((long)(unit * 65536) << 16);
                    putLong(altitude);
                }

                @Override
                public Double getValue() {
                    //The altitude is coded such as:
                    //high integer is the unit
                    //low integer is the fractional part
                    long altitude = getLong();
                    long high_int = altitude >> 16; //move the high int to normal number...
                    long low_int = altitude & 0xffff;   //mask out the higher int to get the fraction part
                    
                    return (high_int / 65536.0d) + (low_int / 65536.0d);
                }                
            };
        } else {
            return new DoubleRequest(0x0570) {
                //feet result
                @Override
                public void setValue(Double value) {
                    double in_meters = value / 3.2808d;
                    double unit = Math.floor(in_meters);
                    double fraction = in_meters - unit;
                    
                    long altitude = (long)(fraction * 65536) + ((long)(unit * 65536) << 16);
                    putLong(altitude);
                }

                @Override
                public Double getValue() {
                    //The altitude is coded such as:
                    //high integer is the unit
                    //low integer is the fractional part
                    long altitude = getLong();
                    long high_int = altitude >> 16; //move the high int to normal number...
                    long low_int = altitude & 0xffff;   //mask out the higher int to get the fraction part
                    
                    //return result in feet
                    return ((high_int / 65536.0d) + (low_int / 65536.0d)) * 3.2808;                                                            
                }      
            };            
        }
    }

    /**
     * Returns request to get number of aircraft engines. READ ONLY!
     *
     * @return Data request to get number of aircraft engines.
     */
    public IDataRequest<Short> getNumberOfEngines() {

        class NumOfEnginesRequest extends DataRequest implements IReadOnlyRequest<Short> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x0AEC;
            }

            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new NumOfEnginesRequest();
    }

    /**
     * Returns request to get aircraft pitch in degrees. The setValue method is also overridden. Expects pitch in degrees. Negative value for pitch up,
     * positive for pitch down. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft pitch in degrees.
     */
    public FloatRequest getPitch() {
        return new FloatRequest(0x0578) {
            @Override
            public Float getValue() {
                int pitch = getInt();

                return pitch * 360.0f / (65536.0f * 65536.0f);
            }

            @Override
            public void setValue(Float value) {
                int pitch = (int)(value / 360.0f * (65536.0f * 65536.0f));
                putInt(pitch);
            }            
        };
    }

    /**
     * Returns request to get aircraft bank in degrees. The setValue method is also overridden. Expects bank in degrees. Negative value for bank right,
     * positive for bank left. Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft bank in degrees.
     */
    public FloatRequest getBank() {
        return new FloatRequest(0x057C) {
            @Override
            public Float getValue() {
                int bank = getInt();

                return bank * 360.0f / (65536.0f * 65536.0f);
            }

            @Override
            public void setValue(Float value) {
                int bank = (int)(value / 360.0f * (65536.0f * 65536.0f));
                putInt(bank);
            }                        
        };
    }

    /**
     * Returns request to get aircraft TRUE heading. The setValue method is also overridden. Expects heading in degrees TRUE. 
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set aircraft TRUE heading in degrees.
     */
    public FloatRequest getHeading() {
        return new FloatRequest(0x0580) {
            @Override
            public Float getValue() {
                int heading = getInt();

                // it gives negative value actually, to get what we would expect, substract from 360 (value is negative, that is why I add here....)
                return 360 + (heading * 360.0f / (65536.0f * 65536.0f));
            }

            @Override
            public void setValue(Float value) {
                int heading = (int)(360 - (value / 360.0f * (65536.0f * 65536.0f)));
                putInt(heading);
            }                        
        };

    }

    /**
     * Returns request to get aircraft magnetic variation in degrees. READ ONLY.
     *
     * @return Data request to get aircraft magnetic variation in degrees.
     */
    public IDataRequest<Float> getMagneticVariation() {        

        class MagVarRequest extends DataRequest implements IReadOnlyRequest<Float> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x02A0;                
            }
            @Override
            public Float getValue() {
                short magVar = getShort();

                return magVar * 360.0f / 65536.0f;
            }            
        }
        return new MagVarRequest();
    }

    /**
     * Returns request to get aircraft engine type. READ ONLY!
     *
     * @return Data request to get aircraft engine type.
     */
    public IDataRequest<Byte> getEngineType() {        

        class EngineTypeRequest extends DataRequest implements IReadOnlyRequest<Byte> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
                this.offset = 0x0609;
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }            
        }
        return new EngineTypeRequest();
    }

    /**
     * Returns request to get ATC aircraft flight number as declared in AircraftHelper.cfg.  
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * <p><i>SimConnect allows this SimVar to be written, but this may not change the Flight Number being used by ATC unless a flight plan has been loaded too (see offset 0130). </i></p>
     *
     * @return Data request to get/set aircraft ATC flight number.
     */
    public StringRequest getATCFlightNumber() {
        return new StringRequest(0x3130, 12);
    }

    /**
     * Returns request to get ATC aircraft identifier (tail number) as declared in AircraftHelper.cfg.
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     * 
     * <p><i>SimConnect seems to allow this SimVar to be written, but whether this does actually change the Tail Number being used I by ATC, I don’t yet know</i></p>
     *
     * @return Data request to get/set aircraft ATC Identification (Ident).
     */
    public StringRequest getATCIdent() {
        return new StringRequest(0x313C, 12);
    }

    /**
     * Returns request to get ATC airline name as declared in AircraftHelper.cfg.
     * Note that the returned object will be READ request by default.
     * To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType) } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * <p><i>SimConnect seems to allow this SimVar to be written, but whether this does actually change the Airline Name being used by ATC, I don’t yet know</i></p>
     * 
     * @return Data request to get/set aircraft airline name.
     */
    public StringRequest getATCAirlineName() {
        return new StringRequest(0x3148, 24);
    }

    /**
     * Returns request to get ATC aircraft type as declared in AircraftHelper.cfg. READ ONLY!
     *
     * @return Data request to get aircraft ATC type.
     */
    public IDataRequest<String> getATCAircraftType() {        

        class ATCAircraftTypeRequest extends DataRequest implements IReadOnlyRequest<String> {
            {
                this.dataBuffer = new byte[24];
                this.offset = 0x3160;
            }

            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }            
        }
        return new ATCAircraftTypeRequest();
    }

    /**
     * Returns request to get whether the aircraft is on ground. READ ONLY!
     *
     * @return Data request to get whether the aircraft is on ground or in the air.
     */
    public IDataRequest<Short> getOnGround() {        

        class OnGroundRequest extends DataRequest implements IReadOnlyRequest<Short> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                this.offset = 0x0366;
            }

            @Override
            public Short getValue() {
                return getShort();
            }
        }        
        return new OnGroundRequest();
    }

    /**
     * This enumeration defines engine types supported by FSUIPC, which can be returned by {@link #getEngineType() } function.
     */
    public static enum EngineType {

        /**
         * Piston engine type
         */
        PISTON((byte) 0),

        /**
         * Jet engine type
         */
        JET((byte) 1),

        /**
         * Sailplane or anything with no engines engine type
         */
        SAILPLANE((byte) 2),

        /**
         * Helicopter engine type
         */
        HELO((byte) 3),

        /**
         * Rocket engine type (NOT supported actually)
         */
        ROCKET((byte) 4),

        /**
         * Turboprop engine type
         */
        TURBOPROP((byte) 5);

        private final byte value;

        private static final Map<Byte, EngineType> lookupTable = new HashMap<>();

        static {
            EnumSet.allOf(EngineType.class).forEach(result -> {
                lookupTable.put(result.getValue(), result);
            });
        }

        private EngineType(byte value) {
            this.value = value;
        }

        /**
         * @return Byte value of this type.
         */
        public byte getValue() {
            return this.value;
        }

        /**
         * Returns {@link EngineType} by corresponding int value.
         *
         * @param value String value corresponding to one of enumeration
         * constants.
         * @return {@link EngineType} by corresponding int value.
         * @throws InvalidParameterException if value not corresponding to any
         * enumeration value is passed.
         */
        public static EngineType get(byte value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("EngineType Result value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }
}
