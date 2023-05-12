
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class AdminConfigController {
    //For Application Log
    private static final Logger LOGGER = ApplicationLog.getLogger(AdminConfigController.class);
    private static final int FINGERPRINT_LIVENESS_MAX;
    private static final int FINGERPRINT_LIVENESS_MIN;
    private final int fingerprintLivenessValue = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_VALUE).trim());

    static {
        try {
            FINGERPRINT_LIVENESS_MAX = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_MAX).trim());
            FINGERPRINT_LIVENESS_MIN = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_MIN).trim());
        } catch (NumberFormatException ex) {
            throw new GenericException("Invalid max or min fingerprint liveness value. It must be a number.");
        }
    }

    @FXML
    public Label messageLabel;
    @FXML
    private TextField liveFpTextField;
    @FXML
    private Button liveFpBtn;

    @FXML
    private AnchorPane confirmPane;

    @FXML
    private ComboBox<String> cameraComboBox;


    /**
     * Automatically called by JavaFX runtime.
     */
    public void initialize() {
//        cameraComboBox.getItems().addAll(ApplicationConstant.INTERNAL, ApplicationConstant.EXTERNAL);
//        // Internal: value = 0; index = 0
//        // External: value = 2; index = 1
//        int cameraId = Integer.parseInt(PropertyFile.getProperty(PropertyName.CAMERA_ID).trim());
//        // default value to display
//        cameraComboBox.getSelectionModel().select(cameraId == 0 ? 0 : 1);
//        cameraComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            int camId = ApplicationConstant.EXTERNAL.equalsIgnoreCase(newValue) ? 2 : 0;
//            PropertyFile.changePropertyValue(PropertyName.CAMERA_ID, "" + camId);
//            Platform.runLater(() -> messageLabel.setText((camId == 0 ? ApplicationConstant.INTERNAL : ApplicationConstant.EXTERNAL) + " Camera Selected"));
//            LOGGER.log(Level.INFO, PropertyFile.getProperty(PropertyName.CAMERA_ID));
//        });
//        liveFpTextField.setText(String.valueOf(fingerprintLivenessValue));
//        liveFpBtn.setOnAction(event -> liveFpBtnAction());

    }

    private void liveFpBtnAction() {
        // check how text on edit button should be displayed
        if (liveFpTextField.isEditable()) {
            String inputValue = liveFpTextField.getText();
            String displayMessage = "Please enter a number between " + FINGERPRINT_LIVENESS_MIN + " and " + FINGERPRINT_LIVENESS_MAX;
            if (inputValue.isBlank()) {
                messageLabel.setText(displayMessage);
                return;
            }
            int number;
            try {
                number = Integer.parseInt(inputValue);
                if (number < FINGERPRINT_LIVENESS_MIN || number > FINGERPRINT_LIVENESS_MAX) {
                    throw new NumberFormatException("Invalid fingerprint value");
                }
            } catch (NumberFormatException ex) {
                liveFpTextField.setText("");
                messageLabel.setText(displayMessage);
                return;
            }
            PropertyFile.changePropertyValue(PropertyName.FINGERPRINT_LIVENESS_VALUE, number + "");
            liveFpBtn.setText("EDIT"); // shows as edit
            messageLabel.setText("Fingerprint liveness value updated successfully.");
        } else {
            messageLabel.setText("");
            liveFpBtn.setText("UPDATE");
        }
        // toggles the edit-ability
        liveFpTextField.setEditable(!liveFpTextField.isEditable());
    }

    @FXML
    public void serverConfig() {
        try {
            App.setRoot("server_config");
        } catch (IOException ex) {
            Logger.getLogger(AdminConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }
    }

    @FXML
    public void licenseInfo() {
        LOGGER.log(Level.INFO, "License Info button clicked");
        try {
            App.setRoot("license_info");
        } catch (IOException ex) {
            Logger.getLogger(AdminConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }

    }

    @FXML
    public void devicecheck() {
        try {
            App.setRoot("device_status");
        } catch (IOException ex) {
            Logger.getLogger(AdminConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, () -> ex + "IOException:");
        }
    }


    @FXML
    public void logOut() {
        try {
            App.setRoot("admin_auth");
        } catch (IOException ex) {
            Logger.getLogger(AdminConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }
    }

    @FXML
    public void initialiseintegrity() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "echo \"true\" | sudo tee /etc/baseline");
            Process process = null;
            try {
                process = processBuilder.start();
                int exitCode = process.waitFor();
                messageLabel.setText("Integrity Check Initialized");
                LOGGER.log(Level.INFO, "\nExited with error code : " + exitCode);
                LOGGER.log(Level.INFO, "Integrity Check Initialized");
            } catch (IOException ex) {
                Logger.getLogger(AdminConfigController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO, ex + "IOException:");
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception:" + e);
        }

    }

    @FXML
    public void restartsystem() {

        confirmPane.setVisible(true);

    }

    @FXML
    private void restart() {
        restartSys();
    }

    @FXML
    private void stayBack() {
        LOGGER.log(Level.INFO, "inside stay back");
        confirmPane.setVisible(false);

    }

    private void restartSys() {
        LOGGER.log(Level.INFO, "restartsystem");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "init 6");
            Process process = null;
            try {
                process = processBuilder.start();
                int exitCode = process.waitFor();
                messageLabel.setText("System Reboot");
                LOGGER.log(Level.INFO, "System Reboot");
                LOGGER.log(Level.INFO, "\nExited with error code : " + exitCode);
            } catch (IOException ex) {
                Logger.getLogger(AdminConfigController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO, ex + "IOException:");
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, e + "Exception:");
        }
    }

}
