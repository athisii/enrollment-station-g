module com.cdac.enrollmentstation {
    //core java
    requires java.net.http;
    requires java.base;
    requires java.logging;
    requires java.naming;
    //javafx
    requires javafx.controls;
    requires javafx.fxml;
//    requires java.desktop;
    requires javafx.swing;
    requires javafx.web;

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
    exports com.cdac.enrollmentstation.service;
    opens com.cdac.enrollmentstation.service to javafx.fxml;
    exports com.cdac.enrollmentstation.util;
    opens com.cdac.enrollmentstation.util to javafx.fxml;
    exports com.cdac.enrollmentstation.dto;
    opens com.cdac.enrollmentstation.dto to javafx.fxml;

    // python
    requires opencv;
    requires jython;
    //device sdk
    requires iengine.ansi.iso.main;
    requires sdk.commons.main;
    requires MantraUtility;
    requires MFS;
    requires MIDIris.Enroll;
    requires MIDFingerAuth;
    requires MIDFinger.Enroll;
    // security
    // requires ans1.runtime
    requires asn1.converter;

    requires org.bouncycastle.provider;

    // json
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    requires org.apache.commons.io;
//    requires org.apache.commons.codec;
//    requires gson;
    requires json.io;
    requires jssc;

//    requires org.codehaus.groovy;
    requires java.xml.bind;
    requires org.json;

    requires static lombok;

}
