/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.APIServerCheck;
import com.cdac.enrollmentstation.model.ARCDetails;
import com.cdac.enrollmentstation.model.ARCDetailsHolder;
import com.cdac.enrollmentstation.model.SaveEnrollmentDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.TouchEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * FXML Controller class
 *
 * @author boss
 */
public class ShowARCDetailsController implements Initializable {
    public ARCDetails arcDetails;

    @FXML
    private TextField txtARCNo;
    @FXML
    private TextField name;
    @FXML
    private TextField rank;
    @FXML
    private TextField appid;
    @FXML
    private TextField unit;
    @FXML
    private TextField finger;
    @FXML
    private TextField iris;
    @FXML
    private TextField arcstatus;
    @FXML
    private TextField arcno;
    @FXML
    private TextField desc;
    public Label Lname;
    public Label Lrank;
    public Label Lappid;
    public Label Lunit;
    public Label Lfinger;
    public Label Liris;
    public Label Ldlink;
    public Label Larcstatus;
    public Label Lerrcode;
    public Label Ldesc;

    @FXML
    Hyperlink dlink;

    @FXML
    Button barcode_arc_next;

    public APIServerCheck apiServerCheck = new APIServerCheck();

    @FXML
    private Label lblStatus;

    @FXML
    private void showFingerPrint() throws IOException {
        App.setRoot("slapscanner");
    }

    @FXML
    private void showARCInput() throws IOException {
        App.setRoot("second_screen");
    }

    @FXML
    private void showPopup() throws IOException {
        System.err.println("text changed !!");
    }


    public void fetchARCDetails() throws Exception {

        try {
            String arcNo = txtARCNo.getText();
            String connurl = apiServerCheck.getARCURL();
            String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl, arcNo);
            System.out.println("connection status :" + connectionStatus);
            if (!connectionStatus.contentEquals("connected")) {
                lblStatus.setText(connectionStatus);
            } else {
                URL url = new URL(connurl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                String jsonInputString = "{\"ARCNo\": \"" + arcNo + "\"}";
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = br.lines().collect(Collectors.joining());
                    System.out.println(response.toString());
                    ObjectMapper objMapper = new ObjectMapper();
                    arcDetails = objMapper.readValue(response.toString(), ARCDetails.class);
                    System.out.println(arcDetails.toString());
        
        /*App app = new App();
        app.setARCDetails(arcDetails);*/

                    // holder.setIrisController(null);


                }
                if (Integer.parseInt(arcDetails.getErrorCode()) == 0) {
                    ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
                    holder.setArcDetails(arcDetails);

                    SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
                    saveEnrollment.setArcNo(txtARCNo.getText());
                    saveEnrollment.setEnrollmentStationID(apiServerCheck.getStationID());
                    saveEnrollment.setEnrollmentStationUnitID(apiServerCheck.getUnitID());
                    saveEnrollment.setFp(new HashSet<>());
                    saveEnrollment.setIris(new HashSet<>());
                    saveEnrollment.setEnrollmentStatus("ARC Details Fetched");
                    holder.setSaveEnrollmentDetails(saveEnrollment);

                } else {
                    lblStatus.setText("Error in retriving details. Please try again");
                }

            }


        } catch (Exception e) {
            System.out.println(e);
        }
        barcode_arc_next.setDisable(false);
    }

    @FXML
    private void showDlink() throws IOException {
        App.setRoot("detaillinkbarcode");
    }

    @FXML
    public void showARCDetails() {
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        ARCDetails a = holder.getArcDetails();

        name.setText(a.getName());
        rank.setText(a.getRank());
        appid.setText(a.getApplicantID());
        unit.setText(a.getUnit());
        finger.setText(a.getFingers().toString());
        iris.setText(a.getIris().toString());
        dlink.setText(a.getDetailLink());
        arcstatus.setText(a.getArcStatus());
        arcno.setText(a.getArcNo());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        dlink.setVisible(false);
        ChangeListener<Scene> sceneListener = new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
                if (newValue != null) {
                    txtARCNo.requestFocus();
                    txtARCNo.sceneProperty().removeListener(this);
                }
            }
        };
        txtARCNo.sceneProperty().addListener(sceneListener);

        txtARCNo.setOnTouchPressed(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {

                event.consume();
            }
        });
        txtARCNo.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {

                try {
                    fetchARCDetails();
                    name.setVisible(true);
                    rank.setVisible(true);
                    appid.setVisible(true);
                    unit.setVisible(true);
                    finger.setVisible(true);
                    iris.setVisible(true);
                    dlink.setVisible(true);

                    arcno.setVisible(true);
                    arcstatus.setVisible(true);
                    Lname.setVisible(true);
                    Lrank.setVisible(true);
                    Lappid.setVisible(true);
                    Lunit.setVisible(true);
                    Lfinger.setVisible(true);
                    Liris.setVisible(true);
                    Ldlink.setVisible(true);

                    Lerrcode.setVisible(true);
                    Larcstatus.setVisible(true);
                    name.setText(arcDetails.getName());
                    rank.setText(arcDetails.getRank());
                    appid.setText(arcDetails.getApplicantID());
                    unit.setText(arcDetails.getUnit());
                    finger.setText(arcDetails.getFingers().toString());
                    iris.setText(arcDetails.getIris().toString());
                    dlink.setText(arcDetails.getDetailLink());

                    arcno.setText(arcDetails.getArcNo());
                    arcstatus.setText(arcDetails.getArcStatus());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        ARCDetails a = holder.getArcDetails();
        dlink.setVisible(true);
        if (a != null) {

            txtARCNo.setText(a.getArcNo());
            name.setText(a.getName());
            rank.setText(a.getRank());
            appid.setText(a.getApplicantID());
            unit.setText(a.getUnit());
            finger.setText(a.getFingers().toString());
            iris.setText(a.getIris().toString());
            dlink.setText(a.getDetailLink());
            arcstatus.setText(a.getArcStatus());
            arcno.setText(a.getArcNo());
        }

//        txtARCNo.setOnKeyPressed(new EventHandler<KeyEvent>() {
//    public void handle(KeyEvent ke) { 
//           try{
//            fetchARCDetails();
//        name.setVisible(true);
//        rank.setVisible(true);
//        appid.setVisible(true);
//        unit.setVisible(true);
//        finger.setVisible(true);
//        iris.setVisible(true);
//        dlink.setVisible(true);
//       
//        arcno.setVisible(true);
//        arcstatus.setVisible(true);
//        Lname.setVisible(true);
//        Lrank.setVisible(true);
//        Lappid.setVisible(true);
//        Lunit.setVisible(true);
//        Lfinger.setVisible(true);
//        Liris.setVisible(true);
//        Ldlink.setVisible(true);
//       
//        Lerrcode.setVisible(true);
//        Larcstatus.setVisible(true);
//         name.setText(arcDetails.getName());
//        rank.setText(arcDetails.getRank());
//        appid.setText(arcDetails.getApplicantID());
//        unit.setText(arcDetails.getUnit());
//        finger.setText(arcDetails.getFingers().toString());
//        iris.setText(arcDetails.getIris().toString());
//        dlink.setText(arcDetails.getDetailLink());
//        
//        arcno.setText(arcDetails.getArcNo());
//        arcstatus.setText(arcDetails.getArcstatus());
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//            
//        
//    }
//    });
    }


}
