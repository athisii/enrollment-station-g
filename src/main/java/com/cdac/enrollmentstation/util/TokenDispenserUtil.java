package com.cdac.enrollmentstation.util;


import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.mantra.serialport.SerialPortNative;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author root
 */
public class TokenDispenserUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(TokenDispenserUtil.class);
    private static final String SERIAL_PORT_FILE;
    private static final int SLEEP_TIME_IN_MILLI_SEC = 100; // can be stored in properties file to

    // Predefined instruction set for Token Dispenser
    private static final byte[] POLL_ADDR = {0x00, 0x00, 0x01, (byte) 0xFD, 0x02};
    private static final byte[] POLL = {0x03, 0x00, 0x01, (byte) 0xFE, (byte) 0xFE};
    private static final byte[] EN_HOPPER = {0x03, 0x01, 0x01, (byte) 0xA4, (byte) 0xA5, (byte) 0xB2};
    private static final byte[] DISPENSE_COUNT = {0x03, 0x00, 0x01, (byte) 0xA8, 0x54};
    private static final byte[] DISPENSE = {0x03, 0x09, 0x01, (byte) 0xA7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x4B};

    static {
        try {
            SERIAL_PORT_FILE = PropertyFile.getProperty(PropertyName.TOKEN_DISPENSER_SERIAL_PORT_FILE).trim();
            if (SERIAL_PORT_FILE.isBlank()) {
                throw new GenericException("Received empty property value");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "No entry for '" + PropertyName.TOKEN_DISPENSER_SERIAL_PORT_FILE + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException("Received null or empty property value");
        }
    }

    //Suppress default constructor for noninstantiability
    private TokenDispenserUtil() {
        throw new AssertionError("The TokenDispenserUtil methods must be accessed statically.");
    }

    public static boolean dispenseToken() {
        try {
            if (0 != SerialPortNative.OpenPort(SERIAL_PORT_FILE)) {
                LOGGER.log(Level.SEVERE, () -> "OpenPortError: Couldn't open the port: " + SERIAL_PORT_FILE);
                return false;
            }
            sendCommandToDispenser(POLL_ADDR, POLL_ADDR.length);
            Thread.sleep(SLEEP_TIME_IN_MILLI_SEC);
            sendCommandToDispenser(POLL, POLL.length);
            Thread.sleep(SLEEP_TIME_IN_MILLI_SEC);
            sendCommandToDispenser(EN_HOPPER, EN_HOPPER.length);
            Thread.sleep(SLEEP_TIME_IN_MILLI_SEC);
            sendCommandToDispenser(DISPENSE_COUNT, DISPENSE_COUNT.length);
            Thread.sleep(SLEEP_TIME_IN_MILLI_SEC);
            sendCommandToDispenser(DISPENSE, DISPENSE.length);
            SerialPortNative.ClosePort();
            return true;
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            SerialPortNative.ClosePort();
            LOGGER.log(Level.SEVERE, () -> "dispenseTokenError: " + ex.getMessage());
            return false;
        }
    }

    private static void sendCommandToDispenser(byte[] command, int commandByteSize) {
        if (0 != SerialPortNative.WriteData(command, commandByteSize)) {
            LOGGER.log(Level.SEVERE, () -> "WriteDataError: Returned error code not equal to 0.");
            throw new GenericException("Returned non-zero error code while writing to Token Dispenser.");
        }
        byte[] buffer = new byte[2048];
        int returnedByteSize = SerialPortNative.ReadData(buffer, buffer.length);
        if (returnedByteSize - commandByteSize < 1) {
            LOGGER.log(Level.SEVERE, () -> "ReadDataError: Read 0 byte of data.");
            throw new GenericException("Read 0 byte of data.");
        }
    }
}
