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
 * from/to flight simulator NAV1 radio. The returned requests are usually modified to
 * return user-friendly data type, even thought the underlying data type may be
 * different. It uses the {@link RadioHelper} class for common functionality.
 * 
 * @author Murdock
 */
public class NAV1Helper extends NavRadioHelper {
    {
        this.frequencyOffset = 0x0350;
        this.standbyFrequencyOffset = 0x311E;
        this.frequencySwapValue = 2;
        
        this.CDINeedleOffset = 0x2AAC;
        this.GSINeedleOffset = 0x2AB0;
        this.localiserNeedleOffset = 0x0C48;
        this.signalStrengthOffset = 0x0C52;
        this.radialOffset = 0x0C50;
        this.OBSSettingOffset = 0x0C4E;
        this.VORrelativeBearingOffset = 0x0C56;
        this.ToFromFlagOffset = 0x0C4B;
        this.backCourseFlagOffset = 0x0C4A;
        this.codeFlagsOffset = 0x0C4D;
        this.GSFlagOffset = 0x0C4C;
        this.MagVarOffset = 0x0C40;
        
        this.latitudeOffset1 = 0x085C;
        this.latitudeOffset2 = 0x0874;
        this.longitudeOffset1 = 0x0864;
        this.longitudeOffset2 = 0x0878;
        this.elevationOffset1 = 0x086C;
        this.elevationOffset2 = 0x087C;
        this.ilsGlideSlopeInclinationOffset = 0x0872;
        this.ilsInverseRunwayHeadingOffset = 0x0870;
        
        this.nameOffset = 0x3006;
        this.identityOffset = 0x3000;
        
        this.dmeDistanceOffset = 0x0300;
        this.dmeSpeedOffset = 0x0302;
        this.dmeTimeToStationOffset = 0x0304;
    }
}
