package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.OnboardingUnitDetail;
import com.cdac.enrollmentstation.dto.UpdateOnboardingReqDto;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

public class OnboardCompleteController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(OnboardCompleteController.class);
    @FXML
    private ImageView downArrowImageView;
    @FXML
    private ImageView upArrowImageView;
    @FXML
    private Label serialNoLabel;
    @FXML
    private VBox hiddenVbox;
    @FXML
    private HBox serialNoDropDownHBox;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private Label unitCodeLbl;
    @FXML
    private Label messageLabel;
    @FXML
    private Button backBtn;
    @FXML
    private Button finishBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private TextField enrollmentStationIdTextField;
    private List<String> serialNos;
    private OnboardingUnitDetail onboardingUnitDetail;

    @FXML
    public void homeBtnAction() throws IOException {
        App.setRoot("main_screen");
    }

    @FXML
    public void backBtnAction() throws IOException {
        App.setRoot("onboard_auth");
    }


    private void finishBtnAction() {
        disableControls(enrollmentStationIdTextField, serialNoDropDownHBox, backBtn, homeBtn, finishBtn);
        App.getThreadPool().execute(() -> {
            try {
                if (onboardingUnitDetail == null) {
                    throw new GenericException("No Unit selected.");
                }
                var enrollmentStationId = enrollmentStationIdTextField.getText();
                if (enrollmentStationId.isEmpty()) {
                    throw new GenericException("Please provide a valid enrolment station ID.");
                }
                var updateOnboardingReqDto = new UpdateOnboardingReqDto(App.getPno(), onboardingUnitDetail.getSerialNo(), "2", onboardingUnitDetail.getHwId(), enrollmentStationId, "ON");
                MafisServerApi.updateOnboarding(updateOnboardingReqDto);
                PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_ID, enrollmentStationId);
                PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_UNIT_CAPTION, onboardingUnitDetail.getUnitName());
                PropertyFile.changePropertyValue(PropertyName.ENROLLMENT_STATION_UNIT_ID, onboardingUnitDetail.getUnitCode());
                PropertyFile.changePropertyValue(PropertyName.HARDWARE_ID, onboardingUnitDetail.getHwId());
                PropertyFile.changePropertyValue(PropertyName.DEVICE_SERIAL_NO, onboardingUnitDetail.getSerialNo());
                PropertyFile.changePropertyValue(PropertyName.INITIAL_SETUP, "0"); // initial setup done.
                if (App.getHostnameChanged()) {
                    App.setHostnameChanged(false);
                    App.getThreadPool().execute(this::rebootSystem);
                }
                App.setRoot("main_screen");
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, () -> "***Error: " + ex.getMessage());
                updateUi(ex.getMessage());
                enableControls(enrollmentStationIdTextField, serialNoDropDownHBox, backBtn, homeBtn, finishBtn);
            }
        });
    }

    // calls automatically by JavaFX runtime
    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
        onboardingUnitDetail = App.getOnboardingUnitDetails().get(0);// first one
        serialNoLabel.setText(onboardingUnitDetail.getSerialNo());
        unitCodeLbl.setText(onboardingUnitDetail.getUnitName());
        serialNos = App.getOnboardingUnitDetails().stream().map(OnboardingUnitDetail::getSerialNo).sorted().toList();
        serialNoDropDownHBox.setOnMouseClicked(this::toggleUnitCaptionListView);
        finishBtn.setOnAction(event -> finishBtnAction());
    }

    private void toggleUnitCaptionListView(MouseEvent mouseEvent) {
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
            listView.setItems(FXCollections.observableArrayList(serialNos));
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    serialNoLabel.setText(newValue);
                    onboardingUnitDetail = App.getOnboardingUnitDetails().stream().filter(ele -> ele.getSerialNo().equalsIgnoreCase(serialNoLabel.getText())).findFirst().orElse(null);
                    if (onboardingUnitDetail != null) {
                        unitCodeLbl.setText(onboardingUnitDetail.getUnitName());
                    }
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
            listView.setItems(FXCollections.observableList(serialNos));
            return;
        }
        String valueUpper = value.toUpperCase();
        listView.setItems(FXCollections.observableList(serialNos.stream().filter(serialNo -> serialNo.toUpperCase().contains(valueUpper)).toList()));
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
        enableControls(enrollmentStationIdTextField, backBtn, homeBtn, finishBtn);
        updateUi("Received an invalid data from the server.");
    }

    private void rebootSystem() {
        try {
            int counter = 5;
            while (counter >= 1) {
                updateUi("Rebooting system to take effect in " + counter + " second(s)...");
                Thread.sleep(1000);
                counter--;
            }
            Process process = Runtime.getRuntime().exec("reboot");
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String eline;
            while ((eline = error.readLine()) != null) {
                String finalEline = eline;
                LOGGER.log(Level.INFO, () -> "***Error: " + finalEline);
            }
            error.close();
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LOGGER.log(Level.INFO, () -> "***Error: Process Exit Value: " + exitVal);
                updateUi(ApplicationConstant.GENERIC_ERR_MSG);
            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.INFO, () -> "**Error while rebooting: " + ex.getMessage());
            updateUi(ApplicationConstant.GENERIC_ERR_MSG);
        }
        enableControls(enrollmentStationIdTextField, serialNoDropDownHBox, backBtn, homeBtn, finishBtn);
    }

}
