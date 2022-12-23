package com.cdac.enrollmentstation.controller;

import RealScan.FP;
import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.model.ARCDetails;
import com.cdac.enrollmentstation.model.ARCDetailsHolder;
import com.cdac.enrollmentstation.model.SaveEnrollmentDetails;
import com.cdac.enrollmentstation.service.ObjectReaderWriter;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mantra.midfingerenroll.*;
import com.mantra.midfingerenroll.enums.DeviceDetection;
import com.mantra.midfingerenroll.enums.DeviceModel;
import com.mantra.midfingerenroll.enums.ImageFormat;
import com.mantra.midfingerenroll.enums.SlapPosition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static java.lang.Thread.sleep;

public class FingerprintController implements Initializable, MIDFingerEnroll_Callback {


    //private DeviceInfo info = null;
    /*private img m_FingerPrintImage;
    private img m_CropFingerPrintImage1;
    private img m_CropFingerPrintImage2;
    private img m_CropFingerPrintImage3;
    private img m_CropFingerPrintImage4;
    */
//    private MyIcon m_FingerPrintImage;
    javafx.scene.image.Image img = null;
    private final Object syncImg = new Object();
    //List<String> deviceList = new ArrayList<String>();
    //List<String> ls = new ArrayList<String>();


    @FXML
    private ImageView preview;

    @FXML
    private ImageView LICanvas;

    @FXML
    private ImageView LMCanvas;

    @FXML
    private ImageView LRCanvas;

    @FXML
    private ImageView LLCanvas;

    @FXML
    private ImageView RICanvas;

    @FXML
    private ImageView RMCanvas;

    @FXML
    private ImageView RRCanvas;

    @FXML
    private ImageView RLCanvas;

    @FXML
    private ImageView LTCanvas;

    @FXML
    private ImageView RTCanvas;

    @FXML
    private ImageView leftlittle;

    @FXML
    private Button scan;

    @FXML
    private Label qlabel;

    @FXML
    private TextField quality;

    @FXML
    private Label log;

    @FXML
    private Label labelview;

    @FXML
    public Label statusField;

    private MIDFingerEnroll enroll = null;
    private DeviceInfo info = null;

    SlapPosition position;

    Set<FP> fingerPrintSet = new HashSet<>();
    Thread tleft = null;
    Thread tRight = null;
    Thread tThumb = null;
    String returnmsg = "";
    public ARCDetails arcDetails;

    public FingerprintController() {
        System.out.print("In Initialize function");
    }

    private void messagestatus(String message) {

        statusField.setText(message);
    }

    @FXML
    private void oninit() {

        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        arcDetails = holder.getArcDetails();

        //List<String> missingfingers = arcDetails.getFingers();        
        List<String> missingfingers = new ArrayList<>();
        missingfingers.add("LR");
        missingfingers.add("LL");
        //missingfingers.add("LT");
        missingfingers.add("LM");
        missingfingers.add("LI");
        //missingfingers.add("RR");        
        //missingfingers.add("RL");
        //missingfingers.add("RT");
        //missingfingers.add("RM");
        //missingfingers.add("RI");
        arcDetails.setFingers(missingfingers);
        System.out.println("MISSING FINGERS:::" + arcDetails.getFingers());


        try {
            tleft = new Thread(DoSequenceCapture);
            tleft.start();
        } catch (Exception e) {
            System.out.println("Error in loop::" + e);

        }

    }


    @FXML
    private void RightScan() {
        try {
            tRight = new Thread(DoSequenceCaptureRight);
            tRight.start();
        } catch (Exception e) {
            System.out.println("Error in loop::" + e);

        }

    }

    @FXML
    private void ThumbScan() {
        try {
            tThumb = new Thread(DoSequenceCaptureThumb);
            tThumb.start();
        } catch (Exception e) {
            System.out.println("Error in loop::" + e);

        }


    }


