/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.model.ARCDetails;
import com.cdac.enrollmentstation.model.ARCDetailsHolder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author boss
 */
public class ShowARCManualController implements Initializable {
    @FXML
    private TextField ArcNo, txtName, txtRank, txtapp, txtUnit, txtFinger, txtiris, txtarcstatus, txtarcno;

    @FXML
    Hyperlink txtDlink;

    @FXML
    private void showFingerPrint() throws IOException {
        App.setRoot("slapscanner");
    }

    @FXML
    private void showARCInput() throws IOException {
        App.setRoot("enrollment_arc");
    }

    @FXML
    private void showDlink() throws IOException {
        App.setRoot("detaillink");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        ARCDetails a = holder.getArcDetails();

        txtName.setText(a.getName());
        txtRank.setText(a.getRank());
        txtapp.setText(a.getApplicantID());
        txtUnit.setText(a.getUnit());
        txtFinger.setText(a.getFingers().toString());
        txtiris.setText(a.getIris().toString());
        txtDlink.setText(a.getDetailLink());
        txtarcstatus.setText(a.getArcStatus());
        txtarcno.setText(a.getArcNo());

    }

}
