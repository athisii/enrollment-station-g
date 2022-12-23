/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.service;


import com.cdac.enrollmentstation.util.TestProp;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * @author root
 */
public class TokenDispense {
    TestProp prop = new TestProp();

    public String tokenDispense() {

        String[] portNames = SerialPortList.getPortNames();
        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
        }
        String serialportfile = null;
        serialportfile = prop.getProp().getProperty("serialportfile");
        // SerialPort serialPort = new SerialPort("/dev/ttyACM0");
        SerialPort serialPort = new SerialPort(serialportfile);
        try {
            serialPort.openPort();//Open serial port
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);

            serialPort.writeInt(02);//Write data to port
            serialPort.closePort();//Close serial port
            return "success";
        } catch (SerialPortException ex) {
            System.out.println(ex);
            return "failure";
        }
    }


    public static void main(String args[]) {

        String[] portNames = SerialPortList.getPortNames();
        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
        }

        SerialPort serialPort = new SerialPort("/dev/ttyACM0");
        try {
            serialPort.openPort();//Open serial port
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);

            serialPort.writeInt(02);//Write data to port
            serialPort.closePort();//Close serial port
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

}
