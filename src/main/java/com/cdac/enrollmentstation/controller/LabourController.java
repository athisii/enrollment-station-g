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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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
import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;
import static com.cdac.enrollmentstation.util.Asn1CardTokenUtil.*;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class LabourController extends AbstractBaseController implements MIDFingerAuth_Callback {
    private static final Logger LOGGER = ApplicationLog.getLogger(LabourController.class);

    private static final int NUMBER_OF_ROWS_PER_PAGE = 8;
    private static final int LABOUR_FP_AUTH_ALLOWED_MAX_ATTEMPT = 6;
    @FXML
    private BorderPane rootBorderPane;
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
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });

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
        LOGGER.log(Level.INFO, () -> "***Fetching labour list from the server.");
        try {
            labourResDto = MafisServerApi.fetchLabourList(contractDetails.getContractorId(), contractDetails.getContractId());
        } catch (GenericException ex) {
            messageLabel.setText(ex.getMessage());
            return;
        } catch (ConnectionTimeoutException ex) {
            messageLabel.setText("Connection timeout. Please try again.");
            return;
        }

        LOGGER.log(Level.INFO, () -> "***ServerResponseErrorCode: " + labourResDto.getErrorCode());
        if (labourResDto.getErrorCode() != 0) {
            LOGGER.log(Level.INFO, () -> "***ServerErrorDesc: " + labourResDto.getDesc());
            if (labourResDto.getDesc().toLowerCase().contains("unable to process")) {
                messageLabel.setText("Unable to process due to server response failed. Kindly try again.");
            } else {
                messageLabel.setText(labourResDto.getDesc());
            }
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
                // check for non-empty rows, double-click
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    fingerprintImageView.setImage(null);
                    LabourDetailsTableRow selectedLabour = tableView.getSelectionModel().getSelectedItem();
                    if (selectedLabour.getCount() == LABOUR_FP_AUTH_ALLOWED_MAX_ATTEMPT) {
                        updateUi("The allowed number of attempts for Labor id: " + selectedLabour.getLabourId() + " has been exhausted.");
                        return;
                    }
                    messageLabel.setText("");
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
            fpMatchMinThreshold = Integer.parseInt(PropertyFile.getProperty(PropertyName.FP_MATCH_MIN_THRESHOLD_TOKEN_ISSUANCE).trim());
        } catch (NumberFormatException | GenericException ex) {
            LOGGER.log(Level.SEVERE, () -> "Not a number or no entry for '" + PropertyName.FP_MATCH_MIN_THRESHOLD_TOKEN_ISSUANCE + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
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
            labourDetailsTableRowRow.setCount(labourDetailsTableRowRow.getCount() + 1);
            LOGGER.log(Level.INFO, () -> "***Fingerprint not matched for labour id: " + labourDetailsTableRowRow.getLabourId() + "***\n\tcount: " + labourDetailsTableRowRow.getCount());
            if (labourDetailsTableRowRow.getCount() >= LABOUR_FP_AUTH_ALLOWED_MAX_ATTEMPT) {
                LOGGER.log(Level.INFO, () -> "***The allowed number of attempts for Labor id: " + labourDetailsTableRowRow.getLabourId() + " has been exhausted.");
                updateUi("The allowed number of attempts for Labor id: " + labourDetailsTableRowRow.getLabourId() + " has been exhausted.");
                if (labourDetailsTableRowRow.getCount() > LABOUR_FP_AUTH_ALLOWED_MAX_ATTEMPT) {
                    return;
                }
                if (labourDetailsTableRowRow.getCount() == LABOUR_FP_AUTH_ALLOWED_MAX_ATTEMPT) {
                    dispenseToken(labour, false);
                    tableView.getItems().remove(labourDetailsTableRowRow);
                    tableView.refresh();
                }
            } else if (labourDetailsTableRowRow.getCount() < 3) {
                updateUi("Fingerprint not matched for labour id: " + labourDetailsTableRowRow.getLabourId());
            } else {
                updateUi("The remaining attempt(s) for " + labourDetailsTableRowRow.getLabourId() + " : " + (LABOUR_FP_AUTH_ALLOWED_MAX_ATTEMPT - labourDetailsTableRowRow.getCount()));
            }
            return; // return since fingerprint auth failed
        }
        updateUi("Fingerprint matched for labour id: " + labourDetailsTableRowRow.getLabourId());
        //now match found.
        dispenseToken(labour, true);
    }

    private void dispenseToken(Labour labour, boolean issueToken) {
        TokenReqDto tokenReqDto;
        if (issueToken) {
            //dispenses token on card writer
            if (!TokenDispenserUtil.dispenseToken()) {
                updateUi("Kindly check the Token Dispenser and try again.");
                return;
            }
            try {
                tokenReqDto = startProcedureCall(labour);
            } catch (GenericException | NoReaderOrCardException ex) {
                updateUi(ex.getMessage());
                return;
            } catch (ConnectionTimeoutException ex) {
                updateUi("Something went wrong. Kindly check Card API service.");
                return;
            }
        } else {
            tokenReqDto = createTokenReqDto(labour.getDynamicFile().getLabourId(), "0", "0");
            tokenReqDto.setLabourStatus("Unsuccess");
            tokenReqDto.setTokenIssuanceStatus("Pending");
        }
        LOGGER.log(Level.INFO, () -> "***Updating token status to the server.");
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

        LOGGER.log(Level.INFO, () -> "***ServerResponseErrorCode: " + resDto.getErrorCode());
        LOGGER.log(Level.INFO, () -> "***ServerResponseDesc: " + resDto.getDesc()); // to be removed
        String errorDescLowerCase = resDto.getDesc().toLowerCase();
        if (resDto.getErrorCode() != 0) {
            if (errorDescLowerCase.contains("not found")) {
                updateUi("No labour with id: " + labour.getDynamicFile().getLabourId() + " found in server.");
            } else if (errorDescLowerCase.contains("already") || errorDescLowerCase.contains("token no. '0'")) {
                updateUi("Token for labour: " + labour.getDynamicFile().getLabourId() + " has already been issued.");
            } else {
                updateUi("Received an unexpected response from the server. Kindly try again.");
            }
            return;
        }

        Optional<LabourDetailsTableRow> labourDetailsTableRowOptional = tableView.getItems().stream().filter(labourDetailsTableRow -> labourDetailsTableRow.getLabourId().equals(labour.getDynamicFile().getLabourId())).findFirst();
        LabourDetailsTableRow labourDetailsTableRow = labourDetailsTableRowOptional.orElseThrow(() -> new GenericException("No matching labor id found in the table."));

        if (issueToken) {
            updateUi("Kindly collect the token.");
            labourDetailsTableRow.setStrStatus("token issued"); // not really import now
            LOGGER.log(Level.INFO, "Token dispensed successfully.");
        } // for auth fp auth failure, already updated on UI

        tableView.getItems().remove(labourDetailsTableRow);
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
                    LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                    throw new GenericException(GENERIC_ERR_MSG);
                }
            });
        }

    }

    private TokenReqDto startProcedureCall(Labour labour) {

        // If user removes the contractor card, the handle will change, so to be on the safe, lets
        // DeInitialize and start over again.

        /*
            DeInitialize
            Initialize
            waitForConnect - card
            selectApp - card
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
        LOGGER.log(Level.INFO, () -> "***LabourController: Calling deInitialize API.");
        Asn1CardTokenUtil.deInitialize();
        LOGGER.log(Level.INFO, () -> "***LabourController: Calling initialize API.");
        Asn1CardTokenUtil.initialize();
        // setup reader; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Card: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitFocConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.INFO, () -> "***Card: Calling waitForConnect API.");
        CRWaitForConnectResDto crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_READER_NAME);
        // already handled for non-zero error code
        byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "****CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        if (!TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getCardChipSerialNo().equals(Strings.fromByteArray(Hex.encode(decodedHexCsn)).toUpperCase())) {
            LOGGER.log(Level.INFO, () -> "****Current CSN not matched with the saved CSN.");
            throw new GenericException("No game. Please use the previous contractor card.");
        }
        int cardHandle = crWaitForConnectResDto.getHandle();
        TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().setCardHandle(cardHandle);
        Asn1CardTokenUtil.selectApp(CARD_TYPE_NUMBER, cardHandle);

        // setup writer; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Token: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitFocConnect API call.");
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

        LOGGER.log(Level.INFO, () -> "***Token: Calling waitForConnect API.");
        crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_WRITER_NAME);
        decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "CSNError: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        String tokenCsn = Strings.fromByteArray(Hex.encode(decodedHexCsn));
        int tokenHandle = crWaitForConnectResDto.getHandle();
        LOGGER.log(Level.INFO, () -> "***Token: Calling selectApp API.");
        Asn1CardTokenUtil.selectApp(TOKEN_TYPE_NUMBER, tokenHandle);
        LOGGER.log(Level.INFO, () -> "***Token: Calling readData API to read static data to get the token number.");
        byte[] asn1EncodedTokenStaticData = Asn1CardTokenUtil.readBufferedData(tokenHandle, CardTokenFileType.STATIC);
        String tokenNumber = new String(extractFromAsn1EncodedStaticData(asn1EncodedTokenStaticData, 1), StandardCharsets.UTF_8);

        // read cert now
        LOGGER.log(Level.INFO, () -> "***Card: Calling readData API for reading system certificate.");
        byte[] systemCertificate = Asn1CardTokenUtil.readBufferedData(TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getCardHandle(), CardTokenFileType.SYSTEM_CERTIFICATE);

        LOGGER.log(Level.INFO, () -> "***Token: Calling verifyCertificate API: handle=token");
        Asn1CardTokenUtil.verifyCertificate(tokenHandle, WHICH_TRUST, WHICH_CERTIFICATE, systemCertificate);

        LOGGER.log(Level.INFO, () -> "***Token: Calling pkiAuth API: handle1=token, handle2=card");
        Asn1CardTokenUtil.pkiAuth(tokenHandle, TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getCardHandle());

        Asn1CardTokenUtil.storeAsn1EncodedDynamicFile(tokenHandle, labour.getDynamicFileAsn1());
        Asn1CardTokenUtil.storeAsn1EncodedDefaultValidityFile(tokenHandle, labour.getDefaultValidityFileAsn1());
        Asn1CardTokenUtil.storeAsn1EncodedSpecialAccessFile(tokenHandle, labour.getAccessFileAsn1());
        Asn1CardTokenUtil.storeAsn1EncodedSignFile1(tokenHandle, labour.getSignFile1());
        Asn1CardTokenUtil.storeAsn1EncodedSignFile3(tokenHandle, labour.getSignFile3());
        Asn1CardTokenUtil.encodeToAsn1AndStorePhotoFile(tokenHandle, labour.getPhoto());
        Asn1CardTokenUtil.encodeToAsn1AndStoreFingerprintFile(tokenHandle, labour.getFps());
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
        tokenReqDto.setLabourStatus("Success");
        tokenReqDto.setTokenIssuanceStatus("Issued");
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

    @FXML
    private void home() throws IOException {
        App.setRoot("main_screen");
    }
}
