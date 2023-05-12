/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.logging.ApplicationLogOld;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author root
 */
public class AdminController implements Initializable {
    @FXML
    public Label statusMsg;

    @FXML
    private AnchorPane confirmPane;

    @FXML
    private Button confirmYesBtn;

    @FXML
    private Button confirmNoBtn;

    @FXML
    private Label confirmpanelabel;

    //For Application Log
    ApplicationLogOld appLog = new ApplicationLogOld();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public AdminController() {
        //this.handler = appLog.getLogger();
        //LOGGER.addHandler(handler); 
    }


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void serverconfig() {
        try {
            App.setRoot("server_config");
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }
    }

    @FXML
    public void licenseInfo() {
        System.out.println("License Info button clicked");
        try {
            App.setRoot("license_info");
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }

    }

    @FXML
    public void devicecheck() {
        try {
            App.setRoot("device_status");
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }
    }

    @FXML
    public void closeApp() {
        System.out.println("Application Close Call made");
        Platform.exit();
        System.out.println("Application Close Call executed");
    }

    @FXML
    public void logOut() {
        try {
            App.setRoot("enterpassword");
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "IOException:");
        }
    }

    @FXML
    public void initialiseintegrity() {
        //System.out.println("initialiseintegrity");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "echo \"true\" | sudo tee /etc/baseline");
            Process process = null;
            try {
                process = processBuilder.start();
                int exitCode = process.waitFor();
                statusMsg.setText("Integrity Check Initialized");
                System.out.println("\nExited with error code : " + exitCode);
                LOGGER.log(Level.INFO, "Integrity Check Initialized");
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO, ex + "IOException:");
            }

        } catch (Exception e) {
            System.out.println("com.cdac.enrollmentStation.AdminController.initialiseintegrity()" + e.getMessage());
            LOGGER.log(Level.INFO, e + "Exception:");
        }
        // System.out.println("initialiseintegrity1");

    }

    @FXML
    public void restartsystem() {

        confirmPane.setVisible(true);

    }

    @FXML
    private void restart() {
        restartSys();
    }

    @FXML
    private void stayBack() {
        System.out.println("inside stay back");
        //backBtn.setDisable(false);
        confirmPane.setVisible(false);

        //showIris.setDisable(false);
        //showCaptureStatus.setDisable(true);

    }

    private void restartSys() {
        System.out.println("restartsystem");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "init 6");
            Process process = null;
            try {
                process = processBuilder.start();
                int exitCode = process.waitFor();
                statusMsg.setText("System Reboot");
                LOGGER.log(Level.INFO, "System Reboot");
                System.out.println("\nExited with error code : " + exitCode);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO, ex + "IOException:");
            }

        } catch (Exception e) {
            //System.out.println("com.cdac.enrollmentStation.AdminController.restartsystem()"+e.getMessage());
            LOGGER.log(Level.INFO, e + "Exception:");
        }
        System.out.println("restartsystem1");

    }


}
