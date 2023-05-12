
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    /**
     * Automatically called by JavaFX runtime.
     */
    public void initialize() {
        liveFpTextField.setText(String.valueOf(fingerprintLivenessValue));
        liveFpBtn.setOnAction(event -> liveFpBtnAction());

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
            PropertyFile.changePropertyValue(PropertyName.FINGERPRINT_LIVENESS_VALUE, String.valueOf(number));
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
    public void serverConfig() throws IOException {
        App.setRoot("server_config");
    }

    @FXML
    public void licenseInfo() throws IOException {
        App.setRoot("license_info");
    }

    @FXML
    public void deviceCheck() throws IOException {
        App.setRoot("device_status");
    }


    @FXML
    public void logOut() throws IOException {
        App.setRoot("admin_auth");

    }

    @FXML
    public void restartSystem() {
        confirmPane.setVisible(true);
    }

    @FXML
    private void restart() {
        restartSys();
    }

    @FXML
    private void stayBack() {
        confirmPane.setVisible(false);
    }

    private void restartSys() {
        try {
            LOGGER.log(Level.INFO, "System restarting..");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "init 6");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            LOGGER.log(Level.INFO, () -> "Exited with error code : " + exitCode);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.INFO, ex.getMessage());
        }
    }

}
