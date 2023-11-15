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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 05/11/23
 */
public class SignatureController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(SignatureController.class);
    private static final int PADDING = 10;
    private static final int RAW_SIZE = 300;
    private static final int DPI = 300;
    private static final int COMPRESSED_SIZE = 100;
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

    public void initialize() {
        clearBtn.setOnAction(this::clearBtnAction);
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

        canvas.setOnMouseDragged(event -> {
            if (lastX >= 0 && lastX <= canvas.getWidth() && lastY >= 0 && lastY <= canvas.getHeight()) {
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
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                lastX = event.getX();
                lastY = event.getY();
                isSigned = true;
            }
        });
        canvas.setOnMouseReleased(event -> {
            // for touch event, it can jump from Release to Drag event directly on tapping the screen.
            lastX = -1;
            lastY = -1;
        });
        arcLbl.setText("e-ARC: " + ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getArcNo());
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
            LOGGER.log(Level.INFO, ex::getMessage);
        }
    }

    private void confirmNo(ActionEvent actionEvent) {
        confirmVbox.setVisible(false);
        enableControls(backBtn, clearBtn, saveSignatureBtn);
    }

    private void clearBtnAction(ActionEvent event) {
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
            if (width >= height) {
                int extra = (width - height) / 2;
                minY -= extra;
                maxY += extra;
            } else {
                int extra = (height - width) / 2;
                minX -= extra;
                maxX += extra;
            }
            minX = Math.max(minX - PADDING, 0);
            minY = Math.max(minY - PADDING, 0);
            width = (int) Math.min(maxX - minX + PADDING, canvas.getWidth() - minX);
            height = (int) Math.min(maxY - minY + PADDING, canvas.getHeight() - minY);

            BufferedImage boundedBox = image.getSubimage((int) minX, (int) minY, width, height);
            BufferedImageOp resampleOpOri = new ResampleOp(RAW_SIZE, RAW_SIZE, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredOri = resampleOpOri.filter(boundedBox, null);
            BufferedImageOp resampleOpCompressed = new ResampleOp(COMPRESSED_SIZE, COMPRESSED_SIZE, ResampleOp.FILTER_LANCZOS);
            BufferedImage filteredCompressed = resampleOpCompressed.filter(boundedBox, null);
            ImageIO.write(filteredCompressed, "png", new File(IMG_SIGNATURE_COMPRESSED_FILE));
            setMetadataAndSave(filteredOri);

            SaveEnrollmentDetail saveEnrollmentDetail = ArcDetailsHolder.getArcDetailsHolder().getSaveEnrollmentDetail();
            saveEnrollmentDetail.setSignature(Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of(IMG_SIGNATURE_FILE))));
            saveEnrollmentDetail.setSignatureCompressed(Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of(IMG_SIGNATURE_COMPRESSED_FILE))));
            saveEnrollmentDetail.setEnrollmentStatus("SignatureCompleted");
            SaveEnrollmentDetailUtil.writeToFile(saveEnrollmentDetail);
            App.setRoot("biometric_capture_complete");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "**saveSignatureBtnActionError: " + ex.getMessage());
            messageLabel.setText(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    private void setMetadataAndSave(BufferedImage bufferedImage) {
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(new File(IMG_SIGNATURE_FILE))) {
            ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("png").next();
            ImageWriteParam imageWriterDefaultWriteParam = imageWriter.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = imageWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriterDefaultWriteParam);
            IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
            horiz.setAttribute("value", Double.toString(DPI));
            IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
            vert.setAttribute("value", Double.toString(DPI));
            IIOMetadataNode dim = new IIOMetadataNode("Dimension");
            dim.appendChild(horiz);
            dim.appendChild(vert);
            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
            root.appendChild(dim);
            metadata.mergeTree("javax_imageio_1.0", root);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(new IIOImage(bufferedImage, null, metadata));
        } catch (Exception e) {
            throw new GenericException(e.getMessage());
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
