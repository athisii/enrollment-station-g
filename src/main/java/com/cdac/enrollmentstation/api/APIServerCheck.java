/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ContactDetail;
import com.cdac.enrollmentstation.security.CryptoAES256;
import com.cdac.enrollmentstation.security.HmacUtils;
import com.cdac.enrollmentstation.security.PKIUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * @author root
 */
public class APIServerCheck {

    public String sessionkey;
    TestProp prop = new TestProp();
    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public APIServerCheck() {
        //this.handler = appLog.getLogger();
        //LOGGER.addHandler(handler); 
    }

    public String checkGetARCNoAPI(String url, String arcNo) {

        String result = "";
        int code = 200;
        int noOfRetries = 1;
        String status = "";
        try {
            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(20000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            String jsonInputString = "{\"ARCNo\": \"" + arcNo + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            while (noOfRetries > 0) {
                con.connect();
                code = con.getResponseCode();
                if (code == 200) {
                    //result = "-> Green <-\t" + "Code: " + code;
                    result = "connected";
                    break;
                } else {
                    result = "notreachable";
                }
                noOfRetries--;
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, e.getMessage() + "Exception: ");

        }
        //System.out.println(url + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
        return result;
    }

    public String getARCNoAPI(String url, String arcNo) {


        //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";
        String jsonInputString = "{\"ARCNo\": \"" + arcNo + "\"}";

        String result = "";

        String response = null;
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            //con.setRequestProperty("UniqueKey", getuuid);
            con.setConnectTimeout(10000);
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";

            try (OutputStream os = con.getOutputStream()) {
                //Uncomment Afterwards
                //byte[] input = encString.getBytes("utf-8");
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {

                response = br.lines().collect(Collectors.joining());
            }

        } catch (IOException e) {
            //result = "Exception: " + e.getMessage();
            //System.out.println(url + "\t\tStatus:" + result);
            LOGGER.log(Level.INFO, result + url + "\t\tStatus:");
            return result;
        }
        return response;
    }

    public String getStatusTokenUpdate(String url, String testjson) {
        String result = "";
        int code = 0;
        int noOfRetries = 3;
        //String status = "";
        try {
            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //System.out.println("josn string : "+ testjson);
            LOGGER.log(Level.INFO, "josn string : " + testjson);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = testjson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            while (noOfRetries > 0) {
                con.connect();

                code = con.getResponseCode();
                if (code == 200) {
                    //result =json "-> Green <-\t" + "Code: " + code;
                    result = "connected";
                    break;
                } else {
                    result = "notreachable";
                }
                noOfRetries--;
            }
        } catch (IOException e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, e.getMessage() + "Exception: ");

        }
        //System.out.println(url + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
        return result;
    }

    public String getTokenUpdate(String url, String jsonTokenUpdate) {
        CryptoAES256 aes256 = new CryptoAES256();
        String getuuid = aes256.generateRandomUUID();
        getuuid = getuuid.replace("-", "");
        System.out.println("guid : " + getuuid.length());
        Key strKey = aes256.generateKey32(getuuid);
        //contactDetail contactdetail = new contactDetail();                     
        //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";
        //String jsonInputString = "{\"UniqueNo\": \"LAB0001\",\"EnrollmentStationID\": \"AA112234BI\",\"CardCSN\": \"CC773\",\"ContractorID\": \"43\",\"ContractorCSN\": \"CC77331\",\"TokenIssuanceDate\": \"2021/11/02 12:56:39\",\"ContractID\": \"7\",\"EnrollmentStationUnitID\": \"UI\",\"TokenID\": \"CC77\",\"VerifyFPSerialNo\": \"1808577\"}";
        //System.out.println("josn string : "+ jsonTokenUpdate);
        LOGGER.log(Level.INFO, "josn string : " + jsonTokenUpdate);

        String encString = "";
        try {
            encString = aes256.encryptString(jsonTokenUpdate, strKey);
        } catch (Exception ex) {
            //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "Exception: ");
        }
        String result = "";
        String response = null;
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            // con.setRequestProperty("SessionKey", getuuid);
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";

            try (OutputStream os = con.getOutputStream()) {
                //byte[] input = encString.getBytes("utf-8");
                byte[] input = jsonTokenUpdate.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
            sessionkey = con.getHeaderField("SessionKey");
            //System.out.println(con.getHeaderFields());
            //System.out.println("Session Key in TokenUpdate::"+sessionkey);

        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            //System.out.println(url + "\t\tStatus:" + result);
            LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
            return result;
        }

        return response.toString();
    }

