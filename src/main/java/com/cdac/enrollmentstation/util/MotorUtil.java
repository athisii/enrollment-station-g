package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MotorUtil {

    public enum MotorCommandType {
        HOME("9"),  // move arm to home position.
        CLOCKWISE("1"), // swing arm to clockwise and comes back to a position(almost home).
        ANTICLOCKWISE("2"); // swing arm to anticlockwise and comes back to a position(almost home).

        private final String value;

        MotorCommandType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(MotorUtil.class.getName());
    private static SerialPort serialPort;
    private static final String MOTOR_SERIAL_PORT_FILE;

    // 1 = clockwise
    // 2 = anticlockwise

    static {
        try {
            MOTOR_SERIAL_PORT_FILE = PropertyFile.getProperty(PropertyName.MOTOR_SERIAL_PORT_FILE).trim();
            if (MOTOR_SERIAL_PORT_FILE.isBlank()) {
                throw new GenericException("Received empty property value");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "No entry for '" + PropertyName.MOTOR_SERIAL_PORT_FILE + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException("Received null or empty property value");
        }
    }

    //Suppress default constructor for noninstantiability
    private MotorUtil() {
        throw new AssertionError("The MotorUtil methods must be accessed statically.");
    }


    public static boolean openSerialPort() {
        serialPort = SerialPort.getCommPort(MOTOR_SERIAL_PORT_FILE);
        serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 2000);
        return serialPort.openPort();
    }

    public static boolean closeSerialPort() {
        if (serialPort != null) {
            return serialPort.closePort();
        }
        return true;
    }

    public static boolean sendData(MotorCommandType motorCommandType) {
        try {
            serialPort.getOutputStream().write(motorCommandType.getValue().getBytes());
            serialPort.getOutputStream().flush();
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error occurred writing data to serial port. ", ex);
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // should move arm to home after clockwise and anticlockwise rotations.

        System.out.println("Opening serial port: " + MOTOR_SERIAL_PORT_FILE);
        if (openSerialPort()) {
            System.out.println("Initializing motor.");
            Thread.sleep(4000); // motor arm movement time
            System.out.println("Initialization done.");

            System.out.println("\n\nMoving to home.");
            if (sendData(MotorCommandType.HOME)) {
                Thread.sleep(3000); // motor arm movement time
                System.out.println("Moved to the home successfully.");
            } else {
                System.out.println("Failed to moved to the home.");
            }

            System.out.println("\n\nRotating clockwise");
            if (sendData(MotorCommandType.CLOCKWISE)) {
                Thread.sleep(3000); // motor arm movement time
                System.out.println("Rotated clockwise successfully.");
            } else {
                System.out.println("Failed rotate clockwise.");
            }

            /* *********************** Close the serial port ***************************/
            System.out.println("Closing the port.");
            Thread.sleep(2000); // without sleep, it closes with port close (single rotation)
            serialPort.closePort();
            Thread.sleep(3000); // motor arm movement time
            System.out.println("Serial port closed successfully.");
        } else {
            System.out.println("Failed to open serial port " + MOTOR_SERIAL_PORT_FILE);
        }

        System.out.println("***************************************************************");

        System.out.println("\n\n\nOpening serial port: " + MOTOR_SERIAL_PORT_FILE);
        // Move anticlockwise.
        if (openSerialPort()) {
            System.out.println("Initializing motor.");
            Thread.sleep(4000); // motor arm movement time
            System.out.println("Initializing done.");
            System.out.println("\n\nMoving to home.");
            if (sendData(MotorCommandType.HOME)) {
                Thread.sleep(3000); // motor arm movement time
                System.out.println("Moved to the home successfully.");
            } else {
                System.out.println("Failed to moved to the home.");
            }

            System.out.println("\n\nRotating anticlockwise");
            if (sendData(MotorCommandType.ANTICLOCKWISE)) {
                Thread.sleep(3000); // motor arm movement time
                System.out.println("Rotated anticlockwise successfully.");
            } else {
                System.out.println("Failed rotate anticlockwise.");
            }

            /* *********************** Close the serial port ***************************/
            System.out.println("Closing the port.");
            Thread.sleep(2000); // without sleep, it closes with port close (single rotation)
            serialPort.closePort();
            Thread.sleep(3000); // motor arm movement time
            System.out.println("Serial port closed successfully.");
        } else {
            System.out.println("Failed to open serial port " + MOTOR_SERIAL_PORT_FILE);
        }
        System.out.println("Exiting the program.");
        Thread.sleep(5000);
        System.out.println("Bye.");
    }
}
