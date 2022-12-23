/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.mantra.midirisenroll.MIDIrisEnroll;
import com.mantra.midirisenroll.MIDIrisEnrollCallback;
import com.mantra.midirisenroll.enums.DeviceDetection;
import com.mantra.midirisenroll.enums.DeviceModel;
import com.mantra.midirisenroll.enums.IrisSide;
import com.mantra.midirisenroll.model.ImagePara;
import com.mantra.midirisenroll.model.ImageQuality;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * @author root
 */
public class TestIris implements MIDIrisEnrollCallback {
    private MIDIrisEnroll mIDIrisEnroll = null;
    private String message = "";
    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;


    public String sdkIrisStatus() {
        mIDIrisEnroll = new MIDIrisEnroll(this);
        //mIDIrisEnroll.SetLogProperties("/home/boss/MIDIris.log", LogLevel.MIDIRIS_ENROLL_LVL_LOG_ERROR);
        //mIDIrisEnroll.Uninit();

        String version = mIDIrisEnroll.GetSDKVersion();
        System.out.println("sdk version :" + version);
        IrisSide[] irisSides = new IrisSide[1];
        System.out.println("IRIS Sides::" + irisSides.toString());
        if (mIDIrisEnroll.IsDeviceConnected(DeviceModel.MATISX, irisSides)) {
            message = "true";
            return message;
        } else {
            message = "failure";

        }
        
        /*
        //supported device list
        List<String> deviceList = new ArrayList<String>();
         System.out.println("Device List::"+deviceList);
        int ret = mIDIrisEnroll.GetSupportedDevices(deviceList);
        System.out.println("Device List2::"+deviceList);
        System.out.println("return value :"+ ret);
        if (ret != 0) {
            //lblerror.setText(mIDIrisEnroll.GetErrorMessage(ret));
            LOGGER.log(Level.INFO,mIDIrisEnroll.GetErrorMessage(ret));
            message="failure";
            return message;
        }else{
            message="true";
            
        }*/
        return message;

    }

    @Override
    public void OnDeviceDetection(String arg0, IrisSide arg1, DeviceDetection arg2) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void OnPreview(int arg0, ImageQuality arg1, ImagePara arg2) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void OnComplete(int arg0, ImageQuality arg1, ImagePara arg2) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
