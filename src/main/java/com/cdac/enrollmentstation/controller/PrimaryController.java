package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrimaryController implements Initializable {


    @FXML
    private TextField irisInit;
    @FXML
    private TextField cameraInit;
    @FXML
    private TextField slapInit;

    @FXML
    private Label version;

    private String versionno = "1.2";

    @FXML
    private void showEnrollmentHome() {

        try {
            // App.setRoot("second_screen");
            App.setRoot("enrollment_arc");
            //App.setRoot("slapscanner_1");
        } catch (IOException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void showContract() {
        try {
            //App.setRoot("list_contract");

            App.setRoot("n_showtoken");
        } catch (IOException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void showPasswdScreen() {
        try {
            App.setRoot("enterpassword");
        } catch (IOException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @FXML
    public void OnSettings() throws IOException {
        System.out.println("In onsettings");
        App.setRoot("enterpassword");
        System.out.println("In onsettings1");
    }

    @FXML
    public void deviceStatus() {
        //Initialize IRIS

//        mIDIrisEnroll = new MIDIrisEnroll(this);
//        String version = mIDIrisEnroll.GetSDKVersion();
//        System.out.println("sdk version :"+ version); 
//     

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        version.setText(versionno);
    }


}
