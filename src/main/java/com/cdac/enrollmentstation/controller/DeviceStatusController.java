package com.cdac.enrollmentstation.controller;
/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.DeviceUtil;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceStatusController extends AbstractBaseController {

    private static final Logger LOGGER = ApplicationLog.getLogger(DeviceStatusController.class);
    private static final Image RED_CROSS_IMAGE;
    private static final Image GREEN_TICK_IMAGE;

    static {
        RED_CROSS_IMAGE = new Image(Objects.requireNonNull(DeviceStatusController.class.getResourceAsStream("/img/red_cross.png")));
        GREEN_TICK_IMAGE = new Image(Objects.requireNonNull(DeviceStatusController.class.getResourceAsStream("/img/tick.png")));
    }

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private ImageView irisUsbImage;
    @FXML
    private ImageView irisSdkImage;
    @FXML
    private ImageView slapSdkImage;
    @FXML
    private ImageView slapUsbImage;
    @FXML
    private ImageView cameraImage;
    @FXML
    private ImageView barcodeImage;
    @FXML
    private ImageView mafisUrlImage;

    private void checkDevicesStatus() {
        App.getThreadPool().execute(this::checkMafisApi);
        App.getThreadPool().execute(this::checkSlapScanner);
        App.getThreadPool().execute(this::checkBarcode);
        checkCamera();
        checkIris();
    }

    @FXML
    private void refresh() {
        checkDevicesStatus();
    }

    @FXML
    private void home() throws IOException {
        App.setRoot("main_screen");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("admin_config");
    }

    // automatically called by JavaFX runtime.
    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
        checkDevicesStatus();
    }


    private void checkCamera() {
        // checks using JNI
        if (DeviceUtil.isCameraConnected()) {
            cameraImage.setImage(GREEN_TICK_IMAGE);
        } else {
            cameraImage.setImage(RED_CROSS_IMAGE);
        }
    }

    private void checkIris() {
        // checks using JNI
        if (DeviceUtil.isIrisConnected()) {
            irisSdkImage.setImage(GREEN_TICK_IMAGE);
            irisUsbImage.setImage(GREEN_TICK_IMAGE);
        } else {
            irisSdkImage.setImage(RED_CROSS_IMAGE);
            irisUsbImage.setImage(RED_CROSS_IMAGE);
        }
    }

    private void checkSlapScanner() {
        // checks using JNI
        if (DeviceUtil.isFpScannerConnected(2)) {
            slapUsbImage.setImage(GREEN_TICK_IMAGE);
            slapSdkImage.setImage(GREEN_TICK_IMAGE);
        } else {
            slapUsbImage.setImage(RED_CROSS_IMAGE);
            slapSdkImage.setImage(RED_CROSS_IMAGE);
        }
    }

    private void checkBarcode() {
        try {
            List<String> lines = Files.readAllLines(Path.of(PropertyFile.getProperty(PropertyName.BARCODE_FILE_PATH)));
            if (lines.isEmpty() || lines.get(0) == null || !lines.get(0).contains("yes")) {
                barcodeImage.setImage(RED_CROSS_IMAGE);
            } else {
                barcodeImage.setImage(GREEN_TICK_IMAGE);
            }
        } catch (InvalidPathException e) {
            LOGGER.log(Level.SEVERE, () -> PropertyFile.getProperty(PropertyName.BARCODE_FILE_PATH) + "not found.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> "An error occur reading barcode file.");
            throw new GenericException("An error occur reading barcode file.");
        }
    }

    private void checkMafisApi() {
        try {
            MafisServerApi.fetchARCDetail("123abc");
            mafisUrlImage.setImage(GREEN_TICK_IMAGE);
        } catch (ConnectionTimeoutException ex) {
            mafisUrlImage.setImage(RED_CROSS_IMAGE);
        } catch (Exception ex) {
            // connected but throws exception on JSON parsing error
            mafisUrlImage.setImage(GREEN_TICK_IMAGE);
        }
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
    }

}
