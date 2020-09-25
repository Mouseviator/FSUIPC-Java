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
package com.mouseviator.fsuipc.example;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.datarequest.primitives.ShortRequest;
import com.mouseviator.fsuipc.datarequest.primitives.StringRequest;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;
import com.mouseviator.fsuipc.helpers.avionics.GPSHelper;
import com.mouseviator.fsuipc.helpers.SimHelper;
import com.mouseviator.fsuipc.helpers.avionics.COM1Helper;
import com.mouseviator.fsuipc.helpers.avionics.COM2Helper;
import com.mouseviator.fsuipc.helpers.avionics.NAV1Helper;
import com.mouseviator.fsuipc.helpers.avionics.NAV2Helper;

/**
 * This is very simple example using one-time requests with {@link FSUIPC} class.
 * 
 * @author Murdock
 */
public class FSUIPCSimpleTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {                            
        
        //1) get FSUIPC instance
        FSUIPC fsuipc = FSUIPC.getInstance();
        
        //2) First of all, load the native library. The default load function will try to determine if we are running under 32 or 64 bit JVM
        // and load 32/64 bit native library respectively
        byte result = FSUIPC.load();
        if (result != FSUIPC.LIB_LOAD_RESULT_OK) {
            System.out.println("Failed to load native library. Quiting...");
            return;
        }
        
        //enable fsuipc library file logging
        FSUIPCWrapper.setupLogging(true, FSUIPCWrapper.LogSeverity.DEBUG.getValue());                
        
