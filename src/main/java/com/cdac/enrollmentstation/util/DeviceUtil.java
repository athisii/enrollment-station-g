package com.cdac.enrollmentstation.util;

import RealScan.RealScan_JNI;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.mantra.midirisenroll.MIDIrisEnroll;
import com.mantra.midirisenroll.enums.DeviceModel;
import com.mantra.midirisenroll.enums.IrisSide;
import org.opencv.videoio.VideoCapture;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class DeviceUtil {
    //Suppress default constructor for noninstantiability
    private DeviceUtil() {
        throw new AssertionError("The DeviceUtil methods must be accessed statically.");
    }

    // returns true if number of connected device >= expectedNoOfDev else false
    public static boolean isFpScannerConnected(int expectedNoOfDev) {
        return RealScan_JNI.RS_InitSDK("", 0) >= expectedNoOfDev;
    }

    public static boolean isIrisConnected() {
        var midIrisEnroll = new MIDIrisEnroll(null);
        return midIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, new IrisSide[1]);
    }

    public static boolean isCameraConnected() {
        // automatically call open() if camera index passed when instantiated.
        var videoCapture = new VideoCapture(Integer.parseInt(PropertyFile.getProperty(PropertyName.CAMERA_ID)));
        if (!videoCapture.isOpened()) {
            return false;
        }
        videoCapture.release();
        return true;
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkBarcodeStatus() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void isCardReaderConnected() {
    }
}
