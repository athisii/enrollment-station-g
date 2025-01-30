package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.UserReqDto;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.security.AuthUtil;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

public class AdminAuthController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(AdminAuthController.class);

    private static final int MAX_LENGTH = 30;
    private volatile boolean isDone = false;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private Button backBtn;
    @FXML
    private Button loginBtn;
    @FXML
    private Label statusMsg;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameTextField;

    @FXML
    public void showHome() throws IOException {
        App.setRoot("main_screen");
    }


    @FXML
    public void loginBtnAction() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (!isDone) {
                statusMsg.setText("The server is taking more time than expected. Kindly try again.");
                enableControls(backBtn, loginBtn);
            }
        }));
        timeline.setCycleCount(1);
        timeline.play();
        disableControls(backBtn, loginBtn);
        App.getThreadPool().execute(() -> authenticateUser(usernameTextField.getText(), passwordField.getText()));
    }

    private void authenticateUser(String username, String password) {
        try {
            // allow admin to log in in dev env
            if (!("admin".equals(username) && "1".equals(PropertyFile.getProperty(PropertyName.ENV)))) {
                LOGGER.log(Level.INFO, () -> "*****  Validating user category ********");
                // Hardware Type Mapping:
                //      PES - 1
                //      FES - 2
                MafisServerApi.validateUserCategory(new UserReqDto(username, PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_ID), "2", PropertyFile.getProperty(PropertyName.ENROLLMENT_STATION_UNIT_ID)));
                LOGGER.info("Done validating user category.");
            }
            if (AuthUtil.authenticate(username, password)) {
                // must set on JavaFX thread.
                App.setPno(username);
                Platform.runLater(() -> {
                    try {
                        App.setRoot("admin_config");
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                        throw new GenericException(ex.getMessage());
                    }
                });
                isDone = true;
                return;
            }
            LOGGER.log(Level.INFO, "Incorrect username or password.");
            updateUi("Wrong username or password.");
        } catch (Exception ex) {
            updateUi(ex.getMessage());
        }
        App.setPno(null);
        isDone = true;
        // clean up UI on failure
        clearUiControls();
        enableControls(backBtn, loginBtn);
    }

    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
        // restrict the TextField Length
        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> limitCharacters(usernameTextField, oldValue, newValue));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> limitCharacters(passwordField, oldValue, newValue));

        // ease of use for operator
        usernameTextField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                passwordField.requestFocus();
                event.consume();
            }
            if (!statusMsg.getText().isBlank()) {
                statusMsg.setText("");
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                loginBtnAction();
            }
        });
    }

    private void limitCharacters(TextField textField, String oldValue, String newValue) {
        if (newValue.length() > MAX_LENGTH) {
            textField.setText(oldValue);
        }
    }

    private void updateUi(String message) {
        Platform.runLater(() -> statusMsg.setText(message));
    }

    private void clearUiControls() {
        Platform.runLater(() -> {
            usernameTextField.requestFocus();
            usernameTextField.setText("");
            passwordField.setText("");
        });
    }

    private void disableControls(Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(true);
        }
    }

    private void enableControls(Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(false);
        }
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        backBtn.setDisable(false);
        loginBtn.setDisable(false);
        updateUi("Something went wrong. Please try again");
    }
}
