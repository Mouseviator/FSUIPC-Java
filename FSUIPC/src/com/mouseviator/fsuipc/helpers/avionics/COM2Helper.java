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
 * from/to flight simulator COM2 radio. The returned requests are usually modified to
 * return user-friendly data type, even thought the underlying data type may be
 * different. It uses the {@link RadioHelper} class for common functionality.
 * 
 * @author Murdock
 */
public class COM2Helper extends RadioHelper {
    {
        this.frequencyOffset = 0x3118;
        this.standbyFrequencyOffset = 0x311C;
        this.frequencySwapValue = 4;
    }
}
