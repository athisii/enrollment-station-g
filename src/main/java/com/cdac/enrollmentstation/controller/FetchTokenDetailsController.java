package com.cdac.enrollmentstation.controller;


import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.APIServerCheck;
import com.cdac.enrollmentstation.logging.ApplicationLogOld;
import com.cdac.enrollmentstation.model.ContactDetail;
import com.cdac.enrollmentstation.model.ContractDetail;
import com.cdac.enrollmentstation.model.ContractDetailList;
import com.cdac.enrollmentstation.model.Details;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FetchTokenDetailsController implements Initializable {
    @FXML
    private TableView<ContractDetail> contractTableView;

    @FXML
    private TextField ContractID;

    @FXML
    private TextField CardSerialNo;

    @FXML
    private Label label;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblcontractorID;

    @FXML
    private Label lblcontractorName;


//    @FXML
//    private TableColumn<contactDetail, String> userIdColumn ;

    @FXML
    public TableColumn<ContractDetail, String> contractID;

    @FXML
    public TableColumn<ContractDetail, String> contractValidFrom;

    @FXML
    public TableColumn<ContractDetail, String> contractValidUpto;

    public APIServerCheck apiServerCheck = new APIServerCheck();

    List<ContractDetail> contractList = new ArrayList<ContractDetail>();

    @FXML
    private Pagination pagination;

    @FXML
    TextField searchBox;

    //For Application Log
    ApplicationLogOld appLog = new ApplicationLogOld();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public FetchTokenDetailsController() {

        //this.handler = appLog.getLogger();
        // LOGGER.addHandler(handler);
    }


    @FXML
    private void showHome() {
        try {
            App.setRoot("first_screen");
        } catch (IOException ex) {
            Logger.getLogger(FetchTokenDetailsController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "Exception:" + ex);
        }

    }


    @FXML
    private void FetchDetails() {

        Details detail = Details.getdetails();
        ContactDetail contractDetails = detail.getContractDetail();
        lblcontractorID.setText(contractDetails.getContractorId());
        lblcontractorName.setText(contractDetails.getContractorName());
        String json = "";
        String connurl = apiServerCheck.getContractListURL();
        String connectionStatus = apiServerCheck.getStatusContractListAPI(connurl, contractDetails.getContractorId(), contractDetails.getSerialNo());
        //System.out.println("connection status :"+connectionStatus);
        LOGGER.log(Level.INFO, "connection status ::" + connectionStatus);

        try {
            if (!connectionStatus.contentEquals("connected")) {
                lblStatus.setText(connectionStatus + " Try Again");
                LOGGER.log(Level.INFO, "Try Again" + connectionStatus);
            } else {
                System.out.println("Contractor ID From Fetch Token ::::" + contractDetails.getContractorId());
                System.out.println("Serial Number From Fetch Token::::" + contractDetails.getSerialNo());

                //Get the Contract List from ContractList API (APIServerCheck)
                json = apiServerCheck.getContractListAPI(connurl, contractDetails.getContractorId(), contractDetails.getSerialNo());
                System.out.println("output :" + json);

                //Getting Session Key from the ContractList API(APIServerCheck)
                //        String sessionkey = apiServerCheck.sessionkey;
                //        System.out.println("SESSION KEY from Fetch Token"+sessionkey);
                //Encrypt the session key        
                //uncomment After
                //CryptoAES256 aes256 = new CryptoAES256(sessionkey);

                //decrypt the JSON output using aes256
                //uncomment After
                // String decJson = aes256.decryptString(json);

                //decrypted JSON to obj
                //uncomment  after
                //Object obj = JsonReader.jsonToJava(decJson);
                try {
                    //Object obj = JsonReader.jsonToJava(json);
                    //System.out.println("obj str : " +obj.toString());
                    ObjectMapper objectMapper = new ObjectMapper();
                    // objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
                    //System.out.println("contractTableView in fetchdetailscontroller:"+ contractTableView);

                    //JSON String to a Java object using the ObjectMapper class
                    //uncomment After
                    //ContractIDList healthResponse = objectMapper.readValue(decJson, ContractIDList.class);
                    ContractDetailList contractidlist = objectMapper.readValue(json, ContractDetailList.class);
                    String errorcode = contractidlist.getErrorCode();
                    System.out.println("Error Code ::: " + errorcode);

                    if (errorcode.equals("0")) {
                        List<ContractDetail> contractListRes = new ArrayList<ContractDetail>();
                        if (contractidlist != null) {
                            if (contractidlist.getContractDetailSet().size() > 0) {
                                for (ContractDetail contract : contractidlist.getContractDetailSet()) {
                                    contractListRes.add(contract);
                                    //Added for Testing
                                    //contractListRes.add(contract);
                                }
                            }
                        }
                        //Save Contract ID List ( contract Id, valid from, valid to ) to contractList POJO
                        contractList = contractListRes;

                        //Added on 07/03/22
                        int extra_page = 0;
                        if (contractList.size() % 8 == 0)
                            extra_page = 0;
                        else
                            extra_page = 1;
                        int pagesize = contractList.size() / 8 + extra_page;
                        pagination.setPageCount(contractList.size() / 8 + 1);
                        pagination.setCurrentPageIndex(0);

                        pagination.setPageFactory(new Callback<Integer, Node>() {
                            @Override
                            public Node call(Integer pageIndex) {
                                System.out.println("page index :" + pageIndex);
                                if (pageIndex > contractList.size() / 8 + 1) {
                                    return null;
                                } else {
                                    return createPage(pageIndex);
                                }
                            }
                        });
                        //Added on 07/03/22

                        searchBox.textProperty().addListener((observable, oldValue, newValue) ->
                                contractTableView.setItems(filterList(contractList, newValue)));

                        //Get the Contract ID
                        List<String> contractIDList1 = new ArrayList<String>();
                        for (ContractDetail contractDetail : contractidlist.getContractDetailSet()) {
                            contractIDList1.add(contractDetail.getContractId());
                        }
                        System.out.println("ContractID List:::" + contractidlist.getContractDetailSet());

                        //        ObservableList<String> list = FXCollections.observableArrayList(
                        //                contractIDList1);
                        ObservableList<ContractDetail> observablelist = FXCollections.observableArrayList(contractListRes);

                        //Set the contract ID , valid from, valid to to the FXML Table
                        contractID.setCellValueFactory(new PropertyValueFactory<ContractDetail, String>("contractId"));
                        contractValidFrom.setCellValueFactory(new PropertyValueFactory<ContractDetail, String>("contractValidFrom"));
                        contractValidUpto.setCellValueFactory(new PropertyValueFactory<ContractDetail, String>("contractValidUpto"));
                        //Details detail = Details.getdetails();
                        detail.setTokenDetail(contractidlist);
                        /*contactDetail contractDetails = detail.getContractdetail();
                        System.out.println("contract details :"+ contractDetails.toString());
                        contractDetails.setContact_id(json); */
                        //contractID.setStyle("-fx-alignment: CENTER; ");
                        //contractValidFrom.setStyle("-fx-alignment: CENTER; ");
                        //contractValidUpto.setStyle("-fx-alignment: CENTER; ");
                        contractTableView.setFixedCellSize(35.0);
                        contractTableView.setItems(observablelist);
                        contractTableView.refresh();


                        // contractTableView.setItems(observablelist);
                        // contractTableView.refresh();
                    } else {
                        String desc = contractidlist.getDesc();
                        lblStatus.setText(desc);
                    }
                } catch (NullPointerException e) {
                    //System.out.print("NullPointerException caught");
                    LOGGER.log(Level.INFO, "Exception:" + e);
                    lblStatus.setText("No Contract List is Available");
                }
            }
        } catch (Exception e) {
            //System.out.println("Exception At Fetch Token Details:: "+e);
            LOGGER.log(Level.INFO, "Exception At Fetch Token Details::" + e);
            lblStatus.setText("" + e);
        }
    }


    private Node createPage(int pageIndex) {

        int fromIndex = pageIndex * 8;
        int toIndex = Math.min(fromIndex + 8, contractList.size());
        //System.out.println(" data size :"+ contractList.size() + " " + fromIndex + " " + toIndex);
        LOGGER.log(Level.INFO, " data size :" + contractList.size() + " " + fromIndex + " " + toIndex);

        contractTableView.setRowFactory(tv -> {
            TableRow<ContractDetail> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // check for non-empty rows, double-click with the primary button of the mouse
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    ContractDetail element = row.getItem();
                    // now you can do whatever you want with the myModel variable.
                    System.out.println(element.getContractId());
                    setContractID(element.getContractId());

                    try {
                        App.setRoot("listofworkers_1");
                    } catch (IOException ex) {
                        //ex.printStackTrace();
                        //System.out.println("IO Exception:: "+ex);
                        LOGGER.log(Level.INFO, "Exception :" + ex);
                        lblStatus.setText("" + ex);
                    }
                }
            });
            return row;

        });
        contractTableView.setFixedCellSize(30.0);
        contractTableView.setItems(FXCollections.observableArrayList(contractList.subList(fromIndex, toIndex)));
        //contractTableView.refresh();
        return contractTableView;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //System.out.println("contractList :"+ (contractList.size()/8 + 1));
        //contractList = createData();
        //Commented on 070322
       /*
        pagination.setPageCount(contractList.size() / 8 + 1);
        pagination.setCurrentPageIndex(0);
        
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                System.out.println("page index :" + pageIndex);
                if (pageIndex > contractList.size() / 8 + 1) {
                    return null;
                } else {
                    return createPage(pageIndex);
                }
            }
        });
        
        
        searchBox.textProperty().addListener((observable, oldValue, newValue) ->
        contractTableView.setItems(filterList(contractList, newValue))            ); */
        //pagination.setPageFactory(this::createPage);
        //Commented on 070322

        try {
            //Fetch Token Details
            FetchDetails();

        } catch (Exception e) {
            //System.out.println("Fecth detail exception:: "+e);
            LOGGER.log(Level.INFO, "Fecth detail exception:: :" + e);
            lblStatus.setText("" + e);
        }
    }

    private boolean searchFindsOrder(ContractDetail contractDetail, String searchText) {
        return (contractDetail.getContractId().toLowerCase().contains(searchText.toLowerCase())) ||
                (contractDetail.getContractValidFrom().toLowerCase().contains(searchText.toLowerCase())) ||
                (contractDetail.getContractValidUpto().toLowerCase().contains(searchText.toLowerCase()));
    }

    private ObservableList<ContractDetail> filterList(List<ContractDetail> list, String searchText) {
        List<ContractDetail> filteredList = new ArrayList<>();
        for (ContractDetail contract : list) {
            if (searchFindsOrder(contract, searchText)) filteredList.add(contract);
        }
        return FXCollections.observableList(filteredList);
    }

    public void setContractID(String contractID) {
        Details detail = Details.getdetails();
        ContactDetail contractDetails = detail.getContractDetail();
        contractDetails.setContactId(contractID);
        detail.setContractDetail(contractDetails);
    }
    
