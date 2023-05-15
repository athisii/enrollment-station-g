package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.LabourResDto;
import com.cdac.enrollmentstation.dto.UpdateTokenResponse;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.*;
import com.cdac.enrollmentstation.util.CardWrite;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;


public class LabourController implements MIDFingerAuth_Callback {
    private static final Logger LOGGER = ApplicationLog.getLogger(LabourController.class);

    private static final int NUMBER_OF_ROWS_PER_PAGE = 8;
    private int jniErrorCode;

    //***********************Fingerprint***************************//
    private MIDFingerAuth midFingerAuth; // For MID finger jar
    private DeviceInfo deviceInfo;
    private boolean isDeviceInitialized;
    private static final int MIN_QUALITY = 60;
    private static final int FINGERPRINT_CAPTURE_TIMEOUT_IN_SEC = 10;

    //***********************Fingerprint***************************//

    private List<LabourDetails> labourListForTable;

    private Map<String, Labour> labourMap;
    private LabourFP matchedLabourFp;

    @FXML
    private Label lblContractName;

    @FXML
    private Label lblContractorName;

    @FXML
    private Label messageLabel;

    @FXML
    private javafx.scene.image.ImageView fingerprintImageView;


    @FXML
    private TableView<LabourDetails> tableview;

    @FXML
    private TableColumn<LabourDetails, String> labourName;

    @FXML
    private TableColumn<LabourDetails, String> labourID;

    @FXML
    public TableColumn<LabourDetails, String> dateOfBirth;

    @FXML
    private TableColumn<LabourDetails, String> strStatus;

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
        midFingerAuth = new MIDFingerAuth(this);
        if (!initFpReader()) {
            return;
        }
        // initially disable it until a row is selected
        captureBtn.setDisable(true);
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
        DetailsHolder detailsHolder = DetailsHolder.getDetailsHolder();
        lblContractorName.setText(detailsHolder.getContractorInfo().getContractorName());
        lblContractName.setText(detailsHolder.getContractorInfo().getContractId());

        ContractorInfo contractDetails = detailsHolder.getContractorInfo();
        LabourResDto labourResDto;
        try {
            labourResDto = MafisServerApi.fetchLabourList(contractDetails.getContractorId(), contractDetails.getContractId());
        } catch (GenericException ex) {
            messageLabel.setText(ex.getMessage());
            return;
        }

        if (labourResDto == null) {
            messageLabel.setText("Connection timeout. Please try again.");
            return;
        }

        if (!"0".equals(labourResDto.getErrorCode())) {
            messageLabel.setText(labourResDto.getDesc());
            return;
        }
        if (labourResDto.getLabours().isEmpty()) {
            messageLabel.setText("No labour found for contractor id: " + contractDetails.getContractorId() + " with contract id: " + contractDetails.getContractId());
            return;
        }

        labourListForTable = new ArrayList<>();
        labourMap = new HashMap<>();

