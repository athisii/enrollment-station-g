package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.api.APIServerCheck;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.UnitListDetails;
import com.cdac.enrollmentstation.model.Units;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ServerConfigController implements Initializable {
//    @FXML
//    private TextField UnitId;

    @FXML
    private TextField Url;

    @FXML
    private TextField SerialNO;

    @FXML
    private TextField finScannerInfo;

    @FXML
    private TextField cardReaderInfo;


    @FXML
    private Label label;

    @FXML
    private Label labelstatus;

    @FXML
    private Button FetchUnitButton;


    @FXML
    private ComboBox<Units> comboUnitId = new ComboBox<>();

    public APIServerCheck apiServerCheck = new APIServerCheck();

    TestProp prop = new TestProp();


    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public ServerConfigController() {
        //this.handler = appLog.getLogger();
        //LOGGER.addHandler(handler);  

    }

    @FXML
    public void showHome() {
        try {
            App.setRoot("first_screen");
        } catch (IOException ex) {
            Logger.getLogger(ServerConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "IOException:" + ex);
        }
    }

    @FXML
    public void goBack() throws IOException {
        App.setRoot("admin_config");
    }

    @FXML
    private void editDetails() {
        Url.setDisable(false);
        SerialNO.setDisable(false);
        comboUnitId.setDisable(false);
    }

    @FXML
    private void saveDetails() {
//        App.setRoot("secondary");
//        System.err.println(UnitId.getText());
//        System.err.println(Url.getText());
//        System.err.println(SerialNO.getText());

        writedata();
    }

    @FXML
    private void fetchunitdetails() {

        if (!isValidUrl(Url.getText())) {
            labelstatus.setText("Not a Valid URL");
            return;
        }
        String json = "";
        //String connurl = apiServerCheck.getUnitListURL();
        String connurl = Url.getText() + "/api/EnrollmentStation/GetAllUnits";
        String connectionStatus = apiServerCheck.getStatusUnitListAPI(connurl);
        //System.out.println("connection status :"+connectionStatus);
        LOGGER.log(Level.INFO, "connection status ::" + connectionStatus);
        if (!connectionStatus.contentEquals("connected")) {
            //labelstatus.setText(connectionStatus);
            labelstatus.setText("Enter the Correct URL in Mafis API");
            LOGGER.log(Level.INFO, "Enter the Correct URL in Mafis API");
        } else {
            try {
                json = apiServerCheck.getUnitListAPI(connurl);
                System.out.println("Output str : " + json);
                ObjectMapper objectmapper = new ObjectMapper();
                UnitListDetails details = null;
                try {
                    details = objectmapper.readValue(json, UnitListDetails.class);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(ServerConfigController.class.getName()).log(Level.SEVERE, null, ex);
                    LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                }
                System.out.println(details.toString());

                try {
                    ObservableList<Units> units = FXCollections.observableArrayList(details.getUnits());
                    comboUnitId.setItems(units);

                    System.out.println("combo value : " + comboUnitId.getSelectionModel().getSelectedItem());
                    comboUnitId.valueProperty().addListener((obs, oldval, newval) -> {
                        if (newval != null)
                            System.out.println("Selected unit: " + newval.getCaption()
                                    + ". ID: " + newval.getValue());
                    });

                } catch (Exception e) {
                    //e.printStackTrace();
                    //System.out.println("error :"+e.getMessage());
                    LOGGER.log(Level.INFO, "Exception:" + e);
                    labelstatus.setText("Kindly Fetch Enrollment Station UnitId");
                }
            } catch (NullPointerException e) {
                LOGGER.log(Level.INFO, "NullPointerException:" + e);
                //System.out.print("NullPointerException caught");
                labelstatus.setText("Unit Id List is not Available in the Server");
            }


        }

    }


    private void writedata() {
        Units unit = comboUnitId.getSelectionModel().getSelectedItem();
        if (unit == null || SerialNO.getText().length() == 0) {
            // if(Url.getText().length()==0 || SerialNO.getText().length() == 0) {
          /*
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("WARN:");
            alert.setContentText("One or more fields contains null values");
            alert.showAndWait();
            return;
            */
            labelstatus.setText("Kindly Select Enrollment Station UnitId");
            return;
        } else if (!isValidUrl(Url.getText())) {
            labelstatus.setText("Not a Valid URL");
            return;
        }
        System.out.println("Value:" + unit.getValue());
        System.out.println("Cap:" + unit.getCaption());
        String line = unit.getValue() + "," + Url.getText() + "," + SerialNO.getText();

        FileWriter file_writer;
        BufferedWriter buffered_writer = null;

        try {
            String urldata = null;
            urldata = prop.getProp().getProperty("urldata");
            if (urldata.isBlank() || urldata.isEmpty() || urldata == null) {
                //System.out.println("The property 'urldata' is empty, Please add it in properties");
                LOGGER.log(Level.INFO, "The property 'urldata' is empty, Please add it in properties");
                return;
            }

            //file_writer = new FileWriter("/etc/data.txt",false);
            file_writer = new FileWriter(urldata, false);

            buffered_writer = new BufferedWriter(file_writer);

            buffered_writer.write(line);
            buffered_writer.flush();
            buffered_writer.close();
            file_writer.close();

            //Alert Message After added data sucessfully
//            Alert alert = new Alert(AlertType.INFORMATION);
//            alert.setTitle("Status:");
//            alert.setContentText("Your Data Has been added sucesfully.");            
//            alert.showAndWait();

            labelstatus.setText("Server Details added sucesfully");
            labelstatus.setText("Server Details added sucesfully");
            Url.setDisable(true);
            SerialNO.setDisable(true);
            comboUnitId.setDisable(true);
            //load the fxml need here!
//            System.out.println("loading fxml!");
//            label.setText("loading fxml!");
        } catch (Exception e) {
            //System.out.println("Add line failed!!" +e);
            LOGGER.log(Level.INFO, "Add line failed!!" + e);
            labelstatus.setText("Server Details failed to add, Try again");
        }
        
        /*
        finally{
            try {
                buffered_writer.flush();
                buffered_writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerConfigController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO,  "IOException",ex );
            }
        }*/
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try (BufferedReader file = new BufferedReader(new FileReader("/etc/data.txt"))) {
            String line = " ";
            String input = " ";
            UnitListDetails details = new UnitListDetails();
            List<Units> listunits = new ArrayList<>();
            Units units = new Units();
            while ((line = file.readLine()) != null) {
                String[] tokens = line.split(",");
                System.out.println("Token 0::" + tokens[0]);
                //comboUnitId.setValue();
                //UnitId.setText(tokens[0]);
                Url.setText(tokens[1]);
                SerialNO.setText(tokens[2]);
                units.setValue(tokens[0]);
                units.setCaption(tokens[0]);
                listunits.add(units);
                //System.out.println("Token 0::"+tokens[0]);
                //listunits.set(0, units);
                details.setUnits(listunits);
                ObservableList<Units> unitss = FXCollections.observableArrayList(details.getUnits());

                // ObservableList<Units> units = FXCollections.observableArrayList();
                comboUnitId.setItems(unitss);
                comboUnitId.getSelectionModel().selectFirst();
                //comboUnitId.setValue();
                Url.setDisable(true);
                SerialNO.setDisable(true);
                comboUnitId.setDisable(true);
                //comboUnitId.setValue(tokens[0]);
//                UnitId.appendText(tokens[0]);
//                Url.appendText(tokens[1]);
//                SerialNO.appendText(tokens[2]);

                // UnitId.getText();


//            FileWriter file_writer;
//            file_writer = new FileWriter("/home/shubham/data.txt",true);
//            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
//            //buffered_writer.write(tokens[0] +","+tokens[1]+""+tokens[2]);
//            buffered_writer.flush();
//            buffered_writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem reading file.");
            labelstatus.setText("Could not able to Read the Data, Try Again");

        }
        
        
        /*
        String json = "";
        String connurl = apiServerCheck.getUnitListURL();
        String connectionStatus = apiServerCheck.getStatusUnitListAPI(connurl);
        System.out.println("connection status :"+connectionStatus);
        if(!connectionStatus.contentEquals("connected")) {
            labelstatus.setText(connectionStatus);
        }
        else {
            
            json = apiServerCheck.getUnitListAPI(connurl);
            System.out.println("Output str : "+json);
        }   
        ObjectMapper objectmapper = new ObjectMapper();
       
        
        try {
            UnitListDetails details = objectmapper.readValue(json, UnitListDetails.class);
            System.out.println(details.toString());
            ObservableList<Units> units = FXCollections.observableArrayList(details.getUnits());
            comboUnitId.setItems(units);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error :"+e.getMessage());
        }
        
        System.out.println("combo value : "+comboUnitId.getSelectionModel().getSelectedItem());
        comboUnitId.valueProperty().addListener((obs, oldval, newval) -> {
            if(newval != null)
            System.out.println("Selected unit: " + newval.getCaption()
                + ". ID: " + newval.getValue());
        });*/


        try {
            String urldata = null;
            urldata = prop.getProp().getProperty("urldata");
            if (urldata.isBlank() || urldata.isEmpty() || urldata == null) {
                //System.out.println("The property 'urldata' is empty, Please add it in properties");
                LOGGER.log(Level.INFO, "The property 'urldata' is empty, Please add it in properties");
                return;
            }
            //BufferedReader file = new BufferedReader(new FileReader("/etc/data.txt"));
            BufferedReader file = new BufferedReader(new FileReader(urldata));
            String line = file.lines().collect(Collectors.joining());
            String[] tokens = line.split(",");
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception" + e);
            //e.printStackTrace();
            // System.out.println("file reading problem. ");
            labelstatus.setText("Could not able to Read the Data, Try Again");
        }

    }

    public boolean isValidUrl(String url) {

        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(ServerConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "Exception", ex);
            return false;
        } catch (URISyntaxException ex) {
            Logger.getLogger(ServerConfigController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "Exception" + ex);
            return false;
        }

    }


    public void fetchSystemDetails() {
        //System.out.println("Inside fetching system information");
        LOGGER.log(Level.INFO, "Inside fetching system information");
        //Code to fetch Slap scanner system details
        //Fix the fetched data to the text field 
        String result = "";
        StringBuilder response = new StringBuilder();
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        String licenseurl = null;
        licenseurl = prop.getProp().getProperty("licenseurl");
        if (licenseurl.isBlank() || licenseurl.isEmpty() || licenseurl == null) {
            //System.out.println("The property 'licenseurl' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'licenseurl' is empty, Please add it in properties");
            return;
        }
        //String licenseURL = "http://localhost:8088/N_getSystemInfo";
        try {
            URL siteURL = new URL(licenseurl);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {

                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception:" + result);

        }
        //System.out.println(licenseurl + " Response :" + response.toString());
        cardReaderInfo.setText("values" + response.toString().substring(1, response.toString().length()));
        //System.out.println(licenseurl + " Response :" + response.toString());
        LOGGER.log(Level.INFO, licenseurl + " Response :" + response.toString());


        //Code to get card reader details
        //Fix the fetched data to the text field 


    }
    
/*
    @FXML
    private void editdetails()throws Exception{
        try (BufferedReader file = new BufferedReader(new FileReader("/etc/data.txt"))) {
            String line = " ";
            String input = " ";
            while((line = file.readLine()) != null){
                String[] tokens = line.split(",");
                //comboUnitId.setValue();
                //UnitId.setText(tokens[0]);
                Url.setText(tokens[1]);
                SerialNO.setText(tokens[2]);
//                UnitId.appendText(tokens[0]);
//                Url.appendText(tokens[1]);
//                SerialNO.appendText(tokens[2]);
                
               // UnitId.getText();
                
                
//            FileWriter file_writer;
//            file_writer = new FileWriter("/home/shubham/data.txt",true);
//            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
//            //buffered_writer.write(tokens[0] +","+tokens[1]+""+tokens[2]);
//            buffered_writer.flush();
//            buffered_writer.close();
            }
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Problem reading file.");
        labelstatus.setText("Could not able to Read the Data, Try Again");

    }
        
    }
      */  
    
    /*
    
       private void ReadData(){
            try {
                BufferedReader file = new BufferedReader(new FileReader("/etc/data.txt"));
                String line;
                String input = "";
                while((line = file.readLine()) != null){
                    if(line.contains(ServerConfigStorage.getUnitID()));
                    {
                        line = "";
                        System.out.println("Line deleted");
                    }
                    input = input + line + '\n';
                }
                FileOutputStream File = new FileOutputStream("/etc/data.txt");
                    File.write(input.getBytes());
                    file.close();
                    File.close();
            } catch (Exception e) {
                System.out.println("Problem reading file.");
                labelstatus.setText("Could not able to Read the Data, Try Again");
            }
        }*/


}


// #522e75