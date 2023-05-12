package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrimaryController implements Initializable {
    @FXML
    private Label version;

    private static final String VERSION_NUMBER = "1.2";

    @FXML
    private void showEnrollmentHome() {
        try {
            App.setRoot("enrollment_arc");
        } catch (IOException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void showContract() {
        try {
            App.setRoot("show_token");
        } catch (IOException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void onSettings() throws IOException {
        App.setRoot("admin_auth");
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        version.setText(VERSION_NUMBER);
    }


}
