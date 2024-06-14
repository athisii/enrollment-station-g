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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

public class ServerConfigController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(ServerConfigController.class);
    @FXML
    private ImageView downArrowImageView;
    @FXML
    private ImageView upArrowImageView;
    @FXML
    private Label unitCaptionLabel;
    @FXML
    private VBox hiddenVbox;
    @FXML
    private HBox unitIdDropDownHBox;
    @FXML
    private BorderPane rootBorderPane;
    private List<Unit> units = new ArrayList<>();
    private List<String> sortedCaptions = new ArrayList<>();

    @FXML
    private TextField mafisUrlTextField;

    @FXML
    private TextField enrollmentStationIdTextField;


    @FXML
    private Label messageLabel;

    @FXML
    private Button fetchUnitsBtn;

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
        if (hiddenVbox.isVisible()) {
            hiddenVbox.getChildren().remove(0, hiddenVbox.getChildren().size());
            hiddenVbox.setVisible(false);
            upArrowImageView.setVisible(false);
            downArrowImageView.setVisible(true);
        }
        enableControls(mafisUrlTextField, enrollmentStationIdTextField, fetchUnitsBtn);
    }


    private void saveUnitIdAndCaption(Unit unit) {
        PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_UNIT_ID, unit.getValue());
        PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_UNIT_CAPTION, unit.getCaption());
        messageLabel.setText("Enrolment Station Unit ID updated successfully.");
    }


    @FXML
    private void fetchBtnAction() {
        if (hiddenVbox.isVisible()) {
            hiddenVbox.getChildren().remove(0, hiddenVbox.getChildren().size());
            hiddenVbox.setVisible(false);
            upArrowImageView.setVisible(false);
            downArrowImageView.setVisible(true);
        }
        homeBtn.requestFocus();
        messageLabel.setText("Fetching units...");
        disableControls(backBtn, homeBtn, editBtn, unitIdDropDownHBox, fetchUnitsBtn);
        sortedCaptions = new ArrayList<>();
        units = new ArrayList<>();
        App.getThreadPool().execute(this::fetchUnits);
    }

    private void fetchUnits() {
        try {
            units = MafisServerApi.fetchAllUnits();
        } catch (GenericException ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, homeBtn, editBtn, unitIdDropDownHBox, fetchUnitsBtn);
            return;
        } catch (ConnectionTimeoutException ex) {
            Platform.runLater(() -> {
                messageLabel.setText("Connection timeout. Please try again.");
                enableControls(backBtn, homeBtn, editBtn, unitIdDropDownHBox, fetchUnitsBtn);
            });
            return;
        }

        if (units == null || units.isEmpty()) {
            updateUi("No units for selected mafis url.");
            enableControls(backBtn, homeBtn, editBtn, unitIdDropDownHBox, fetchUnitsBtn);
            return;
        }
        sortedCaptions = units.stream().map(Unit::getCaption).sorted().toList();
        updateUi("Units fetched successfully.");
        enableControls(backBtn, homeBtn, editBtn, unitIdDropDownHBox, fetchUnitsBtn);
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
        unitCaptionLabel.setText(enrollmentStationUnitCaption);

        enrollmentStationIdTextField.textProperty().addListener((observable, oldValue, newValue) -> saveEnrollmentStationId(newValue));
        mafisUrlTextField.textProperty().addListener((observable, oldValue, newValue) -> saveMafisUrl(newValue));

        unitIdDropDownHBox.setOnMouseClicked(this::toggleUnitCaptionListView);
    }

    private void saveMafisUrl(String newValue) {
        if (newValue != null && !newValue.isBlank()) {
            PropertyFile.changePropertyValue(PropertyName.MAFIS_API_URL, newValue);
            enableControls(backBtn, editBtn, enrollmentStationIdTextField, unitIdDropDownHBox, homeBtn, fetchUnitsBtn);
            if (hiddenVbox.isVisible()) {
                hiddenVbox.setDisable(false);
            }
            updateUi("Mafis API Server Url updated successfully");
        } else {
            updateUi("Enter a valid Mafis API Server Url");
            disableControls(backBtn, homeBtn, editBtn, enrollmentStationIdTextField, unitIdDropDownHBox, fetchUnitsBtn);
            if (hiddenVbox.isVisible()) {
                hiddenVbox.setDisable(true);
            }
        }
    }

    private void saveEnrollmentStationId(String newValue) {
        if (newValue != null && !newValue.isBlank()) {
            PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_ID, newValue);
            enableControls(backBtn, editBtn, mafisUrlTextField, unitIdDropDownHBox, homeBtn, fetchUnitsBtn);
            if (hiddenVbox.isVisible()) {
                hiddenVbox.setDisable(false);
            }
            updateUi("Enrolment Station ID updated successfully");
        } else {
            updateUi("Enter a valid Enrolment Station ID");
            disableControls(backBtn, homeBtn, editBtn, mafisUrlTextField, unitIdDropDownHBox, fetchUnitsBtn);
            if (hiddenVbox.isVisible()) {
                hiddenVbox.setDisable(true);
            }
        }
    }

    private void toggleUnitCaptionListView(MouseEvent mouseEvent) {
        if (sortedCaptions.isEmpty() || units.isEmpty()) {
            return;
        }
        if (hiddenVbox.isVisible()) {
            hiddenVbox.getChildren().remove(0, hiddenVbox.getChildren().size());
            hiddenVbox.setVisible(false);
            upArrowImageView.setVisible(false);
            downArrowImageView.setVisible(true);
        } else {
            hiddenVbox.setVisible(true);
            downArrowImageView.setVisible(false);
            upArrowImageView.setVisible(true);
            TextField sarchTextField = new TextField();
            sarchTextField.setPromptText("Search");
            hiddenVbox.getChildren().add(sarchTextField);
            ListView<String> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(sortedCaptions));
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Optional<Unit> unitOptional = units.stream().filter(u -> u.getCaption().equals(newValue)).findFirst();
                    unitOptional.ifPresent(this::saveUnitIdAndCaption);
                    unitCaptionLabel.setText(newValue);
                    hiddenVbox.getChildren().remove(0, hiddenVbox.getChildren().size());
                    hiddenVbox.setVisible(false);
                    downArrowImageView.setVisible(true);
                    upArrowImageView.setVisible(false);
                }
            });
            sarchTextField.textProperty().addListener((observable, oldVal, newVal) -> searchFilter(newVal, listView));
            hiddenVbox.getChildren().add(1, listView);
        }
    }

    private void searchFilter(String value, ListView<String> listView) {
        if (value.isEmpty()) {
            listView.setItems(FXCollections.observableList(units.stream().map(Unit::getCaption).toList()));
            return;
        }
        String valueUpper = value.toUpperCase();
        listView.setItems(FXCollections.observableList(units.stream().map(Unit::getCaption).filter(caption -> caption.toUpperCase().contains(valueUpper)).toList()));
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
