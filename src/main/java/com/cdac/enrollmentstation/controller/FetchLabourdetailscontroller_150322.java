//package com.cdac.enrollmentstation.controller;
//
//import MFS100.DeviceInfo;
//import MFS100.FingerData;
//import MFS100.MFS100;
//import MFS100.MFS100Event;
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.api.APIServerCheck;
//import com.cdac.enrollmentstation.dto.UpdateTokenResponse;
//import com.cdac.enrollmentstation.model.*;
//import com.cdac.enrollmentstation.security.CryptoAES256;
//import com.cdac.enrollmentstation.security.PKIUtil;
//import com.cdac.enrollmentstation.service.CardWrite;
//import com.cdac.enrollmentstation.service.TokenDispense;
//import com.cedarsoftware.util.io.JsonReader;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
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
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.net.URL;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//
//public class FetchLabourdetailscontroller_150322 implements Initializable, MFS100Event {
//
//    @FXML
//    private Label lblContractName;
//
//    @FXML
//    private Label lblcontractorName;
//
//    @FXML
//    public Label lblworkererror;
//
//    @FXML
//    private javafx.scene.image.ImageView m_FingerPrintImage;
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
//    public Button capturesinglefinger;
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
//    int fingerprintinit;
//    int fpQuality = 7000;
//
//
//    @FXML
//    private Button capturefinger;
//    MFS100 mfs100 = null;
//    int quality = 60;
//    int timeout = 20000;
//    byte[] ISOTemplate = null;
//    byte[] ANSITemplate = null;
//    String key = "";
//    DeviceInfo deviceInfo = new DeviceInfo();
//
//
//    public FetchLabourdetailscontroller_150322() {
//        mfs100 = new MFS100(this, key);
//        try {
//            System.out.println("JAVA_VERSION: " + System.getProperty("java.version"));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (mfs100.IsConnected()) {
//            System.out.println("Device is connected... ");
//            System.out.println(mfs100.GetSDKVersion());
//
//        } else {
//            System.out.println("Device is not Connected...");
//            //lblworkererror.setText("Kindly Reconnect the Single Fingerprint reader");
//        }
//
//        fingerprintinit = mfs100.Init();
//
//        if (fingerprintinit == 0) {
//            deviceInfo = mfs100.GetDeviceInfo();
//            System.out.println("Width: " + String.valueOf(deviceInfo.Width()));
//            System.out.println("Height: " + String.valueOf(deviceInfo.Height()));
//
//        } else {
//            System.out.println("Error Single Fingerprint reader not initialized");
//            //lblworkererror.setText("Kindly Reconnect the Single Fingerprint reader");
//        }
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//
//        //labourList = createdata();
//        // lblworkererror.setText("Initialized");
//        Details detail = Details.getdetails();
//        //To set the Contrator Name
//        lblcontractorName.setText(detail.getContractDetail().getContractorName());
//        //lblContractName.setText(detail.getContractdetail().getContact_id());
//
//        //Commented and will be added after labourList
//        /*
//        int pagesize = labourList.size() / 8 + 1;
//        System.out.println("Labour List Size::"+labourList.size());
//        System.out.println("Page Size::"+pagesize);
//        pagination.setPageCount(labourList.size() / 8 + 1);
//        pagination.setCurrentPageIndex(0);
//
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
//        tableview.setRowFactory(tv -> new TableRow<LabourDetails>() {
//            @Override
//            public void updateItem(LabourDetails item, boolean empty) {
//                super.updateItem(item, empty) ;
//                if (item == null) {
//                    setStyle("");
//                } else if (item.getStrStatus().equals("verified")) {
//                     setStyle("-fx-background-color: green;");
//                    //setStyle("-fx-background-color: #66ccff;");
//                } else {
//                    setStyle("");
//                }
//            }
//        });
//        */
//
////        labourName.setCellValueFactory(new PropertyValueFactory<LabourDetails,String>("labourName"));
////        labourID.setCellValueFactory(new PropertyValueFactory<LabourDetails,String>("labourID"));
////        dateOfBirth.setCellValueFactory(new PropertyValueFactory<LabourDetails,String>("dateOfBirth"));
////
//        String jsonLabourList = "";
//        String connurl = apiServerCheck.getLabourListURL();
////        String connectionStatus = apiServerCheck.getStatusLabourListAPI(connurl);
////        System.out.println("connection status :"+connectionStatus);
////        //Uncomment
////        if(!connectionStatus.contentEquals("connected")) {
////          lblworkererror.setText(connectionStatus);
////        }
//        //else {
//        //Details detail = Details.getdetails();
//        ContactDetail contractDetails = detail.getContractDetail();
//        jsonLabourList = apiServerCheck.getLabourListAPI(connurl, contractDetails.getContractorId(), contractDetails.getContactId());
//
//        //}
//        String sessionkey = apiServerCheck.sessionkey;
//        System.out.println("session Key LabourList::" + sessionkey);
//
//        //PKI Decrypt Session Key
//        // TO be uncommented later
//        PKIUtil pki = new PKIUtil();
//        byte[] base64decodesessionkey = Base64.getDecoder().decode(sessionkey);
//        String decodedString = new String(base64decodesessionkey);
//        System.out.println("decodedString:::" + decodedString);
//        String decryptedpkisessKey = "";
//
//        decryptedpkisessKey = pki.decrypt(base64decodesessionkey);
//
//        CryptoAES256 aes256 = new CryptoAES256(decryptedpkisessKey);
//        String decJson = aes256.decryptString(jsonLabourList);
//        System.out.println("Decrypt Json:::" + decJson);
//
//        //Object obj = JsonReader.jsonToJava(testJson);
//        //uncomment after
//        Object obj = JsonReader.jsonToJava(decJson);
//        System.out.println("obj str : " + obj.toString());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY); //Added for single value as array for dynamic details
//        //objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
//        //System.out.println("contractTableView in fetchdetailscontroller:"+ contractTableView);
//        LabourListDetails labourListResponse = null;
//        try {
//            //uncomment after
//            System.out.println(decJson);
//            labourListResponse = objectMapper.readValue(decJson, LabourListDetails.class);
//            //For testing purpose
//            //labourListResponse.getLaboursList().add(labourListResponse.getLaboursList().get(0));
//
//
//            //healthResponse = objectMapper.readValue(testJson, LabourListDetails.class);
//            System.out.println("labour list detals : " + labourListResponse.toString());
//        } catch (JsonProcessingException ex) {
//            Logger.getLogger(FetchLabourdetailscontroller_150322.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        String errorcode_labour = labourListResponse.getErrorCode();
//        System.out.println("Error Code ::: " + errorcode_labour);
//        if (!errorcode_labour.equals("0")) {
//            System.out.println("Error" + labourListResponse.getDesc());
//            lblworkererror.setText(labourListResponse.getDesc());
//        }
//
//        if (labourListResponse.getLabourList() != null) {
//            labourFpLists = labourListResponse.getLabourList();
//        }
//
//
//        if (labourListResponse.getLabourList().size() > 0) {
//            for (Labour labour : labourListResponse.getLabourList()) {
//                //Dynamic File List For a Labour
//                for (DynamicFileList dynamicFileList : labour.getDynamicFileList()) {
//                    System.out.println("Contractor ID::::" + dynamicFileList.getContractorId());
//                    contractorDynamicFile.setDynamicContractorId(dynamicFileList.getContractorId());
//                    contractorDynamicFile.setDynamicIssuanceUnit(dynamicFileList.getIssuanceUnit());
//                    contractorDynamicFile.setDynamicUserCategoryId(dynamicFileList.getUserCategoryId());
//                    //Modified by K. Karthikeyan
//                    System.out.println("\nContractor ID: " + dynamicFileList.getContractorId() + "\nIssuance Unit: " + dynamicFileList.getIssuanceUnit());
//                    System.out.println("\nUserCategory ID: " + dynamicFileList.getUserCategoryId());
//                    //
//                }
//                //Access Files Lsit For a Labour
//                for (AccessFileList accessFileList : labour.getAccessFileList()) {
//                    contractorDynamicFile.setAccessPermissionUnitCode(accessFileList.getUnitCode());
//                    contractorDynamicFile.setAccessPermissionZoneId(accessFileList.getZoneId());
//                    contractorDynamicFile.setAccessPermissionWorkingCode(accessFileList.getWorkingHourCode());
//                    contractorDynamicFile.setAccessDetailsFromDate(accessFileList.getFromDate());
//                    contractorDynamicFile.setAccessDetailsToDate(accessFileList.getToDate());
//                    //Modified by K. Karthikeyan
//                    System.out.println("\nAccPerUnitCode: " + accessFileList.getUnitCode() + "\nZoneId : " + accessFileList.getZoneId());
//                    System.out.println("\nAccPerWorkingCode: " + accessFileList.getWorkingHourCode() + "\nFrom Date: " + accessFileList.getFromDate());
//                    System.out.println("To Date: " + accessFileList.getToDate());
//                    //Modified by K. Karthikeyan
//                }
//                //Fingerprint List For a labour
//                for (LabourFP labourFP : labour.getFPs()) {
//                    contractorDynamicFile.setLabourFpPos(labourFP.getFpPos());
//                    contractorDynamicFile.setLabourFpData(labourFP.getFpData());
//                    //Modified by K. Karthikeyan
//                    System.out.println("\nFpPos " + labourFP.getFpPos() + "\nFpData" + labourFP.getFpData());
//                }
//
//                contractorDynamicFile.setSignatureFile1(labour.getSignFile1());
//                contractorDynamicFile.setSignatureFile3(labour.getSignFile3());
//            }
//        }
//
//
//        //Set the Contract Name
//        lblContractName.setText(contractorDynamicFile.getDynamicContractorId());
//        System.out.println("Dynamic Contractor ID :" + contractorDynamicFile.getDynamicContractorId());
//
//
//        List<LabourDetails> labourListres = new ArrayList<LabourDetails>();
//
//
//        if (labourListResponse != null) {
//            if (labourListResponse.getLabourList().size() > 0) {
//                for (Labour labour : labourListResponse.getLabourList()) {
//                    for (DynamicFileList dynamicFileList : labour.getDynamicFileList()) {
//                        LabourDetails labourDetails = new LabourDetails();
//
//                        labourDetails.setDateOfBirth(dynamicFileList.getLabourDateOfBirth());
//                        labourDetails.setLabourID(dynamicFileList.getLabourId());
//                        labourDetails.setLabourName(dynamicFileList.getLabourName());
//                        labourDetails.setStrStatus("Not verified");
//                        labourListres.add(labourDetails);
//                        // labourMap.put(labour.getLabourId(), labour);
//                        labourMap.put(dynamicFileList.getLabourId(), labour);
//                        //Modified by K. Karthikeyan
//                        System.out.println("\nDOB : " + dynamicFileList.getLabourDateOfBirth() + "\nLabourID" + dynamicFileList.getLabourId());
//                        System.out.println("\nName : " + dynamicFileList.getLabourName());//+"\nStrstatus"+dynamicFileList.getLabourId());
//
//                    }
//
//                }
//
//            }
//        }
//
//        labourList = labourListres;
//
//        System.out.println("LABOUR LIST:::::" + labourList.toString());
//        System.out.println("LABOUR LIST:::::" + labourList.size());
//
//        //Added on 07-03-22
//        int extra_page = 0;
//        if (labourList.size() % 8 == 0) extra_page = 0;
//        else extra_page = 1;
//        int pagesize = labourList.size() / 8 + extra_page;
//        System.out.println("Labour List Size::" + labourList.size());
//        System.out.println("Page Size::" + pagesize);
//        pagination.setPageCount(pagesize);
//        pagination.setCurrentPageIndex(0);
//
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
//        searchBox.textProperty().addListener((observable, oldValue, newValue) -> tableview.setItems(filterList(labourList, newValue)));
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
//                capturesinglefinger.setDisable(false);
//                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
//                    LabourDetails element = row.getItem();
//                    // now you can do whatever you want with the myModel variable.
//                    System.out.println(element.toString());
//                    id = element.getLabourID();
//                    name = element.getLabourName();
//                    dob = element.getDateOfBirth();
//                    System.out.println(name);
//
//                    //setContractID(element);
//
//                }
//            });
//            return row;
//        });
//
//
////        try {
////           startCapture();
////        } catch (InterruptedException ex) {
////            Logger.getLogger(FetchLabourdetailscontroller.class.getName()).log(Level.SEVERE, null, ex);
////        }
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
//        // lblworkererror.setText("Button Init");
////            Object object =  tableview.getSelectionModel().selectedItemProperty().getClass();
////            int index = tableview.getSelectionModel().selectedIndexProperty().get();
////
////            System.out.println(object);
////            System.out.println("The Row number of User is:   "+index);
//        //Empty the lblworkererror
//        lblworkererror.setText("");
//        capturesinglefinger.setDisable(true);
//
//        //Initialize Fingerprint Device if not initialized
//        if (fingerprintinit != 0) {
//            fingerprintinit = mfs100.Init();
//            System.out.println("FingerPrintinit:::" + fingerprintinit);
//        }
//        System.out.println("FingerPrintinitttt:::" + fingerprintinit);
//
//        if (tableview.getSelectionModel().getSelectedItem() == null) {
//            lblworkererror.setText("Kindly Select the Labour");
//
//        } else {
//
//            int ret = mfs100.StartCapture(quality, timeout, true);
//            System.out.println("ret val :" + ret);
//            if (ret != 0) {
//                System.out.println("Error..!!");
//                lblworkererror.setText("Reconnect the single Fingerprint Device");
//            }
//        }
//
//    }
//
//    @FXML
//    @Override
//    public void OnPreview(FingerData fd) {
//
//        System.out.println("OnPreview");
//
//        Runnable runnable = new Runnable() {
//            public void run() {
//                if (fd != null) {
//                    BufferedImage bufferedImage = mfs100.BytesToBitmap(fd.FingerImage());
//                    WritableImage wr = null;
//                    if (bufferedImage != null) {
//                        System.out.println("BUFDREA not null");
//                        wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
//                        PixelWriter pw = wr.getPixelWriter();
//                        for (int x = 0; x < bufferedImage.getWidth(); x++) {
//                            for (int y = 0; y < bufferedImage.getHeight(); y++) {
//                                pw.setArgb(x, y, bufferedImage.getRGB(x, y));
//                            }
//                        }
//                    }
//
//                    ImageView imView = new ImageView(wr);
//                    System.out.println("IMAGEEEEEE" + imView.getImage().toString());
//                    m_FingerPrintImage.setImage(wr);
//                }
//            }
//        };
//        Thread trd = new Thread(runnable);
//        trd.start();
//
//
//    }
//
//    @FXML
//    @Override
//    public void OnCaptureCompleted(boolean status, int i, String string, FingerData fingerData) {
//
////        if(status){
////        Runnable runnable = new Runnable() {
////
////            @Override
////            public void run() {
////                System.out.println("Capture completed");
////                //BufferedImage bufferedImage = mfs100.BytesToBitmap(fingerData.FingerImage());
////                BufferedImage bufferedImage = mfs100.BytesToBitmap(fingerData.FingerImage());
////                WritableImage wr = null;
////                        if (bufferedImage != null) {
////                            System.out.println("ONCAPTURE COMPLETED not null");
////                            wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
////                            PixelWriter pw = wr.getPixelWriter();
////                            for (int x = 0; x < bufferedImage.getWidth(); x++) {
////                            for (int y = 0; y < bufferedImage.getHeight(); y++) {
////                                pw.setArgb(x, y, bufferedImage.getRGB(x, y));
////                                }
////                            }
////                        }
////
////            ImageView imageview = new ImageView();
////            System.out.println("ONCAPTURE COMPLETED" +imageview.getImage().toString());
////            m_FingerPrintImage.setImage(wr);
////
////            }
////        };
////     }
//        System.out.println("In Capture Completed");
//
//
//        fingerprintMatching(fingerData);
//
//    }
//
//    @FXML
//    public void fingerprintMatching(FingerData fingerData) {
//
//
//        //  lblworkererror.setText("Fingerprint Matching");
//        // for fingerprint matching logic
////        File file = new File("/usr/share/enrollment/fingerprint/fingerprint.ansi");
////        System.out.println("In FingerPrint Matching");
////        byte[] bytes = new byte[(int) file.length()];
////        String fpData = "";
////        // funny, if can use Java 7, please uses Files.readAllBytes(path)
////        try(FileInputStream fis = new FileInputStream(file)){
////        fis.read(bytes);
////        System.out.println("bytes :"+ bytes.toString() + " : " + bytes.length);
//        //ObservableList<LabourDetails> labourdetails = tableview.getItems();
//        String fpData = "";
//        int matchfound = 0;
//        for (int j = 0; j < labourFpLists.size(); j++) {
//            List<LabourFP> labour = labourFpLists.get(j).getFPs();
//            for (int k = 0; k < labour.size(); k++) {
//                System.out.println("FingerPrint position:::::" + labour.get(k).getFpPos());
//                System.out.println("FingerPrint Data:::::" + labour.get(k).getFpData());
//                System.out.println("MatchISO\n" + fingerData.ISOTemplate() + "\n===========================\n" + Base64.getDecoder().decode(labour.get(k).getFpData()));
//                int ret = mfs100.MatchISO(fingerData.ISOTemplate(), Base64.getDecoder().decode(labour.get(k).getFpData()));
//
//
//                // int ret = mfs100.MatchISO(fingerData.ISOTemplate(),Base64.getDecoder().decode(labour.get(k).fpData));
//                // String fingertemplate="Rk1SACAyMAAAAADYAAABIQGZAMUAxQEAAAAxH0CRAN6mZIBeAN0eZIDKAP+zZECUATfQSUDYAQ7VZEC4AJb/ZEDsAR5jRECVAWZnQUDZAIn4ZIBdAWsDHkCqAC2KTEB3ANOgZEBxASGwZEB1ALKcZIDRANeIZECUAURHL0DKAKKIZECAAWJ+MYCeAHWTZECSAXJ4FEBVAHUYZEBeAQKmX0B8ASjEZIBIAOekZIBxATu8ZEA0AOQfZEDvANNtZEBcAVkXRYA+AI+ZZIC0AW9kEUBIAHGaWwAA";
//                //String fingertemplate2011="Rk1SADAzMAAAAADrAAEAAAAA3P///////////wAAAAAAAAAAAMUAxQABIQGZYB9AkQDepmSAXgDdHmSAygD/s2RAlAE30ElA2AEO1WRAuACW/2RA7AEeY0RAlQFmZ0FA2QCJ+GSAXQFrAx5AqgAtikxAdwDToGRAcQEhsGRAdQCynGSA0QDXiGRAlAFERy9AygCiiGRAgAFifjGAngB1k2RAkgFyeBRAVQB1GGRAXgECpl9AfAEoxGSASADnpGSAcQE7vGRANADkH2RA7wDTbWRAXAFZF0WAPgCPmWSAtAFvZBFASABxmlsAAA==";
//                //int ret = mfs100.MatchISO(fingerData.ISOTemplate(),Base64.getDecoder().decode(fingertemplate));
//                //int ret = mfs100.MatchANSI(fingerData.ANSITemplate(),Base64.getDecoder().decode(fingertemplate2011));
//                //int ret = -1309;
//                System.out.println("return value:: : " + ret);
//
//                if (ret > fpQuality) {   //Match Single Fingerprint
//                    //if(ret==0){
//                    System.out.println("Finger print quality check : " + fpQuality);
//                    MatchIsoTemplate(fingerData.ISOTemplate(), Base64.getDecoder().decode(fpData), ret);
//                    matchfound = 1;
//                    break;
//                }
//                /*
//                if(labour.get(k).fpPos.equals("RT")) {
//                    fpData = labour.get(k).fpData;
//                   // break;
//                }*/
//            }
//            System.out.println("Finger fp list :" + labourFpLists.get(j).toString());
//            System.out.println("size :" + labourFpLists.get(j).getFPs().size());
//            System.out.println("\nLabour FPlists Size" + labourFpLists.size() + "\nLabour Size::" + labour.size() + "\nIteration Number :" + j);
//            if (matchfound == 1) {
//                System.out.println("Match found exiting");
//                break;
//            }
//            //System.out.println("Finger data : "+ fpData.getBytes() + " : " + fingerData.ISOTemplate());
//
//        }
//
//        if (matchfound == 0) {
//            System.out.println("Finger Print NOT Matched");
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    lblworkererror.setText("Finger print not Matched Try Again");
//                }
//            });
//        }
//        //MatchIsoTemplate(fingerData.ISOTemplate(),Base64.getDecoder().decode(fpData));
//        //  int ret = mfs100.MatchANSI(fingerData.ANSITemplate(),bytes);
//
//
////    }   catch (FileNotFoundException ex) {
////            ex.printStackTrace();
////        } catch (IOException ex) {
////            ex.printStackTrace();
////        }
//        //WriteBytesToFile("fingerprint.bmp", fingerData.FingerImage());
//    }
//
//    @FXML
//    public void MatchIsoTemplate(byte[] ISOTemplate, byte[] decode, int ret) {
//        //     public void MatchIsoTemplate(byte[] ISOTemplate, byte[] decode) {
//
//        LabourDetails row = tableview.getSelectionModel().getSelectedItem();
//        if (row == null) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    //lblworkererror.setText("Finger Print Matching...");
//                    lblworkererror.setText("Kindly Select the Labour");
//                }
//            });
//
//        } else {
//            //Base64.getDecoder().decode(fpData);
//            //int ret = mfs100.MatchISO(ISOTemplate,decode);
//            System.out.println("return value : " + ret);
//            //Label lblworkererror = new Label();
//
//
//            //uncomment later
//            if (ret > fpQuality) {
//                //if(ret == 0){
//                //if(ret < 0){
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        //lblworkererror.setText("Finger Print Matching...");
//                        lblworkererror.setText("Finger print Matched");
//                    }
//                });
//
//
//                //Token Dispence
//                //Uncomment Later
//                TokenDispense tokenDispence = new TokenDispense();
//                String tokenDispenceOutput = tokenDispence.tokenDispense();
//                if (tokenDispenceOutput == "failure") {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            lblworkererror.setText("Kindly Connect the Token Dispencer And Try Again");
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
//                        System.out.println("Card Write Details:::" + returncardwrite);
//
//                        if (returncardwrite == "failure") {
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    lblworkererror.setText("Kindly put the token in Token Dispencer And Try Again");
//                                }
//                            });
//
//                        } else {
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
//                            token.setVerifyFPSerialNo(mfs100.GetDeviceInfo().SerialNo());
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
//                                            lblworkererror.setText("Kindly collect the Token");
//                                        }
//                                    });
//
//                                } else {
//
//                                    Platform.runLater(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            lblworkererror.setText("Token Details Not Updated to Server, Try Again");
//                                        }
//                                    });
//
//                                }
//
//                            } catch (IOException ex) {
//                                Logger.getLogger(FetchLabourdetailscontroller_150322.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                            if (tableview.getItems().size() == 0) {
//                                try {
//                                    App.setRoot("list_contract");
//                                } catch (IOException ex) {
//                                    Logger.getLogger(FetchLabourdetailscontroller_150322.class.getName()).log(Level.SEVERE, null, ex);
//                                }
//                            }
//                        }
//                    } catch (Exception ex) {
//                        Logger.getLogger(FetchLabourdetailscontroller_150322.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            } else {
//                System.out.println("Finger Print NOT Matched");
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        lblworkererror.setText("Finger print not Matched Try Again");
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
//                Object obj = JsonReader.jsonToJava(jsonTokenUpdateResponse);
//                System.out.println("obj str : " + obj.toString());
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
//                            lblworkererror.setText("Kindly collect the Token");
//                        }
//                    });
//
//                    System.out.println("Token Details Updated to Server");
//                } else {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            lblworkererror.setText("Token Details Not Updated to Server, Try Again");
//                        }
//                    });
//
//                    System.out.println("Token Not Updated to Server, Try Again");
//                }
//
//            } catch (Exception e) {
//                System.out.println(e);
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
//            System.out.println("Json exception : " + ex.getMessage());
//        }
//        System.out.println("Error : " + error);
//        return error;
//    }
//
//
//    private Node createPage(int pageIndex) {
//
//        int fromIndex = pageIndex * 8;
//        int toIndex = Math.min(fromIndex + 8, labourList.size());
//        System.out.println(" data size :" + labourList.size() + " " + fromIndex + " " + toIndex);
//
//
//        tableview.setFixedCellSize(30.0);
//        tableview.setItems(FXCollections.observableArrayList(labourList.subList(fromIndex, toIndex)));
//        //contractTableView.refresh();
//        return tableview;
//    }
//
//    private boolean searchFindsOrder(LabourDetails labourDetails, String searchText) {
//        return (labourDetails.getLabourID().toLowerCase().contains(searchText.toLowerCase())) || (labourDetails.getLabourName().toLowerCase().contains(searchText.toLowerCase())) || (labourDetails.getDateOfBirth().toLowerCase().contains(searchText.toLowerCase()));
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
//
//}
//
//
