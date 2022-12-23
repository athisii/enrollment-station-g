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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author root
 */
public class DetailLinkBarcodeController implements Initializable {
    @FXML
    WebView webview;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
        ARCDetails a = holder.getArcDetails();
        WebEngine e = webview.getEngine();

        // load a website
        e.load(a.getDetailLink());

        // set font scale for the webview
        webview.setFontScale(1.5f);

        // set zoom
        webview.setZoom(0.8);
    }

    @FXML
    public void showARCDetails() {
        try {
            App.setRoot("barcode_arc");
        } catch (IOException ex) {
            Logger.getLogger(DetailLinkBarcodeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
