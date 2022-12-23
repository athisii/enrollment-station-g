/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RealScan;

/**
 * @author root
 */
public class LoadSDK {

    public static void main(String[] args) {

        try {
            System.loadLibrary("RealScanJni");
        } catch (Exception e) {
            System.out.println("exception :" + e);
        }
        // RealScanTest1 test = new RealScanTest1();
        //  RealScan_JNI jni = new RealScan_JNI(test, true);
        // RealScan_JNI.RS_JNI_Init(jni);
        System.out.println("init ::");

        int result = RealScan_JNI.RS_SUCCESS;

        int numOfDevice = RealScan_JNI.RS_InitSDK("", 0);

        if (RealScan_JNI.RS_GetLastError() == RealScan_JNI.RS_SUCCESS || RealScan_JNI.RS_GetLastError() == RealScan_JNI.RS_ERR_SDK_ALREADY_INITIALIZED) {
            System.out.println("initialized successfully");
        }
        StringBuffer deviceName = new StringBuffer("Device ");
        for (int i = 0; i < numOfDevice; i++) {
            deviceName.append("Device " + i);
            System.out.println("device :" + deviceName);
        }
    }
}
