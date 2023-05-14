package com.cdac.enrollmentstation.util;


import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author root
 */
public class TokenDispenserUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(TokenDispenserUtil.class);

    //Suppress default constructor for noninstantiability
    private TokenDispenserUtil() {
        throw new AssertionError("The TokenDispenserUtil methods must be accessed statically.");
    }

    public static boolean dispenseToken() {
        String serialPortFile;
        try {
            serialPortFile = PropertyFile.getProperty(PropertyName.SERIAL_PORT_FILE).trim();
        } catch (NumberFormatException | GenericException ex) {
            LOGGER.log(Level.SEVERE, () -> "Not a number or no entry for '" + PropertyName.SERIAL_PORT_FILE + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            return false;
        }
        // SerialPort serialPort = new SerialPort("/dev/ttyACM0")
        SerialPort serialPort = new SerialPort(serialPortFile);
        try {
            serialPort.openPort();//Open serial port
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.writeInt(02);//Write data to port
            serialPort.closePort();//Close serial port
            return true;
        } catch (SerialPortException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }
    }

}
