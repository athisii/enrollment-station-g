package RealScan;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.controller.BaseController;
import com.cdac.enrollmentstation.dto.Fp;
import com.cdac.enrollmentstation.dto.SaveEnrollmentDetail;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ArcDetailsHolder;
import com.cdac.enrollmentstation.util.PropertyFile;
import com.cdac.enrollmentstation.util.SaveEnrollmentDetailUtil;
import com.innovatrics.commons.img.RawGrayscaleImage;
import com.innovatrics.iengine.ansiiso.AnsiIso;
import com.innovatrics.iengine.ansiiso.AnsiIsoImageFormatEnum;
import com.innovatrics.iengine.ansiiso.IEngineTemplateFormat;
import com.mantra.IMAGE_FORMAT;
import com.mantra.Utility;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static RealScan.RealScan_JNI.*;
import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;
import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_RS_ERR_MSG;
import static com.cdac.enrollmentstation.model.ArcDetailsHolder.getArcDetailsHolder;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

public class SlapScannerController implements BaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(SlapScannerController.class);
    private static final int TIME_TO_WAIT_FOR_NEXT_CAPTURE_IN_SEC = 1; // to be on safe side
    private static final int TIME_TO_WAIT_FOR_SWITCHING_FINGER_TYPE_TO_SCAN_IN_MILLIS = 100;
    // range: 0~7
    public static final String UNSUPPORTED_FINGER_SET_TYPE = "Unsupported finger set type.";
    private boolean isFpScanCompleted;
    // cannot be static.
    private final int fingerprintLivenessValue;

    private static final int FP_SEGMENT_WIDTH;
    private static final int FP_SEGMENT_HEIGHT;

    private static final int FP_NIST_VALUE;

    static {
        try {
            FP_SEGMENT_WIDTH = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_SEGMENT_WIDTH).trim());
            FP_SEGMENT_HEIGHT = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_SEGMENT_HEIGHT).trim());
            FP_NIST_VALUE = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_NIST_VALUE).trim());
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException("Not a number or no entry found.");
        }
    }

    {
        try {
            fingerprintLivenessValue = Integer.parseInt(PropertyFile.getProperty(PropertyName.FINGERPRINT_LIVENESS_VALUE).trim());
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException("Not a number or no entry found.");
        }
    }


    /* *********** GLOBAL MEMBER VARIABLES ************* */
    /* ****** ITS VALUE CHANGES AND ARE USED IN DIFFERENT METHODS ******/
    private volatile FingerSetType fingerSetTypeToScan = FingerSetType.LEFT; // global finger to scan holder to be used in common methods.
    private volatile boolean isFromPrevScan; // to avoid unnecessary wait.
    private volatile int jniReturnedCode;
    private volatile String jniErrorMsg;
    private volatile boolean isLeftFpDeviceInitialised; // to maintain device status.
    private volatile boolean isRightFpDeviceInitialised; // to maintain device status.
    private volatile int leftFpDeviceHandler;
    private volatile int rightFpDeviceHandler;
    private volatile int captureMode; // to be used when setting capture mode.
    private volatile int slapType; // to be used during segmentation.
    private volatile boolean duplicateFpFound;
    private Button currentEnabledButton;


    private final RSDeviceInfo leftFpDeviceInfo = new RSDeviceInfo();
    private final RSDeviceInfo rightFpDeviceInfo = new RSDeviceInfo();

    private static final AnsiIso ansiIso = new AnsiIso(); // for template conversion
    // GLOBAL map that stores scanned fingerprints. (fingerType -> RSImageInfo mapping)
    private final Map<Integer, RSImageInfo> scannedFingerTypeToRsImageInfoMap = new HashMap<>();
    private Map<String, Integer> leftFingerToFingerTypeLinkedHashMap; // left fingers to scan in finger type sequence (ordered as per SDK)(very important)
    private Map<String, Integer> rightFingerToFingerTypeLinkedHashMap; // right fingers to scan in finger type sequence (ordered as per SDK)(very important)
    private Map<String, Integer> thumbToFingerTypeLinkedHashMap;  // thumbs to scan in finger type sequence(ordered as per SDK)(very important)

    private static final Map<String, String> fingerAbrvToLFMap = new HashMap<>();

    /* ************************************************************************************ */

    static {
        fingerAbrvToLFMap.put("RT", "Right Thumb");
        fingerAbrvToLFMap.put("RI", "Right Index");
        fingerAbrvToLFMap.put("RM", "Right Middle");
        fingerAbrvToLFMap.put("RR", "Right Ring");
        fingerAbrvToLFMap.put("RL", "Right Little");

        fingerAbrvToLFMap.put("LT", "Left Thumb");
        fingerAbrvToLFMap.put("LI", "Left Index");
        fingerAbrvToLFMap.put("LM", "Left Middle");
        fingerAbrvToLFMap.put("LR", "Left Ring");
        fingerAbrvToLFMap.put("LL", "Left Little");
    }

    enum FingerSetType {
        LEFT, RIGHT, THUMB
    }

    @FXML
    private Label displayArcLabel;

    @FXML
    private Button captureIrisBtn;
    @FXML
    private Button leftScanBtn;
    @FXML
    private Button rightScanBtn;
    @FXML
    private Button thumbScanBtn;
    @FXML
    private Label messageLabel;
    @FXML
    private Button scanBtn;
    @FXML
    private Button backBtn;

    @FXML
    private AnchorPane confirmPane;
    @FXML
    private Button confirmYesBtn;
    @FXML
    private Button confirmNoBtn;


    @FXML
    private ImageView rawFingerprintImageView;
    @FXML
    private ImageView leftLittleFingerImageView;
    @FXML
    private ImageView leftRingFingerImageView;
    @FXML
    private ImageView leftMiddleFingerImageView;
    @FXML
    private ImageView leftIndexFingerImageView;
    @FXML
    private ImageView rightThumbImageView;
    @FXML
    private ImageView leftThumbImageView;
    @FXML
    private ImageView rightLittleFingerImageView;
    @FXML
    private ImageView rightRingFingerImageView;
    @FXML
    private ImageView rightMiddleFingerImageView;
    @FXML
    private ImageView rightIndexFingerImageView;
    @FXML
    private Text confirmText;
    @FXML
    private Label version;


    // calls automatically by JavaFX runtime

    public void initialize() {
        //To get the Version Number
        getVersion();
        scanBtn.setOnAction(event -> scanBtnAction());
        leftScanBtn.setOnAction(event -> leftScanBtnAction());
        rightScanBtn.setOnAction(event -> rightScanBtnAction());
        thumbScanBtn.setOnAction(event -> thumbScanBtnAction());
        backBtn.setOnAction(event -> backBtnAction());
        captureIrisBtn.setOnAction(event -> showIris());
        confirmNoBtn.setOnAction(event -> confirmStay());
        confirmYesBtn.setOnAction(event -> confirmBack());

        try {
            initIEngineLicense();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            messageLabel.setText(ex.getMessage());
            scanBtn.setDisable(true);
            return;
        }


        if (getArcDetailsHolder().getArcDetail() == null) {
            messageLabel.setText(ApplicationConstant.GENERIC_ERR_MSG);
            scanBtn.setDisable(true);
            return;
        }

        leftFingerToFingerTypeLinkedHashMap = getFingersToScanSeqMap(getArcDetailsHolder().getArcDetail().getFingers(), FingerSetType.LEFT);
        rightFingerToFingerTypeLinkedHashMap = getFingersToScanSeqMap(getArcDetailsHolder().getArcDetail().getFingers(), FingerSetType.RIGHT);
        thumbToFingerTypeLinkedHashMap = getFingersToScanSeqMap(getArcDetailsHolder().getArcDetail().getFingers(), FingerSetType.THUMB);

        if (getArcDetailsHolder().getArcDetail() != null && getArcDetailsHolder().getArcDetail().getArcNo() != null) {
            displayArcLabel.setText("e-ARC: " + getArcDetailsHolder().getArcDetail().getArcNo());
        }
    }

    private void getVersion() {
        String appVersionNumber = PropertyFile.getProperty(PropertyName.APP_VERSION_NUMBER);
        if (appVersionNumber == null || appVersionNumber.isEmpty()) {
            LOGGER.log(Level.SEVERE, () -> "No entry for '" + PropertyName.APP_VERSION_NUMBER + "' or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException("No entry for '" + PropertyName.APP_VERSION_NUMBER + "' or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }
        version.setText(appVersionNumber);
    }

    private void initSdkAndScanners() {
        if (isLeftFpDeviceInitialised) {
            releaseDevice(leftFpDeviceHandler);
        }
        if (isRightFpDeviceInitialised) {
            releaseDevice(rightFpDeviceHandler);
        }
        /*
        RS_InitSDK Error Code:
            RS_SUCCESS  - The SDK is successfully initialized and the connected devices are found.
            RS_ERR_CANNOT_GET_USB_DEVICE - Cannot get device information from USB.
            RS_ERR_SDK_ALREADY_INITIALIZED - There remain unreleased devices.
        */
        int numOfScanners = RS_InitSDK("", 0);
        jniReturnedCode = RS_GetLastError();
        if (jniReturnedCode != RS_SUCCESS && jniReturnedCode != RS_ERR_SDK_ALREADY_INITIALIZED) {
            LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }

        if (numOfScanners < 2) {
            LOGGER.log(Level.SEVERE, "Required number of biometric data capturing device(s) not connected.");
            throw new GenericException("Required number of biometric data capturing device(s) not connected. Kindly connect and try again.");
        }
        leftFpDeviceHandler = initDevice(0, leftFpDeviceInfo);
        isLeftFpDeviceInitialised = true;
        rightFpDeviceHandler = initDevice(1, rightFpDeviceInfo);
        isRightFpDeviceInitialised = true;
    }

    private int initDevice(int index, RSDeviceInfo rsDeviceInfo) {
        int deviceHandler;
         /*
        RS_InitDevice Error Code:
            RS_SUCCESS - The device is initialized successfully.
            RS_ERR_SDK_UNINITIALIZED - The SDK is not yet initialized.
            RS_ERR_INVALID_DEVICE_INDEX - The device index is invalid.
            RS_ERR_DEVICE_ALREADY_INITIALIZED - The device is already initialized.
            RS_ERR_CANNOT_OPEN_DEVICE - Cannot connect to the device.
            RS_ERR_SENSOR_DIRTY - A finger is on the sensor or Sensor is too dirty. Clean the sensor and try again
            RS_ERR_FINGER_EXIST - A finger is on the sensor or Sensor is too dirty. Clean the sensor and try again
         */
        deviceHandler = RS_InitDevice(index);
        // only stop if these errors occur
        jniReturnedCode = RS_GetLastError();
        if (jniReturnedCode == RS_ERR_SENSOR_DIRTY || jniReturnedCode == RS_ERR_FINGER_EXIST) {
            String errorMessage = RS_GetErrString(jniReturnedCode);
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new GenericException(errorMessage);
        }

        if (jniReturnedCode != RS_SUCCESS && jniReturnedCode != RS_ERR_DEVICE_ALREADY_INITIALIZED) {
            LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }
        /*
        RS_GetDeviceInfo Error Codes:
            RS_SUCCESS - The device information is read successfully.
            RS_ERR_INVALID_HANDLE - The device handle is invalid.
        */
        jniReturnedCode = RS_GetDeviceInfo(deviceHandler, rsDeviceInfo);
        if (jniReturnedCode != RS_SUCCESS) {
            LOGGER.log(Level.SEVERE, "Could not get device info.");
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }
        return deviceHandler;
    }


    private void initIEngineLicense() throws IOException {
        String licFilePath = PropertyFile.getProperty(PropertyName.LIC_IENGINE);
        if (licFilePath == null || licFilePath.isBlank()) {
            throw new GenericException("License property value is null or empty");
        }
        Path licensePath = Paths.get(licFilePath);
        if (Files.notExists(licensePath)) {
            LOGGER.log(Level.SEVERE, "License file not found");
            throw new GenericException("License file not found");
        }
        byte[] licBytes = Files.readAllBytes(licensePath);
        ansiIso.setLicenseContent(licBytes, licBytes.length);
        // throws AnsiIsoException
        ansiIso.init();
    }

    private void scanBtnAction() {
        isFpScanCompleted = false;
        disableControls(scanBtn, leftScanBtn, rightScanBtn, thumbScanBtn, backBtn, captureIrisBtn);
        try {
            initSdkAndScanners();
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
            scanBtn.setDisable(false);
            backBtn.setDisable(false);
            return;
        }
        clearFingerprintOnUI();
        App.getThreadPool().execute(this::startLeftScan);
    }

    private void leftScanBtnAction() {
        disableControls(scanBtn, leftScanBtn, rightScanBtn, thumbScanBtn, backBtn, captureIrisBtn);
        App.getThreadPool().execute(this::startLeftScan);
    }

    private void rightScanBtnAction() {
        disableControls(scanBtn, leftScanBtn, rightScanBtn, thumbScanBtn, backBtn, captureIrisBtn);
        App.getThreadPool().execute(this::startRightScan);
    }

    private void thumbScanBtnAction() {
        disableControls(scanBtn, leftScanBtn, rightScanBtn, thumbScanBtn, backBtn, captureIrisBtn);
        App.getThreadPool().execute(this::startThumbScan);
    }

    private void startLeftScan() {
        fingerSetTypeToScan = FingerSetType.LEFT;
        Platform.runLater(this::clearFingerprintOnUI);
        // display the message for 2 seconds.
        if (duplicateFpFound) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        duplicateFpFound = false;
        if (!isLeftFpDeviceInitialised) {
            LOGGER.log(Level.SEVERE, "Device is not initialised. Status of isDeviceInitialised is 'false'.");
            updateUi(GENERIC_RS_ERR_MSG);
            enableControls(backBtn, leftScanBtn);
            return;
        }

        if (leftFingerToFingerTypeLinkedHashMap.isEmpty()) {
            // runs in same thread.
            updateUi("No left fingers to scan. Going to scan right fingers...");
            startRightScan();
            return;
        }

        if (leftFingerToFingerTypeLinkedHashMap.size() > 3) {
            updateUi("Place your four left fingers on the sensor.");
        } else {
            // Place your Left Index, Left Middle finger(s) on the sensor.
            String fingers = leftFingerToFingerTypeLinkedHashMap.keySet().stream().map(fingerAbrvToLFMap::get).collect(Collectors.joining(", "));
            updateUi("Place your  " + fingers + " finger(s) on the sensor.");
        }

        // throws GenericException
        try {
            // when fingers are already placed on the sensor at the time of initialization, it fails
            setModeAndStartCapture();
        } catch (Exception ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, leftScanBtn);
        }

    }

    private void startRightScan() {
        fingerSetTypeToScan = FingerSetType.RIGHT;
        Platform.runLater(this::clearFingerprintOnUI);
        if (isFromPrevScan) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(TIME_TO_WAIT_FOR_NEXT_CAPTURE_IN_SEC));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!isRightFpDeviceInitialised) {
            LOGGER.log(Level.SEVERE, "Device is not initialised. Status of isDeviceInitialised is 'false'. ");
            updateUi(GENERIC_RS_ERR_MSG);
            enableControls(backBtn, rightScanBtn);
            return;
        }
        if (rightFingerToFingerTypeLinkedHashMap.isEmpty()) {
            // runs in same thread.
            updateUi("No right fingers to scan. Going to scan thumb...");
            startThumbScan();
            return;
        }
        if (rightFingerToFingerTypeLinkedHashMap.size() > 3) {
            updateUi("Place your four right fingers on the sensor.");
        } else {
            // Place your Right Index, Right Middle finger(s) on the sensor.
            String fingers = rightFingerToFingerTypeLinkedHashMap.keySet().stream().map(fingerAbrvToLFMap::get).collect(Collectors.joining(", "));
            updateUi("Place your " + fingers + " finger(s) on the sensor.");
        }

        // throws GenericException
        try {
            // when fingers are already placed on the sensor at the time of initialization, it fails
            setModeAndStartCapture();
        } catch (Exception ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, rightScanBtn);
        }


    }

    private void startThumbScan() {
        fingerSetTypeToScan = FingerSetType.THUMB;
        Platform.runLater(this::clearFingerprintOnUI);
        if (isFromPrevScan) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(TIME_TO_WAIT_FOR_NEXT_CAPTURE_IN_SEC));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!isLeftFpDeviceInitialised) {
            LOGGER.log(Level.SEVERE, "Device is not initialised. Status of isDeviceInitialised is 'false'.");
            updateUi(GENERIC_RS_ERR_MSG);
            enableControls(backBtn, thumbScanBtn);
            return;
        }
        if (thumbToFingerTypeLinkedHashMap.isEmpty()) {// runs in same thread.
            updateUi("No thumb to scan. Please wait. ");
            convertToTemplate();
            return;
        }
        if (thumbToFingerTypeLinkedHashMap.size() > 1) {
            updateUi("Place your two thumbs on the sensor.");
        } else {
            // Place your Left thumb on the sensor.
            String thumb = thumbToFingerTypeLinkedHashMap.keySet().stream().map(fingerAbrvToLFMap::get).collect(Collectors.joining(", "));
            updateUi("Place your " + thumb + " on the sensor.");
        }

        // throws GenericException
        try {
            // when fingers are already placed on the sensor at the time of initialization, it fails
            setModeAndStartCapture();
        } catch (Exception ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, thumbScanBtn);
        }
    }

    // called when capture succeeds or error occurs
    private void processCapturedFp(int deviceHandle, int errorCode, byte[] imageData, int imageWidth, int imageHeight) {
        Button button;  // local reference for pointing to different multiple buttons
        String successMessage;
        if (FingerSetType.LEFT == fingerSetTypeToScan) {
            scannedFingerTypeToRsImageInfoMap.clear(); // for fresh start
            button = leftScanBtn;
            successMessage = "Left fingerprints captured successfully. Please wait.";
        } else if (FingerSetType.RIGHT == fingerSetTypeToScan) {
            button = rightScanBtn;
            successMessage = "Right fingerprints captured successfully. Please wait.";
        } else if (FingerSetType.THUMB == fingerSetTypeToScan) {
            button = thumbScanBtn;
            successMessage = "Thumbprints captured successfully. Please wait.";
        } else {
            LOGGER.log(Level.SEVERE, UNSUPPORTED_FINGER_SET_TYPE);
            throw new GenericException(UNSUPPORTED_FINGER_SET_TYPE);
        }
        // very important
        // error message set by RS_TakeImageData call in setModeAndStartCapture ()
        // so just return.
        if (errorCode != RS_SUCCESS) {
            return;
        }

        if (imageData == null || imageHeight == 0 || imageWidth == 0) {
            updateUi("Something went wrong. Please try again.");
            enableControls(backBtn, button);
            return;
        }

        int nistQuality = RS_GetQualityScore(imageData, imageWidth, imageHeight);
        if (nistQuality > FP_NIST_VALUE) {
            LOGGER.log(Level.INFO, () -> "Quality too poor (NFIQ): " + nistQuality);
            updateUi("Quality too poor. Please try again.");
            enableControls(backBtn, button);
            return;
        }

        // check LFD only when value is non-zero
        if (fingerprintLivenessValue != 0) {
            try {
                checkLFD(deviceHandle);
            } catch (Exception ex) {
                updateUi(ex.getMessage());
                enableControls(backBtn, button);
                return;
            }
        }
        // to maintain record, it can be misused
        else {
            LOGGER.log(Level.INFO, () -> "***Fingerprints accepted without LFD check for arc: " + ArcDetailsHolder.getArcDetailsHolder().getArcDetail().getArcNo());
        }


        // saves in RSImageInfo for later modification
        RSImageInfo resImageInfo = byteArrayToRSImageInfo(imageData, imageWidth, imageHeight);

        // throws GenericException
        try {
            Platform.runLater(() -> displayFpImage(imageData, imageWidth, imageHeight, rawFingerprintImageView));
            // throws GenericException
            Map<Integer, RSImageInfo> mFingerTypeRsImageInfoMap = segmentSlapImage(resImageInfo);
            // adds to global finger sequence holder map.
            scannedFingerTypeToRsImageInfoMap.putAll(mFingerTypeRsImageInfoMap);
            updateUi(successMessage);
            Platform.runLater(() -> displaySegmentedFpImage(mFingerTypeRsImageInfoMap));
            // to get hold of fingerSetTypeToScan used by displaySegmentedFpImage()
            // if not it will cause issue, since, we change its value in the following line
            Thread.sleep(TIME_TO_WAIT_FOR_SWITCHING_FINGER_TYPE_TO_SCAN_IN_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, button);
            isFromPrevScan = false;
            return;
        }

        /* now get ready for next call */

        // first call is the result of left finger scan
        if (FingerSetType.LEFT == fingerSetTypeToScan) {
            fingerSetTypeToScan = FingerSetType.RIGHT;
            isFromPrevScan = true;
            // SHOULD START FROM ANOTHER THREAD ELSE FINGERPRINT SCANNER ALWAYS STAYS ON UNTIL IT RETURNS
            App.getThreadPool().execute(this::startRightScan);
        } else if (FingerSetType.RIGHT == fingerSetTypeToScan) {
            // second call is the result of right finger scan
            fingerSetTypeToScan = FingerSetType.THUMB;
            isFromPrevScan = true;
            // SHOULD START FROM ANOTHER THREAD ELSE FINGERPRINT SCANNER ALWAYS STAYS ON UNTIL IT RETURNS
            App.getThreadPool().execute(this::startThumbScan);

        } else if (FingerSetType.THUMB == fingerSetTypeToScan) {
            // third call is the result of thumb scan
            fingerSetTypeToScan = FingerSetType.LEFT; // if user needs to restart from the beginning.
            isFromPrevScan = false;
            // SHOULD START FROM ANOTHER THREAD ELSE FINGERPRINT SCANNER ALWAYS STAYS ON UNTIL IT RETURNS
            App.getThreadPool().execute(this::convertToTemplate);
        } else {
            LOGGER.log(Level.SEVERE, UNSUPPORTED_FINGER_SET_TYPE);
            updateUi(GENERIC_RS_ERR_MSG);
        }
    }

    private void checkLFD(int deviceHandler) {
        RSLFDResult rsLfdResult = new RSLFDResult();
        /*
        RS_SetLFDLevel Error Codes:
            RS_SUCCESS - The option is set successfully.
            RS_ERR_UNSUPPORTED_COMMAND - Unsupported device.
         */
        jniReturnedCode = RS_GetLFDResult(deviceHandler, rsLfdResult);
        if (jniReturnedCode != RS_SUCCESS) {
            LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }
        Map<String, Integer> mFingersToScanSeqMap;
        if (FingerSetType.LEFT == fingerSetTypeToScan) {
            mFingersToScanSeqMap = leftFingerToFingerTypeLinkedHashMap;
        } else if (FingerSetType.RIGHT == fingerSetTypeToScan) {
            mFingersToScanSeqMap = rightFingerToFingerTypeLinkedHashMap;
        } else if (FingerSetType.THUMB == fingerSetTypeToScan) {
            mFingersToScanSeqMap = thumbToFingerTypeLinkedHashMap;
        } else {
            // for developers
            throw new GenericException("Unsupported finger set type: ");
        }
        if (mFingersToScanSeqMap.size() != rsLfdResult.nNumofFinger) {
            LOGGER.log(Level.SEVERE, () -> "The finger count doesn't match.");
            throw new GenericException("The finger count doesn't match.");
        }
        if (FingerSetType.THUMB == fingerSetTypeToScan) {
            LOGGER.log(Level.INFO, () -> "Not Checking LFD For Thumb");
        } else {
            for (int i = 0; i < mFingersToScanSeqMap.size(); i++) {
                // exit immediately if fake fingerprint captured.
                if (rsLfdResult.nResult[i] == RS_LFD_FAKE) {
                    int j = i; // used in lambda
                    LOGGER.log(Level.SEVERE, () -> "Fake fingerprint detected. Score: " + rsLfdResult.nScore[j]);
                    throw new GenericException("Quality standard not met or captured fake fingerprint. Kindly try again.");
                }
            }
        }
    }

    private void setModeAndStartCapture() {
        Map<String, Integer> mFingersToScanSeqMap;
        int deviceHandler;

        // fingerSetTypeToScan - GLOBAL MEMBER FIELD
        // slapType -> GLOBAL MEMBER FIELD: to be used during segmentation.
        if (FingerSetType.THUMB == fingerSetTypeToScan) {
            mFingersToScanSeqMap = thumbToFingerTypeLinkedHashMap;
            captureMode = RS_CAPTURE_FLAT_TWO_FINGERS;
            slapType = RS_SLAP_TWO_THUMB;
            deviceHandler = leftFpDeviceHandler;
        } else if (FingerSetType.LEFT == fingerSetTypeToScan) {
            mFingersToScanSeqMap = leftFingerToFingerTypeLinkedHashMap;
            captureMode = RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS;
            slapType = RS_SLAP_LEFT_FOUR;
            deviceHandler = leftFpDeviceHandler;
        } else if (FingerSetType.RIGHT == fingerSetTypeToScan) {
            mFingersToScanSeqMap = rightFingerToFingerTypeLinkedHashMap;
            captureMode = RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS;
            slapType = RS_SLAP_RIGHT_FOUR;
            deviceHandler = rightFpDeviceHandler;
        } else {
            // meant for developers
            LOGGER.log(Level.SEVERE, UNSUPPORTED_FINGER_SET_TYPE);
            throw new GenericException(UNSUPPORTED_FINGER_SET_TYPE);
        }

         /* RS_SetCaptureMode Error Codes:
                 RS_SUCCESS - The capture mode is set successfully.
                 RS_ERR_SDK_UNINITIALIZED - The SDK is not yet initialized.
                 RS_ERR_INVALID_HANDLE - The device handle is invalid.
                 RS_ERR_INVALID_CAPTURE_MODE - The capture mode is not supported.
                 RS_ERR_INVALID_PARAM - The capture option is invalid.
                 RS_ERR_CAPTURE_IS_RUNNING - A capture process is running. You have to stop it first.
         */
        jniReturnedCode = RS_SetCaptureMode(deviceHandler, captureMode, RS_AUTO_SENSITIVITY_NORMAL, true);

        if (jniReturnedCode != RS_SUCCESS) {
            LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }


         /*
            RS_SetMinimumFinger Error Codes:
                RS_SUCCESS - The capture mode is set successfully.
                RS_ERR_SDK_UNINITIALIZED - The SDK is not yet initialized.
                RS_ERR_INVALID_HANDLE - The device handle is invalid.
                RS_ERR_INVALID_CAPTURE_MODE - The capture mode is not supported.
                RS_ERR_WRONG_MIN_FINGER_COUNT - The minimum finger is wrong.
         */
        RS_SetMinimumFinger(deviceHandler, mFingersToScanSeqMap.size());

        jniReturnedCode = RS_GetLastError();
        if (jniReturnedCode != RS_SUCCESS) {
            LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }

        // set LFD only when value is non-zero
        if (fingerprintLivenessValue != 0) {
            // RS_LFD_OFF = 0
            // upto
            // RS_LFD_LEVEL_6 = 6
        /*
        RS_SetLFDLevel Error Codes:
            RS_SUCCESS - The option is set successfully.
            RS_ERR_UNSUPPORTED_COMMAND - Unsupported device.
         */
            jniReturnedCode = RS_SetLFDLevel(deviceHandler, fingerprintLivenessValue);
            if (jniReturnedCode != RS_SUCCESS) {
                LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
                throw new GenericException(GENERIC_RS_ERR_MSG);
            }
        }

       /*
        RS_TakeImageData Error Codes:
        RS_SUCCESS - An image is captured successfully.
        RS_ERR_SDK_UNINITIALIZED - The SDK is not yet initialized.
        RS_ERR_INVALID_HANDLE - The device handle is invalid.
        RS_ERR_SENSOR_DIRTY -  The sensor surface is too dirty. See RS_SetAutomaticCalibrate for details.
        RS_ERR_FINGER_EXIST - Fingers are placed on the sensor before capturing starts. See RS_SetAutomaticCalibrate for details.
        RS_ERR_CAPTURE_DISABLED - The capture mode is disabled.
        RS_ERR_CAPTURE_TIMEOUT - Cannot capture an image within the specified timeout period.
        RS_ERR_ROLL_PART_LIFT - A part of the rolling finger is lifted.
        RS_ERR_ROLL_DIRTY - The sensor surface is dirty, or more than one finger is detected.
        RS_ERR_ROLL_TOO_FAST - Rolling speed is too fast.
        RS_ERR_ROLL_SHIFTED - The finger is heavily shifted or rotated.
        RS_ERR_ROLL_DRY - The finger could not be recognized correctly because of bad image contrast or smeared finger patterns
        RS_ERR_ROLL_WRONG_DIR - The rolling does not confirm to the specified direction.
        RS_ERR_ROLL_TOO_SHORT - Rolling time is too short.
         */
        RSImageInfo imageInfo = new RSImageInfo();
        jniReturnedCode = RS_TakeImageData(deviceHandler, 10000, imageInfo);
        if (jniReturnedCode != RS_SUCCESS) {
            if (jniReturnedCode == RS_ERR_SENSOR_DIRTY || jniReturnedCode == RS_ERR_FINGER_EXIST) {
                LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
                throw new GenericException("Sensor is too dirty or a finger exists on the sensor. Please try again.");
            }
            if (jniReturnedCode == RS_ERR_CAPTURE_TIMEOUT) {
                LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
                throw new GenericException("Capture timeout. Kindly try again.");
            }
            if (jniReturnedCode == RS_WRN_TOO_POOR_QUALITY) {
                LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
                throw new GenericException("Quality too poor. Kindly try again.");
            }
            LOGGER.log(Level.SEVERE, () -> RS_GetErrString(jniReturnedCode));
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }
        processCapturedFp(deviceHandler, jniReturnedCode, imageInfo.pbyImgBuf, imageInfo.imageWidth, imageInfo.imageHeight);
    }

    private void rescanFromStart() {
        scannedFingerTypeToRsImageInfoMap.clear();
        leftScanBtnAction();
    }

    private void displaySegmentedFpImage(Map<Integer, RSImageInfo> fingerTypeRsImageInfo) {
        fingerTypeRsImageInfo.forEach((fingerType, rsImageInfo) -> {
            // update only if non-null
            if (rsImageInfo.pbyImgBuf != null && rsImageInfo.imageWidth != 0 && rsImageInfo.imageHeight != 0) {
                // only update based on finger set
                if (FingerSetType.RIGHT == fingerSetTypeToScan) {
                    if (RS_FGP_RIGHT_INDEX == fingerType) { // 2
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, rightIndexFingerImageView);
                    } else if (RS_FGP_RIGHT_MIDDLE == fingerType) { // 3
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, rightMiddleFingerImageView);
                    } else if (RS_FGP_RIGHT_RING == fingerType) {  // 4
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, rightRingFingerImageView);
                    } else if (RS_FGP_RIGHT_LITTLE == fingerType) { //5
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, rightLittleFingerImageView);
                    }
                } else if (FingerSetType.LEFT == fingerSetTypeToScan) {
                    if (RS_FGP_LEFT_INDEX == fingerType) {  // 7
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, leftIndexFingerImageView);
                    } else if (RS_FGP_LEFT_MIDDLE == fingerType) { // 8
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, leftMiddleFingerImageView);
                    } else if (RS_FGP_LEFT_RING == fingerType) { // 9
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, leftRingFingerImageView);
                    } else if (RS_FGP_LEFT_LITTLE == fingerType) { // 10
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, leftLittleFingerImageView);
                    }
                } else if (FingerSetType.THUMB == fingerSetTypeToScan) {
                    if (RS_FGP_RIGHT_THUMB == fingerType) { // 1
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, rightThumbImageView);
                    } else if (RS_FGP_LEFT_THUMB == fingerType) { // 6
                        displayFpImage(rsImageInfo.pbyImgBuf, rsImageInfo.imageWidth, rsImageInfo.imageHeight, leftThumbImageView);
                    }
                }
            }
        });
    }


    private synchronized void clearFingerprintOnUI() {
        ImageView[] leftFingers = {leftIndexFingerImageView, leftMiddleFingerImageView, leftRingFingerImageView, leftLittleFingerImageView};
        ImageView[] rightFingers = {rightIndexFingerImageView, rightMiddleFingerImageView, rightRingFingerImageView, rightLittleFingerImageView};
        ImageView[] thumbs = {leftThumbImageView, rightThumbImageView};

        clearImageViews(rawFingerprintImageView);
        if (FingerSetType.LEFT == fingerSetTypeToScan) {
            clearImageViews(leftFingers);
            clearImageViews(rightFingers);
            clearImageViews(thumbs);
        } else if (FingerSetType.RIGHT == fingerSetTypeToScan) {
            clearImageViews(rightFingers);
            clearImageViews(thumbs);
        } else if (FingerSetType.THUMB == fingerSetTypeToScan) {
            clearImageViews(thumbs);
        }
    }

    private void clearImageViews(ImageView... imageViews) {
        for (ImageView imageView : imageViews) {
            imageView.setImage(null);
        }
    }

    private synchronized void displayFpImage(byte[] imageArray, int imageWidth, int imageHeight, ImageView imageView) {
        int tempImageWidth = imageWidth;
        // ImageWidth MUST be divisible by 4 for using RS_SaveBitmapMem API
        if (tempImageWidth % 4 != 0) {
            tempImageWidth -= tempImageWidth % 4;
        }
        // copy only if they are not equal
        if (tempImageWidth != imageWidth) {
            byte[] tempData = new byte[tempImageWidth * imageHeight];
            for (int row = 0; row < imageHeight; row++) {
                // imageWidth --> original
                System.arraycopy(imageArray, row * imageWidth, tempData, row * tempImageWidth, tempImageWidth);
            }
            // update to new values
            imageArray = tempData;
            imageWidth = tempImageWidth;
        }
        byte[] imageData = new byte[imageWidth * imageHeight + 1078]; // extra buffer
        /*
        RS_SaveBitmapMem Error Codes:
            RS_SUCCESS The image data is saved successfully.
            RS_ERR_CANNOT_WRITE_FILE Cannot write the BMP file.
         */
        jniReturnedCode = RS_SaveBitmapMem(imageArray, imageWidth, imageHeight, imageData);
        if (jniReturnedCode != RS_SUCCESS) {
            LOGGER.log(Level.SEVERE, "Cannot save bitmap in memory");
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }
        InputStream inputStream = new ByteArrayInputStream(imageData);
        imageView.setImage(new Image(inputStream, imageView.getFitWidth(), imageView.getFitHeight(), true, true));
    }

    // throws GenericException
    private Map<Integer, RSImageInfo> segmentSlapImage(RSImageInfo rsImageInfo) {
        Map<String, Integer> mFingersToScanSeqMap;
        if (FingerSetType.LEFT == fingerSetTypeToScan) {
            mFingersToScanSeqMap = leftFingerToFingerTypeLinkedHashMap;
        } else if (FingerSetType.RIGHT == fingerSetTypeToScan) {
            mFingersToScanSeqMap = rightFingerToFingerTypeLinkedHashMap;
        } else if (FingerSetType.THUMB == fingerSetTypeToScan) {
            mFingersToScanSeqMap = thumbToFingerTypeLinkedHashMap;
        } else {
            LOGGER.log(Level.SEVERE, UNSUPPORTED_FINGER_SET_TYPE);
            throw new GenericException(GENERIC_RS_ERR_MSG);
        }

        int numOfFingers = mFingersToScanSeqMap.size();
        // SIZE MUST BE ATLEAST 4 ELSE CRASHES THE APP
        // It can segment as much as individual fingerprint available in a slap image.
        int size = 4;
        RSImageInfo[] imageInfoArray = new RSImageInfo[size];
        RSSlapInfo[] slapInfoArray = new RSSlapInfo[size];
        for (int i = 0; i < size; i++) {
            // stores at same index respectively
            imageInfoArray[i] = new RSImageInfo(); // stores image data
            slapInfoArray[i] = new RSSlapInfo(); // stores finger position and quality
        }

        /* RS_Segment Error Codes:
            RS_SUCCESS - The slap image is segmented successfully.
            RS_ERR_NO_DEVICE - No device is connected to the PC.
            RS_ERR_INVALID_PARAM - The slapType is invalid.
            RS_ERR_MEM_FULL - Cannot allocate memory.
            RS_ERR_CANNOT_SEGMENT - Cannot segment the slap image.
            RS_ERR_CANNOT_GET_QUALITY - Cannot get the scores of the segmented images.
            RS_ERR_SEGMENT_FEWER_FINGER - The captured image has fewer fingers than expected.
            RS_ERR_SEGMENT_WRONG_HAND - Left hand is captured for right hand, or vice versa.
         */
//        int returnedNumOfFingers = RS_Segment(rsImageInfo, slapType, 0, slapInfoArray, imageInfoArray); // slap type set during setting capture mode
        int returnedNumOfFingers = RS_SegmentWithSize(rsImageInfo, slapType, 0, slapInfoArray, imageInfoArray, FP_SEGMENT_WIDTH, FP_SEGMENT_HEIGHT); // slap type set during setting capture mode
        if (numOfFingers != returnedNumOfFingers) {
            LOGGER.log(Level.SEVERE, "Finger counts does not match.");
            throw new GenericException("Finger count different than specified. Please try again.");
        }
        jniReturnedCode = RS_GetLastError();
        if (jniReturnedCode != RS_SUCCESS) {
            // checks just for proper error message to be displayed on UI
            jniErrorMsg = RS_GetErrString(jniReturnedCode);
            if (jniReturnedCode == RS_ERR_SEGMENT_FEWER_FINGER) { // proceed gracefully
                // SHOULD ALLOW FOR 3 FINGERS
                // OTHERWISE WILL ALWAYS THROW THIS ERROR
                // VERY IMPORTANT
                LOGGER.log(Level.INFO, "Odd number of fingers scanned");
            } else if (jniReturnedCode == RS_ERR_SEGMENT_WRONG_HAND) {
                LOGGER.log(Level.SEVERE, jniErrorMsg);
                // wrong spelling returned from native API
                throw new GenericException("Finger set type different than specified. Please try again.");
            } else if (jniReturnedCode == RS_ERR_CANNOT_GET_QUALITY || jniReturnedCode == RS_ERR_CANNOT_SEGMENT) {
                LOGGER.log(Level.SEVERE, jniErrorMsg);
                throw new GenericException("Quality not good or something went wrong. Please try again.");
            } else {
                LOGGER.log(Level.SEVERE, jniErrorMsg);
                LOGGER.log(Level.SEVERE, () -> "ERROR CODE:" + jniReturnedCode);
                throw new GenericException(GENERIC_RS_ERR_MSG);
            }
        }

        Map<Integer, RSImageInfo> mFingerTypeRsImageInfoMap = new HashMap<>();

        /*
         * Setting finger type for unknown fingerprint segment.
         * Finger type will always be zero for any missing fingers even thought SLAP type is mentioned during segmentation.
         * So based on fingers to be scanned(mFingersToScanMap), we assigned the finger type accordingly, hoping it to be correct.
         */

        // based on finger position specified in RealScan G-10 SDK documentation.
        AtomicInteger counter = new AtomicInteger(0);
        mFingersToScanSeqMap.forEach((finger, position) -> {
            if (slapInfoArray[counter.get()].fingerType == RS_FGP_UNKNOWN) {
                slapInfoArray[counter.get()].fingerType = position;
            }
            // checks if null value produced during segmentation
            RSImageInfo rsImageInfoTemp = imageInfoArray[counter.get()];
            if (rsImageInfoTemp.pbyImgBuf == null || rsImageInfoTemp.imageWidth == 0 || rsImageInfoTemp.imageHeight == 0) {
                LOGGER.log(Level.SEVERE, "Received a null value for RsImageInfo buffer.");
                throw new GenericException(GENERIC_RS_ERR_MSG);
            }
            mFingerTypeRsImageInfoMap.put(slapInfoArray[counter.get()].fingerType, rsImageInfoTemp);
            counter.getAndIncrement();
        });
        return mFingerTypeRsImageInfoMap;
    }

    private void backBtnAction() {
        confirmPane.setVisible(true);
        if (!scanBtn.isDisable()) {
            currentEnabledButton = scanBtn;
        } else if (!leftScanBtn.isDisable()) {
            currentEnabledButton = leftScanBtn;
        } else if (!rightScanBtn.isDisable()) {
            currentEnabledButton = rightScanBtn;
        } else if (!thumbScanBtn.isDisable()) {
            currentEnabledButton = thumbScanBtn;
        } else {
            LOGGER.log(Level.SEVERE, "No button is enabled.");
            throw new GenericException("At least one button should be enabled.");
        }
        disableControls(backBtn, scanBtn, leftScanBtn, rightScanBtn, thumbScanBtn, captureIrisBtn);
    }

    private void showIris() {
        try {
            App.setRoot("iris");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            messageLabel.setText(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    private void confirmBack() {
        App.getThreadPool().execute(() -> {
            try {
                disableControls(confirmNoBtn, confirmYesBtn);
                Platform.runLater(() -> confirmText.setText("Please Wait...."));
                if (isLeftFpDeviceInitialised || isRightFpDeviceInitialised) {
                    Thread.sleep(1000);
                    releaseDevice(leftFpDeviceHandler);
                    releaseDevice(rightFpDeviceHandler);
                }
                App.setRoot("biometric_enrollment");
            } catch (IOException | InterruptedException ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOGGER.log(Level.SEVERE, ex.getMessage());
                Platform.runLater(() -> confirmText.setText(GENERIC_ERR_MSG));
                enableControls(confirmNoBtn, confirmYesBtn);
            }
        });
    }

    private void confirmStay() {
        backBtn.setDisable(false);
        confirmPane.setVisible(false);
        captureIrisBtn.setDisable(!isFpScanCompleted);
        currentEnabledButton.setDisable(false);
    }


    private synchronized void releaseDevice(int deviceHandler) {
        // must run on the main thread else crashes the app.
        Platform.runLater(() -> {
            boolean isDeviceInitialized;
            if (deviceHandler == leftFpDeviceHandler) {
                isDeviceInitialized = isLeftFpDeviceInitialised;
            } else {
                isDeviceInitialized = isRightFpDeviceInitialised;
            }

            if (!isDeviceInitialized) {
                LOGGER.log(Level.SEVERE, "Device not initialised");
                return;
            }
            /*
            RS_ExitDevice Error Code:
                 RS_SUCCESS - The handle and resources are released successfully.
                 RS_ERR_SDK_UNINITIALIZED - The SDK is not yet initialized.
                 RS_ERR_INVALID_HANDLE - The device handle is invalid.
                 RS_ERR_CAPTURE_IS_RUNNING - A capture process is running. You have to stop it first.
            */
            jniReturnedCode = RS_ExitDevice(deviceHandler);

            if (jniReturnedCode == RS_ERR_CAPTURE_IS_RUNNING) {
                /*
                RS_AbortCapture Error Code:
                    RS_SUCCESS - Capture process is aborted successfully.
                    RS_ERR_SDK_UNINITIALIZED - The SDK is not yet initialized.
                    RS_ERR_INVALID_HANDLE - The device handle is invalid.
                */
                jniReturnedCode = RS_AbortCapture(deviceHandler);
                jniReturnedCode = RS_ExitDevice(deviceHandler);
            }
            if (jniReturnedCode != RS_SUCCESS) {
                LOGGER.log(Level.SEVERE, RS_GetErrString(jniReturnedCode));
                RS_ExitAllDevices();
            }

            if (deviceHandler == leftFpDeviceHandler) {
                isLeftFpDeviceInitialised = false;
            } else {
                isRightFpDeviceInitialised = false;
            }
        });

    }

    // returns a linked(sequence, order is important) map which has finger-to-position mapping based on RealScan G-10 SDK specification
    private Map<String, Integer> getFingersToScanSeqMap(List<String> fingersException, FingerSetType fingerSetType) {
        Map<String, Integer> fingersMap = new LinkedHashMap<>();
        // Finger Positions are based on RealScan G-10 SDK specification.
        if (FingerSetType.RIGHT == fingerSetType) {
            fingersMap.put("RI", RS_FGP_RIGHT_INDEX);  // 2
            fingersMap.put("RM", RS_FGP_RIGHT_MIDDLE); // 3
            fingersMap.put("RR", RS_FGP_RIGHT_RING);   // 4
            fingersMap.put("RL", RS_FGP_RIGHT_LITTLE); // 5
        } else if (FingerSetType.LEFT == fingerSetType) {
            fingersMap.put("LL", RS_FGP_LEFT_LITTLE); // 10
            fingersMap.put("LR", RS_FGP_LEFT_RING);   // 9
            fingersMap.put("LM", RS_FGP_LEFT_MIDDLE); // 8
            fingersMap.put("LI", RS_FGP_LEFT_INDEX);  // 7
        } else if (FingerSetType.THUMB == fingerSetType) {
            fingersMap.put("LT", RS_FGP_LEFT_THUMB);   // 6
            fingersMap.put("RT", RS_FGP_RIGHT_THUMB);  // 1
        } else {
            // meant for developers
            LOGGER.log(Level.SEVERE, UNSUPPORTED_FINGER_SET_TYPE);
            throw new GenericException(UNSUPPORTED_FINGER_SET_TYPE);
        }

        fingersException.forEach(fingersMap::remove);
        return fingersMap;
    }

    private RSImageInfo byteArrayToRSImageInfo(byte[] imageData, int imageWidth, int imageHeight) {
        RSImageInfo resImageInfo = new RSImageInfo();
        resImageInfo.pbyImgBuf = imageData;
        resImageInfo.imageWidth = imageWidth;
        resImageInfo.imageHeight = imageHeight;
        return resImageInfo;
    }


    private synchronized void convertToTemplate() {
        updateUi("Processing fingerprints. Please wait...");
        int requiredFingerCount = leftFingerToFingerTypeLinkedHashMap.size() + rightFingerToFingerTypeLinkedHashMap.size() + thumbToFingerTypeLinkedHashMap.size();
        Set<Integer> fingerSet = new HashSet<>(leftFingerToFingerTypeLinkedHashMap.values());
        fingerSet.addAll(rightFingerToFingerTypeLinkedHashMap.values());
        fingerSet.addAll(thumbToFingerTypeLinkedHashMap.values());

        // check if total fingers to scan is same with already scanned fingers
        if (fingerSet.size() != requiredFingerCount || requiredFingerCount != scannedFingerTypeToRsImageInfoMap.size()) {
            LOGGER.log(Level.SEVERE, "requiredFingerCount and map size not equal.");
            Platform.runLater(this::clearFingerprintOnUI);
            updateUi("Finger counts different than specified. Kindly rescan all the required fingers.");
            enableControls(leftScanBtn);
            return;
        }

        List<byte[]> isoTemplates = new ArrayList<>();

        Set<Fp> fps = new HashSet<>();
        String fingerPositionString;

        for (Integer finger : fingerSet) {
            switch (finger) {
                case RS_FGP_RIGHT_THUMB: // 1
                    fingerPositionString = "RT";
                    break;
                case RS_FGP_RIGHT_INDEX: // 2
                    fingerPositionString = "RI";
                    break;
                case RS_FGP_RIGHT_MIDDLE: // 3
                    fingerPositionString = "RM";
                    break;
                case RS_FGP_RIGHT_RING:  // 4
                    fingerPositionString = "RR";
                    break;
                case RS_FGP_RIGHT_LITTLE:  // 5
                    fingerPositionString = "RL";
                    break;
                case RS_FGP_LEFT_THUMB:  // 6
                    fingerPositionString = "LT";
                    break;
                case RS_FGP_LEFT_INDEX:  // 7
                    fingerPositionString = "LI";
                    break;
                case RS_FGP_LEFT_MIDDLE:  // 8
                    fingerPositionString = "LM";
                    break;
                case RS_FGP_LEFT_RING:  // 9
                    fingerPositionString = "LR";
                    break;
                case RS_FGP_LEFT_LITTLE:  // 10
                    fingerPositionString = "LL";
                    break;
                default:
                    // meant for developers
                    LOGGER.log(Level.SEVERE, "Unsupported finger type.");
                    throw new GenericException("Unsupported finger type.");
            }
            // check if GLOBAL FingerType to RSImageInfo Map has this finger key-value mapping.
            RSImageInfo rsImageInfo = scannedFingerTypeToRsImageInfoMap.get(finger);
            if (rsImageInfo == null) {
                LOGGER.log(Level.SEVERE, () -> "Finger-RSImageInfo mapping not found in scannedFingerTypeToRsImageInfoMap for key: " + finger);
                Platform.runLater(this::clearFingerprintOnUI);
                updateUi("Fingerprint map not found. Kindly rescan from start");
                enableControls(leftScanBtn);
                return;
            }

            // also returns null value if exceptions occur
            Map<String, byte[]> imageAndIsoTemplateMap = convertToIsoTemplate(rsImageInfo.imageWidth, rsImageInfo.imageHeight, rsImageInfo.pbyImgBuf);
            if (imageAndIsoTemplateMap == null) {
                LOGGER.log(Level.SEVERE, "imageAndIsoTemplateMap is null.");
                Platform.runLater(this::clearFingerprintOnUI);
                updateUi("Error converting to template. Kindly rescan from start");
                enableControls(leftScanBtn);
                return;
            }

            byte[] image = imageAndIsoTemplateMap.get("image");
            byte[] template = imageAndIsoTemplateMap.get("template");

            if (image == null || template == null) {
                LOGGER.log(Level.SEVERE, "Either image or template is null");
                Platform.runLater(this::clearFingerprintOnUI);
                updateUi("Either image or template is invalid");
                enableControls(leftScanBtn);
                return;
            }

            isoTemplates.add(template);

            Fp fp = new Fp();
            fp.setPosition(fingerPositionString);
            fp.setImage(Base64.getEncoder().encodeToString(image));
            fp.setTemplate(Base64.getEncoder().encodeToString(template));
            fps.add(fp);
        }

        if (checkDuplicateFp(isoTemplates)) {
            LOGGER.log(Level.SEVERE, "Scanned duplicate fingerprints.");
            updateUi("Duplicate fingerprints found. Rescanning from the start....");
            duplicateFpFound = true;
            rescanFromStart();
            return;
        }

        //if error occurs for clearing the UI images
        fingerSetTypeToScan = FingerSetType.LEFT;
        if (isLeftFpDeviceInitialised || isRightFpDeviceInitialised) {
            releaseDevice(leftFpDeviceHandler);
            releaseDevice(rightFpDeviceHandler);
        }

        ArcDetailsHolder arcDetailsHolder = ArcDetailsHolder.getArcDetailsHolder();
        SaveEnrollmentDetail saveEnrollmentDetail = arcDetailsHolder.getSaveEnrollmentDetail();
        saveEnrollmentDetail.setLeftFpScannerSerialNo(leftFpDeviceInfo.deviceID);
        saveEnrollmentDetail.setRightFpScannerSerialNo(rightFpDeviceInfo.deviceID);
        saveEnrollmentDetail.setFp(fps);
        saveEnrollmentDetail.setEnrollmentStatus("FingerPrintCompleted");

        // throws GenericException
        try {
            SaveEnrollmentDetailUtil.writeToFile(saveEnrollmentDetail);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            updateUi(ApplicationConstant.GENERIC_TEMPLATE_CONVERSION_ERR_MSG);
            enableControls(scanBtn);
            return;
        }
        // enable the controls
        Platform.runLater(() -> {
            scanBtn.setText("RESCAN");
            scanBtn.setDisable(false);
            messageLabel.setText("Please click 'CAPTURE Iris' button to continue. ");
            backBtn.setDisable(false);
            captureIrisBtn.setDisable(false);
            isFpScanCompleted = true;
        });

    }

    private boolean checkDuplicateFp(List<byte[]> isoTemplates) {
        int fpMatchMinThreshold;
        try {
            fpMatchMinThreshold = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_MATCH_MIN_THRESHOLD).trim());
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, () -> "Not a number or no entry for '" + PropertyName.FP_MATCH_MIN_THRESHOLD + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException(GENERIC_ERR_MSG);
        }

        for (int i = 0; i < isoTemplates.size(); i++) {
            for (int j = i + 1; j < isoTemplates.size(); j++) {
                if (ansiIso.isoVerifyMatch(isoTemplates.get(i), isoTemplates.get(j), 0) >= fpMatchMinThreshold) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, byte[]> convertToIsoTemplate(int width, int height, byte[] imageData) {
        try {
            RawGrayscaleImage rawGrayscaleImage = new RawGrayscaleImage(width, height, imageData);
            byte[] inputImage = ansiIso.convertRawToImage(rawGrayscaleImage, AnsiIsoImageFormatEnum.JPEG2K);
            int numberOfByteForOutputImage = inputImage.length + 2000; // required extra 2000 bytes
            byte[] outputImage = new byte[numberOfByteForOutputImage];
            int[] outputImageLen = new int[]{numberOfByteForOutputImage};

            int returnedCode = Utility.ImageConvert(inputImage, inputImage.length, outputImage, outputImageLen, IMAGE_FORMAT.IENGINE_FORMAT_JPEG2K_TO_FIR_JPEG2000_V2005.getValue(), 354, 296);
            if (returnedCode != 0) {
                if (returnedCode == Utility.UNKNOWN_ERROR) {
                    LOGGER.log(Level.SEVERE, "UNKNOWN_ERROR DURING IMAGE CONVERSION");
                } else if (returnedCode == Utility.UNSUPPORTED_IMAGE_FORMAT) {
                    LOGGER.log(Level.SEVERE, "UNSUPPORTED_IMAGE_FORMAT DURING IMAGE CONVERSION");

                } else if (returnedCode == Utility.OBJECT_CANNOT_BE_NULL_OR_EMPTY) {
                    LOGGER.log(Level.SEVERE, "OBJECT_CANNOT_BE_NULL_OR_EMPTY DURING IMAGE CONVERSION");
                }
                return null;
            }

            byte[] isoTemplate = ansiIso.isoCreateTemplate(rawGrayscaleImage);
            byte[] iengineTemplate = ansiIso.iengineConvertTemplate(IEngineTemplateFormat.ISO_TEMPLATE, isoTemplate, IEngineTemplateFormat.ISO_TEMPLATE_V30);
            Map<String, byte[]> imageAndIsoTemplateMap = new HashMap<>();
            imageAndIsoTemplateMap.put("image", outputImage);
            imageAndIsoTemplateMap.put("template", iengineTemplate);
            return imageAndIsoTemplateMap;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return null;
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
        backBtn.setDisable(false);
        currentEnabledButton.setDisable(false);
        updateUi("Unhandled exception occurred. Please try again. ");
    }
}