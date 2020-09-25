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
package com.mouseviator.fsuipc.helpers.avionics;

import com.mouseviator.fsuipc.datarequest.DataRequest;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_INT;
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_SHORT;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;
import com.mouseviator.fsuipc.datarequest.primitives.ShortRequest;
import java.security.InvalidParameterException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class contains methods/functions - ie. the functionality to get/set various data common to navigation radios in flight simulator.
 * The classes implementing specific radios just needs to specify correct offsets to get/set respective data using respective variables.
 * 
 * @author Murdock
 */
public abstract class NavRadioHelper extends RadioHelper {

    protected int CDINeedleOffset;
    protected int localiserNeedleOffset;
    protected int GSINeedleOffset;
    protected int signalStrengthOffset;
    protected int radialOffset;
    protected int OBSSettingOffset;
    protected int VORrelativeBearingOffset;
    protected int ToFromFlagOffset;
    protected int backCourseFlagOffset;
    protected int codeFlagsOffset;
    protected int GSFlagOffset;
    protected int MagVarOffset;
    protected int latitudeOffset1;
    protected int latitudeOffset2;
    protected int longitudeOffset1;
    protected int longitudeOffset2;
    protected int elevationOffset1;
    protected int elevationOffset2;
    protected int ilsGlideSlopeInclinationOffset;
    protected int ilsInverseRunwayHeadingOffset;
    protected int nameOffset;
    protected int identityOffset;
    protected int dmeDistanceOffset;
    protected int dmeSpeedOffset;
    protected int dmeTimeToStationOffset;    

