package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class LicenceInfoController implements BaseController {
    @FXML
    private Button homeBtn;
    @FXML
    private Button backBtn;
    @FXML
    private Button reloadBtn;
    @FXML
    private Label messageLabel;
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
            messageLabel.setText(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        backBtn.setDisable(false);
        reloadBtn.setDisable(false);
        homeBtn.setDisable(false);
        updateUi("Something went wrong. Please try again.");
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }
}
        
      