/*
   public List<ContractIDNew> createData(){
        
        List<ContractIDNew> contractList1 = new ArrayList<>();
       ContractIDNew cont1 = new ContractIDNew();
        cont1.setContractId("1234567812");
       cont1.setContractName("Contract 1");
        cont1.setWorkOrderNo("1234567976");
        contractList1.add(cont1);
        
        
        ContractIDNew cont2 = new ContractIDNew();
        cont2.setContractId("1234567813");
        cont2.setContractName("Contract 2");
        cont2.setWorkOrderNo("1234567977");
        contractList1.add(cont2);
        
        ContractIDNew cont3 = new ContractIDNew();
        cont3.setContractId("1234567814");
        cont3.setContractName("Contract 3");
        cont3.setWorkOrderNo("1234567978");
        contractList1.add(cont3);
        
        ContractIDNew cont4 = new ContractIDNew();
        cont4.setContractId("1234567815");
        cont4.setContractName("Contract 4");
        cont4.setWorkOrderNo("1234567979");
        contractList1.add(cont4);
        
        ContractIDNew cont5 = new ContractIDNew();
        cont5.setContractId("1234567816");
        cont5.setContractName("Contract 5");
        cont5.setWorkOrderNo("1234567980");
        contractList1.add(cont5);
        
        ContractIDNew cont6 = new ContractIDNew();
        cont6.setContractId("1234567817");
        cont6.setContractName("Contract 6");
        cont6.setWorkOrderNo("1234567981");
        contractList1.add(cont6);
        
        ContractIDNew cont11 = new ContractIDNew();
        cont11.setContractId("1234567818");
        cont11.setContractName("Contract 7");
        cont11.setWorkOrderNo("1234567982");
        contractList1.add(cont11);
        
        ContractIDNew cont7 = new ContractIDNew();
        cont7.setContractId("1234567819");
        cont7.setContractName("Contract 8");
        cont7.setWorkOrderNo("1234567983");
        contractList1.add(cont7);
        
        ContractIDNew cont8 = new ContractIDNew();
        cont8.setContractId("1234567820");
        cont8.setContractName("Contract 9");
        cont8.setWorkOrderNo("1234567984");
        contractList1.add(cont8);
        
        ContractIDNew cont9 = new ContractIDNew();
        cont9.setContractId("1234567821");
        cont9.setContractName("Contract 10");
        cont9.setWorkOrderNo("1234567985");
        contractList1.add(cont9);
        
        ContractIDNew cont10 = new ContractIDNew();
        cont10.setContractId("1234567822");
        cont10.setContractName("Contract 11");
        cont10.setWorkOrderNo("1234567986");
        contractList1.add(cont10);
        
        ContractIDNew cont12 = new ContractIDNew();
        cont12.setContractId("1234567823");
        cont12.setContractName("Contract 12");
        cont12.setWorkOrderNo("1234567988");
        contractList1.add(cont12);
        
        ContractIDNew cont13 = new ContractIDNew();
        cont13.setContractId("1234567824");
        cont13.setContractName("Contract 13");
        cont13.setWorkOrderNo("1234567987");
        contractList1.add(cont13);
        
        
        return contractList1;
    }
  
    
      
  
    @FXML
    private void fetchcondetails() throws IOException,Exception{

//        String json = "{" +
//    "  \"Labours\": {" +
//    "    \"ErrorCode\": \"123\"," +
//    "    \"Desc\": \"ABC\"," +
//    "    \"LabourList\": [" +
//    "     \"DeviceCert\": \"string\",\n" +
//"      \"CardTypeID\": \"string\",\n" +
//"      \"UserCategoryID\": \"string\",\n" +
//"      \"ValidityofICard\": \"string\",\n" +
//"      \"Name\": \"string\",\n" +
//"      \"Gender\": \"string\",\n" +
//"      \"DateofBirth\": \"string\",\n" +
//"      \"BloodGroup\": \"string\",\n" +
//"      \"Nationality\": \"string\",\n" +
//"      \"Unit\": \"string\",\n" +
//"      \"RequestingUnit\": \"string\",\n" +
//"      \"UniqueID\": \"string\",\n" +
//"      \"ContractorID\": \"string\",\n" +
//"      \"ValidityFromDateTime\": \"string\",\n" +
//"      \"ValidityToDateTime\":\"string\",\n" +
//"      \"DateIssued\":\"string\",\n" +
//"      \"AccessPermission\": \"string\",\n" +
//"      \"FP1\": \"string\",\n" +
//"      \"FP2\": \"string\",\n" +
//"      \"FP3\": \"string\",\n" +
//"      \"FP4\": \"string\",\n" +
//"      \"IRIS1\": \"string\",\n" +
//"      \"IRIS2\": \"string\",\n" +
//"      \"Photo\": \"string\",\n" +
//"      \"SignOfIA\": \"string\"" +
//    "    ]" +
//    "  }" +
//    


// String json = "{" +
//    "  \"Tokens\": {" +
//    "    \"ErrorCode\": \"123\"," +
//    "    \"Desc\": \"ABC\"," +
//    "    \"ContractIDList\": [" +
//    "    \"contractID1\", \"contractID2\"" +
//    "    ]" +
//    "  }" +
//    "}";    
 
String json = "{" +
    "  \"Labours\": {" +
    "    \"ErrorCode\": \"123\"," +
    "    \"Desc\": \"ABC\"," +
    "    \"laboursList\": [" +
    "    \"DeviceCert\" , \"string\",\n" + 
    "    \"CardTypeID\" , \"saving\",\n" +
    "     \"UserCategoryID\" , \"string\",\n" +
    "    \"ValidityofICard\" ,  \"string\",\n" +
    "     \"Name\" , \"string\",\n" +
    "      \"Gender\" , \"string\",\n" +
    "      \"DateofBirth\" , \"string\",\n" +
    "      \"BloodGroup\" , \"string\",\n" +
    "      \"Nationality\" , \"string\",\n" +
    "      \"Unit\" , \"string\",\n" +
    "      \"RequestingUnit\" , \"string\",\n" +
    "      \"UniqueID\" ,  \"string\",\n" +
    "      \"ContractorID\" , \"string\",\n" +
    "      \"ValidityFromDateTime\" , \"string\",\n" +
    "      \"ValidityToDateTime\" , \"string\",\n" +
    "      \"DateIssued\" , \"string\",\n" +
    "      \"AccessPermission\" ,  \"string\",\n" +
    "      \"FP1\" , \"string\",\n" +
    "      \"FP2\" , \"string\",\n" +
    "      \"FP3\" , \"string\",\n" +
    "      \"FP4\" , \"string\",\n" +
    "      \"IRIS1\" , \"string\",\n" +
    "      \"IRIS2\" , \"string\",\n" +
    "      \"Photo\" , \"string\",\n" +
    "      \"SignOfIA\" , \"string\"" +
    "    ]" +
    "  }" +
    "}";  

        ObjectMapper objectmapper = new ObjectMapper();
        objectmapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        
        try {
            LabourListDetails details = objectmapper.readValue(json, LabourListDetails.class);
            System.out.println(details.toString());
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */


