///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cdac.enrollmentstation.controller;
//
//
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.util.TestProp;
//import com.cdac.enrollmentstation.api.APIServerCheck;
//import com.cdac.enrollmentstation.logging.ApplicationLog;
//import com.cdac.enrollmentstation.model.ARCDetails;
//import com.cdac.enrollmentstation.model.ARCDetailsHolder;
//import com.cdac.enrollmentstation.model.SaveEnrollmentDetails;
//import com.cdac.enrollmentstation.service.ObjectReaderWriter;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.event.EventHandler;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Hyperlink;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.input.TouchEvent;
//
//import javax.crypto.SecretKey;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.util.HashSet;
//import java.util.ResourceBundle;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * FXML Controller class
// *
// * @author boss
// */
//public class ARCNoController_withoutbiometric implements Initializable {
//
//    public ARCDetails arcDetails;
//
//    public APIServerCheck apiServerCheck = new APIServerCheck();
//
//    private SecretKey skey;
//
//    TestProp prop = new TestProp();
//
//    @FXML
//    private TextField txtARCno, txtARCBarcode;
//
//    @FXML
//    private Label lblStatus;
//
//    @FXML
//    private TextField ArcNo, txtName, txtRank, txtapp, txtUnit, txtFinger, txtiris, txtarcstatus, txtarcno, txtbiometricoptions;
//
//    @FXML
//    Hyperlink txtDlink;
//
//    @FXML
//    Button show_arcdetails_next1;
//
//    @FXML
//    private Button inputarcbutton, barcodearcbutton;
//
//
//    //For Application Log
//    ApplicationLog appLog = new ApplicationLog();
//    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
//    Handler handler;
//
//
//    @FXML
//    private void showFingerPrint() {
//
//        //Added For Only Photo
//
//
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//      /*
//      if(holder.getARC().getBiometricoptions().contains("Photo")){
//                LOGGER.log(Level.INFO, "Going to Camera Page");
//                try {
//                     App.setRoot("camera");
//                    } catch (IOException ex) {
//                      //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                       LOGGER.log(Level.INFO, "IOException At Get Biometric Options:"+ex);
//                    }
//      }else{  *///Added For Only Photo
//        //App.setRoot("slapscanner");
//        //Code added by K. Karthikeyan - Start [for app crash and resume from previous data]
//        String saveenrollment = null;
//        saveenrollment = prop.getProp().getProperty("saveenrollment");
//        if (saveenrollment.isBlank() || saveenrollment.isEmpty() || saveenrollment == null) {
//            //System.out.println("The property 'saveenrollment' is empty, Please add it in file properties");
//            LOGGER.log(Level.INFO, "The property 'saveenrollment' is empty, Please add it in file properties");
//            return;
//        }
//        //String objFilePath = "/usr/share/enrollment/save/saveEnrollment.txt";
//        File saveenrollmentfile = new File(saveenrollment);
//        if (saveenrollmentfile.exists() && !(saveenrollmentfile.length() == 0)) {
//            //if(saveenrollmentfile.exists()){
//            //To-do
//            ObjectReaderWriter objReadWrite = new ObjectReaderWriter();
//            SaveEnrollmentDetails s = objReadWrite.reader();
//            String prevStatus = s.getEnrollmentStatus();
//            LOGGER.log(Level.INFO, "Enrollment Status :{0}", prevStatus);
//            //System.out.println("Enrollment Status :"+prevStatus);
//            //System.out.println("ARC No Entered : "+txtARCno.getText());
//            //System.out.println("ARC No Entered : "+txtARCBarcode.getText());
//            //System.out.println("Previous ARC No : "+s.getArcNo());
//            LOGGER.log(Level.INFO, "Previous ARC No :{0}", s.getArcNo());
//
//            if (s.getArcNo() != null) {
//
//                //System.out.println("S Get ARC NO ::"+s.getArcNo());
//                //System.out.println("Txt ARC ::"+txtarcno.getText());
//
//                //if(s.getArcNo().equals(txtARCno.getText())){
//                // if(s.getArcNo().equals(txtARCBarcode.getText())||s.getArcNo().equals(txtARCno.getText())){
//                if (s.getArcNo().equals(txtarcno.getText())) {
//                    //if(s.getArcNo().equals(txtarcno.getText())){
//
//                    //System.out.println("Both ARC same and proceeeding");
//                    LOGGER.log(Level.INFO, "Both ARC same and proceeeding");
//                    //Commented For Only Photo
//                    //ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                    holder.setSaveEnrollmentDetails(s);
//
//                    switch (prevStatus) {
//                        case "FingerPrintCompleted":
//                            //System.out.println("Going to Iris Capture");
//                            LOGGER.log(Level.INFO, "Going to Iris Capture");
//                        {
//                            try {
//                                App.setRoot("iris");
//                            } catch (IOException ex) {
//                                Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                                LOGGER.log(Level.INFO, "IOException:" + ex);
//                            }
//                        }
//                        break;
//
//
//                        case "IrisCompleted":
//                            //System.out.println("Going to Camera Capture");
//                            LOGGER.log(Level.INFO, "Going to Camera Capture");
//                        {
//                            try {
//                                App.setRoot("camera");
//                            } catch (IOException ex) {
//                                //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                                LOGGER.log(Level.INFO, "IOException:" + ex);
//                            }
//                        }
//                        break;
//
//
//                        case "PhotoCompleted":
//                            //System.out.println("Going to Submit Page");
//                            LOGGER.log(Level.INFO, "Going to Submit Page");
//                        {
//                            try {
//                                App.setRoot("capturecomplete");
//                            } catch (IOException ex) {
//                                //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                                LOGGER.log(Level.INFO, "IOException:" + ex);
//                            }
//                        }
//                        break;
//
//
//                        case "SUCCESS":
//                            //System.out.println("Going for Finger print scan");
//                            LOGGER.log(Level.INFO, "Going for Capture Complete");
//                        {
//                            try {
//                                App.setRoot("capturecomplete");
//                            } catch (IOException ex) {
//                                //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                                LOGGER.log(Level.INFO, "IOException:" + ex);
//                            }
//                        }
//                        break;
//
//
//                        default:
//                            //System.out.println("Going for Finger print scan");
//                            LOGGER.log(Level.INFO, "Going for Finger print scan");
//                        {
//                            try {
//                            /*
//                            RealScan.TestSlapScanner testSlap = new RealScan.TestSlapScanner();
//                            if(testSlap.sdkSlapScannerStatus().equals("true")){
//                                System.out.println("Inside Test Slap");
//                                App.setRoot("slapscanner_1");
//                            }else{
//                                System.out.println("Inside Else Test Slap");
//                                App.setRoot("slapscanner_midFinger");
//                            }*/
//                                App.setRoot("slapscanner_1");
//                            } catch (IOException ex) {
//                                //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                                LOGGER.log(Level.INFO, "IOException:" + ex);
//                            }
//                        }
//                        break;
//
//
//                    }
//
//                } else {
//                    //System.out.println("Previous file not there, Going for Finger print scan");
//                    LOGGER.log(Level.INFO, "Previous file not there, Going for Finger print scan");
//                    try {
//                             /*
//                              RealScan.TestSlapScanner testSlap = new RealScan.TestSlapScanner();
//                            if(testSlap.sdkSlapScannerStatus().equals("true")){
//                                System.out.println("Inside Test Slap");
//                                App.setRoot("slapscanner_1");
//                            }else{
//                                System.out.println("Inside Else Test Slap");
//                                App.setRoot("slapscanner_midFinger");
//                            }*/
//                        App.setRoot("slapscanner_1");
//                    } catch (IOException ex) {
//                        //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                        LOGGER.log(Level.INFO, "IOException:" + ex);
//                    }
//                }
//            } else {
//                //System.out.println("Previous Value is null, Going for Finger print scan");
//                LOGGER.log(Level.INFO, "Previous Value is null, Going for Finger print scan");
//                try {       /*
//                            RealScan.TestSlapScanner testSlap = new RealScan.TestSlapScanner();
//                            if(testSlap.sdkSlapScannerStatus().equals("true")){
//                                System.out.println("Inside Test Slap");
//                                App.setRoot("slapscanner_1");
//                            }else{
//                                System.out.println("Inside Else Test Slap");
//                                App.setRoot("slapscanner_midFinger");
//                            }*/
//                    App.setRoot("slapscanner_1");
//                } catch (IOException ex) {
//                    //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                    LOGGER.log(Level.INFO, "IOException:" + ex);
//                }
//            }
//
//        } else {
//            LOGGER.log(Level.INFO, "File is empty, Going for Finger print scan");
//            try {
//                          /*
//                             RealScan.TestSlapScanner testSlap = new RealScan.TestSlapScanner();
//                            if(testSlap.sdkSlapScannerStatus().equals("true")){
//                                System.out.println("Inside Test Slap");
//                                App.setRoot("slapscanner_1");
//                            }else{
//                                System.out.println("Inside Else Test Slap");
//                                App.setRoot("slapscanner_midFinger");
//                            }*/
//                App.setRoot("slapscanner_1");
//            } catch (IOException ex) {
//                //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//                LOGGER.log(Level.INFO, "IOException:" + ex);
//            }
//        }
//
//        //Code added by K. Karthikeyan - finish [for app crash and resume from previous data]
//        //   }//Added For Only Photo */
//    }
//
//    @FXML
//    private void showDlink() {
//        try {
//            App.setRoot("detaillink");
//        } catch (IOException ex) {
//            //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//            LOGGER.log(Level.INFO, "IOException:" + ex);
//        }
//    }
//
//
//    @FXML
//    private void showEnrollmentHome() {
//        try {
//            App.setRoot("second_screen");
//        } catch (IOException ex) {
//            //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//            LOGGER.log(Level.INFO, "IOException:" + ex);
//        }
//
//    }
//
//    @FXML
//    private void showHome() {
//        try {
//            App.setRoot("first_screen");
//        } catch (IOException ex) {
//            //Logger.getLogger(ARCNoController_withoutbiometric.class.getName()).log(Level.SEVERE, null, ex);
//            LOGGER.log(Level.INFO, "IOException:" + ex);
//        }
//
//    }
//
//    @FXML
//    public void showARCDetails() {
//
//        try {
//
//            String ArcDetailsList = "";
//            String arcNo = txtARCno.getText(); //need to uncomment
//            //System.out.println("ARCNO:::"+arcNo);
//            LOGGER.log(Level.INFO, "ARCNO:::{0}", arcNo);
//            //ARCDetails arcDetails = new ARCDetails();
//            String connurl = "";
//            String connectionStatus = "";
//
//            try {
//                if (arcNo.equals("") || arcNo.trim().equals("")) {
//                    //System.out.println("String is null, empty or blank");
//                    LOGGER.log(Level.INFO, "String is null, empty or blank");
//                    lblStatus.setText("Kindly Enter ARC Number Details");
//                    return;
//                }
//
//                connurl = apiServerCheck.getARCURL();
//                connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);
//                System.out.println("Connection Statux::::" + connectionStatus);
//                if (!connectionStatus.contentEquals("connected")) {
//                    lblStatus.setText("Network Connection Timed out From Server,Kindly try again");
//                    LOGGER.log(Level.INFO, "Fetch Arc Details Connection Status:" + connectionStatus);
//                    return;
//                }
//
//
//                ArcDetailsList = fetchArcDetails(arcNo);
//                //Object obj = JsonReader.jsonToJava(ArcDetailsList);
//                //System.out.println("obj str : " +obj.toString());
//                //System.out.println("ARCdetail"+ArcDetailsList);
//                LOGGER.log(Level.INFO, "ARCdetail:::{0}", ArcDetailsList);
//                ObjectMapper objMapper = new ObjectMapper();
//                arcDetails = objMapper.readValue(ArcDetailsList, ARCDetails.class);
//                //System.out.println(arcDetails.toString());
//                //System.out.println("ARC Details Desc:"+arcDetails.getDesc());
//                LOGGER.log(Level.INFO, "ARC Details Desc:{0}", arcDetails.getDesc());
//                if (arcDetails.getErrorCode().equals("0")) {
//                    lblStatus.setText("ARC Details Fetched Successfully");
//                    LOGGER.log(Level.INFO, "ARC Details Fetched Successfully");
//                } else {
//                    lblStatus.setText(arcDetails.getDesc());
//                    LOGGER.log(Level.INFO, "ARC Details Desc :" + arcDetails.getDesc());
//                }
//
//                if (Integer.parseInt(arcDetails.getErrorCode()) == 0) {
//                    try {
//                        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                        holder.setArcDetails(arcDetails);
//                        txtName.setText(arcDetails.getName());
//                        txtRank.setText(arcDetails.getRank());
//                        txtapp.setText(arcDetails.getApplicantID());
//                        txtUnit.setText(arcDetails.getUnit());
//                        //System.out.println("Fingers List:::"+arcDetails.getFingers().toString().substring(1,arcDetails.getFingers().toString().length()-1));
//                        System.out.println("Iris List:::" + arcDetails.getIris().toString());
//                        if (arcDetails.getFingers().size() > 0) {
//                            txtFinger.setText(arcDetails.getFingers().toString().substring(1, arcDetails.getFingers().toString().length() - 1));
//                            System.out.println("greater than zero");
//
//                        } else {
//                            txtFinger.setText("NA");
//                            System.out.println("less than zero");
//                        }
//                        //txtFinger.setText(arcDetails.getFingers().toString().substring(1,arcDetails.getFingers().toString().length()-1));
//                        //txtFinger.setText(arcDetails.getFingers().toString());
//                        //txtiris.setText(arcDetails.getIris().toString());
//                        if (arcDetails.getIris().size() > 0) {
//                            txtiris.setText(arcDetails.getIris().toString().substring(1, arcDetails.getIris().toString().length() - 1));
//                            System.out.println("greater than zero");
//
//                        } else {
//                            txtiris.setText("NA");
//                            System.out.println("less than zero");
//                        }
//                        //txtiris.setText(arcDetails.getIris().toString().substring(1,arcDetails.getIris().toString().length()-1));
//
//                        txtDlink.setText(arcDetails.getDetailLink());
//                        txtarcstatus.setText(arcDetails.getArcStatus());
//                        txtarcno.setText(arcDetails.getArcNo());
//                        SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
//                        //saveEnrollment.setArcNo(txtARCno.getText());
//                        saveEnrollment.setArcNo(arcDetails.getArcNo());
//                        saveEnrollment.setEnrollmentStationID(apiServerCheck.getStationID());
//                        saveEnrollment.setEnrollmentStationUnitID(apiServerCheck.getUnitID());
//                        saveEnrollment.setFp(new HashSet<>());
//                        saveEnrollment.setIris(new HashSet<>());
//                        //Added For Biometric Options
//                             /*
//                            LOGGER.log(Level.INFO, "ARC BIOmetric Options::"+arcDetails.getBiometricoptions());
//                            if(arcDetails.getBiometricoptions() == null || arcDetails.getBiometricoptions().isEmpty() || arcDetails.getBiometricoptions().contains("None") || arcDetails.getBiometricoptions().contains("none")){
//                                lblStatus.setText("Biometric capturing not required for given ARC Number");
//                                return;
//                            }
//                            txtbiometricoptions.setText(arcDetails.getBiometricoptions());
//                            saveEnrollment.setBiometricoptions(arcDetails.getBiometricoptions());
//                             */
//                        //Added For Biometric Options
//                        saveEnrollment.setEnrollmentStatus("ARC Details Fetched");
//                        holder.setSaveEnrollmentDetails(saveEnrollment);
//
//                        show_arcdetails_next1.setDisable(false);
//                        //Added For Biometric Options
//
//                        //   App.setRoot("show_arcdetails");
//                    } catch (NullPointerException e) {
//                        //System.out.println("Null Pointer Exception");
//                        LOGGER.log(Level.INFO, "showARCDetails- Null Pointer Exception");
//
//                    }
//                } else {
//                    try {
//                        //lblStatus.setText("Error in retriving details. Please try again");
//                        emtyTextBox();
//                        show_arcdetails_next1.setDisable(true);
//                        lblStatus.setText(arcDetails.getDesc());
//
//                    } catch (NullPointerException e) {
//                        //System.out.println("Null Pointer Exception");
//                        LOGGER.log(Level.INFO, "showARCDetails- Null Pointer Exception");
//                    }
//                }
//
//            } catch (NullPointerException e) {
//                lblStatus.setText("Results Not Fetched From Server");
//                LOGGER.log(Level.INFO, "showARCDetails- Null Pointer Exception");
//
//            } catch (JsonProcessingException ex) {
//
//                lblStatus.setText("Invalid Data Received From the Server");
//                LOGGER.log(Level.INFO, "Invalid Data Received From the Server");
//
//            }
//
//
//        } catch (NullPointerException e) {
//            lblStatus.setText("Results Not Fetched From Server");
//            LOGGER.log(Level.INFO, "Results Not Fetched From Server");
//            //System.out.println("Null pointer Exception at showARCDetails");
//
//        }
//
//    }
//
//
//    public String fetchArcDetails(String arcNo) {
//
//        String ArcDetailsList = "";
//        String connurl = "";
//        //String connectionStatus="";
//        try {
//
//            lblStatus.setText("");
//            connurl = apiServerCheck.getARCURL();
//            //String ArcDetailsList = "";
//            ArcDetailsList = apiServerCheck.getARCNoAPI(connurl, arcNo);
//
//        } catch (Exception e) {
//            lblStatus.setText("Results Not Fetched From Server");
//            //System.out.println("Null pointer Exception at fetchArcDetails");
//            LOGGER.log(Level.INFO, "Null pointer Exception at fetchArcDetails");
//        }
//
//        return ArcDetailsList;
//
//    }
//
//
//   /*
//    public String fetchArcDetails(String arcNo){
//
//        String ArcDetailsList="";
//        String connurl ="";
//        String connectionStatus="";
//             try{
//
//                if(arcNo.equals("") || arcNo.trim().equals("")){
//                    System.out.println("String is null, empty or blank");
//                    lblStatus.setText("Kindly Enter ARC Number Details");
//                }
//                else{
//                try{
//                lblStatus.setText("");
//                connurl = apiServerCheck.getARCURL();
//                connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);
//                }catch(NullPointerException e){
//                    lblStatus.setText(connectionStatus);
//                }
//                System.out.println("connection status :"+connectionStatus);
//                if(!connectionStatus.contentEquals("connected")) {
//                    lblStatus.setText(connectionStatus);
//                      /*
//
//                    File csvFile = new File("/etc/SampleDataToExportForBiometricCapture_v1.2.csv");
//                if(csvFile.exists()) {
//                    arcNo = txtARCno.getText();
//                    try (BufferedReader file = new BufferedReader(new FileReader("/etc/SampleDataToExportForBiometricCapture_v1.2.csv"))) {
//                    String line = " ";
//                    String input = " ";
//
//                    if(txtARCno.getText().isEmpty()){
//                        lblStatus.setText("Please input ARCNo/scan barcode and try again");
//                        return;
//                    }
//                    else{
//                        while((line = file.readLine()) != null){
//                            String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
//                            String Name = (tokens[11]);
//                            String Unit = (tokens[17]);
//                            String Rank = (tokens[18]);
//                            String[] Fingersleft = (tokens[52]).split(",");
//                            String[] Fingersright = (tokens[53]).replaceAll("^\"|\"$", "").split(",");
//                            String[] arrIris = (tokens[54].split(","));
//                            List<String> arr = new ArrayList<>();
//                            Collections.addAll(arr, Fingersleft);
//                            Collections.addAll(arr, Fingersright);
//                            System.out.println(arr);
//
//                            List<String> arrIr = new ArrayList<>();
//                            Collections.addAll(arrIr, arrIris);
//                            System.out.println("Missing iris ::"+arrIr);
//
//
//                            String ARCNO = (tokens[6]);
//                            String ApplicationID = (tokens[10]);
//
//                            String ARCStatus = (tokens[8]);
//
//                            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                            arcDetails = new ARCDetails();
//
//                            //  setdata --pass to next controller for creating the object of ARC details class.
//                            //  get the value to next controller for creating same object.
//                            arcDetails.setName(tokens[11]);
//                            arcDetails.setUnit(tokens[17]);
//                            arcDetails.setRank(tokens[18]);
//                            arcDetails.setFingers(arr);
//                            arcDetails.setArcNo(ARCNO);
//                            arcDetails.setApplicantID(ApplicationID);
//                            arcDetails.setIris(arrIr);
//                            arcDetails.setArcstatus(ARCStatus);
//                            holder.setARC(arcDetails);
//                            txtName.setText(arcDetails.getName());
//                            txtRank.setText(arcDetails.getRank());
//                            txtapp.setText(arcDetails.getApplicantID());
//                            txtUnit.setText(arcDetails.getUnit());
//                            txtFinger.setText(arcDetails.getFingers().toString());
//                            txtiris.setText(arcDetails.getIris().toString());
//                            txtDlink.setText(arcDetails.getDetailLink());
//                            txtarcstatus.setText(arcDetails.getArcstatus());
//                            txtarcno.setText(arcDetails.getArcNo());
//                            SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
//                            saveEnrollment.setArcNo(txtARCBarcode.getText());
//                            saveEnrollment.setEnrollmentStationID(apiServerCheck.getStationID());
//                            saveEnrollment.setEnrollmentStationUnitID(apiServerCheck.getUnitID());
//                            saveEnrollment.setFp(new HashSet<>());
//                            saveEnrollment.setIris(new HashSet<>());
//                            saveEnrollment.setEnrollmentStatus("ARC Details Fetched");
//                            holder.setEnrollmentDetails(saveEnrollment);
//                             show_arcdetails_next1.setDisable(false);
//                           // LOGGER.log(Level.INFO, "ARCDetails added in Save Enrollment");
//                        }
//                     }}}*/
//
// /*               }
//                        else {
//
//                         try{
//                          //String ArcDetailsList = "";
//                          ArcDetailsList = apiServerCheck.getARCNoAPI(connurl, arcNo);
//                         }catch(NullPointerException e){
//                             lblStatus.setText("Results Not Fetched From Server");
//                         }
//
//
//                   }
//
//                 }
//
//            }   catch(NullPointerException e){
//                          lblStatus.setText("Results Not Fetched From Server");
//                   }
//
//
//             return ArcDetailsList;
//    }*/
//
//
//    @FXML
//    public void barcodescan() {
//
//        try {
//
//            txtARCBarcode.requestFocus();
//
//            ChangeListener<Scene> sceneListener = new ChangeListener<Scene>() {
//                @Override
//                public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
//                    if (newValue != null) {
//
//                        txtARCBarcode.requestFocus();
//                        txtARCBarcode.sceneProperty().removeListener(this);
//                    }
//                }
//            };
//            txtARCBarcode.sceneProperty().addListener(sceneListener);
//
//            txtARCBarcode.setOnTouchPressed(new EventHandler<TouchEvent>() {
//                @Override
//                public void handle(TouchEvent event) {
//
//                    event.consume();
//                }
//            });
//
//        /*
//        txtARCBarcode.textProperty().addListener(new ChangeListener<String>() {
//            public void changed(ObservableValue<? extends String> observable,
//            String oldValue, String newValue) {
//                LOGGER.log(Level.INFO, "BARCODE Listener111");
//                txtARCBarcode.setDisable(true);
//                showDetailsBarcode();
//
//        }
//        });    */
//
//            LOGGER.log(Level.INFO, "BARCODE Listener");
//            txtARCBarcode.setDisable(true);
//            showDetailsBarcode();
//        } catch (NullPointerException e) {
//            //System.out.println("Null pointer Exception at barcodescan");
//            LOGGER.log(Level.INFO, "Null pointer Exception at barcodescan");
//
//        }
//
//    }
//
//    /*public void showDetailsBarcode(){
//
//        String arcNo = txtARCBarcode.getText();
//        String connurl = apiServerCheck.getARCURL();
//        String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);
//        System.out.println("connection status :"+connectionStatus);
//        if(!connectionStatus.contentEquals("connected")) {
//            lblStatus.setText(connectionStatus);
//            emptyBarcode();
//            txtARCBarcode.setDisable(false);
//        }
//        else {
//        String ArcDetailsList = "";
//          ArcDetailsList = apiServerCheck.getARCNoAPI(connurl, arcNo);
//          //Object obj = JsonReader.jsonToJava(ArcDetailsList);
//          //System.out.println("obj str : " +obj.toString());
//
//          ObjectMapper objMapper = new ObjectMapper();
//            try {
//                arcDetails = objMapper.readValue(ArcDetailsList, ARCDetails.class);
//            } catch (JsonProcessingException ex) {
//                 lblStatus.setText("Invalid Data Received From the Server");
//                 emptyBarcode();
//                 txtARCBarcode.setDisable(false);
//            }
//          System.out.println(arcDetails.toString());
//          if (arcDetails.getErrorCode().equals("0")){
//               lblStatus.setText("ARC Details Fetched Successfully");
//               emptyBarcode();
//               txtARCBarcode.setDisable(false);
//           }else{
//               lblStatus.setText(arcDetails.getDesc());
//               emptyBarcode();
//               txtARCBarcode.setDisable(false);
//           }
//
//        if(Integer.parseInt(arcDetails.getErrorCode())== 0) {
//            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//            holder.setARC(arcDetails);
//            txtName.setText(arcDetails.getName());
//            txtRank.setText(arcDetails.getRank());
//            txtapp.setText(arcDetails.getApplicantID());
//            txtUnit.setText(arcDetails.getUnit());
//            txtFinger.setText(arcDetails.getFingers().toString());
//            txtiris.setText(arcDetails.getIris().toString());
//            txtDlink.setText(arcDetails.getDetailLink());
//            txtarcstatus.setText(arcDetails.getArcstatus());
//            txtarcno.setText(arcDetails.getArcNo());
//            SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
//            saveEnrollment.setArcNo(txtARCno.getText());
//            saveEnrollment.setEnrollmentStationID(apiServerCheck.getStationID());
//            saveEnrollment.setEnrollmentStationUnitID(apiServerCheck.getUnitID());
//            saveEnrollment.setFp(new HashSet<>());
//            saveEnrollment.setIris(new HashSet<>());
//            saveEnrollment.setEnrollmentStatus("ARC Details Fetched");
//            holder.setEnrollmentDetails(saveEnrollment);
//            show_arcdetails_next1.setDisable(false);
//            emptyBarcode();
//            txtARCBarcode.setDisable(false);
//
//         //   App.setRoot("show_arcdetails");
//        }
//        else {
//             emtyTextBox();
//             show_arcdetails_next1.setDisable(true);
//             lblStatus.setText(arcDetails.getDesc());
//             emptyBarcode();
//             txtARCBarcode.setDisable(false);
//            //lblStatus.setText("Error in retriving details. Please try again");
//           }
//
//        }
//
//
//
//    }*/
//
//
//    public void showDetailsBarcode() {
//
//        try {
//            String ArcDetailsList = "";
//            //String arcNo = txtARCno.getText(); //need to uncomment
//            String arcNo = txtARCBarcode.getText();
//            //System.out.println("ARCNO:::"+arcNo);
//            //ARCDetails arcDetails = new ARCDetails();
//            String connurl = "";
//            String connectionStatus = "";
//
//
//            try {
//                if (arcNo.equals("") || arcNo.trim().equals("")) {
//                    //System.out.println("String is null, empty or blank");
//                    LOGGER.log(Level.INFO, "String is null, empty or blank");
//                    lblStatus.setText("Kindly Enter ARC Number Details");
//                    return;
//                }
//
//                connurl = apiServerCheck.getARCURL();
//                connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);
//
//                if (!connectionStatus.contentEquals("connected")) {
//                    lblStatus.setText("Network Connection Timed out From Server, Kindly try again");
//                    LOGGER.log(Level.INFO, "Fetch Arc Details Connection Status:" + connectionStatus);
//                    return;
//                }
//                ArcDetailsList = fetchArcDetails(arcNo);
//                //Object obj = JsonReader.jsonToJava(ArcDetailsList);
//                //System.out.println("obj str : " +obj.toString());
//                //System.out.println("ARCdetail"+ArcDetailsList);
//                ObjectMapper objMapper = new ObjectMapper();
//                arcDetails = objMapper.readValue(ArcDetailsList, ARCDetails.class);
//                //System.out.println(arcDetails.toString());
//                //System.out.println("ARC Details Desc:"+arcDetails.getDesc());
//                LOGGER.log(Level.INFO, "ARC Details Desc:", arcDetails.getDesc());
//                if (arcDetails.getErrorCode().equals("0")) {
//                    lblStatus.setText("ARC Details Fetched Successfully");
//                    LOGGER.log(Level.INFO, "ARC Details Fetched Successfully");
//                } else {
//                    lblStatus.setText(arcDetails.getDesc());
//                    LOGGER.log(Level.INFO, arcDetails.getDesc() + "ARC Details Fetched Error");
//                }
//
//                if (Integer.parseInt(arcDetails.getErrorCode()) == 0) {
//                    try {
//                        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                        holder.setArcDetails(arcDetails);
//                        txtName.setText(arcDetails.getName());
//                        txtRank.setText(arcDetails.getRank());
//                        txtapp.setText(arcDetails.getApplicantID());
//                        txtUnit.setText(arcDetails.getUnit());
//                        //txtFinger.setText(arcDetails.getFingers().toString());
//                        //txtFinger.setText(arcDetails.getFingers().toString().substring(1,arcDetails.getFingers().toString().length()-1));
//                        if (arcDetails.getFingers().size() > 0) {
//                            txtFinger.setText(arcDetails.getFingers().toString().substring(1, arcDetails.getFingers().toString().length() - 1));
//                            System.out.println("greater than zero");
//
//                        } else {
//                            txtFinger.setText("NA");
//                            System.out.println("less than zero");
//                        }
//                        //txtiris.setText(arcDetails.getIris().toString());
//                        if (arcDetails.getIris().size() > 0) {
//                            txtiris.setText(arcDetails.getIris().toString().substring(1, arcDetails.getIris().toString().length() - 1));
//                            System.out.println("greater than zero");
//
//                        } else {
//                            txtiris.setText("NA");
//                            System.out.println("less than zero");
//                        }
//                        // txtiris.setText(arcDetails.getIris().toString().substring(1,arcDetails.getIris().toString().length()-1));
//                        txtDlink.setText(arcDetails.getDetailLink());
//                        txtarcstatus.setText(arcDetails.getArcStatus());
//                        txtarcno.setText(arcDetails.getArcNo());
//                        SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
//                        //saveEnrollment.setArcNo(txtARCBarcode.getText());
//                        saveEnrollment.setArcNo(arcDetails.getArcNo());
//                        saveEnrollment.setEnrollmentStationID(apiServerCheck.getStationID());
//                        saveEnrollment.setEnrollmentStationUnitID(apiServerCheck.getUnitID());
//                        saveEnrollment.setFp(new HashSet<>());
//                        saveEnrollment.setIris(new HashSet<>());
//
//                        //Added For Biometric Options
//                        LOGGER.log(Level.INFO, "ARC BIOmetric Options::" + arcDetails.getBiometricOptions());
//                        if (arcDetails.getBiometricOptions() == null || arcDetails.getBiometricOptions().isEmpty() || arcDetails.getBiometricOptions().contains("None") || arcDetails.getBiometricOptions().contains("none")) {
//                            lblStatus.setText("Biometric capturing not required for given ARC Number");
//                            txtARCBarcode.setDisable(false);
//                            barcodearcbutton.requestFocus();
//                            return;
//                        }
//                        txtbiometricoptions.setText(arcDetails.getBiometricOptions());
//                        saveEnrollment.setBiometricOptions(arcDetails.getBiometricOptions());
//                        //Added For Biometric Options
//                        saveEnrollment.setEnrollmentStatus("ARC Details Fetched");
//
//                        holder.setSaveEnrollmentDetails(saveEnrollment);
//                        show_arcdetails_next1.setDisable(false);
//                        //emptyBarcode();
//                        //txtARCBarcode.setDisable(false);
//                        barcodearcbutton.requestFocus();
//                        //System.out.println("Holder :::"+holder.getARC().getArcNo());
//
//                        //   App.setRoot("show_arcdetails");
//                    } catch (NullPointerException e) {
//                        System.out.println("Null Pointer Exception");
//                        LOGGER.log(Level.INFO, "Null Pointer Exception - showDetailsBarcode");
//                    }
//                } else {
//                    try {
//                        //lblStatus.setText("Error in retriving details. Please try again");
//                        emtyTextBox();
//                        show_arcdetails_next1.setDisable(true);
//                        lblStatus.setText(arcDetails.getDesc());
//                        LOGGER.log(Level.INFO, arcDetails.getDesc() + "Arc Details Description");
//                    } catch (NullPointerException e) {
//                        //System.out.println("Null Pointer Exception");
//                        LOGGER.log(Level.INFO, "Null Pointer Exception - showDetailsBarcode");
//                    }
//                }
//
//            } catch (NullPointerException e) {
//                lblStatus.setText("Results Not Fetched From Server");
//                LOGGER.log(Level.INFO, "Results Not Fetched From Server - showDetailsBarcode");
//            } catch (JsonProcessingException ex) {
//
//                lblStatus.setText("Invalid Data Received From the Server");
//                LOGGER.log(Level.INFO, "Invalid Data Received From the Server - showDetailsBarcode");
//            }
//
//
//        } catch (NullPointerException e) {
//            lblStatus.setText("Results Not Fetched From Server");
//            //System.out.println("Null pointer Exception at showARCDetails");
//            LOGGER.log(Level.INFO, "Results Not Fetched From Server - showDetailsBarcode");
//        }
//
//    }
//
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//        //this.handler = appLog.getLogger();
//        //LOGGER.addHandler(handler);
//        /* max length of text field user name */
//        int maxLength = 15;
//        /* add ChangeListner to TextField to restrict the TextField Length*/
//        txtARCno.textProperty().addListener(new com.cdac.enrollmentstation.event.ChangeListener(txtARCno, maxLength));
//        txtARCBarcode.textProperty().addListener(new com.cdac.enrollmentstation.event.ChangeListener(txtARCBarcode, maxLength));
//
//
//        //inputarcbutton.setFocusTraversable(false);
//    }
//
//    public void emtyTextBox() {
//        txtName.setText("");
//        txtRank.setText("");
//        txtapp.setText("");
//        txtUnit.setText("");
//        txtFinger.setText("");
//        txtiris.setText("");
//        txtDlink.setText("");
//        txtarcstatus.setText("");
//        txtarcno.setText("");
//
//    }
//
//    public void emptyBarcode() {
//        txtARCBarcode.setText("");
//    }
//
//
//}
