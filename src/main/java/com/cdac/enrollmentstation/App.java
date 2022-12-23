package com.cdac.enrollmentstation;


import com.cdac.enrollmentstation.api.APIServerCheck;
import com.cdac.enrollmentstation.dto.SaveEnrollmentResponse;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.IrisInitialize;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * JavaFX App
 */
public class App extends Application implements EventHandler<WindowEvent> {
    private static Scene scene;
    public static IrisInitialize irisInit;
    public static SecretKey skey;
    private APIServerCheck apiServerCheck = new APIServerCheck();
    public SaveEnrollmentResponse saveEnrollmentResponse;

    //For Application Log
    ApplicationLog appLog = new ApplicationLog();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public App() {
        this.handler = appLog.getLogger();
        LOGGER.addHandler(handler);
    }


    @Override
    public void start(Stage stage) throws IOException {

        //Added for Close Button
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                // consume event
                //System.out.println("Before event consume");
                event.consume();
                Platform.exit();  //Comment this line in production/deployment (Alt+f4 and close button)
                //System.out.println("After event consume");
            }
        }); //end for close button

        scene = new Scene(loadFXML("first_screen"), 1024, 768);
        //scene = new Scene(loadFXML("first_screen"), 1366, 768);
        App.setRoot("first_screen");
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.INFO, "detected from default UEH.\nWill exit now:" + throwable.getClass());
        });
        //stage.initStyle(StageStyle.UTILITY);
        stage.initStyle(StageStyle.UNDECORATED);// - Enable this
        stage.setScene(scene);
        stage.setTitle("Enrollment Application");
        //stage.setFullScreen(true);
        //stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
        //System.out.println("is touch supported : "+Platform.isSupported(ConditionalFeature.INPUT_TOUCH));
        LOGGER.log(Level.INFO, "is touch supported : " + Platform.isSupported(ConditionalFeature.INPUT_TOUCH));
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) throws IOException {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch();

    }
    
    /*    
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes1) {
        StringBuilder sb = new StringBuilder(bytes1.length * 2);
        for(byte b: bytes1)
           sb.append(String.format("%02x", b));
        return sb.toString();       
      
    } */

    @Override
    public void handle(WindowEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}