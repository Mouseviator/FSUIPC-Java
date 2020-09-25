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

/**
 * This class provides methods that return data requests to gather/set various data
 * from/to flight simulator NAV2 radio. The returned requests are usually modified to
 * return user-friendly data type, even thought the underlying data type may be
 * different. It uses the {@link RadioHelper} class for common functionality.
 * 
 * @author Murdock
 */
public class NAV2Helper extends NavRadioHelper {
    {
        this.frequencyOffset = 0x0352;
        this.standbyFrequencyOffset = 0x3120;
        this.frequencySwapValue = 1;
        
        this.CDINeedleOffset = 0x2AB4;
        this.GSINeedleOffset = 0x2AB8;
        this.localiserNeedleOffset = 0x0C59;
        this.signalStrengthOffset = 0x0C62;
        this.radialOffset = 0x0C60;
        this.OBSSettingOffset = 0x0C5E;
        this.VORrelativeBearingOffset = 0x0C5C;
        this.ToFromFlagOffset = 0x0C5B;
        this.backCourseFlagOffset = 0x0C5A;
        this.codeFlagsOffset = 0x0C70;
        this.GSFlagOffset = 0x0C6F;
        this.MagVarOffset = 0x0C42;
        
        this.latitudeOffset1 = 0x0858;
        this.latitudeOffset2 = 0x084C;
        this.longitudeOffset1 = 0x0860;
        this.longitudeOffset2 = 0x0850;
        this.elevationOffset1 = 0x0868;
        this.elevationOffset2 = 0x0854;
        this.ilsGlideSlopeInclinationOffset = 0x0846;
        this.ilsInverseRunwayHeadingOffset = 0x0844;
        
        this.nameOffset = 0x301F;
        this.identityOffset = 0x3025;
        
        this.dmeDistanceOffset = 0x0306;
        this.dmeSpeedOffset = 0x0308;
        this.dmeTimeToStationOffset = 0x030A;
    }
}
