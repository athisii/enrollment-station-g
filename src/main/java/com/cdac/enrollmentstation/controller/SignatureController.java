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
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 05/11/23
 */
public class SignatureController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(SignatureController.class);
    // 5x27mm aspect ratio
    private static final int RAW_WIDTH = 432; // 27x16
    private static final int RAW_HEIGHT = 80; // 5x16
    private static final int COMPRESSED_WIDTH = 189; //27*7
    private static final int COMPRESSED_HEIGHT = 35; //5*7
    private static final String IMG_SIGNATURE_FILE;
    private static final String IMG_SIGNATURE_COMPRESSED_FILE;
    int counter;

    static {
        try {
            IMG_SIGNATURE_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_FILE), PropertyName.IMG_SIGNATURE_FILE);
            IMG_SIGNATURE_COMPRESSED_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_COMPRESSED_FILE), PropertyName.IMG_SIGNATURE_COMPRESSED_FILE);
        } catch (Exception ex) {
            throw new GenericException(ex.getMessage());
        }
    }

    private enum EventType {
        DRAG, CLICK;
    }

    @FXML
    private ImageView previewSignatureImageView;

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

        canvas.setOnTouchMoved(touchEvent -> onAction(touchEvent, EventType.DRAG));
        canvas.setOnMouseDragged(mouseEvent -> onAction(mouseEvent, EventType.DRAG));
        canvas.setOnMouseReleased(this::resetXAndY);
        canvas.setOnTouchReleased(this::resetXAndY);
        canvas.setOnMouseClicked(mouseEvent -> onAction(mouseEvent, EventType.CLICK));
        arcLbl.setText("e-ARC: " + ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getArcNo());
    }

    private void onAction(InputEvent event, EventType eventType) {
        // lastX = -1
        // lastY = -1
        if (eventType == EventType.CLICK) {
            if (firstLoading) {
                clearBtnAction();
                firstLoading = false;
            }
            drawSignature(event, eventType);
        } else if (eventType == EventType.DRAG && lastX >= 0 && lastX <= canvas.getWidth() && lastY >= 0 && lastY <= canvas.getHeight() && !firstLoading) {
            drawSignature(event, eventType);
        }
        if (firstLoading) {
            clearBtnAction();
            firstLoading = false;
        }
    }

    private void drawSignature(InputEvent event, EventType eventType) {
        double x;
        double y;
        if (event instanceof TouchEvent touchEvent) {
            x = touchEvent.getTouchPoint().getX();
            y = touchEvent.getTouchPoint().getY();
        } else {
            x = ((MouseEvent) event).getX();
            y = ((MouseEvent) event).getY();
        }
        gc.beginPath();
        // for dot
        if (EventType.CLICK == eventType) {
            // for the bounding box
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            gc.moveTo(x - 0.5, y); // mouse click event
        } else {
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
            gc.moveTo(lastX, lastY);
        }
        gc.lineTo(x, y);
        gc.stroke();
        lastX = x;
        lastY = y;
        isSigned = true;
        showPreview();
    }

    private void resetXAndY(InputEvent event) {
        // for touch event, it can jump from Release to Drag event directly on tapping the screen.
        lastX = -1;
        lastY = -1;
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
        previewSignatureImageView.setImage(null);
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
            int width = (int) (maxX - minX);
            int height = (int) (maxY - minY);

            // just a check to ensure valid/big signature is provided.
            if (width < 20 || height < 20) {
                messageLabel.setText("Kindly provide a valid or larger signature.");
                return;
            }
            minX = Math.max(minX, 0);
            minY = Math.max(minY, 0);

            width = (int) Math.min(maxX - minX, canvas.getWidth() - minX);
            height = (int) Math.min(maxY - minY, canvas.getHeight() - minY);

            BufferedImage boundedBox = image.getSubimage((int) minX, (int) minY, width, height);
            BufferedImageOp resampleOpOri = new ResampleOp(RAW_WIDTH, RAW_HEIGHT, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredOri = resampleOpOri.filter(boundedBox, null);
            BufferedImageOp resampleOpCompressed = new ResampleOp(COMPRESSED_WIDTH, COMPRESSED_HEIGHT, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredCompressed = resampleOpCompressed.filter(boundedBox, null);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(filteredOri, "png", byteArrayOutputStream);
            byteArrayOutputStream.close();
            byte[] data = byteArrayOutputStream.toByteArray();

            if (data.length < 2048) {
                LOGGER.log(Level.WARNING, () -> "Signature byte size: " + data.length);
                messageLabel.setText("Kindly provide a valid or larger signature.");
                return;
            }

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

    private void showPreview() {
        AtomicInteger minXAtomicInt = new AtomicInteger((int) minX);
        AtomicInteger minYAtomicInt = new AtomicInteger((int) minY);
        if (Double.MAX_VALUE == minX || Double.MAX_VALUE == minY) {
            minXAtomicInt.set(0);
            minYAtomicInt.set(0);
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage writableImage = canvas.snapshot(params, null);

        double finalMaxX = maxX;
        double finalMaxY = maxY;
        App.getThreadPool().execute(() -> {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            // if less than 0, then set as 0
            minXAtomicInt.set(Math.max(minXAtomicInt.get(), 0));
            minYAtomicInt.set(Math.max(minYAtomicInt.get(), 0));

            int width = (int) Math.min(finalMaxX - minXAtomicInt.get(), canvas.getWidth() - minXAtomicInt.get());
            int height = (int) Math.min(finalMaxY - minYAtomicInt.get(), canvas.getHeight() - minYAtomicInt.get());
            // throws error if width=0, height=0 for subImage
            width = Math.max(1, width);
            height = Math.max(1, height);
            BufferedImage boundedBox = bufferedImage.getSubimage(minXAtomicInt.get(), minYAtomicInt.get(), width, height);
            BufferedImageOp resampleOpOri = new ResampleOp(RAW_WIDTH, RAW_HEIGHT, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredOri = resampleOpOri.filter(boundedBox, null);
            Platform.runLater(() -> previewSignatureImageView.setImage(SwingFXUtils.toFXImage(filteredOri, null)));
        });

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
