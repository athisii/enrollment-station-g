package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.*;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.exception.NoReaderOrCardException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ContractorCardInfo;
import com.cdac.enrollmentstation.model.LabourDetailsTableRow;
import com.cdac.enrollmentstation.model.TokenDetailsHolder;
import com.cdac.enrollmentstation.util.Asn1CardTokenUtil;
import com.cdac.enrollmentstation.util.PropertyFile;
import com.cdac.enrollmentstation.util.TokenDispenserUtil;
import com.mantra.midfingerauth.DeviceInfo;
import com.mantra.midfingerauth.MIDFingerAuth;
import com.mantra.midfingerauth.MIDFingerAuth_Callback;
import com.mantra.midfingerauth.enums.DeviceDetection;
import com.mantra.midfingerauth.enums.DeviceModel;
import com.mantra.midfingerauth.enums.TemplateFormat;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;
import static com.cdac.enrollmentstation.util.Asn1CardTokenUtil.*;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class LabourController extends AbstractBaseController implements MIDFingerAuth_Callback {
    private static final Logger LOGGER = ApplicationLog.getLogger(LabourController.class);

    private static final int NUMBER_OF_ROWS_PER_PAGE = 8;
    @FXML
    private Button selectNextContractorBtn;
    private int jniErrorCode;

    //***********************Fingerprint***************************//
    private MIDFingerAuth midFingerAuth; // For MID finger jar
    private DeviceInfo deviceInfo;
    private boolean isDeviceInitialized;
    private static final int MIN_QUALITY = 60;
    private static final int FINGERPRINT_CAPTURE_TIMEOUT_IN_SEC = 10;
    private static final int TOKEN_DROP_SLEEP_TIME_IN_SEC = 7;

    //***********************Fingerprint***************************//

    private List<LabourDetailsTableRow> labourDetailsTableRows;
    private Map<String, Labour> labourMap;
    @FXML
    private Label lblContractName;

    @FXML
    private Label lblContractorName;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private TableView<LabourDetailsTableRow> tableView;

    @FXML
    private TextField searchBox;

    @FXML
    private Button captureBtn;

    @FXML
    private Pagination pagination;


    public void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    public void initialize() {
        // initially disable it until a row is selected
        captureBtn.setDisable(true);
        midFingerAuth = new MIDFingerAuth(this);
        if (!initFpReader()) {
            return;
        }
        fetchLabourList();
    }

    private boolean initFpReader() {
        List<String> devices = new ArrayList<>();
        jniErrorCode = midFingerAuth.GetConnectedDevices(devices);
        if (jniErrorCode != 0 || devices.isEmpty()) {
            LOGGER.log(Level.INFO, () -> midFingerAuth.GetErrorMessage(jniErrorCode));
            messageLabel.setText("Single fingerprint reader not connected.");
            return false;
        }
        if (!midFingerAuth.IsDeviceConnected(DeviceModel.valueFor(devices.get(0)))) {
            LOGGER.log(Level.INFO, "Fingerprint reader not connected");
            messageLabel.setText("Device not connected. Please connect and try again.");
            return false;
        }

        deviceInfo = new DeviceInfo();
        jniErrorCode = midFingerAuth.Init(DeviceModel.valueFor(devices.get(0)), deviceInfo);
        if (jniErrorCode != 0) {
            LOGGER.log(Level.INFO, () -> midFingerAuth.GetErrorMessage(jniErrorCode));
            messageLabel.setText("Single fingerprint reader not initialized.");
            return false;
        }
        isDeviceInitialized = true;
        return true;
    }

    public void fetchLabourList() {
        TokenDetailsHolder tokenDetailsHolder = TokenDetailsHolder.getDetailsHolder();
        lblContractorName.setText("CONTRACTOR NAME: " + tokenDetailsHolder.getContractorCardInfo().getContractorName());
        lblContractName.setText("CONTRACT ID: " + tokenDetailsHolder.getContractorCardInfo().getContractId());

        ContractorCardInfo contractDetails = tokenDetailsHolder.getContractorCardInfo();
        LabourResDto labourResDto;
        try {
            labourResDto = MafisServerApi.fetchLabourList(contractDetails.getContractorId(), contractDetails.getContractId());
        } catch (GenericException ex) {
            messageLabel.setText(ex.getMessage());
            return;
        } catch (ConnectionTimeoutException ex) {
            messageLabel.setText("Connection timeout. Please try again.");
            return;
        }

        if (labourResDto.getErrorCode() != 0) {
            LOGGER.log(Level.INFO, () -> "***ServerErrorCode: " + labourResDto.getErrorCode());
            messageLabel.setText(labourResDto.getDesc());
            return;
        }
        if (labourResDto.getLabours() == null || labourResDto.getLabours().isEmpty()) {
            messageLabel.setText("No labour found for contract id: " + contractDetails.getContractId());
            return;
        }

        labourDetailsTableRows = new ArrayList<>();
        labourMap = new HashMap<>();

        for (Labour labour : labourResDto.getLabours()) {
            if (labour.getDynamicFile() == null) {
                // for debugging purposes
                LOGGER.log(Level.INFO, "Labour dynamicFileList is null.");
            } else {
                DynamicFile dynamicFile = labour.getDynamicFile();
                LabourDetailsTableRow labourDetailsTableRow = new LabourDetailsTableRow();
                labourDetailsTableRow.setDateOfBirth(dynamicFile.getDateOfBirth());
                labourDetailsTableRow.setLabourId(dynamicFile.getLabourId());
                labourDetailsTableRow.setLabourName(dynamicFile.getLabourName());
                //for differentiating table row for token issued labour, if required but now removed from the list if issued
                labourDetailsTableRow.setStrStatus("token not issued");
                labourDetailsTableRows.add(labourDetailsTableRow);
                labourMap.put(dynamicFile.getLabourId(), labour);
            }

        }
        updateLabourDetailsInTable(labourDetailsTableRows);
    }

    public void updateLabourDetailsInTable(List<LabourDetailsTableRow> labourDetailsTableRows) {
        int extraPage;
        if (labourDetailsTableRows.size() % NUMBER_OF_ROWS_PER_PAGE == 0) {
            extraPage = 0;
        } else {
            extraPage = 1;
        }
        int pageCount = labourDetailsTableRows.size() / NUMBER_OF_ROWS_PER_PAGE + extraPage;

        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex > pageCount) {
                return null;
            }
            return createPage(pageIndex);
        });
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> tableView.setItems(filterList(labourDetailsTableRows, newValue)));
        ObservableList<LabourDetailsTableRow> observablelist = FXCollections.observableArrayList(labourDetailsTableRows);
        tableView.setItems(observablelist);
        tableView.refresh();
        tableView.setRowFactory(tv -> {
            TableRow<LabourDetailsTableRow> row = new TableRow<>() {
                @Override
                public void updateItem(LabourDetailsTableRow item, boolean empty) {
                    if (item != null) {
                        super.updateItem(item, empty);
                        if (item.getStrStatus().equalsIgnoreCase("token issued")) {
                            setStyle("-fx-background-color: green;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                // check for non-empty rows, double-click with the primary button of the mouse
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    messageLabel.setText("");
                    fingerprintImageView.setImage(null);
                    captureBtn.setDisable(false);
                }
            });
            return row;
        });
    }

    @FXML
    private void showContractBtnAction() throws IOException {
        App.setRoot("contract");
    }

    @FXML
    private void captureBtnAction() {
        captureBtn.setDisable(true);
        if (!isDeviceInitialized && (!initFpReader())) {
            //message updated by initFpReader()
            captureBtn.setDisable(false);
            return;
        }
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            messageLabel.setText("Kindly select a labour");
        } else {
            jniErrorCode = midFingerAuth.StartCapture(MIN_QUALITY, (int) TimeUnit.SECONDS.toMillis(FINGERPRINT_CAPTURE_TIMEOUT_IN_SEC));
            if (jniErrorCode != 0) {
                LOGGER.log(Level.SEVERE, () -> midFingerAuth.GetErrorMessage(jniErrorCode));
                messageLabel.setText(midFingerAuth.GetErrorMessage(jniErrorCode));
            }
        }

    }

    // runs in worker thread spawned by OnComplete() callback
    private void matchFingerprintTemplate(byte[] fingerData) {
        LabourDetailsTableRow labourDetailsTableRowRow = tableView.getSelectionModel().getSelectedItem();
        if (labourDetailsTableRowRow == null) {
            updateUi("Kindly select a labour.");
            return;
        }

        Labour labour = labourMap.get(labourDetailsTableRowRow.getLabourId());
        if (labour == null) {
            updateUi("No labour details found for selected labour id: " + labourDetailsTableRowRow.getLabourId());
            return;
        }

        if (labour.getFps() == null || labour.getFps().isEmpty()) {
            updateUi("No fingerprint data for selected labour id: " + labourDetailsTableRowRow.getLabourId());
            return;
        }
        int[] matchScore = new int[1];
        boolean matchFound = false;
        int fpMatchMinThreshold;
        try {
            fpMatchMinThreshold = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_MATCH_MIN_THRESHOLD).trim());
        } catch (NumberFormatException | GenericException ex) {
            LOGGER.log(Level.SEVERE, () -> "Not a number or no entry for '" + PropertyName.FP_MATCH_MIN_THRESHOLD + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException(GENERIC_ERR_MSG);
        }

        for (int i = 0; i < labour.getFps().size(); i++) {
            jniErrorCode = midFingerAuth.MatchTemplate(fingerData, Base64.getDecoder().decode(labour.getFps().get(i).getFpData()), matchScore, TemplateFormat.FMR_V2011);
            if (jniErrorCode != 0) {
                LOGGER.log(Level.SEVERE, () -> midFingerAuth.GetErrorMessage(jniErrorCode));
                updateUi(midFingerAuth.GetErrorMessage(jniErrorCode));
                return;
            }
            if (matchScore[0] >= fpMatchMinThreshold) {
                matchFound = true;
                break;
            }
        }

        if (!matchFound) {
            updateUi("Fingerprint not matched for labour id: " + labourDetailsTableRowRow.getLabourId());
            return;
        }
        updateUi("Fingerprint matched for labour id: " + labourDetailsTableRowRow.getLabourId());
        //now match found.
        dispenseToken(labour);
    }

    private void dispenseToken(Labour labour) {
        //dispenses token on card writer
        if (!TokenDispenserUtil.dispenseToken()) {
            updateUi("Kindly check the Token Dispenser and try again.");
            return;
        }
        TokenReqDto tokenReqDto;
        try {
            tokenReqDto = startProcedureCall(labour);
        } catch (GenericException | NoReaderOrCardException ex) {
            updateUi(ex.getMessage());
            return;
        } catch (ConnectionTimeoutException ex) {
            updateUi("Something went wrong. Kindly check Card API service.");
            return;
        }

        CommonResDto resDto;
        //Update token details to MAFIS
        try {
            resDto = MafisServerApi.updateTokenStatus(tokenReqDto);
        } catch (GenericException ex) {
            updateUi(ex.getMessage());
            return;
        } catch (ConnectionTimeoutException ex) {
            updateUi("Connection timeout. Failed to update token status to server. Please try again.");
            return;
        }

        if (resDto.getErrorCode() != 0) {
            LOGGER.log(Level.SEVERE, () -> "***ServerErrorCode: " + resDto.getErrorCode());
            LOGGER.log(Level.SEVERE, () -> "***ServerErrorDesc: " + resDto.getDesc());
            updateUi(resDto.getDesc());
            return;
        }
        updateUi("Kindly collect the token.");
        LOGGER.log(Level.INFO, "Token dispensed successfully.");
        Optional<LabourDetailsTableRow> labourDetailsTableRowOptional = tableView.getItems().stream().filter(labourDetailsTableRow -> labourDetailsTableRow.getLabourId().equals(labour.getDynamicFile().getLabourId())).findFirst();

        LabourDetailsTableRow labourDetailsTableRow = labourDetailsTableRowOptional.orElseThrow(() -> new GenericException("No matching labor id found in the table."));

        labourDetailsTableRow.setStrStatus("token issued"); // not really import now

        tableView.getItems().remove(labourDetailsTableRow); // remove for now
        tableView.refresh();

        if (tableView.getItems().isEmpty()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> {
                try {
                    App.setRoot("contract");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                    throw new GenericException(GENERIC_ERR_MSG);
                }
            });
        }

    }

    private TokenReqDto startProcedureCall(Labour labour) {
        // If user removes the contractor card, the handle will change, so to be on the safe, lets
        // DeInitialize and start over again.

        /*
            waitForConnect - token
            selectApp - token
            read data(static) - token
            read cert - card
            verify cert - token handle
            pki auth - (token handle, card handle)
            token write:
                  1. dynamic data
                  2. default access validity
                  3. special access permission
                  4. photo
                  5. fingerprint
                  6. signature 1
                  7. signature 3
         */
        // setup writer; need to add a delay for some milliseconds
        try {
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }
        updateUi("Preparing the token for data writing. Please wait.");

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(TOKEN_DROP_SLEEP_TIME_IN_SEC));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CRWaitForConnectResDto crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_WRITER_NAME);
        byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        String tokenCsn = Strings.fromByteArray(Hex.encode(decodedHexCsn)).toUpperCase();
        int tokenHandle = crWaitForConnectResDto.getHandle();
        Asn1CardTokenUtil.selectApp(TOKEN_TYPE_NUMBER, tokenHandle);
        byte[] asn1EncodedTokenStaticData = Asn1CardTokenUtil.readBufferedData(tokenHandle, CardTokenFileType.STATIC);
        String tokenNumber = new String(extractFromAsn1EncodedStaticData(asn1EncodedTokenStaticData, 1), StandardCharsets.UTF_8);

        // read cert now
        byte[] systemCertificate = Asn1CardTokenUtil.readBufferedData(TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getCardHandle(), CardTokenFileType.SYSTEM_CERTIFICATE);
        Asn1CardTokenUtil.verifyCertificate(tokenHandle, WHICH_TRUST, WHICH_CERTIFICATE, systemCertificate);
        Asn1CardTokenUtil.pkiAuth(tokenHandle, TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getCardHandle());

        Asn1CardTokenUtil.encodeAndStoreDynamicFile(tokenHandle, labour.getDynamicFile());
        Asn1CardTokenUtil.encodeAndStoreDefaultValidityFile(tokenHandle, labour.getDefaultValidityFile());
        Asn1CardTokenUtil.encodeAndStoreSpecialAccessFile(tokenHandle, labour.getAccessFile());
        Asn1CardTokenUtil.encodeAndStorePhotoFile(tokenHandle, labour.getPhoto());
        Asn1CardTokenUtil.encodeAndStoreFingerprintFile(tokenHandle, labour.getFps());
        Asn1CardTokenUtil.encodeAndStoreSignFile1(tokenHandle, labour.getSignFile1());
        Asn1CardTokenUtil.encodeAndStoreSignFile3(tokenHandle, labour.getSignFile3());
        return createTokenReqDto(labour.getDynamicFile().getLabourId(), tokenCsn, tokenNumber);
    }

    private TokenReqDto createTokenReqDto(String labourId, String tokenCsn, String tokenNumber) {
        TokenReqDto tokenReqDto = new TokenReqDto();
        ContractorCardInfo contractorCardInfo = TokenDetailsHolder.getDetailsHolder().getContractorCardInfo();
        tokenReqDto.setCardCsn(tokenCsn);
        tokenReqDto.setContractorCsn(contractorCardInfo.getCardChipSerialNo());
        tokenReqDto.setContractorId(contractorCardInfo.getContractorId());
        tokenReqDto.setContractId(contractorCardInfo.getContractId());
        tokenReqDto.setEnrollmentStationUnitId(MafisServerApi.getEnrollmentStationUnitId());
        tokenReqDto.setUniqueNo(labourId);
        tokenReqDto.setEnrollmentStationId(MafisServerApi.getEnrollmentStationId());
        tokenReqDto.setTokenId(tokenNumber);
        tokenReqDto.setVerifyFpSerialNo(deviceInfo.SerialNo);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        tokenReqDto.setTokenIssuanceDate(dtf.format(LocalDateTime.now()));
        return tokenReqDto;
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * 8;
        int toIndex = Math.min(fromIndex + 8, labourDetailsTableRows.size());
        tableView.setItems(FXCollections.observableArrayList(labourDetailsTableRows.subList(fromIndex, toIndex)));
        return tableView;
    }

    private ObservableList<LabourDetailsTableRow> filterList(List<LabourDetailsTableRow> labourDetailsTableRowList, String searchText) {
        List<LabourDetailsTableRow> filteredList = new ArrayList<>();
        for (LabourDetailsTableRow labourData : labourDetailsTableRowList) {
            if (labourData.getLabourId().toLowerCase().contains(searchText.toLowerCase()) || labourData.getLabourName().toLowerCase().contains(searchText.toLowerCase()) || labourData.getDateOfBirth().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(labourData);
            }
        }
        return FXCollections.observableList(filteredList);
    }


    @Override
    public void OnDeviceDetection(String s, DeviceDetection deviceDetection) {
        if (DeviceDetection.DISCONNECTED == deviceDetection) {
            LOGGER.log(Level.INFO, "Fingerprint scanner disconnected.");
            updateUi("Fingerprint scanner disconnected.");
            midFingerAuth.Uninit();
            isDeviceInitialized = false;
        }
    }

    @Override
    public void OnPreview(int errorCode, int quality, final byte[] imageData) {
        if (errorCode != 0 || imageData == null) {
            LOGGER.log(Level.SEVERE, () -> midFingerAuth.GetErrorMessage(errorCode));
            captureBtn.setDisable(false);
            return;
        }
        InputStream inputStream = new ByteArrayInputStream(imageData);
        Image image = new Image(inputStream, fingerprintImageView.getFitWidth(), fingerprintImageView.getFitHeight(), true, false);
        fingerprintImageView.setImage(image);
    }


    @Override
    public void OnComplete(int errorCode, int quality, int nfiq) {
        if (errorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> midFingerAuth.GetErrorMessage(errorCode));
            updateUi("Fingerprint quality too poor. Please try again.");
            captureBtn.setDisable(false);
            return;
        }

        int dataLen = 2500;  // as is. but can also be used from OnPreview callback
        byte[] template = new byte[dataLen];
        int[] templateLen = {dataLen};
        jniErrorCode = midFingerAuth.GetTemplate(template, templateLen, TemplateFormat.FMR_V2011);
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> midFingerAuth.GetErrorMessage(errorCode));
            updateUi(midFingerAuth.GetErrorMessage(errorCode));
            captureBtn.setDisable(false);
            return;
        }
        Platform.runLater(() -> fingerprintImageView.setImage(null));
        // tries matching fingerprint
        matchFingerprintTemplate(template);
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        selectNextContractorBtn.setDisable(false);
        updateUi("Received an invalid data from the server.");
    }
}