    Runnable DoSequenceCapture = new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("DoSequence");
                leftScan();
                rightScan();
                thumbScan();
            } catch (Exception e) {
                System.out.println("Exception:" + e);
            }
        }
    };

    Runnable DoSequenceCaptureRight = new Runnable() {
        @Override
        public void run() {
            try {
                rightScan();
                thumbScan();
            } catch (Exception e) {
                System.out.println("Exception:" + e);
            }
        }
    };


    Runnable DoSequenceCaptureThumb = new Runnable() {
        @Override
        public void run() {
            try {
                thumbScan();
            } catch (Exception e) {
                System.out.println("Exception:" + e);
            }
        }
    };


    @FXML
    private void goBack() {
        System.out.println("In Go Back");
    }

    @FXML
    private void stayBack() {
        System.out.println("In stayBack");
    }


    private void leftScan() {

        try {

            enroll = new MIDFingerEnroll(this);
            System.out.println("ENROLL:" + enroll);
            //String version1=enroll.GetSDKVersion();
            //quality.setText(version1);
            System.out.println("SDK version::::" + enroll.GetSDKVersion());


            /* String model = jcbConnectedDevices.getSelectedItem().toString();*/
            //String model = "MORPHS_5287406";
            info = new DeviceInfo();
            //System.out.println("Device Model::"+DeviceModel.valueFor(model));
            System.out.println("Info::" + info);
            //System.out.println("Model::"+model);
            List<String> deviceList = new ArrayList<String>();
            int ret = enroll.GetConnectedDevices(deviceList);

            System.out.println("device return value" + ret);

            if (ret != 0) {
                //showLogs("Get Connected Device List Error: " + ret);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        returnmsg = "Get Connected Device List Error: " + ret;
                        messagestatus(returnmsg);
                        //rightScan.setDisable(false);                      
                        return;
                    }
                });
                return;
            }
            List<String> ls = new ArrayList<String>();
            for (String list : deviceList) {
                ls.add(list);
                System.out.println("Device Name:" + list);
            }
            System.out.println("DeviceList:" + deviceList);
            int size = deviceList.size();
            System.out.println("Size of Device List" + size);
            System.out.println("DeviceLs:" + ls);

            System.out.println("get Device List1" + ls.get(0));
            System.out.println("get Device List1" + ls.get(1));
            System.out.println("Connectd device : " + deviceList.get(0));
            //System.out.println("Connectd device : "+deviceList.get(1));
            int retinit = enroll.Init(DeviceModel.valueFor(ls.get(0)), info, ls.get(0));
            System.out.println(retinit);
            if (retinit != 0) {
                System.out.println("Start Init Error:" + enroll.GetErrorMessage(retinit));
                return;
            }

            System.out.println("Initilized Successfully");
            //sleep(1000);


            //System.out.println(info);
        } catch (Exception e) {
            System.out.println("Error in init::" + e);
        }


        // App.setRoot("second_screen");
        //App.setRoot("samplefx_1");
        try {
            System.out.println("In On Scan");
            int minQuality = 40;
            int timeout = 10000;

            //int retCapture = enroll.StartCapture(minQuality, timeout, SlapPosition.RIGHT_HAND, fingerPosition);
            position = SlapPosition.valueOf("LEFT_HAND");

            FingerPosition fingerPosition = new FingerPosition();
            //fingerPosition.RIGHT_LITTLE = false;
            //fingerPosition.RIGHT_RING = false;
            //fingerPosition.RIGHT_MIDDLE = false;
            //fingerPosition.RIGHT_INDEX = false;

            System.out.println("Slap Position::" + position);
            System.out.println("Finger Position::" + fingerPosition);

            int retCapture = enroll.StartCapture(minQuality, timeout, position, fingerPosition);
            System.out.println("Start Capture Return Value ::" + retCapture);
            sleep(10000);
            if (retCapture != 0) {
                System.out.println("Start Capture Error:" + enroll.GetErrorMessage(retCapture));
                return;
            }
        } catch (Exception e) {
            System.out.println("Error in OnScan::" + e);
        }
    }


    private void rightScan() {

        try {
            enroll = new MIDFingerEnroll(this);
            System.out.println("ENROLL:" + enroll);
            //String version1=enroll.GetSDKVersion();
            //quality.setText(version1);
            System.out.println("SDK version::::" + enroll.GetSDKVersion());
            /* String model = jcbConnectedDevices.getSelectedItem().toString();*/
            String model = "MORPHS_5287406";
            info = new DeviceInfo();
            System.out.println("Device Model::" + DeviceModel.valueFor(model));
            System.out.println("Info::" + info);
            System.out.println("Model::" + model);
            List<String> deviceList = new ArrayList<String>();
            int ret = enroll.GetConnectedDevices(deviceList);
            System.out.println(ret);
            List<String> ls = new ArrayList<String>();
            for (String list : deviceList) {
                ls.add(list);
                System.out.println("Device Name:" + list);
            }
            System.out.println("DeviceList:" + deviceList);
            int size = deviceList.size();
            System.out.println("Size of Device List" + size);
            System.out.println("DeviceLs:" + ls);
            System.out.println("get Device List1" + ls.get(0));
            System.out.println("get Device List1" + ls.get(1));

            System.out.println("Connectd device : " + deviceList.get(1));
            //System.out.println("Connectd device : "+deviceList.get(1));
            int retinit = enroll.Init(DeviceModel.valueFor(ls.get(1)), info, ls.get(1));
            System.out.println(retinit);
            if (retinit != 0) {
                System.out.println("Start Init Error:" + enroll.GetErrorMessage(retinit));
                return;
            }

            System.out.println("Initilized Successfully");
            //sleep(1000);

            //System.out.println(info);
        } catch (Exception e) {
            System.out.println("Error in init::" + e);
        }


        // App.setRoot("second_screen");
        //App.setRoot("samplefx_1");
        try {
            System.out.println("In On Scan");
            int minQuality = 40;
            int timeout = 10000;

            //int retCapture = enroll.StartCapture(minQuality, timeout, SlapPosition.RIGHT_HAND, fingerPosition);
            position = SlapPosition.valueOf("RIGHT_HAND");
            FingerPosition fingerPosition = new FingerPosition();
            //fingerPosition.RIGHT_LITTLE = false;
            //fingerPosition.RIGHT_RING = false;
            //fingerPosition.RIGHT_MIDDLE = false;
            //fingerPosition.RIGHT_INDEX = false;

            System.out.println("Slap Position::" + position);
            System.out.println("Finger Position::" + fingerPosition);

            int retCapture = enroll.StartCapture(minQuality, timeout, position, fingerPosition);
            System.out.println("Start Capture Return Value::" + retCapture);
            sleep(10000);

            if (retCapture != 0) {
                System.out.println("Start Capture Error:" + enroll.GetErrorMessage(retCapture));
                return;
            }
        } catch (Exception e) {
            System.out.println("Error in OnScan::" + e);
        }
    }


    private void thumbScan() {

        try {
            enroll = new MIDFingerEnroll(this);
            System.out.println("ENROLL:" + enroll);
            //String version1=enroll.GetSDKVersion();
            //quality.setText(version1);
            System.out.println("SDK version::::" + enroll.GetSDKVersion());
            /* String model = jcbConnectedDevices.getSelectedItem().toString();*/
            String model = "MORPHS_5287406";
            info = new DeviceInfo();
            System.out.println("Device Model::" + DeviceModel.valueFor(model));
            System.out.println("Info::" + info);
            System.out.println("Model::" + model);
            List<String> deviceList = new ArrayList<String>();
            int ret = enroll.GetConnectedDevices(deviceList);

            System.out.println(ret);

            if (ret != 0) {
                //showLogs("Get Connected Device List Error: " + ret);
                return;
            }


            System.out.println(ret);
            List<String> ls = new ArrayList<String>();
            for (String list : deviceList) {
                ls.add(list);
                System.out.println("Device Name:" + list);
            }
            System.out.println("DeviceList:" + deviceList);
            int size = deviceList.size();
            System.out.println("Size of Device List" + size);
            System.out.println("DeviceLs:" + ls);
            System.out.println("get Device List1" + ls.get(0));
            System.out.println("get Device List1" + ls.get(1));

            System.out.println("Connectd device : " + deviceList.get(0));
            //System.out.println("Connectd device : "+deviceList.get(1));
            int retinit = enroll.Init(DeviceModel.valueFor(ls.get(0)), info, ls.get(0));
            System.out.println(retinit);
            if (retinit != 0) {
                System.out.println("Start Init Error:" + enroll.GetErrorMessage(retinit));
                return;
            }

            System.out.println("Initilized Successfully");
            //sleep(1000);

            //System.out.println(info);
        } catch (Exception e) {
            System.out.println("Error in init::" + e);
        }


        // App.setRoot("second_screen");
        //App.setRoot("samplefx_1");
        try {
            System.out.println("In On Scan Thumb");
            int minQuality = 40;
            int timeout = 10000;

            //int retCapture = enroll.StartCapture(minQuality, timeout, SlapPosition.RIGHT_HAND, fingerPosition);
            position = SlapPosition.valueOf("THUMB");
            FingerPosition fingerPosition = new FingerPosition();
            //fingerPosition.RIGHT_LITTLE = false;
            //fingerPosition.RIGHT_RING = false;
            //fingerPosition.RIGHT_MIDDLE = false;
            //fingerPosition.RIGHT_INDEX = false;

            System.out.println("Slap Position::" + position);
            System.out.println("Finger Position::" + fingerPosition);

            int retCapture = enroll.StartCapture(minQuality, timeout, position, fingerPosition);
            System.out.println("Start Capture Return Value::" + retCapture);
            sleep(10000);
            // System.out.print
            if (retCapture != 0) {
                System.out.println("Start Capture Error:" + enroll.GetErrorMessage(retCapture));
                return;
            }
        } catch (Exception e) {
            System.out.println("Error in OnScan::" + e);
        }
    }


    @Override
    public void OnDeviceDetection(String DeviceName, DeviceDetection detection) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println("On Device Detection");

        if (detection == DeviceDetection.CONNECTED) {
            //jlbIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Connect.png")));
            //showLogs("Device Name: [" + DeviceName + "] , Status: [ATTACHED]");
            System.out.println("Device Name: [" + DeviceName + "] , Status: [ATTACHED]");

        } else { // DETACHED
            // if (jcbConnectedDevices.getItemCount() <= 0) {
            //   jlbIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Disconnect.png")));
            // showLogs("Device Not Connected");
            System.out.println("Device Not Connected");
        }
        //showDeviceInfo(null);
        //ResetIconLable();

        // }
    }


    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
            /*
        // String model = jcbConnectedDevices.getSelectedItem().toString();
              
        info = new DeviceInfo();
        int ret = enroll.Init(DeviceModel.valueFor(model), info, model);
        if (ret != 0) {
        //    showLogs("Init Device Error: " + ret + " (" + enroll.GetErrorMessage(ret) + ")");
            return;
        } */
            System.out.println("In Initialize");

            //OnScan();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLogs(String str) {
        //log.setText(str);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void OnPreview(int ErrorCode, final PreviewImagPara image) {
        System.out.println("In Preview");
        if (ErrorCode != 0) {
            //showLogs(enroll.GetErrorMessage(ErrorCode));
            System.out.println("Error In Preview");
            return;
        }
        //  try {
        //       new Thread(new Runnable() {
        //          @Override
        //         public void run() {
        try {
            //  synchronized(syncImg) {
            System.out.println("In Preview Sync:");
            InputStream in = new ByteArrayInputStream(image.BoxedBmpImage);
            BufferedImage bufferedImage = ImageIO.read(in);
//                        m_FingerPrintImage.setImage(bufferedImage);
            //    m_FingerPrintImage.setImageParams(image.ImageParams);
            //labelview.setLabel(m_FingerPrintImage);
            WritableImage wr = null;
            if (bufferedImage != null) {
                System.out.println("bufferimg not null");
                wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                PixelWriter pw = wr.getPixelWriter();
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        pw.setArgb(x, y, bufferedImage.getRGB(x, y));
                    }
                }
            }
            // img = wr;
            //}
            ImageView imView = new ImageView(wr);
            System.out.println("IMAGE Appears" + imView.getImage().toString());
            preview.setImage(wr);

        } catch (Exception e) {
            System.out.println("Exception::" + e);
        }
        //Platform.runLater(showPrevData);
        // }
        //    }).start();
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //}
    }

    Runnable showPrevData = new Runnable() {
        public void run() {
            synchronized (syncImg) {
                preview.setImage(img);
                System.out.println("In Preview Sync in sysnc:");
            }
            preview.setImage(img);
            System.out.println("In Preview Sync out sysnc:");

        }
    };

    @Override
    public void OnComplete(int ErrorCode, ImageParams imageParams, FingerList fingerList) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println("In OnComplete");
        System.out.println("Error On Complete:" + ErrorCode);
        System.out.println("Image Param:" + imageParams);
        System.out.println("Finger List:" + fingerList);
        if (ErrorCode != 0) {
            //showLogs(enroll.GetErrorMessage(ErrorCode));
            System.out.println("Error On Complete::" + enroll.GetErrorMessage(ErrorCode));
            return;
        }
        //showLogs("Capture Success");
        System.out.println("Capture Success");

        System.out.println("Image Param::" + imageParams);
        System.out.println("Finger List:::" + fingerList);
        try {
            displayFingers(imageParams, fingerList);
            onSaveImage();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("-----" + ex);

        }
    }

    private void displayFingers(ImageParams imageParams, FingerList fingerList) throws IOException {

        System.out.println("S--- Pos--::" + position.toString());
        int slapposition = 2;
        String left = "LEFT_HAND";
        String right = "RIGHT_HAND";
        String thumb = "THUMB";
        if (position.toString() == left) {
            slapposition = 0;
            System.out.println("AT Left" + slapposition);
        } else if (position.toString() == right) {
            slapposition = 1;
            System.out.println("AT right" + slapposition);
        } else if (position.toString() == thumb) {
            slapposition = 2;
            System.out.println("AT thumb" + slapposition);
        }


        System.out.println("In Display Fingers");
        boolean finger1 = false;
        boolean finger2 = false;
        boolean finger3 = false;
        boolean finger4 = false;

        for (int i = 1; i <= fingerList.FingerCount; i++) {
            try {
                Finger finger = fingerList.Fingers[i];
                ImageInfo imageInfo = imageParams.ImageInfo[i - 1];
                InputStream in = new ByteArrayInputStream(finger.Data);
                BufferedImage bufferedImage = ImageIO.read(in);

                WritableImage wr = null;
                if (bufferedImage != null) {
                    System.out.println("bufferimg not null");
                    wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                    PixelWriter pw = wr.getPixelWriter();
                    for (int x = 0; x < bufferedImage.getWidth(); x++) {
                        for (int y = 0; y < bufferedImage.getHeight(); y++) {
                            pw.setArgb(x, y, bufferedImage.getRGB(x, y));
                        }
                    }
                }

                ImageView imView = new ImageView(wr);
                System.out.println("IMAGE Appears" + imView.getImage().toString());
                //img = imView.getImage();
                //preview.setImage(wr);
                //LICanvas.setImage(wr);
                System.out.println("Slap position:::" + slapposition);
                switch (slapposition) {
                    case 0:
                        if (!finger1) {
                            finger1 = true;
                            LLCanvas.setImage(wr);
                        } else if (!finger2) {
                            finger2 = true;
                            LRCanvas.setImage(wr);

                        } else if (!finger3) {
                            finger3 = true;
                            LMCanvas.setImage(wr);

                        } else if (!finger4) {
                            finger4 = true;
                            LICanvas.setImage(wr);
                        }

                        break;
                    case 1:
                        if (!finger1) {
                            finger1 = true;
                            RLCanvas.setImage(wr);
                        } else if (!finger2) {
                            finger2 = true;
                            RRCanvas.setImage(wr);

                        } else if (!finger3) {
                            finger3 = true;
                            RMCanvas.setImage(wr);

                        } else if (!finger4) {
                            finger4 = true;
                            RICanvas.setImage(wr);
                        }
                        break;
                    case 2:
                        if (!finger1) {
                            finger1 = true;
                            LTCanvas.setImage(wr);

                        } else if (!finger2) {
                            finger2 = true;
                            RTCanvas.setImage(wr);

                        }

                }

            } catch (Exception e) {
                System.out.println("Exception" + e);
            }
        }
    }

    private void onSaveImage() {
        int compressionRatio = 0;
        //String A = "FIR_JPEG2000_V2011";

        ImageFormat format = ImageFormat.JPEG2000;
        //ImageFormat format = ImageFormat.FIR_JPEG2000_V2011;
        /*String B = template.toString();
        if (A == B) {
            if (A.equals("FIR_JPEG2000_V2011"))
                compressionRatio = 10;
        }*/

        //String B = format.toString();
        //if (A == B){
        
        /*if (A.equals("WSQ") || A.equals("RAW") || A.equals("FIR_WSQ_V2011") || A.equals("FIR_JPEG2000_V2011")) {
            compressionRatio = 10;
        }*/
        //compressionRatio =10;
        FingerList fingerList = new FingerList();
        int ret = enroll.GetImage(fingerList, format, compressionRatio);
        if (ret == 0) {
            /*String path = System.getProperty("user.dir") + "//FingerData//";
             File file = new File(path);
             deleteDirectory(file);*/
            System.out.println("Finger List Finger:::" + fingerList.FingerCount);
            for (int i = 0; i <= fingerList.FingerCount; i++) {
                
                /*
                String extension = format.toString().toLowerCase();
                if (format.toString().contains("JPEG2000")) {
                    extension = "jp2";
                }/*
                if (format.toString().contains("FIR")) {
                    extension = "iso";
                }*/
                String fileName = "";
                int slapposition = 3;
                String left = "LEFT_HAND";
                String right = "RIGHT_HAND";
                String thumb = "THUMB";
                if (position.toString() == left) {
                    slapposition = 0;
                    System.out.println("AT Left" + slapposition);
                } else if (position.toString() == right) {
                    slapposition = 1;
                    System.out.println("AT right" + slapposition);
                } else if (position.toString() == thumb) {
                    slapposition = 2;
                    System.out.println("AT thumb" + slapposition);
                }
                //int slapPos = position.compareTo(position);
                switch (i) {
                   /* case 0:
                        
                        if (slapposition == 0 ) {
                            fileName = "0_LH";
                            String finger="LH";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);                           
                            saveFingerprintDetails(finger,image,template);
                            
                       } 
                        else if (slapposition == 1 ){
                                fileName = "0_RH";
                                String finger="RH";
                                String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                                String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                                saveFingerprintDetails(finger,image,template);
                                               
                          
                        }else {
                                fileName = "0_THUMBS";
                                String finger="T";
                                String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                                String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);                           
                                saveFingerprintDetails(finger,image,template);
                                               
                        }
                        break; */
                    case 1:
                        if (slapposition == 0) {
                            fileName = "1_LL";
                            String finger = "LL";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            System.out.println("In LL:" + image);
                            saveFingerprintDetails(finger, image, template);
                        } else if (slapposition == 1) {
                            fileName = "1_RI";
                            String finger = "RI";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            saveFingerprintDetails(finger, image, template);

                        } else {
                            fileName = "1_LT";
                            String finger = "LT";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);

                            saveFingerprintDetails(finger, image, template);

                        }
                        break;
                    case 2:
                        if (slapposition == 0) {
                            fileName = "2_LR";
                            String finger = "LR";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            System.out.println("In LR:" + image);
                            saveFingerprintDetails(finger, image, template);
                        } else if (slapposition == 1) {
                            fileName = "2_RM";
                            String finger = "RM";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            saveFingerprintDetails(finger, image, template);

                        } else {

                            fileName = "2_RT";
                            String finger = "RT";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);

                            System.out.println("Finger Value" + finger);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            System.out.println("Finger Value" + template);
                            saveFingerprintDetails(finger, image, template);
                        }
                        break;
                    case 3:
                        if (slapposition == 0) {
                            fileName = "3_LM";
                            String finger = "LM";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);

                            saveFingerprintDetails(finger, image, template);
                        } else {
                            fileName = "3_RR";
                            String finger = "RR";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            saveFingerprintDetails(finger, image, template);

                        }
                        /*} else {
                           
                        }*/
                        break;
                    case 4:
                        if (slapposition == 0) {
                            fileName = "4_LI";
                            String finger = "LI";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            saveFingerprintDetails(finger, image, template);

                        } else {
                            fileName = "4_RL";
                            String finger = "RL";
                            String image = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            String template = Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data);
                            saveFingerprintDetails(finger, image, template);
                        }
                        break;
                }
                
                /*
                int fingerpos;
              //  Bitmap 
               fingerpos= fingerList.Fingers[i].hashCode();
               System.out.println("Fingerpos"+fingerpos);
               InputStream in = new ByteArrayInputStream(fingerList.Fingers[i].Data);
               //System.out.println("Image::::"+in);
               //System.out.println("Image1111::::"+Base64.getEncoder().encodeToString(fingerList.Fingers[i].Data));
               InputStream t = new ByteArrayInputStream(fingerList.Fingers[i].Data);
              //  BufferedImage image = ImageIO.read(in);
               
              // Image= fingerList.Fingers[i].Data;
                //WriteImageFile(format.toString(), fileName + format.toString() + "." + extension,fingerList.Fingers[i].Data);
                //System.out.println(format.toString() + " Generated");
                */
            }
        } else {
            // showLogs(format.toString() + " Generated error code: " + ret + "
            // (" + enroll.GetErrorMessage(ret) + ")");
            System.out.println(enroll.GetErrorMessage(ret));
        }

    }// GEN-LAST:event_jbtnSaveImageActionPerformed

    public void saveFingerprintDetails(String fingerpos, String in, String t) {

        FP fplt = new FP();
        fplt.setPosition(fingerpos);
        // fplt.setTemplate(Base64.getEncoder().encodeToString(template2011));
        //fplt.setImage(Base64.getEncoder().encodeToString(image));
        fplt.setImage(in);
        fplt.setTemplate(t);
        fingerPrintSet.add(fplt);
        //System.out.println("com.mantra.enrollmenttry.FingerprintController.saveFingerprintDetails()");
        // System.out.println("Position :::"+fplt.getPosition());
        // System.out.println("Image:::"+fplt.getImage());
        // System.out.println("Template::"+fplt.getTemplate());
        //fplt.toString();


    }


 /*
 
 private void WriteImageFile(String folderName, String filename, byte[] bytes) {
        try {
            // String chack = jcbSlapSelection.getSelectedItem().toString();
            // String path = System.getProperty("user.dir") + "//FingerData//" +
            // chack + "//" + folderName;
            String path = System.getProperty("user.dir") + "//FingerData//";
            File file = new File(path);

            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + "//" + filename;
            file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(bytes);
            stream.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
*/

    @FXML
    private void showIris() {
        try {
            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
            ARCDetails a = holder.getArcDetails();
            SaveEnrollmentDetails saveEnrollment = holder.getSaveEnrollmentDetails();
            saveEnrollment.setLeftFPScannerSerialNo("11323");
            saveEnrollment.setRightFPScannerSerialNo("123232");

            saveEnrollment.setFp(fingerPrintSet);
            //saveEnrollment.setEnrollmentStatus("FingerPrint Capture Completed");
            saveEnrollment.setEnrollmentStatus("FingerPrintCompleted");
            System.out.println("Finger print capture completed");

            holder.setSaveEnrollmentDetails(saveEnrollment);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setBase64Variant(Base64Variants.MIME_NO_LINEFEEDS);

            String postJson;
            try {
                postJson = mapper.writeValueAsString(saveEnrollment);

                //Code Added by K. Karthikeyan - 18-4-22 - Start
                ObjectReaderWriter objReadWrite = new ObjectReaderWriter();
                objReadWrite.writer(saveEnrollment);
                System.out.println("Save Enrollment Object write");
                SaveEnrollmentDetails s = objReadWrite.reader();
                System.out.println("Enrollment Status " + s.getEnrollmentStatus());
                //Code Added by K. Karthikeyan - 18-4-22 - Finish

                //System.out.println("post json slap :"+ postJson);
            } catch (JsonProcessingException ex) {
                //Logger.getLogger(SlapScannerController.class.getName()).log(Level.SEVERE, null, ex);
                //LOGGER.log(Level.SEVERE, "Error: "+ ex.getMessage());
                //statusField.setText("Fetched Details From Server has Error");
                System.out.println("Error: " + ex.getMessage());

            }


            App.setRoot("iris");
        } catch (IOException ex) {
            System.out.println("Exception::" + ex);
            //Logger.getLogger(SlapScannerController.class.getName()).log(Level.SEVERE, null, ex);
            // LOGGER.log(Level.SEVERE, "Error: "+ ex.getMessage());
        }
    }

}
