package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.CardReaderAPI;
import com.cdac.enrollmentstation.api.CardReaderAPIURLs;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.*;
import com.cdac.enrollmentstation.security.HextoASNFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * FXML Controller class
 *
 * @author root
 */
public class TokenIssuanceController {
    public
    @FXML Button showContractDetails;

    public
    @FXML Button show_home_token;

    public
    @FXML Label lblcarderror;

    com.cdac.enrollmentstation.api.CardReaderAPIURLs CardReaderAPIURLs = new CardReaderAPIURLs();

    CardReaderAPI cardReaderAPI = new CardReaderAPI();


    //For Application Log
    private static final Logger LOGGER = ApplicationLog.getLogger(TokenIssuanceController.class);
    Handler handler;


    @FXML
    private void backBtnAction() throws IOException {
        App.setRoot("main_screen");
    }

    public void messageStatus(String message) {
        lblcarderror.setText(message);
    }


    public void responseStatus(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lblcarderror.setText(message);
            }
        });
    }


    @FXML
    public void readCardDetails() throws MalformedURLException, IOException, IllegalStateException {

        String response = readCard();
        if (response.equals("success")) {
            //lblcarderror.setText("Kindly Check the CardReader Api Service");
            App.setRoot("list_contract");
            return;
        } else {
            messageStatus(response);
        }
    }


    public String readCard() {

        String response = "";
        String MantraCardReader = "Mantra Reader (1.00) 00 00";
        String ACSCardReader = "ACS ACR1281 1S Dual Reader 00 01";

        String dintializeresponse = cardReaderAPI.deInitialize();
        //if (dintializeresponse.equals("")){
        if (dintializeresponse.isEmpty() || dintializeresponse.contains("Exception")) {
            response = "Kindly Check the CardReader Api Service";
            LOGGER.log(Level.INFO, "Kindly Check the CardReader Api Service");
            return response;
        }
        String responseinit = cardReaderAPI.initialize();
        if (responseinit.equals("")) {
            //lblcarderror.setText("Kindly Check the CardReader Api Service");
            response = "Kindly Check the CardReader Api Service";
            LOGGER.log(Level.INFO, "Kindly Check the CardReader Api Service");
            return response;
        }
        ObjectMapper objMapper = new ObjectMapper();
        CardReaderInitialize cardReaderInitialize;
        try {
            cardReaderInitialize = objMapper.readValue(responseinit, CardReaderInitialize.class);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(TokenIssuanceController.class.getName()).log(Level.SEVERE, null, ex);
            response = "JSON Prossessing Error cardReaderInitialize";
            LOGGER.log(Level.INFO, "JSON Prossessing Error cardReaderInitialize");
            return response;

        }
        System.out.println("card init" + cardReaderInitialize.toString());
        if (cardReaderInitialize.getRetVal() == 0) {
            //String waitConnStatus = cardReaderAPI.getWaitConnectStatus(listofreaders[0].trim());
            //String waitConnStatus = cardReaderAPI.getWaitConnectStatus(ACSCardReader);
            String waitConnStatus = cardReaderAPI.getWaitConnectStatus(MantraCardReader);
            System.out.println("connection status :" + waitConnStatus);
            if (!waitConnStatus.contentEquals("connected")) {
                //lblcarderror.setText(connectionStatus);
                response = "Kindly Check the CardReader Api Service";
                LOGGER.log(Level.INFO, "Kindly Check the CardReader Api Service");
                return response;
                //lblcarderror.setText("Kindly Check the CardReader Api Service");;
                //return;
            } else {
                //String responseWaitConnect = cardReaderAPI.getWaitConnect(listofreaders[0].trim());
                //String responseWaitConnect = cardReaderAPI.getWaitConnect(ACSCardReader);
                String responseWaitConnect = cardReaderAPI.getWaitConnect(MantraCardReader);
                System.out.println("response Wait For Connect " + responseWaitConnect);

                ObjectMapper objMapperWaitConn = new ObjectMapper();
                CardReaderWaitForConnect waitForConnect;
                try {
                    waitForConnect = objMapperWaitConn.readValue(responseWaitConnect, CardReaderWaitForConnect.class);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(TokenIssuanceController.class.getName()).log(Level.SEVERE, null, ex);
                    response = "JSON Prossessing Error CardReaderWaitforConnect";
                    LOGGER.log(Level.INFO, "JSON Prossessing Error CardReaderWaitforConnect");
                    return response;
                }
                if (waitForConnect.getRetVal() == 0) {
                    System.out.println("Wait for conect succes");

                    //Get CSN and handle Value
                    //base 64 encoded bytes
                    String csnValue = waitForConnect.getCsn();
                    int handleValue = waitForConnect.getHandle();
                    HextoASNFormat hextoasn = new HextoASNFormat();
                    String decodedCsnValue = hextoasn.getDecodedCSN(csnValue);
                    System.out.println("Decoded Csn Value::::" + decodedCsnValue);
                    System.out.println("CSN Value::::" + csnValue);
                    System.out.println("Handle Value::::" + handleValue);

                    //Naval ID/Contractor Card value is 4 , For Token the value is 5
                    byte[] cardtype = {4};

                    String responseSelectApp = cardReaderAPI.getSelectApp(cardtype, handleValue);
                    ObjectMapper objMapperSelectApp = new ObjectMapper();
                    CardReaderSelectApp selectApp;
                    try {
                        selectApp = objMapperSelectApp.readValue(responseSelectApp, CardReaderSelectApp.class);
                    } catch (JsonProcessingException ex) {
                        Logger.getLogger(TokenIssuanceController.class.getName()).log(Level.SEVERE, null, ex);
                        response = "JSON Prossessing Error CardReaderSelectApp";
                        LOGGER.log(Level.INFO, "JSON Prossessing Error CardReaderSelectApp");
                        return response;
                    }

                    if (selectApp.getRetVal() == 0) {
                        System.out.println("Select App Connect succes");

                        //Card Reading
                        byte[] whichdata = {21}; //static data
                        int offset = 0;
                        //int reqlength = 122;
                        //int addlength = 122;
                        int reqlength = 1024;
                        int addlength = 1024;
                        ArrayList<String> responseReadDataFromNavalcard = new ArrayList<String>();
                        for (int i = 0; i <= 2; i++) {
                            // System.out.println("Inside For Loop");
                            responseReadDataFromNavalcard.add(cardReaderAPI.readDataFromNaval(handleValue, whichdata, offset, reqlength));
                            offset = offset + addlength;
                        }

                        ArrayList<String> responseString = new ArrayList<String>();
                        //String decodedResponseString = "";
                        StringBuffer decodedResponseString = new StringBuffer("");
                        byte[] decodedDatafromNaval;
                        String decodedStringFromNaval;
                        ObjectMapper objReadDataFromNaval = new ObjectMapper();
                        CardReaderReadData readDataFromNaval = new CardReaderReadData();
                        for (String responseReadDataArray : responseReadDataFromNavalcard) {
                            System.out.println(responseReadDataArray);
                            try {
                                readDataFromNaval = objReadDataFromNaval.readValue(responseReadDataArray, CardReaderReadData.class);
                            } catch (JsonProcessingException ex) {
                                Logger.getLogger(TokenIssuanceController.class.getName()).log(Level.SEVERE, null, ex);
                                response = "JSON Prossessing Error CardReaderReadData";
                                LOGGER.log(Level.INFO, "JSON Prossessing Error CardReaderReadData");
                                return response;
                            }

                            if (readDataFromNaval.getRetVal() == 0) {
                                System.out.println("Read Data from card Connect succes");
                                //Decode Data from Naval Card
                                decodedDatafromNaval = Base64.getDecoder().decode(readDataFromNaval.getResponse());

                                decodedStringFromNaval = DatatypeConverter.printHexBinary(decodedDatafromNaval);
                                decodedResponseString.append(decodedStringFromNaval);
                            } else {
                                System.out.println("Read Data from card Connect Failure");
                                LOGGER.log(Level.INFO, "Read Data from card Connect Failure");
                            }

                        }
                        String contractorId = "";
                        String contractorName = "";
                        if (decodedResponseString.length() > 0) {
                            System.out.println("DECODED RESPONSE STRING::::" + decodedResponseString);
                            contractorId = hextoasn.getContractorIdfromASN(decodedResponseString.toString());
                            contractorName = hextoasn.getContractorNamefromASN(decodedResponseString.toString());
                            System.out.println("Contractor ID:::::" + contractorId);
                        } else {
                            response = "Error While Reading the Card, Try with other Card";
                            LOGGER.log(Level.INFO, "Error While Reading the Card, Try with other Card");
                            return response;
                        }

                        //Set the Contractor Id and Card Serial Number (CSN)
                        ContractorInfo contractorInfo = new ContractorInfo();
                        contractorInfo.setContractorId(contractorId.trim());
                        contractorInfo.setContractorName(contractorName);
                        //contactdetail.setSerial_no(decodedCsnValue.toLowerCase());
                        contractorInfo.setSerialNo(decodedCsnValue);
                        contractorInfo.setCardReaderHandle(handleValue);

                        DetailsHolder detailsHolder = DetailsHolder.getdetailsHolder();
                        detailsHolder.setContractorInfo(contractorInfo);
                        System.out.println("Details from Show Token:::" + contractorInfo.getContractorId());
                        System.out.println("Details from Show Token:::" + contractorInfo.getSerialNo());

                        String contractorID = contractorInfo.getContractorId();
                        String serialNo = contractorInfo.getSerialNo();
                        if (contractorID != null && !contractorID.isEmpty() && serialNo != null && !serialNo.isEmpty()) {
                            //App.setRoot("list_contract");
                            response = "success";
                            return response;
                        } else {
                            System.out.println("Contract ID or Serial Number is Null");
                            //lblcarderror.setText("Contract ID or Serial Number is Empty, Try Again with proper Card");
                            response = "Contract ID or Serial Number is Empty, Try Again with proper Card";
                            LOGGER.log(Level.INFO, "Contract ID or Serial Number is Empty, Try Again with proper Card");
                            return response;
                            //return;
                        }
                    } else {
                        System.out.println("Select App  Failure");
                        response = "Kindly Place the Valid Card in the Card Reader and Try Again";
                        LOGGER.log(Level.INFO, "Kindly Place the Valid Card in the Card Reader and Try Again");
                        return response;
                        //lblcarderror.setText("Kindly Place the Valid Card in the Card Reader and Try Again");
                        //return;
                    }
                } else {
                    System.out.println("Wait for connect Failure");
                    response = "Kindly Place the Valid Card in the Card Reader and Try Again";
                    LOGGER.log(Level.INFO, "Kindly Place the Valid Card in the Card Reader and Try Again");
                    return response;
                    //lblcarderror.setText("Kindly Place the Card in the Card Reader and Try Again");
                    //return;
                }
            }
        } else {
            System.out.println("Initialize Card Failed");
            LOGGER.log(Level.INFO, "Initialize Card Failed");
            //lblcarderror.setText("Initialize Card Failed");
            response = "Initialize Card Failed";

            String responseDeInitialize = cardReaderAPI.deInitialize();
            ObjectMapper objMapperDeInitialize = new ObjectMapper();
            CardReaderDeInitialize cardReaderDeInitialize = new CardReaderDeInitialize();
            try {
                cardReaderDeInitialize = objMapperDeInitialize.readValue(responseDeInitialize, CardReaderDeInitialize.class);
            } catch (JsonProcessingException ex) {
                Logger.getLogger(TokenIssuanceController.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (cardReaderDeInitialize.getRetVal() == 0) {
                System.out.println("DeInitialize Card Successfully");
                LOGGER.log(Level.INFO, "DeInitialize Card Successfully");
                //lblcarderror.setText("Card DeInitialized");
                response = "Card DeInitialized";
                return response;
            } else {
                System.out.println("DeInitialize Card Failed");
                LOGGER.log(Level.INFO, "DeInitialize Card Failed");
                //lblcarderror.setText("Card DeInitialized Failed");
                response = "Card DeInitialized Failed";
                return response;
            }
        }
    }

}
