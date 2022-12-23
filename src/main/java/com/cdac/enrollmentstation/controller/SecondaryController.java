package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecondaryController {

    @FXML
    private void showARCInput() {
        try {
            App.setRoot("enrollment_arc");
        } catch (IOException ex) {
            Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}