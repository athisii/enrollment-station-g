package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.api.CardReaderAPI;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.*;
import com.cdac.enrollmentstation.security.ASNtoHexFormat;
import com.cdac.enrollmentstation.security.HextoASNFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author padmanabhanj
 */
public class CardWrite {

    CardReaderAPI cardReaderAPI = new CardReaderAPI();

    private static final Logger LOGGER = ApplicationLog.getLogger(CardWrite.class);


    public String cardWriteDeatils() {
        String response = "";
        //String MantraCardReader2 = "Mantra Reader (1.00) 00 01";
        String MantraCardReader2 = "Mantra Reader (1.00) 01 00";
        String ACSCardReader2 = "ACS ACR1281 1S Dual Reader 01 01";

        //String[] listofreaders = cardReaderAPI.listofreaders();
        //LOGGER.log(Level.INFO, "Readers List:::"+listofreaders[1].trim());
        //System.out.println("REaders List:::"+listofreaders[1].trim());
        //System.out.println("REaders List2:::"+MantraCardReader2);
        /*
        if(listofreaders == null ){
            response = "Kindly Check the Card Service running or not";
            LOGGER.log(Level.INFO, "Kindly Check the Card Service running or not");
            //lblcarderror.setText("Kindly Check the Card Service running or not");
            return response;
        }

        if(listofreaders.length < 2 ){
            response = "Kindly Check Both Card readers connected";
            //lblcarderror.setText("Kindly Check the Both Card readers connected");
            return response;
        }*/


        //String waitConnStatus = cardReaderAPI.getWaitConnectStatus(listofreaders[1].trim());
        String waitConnStatus = cardReaderAPI.getWaitConnectStatus(MantraCardReader2);
        //String waitConnStatus = cardReaderAPI.getWaitConnectStatus(ACSCardReader2);
        //System.out.println("connection status :"+waitConnStatus);
        LOGGER.log(Level.INFO, "connection status :", waitConnStatus);
        if (!waitConnStatus.contentEquals("connected")) {
            // lblStatus.setText(connectionStatus);
            //System.out.println("Wait Status Connection Error");
            LOGGER.log(Level.INFO, "Wait Status Connection Error");
            response = "failure";
        } else {
            CardReaderWaitForConnect waitForConnect = new CardReaderWaitForConnect();
            String responseWaitConnect = null;
            ObjectMapper objMapperWaitConn = new ObjectMapper();
            for (int i = 0; i <= 10; i++) {
                //responseWaitConnect = cardReaderAPI.getWaitConnect(listofreaders[1].trim());
                responseWaitConnect = cardReaderAPI.getWaitConnect(MantraCardReader2);
                //responseWaitConnect = cardReaderAPI.getWaitConnect(ACSCardReader2);
                //System.out.println("response Wait For Connect "+responseWaitConnect);
                LOGGER.log(Level.INFO, "response Wait For Connect " + responseWaitConnect);

                try {
                    Thread.sleep(2000);
                    System.out.println("Sleep 2 seconds");
                } catch (InterruptedException ex) {
                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                    LOGGER.log(Level.INFO, "InterruptedException :" + ex);
                }
                try {
                    //Commented For codereview on 270522
                    //ObjectMapper objMapperWaitConn = new ObjectMapper();
                    waitForConnect = objMapperWaitConn.readValue(responseWaitConnect, CardReaderWaitForConnect.class);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                    LOGGER.log(Level.INFO, "JsonProcessingException :" + ex);
                }
                if (waitForConnect.getRetVal() == 0) {
                    break;
                }
            }

            if (waitForConnect.getRetVal() == 0) {
                //System.out.println("Wait for connect succes");
                LOGGER.log(Level.INFO, "Wait for connect succes ");
                //Get CSN and handle Value
                //base 64 encoded bytes
                String csnValue = waitForConnect.getCsn();
                int handleValue = waitForConnect.getHandle();

                //Naval ID Card value is 4 , For Token the value is 5
                byte[] cardtype = {5};

                // To get the Select App
                String responseSelectApp = cardReaderAPI.getSelectApp(cardtype, handleValue);
                ObjectMapper objMapperSelectApp = new ObjectMapper();
                CardReaderSelectApp selectApp = null;
                try {
                    selectApp = objMapperSelectApp.readValue(responseSelectApp, CardReaderSelectApp.class);
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                    LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                }

                if (selectApp.getRetVal() == 0) {
                    //System.out.println("Select App Connect succes");
                    LOGGER.log(Level.INFO, "Select App Connect succes");

                    //Read System certificate from Naval Card

                    //Card Reading
                    byte[] whichdatacert = {32}; //32 for reading certificate
                    int offsetcert = 0;
                    int reqlengthcert = 1024;
                    int addlengthcert = 1024;
                    int finallength = 729;
                    //int finallength = 1024;

                    ArrayList<String> responseReadDataFromNavalcardcert = new ArrayList<String>();
                    /*
                    for(int i=0;i<=14;i++){
                        // System.out.println("Inside For Loop");
                        if (i==14){
                           responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(handleValue, whichdatacert, offsetcert, finallength));
                           break;
                        }
                        responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(handleValue, whichdatacert, offsetcert, reqlengthcert));
                        offsetcert = offsetcert+addlengthcert;
                    }*/
                    // To get the Read Card/ Navy Card Handle
                    //To be uncommented for handle from card read
                    DetailsHolder detailsHolder = DetailsHolder.getDetailsHolder();
                    int cardReadhandleValue = detailsHolder.getContractorInfo().getCardReaderHandle();

                    for (int i = 0; i <= 1; i++) {
                        if (i == 1) {
                            //responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(handleValue, whichdatacert, offsetcert, finallength));
                            //Read Certificate from the Card Reader

                            //responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, finallength));
                            responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, finallength));

                            break;
                        }
                        //responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, reqlengthcert));
                        //Read Certificate from the Card Reader
                        //responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, reqlengthcert));
                        responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, reqlengthcert));

                        offsetcert = offsetcert + addlengthcert;
                    }
                    //responseReadDataFromNavalcardcert
                    //System.out.println("READ DATA FORM CARD READER::"+responseReadDataFromNavalcardcert);
                    LOGGER.log(Level.INFO, "READ DATA FORM CARD READER::" + responseReadDataFromNavalcardcert);

