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
import static com.mouseviator.fsuipc.datarequest.DataRequest.BUFFER_LENGTH_DOUBLE;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.IReadOnlyRequest;

/**
 * This class provides methods that return data requests to gather various data
 * from flight simulator GPS. The returned requests are usually modified to
 * return user-friendly data type, even thought the underlying data type may be
 * different.
 *
 * @author Mouseviator
 */
public class GPSHelper {

    /**
     * Returns request to get GPS altitude in meters of feet. READ ONLY!
     *
     * @param bFeet True to get result in feet, False for meters.
     * @return Data request to get GPS altitude in meters or feet.
     */
    public IDataRequest<Double> getAltitude(boolean bFeet) {
        if (!bFeet) {
            class AltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6020;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new AltitudeRequest();
        } else {
            class AltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6020;
                }

                @Override
                public Double getValue() {
                    double altitude = getDouble();

                    return altitude * 3.2808;
                }
            }
            return new AltitudeRequest();
        }
    }

    /**
     * Returns request to get GPS longitude in degrees. Positive values = E,
     * negative values = W. READ ONLY!
     *
     * @return Data request to get GPS longitude in degrees.
     */
    public IDataRequest<Double> getLongitude() {
        class LongitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x6018;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new LongitudeRequest();
    }

    /**
     * Returns request to get GPS latitude in degrees. Positive values = N,
     * negative values = S. READ ONLY!
     *
     * @return Data request to get GPS latitude in degrees.
     */
    public IDataRequest<Double> getLatitude() {
        class LatitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x6010;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new LatitudeRequest();
    }

    /**
     * Returns request to get GPS magnetic variation in radians or degrees. READ
     * ONLY!
     *
     * @param bDegrees True to get result in degrees, False for radians.
     * @return Data request to get GPS Magnetic variation in radians or degrees.
     */
    public IDataRequest<Double> getMagneticVariation(boolean bDegrees) {
        if (!bDegrees) {
            class MagVarRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6028;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new MagVarRequest();

        } else {
            //return in degrees
            class MagVarRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6028;
                }

                @Override
                public Double getValue() {
                    double magVar = getDouble();

                    return Math.toDegrees(magVar);
                }
            }
            return new MagVarRequest();
        }
    }

    /**
     * Returns request to get GPS vertical speed. READ ONLY!
     *
     * @return Data request to get GPS vertical speed.
     */
    public IDataRequest<Double> getVerticalSpeed() {
        class VSRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x6078;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new VSRequest();
    }

    /**
     * Returns request to get GPS ground speed in m/s or knots. READ ONLY!
     *
     * @param bKnots True to get result in knots, False for m/s.
     * @return Data request to get GPS Ground speed in m/s or knots.
     */
    public IDataRequest<Double> getGroundSpeed(boolean bKnots) {
        if (!bKnots) {
            class GroundSpeedRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6030;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new GroundSpeedRequest();
        } else {
            class GroundSpeedRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6030;
                }

                @Override
                public Double getValue() {
                    double gs = getDouble();

                    return gs * 1.94384449;
                }
            }
            return new GroundSpeedRequest();
        }
    }

    /**
     * Returns request to get GPS aircraft TRUE heading in radians or degrees.
     * READ ONLY!
     *
     * @param bDegrees True to get result as degrees, False for radians.
     * @return Data request to get GPS TRUE heading in radians or degrees.
     */
    public IDataRequest<Double> getHeading(boolean bDegrees) {
        if (!bDegrees) {
            //return normally in radians
            class HeadingRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6038;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new HeadingRequest();
        } else {
            //return in degrees
            class HeadingRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6038;
                }

                @Override
                public Double getValue() {
                    double heading = getDouble();

                    return Math.toDegrees(heading);
                }
            }
            return new HeadingRequest();
        }
    }

    /**
     * Returns request to get GPS aircraft required TRUE heading in radians or
     * degrees. READ ONLY!
     *
     * @param bDegrees True to get result as degrees, False for radians.
     * @return Data request to get GPS required TRUE heading in radians or
     * degrees.
     */
    public IDataRequest<Double> getRequiredHeading(boolean bDegrees) {
        if (!bDegrees) {
            //return normally in radians
            class RequiredHeadingRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6060;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new RequiredHeadingRequest();
        } else {
            //return in degrees
            class RequiredHeadingRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6060;
                }

                @Override
                public Double getValue() {
                    double heading = getDouble();

                    return Math.toDegrees(heading);
                }
            }
            return new RequiredHeadingRequest();
        }
    }

    /**
     * Returns request to get GPS aircraft magnetic track in radians or degrees.
     * READ ONLY!
     *
     * @param bDegrees True to get result as degrees, False for radians.
     * @return Data request to get GPS Aircraft magnetic track in radians or
     * degrees.
     */
    public IDataRequest<Double> getMagneticTrack(boolean bDegrees) {
        if (!bDegrees) {
            //return normally in radians
            class MagneticTrackRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6040;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new MagneticTrackRequest();
        } else {
            //return in degrees
            class MagneticTrackRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6040;
                }

                @Override
                public Double getValue() {
                    double mTrack = getDouble();

                    return Math.toDegrees(mTrack);
                }
            }
            return new MagneticTrackRequest();
        }
    }

    /**
     * Returns request to get GPS distance to next waypoint in meters or
     * nautical miles. READ ONLY!
     *
     * @param bNauticalMiles True to get result as nautical miles, False for
     * meters.
     * @return Data request to get GPS distance to next waypoint.
     */
    public IDataRequest<Double> getDistanceToNextWaypoint(boolean bNauticalMiles) {
        //return normally in radians
        if (!bNauticalMiles) {
            class DistToNextWaypointRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6048;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new DistToNextWaypointRequest();
        } else {
            //value in nautical miles
            class DistToNextWaypointRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6048;
                }

                @Override
                public Double getValue() {
                    double dist = getDouble();

                    return dist * 0.00053996f;
                }
            }
            return new DistToNextWaypointRequest();
        }
    }

    /**
     * Returns request to get GPS magnetic bearing to next waypoint in radians
     * or degrees. degrees. READ ONLY!
     *
     * @param bDegrees True to get result in degrees, False for radians.
     * @return Data request to get GPS magnetic bearing to next waypoint.
     */
    public IDataRequest<Double> getMagBearingToNextWaypoint(boolean bDegrees) {
        //return normally in radians
        if (!bDegrees) {
            class MagBearingToNextWaypointRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6050;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new MagBearingToNextWaypointRequest();
        } else {
            //value in nautical miles
            class MagBearingToNextWaypointRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6050;
                }

                @Override
                public Double getValue() {
                    double bearing = getDouble();

                    return Math.toDegrees(bearing);
                }
            }
            return new MagBearingToNextWaypointRequest();
        }
    }

    /**
     * Returns request to get GPS cross track error in meters or nautical miles. READ ONLY!
     *
     * @param bNauticalMiles True to get result as nautical miles, False for
     * meters.
     * @return Data request to get GPS cross track error.
     */
    public IDataRequest<Double> getCrossTrackError(boolean bNauticalMiles) {
        //return normally in radians
        if (!bNauticalMiles) {
            class CrossTrackErrorRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6058;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new CrossTrackErrorRequest();
        } else {
            //value in nautical miles
            class CrossTrackErrorRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6058;
                }

                @Override
                public Double getValue() {
                    double dist = getDouble();

                    return dist * 0.00053996f;
                }
            }
            return new CrossTrackErrorRequest();
        }
    }

    /**
     * Returns request to get GPS track error in radians or degrees. degrees.
     * READ ONLY!
     *
     * @param bDegrees True to get result in degrees, False for radians.
     * @return Data request to get GPS track error.
     */
    public IDataRequest<Double> getTrackError(boolean bDegrees) {
        //return normally in radians
        if (!bDegrees) {
            class TrackErrorRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6068;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new TrackErrorRequest();
        } else {
            //value in nautical miles
            class TrackErrorRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x6068;
                }

                @Override
                public Double getValue() {
                    double track_error = getDouble();

                    return Math.toDegrees(track_error);
                }
            }
            return new TrackErrorRequest();
        }
    }

    /**
     * Returns request to get GPS previous waypoint valid flag.
     * READ ONLY!
     *     
     * @return Data request to get GPS previous waypoint valid flag. Request value will be 0 if not valid.
     */
    public IDataRequest<Byte> getPreviousWaypointValidFlag() {
        class PrevWaypointValidFlagRequest extends DataRequest implements IReadOnlyRequest<Byte> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_BYTE];
                this.offset = 0x6080;
            }

            @Override
            public Byte getValue() {
                return this.dataBuffer[0];
            }
        }
        return new PrevWaypointValidFlagRequest();
    }
    
    /**
     * Returns request to get GPS previous waypoint string ID.
     * READ ONLY!
     *     
     * @return Data request to get GPS previous waypoint string ID.
     */
    public IDataRequest<String> getPreviousWaypointID() {
        class PrevWaypointIDRequest extends DataRequest implements IReadOnlyRequest<String> {

            {
                this.dataBuffer = new byte[6];
                this.offset = 0x6081;
            }

            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }
        }
        return new PrevWaypointIDRequest();
    }
    
    /**
     * Returns request to get GPS previous waypoint longitude in degrees. Positive values = E,
     * negative values = W. READ ONLY!
     *
     * @return Data request to get GPS previous waypoint longitude in degrees.
     */
    public IDataRequest<Double> getPreviousWaypointLongitude() {
        class LongitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x6094;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new LongitudeRequest();
    }

    /**
     * Returns request to get GPS previous waypoint latitude in degrees. Positive values = N,
     * negative values = S. READ ONLY!
     *
     * @return Data request to get GPS previous waypoint latitude in degrees.
     */
    public IDataRequest<Double> getPreviousWaypointLatitude() {
        class LatitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x608C;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new LatitudeRequest();
    }
    
    /**
     * Returns request to get GPS previous waypoint aircraft altitude in meters of feet. READ ONLY!
     *
     * @param bFeet True to get result in feet, False for meters.
     * @return Data request to get GPS previous waypoint aircraft altitude in meters or feet.
     */
    public IDataRequest<Double> getPreviousWaypointAircraftAltitude(boolean bFeet) {
        if (!bFeet) {
            class AltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x609C;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new AltitudeRequest();
        } else {
            class AltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x609C;
                }

                @Override
                public Double getValue() {
                    double altitude = getDouble();

                    return altitude * 3.2808;
                }
            }
            return new AltitudeRequest();
        }
    }
    
    /**
     * Returns request to get GPS next waypoint string ID.
     * READ ONLY!
     *     
     * @return Data request to get GPS next waypoint string ID.
     */
    public IDataRequest<String> getNextWaypointID() {
        class NextWaypointIDRequest extends DataRequest implements IReadOnlyRequest<String> {

            {
                this.dataBuffer = new byte[6];
                this.offset = 0x60A4;
            }

            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }
        }
        return new NextWaypointIDRequest();
    }
    
    /**
     * Returns request to get GPS next waypoint longitude in degrees. Positive values = E,
     * negative values = W. READ ONLY!
     *
     * @return Data request to get GPS next waypoint longitude in degrees.
     */
    public IDataRequest<Double> getNextWaypointLongitude() {
        class LongitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x60B4;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new LongitudeRequest();
    }

    /**
     * Returns request to get GPS next waypoint latitude in degrees. Positive values = N,
     * negative values = S. READ ONLY!
     *
     * @return Data request to get GPS next waypoint latitude in degrees.
     */
    public IDataRequest<Double> getNextWaypointLatitude() {
        class LatitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x60AC;
            }

            @Override
            public Double getValue() {
                return getDouble();
            }
        }
        return new LatitudeRequest();
    }
    
    /**
     * Returns request to get GPS next waypoint aircraft altitude in meters of feet. READ ONLY!
     *
     * @param bFeet True to get result in feet, False for meters.
     * @return Data request to get GPS next waypoint aircraft altitude in meters or feet.
     */
    public IDataRequest<Double> getNextWaypointAircraftAltitude(boolean bFeet) {
        if (!bFeet) {
            class AltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x60BC;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new AltitudeRequest();
        } else {
            class AltitudeRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x60BC;
                }

                @Override
                public Double getValue() {
                    double altitude = getDouble();

                    return altitude * 3.2808;
                }
            }
            return new AltitudeRequest();
        }
    }
    
    /**
     * Returns request to get GPS next waypoint ETE (estimated time en-route) in seconds. READ ONLY!
     * 
     * @return Data request to get GPS next waypoint ETE in seconds.
     */
    public IDataRequest<Integer> getNextWaypointETE() {
        class NextWaypointETERequest extends DataRequest implements IReadOnlyRequest<Integer> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                this.offset = 0x60E4;
            }
            @Override
            public Integer getValue() {
                return getInt();
            }            
        }
        return new NextWaypointETERequest();
    }
    
    /**
     * Returns request to get GPS next waypoint ETA (estimated time arrival) in seconds local time. READ ONLY!
     * 
     * @return Data request to get GPS next waypoint ETA in seconds.
     */
    public IDataRequest<Integer> getNextWaypointETA() {
        class NextWaypointETARequest extends DataRequest implements IReadOnlyRequest<Integer> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                this.offset = 0x60E8;
            }
            @Override
            public Integer getValue() {
                return getInt();
            }            
        }
        return new NextWaypointETARequest();
    }
    
    /**
     * Returns request to get GPS course to set in radians or degrees. READ ONLY!
     *
     * @param bDegrees  True to get result as degrees, False for radians.
     * @return Data request to get GPS course to set.
     */
    public IDataRequest<Double> getCourseToSet(boolean bDegrees) {
        //return normally in radians
        if (!bDegrees) {
            class CourseToSetRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x610C;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new CourseToSetRequest();
        } else {
            //value in nautical miles
            class CourseToSetRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x610C;
                }

                @Override
                public Double getValue() {
                    double course = getDouble();

                    return Math.toDegrees(course);
                }
            }
            return new CourseToSetRequest();
        }
    }
    
    /**
     * Returns request to get GPS previous waypoint string ID.
     * READ ONLY!
     *     
     * @return Data request to get GPS previous waypoint string ID.
     */
    public IDataRequest<String> getDestinationAirportID() {
        class DestinationAirportIDRequest extends DataRequest implements IReadOnlyRequest<String> {

            {
                this.dataBuffer = new byte[5];
                this.offset = 0x6137;
            }

            @Override
            public String getValue() {
                return new String(this.dataBuffer).trim();
            }
        }
        return new DestinationAirportIDRequest();
    }
    
    /**
     * Returns request to get GPS destination ETE (estimated time en-route) in seconds. READ ONLY!
     * NOTE THAT the FSUIPC offset status document states this AS NOT WORKING, HOPING FOR ADDITIONS TO SimConnect.
     * 
     * @return Data request to get GPS destination ETE in seconds.
     */
    public IDataRequest<Integer> getDestinationETE() {
        class DestinationETERequest extends DataRequest implements IReadOnlyRequest<Integer> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                this.offset = 0x6198;
            }
            @Override
            public Integer getValue() {
                return getInt();
            }            
        }
        return new DestinationETERequest();
    }
    
    /**
     * Returns request to get GPS destination ETA (estimated time arrival) in seconds local time. READ ONLY!
     * NOTE THAT the FSUIPC offset status document states this AS NOT WORKING, HOPING FOR ADDITIONS TO SimConnect.
     * 
     * @return Data request to get GPS destination ETA in seconds.
     */
    public IDataRequest<Integer> getDestinationETA() {
        class DestinationETARequest extends DataRequest implements IReadOnlyRequest<Integer> {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_INT];
                this.offset = 0x619C;
            }
            @Override
            public Integer getValue() {
                return getInt();
            }            
        }
        return new DestinationETARequest();
    }
    
    /**
     * Returns request to get GPS total route distance in meters or nautical miles. READ ONLY!
     * NOTE THAT the FSUIPC offset status document states this AS NOT WORKING, HOPING FOR ADDITIONS TO SimConnect.
     *
     * @param bNauticalMiles True to get result as nautical miles, False for
     * meters.
     * @return Data request to get GPS total route distance.
     */
    public IDataRequest<Double> getRouteTotalDistance(boolean bNauticalMiles) {
        //return normally in radians
        if (!bNauticalMiles) {
            class RouteTotalDistanceRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x61A0;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new RouteTotalDistanceRequest();
        } else {
            //value in nautical miles
            class RouteTotalDistanceRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x61A0;
                }

                @Override
                public Double getValue() {
                    double dist = getDouble();

                    return dist * 0.00053996f;
                }
            }
            return new RouteTotalDistanceRequest();
        }
    }
    
    /**
     * Returns request to get GPS estimated fuel burn in gallons or litres. READ ONLY!
     * NOTE THAT the FSUIPC offset status document states this AS NOT WORKING, HOPING FOR ADDITIONS TO SimConnect.
     *
     * @param bLitres True to get result as litres, False for
     * gallons.
     * @return Data request to get GPS estimated fuel burn in gallons or litres.
     */
    public IDataRequest<Double> getEstimatedFuelBurn(boolean bLitres) {
        //return normally in gallons
        if (!bLitres) {
            class EstimatedFuelBurnRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x61A8;
                }

                @Override
                public Double getValue() {
                    return getDouble();
                }
            }
            return new EstimatedFuelBurnRequest();
        } else {
            //value in litres
            class EstimatedFuelBurnRequest extends DataRequest implements IReadOnlyRequest<Double> {

                {
                    this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                    this.offset = 0x61A8;
                }

                @Override
                public Double getValue() {
                    double gallons = getDouble();

                    return gallons * 3.78541178;
                }
            }
            return new EstimatedFuelBurnRequest();
        }
    }
}