        for (Labour labour : labourResDto.getLabours()) {
            if (labour.getDynamicFileList() == null || labour.getDynamicFileList().isEmpty()) {
                // for debugging purposes
                LOGGER.log(Level.INFO, "Labour dynamicFileList is null or empty.");
            } else {
                for (DynamicFileList dynamicFileList : labour.getDynamicFileList()) {
                    LabourDetails labourDetails = new LabourDetails();
                    labourDetails.setDateOfBirth(dynamicFileList.getLabourDateOfBirth());
                    labourDetails.setLabourID(dynamicFileList.getLabourId());
                    labourDetails.setLabourName(dynamicFileList.getLabourName());
                    labourDetails.setStrStatus("Not verified"); //not sure why?
                    labourListForTable.add(labourDetails);
                    labourMap.put(dynamicFileList.getLabourId(), labour);
                }
            }

        }
        DetailsHolder.getDetailsHolder().setLabours(labourResDto.getLabours());
        updateLabourDetailsInTable(labourListForTable);
    }

    public void updateLabourDetailsInTable(List<LabourDetails> labourList) {
        int extraPage;
        if (labourList.size() % NUMBER_OF_ROWS_PER_PAGE == 0) {
            extraPage = 0;
        } else {
            extraPage = 1;
        }
        int pageCount = labourList.size() / NUMBER_OF_ROWS_PER_PAGE + extraPage;

        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex > pageCount) {
                return null;
            }
            return createPage(pageIndex);
        });

        searchBox.textProperty().addListener((observable, oldValue, newValue) -> tableview.setItems(filterList(labourList, newValue)));

        ObservableList<LabourDetails> observablelist = FXCollections.observableArrayList(labourList);

        labourName.setCellValueFactory(new PropertyValueFactory<>("labourName"));
        labourID.setCellValueFactory(new PropertyValueFactory<>("labourID"));
        dateOfBirth.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        strStatus.setCellValueFactory(new PropertyValueFactory<>("strStatus"));

        tableview.setStyle(".table-row-cell {-fx-font-size: 12pt ;}");
        tableview.setFixedCellSize(35.0);
        tableview.setItems(observablelist);
        tableview.refresh();

        tableview.setRowFactory(tv -> {
            TableRow<LabourDetails> row = new TableRow<>() {
                @Override
                public void updateItem(LabourDetails item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item.getStrStatus().equalsIgnoreCase("verified")) {
                        setStyle("-fx-background-color: green;");
                    } else {
                        setStyle("");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                // check for non-empty rows, double-click with the primary button of the mouse
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    captureBtn.setDisable(false);
                }
            });
            return row;
        });
    }

    @FXML
    private void showHome() throws IOException {
        App.setRoot("contract");
    }

    @FXML
    private void captureBtnAction() {
        messageLabel.setText("");
        captureBtn.setDisable(true);
        if (!isDeviceInitialized && (!initFpReader())) {
            //message updated by initFpReader()
            captureBtn.setDisable(false);
            return;
        }
        if (tableview.getSelectionModel().getSelectedItem() == null) {
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
        LabourDetails labourDetailsRow = tableview.getSelectionModel().getSelectedItem();
        if (labourDetailsRow == null) {
            updateUi("Kindly select a labour.");
            return;
        }

        Labour labour = labourMap.get(labourDetailsRow.getLabourID());
        if (labour == null) {
            updateUi("No labour details found for selected labour id: " + labourDetailsRow.getLabourID());
            return;
        }

        if (labour.getFps().isEmpty()) {
            updateUi("No fingerprint data for selected labour id: " + labourDetailsRow.getLabourID());
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
                matchedLabourFp = labour.getFps().get(i);
                break;
            }
        }

        if (!matchFound) {
            updateUi("Fingerprint not matched for labour id: " + labourDetailsRow.getLabourID());
            return;
        }
        updateUi("Fingerprint matched for labour id: " + labourDetailsRow.getLabourID());
        //now match found.
        dispenseToken(labourDetailsRow, labour);
    }

    private void dispenseToken(LabourDetails labourDetailsRow, Labour labour) {
        //dispenses token on card writer
        if (!TokenDispenserUtil.dispenseToken()) {
            updateUi("Kindly connect the Token Dispenser And Try Again");
            return;
        }

        ContractorDetailsFile contractorDetailsFile = new ContractorDetailsFile();
        // already checked for nullity and emptiness
        // need to confirm why it comes in list.
        DynamicFileList dynamicFileListAtIndex0 = labour.getDynamicFileList().get(0);
        contractorDetailsFile.setDynamicContractorId(dynamicFileListAtIndex0.getContractorId());
        contractorDetailsFile.setDynamicIssuanceUnit(dynamicFileListAtIndex0.getIssuanceUnit());
        contractorDetailsFile.setDynamicUserCategoryId(dynamicFileListAtIndex0.getUserCategoryId());

        if (labour.getAccessFileList() == null || labour.getAccessFileList().isEmpty()) {
            LOGGER.log(Level.INFO, "Access file list is null or empty");
        } else {
            AccessFileList accessFileListAtIndex0 = labour.getAccessFileList().get(0);
            contractorDetailsFile.setAccessUnitCode(accessFileListAtIndex0.getUnitCode());
            contractorDetailsFile.setAccessZoneId(accessFileListAtIndex0.getZoneId());
            contractorDetailsFile.setAccessWorkingHourCode(accessFileListAtIndex0.getWorkingHourCode());
            contractorDetailsFile.setAccessFromDate(accessFileListAtIndex0.getFromDate());
            contractorDetailsFile.setAccessToDate(accessFileListAtIndex0.getToDate());
        }

        contractorDetailsFile.setSignatureFile1(labour.getSignFile1());
        contractorDetailsFile.setSignatureFile3(labour.getSignFile3());
        contractorDetailsFile.setLabourPhoto(labour.getPhoto());
        contractorDetailsFile.setLabourFpPos(matchedLabourFp.getFpPos());
        contractorDetailsFile.setLabourFpData(matchedLabourFp.getFpData());

        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        holder.setLabourDetails(labour);
        holder.setContractorDynamicDetails(contractorDetailsFile);

        CardWrite cardwrite = new CardWrite();
        String returnedCardWriteMessage = cardwrite.cardWriteDeatils();
        if (returnedCardWriteMessage.toLowerCase().contains("failure")) {
            updateUi(returnedCardWriteMessage);
            return;
        }
        //Token update Details to Mafis API
        UpdateToken updateToken = new UpdateToken();
        DetailsHolder detail = DetailsHolder.getDetailsHolder();
        ContractorInfo contractorInfo = detail.getContractorInfo();
        updateToken.setCardCSN(contractorInfo.getSerialNo());    // Need to be changed later
        updateToken.setContractorCSN(contractorInfo.getSerialNo());
        updateToken.setContractorID(contractorInfo.getContractorId());
        updateToken.setContractID(contractorInfo.getContractId());
        updateToken.setEnrollmentStationUnitID(MafisServerApi.getEnrollmentStationUnitId());
        updateToken.setUniqueNo(labourDetailsRow.getLabourID());
        updateToken.setEnrollmentStationID(MafisServerApi.getEnrollmentStationId());
        updateToken.setTokenID(contractorInfo.getSerialNo());   // Need to be changed later
        updateToken.setVerifyFPSerialNo(deviceInfo.SerialNo);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        updateToken.setTokenIssuanceDate(dtf.format(LocalDateTime.now()));

        UpdateTokenResponse updateTokenResponse;
        try {
            updateTokenResponse = MafisServerApi.updateTokenStatus(updateToken);
        } catch (GenericException ex) {
            updateUi(ex.getMessage());
            return;
        }
        // connection timeout
        if (updateTokenResponse == null) {
            LOGGER.log(Level.INFO, "Connection timeout. Failed to update token status to server.");
            updateUi("Connection timeout. Failed to update token status to server. Please try again.");
            return;
        }

        if (!"0".equals(updateTokenResponse.getErrorCode())) {
            LOGGER.log(Level.SEVERE, () -> "Error Desc: " + updateTokenResponse.getDesc());
            updateUi(updateTokenResponse.getDesc());
            return;
        }
        updateUi("Kindly collect the token");
        LOGGER.log(Level.INFO, "Token dispensed successfully.");
        labourDetailsRow.setStrStatus("verified");
        tableview.getItems().remove(labourDetailsRow);
        tableview.refresh();
        if (tableview.getItems().isEmpty()) {
            try {
                App.setRoot("contract");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                throw new GenericException(GENERIC_ERR_MSG);
            }
        }

    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * 8;
        int toIndex = Math.min(fromIndex + 8, labourListForTable.size());
        tableview.setFixedCellSize(30.0);
        tableview.setItems(FXCollections.observableArrayList(labourListForTable.subList(fromIndex, toIndex)));
        return tableview;
    }

    private ObservableList<LabourDetails> filterList(List<LabourDetails> labourDetailsList, String searchText) {
        List<LabourDetails> filteredList = new ArrayList<>();
        for (LabourDetails labourData : labourDetailsList) {
            if (labourData.getLabourID().toLowerCase().contains(searchText.toLowerCase()) || labourData.getLabourName().toLowerCase().contains(searchText.toLowerCase()) || labourData.getDateOfBirth().toLowerCase().contains(searchText.toLowerCase())) {
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
    public void OnComplete(int errorCode, int Quality, int NFIQ) {
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
        // tries matching fingerprint
        matchFingerprintTemplate(template);
    }
}