   /*

                    while(true){
                           ObjectMapper objReadDataFromNaval1 = new ObjectMapper();
                           CardReaderReadData readValue = objReadDataFromNaval1.readValue(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, reqlengthcert), CardReaderReadData.class);
                           System.out.println("Naval Response::"+readValue.responseLen);
                           if(readValue.responseLen < 1024){
                               responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, readValue.responseLen));
                               System.out.println("READ NAVAL CERT in 729");
                               break;
                           }
                            //responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, reqlengthcert));
                              //Read Certificate from the Card Reader
                              responseReadDataFromNavalcardcert.add(cardReaderAPI.readDataFromNaval(cardReadhandleValue, whichdatacert, offsetcert, reqlengthcert));
                              System.out.println("READ NAVAL CERT");
                              offsetcert = offsetcert+addlengthcert;
                    }
 */


                    //String decodedResponseStringforCert = "";
                    StringBuffer decodedResponseStringforCert = new StringBuffer("");
                    byte[] decodedDatafromNaval;
                    String decodedStringFromNavalcert;
                    String responseForRemoval;

                    ObjectMapper objReadDataFromNaval = new ObjectMapper();
                    CardReaderReadData readDataFromNavalcert = new CardReaderReadData();
                    ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                    CardReaderWaitForRemoval waitForRemoval = new CardReaderWaitForRemoval();
                    for (String responseReadDataArray : responseReadDataFromNavalcardcert) {
                        System.out.println(responseReadDataArray);
                        try {
                            //Changed on 270522 for code review
                            //ObjectMapper objReadDataFromNaval = new ObjectMapper();
                            //CardReaderReadData readDataFromNavalcert = objReadDataFromNaval.readValue(responseReadDataArray, CardReaderReadData.class);
                            readDataFromNavalcert = objReadDataFromNaval.readValue(responseReadDataArray, CardReaderReadData.class);
                        } catch (JsonProcessingException ex) {
                            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                            LOGGER.log(Level.INFO, "JsonProcessingException::" + ex);
                        }

                        if (readDataFromNavalcert.getRetVal() == 0) {
                            //System.out.println("Read Data from card Connect succes");
                            LOGGER.log(Level.INFO, "Read Data from card Connect succes");
                            response = "Read Data From Card Success";
                            //Decode Data from Naval Card
                            decodedDatafromNaval = Base64.getDecoder().decode(readDataFromNavalcert.getResponse());
                            System.out.println("DECODED CERT DATA:::" + decodedDatafromNaval);
                            decodedStringFromNavalcert = DatatypeConverter.printHexBinary(decodedDatafromNaval);
                            System.out.println("DECODED CERT DATA Convert to HexBinary::" + decodedStringFromNavalcert);
                            decodedResponseStringforCert.append(decodedStringFromNavalcert);
                        } else {
                            //System.out.println("Read Data from card Connect Failure");
                            LOGGER.log(Level.INFO, "Read Data from card Connect Failure");
                            //lblcarderror.setText("Error While Reading the Card");
                            //response = "Read Data From Card Failure";
                            response = "Failure : Read Data from card Connect Failed";
                            // Wait For Removal Call

                            responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                            //System.out.println("response Wait For Connect "+responseForRemoval);
                            LOGGER.log(Level.INFO, "response Wait For Connect " + responseForRemoval);
                            try {
                                //Changed on 270522 for code review
                                //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                            } catch (JsonProcessingException ex) {
                                Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                LOGGER.log(Level.INFO, "JsonProcessingException" + ex);
                            }

                            if (waitForRemoval.getRetVal() == 0) {
                                //System.out.println("Wait For removal is success");
                                LOGGER.log(Level.INFO, "Wait For removal is success");
                                return response;
                            } else {
                                //System.out.println("Wait For removal is failed");
                                LOGGER.log(Level.INFO, "Wait For removal is failed");
                                return response;
                            }
                        }

                    }

