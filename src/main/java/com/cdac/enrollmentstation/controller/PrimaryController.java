package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

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
    private BorderPane rootBorderPane;

    public void initialize() {
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
    }

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
        if ("1".equals(PropertyFile.getProperty(PropertyName.INITIAL_SETUP).trim())) {
            App.setRoot("hostname_ip");
        } else {
            App.setRoot("admin_auth");
        }
    }


    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.SEVERE, "***Unhandled exception occurred.");
    }
}
