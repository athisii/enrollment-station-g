/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.api;

import com.cdac.enrollmentstation.logging.ApplicationLog;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author root
 */
public class CardReaderAPI {
    private CardReaderAPIURLs cardReaderAPIURLs = new CardReaderAPIURLs();

    private static final Logger LOGGER = ApplicationLog.getLogger(CardReaderAPI.class);
    Handler handler;

    public CardReaderAPI() {
        //this.handler = appLog.getLogger();
        // LOGGER.addHandler(handler);
    }


    public String initialize() {

        String result = "";
        String response = null;
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.getInitializeURL());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();

        }
        //System.out.println(cardReaderAPIURLs.getInitializeURL() + "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.getInitializeURL() + response.toString());
        return response.toString();
    }

    public String[] listofreaders() {
        //public String listofreaders(){

        String result = "";
        //String[] response = new StringBuilder();
        //List<String> response =   new ArrayList<String>();
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        String[] processed = null;

        try {
            URL siteURL = new URL(cardReaderAPIURLs.getListofReaders());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";


            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

            String responseLine = null;
            String unprocessed = null;

            while ((responseLine = br.readLine()) != null) {
                System.out.println("ResponseLine " + responseLine);
                unprocessed = responseLine;
                //response.append(responseLine.trim());
                //response.add(responseLine.trim());
            }
            //System.out.println("ResponseLine "+unprocessed);
            //System.out.println(unprocessed.substring(1, unprocessed.length()-1));
            //System.out.println("Hai"+unprocessed.substring(1, unprocessed.length()-1).split(","));
            processed = unprocessed.substring(1, unprocessed.length() - 1).split(",");
            //System.out.println("result "+processed[0].split("\"")[1]);
            processed[0] = processed[0].split("\"")[1];
            processed[1] = processed[1].split("\"")[1];
            //System.out.println("result1 "+processed[1].split("\"")[1]);
            result = processed.toString();
            //System.out.println("result "+processed);
            //System.out.println("result "+processed.toString());

            //System.out.println("0. "+processed[0]+"1. "+processed[1]);
            LOGGER.log(Level.INFO, "0. " + processed[0] + "1. " + processed[1]);
            return processed;
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        return processed;
    }


    public String getWaitConnect(String readerName) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        String response = null;
        try {
            URL siteURL = new URL(cardReaderAPIURLs.getWaitConnect());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            String jsonInputString = "{\"readername\": \"" + readerName + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }

        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.getWaitConnect() + "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.getWaitConnect() + response.toString());
        return response.toString();
    }

    public String getSelectApp(byte[] cardtype, int handle) {
        String result = "";
        int code = 200;
        String response = null;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.getSelectApp());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"cardtype\": \""+cardType[0]+"\""+",\"handle\": \""+handle+"\"}";
            JSONObject jsonInputString = new JSONObject();

            System.out.println("CardType :::::" + cardtype[0]);
            System.out.println("CardType :::::" + handle);
            jsonInputString.put("cardtype", cardtype[0]);
            jsonInputString.put("handle", handle);

            try (OutputStream os = con.getOutputStream()) {
                //byte[] input = jsonInputString.getBytes("utf-8");
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.getSelectApp() + "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.getSelectApp() + response.toString());
        return response;
    }

    public String readDataFromNaval(int handle, byte[] whichdata, int offset, int reqlen) {
        String result = "";
        int code = 200;
        String response = null;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.readDataFromNaval());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            // String jsonInputString = "{\"handle\": \""+handle+"\""+",\"whichdata\": \""+whichdata+"\""+",\"offset\": \""+offset+"\""+",\"reqlen\": \""+reqlen+"\"}";

            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("handle", handle);
            jsonInputString.put("whichdata", whichdata[0]);
            jsonInputString.put("offset", offset);
            jsonInputString.put("reqlen", reqlen);

            try (OutputStream os = con.getOutputStream()) {
                //byte[] input = jsonInputString.getBytes("utf-8");
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.readDataFromNaval() + "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.readDataFromNaval() + response.toString());
        return response.toString();
    }

    public String storeDataOnNaval(int handle, byte[] whichdata, int offset, String base64Data, int reqlen) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String response = null;
        String status = "";
        try {
            // base64Data = Base64.getEncoder().encodeToString(whichdata);
            URL siteURL = new URL(cardReaderAPIURLs.storeDataOnNaval());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"handle\": \""+handle+"\""+",\"whichdata\": \""+whichdata+"\""+",\"offset\": \""+offset+"\""+",\"data\": \""+base64Data+"\""+",\"reqlen\": \""+reqlen+"\"}";
            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("handle", handle);
            jsonInputString.put("whichdata", whichdata[0]);
            jsonInputString.put("offset", offset);
            jsonInputString.put("data", base64Data);
            jsonInputString.put("datalen", reqlen);

            System.out.println("handle:" + handle);
            System.out.println("whichdata:" + whichdata[0]);
            System.out.println("offset:" + offset);
            System.out.println("data:" + base64Data);
            System.out.println("datalen:" + reqlen);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.storeDataOnNaval() + "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.storeDataOnNaval() + response.toString());
        return response.toString();
    }

    public String verifyCertificate(int handle, byte[] whichtrust, byte[] whichcertificate, String CertificateChain, int CertificateChain_len) {
        String result = "";
        int code = 200;
        String response = null;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(cardReaderAPIURLs.verifyCertificate());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"handle\": \""+handle+"\""+",\"whichtrust\": \""+whichtrust+"\""+",\"whichcertificate\": \""+whichcertificate+"\""+",\"CertificateChain\": \""+CertificateChain+"\""+",\"CertificateChain_len\": \""+CertificateChain_len+"\"}";


            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("handle", handle);
            jsonInputString.put("whichtrust", whichtrust[0]);
            jsonInputString.put("whichcertificate", whichcertificate[0]);
            jsonInputString.put("CertificateChain", CertificateChain);
            jsonInputString.put("CertificateChain_len", CertificateChain_len);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.verifyCertificate()+ "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.verifyCertificate() + response.toString());
        return response.toString();
    }


    public String PKIAuth(int handle1, int handle2) {
        String result = "";
        int code = 200;
        String response = null;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.PKIAuth());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"handle1\": \""+handle1+"\""+",\"handle2\": \""+handle2+"\""+"\"}";
            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("handle1", handle1);
            jsonInputString.put("handle2", handle2);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {

                response = br.lines().collect(Collectors.joining());

            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.PKIAuth()+ "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.PKIAuth() + response.toString());
        return response;
    }

    public String waitForRemoval(int handle) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String response = null;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.cardRemoval());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"handle\": \""+handle1+"\""+"\"}";
            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("handle", handle);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }

        //System.out.println(cardReaderAPIURLs.cardRemoval()+ "Response :" + response.toString());
        LOGGER.log(Level.INFO, "Response :" + cardReaderAPIURLs.cardRemoval() + response.toString());
        return response.toString();
    }

    public String deInitialize() {
        String result = "";
        int code = 200;
        String response = null;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.deInitialize());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = br.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
            return result;
        }
        //System.out.println(cardReaderAPIURLs.deInitialize() + "Response :" + response.toString());
        LOGGER.log(Level.INFO, cardReaderAPIURLs.deInitialize() + "Response :" + response.toString());
        return response.toString();

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public String initializeStatus() {

        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.getInitializeURL());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";


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
            LOGGER.log(Level.INFO, "Exception: ", e.getMessage());
        }
        //System.out.println(cardReaderAPIURLs.getInitializeURL() + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.getInitializeURL() + "\t\tStatus:" + result);
        return result;
    }

    public String getWaitConnectStatus(String readerName) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.getWaitConnect());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            String jsonInputString = "{\"readername\": \"" + readerName + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
                System.out.println("Request Method" + os);
            }
            System.out.println("Connection:::::" + con);


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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.getWaitConnect() + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.getWaitConnect() + "\t\tStatus:" + result);
        return result;
    }

    public String getSelectAppStatus(byte[] cardtype, int handle) {
        String result = "";

        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            //Getting URL from cardReaderAPIURLs class
            URL siteURL = new URL(cardReaderAPIURLs.getSelectApp());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);


            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"readername\": \""+readerName+"\"}";
            //String jsonInputString = "{\"cardtype: "+cardtype[0]+",\"handle\": "+handle+" }";

            //Working
            //String jsonInputString = "{\"cardtype\": 4,\"handle\": 4555}";

            //JSON INPUT as byte Parameter
            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("cardtype", cardtype[0]);
            jsonInputString.put("handle", handle);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                //byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
                System.out.println("Request Method" + os);
            }
            System.out.println("Connection::::" + con);


            while (noOfRetries > 0) {
                con.connect();

                code = con.getResponseCode();
                System.out.println("CODE:::" + code);
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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
        }

        LOGGER.log(Level.INFO, cardReaderAPIURLs.getSelectApp() + "\t\tStatus:" + result);
        //System.out.println(cardReaderAPIURLs.getSelectApp() + "\t\tStatus:" + result);
        return result;
    }


    public String readDataFromNavalStatus(int handle, byte[] whichdata, int offset, int reqlen) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            URL siteURL = new URL(cardReaderAPIURLs.readDataFromNaval());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            //String jsonInputString = "{\"handle\": \""+handle+"\""+",\"whichdata\": \""+whichdata+"\""+",\"offset\": \""+offset+"\""+",\"reqlen\": \""+reqlen+"\"}";

            //JSON INPUT as byte Parameter
            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("handle", handle);
            jsonInputString.put("whichdata", whichdata[0]);
            jsonInputString.put("offset", offset);
            jsonInputString.put("reqlen", reqlen);


            try (OutputStream os = con.getOutputStream()) {
                // byte[] input = jsonInputString.getBytes("utf-8");
                byte[] input = jsonInputString.toString().getBytes("utf-8");
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
            LOGGER.log(Level.INFO, "Exception: ", e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.readDataFromNaval() + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.readDataFromNaval() + "\t\tStatus:" + result);
        return result;
    }

    public String storeDataOnNavalStatus(int handle, byte[] whichdata, int offset, String base64Data, int reqlen) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {
            base64Data = Base64.getEncoder().encodeToString(whichdata);
            URL siteURL = new URL(cardReaderAPIURLs.storeDataOnNaval());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            String jsonInputString = "{\"handle\": \"" + handle + "\"" + ",\"whichdata\": \"" + whichdata + "\"" + ",\"offset\": \"" + offset + "\"" + ",\"data\": \"" + base64Data + "\"" + ",\"reqlen\": \"" + reqlen + "\"}";
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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.storeDataOnNaval() + "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.storeDataOnNaval() + "\t\tStatus:" + result);
        return result;
    }

    public String verifyCertificateStatus(int handle, byte[] whichtrust, byte[] whichcertificate, String CertificateChain, String CertificateChain_len) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(cardReaderAPIURLs.verifyCertificate());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            String jsonInputString = "{\"handle\": \"" + handle + "\"" + ",\"whichtrust\": \"" + whichtrust + "\"" + ",\"whichcertificate\": \"" + whichcertificate + "\"" + ",\"CertificateChain\": \"" + CertificateChain + "\"" + ",\"CertificateChain_len\": \"" + CertificateChain_len + "\"}";
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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.verifyCertificate()+ "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.verifyCertificate() + "\t\tStatus:" + result);
        return result;
    }


    public String PKIAuthStatus(int handle1, int handle2) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(cardReaderAPIURLs.PKIAuth());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            String jsonInputString = "{\"handle1\": \"" + handle1 + "\"" + ",\"handle2\": \"" + handle2 + "\"" + "\"}";
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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.PKIAuth()+ "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.PKIAuth() + "\t\tStatus:" + result);
        return result;
    }

    public String waitForRemovalStatus(int handle1) {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(cardReaderAPIURLs.cardRemoval());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            //String arcNo = "123abc";

            //String jsonInputString = "{\"ContractorID\": \"CONTRACT001\" ,\"CardSerialNo\": \"1234567890\"}";
            String jsonInputString = "{\"handle\": \"" + handle1 + "\"" + "\"}";
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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.cardRemoval()+ "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, cardReaderAPIURLs.cardRemoval() + "\t\tStatus:" + result);
        return result;
    }

    public String deInitializeStatus() {
        String result = "";
        int code = 200;
        int noOfRetries = 3;
        String status = "";
        try {

            URL siteURL = new URL(cardReaderAPIURLs.deInitialize());
            HttpURLConnection con = (HttpURLConnection) siteURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
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
            LOGGER.log(Level.INFO, "Exception: " + e.getMessage());

        }
        //System.out.println(cardReaderAPIURLs.deInitialize()+ "\t\tStatus:" + result);
        LOGGER.log(Level.INFO, "\t\tStatus:" + cardReaderAPIURLs.deInitialize() + result);
        return result;
    }


}