        System.out.println("Running tests");
        int ret = fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
        System.out.println("ret =" + ret);
        if (ret == 0) {
            System.out.println("Flight sim not found");
        } else {
            System.out.println("Flight Sim found!");
            
            System.out.println("FSUIPC Version: " + String.valueOf(FSUIPCWrapper.getVersion()));
            System.out.println("FSUIPC Version (string): " + fsuipc.getVersion());
            System.out.println("FSUIPC FS Version: " + String.valueOf(FSUIPCWrapper.getFSVersion()));
            System.out.println("FSUIPC FS Version (string): " + fsuipc.getFSVersion());
            System.out.println("FSUIPC Lib Version: " + String.valueOf(FSUIPCWrapper.getLibVersion()));
            System.out.println("FSUIPC Lib Version (string): " + fsuipc.getLibVersion());
            
            //Helepers to create data requests
            GPSHelper gpsHelper = new GPSHelper();
            AircraftHelper aircraftHelper = new AircraftHelper();
            SimHelper simHelper = new SimHelper();          
            COM1Helper com1Helper = new COM1Helper();
            COM2Helper com2Helper = new COM2Helper();
            NAV1Helper nav1Helper = new NAV1Helper();
            NAV2Helper nav2Helper = new NAV2Helper();
            
            //Creating some basic data requests to test helpers
            IDataRequest<Short> on_ground = (IDataRequest<Short>) fsuipc.addOneTimeRequest(aircraftHelper.getOnGround());
            IDataRequest<String> fsxp3dVersion = (IDataRequest<String>) fsuipc.addOneTimeRequest(simHelper.getFSXP3DVersion());
            
            ShortRequest eng1_thr = (ShortRequest) fsuipc.addOneTimeRequest(new ShortRequest(0x088C));
            ShortRequest eng2_thr = (ShortRequest) fsuipc.addOneTimeRequest(new ShortRequest(0x0924));
                                                
            FloatRequest ias = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getIAS());            
            FloatRequest tas = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getTAS());
            FloatRequest vs = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getVerticalSpeed(true));
            DoubleRequest altitude = (DoubleRequest) fsuipc.addOneTimeRequest(aircraftHelper.getAltitude(true));
            DoubleRequest latitude = (DoubleRequest) fsuipc.addOneTimeRequest(aircraftHelper.getLatitude());
            DoubleRequest longitude = (DoubleRequest) fsuipc.addOneTimeRequest(aircraftHelper.getLongitude());
            IDataRequest<Double> gps_latitude = (IDataRequest<Double>) fsuipc.addOneTimeRequest(gpsHelper.getLatitude());
            IDataRequest<Double> gps_longitude = (IDataRequest<Double>) fsuipc.addOneTimeRequest(gpsHelper.getLongitude());
            FloatRequest true_heading = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getHeading());
            FloatRequest bank = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getBank());
            FloatRequest pitch = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getPitch());
            IDataRequest<Float> magVar = (IDataRequest<Float>) fsuipc.addOneTimeRequest(aircraftHelper.getMagneticVariation());
            IDataRequest<Short> num_of_engines = (IDataRequest<Short>) fsuipc.addOneTimeRequest(aircraftHelper.getNumberOfEngines());
            IDataRequest<Double> gps_altitude = (IDataRequest<Double>) fsuipc.addOneTimeRequest(gpsHelper.getAltitude(true));
            IDataRequest<Byte> engine_type = (IDataRequest<Byte>) fsuipc.addOneTimeRequest(aircraftHelper.getEngineType());
            
            IDataRequest<String> situation_file = (IDataRequest<String>) fsuipc.addOneTimeRequest(simHelper.getSituationFile());
            StringRequest atc_flight_num = (StringRequest) fsuipc.addOneTimeRequest(aircraftHelper.getATCFlightNumber());
            StringRequest atc_ident = (StringRequest) fsuipc.addOneTimeRequest(aircraftHelper.getATCIdent());
            
            //Radio helpers tests
            FloatRequest com1_frequency = (FloatRequest) fsuipc.addOneTimeRequest(com1Helper.getFrequency());
            FloatRequest com2_frequency = (FloatRequest) fsuipc.addOneTimeRequest(com2Helper.getFrequency());
            FloatRequest nav1_frequency = (FloatRequest) fsuipc.addOneTimeRequest(nav1Helper.getFrequency());
            FloatRequest nav2_frequency = (FloatRequest) fsuipc.addOneTimeRequest(nav2Helper.getFrequency());
            
            IDataRequest<String> nav1_name = (IDataRequest<String>) fsuipc.addOneTimeRequest(nav1Helper.getName());
            IDataRequest<String> nav1_ident = (IDataRequest<String>) fsuipc.addOneTimeRequest(nav1Helper.getIdentity());
            IDataRequest<Float> nav1_cdi = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getCDINeedle());
            IDataRequest<Float> nav1_gsi = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getGSINeedle());
            IDataRequest<Byte> nav1_code_flag = (IDataRequest<Byte>) fsuipc.addOneTimeRequest(nav1Helper.getCodeFlags());
            IDataRequest<Byte> nav1_bc_flag = (IDataRequest<Byte>) fsuipc.addOneTimeRequest(nav1Helper.getBackCourseFlags());
            IDataRequest<Integer> nav1_signal_strength = (IDataRequest<Integer>) fsuipc.addOneTimeRequest(nav1Helper.getSignalStrength());
            IDataRequest<Float> nav1_radial = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getRadial(true));
            IDataRequest<Float> nav1_dme_distance = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getDMEDistance());
            IDataRequest<Float> nav1_dme_speed = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getDMESpeed());
            IDataRequest<Float> nav1_dme_tts = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getDMETimeToStation());
            IDataRequest<Double> nav1_latitude1 = (IDataRequest<Double>) fsuipc.addOneTimeRequest(nav1Helper.getLatitude1());
            IDataRequest<Double> nav1_longitude1 = (IDataRequest<Double>) fsuipc.addOneTimeRequest(nav1Helper.getLongitude1());
            IDataRequest<Double> nav1_elevation1 = (IDataRequest<Double>) fsuipc.addOneTimeRequest(nav1Helper.getElevation1(true));
            ShortRequest nav1_obs = (ShortRequest) fsuipc.addOneTimeRequest(nav1Helper.getOBSSettings());
            IDataRequest<Float> nav1_magvar = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getMagneticVariation(true));
            IDataRequest<Float> nav1_relbearing = (IDataRequest<Float>) fsuipc.addOneTimeRequest(nav1Helper.getVORRelativeBearing());
            
            //Example of getting ias without helper
            IntRequest ias2 = new IntRequest(0x02BC);
            fsuipc.addOneTimeRequest(ias2);
                        
            //process the requests, once
            ret = fsuipc.processRequestsOnce();
            
            //Now, if the processing went Ok, we will print out the resulting data
            System.out.println("FSUIPC processed one time requests with result code: " + String.valueOf(ret));
            if (ret == FSUIPC.PROCESS_RESULT_OK) {
                System.out.println("FSX/P3D version: " + fsxp3dVersion.getValue());
                System.out.println("On ground: " + String.valueOf(on_ground.getValue()));
                
                System.out.println("Aircraft IAS: " + String.valueOf(ias.getValue()));
                System.out.println("Aircraft IAS (method 2): " + String.valueOf(ias2.getValue() / 128.0f));
                System.out.println("Aircraft TAS: " + String.valueOf(tas.getValue()));
                System.out.println("Aircraft VS: " + String.valueOf(vs.getValue()));
                System.out.println("Aircraft Altitude: " + String.valueOf(altitude.getValue()));                
                System.out.println("Aircraft Latitude: " + String.valueOf(latitude.getValue()));
                System.out.println("Aircraft Longitude: " + String.valueOf(longitude.getValue()));
                System.out.println("Aircraft GPS Latitude: " + String.valueOf(gps_latitude.getValue()));
                System.out.println("Aircraft GPS Longitude: " + String.valueOf(gps_longitude.getValue()));
                System.out.println("Aircraft GPS Altitude: " + String.valueOf(gps_altitude.getValue()));
                float fHeading = true_heading.getValue();
                float fMagVar = magVar.getValue();
                System.out.println("Aircraft TRUE Heading: " + String.valueOf(fHeading));
                System.out.println("Aircraft Magnetic Variation: " + String.valueOf(fMagVar));
                System.out.println("Aircraft Magnetic Heading: " + String.valueOf(fHeading - fMagVar));
                System.out.println("Aircraft Pitch: " + String.valueOf(pitch.getValue()));
                System.out.println("Aircraft Bank: " + String.valueOf(bank.getValue()));
                System.out.println("Aircraft Number Of Engines: " + String.valueOf(num_of_engines.getValue()));
                System.out.println("Aircraft Engine Type: " + String.valueOf(engine_type.getValue()));
                
                System.out.println("Engine 1 Throttle: " + String.valueOf(eng1_thr.getValue()));
                System.out.println("Engine 2 Throttle: " + String.valueOf(eng2_thr.getValue()));
                System.out.println("Situation file: " + situation_file.getValue());
                System.out.println("ATC flight number: " + atc_flight_num.getValue());
                System.out.println("ATC identification: " + atc_ident.getValue());
                
                System.out.println("COM1 frequency: " + String.valueOf(com1_frequency.getValue()));
                System.out.println("COM2 frequency: " + String.valueOf(com2_frequency.getValue()));
                System.out.println("NAV1 frequency: " + String.valueOf(nav1_frequency.getValue()));
                System.out.println("NAV2 frequency: " + String.valueOf(nav2_frequency.getValue()));
                
                System.out.println("NAV1 name: " + nav1_name.getValue());
                System.out.println("NAV1 identity: " + nav1_ident.getValue());
                System.out.println("NAV1 signal strength: " + String.valueOf(nav1_signal_strength.getValue()));
                System.out.println("NAV1 radial: " + String.valueOf(nav1_radial.getValue()));
                System.out.println("NAV1 CDI: " + String.valueOf(nav1_cdi.getValue()));
                System.out.println("NAV1 GSI: " + String.valueOf(nav1_gsi.getValue()));
                System.out.println("NAV1 code flags: " + Integer.toBinaryString(nav1_code_flag.getValue()));
                System.out.println("NAV1 back course flags: " + Integer.toBinaryString(nav1_bc_flag.getValue()));                
                System.out.println("NAV1/VOR1 DME distance: " + String.valueOf(nav1_dme_distance.getValue()) + " Nm");
                System.out.println("NAV1/VOR1 DME speed: " + String.valueOf(nav1_dme_speed.getValue()) + " Kts");
                System.out.println("NAV1/VOR1 DME time to station: " + String.valueOf(nav1_dme_tts.getValue()) + " seconds");
                System.out.println("NAV1/VOR1 (or glideslope transmitter) latitude: " + String.valueOf(nav1_latitude1.getValue()));
                System.out.println("NAV1/VOR1 (or glideslope transmitter) longitude: " + String.valueOf(nav1_longitude1.getValue()));
                System.out.println("NAV1/VOR1 (or glideslope transmitter) elevation: " + String.valueOf(nav1_elevation1.getValue()) + " feet");
                System.out.println("NAV1 OBS settings: " + String.valueOf(nav1_obs.getValue()));   
                System.out.println("NAV1/VOR1 magnetic variation: " + String.valueOf(nav1_magvar.getValue()));   
                System.out.println("NAV1/VOR1 relative bearing: " + String.valueOf(nav1_relbearing.getValue())); 
                
                //test our store procedure
                com1_frequency.setValue(124.88f);
                System.out.println("COM1 frequency: " + String.valueOf(com1_frequency.getValue()));
            }
            
            //Disconnect FSUIPC and free used resources
            fsuipc.disconnect();
        }
    }    
}