                    //Decoded Response Cert
                    System.out.println("RESPONSE STRING::::" + decodedResponseStringforCert);
                    int lengthofHex = decodedResponseStringforCert.length() / 2;
                    System.out.println("Certificate Length :::" + lengthofHex);

                    //For Testing Purpose
                    //String firstpart="318206D53082034E30820236A003020102020103300D06092A864886F70D01010B0500305E310B300906035504061302494E310B3009060355040813025550310E300C060355040713054E4F494441310D300B060355040A130443444143310B3009060355040B130245533116301406035504030C0D41465341435F524F4F545F4341301E170D3230313032393038323130305A170D3330313033303038323130305A3057310B300906035504061302494E310B3009060355040813025550310E300C060355040713054E4F494441310D300B060355040A130443444143310B3009060355040B13024553310F300D0603550403130642454C2D434130820122300D06092A864886F70D01010105000382010F003082010A0282010100C92AC156073A9A0101B996386A9854243671347BB61CA6F74182900CD4E719051A1B8C81BFCE079E8346E22932CFDDF601D69DBD39597BCA3C91EC080B42D18825F18316C7B807FF473BB1ED1EC19CEF07C16090A95E5871968BE20A44819767CE7FCAF620F2F4D715C92511FE3DD43E0B2432B42B12B17C81F11637CBDE1DD9BEE82BCEB9E12535235867AAFA0E59D74D1E1021DF152DC9B7DC684FB421444511DEE19BD9D4BDC956099D0D36582F8165EDB7366B90F4B18B4057501A25943FA000400E9CDADD595B8061DA11FF3F21889C66151085A6D5FB2828F6C382A472615802076FE2B3A2626424A20BBA38261E63D9AD7A2B19198A59867945DC1C930203010001A31E301C300C0603551D13040530030101FF300C0603551D0F0405030307B780300D06092A864886F70D01010B050003820101005C7E834A312796C96FE8BD8F7F5AD2B415D23A7054243CE0B96DBA6A9A945704972735DABA2CCDBDCDDD5EBBD97362927BB5D272E85BA4FA04AEE9AA847CBD2EDFCE5B4735EF468D9A7682EDA1A9BF3DC60CE2AD3FFF94E03872642417ACDA18F5FD8369526A9F791187C806A098D6F33C446D4A40E7D143AC27D86318DC364E1966E5798C0C766DFBCEDA23DD7E751689072D6C0A474AB8979D0E0A5E0447841CAB152FC1DAE96F64234E5DE9FE4B963769679D81B03BE86674135A3AED073BD8CE65C0D084BA8BE66794DF1B33794F1229093D857363022AE1B9956789E78368AAF42B22EA02E5A1DFD80C5FE2CF4774198E2DE6803A80695D7F88AA01B6553082037F30820267A003020102020132300D06092A864886F70D01010B05003057310B300906035504061302494E310B30090603";
                    //String secondpart="55040813025550310E300C060355040713054E4F494441310D300B060355040A130443444143310B3009060355040B13024553310F300D0603550403130642454C2D4341301E170D3231303931303034353530305A170D3232303931303034353530305A308192310B300906035504061302494E3110300E060355040A13074146534143204F3111300F060355040B13084146534143204F5531493047060355040313403737393936323438313538373246414543424636413834323943314342393045354437323843443743373642433133313946363032333141364230394531423731133011060355040C130A53595354454D4345525430820122300D06092A864886F70D01010105000382010F003082010A02820101008E2AD3907C6F31EC4D7B1785757CAD4511CE0692A509B82B6CA0F3643CAE690271080CF67B6D27EE8EB21830B202EA4C75ED9648931640DD04AAAA48B0BB6ABEC81DA5557E03C117B4C3CE7EE6D5DF439A936892BDD9BFF0A9E1E41D94B8A40E1EE42CA8C029AD4ADA716ECA146825E909D8915C1BA45F47EF9E9802CD1F5D4814ADB9AA4A78D09BCA708252D977BE61DE7A0BFD09B91E259F3C5D35462FDC6AAAC0CB5C4F943757E24302247686B8582C033727DC9D0E8025ACBB9AD0D85C09A9163F4CC415C744E116AC3CEA4A8D79416B12B136AE061D77EA7B39AD69C2A051E093618362D388074BA2629816620A452E21C292324DECB1DC36630528A1730203010001A31A301830090603551D1304023000300B0603551D0F0404030204B0300D06092A864886F70D01010B050003820101007B7F6F99247FEF8063E52C61D3CC7CB4B6B3E7662E778350F85FBF0416C24BCA28B340D25E43F75B64D11DE7FB6E1A17A7D6EA7B1054BDE79C21E10BCA61A7CFF8F45EFE0D082BF0C466A2F106D06966D5023073DACC779058DDCFB2D40177E979728D7285F84AEE34E209944A84C06814811102E88A380AA3554FCFAD625D1E636D3D35CF3B0859C51705D8F46C8B9A69B891E06148D03F1C25F82249D7917B94D4A467C97B7BA7C0B5DD8E87CB9B99181B0782C7ECD2B34FA76B13A3479818710B645988C07F7B6AE1F4D5E1894E679AD39C0C861C2EA889AF969FE096108AA72B04957B88B6295B62C07A47BD8696379F147EEF0E67A4FC9BDA1B28429A47";
                    //HextoASNFormat hextoasncert1 = new HextoASNFormat();
                    //String encodedCertChaintfirstpart = hextoasncert1.getEncodedCert(firstpart);
                    //String encodedCertChaintsecondpart = hextoasncert1.getEncodedCert(secondpart);
                    //System.out.println("Encode Cert Chain First Part::::"+encodedCertChaintfirstpart);
                    //System.out.println("Encode Cert Chain Second Part::::"+encodedCertChaintsecondpart);

