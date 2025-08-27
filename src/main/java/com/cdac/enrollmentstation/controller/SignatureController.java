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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
    private static final int PADDING = 3;
    private static final int RAW_WIDTH = 319;
    private static final int RAW_HEIGHT = 59;
    private static final int COMPRESSED_WIDTH = 271; // 319*0.85
    private static final int COMPRESSED_HEIGHT = 50;//59*0.85
    private static final String IMG_SIGNATURE_FILE;
    private static final String IMG_SIGNATURE_COMPRESSED_FILE;
    private static final int IMG_SIGNATURE_COMPRESSED_MIN_SIZE_IN_BYTES;
    private static final int IMG_SIGNATURE_COMPRESSED_MAX_SIZE_IN_BYTES;

    static {
        try {
            IMG_SIGNATURE_FILE = PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_FILE);
            IMG_SIGNATURE_COMPRESSED_FILE = PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_COMPRESSED_FILE);
            IMG_SIGNATURE_COMPRESSED_MIN_SIZE_IN_BYTES = Integer.parseInt(PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_COMPRESSED_MIN_SIZE));
            IMG_SIGNATURE_COMPRESSED_MAX_SIZE_IN_BYTES = Integer.parseInt(PropertyFile.getProperty(PropertyName.IMG_SIGNATURE_COMPRESSED_MAX_SIZE));
        } catch (Exception ex) {
            throw new GenericException(ex.getMessage());
        }
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
    // sometimes, when touch screen is used, drag event is triggered even before mouse pressed event.
    // which is unexpected.
    private boolean mousePressedEventActivated = false;
    private boolean forDot = false;


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
            gc.beginPath();
            gc.moveTo(lastX, lastY);
            forDot = true;
            mousePressedEventActivated = true;
        });

        // called by JavaFx even when mouse release outside
        canvas.setOnMouseReleased(event -> {
            mousePressedEventActivated = false;
        });

        // called by JavaFx even outside canvas (when mouse drag from inside and goes outside canvas)
        canvas.setOnMouseDragged(this::onMouseDragAction);

        // called by JavaFx after mouse-press and mouse-release cycle.
        // not called by JavaFx when mouse pressed inside canvas but released outside the canvas
        canvas.setOnMouseClicked(this::mousePressedReleasedCycleAction);
        arcLbl.setText("e-ARC: " + ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getArcNo());
    }

    private void mousePressedReleasedCycleAction(MouseEvent mouseEvent) {
        if (forDot) {
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();
            if (x >= 0 && x <= canvas.getWidth()) {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
            if (y >= 0 && y <= canvas.getHeight()) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            gc.lineTo(x, y);
            gc.stroke();
            gc.closePath();
            isSigned = true;
            showPreview(minX, minY, maxX, maxY);
        }
    }

    private void onMouseDragAction(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        if (x > 0 && x < canvas.getWidth() && y > 0 && y < canvas.getHeight() && mousePressedEventActivated) {
            drawSignature(x, y);
            showPreview(minX, minY, maxX, maxY);
        }
        forDot = false;

        if (!mousePressedEventActivated) {
            LOGGER.warning("**Mouse dragged event triggered even without mouse pressed event called by JavaFx.");
        }
    }

    private void drawSignature(double x, double y) {
        gc.beginPath();
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
        gc.lineTo(x, y);
        gc.stroke();
        lastX = x;
        lastY = y;
        isSigned = true;
    }

    private void showPreview(double minX, double minY, double maxX, double maxY) {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage writableImage = canvas.snapshot(params, null);
        App.getThreadPool().execute(() -> {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            int width = (int) Math.min(maxX - minX, canvas.getWidth() - minX);
            int height = (int) Math.min(maxY - minY, canvas.getHeight() - minY);

            int bMinX = (int) minX;
            int bMinY = (int) minY;

            if (bMinX > PADDING) {
                bMinX = bMinX - PADDING;
                width = width + PADDING;
            }
            if (bMinY > PADDING) {
                bMinY = bMinY - PADDING;
                height = height + PADDING;
            }
            if (maxX + PADDING < canvas.getWidth()) {
                width = width + PADDING;
            }
            if (maxY + PADDING < canvas.getHeight()) {
                height = height + PADDING;
            }
            BufferedImage imageBoundedBox = bufferedImage.getSubimage(bMinX, bMinY, width, height);
            BufferedImage resizedImage = resizeImage(width, height, imageBoundedBox, RAW_WIDTH, RAW_HEIGHT);
            Platform.runLater(() -> previewSignatureImageView.setImage(SwingFXUtils.toFXImage(resizedImage, null)));
        });
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
            App.setRoot("photo");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }

    private void confirmNo(ActionEvent actionEvent) {
        confirmVbox.setVisible(false);
        enableControls(backBtn, clearBtn, saveSignatureBtn);
    }

    private void clearBtnAction() {
        messageLabel.setText("Kindly sign inside the black box and click 'SAVE SIGNATURE' button to proceed.");
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
        try {
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage writableImage = canvas.snapshot(params, null);
            BufferedImage image = SwingFXUtils.fromFXImage(writableImage, null);
            int width = (int) (maxX - minX);
            int height = (int) (maxY - minY);

            // just a check to ensure valid/big signature is provided.
            if (width < 20 || height < 20) {
                messageLabel.setText("Kindly provide a valid or larger signature.");
                return;
            }

            if (minX > PADDING) {
                minX = minX - PADDING;
                width = width + PADDING;
            }
            if (minY > PADDING) {
                minY = minY - PADDING;
                height = height + PADDING;
            }
            if (maxX + PADDING < canvas.getWidth()) {
                width = width + PADDING;
            }
            if (maxY + PADDING < canvas.getHeight()) {
                height = height + PADDING;
            }

            BufferedImage imageBoundedBox = image.getSubimage((int) minX, (int) minY, width, height);
            BufferedImage resizedImage = resizeImage(width, height, imageBoundedBox, RAW_WIDTH, RAW_HEIGHT);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", byteArrayOutputStream);
            byteArrayOutputStream.close();
            byte[] data = byteArrayOutputStream.toByteArray();

            if (data.length < IMG_SIGNATURE_COMPRESSED_MIN_SIZE_IN_BYTES) {
                LOGGER.log(Level.WARNING, () -> "Signature byte size: " + data.length);
                messageLabel.setText("Kindly provide a larger signature.");
                return;
            }
            if (data.length > IMG_SIGNATURE_COMPRESSED_MAX_SIZE_IN_BYTES) {
                LOGGER.log(Level.WARNING, () -> "Signature byte size: " + data.length);
                messageLabel.setText("Kindly provide a smaller signature.");
                return;
            }

            // Convert the resized BufferedImage to WritableImage
            WritableImage wImage = new WritableImage(resizedImage.getWidth(), resizedImage.getHeight());
            wImage = SwingFXUtils.toFXImage(resizedImage, wImage);
            // Create a Canvas and draw the image on it
            double rectWidthMid = RAW_WIDTH / (double) 2;
            double rectHeightMid = RAW_HEIGHT / (double) 2;
            Canvas imageCanvas = new Canvas(RAW_WIDTH, RAW_HEIGHT);
            imageCanvas.getGraphicsContext2D().drawImage(wImage, rectWidthMid - (resizedImage.getWidth() / (double) 2), rectHeightMid - (resizedImage.getHeight() / (double) 2));

            SnapshotParameters imageParams = new SnapshotParameters();
            imageParams.setFill(Color.TRANSPARENT);
            wImage = imageCanvas.snapshot(imageParams, null);
            BufferedImage finalImage = SwingFXUtils.fromFXImage(wImage, null);

            Path signaturePath = Paths.get(IMG_SIGNATURE_FILE);
            ImageIO.write(finalImage, "png", signaturePath.toFile());

            // for compressed image
            BufferedImage resizedImageCompressed = resizeImage(width, height, imageBoundedBox, COMPRESSED_WIDTH, COMPRESSED_HEIGHT);
            // Convert the resized BufferedImage to WritableImage
            wImage = new WritableImage(resizedImageCompressed.getWidth(), resizedImageCompressed.getHeight());
            wImage = SwingFXUtils.toFXImage(resizedImageCompressed, wImage);
            // Create a Canvas and draw the image on it
            rectWidthMid = COMPRESSED_WIDTH / (double) 2;
            rectHeightMid = COMPRESSED_HEIGHT / (double) 2;
            Canvas imageCompressedCanvas = new Canvas(COMPRESSED_WIDTH, COMPRESSED_HEIGHT);
            imageCompressedCanvas.getGraphicsContext2D().drawImage(wImage, rectWidthMid - (resizedImageCompressed.getWidth() / (double) 2), rectHeightMid - (resizedImageCompressed.getHeight() / (double) 2));

            imageParams = new SnapshotParameters();
            imageParams.setFill(Color.TRANSPARENT);
            wImage = imageCompressedCanvas.snapshot(imageParams, null);
            BufferedImage finalImageCompressed = SwingFXUtils.fromFXImage(wImage, null);


            Path signatureCompressedPath = Paths.get(IMG_SIGNATURE_COMPRESSED_FILE);
            ImageIO.write(finalImageCompressed, "png", signatureCompressedPath.toFile());

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

    private int[] shrinkUntilBothFit(double w, double h, int requiredWidth, int requiredHeight) {
        double hRem = requiredHeight / h;
        double width = w * hRem;
        double height = requiredHeight;

        if (width > requiredWidth) {
            double wRem = requiredWidth / width;
            height = requiredHeight * wRem;
        }
        return new int[]{(int) width, (int) height};
    }

    private int[] shrinkUntilHeightFit(double w, double h, int requiredHeight) {
        double hRem = requiredHeight / h;
        double width = w * hRem;
        return new int[]{(int) width, requiredHeight};
    }

    private int[] shrinkUntilWidthFit(double w, double h, int requiredWidth) {
        double wRem = requiredWidth / w;
        double height = h * wRem;
        return new int[]{requiredWidth, (int) height};
    }

    private int[] enlargeBothUntilFit(double w, double h, int requiredWidth, int requiredHeight) {
        double width = w;
        double height = h;
        double counter = 1;

        while (width < requiredWidth && height < requiredHeight) {
            width = w * counter;
            height = h * counter;
            counter += 0.1;
        }
        return new int[]{(int) width, (int) height};
    }


    private BufferedImage resizeImage(int width, int height, BufferedImage boundedBox, int requiredWidth, int requiredHeight) {
        int[] widthHeight;
        if (height > RAW_HEIGHT) {
            if (width > RAW_WIDTH) {
                widthHeight = shrinkUntilBothFit(width, height, requiredWidth, requiredHeight);
                width = widthHeight[0];
                height = widthHeight[1];
            } else if (width < RAW_WIDTH) {
                widthHeight = shrinkUntilHeightFit(width, height, requiredHeight);
                width = widthHeight[0];
                height = widthHeight[1];
            }
        } else if (height < RAW_HEIGHT) {
            if (width > RAW_WIDTH) {
                widthHeight = shrinkUntilWidthFit(width, height, requiredWidth);
                width = widthHeight[0];
                height = widthHeight[1];
            } else if (width < RAW_WIDTH) {
                widthHeight = enlargeBothUntilFit(width, height, requiredWidth, requiredHeight);
                width = widthHeight[0];
                height = widthHeight[1];
            }
        }
        BufferedImageOp boundedBoxResampleOp = new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS);
        boundedBox = boundedBoxResampleOp.filter(boundedBox, null);
        return boundedBox;
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
        clearBtnAction();
        updateUi("Something went wrong. Kindly try again.");
    }
}
