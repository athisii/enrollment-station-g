package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLogNew;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class PropertyFile {
    //Suppress default constructor for noninstantiability
    private PropertyFile() {
        throw new AssertionError("The PropertyFile methods should be accessed statically");
    }

    // Global properties to be accessed from anywhere in the app.
    private static final Properties properties = new Properties();

    static {
        // closes the file after reading it.
        try (FileReader reader = new FileReader(ApplicationConstant.DEFAULT_PROPERTY_FILE)) {
            properties.load(reader);
        } catch (IOException ex) {
            ApplicationLogNew.getLogger(PropertyFile.class).log(Level.SEVERE, "File not found");
            throw new GenericException("File not found!");
        }
    }

    public static String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
