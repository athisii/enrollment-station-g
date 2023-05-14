///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cdac.enrollmentstation.model;
//
//import com.cdac.enrollmentstation.controller.IrisController;
//import com.mantra.midirisenroll.DeviceInfo;
//import com.mantra.midirisenroll.MIDIrisEnroll;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.ToString;
//import lombok.experimental.FieldDefaults;
//
///**
// * @author root
// */
//@Getter
//@Setter
//@ToString
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class IrisInitialize {
//    MIDIrisEnroll mIDIrisEnroll = null;
//    DeviceInfo deviceInfo = null;
//    String deviceName = null;
//    int minQuality = 30;
//    int timeout = 10000;
//    IrisController.MyIcon mLeftIrisImage;
//    IrisController.MyIcon mRightIrisImage;
//    int retInit;
//
////    @Override
////    public void OnDeviceDetection(String DeviceName, IrisSide irisSide, DeviceDetection detection) {
////         if (detection == DeviceDetection.CONNECTED) {
////          System.out.println(DeviceName + " Connected");
////          deviceName = DeviceName;
////
////          System.out.println("DEvice Name:"+deviceName );
////          //  jcbConnectedDevices.addItem(DeviceName);
////           //lblerror.setText(DeviceName + " Connected88");
////        }
////        else { //DETACHED
////
//////            if (jcbConnectedDevices.getItemCount() <= 0) {
//////                jlbIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Disconnect.png")));
//////            }
////            if (deviceInfo != null) {
////                if (deviceInfo.Model.equals(DeviceName)) {
////
////                    deviceInfo = null;
////                }
////            }
////        }
////      // mIDIrisEnroll.DeviceCallback(DeviceName, timeout, timeout);
////    }
////
////    @Override
////    public void OnPreview(int ErrorCode, ImageQuality imageQuality, final ImagePara imagePara) {
////        try {
////            //showLogs("Capture Success");
//////            jblIrisLeft.setText("Quality: " + imageQuality.LeftIrisQuality);
//////            jblIrisRight.setText("Quality: " + imageQuality.RightIrisQuality);
////            IrisController_withoutbiometric iriscontroller = new IrisController_withoutbiometric();
////
////            if (imagePara.LeftImageBufferLen > 0) {
////                iriscontroller.displayImage(imagePara.LeftImageBuffer, imageQuality.LeftIrisQuality,
////                        imageQuality.LeftIrisX, imageQuality.LeftIrisY, imageQuality.LeftIrisR, iriscontroller.lefticon, mLeftIrisImage);
////
////            }
////
////            if (imagePara.RightImageBufferLen > 0) {
////                iriscontroller.displayImage(imagePara.RightImageBuffer, imageQuality.RightIrisQuality,
////                        imageQuality.RightIrisX, imageQuality.RightIrisY, imageQuality.RightIrisR, iriscontroller.righticon, mRightIrisImage);
////
////            }
////        } catch (Exception ex) {
////            ex.printStackTrace();
////        }
////        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
////
////    @Override
////    public void OnComplete(int ErrorCode, ImageQuality imageQuality, final ImagePara imagePara) {
////    try {
//////            showLogs("Capture Success");
//////            jblIrisLeft.setText("Quality: " + imageQuality.LeftIrisQuality);
//////            jblIrisRight.setText("Quality: " + imageQuality.RightIrisQuality);
////            IrisController_withoutbiometric irisController = new IrisController_withoutbiometric();
////             if(imagePara== null) {
////                System.out.println("stream empty");
////                irisController.irisCapturedLeft = false;
////                irisController.irisCapturedRight = false;
////                Platform.runLater(new Runnable() {
////                        @Override public void run() {
////                            Image image = new Image("/haar_facedetection/red_cross.png");
////                            irisController.camera.setDisable(false);
////                            irisController.statusImage.setImage(image);
////                    }
////                });
////
////                return;
////            }
////
////            if (imagePara.LeftImageBufferLen > 0) {
////                irisController.irisCapturedLeft = true;
////                irisController.displayImageComplete(imagePara.LeftImageBuffer, imageQuality.LeftIrisQuality,
////                        imageQuality.LeftIrisX, imageQuality.LeftIrisY, imageQuality.LeftIrisR, irisController.lefticon, mLeftIrisImage);
////                irisController.leftIrisImage = imagePara.LeftImageBuffer;
////            }
////
////            if (imagePara.RightImageBufferLen > 0) {
////                irisController.irisCapturedRight = true;
////                irisController.displayImageComplete(imagePara.RightImageBuffer, imageQuality.RightIrisQuality,
////                        imageQuality.RightIrisX, imageQuality.RightIrisY, imageQuality.RightIrisR, irisController.righticon, mRightIrisImage);
////                irisController.rightIrisImage = imagePara.RightImageBuffer;
////            }
////        } catch (Exception ex) {
////            ex.printStackTrace();
////        }
////    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
////
//
//}
