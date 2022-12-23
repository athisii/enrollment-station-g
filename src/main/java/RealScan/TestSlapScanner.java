/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RealScan;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.logging.ApplicationLog;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * @author root
 */
public class TestSlapScanner {
    private String message = "";
    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public String sdkSlapScannerStatus() {

        int sdkresult = RealScan_JNI.RS_SUCCESS;
        int numOfDevice = RealScan_JNI.RS_InitSDK("", 0);

        if (numOfDevice < 2) {
            message = "false";
            return message;
        } else {
            message = "true";
            //return message;
        }

        return message;
    }

    public static void main(String[] args) {
        int sdkresult = RealScan_JNI.RS_SUCCESS;

        int numOfDevice = RealScan_JNI.RS_InitSDK("", 0);

        if (numOfDevice < 2) {
            System.out.println("Device not connected:" + numOfDevice);
        } else {
            System.out.println("Device connected:" + numOfDevice);
        }

        System.out.println("No of Device:" + numOfDevice);
        System.out.println("No of Device:" + RealScan_JNI.RS_GetLastError());
        System.out.println("No of Device:" + RealScan_JNI.RS_SUCCESS);
        System.out.println("No of Device:" + RealScan.RealScan_JNI.RS_ERR_SDK_ALREADY_INITIALIZED);

        if (RealScan_JNI.RS_GetLastError() == RealScan_JNI.RS_SUCCESS || RealScan_JNI.RS_GetLastError() == RealScan.RealScan_JNI.RS_ERR_SDK_ALREADY_INITIALIZED) {
            // statusField.setText("Kindly connect the Device, if not connected");
            // LOGGER.log(Level.INFO, "SDK initialized successfully");
            //backBtn.setDisable(false);
            //scan.setDisable(false);
            System.out.println("Device Not Connected");
        } else {
            System.out.println("Device is Connected");
        }


    }

}
