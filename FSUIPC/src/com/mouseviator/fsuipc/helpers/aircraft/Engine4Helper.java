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
 * This class provides methods that return data requests to gather various info about aircraft engine number 4.
 * The returned requests are usually modified to return user-friendly data type, even thought the underlying data type may be different.
 * 
 * @author Mouseviator
 */
public class Engine4Helper extends Engine1Helper {
    {
        throttleLeverOffset = 0x0A54;
        propLeverOffset = 0x0A56;
        mixtureLeverOffset = 0x0A58;
        fuelFlowOffset = 0x0AE0;      //pounds per hour, float64 (double)
        oilTempOffset = 0x0A80;       //140C = 16384
        oilPressureOffset = 0x0A82;   //16384 = 55 psi, 65535 = 220 psi
        oilQuantityOffset = 0x0A98;         //16384 = 100%
        manifoldPressureOffset = 0x0A88;
        fuelUsedOffset = 0x0AD4;
        elapsedTimeOffset = 0x0AD8;
    }
}
