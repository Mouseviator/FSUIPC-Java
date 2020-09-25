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
import com.mouseviator.fsuipc.IFSUIPCListener;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.advanced.FSControlRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.datarequest.primitives.ShortRequest;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;
import com.mouseviator.fsuipc.helpers.aircraft.Engine1Helper;
import com.mouseviator.fsuipc.helpers.aircraft.Engine2Helper;
import com.mouseviator.fsuipc.helpers.avionics.GPSHelper;
import com.mouseviator.fsuipc.helpers.SimHelper;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.util.AbstractQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import javax.swing.SwingUtilities;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * This is more complex example on using {@link FSUIPC} calls, data requests, {@link IFSUIPCListener} for continual data request processing,
 * and displaying data in Swing GUI application.
 * 
 * @author Murdock
 */
public class FSUIPCSimMonitor extends javax.swing.JFrame {

    /**
     * Default zoom
     */
    private final byte DEFAULT_ZOOM = 7;
    /**
     * Default location to center map onto
     */
    public static final GeoPosition DEFAULT_LOCATION = new GeoPosition(48.652032, -122.585922);

    /**
     * JXmapViewer kit
     */
    private final JXMapKit jXMapKit = new JXMapKit();

    /**
     * FSUIPC Instance
     */
    private final FSUIPC fsuipc = FSUIPC.getInstance();
    /**
     * FSUIPC listener
     */
    private IFSUIPCListener fsuipcListener;
    /**
     * logger
     */
    private static final Logger logger = Logger.getLogger(FSUIPCSimMonitor.class.getName());

    private final AircraftHelper aircraftHelper = new AircraftHelper();
    private final SimHelper simHelper = new SimHelper();
    private final GPSHelper gpsHelper = new GPSHelper();
    private final Engine1Helper engine1Helper = new Engine1Helper();
    private final Engine2Helper engine2Helper = new Engine2Helper();
    
    private FloatRequest aircraftHeading;
    private IDataRequest<Float> aircraftMagVar;
    private FloatRequest aircraftBank;
    private FloatRequest aircraftPitch;
    private IDataRequest<Double> gpsAltitude;
    private DoubleRequest aircraftLatitude;
    private DoubleRequest aircraftLongitude;    
    private FloatRequest aircraftIAS;
    private FloatRequest aircraftTAS;
    private FloatRequest aircraftVS;
    private IDataRequest<Short> pauseIndicator;
    private IntRequest simLocalTime;
    private ShortRequest eng1ThrLever;
    private ShortRequest eng1MixLever;
    private ShortRequest eng1PropLever;
    private FloatRequest eng1OilTemp;
    private FloatRequest eng1OilQuantity;
    private FloatRequest eng1OilPressure;
    private DoubleRequest eng1FuelFlow;
    private ShortRequest eng2ThrLever;
    private ShortRequest eng2MixLever;
    private ShortRequest eng2PropLever;
    private FloatRequest eng2OilTemp;
    private FloatRequest eng2OilQuantity;
    private FloatRequest eng2OilPressure;
    private DoubleRequest eng2FuelFlow;
    private IDataRequest<Float> simFrameRate;
    private ShortRequest slewMode;
    private FSControlRequest slewControl = new FSControlRequest(65557);   //fs slew toggle control

    private final DecimalFormat decimalFormat1 = new DecimalFormat("#.#");
    
    private final DecimalFormat decimalFormat3 = new DecimalFormat("#.###");
    
    private boolean libFileLoggingEnabled = true;

