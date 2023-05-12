package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LicenceInfoController {
    @FXML
    private Label statusLabel;
    @FXML
    private TextField finScannerInfo;
    private static final Logger LOGGER = ApplicationLog.getLogger(LicenceInfoController.class);


    public void initialize() {
        fetchLicenceDetails();
    }

    @FXML
    public void showHome() {
        try {
            App.setRoot("main_screen");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
        }
    }

    @FXML
    public void back() {
        try {
            App.setRoot("admin_config");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
        }
    }

    @FXML
    private void fetchLicenceDetails() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "/usr/share/enrollment/ansi/license_manager_cli -p");
        try {
            finScannerInfo.setText("");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                finScannerInfo.setText(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.SEVERE, ex.getMessage());
            statusLabel.setText(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }
}
        
      