module com.cdac.enrollmentstation {
    //core java
    requires java.net.http;
    requires java.logging;
    requires java.naming;
    requires java.smartcardio;
    //javafx
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing; // for SwingFXUtils

    requires com.twelvemonkeys.common.image; // image resampling

    // python
    requires opencv;
    requires jython;
    //device sdk
    requires iengine.ansi.iso.main;
    requires jna; // used by iengine
    requires sdk.commons.main;
    requires MantraUtility;
    requires MFS;
    requires MIDIris.Enroll;
    requires MIDFingerAuth;
    requires serial.port.token.dispenser;

    requires com.fazecast.jSerialComm;

    requires org.bouncycastle.provider;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    // exports
    opens com.cdac.enrollmentstation to javafx.fxml;
    opens RealScan to javafx.fxml;
    exports com.cdac.enrollmentstation;
    exports RealScan;
    exports com.cdac.enrollmentstation.model;
    opens com.cdac.enrollmentstation.model to javafx.fxml;
    exports com.cdac.enrollmentstation.controller;
    opens com.cdac.enrollmentstation.controller to javafx.fxml;
    exports com.cdac.enrollmentstation.security;
    opens com.cdac.enrollmentstation.security to javafx.fxml;
    exports com.cdac.enrollmentstation.api;
    opens com.cdac.enrollmentstation.api to javafx.fxml;
    exports com.cdac.enrollmentstation.logging;
    opens com.cdac.enrollmentstation.logging to javafx.fxml;
    exports com.cdac.enrollmentstation.util;
    opens com.cdac.enrollmentstation.util to javafx.fxml;
    exports com.cdac.enrollmentstation.dto;
    opens com.cdac.enrollmentstation.dto to javafx.fxml;
    exports com.cdac.enrollmentstation.enums;
    opens com.cdac.enrollmentstation.enums to javafx.fxml;

    requires static lombok;
}