//    @FXML
//    void opennew(ActionEvent event){
//          try {
//             System.out.println(ContractID.getText());
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("ContractDetailsCon.fxml"));
//             Parent root = (Parent) loader.load();
//             
//             FetchTokenDetailsController controller = loader.getController();
//            
//             Stage stage = new Stage();
//             stage.setScene(new Scene(root));
//             stage.show();
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    @FXML
//    public void handle(ActionEvent event){
//        String id = ContractID.getText();
//        System.err.println(id);
//        
//        FXMLLoader Loader = new FXMLLoader();
//        Loader.setLocation(getClass().getResource("list_contract.fxml"));
//        
//        try {
//            Loader.load();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//                System.err.println(id);
//
//        ListContract list = new ListContract();
//        list.setText(id);
//    }

//    
//    @FXML
//    public void handle(){
//         try {
//           FXMLLoader loader = new FXMLLoader(getClass().getResource("list_contract.fxml"));
//           Parent root = loader.load();
//   
//           ListContract controller2 = loader.getController();
//           controller2.setText(ContractID.getText());
//           System.out.println(ContractID.getText());
//   
////           Stage stage = new Stage();
////           stage.setScene(new Scene(root));
////           stage.setTitle("Layout2 + Controller2");
////           stage.show();
//   
//       } catch (IOException e) {
//           e.printStackTrace();
//       }
//    }


}

