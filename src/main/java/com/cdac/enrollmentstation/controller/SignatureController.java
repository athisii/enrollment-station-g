package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.SaveEnrollmentDetail;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ArcDetailsHolder;
import com.cdac.enrollmentstation.util.PropertyFile;
import com.cdac.enrollmentstation.util.SaveEnrollmentDetailUtil;
import com.twelvemonkeys.image.ResampleOp;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 05/11/23
 */
public class SignatureController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(SignatureController.class);
    private static final int PADDING = 10;
    // 5 mm -> 19 px
    // 27 mm -> 102 px
    private static final int RAW_WIDTH = 408; // x4
    private static final int RAW_HEIGHT = 76; // x4
    private static final int COMPRESSED_WIDTH = 102; //x1
    private static final int COMPRESSED_HEIGHT = 19; //x1
    private static final String IMG_SIGNATURE_FILE;
    private static final String IMG_SIGNATURE_COMPRESSED_FILE;

    static {
        try {
            IMG_SIGNATURE_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_FILE), PropertyName.IMG_SIGNATURE_FILE);
            IMG_SIGNATURE_COMPRESSED_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_COMPRESSED_FILE), PropertyName.IMG_SIGNATURE_COMPRESSED_FILE);
        } catch (Exception ex) {
            throw new GenericException(ex.getMessage());
        }
    }

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private Label arcLbl;

    @FXML
    private Label confirmPaneLbl;
    @FXML
    private Button backBtn;

    @FXML
    private Label messageLabel;

    @FXML
    private Button clearBtn;
    @FXML
    private Button saveSignatureBtn;

    @FXML
    private VBox confirmVbox;
    @FXML
    private Button confirmYesBtn;
    @FXML
    private Button confirmNoBtn;


    @FXML
    private Canvas canvas;
    private boolean isSigned;
    private double lastX;
    private double lastY;

    // For bounding box
    private double minX = Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxX = Double.MIN_VALUE;
    private double maxY = Double.MIN_VALUE;

    private GraphicsContext gc;
    private boolean firstLoading = true;
