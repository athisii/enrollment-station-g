/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.security.AuthUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.io.IOException;

/**
 * FXML Controller class
 *
 * @author root
 */
public class AdminAuthController {
    private static final int MAX_LENGTH = 30;
    private static volatile boolean isDone = false;
    @FXML
    private Button backBtn;
    @FXML
    private Button loginBtn;
    @FXML
    private Label statusMsg;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField textField;

    @FXML
    public void showHome() throws IOException {
        App.setRoot("main_screen");
    }


    @FXML
    public void serverConfig() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            if (!isDone) {
                statusMsg.setText("The server is taking more time than expected. Kindly try again.");
                enableControls(backBtn, loginBtn);
            }
        }));
        timeline.setCycleCount(1);
        timeline.play();
        disableControls(backBtn, loginBtn);
        App.getThreadPool().execute(this::authenticateUser);
    }

    private void authenticateUser() {
        try {
            if (AuthUtil.authenticate(textField.getText(), passwordField.getText())) {
                App.setRoot("admin_config");
                isDone = true;
                return;
            }
            updateUi("Wrong username or password.");
        } catch (GenericException | IOException ex) {
            updateUi(ex.getMessage());
        }
        isDone = true;
        // clean up UI on failure
        clearPasswordField();
        enableControls(backBtn, loginBtn);
    }

    public void initialize() {
        // restrict the TextField Length
        textField.textProperty().addListener((observable, oldValue, newValue) -> limitCharacters(textField, oldValue, newValue));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> limitCharacters(passwordField, oldValue, newValue));

        // ease of use for operator
        textField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                passwordField.requestFocus();
                event.consume();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                serverConfig();
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

    private void clearPasswordField() {
        Platform.runLater(() -> {
            textField.requestFocus();
            textField.setText("");
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


}