                    //Encode the Response Cert
                    HextoASNFormat hextoasncert = new HextoASNFormat();
                    String encodedCertChain = hextoasncert.getEncodedCert(decodedResponseStringforCert.toString());

                    System.out.println("Encoded Certificate Chain:::::" + encodedCertChain);

                    //To get the Read Card/ Navy Card Handle
                    //To be uncommented for handle from card read
                    //Details details = Details.getdetails();
                    //int cardReadhandleValue = details.getContractdetail().getReadcard_handle();
                    System.out.println("Card Read Handle value" + cardReadhandleValue);


                    //int cardReadhandleValue = 4937; //uncomment For Manual Verification
                    //Verify the Encoded Certificate
                    byte[] whichtrustcert = {11};
                    byte[] whichcert = {14};
                    //int certificatechainlength = 1753;
                    int certificatechainlength = lengthofHex;
                    //String responseVerifyCert = cardReaderAPI.verifyCertificate(cardReadhandleValue, whichtrustcert, whichcert, encodedCertChain, certificatechainlength);
                    //Card Write Handle
                    String responseVerifyCert = cardReaderAPI.verifyCertificate(handleValue, whichtrustcert, whichcert, encodedCertChain, certificatechainlength);
                    ObjectMapper objMapperVerifyCert = new ObjectMapper();
                    CardReaderVerifyCert verifyCert = null;
                    try {
                        verifyCert = objMapperVerifyCert.readValue(responseVerifyCert, CardReaderVerifyCert.class);
                    } catch (JsonProcessingException ex) {
                        Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                        LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                    }

