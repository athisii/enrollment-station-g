///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cdac.enrollmentstation.controller;
//
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.model.ARCDetails;
//import com.cdac.enrollmentstation.model.ARCDetailsHolder;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.ResourceBundle;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * FXML Controller class
// *
// * @author root
// */
//public class BiometricCaptureCompleteController1 implements Initializable {
//
//    /**
//     * Initializes the controller class.
//     */
//    @FXML
//    private Label statusMessage;
//
//    @FXML
//    private ImageView statusImg;
//
//
//    @FXML
//    private void homescreen() {
//
//        try {
//            App.setRoot("first_screen");
//        } catch (IOException ex) {
//            Logger.getLogger(BiometricCaptureCompleteController1.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//        // TODO
//        System.out.println(" init :");
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//        ARCDetails a = holder.getArcDetails();
//        statusMessage.setText(a.getDesc());
//        //System.out.println(a.getArcstatus().contains("refused") + " " + a.getArcstatus().contains("notreachable"));
//        if (a.getDesc().contains("refused") || a.getDesc().contains("notreachable")) {
//            Image image = new Image("/haar_facedetection/redcross.png");
//            statusImg.setImage(image);
//        } else {
//            Image image = new Image("/haar_facedetection/tickgreen.jpg");
//            statusImg.setImage(image);
//        }
//
//
//    }
//
//}
