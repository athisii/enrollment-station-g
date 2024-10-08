package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.dto.ArcDetail;
import com.cdac.enrollmentstation.dto.SaveEnrollmentDetail;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ArcDetailsHolder;
import com.cdac.enrollmentstation.util.SaveEnrollmentDetailUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;
import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class BiometricEnrollmentController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(BiometricEnrollmentController.class);

    // *****************************BARCODE SCANNER *************************
    private static final int MIN_ARC_LENGTH = 12; // 00001-A-AA23
    private static final int KEY_PRESSED_EVENT_GAP_THRESHOLD = 30;
    private long lastEventTimeStamp = 0L;
    private static final StringBuilder barcodeStringBuilder = new StringBuilder();
    private final AtomicInteger lastCaretPosition = new AtomicInteger(0);

    // *
    private String tempArc;

    @FXML
    private Button continueBtn;
    @FXML

    private Button backBtn;
    @FXML
    private Button showArcBtn;

    @FXML
    private TextField arcNumberTextField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label txtName;
    @FXML
    private Label txtRank;
    @FXML
    private Label txtApp;
    @FXML
    private Label txtUnit;
    @FXML
    private Label txtFinger;
    @FXML
    private Label txtIris;
    @FXML
    private Label txtArcStatus;
    @FXML
    private Label txtSignatureRequired;

    @FXML
    private Label txtBiometricOptions;
    int counter = 1;

    public void initialize() {
        backBtn.setOnAction(event -> backBtnAction());
        showArcBtn.setOnAction(event -> showArcBtnAction());
        continueBtn.setOnAction(event -> continueBtnAction());
        arcNumberTextField.setOnKeyTyped(this::changeTextToUpperCase);
    }

    private void changeTextToUpperCase(KeyEvent keyEvent) {
        keyTyped(keyEvent);
        lastCaretPosition.set(arcNumberTextField.getCaretPosition());
        arcNumberTextField.setText(arcNumberTextField.getText().toUpperCase());
        arcNumberTextField.positionCaret(lastCaretPosition.get());
    }

    private void continueBtnAction() {
        ArcDetail arcDetail = ArcDetailsHolder.getArcDetailsHolder().getArcDetail();
        if (arcDetail.getBiometricOptions().trim().equalsIgnoreCase("photo")) {
            try {
                App.setRoot("camera");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
            }
        } else if (arcDetail.getBiometricOptions().trim().equalsIgnoreCase("both") || arcDetail.getBiometricOptions().trim().equalsIgnoreCase("biometric")) {
            setNextScreen();
        } else {
            messageLabel.setText("Biometric capturing not required for e-ARC number: " + tempArc);
            LOGGER.log(Level.INFO, "Biometric capturing not required for given e-ARC Number");
        }

    }

    private void setNextScreen() {
        SaveEnrollmentDetail saveEnrollmentDetail;
        try {
            saveEnrollmentDetail = SaveEnrollmentDetailUtil.readFromFile();
        } catch (GenericException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            messageLabel.setText(GENERIC_ERR_MSG);
            return;
        }

        // different e-ARC number is entered.
        if (saveEnrollmentDetail.getArcNo() == null || !ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getArcNo().equals(saveEnrollmentDetail.getArcNo())) {
            try {
                App.setRoot("slap_scanner");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
            }
        } else {
            // same e-ARC number is entered as the one saved in saveEnrollment.txt file.
            ArcDetailsHolder.getArcDetailsHolder().setSaveEnrollmentDetail(saveEnrollmentDetail);
            changeScreenBasedOnEnrollmentStatus();
        }

    }

    private void changeScreenBasedOnEnrollmentStatus() {
        switch (ArcDetailsHolder.getArcDetailsHolder().getSaveEnrollmentDetail().getEnrollmentStatus()) {
            case "FingerPrintCompleted":
                try {
                    App.setRoot("iris");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                }
                break;

            case "IrisCompleted":
                try {
                    if ("biometric".equalsIgnoreCase(ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getBiometricOptions().trim())) {
                        if (ArcDetailsHolder.getArcDetailsHolder().getArcDetail().isSignatureRequired()) {
                            App.setRoot("signature");
                            return;
                        }
                        App.setRoot("biometric_capture_complete");
                        return;
                    }
                    App.setRoot("camera");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                }
                break;

            case "PhotoCompleted":
                try {
                    if (ArcDetailsHolder.getArcDetailsHolder().getArcDetail().isSignatureRequired()) {
                        App.setRoot("signature");
                        return;
                    }
                    App.setRoot("biometric_capture_complete");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                }
                break;

            case "SignatureCompleted":

            case "SUCCESS":
                try {
                    App.setRoot("biometric_capture_complete");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                }
                break;

            default:
                try {
                    App.setRoot("slap_scanner");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                }
                break;
        }

    }

    @FXML
    private void backBtnAction() {
        try {
            App.setRoot("main_screen");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    private void showArcDetails() {
        ArcDetail arcDetail;
        LOGGER.log(Level.INFO, () -> "***Fetching details for e-ARC: " + tempArc);
        try {
            disableControls(backBtn, showArcBtn);
            arcDetail = MafisServerApi.fetchARCDetail(tempArc);
        } catch (ConnectionTimeoutException ex) {
            enableControls(backBtn, showArcBtn);
            LOGGER.log(Level.INFO, "Connection timeout");
            updateUiDynamicLabelText(null);
            updateUi("Connection timeout. Please try again.");
            return;
        } catch (Exception ex) {
            enableControls(backBtn, showArcBtn);
            updateUiDynamicLabelText(null);
            updateUi(GENERIC_ERR_MSG);
            return;
        }
        LOGGER.log(Level.INFO, () -> "***ServerResponseErrorCode: " + arcDetail.getErrorCode());
        if (arcDetail.getErrorCode() != 0) {
            LOGGER.log(Level.INFO, () -> "ServerErrorDesc: " + arcDetail.getDesc());
            enableControls(backBtn, showArcBtn);
            updateUiDynamicLabelText(null);
            // should handle based on error code but not maintained
            if (-9999 == arcDetail.getErrorCode() || "null".equals(arcDetail.getDesc())) { // very common error when server's dependencies are not available
                updateUi("Received an unexpected response from server. Kindly try again.");
            } else if (arcDetail.getDesc().toLowerCase().contains("not found")) {
                updateUi("Details not found for e-ARC: " + tempArc);
            } else if (arcDetail.getDesc().toLowerCase().contains("unable to process")) {
                updateUi("Unable to process due to server response failed. Kindly try again.");
            } else {
                updateUi(arcDetail.getDesc());
            }
            return;
        }

        if (arcDetail.getBiometricOptions() == null || arcDetail.getBiometricOptions().isBlank() || arcDetail.getBiometricOptions().trim().equalsIgnoreCase("none")) {
            LOGGER.log(Level.INFO, () -> "Biometric capturing not required for e-ARC: " + tempArc);
            enableControls(backBtn, showArcBtn);
            updateUiDynamicLabelText(arcDetail);
            updateUi("Biometric capturing not required for e-ARC: " + tempArc);
            return;
        }

        updateUiDynamicLabelText(arcDetail);
        updateUi("Details fetched successfully for e-ARC: " + tempArc);

        ArcDetailsHolder holder = ArcDetailsHolder.getArcDetailsHolder();
        holder.setArcDetail(arcDetail);
        SaveEnrollmentDetail saveEnrollmentDetail = new SaveEnrollmentDetail();

        try {
            saveEnrollmentDetail.setEnrollmentStationUnitId(MafisServerApi.getEnrollmentStationUnitId());
            saveEnrollmentDetail.setEnrollmentStationId(MafisServerApi.getEnrollmentStationId());
        } catch (Exception ex) {
            enableControls(backBtn, showArcBtn);
            LOGGER.log(Level.SEVERE, ex.getMessage());
            updateUi(GENERIC_ERR_MSG);
            return;
        }

        saveEnrollmentDetail.setArcNo(arcDetail.getArcNo());
        saveEnrollmentDetail.setBiometricOptions(arcDetail.getBiometricOptions());
        holder.setSaveEnrollmentDetail(saveEnrollmentDetail);
        enableControls(showArcBtn, backBtn, continueBtn);
    }

    private void showArcBtnAction() {
        tempArc = arcNumberTextField.getText().trim();
        if (isMalformedArc()) {
            messageLabel.setText("Kindly enter the valid format for e-ARC number.");
            return;
        }
        disableControls(showArcBtn, backBtn, continueBtn);
        // fetches e-ARC in worker thread.
        App.getThreadPool().execute(this::showArcDetails);
        messageLabel.setText("Fetching details for e-ARC: " + tempArc + ". Kindly wait...");
    }

    private void updateUiDynamicLabelText(ArcDetail arcDetail) {
        Platform.runLater(() -> {
            if (arcDetail == null) {
                clearLabelText(txtName, txtRank, txtApp, txtUnit, txtFinger, txtIris, txtBiometricOptions, txtArcStatus);
                return;
            }
            txtName.setText(arcDetail.getName());
            txtRank.setText(arcDetail.getRank());
            txtApp.setText(arcDetail.getApplicantId());
            txtUnit.setText(arcDetail.getUnit());
            if (arcDetail.getFingers().isEmpty()) {
                txtFinger.setText("NA");
            } else {
                txtFinger.setText(String.join(",", arcDetail.getFingers()));
            }
            if (arcDetail.getIris().isEmpty()) {
                txtIris.setText("NA");
            } else {
                txtIris.setText(String.join(",", arcDetail.getIris()));
            }
            txtBiometricOptions.setText(arcDetail.getBiometricOptions());
            txtArcStatus.setText(arcDetail.getArcStatus());
            if (arcDetail.isSignatureRequired()) {
                txtSignatureRequired.setText("YES");
            } else {
                txtSignatureRequired.setText("NO");
            }
        });
    }

    private void clearLabelText(Label... labels) {
        for (Label label : labels) {
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
    private void keyTyped(KeyEvent keyEvent) {
        long now = Instant.now().toEpochMilli();
        // events must come fast enough to separate from manual input
        if (now - lastEventTimeStamp > KEY_PRESSED_EVENT_GAP_THRESHOLD) {
            barcodeStringBuilder.setLength(0);
        }
        barcodeStringBuilder.append(keyEvent.getCharacter());
        if (keyEvent.getCode().equals(KeyCode.ENTER) || barcodeStringBuilder.length() >= MIN_ARC_LENGTH) {
            arcNumberTextField.setText(barcodeStringBuilder.toString().trim());
            showArcBtn.requestFocus();
            showArcBtnAction();
            barcodeStringBuilder.setLength(0);
        }
        lastEventTimeStamp = now;
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        backBtn.setDisable(false);
        showArcBtn.setDisable(false);
        updateUi("Received an invalid data from the server.");
    }
}