//    private boolean firstRelease = false;


    /*
        Mantra touch screen event is not properly handled by javafx runtime on the first canvas touch.
        So simulating to avoid it.
     */
    private void simulateMousePressedAction(Canvas canvas, double x, double y) {
        MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, x, y, x, y, null, 0, false, false, false, false, true, false, false, false, false, false, null);
        canvas.fireEvent(mouseEvent);
    }


    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

        clearBtn.setOnAction(event -> clearBtnAction());
        saveSignatureBtn.setOnAction(this::saveSignatureBtnAction);
        confirmNoBtn.setOnAction(this::confirmNo);
        confirmYesBtn.setOnAction(this::confirmYes);
        backBtn.setOnAction(this::backBtnAction);

        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);


        canvas.setOnMousePressed(event -> {
            lastX = event.getX();
            lastY = event.getY();
        });

        canvas.setOnTouchPressed(event -> {
            lastX = event.getTouchPoint().getX();
            lastY = event.getTouchPoint().getY();
        });

        canvas.setOnTouchMoved(this::onMoveAction);
        canvas.setOnMouseDragged(this::onMoveAction);
        canvas.setOnMouseReleased(this::resetXAndY);
        canvas.setOnTouchReleased(this::resetXAndY);
        canvas.setOnMouseClicked(event -> {
            if (firstLoading) {
                clearBtnAction();
                firstLoading = false;
            }
            double x = event.getX();
            double y = event.getY();
            gc.beginPath();
            gc.moveTo(x - 0.5, y);
            gc.lineTo(x, y);
            gc.stroke();
        });
        arcLbl.setText("e-ARC: " + ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getArcNo());
    }

    private void onMoveAction(InputEvent event) {
        if (lastX >= 0 && lastX <= canvas.getWidth() && lastY >= 0 && lastY <= canvas.getHeight() && !firstLoading) {
//        if (lastX >= 0 && lastX <= canvas.getWidth() && lastY >= 0 && lastY <= canvas.getHeight() && !firstRelease) {
            double x;
            double y;
            if (event instanceof TouchEvent touchEvent) {
                x = touchEvent.getTouchPoint().getX();
                y = touchEvent.getTouchPoint().getY();
            } else {
                x = ((MouseEvent) event).getX();
                y = ((MouseEvent) event).getY();
            }

            // for the bounding box
            if (lastX < minX) {
                minX = lastX;
            }
            if (lastY < minY) {
                minY = lastY;
            }

            if (lastX > maxX) {
                maxX = lastX;
            }
            if (lastY > maxY) {
                maxY = lastY;
            }
            // drawing on the canvas
            gc.beginPath();
            gc.moveTo(lastX, lastY);
            gc.lineTo(x, y);
            gc.stroke();
            lastX = x;
            lastY = y;
            isSigned = true;
        }
        if (firstLoading) {
            clearBtnAction();
            firstLoading = false;
        }
    }

    private void resetXAndY(InputEvent event) {
        // for touch event, it can jump from Release to Drag event directly on tapping the screen.
        lastX = -1;
        lastY = -1;
        // firstRelease = true;
    }

    private void backBtnAction(ActionEvent event) {
        confirmVbox.setVisible(true);
        disableControls(backBtn, clearBtn, saveSignatureBtn);
        if ("biometric".equalsIgnoreCase(ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getBiometricOptions().trim())) {
            confirmPaneLbl.setText("Click 'Yes' to Iris Scan or Click 'No' Capture Signature");
        } else {
            confirmPaneLbl.setText("Click 'Yes' to Capture Photo or Click 'No' Capture Signature");
        }
    }

    private void confirmYes(ActionEvent actionEvent) {
        confirmVbox.setVisible(false);
        try {
            if ("biometric".equalsIgnoreCase(ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getBiometricOptions().trim())) {
                App.setRoot("iris");
                return;
            }
            App.setRoot("camera");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }

    private void confirmNo(ActionEvent actionEvent) {
        confirmVbox.setVisible(false);
        enableControls(backBtn, clearBtn, saveSignatureBtn);
    }

    private void clearBtnAction() {
        messageLabel.setText("Kindly sign in the centre of the black box");
        isSigned = false;
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
    }

    private void saveSignatureBtnAction(ActionEvent event) {
        if (!isSigned) {
            messageLabel.setText("Kindly provide the signature. ");
            return;
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage writableImage = canvas.snapshot(params, null);
        try {
            BufferedImage image = SwingFXUtils.fromFXImage(writableImage, null);
            // to make square box
            int width = (int) (maxX - minX);
            int height = (int) (maxY - minY);

            // just a check to ensure valid/big signature is provided.
            if (width < 20 || height < 20) {
                messageLabel.setText("Kindly provide a valid or larger signature.");
                return;
            }
            minX = Math.max(minX - PADDING, 0);
            minY = Math.max(minY - PADDING, 0);
            width = (int) Math.min(maxX - minX + PADDING, canvas.getWidth() - minX);
            height = (int) Math.min(maxY - minY + PADDING, canvas.getHeight() - minY);

            BufferedImage boundedBox = image.getSubimage((int) minX, (int) minY, width, height);
            BufferedImageOp resampleOpOri = new ResampleOp(RAW_WIDTH, RAW_HEIGHT, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredOri = resampleOpOri.filter(boundedBox, null);
            BufferedImageOp resampleOpCompressed = new ResampleOp(COMPRESSED_WIDTH, COMPRESSED_HEIGHT, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredCompressed = resampleOpCompressed.filter(boundedBox, null);

            Path signaturePath = Paths.get(IMG_SIGNATURE_FILE);
            Path signatureCompressedPath = Paths.get(IMG_SIGNATURE_COMPRESSED_FILE);
            ImageIO.write(filteredOri, "png", signaturePath.toFile());
            ImageIO.write(filteredCompressed, "png", signatureCompressedPath.toFile());

            SaveEnrollmentDetail saveEnrollmentDetail = ArcDetailsHolder.getArcDetailsHolder().getSaveEnrollmentDetail();
            saveEnrollmentDetail.setSignatureRequired(true);
            saveEnrollmentDetail.setSignature(Base64.getEncoder().encodeToString(Files.readAllBytes(signaturePath)));
            saveEnrollmentDetail.setSignatureCompressed(Base64.getEncoder().encodeToString(Files.readAllBytes(signatureCompressedPath)));
            saveEnrollmentDetail.setEnrollmentStatus("SignatureCompleted");
            SaveEnrollmentDetailUtil.writeToFile(saveEnrollmentDetail);
            App.setRoot("biometric_capture_complete");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
            messageLabel.setText(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
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

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        enableControls(backBtn);
        updateUi("Something went wrong. Kindly try again.");
    }

    private static String requireNonBlank(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            String errorMessage = propertyName + " value is null or blank in " + ApplicationConstant.DEFAULT_PROPERTY_FILE;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new GenericException(errorMessage);
        }
        return value;
    }
}
