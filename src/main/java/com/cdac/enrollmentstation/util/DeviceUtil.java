package com.cdac.enrollmentstation.util;

import RealScan.RealScan_JNI;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class DeviceUtil {
    //Suppress default constructor for noninstantiability
    private DeviceUtil() {
        throw new AssertionError("The DeviceUtil methods should be accessed statically");
    }

    // returns true if number of connected device >= expectedNoOfDev else false
    public static boolean checkFpScannerStatus(int expectedNoOfDev) {
        return RealScan_JNI.RS_InitSDK(null, 0) >= expectedNoOfDev;
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkIrisStatus() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkCameraStatus() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkBarcodeStatus() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkDevicesStatus() {
    }
}
