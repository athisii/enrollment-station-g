package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class PrimaryController implements BaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(PrimaryController.class);

    @FXML
    private Label version;

    @FXML
    private void showEnrollmentHome() {
        try {
            App.setRoot("biometric_enrollment");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
        }

    }

    @FXML
    private void showContract() {
        try {
            App.setRoot("token_issuance");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
        }

    }

    @FXML
    public void onSettings() throws IOException {
        App.setRoot("admin_auth");
    }

    public void initialize() {
        String appVersionNumber = PropertyFile.getProperty(PropertyName.APP_VERSION_NUMBER);
        if (appVersionNumber == null || appVersionNumber.isEmpty()) {
            LOGGER.log(Level.SEVERE, () -> "No entry for '" + PropertyName.APP_VERSION_NUMBER + "' or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException("No entry for '" + PropertyName.APP_VERSION_NUMBER + "' or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }
        version.setText(appVersionNumber);
    }


    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.SEVERE, "***Unhandled exception occurred.");
    }
}