                    if (verifyCert.getRetVal() == 0) {
                        //System.out.println("Verify Certificate Success");
                        LOGGER.log(Level.INFO, "Verify Certificate Success");

                        //PKI Auth
                        //To Get the card2 handle value

                        //String responsePKIAuth = cardReaderAPI.PKIAuth(cardReadhandleValue, handleValue);
                        String responsePKIAuth = cardReaderAPI.PKIAuth(handleValue, cardReadhandleValue);
                        ObjectMapper objMapperPKIAuth = new ObjectMapper();
                        CardReaderPKIAuth pkiAuth = null;
                        try {
                            pkiAuth = objMapperPKIAuth.readValue(responsePKIAuth, CardReaderPKIAuth.class);
                        } catch (JsonProcessingException ex) {
                            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if (pkiAuth.getRetVal() == 0) {
                            //System.out.println("PKI AUTH Success");
                            LOGGER.log(Level.INFO, "PKI AUTH Success");
                            //Details to write to the card
                            ASNtoHexFormat asntohex = new ASNtoHexFormat();
                            byte[] whichdata_dyn = {36}; //36 for dynamic file
                            byte[] whichdata_dav = {22}; //22 for default access validity
                            byte[] whichdata_sap = {24}; //24 for special access permission
                            byte[] whichdata_fp = {25}; //24 for special access permission
                            byte[] whichdata_photo = {43}; //43 for photo file
                            byte[] whichdata_signature1 = {33}; //35 for signature file3
                            byte[] whichdata_signature3 = {35}; //35 for signature file3
                            int offset = 0;
                            //int reqlength = 122;
                            // To Write Dynamic File

                            byte[] dynamicFileEncodedbytes = null;
                            int reqlength_dyn = 0;
                            Object[] encodedDynamicValuesObject = asntohex.getEncodedDyanamicFile();
                            List<Object> encodedDynamicValuesObjectArray = Arrays.asList(encodedDynamicValuesObject);
//                                                                  System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
//                                                                  System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            dynamicFileEncodedbytes = (byte[]) encodedDynamicValuesObjectArray.get(0);
                            reqlength_dyn = Integer.parseInt(encodedDynamicValuesObjectArray.get(1).toString());
//                                                                  System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
//                                                                  System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64Dynamic = Base64.getEncoder().encodeToString(dynamicFileEncodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            //String dynamicFileResponse = writeTokenDynamicFile(handleValue,whichdata_signature3,offset,encodedbase64Dynamic,reqlength_dyn);
                            String dynamicFileResponse = writeTokenDynamicFile(handleValue, whichdata_dyn, offset, encodedbase64Dynamic, reqlength_dyn);
                            String success_dynamic = "success";
                            System.out.println("Default Access Response::" + dynamicFileResponse);

                            if (dynamicFileResponse.equals(success_dynamic)) {
                                System.out.println("Token Successfully Written for Dynamic File Response");
                                response = "success";
                                // Wait For Removal Call
                                /*
                                String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect "+responseForRemoval);
                                ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                       if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                       else{ System.out.println("Wait For removal is failed");}
                                */
                            } else {
                                System.out.println("Error While writing Token for Dynamic File");
                                response = "Failure : Error While writing Token for Dynamic File";
                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                    LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }
                            }
                            //End of Dynamic File
                            // To Write Default Access File
                            byte[] defaultAccessValidityEncodedbytes = null;
                            int reqlength_dav = 0;
                            Object[] encodedValuesObject = asntohex.getEncodedDefaultAccessValidity();
                            List<Object> encodedValuesObjectArray = Arrays.asList(encodedValuesObject);
                            //                          System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
                            //                          System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            defaultAccessValidityEncodedbytes = (byte[]) encodedValuesObjectArray.get(0);
                            reqlength_dav = Integer.parseInt(encodedValuesObjectArray.get(1).toString());
                            //System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
                            //System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64DAC = Base64.getEncoder().encodeToString(defaultAccessValidityEncodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            //String defaultAccessResponse = writeTokenDefaultAccess(handleValue,whichdata_signature3,offset,encodedbase64DAC,reqlength_dav);
                            String defaultAccessResponse = writeTokenDefaultAccess(handleValue, whichdata_dav, offset, encodedbase64DAC, reqlength_dav);
                            String success_defaultaccess = "success";
                            System.out.println("Default Access Response::" + defaultAccessResponse);

                            if (defaultAccessResponse.equals(success_defaultaccess)) {
                                //System.out.println("Token Successfully Written for Default Access");
                                LOGGER.log(Level.INFO, "Token Successfully Written for Default Access");
                                response = "success";

                                // Wait For Removal Call
                                /*
                                String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect "+responseForRemoval);
                                ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                       if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                       else{ System.out.println("Wait For removal is failed");}*/
                            } else {

                                //System.out.println("Error While writing Token for Default Access File");
                                LOGGER.log(Level.INFO, "Error While writing Token for Default Access File");
                                response = "Failure : Error While writing Token for Default Access File";
                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                    LOGGER.log(Level.INFO, "JsonProcessingException", ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }
                            }
                            //End of Default Access File
                            // To Special Access File
                            byte[] defaultSpecialAccessEncodedbytes = null;
                            int reqlength_sap = 0;
                            Object[] defaultSpecialAccessValuesObject = asntohex.getEncodedSpecialAccessPermission();
                            List<Object> encodeddefaultSpecialAccessValuesObjectArray = Arrays.asList(defaultSpecialAccessValuesObject);
                            //System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
                            //System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            defaultSpecialAccessEncodedbytes = (byte[]) encodeddefaultSpecialAccessValuesObjectArray.get(0);
                            reqlength_sap = Integer.parseInt(encodeddefaultSpecialAccessValuesObjectArray.get(1).toString());
                            //System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
                            //System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64defaultspecialaccess = Base64.getEncoder().encodeToString(defaultSpecialAccessEncodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            //String defaultSpecialAccessResponse = writeTokenSpecialAccess(handleValue,whichdata_signature1,offset,encodedbase64defaultspecialaccess,reqlength_sap);
                            String defaultSpecialAccessResponse = writeTokenSpecialAccess(handleValue, whichdata_sap, offset, encodedbase64defaultspecialaccess, reqlength_sap);
                            String success_defaultSpecialAccess = "success";
                            System.out.println("Default Special Access Response::" + defaultSpecialAccessResponse);

                            if (defaultSpecialAccessResponse.equals(success_defaultSpecialAccess)) {
                                //System.out.println("Token Successfully Written for Special Access");
                                LOGGER.log(Level.INFO, "Token Successfully Written for Special Access");
                                response = "success";
                                // Wait For Removal Call

                                //String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                //System.out.println("response Wait For Connect "+responseForRemoval);
                                //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                //      if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                //     else{ System.out.println("Wait For removal is failed");}
                            } else {
                                //System.out.println("Error While writing Token for Special Access");
                                LOGGER.log(Level.INFO, "Error While writing Token for Special Access");
                                response = "Failure : Error While writing Token for Special Access";
                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }
                            }

                            //End of Default Special Access File
                            // To Write Signature File1
                            byte[] signatureFile1Encodedbytes = null;
                            int reqlength_signature1 = 0;
                            Object[] encodedSignature1ValuesObject = asntohex.getEncodedSignature1();
                            List<Object> encodedSignature1ValuesObjectArray = Arrays.asList(encodedSignature1ValuesObject);
                            //System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
                            //System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            signatureFile1Encodedbytes = (byte[]) encodedSignature1ValuesObjectArray.get(0);
                            reqlength_signature1 = Integer.parseInt(encodedSignature1ValuesObjectArray.get(1).toString());
                            //System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
                            //System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64signature1 = Base64.getEncoder().encodeToString(signatureFile1Encodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            String signature1Response = writeTokenSignature1(handleValue, whichdata_signature1, offset, encodedbase64signature1, reqlength_signature1);
                            String success_signature1 = "success";
                            System.out.println("Signature Response::" + signature1Response);

                            if (signature1Response.equals(success_signature1)) {
                                //System.out.println("Token Successfully Written for Signature1");
                                LOGGER.log(Level.INFO, "Token Successfully Written for Signature1");
                                response = "success";

                                // Wait For Removal Call
                                 /*
                                 String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                 System.out.println("response Wait For Connect "+responseForRemoval);
                                 ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                 CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                        if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                        else{ System.out.println("Wait For removal is failed");}*/
                            } else {

                                //System.out.println("Error While writing Token for Signature1");
                                LOGGER.log(Level.INFO, "Error While writing Token for Signature1");
                                response = "Failure : Error While writing Token for Signature1";
                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                    LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }
                            }

