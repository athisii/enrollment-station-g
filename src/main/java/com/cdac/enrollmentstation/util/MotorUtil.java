package com.cdac.enrollmentstation.util;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MotorUtil {
    private static final Logger LOGGER = Logger.getLogger(MotorUtil.class.getName());
    private static SerialPort serialPort;

    public static boolean serialPortOpen(String serialPortName) {
        serialPort = SerialPort.getCommPort(serialPortName);
        serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 2000);
        return serialPort.openPort();
    }

    public static boolean sendData(String data) {
        try {
            serialPort.getOutputStream().write(data.getBytes());
            serialPort.getOutputStream().flush();
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error occurred writing data to serial port. ", ex);
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // should move to home after clockwise and anticlockwise rotations.
        // 9 = initialization bring to home.
        // 1 = clockwise
        // 2 = anticlockwise

        String serialPortName = "/dev/ttyUSB1";
        System.out.println("Opening serial port: " + serialPortName);
        if (serialPortOpen(serialPortName)) {
            System.out.println("Initializing motor.");
            Thread.sleep(4000); // hand movement time
            System.out.println("Initialization done.");

            System.out.println("\n\nMoving to home.");
            if (sendData("9")) {
                Thread.sleep(3000); // hand movement time
                System.out.println("Moved to the home successfully.");
            } else {
                System.out.println("Failed to moved to the home.");
            }

            System.out.println("\n\nRotating clockwise");
            if (sendData("1")) {
                Thread.sleep(3000); // hand movement time
                System.out.println("Rotated clockwise successfully.");
            } else {
                System.out.println("Failed rotate clockwise.");
            }

            /* *********************** Close the serial port ***************************/
            System.out.println("Closing the port.");
            Thread.sleep(2000); // without sleep, it closes with port close (single rotation)
            serialPort.closePort();
            Thread.sleep(3000); // hand movement time
            System.out.println("Serial port closed successfully.");
        } else {
            System.out.println("Failed to open serial port " + serialPortName);
        }

        System.out.println("***************************************************************");

        System.out.println("\n\n\nOpening serial port: " + serialPortName);
        // Move anticlockwise.
        if (serialPortOpen(serialPortName)) {
            System.out.println("Initializing motor.");
            Thread.sleep(4000); // hand movement time
            System.out.println("Initializing done.");
            System.out.println("\n\nMoving to home.");
            if (sendData("9")) {
                Thread.sleep(3000); // hand movement time
                System.out.println("Moved to the home successfully.");
            } else {
                System.out.println("Failed to moved to the home.");
            }

            System.out.println("\n\nRotating anticlockwise");
            if (sendData("2")) {
                Thread.sleep(3000); // hand movement time
                System.out.println("Rotated anticlockwise successfully.");
            } else {
                System.out.println("Failed rotate anticlockwise.");
            }

            /* *********************** Close the serial port ***************************/
            System.out.println("Closing the port.");
            Thread.sleep(2000); // without sleep, it closes with port close (single rotation)
            serialPort.closePort();
            Thread.sleep(3000); // hand movement time
            System.out.println("Serial port closed successfully.");
        } else {
            System.out.println("Failed to open serial port " + serialPortName);
        }
        System.out.println("Exiting the program.");
        Thread.sleep(5000);
        System.out.println("Bye.");
    }
}