    /**
     * Returns data request to get Course deviation needle indication. The
     * request will return float value, ranging from -127 (left) to 127 (right).
     * READ ONLY!
     *
     * @return Data request to get Course deviation needle indication.
     */
    public IDataRequest<Float> getCDINeedle() {
        class CDINeedleRequest extends DataRequest implements IReadOnlyRequest<Float> {

            {
                this.offset = CDINeedleOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_FLOAT];
            }

            @Override
            public Float getValue() {
                return getFloat();
            }
        }
        return new CDINeedleRequest();
    }

    /**
     * Returns data request to get Glide slope needle indication. The request
     * will return float value, ranging from -127 (up) to 127 (down). READ ONLY!
     *
     * @return Data request to get Glide slope needle indication.
     */
    public IDataRequest<Float> getGSINeedle() {
        class GSINeedleRequest extends DataRequest implements IReadOnlyRequest<Float> {

            {
                this.offset = GSINeedleOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_FLOAT];
            }

            @Override
            public Float getValue() {
                return getFloat();
            }
        }
        return new GSINeedleRequest();
    }

    /**
     * Returns data request to get localiser needle indication. The request will
     * return byte value, ranging from -127 (left) to 127 (right). Not tested if
     * this always returns the same value as the {@link #getCDINeedle() } data
     * request. The range of values is the same. The offset used is different.
     * READ ONLY!
     *
     * @return Data request to get localiser needle indication.
     */
    public IDataRequest<Byte> getLocaliserNeedle() {
        class LocaliserNeedleRequest extends DataRequest implements IReadOnlyRequest<Byte> {

            {
                this.offset = localiserNeedleOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }
        }
        return new LocaliserNeedleRequest();
    }

    /**
     * Returns data request to get signal strength. The returned value will be
     * integer. For Localisers, seems to be either 0 or 256 For VORs varies from
     * 0 to over 1,000,000 when really close! READ ONLY!
     *
     * @return Data request to get signal strength.
     */
    public IDataRequest<Integer> getSignalStrength() {
        class SignalStrengthRequest extends DataRequest implements IReadOnlyRequest<Integer> {

            {
                this.offset = signalStrengthOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            }

            @Override
            public Integer getValue() {
                return getInt();
            }
        }
        return new SignalStrengthRequest();
    }

    /**
     * Returns data request to get VOR radial.The returned value will be in
     * degrees Magnetic for a VOR, but TRUE for an ILS LOC. READ ONLY!
     *
     * @param bDegrees Whether you want the result in degrees (True), or radians
     * (False)
     * @return Data request to get VOR radial.
     */
    public IDataRequest<Float> getRadial(boolean bDegrees) {
        if (bDegrees) {
            class RadialRequest extends DataRequest implements IReadOnlyRequest<Float> {

                {
                    this.offset = radialOffset;
                    this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                }

                @Override
                public Float getValue() {
                    short value = getShort();
                    float radial = value * 360 / 65536.0f;
                    return radial;
                }
            }
            return new RadialRequest();
        } else {
            class RadialRequest extends DataRequest implements IReadOnlyRequest<Float> {

                {
                    this.offset = radialOffset;
                    this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                }

                @Override
                public Float getValue() {
                    short value = getShort();
                    float radial = value * 360 / 65536.0f;
                    return (float) Math.toRadians(radial);
                }
            }
            return new RadialRequest();
        }
    }

    /**
     * Returns data request to get/set OBS settings. The value returned by this
     * data request will be in degrees (0-359). Note that the returned object
     * will be READ request by default. To make it write request, call its {@link IDataRequest#setType(com.mouseviator.fsuipc.datarequest.IDataRequest.RequestType)
     * } method with parameter {@link IDataRequest.RequestType#WRITE}.
     *
     * @return Data request to get/set OBS settings.
     */
    public ShortRequest getOBSSettings() {
        return new ShortRequest(OBSSettingOffset);
    }

    /**
     * Returns data request to get relative bearing to VOR. The value returned
     * by this data request will be in degrees (0-359). READ ONLY!
     *
     * @return Data request to get relative bearing to a VOR.
     */
    public IDataRequest<Short> getVORRelativeBearing() {
        class VORRelativeBearingRequest extends DataRequest implements IReadOnlyRequest<Short> {

            {
                this.offset = VORrelativeBearingOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }

            @Override
            public Short getValue() {
                return getShort();
            }
        }
        return new VORRelativeBearingRequest();
    }

    /**
     * Returns data request to get To/From flag. The data request will return
     * 0=Not active, 1=To, 2=From. You can also use {@link ToFromFlag}
     * enumeration, which defines these values, to compare the result. READ
     * ONLY!
     *
     * @return Data request to get To/From flag.
     */
    public IDataRequest<Byte> getToFromFlag() {
        class ToFromFlagRequest extends DataRequest implements IReadOnlyRequest<Byte> {

            {
                this.offset = ToFromFlagOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }
        }
        return new ToFromFlagRequest();
    }

    /**
     * Returns data request to get glide slope flag. The data request will
     * return TRUE (non-zero) value when glide slope is alive. READ ONLY!
     *
     * @return Data request to get glide slope flag.
     */
    public IDataRequest<Byte> getGSFlag() {
        class GSFlagRequest extends DataRequest implements IReadOnlyRequest<Byte> {

            {
                this.offset = GSFlagOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }
        }
        return new GSFlagRequest();
    }

    /**
     * Returns data request to get back course flag. The data request will
     * return some byte number representing flags set. You can pass this value
     * to an instance of the helper class {@link BackCourseFlags}, and use its
     * methods to determine what bits are set. Or you can test them yourself,
     * according to FSUIPC documentation of the respective offset (for
     * NAV1=0x0C4A or NAV2=0x0C5A). READ ONLY!
     *
     * @return Data request to get back course flags.
     */
    public IDataRequest<Byte> getBackCourseFlags() {
        class BackCourseFlagsRequest extends DataRequest implements IReadOnlyRequest<Byte> {

            {
                this.offset = backCourseFlagOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }
        }
        return new BackCourseFlagsRequest();
    }

    /**
     * Returns data request to get VOR magnetic variation. The returned value
     * will be in degrees or radians. READ ONLY!
     * <i>(Note that there are two different data sources for MagVars, and this
     * may not agree with the airport MagVar for airport-based VORs)</i>
     *
     * @param bDegrees Whether you want the result in degrees (True), or radians
     * (False)
     * @return Data request to get VOR magnetic variation.
     */
    public IDataRequest<Float> getMagneticVariation(boolean bDegrees) {
        if (bDegrees) {
            class MagVarRequest extends DataRequest implements IReadOnlyRequest<Float> {

                {
                    this.offset = MagVarOffset;
                    this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                }

                @Override
                public Float getValue() {
                    short value = getShort();
                    float magVar = value * 360 / 65536.0f;
                    return magVar;
                }
            }
            return new MagVarRequest();
        } else {
            class MagVarRequest extends DataRequest implements IReadOnlyRequest<Float> {

                {
                    this.offset = MagVarOffset;
                    this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                }

                @Override
                public Float getValue() {
                    short value = getShort();
                    float magVar = value * 360 / 65536.0f;
                    return (float) Math.toRadians(magVar);
                }
            }
            return new MagVarRequest();
        }
    }

    /**
     * Returns data request to get back course flag. The data request will
     * return some byte number representing flags set. You can pass this value
     * to an instance of the helper class {@link CodeFlags}, and use its methods
     * to determine what bits are set. Or you can test them yourself, according
     * to FSUIPC documentation of the respective offset (for NAV1=0x0C4D or
     * NAV2=0x0C70). READ ONLY!
     *
     * @return Data request to get back course flags.
     */
    public IDataRequest<Byte> getCodeFlags() {
        class CodeFlagsRequest extends DataRequest implements IReadOnlyRequest<Byte> {

            {
                this.offset = codeFlagsOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }
        }
        return new CodeFlagsRequest();
    }

    /**
     * Returns data request to get VOR latitude in degrees. If the NAV radio is
     * tuned to ILS, this data request will return latitude of the glide slope
     * transmitter. If the NAV radio is tuned to ILS and you want to get the
     * latitude of the localiser, use the {@link #getLatitude2() } data request.
     *
     * @return Data request to get latitude of VOR or glide slope transmitter.
     */
    public IDataRequest<Double> getLatitude1() {
        class LatitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.offset = latitudeOffset1;
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            }

            @Override
            public Double getValue() {
                int value = getInt();

                double latitude = value * 90.0d / 10001750.0d;
                return latitude;
            }

        }
        return new LatitudeRequest();
    }

    /**
     * Returns data request to get VOR latitude in degrees. If the NAV radio is
     * tuned to ILS, this data request will return latitude of the localiser
     * transmitter. If the NAV radio is tuned to ILS and you want to get the
     * latitude of the glide slope transmitter, use the {@link #getLatitude1() }
     * data request.
     *
     * @return Data request to get latitude of VOR or localiser transmitter.
     */
    public IDataRequest<Double> getLatitude2() {
        class LatitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.offset = latitudeOffset2;
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            }

            @Override
            public Double getValue() {
                int value = getInt();

                double latitude = value * 90.0d / 10001750.0d;
                return latitude;
            }

        }
        return new LatitudeRequest();
    }

    /**
     * Returns data request to get VOR longitude in degrees. If the NAV radio is
     * tuned to ILS, this data request will return longitude of the glide slope
     * transmitter. If the NAV radio is tuned to ILS and you want to get the
     * longitude of the localiser, use the {@link #getLongitude2() } data
     * request.
     *
     * @return Data request to get latitude of VOR or glide slope transmitter.
     */
    public IDataRequest<Double> getLongitude1() {
        class LongitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.offset = longitudeOffset1;
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            }

            @Override
            public Double getValue() {
                int value = getInt();

                double longitude = value * 360.0d / (65536.0d * 65536.0d);
                return longitude;
            }

        }
        return new LongitudeRequest();
    }

    /**
     * Returns data request to get VOR longitude in degrees. If the NAV radio is
     * tuned to ILS, this data request will return longitude of the localiser
     * transmitter. If the NAV radio is tuned to ILS and you want to get the
     * longitude of the glide slope transmitter, use the {@link #getLongitude1()
     * } data request.
     *
     * @return Data request to get longitude of VOR or localiser transmitter.
     */
    public IDataRequest<Double> getLongitude2() {
        class LongitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.offset = longitudeOffset2;
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
            }

            @Override
            public Double getValue() {
                int value = getInt();

                double longitude = value * 360.0d / (65536.0d * 65536.0d);
                return longitude;
            }

        }
        return new LongitudeRequest();
    }

    /**
     * Returns data request to get VOR elevation in meters of feet.If the NAV
     * radio is tuned to ILS, this data request will return elevation of the
     * glide slope transmitter. If the NAV radio is tuned to ILS and you want to
     * get the elevation of the localiser, use the {@link #getElevation2(boolean) }
     * data request.
     *
     * @param bFeet Whether to get result in meters or feet.
     * @return Data request to get elevation of VOR or glide slope transmitter.
     */
    public IDataRequest<Double> getElevation1(boolean bFeet) {
        if (!bFeet) {
            class ElevationRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.offset = elevationOffset1;
                    this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                }

                @Override
                public Double getValue() {
                    int elevation = getInt();
                    return (double) elevation;
                }

            }
            return new ElevationRequest();
        } else {
            class ElevationRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.offset = elevationOffset1;
                    this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                }

                @Override
                public Double getValue() {
                    int elevation = getInt();
                    return (double) (elevation * 3.2808d);
                }

            }
            return new ElevationRequest();
        }
    }

    /**
     * Returns data request to get VOR elevation in meters or feet.If the NAV
     * radio is tuned to ILS, this data request will return elevation of the
     * localiser transmitter. If the NAV radio is tuned to ILS and you want to
     * get the elevation of the glide slope transmitter, use the {@link #getElevation1(boolean)
     * } data request.
     *
     * @param bFeet Whether to get result in meters or feet.
     * @return Data request to get elevation of VOR or localiser transmitter.
     */
    public IDataRequest<Double> getElevation2(boolean bFeet) {
        if (!bFeet) {
            class ElevationRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.offset = elevationOffset2;
                    this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                }

                @Override
                public Double getValue() {
                    int elevation = getInt();
                    return (double) elevation;
                }

            }
            return new ElevationRequest();
        } else {
            class ElevationRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.offset = elevationOffset2;
                    this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                }

                @Override
                public Double getValue() {
                    int elevation = getInt();
                    return (double) (elevation * 3.2808d);
                }

            }
            return new ElevationRequest();
        }
    }

    /**
     * Returns data request to get ILS glide slope inclination. The returned
     * value will be in degrees or radians. READ ONLY!
     *
     * @param bDegrees Whether you want the result in degrees (True), or radians
     * (False)
     * @return Data request to get ILS glide slope inclination.
     */
    public IDataRequest<Float> getILSGlideslopeInclination(boolean bDegrees) {
        if (bDegrees) {
            class ILSGSInclinationRequest extends DataRequest implements IReadOnlyRequest<Float> {

                {
                    this.offset = ilsGlideSlopeInclinationOffset;
                    this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                }

                @Override
                public Float getValue() {
                    final short value = getShort();
                    float inclination = value * 360 / 65536.0f;
                    return inclination;
                }
            }
            return new ILSGSInclinationRequest();
        } else {
            class ILSGSInclinationRequest extends DataRequest implements IReadOnlyRequest<Float> {

                {
                    this.offset = ilsGlideSlopeInclinationOffset;
                    this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
                }

                @Override
                public Float getValue() {
                    final short value = getShort();
                    float inclination = value * 360 / 65536.0f;
                    return (float) Math.toRadians(inclination);
                }
            }
            return new ILSGSInclinationRequest();
        }
    }

    /**
     * Returns data request to get ILS localiser inverse runway heading in degrees. The NAV radio must be tuned to ILS. The value returned by the data request
     * is 180 degrees different to the direction of flight to follow the localiser. READ ONLY!
     * 
     * @return Data request to get ILS localiser inverse runway heading.
     */
    public IDataRequest<Float> getILSLocaliserInverseRunwayHeading() {
        class ILSLocInvRWHeadingRequest extends DataRequest implements IReadOnlyRequest<Float> {

            {
                this.offset = ilsInverseRunwayHeadingOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }

            @Override
            public Float getValue() {
                final short value = getShort();
                float invHeading = value * 360 / 65536.0f;
                return invHeading;
            }
        }
        return new ILSLocInvRWHeadingRequest();
    }
    
    /**
     * Returns data request to get DME distance. The value this data request will return is in Nm (nautical miles). READ ONLY!
     * 
     * @return Data request to get DME distance.
     */
    public IDataRequest<Float> getDMEDistance() {
        class DMEDistanceRequest extends DataRequest implements IReadOnlyRequest<Float> {
            {
                this.offset = dmeDistanceOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }
            @Override
            public Float getValue() {
                final short value = getShort();
                return value / 10.0f;
            }            
        }
        return new DMEDistanceRequest();
    }
    
    /**
     * Returns data request to get DME speed. The value this data request will return is in Kts. READ ONLY!
     * 
     * @return Data request to get DME speed.
     */
    public IDataRequest<Float> getDMESpeed() {
        class DMESpeedRequest extends DataRequest implements IReadOnlyRequest<Float> {
            {
                this.offset = dmeSpeedOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }
            @Override
            public Float getValue() {
                final short value = getShort();
                return value / 10.0f;
            }            
        }
        return new DMESpeedRequest();
    }
    
    /**
     * Returns data request to get DME time to station. The value this data request will return is in seconds. READ ONLY!
     * 
     * @return Data request to get DME time to station.
     */
    public IDataRequest<Float> getDMETimeToStation() {
        class DMETimeToStationRequest extends DataRequest implements IReadOnlyRequest<Float> {
            {
                this.offset = dmeTimeToStationOffset;
                this.dataBuffer = new byte[BUFFER_LENGTH_SHORT];
            }
            @Override
            public Float getValue() {
                final short value = getShort();
                return value / 10.0f;
            }            
        }
        return new DMETimeToStationRequest();
    }
    
    /**
     * Returns data request to get the nav radio name. It will be max 24 characters long. READ ONLY!
     * 
     * @return Data request to get radio name.
     */
    public IDataRequest<String> getName() {
        class NameRequest extends DataRequest implements IReadOnlyRequest<String> {
            {
                this.offset = nameOffset;
                this.dataBuffer = new byte[25];
            }
            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }            
        }
        return new NameRequest();
    }
    
    /**
     * Returns data request to get the nav radio identity (ICAO code). It will be max 5 characters long. READ ONLY!
     * 
     * @return Data request to get radio identity.
     */
    public IDataRequest<String> getIdentity() {
        class IdentityRequest extends DataRequest implements IReadOnlyRequest<String> {
            {
                this.offset = identityOffset;
                this.dataBuffer = new byte[6];
            }
            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }            
        }
        return new IdentityRequest();
    }

    /**
     * Helper class to what back course flag bits are set. You can obtain back
     * course flags using the {@link #getBackCourseFlags() } function and pass
     * the result of the data request to an instance of this class to test the
     * bits using its functions.
     */
    public class BackCourseFlags {

        private byte flags;

        /**
         * Constructs a new instance of this class.
         *
         * @param flags The flags to initialize with.
         */
        public BackCourseFlags(byte flags) {
            this.flags = flags;
        }

        /**
         * Set the flags value.
         *
         * @param flags The flags value.
         */
        public void setFlags(byte flags) {
            this.flags = flags;
        }

        /**
         * Returns the flags value.
         *
         * @return The flags value.
         */
        public byte getFlags() {
            return this.flags;
        }

        /**
         * Tests whether bit 0 - BC available, is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isAvailable() {
            //bit 0 set?
            return (flags & 0x1) != 0;
        }

        /**
         * Tests whether bit 1 - Localiser tuned in, is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isLocaliserTunedIn() {
            //bit 1 set?
            return (flags & 0x2) != 0;
        }

        /**
         * Tests whether bit 4 - On Back Course (Not found for FSX), is set or
         * not.
         *
         * @return True if bit is set.
         */
        public boolean isOnBackCourse() {
            //bit 2 set?
            return (flags & 0x4) != 0;
        }

        /**
         * Tests whether bit 7 - Station active (even if no BC), is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isStationActive() {
            //bit 7 set?
            return (flags & 0x80) != 0;
        }
    }

    /**
     * Helper class to what code flags bits are set. You can obtain code flags
     * using the {@link #getCodeFlags() } function and pass the result of the
     * data request to an instance of this class to test the bits using its
     * functions.
     */
    public class CodeFlags {

        private byte flags;

        /**
         * Constructs a new instance of this class.
         *
         * @param flags The flags to initialize with.
         */
        public CodeFlags(byte flags) {
            this.flags = flags;
        }

        /**
         * Set the flags value.
         *
         * @param flags The flags value.
         */
        public void setFlags(byte flags) {
            this.flags = flags;
        }

        /**
         * Returns the flags value.
         *
         * @return The flags value.
         */
        public byte getFlags() {
            return this.flags;
        }

        /**
         * Tests whether bit 0 - DME available, is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isDMEActive() {
            //bit 0 set?
            return (flags & 0x1) != 0;
        }

        /**
         * Tests whether bit 1 - TACAN (Not found for FSX), is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isTACAN() {
            //bit 1 set?
            return (flags & 0x2) != 0;
        }

        /**
         * Tests whether bit 2 - Voice available (Not found for FSX), is set or
         * not.
         *
         * @return True if bit is set.
         */
        public boolean isVoiceAvailable() {
            //bit 2 set?
            return (flags & 0x4) != 0;
        }

        /**
         * Tests whether bit 3 - No signal available, is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isNoSignal() {
            //bit 3 set?
            return (flags & 0x8) != 0;
        }

        /**
         * Tests whether bit 4 - DME/GS co-located (Not found for FSX), is set
         * or not.
         *
         * @return True if bit is set.
         */
        public boolean isDMEGSColocated() {
            //bit 4 set?
            return (flags & 0x10) != 0;
        }

        /**
         * Tests whether bit 5 - No back course, is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isNoBackCourse() {
            //bit 5 set?
            return (flags & 0x20) != 0;
        }

        /**
         * Tests whether bit 6 - GS available, is set or not.
         *
         * @return True if bit is set.
         */
        public boolean isGSAvailable() {
            //bit 6 set?
            return (flags & 0x40) != 0;
        }

        /**
         * Tests whether bit 7 - This is a localiser (else it’s a VOR), is set
         * or not.
         *
         * @return True if bit is set.
         */
        public boolean isStationActive() {
            //bit 7 set?
            return (flags & 0x80) != 0;
        }
    }

    /**
     * An enumeration of possible return values for data request to obtain
     * To/From flag indication. See {@link #getToFromFlag() } function.
     */
    public static enum ToFromFlag {

        /**
         * Not active
         */
        NOT_ACTIVE((byte) 0),
        /**
         * To indication
         */
        TO((byte) 1),
        /**
         * From indication
         */
        FROM((byte) 2);

        private final byte value;

        private static final Map<Byte, ToFromFlag> lookupTable = new HashMap<>();

        static {
            for (ToFromFlag flag : EnumSet.allOf(ToFromFlag.class)) {
                lookupTable.put(flag.getValue(), flag);
            }
        }

        private ToFromFlag(byte value) {
            this.value = value;
        }

        /**
         * @return Byte value of this type.
         */
        public byte getValue() {
            return this.value;
        }

        /**
         * Returns {@link ToFromFlag} by corresponding int value.
         *
         * @param value String value corresponding to one of enumeration
         * constants.
         * @return {@link ToFromFlag} by corresponding int value.
         * @throws InvalidParameterException if value not corresponding to any
         * enumeration value is passed.
         */
        public static ToFromFlag get(byte value) throws InvalidParameterException {
            if (!lookupTable.containsKey(value)) {
                throw new InvalidParameterException("Log severity value: " + value + " NOT supported!");
            }
            return lookupTable.get(value);
        }
    }
}