                            //End of Signature File1

                            // To Write Signature File3
                            byte[] signatureFile3Encodedbytes = null;
                            int reqlength_signature3 = 0;
                            Object[] encodedSignature3ValuesObject = asntohex.getEncodedSignature3();
                            List<Object> encodedSignature3ValuesObjectArray = Arrays.asList(encodedSignature3ValuesObject);
                            //System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
                            //System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            signatureFile3Encodedbytes = (byte[]) encodedSignature3ValuesObjectArray.get(0);
                            reqlength_signature3 = Integer.parseInt(encodedSignature3ValuesObjectArray.get(1).toString());
                            //System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
                            //System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64signature3 = Base64.getEncoder().encodeToString(signatureFile3Encodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            String signature3Response = writeTokenSignature3(handleValue, whichdata_signature3, offset, encodedbase64signature3, reqlength_signature3);
                            String success_signature3 = "success";
                            System.out.println("Signature Response::" + signature3Response);

                            if (signature3Response.equals(success_signature3)) {
                                //System.out.println("Token Successfully Written for Signature3");
                                LOGGER.log(Level.INFO, "Token Successfully Written for Signature3");
                                response = "success";

                                // Wait For Removal Call
                                /*
                                String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect "+responseForRemoval);
                                ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                       if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                       else{ System.out.println("Wait For removal is failed");}*/
                            } else {

                                //System.out.println("Error While writing Token for Signature3");
                                LOGGER.log(Level.INFO, "Error While writing Token for Signature3");
                                response = "Failure : Error While writing Token for Signature3";
                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                    LOGGER.log(Level.INFO, "JsonProcessingException" + ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }
                            }

                            //End of Signature File3

                            // Should be Uncommented
                            // To Write Fingerprint
                            byte[] fingerprintEncodedbytes = null;
                            int reqlength_fp = 0;
                            Object[] encodedFingerprintValuesObject = asntohex.getEncodedFingerprintData();
                            List<Object> encodedFingerprintValuesObjectArray = Arrays.asList(encodedFingerprintValuesObject);
//                                                                  System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
//                                                                  System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            fingerprintEncodedbytes = (byte[]) encodedFingerprintValuesObjectArray.get(0);
                            reqlength_fp = Integer.parseInt(encodedFingerprintValuesObjectArray.get(1).toString());
//                                                                  System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
//                                                                  System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64Fingerprint = Base64.getEncoder().encodeToString(fingerprintEncodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            String fingerprintResponse = writeTokenFingerprint(handleValue, whichdata_fp, offset, encodedbase64Fingerprint, reqlength_fp);
                            String success_fingerprint = "success";
                            System.out.println("Fingerprint Response::" + fingerprintResponse);

                            if (fingerprintResponse.equals(success_fingerprint)) {
                                //System.out.println("Token Successfully Written for Fingerprint");
                                LOGGER.log(Level.INFO, "Token Successfully Written for Fingerprint");
                                response = "success";

                                // Wait For Removal Call

                                // String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                // System.out.println("response Wait For Connect "+responseForRemoval);
                                // ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                // CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                //       if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                //      else{ System.out.println("Wait For removal is failed");}
                            } else {
                                LOGGER.log(Level.INFO, "Error While writing Token for Fingerprint");
                                response = "Failure : Error While writing Token for Fingerprint";

                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                    LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }

                            }

                            //End of Fingerprint


                            //Should be Uncommented
                            // To Write Photo File
                            byte[] labourPhotEncodedbytes = null;
                            int reqlength_photo = 0;
                            Object[] encodedPhotoValuesObject = asntohex.getEncodedLabourPhoto();
                            List<Object> encodedPhotoValuesObjectArray = Arrays.asList(encodedPhotoValuesObject);
