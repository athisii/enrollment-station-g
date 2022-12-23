/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.util;

import com.mantra.midfingerauth.MIDFingerAuth;
import com.mantra.midfingerauth.MIDFingerAuth_Callback;
import com.mantra.midfingerauth.enums.DeviceDetection;
import com.mantra.midfingerauth.enums.TemplateFormat;

import java.util.Base64;

/**
 * @author root
 */
public class MatchTemplate implements MIDFingerAuth_Callback {
    public MIDFingerAuth midFingerAuth = null; // For MID finger jar 
    public MatchTemplate matchtemp = null;

    public static void main(String args[]) {

        System.out.println("Test");
        //matchtemp.match();
    }

    private void match() {
        matchSample();
    }

    private void matchSample() {
        byte[] fmrTemplate = Base64.getDecoder().decode("Rk1SADAzMAAAAADrAAEAAAAA3P///////////wAAAAAAAAAAAMUAxQABIQGZYB9AkQDepmSAXgDdHmSAygD/s2RAlAE30ElA2AEO1WRAuACW/2RA7AEeY0RAlQFmZ0FA2QCJ+GSAXQFrAx5AqgAtikxAdwDToGRAcQEhsGRAdQCynGSA0QDXiGRAlAFERy9AygCiiGRAgAFifjGAngB1k2RAkgFyeBRAVQB1GGRAXgECpl9AfAEoxGSASADnpGSAcQE7vGRANADkH2RA7wDTbWRAXAFZF0WAPgCPmWSAtAFvZBFASABxmlsAAA==");
        int[] matchScore = new int[1];
        int ret = midFingerAuth.MatchTemplate(fmrTemplate, fmrTemplate, matchScore, TemplateFormat.FMR_V2011);
        if (ret < 0) {
            System.out.println(midFingerAuth.GetErrorMessage(ret));
        } else {
            int minThresold = 96;
            if (matchScore[0] >= minThresold) {
                System.out.println("Finger matched with score: " + matchScore[0]);
            } else {
                System.out.println("Finger not matched with score: " + matchScore[0]);
            }
        }
    }

    @Override
    public void OnDeviceDetection(String arg0, DeviceDetection arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void OnPreview(int arg0, int arg1, byte[] arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void OnComplete(int arg0, int arg1, int arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
