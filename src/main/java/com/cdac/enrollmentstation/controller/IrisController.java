///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cdac.enrollmentstation.controller;
//
//import RealScan.SlapScannerController;
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.logging.ApplicationLogOld;
//import com.cdac.enrollmentstation.model.*;
//import com.cdac.enrollmentstation.service.ObjectReaderWriter;
//import com.fasterxml.jackson.core.Base64Variants;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.mantra.midirisenroll.DeviceInfo;
//import com.mantra.midirisenroll.MIDIrisEnroll;
//import com.mantra.midirisenroll.MIDIrisEnrollCallback;
//import com.mantra.midirisenroll.enums.DeviceDetection;
//import com.mantra.midirisenroll.enums.DeviceModel;
//import com.mantra.midirisenroll.enums.ImageFormat;
//import com.mantra.midirisenroll.enums.IrisSide;
//import com.mantra.midirisenroll.model.ImagePara;
//import com.mantra.midirisenroll.model.ImageQuality;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.WritableImage;
//import javafx.scene.layout.AnchorPane;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.util.List;
//import java.util.*;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * FXML Controller class
// *
// * @author boss
// */
//public class IrisController implements MIDIrisEnrollCallback, Initializable {
//    private MIDIrisEnroll mIDIrisEnroll = null;
//    private DeviceInfo deviceInfo = null;
//    private String deviceName = null;
//    private int minQuality = 30;
//    private int timeout = 10000;
//    private MyIcon mLeftIrisImage;
//    private MyIcon mRightIrisImage;
//    public int retInit;
//
//    @FXML
//    public Label lblerror;
//    @FXML
//    public Label labelarc;
//    @FXML
//    public ImageView lefticon;
//    @FXML
//    public ImageView righticon;
//
//    @FXML
//    public ImageView statusImage;
//
//    @FXML
//    public Button camera;
//
//    @FXML
//    private AnchorPane confirmPane;
//
//    /*
//    @FXML
//    private AnchorPane confirmPane1;*/
//
//    @FXML
//    private Button confirmYesBtn;
//
//    /*
//    @FXML
//    private Button confirmYesBtn1;*/
//
//    @FXML
//    private Button confirmNoBtn;
//
//    /*
//    @FXML
//    private Button confirmNoBtn1;*/
//
//    @FXML
//    private Button captureIris;
//
//    @FXML
//    private Button showFinger;
//
//
//    byte[] leftIrisImage;
//    byte[] rightIrisImage;
//    byte[] leftIrisTemplate;
//    byte[] rightIrisTemplate;
//
//
//    String irisCaptureInfo;
//
//    Boolean isDeviceInitialized = false;
//    Boolean isDeviceConnected = false;
//    public Boolean irisCapturedLeft = false;
//
//    public Boolean irisCapturedRight = false;
//
//
//    SaveEnrollmentDetails saveEnrollment;
//    EnrollmentDetailsHolder enrollmentDetailsHolder = null;
//
//    Set<IRIS> irisSet = new HashSet<>();
//
//    //For Application Log
//    ApplicationLogOld appLog = new ApplicationLogOld();
//    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
//    Handler handler;
//
//    public IrisController() {
//
//        // this.handler = appLog.getLogger();
//        //LOGGER.addHandler(handler);
//        //Comented for IRIs device not connected error from slapscanner
//        /*
//        mIDIrisEnroll = new MIDIrisEnroll(this);
//        //mIDIrisEnroll.SetLogProperties("/home/boss/MIDIris.log", LogLevel.MIDIRIS_ENROLL_LVL_LOG_ERROR);
//        String version = mIDIrisEnroll.GetSDKVersion();
//        System.out.println("sdk version :"+ version);
//
//        //supported device list
//        List<String> deviceList = new ArrayList<String>();
//        int ret = mIDIrisEnroll.GetSupportedDevices(deviceList);
//        System.out.println("return value :"+ ret);
//        if (ret == 0) {
//            //lblerror.setText(mIDIrisEnroll.GetErrorMessage(ret));
//            lblerror.setText("Device is not connected, Kindly connect the IRIS Device");
//            //return;
//        }
//
//        List<String> ls = new ArrayList<String>();
//        for (String list : deviceList) {
//            ls.add(list);
//        }
//        IrisSide[] irisSides = new IrisSide[1];
//        if(mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides)) {
//            List<String> deviceListConnected = new ArrayList<String>();
//        int retConnected = mIDIrisEnroll.GetConnectedDevices(deviceListConnected);
//        if (retConnected != 0) {
//            lblerror.setText("Device is not connected, Kindly connect the IRIS Device");
//            return;
//        }
//        System.out.println("connected devices :"+ deviceListConnected);
//
//        String model = "";
//        //Initialize Iris Device
//        if(deviceListConnected.size()>0) {
//         model = deviceListConnected.get(0);
//        }
//
//        System.out.println("DEVICE NAME:"+model);
//        DeviceInfo info = new DeviceInfo();
//
//        retInit = mIDIrisEnroll.Init(DeviceModel.valueFor(model), info, irisSides);
//        if (retInit != 0) {
//            deviceInfo = null;
//
//            System.out.println("Device err :" + mIDIrisEnroll.GetErrorMessage(ret));
//             isDeviceInitialized = false;
////            showDeviceInfo(null);
////            jblIrisLeftIcon.setIcon(null);
////            jblIrisRightIcon.setIcon(null);
////            showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//            //return;
//        }else {
//
//            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//            SaveEnrollmentDetails saveEnrollment = holder.getenrollmentDetails();
//            System.out.println("details : " + saveEnrollment.getArcNo());
//            saveEnrollment.setiRISScannerSerailNo(info.SerialNo);
//            saveEnrollment.setEnrollmentStatus("IRIS Capture Completed");
//            holder.setEnrollmentDetails(saveEnrollment);
//            System.out.println("DEVICE INITIALIZED");
//            isDeviceInitialized = true;
//        }
//
//        //StartCapture
//        System.out.println("show Iris :::");
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//        ARCDetails a= holder.getARC();
//
//       List<String> iris = a.getIris();
//
//        if(iris.size() == 0) {
//            irisCaptureInfo = "RI, LI";
//            try {
//                int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_BOTH, minQuality, timeout);
//                if (retStartCapture != 0) {
//                    lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//
//                    return;
//                }else {
//                System.out.println("CAPTUTRED");
//
//                }
//            }
//            catch(Exception e){
//                System.out.println("Error : "+e.getMessage());
//            }
//        }
//        else if(iris.get(0).contains("LI")) {
//            irisCaptureInfo = "RI";
//            try {
//                int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_RIGHT, minQuality, timeout);
//                if (retStartCapture != 0) {
//                    lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//
//                    return;
//                }else {
//                System.out.println("CAPTUTRED");
//
//                }
//            }
//            catch(Exception e){
//                System.out.println("Error : "+e.getMessage());
//            }
//        }
//        else if(iris.get(0).contains("RI")) {
//            irisCaptureInfo = "LI";
//            try {
//                int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_LEFT, minQuality, timeout);
//                if (retStartCapture != 0) {
//                    lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//
//                    return;
//                }else {
//                    System.out.println("CAPTUTRED");
//
//                }
//            }
//            catch(Exception e){
//                    System.out.println("Error : "+e.getMessage());
//            }
//        }
//        }
//        else {
//            lblerror.setText("Device is not connected, Kindly reconnect the IRIS");
//        }
//        //connected device list
//       */
//    }
//
//
//    @FXML
//    private void captureIris() {
//
//        try {
//            //connected device list
//            captureIris.setDisable(true);
//            captureIris.setText("RESCAN");
//            lblerror.setText("");
//            IrisSide[] irisSides = new IrisSide[1];
//
//            if (mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides)) {
//                LOGGER.log(Level.INFO, "mIDIrisEnroll.IsDeviceConnected function status:" + mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides));
//                List<String> deviceListConnected = new ArrayList<String>();
//                int retConnected = mIDIrisEnroll.GetConnectedDevices(deviceListConnected);
//                //System.out.println("Get Device Connected:::"+retConnected);
//                LOGGER.log(Level.INFO, "Device Connected List:" + deviceListConnected);
//
//                if (retConnected != 0) {
//                    LOGGER.log(Level.SEVERE, "Get Device Connected error :" + mIDIrisEnroll.GetErrorMessage(retConnected));
//                    lblerror.setText("Device is not connected, Kindly reconnect the IRIS Scanner");
//                    captureIris.setDisable(false);
//                }
//                //System.out.println("connected devices :"+ deviceListConnected);
//                String model = "";
//                //Initialize Iris Device
//                if (deviceListConnected.size() > 0) {
//                    model = deviceListConnected.get(0);
//                }
//                //System.out.println("DEVICE NAME:"+model);
//                LOGGER.log(Level.INFO, "DEVICE NAME:" + model);
//                DeviceInfo info = new DeviceInfo();
//
////        retInit = mIDIrisEnroll.Init(DeviceModel.valueFor(model), info, irisSides);
////        if (retInit != 0) {
////            deviceInfo = null;
////            System.out.println("Device err :" + mIDIrisEnroll.GetErrorMessage(retInit));
//////            showDeviceInfo(null);
//////            jblIrisLeftIcon.setIcon(null);
//////            jblIrisRightIcon.setIcon(null);
//////            showLogs(mIDIrisEnroll.GetErrorMessage(ret));
////                lblerror.setText(mIDIrisEnroll.GetErrorMessage(retInit));
////            return;
////        }else {
////            System.out.println("DEVICE INITIALIZED");
////        }
////
//
////Initialize Iris Device
//                if (isDeviceInitialized == false) {
//
//                    retInit = mIDIrisEnroll.Init(DeviceModel.valueFor(model), info, irisSides);
//                    if (retInit != 0) {
//                        deviceInfo = null;
//                        LOGGER.log(Level.SEVERE, "Device initialization error :" + mIDIrisEnroll.GetErrorMessage(retInit));
//                        //System.out.println("Device err :" + mIDIrisEnroll.GetErrorMessage(retInit));
//                        isDeviceInitialized = false;
////            showDeviceInfo(null);
////            jblIrisLeftIcon.setIcon(null);
////            jblIrisRightIcon.setIcon(null);
////            showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//                        //return;
//                    } else {
//
//                        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                        SaveEnrollmentDetails saveEnrollment = holder.getSaveEnrollmentDetails();
//                        System.out.println("details : " + saveEnrollment.getArcNo());
//                        saveEnrollment.setIRISScannerSerialNo(info.SerialNo);
//                        saveEnrollment.setEnrollmentStatus("IRIS Capture Completed");
//                        holder.setSaveEnrollmentDetails(saveEnrollment);
//                        LOGGER.log(Level.INFO, "IRIS Device initialization successfull :");
//                        //System.out.println("DEVICE INITIALIZED");
//                        isDeviceInitialized = true;
//                    }
//
//                }
//                //StartCapture
//
//                //System.out.println("show Iris ::");
//                ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                ARCDetails a = holder.getArcDetails();
//
//
//                List<String> iris = a.getIris();
//                //To check both IRIS
//       /*
//       if(iris.size() > 0){
//       iris.remove(0);
//       }*/
//                //iris.add("RI");
//                //iris.add("LI");
//                //System.out.println("IRIS left Infor::" +iris.get(0));
//                //System.out.println("IRIS right Infor::" +iris.get(1));
//
//
//                LOGGER.log(Level.INFO, "Inside IrisController getIris :" + iris.toString());
//                if (iris.size() == 0) {
//                    irisCaptureInfo = "RI, LI";
//                    int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_BOTH, minQuality, timeout);
//                    if (retStartCapture != 0) {
//                        lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//                        LOGGER.log(Level.SEVERE, "mIDIrisEnroll.StartCapture() function error message:" + mIDIrisEnroll.GetErrorMessage(retStartCapture));
//                        return;
//                    } else {
//                        LOGGER.log(Level.INFO, "CAPTUTRED");
//
//                    }
//                } else if (iris.size() == 1) {
//
//                    if (iris.get(0).contains("LI")) {
//                        irisCaptureInfo = "RI";
//                        int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_RIGHT, minQuality, timeout);
//                        if (retStartCapture != 0) {
//                            lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//                            LOGGER.log(Level.SEVERE, "mIDIrisEnroll.StartCapture() function error message:" + mIDIrisEnroll.GetErrorMessage(retStartCapture));
//                            return;
//                        } else {
//                            LOGGER.log(Level.INFO, "CAPTUTRED");
//                        }
//                    } else if (iris.get(0).contains("RI")) {
//                        irisCaptureInfo = "LI";
//                        int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_LEFT, minQuality, timeout);
//                        if (retStartCapture != 0) {
//                            lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//                            LOGGER.log(Level.SEVERE, "mIDIrisEnroll.StartCapture() function error message:" + mIDIrisEnroll.GetErrorMessage(retStartCapture));
//                            return;
//                        } else {
//                            LOGGER.log(Level.INFO, "CAPTUTRED");
//                        }
//                    }
//                } else if (iris.size() == 2) {
//                    System.out.println("IN BOTH IRIS EXCEPTION");
//                    camera.setDisable(false);
//                    captureIris.setDisable(true);
//                    showFinger.setDisable(true);
//                    lblerror.setText("IRIS Capturing not required, Kindly proceed to capture photo");
//                    LOGGER.log(Level.INFO, "IRIS Capture Not Required, Kindly proceed to photo Capture");
//                }
//
//            } else {
//                LOGGER.log(Level.INFO, "mIDIrisEnroll.IsDeviceConnected function status:" + mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides));
//                //lblerror.setText("Device is not connected, Kindly reconnect the IRIS");
//                lblerror.setText("Device is not connected, Kindly reconnect the IRIS Scanner");
//                captureIris.setDisable(false);
//                //LOGGER.log(Level.INFO,"Device is not connected, Kindly reconnect the IRIS");
//            }
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Exception:" + e);
//            lblerror.setText("Exception:" + e);
//        }
//
//
//    }
//
//    @Override
//    public void initialize(URL arg0, ResourceBundle arg1) {
//
//        try {
//            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//            SaveEnrollmentDetails saveEnrollment = holder.getSaveEnrollmentDetails();
//
//            ARCDetails a = holder.getArcDetails();
//
//            //saveEnrollment.setEnrollmentStatus("IRIS Capture Completed");
//
//            labelarc.setText("ARC: " + a.getArcNo());
//
//            //Added now 21122021 getting Error Message
//            mIDIrisEnroll = new MIDIrisEnroll(this);
//            //mIDIrisEnroll.SetLogProperties("/home/boss/MIDIris.log", LogLevel.MIDIRIS_ENROLL_LVL_LOG_ERROR);
//            String version = mIDIrisEnroll.GetSDKVersion();
//            System.out.println("sdk version :" + version);
//
//            //supported device list
//            List<String> deviceList = new ArrayList<String>();
//            int ret = mIDIrisEnroll.GetSupportedDevices(deviceList);
//            System.out.println("return value :" + ret);
//            if (ret != 0) {
//                lblerror.setText(mIDIrisEnroll.GetErrorMessage(ret));
//                LOGGER.log(Level.INFO, mIDIrisEnroll.GetErrorMessage(ret));
//                return;
//            }
//
//            List<String> ls = new ArrayList<String>();
//            for (String list : deviceList) {
//                ls.add(list);
//            }
//        /*
//        IrisSide[] irisSides = new IrisSide[1];
//        if(mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides)) {
//           LOGGER.log(Level.INFO,"mIDIrisEnroll.IsDeviceConnected function status:"+mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides));
//           lblerror.setText("IRIS Device Connected");
//        }else{
//           LOGGER.log(Level.INFO,"mIDIrisEnroll.IsDeviceConnected function status:"+mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides));
//           //lblerror.setText("Device is not connected, Kindly reconnect the IRIS");
//           lblerror.setText("IRIS Device MalFunction, Kindly contact Device Admin");
//        }*/
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Exception:" + e);
//            lblerror.setText("Exception:" + e);
//        }
//        //Commented on 21/04/22
//       /*
//      if(saveEnrollment.getEnrollmentStatus().equals("IRIS Capture Completed"))
//        {
//            System.out.println("Iris is Completed");
//            confirmPane1.setVisible(true);
//
//
//        }else{
//          System.out.println("Iris is Not Completed");
//         }*/
//
//        /*
//        IrisSide[] irisSides = new IrisSide[1];
//        if(mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides)) {
//            List<String> deviceListConnected = new ArrayList<String>();
//        int retConnected = mIDIrisEnroll.GetConnectedDevices(deviceListConnected);
//        if (retConnected != 0) {
//            lblerror.setText("Device is not connected, Kindly connect the IRIS Device");
//            return;
//        }
//        System.out.println("connected devices :"+ deviceListConnected);
//
//        String model = "";
//        //Initialize Iris Device
//        if(deviceListConnected.size()>0) {
//         model = deviceListConnected.get(0);
//        }
//
//        System.out.println("DEVICE NAME:"+model);
//        DeviceInfo info = new DeviceInfo();
//
//        retInit = mIDIrisEnroll.Init(DeviceModel.valueFor(model), info, irisSides);
//        if (retInit != 0) {
//            deviceInfo = null;
//
//            System.out.println("Device err :" + mIDIrisEnroll.GetErrorMessage(ret));
//             isDeviceInitialized = false;
////            showDeviceInfo(null);
////            jblIrisLeftIcon.setIcon(null);
////            jblIrisRightIcon.setIcon(null);
////            showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//            //return;
//        }else {
//
//          //  ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//            SaveEnrollmentDetails saveEnrollment = holder.getenrollmentDetails();
//            System.out.println("details : " + saveEnrollment.getArcNo());
//            saveEnrollment.setiRISScannerSerailNo(info.SerialNo);
//            saveEnrollment.setEnrollmentStatus("IRIS Capture Completed");
//            holder.setEnrollmentDetails(saveEnrollment);
//            System.out.println("DEVICE INITIALIZED");
//            isDeviceInitialized = true;
//        }
//
//        //StartCapture
//        System.out.println("show Iris :::");
////        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
////        ARCDetails a= holder.getARC();
//
//       List<String> iris = a.getIris();
//
//
//        if(iris.size() == 0) {
//            irisCaptureInfo = "RI, LI";
//            try {
//                int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_BOTH, minQuality, timeout);
//                if (retStartCapture != 0) {
//                    lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//
//                    return;
//                }else {
//                System.out.println("CAPTUTRED");
//
//                }
//            }
//            catch(Exception e){
//                System.out.println("Error : "+e.getMessage());
//            }
//        }
//        else if(iris.get(0).contains("LI")) {
//            irisCaptureInfo = "RI";
//            try {
//                int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_RIGHT, minQuality, timeout);
//                if (retStartCapture != 0) {
//                    lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//
//                    return;
//                }else {
//                System.out.println("CAPTUTRED");
//
//                }
//            }
//            catch(Exception e){
//                System.out.println("Error : "+e.getMessage());
//            }
//        }
//        else if(iris.get(0).contains("RI")) {
//            irisCaptureInfo = "LI";
//            try {
//                int retStartCapture = mIDIrisEnroll.StartCapture(IrisSide.MIDIRIS_ENROLL_IRIS_SIDE_LEFT, minQuality, timeout);
//                if (retStartCapture != 0) {
//                    lblerror.setText(mIDIrisEnroll.GetErrorMessage(retStartCapture));
//
//                    return;
//                }else {
//                    System.out.println("CAPTUTRED");
//
//                }
//            }
//            catch(Exception e){
//                    System.out.println("Error : "+e.getMessage());
//            }
//        }
//        }
//        else {
//            lblerror.setText("Device is not connected, Kindly reconnect the IRIS");
//        } */
//
//
//    }
//
//
//    public class MyIcon implements Icon {
//
//        int _Width = 0;
//        int _Height = 0;
//        float widthCorrection = 0;
//        float heightCorrection = 0;
//        int deviceWidth = 0;
//        int deviceHeight = 0;
//
//        public MyIcon(int Width, int Height) {
//            this._Width = Width;
//            this._Height = Height;
//            m_Image = null;
//
//            if (deviceInfo != null) {
//                deviceWidth = deviceInfo.Width;
//                deviceHeight = deviceInfo.Height;
//                widthCorrection = ((float) _Width) / deviceWidth;
//                heightCorrection = ((float) _Height) / deviceHeight;
//            }
//        }
//
//        @Override
//        public void paintIcon(Component c, Graphics g, int x, int y) {
//            if (m_Image != null) {
//                g.drawImage(m_Image, x, y, getIconWidth(), getIconHeight(), null);
//
//                Graphics2D g2d = (Graphics2D) g;
//
//
//                int quality = 60;
//                try {
//                    quality = minQuality;
//                } catch (Exception e) {
//                    System.out.println("Error : " + e.getMessage());
//                }
//
//                if (qtyy >= quality) {
//                    g2d.setColor(new Color(0, 100, 0)); // GREEN
//                } else {
//                    g2d.setColor(new Color(144, 0, 48)); // RED
//                }
//                g2d.setStroke(new BasicStroke(2));
//
////                System.out.println("Before X: " + xx + ", Y: " + yy + ", R: " + rr);
//                int X = (int) ((deviceWidth - xx) * widthCorrection);
//                int Y = (int) (yy * heightCorrection);
//                if (Y < 0) {
//                    Y = -Y;
//                }
//                int R = (int) (rr * (heightCorrection + widthCorrection));
//
//                X = X - (R / 2);
//                Y = Y - (R / 2);
//
//                g2d.drawOval(X, Y, R, R);
//            } else {
//                g.fillRect(x, y, getIconWidth(), getIconHeight());
//            }
//        }
//
//        @Override
//        public int getIconWidth() {
//            return _Width;
//        }
//
//        @Override
//        public int getIconHeight() {
//            return _Height;
//        }
//    }
//
//    private java.awt.Image m_Image;
//    private int qtyy;
//    private int xx;
//    private int yy;
//    private int rr;
//
//    @Override
//    public void OnDeviceDetection(String DeviceName, IrisSide irisSide, DeviceDetection detection) {
////         if (detection == DeviceDetection.CONNECTED) {
////          System.out.println(DeviceName + " Connected");
////          deviceName = DeviceName;
////          System.out.println("DEvice Name:"+deviceName );
////        IrisSide[] irisSides = new IrisSide[1];
////        if(mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides)) {
////            List<String> deviceListConnected = new ArrayList<String>();
////        int retConnected = mIDIrisEnroll.GetConnectedDevices(deviceListConnected);
////        if (retConnected != 0) {
////            //lblerror.setText("Device is not connected");
////           // return;
////        }
////
////        System.out.println("connected devices :"+ deviceListConnected);
////        String model = "";
////        //Initialize Iris Device
////        if(deviceListConnected.size()>0) {
////         model = deviceListConnected.get(0);
////        }
////
////        System.out.println("DEVICE NAME:"+model);
////        DeviceInfo info = new DeviceInfo();
////
////        retInit = mIDIrisEnroll.Init(DeviceModel.valueFor(model), info, irisSides);
////        if (retInit != 0) {
////            deviceInfo = null;
////
////
////            System.out.println("Device err :" + mIDIrisEnroll.GetErrorMessage(retInit));
//////            showDeviceInfo(null);
//////            jblIrisLeftIcon.setIcon(null);
//////            jblIrisRightIcon.setIcon(null);
//////            showLogs(mIDIrisEnroll.GetErrorMessage(ret));
////            //return;
////        }else {
////
////            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
////            SaveEnrollmentDetails saveEnrollment = holder.getenrollmentDetails();
////            System.out.println("details : " + saveEnrollment.getArcNo());
////            saveEnrollment.setiRISScannerSerailNo(info.SerialNo);
////            saveEnrollment.setEnrollmentStatus("IRIS Capture Completed");
////            holder.setEnrollmentDetails(saveEnrollment);
////            System.out.println("DEVICE INITIALIZED");
////        }
////        }
////          //  jcbConnectedDevices.addItem(DeviceName);
////           //lblerror.setText(DeviceName + " Connected88");
////        }
////         else { //DETACHED
////           // jcbConnectedDevices.removeItem(DeviceName);
////            System.out.println(DeviceName + " Disconnected");
////            //showLogs(DeviceName + " Disconnected");
////
////
////        }
//
//        try {
//
//            if (detection == DeviceDetection.CONNECTED) {
//                //lblerror.setText("IRIS Device Connected");
//                LOGGER.log(Level.INFO, "IRIS deviedetection from SDK:" + DeviceDetection.CONNECTED);
//                LOGGER.log(Level.INFO, "Connected:" + DeviceName);
//                deviceName = DeviceName;
//                LOGGER.log(Level.INFO, "Device Name:" + deviceName);
//                isDeviceConnected = true;
//                //  jcbConnectedDevices.addItem(DeviceName);
//                //lblerror.setText(DeviceName + " Connected88");
//            } else { //DETACHED
//                // jcbConnectedDevices.removeItem(DeviceName);
//                //lblerror.setText("IRIS Device MalFunction, Kindly contact Device Admin");
//                LOGGER.log(Level.INFO, "IRIS deviedetection from SDK:" + DeviceDetection.CONNECTED);
//                isDeviceConnected = false;
//                isDeviceInitialized = false;
//                mIDIrisEnroll.Uninit();
//                LOGGER.log(Level.INFO, "Disconnected" + DeviceName);
//                //showLogs(DeviceName + " Disconnected");
//            }
//        } catch (Exception ex) {
//
//            ex.printStackTrace();
//            LOGGER.log(Level.SEVERE, "IRIS Device, Error while OnDeviceDetection ");
//        }
//
//    }
//
//    @Override
//    public void OnPreview(int ErrorCode, ImageQuality imageQuality, final ImagePara imagePara) {
//        try {
//            //showLogs("Capture Success");
////            jblIrisLeft.setText("Quality: " + imageQuality.LeftIrisQuality);
////            jblIrisRight.setText("Quality: " + imageQuality.RightIrisQuality);
//
//            if (imagePara.LeftImageBufferLen > 0) {
//                displayImage(imagePara.LeftImageBuffer, imageQuality.LeftIrisQuality, imageQuality.LeftIrisX, imageQuality.LeftIrisY, imageQuality.LeftIrisR, lefticon, mLeftIrisImage);
//
//            }
//
//            if (imagePara.RightImageBufferLen > 0) {
//                displayImage(imagePara.RightImageBuffer, imageQuality.RightIrisQuality, imageQuality.RightIrisX, imageQuality.RightIrisY, imageQuality.RightIrisR, righticon, mRightIrisImage);
//
//            }
//        } catch (Exception ex) {
//
//            ex.printStackTrace();
//            LOGGER.log(Level.SEVERE, "IRIS Device, Error while OnPreview ");
//        }
//        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void OnComplete(int ErrorCode, ImageQuality imageQuality, final ImagePara imagePara) {
//        try {
////            showLogs("Capture Success");
////            jblIrisLeft.setText("Quality: " + imageQuality.LeftIrisQuality);
////            jblIrisRight.setText("Quality: " + imageQuality.RightIrisQuality);
//            if (imagePara == null) {
//                System.out.println("stream empty");
//                irisCapturedLeft = false;
//                irisCapturedRight = false;
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        Image image = new Image("/haar_facedetection/redcross.png");
//                        camera.setDisable(true);
//                        captureIris.setDisable(false);
//                        showFinger.setDisable(false);
//                        statusImage.setImage(image);
//                    }
//                });
//
//                return;
//            }
//
//            if (imagePara.LeftImageBufferLen > 0) {
//                irisCapturedLeft = true;
//                displayImageComplete(imagePara.LeftImageBuffer, imageQuality.LeftIrisQuality, imageQuality.LeftIrisX, imageQuality.LeftIrisY, imageQuality.LeftIrisR, lefticon, mLeftIrisImage);
//                leftIrisImage = imagePara.LeftImageBuffer;
//            }
//
//            if (imagePara.RightImageBufferLen > 0) {
//                irisCapturedRight = true;
//                displayImageComplete(imagePara.RightImageBuffer, imageQuality.RightIrisQuality, imageQuality.RightIrisX, imageQuality.RightIrisY, imageQuality.RightIrisR, righticon, mRightIrisImage);
//                rightIrisImage = imagePara.RightImageBuffer;
//            }
//        } catch (Exception ex) {
//
//            ex.printStackTrace();
//            LOGGER.log(Level.SEVERE, "IRIS Device, Error while Oncomplete ");
//
//        }
//        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//
//    void displayImage(final byte[] buffer, final int qty, final int x, final int y, final int r, final ImageView jLabel, final MyIcon icon) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    InputStream in = new ByteArrayInputStream(buffer);
//                    BufferedImage bufferedImage = ImageIO.read(in);
//                    System.out.println("BUFFFF" + bufferedImage.getData());
////                    icon.setImage(bufferedImage, qty, x, y, r);
//
//                    WritableImage wr = null;
//                    if (bufferedImage != null) {
//                        System.out.println("BUFDREA not null");
//                        wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
//                        PixelWriter pw = wr.getPixelWriter();
//                        for (int x = 0; x < bufferedImage.getWidth(); x++) {
//                            for (int y = 0; y < bufferedImage.getHeight(); y++) {
//                                pw.setArgb(x, y, bufferedImage.getRGB(x, y));
//                            }
//                        }
//                    }
//
//                    ImageView imView = new ImageView(wr);
//                    System.out.println("IMAGEEEE" + imView.getImage().toString());
//                    jLabel.setImage(wr);
//                    // righticon.setImage(wr);
//
//                    //Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//                    //lefticon.setImage(image);
//                    //jLabel.setIcon(icon);
//                    //jLabel.repaint();
//                } catch (Exception e) {
//                    //System.out.println("Error : "+e.getMessage());
//                    LOGGER.log(Level.SEVERE, "Error: " + e.getMessage());
//                }
//            }
//        }).start();
//
//        try {
//            ImageFormat format = ImageFormat.valueOf("K7");
//            int compressionRatio = 1;
//            ImagePara imagePara = new ImagePara();
//            int ret = mIDIrisEnroll.GetImage(imagePara, compressionRatio, format);
//            if (ret != 0) {
//                //showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//            } else {
//                if (imagePara.LeftImageBufferLen > 0) {
//                    byte[] finalLeftBuffer = new byte[imagePara.LeftImageBufferLen];
//                    System.arraycopy(imagePara.LeftImageBuffer, 0, finalLeftBuffer, 0, imagePara.LeftImageBufferLen);
//                    leftIrisImage = finalLeftBuffer;
//                }
//
//                if (imagePara.RightImageBufferLen > 0) {
//                    byte[] finalRightBuffer = new byte[imagePara.RightImageBufferLen];
//                    System.arraycopy(imagePara.RightImageBuffer, 0, finalRightBuffer, 0, imagePara.RightImageBufferLen);
//                    rightIrisImage = finalRightBuffer;
//                }
//                // showLogs("Image Save");
//            }
//            ImageFormat formatIso = ImageFormat.valueOf("IIR_K7_2011");
//            int compressionRatioIso = 1;
//            ImagePara imageParaIso = new ImagePara();
//            int retIso = mIDIrisEnroll.GetImage(imageParaIso, compressionRatioIso, formatIso);
//            if (retIso != 0) {
//                //showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//            } else {
//                if (imageParaIso.LeftImageBufferLen > 0) {
//                    byte[] finalLeftBuffer = new byte[imageParaIso.LeftImageBufferLen];
//                    System.arraycopy(imageParaIso.LeftImageBuffer, 0, finalLeftBuffer, 0, imageParaIso.LeftImageBufferLen);
//                    leftIrisTemplate = finalLeftBuffer;
//
//                }
//
//                if (imageParaIso.RightImageBufferLen > 0) {
//                    byte[] finalRightBuffer = new byte[imageParaIso.RightImageBufferLen];
//                    System.arraycopy(imageParaIso.RightImageBuffer, 0, finalRightBuffer, 0, imageParaIso.RightImageBufferLen);
//                    rightIrisTemplate = finalRightBuffer;
//                }
//                // showLogs("Image Save");
//            }
//        } catch (Exception e) {
//            LOGGER.log(Level.INFO, "Exception:" + e + ":Try Again");
//        }
//    }
//
//
//    private void displayImageComplete(final byte[] buffer, final int qty, final int x, final int y, final int r, final ImageView jLabel, final MyIcon icon) {
//
//        try {
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        InputStream in = new ByteArrayInputStream(buffer);
//                        BufferedImage bufferedImage = ImageIO.read(in);
//                        System.out.println("BUFFFF" + bufferedImage.getData());
////                    icon.setImage(bufferedImage, qty, x, y, r);
//
//                        WritableImage wr = null;
//                        if (bufferedImage != null) {
//                            System.out.println("BUFDREA not null");
//                            wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
//                            PixelWriter pw = wr.getPixelWriter();
//                            for (int x = 0; x < bufferedImage.getWidth(); x++) {
//                                for (int y = 0; y < bufferedImage.getHeight(); y++) {
//                                    pw.setArgb(x, y, bufferedImage.getRGB(x, y));
//                                }
//                            }
//                        }
//
//                        ImageView imView = new ImageView(wr);
//                        System.out.println("IMAGEEEE" + imView.getImage().toString());
//                        jLabel.setImage(wr);
//
//
//                        // righticon.setImage(wr);
//
//                        //Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//                        //lefticon.setImage(image);
//                        //jLabel.setIcon(icon);
//                        //jLabel.repaint();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        LOGGER.log(Level.SEVERE, "IRIS Device, Error while Displaying image ");
//                    }
//                }
//            }).start();
//            System.out.println("iris info :" + irisCaptureInfo.contains("RI"));
//            System.out.println("iris info :" + irisCaptureInfo.contains("LI"));
//            System.out.println(" iris left : iris right : " + irisCapturedLeft + " : " + irisCapturedRight);
//
//            if (irisCaptureInfo.contains("RI") && irisCaptureInfo.contains("LI")) {
//                if (irisCapturedRight == false) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/redcross.png");
//                            camera.setDisable(true);
//                            captureIris.setDisable(false);
//                            showFinger.setDisable(false);
//                            statusImage.setImage(image);
//                        }
//                    });
//                    return;
//                } else if (irisCapturedLeft == false) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/redcross.png");
//                            camera.setDisable(true);
//                            captureIris.setDisable(false);
//                            showFinger.setDisable(false);
//                            statusImage.setImage(image);
//                        }
//                    });
//                    return;
//                } else if (irisCapturedLeft == true && irisCapturedRight == true) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/tickgreen.jpg");
//                            camera.setDisable(false);
//                            showFinger.setDisable(true);
//                            captureIris.setDisable(true);
//                            statusImage.setImage(image);
//                        }
//                    });
//                }
//
//            } else if (irisCaptureInfo.contains("RI")) {
//                if (irisCapturedRight == false) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/redcross.png");
//                            camera.setDisable(true);
//                            captureIris.setDisable(false);
//                            showFinger.setDisable(false);
//                            statusImage.setImage(image);
//                        }
//                    });
//                    return;
//                } else {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/tickgreen.jpg");
//                            camera.setDisable(false);
//                            showFinger.setDisable(true);
//                            captureIris.setDisable(true);
//                            statusImage.setImage(image);
//                        }
//                    });
//                }
//            } else if (irisCaptureInfo.contains("LI")) {
//                if (irisCapturedLeft == false) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/redcross.png");
//                            camera.setDisable(true);
//                            captureIris.setDisable(false);
//                            showFinger.setDisable(false);
//                            statusImage.setImage(image);
//                        }
//                    });
//
//                    return;
//                } else {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Image image = new Image("/haar_facedetection/tickgreen.jpg");
//                            camera.setDisable(false);
//                            showFinger.setDisable(true);
//                            captureIris.setDisable(true);
//                            statusImage.setImage(image);
//                        }
//                    });
//                }
//            }
//            ImageFormat format = ImageFormat.valueOf("K7"); //Image Format K7 for Image
//            int compressionRatio = 1;
//            ImagePara imagePara = new ImagePara();
//            int ret = mIDIrisEnroll.GetImage(imagePara, compressionRatio, format);
//            if (ret != 0) {
//                //showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//            } else {
//                if (imagePara.LeftImageBufferLen > 0) {
//                    byte[] finalLeftBuffer = new byte[imagePara.LeftImageBufferLen];
//                    System.arraycopy(imagePara.LeftImageBuffer, 0, finalLeftBuffer, 0, imagePara.LeftImageBufferLen);
//                    leftIrisImage = finalLeftBuffer;
//                /*
//                 OutputStream os2 = new FileOutputStream("/home/boss/leftIrisImage");
//                       // Starts writing the bytes in it
//                 os2.write(leftIrisImage);
//                  System.out.println("leftIrisTemplate:::"+leftIrisImage); */
//                }
//
//                if (imagePara.RightImageBufferLen > 0) {
//                    byte[] finalRightBuffer = new byte[imagePara.RightImageBufferLen];
//                    System.arraycopy(imagePara.RightImageBuffer, 0, finalRightBuffer, 0, imagePara.RightImageBufferLen);
//                    rightIrisImage = finalRightBuffer;
//                /*OutputStream os3 = new FileOutputStream("/home/boss/rightIrisImage");
//                       // Starts writing the bytes in it
//                 os3.write(rightIrisImage);
//                  System.out.println("leftIrisTemplate:::"+rightIrisImage);*/
//                }
//                // showLogs("Image Save");
//            }
//            ImageFormat formatIso = ImageFormat.valueOf("IIR_K7_2011");//Image Format IIR_K7_2011 for Image Template
//            int compressionRatioIso = 1;
//            ImagePara imageParaIso = new ImagePara();
//            int retIso = mIDIrisEnroll.GetImage(imageParaIso, compressionRatioIso, formatIso);
//            if (retIso != 0) {
//                //showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//            } else {
//                if (imageParaIso.LeftImageBufferLen > 0) {
//                    byte[] finalLeftBuffer = new byte[imageParaIso.LeftImageBufferLen];
//                    System.arraycopy(imageParaIso.LeftImageBuffer, 0, finalLeftBuffer, 0, imageParaIso.LeftImageBufferLen);
//                    leftIrisTemplate = finalLeftBuffer;
//                /*OutputStream os = new FileOutputStream("/home/boss/irisTemplateleft");
//                       // Starts writing the bytes in it
//                 os.write(leftIrisTemplate);
//                 System.out.println("leftIrisTemplate:::"+leftIrisTemplate);*/
//                }
//
//                if (imageParaIso.RightImageBufferLen > 0) {
//                    byte[] finalRightBuffer = new byte[imageParaIso.RightImageBufferLen];
//                    System.arraycopy(imageParaIso.RightImageBuffer, 0, finalRightBuffer, 0, imageParaIso.RightImageBufferLen);
//                    rightIrisTemplate = finalRightBuffer;
//                /*OutputStream os1 = new FileOutputStream("/home/boss/irisTemplateright");
//                       // Starts writing the bytes in it
//                 os1.write(rightIrisTemplate);
//                 System.out.println("rightIrisTemplate::::"+rightIrisTemplate);*/
//                }
//                // showLogs("Image Save");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            LOGGER.log(Level.SEVERE, "Error: " + e.getMessage());
//        }
//    }
//
//
//    private void jbtnSaveImageActionPerformed(java.awt.event.ActionEvent evt) {
//        if (deviceInfo == null) {
//            //showLogs("Device is not initialized.");
//            return;
//        }
//        ImageFormat format = ImageFormat.valueOf("BMP");
//        int compressionRatio = 1;
//        ImagePara imagePara = new ImagePara();
//        int ret = mIDIrisEnroll.GetImage(imagePara, compressionRatio, format);
//        if (ret != 0) {
//            // showLogs(mIDIrisEnroll.GetErrorMessage(ret));
//        } else {
//            if (imagePara.LeftImageBufferLen > 0) {
//                byte[] finalLeftBuffer = new byte[imagePara.LeftImageBufferLen];
//                System.arraycopy(imagePara.LeftImageBuffer, 0, finalLeftBuffer, 0, imagePara.LeftImageBufferLen);
//
////                switch (format) {
////                    case RAW:
////                        WriteImageFile("Left_Raw.raw", finalLeftBuffer);
////                        break;
////                    case BMP:
////                        WriteImageFile("Left_Bitmap.bmp", finalLeftBuffer);
////                        break;
////                    case JPEG2000:
////                        WriteImageFile("Left_JPEG2000.jp2", finalLeftBuffer);
////                        break;
////                    case K7:
////                        WriteImageFile("Left_K7.bmp", finalLeftBuffer);
////                        break;
////                    case IIR_K7_2011:
////                        WriteImageFile("Left_k7_IIR.iso", finalLeftBuffer);
////                        break;
////                }
//                String strIrisLeft = Base64.getEncoder().encodeToString(finalLeftBuffer);
//                //WriteImageFile("Left_k7_IIR.iso",strIrisLeft.getBytes());
//            }
//
//            if (imagePara.RightImageBufferLen > 0) {
//                byte[] finalRightBuffer = new byte[imagePara.RightImageBufferLen];
//                System.arraycopy(imagePara.RightImageBuffer, 0, finalRightBuffer, 0, imagePara.RightImageBufferLen);
//
////                switch (format) {
////                    case RAW:
////                        WriteImageFile("Right_Raw.raw", finalRightBuffer);
////                        break;
////                    case BMP:
////                        WriteImageFile("Right_Bitmap.bmp", finalRightBuffer);
////                        break;
////                    case JPEG2000:
////                        WriteImageFile("Right_JPEG2000.jp2", finalRightBuffer);
////                        break;
////                    case K7:
////                        WriteImageFile("Right_K7.bmp", finalRightBuffer);
////                        break;
////                    case IIR_K7_2011:
////                        WriteImageFile("Right_k7_IIR.iso", finalRightBuffer);
////                        break;
////                }
//                String strIrisRight = Base64.getEncoder().encodeToString(finalRightBuffer);
//            }
//            //showLogs("Image Save");
//        }
//    }
//
//    /*
//    private void WriteImageFile(String filename, byte[] bytes) {
//
//        try {
//            String path = System.getProperty("user.dir") + "//IrisData";
//            System.out.println("user dir :"+path);
//            File file = new File(path);
//
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//            path = path + "//" + filename;
//            file = new File(path);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileOutputStream stream = new FileOutputStream(path);
//            stream.write(bytes);
//            stream.close();
//
//
//        } catch (Exception e1) {
//            System.out.println("Error : "+e1.getMessage());
//        }
//    }
//*/
//    @FXML
//    private void switchToSlap() {
//        //App.setRoot("secondary");
//        showFinger.setDisable(true);
//        captureIris.setDisable(true);
//        camera.setDisable(true);
//        confirmPane.setVisible(true);
//        System.out.println("Confirm Pane Call::");
//    }
//
//    @FXML
//    public void cameraCapture() {
//        //App.setRoot("secondary");
//
//
//        //commented on 26-05-22 and moved to setIris
//
//        if (rightIrisImage == null && leftIrisImage != null) {
//            System.out.println("In Left Iris Image" + leftIrisImage);
//            IRIS irisLeft = new IRIS();
//            irisLeft.setPosition("LI");
//            irisLeft.setImage(Base64.getEncoder().encodeToString(leftIrisImage));
//            irisLeft.setTemplate(Base64.getEncoder().encodeToString(leftIrisTemplate));
//            irisSet.add(irisLeft);
//            System.out.println("In Camera Capture1");
//        } else if (leftIrisImage == null && rightIrisImage != null) {
//
//            System.out.println("In Left Iris Image" + rightIrisImage);
//            IRIS irisRight = new IRIS();
//            irisRight.setPosition("RI");
//            irisRight.setImage(Base64.getEncoder().encodeToString(rightIrisImage));
//            irisRight.setTemplate(Base64.getEncoder().encodeToString(rightIrisTemplate));
//            irisSet.add(irisRight);
//        } else if (rightIrisImage != null && leftIrisImage != null) {
//            IRIS irisLeft = new IRIS();
//            irisLeft.setPosition("LI");
//            irisLeft.setImage(Base64.getEncoder().encodeToString(leftIrisImage));
//            irisLeft.setTemplate(Base64.getEncoder().encodeToString(leftIrisTemplate));
//            irisSet.add(irisLeft);
//
//            IRIS irisRight = new IRIS();
//            irisRight.setPosition("RI");
//            irisRight.setImage(Base64.getEncoder().encodeToString(rightIrisImage));
//            irisRight.setTemplate(Base64.getEncoder().encodeToString(rightIrisTemplate));
//            irisSet.add(irisRight);
//        }
//
//
//        /*
//        String irisimageleft = Base64.getEncoder().encodeToString(leftIrisImage);
//        String irisimageright = Base64.getEncoder().encodeToString(rightIrisImage);
//        String iristemplateleft = Base64.getEncoder().encodeToString(leftIrisTemplate);
//        String iristemplateright = Base64.getEncoder().encodeToString(rightIrisTemplate);
//        String reponse = setIris(irisimageleft,irisimageright,iristemplateleft,iristemplateright);
//        String reponse = setIris(); */
//        System.out.println("show Iris ::");
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//        ARCDetails a = holder.getArcDetails();
//        SaveEnrollmentDetails saveEnrollment = holder.getSaveEnrollmentDetails();
//        System.out.println("details : " + saveEnrollment.getArcNo());
//        saveEnrollment.setIris(irisSet);
//        saveEnrollment.setEnrollmentStatus("IrisCompleted");
//        System.out.println("details : Iris capture completeed");
//        holder.setSaveEnrollmentDetails(saveEnrollment);
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.setBase64Variant(Base64Variants.MIME_NO_LINEFEEDS);
//        //System.out.println("Reached here1");
//
//        String postJson;
//        try {
//            postJson = mapper.writeValueAsString(saveEnrollment);
//            //Code Added by K. Karthikeyan - 18-4-22 - Start
//            ObjectReaderWriter objReadWrite = new ObjectReaderWriter();
//            objReadWrite.writer(saveEnrollment);
//            System.out.println("Save Enrollment Object write");
//            SaveEnrollmentDetails s = objReadWrite.reader();
//            System.out.println("Enrollment Status " + s.getEnrollmentStatus());
//            //Code Added by K. Karthikeyan - 18-4-22 - Finish
//
//            //     System.out.println("post json iris :"+ postJson);
//        } catch (JsonProcessingException ex) {
//            Logger.getLogger(SlapScannerController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//         /*
//        //System.out.println("Reached here2");
//        mIDIrisEnroll.Uninit();
//        //System.out.println("Reached here3");
//
//        try {
//            App.setRoot("camera");
//        } catch (IOException ex) {
//            //Logger.getLogger(IrisController.class.getName()).log(Level.SEVERE, null, ex);
//             LOGGER.log(Level.SEVERE, "Error: "+ ex.getMessage());
//        }*/
//
//        // Added For Biometric Options
//        if (holder.getArcDetails().getBiometricOptions().contains("Biometric")) {
//            try {
//                mIDIrisEnroll.Uninit();
//                App.setRoot("capturecomplete");
//            } catch (IOException ex) {
//                //Logger.getLogger(ARCNoController.class.getName()).log(Level.SEVERE, null, ex);
//                LOGGER.log(Level.INFO, "IOException At Get Biometric Options:" + ex);
//            }
//        } else {
//            try {
//                mIDIrisEnroll.Uninit();
//                App.setRoot("camera");
//            } catch (IOException ex) {
//                //Logger.getLogger(ARCNoController.class.getName()).log(Level.SEVERE, null, ex);
//                LOGGER.log(Level.INFO, "IOException At Get Biometric Options:" + ex);
//            }
//        }
//        // Added For Biometric Options
//    }
//
//    /*
//    //public String setIris(String irisimageleft,String irisimageright,String iristemplateleft,String iristemplateright){
//    public String setIris(){
//
//        String reponse = "saved";
//        if(rightIrisImage == null){
//        System.out.println("In Left Iris Image"+leftIrisImage);
//        IRIS irisLeft = new IRIS();
//        irisLeft.setPosition("LI");
//        irisLeft.setImage(Base64.getEncoder().encodeToString(leftIrisImage));
//        irisLeft.setTemplate(Base64.getEncoder().encodeToString(leftIrisTemplate));
//        //irisLeft.setImage(irisimageleft);
//        //irisLeft.setTemplate(iristemplateleft);
//        irisSet.add(irisLeft);
//        System.out.println("In Camera Capture1");
//        }else if(leftIrisImage == null){
//
//        System.out.println("In Left Iris Image"+rightIrisImage);
//        IRIS irisRight = new IRIS();
//        irisRight.setPosition("RI");
//        irisRight.setImage(Base64.getEncoder().encodeToString(rightIrisImage));
//        irisRight.setTemplate(Base64.getEncoder().encodeToString(rightIrisTemplate));
//        //irisRight.setImage(irisimageright);
//        //irisRight.setTemplate(iristemplateright);
//        irisSet.add(irisRight);
//       }else{
//        IRIS irisLeft = new IRIS();
//        irisLeft.setPosition("LI");
//        irisLeft.setImage(Base64.getEncoder().encodeToString(leftIrisImage));
//        irisLeft.setTemplate(Base64.getEncoder().encodeToString(leftIrisTemplate));
//        //irisLeft.setImage(irisimageleft);
//        //irisLeft.setTemplate(iristemplateleft);
//        irisSet.add(irisLeft);
//
//        IRIS irisRight = new IRIS();
//        irisRight.setPosition("RI");
//        irisRight.setImage(Base64.getEncoder().encodeToString(rightIrisImage));
//        irisRight.setTemplate(Base64.getEncoder().encodeToString(rightIrisTemplate));
//        //irisRight.setImage(irisimageright);
//        //irisRight.setTemplate(iristemplateright);
//        irisSet.add(irisRight);
//        }
//        return reponse;
//    }*/
//
//    @FXML
//    private void goBack() {
//        System.out.println("inside go back");
//        //backBtn.setDisable(false);
//        //scan.setDisable(false);
//        //statusField.setText("");
//
//        try {
//
//            Thread.sleep(1);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(IrisController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        mIDIrisEnroll.Uninit();
//        try {
//            App.setRoot("slapscanner_1");
//        } catch (IOException ex) {
//            //Logger.getLogger(IrisController.class.getName()).log(Level.SEVERE, null, ex);
//            LOGGER.log(Level.SEVERE, "Error: " + ex.getMessage());
//        }
//
//    }
//
//    @FXML
//    private void stayBack() {
//        System.out.println("inside stay back");
//        //backBtn.setDisable(false);
//        confirmPane.setVisible(false);
//
//        captureIris.setDisable(false);
//        showFinger.setDisable(false);
//
//    }
//
//    /*
//    @FXML
//    private void stayBack1() throws IOException {
//        System.out.println("inside stay back1");
//        //backBtn.setDisable(false);
//
//        confirmPane1.setVisible(false);
//
//        captureIris.setDisable(false);
//        showFinger.setDisable(false);
//
//    }*/
//    /*
//    @FXML
//    private void goFront() throws IOException {
//        System.out.println("inside Go Front");
//         App.setRoot("camera");
//        //backBtn.setDisable(false);
//        //confirmPane1.setVisible(false);
//
//        //captureIris.setDisable(false);
//        //showFinger.setDisable(false);
//
//    }*/
//
//}
