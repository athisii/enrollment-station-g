package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.Unit;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

public class ServerConfigController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(ServerConfigController.class);
    private List<Unit> units;

    @FXML
    private TextField mafisUrlTextField;

    @FXML
    private TextField enrollmentStationIdTextField;


    @FXML
    private Label messageLabel;

    @FXML
    private Button fetchUnitsBtn;


    @FXML
    private ComboBox<String> enrollmentStationUnitIdsComboBox;
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
        enableControls(mafisUrlTextField, enrollmentStationIdTextField, enrollmentStationUnitIdsComboBox, fetchUnitsBtn);
    }


    private void saveToFile(Unit unit) {
        if (mafisUrlTextField.getText().isBlank()) {
            messageLabel.setText("Invalid mafis url.");
            return;
        }
        if (enrollmentStationIdTextField.getText().isBlank()) {
            messageLabel.setText("Enrolment station id is empty.");
            return;
        }

        PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, mafisUrlTextField.getText());
        PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_ID, enrollmentStationIdTextField.getText());
        PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_UNIT_ID, unit.getValue());
        PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_UNIT_CAPTION, unit.getCaption());
        messageLabel.setText("Updated successfully.");
    }


    @FXML
    private void fetchBtnAction() {
        if (mafisUrlTextField.getText().isBlank()) {
            messageLabel.setText(("Not a valid url."));
            return;
        }
        homeBtn.requestFocus();
        messageLabel.setText("Fetching units...");
        disableControls(backBtn, homeBtn, editBtn, fetchUnitsBtn);
        enrollmentStationUnitIdsComboBox.setItems(FXCollections.observableArrayList());
        enrollmentStationUnitIdsComboBox.setValue(null); // selected value
        App.getThreadPool().execute(this::fetchUnits);
    }

    private void fetchUnits() {
        try {
            PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, mafisUrlTextField.getText());
            units = MafisServerApi.fetchAllUnits();
        } catch (GenericException ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, homeBtn, editBtn, fetchUnitsBtn);
            return;
        } catch (ConnectionTimeoutException ex) {
            Platform.runLater(() -> {
                messageLabel.setText("Connection timeout. Please try again.");
                enableControls(backBtn, homeBtn, editBtn, fetchUnitsBtn);
            });
            return;
        }

        if (units == null || units.isEmpty()) {
            updateUi("No units for selected mafis url.");
            enableControls(backBtn, homeBtn, editBtn, fetchUnitsBtn);
            return;
        }
        List<String> captions = units.stream().map(Unit::getCaption).collect(Collectors.toList());
        Platform.runLater(() -> enrollmentStationUnitIdsComboBox.setItems(FXCollections.observableArrayList(captions)));
        String enrollmentStationUnitCaption = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_CAPTION);
        Platform.runLater(() -> enrollmentStationUnitIdsComboBox.getSelectionModel().select(enrollmentStationUnitCaption));
        updateUi("Units fetched successfully.");
        enableControls(backBtn, homeBtn, editBtn, fetchUnitsBtn);
    }

    // calls automatically by JavaFX runtime
    public void initialize() {
        String commonText = " is required in " + ApplicationConstant.DEFAULT_PROPERTY_FILE + ".";
        String errorMessage = "";
        String mafisUrl = PropertyFile.getProperty(PropertyName.MAFIS_API_URL);
        String enrollmentStationId = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_ID);
        String enrollmentStationUnitId = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_ID);
        String enrollmentStationUnitCaption = PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_CAPTION);
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
        if (!errorMessage.isBlank()) {
            throw new GenericException(errorMessage);
        }

        mafisUrlTextField.setText(mafisUrl);
        enrollmentStationIdTextField.setText(enrollmentStationId);
        enrollmentStationUnitIdsComboBox.getSelectionModel().select(enrollmentStationUnitCaption);
        enrollmentStationUnitIdsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // sometimes old and new value will be null.
            if (newValue != null) {
                Optional<Unit> unitOptional = units.stream().filter(u -> u.getCaption().equals(newValue)).findFirst();
                unitOptional.ifPresent(this::saveToFile);
            }
        });

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
        enableControls(backBtn, fetchUnitsBtn, homeBtn, editBtn);
        updateUi("Received an invalid data from the server.");
    }
}
