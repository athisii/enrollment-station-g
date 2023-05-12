//package com.cdac.enrollmentstation.controller;
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.util.TestProp;
//import com.cdac.enrollmentstation.api.APIServerCheck;
//import com.cdac.enrollmentstation.dto.UpdateTokenResponse;
//import com.cdac.enrollmentstation.logging.ApplicationLogOld;
//import com.cdac.enrollmentstation.model.*;
////import com.cdac.enrollmentstation.service.CardWrite;
//import com.cdac.enrollmentstation.service.TokenDispense;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mantra.midfingerauth.DeviceInfo;
//import com.mantra.midfingerauth.MIDFingerAuth;
//import com.mantra.midfingerauth.MIDFingerAuth_Callback;
//import com.mantra.midfingerauth.enums.DeviceDetection;
//import com.mantra.midfingerauth.enums.DeviceModel;
//import com.mantra.midfingerauth.enums.TemplateFormat;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.Node;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.WritableImage;
//import javafx.scene.input.MouseButton;
//import javafx.util.Callback;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.net.URL;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//
//public class FetchLabourDetailsController implements Initializable, MIDFingerAuth_Callback {
//    @FXML
//    private Label lblContractName;
//
//    @FXML
//    private Label lblContractorName;
//
//    @FXML
//    public Label lblWorkerError;
//
//    @FXML
//    private javafx.scene.image.ImageView mFingerPrintImage;
//
//
//    @FXML
//    public TableView<LabourDetails> tableview;
//
//    @FXML
//    public TableColumn<LabourDetails, String> labourName;
//
//    @FXML
//    public TableColumn<LabourDetails, String> labourID;
//
//    @FXML
//    public TableColumn<LabourDetails, String> dateOfBirth;
//
//    @FXML
//    public TableColumn<LabourDetails, String> strStatus;
//
//    @FXML
//    private TextField searchBox;
//
//    @FXML
//    public Button captureSingleFinger;
//
//    @FXML
//    private Pagination pagination;
//
//    List<Labour> labourFpLists = new ArrayList<>();
//
//
//    List<LabourDetails> labourList = new ArrayList<LabourDetails>();
//
//    //List<AccessDetails> specialAccessList = new ArrayList<AccessDetails>();
//
//    Map<String, Labour> labourMap = new HashMap<String, Labour>();
//
//    public APIServerCheck apiServerCheck = new APIServerCheck();
//
//    ContractorDynamicFile contractorDynamicFile = new ContractorDynamicFile();
//
//    public String id;
//    public String name;
//    public String dob;
//
//    int fingerprintInit;
//    int fpQuality = 96;
//    String fpquality = null;
//
//
//    TestProp prop = new TestProp();
//
//    //For Application Log
//    ApplicationLogOld appLog = new ApplicationLogOld();
//    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
//    Handler handler;
//
//    @FXML
//    private Button capturefinger;
//    //MFS100 mfs100 = null;
//    //int quality = 60;
//    int minQuality = 60;
//    int timeout = 10000;
//    byte[] ISOTemplate = null;
//    byte[] ANSITemplate = null;
//    String key = "";
//    //DeviceInfo deviceInfo = new DeviceInfo();
//
//    private MIDFingerAuth midFingerAuth = null; // For MID finger jar
//    private DeviceInfo deviceInfo = null;
//    private byte[] lastCaptureTemplat = null;
//
//    public void messageStatus(String message) {
//        lblWorkerError.setText(message);
//    }
//
//
//    public FetchLabourDetailsController() {
//
//
//        // this.handler = appLog.getLogger();
//        // LOGGER.addHandler(handler);
//
//        //MIDfinger
//        midFingerAuth = new MIDFingerAuth(this);
//
//        String version = midFingerAuth.GetSDKVersion();
//
//
//        //mfs100 = new MFS100(this, key);
//        try {
//            //System.out.println("JAVA_VERSION: " + System.getProperty("java.version"));
//            LOGGER.log(Level.INFO, "JAVA_VERSION:" + System.getProperty("java.version"));
//
//        } catch (Exception e) {
//            //e.printStackTrace();
//            LOGGER.log(Level.INFO, "Exception:" + e);
//        }
//
//        //MIDfinger
//        List<String> deviceList = new ArrayList<String>();
//        //deviceList = null;
//        //int ret = midFingerAuth.GetSupportedDevices(deviceList);
//        int ret = midFingerAuth.GetConnectedDevices(deviceList);
//
//        if (ret != 0) {
//            //System.out.println("supported devices " +midFingerAuth.GetErrorMessage(ret));
//            LOGGER.log(Level.INFO, "supported devices:" + midFingerAuth.GetErrorMessage(ret));
//            return;
//        }
//
//        for (int i = 0; i < deviceList.size(); i++) {
//
//            // Print all elements of List
//            //System.out.println(deviceList.get(i));
//            LOGGER.log(Level.INFO, "deviceList:" + deviceList.get(i));
//        }
//        //String model = jcbConnectedDevices.getSelectedItem().toString();
//        String model = "MFS100";
//        boolean isDeviceConnected = midFingerAuth.IsDeviceConnected(DeviceModel.valueFor(deviceList.get(0)));
//        if (isDeviceConnected) {
//            //System.out.println("Device is connected... ");
//            LOGGER.log(Level.INFO, "Device is connected...");
//        } else {
//            //System.out.println("Device is not Connected...");
//            LOGGER.log(Level.INFO, "Device is not connected...");
//        }
//                   /*
//                    if(mfs100.IsConnected()){
//                        System.out.println("Device is connected... ");
//                        System.out.println(mfs100.GetSDKVersion());
//
//                    }else{
//                        System.out.println("Device is not Connected...");
//                        //lblworkererror.setText("Kindly Reconnect the Single Fingerprint reader");
//                    }*/
//
//        //MIDFinger Init
//        // String model = jcbConnectedDevices.getSelectedItem().toString();
//        DeviceInfo info = new DeviceInfo();
//        int fingerprintinit = midFingerAuth.Init(DeviceModel.valueFor(model), info);
//        if (fingerprintinit != 0) {
//            //System.out.println("Device Initialization not success");
//            //System.out.println("Device Initialization not success"+midFingerAuth.GetErrorMessage(fingerprintinit));
//            LOGGER.log(Level.INFO, "Device Initialization not success:" + midFingerAuth.GetErrorMessage(fingerprintinit));
//            return;
//        }
//
//        if (fingerprintinit == 0) {
//            deviceInfo = info;
//            System.out.println("Width: " + String.valueOf(deviceInfo.Width));
//            System.out.println("Height: " + String.valueOf(deviceInfo.Height));
//        } else {
//            //System.out.println("Error Single Fingerprint reader not initialized");
//            LOGGER.log(Level.INFO, "Error Single Fingerprint reader not initialized");
//            //lblworkererror.setText("Kindly Reconnect the Single Fingerprint reader");
//        }
//
//                    /*
//                    fingerprintinit = mfs100.Init();
//
//                    if(fingerprintinit == 0){
//                       deviceInfo = mfs100.GetDeviceInfo();
//                       System.out.println("Width: "+ String.valueOf(deviceInfo.Width()));
//                       System.out.println("Height: "+String.valueOf(deviceInfo.Height()));
//
//                    }else{
//                        System.out.println("Error Single Fingerprint reader not initialized");
//                        //lblworkererror.setText("Kindly Reconnect the Single Fingerprint reader");
//                    }*/
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//
//        String response = listLabourDetailsInfxml();
//        messageStatus(response);
//
//
////        try {
////           startCapture();
////        } catch (InterruptedException ex) {
////            Logger.getLogger(FetchLabourdetailscontroller.class.getName()).log(Level.SEVERE, null, ex);
////        }
//    }
//
//    public String listLabourDetailsInfxml() {
//        String response = "";
//        try {
//
//            Details detail = Details.getdetails();
//            //To set the Contrator Name
//            lblContractorName.setText(detail.getContractDetail().getContractorName());
//            String jsonLabourList = "";
//            String connurl = apiServerCheck.getLabourListURL();
//            ContactDetail contractDetails = detail.getContractDetail();
//            jsonLabourList = apiServerCheck.getLabourListAPI(connurl, contractDetails.getContractorId(), contractDetails.getContactId());
//            System.out.println("Decrypted Labour List :" + jsonLabourList);
//            if (jsonLabourList.contains("Exception")) {
//                response = "Labour List is not available from Server, Try Again";
//                return response;
//            }
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY); //Added for single value as array for dynamic details
//            //objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
//            //System.out.println("contractTableView in fetchdetailscontroller:"+ contractTableView);
//            LabourListDetails labourListResponse = null;
//
//            try {
//                //uncomment after
//                //System.out.println(jsonLabourList);
//                labourListResponse = objectMapper.readValue(jsonLabourList, LabourListDetails.class);
//                //For testing purpose
//                //labourListResponse.getLaboursList().add(labourListResponse.getLaboursList().get(0));
//                //labourListResponse.getLaboursList().add(labourListResponse.getLaboursList().get(0));
//                //healthResponse = objectMapper.readValue(testJson, LabourListDetails.class);
//                System.out.println("labour list detals : " + labourListResponse.toString());
//            } catch (JsonProcessingException ex) {
//                Logger.getLogger(FetchLabourDetailsController.class.getName()).log(Level.SEVERE, null, ex);
//                LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
//                //lblworkererror.setText("Labour List is not Available or not approved for today");
//                response = "Labour List from Server is Corrupted, Try Again";
//                return response;
//            }
//
//            String errorcode_labour = labourListResponse.getErrorCode();
//            System.out.println("Error Code ::: " + errorcode_labour);
//            if (!errorcode_labour.equals("0")) {
//                System.out.println("Error" + labourListResponse.getDesc());
//                //lblworkererror.setText(labourListResponse.Desc);
//                response = labourListResponse.getDesc();
//                return response;
//            }
//
//            if (labourListResponse.getLabourList() != null) {
//                labourFpLists = labourListResponse.getLabourList();
//            }
//
//
//            if (labourListResponse.getLabourList().size() > 0) {
//                for (Labour labour : labourListResponse.getLabourList()) {
//                    //Dynamic File List For a Labour
//                    for (DynamicFileList dynamicFileList : labour.getDynamicFileList()) {
//                        System.out.println("Contractor ID::::" + dynamicFileList.getContractorId());
//                        contractorDynamicFile.setDynamicContractorId(dynamicFileList.getContractorId());
//                        contractorDynamicFile.setDynamicIssuanceUnit(dynamicFileList.getIssuanceUnit());
//                        contractorDynamicFile.setDynamicUserCategoryId(dynamicFileList.getUserCategoryId());
//                        //Modified by K. Karthikeyan
//                        System.out.println("\nContractor ID: " + dynamicFileList.getContractorId() + "\nIssuance Unit: " + dynamicFileList.getIssuanceUnit());
//                        System.out.println("\nUserCategory ID: " + dynamicFileList.getUserCategoryId());
//                        //
//                    }
//                    //Access Files Lsit For a Labour
//                    for (AccessFileList accessFileList : labour.getAccessFileList()) {
//                        contractorDynamicFile.setAccessPermissionUnitCode(accessFileList.getUnitCode());
//                        contractorDynamicFile.setAccessPermissionZoneId(accessFileList.getZoneId());
//                        contractorDynamicFile.setAccessPermissionWorkingCode(accessFileList.getWorkingHourCode());
//                        contractorDynamicFile.setAccessDetailsFromDate(accessFileList.getFromDate());
//                        contractorDynamicFile.setAccessDetailsToDate(accessFileList.getToDate());
//                        //Modified by K. Karthikeyan
//                        System.out.println("\nAccPerUnitCode: " + accessFileList.getUnitCode() + "\nZoneId : " + accessFileList.getZoneId());
//                        System.out.println("\nAccPerWorkingCode: " + accessFileList.getWorkingHourCode() + "\nFrom Date: " + accessFileList.getFromDate());
//                        System.out.println("To Date: " + accessFileList.getToDate());
//                        //Modified by K. Karthikeyan
//                    }
//                    //Fingerprint List For a labour
//                    for (LabourFP labourFP : labour.getFPs()) {
//                        contractorDynamicFile.setLabourFpPos(labourFP.getFpPos());
//                        contractorDynamicFile.setLabourFpData(labourFP.getFpData());
//                        //Modified by K. Karthikeyan
//                        //System.out.println("\nFpPos "+labourFP.getFpPos()+"\nFpData"+labourFP.getFpData());
//                    }
//
//                    contractorDynamicFile.setSignatureFile1(labour.getSignFile1());
//                    contractorDynamicFile.setSignatureFile3(labour.getSignFile3());
//                }
//            } else {
//                //lblworkererror.setText("Labour List is not Available or not approved for today");
//                response = "Labour List is not Available or not approved for today";
//                LOGGER.log(Level.INFO, "Labour List is not Available or not approved for today");
//                return response;
//            }
//
//
//            //Set the Contract Name
//            lblContractName.setText(contractorDynamicFile.getDynamicContractorId());
//            //System.out.println("Dynamic Contractor ID :"+contractorDynamicFile.getDynamicContractorId());
//            LOGGER.log(Level.INFO, "Dynamic Contractor ID :" + contractorDynamicFile.getDynamicContractorId());
//
//
//            List<LabourDetails> labourListres = new ArrayList<LabourDetails>();
//
//
//            if (labourListResponse != null) {
//                if (labourListResponse.getLabourList().size() > 0) {
//                    for (Labour labour : labourListResponse.getLabourList()) {
//                        for (DynamicFileList dynamicFileList : labour.getDynamicFileList()) {
//                            LabourDetails labourDetails = new LabourDetails();
//
//                            labourDetails.setDateOfBirth(dynamicFileList.getLabourDateOfBirth());
//                            labourDetails.setLabourID(dynamicFileList.getLabourId());
//                            labourDetails.setLabourName(dynamicFileList.getLabourName());
//                            labourDetails.setStrStatus("Not verified");
//                            labourListres.add(labourDetails);
//                            // labourMap.put(labour.getLabourId(), labour);
//                            labourMap.put(dynamicFileList.getLabourId(), labour);
//                            //Modified by K. Karthikeyan
//                            System.out.println("\nDOB : " + dynamicFileList.getLabourDateOfBirth() + "\nLabourID" + dynamicFileList.getLabourId());
//                            System.out.println("\nName : " + dynamicFileList.getLabourName());//+"\nStrstatus"+dynamicFileList.getLabourId());
//
//                        }
//
//                    }
//
//                }
//            }
//
//            labourList = labourListres;
//
//            System.out.println("LABOUR LIST:::::" + labourList.toString());
//            System.out.println("LABOUR LIST:::::" + labourList.size());
//
//            String uptadelabourdetailstable = updateLabourDetailsInTable(labourList, labourListResponse);
//
//            //System.out.println("Update Labour Details"+uptadelabourdetailstable);
//
//        } catch (NullPointerException e) {
//            //System.out.print("NullPointerException caught");
//            LOGGER.log(Level.INFO, "Exception :" + e);
//            //lblworkererror.setText("Labour List is not Available or not approved for today");
//            response = "Labour List is not Available or not approved for today";
//            return response;
//        }
//        return response;
//    }
//
//    public String updateLabourDetailsInTable(List<LabourDetails> labourList, LabourListDetails labourListResponse) {
//        Details detail = Details.getdetails();
//        int extra_page = 0;
//        if (labourList.size() % 8 == 0)
//            extra_page = 0;
//        else
//            extra_page = 1;
//        int pagesize = labourList.size() / 8 + extra_page;
//        //System.out.println("Labour List Size::"+labourList.size());
//        LOGGER.log(Level.INFO, "Labour List Size::" + labourList.size());
//        LOGGER.log(Level.INFO, "Page Size::" + pagesize);
//        //System.out.println("Page Size::"+pagesize);
//        pagination.setPageCount(pagesize);
//        pagination.setCurrentPageIndex(0);
//
//        //Set Page Factory
//        pagination.setPageFactory(new Callback<Integer, Node>() {
//            @Override
//            public Node call(Integer pageIndex) {
//                System.out.println("page index :" + pageIndex);
//                if (pageIndex > labourList.size() / 8 + 1) {
//                    return null;
//                } else {
//                    return createPage(pageIndex);
//                }
//            }
//        });
//
//        //Set Row Factory
//        tableview.setRowFactory(tv -> new TableRow<LabourDetails>() {
//            @Override
//            public void updateItem(LabourDetails item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null) {
//                    setStyle("");
//                } else if (item.getStrStatus().equals("verified")) {
//                    setStyle("-fx-background-color: green;");
//                    //setStyle("-fx-background-color: #66ccff;");
//                } else {
//                    setStyle("");
//                }
//            }
//        });
//        //Added on 07-03-22
//
//        ObservableList<LabourDetails> observablelist = FXCollections.observableArrayList(labourList);
//
//        System.out.println("Observable List::::" + observablelist.toString());
//        searchBox.textProperty().addListener((observable, oldValue, newValue) ->
//                tableview.setItems(filterList(labourList, newValue))
//        );
//
//
//        labourName.setCellValueFactory(new PropertyValueFactory<LabourDetails, String>("labourName"));
//        labourID.setCellValueFactory(new PropertyValueFactory<LabourDetails, String>("labourID"));
//        dateOfBirth.setCellValueFactory(new PropertyValueFactory<LabourDetails, String>("dateOfBirth"));
//        strStatus.setCellValueFactory(new PropertyValueFactory<LabourDetails, String>("strStatus"));
//        detail.setLabourListDetail(labourListResponse);
//
//        tableview.setStyle(".table-row-cell {-fx-font-size: 12pt ;} ");
//        tableview.setFixedCellSize(35.0);
//
//        tableview.setItems(observablelist);
//        tableview.refresh();
//        System.out.println("Observable List2::::" + observablelist.toString());
//
//        tableview.setRowFactory(tv -> {
//            TableRow<LabourDetails> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//                // check for non-empty rows, double-click with the primary button of the mouse
//                captureSingleFinger.setDisable(false);
//                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
//                    LabourDetails element = row.getItem();
//                    // now you can do whatever you want with the myModel variable.
//                    System.out.println(element.toString());
//                    id = element.getLabourID();
//                    name = element.getLabourName();
//                    dob = element.getDateOfBirth();
//                    //System.out.println(name);
//                    LOGGER.log(Level.INFO, "name::" + name);
//
//                    //setContractID(element);
//
//                }
//            });
//            return row;
//        });
//        return "success";
//    }
//
//   /*
//    public void startCapture() throws InterruptedException{
//              lblworkererror.setText("Start Capture");
//        Runnable helloRunnable = new Runnable() {
//            public void run() {
//
//                    int retValue = mfs100.StartCapture(quality, timeout, true);
//                    System.out.println("ret val :"+ retValue);
//
//                    if( retValue != 0 ) {
//                    System.out.println("Error..!!");
//                  }
//
//            }
//    };
//
//ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//executor.scheduleAtFixedRate(helloRunnable, 0, 5, TimeUnit.SECONDS);
//
//    } */
//
//    @FXML
//    private void showHome() throws IOException {
//        App.setRoot("list_contract");
//    }
//
//    @FXML
//    private void btnInitActionPerformed() throws Exception {
//
//        // lblworkererror.setText("Button Init");
////            Object object =  tableview.getSelectionModel().selectedItemProperty().getClass();
////            int index = tableview.getSelectionModel().selectedIndexProperty().get();
////
////            System.out.println(object);
////            System.out.println("The Row number of User is:   "+index);
//        //Empty the lblworkererror
//        lblWorkerError.setText("");
//        captureSingleFinger.setDisable(true);
//
//
//              /*
//              //Initialize Fingerprint Device if not initialized
//              if(fingerprintinit != 0){
//                 fingerprintinit = mfs100.Init();
//                 System.out.println("FingerPrintinit:::"+fingerprintinit);
//              }*/
//
//        //MIDAuth Init
//        if (fingerprintInit != 0) {
//            String model = "MFS100";
//            DeviceInfo info = new DeviceInfo();
//            int fingerprintinit = midFingerAuth.Init(DeviceModel.valueFor(model), info);
//            //System.out.println("FingerPri"+fingerprintinit);
//            LOGGER.log(Level.INFO, "fingerprintinit::" + fingerprintinit);
//        }
//
//
//        //System.out.println("FingerPrintinitttt:::"+fingerprintinit);
//        LOGGER.log(Level.INFO, "fingerprintinit:" + fingerprintInit);
//
//        if (tableview.getSelectionModel().getSelectedItem() == null) {
//            //lblworkererror.setText("Kindly Select the Labour");
//            messageStatus("Kindly Select the Labour");
//            LOGGER.log(Level.INFO, "Kindly Select the Labour");
//            return;
//
//        } else {
//                    /*
//                    int ret = mfs100.StartCapture(quality, timeout, true);
//                    System.out.println("ret val :"+ ret);
//                     if( ret != 0 ) {
//                        System.out.println("Error..!!");
//                        lblworkererror.setText("Reconnect the single Fingerprint Device");
//                    } */
//
//            //MIDAuth StartCapture
//            int retCapture = midFingerAuth.StartCapture(minQuality, timeout);
//            if (retCapture != 0) {
//                //System.out.println("Start Capture Error:"+midFingerAuth.GetErrorMessage(retCapture));
//                LOGGER.log(Level.INFO, "Start Capture Error:" + midFingerAuth.GetErrorMessage(retCapture));
//                messageStatus("Start Capture Error:" + midFingerAuth.GetErrorMessage(retCapture));
//                return;
//            }
//        }
//
//    }
//
//    @FXML
//    //public void fingerprintMatching(FingerData fingerData) {
//    public void fingerprintMatching(byte[] fingerData) throws IOException {
//
//        String fpqpath = prop.getProp().getProperty("fpquality");
//        if (fpqpath.isBlank() || fpqpath.isEmpty() || fpqpath == null) {
//            //System.out.println("The property 'fpquality' is empty, Please add it in properties");
//            LOGGER.log(Level.INFO, "The property 'fpquality' is empty, Please add it in properties");
//            return;
//        }
//        try (BufferedReader file = new BufferedReader(new FileReader(fpqpath))) {
//            String input = " ";
//            String fpq = file.lines().collect(Collectors.joining());
//            fpQuality = Integer.parseInt(fpq);
//            System.out.println("FP Quality is :: " + fpQuality);
//            LOGGER.log(Level.INFO, "FP Quality is :: " + fpQuality);
//            file.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            //System.out.println("Problem reading file./usr/share/enrollment/quality/fpquality");
//            LOGGER.log(Level.INFO, "Problem reading file./usr/share/enrollment/quality/fpquality");
//        }
//
//        String fpData = "";
//        int matchfound = 0;
//        LabourDetails row = tableview.getSelectionModel().getSelectedItem();
//        for (int j = 0; j < labourFpLists.size(); j++) {
//            String labourID = labourFpLists.get(j).getDynamicFileList().get(0).getLabourId();
//            String rowLabourID = row.getLabourID();
//            System.out.println("J:::::" + j);
//            System.out.println("Row LabourID:::::" + rowLabourID);
//            System.out.println("LabourID:::::" + labourFpLists.get(j).getDynamicFileList().get(0).getLabourId());
//            if (labourID.equals(rowLabourID)) {
//                System.out.println("The Employee matched");
//                List<LabourFP> labour = labourFpLists.get(j).getFPs();
//                //=================
//                for (int k = 0; k < labour.size(); k++) {
//                    System.out.println("FingerPrint position:::::" + labour.get(k).getFpPos());
//                    System.out.println("FingerPrint Data:::::" + labour.get(k).getFpData());
//                    //System.out.println("MatchISO\n"+fingerData.ISOTemplate()+"\n===========================\n"+Base64.getDecoder().decode(labour.get(k).fpData));
//                    //int ret = mfs100.MatchISO(fingerData.ISOTemplate(),Base64.getDecoder().decode(labour.get(k).fpData));
//
//
//                    //byte[] fmrTemplate = Base64.getDecoder().decode("Rk1SADAzMAAAAADrAAEAAAAA3P///////////wAAAAAAAAAAAMUAxQABIQGZYB9AkQDepmSAXgDdHmSAygD/s2RAlAE30ElA2AEO1WRAuACW/2RA7AEeY0RAlQFmZ0FA2QCJ+GSAXQFrAx5AqgAtikxAdwDToGRAcQEhsGRAdQCynGSA0QDXiGRAlAFERy9AygCiiGRAgAFifjGAngB1k2RAkgFyeBRAVQB1GGRAXgECpl9AfAEoxGSASADnpGSAcQE7vGRANADkH2RA7wDTbWRAXAFZF0WAPgCPmWSAtAFvZBFASABxmlsAAA==");
//                    //System.out.println("fmrTemplate:::"+fmrTemplate);
//                    int[] matchScore = new int[1];
//
//                    //int ret = midFingerAuth.MatchTemplate(fingerData, fmrTemplate, matchScore, TemplateFormat.FMR_V2011);
//                    int ret = FingerprintTemplateMatching(fingerData, Base64.getDecoder().decode(labour.get(k).getFpData()), matchScore, TemplateFormat.FMR_V2011);
//                    // int ret = midFingerAuth.MatchTemplate(fingerData, Base64.getDecoder().decode(labour.get(k).fpData), matchScore, TemplateFormat.FMR_V2011);
//
//                    if (ret < 0) {
//                        //System.out.println(midFingerAuth.GetErrorMessage(ret));
//                        LOGGER.log(Level.INFO, "midFingerAuth.GetErrorMessage" + midFingerAuth.GetErrorMessage(ret));
//                    } else {
//                        int minThresold = 96;
//                        if (matchScore[0] >= minThresold) {
//                            //System.out.println("Finger matched with score: " + matchScore[0]);
//                            LOGGER.log(Level.INFO, "Finger matched with score: " + matchScore[0]);
//                        } else {
//                            //System.out.println("Finger not matched with score: " + matchScore[0]);
//                            LOGGER.log(Level.INFO, "Finger not matched with score: " + matchScore[0]);
//                        }
//                    }
//
//                    //int ret = -1309;
//                    //System.out.println("return value:: : "+ret);
//                    LOGGER.log(Level.INFO, "return value:: : " + ret);
//
//                    if (matchScore[0] > fpQuality) {   //Match Single Fingerprint
//                        //System.out.println("Finger print quality check : "+fpQuality);
//                        LOGGER.log(Level.INFO, "Finger print quality check : " + fpQuality);
//                        MatchIsoTemplate(matchScore[0]);
//                        matchfound = 1;
//                        break;
//                    }
//                }
//
//
//            } else {
//                continue;
//            }
//
//            System.out.println("Finger fp list :" + labourFpLists.get(j).toString());
//            System.out.println("size :" + labourFpLists.get(j).getFPs().size());
//
//            if (matchfound == 1) {
//                //System.out.println("Match found exiting");
//                LOGGER.log(Level.INFO, "Match found exiting");
//                break;
//            }
//
//
//        }
//
//        if (matchfound == 0) {
//            //System.out.println("Finger Print NOT Matched");
//            LOGGER.log(Level.INFO, "Finger Print NOT Matched");
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    lblWorkerError.setText("Finger print not Matched Try Again");
//                }
//            });
//        }
//
//    }
//
//    public int FingerprintTemplateMatching(byte[] fingerData, byte[] fingerprintData, int[] matchScore, TemplateFormat format) {
//        int ret = midFingerAuth.MatchTemplate(fingerData, fingerprintData, matchScore, TemplateFormat.FMR_V2011);
//        return ret;
//    }
//
//    @FXML
//    //public void MatchIsoTemplate(byte[] ISOTemplate, byte[] decode,int ret) {
//    public void MatchIsoTemplate(int ret) throws IOException {
//        //     public void MatchIsoTemplate(byte[] ISOTemplate, byte[] decode) {
//        String fpqpath = prop.getProp().getProperty("fpquality");
//        if (fpqpath.isBlank() || fpqpath.isEmpty() || fpqpath == null) {
//            //System.out.println("The property 'fpquality' is empty, Please add it in properties");
//            LOGGER.log(Level.INFO, "The property 'fpquality' is empty, Please add it in properties");
//            return;
//        }
//        try (BufferedReader file = new BufferedReader(new FileReader(fpqpath))) {
//            String input = " ";
//            String fpq = file.lines().collect(Collectors.joining());
//            fpQuality = Integer.parseInt(fpq);
//            System.out.println("FP Quality is : " + fpQuality);
//            file.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            //System.out.println("Problem reading file./usr/share/enrollment/quality/fpquality");
//            LOGGER.log(Level.INFO, "Problem reading file./usr/share/enrollment/quality/fpquality");
//        }
//
//
//        LabourDetails row = tableview.getSelectionModel().getSelectedItem();
//        System.out.println("Selected Row is : " + row.toString());
//        if (row == null) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    //lblworkererror.setText("Finger Print Matching...");
//                    lblWorkerError.setText("Kindly Select the Labour");
//                    LOGGER.log(Level.INFO, "Kindly Select the Labour");
//                }
//            });
//
//        } else {
//            //Base64.getDecoder().decode(fpData);
//            //int ret = mfs100.MatchISO(ISOTemplate,decode);
//            System.out.println("return value : " + ret);
//            //Label lblworkererror = new Label();
//
//            System.out.println("FPQuality@::" + fpQuality);
//            //uncomment later
//            if (ret > fpQuality) {
//                //if(ret == 0){
//                //if(ret < 0){
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        //lblworkererror.setText("Finger Print Matching...");
//                        lblWorkerError.setText("Finger print Matched");
//                        LOGGER.log(Level.INFO, "Finger print Matched");
//                    }
//                });
//
//
//                //Token Dispence
//                //Uncomment Later
//
//                //Commented for Testing without TokenDispense
//
//                TokenDispense tokenDispence = new TokenDispense();
//                String tokenDispenceOutput = tokenDispence.tokenDispense();
//                if (tokenDispenceOutput.contains("failure")) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            lblWorkerError.setText("Kindly Connect the Token Dispencer And Try Again");
//                            LOGGER.log(Level.INFO, "Kindly Connect the Token Dispencer And Try Again");
//                        }
//                    });
//                    //mfs100.Uninit();
//                } else {
//
//                    System.out.println("TokenDispenceOutput::::" + tokenDispenceOutput);
//
//                    System.out.println("clicked labour Details::::" + row);
//                    System.out.println("Finger print matched1::::");
//
////                LabourDetails row = new LabourDetails();
////                row.setDateOfBirth(labourLists.get(j).getDateOfBirth());
////                row.setLabourID(labourLists.get(j).getLabourId());
////                row.setLabourName(labourLists.get(j).getLabourName());
//                    //row.setStrStatus("verified");
//                    //tableview.getSelectionModel().select(row);
//
//
//                    Labour labourDetails = labourMap.get(row.getLabourID());
//                    ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//                    holder.setLabourDetails(labourDetails);
//                    holder.setContractorDynamicDetails(contractorDynamicFile);
//                    System.out.println("HolDer:::" + holder.getLabourDetails().toString());
//                    //tableview.getItems().remove(row);
//                    // row.setStrStatus("verified");
//                    // tableview.refresh();
//
//                    /*
//                    ASNtoHexFormat getDynamicFileDetail = new ASNtoHexFormat();
//                    byte[] dynamicEncodedbytes =  getDynamicFileDetail.getEncodedDyanamicFile();
//                    String encodedbase64Dynamic = Base64.getEncoder().encodeToString(dynamicEncodedbytes);
//                    System.out.println("Encoded Card Write Base64 Dynamic File :::"+encodedbase64Dynamic);
//
//                     byte[] photoEncodedbytes =  getDynamicFileDetail.getEncodedLabourPhoto();
//                    String encodedbase64LabourPhoto = Base64.getEncoder().encodeToString(photoEncodedbytes);
//                    System.out.println("Encoded Card Write Base64 Dynamic File :::"+encodedbase64LabourPhoto);
//
//                    byte[] defaultAccessValidityEncodedbytes =  getDynamicFileDetail.getEncodedDefaultAccessValidity();
//                    String encodedbase64DAC = Base64.getEncoder().encodeToString(defaultAccessValidityEncodedbytes);
//                    System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);
//
//                    byte[] defaultSpecialAccessEncodedbytes =  getDynamicFileDetail.getEncodedSpecialAccessPermission();
//                    String encodedbase64specialaccess = Base64.getEncoder().encodeToString(defaultSpecialAccessEncodedbytes);
//                    System.out.println("Encoded Card Write Base64 Special Access Permission :::"+encodedbase64specialaccess);*/
//
//                    //Write to the Token
//                    CardWrite cardwrite = new CardWrite();
//                    String returncardwrite;
//
//                    try {
//                        returncardwrite = cardwrite.cardWriteDeatils();
//                        //System.out.println("Card Write Details:::"+returncardwrite);
//                        LOGGER.log(Level.INFO, "Card Write Details:::" + returncardwrite);
//
//                        if (returncardwrite.contains("Failure")) {
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    //lblworkererror.setText("Kindly put the token in Token Dispencer And Try Again");
//                                    //lblworkererror.setText("Card Write Failure, Kindly put the Token back to Dispencer");
//                                    lblWorkerError.setText(returncardwrite);
//                                }
//                            });
//                            return;
//
//                        } else {
//
//
//                            //Token update Details to Mafis API
//                            UpdateToken token = new UpdateToken();
//                            Details detail = Details.getdetails();
//                            ContactDetail contractDetails = detail.getContractDetail();
//                            token.setCardCSN(contractDetails.getSerialNo());    // Need to be changed later
//                            token.setContractorCSN(contractDetails.getSerialNo());
//                            token.setContractorID(contractDetails.getContractorId());
//                            token.setContractID(contractDetails.getContactId());
//                            token.setEnrollmentStationUnitID(apiServerCheck.getUnitID());
//                            token.setUniqueNo(row.getLabourID());
//                            token.setEnrollmentStationID(apiServerCheck.getStationID());
//                            token.setTokenID(contractDetails.getSerialNo());   // Need to be changed later
//                            //token.setVerifyFPSerialNo(mfs100.GetDeviceInfo().SerialNo()); //commented for MFS100
//                            token.setVerifyFPSerialNo(deviceInfo.SerialNo);
//                            //set token issuance date and time
//                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                            LocalDateTime now = LocalDateTime.now();
//                            System.out.println(dtf.format(now));
//                            token.setTokenIssuanceDate(dtf.format(now));
//                            try {
//                                String errorresponse = updateToken(token);
//                                if (errorresponse.contentEquals("0")) {
//                                    //To change verified and color of the Row
//                                    row.setStrStatus("verified");
//                                    tableview.getItems().remove(row);
//                                    tableview.setRowFactory(tv -> new TableRow<LabourDetails>() {
//                                        @Override
//                                        public void updateItem(LabourDetails item, boolean empty) {
//                                            super.updateItem(item, empty);
//                                            if (item == null) {
//                                                setStyle("");
//                                            } else if (item.getStrStatus().equals("verified")) {
//                                                //setStyle("-fx-background-color: green;");
//                                                setStyle("-fx-background-color: #66ccff;");
//                                            } else {
//                                                setStyle("");
//                                            }
//                                        }
//                                    });
//
//                                    tableview.refresh();
//
//                                    Platform.runLater(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            lblWorkerError.setText("Kindly collect the Token");
//                                            LOGGER.log(Level.INFO, "Kindly collect the Token");
//                                        }
//                                    });
//
//                                } else {
//
//                                    Platform.runLater(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            lblWorkerError.setText("Token Details Not Updated to Server, Try Again");
//                                            LOGGER.log(Level.INFO, "Token Details Not Updated to Server, Try Again");
//                                        }
//                                    });
//
//                                }
//                                captureSingleFinger.setDisable(false);
//
//                                /*ImageView imView = new ImageView();
//                                System.out.println("IMAGEEEEEE"+imView.getImage().toString());
//                                m_FingerPrintImage.setImage(imView.getImage());*/
//
//
//                            } catch (IOException ex) {
//                                Logger.getLogger(FetchLabourDetailsController.class.getName()).log(Level.SEVERE, null, ex);
//                                LOGGER.log(Level.INFO, "IOException" + ex);
//                            }
//                            if (tableview.getItems().size() == 0) {
//                                try {
//                                    App.setRoot("list_contract");
//                                } catch (IOException ex) {
//                                    Logger.getLogger(FetchLabourDetailsController.class.getName()).log(Level.SEVERE, null, ex);
//                                    LOGGER.log(Level.INFO, "IOException" + ex);
//                                }
//                            }
//                        }
//                    } catch (Exception ex) {
//                        Logger.getLogger(FetchLabourDetailsController.class.getName()).log(Level.SEVERE, null, ex);
//                        LOGGER.log(Level.INFO, "IOException" + ex);
//                    }
//                }//Commented for Testing without TokenDispense
//            } else {
//                System.out.println("Finger Print NOT Matched");
//                LOGGER.log(Level.INFO, "Finger Print NOT Matched");
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        lblWorkerError.setText("Finger print not Matched Try Again");
//                        LOGGER.log(Level.INFO, "Finger print not Matched Try Again");
//                    }
//                });
//
//
//            }
//        }
//
//    }
//
//
//    private String updateToken(UpdateToken token) throws IOException {
//
//        ObjectMapper mapper = new ObjectMapper();
//        String postJson;
//        String desc = "";
//        String error = "";
//        try {
//            postJson = mapper.writeValueAsString(token);
//            postJson = postJson.replace("\n", "");
//            int jsonHash = postJson.hashCode();
//            // System.out.println("post json final :"+ postJson);
////                 File json = new File("/home/cloud/postjson");
////
////                 FileOutputStream output = new FileOutputStream(json);
////                 output.write(postJson.getBytes());
////                 output.close();
//            String connurl = apiServerCheck.getTokenUpdateURL();
//            String uniqueNo = token.getUniqueNo();
//            String enrollStationId = token.getEnrollmentStationID();
//            String cardcsnNo = token.getCardCSN();
//            String contractorId = token.getContractorID();
//            String contractorCSN = token.getContractorCSN();
//            String tokenIssuanceDate = token.getTokenIssuanceDate();
//            String contractID = token.getContractID();
//            String enrollmentStationUnitId = token.getEnrollmentStationUnitID();
//            String tokenId = token.getTokenID();
//            String verifyFPSerialNo = token.getVerifyFPSerialNo();
//            String jsonInputString = "{\"UniqueNo\": \"" + uniqueNo + "\"" + ",\"EnrollmentStationID\": \"" + enrollStationId + "\"" + ",\"CardCSN\": \"" + cardcsnNo + "\"" + ",\"ContractorID\": \"" + contractorId + "\"" + ",\"ContractorCSN\": \"" + contractorCSN + "\"" + ",\"TokenIssuanceDate\": \"" + tokenIssuanceDate + "\"" + ",\"ContractID\": \"" + contractID + "\"" + ",\"EnrollmentStationUnitID\": \"" + enrollmentStationUnitId + "\"" + ",\"TokenID\": \"" + tokenId + "\"" + ",\"VerifyFPSerialNo\": \"" + verifyFPSerialNo + "\"}";
//            System.out.println("TOKEN JSON INPUT STRING::::" + jsonInputString);
//
////        String testJson = "{\n" +
////"  \"UniqueNo\": \"LAB0001\",\n" +
////"  \"EnrollmentStationID\": \"EST123456789\",\n" +
////"  \"CardCSN\": \"1234567890\",\n" +
////"  \"ContractorID\": \"CONTRACT001\",\n" +
////"  \"ContractorCSN\": \"1234567890\",\n" +
////"  \"TokenIssuanceDate\": \"2021-04-26 10:10:31\",\n" +
////"  \"ContractID\": \"CONTRA0001\",\n" +
////"  \"EnrollmentStationUnitID\": \"U1\",\n" +
////"  \"TokenID\": \"1234567890\"\n" +
////"  \"VerifyFPSerialNo\": \"11223344\"\n" +
////"}";
//            // String connectionStatus = apiServerCheck.getStatusTokenUpdate(connurl, testJson);
//            String jsonTokenUpdateResponse = "";
//            String sessionkey_token = "";
//
////        String connectionStatus = apiServerCheck.getStatusTokenUpdate(connurl, jsonInputString);
////        System.out.println("connection status :"+connectionStatus);
//
////        if(!connectionStatus.contentEquals("connected")) {
////
////           // App.setRoot("capturecomplete");
////
////             lblworkererror.setText("MAFIS API Connection Error, Try Again");
////        }
////        else {
//            try {
//                jsonTokenUpdateResponse = apiServerCheck.getTokenUpdate(connurl, jsonInputString);
//                System.out.println("outputTokenJSON :" + jsonTokenUpdateResponse);
//
//
//                //Getting Session Key from the TokenUpdate API(APIServerCheck)
////                    System.out.println("SessionKey----"+apiServerCheck.sessionkey);
////                    sessionkey_token = apiServerCheck.sessionkey;
////                    System.out.println("SESSION KEY"+sessionkey_token);
////                    System.out.println("SESSION KEY_public"+sessionKey_public);
//
//                //Encrypt the session key
////                    CryptoAES256 aes256 = new CryptoAES256(sessionkey_token);
//
//                //decrypt the JSON output using aes256
////                    String decJson = aes256.decryptString(jsonTokenUpdateResponse);
////                    System.out.println("Decrypted JSON"+decJson);
//
//                //decrypted JSON to obj
//                // Object obj = JsonReader.jsonToJava(decJson);
//                //Object obj = JsonReader.jsonToJava(jsonTokenUpdateResponse);
//                //System.out.println("obj str : " +obj.toString());
//
//                //JSON String to a Java object using the ObjectMapper class
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                //UpdateTokenResponse updateTokenResponse = objectMapper.readValue(decJson, UpdateTokenResponse.class);
//                UpdateTokenResponse updateTokenResponse = objectMapper.readValue(jsonTokenUpdateResponse, UpdateTokenResponse.class);
//                desc = updateTokenResponse.getDesc();
//                error = updateTokenResponse.getErrorCode();
//                System.out.println("ERORR:::" + error);
//                System.out.println("DESC:::" + desc);
//                if (error.contentEquals("0")) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            lblWorkerError.setText("Kindly collect the Token");
//                            LOGGER.log(Level.INFO, "Kindly collect the Token");
//                        }
//                    });
//                    LOGGER.log(Level.INFO, "Token Details Updated to Server");
//                    //System.out.println("Token Details Updated to Server");
//                } else {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            lblWorkerError.setText("Token Details Not Updated to Server, Try Again");
//                            LOGGER.log(Level.INFO, "Token Details Not Updated to Server, Try Again");
//                        }
//                    });
//
//                    //System.out.println("Token Not Updated to Server, Try Again");
//                    LOGGER.log(Level.INFO, "Token Details Not Updated to Server, Try Again");
//                }
//
//            } catch (Exception e) {
//                System.out.println(e);
//                LOGGER.log(Level.INFO, "Exception:" + e);
//            }
//
//         /*
//        try{
//
//          //  json = apiServerCheck.getContractListAPI(connurl, contractDetails.getContractor_id(), contractDetails.getSerial_no());
//            jsonTokenUpdate = apiServerCheck.getTokenUpdate(connurl, jsonInputString);
//             System.out.println("outputTokenJSON :"+jsonTokenUpdate);
//
//            //Getting Session Key from the TokenUpdate API(APIServerCheck)
//            System.out.println("SessionKey----"+apiServerCheck.sessionkey);
//            sessionkey_token = apiServerCheck.sessionkey;
//            System.out.println("SESSION KEY"+sessionkey_token);
//            System.out.println("SESSION KEY_public"+sessionKey_public);
//
//            //Encrypt the session key
//            CryptoAES256 aes256 = new CryptoAES256(sessionkey_token);
//
//            //decrypt the JSON output using aes256
//            String decJson = aes256.decryptString(jsonTokenUpdate);
//
//            //decrypted JSON to obj
//            Object obj = JsonReader.jsonToJava(decJson);
//            System.out.println("obj str : " +obj.toString());
////        URL url=new URL(connurl);
////        HttpURLConnection con = (HttpURLConnection)url.openConnection();
////        con.setRequestMethod("POST");
////        con.setRequestProperty("Content-Type", "application/json; utf-8");
////        con.setRequestProperty("Accept", "application/json");
////        con.setDoOutput(true);
//
//
//            CryptoAES256 aes256 = new CryptoAES256();
//            skey = aes256.getAESKey();
//            String getuuid = aes256.generateRandomUUID();
//            getuuid = getuuid.replace("-","");
//            System.out.println("guid : "+getuuid.length());
//            Key strKey = aes256.generateKey32(getuuid);
//            String encstr = aes256.encryptString("test", strKey);
//            String dec = aes256.decryptStringSK(encstr, strKey);
//            System.out.println("dec string :"+ dec);
//           // postJson = mapper.writeValueAsString(saveEnrollment);
//            postJson = postJson.replace("\n", "");
//            String encryptedJson = aes256.encryptString(postJson, strKey);
//            URL url=new URL(connurl);
//            HttpURLConnection con = (HttpURLConnection)url.openConnection();
//            con.setRequestMethod("POST");
//            con.setRequestProperty("Content-Type", "application/json; utf-8");
//            con.setRequestProperty("SessionKey", getuuid);
//            con.setRequestProperty("Accept", "application/json");
//            con.setDoOutput(true);
//
//        try(OutputStream os = con.getOutputStream()) {
//            byte[] input = encryptedJson.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        try(BufferedReader br = new BufferedReader(
//            new InputStreamReader(con.getInputStream(), "utf-8"))) {
//            StringBuilder response = new StringBuilder();
//            String responseLine = null;
//            while ((responseLine = br.readLine()) != null) {
//                response.append(responseLine.trim());
//            }
//            Map<String, List<String>> map = con.getHeaderFields();
//            Boolean isSessionKeyPresent = false;
//            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//                if(entry.getKey() == null)
//                    continue;
//                System.out.println("Key : " + entry.getKey() +
//                         " ,Value : " + entry.getValue());
//                if(entry.getKey().contains("SessionKey")){
//                    isSessionKeyPresent = true;
//                }
//            }
//             String secKey = "";
//            if(isSessionKeyPresent) {
//                secKey = con.getHeaderField("SessionKey");
//                System.out.println("Session key :"+secKey);
//            }
//                CryptoAES256 aesdec = new CryptoAES256(secKey);
//                //byte[] decodedKey = Base64.getDecoder().decode(secKey);
//            // rebuild key using SecretKeySpec
//            //SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//            String decResponse = aesdec.decryptString(response.toString());
//            System.out.println("response received :"+ response.toString());
//                System.out.println("dec response : "+ decResponse);
//            System.out.println("Token update response : "+ response);
//            //Getting Session Key from the TokenUpdate API(APIServerCheck)
//
//           // App.setRoot("capturecomplete");
//
//        }
//
//       }
//        catch(Exception e)
//            {
//             System.out.println(e);
//            }  */
//            // }
//        } catch (JsonProcessingException ex) {
//            //System.out.println("Json exception : "+ ex.getMessage());
//            LOGGER.log(Level.INFO, "Json Exception" + ex);
//        }
//        //System.out.println("Error : "+ error);
//        LOGGER.log(Level.INFO, "Error" + error);
//        return error;
//    }
//
//
//    private Node createPage(int pageIndex) {
//
//        int fromIndex = pageIndex * 8;
//        int toIndex = Math.min(fromIndex + 8, labourList.size());
//        //System.out.println(" data size :"+ labourList.size() + " " + fromIndex + " " + toIndex);
//        LOGGER.log(Level.INFO, " data size :" + labourList.size() + " " + fromIndex + " " + toIndex);
//
//
//        tableview.setFixedCellSize(30.0);
//        tableview.setItems(FXCollections.observableArrayList(labourList.subList(fromIndex, toIndex)));
//        //contractTableView.refresh();
//        return tableview;
//    }
//
//    private boolean searchFindsOrder(LabourDetails labourDetails, String searchText) {
//        return (labourDetails.getLabourID().toLowerCase().contains(searchText.toLowerCase())) ||
//                (labourDetails.getLabourName().toLowerCase().contains(searchText.toLowerCase())) ||
//                (labourDetails.getDateOfBirth().toLowerCase().contains(searchText.toLowerCase()));
//    }
//
//    private ObservableList<LabourDetails> filterList(List<LabourDetails> list, String searchText) {
//        List<LabourDetails> filteredList = new ArrayList<>();
//        for (LabourDetails labourData : list) {
//            if (searchFindsOrder(labourData, searchText)) filteredList.add(labourData);
//        }
//        return FXCollections.observableList(filteredList);
//    }
//
////    public List<LabourDetails> createdata(){
////
////        List<LabourDetails> labourList1 = new ArrayList<LabourDetails>();
////        LabourDetails labourDetails = new LabourDetails();
////                    labourDetails.setDateOfBirth("22-10-1972");
////                    labourDetails.setLabourID("AA112200");
////                    labourDetails.setLabourName("Aakash");
////                    labourDetails.setStrStatus("verified");
////                    labourList1.add(labourDetails);
////        LabourDetails labourDetails1 = new LabourDetails();
////                    labourDetails1.setDateOfBirth("18-12-1975");
////                    labourDetails1.setLabourID("AA112200");
////                    labourDetails1.setLabourName("Avinash");
////                    labourDetails1.setStrStatus("verified");
////                    labourList1.add(labourDetails1);
////        LabourDetails labourDetails2 = new LabourDetails();
////                    labourDetails2.setDateOfBirth("17-11-1977");
////                    labourDetails2.setLabourID("AA112200");
////                    labourDetails2.setLabourName("Aarav");
////                    labourDetails2.setStrStatus("verified");
////                    labourList1.add(labourDetails2);
////
////        LabourDetails labourDetails3 = new LabourDetails();
////                    labourDetails3.setDateOfBirth("25-08-1972");
////                    labourDetails3.setLabourID("AA112200");
////                    labourDetails3.setLabourName("Abhinaya");
////                    labourDetails3.setStrStatus("verified");
////                    labourList1.add(labourDetails3);
////        LabourDetails labourDetails4 = new LabourDetails();
////                    labourDetails4.setDateOfBirth("12-10-1974");
////                    labourDetails4.setLabourID("AA112200");
////                    labourDetails4.setLabourName("Priya");
////                    labourDetails4.setStrStatus("verified");
////                    labourList1.add(labourDetails4);
////        LabourDetails labourDetails5 = new LabourDetails();
////                    labourDetails5.setDateOfBirth("02-10-1979");
////                    labourDetails5.setLabourID("AA112200");
////                    labourDetails5.setLabourName("Prasanna");
////                    labourDetails5.setStrStatus("verified");
////                    labourList1.add(labourDetails5);
////        LabourDetails labourDetails6 = new LabourDetails();
////                    labourDetails6.setDateOfBirth("02-12-1974");
////                    labourDetails6.setLabourID("AA112200");
////                    labourDetails6.setLabourName("Praneesh");
////                    labourDetails6.setStrStatus("verified");
////                    labourList1.add(labourDetails6);
////        LabourDetails labourDetails7 = new LabourDetails();
////                    labourDetails7.setDateOfBirth("16-10-1976");
////                    labourDetails7.setLabourID("AA112200");
////                    labourDetails7.setLabourName("Pughal");
////                    labourDetails7.setStrStatus("verified");
////                    labourList1.add(labourDetails7);
////        LabourDetails labourDetails8 = new LabourDetails();
////                    labourDetails8.setDateOfBirth("21-05-1976");
////                    labourDetails8.setLabourID("AA112200");
////                    labourDetails8.setLabourName("Neha");
////                    labourDetails8.setStrStatus("verified");
////                    labourList1.add(labourDetails8);
////        LabourDetails labourDetails9 = new LabourDetails();
////                    labourDetails9.setDateOfBirth("22-10-1972");
////                    labourDetails9.setLabourID("AA112200");
////                    labourDetails9.setLabourName("Nirbhaya");
////                    labourDetails9.setStrStatus("verified");
////                    labourList1.add(labourDetails9);
////        LabourDetails labourDetails10 = new LabourDetails();
////                    labourDetails10.setDateOfBirth("22-10-1972");
////                    labourDetails10.setLabourID("AA112200");
////                    labourDetails10.setLabourName("Niranjan");
////                    labourDetails10.setStrStatus("verified");
////                    labourList1.add(labourDetails10);
////        LabourDetails labourDetails11 = new LabourDetails();
////                    labourDetails11.setDateOfBirth("22-10-1972");
////                    labourDetails11.setLabourID("AA112200");
////                    labourDetails11.setLabourName("Kanha");
////                    labourDetails11.setStrStatus("verified");
////                    labourList1.add(labourDetails11);
////        LabourDetails labourDetails12 = new LabourDetails();
////                    labourDetails12.setDateOfBirth("22-10-1972");
////                    labourDetails12.setLabourID("AA112200");
////                    labourDetails12.setLabourName("Shreya");
////                    labourDetails12.setStrStatus("verified");
////                    labourList1.add(labourDetails12);
////        LabourDetails labourDetails13 = new LabourDetails();
////                    labourDetails13.setDateOfBirth("22-10-1972");
////                    labourDetails13.setLabourID("AA112200");
////                    labourDetails13.setLabourName("Murali");
////                    labourDetails13.setStrStatus("verified");
////                    labourList1.add(labourDetails13);
////        LabourDetails labourDetails14 = new LabourDetails();
////                    labourDetails14.setDateOfBirth("22-10-1972");
////                    labourDetails14.setLabourID("AA112200");
////                    labourDetails14.setLabourName("Aakash");
////                    labourDetails14.setStrStatus("verified");
////                    labourList1.add(labourDetails14);
////
////                    return labourList1;
////    }
//
//
////    private void WriteBytesToFile(String FileName, byte[] Bytes){
////        try {
////            String FilePath = new File("/usr/share/enrollment/fingerprint/fingerprint.ansi").getAbsolutePath();
////            System.out.println("File Path:::  "+FilePath);
////           // FilePath = FilePath + File.separator + "FingerData";
////            File file = new File(FilePath);
////            if(!file.exists()){
////                file.mkdir();
////            }
////
////            FilePath = FilePath + File.separator + FileName;
////            FileOutputStream fos = new FileOutputStream(FilePath);
////            fos.write(Bytes);
////            fos.close();
////            System.out.println("Print FileName++++++"+FilePath);
////
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
//
///*     String testJson = "{\n"+
// " \"ErrorCode\": \"0\",\n" +
// " \"Desc\": \"Success\",\n"  +
// " \"LabourList\": [\n" +
// "   {\n" +
//  "    \"DynamicFile\": {\n "+
// "      \"userCategoryID\": \"USER1\",\n "+
//"       \"labourName\": \"LAbour1\",\n"  +
//"      \"dateOfBirth\": \"011121\",\n " +
//"      \"genderId\": \"1\",\n "   +
//"      \"bloodGroupId\": \"B\",\n " +
//"        \"nationalityId\": \"na1\",\n"+
//"        \"issuanceUnit\": \"unit1\",\n"+
//"        \"labourID\": \"labour1\",\n "+
//"       \"contractorID\": \"contract1\"\n "+
//"     },\n "+
//"      \"DefaultValidityFile\": {\n "+
//"       \"validFrom\": \"011221\",\n "+
//"       \"validTo\": \"121221\"\n "+
//"      },\n "+
//"     \"AccessFile\": {\n"+
//"        \"unitCode\": \"Unitone\",\n "+
//"        \"zoneId\": \"Zoneone\",\n  "+
//"       \"workingHourCode\": \"3\",\n "+
//"        \"fromDate\": \"231221\",\n "+
//"       \"toDate\": \"241221\"\n "+
//"     },\n" +
//"      \"SignFile1\": \"sign1\",\n "+
//"     \"SignFile3\": \"sign2\",\n "+
//"     \"FPs\": [\n "+
//"       {\n"+
//"          \"FPPos\": \"fp1\",\n "+
//"         \"FPData\": \"fpdata1\"\n   "+
//"     }\n"+
//"      ],\n "+
//"     \"IRIS1\": \"iris1\",\n "+
//"     \"IRIS2\": \"iris1\",\n "+
//"     \"Photo\": \"photostring1\"\n   "+
//" },\n "+
//" {\n" +
//"    \"DynamicFile\": {\n "+
//"      \"userCategoryID\": \"USER2\",\n "+
//"       \"labourName\": \"LAbour2\",\n"  +
//"      \"dateOfBirth\": \"021221\",\n " +
//"      \"genderId\": \"2\",\n "   +
//"      \"bloodGroupId\": \"B\",\n " +
//"        \"nationalityId\": \"na2\",\n"+
//"        \"issuanceUnit\": \"unit2\",\n"+
//"        \"labourID\": \"labour2\",\n "+
//"       \"contractorID\": \"contract2\"\n "+
//"     },\n "+
//"      \"DefaultValidityFile\": {\n "+
//"       \"validFrom\": \"011221\",\n "+
//"       \"validTo\": \"121221\"\n "+
//"      },\n "+
//"     \"AccessFile\": {\n"+
//"        \"unitCode\": \"Unitone\",\n "+
//"        \"zoneId\": \"Zoneone\",\n  "+
//"       \"workingHourCode\": \"3\",\n "+
//"        \"fromDate\": \"231221\",\n "+
//"       \"toDate\": \"241221\"\n "+
//"     },\n" +
//"      \"SignFile1\": \"sign1\",\n "+
//"     \"SignFile3\": \"sign2\",\n "+
//"     \"FPs\": [\n "+
//"       {\n"+
//"          \"FPPos\": \"fp2\",\n "+
//"         \"FPData\": \"fpdata2\"\n   "+
//"     }\n"+
//"      ],\n "+
//"     \"IRIS1\": \"iris2\",\n "+
//"     \"IRIS2\": \"iris2\",\n "+
//"     \"Photo\": \"photostring2\"\n   "+
//" }\n "+
//"]\n" +
//" }"; */
//
//    @Override
//    public void OnDeviceDetection(String arg0, DeviceDetection arg1) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    /*
//    @Override
//    public void OnPreview(int arg0, int arg1, byte[] arg2) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void OnComplete(int arg0, int arg1, int arg2) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//  */
//
//    @Override
//    public void OnPreview(int errorCode, int quality, final byte[] image) {
//        if (errorCode != 0) {
//            //System.out.println("errorCode: " + errorCode);
//            LOGGER.log(Level.INFO, "errorCode: " + errorCode);
//            //System.out.println("errorCode: " + midFingerAuth.GetErrorMessage(errorCode));
//            LOGGER.log(Level.INFO, "errorCode: " + midFingerAuth.GetErrorMessage(errorCode));
//            return;
//        }
//        try {
//            // jlbPreviewQuality.setText("Quality: " + quality);
//
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        InputStream in = new ByteArrayInputStream(image);
//                        BufferedImage bufferedImage = ImageIO.read(in);
//                        WritableImage wr = null;
//                        if (bufferedImage != null) {
//                            System.out.println("BUFDREA not null");
//                            wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
//                            PixelWriter pw = wr.getPixelWriter();
//                            for (int x = 0; x < bufferedImage.getWidth(); x++) {
//                                for (int y = 0; y < bufferedImage.getHeight(); y++) {
//                                    pw.setArgb(x, y, bufferedImage.getRGB(x, y));
//                                }
//                            }
//                        }
//
//                        ImageView imView = new ImageView(wr);
//                        System.out.println("IMAGEEEEEE" + imView.getImage().toString());
//                        mFingerPrintImage.setImage(wr);
//
//                    } catch (Exception e) {
//                    }
//                }
//            }).start();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void OnComplete(int errorCode, int Quality, int NFIQ) {
//        if (errorCode != 0) {
//            //System.out.println("Capture"+midFingerAuth.GetErrorMessage(errorCode));
//            LOGGER.log(Level.INFO, "Capture:" + midFingerAuth.GetErrorMessage(errorCode));
//            return;
//        }
//        try {
//
//            //System.out.println("Capture Success");
//            //System.out.println("Quality: " + Quality + ", NFIQ: " + NFIQ);
//            LOGGER.log(Level.INFO, "Capture Success");
//            LOGGER.log(Level.INFO, "Quality: " + Quality + ", NFIQ: " + NFIQ);
//            //getBitmapOnComplete();
//            //  templateProcess();
//            // String fingertemplate2011="Rk1SADAzMAAAAADrAAEAAAAA3P///////////wAAAAAAAAAAAMUAxQABIQGZYB9AkQDepmSAXgDdHmSAygD/s2RAlAE30ElA2AEO1WRAuACW/2RA7AEeY0RAlQFmZ0FA2QCJ+GSAXQFrAx5AqgAtikxAdwDToGRAcQEhsGRAdQCynGSA0QDXiGRAlAFERy9AygCiiGRAgAFifjGAngB1k2RAkgFyeBRAVQB1GGRAXgECpl9AfAEoxGSASADnpGSAcQE7vGRANADkH2RA7wDTbWRAXAFZF0WAPgCPmWSAtAFvZBFASABxmlsAAA==";
//            int[] dataLen = new int[]{2500};
//            byte[] data = new byte[dataLen[0]];
//
//            int ret = midFingerAuth.GetTemplate(data, dataLen, TemplateFormat.FMR_V2011);
//            lastCaptureTemplat = new byte[dataLen[0]];
//            System.arraycopy(data, 0, lastCaptureTemplat, 0, dataLen[0]);
//
//
//            fingerprintMatching(lastCaptureTemplat);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            LOGGER.log(Level.INFO, "Exception:" + ex);
//        }
//    }
//
//
//}
//
//