//                                                                  System.out.println("AS List0"+encodedValuesObjectArray.get(0).toString());
//                                                                  System.out.println("AS List1"+encodedValuesObjectArray.get(1).toString());

                            // Get Encoded Bytes and the Length of the Data
                            labourPhotEncodedbytes = (byte[]) encodedPhotoValuesObjectArray.get(0);
                            reqlength_photo = Integer.parseInt(encodedPhotoValuesObjectArray.get(1).toString());
//                                                                  System.out.println("Encoded String from DA:::"+defaultAccessValidityEncodedbytes);
//                                                                  System.out.println("Required Length:::"+reqlength_dav);

                            //Encode the Data to  Base64
                            String encodedbase64photo = Base64.getEncoder().encodeToString(labourPhotEncodedbytes);
                            //System.out.println("Encoded Card Write Base64 Default Access :::"+encodedbase64DAC);

                            //Write the Data to the Token
                            String labourPhotoResponse = writeTokenPhoto(handleValue, whichdata_photo, offset, encodedbase64photo, reqlength_photo);
                            String success_labourPhoto = "success";
                            System.out.println("Labor Photo Response::" + success_labourPhoto);

                            if (defaultAccessResponse.equals(success_labourPhoto)) {
                                //System.out.println("Token Successfully Written for LabourPhoto");
                                LOGGER.log(Level.INFO, "Token Successfully Written for LabourPhoto");
                                response = "success";
                                // Wait For Removal Call

                                //  String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                //  System.out.println("response Wait For Connect "+responseForRemoval);
                                //  ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                //  CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                //        if(waitForRemoval.retval == 0){ System.out.println("Wait For removal is success");}
                                //        else{ System.out.println("Wait For removal is failed");}
                            } else {

                                LOGGER.log(Level.INFO, "Error While writing Token for Photo");
                                response = "Failure : Error While writing Token for Photo";

                                // Wait For Removal Call

                                responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                                System.out.println("response Wait For Connect " + responseForRemoval);
                                try {
                                    //Changed on 270522 for code review
                                    //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                    //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                    waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                                } catch (JsonProcessingException ex) {
                                    Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                    LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                                }
                                if (waitForRemoval.getRetVal() == 0) {
                                    //System.out.println("Wait For removal is success");
                                    LOGGER.log(Level.INFO, "Wait For removal is success");
                                    return response;
                                } else {
                                    //System.out.println("Wait For removal is failed");
                                    LOGGER.log(Level.INFO, "Wait For removal is failed");
                                    return response;
                                }
                            }

                            //End of Photo File


                            // Final Wait For Removal Call

                            responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                            System.out.println("response Wait For Connect " + responseForRemoval);
                            try {
                                //Changed on 270522 for code review
                                //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                            } catch (JsonProcessingException ex) {
                                Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                            }
                            if (waitForRemoval.getRetVal() == 0) {
                                //System.out.println("Wait For removal is success");
                                LOGGER.log(Level.INFO, "Wait For removal is success");
                                return response;
                            } else {
                                //System.out.println("Wait For removal is failed");
                                LOGGER.log(Level.INFO, "Wait For removal is failed");
                                return response;
                            }


                        } else {
                            //System.out.println("PKI AUTH Failure");
                            LOGGER.log(Level.INFO, "PKI AUTH Failure");
                            response = "Failure : PKI Authentication Failed";

                            // Wait For Removal Call

                            responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                            System.out.println("response Wait For Connect " + responseForRemoval);
                            try {
                                //Changed on 270522 for code review
                                //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                                //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                                waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                            } catch (JsonProcessingException ex) {
                                Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                                LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                            }
                            if (waitForRemoval.getRetVal() == 0) {
                                //System.out.println("Wait For removal is success");
                                LOGGER.log(Level.INFO, "Wait For removal is success");
                                return response;
                            } else {
                                //System.out.println("Wait For removal is failed");
                                LOGGER.log(Level.INFO, "Wait For removal is failed");
                                return response;
                            }
                        }

                    } else {
                        //System.out.println("Verify Certificate Failure");
                        LOGGER.log(Level.INFO, "Verify Certificate Failure");
                        response = "Failure : Verify Certificate Failed";

                        // Wait For Removal Call

                        responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                        System.out.println("response Wait For Connect " + responseForRemoval);
                        try {
                            //Changed on 270522 for code review
                            //ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                            //CardReaderWaitforRemoval waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitforRemoval.class);
                            waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                        } catch (JsonProcessingException ex) {
                            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                        }
                        if (waitForRemoval.getRetVal() == 0) {
                            //System.out.println("Wait For removal is success");
                            LOGGER.log(Level.INFO, "Wait For removal is success");
                            return response;
                        } else {
                            //System.out.println("Wait For removal is failed");
                            LOGGER.log(Level.INFO, "Wait For removal is failed");
                            return response;
                        }
                    }
                } else {
                    //System.out.println("Select App Connect Failure");
                    LOGGER.log(Level.INFO, "Select App Connect Failure");
                    response = "Failure : Select App Connect Failed";

                    // Wait For Removal Call

                    String responseForRemoval = cardReaderAPI.waitForRemoval(handleValue);
                    System.out.println("response Wait For Connect " + responseForRemoval);
                    ObjectMapper objMapperWaitForRemoval = new ObjectMapper();
                    CardReaderWaitForRemoval waitForRemoval = null;
                    try {
                        waitForRemoval = objMapperWaitForRemoval.readValue(responseForRemoval, CardReaderWaitForRemoval.class);
                    } catch (JsonProcessingException ex) {
                        Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
                        LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
                    }
                    if (waitForRemoval.getRetVal() == 0) {
                        //System.out.println("Wait For removal is success");
                        LOGGER.log(Level.INFO, "Wait For removal is success");
                        return response;
                    } else {
                        //System.out.println("Wait For removal is failed");
                        LOGGER.log(Level.INFO, "Wait For removal is failed");
                        return response;
                    }
                }

            } else {
                //System.out.println("Wait For Connect Failure");
                LOGGER.log(Level.INFO, "Wait For Connect Failure");
                response = "Failure";
            }
        }
        return response;

    }


    public static void main(String[] args) {

        CardWrite cardwrite = new CardWrite();
        String cardwriteresult = cardwrite.cardWriteDeatils();
        System.out.println("CARD WRITE RESULT :::::" + cardwriteresult);

    }

    private String writeTokenDynamicFile(int handleValue, byte[] whichdata_dyn, int offset, String encodedbase64Dynamic, int reqlength) {

        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_dyn, offset, encodedbase64Dynamic, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;

        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Dynamic File Data written on the card sucessfully");
                LOGGER.log(Level.INFO, "Dynamic File Data written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Dynamic File data on the card");
                LOGGER.log(Level.INFO, "Error while writing Dynamic File data on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }


        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeTokenPhoto(int handleValue, byte[] whichdata_photo, int offset, String encodedbase64LabourPhoto, int reqlength) {
        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_photo, offset, encodedbase64LabourPhoto, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;
        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Photo written on the card sucessfully");
                LOGGER.log(Level.INFO, "Photo written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Photo on the card");
                LOGGER.log(Level.INFO, "Error while writing Photo on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeTokenDefaultAccess(int handleValue, byte[] whichdata_dav, int offset, String encodedbase64DAC, int reqlength) {

        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_dav, offset, encodedbase64DAC, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;
        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            //if(storeDataonNaval.retval == 0){
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Default Access Data written on the card sucessfully");
                LOGGER.log(Level.INFO, "Default Access Data written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Default Access on the card");
                LOGGER.log(Level.INFO, "Error while writing Default Access on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeTokenSpecialAccess(int handleValue, byte[] whichdata_sap, int offset, String encodedbase64specialaccess, int reqlength) {
        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_sap, offset, encodedbase64specialaccess, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;
        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Dynamic File Data written on the card sucessfully");
                LOGGER.log(Level.INFO, "Dynamic File Data written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Dynamic File data on the card");
                LOGGER.log(Level.INFO, "Error while writing Dynamic File data on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeTokenSignature1(int handleValue, byte[] whichdata_signature1, int offset, String encodedbase64signature1, int reqlength) {

        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_signature1, offset, encodedbase64signature1, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;
        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Signature1 written on the card sucessfully");
                LOGGER.log(Level.INFO, "Signature1 written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Signature1 on the card");
                LOGGER.log(Level.INFO, "Error while writing Signature1 on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeTokenSignature3(int handleValue, byte[] whichdata_signature3, int offset, String encodedbase64signature3, int reqlength) {

        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_signature3, offset, encodedbase64signature3, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;
        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Signature3 written on the card sucessfully");
                LOGGER.log(Level.INFO, "Signature3 written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Signature3 on the card");
                LOGGER.log(Level.INFO, "Error while writing Signature3 on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String writeTokenFingerprint(int handleValue, byte[] whichdata_fp, int offset, String encodedbase64fingerprint, int reqlength) {

        String responseStoreDataonNaval = cardReaderAPI.storeDataOnNaval(handleValue, whichdata_fp, offset, encodedbase64fingerprint, reqlength);

        ObjectMapper objMapperStoreData = new ObjectMapper();
        CardReaderStoreData storeDataonNaval;
        try {
            storeDataonNaval = objMapperStoreData.readValue(responseStoreDataonNaval, CardReaderStoreData.class);
            if (storeDataonNaval.getRetVal() == 0) {
                //System.out.println("Fingerprint written on the card sucessfully");
                LOGGER.log(Level.INFO, "Fingerprint written on the card sucessfully");
                return "success";
            } else {
                //System.out.println("Error while writing Fingerprint on the card");
                LOGGER.log(Level.INFO, "Error while writing Fingerprint on the card");
                return "failure";
            }
        } catch (JsonProcessingException ex) {
            Logger.getLogger(CardWrite.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, "JsonProcessingException:" + ex);
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

