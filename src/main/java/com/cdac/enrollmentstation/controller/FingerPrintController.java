/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author boss
 */
public class FingerPrintController {

    @FXML
    private void showIris() {
        try {
            App.setRoot("iris");
        } catch (IOException ex) {
            Logger.getLogger(FingerPrintController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void showARCInput() {
        try {
            App.setRoot("enrollment_arc");
        } catch (IOException ex) {
            Logger.getLogger(FingerPrintController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
