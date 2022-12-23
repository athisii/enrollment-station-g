package com.cdac.enrollmentstation.controller;


import RealScan.TestSlapScanner;
import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestIris;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.api.APIServerCheck;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceStatusController implements Initializable {

    public APIServerCheck apiServerCheck = new APIServerCheck();
    @FXML
    public ImageView irisstatus, sdkirisstatus, sdkslapstatus;
    @FXML
    public ImageView slapstatus;
    @FXML
    public ImageView camerastatus;
    @FXML
    public ImageView barcodestatus;
    @FXML
    public ImageView mafisurl;

    TestProp prop = new TestProp();
    TestIris testIris = new TestIris();
    TestSlapScanner testSlap = new TestSlapScanner();


    @FXML
    public void showDeviceStatus() {

        System.out.println("inside show device status");
        Image redcross = new Image("/haar_facedetection/redcross.png");
        Image greentick = new Image("/haar_facedetection/tickgreen.jpg");


        //To Test Iris SDK
        try {
            String message = testIris.sdkIrisStatus();
            System.out.println("message:::" + message);
            if (message.equals("true")) {
                sdkirisstatus.setImage(greentick);
            } else {
                sdkirisstatus.setImage(redcross);
            }

        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }

        //To Test Slap SDK
        try {
            String message = testSlap.sdkSlapScannerStatus();
            System.out.println("message:::" + message);
            if (message.equals("true")) {
                sdkslapstatus.setImage(greentick);
            } else {
                sdkslapstatus.setImage(redcross);
            }

        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }


        FileInputStream readIrisFile = null;
        try {
            //String irisFile = "/etc/fingerprint_iris.txt";
            String irisFile = "";
            irisFile = prop.getProp().getProperty("irisFile");
            File iris_file = new File(irisFile);
            readIrisFile = new FileInputStream(iris_file);
            byte[] data = new byte[(int) iris_file.length()];
            readIrisFile.read(data);
            String fileContent = new String(data, "UTF-8");
            System.out.println(readIrisFile);

            if (fileContent.contains("yes")) {
                System.out.println("Iris Connected");
                irisstatus.setImage(greentick);

            } else {
                System.out.println("Iris Not Connected");
                irisstatus.setImage(redcross);
            }
            readIrisFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading IrisDevice File.");
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                readIrisFile.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        FileInputStream fis = null;

        try {
            //String slapscanFile = "/etc/fingerprint_realscan.txt";
            String slapscanFile = "";
            slapscanFile = prop.getProp().getProperty("slapscanFile"); /*File slapscan_file = new File(slapscanFile);
            /*File slapscan_file = new File(slapscanFile);
            Scanner readSlapFile = new Scanner(slapscan_file);*/

            File file = new File(slapscanFile);
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");

            System.out.println(fileContent);
            if (fileContent.contains("yes")) {
                System.out.println("Slap Scanner Connected");
                slapstatus.setImage(greentick);
            } else {
                System.out.println("Slap Scanner Not Connected");
                slapstatus.setImage(redcross);
            }


        } catch (FileNotFoundException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Camera
        try {
            //String cameraFilePath = "/etc/fingerprint_camera.txt";
            String cameraFilePath = "";
            cameraFilePath = prop.getProp().getProperty("cameraFilePath");
            File cameraFile = new File(cameraFilePath);
            fis = new FileInputStream(cameraFile);
            byte[] data = new byte[(int) cameraFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                System.out.println("Camera Connected");
                camerastatus.setImage(greentick);
            } else {
                System.out.println("Camera Not Connected");
                camerastatus.setImage(redcross);
            }


        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading Camera File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Barcode Scanner
        try {
            //String barcodeFilePath = "/etc/fingerprint_barcode.txt";
            String barcodeFilePath = "";
            barcodeFilePath = prop.getProp().getProperty("barcodeFilePath");
            File barcodeFile = new File(barcodeFilePath);
            fis = new FileInputStream(barcodeFile);
            byte[] data = new byte[(int) barcodeFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                System.out.println("Barcode Connected");
                barcodestatus.setImage(greentick);

            } else {
                System.out.println("Barcode Not Connected");
                barcodestatus.setImage(redcross);

            }
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading Barcode File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //URL Connectivity

        try {

            String arcNo = "123abc";
            String connurl = apiServerCheck.getARCURL();
            String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);

            if (!connectionStatus.contentEquals("connected")) {
                System.out.println("mafisurl not Connected");
                mafisurl.setImage(redcross);
            } else {
                System.out.println("mafisurl Connected");
                mafisurl.setImage(greentick);
            }
        } catch (Exception e) {
            System.out.println("Exception:: " + e);
        }


    }

    @FXML
    public void showDeviceStatusPrevious() throws IOException {
        App.setRoot("first_screen");

    }

    @FXML
    public void showDeviceStatusBack() throws IOException {
        App.setRoot("admin_config");

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.


        Image redcross = new Image("/haar_facedetection/redcross.png");
        Image greentick = new Image("/haar_facedetection/tickgreen.jpg");

        //To Test Iris SDK
        try {
            String message = testIris.sdkIrisStatus();
            System.out.println("message" + message);
            if (message.equals("true")) {
                sdkirisstatus.setImage(greentick);
            } else {
                sdkirisstatus.setImage(redcross);
            }

        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }

        //To Test Slap SDK
        try {
            String message = testSlap.sdkSlapScannerStatus();
            System.out.println("message:::" + message);
            if (message.equals("true")) {
                sdkslapstatus.setImage(greentick);
            } else {
                sdkslapstatus.setImage(redcross);
            }

        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }


        FileInputStream readIrisFile = null;
        try {
            //String irisFile = "/etc/fingerprint_iris.txt";
            String irisFile = "";
            irisFile = prop.getProp().getProperty("irisFile");
            File iris_file = new File(irisFile);
            readIrisFile = new FileInputStream(iris_file);
            byte[] data = new byte[(int) iris_file.length()];
            readIrisFile.read(data);
            String fileContent = new String(data, "UTF-8");
            System.out.println(readIrisFile);

            if (fileContent.contains("yes")) {
                System.out.println("Iris Connected");
                irisstatus.setImage(greentick);

            } else {
                System.out.println("Iris Not Connected");
                irisstatus.setImage(redcross);
            }


        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading IrisDevice File.");
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                readIrisFile.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        FileInputStream fis = null;
        try {
            //String slapscanFile = "/etc/fingerprint_realscan.txt";
            String slapscanFile = "";
            slapscanFile = prop.getProp().getProperty("slapscanFile"); /*File slapscan_file = new File(slapscanFile);
            /*File slapscan_file = new File(slapscanFile);
            Scanner readSlapFile = new Scanner(slapscan_file);*/

            File file = new File(slapscanFile);
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");

            System.out.println(fileContent);
            if (fileContent.contains("yes")) {
                System.out.println("Slap Scanner Connected");
                slapstatus.setImage(greentick);
            } else {
                System.out.println("Slap Scanner Not Connected");
                slapstatus.setImage(redcross);
            }


        } catch (FileNotFoundException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Camera
        try {
            //String cameraFilePath = "/etc/fingerprint_camera.txt";
            String cameraFilePath = "";
            cameraFilePath = prop.getProp().getProperty("cameraFilePath");
            File cameraFile = new File(cameraFilePath);
            fis = new FileInputStream(cameraFile);
            byte[] data = new byte[(int) cameraFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                System.out.println("Camera Connected");
                camerastatus.setImage(greentick);
            } else {
                System.out.println("Camera Not Connected");
                camerastatus.setImage(redcross);
            }


        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading Camera File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Barcode Scanner
        try {
            //String barcodeFilePath = "/etc/fingerprint_barcode.txt";
            String barcodeFilePath = "";
            barcodeFilePath = prop.getProp().getProperty("barcodeFilePath");
            File barcodeFile = new File(barcodeFilePath);
            fis = new FileInputStream(barcodeFile);
            byte[] data = new byte[(int) barcodeFile.length()];
            fis.read(data);

            String fileContent = new String(data, "UTF-8");
            System.out.println(fileContent);

            if (fileContent.contains("yes")) {
                System.out.println("Barcode Connected");
                barcodestatus.setImage(greentick);

            } else {
                System.out.println("Barcode Not Connected");
                barcodestatus.setImage(redcross);

            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading Barcode File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occur reading SlapScanner File.");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //URL Connectivity

        try {

            String arcNo = "123abc";
            String connurl = apiServerCheck.getARCURL();
            String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);

            if (!connectionStatus.contentEquals("connected")) {
                System.out.println("mafisurl not Connected");
                mafisurl.setImage(redcross);
            } else {
                System.out.println("mafisurl Connected");
                mafisurl.setImage(greentick);
            }
        } catch (Exception e) {
            System.out.println("Exception:: " + e);
        }

    }


}
