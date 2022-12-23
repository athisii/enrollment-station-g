/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author root
 */
package com.cdac.enrollmentstation.service;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.SaveEnrollmentDetails;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ObjectReaderWriter {
    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    TestProp prop = new TestProp();

    public ObjectReaderWriter() {
        //this.handler = appLog.getLogger();
        //LOGGER.addHandler(handler); 
    }

    String objFilePath = prop.getProp().getProperty("saveenrollment");


    //public String objFilePath = "/usr/share/enrollment/save/saveEnrollment.txt";
    public void writer(SaveEnrollmentDetails saveEnrollment) {
        //SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
        try {
                        /*System.out.println("Create Fileoutputstream");
			FileOutputStream f = new FileOutputStream(new File(objFilePath));
                        System.out.println("Create Objectoutputstream");
			ObjectOutputStream o = new ObjectOutputStream(f);
                        System.out.println("After creation");

			// Write objects to file
                        System.out.println("Writing object");
			o.writeObject(saveEnrollment);
                        System.out.println("Writing object finished");
			//o.writeObject(p2);
                        
                        System.out.println("close Objectoutputstream");
			o.close();
                        System.out.println("close Fileoutputstream");
			f.close();*/
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setBase64Variant(Base64Variants.MIME_NO_LINEFEEDS);


            String postJson;

            postJson = mapper.writeValueAsString(saveEnrollment);
            FileUtils.writeStringToFile(new File(objFilePath), postJson);

        } catch (FileNotFoundException e) {
            //System.out.println("File not found");
            LOGGER.log(Level.INFO, "File not found:" + e);
        } catch (IOException e) {
            //System.out.println("Error initializing stream"+e.toString());
            LOGGER.log(Level.INFO, "IOException:" + e);
        } catch (Exception e) {
            //System.out.println("Error initializing stream"+e.toString());
            LOGGER.log(Level.INFO, "Exception:" + e);
        }
    }

    public SaveEnrollmentDetails reader() {
        SaveEnrollmentDetails saveEnrollment = new SaveEnrollmentDetails();
        try {

                /*FileInputStream fi = new FileInputStream(new File(objFilePath));
                ObjectInputStream oi = new ObjectInputStream(fi);

                // Read objects
                saveEnrollment  = (SaveEnrollmentDetails) oi.readObject();
                //Person pr2 = (Person) oi.readObject();

                System.out.println(saveEnrollment.toString());
                //System.out.println(pr2.toString());

                oi.close();
                fi.close();*/

            FileReader reader = new FileReader(objFilePath);

            ObjectMapper objMapper = new ObjectMapper();

            saveEnrollment = objMapper.readValue(reader, SaveEnrollmentDetails.class);
            System.out.println(" save enrollment : " + saveEnrollment.toString());

            return saveEnrollment;
        } catch (FileNotFoundException e) {
            //System.out.println("File not found");
            LOGGER.log(Level.INFO, "File not found:" + e);
        } catch (IOException e) {
            //System.out.println("Error initializing stream");
            LOGGER.log(Level.INFO, "IOException:" + e);

        }
        return saveEnrollment;
    }


}
