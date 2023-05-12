package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class PropertyFile {

    //Suppress default constructor for noninstantiability
    private PropertyFile() {
        throw new AssertionError("The PropertyFile methods must be accessed statically.");
    }

    // Global properties to be accessed from anywhere in the app.
    private static final Properties properties = new Properties();

    static {
        reloadProperty();
    }

    public static void reloadProperty() {
        try (FileReader reader = new FileReader(ApplicationConstant.DEFAULT_PROPERTY_FILE)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new GenericException("Enrollment app properties file not found!");
        }
    }

    // also return null values
    public static String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public static synchronized void changePropertyValue(String propertyName, String value) {
        if (propertyName == null || propertyName.isBlank() || value == null || value.isBlank()) {
            throw new GenericException("Received null or empty property name or value");
        }
        properties.setProperty(propertyName, value);
        savePropertiesToFile();
    }

    public static synchronized void savePropertiesToFile() {
        try (FileWriter fileWriter = new FileWriter(ApplicationConstant.DEFAULT_PROPERTY_FILE)) {
            properties.store(fileWriter, "Enrollment properties.");
        } catch (IOException ex) {
            throw new GenericException("Error occurred while writing properties file");
        }
        reloadProperty();

    }
}