    /**
     * Creates new form FSUIPCAircraftMonitor
     */
    public FSUIPCSimMonitor() {
        //Init decimal formatters, will need that for "nice" data output
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        this.decimalFormat1.setDecimalFormatSymbols(dfs);
        this.decimalFormat3.setDecimalFormatSymbols(dfs);
        
        //set finer level for debugging
        final Logger logger = Logger.getLogger("com.mouseviator");
        //disable system handlers for us, or the FINER level will not apply
        logger.setUseParentHandlers(false);
        //custom console handler
        final Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINER);
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.FINER);
        

        initComponents();

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                //disconnect fsuipc, no nned to cancel processing tasks, the diconnect method will do it for us
                fsuipc.disconnect();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        initMapViewer();
        fsuipcStatusChanged(false);
        setTitle(false);
        startFSUIPC();        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMapViewer = new javax.swing.JPanel();
        lblSituationFileCap = new javax.swing.JLabel();
        lblSituationFile = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblPitchCap = new javax.swing.JLabel();
        lblPitch = new javax.swing.JLabel();
        lblBank = new javax.swing.JLabel();
        lblBankCap = new javax.swing.JLabel();
        lblHeadingCap = new javax.swing.JLabel();
        lblHeading = new javax.swing.JLabel();
        lblLatitudeCap = new javax.swing.JLabel();
        lblLatitude = new javax.swing.JLabel();
        lblLongitudeCap = new javax.swing.JLabel();
        lblLongitude = new javax.swing.JLabel();
        lblAltitudeCap = new javax.swing.JLabel();
        lblAltitude = new javax.swing.JLabel();
        lblVSCap = new javax.swing.JLabel();
        lblVS = new javax.swing.JLabel();
        lblIASCap = new javax.swing.JLabel();
        lblIAS = new javax.swing.JLabel();
        lblTASCap = new javax.swing.JLabel();
        lblTAS = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblStatusCap = new javax.swing.JLabel();
        lblFSVersionCap = new javax.swing.JLabel();
        lblFSVersion = new javax.swing.JLabel();
        lblFSUIPCVersionCap = new javax.swing.JLabel();
        lblFSUIPCVersion = new javax.swing.JLabel();
        lblFSUIPCLibVersionCap = new javax.swing.JLabel();
        lblFSUIPCLibVersion = new javax.swing.JLabel();
        lblStatus2 = new javax.swing.JLabel();
        lblStatus1 = new javax.swing.JLabel();
        lblSimLocalTimeCap = new javax.swing.JLabel();
        lblSimLocalTime = new javax.swing.JLabel();
        cmdPause = new javax.swing.JButton();
        lblFrameRateCap = new javax.swing.JLabel();
        lblFrameRate = new javax.swing.JLabel();
        lblInfo = new javax.swing.JLabel();
        pnlEngineInfo = new javax.swing.JPanel();
        lblEng1ThrCap = new javax.swing.JLabel();
        lblEng2ThrCap = new javax.swing.JLabel();
        lblEng1MixCap = new javax.swing.JLabel();
        lblEng2MixCap = new javax.swing.JLabel();
        pbEngine1Thr = new javax.swing.JProgressBar();
        pbEngine2Thr = new javax.swing.JProgressBar();
        pbEngine1Mix = new javax.swing.JProgressBar();
        pbEngine2Mix = new javax.swing.JProgressBar();
        lblEng2PropCap = new javax.swing.JLabel();
        lblEng1PropCap = new javax.swing.JLabel();
        pbEngine1Prop = new javax.swing.JProgressBar();
        pbEngine2Prop = new javax.swing.JProgressBar();
        lblEng1OilQuantityCap = new javax.swing.JLabel();
        lblEngine1 = new javax.swing.JLabel();
        lblEng1OilQuantity = new javax.swing.JLabel();
        lblEng1OilTemp = new javax.swing.JLabel();
        lblEng1OilTempCap = new javax.swing.JLabel();
        lblEng1OilPress = new javax.swing.JLabel();
        lblEng1OilPressCap = new javax.swing.JLabel();
        lblEngine2 = new javax.swing.JLabel();
        lblEng2OilQuantityCap = new javax.swing.JLabel();
        lblEng2OilQuantity = new javax.swing.JLabel();
        lblEng2OilTempCap = new javax.swing.JLabel();
        lblEng2OilPressCap = new javax.swing.JLabel();
        lblEng2OilTemp = new javax.swing.JLabel();
        lblEng2OilPress = new javax.swing.JLabel();
        lblEng2FFCap = new javax.swing.JLabel();
        lblEng2FF = new javax.swing.JLabel();
        lblEng1FFCap = new javax.swing.JLabel();
        lblEng1FF = new javax.swing.JLabel();
        cmdToggleLibFileLogging = new javax.swing.JButton();
        cmdSlewToggle = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FSUIPC Aircraft Monitor");

        pnlMapViewer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlMapViewer.setLayout(new java.awt.BorderLayout());

        lblSituationFileCap.setText("Situation file:");

        lblSituationFile.setFont(new java.awt.Font("Tahoma", 3, 11)); // NOI18N
        lblSituationFile.setText("N/A");
        lblSituationFile.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSituationFile.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Aircraft Info:"));

        lblPitchCap.setText("Pitch:");

        lblPitch.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblBank.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblBankCap.setText("Bank:");

        lblHeadingCap.setText("Heading:");

        lblHeading.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblLatitudeCap.setText("Latitude:");

        lblLatitude.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblLongitudeCap.setText("Longitude:");

        lblLongitude.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblAltitudeCap.setText("Altitude:");

        lblAltitude.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblVSCap.setText("Vertical speed:");

        lblVS.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblIASCap.setText("IAS:");

        lblIAS.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblTASCap.setText("TAS:");

        lblTAS.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblVSCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblAltitudeCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPitchCap, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblBankCap, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHeadingCap, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblLongitudeCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblLatitudeCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblVS, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                    .addComponent(lblLatitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblLongitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblAltitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblHeading, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblPitch, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblBank, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblIASCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTASCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblIAS, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .addComponent(lblTAS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblAltitudeCap, lblBankCap, lblHeadingCap, lblIASCap, lblLatitudeCap, lblLongitudeCap, lblPitchCap, lblTASCap, lblVSCap});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblAltitude, lblBank, lblHeading, lblIAS, lblLatitude, lblLongitude, lblPitch, lblTAS, lblVS});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPitchCap)
                    .addComponent(lblPitch, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBankCap)
                    .addComponent(lblBank, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHeadingCap)
                    .addComponent(lblHeading, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLatitudeCap)
                    .addComponent(lblLatitude, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLongitudeCap)
                    .addComponent(lblLongitude, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAltitudeCap)
                    .addComponent(lblAltitude, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVSCap)
                    .addComponent(lblVS, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIASCap)
                    .addComponent(lblIAS, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTASCap)
                    .addComponent(lblTAS, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Sim info:"));

        lblStatusCap.setText("Status:");
        lblStatusCap.setToolTipText("");

        lblFSVersionCap.setText("FS Version:");
        lblFSVersionCap.setToolTipText("");

        lblFSVersion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblFSUIPCVersionCap.setText("FSUIPC version:");
        lblFSUIPCVersionCap.setToolTipText("");

        lblFSUIPCVersion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblFSUIPCLibVersionCap.setText("FSUIPC Lib version:");
        lblFSUIPCLibVersionCap.setToolTipText("");

        lblFSUIPCLibVersion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        lblStatus2.setLabelFor(lblStatusCap);

        lblStatus1.setLabelFor(lblStatusCap);

        lblSimLocalTimeCap.setText("Local time:");
        lblSimLocalTimeCap.setToolTipText("");

        lblSimLocalTime.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        cmdPause.setText("Pause");
        cmdPause.setEnabled(false);
        cmdPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdPauseActionPerformed(evt);
            }
        });

        lblFrameRateCap.setText("Frame rate:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStatusCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFSUIPCLibVersionCap, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFSUIPCVersionCap, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFSVersionCap, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFSVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFSUIPCVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFSUIPCLibVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblStatus1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblStatus2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmdPause))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblFrameRateCap, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSimLocalTimeCap, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSimLocalTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFrameRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblFSUIPCLibVersionCap, lblFSUIPCVersionCap, lblFSVersionCap, lblStatusCap});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblStatusCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblStatus1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblStatus2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmdPause))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFSVersionCap)
                    .addComponent(lblFSVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFSUIPCVersionCap)
                    .addComponent(lblFSUIPCVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFSUIPCLibVersionCap)
                    .addComponent(lblFSUIPCLibVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSimLocalTimeCap)
                    .addComponent(lblSimLocalTime, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFrameRateCap)
                    .addComponent(lblFrameRate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlEngineInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("Engine Info:"));
        pnlEngineInfo.setPreferredSize(new java.awt.Dimension(215, 100));

        lblEng1ThrCap.setText("Throttle Lever:");

        lblEng2ThrCap.setText("Engine 2 Throttle:");

        lblEng1MixCap.setText("Mixture Lever:");

        lblEng2MixCap.setText("Engine 2 Mixture:");

        pbEngine1Thr.setMaximum(16384);
        pbEngine1Thr.setMinimum(-4096);
        pbEngine1Thr.setString("0");
        pbEngine1Thr.setStringPainted(true);

        pbEngine2Thr.setMaximum(16384);
        pbEngine2Thr.setMinimum(-4096);
        pbEngine2Thr.setString("0");
        pbEngine2Thr.setStringPainted(true);

        pbEngine1Mix.setMaximum(16384);
        pbEngine1Mix.setString("0");
        pbEngine1Mix.setStringPainted(true);

        pbEngine2Mix.setMaximum(16384);
        pbEngine2Mix.setString("0");
        pbEngine2Mix.setStringPainted(true);

        lblEng2PropCap.setText("Engine 2 Propeller:");

        lblEng1PropCap.setText("Propeller Lever:");

        pbEngine1Prop.setMaximum(16384);
        pbEngine1Prop.setMinimum(-4096);
        pbEngine1Prop.setString("0");
        pbEngine1Prop.setStringPainted(true);

        pbEngine2Prop.setMaximum(16384);
        pbEngine2Prop.setMinimum(-4096);
        pbEngine2Prop.setString("0");
        pbEngine2Prop.setStringPainted(true);

        lblEng1OilQuantityCap.setText("Oil Quantity:");

        lblEngine1.setBackground(new java.awt.Color(204, 204, 204));
        lblEngine1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEngine1.setText("Engine 1");
        lblEngine1.setOpaque(true);

        lblEng1OilTempCap.setText("Oil Temperature:");

        lblEng1OilPressCap.setText("Oil Pressure:");

        lblEngine2.setBackground(new java.awt.Color(204, 204, 204));
        lblEngine2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEngine2.setText("Engine 2");
        lblEngine2.setOpaque(true);

        lblEng2OilQuantityCap.setText("Oil Quantity:");

        lblEng2OilTempCap.setText("Oil Temperature:");

        lblEng2OilPressCap.setText("Oil Pressure:");

        lblEng2FFCap.setText("Fuel flow:");

        lblEng1FFCap.setText("Fuel flow:");

        javax.swing.GroupLayout pnlEngineInfoLayout = new javax.swing.GroupLayout(pnlEngineInfo);
        pnlEngineInfo.setLayout(pnlEngineInfoLayout);
        pnlEngineInfoLayout.setHorizontalGroup(
            pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEng2ThrCap, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEng2MixCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pbEngine2Mix, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                            .addComponent(pbEngine2Thr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng2PropCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pbEngine2Prop, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEng1ThrCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEng1MixCap, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pbEngine1Mix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pbEngine1Thr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng1PropCap, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pbEngine1Prop, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng1OilQuantityCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng1OilQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblEngine1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng1OilTempCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng1OilTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng1OilPressCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng1OilPress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblEngine2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng2OilQuantityCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng2OilQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng2OilTempCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng2OilTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng2OilPressCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng2OilPress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng2FFCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng2FF, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                        .addComponent(lblEng1FFCap, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEng1FF, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pnlEngineInfoLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblEng1MixCap, lblEng1ThrCap, lblEng2MixCap, lblEng2ThrCap});

        pnlEngineInfoLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {pbEngine1Mix, pbEngine1Thr, pbEngine2Mix, pbEngine2Thr});

        pnlEngineInfoLayout.setVerticalGroup(
            pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEngineInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblEngine1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEng1ThrCap)
                    .addComponent(pbEngine1Thr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEng1MixCap)
                    .addComponent(pbEngine1Mix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEng1PropCap)
                    .addComponent(pbEngine1Prop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng1OilQuantityCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng1OilQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng1OilTempCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng1OilTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng1OilPressCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng1OilPress, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng1FFCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng1FF, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblEngine2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEng2ThrCap)
                    .addComponent(pbEngine2Thr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEng2MixCap)
                    .addComponent(pbEngine2Mix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEng2PropCap)
                    .addComponent(pbEngine2Prop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng2OilQuantityCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng2OilQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng2OilTempCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng2OilTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng2OilPressCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng2OilPress, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEngineInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEng2FFCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEng2FF, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cmdToggleLibFileLogging.setText("Enable Lib File Log");
        cmdToggleLibFileLogging.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdToggleLibFileLoggingActionPerformed(evt);
            }
        });

        cmdSlewToggle.setText("Slew");
        cmdSlewToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSlewToggleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlMapViewer, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmdSlewToggle))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlEngineInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSituationFileCap, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblSituationFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 1013, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmdToggleLibFileLogging, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(14, 14, 14))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlEngineInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlMapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdSlewToggle)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSituationFileCap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSituationFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdToggleLibFileLogging))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPauseActionPerformed
        final IDataRequest<Short> writePauseRequest;
        
        //well, dont like to do the decision by string comparison, but this is an example
        if (cmdPause.getText().equals("Pause")) {
            //pause the sim
            writePauseRequest = simHelper.setPause(true);
        } else {
            //unpuase the sim
            writePauseRequest = simHelper.setPause(false);
        }                
        
        //send the request to fsuipc - it will be processed on next processing call (which started by processRequests)
        fsuipc.addOneTimeRequest(writePauseRequest);
    }//GEN-LAST:event_cmdPauseActionPerformed

    private void cmdToggleLibFileLoggingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdToggleLibFileLoggingActionPerformed
        // TODO add your handling code here:
        toggleLibFileLogging();
    }//GEN-LAST:event_cmdToggleLibFileLoggingActionPerformed

    private void cmdSlewToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSlewToggleActionPerformed
        //send fs control to toggle the slew mode
        fsuipc.addOneTimeRequest(slewControl);
    }//GEN-LAST:event_cmdSlewToggleActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FSUIPCSimMonitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FSUIPCSimMonitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FSUIPCSimMonitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FSUIPCSimMonitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FSUIPCSimMonitor().setVisible(true);
            }
        });
    }

    private void initMapViewer() {

        //some init stuff here
        pnlMapViewer.add(jXMapKit);

        jXMapKit.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

        jXMapKit.setAddressLocationShown(true);
        jXMapKit.setAddressLocation(DEFAULT_LOCATION);
        jXMapKit.setZoom(DEFAULT_ZOOM);        
    }

    private void startFSUIPC() {
        // First of all, load the native library. The default load function will try to determine if we are running under 32 or 64 bit JVM
        // and load 32/64 bit native library respectively
        byte result = FSUIPC.load();
        if (result != FSUIPC.LIB_LOAD_RESULT_OK) {
            lblInfo.setText("<html><p style=\"background-color: red; color: white; font-weight: bold\">Failed to load native library. NO JOY! This is all folks!!!</p></html>");                        
            return;
        }
        
        toggleLibFileLogging();    
        
        fsuipcListener = new IFSUIPCListener() {
            @Override
            public void onConnected() {
                logger.info("FSUIPC connected!");

                //register one time requests
                IDataRequest<String> situationFile = (IDataRequest<String>) fsuipc.addOneTimeRequest(simHelper.getSituationFile());
                IDataRequest<String> fsxp3dVersion = (IDataRequest<String>) fsuipc.addOneTimeRequest(simHelper.getFSXP3DVersion());
                fsuipc.processRequestsOnce();

                //clear all previous continual requests, it will also stop processing thread
                fsuipc.clearContinualRequests();

                //register continual requests                
                aircraftHeading = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getHeading());
                aircraftMagVar = (IDataRequest<Float>) fsuipc.addContinualRequest(aircraftHelper.getMagneticVariation());
                aircraftBank = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getBank());
                aircraftPitch = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getPitch());
                gpsAltitude = (IDataRequest<Double>) fsuipc.addContinualRequest(gpsHelper.getAltitude(true));                
                aircraftLatitude = (DoubleRequest) fsuipc.addContinualRequest(aircraftHelper.getLatitude());
                aircraftLongitude = (DoubleRequest) fsuipc.addContinualRequest(aircraftHelper.getLongitude());
                aircraftVS = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getVerticalSpeed(true));
                aircraftIAS = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getIAS());
                aircraftTAS = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getTAS());
                pauseIndicator = (IDataRequest<Short>) fsuipc.addContinualRequest(simHelper.getPauseIndicator());
                simLocalTime = (IntRequest) fsuipc.addContinualRequest(simHelper.getLocalTime());
                simFrameRate = (IDataRequest<Float>) fsuipc.addContinualRequest(simHelper.getFrameRate());
                
                eng1ThrLever = (ShortRequest) fsuipc.addContinualRequest(engine1Helper.getThrottleLever());
                eng1MixLever = (ShortRequest) fsuipc.addContinualRequest(engine1Helper.getMixtureLever());
                eng1PropLever = (ShortRequest) fsuipc.addContinualRequest(engine1Helper.getPropellerLever());
                eng1OilQuantity = (FloatRequest) fsuipc.addContinualRequest(engine1Helper.getOilQuantity());
                eng1OilTemp = (FloatRequest) fsuipc.addContinualRequest(engine1Helper.getOilTemperature());
                eng1OilPressure = (FloatRequest) fsuipc.addContinualRequest(engine1Helper.getOilPressure());
                eng1FuelFlow = (DoubleRequest) fsuipc.addContinualRequest(engine2Helper.getFuelFlow());
                
                eng2ThrLever = (ShortRequest) fsuipc.addContinualRequest(engine2Helper.getThrottleLever());
                eng2MixLever = (ShortRequest) fsuipc.addContinualRequest(engine2Helper.getMixtureLever());
                eng2PropLever = (ShortRequest) fsuipc.addContinualRequest(engine2Helper.getPropellerLever());
                eng2OilQuantity = (FloatRequest) fsuipc.addContinualRequest(engine2Helper.getOilQuantity());
                eng2OilTemp = (FloatRequest) fsuipc.addContinualRequest(engine2Helper.getOilTemperature());
                eng2OilPressure = (FloatRequest) fsuipc.addContinualRequest(engine2Helper.getOilPressure());
                eng2FuelFlow = (DoubleRequest) fsuipc.addContinualRequest(engine2Helper.getFuelFlow());
                
                //slew mode indicator request
                slewMode = (ShortRequest) fsuipc.addContinualRequest(new ShortRequest(0x05DC));

                //start continual request processing at the rate of 250ms
                fsuipc.processRequests(250, true);

                //GUI updates should be done at EDT thread
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fsuipcStatusChanged(true);
                        lblSituationFile.setText(situationFile.getValue().trim());
                        lblFSVersion.setText(fsuipc.getFSVersion() + " (" + fsxp3dVersion.getValue() + ")");
                        
                        cmdPause.setEnabled(true);
                        //set app title - connected
                        setTitle(true);
                    }
                });
            }

            @Override
            public void onDisconnected() {
                logger.info("FSUIPC disconnected!");

                //cancel continual request processing
                //not needed anymore, the fsuipc class will do it while it discovers that FSUIPC disconnected, before the listener is called
                //fsuipc.cancelRequestsProcessing();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fsuipcStatusChanged(false);
                        
                        lblStatus2.setText("N/A");
                        lblSimLocalTime.setText("N/A");
                        lblFrameRate.setText("N/A");
                        
                        lblHeading.setText("N/A");
                        lblPitch.setText("N/A");
                        lblBank.setText("N/A");
                        lblLatitude.setText("N/A");
                        lblLongitude.setText("N/A");
                        lblAltitude.setText("N/A");
                        lblVS.setText("N/A");
                        lblIAS.setText("N/A");                        
                        lblTAS.setText("N/A");
                        
                        lblSituationFile.setText("N/A");
                        cmdPause.setEnabled(false);
                        
                        pbEngine1Thr.setValue(0);
                        pbEngine1Mix.setValue(0);
                        pbEngine1Prop.setValue(0);
                        lblEng1FF.setText("N/A");
                        lblEng1OilPress.setText("N/A");
                        lblEng1OilQuantity.setText("N/A");
                        lblEng1OilTemp.setText("N/A");
                        
                        pbEngine2Thr.setValue(0);
                        pbEngine2Mix.setValue(0);
                        pbEngine2Prop.setValue(0);
                        lblEng2FF.setText("N/A");
                        lblEng2OilPress.setText("N/A");
                        lblEng2OilQuantity.setText("N/A");
                        lblEng2OilTemp.setText("N/A");                                                
                        
                        //set app title - not connected
                        setTitle(false);
                    }
                });

            }

            @Override
            public void onProcess(AbstractQueue<IDataRequest> arRequests) {
                logger.fine("FSUIPC continual request processing callback!");

                //set map center
                jXMapKit.setAddressLocation(new GeoPosition(aircraftLatitude.getValue(), aircraftLongitude.getValue()));

                //GUI updates on EDT thread
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        //set the labels from values
                        lblHeading.setText(String.format("%d Mag (%d TRUE)", (int) Math.round(aircraftHeading.getValue() - aircraftMagVar.getValue()), (int) Math.round(aircraftHeading.getValue())));
                        float fValue = aircraftPitch.getValue();                        
                        String label = decimalFormat1.format(Math.abs(fValue));
                        if (fValue == 0) {
                            label = "LEVEL";
                        } else if (fValue < 0) {
                            label += " ° Up";
                        } else {
                            label += " ° Down";
                        }
                        lblPitch.setText(label);
                        
                        fValue = aircraftBank.getValue();                        
                        label = decimalFormat1.format(Math.abs(fValue));
                        if (fValue == 0) {
                            label = "LEVEL";
                        } else if (fValue < 0) {
                            label += " ° Right";
                        } else {
                            label += " ° Left";
                        }
                        lblBank.setText(label);

                        lblLatitude.setText(decimalFormat3.format(aircraftLatitude.getValue()));
                        lblLongitude.setText(decimalFormat3.format(aircraftLongitude.getValue()));
                        lblAltitude.setText(String.format("%d feet", (int) Math.ceil(gpsAltitude.getValue())));

                        lblVS.setText(String.format("%d fpm", (int) Math.ceil(aircraftVS.getValue())));

                        lblIAS.setText(String.format("%d Kts", (int) Math.ceil(aircraftIAS.getValue())));
                        lblTAS.setText(String.format("%d Kts", (int) Math.ceil(aircraftTAS.getValue())));

                        if (pauseIndicator.getValue() == 0) {
                            cmdPause.setText("Pause");
                            lblStatus2.setText("<html><p style=\"background-color: green; color: white; font-weight: bold\">RUNNING</p></html>");
                        } else {
                            cmdPause.setText("Unpause");
                            lblStatus2.setText("<html><p style=\"background-color: red; color: white; font-weight: bold\">PAUSED</p></html>");
                        }
                        
                        final LocalTime localTime = LocalTime.ofSecondOfDay(simLocalTime.getValue());
                        lblSimLocalTime.setText(localTime.toString());
                        lblFrameRate.setText(decimalFormat3.format(simFrameRate.getValue()));
                        
                        //update engine info
                        pbEngine1Thr.setValue(eng1ThrLever.getValue());
                        pbEngine1Mix.setValue(eng1MixLever.getValue());
                        pbEngine1Prop.setValue(eng1PropLever.getValue());
                        lblEng1OilQuantity.setText(String.format("%s %%", decimalFormat1.format(eng1OilQuantity.getValue())));
                        lblEng1OilTemp.setText(String.format("%s °C", decimalFormat1.format(eng1OilTemp.getValue())));
                        lblEng1OilPress.setText(String.format("%s psi", decimalFormat1.format(eng1OilPressure.getValue())));
                        lblEng1FF.setText(String.format("%s pph", decimalFormat3.format(eng1FuelFlow.getValue())));
                        pbEngine1Thr.setString(eng1ThrLever.getValue().toString());
                        pbEngine1Mix.setString(eng1MixLever.getValue().toString());
                        pbEngine1Prop.setString(eng1PropLever.getValue().toString());
                        
                        pbEngine2Thr.setValue(eng2ThrLever.getValue());
                        pbEngine2Mix.setValue(eng2MixLever.getValue());
                        pbEngine2Prop.setValue(eng2PropLever.getValue());
                        lblEng2OilQuantity.setText(String.format("%s %%", decimalFormat1.format(eng2OilQuantity.getValue())));
                        lblEng2OilTemp.setText(String.format("%s °C", decimalFormat1.format(eng2OilTemp.getValue())));
                        lblEng2OilPress.setText(String.format("%s psi", decimalFormat1.format(eng2OilPressure.getValue())));
                        lblEng2FF.setText(String.format("%s pph", decimalFormat3.format(eng2FuelFlow.getValue())));
                        pbEngine2Thr.setString(eng2ThrLever.getValue().toString());
                        pbEngine2Mix.setString(eng2MixLever.getValue().toString());
                        pbEngine2Prop.setString(eng2PropLever.getValue().toString());
                        
                        //if slew mode off
                        if (slewMode.getValue() == (short)0) {
                            cmdSlewToggle.setText("<html><p style=\"background-color: red; color: white; font-weight: bold\">SLEW OFF</p></html>");
                            cmdSlewToggle.setSelected(false);
                        } else {
                            cmdSlewToggle.setText("<html><p style=\"background-color: green; color: white; font-weight: bold\">SLEW ON</p></html>");
                            cmdSlewToggle.setSelected(true);
                        }
                        
                        //update processing time info
                        lblInfo.setText(String.format("Last processing time: %d ms", (long)fsuipc.getLastProcessingTime() / 1000000));
                    }
                });
            }

            @Override
            public void onFail(int lastResult) {
                logger.log(Level.INFO, "Last FSUIPC function call ended with error code: {0}, message: {1}",
                        new Object[]{lastResult,
                            FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult))});
            }
        };

        //add the listener to fsuipc
        fsuipc.addListener(fsuipcListener);

        //start the thread that will wait for successful fsuipc connection, will try every 5 seconds
        fsuipc.waitForConnection(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY, 5);
    }

    private void toggleLibFileLogging() {
        //enable fsuipc library file logging
        if (libFileLoggingEnabled) {                   
            FSUIPCWrapper.setupLogging(true, "fsuipc_java.log", FSUIPCWrapper.LogSeverity.DEBUG.getValue(), 20 * 1024 * 1024);
            cmdToggleLibFileLogging.setText("Disable Lib File Log");
            libFileLoggingEnabled = false;
        } else {
            FSUIPCWrapper.setupLogging(false, "fsuipc_java.log", FSUIPCWrapper.LogSeverity.DEBUG.getValue(), 20 * 1024 * 1024);
            cmdToggleLibFileLogging.setText("Enable Lib File Log");
            libFileLoggingEnabled = true;
        }
    }

    private void setTitle(boolean connected) {
        String title = "FSUIPC Aircraft Monitor";
        
        try {
            String arch = System.getProperty("sun.arch.data.model");
            if (arch.equals("32")) {
                title += " (32 bit) ";
            } else if (arch.equals("64")) {
                title += " (64 bit) ";
            } else {
                title += " (Unknown architecture) ";
            }
        } catch (Exception ex) {
            logger.severe("Failed to determine system architecture!");
        }
        
        if (connected) {
            title += " - Sim CONNECTED!";
        }
        setTitle(title);
    }

    private void fsuipcStatusChanged(boolean connected) {
        if (connected) {
            lblStatus1.setText("<html><p style=\"background-color: green; color: white; font-weight: bold\">CONNECTED</p></html>");
            //lblFSVersion.setText(fsuipc.getFSVersion());
            lblFSUIPCVersion.setText(fsuipc.getVersion());
            lblFSUIPCLibVersion.setText(fsuipc.getLibVersion());
        } else {
            lblStatus1.setText("<html><p style=\"background-color: red; color: white; font-weight: bold\">DISCONNECTED</p></html>");
            lblFSVersion.setText("N/A");
            lblFSUIPCVersion.setText("N/A");
            lblFSUIPCLibVersion.setText("N/A");
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdPause;
    private javax.swing.JToggleButton cmdSlewToggle;
    private javax.swing.JButton cmdToggleLibFileLogging;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblAltitude;
    private javax.swing.JLabel lblAltitudeCap;
    private javax.swing.JLabel lblBank;
    private javax.swing.JLabel lblBankCap;
    private javax.swing.JLabel lblEng1FF;
    private javax.swing.JLabel lblEng1FFCap;
    private javax.swing.JLabel lblEng1MixCap;
    private javax.swing.JLabel lblEng1OilPress;
    private javax.swing.JLabel lblEng1OilPressCap;
    private javax.swing.JLabel lblEng1OilQuantity;
    private javax.swing.JLabel lblEng1OilQuantityCap;
    private javax.swing.JLabel lblEng1OilTemp;
    private javax.swing.JLabel lblEng1OilTempCap;
    private javax.swing.JLabel lblEng1PropCap;
    private javax.swing.JLabel lblEng1ThrCap;
    private javax.swing.JLabel lblEng2FF;
    private javax.swing.JLabel lblEng2FFCap;
    private javax.swing.JLabel lblEng2MixCap;
    private javax.swing.JLabel lblEng2OilPress;
    private javax.swing.JLabel lblEng2OilPressCap;
    private javax.swing.JLabel lblEng2OilQuantity;
    private javax.swing.JLabel lblEng2OilQuantityCap;
    private javax.swing.JLabel lblEng2OilTemp;
    private javax.swing.JLabel lblEng2OilTempCap;
    private javax.swing.JLabel lblEng2PropCap;
    private javax.swing.JLabel lblEng2ThrCap;
    private javax.swing.JLabel lblEngine1;
    private javax.swing.JLabel lblEngine2;
    private javax.swing.JLabel lblFSUIPCLibVersion;
    private javax.swing.JLabel lblFSUIPCLibVersionCap;
    private javax.swing.JLabel lblFSUIPCVersion;
    private javax.swing.JLabel lblFSUIPCVersionCap;
    private javax.swing.JLabel lblFSVersion;
    private javax.swing.JLabel lblFSVersionCap;
    private javax.swing.JLabel lblFrameRate;
    private javax.swing.JLabel lblFrameRateCap;
    private javax.swing.JLabel lblHeading;
    private javax.swing.JLabel lblHeadingCap;
    private javax.swing.JLabel lblIAS;
    private javax.swing.JLabel lblIASCap;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblLatitude;
    private javax.swing.JLabel lblLatitudeCap;
    private javax.swing.JLabel lblLongitude;
    private javax.swing.JLabel lblLongitudeCap;
    private javax.swing.JLabel lblPitch;
    private javax.swing.JLabel lblPitchCap;
    private javax.swing.JLabel lblSimLocalTime;
    private javax.swing.JLabel lblSimLocalTimeCap;
    private javax.swing.JLabel lblSituationFile;
    private javax.swing.JLabel lblSituationFileCap;
    private javax.swing.JLabel lblStatus1;
    private javax.swing.JLabel lblStatus2;
    private javax.swing.JLabel lblStatusCap;
    private javax.swing.JLabel lblTAS;
    private javax.swing.JLabel lblTASCap;
    private javax.swing.JLabel lblVS;
    private javax.swing.JLabel lblVSCap;
    private javax.swing.JProgressBar pbEngine1Mix;
    private javax.swing.JProgressBar pbEngine1Prop;
    private javax.swing.JProgressBar pbEngine1Thr;
    private javax.swing.JProgressBar pbEngine2Mix;
    private javax.swing.JProgressBar pbEngine2Prop;
    private javax.swing.JProgressBar pbEngine2Thr;
    private javax.swing.JPanel pnlEngineInfo;
    private javax.swing.JPanel pnlMapViewer;
    // End of variables declaration//GEN-END:variables
}
