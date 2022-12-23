/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.event.ChangeListener;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.service.DirectoryLookup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
public class VerifyAdminPasswdController implements Initializable {
    @FXML
    public Label statusMsg;

    @FXML
    private PasswordField adminpwd;

    @FXML
    private TextField user;

    TestProp prop = new TestProp();

    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public VerifyAdminPasswdController() {
        //this.handler = appLog.getLogger();
        // LOGGER.addHandler(handler);
    }


    @FXML
    public void showHome() {
        try {
            App.setRoot("first_screen");
        } catch (IOException ex) {
            Logger.getLogger(VerifyAdminPasswdController.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "IOException:" + ex);
        }
    }


    @FXML
    public void serverConfig() throws IOException {


        String adminpasswd = null;

        adminpasswd = prop.getProp().getProperty("adminpasswd");
        if (adminpasswd.isBlank() || adminpasswd.isEmpty() || adminpasswd == null) {
            //System.out.println("The property 'adminpasswd' is empty, Please add it in properties");
            statusMsg.setText("The property 'adminpasswd' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'adminpasswd' is empty, Please add it in properties");
            return;
        }
        if (adminpwd.getText().length() == 0 || user.getText().length() == 0) {
            statusMsg.setText("Please provide the admin username/password");
            LOGGER.log(Level.INFO, "Please provide the admin username/password");
        } else {
            if (adminpwd.getText().equals(adminpasswd)) {
                App.setRoot("admin_config");
            } else {
                statusMsg.setText("Password did not match");
                LOGGER.log(Level.INFO, "Password did not match");
            }
        }


        try {
            DirectoryLookup ldap = new DirectoryLookup();
            String status = "";
            //System.out.println("server config ");
            System.out.println("username : pwd : obj : " + user.getText() + " : " + adminpwd.getText());
            LOGGER.log(Level.INFO, "username : pwd : obj : " + user.getText() + " : " + adminpwd.getText());
            if (adminpwd.getText().length() == 0 || user.getText().length() == 0) {
                System.err.println("Length equals zero1");
                status = "Please provide the admin username/password";

            } else {
                status = ldap.doLookup(user.getText(), adminpwd.getText());
            }
            //System.out.println("status : "+status);
            LOGGER.log(Level.INFO, status);
            if (status.contains("true")) {
                App.setRoot("admin_config");
            } else {
                statusMsg.setText(status);
                LOGGER.log(Level.INFO, status);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int maxLength = 15;
        /* add ChangeListner to TextField to restrict the TextField Length*/
        user.textProperty().addListener(new ChangeListener(user, maxLength));
        adminpwd.textProperty().addListener(new ChangeListener(adminpwd, maxLength));

    }
}
    

