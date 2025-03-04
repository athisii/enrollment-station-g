package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.UserReqDto;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

public class ServerConfigController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(ServerConfigController.class);

    @FXML
    private Label serialNoOfDevice;
    @FXML
    private Label unitCaptionLabel;
    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private TextField mafisUrlTextField;

    @FXML
    private Label enrollmentStationIdTextField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button validateBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Button editBtn;
    @FXML
    private Button homeBtn;


    @FXML
    public void homeBtnAction() throws IOException {
        App.setRoot("main_screen");
    }

    @FXML
    public void backBtnAction() throws IOException {
        App.setRoot("admin_config");
    }


    @FXML
    private void editBtnAction() {
        updateUi("");
        enableControls(mafisUrlTextField, validateBtn);
    }


    @FXML
    private void validateBtnAction() {
        homeBtn.requestFocus();
        messageLabel.setText("Validating MAFIS URL...");
        disableControls(backBtn, homeBtn, editBtn, validateBtn);
        App.getThreadPool().execute(() -> validateServer(mafisUrlTextField.getText()));
    }

    private void validateServer(String mafisUrl) {
        String oldMafisUrl = PropertyFile.getProperty(PropertyName.MAFIS_API_URL);
        PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, mafisUrl);
        try {
            // Hardware Type Mapping:
            //      PES - 1
            //      FES - 2
            MafisServerApi.validateUserCategory(new UserReqDto(App.getPno(), PropertyFile.getProperty(PropertyName.DEVICE_SERIAL_NO), "2", PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_ID)));
            LOGGER.info("Done validating user category.");
        } catch (GenericException ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, homeBtn, editBtn, validateBtn);
            PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, oldMafisUrl);
            Platform.runLater(() -> mafisUrlTextField.setText(oldMafisUrl));
            return;
        } catch (ConnectionTimeoutException ex) {
            Platform.runLater(() -> {
                messageLabel.setText("Connection timeout. Please try again.");
                enableControls(backBtn, homeBtn, editBtn, validateBtn);
                mafisUrlTextField.setText(oldMafisUrl);
            });
            PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, oldMafisUrl);
            return;
        }
        updateUi("MAFIS URL updated successfully.");
        enableControls(backBtn, homeBtn, editBtn, validateBtn);
    }

    // calls automatically by JavaFX runtime
    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

        String commonText = " is required in " + ApplicationConstant.DEFAULT_PROPERTY_FILE + ".";
        String errorMessage = "";
        String mafisUrl = PropertyFile.getProperty(PropertyName.MAFIS_API_URL);
        String enrollmentStationId = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_ID);
        String enrollmentStationUnitId = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_ID);
        String enrollmentStationUnitCaption = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_CAPTION);
        String deviceSerialNumber = PropertyFile.getProperty(PropertyName.DEVICE_SERIAL_NO);
        if (mafisUrl.isBlank()) {
            errorMessage += PropertyName.MAFIS_API_URL + commonText;
        }
        if (enrollmentStationId.isBlank()) {
            errorMessage += "\n" + PropertyName.ENROLLMENT_STATION_ID + commonText;
        }
        if (enrollmentStationUnitId.isBlank()) {
            errorMessage += "\n" + PropertyName.ENROLLMENT_STATION_UNIT_ID + commonText;
        }
        if (enrollmentStationUnitCaption.isBlank()) {
            errorMessage += "\n" + PropertyName.ENROLLMENT_STATION_UNIT_CAPTION + commonText;
        }
        if (deviceSerialNumber.isBlank()) {
            errorMessage += "\n" + PropertyName.DEVICE_SERIAL_NO + commonText;
        }
        if (!errorMessage.isBlank()) {
            throw new GenericException(errorMessage);
        }

        mafisUrlTextField.setText(mafisUrl);
        enrollmentStationIdTextField.setText(enrollmentStationId);
        unitCaptionLabel.setText(enrollmentStationUnitCaption);
        serialNoOfDevice.setText(deviceSerialNumber);

        // hides in prod
//        if ("0".equals(PropertyFile.getProperty(PropertyName.ENV))) {
//            validateBtn.setManaged(false);
//            editBtn.setManaged(false);
//            mafisUrlTextField.setDisable(false);
//            mafisUrlTextField.setEditable(false);
//        }
    }


    private void updateUi(String message) {
        Platform.runLater((() -> messageLabel.setText(message)));
    }


    private void disableControls(Node... nodes) {
        Platform.runLater((() -> {
            for (Node node : nodes) {
                node.setDisable(true);
            }
        }));
    }

    private void enableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        enableControls(backBtn, validateBtn, homeBtn, editBtn);
        updateUi("Received an invalid data from the server.");
    }
}
