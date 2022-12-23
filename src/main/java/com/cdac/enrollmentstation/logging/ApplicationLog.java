/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.logging;

import com.cdac.enrollmentstation.util.TestProp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.*;

/**
 * @author root
 */
public class ApplicationLog {
    private static final Logger LOGGER = Logger.getLogger(ApplicationLog.class.getName());

    public static Handler handler;

    TestProp prop = new TestProp();

    //private static final Logger LOGGER; 


    public static Handler getLogger() {
        try {
            //String applogfile="/var/log/enrollment/enrollmentstationapp.log";
            String applogfile = "";
            ApplicationLog applog = new ApplicationLog();
            applogfile = applog.getLogFileLoc() + LocalDateTime.now().withNano(0);
            handler = new FileHandler(applogfile, 1024000, 2); //1024000 is 1Mb - It will rollover after a file becomes 1Mb
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception in Application Log. Failed to add new handler");

        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Exception in Application Log. Security Exception");
        }
        return handler;
    }

    public String getLogFileLoc() {
        String applogfile = "";
        applogfile = prop.getProp().getProperty("logfile");
        return applogfile;
    }
}
