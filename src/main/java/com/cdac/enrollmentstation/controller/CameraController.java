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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;
import static com.cdac.enrollmentstation.model.ArcDetailsHolder.getArcDetailsHolder;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */
public class CameraController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(CameraController.class);
    private static final int COUNTDOWN_IN_SEC = 5;
    private static final AtomicInteger COUNTDOWN = new AtomicInteger(COUNTDOWN_IN_SEC);
    private static final int FIXED_DELAY_TIME_IN_MILLIS = 5; // in milliseconds
    private static final int EXECUTOR_SHUTDOWN_WAIT_TIME_IN_MILLIS = 50; // in milliseconds
    private static final int IMAGE_CAPTURE_LIMIT = 30;
    private static final String IMG_PHOTO_INPUT_FILE;
    private static final String PYTHON_IMAGE_PROCESSOR_COMMAND;
    private static final String IMG_PHOTO_FILE;
    private static final String IMG_PHOTO_COMPRESSED_FILE;

    private static final Image OUT_OF_FRAME_IMAGE;
    private static final Image NO_MASK_IMAGE;
    private static final Image NO_GLASSES_IMAGE;
    private static final Image CLOCK_COLOR_IMAGE;
    private static final Image ANTI_CLOCK_COLOR_IMAGE;
    private static final Image RIGHT_ROTATE_COLOR_IMAGE;
    private static final Image LEFT_ROTATE_COLOR_IMAGE;
    private static final Image CHIN_DOWN_COLORED_IMAGE;
    private static final Image CHIN_UP_COLOR_IMAGE;
    private static final Image TICK_GREEN_IMAGE;
    private static final int MIN_PHOTO_COMPRESSED_SIZE_IN_BYTES = 3072; // 3kb

    static {
        try {
            IMG_PHOTO_INPUT_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_PHOTO_INPUT_FILE), PropertyName.IMG_PHOTO_INPUT_FILE);
            IMG_PHOTO_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_PHOTO_FILE), PropertyName.IMG_PHOTO_FILE);
            IMG_PHOTO_COMPRESSED_FILE = requireNonBlank(PropertyFile.getProperty(PropertyName.IMG_PHOTO_COMPRESSED_FILE), PropertyName.IMG_PHOTO_COMPRESSED_FILE);
            PYTHON_IMAGE_PROCESSOR_COMMAND = requireNonBlank(PropertyFile.getProperty(PropertyName.PYTHON_IMAGE_PROCESSOR_COMMAND), PropertyName.PYTHON_IMAGE_PROCESSOR_COMMAND);
            // loads --> /img/
            NO_MASK_IMAGE = loadFileFromImgDirectory("no_mask.png");
            NO_GLASSES_IMAGE = loadFileFromImgDirectory("no_goggles.png");
            // loads --> /facecode
            OUT_OF_FRAME_IMAGE = loadFileFromFaceCodeDirectory("out_of_frame_color.png");
            CLOCK_COLOR_IMAGE = loadFileFromFaceCodeDirectory("clock_color.png");
            ANTI_CLOCK_COLOR_IMAGE = loadFileFromFaceCodeDirectory("anti_clock_color.png");
            RIGHT_ROTATE_COLOR_IMAGE = loadFileFromFaceCodeDirectory("right_rotate_color.png");
            LEFT_ROTATE_COLOR_IMAGE = loadFileFromFaceCodeDirectory("left_rotate_color.png");
            CHIN_DOWN_COLORED_IMAGE = loadFileFromFaceCodeDirectory("chin_down_colored.png");
            CHIN_UP_COLOR_IMAGE = loadFileFromFaceCodeDirectory("chin_up_color.png");
            TICK_GREEN_IMAGE = loadFileFromFaceCodeDirectory("tick.png");
        } catch (Exception ex) {
            throw new GenericException(ex.getMessage());
        }
    }

    @FXML
    private BorderPane rootBorderPane;


    @FXML
    private ImageView sunGlassIcon;

    @FXML
    private ImageView resultImageView;
    @FXML

    private ImageView iconFrame;
    @FXML

    private VBox confirmVbox;
    @FXML
    private Label confirmPaneLbl;
    @FXML
    private ImageView liveImageView;
    @FXML
    private Button confirmNoBtn;
    @FXML
    private Button confirmYesBtn;
    @FXML
    private Label arcLbl;
    @FXML
    private ImageView msgIcon;
    @FXML
    private Button savePhotoBtn;
    @FXML
    private Button backBtn;
    @FXML
    private Label messageLabel;
    @FXML
    private Button startStopCameraBtn;
    private volatile boolean isCameraActive = false;
    private volatile boolean stopLive = false;
    //    private static final int CAMERA_ID = Integer.parseInt(PropertyFile.getProperty(PropertyName.CAMERA_ID))
    private volatile int cameraId; // default 0
    private VideoCapture videoCapture;

    private final AtomicInteger imageCaptureCount = new AtomicInteger(0);
    private volatile boolean validImage = false;
    private volatile boolean stopBtnClicked = false;


    private ScheduledExecutorService scheduledExecutorService;

    // automatically called by JavaFx runtime.
    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

        cameraId = Integer.parseInt(PropertyFile.getProperty(PropertyName.CAMERA_ID).trim());
        // set action for button click
        startStopCameraBtn.setOnAction(this::startCamera);
        savePhotoBtn.setOnAction(this::savePhoto);
        savePhotoBtn.setDisable(!validImage);
        backBtn.setOnAction(this::back);
        confirmNoBtn.setOnAction(this::confirmNo);
        confirmYesBtn.setOnAction(this::confirmYes);
        if (getArcDetailsHolder().getArcDetail() != null && getArcDetailsHolder().getArcDetail().getArcNo() != null) {
            arcLbl.setText("e-ARC: " + getArcDetailsHolder().getArcDetail().getArcNo());
        }
    }


    private void confirmYes(ActionEvent actionEvent) {
        confirmVbox.setVisible(false);
        savePhotoBtn.setDisable(!validImage);
        enableControls(startStopCameraBtn, backBtn);
        try {
            ArcDetailsHolder holder = getArcDetailsHolder();
            if (holder.getArcDetail().getBiometricOptions().trim().equalsIgnoreCase("Photo")) {
                App.setRoot("biometric_enrollment");
            } else {
                App.setRoot("iris");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }

    }

    private void confirmNo(ActionEvent actionEvent) {
        confirmVbox.setVisible(false);
        savePhotoBtn.setDisable(!validImage);
        enableControls(startStopCameraBtn, backBtn);
    }

    // action for back button
    private void back(ActionEvent actionEvent) {
        confirmVbox.setVisible(true);
        ArcDetailsHolder holder = getArcDetailsHolder();
        // Added For Biometric Options
        if (holder.getArcDetail().getBiometricOptions().trim().equalsIgnoreCase("Photo")) {
            confirmPaneLbl.setText("Click 'Yes' to Fetch e-ARC or Click 'No' to Capture photo");
        } else {
            confirmPaneLbl.setText("Click 'Yes' to Scan Iris or Click 'No' to Capture photo");
        }
        disableControls(startStopCameraBtn, backBtn, savePhotoBtn);
    }

    // action for save photo button
    private void savePhoto(ActionEvent actionEvent) {
        try {
            addPhoto(ArcDetailsHolder.getArcDetailsHolder().getSaveEnrollmentDetail());
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
            backBtn.setDisable(false);
            return;
        }
        try {
            if (ArcDetailsHolder.getArcDetailsHolder().getArcDetail().isSignatureRequired()) {
                App.setRoot("signature");
                return;
            }
            App.setRoot("biometric_capture_complete");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
        }
    }

    private void addPhoto(SaveEnrollmentDetail saveEnrollmentDetail) {
        Path photoPath = Paths.get(IMG_PHOTO_FILE);
        Path compressPhotoPath = Paths.get(IMG_PHOTO_COMPRESSED_FILE);
        // check if photo files exist.
        if (!Files.exists(photoPath) || !Files.exists(compressPhotoPath)) {
            LOGGER.log(Level.SEVERE, "Both or either sub photo and compress photo file not found.");
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        try {
            saveEnrollmentDetail.setPhoto(Base64.getEncoder().encodeToString(Files.readAllBytes(photoPath)));
            saveEnrollmentDetail.setPhotoCompressed(Base64.getEncoder().encodeToString(Files.readAllBytes(compressPhotoPath)));
            saveEnrollmentDetail.setEnrollmentStatus("PhotoCompleted");
            SaveEnrollmentDetailUtil.writeToFile(saveEnrollmentDetail);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }


    // action for start/stop button
    public void startCamera(ActionEvent actionEvent) {
        imageCaptureCount.getAndSet(0);
        validImage = false;
        // if active, then stop it
        if (isCameraActive) {
            stopBtnClicked = true;
            disableControls(startStopCameraBtn);
            stopLive = true;
            startStopCameraBtn.setText("Start Camera");
            backBtn.setDisable(false);
            updateImageView(sunGlassIcon, NO_GLASSES_IMAGE);
            updateImageView(iconFrame, NO_MASK_IMAGE);
            updateImageView(msgIcon, null);
            updateImageView(resultImageView, null);
            // stop the camera and thread executor
            shutdownExecutorServiceAndReleaseResource();
            enableControls(startStopCameraBtn);
        } else {
            // active status to be used by worker thread
            videoCapture = new VideoCapture(cameraId);
            if (!videoCapture.isOpened()) {
                LOGGER.log(Level.SEVERE, () -> "Unable to open camera.");
                messageLabel.setText("Unable to start camera. Please check camera and try again.");
                return;
            }
            isCameraActive = true;
            stopLive = false;
            stopBtnClicked = false;
            //disable controls during the countdown
            startStopCameraBtn.setText("Stop Camera");
            disableControls(startStopCameraBtn, backBtn, savePhotoBtn);
            // clears icons
            updateImageView(sunGlassIcon, null);
            updateImageView(iconFrame, null);
            updateImageView(msgIcon, null);
            updateImageView(resultImageView, null);
            // thread for showing live image
            App.getThreadPool().execute(this::liveImageThread);
            // thread for capturing photo
            App.getThreadPool().execute(this::capturePhotoThread);
        }
    }

    private void capturePhotoThread() {
        // Display count down on UI
        // should run on worker thread
        COUNTDOWN.set(COUNTDOWN_IN_SEC);

        while (COUNTDOWN.get() > 0) {
            try {
                updateUi("(" + COUNTDOWN.get() + ") Move your body to fit in red box");
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                COUNTDOWN.decrementAndGet();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        // enable controls after countdown
        Platform.runLater(() -> {
            startStopCameraBtn.setText("Stop Camera");
            startStopCameraBtn.setDisable(false);
            messageLabel.setText("");
        });
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(this::grabFrame, 0, FIXED_DELAY_TIME_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void liveImageThread() {
        Mat matrix = new Mat();
        while (!stopLive) {
            boolean read = videoCapture.read(matrix);
            if (!read) {
                // only log when camera is active
                if (isCameraActive) {
                    LOGGER.log(Level.INFO, () -> "Failed to capture the photo.");
                }
                return;
            }
            // 440x440 square box
            Imgproc.rectangle(matrix,                   //Matrix obj of the image
                    new Point(100, 38),            //p1
                    new Point(540, 478),           //p2
                    new Scalar(0, 0, 255),              //Scalar object for color
                    4                                   //Thickness of the line
            );

            updateImageView(liveImageView, mat2Image(matrix));
        }
    }

    public Image mat2Image(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }


    private void updateImageView(ImageView view, Image image) {
        Platform.runLater(() -> view.setImage(image));
    }


    private void grabFrame() {
        if (!isCameraActive) {
            LOGGER.log(Level.INFO, () -> "*** Valid photo already captured or camera already stopped.");
            shutdownExecutorServiceAndReleaseResource();
            return;
        }
        if (!videoCapture.isOpened()) {
            LOGGER.log(Level.INFO, () -> "Camera is not opened.");
            return;
        }
        Mat matrix = new Mat();
        // read the current matrix
        videoCapture.read(matrix);
        // if the matrix is empty, return
        if (matrix.empty()) {
            LOGGER.log(Level.INFO, () -> "Read empty matrix");
            return;
        }
        // writes to /usr/share/enrollment/images/input.jpeg
        Imgcodecs.imwrite(IMG_PHOTO_INPUT_FILE, matrix);
        this.imageCaptureCount.getAndIncrement();
        // stop camera after IMAGE_CAPTURE_LIMIT shots
        if (imageCaptureCount.get() > IMAGE_CAPTURE_LIMIT) {
            shutdownExecutorServiceAndReleaseResource();
            updateImageView(sunGlassIcon, NO_GLASSES_IMAGE);
            updateImageView(iconFrame, NO_MASK_IMAGE);
            updateImageView(msgIcon, null);
            Platform.runLater(() -> {
                messageLabel.setText("Capture threshold reached. Start camera again.");
                startStopCameraBtn.setText("Start Camera");
            });
            enableControls(backBtn);
            return;
        }
        BufferedReader input = null;
        BufferedReader error = null;
        try {
            Process pr = Runtime.getRuntime().exec(PYTHON_IMAGE_PROCESSOR_COMMAND);
            input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String eline;
            while ((eline = error.readLine()) != null) {
                LOGGER.log(Level.SEVERE, eline);
            }
            String line;
            while ((line = input.readLine()) != null) {
                if (line.contains("Valid")) {
                    byte[] bytes = Files.readAllBytes(Path.of(PropertyFile.getProperty(PropertyName.IMG_PHOTO_COMPRESSED_FILE)));
                    LOGGER.log(Level.INFO, () -> "Compressed photo size in bytes: " + bytes.length);
                    if (bytes.length >= MIN_PHOTO_COMPRESSED_SIZE_IN_BYTES) {
                        validImage = true;
                        stopLive = true;
                    }
                } else if (line.contains("Message=")) {
                    String subString = line.substring("Message= ".length());
                    Platform.runLater(() -> messageLabel.setText(subString));
                    hintMessage(subString);
                } else {
                    String finalLine = line; // to be used in lambda
                    LOGGER.log(Level.INFO, () -> "Valued read from python process: " + finalLine);
                }
            }
            int exitVal = pr.waitFor();
            LOGGER.log(Level.INFO, () -> "Process Exit Value: " + exitVal);
            updateUIOnValidImageAndNormalExit(exitVal);
        } catch (Exception ex) {
            Platform.runLater(() -> messageLabel.setText("Something went wrong while capturing photo."));
            LOGGER.log(Level.SEVERE, ex::getMessage);
            shutdownExecutorServiceAndReleaseResource();
            Thread.currentThread().interrupt();
            enableControls(backBtn);
        } finally {
            try {
                if (error != null) {
                    error.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e::getMessage);
            }
        }
    }

    private void updateUIOnValidImageAndNormalExit(int exitVal) throws IOException {
        if (exitVal == 0 && validImage && !stopBtnClicked) {
            Image image = new Image(Files.newInputStream(Paths.get(IMG_PHOTO_FILE)));
            resultImageView.setImage(image);
            updateImageView(iconFrame, TICK_GREEN_IMAGE);
            updateImageView(msgIcon, null);
            updateImageView(sunGlassIcon, null);
            Platform.runLater(() -> {
                // camSlider.setVisible(true)
                // brightness.setVisible(true)
                messageLabel.setText("Click 'Restart Camera' button if the left photo is unclear.");
                startStopCameraBtn.setText("Restart Camera");
                enableControls(startStopCameraBtn, backBtn, savePhotoBtn);
            });
            shutdownExecutorServiceAndReleaseResource();
        }
    }

    private void hintMessage(String message) {
        if (message.contains("Face Going out of matrix")) {
            updateImageView(msgIcon, OUT_OF_FRAME_IMAGE);
        } else if (message.contains("ROTATE Face CLOCK")) {
            updateImageView(msgIcon, CLOCK_COLOR_IMAGE);
        } else if (message.contains("ROTATE Face ANTI")) {
            updateImageView(msgIcon, ANTI_CLOCK_COLOR_IMAGE);
        } else if (message.contains("ROTATE Face RIGHT")) {
            updateImageView(msgIcon, RIGHT_ROTATE_COLOR_IMAGE);
        } else if (message.contains("ROTATE Face LEFT")) {
            updateImageView(msgIcon, LEFT_ROTATE_COLOR_IMAGE);
        } else if (message.contains("CHIN DOWN")) {
            updateImageView(msgIcon, CHIN_DOWN_COLORED_IMAGE);
        } else if (message.contains("CHIN UP")) {
            updateImageView(msgIcon, CHIN_UP_COLOR_IMAGE);
        }
    }

    private static String requireNonBlank(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new GenericException(propertyName + " value is null or blank in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }
        return value;
    }

    private static Image loadFileFromFaceCodeDirectory(String filename) {
        InputStream inputStream = CameraController.class.getResourceAsStream("/facecode/" + filename);
        if (inputStream == null) {
            String errorMessage = filename + " not found in '/facecode/' directory";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new GenericException(errorMessage);
        }
        return new Image(inputStream);
    }

    private static Image loadFileFromImgDirectory(String filename) {
        InputStream inputStream = CameraController.class.getResourceAsStream("/img/" + filename);
        if (inputStream == null) {
            String errorMessage = filename + " not found in '/img/' directory";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new GenericException(errorMessage);
        }
        return new Image(inputStream);
    }

    private void shutdownExecutorServiceAndReleaseResource() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            try {
                // stop the timer
                scheduledExecutorService.shutdown();
                scheduledExecutorService.awaitTermination(EXECUTOR_SHUTDOWN_WAIT_TIME_IN_MILLIS, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOGGER.log(Level.INFO, ex::getMessage);
            }
            stopLive = true;
            try {
                if (videoCapture.isOpened()) {
                    videoCapture.release();
                    isCameraActive = false;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex::getMessage);
                Platform.runLater(() -> messageLabel.setText("Unable to close the camera."));
            }
        }
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

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        backBtn.setDisable(false);
        startStopCameraBtn.setDisable(false);
        updateUi("Something went wrong. Please try again");
    }
}
