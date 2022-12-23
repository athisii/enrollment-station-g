///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cdac.enrollmentstation.controller;
//
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.api.CardReaderAPI;
//import com.cdac.enrollmentstation.api.CardReaderAPIURLs;
//import com.cdac.enrollmentstation.model.*;
//import com.cdac.enrollmentstation.security.HextoASNFormat;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//
//import javax.xml.bind.DatatypeConverter;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.ResourceBundle;
//
//
///**
// * FXML Controller class
// *
// * @author root
// */
//public class ShowTokenController_280522 implements Initializable {
//
//    /**
//     * Initializes the controller class.
//     */
//
//    public
//    @FXML Button showContractDetails;
//
//    public
//    @FXML Button show_home_token;
//
//    public
//    @FXML Label lblcarderror;
//
//    com.cdac.enrollmentstation.api.CardReaderAPIURLs CardReaderAPIURLs = new CardReaderAPIURLs();
//
//    CardReaderAPI cardReaderAPI = new CardReaderAPI();
//
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//        // TODO
//    }
//
//    @FXML
//    private void showHome() throws IOException {
//        App.setRoot("first_screen");
//
//    }
//
//    @FXML
//    public void readCardDetails() throws MalformedURLException, IOException, IllegalStateException {
//
//        String MantraCardReader = "Mantra Reader (1.00) 00 00";
//        String ACSCardReader = "ACS ACR1281 1S Dual Reader 00 01";
//
//        String[] listofreaders = cardReaderAPI.listofreaders();
//        if (listofreaders == null) {
//            lblcarderror.setText("Kindly Check the Card Service running or not");
//            return;
//        }
//
//        if (listofreaders.length < 2) {
//            lblcarderror.setText("Kindly Check the Both Card readers connected");
//            return;
//        }
//        //System.out.println("List of readers is :\n0."+listofreaders[0]+"\n1. "+listofreaders[1]);
//
//
//        /*List<String> splitedreaderslist = Arrays.asList(listofreaders.split(",", -1));
//             for(int i=0;i<splitedreaderslist.size();i++)
//                 System.out.println("\n"+i+". "+splitedreaderslist.get(i));*/
//
//             /*System.out.println( "  Response :" + splitedreaderslist.get(0).replace(',', '\n'));
//             String a = listofreaders;
//             String b = a.substring(1, a.length()-1);
//             List<String> listofreadersArray = new ArrayList<String>(Arrays.asList(b.split(",", -1)));
//             System.out.println("New test \n"+a+"\nNew rest"+b);
//
//
//             for(int i=0;i<listofreadersArray.size();i++){
//                 String str = listofreadersArray.get(i);
//                  String str1=str.replace("\"", "");
//                 listofreadersArray.set(i, str1);
//                 System.out.println("Readers Lists:::"+listofreadersArray.get(i));
//             }
//
//          //To check the List of card readers available
//        if(listofreadersArray.get(0).equals("")){
//         lblcarderror.setText("Kindly Check the List of Cardreaders");
//          return;
//         } */
//
//        String dintializeresponse = cardReaderAPI.deInitialize();
//        if (dintializeresponse.equals("")) {
//            lblcarderror.setText("Kindly Check the CardReader Api Service");
//            return;
//        }
//        String response = cardReaderAPI.initialize();
//        if (response.equals("")) {
//            lblcarderror.setText("Kindly Check the CardReader Api Service");
//            return;
//        }
//        ObjectMapper objMapper = new ObjectMapper();
//        CardReaderInitialize cardReaderInitialize = objMapper.readValue(response, CardReaderInitialize.class);
//        System.out.println("card init" + cardReaderInitialize.toString());
//        if (cardReaderInitialize.getRetVal() == 0) {
//            String waitConnStatus = cardReaderAPI.getWaitConnectStatus(listofreaders[0].trim());
//            //String waitConnStatus = cardReaderAPI.getWaitConnectStatus(ACSCardReader);
//            //String waitConnStatus = cardReaderAPI.getWaitConnectStatus(MantraCardReader);
//            System.out.println("connection status :" + waitConnStatus);
//            if (!waitConnStatus.contentEquals("connected")) {
//                //lblcarderror.setText(connectionStatus);
//                lblcarderror.setText("Kindly Check the CardReader Api Service");
//                ;
//                return;
//            } else {
//                String responseWaitConnect = cardReaderAPI.getWaitConnect(listofreaders[0].trim());
//                //String responseWaitConnect = cardReaderAPI.getWaitConnect(ACSCardReader);
//                //String responseWaitConnect = cardReaderAPI.getWaitConnect(MantraCardReader);
//                System.out.println("response Wait For Connect " + responseWaitConnect);
//
//                ObjectMapper objMapperWaitConn = new ObjectMapper();
//                CardReaderWaitForConnect waitForConnect = objMapperWaitConn.readValue(responseWaitConnect, CardReaderWaitForConnect.class);
//                if (waitForConnect.getRetVal() == 0) {
//                    System.out.println("Wait for conect succes");
//
//                    //Get CSN and handle Value
//                    //base 64 encoded bytes
//                    String csnValue = waitForConnect.getCsn();
//                    int handleValue = waitForConnect.getHandle();
//                    HextoASNFormat hextoasn = new HextoASNFormat();
//                    String decodedCsnValue = hextoasn.getDecodedCSN(csnValue);
//                    System.out.println("Decoded Csn Value::::" + decodedCsnValue);
//                    System.out.println("CSN Value::::" + csnValue);
//                    System.out.println("Handle Value::::" + handleValue);
//
//                    //Naval ID/Contractor Card value is 4 , For Token the value is 5
//                    byte[] cardtype = {4};
//
//                    String responseSelectApp = cardReaderAPI.getSelectApp(cardtype, handleValue);
//                    ObjectMapper objMapperSelectApp = new ObjectMapper();
//                    CardReaderSelectApp selectApp = objMapperSelectApp.readValue(responseSelectApp, CardReaderSelectApp.class);
//
//                    if (selectApp.getRetVal() == 0) {
//                        System.out.println("Select App Connect succes");
//
//                        //Card Reading
//                        byte[] whichdata = {21}; //static data
//                        int offset = 0;
//                        //int reqlength = 122;
//                        //int addlength = 122;
//                        int reqlength = 1024;
//                        int addlength = 1024;
//                        ArrayList<String> responseReadDataFromNavalcard = new ArrayList<String>();
//                        for (int i = 0; i <= 2; i++) {
//                            // System.out.println("Inside For Loop");
//                            responseReadDataFromNavalcard.add(cardReaderAPI.readDataFromNaval(handleValue, whichdata, offset, reqlength));
//                            offset = offset + addlength;
//                        }
//
//                        ArrayList<String> responseString = new ArrayList<String>();
//                        //String decodedResponseString = "";
//                        StringBuffer decodedResponseString = new StringBuffer("");
//                        byte[] decodedDatafromNaval;
//                        String decodedStringFromNaval;
//                        ObjectMapper objReadDataFromNaval = new ObjectMapper();
//                        CardReaderReadData readDataFromNaval = new CardReaderReadData();
//                        for (String responseReadDataArray : responseReadDataFromNavalcard) {
//                            System.out.println(responseReadDataArray);
//                            //Commented on 270522 for code review
//                            //ObjectMapper objReadDataFromNaval = new ObjectMapper();
//                            //CardReaderReadData readDataFromNaval = objReadDataFromNaval.readValue(responseReadDataArray, CardReaderReadData.class);
//                            readDataFromNaval = objReadDataFromNaval.readValue(responseReadDataArray, CardReaderReadData.class);
//
//                            if (readDataFromNaval.getRetVal() == 0) {
//                                System.out.println("Read Data from card Connect succes");
//                                //Decode Data from Naval Card
//                                decodedDatafromNaval = Base64.getDecoder().decode(readDataFromNaval.getResponse());
//
//                                decodedStringFromNaval = DatatypeConverter.printHexBinary(decodedDatafromNaval);
//                                //decodedResponseString = decodedResponseString+decodedStringFromNaval;
//                                decodedResponseString.append(decodedStringFromNaval);
//                            } else {
//                                System.out.println("Read Data from card Connect Failure");
//                                lblcarderror.setText("Error While Reading the Card, Try with other Card");
//                            }
//
//                        }
//                        System.out.println("DECODED RESPONSE STRING::::" + decodedResponseString);
//                        String contractorId = hextoasn.getContractorIdfromASN(decodedResponseString.toString());
//                        String contractorName = hextoasn.getContractorNamefromASN(decodedResponseString.toString());
//                        System.out.println("Contractor ID:::::" + contractorId);
//
//                        //Set the Contractor Id and Card Serial Number (CSN)
//                        ContactDetail contactdetail = new ContactDetail();
//                        contactdetail.setContractorId(contractorId.trim());
//                        contactdetail.setContractorName(contractorName);
//                        //contactdetail.setSerial_no(decodedCsnValue.toLowerCase());
//                        contactdetail.setSerialNo(decodedCsnValue);
//                        contactdetail.setCardReaderHandle(handleValue);
//
//                        Details details = Details.getdetails();
//                        details.setContractDetail(contactdetail);
//                        System.out.println("Details from Show Token:::" + contactdetail.getContractorId());
//                        System.out.println("Details from Show Token:::" + contactdetail.getSerialNo());
//
//                        String contractorID = contactdetail.getContractorId();
//                        String serialNo = contactdetail.getSerialNo();
//                        if (contractorID != null && !contractorID.isEmpty() && serialNo != null && !serialNo.isEmpty()) {
//                            App.setRoot("list_contract");
//                        } else {
//                            System.out.println("Contract ID or Serial Number is Null");
//                            lblcarderror.setText("Contract ID or Serial Number is Empty, Try Again with proper Card");
//                            return;
//                        }
//                    } else {
//                        System.out.println("Select App  Failure");
//                        lblcarderror.setText("Kindly Place the Valid Card in the Card Reader and Try Again");
//                        return;
//                    }
//                } else {
//                    System.out.println("Wait for connect Failure");
//                    lblcarderror.setText("Kindly Place the Card in the Card Reader and Try Again");
//                    return;
//                }
//            }
//        } else {
//            System.out.println("Initialize Card Failed");
//            lblcarderror.setText("Initialize Card Failed");
//            String responseDeInitialize = cardReaderAPI.deInitialize();
//            ObjectMapper objMapperDeInitialize = new ObjectMapper();
//            CardReaderDeInitialize cardReaderDeInitialize = objMapperDeInitialize.readValue(responseDeInitialize, CardReaderDeInitialize.class);
//
//            if (cardReaderDeInitialize.getRetVal() == 0) {
//                System.out.println("DeInitialize Card Successfully");
//                lblcarderror.setText("Card DeInitialized");
//            } else {
//                System.out.println("DeInitialize Card Failed");
//                lblcarderror.setText("Card DeInitialized Failed");
//            }
//        }
//    }
//
//}
