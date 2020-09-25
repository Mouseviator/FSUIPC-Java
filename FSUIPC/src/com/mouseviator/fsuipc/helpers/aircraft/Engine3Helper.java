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

/**
 * This class provides methods that return data requests to gather various info about aircraft engine number 3.
 * The returned requests are usually modified to return user-friendly data type, even thought the underlying data type may be different.
 * 
 * @author Mouseviator
 */
public class Engine3Helper extends Engine1Helper {
    {
        throttleLeverOffset = 0x09BC;
        propLeverOffset = 0x09BE;
        mixtureLeverOffset = 0x09C0;
        fuelFlowOffset = 0x0A48;      //pounds per hour, float64 (double)
        oilTempOffset = 0x09E8;       //140C = 16384
        oilPressureOffset = 0x09EA;   //16384 = 55 psi, 65535 = 220 psi
        oilQuantityOffset = 0x0A00;         //16384 = 100%
        manifoldPressureOffset = 0x09F0;
        fuelUsedOffset = 0x0A3C;
        elapsedTimeOffset = 0x0A40;
    }
}
