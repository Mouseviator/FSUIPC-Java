/**
 * <p>This package (and sub-packages) contains helper classes to get data requests for use with {@link com.mouseviator.fsuipc.FSUIPC} class to gather various sim data. The helpers are designed
 * to return/expect "friendly" values. For example, airspeed, available via FSUIPC offset 0x02BC - is stored as integer value * 128. When reading this value normally using the
 * {@link com.mouseviator.fsuipc.datarequest.primitives.IntRequest} we would have to divide it by 128 after reading the value. But the {@link com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper#getIAS() }
 * will do this for us and returned value will be the airspeed in knots as float value.</p>
 */
package com.mouseviator.fsuipc.helpers;
