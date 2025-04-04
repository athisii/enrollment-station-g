package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class AdminConfigController extends AbstractBaseController {
    //For Application Log
    private static final Logger LOGGER = ApplicationLog.getLogger(AdminConfigController.class);
    private static final int FINGERPRINT_LIVENESS_MAX;
    private static final int FINGERPRINT_LIVENESS_MIN;
    private final int fingerprintLivenessValue = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_VALUE).trim());
    private final int nfiqValue = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_NFIQ_VALUE).trim());

    static {
        try {
            FINGERPRINT_LIVENESS_MAX = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_MAX).trim());
            FINGERPRINT_LIVENESS_MIN = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_MIN).trim());
        } catch (NumberFormatException ex) {
            throw new GenericException("Invalid max or min fingerprint liveness value. It must be a number.");
        }
    }

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private TextField nfiqTextField;
    @FXML
    private Button nfiqBtn;

    @FXML
    private Label messageLabel;
    @FXML
    private TextField liveFpTextField;
    @FXML
    private Button liveFpBtn;

    /**
     * Automatically called by JavaFX runtime.
     */
    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

        liveFpTextField.setText(String.valueOf(fingerprintLivenessValue));
        liveFpBtn.setOnAction(event -> liveFpBtnAction());

        nfiqBtn.setOnAction(event -> nfiqBtnAction());
        nfiqTextField.setText(String.valueOf(nfiqValue));
    }

    private void nfiqBtnAction() {
        // check how text on edit button should be displayed
        if (nfiqTextField.isEditable()) {
            String inputValue = nfiqTextField.getText();
            String displayMessage = "Please enter a number from 1 to 5.";
            if (inputValue.isBlank()) {
                messageLabel.setText(displayMessage);
                return;
            }
            int number;
            try {
                number = Integer.parseInt(inputValue);
                if (number < 1 || number > 5) {
                    throw new NumberFormatException("Invalid NFIQ value.");
                }
            } catch (NumberFormatException ex) {
                nfiqTextField.setText("");
                messageLabel.setText(displayMessage);
                return;
            }
            PropertyFile.changePropertyValue(PropertyName.FP_NFIQ_VALUE, String.valueOf(number));
            nfiqBtn.setText("EDIT"); // shows as edit
            messageLabel.setText("NFIQ value updated successfully.");
        } else {
            messageLabel.setText("");
            nfiqBtn.setText("UPDATE");
        }
        // toggles the edit-ability
        nfiqTextField.setEditable(!nfiqTextField.isEditable());

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
    public void deOnboardAction() throws IOException {
        App.setRoot("de_onboard");
    }


    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        updateUi("Something went wrong. Please try again");
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }
}
