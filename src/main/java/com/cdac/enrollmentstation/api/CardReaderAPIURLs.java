/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.TestProp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author root
 */
public class CardReaderAPIURLs {
    TestProp prop = new TestProp();

    private static final Logger LOGGER = ApplicationLog.getLogger(CardReaderAPIURLs.class);


    public String getInitializeURL() {

        String initialize = null;
        initialize = prop.getProp().getProperty("initialize"); // return "http://localhost:8088/N_Initialize";
        if (initialize.isBlank() || initialize.isEmpty() || initialize == null) {
            //System.out.println("The property 'inputfile' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'inputfile' is empty, Please add it in properties");
            return null;
        }

        return initialize;
    }

    public String getWaitConnect() {
        String waitforconnect = null;
        waitforconnect = prop.getProp().getProperty("waitforconnect"); // return "http://localhost:8088/N_Wait_for_Connect";
        if (waitforconnect.isBlank() || waitforconnect.isEmpty() || waitforconnect == null) {
            //System.out.println("The property 'waitforconnect' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'waitforconnect' is empty, Please add it in properties");
            return null;
        }

        return waitforconnect;

    }

    public String getSelectApp() {
        String selectapp = null;
        selectapp = prop.getProp().getProperty("selectapp"); //  return "http://localhost:8088/N_SelectApp";
        if (selectapp.isBlank() || selectapp.isEmpty() || selectapp == null) {
            //System.out.println("The property 'selectapp' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'selectapp' is empty, Please add it in properties");
            return null;
        }

        return selectapp;

    }

    public String readDataFromNaval() {
        String readdata = null;
        readdata = prop.getProp().getProperty("readdata"); //   return "http://localhost:8088/N_readDatafromNaval";
        if (readdata.isBlank() || readdata.isEmpty() || readdata == null) {
            //System.out.println("The property 'readdata' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'readdata' is empty, Please add it in properties");
            return null;
        }

        return readdata;


    }

    public String storeDataOnNaval() {
        String storedata = null;
        storedata = prop.getProp().getProperty("storedata"); // return "http://localhost:8088/N_storeDataonNaval";
        if (storedata.isBlank() || storedata.isEmpty() || storedata == null) {
            //System.out.println("The property 'storedata' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'storedata' is empty, Please add it in properties");
            return null;
        }

        return storedata;

    }

    public String verifyCertificate() {
        String verifycert = null;
        verifycert = prop.getProp().getProperty("verifycert"); //  return "http://localhost:8088/N_verifyCertificate";
        if (verifycert.isBlank() || verifycert.isEmpty() || verifycert == null) {
            //System.out.println("The property 'verifycert' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'verifycert' is empty, Please add it in properties");
            return null;
        }

        return verifycert;

    }

    public String PKIAuth() {
        String pkiauth = null;
        pkiauth = prop.getProp().getProperty("pkiauth"); //  return "http://localhost:8088/N_PKIAuth";
        if (pkiauth.isBlank() || pkiauth.isEmpty() || pkiauth == null) {
            //System.out.println("The property 'pkiauth' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'pkiauth' is empty, Please add it in properties");
            return null;
        }

        return pkiauth;


    }

    public String cardRemoval() {
        String waitforremoval = null;
        waitforremoval = prop.getProp().getProperty("waitforremoval"); //  return "http://localhost:8088/N_Wait_for_Removal";
        if (waitforremoval.isBlank() || waitforremoval.isEmpty() || waitforremoval == null) {
            //System.out.println("The property 'waitforremoval' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'waitforremoval' is empty, Please add it in properties");
            return null;
        }

        return waitforremoval;

    }

    public String deInitialize() {
        String deinitialize = null;
        deinitialize = prop.getProp().getProperty("deinitialize"); //   return "http://localhost:8088/N_DeInitialize";
        if (deinitialize.isBlank() || deinitialize.isEmpty() || deinitialize == null) {
            //System.out.println("The property 'deinitialize' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'deinitialize' is empty, Please add it in properties");
            return null;
        }

        return deinitialize;


    }

    public String getListofReaders() {
        String listofreaders = null;
        listofreaders = prop.getProp().getProperty("listofreaders"); //   return "http://localhost:8088/listOfReaders";
        if (listofreaders.isBlank() || listofreaders.isEmpty() || listofreaders == null) {
            //System.out.println("The property 'listofreaders' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'listofreaders' is empty, Please add it in properties");
            return null;
        }

        return listofreaders;

    }


}
