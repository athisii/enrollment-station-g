package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.*;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ArcDetailsHolder;
import com.cdac.enrollmentstation.util.PropertyFile;
import com.cdac.enrollmentstation.util.SaveEnrollmentDetailUtil;
import com.cdac.enrollmentstation.util.Singleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class BiometricCaptureCompleteController extends AbstractBaseController {
    //For Application Log
    private static final Logger LOGGER = ApplicationLog.getLogger(BiometricCaptureCompleteController.class);
    private static final String NOT_AVAILABLE = "Not Available";
    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView statusImageView;

    @FXML
    private Button submitBtn;

    @FXML
    private Button homeBtn;

    @FXML
    private Button fetchArcBtn;

    @FXML
    private ProgressIndicator progressIndicator;

    // after submitted successfully/failed, go back to main screen after 10 secs
    private static boolean isStillHere = true;
    private static final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
        if (isStillHere) {
            try {
                App.setRoot("main_screen");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
            }
        }
    }));

    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
    }

    @FXML
    private void homeBtnAction() {
        isStillHere = false;
        try {
            App.setRoot("main_screen");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }

    @FXML
    private void fetchArcBtnAction() {
        isStillHere = false;
        try {
            App.setRoot("biometric_enrollment");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }


    @FXML
    private void submitBtnAction() {
        submitBtn.setDisable(true);
        progressIndicator.setVisible(true);
        messageLabel.setText("Please wait...");
        App.getThreadPool().execute(this::submitData);
    }

    private void submitData() {
        ArcDetailsHolder holder = ArcDetailsHolder.getArcDetailsHolder();
        ArcDetail arcDetail = holder.getArcDetail();
        SaveEnrollmentDetail saveEnrollmentDetail = holder.getSaveEnrollmentDetail();

        saveEnrollmentDetail.setEnrollmentStatus("SUCCESS");
        // based on biometricOptions just do the necessary actions
        if (arcDetail.getBiometricOptions().toLowerCase().contains("biometric")) {
            saveEnrollmentDetail.setPhoto(NOT_AVAILABLE);
            saveEnrollmentDetail.setPhotoCompressed(NOT_AVAILABLE);
        } else if (arcDetail.getBiometricOptions().toLowerCase().contains("photo")) {
            // set NA for slap_scanner, iris etc.
            saveEnrollmentDetail.setIrisScannerSerialNo(NOT_AVAILABLE);
            saveEnrollmentDetail.setLeftFpScannerSerialNo(NOT_AVAILABLE);
            saveEnrollmentDetail.setRightFpScannerSerialNo(NOT_AVAILABLE);
            Set<Fp> fingerprintset = new HashSet<>(Set.of(new Fp(NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE)));
            saveEnrollmentDetail.setFp(fingerprintset);
            Set<Iris> irisSet = new HashSet<>(Set.of(new Iris(NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE)));
            saveEnrollmentDetail.setIris(irisSet);
        }

        if (!arcDetail.isSignatureRequired()) {
            saveEnrollmentDetail.setSignatureRequired(false);
            saveEnrollmentDetail.setSignature(NOT_AVAILABLE);
            saveEnrollmentDetail.setSignatureCompressed(NOT_AVAILABLE);
        }

        // common properties
        saveEnrollmentDetail.setEnrollmentStationId(PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_ID));
        saveEnrollmentDetail.setEnrollmentStationUnitId(PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_ID));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        saveEnrollmentDetail.setEnrollmentDate(formatter.format(date));
        saveEnrollmentDetail.setArcStatus(arcDetail.getArcStatus());
        saveEnrollmentDetail.setUniqueId(arcDetail.getApplicantId());//For ApplicantID
        saveEnrollmentDetail.setBiometricOptions(arcDetail.getBiometricOptions());

        // saves saveEnrollmentDetail for backups
        try {
            SaveEnrollmentDetailUtil.writeToFile(saveEnrollmentDetail);
        } catch (Exception ex) {
            onErrorUpdateUiControls(ex.getMessage());
            return;
        }

        // converts saveEnrollmentDetail object to json string
        String jsonData;
        try {
            jsonData = Singleton.getObjectMapper().writeValueAsString(saveEnrollmentDetail);
        } catch (JsonProcessingException ignored) {
            LOGGER.log(Level.SEVERE, ApplicationConstant.JSON_WRITE_ER_MSG);
            onErrorUpdateUiControls(ApplicationConstant.GENERIC_ERR_MSG);
            return;
        }

        CommonResDto resDto;
        LOGGER.log(Level.INFO, () -> "***Sending biometric data to the server.");
        // try submitting to the server.
        try {
            resDto = MafisServerApi.postEnrollment(jsonData);
        } catch (ConnectionTimeoutException ex) {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                messageLabel.setText("Connection timeout. Failed to save record.");
                submitBtn.setDisable(false);
                homeBtn.setDisable(false);
                timeline.setCycleCount(1);
                timeline.play();
            });
            return;
        } catch (Exception ex) {
            onErrorUpdateUiControls(ex.getMessage());
            return;
        }
        LOGGER.log(Level.INFO, () -> "***ServerResponseErrorCode: " + resDto.getErrorCode());
        // checks for error response
        if (resDto.getErrorCode() != 0) {
            LOGGER.log(Level.SEVERE, () -> "Server desc: " + resDto.getDesc());
            if (resDto.getErrorCode() == -1) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    messageLabel.setText("Failed to save record by the server. Please re-submit again.");
                    submitBtn.setDisable(false);
                    homeBtn.setDisable(false);
                });
                return;
            }
            // runs on main thread
            updateUiIconOnServerResponse(false, resDto.getDesc());
        } else {
            // else saved successfully on the server
            // runs on main thread
            updateUiIconOnServerResponse(true, "Applicant's Biometric Captured Successfully.");
        }

        // time for cleanup
        try {
            SaveEnrollmentDetailUtil.delete();
        } catch (Exception ex) {
            onErrorUpdateUiControls(ex.getMessage());
        }
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void updateUiIconOnServerResponse(boolean success, String message) {
        Platform.runLater(() -> {
            InputStream inputStream;
            if (success) {
                inputStream = BiometricCaptureCompleteController.class.getResourceAsStream("/img/tick.png");
            } else {
                inputStream = BiometricCaptureCompleteController.class.getResourceAsStream("/img/red_cross.png");
            }
            if (inputStream == null) {
                LOGGER.log(Level.SEVERE, "Image not found for updating the UI image.");
                //TODO: continue for now
            }
            messageLabel.setText(message);
            statusImageView.setImage(new Image(inputStream));
            submitBtn.setDisable(true);
            homeBtn.setDisable(false);
            fetchArcBtn.setDisable(false);
            progressIndicator.setVisible(false);
        });
    }

    private void onErrorUpdateUiControls(String message) {
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            messageLabel.setText(message);
            homeBtn.setDisable(false);
            fetchArcBtn.setDisable(false);
        });
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        homeBtn.setDisable(false);
        updateUi("Something went wrong. Please try again");
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }
}

 