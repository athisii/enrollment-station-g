package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ARCDetails;
import com.cdac.enrollmentstation.model.ARCDetailsHolder;
import com.cdac.enrollmentstation.model.SaveEnrollmentDetails;
import com.cdac.enrollmentstation.util.SaveEnrollmentDetailsUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class ARCNoController {
    private static final Logger LOGGER = ApplicationLog.getLogger(ARCNoController.class);
    private static final int INACTIVE_TIME_IN_SEC = 10;


    // *****************************BARCODE SCANNER *************************
    private static final int MIN_ARC_LENGTH = 12; // 00001-A-AA23
    private static final int KEY_PRESSED_EVENT_GAP_THRESHOLD = 30;
    private long lastEventTimeStamp = 0L;
    private static final StringBuilder barcodeStringBuilder = new StringBuilder();
    // *
    private String tempArc;

    private Timeline refocusArcInputTimeline;

    @FXML
    public Button continueBtn;
    @FXML

    public Button backBtn;
    @FXML
    public Button showArcBtn;

    @FXML
    private TextField arcNumberTextField;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtRank;
    @FXML
    private TextField txtApp;
    @FXML
    private TextField txtUnit;
    @FXML
    private TextField txtFinger;
    @FXML
    private TextField txtIris;
    @FXML
    private TextField txtArcStatus;

    @FXML
    private TextField txtBiometricOptions;

    public void initialize() {
        backBtn.setOnAction(event -> backBtnAction());
        showArcBtn.setOnAction(event -> showArcDetails());
        continueBtn.setOnAction(event -> continueBtnAction());

        arcNumberTextField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                showArcDetails();
            }
        });

        refocusArcInputTimeline = new Timeline(new KeyFrame(Duration.seconds(INACTIVE_TIME_IN_SEC), event -> {
            showArcBtn.requestFocus();
        }));
        refocusArcInputTimeline.setCycleCount(1);
        refocusArcInputTimeline.playFromStart();
        arcNumberTextField.setOnKeyReleased(event -> refocusArcInputTimeline.playFromStart());
        arcNumberTextField.setOnKeyPressed(event -> refocusArcInputTimeline.stop());
    }

    private void continueBtnAction() {
        ARCDetails arcDetails = ARCDetailsHolder.getArcDetailsHolder().getArcDetails();
        if (arcDetails.getBiometricOptions().trim().equalsIgnoreCase("photo")) {
            try {
                App.setRoot("camera");
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, ex.getMessage());
            }
        } else if (arcDetails.getBiometricOptions().trim().equalsIgnoreCase("both") || arcDetails.getBiometricOptions().trim().equalsIgnoreCase("biometric")) {
            setNextScreen();
        } else {
            messageLabel.setText("Biometric capturing not required for e-ARC number: " + tempArc);
            LOGGER.log(Level.INFO, "Biometric capturing not required for given e-ARC Number");
        }

    }

    private void setNextScreen() {
        SaveEnrollmentDetails saveEnrollmentDetails;
        try {
            saveEnrollmentDetails = SaveEnrollmentDetailsUtil.readFromFile();
        } catch (GenericException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            messageLabel.setText(GENERIC_ERR_MSG);
            return;
        }

        // different e-ARC number is entered.
        if (saveEnrollmentDetails.getArcNo() == null || !ARCDetailsHolder.getArcDetailsHolder().getArcDetails().getArcNo().equals(saveEnrollmentDetails.getArcNo())) {
            try {
                App.setRoot("slap_scanner");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
        } else {
            // same e-ARC number is entered as the one saved in saveEnrollment.txt file.
            ARCDetailsHolder.getArcDetailsHolder().setSaveEnrollmentDetails(saveEnrollmentDetails);
            changeScreenBasedOnEnrollmentStatus();
        }

    }

    private void changeScreenBasedOnEnrollmentStatus() {
        switch (ARCDetailsHolder.getArcDetailsHolder().getSaveEnrollmentDetails().getEnrollmentStatus()) {
            case "FingerPrintCompleted":
                try {
                    App.setRoot("iris");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                }
                break;

            case "IrisCompleted":
                try {
                    App.setRoot("camera");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                }
                break;
            case "PhotoCompleted":

            case "SUCCESS":
                try {
                    App.setRoot("biometric_capture_complete");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());

                }
                break;
            default:
                try {
                    App.setRoot("slap_scanner");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                }
                break;
        }
    }

    @FXML
    private void backBtnAction() {
        try {
            App.setRoot("main_screen");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, () -> "Error loading fxml: " + ex.getMessage());
        }
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    private void showArcDetails() {
        tempArc = arcNumberTextField.getText().trim();
        if (isMalformedArc()) {
            messageLabel.setText("Kindly enter correct e-ARC number.");
            refocusArcInputTimeline.playFromStart();
            return;
        }
        disableControls(showArcBtn);
        // fetches e-ARC in worker thread.
        new Thread(() -> {
            ARCDetails arcDetails;
            try {
                disableControls(backBtn, showArcBtn);
                arcDetails = MafisServerApi.fetchARCDetails(tempArc);
            } catch (GenericException ex) {
                enableControls(backBtn, showArcBtn);
                updateUiTextFields(null);
                updateUi(GENERIC_ERR_MSG);
                return;
            }

            // connection timeout
            if (arcDetails == null) {
                enableControls(backBtn, showArcBtn);
                LOGGER.log(Level.INFO, "Connection timeout");
                updateUiTextFields(null);
                updateUi("Connection timeout. Please try again.");
                return;
            }

            if (!"0".equals(arcDetails.getErrorCode())) {
                LOGGER.log(Level.INFO, () -> "Error Desc: " + arcDetails.getDesc());
                enableControls(backBtn, showArcBtn);
                updateUiTextFields(null);
                if (arcDetails.getDesc().toLowerCase().contains("not found")) {
                    updateUi("Details not found for e-ARC: " + tempArc);
                } else {
                    updateUi(arcDetails.getDesc());
                }
                return;
            }

            if (arcDetails.getBiometricOptions() == null || arcDetails.getBiometricOptions().isBlank() || arcDetails.getBiometricOptions().trim().equalsIgnoreCase("none")) {
                enableControls(backBtn, showArcBtn);
                LOGGER.log(Level.INFO, () -> "Biometric capturing not required for e-ARC: " + tempArc);
                updateUiTextFields(arcDetails);
                updateUi("Biometric capturing not required for e-ARC: " + tempArc);
                return;
            }

            updateUiTextFields(arcDetails);
            updateUi("");

            ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
            holder.setArcDetails(arcDetails);
            SaveEnrollmentDetails saveEnrollmentDetails = new SaveEnrollmentDetails();

            try {
                saveEnrollmentDetails.setEnrollmentStationUnitID(MafisServerApi.getEnrollmentStationUnitId());
                saveEnrollmentDetails.setEnrollmentStationID(MafisServerApi.getEnrollmentStationId());
            } catch (GenericException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                updateUi(GENERIC_ERR_MSG);
                return;
            }

            saveEnrollmentDetails.setArcNo(arcDetails.getArcNo());
            saveEnrollmentDetails.setBiometricOptions(arcDetails.getBiometricOptions());
            holder.setSaveEnrollmentDetails(saveEnrollmentDetails);
            continueBtn.setDisable(false);
            backBtn.setDisable(false);
            showArcBtn.setDisable(false);
            refocusArcInputTimeline.playFromStart();
        }).start();

        messageLabel.setText("Please wait. Fetching details for e-ARC: " + tempArc);
    }

    private void updateUiTextFields(ARCDetails arcDetails) {
        Platform.runLater(() -> {
            if (arcDetails == null) {
                clearLabelText(txtName, txtRank, txtApp, txtUnit, txtFinger, txtIris, txtBiometricOptions, txtArcStatus);
                return;
            }
            txtName.setText(arcDetails.getName());
            txtRank.setText(arcDetails.getRank());
            txtApp.setText(arcDetails.getApplicantID());
            txtUnit.setText(arcDetails.getUnit());
            if (arcDetails.getFingers().isEmpty()) {
                txtFinger.setText("NA");
            } else {
                txtFinger.setText(String.join(",", arcDetails.getFingers()));
            }
            if (arcDetails.getIris().isEmpty()) {
                txtIris.setText("NA");
            } else {
                txtIris.setText(String.join(",", arcDetails.getIris()));
            }
            txtBiometricOptions.setText(arcDetails.getBiometricOptions());
            txtArcStatus.setText(arcDetails.getArcStatus());
        });
    }

    private void clearLabelText(TextField... textFields) {
        for (TextField label : textFields) {
            label.setText("");
        }
    }

    private boolean isMalformedArc() {
        // 00001-A-AA01
        return tempArc.split("-").length != 3;
    }

    private void disableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    private void enableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    @FXML
    public void keyTyped(KeyEvent keyEvent) {
        long now = Instant.now().toEpochMilli();
        // events must come fast enough to separate from manual input
        if (now - lastEventTimeStamp > KEY_PRESSED_EVENT_GAP_THRESHOLD) {
            barcodeStringBuilder.setLength(0);
        }
        barcodeStringBuilder.append(keyEvent.getCharacter());
        if (keyEvent.getCode().equals(KeyCode.ENTER) || barcodeStringBuilder.length() >= MIN_ARC_LENGTH) {
            LOGGER.log(Level.INFO, () -> "Barcode e-ARC: " + barcodeStringBuilder);
            arcNumberTextField.setText(barcodeStringBuilder.toString());
            showArcBtn.requestFocus();
            showArcDetails();
            barcodeStringBuilder.setLength(0);
        }
        lastEventTimeStamp = now;
    }
}