    public String getStatusContractListAPI(String url, String contractorID, String cardSerialNo) {

        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            ContactDetail contactdetail = new ContactDetail();


            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            // String jsonInputString = "{\"readername\": \""+readerName+"\"}";
            String jsonInputString = "{\"ContractorID\": \"" + contractorID + "\" ,\"CardSerialNo\": \"" + cardSerialNo + "\"}";
            //System.out.println("josn string : "+ jsonInputString);
            LOGGER.log(Level.INFO, "josn string : " + jsonInputString);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            while (noOfRetries > 0) {
                con.connect();

                code = con.getResponseCode();
                if (code == 200) {
                    //result =json "-> Green <-\t" + "Code: " + code;
                    result = "connected";
                    break;
                } else {
                    result = "notreachable";
                }
                noOfRetries--;
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, e.getMessage() + "Exception: ");

        }
        //System.out.println(url + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, result + url + "\t\tStatus:");
        return result;
    }

    public String getContractListAPI(String url, String contractorID, String cardSerialNo) {

        CryptoAES256 aes256 = new CryptoAES256();
        String getuuid = aes256.generateRandomUUID();
        getuuid = getuuid.replace("-", "");
        //System.out.println("guid : "+getuuid.length());
        LOGGER.log(Level.INFO, "guid : " + getuuid.length());
        Key strKey = aes256.generateKey32(getuuid);
        ContactDetail contactdetail = new ContactDetail();

        //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";
        String jsonInputString = "{\"ContractorID\": \"" + contractorID + "\" ,\"CardSerialNo\": \"" + cardSerialNo + "\"}";
        //System.out.println("josn string : "+ jsonInputString);
        LOGGER.log(Level.INFO, "josn string : " + jsonInputString);
        String encString = "";
        /*
        try {
             //Uncomment After
            //encString = aes256.encryptString(jsonInputString, strKey);
        } catch (Exception ex) {
            //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
             LOGGER.log(Level.INFO, ex+ "Exception:");
        }*/
        String result = "";
        String response = null;
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("UniqueKey", getuuid);
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";

            try (OutputStream os = con.getOutputStream()) {
                //Uncomment Afterwards
                //byte[] input = encString.getBytes("utf-8");
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }

            sessionkey = con.getHeaderField("SessionKey");
            //System.out.println(con.getHeaderFields());
            //System.out.println("Session Key in contraclist::"+sessionkey);
            LOGGER.log(Level.INFO, sessionkey + "Session Key in contraclist::");

        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            //System.out.println(url + "\t\tStatus:" + result);
            LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
            return result;
        }

        return response;
    }

    public String getStatusLabourListAPI(String url) {

        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        String encString = "";
        try {
            // TO be uncommented later
            PKIUtil pki = new PKIUtil();
            CryptoAES256 aes256 = new CryptoAES256();
            String getuuid = aes256.generateRandomUUID();
            getuuid = getuuid.replace("-", "");
            //System.out.println("guid : "+getuuid.length());
            LOGGER.log(Level.INFO, "guid : " + getuuid.length());
            Key strKey = aes256.generateKey32(getuuid);
            byte[] pkigetuuid = null;
            pkigetuuid = pki.encrypt_test(getuuid);
            String encodedBase64getuuid = Base64.getEncoder().encodeToString(pkigetuuid);


            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("UniqueKey", encodedBase64getuuid);
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"ContractID\": \"1234567890\"}";
            //System.out.println("josn string : "+ jsonInputString);
            LOGGER.log(Level.INFO, "josn string : " + jsonInputString);
            encString = aes256.encryptString(jsonInputString, strKey);
            try (OutputStream os = con.getOutputStream()) {
                //byte[] input = jsonInputString.getBytes("utf-8");
                byte[] input = encString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            while (noOfRetries > 0) {
                con.connect();

                code = con.getResponseCode();
                if (code == 200) {
                    //result =json "-> Green <-\t" + "Code: " + code;
                    result = "connected";
                    break;
                } else {
                    result = "notreachable";
                }
                noOfRetries--;
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);

        }
        //System.out.println(url + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
        return result;
    }

    public String getStatusUnitListAPI(String url) {

        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {


            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";


            while (noOfRetries > 0) {
                con.connect();

                code = con.getResponseCode();
                if (code == 200) {
                    //result =json "-> Green <-\t" + "Code: " + code;
                    result = "connected";
                    break;
                } else {
                    result = "notreachable";
                }
                noOfRetries--;
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);

        }
        //System.out.println(url + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
        return result;
    }


    public String getLabourListAPI(String url, String contractorID, String contractID) {
        // TO be uncommented later
        PKIUtil pki = new PKIUtil();
        String decResponse = "";
        System.out.println("inside Labour list");
        CryptoAES256 aes256 = new CryptoAES256();
        String getuuid = aes256.generateRandomUUID();
        getuuid = getuuid.replace("-", "");
        System.out.println("guid : " + getuuid.length());
        Key strKey = aes256.generateKey32(getuuid);
        String jsonInputString = "{\"ContractorID\": \"" + contractorID + "\" ,\"ContractID\": \"" + contractID + "\"}";
        //String jsonInputString = "{\"ContractorID\": \"43\" ,\"ContractID\": \"7\"}";
        //System.out.println("JSON Input String:::"+jsonInputString);
        LOGGER.log(Level.INFO, "josn string : " + jsonInputString);

        byte[] pkigetuuid = null;

        //Encrypt the generated uuid
        pkigetuuid = pki.encrypt_test(getuuid);
        //pkigetuuid = pki.encrypt_test(originalInput);


        System.out.println("PKI Get UUid" + pkigetuuid);

        //Encode the encrypted uuid(uniquekey)
        String encodedBase64getuuid = Base64.getEncoder().encodeToString(pkigetuuid);
        System.out.println("getuuidpkiencryptbase64 Bytes" + encodedBase64getuuid);

        //PKI Decrypt Session Key
//               byte[] base64decodesessionkey = Base64.getDecoder().decode(encodedBase64getuuid);
//               String decodedString = new String(base64decodesessionkey);  
//               System.out.println("decodedString:::"+decodedString);
        // String base64decodesessionkeystring = base64decodesessionkey.toString();

        // System.out.println("base64decodeString:"+base64decodesessionkeystring);
//                String sessKey="";
//        try {
//            sessKey = pki.decrypt(base64decodesessionkey);
//        } catch (KeyStoreException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (CertificateException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (UnrecoverableKeyException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchPaddingException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalBlockSizeException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (BadPaddingException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InvalidKeyException ex) {
//            Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
//        }
//                System.out.println("Decrypted PKI Session Key:::"+sessKey);

        System.out.println("josn string : " + jsonInputString);
        String encString = "";
        try {
            //Encrypt JSON Input String
            encString = aes256.encryptString(jsonInputString, strKey);
        } catch (Exception ex) {
            //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.log(Level.INFO, ex + "Exception:");
        }
        //Hashvalue for JSON
        HmacUtils hm = new HmacUtils();
        String messageDigestJson = "";
        messageDigestJson = hm.generateHmac256(encString, getuuid.getBytes()); //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
        //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);

        //System.out.println("messageDigestJson::"+messageDigestJson); 
        //System.out.println("getuuid::"+getuuid); 
        //System.out.println("getuuidBase64 PKI Encrypt::"+encodedBase64getuuid); 
        String result = "";
        String response = null;
        //StringBuilder response = new StringBuilder();
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            //con.setRequestProperty("SessionKey", getuuid);
            //con.setRequestProperty("UniqueKey", getuuid);
            con.setRequestProperty("UniqueKey", encodedBase64getuuid);
            con.setRequestProperty("HashKey", messageDigestJson);
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";

            try (OutputStream os = con.getOutputStream()) {
                //uncommment
                byte[] input = encString.getBytes("utf-8");
                //byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            //System.out.println("Encrypted JSON:"+encString);


            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
                //System.out.println("Response::::::::"+response);
            } catch (Exception e) {
                result = "Exception: " + e.getMessage();
                //System.out.println(url + "\t\tStatus1:" + result);
                LOGGER.log(Level.INFO, e.getMessage() + "Exception: ");
                return result;
            }
            //Uncomment
            //sessionkey = con.getHeaderField("SessionKey");
            sessionkey = con.getHeaderField("UniqueKey");
            //System.out.println(con.getHeaderFields());                                                    
            //System.out.println("Session Key in Labour List::"+sessionkey);

            //PKI Decrypt Session Key
            byte[] base64decodesessionkey = Base64.getDecoder().decode(sessionkey);
            String decodedString = new String(base64decodesessionkey);
            //System.out.println("decodedString:::"+decodedString);

            String sessKey = "";
            sessKey = pki.decrypt(base64decodesessionkey);
            //System.out.println("Decrypted PKI Session Key:::"+sessKey);

            //Pass Decrypted Session Key to AES Algo
            CryptoAES256 aesdec = new CryptoAES256(sessKey);
            decResponse = aesdec.decryptString(response.toString());

        } catch (Exception e) {
            result = "Exception in Get Labour List: " + e.getMessage();
            //System.out.println("\t\tStatus:" + result);
            LOGGER.log(Level.INFO, e.getMessage() + "Exception in Get Labour List: ");
            return result;
        }
        return decResponse;
    }

    public String getUnitListAPI(String url) {

        System.out.println("inside unit list");

        String result = "";
        String response = null;
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            //String jsonInputString = "{\"ContractorID\": \""+contractorID+"\" ,\"CardSerialNo\": \""+cardSerialNo+"\"}";

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {

                response = br.lines().collect(Collectors.joining());

            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            //System.out.println(url + "\t\tStatus:" + result);
            LOGGER.log(Level.INFO, e.getMessage() + "Exception: ");
            //con.close()
            return result;
        }

        return response.toString();
    }


    public String checkGetEnrollmentSaveAPI(String url, String postJson) {


        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        String encString = "";
        try {
            // TO be uncommented later
            PKIUtil pki = new PKIUtil();
            CryptoAES256 aes256 = new CryptoAES256();
            String getuuid = aes256.generateRandomUUID();
            getuuid = getuuid.replace("-", "");
            System.out.println("guid : " + getuuid.length());
            Key strKey = aes256.generateKey32(getuuid);
            byte[] pkigetuuid = null;
            pkigetuuid = pki.encrypt_test(getuuid);
            String encodedBase64getuuid = Base64.getEncoder().encodeToString(pkigetuuid);

            URL siteURL = new URL(url);

            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");

            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);


            encString = aes256.encryptString(postJson, strKey);
            try (OutputStream os = con.getOutputStream()) {
                //byte[] input = postJson.getBytes("utf-8");
                byte[] input = encString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            while (noOfRetries > 0) {
                con.connect();

                code = con.getResponseCode();
                if (code == 200) {
                    //result = "-> Green <-\t" + "Code: " + code;
                    result = "connected";
                    break;
                } else {
                    result = "notreachable";
                }
                noOfRetries--;
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, result);

        }
        //System.out.println(url + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
        return result;
    }

    public String getEnrollmentSaveAPI(String url, String postJson) {

        String result = "";
        StringBuilder response = new StringBuilder();
        String decResponse = "";
        CryptoAES256 aes256 = new CryptoAES256();
        //skey = aes256.getAESKey();
        String getuuid = aes256.generateRandomUUID();
        getuuid = getuuid.replace("-", "");
        System.out.println("guid : " + getuuid.length());
        Key strKey = aes256.generateKey32(getuuid);
        String encstr;
        try {
            //encstr = aes256.encryptString("test", strKey);
            //String dec = aes256.decryptStringSK(encstr, strKey);
            //System.out.println("dec string :"+ dec);
            postJson = postJson.replace("\n", "");
            String encryptedJson = "";
            encryptedJson = aes256.encryptString(postJson, strKey);
            //System.out.println("Encrypted JSON::"+encryptedJson);
            FileUtils.writeStringToFile(new File("/home/enadmin/saveBioencrypt_" + getuuid + ".txt"), encryptedJson);
            FileUtils.writeStringToFile(new File("/home/enadmin/saveBiojson.txt"), postJson);

            // TO be uncommented later
            byte[] pkigetuuid = null;
            PKIUtil pki = new PKIUtil();
            pkigetuuid = pki.encrypt_test(getuuid);
            //System.out.println("PKI Get UUid"+pkigetuuid);
            String encodedBase64getuuid = Base64.getEncoder().encodeToString(pkigetuuid);
            //System.out.println("getuuid:"+getuuid);
            //System.out.println("getuuidpkiencryptbase64 Bytes"+encodedBase64getuuid);

            //Hashvalue for JSON
            HmacUtils hm = new HmacUtils();
            String messageDigestJson = hm.generateHmac256(encryptedJson, getuuid.getBytes());
            System.out.println("messageDigestJson::" + messageDigestJson);

            URL siteURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            //con.setRequestProperty("SessionKey", getuuid);
            //con.setRequestProperty("UniqueKey", getuuid);
            con.setRequestProperty("UniqueKey", encodedBase64getuuid);
            con.setRequestProperty("HashKey", messageDigestJson);
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(60000);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = encryptedJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {

                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                    //System.out.println("Response:::"+response);
                }

                // }

                Map<String, List<String>> map = con.getHeaderFields();
                Boolean isSessionKeyPresent = false;
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    if (entry.getKey() == null)
                        continue;
                    System.out.println("Key : " + entry.getKey() +
                            " ,Value : " + entry.getValue());
                    //if(entry.getKey().contains("SessionKey")){
                    if (entry.getKey().contains("UniqueKey")) {
                        isSessionKeyPresent = true;
                    }
                }

                String secKey = "";
                if (isSessionKeyPresent) {
                    //secKey = con.getHeaderField("SessionKey");
                    secKey = con.getHeaderField("UniqueKey");
                    System.out.println("Unique key :" + secKey);
                } else {
                    result = "Exception: " + "Unique Key From Server is Empty";
                    return result;
                }

                //PKI Decrypt Session Key
                byte[] base64decodesessionkey = Base64.getDecoder().decode(secKey);
                String decodedString = new String(base64decodesessionkey);
                System.out.println("decodedString:::" + decodedString);

                String sessKey = "";
                sessKey = pki.decrypt(base64decodesessionkey);
                System.out.println("Decrypted PKI Session Key:::" + sessKey);

                //Pass Decrypted Session Key to AES Algo
                CryptoAES256 aesdec = new CryptoAES256(sessKey);
                decResponse = aesdec.decryptString(response.toString());


            }
        } catch (Exception ex) {
            //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("In GET Enrollment Save API Exception"+ex);
            result = "Exception: " + ex.getMessage();
            LOGGER.log(Level.INFO, url + "\t\tStatus:" + result);
            return result;
        }


        //return result;
        //System.out.println("In GET Enrollment Save API"+decResponse);
        LOGGER.log(Level.INFO, "In GET Enrollment Save API" + decResponse);
        return decResponse;
    }

    public String getARCURL() {
        //System.out.println("Mafis server API :"+getMAFISAPIServer());
        LOGGER.log(Level.INFO, "Mafis server API :" + getMAFISAPIServer());
        String arcURL = getMAFISAPIServer() + "/api/EnrollmentStation/GetDetailsByARCNo";
        return arcURL;
    }

    public String getContractListURL() {
        //System.out.println("Mafis server API :"+getMAFISAPIServer());
        LOGGER.log(Level.INFO, "Mafis server API :" + getMAFISAPIServer());
        String contractURL = getMAFISAPIServer() + "/api/EnrollmentStation/GetContractList";
        return contractURL;
    }

    public String getLabourListURL() {
        //System.out.println("Mafis server API :"+getMAFISAPIServer());
        LOGGER.log(Level.INFO, "Mafis server API :" + getMAFISAPIServer());
        String labourListURL = getMAFISAPIServer() + "/api/EnrollmentStation/GetLabourList";
        return labourListURL;
    }

    public String getUnitListURL() {
        String unitListURL = getMAFISAPIServer() + "/api/EnrollmentStation/GetAllUnits";
        return unitListURL;
    }

    public String getTokenUpdateURL() {
        String updateToken = getMAFISAPIServer() + "/api/EnrollmentStation/UpdateTokenStatus";
        return updateToken;
    }

    public String getEnrollmentSaveURL() {

        String enrollmentSaveURL = getMAFISAPIServer() + "/api/EnrollmentStation/SaveEnrollment";
        return enrollmentSaveURL;
    }

    public String getMAFISAPIServer() {
        String mafisServerAPI = "";
        //String serverAPI = "/etc/data.txt";
        String serverAPI = "";
        serverAPI = prop.getProp().getProperty("urldata");
        //try (BufferedReader file = new BufferedReader(new FileReader("/etc/data.txt"))) {
        try (BufferedReader file = new BufferedReader(new FileReader(serverAPI))) {
            String line = file.lines().collect(Collectors.joining());
            String input = " ";
            String[] tokens = line.split(",");
            mafisServerAPI = tokens[1];
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem reading file.");
        }
        if (mafisServerAPI.endsWith("/")) {

            return mafisServerAPI.substring(0, mafisServerAPI.lastIndexOf("/"));

        } else {

            return mafisServerAPI;
        }

    }

    public String getUnitID() {
        String unitID = "";
        //String serverAPI = "/etc/data.txt";
        String serverAPI = "";
        serverAPI = prop.getProp().getProperty("urldata");
        try (BufferedReader file = new BufferedReader(new FileReader(serverAPI))) {
            String line = file.lines().collect(Collectors.joining());
            String input = " ";
            String[] tokens = line.split(",");
            unitID = tokens[0];
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem reading file.");
        }
        return unitID;
    }

    public String getStationID() {
        String stationID = "";
        //String serverAPI = "/etc/data.txt";
        String serverAPI = "";
        serverAPI = prop.getProp().getProperty("urldata"); //Logger.getLogger(APIServerCheck.class.getName()).log(Level.SEVERE, null, ex);
        //System.out.println("File /etc/data.txt Reading Error");
        try (BufferedReader file = new BufferedReader(new FileReader(serverAPI))) {
            String line = file.lines().collect(Collectors.joining());
            String input = " ";
            String[] tokens = line.split(",");
            stationID = tokens[2];
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Problem reading file.");
            LOGGER.log(Level.INFO, "Problem reading file.");
        }
        return stationID;
    }

}
    

