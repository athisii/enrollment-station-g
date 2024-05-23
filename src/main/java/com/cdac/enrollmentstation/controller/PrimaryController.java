package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class PrimaryController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(PrimaryController.class);


    @FXML
    private void showEnrollmentHome() {
        try {
            App.setRoot("biometric_enrollment");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }

    }

    @FXML
    private void showContract() {
        try {
            App.setRoot("token_issuance");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }

    }

    @FXML
    public void onSettings() throws IOException {
        App.setRoot("admin_auth");
    }


    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.SEVERE, "***Unhandled exception occurred.");
    }
}
