package com.cdac.enrollmentstation.controller;


import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.api.APIServerCheck;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ARCDetails;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceStatusController_040722 implements Initializable {
    public ARCDetails arcDetails;
    public APIServerCheck apiServerCheck = new APIServerCheck();
    @FXML
    public ImageView irisstatus;
    @FXML
    public ImageView slapstatus;
    @FXML
    public ImageView camerastatus;
    @FXML
    public ImageView barcodestatus;
    @FXML
    public ImageView mafisurl;
    @FXML
    Label statusMsg;
    TestProp prop = new TestProp();

    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public DeviceStatusController_040722() {
        // this.handler = appLog.getLogger();
        // LOGGER.addHandler(handler);
    }


    @FXML
    public void showDeviceStatus() {

        //System.out.println("inside show device status");
        LOGGER.log(Level.INFO, "inside show device status");
        Image redcross = new Image("/haar_facedetection/redcross.png");
        Image greentick = new Image("/haar_facedetection/tickgreen.jpg");


        try {
            //String irisFile = "/etc/fingerprint_iris.txt";
            String irisFile = "";
            irisFile = prop.getProp().getProperty("irisFile");
            File iris_file = new File(irisFile);
            FileInputStream readIrisFile = new FileInputStream(iris_file);
            byte[] data = new byte[(int) iris_file.length()];
            readIrisFile.read(data);
            String fileContent = new String(data, "UTF-8");
            System.out.println(readIrisFile);

            if (fileContent.contains("yes")) {
                //System.out.println("Iris Connected");
                LOGGER.log(Level.INFO, "Iris Connected");
                irisstatus.setImage(greentick);

            } else {
                //System.out.println("Iris Not Connected");
                LOGGER.log(Level.INFO, "Iris Not Connected");
                irisstatus.setImage(redcross);
            }
            readIrisFile.close();

        } catch (FileNotFoundException e) {
            //System.out.println("An error occurred reading IrisDevice File.");
            LOGGER.log(Level.INFO, "An error occurred reading IrisDevice File.", e);
            //e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DeviceStatusController_040722.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "IOException", ex);
        }


        try {
            //String slapscanFile = "/etc/fingerprint_realscan.txt";
            String slapscanFile = "";
            slapscanFile = prop.getProp().getProperty("slapscanFile"); /*File slapscan_file = new File(slapscanFile);
            Scanner readSlapFile = new Scanner(slapscan_file);*/

            File file = new File(slapscanFile);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");

            System.out.println(fileContent);
            if (fileContent.contains("yes")) {
                //System.out.println("Slap Scanner Connected");
                LOGGER.log(Level.INFO, "Slap Scanner Connected");
                slapstatus.setImage(greentick);
            } else {
                //System.out.println("Slap Scanner Not Connected");
                LOGGER.log(Level.INFO, "Slap Scanner Not Connected");
                slapstatus.setImage(redcross);
            }
            fis.close();

        } catch (FileNotFoundException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "An error occur reading SlapScanner File.", e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "An error occur reading SlapScanner File.", e.getMessage());
            //e.printStackTrace();
        }

        //Camera
        try {
            //String cameraFilePath = "/etc/fingerprint_camera.txt";
            String cameraFilePath = "";
            cameraFilePath = prop.getProp().getProperty("cameraFilePath");
            File cameraFile = new File(cameraFilePath);
            FileInputStream fis = new FileInputStream(cameraFile);
            byte[] data = new byte[(int) cameraFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                //System.out.println("Camera Connected");
                LOGGER.log(Level.INFO, "Camera  Connected");
                camerastatus.setImage(greentick);
            } else {
                //System.out.println("Camera Not Connected");
                LOGGER.log(Level.INFO, "Camera Not Connected");
                camerastatus.setImage(redcross);
            }
            fis.close();

        } catch (FileNotFoundException e) {
            //System.out.println("An error occurred reading Camera File.");
            LOGGER.log(Level.INFO, "An error occur reading Camera File.", e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "An error occur reading Camera File.", e.getMessage());
            //e.printStackTrace();
        }

        //Barcode Scanner
        try {
            //String barcodeFilePath = "/etc/fingerprint_barcode.txt";
            String barcodeFilePath = "";
            barcodeFilePath = prop.getProp().getProperty("barcodeFilePath");
            File barcodeFile = new File(barcodeFilePath);
            FileInputStream fis = new FileInputStream(barcodeFile);
            byte[] data = new byte[(int) barcodeFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                //System.out.println("Barcode Connected");
                LOGGER.log(Level.INFO, "Barcode  Connected");
                barcodestatus.setImage(greentick);

            } else {
                //System.out.println("Barcode Not Connected");
                LOGGER.log(Level.INFO, "Barcode Not Connected");
                barcodestatus.setImage(redcross);

            }
            fis.close();
        } catch (FileNotFoundException e) {
            //System.out.println("An error occurred reading Barcode File.");
            LOGGER.log(Level.INFO, "An error occurred reading Barcode File.", e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "An error occurred reading Barcode File.", e.getMessage());
            //e.printStackTrace();
        }

        //URL Connectivity

        try {

            String arcNo = "123abc";
            String connurl = apiServerCheck.getARCURL();
            String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);

            if (!connectionStatus.contentEquals("connected")) {
                //System.out.println("mafisurl not Connected");
                LOGGER.log(Level.INFO, "mafisurl Not Connected");
                mafisurl.setImage(redcross);
            } else {
                //System.out.println("mafisurl Connected");
                LOGGER.log(Level.INFO, "mafisurl Connected");
                mafisurl.setImage(greentick);
            }
        } catch (Exception e) {
            //System.out.println("Exception:: "+e);
            LOGGER.log(Level.INFO, "Exception:: ", e);
        }


    }

    @FXML
    public void showDeviceStatusPrevious() {
        try {
            App.setRoot("first_screen");
        } catch (IOException ex) {
            Logger.getLogger(DeviceStatusController_040722.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "Exception:: ", ex);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        Image redcross = new Image("/haar_facedetection/redcross.png");
        Image greentick = new Image("/haar_facedetection/tickgreen.jpg");


        try {
            //String irisFile = "/etc/fingerprint_iris.txt";
            String irisFile = "";
            irisFile = prop.getProp().getProperty("irisFile");
            File iris_file = new File(irisFile);
            FileInputStream readIrisFile = new FileInputStream(iris_file);
            byte[] data = new byte[(int) iris_file.length()];
            readIrisFile.read(data);
            String fileContent = new String(data, "UTF-8");
            System.out.println(readIrisFile);

            if (fileContent.contains("yes")) {
                //System.out.println("Iris Connected");
                LOGGER.log(Level.INFO, "Iris Not Connected");
                irisstatus.setImage(greentick);

            } else {
                //System.out.println("Iris Not Connected");
                LOGGER.log(Level.INFO, "Iris Not Connected");
                irisstatus.setImage(redcross);
            }
            readIrisFile.close();

        } catch (FileNotFoundException e) {
            //System.out.println("An error occurred reading IrisDevice File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DeviceStatusController_040722.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "Exception:: ", ex);
        }


        try {
            //String slapscanFile = "/etc/fingerprint_realscan.txt";
            String slapscanFile = "";
            slapscanFile = prop.getProp().getProperty("slapscanFile"); /*File slapscan_file = new File(slapscanFile);
            Scanner readSlapFile = new Scanner(slapscan_file);*/

            File file = new File(slapscanFile);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");

            System.out.println(fileContent);
            if (fileContent.contains("yes")) {
                //System.out.println("Slap Scanner Connected");
                LOGGER.log(Level.INFO, "Slap Scanner Connected");
                slapstatus.setImage(greentick);
            } else {
                //System.out.println("Slap Scanner Not Connected");
                LOGGER.log(Level.INFO, "Slap Scanner Not Connected");
                slapstatus.setImage(redcross);
            }
            fis.close();

        } catch (FileNotFoundException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        }

        //Camera
        try {
            //String cameraFilePath = "/etc/fingerprint_camera.txt";
            String cameraFilePath = "";
            cameraFilePath = prop.getProp().getProperty("cameraFilePath");
            File cameraFile = new File(cameraFilePath);
            FileInputStream fis = new FileInputStream(cameraFile);
            byte[] data = new byte[(int) cameraFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                //System.out.println("Camera Connected");
                LOGGER.log(Level.INFO, "Camera Connected");
                camerastatus.setImage(greentick);
            } else {
                //System.out.println("Camera Not Connected");
                LOGGER.log(Level.INFO, "Camera Not Connected");
                camerastatus.setImage(redcross);
            }
            fis.close();

        } catch (FileNotFoundException e) {
            //System.out.println("An error occurred reading Camera File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        }

        //Barcode Scanner
        try {
            //String barcodeFilePath = "/etc/fingerprint_barcode.txt";
            String barcodeFilePath = "";
            barcodeFilePath = prop.getProp().getProperty("barcodeFilePath");
            File barcodeFile = new File(barcodeFilePath);
            FileInputStream fis = new FileInputStream(barcodeFile);
            byte[] data = new byte[(int) barcodeFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                //System.out.println("Barcode Connected");
                LOGGER.log(Level.INFO, "Barcode  Connected");
                barcodestatus.setImage(greentick);

            } else {
                //System.out.println("Barcode Not Connected");
                LOGGER.log(Level.INFO, "Barcode Not Connected");
                barcodestatus.setImage(redcross);

            }
            fis.close();
        } catch (FileNotFoundException e) {
            //System.out.println("An error occurred reading Barcode File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("An error occur reading SlapScanner File.");
            LOGGER.log(Level.INFO, "Exception:: ", e);
            //e.printStackTrace();
        }

        //URL Connectivity

        try {

            String arcNo = "123abc";
            String connurl = apiServerCheck.getARCURL();
            String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);

            if (!connectionStatus.contentEquals("connected")) {
                // System.out.println("mafisurl not Connected");
                LOGGER.log(Level.INFO, "mafisurl Not Connected");
                mafisurl.setImage(redcross);
            } else {
                // System.out.println("mafisurl Connected");
                LOGGER.log(Level.INFO, "mafisurl  Connected");
                mafisurl.setImage(greentick);
            }
        } catch (Exception e) {
            //System.out.println("Exception:: "+e);
            LOGGER.log(Level.INFO, "Exception:: ", e);
        }

    }

}
