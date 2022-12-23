/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.util;

/**
 * @author root
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TestProp {
    public static void main(String[] args) {
        FileReader reader = null;
        try {
            reader = new FileReader("/etc/file.properties");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestProp.class.getName()).log(Level.SEVERE, null, ex);
        }

        Properties p = new Properties();
        try {
            p.load(reader);

            // System.out.println(p.getProperty("user"));
            // System.out.println(p.getProperty("password"));  
        } catch (IOException ex) {
            Logger.getLogger(TestProp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Properties getProp() {
        FileReader reader = null;
        try {
            reader = new FileReader("/etc/file.properties");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestProp.class.getName()).log(Level.SEVERE, null, ex);
        }

        Properties p = new Properties();
        try {
            p.load(reader);
        } catch (IOException ex) {
            Logger.getLogger(TestProp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return p;
    }

}
