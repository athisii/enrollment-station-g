package com.cdac.enrollmentstation;


import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.controller.BaseController;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.DisplayUtil;
import com.cdac.enrollmentstation.util.PropertyFile;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * JavaFX App
 */
public final class App extends Application {
    private static Scene scene;
    private static BaseController controller;
    private static final Logger LOGGER = ApplicationLog.getLogger(App.class);
    // GLOBAL THREAD POOL for the application.
    private static final ExecutorService executorService;

    static {
        int processorCount = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(Math.min(processorCount, 3));
    }

    @Override
    public void start(Stage stage) throws IOException {
        //Added for Close Button
        stage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
        });
        scene = new Scene(loadFXML("main_screen"), DisplayUtil.SCREEN_WIDTH, DisplayUtil.SCREEN_HEIGHT);
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE, () -> "Caused: " + throwable.getCause());
            LOGGER.log(Level.SEVERE, () -> "Message: " + throwable.getMessage());
            controller.onUncaughtException();
        });
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(getCssFileName())).toExternalForm());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setTitle("Enrolment Application");
        stage.setResizable(false);
        stage.show();
        LOGGER.log(Level.INFO, () -> "Touch is " + (Platform.isSupported(ConditionalFeature.INPUT_TOUCH) ? "" : "not") + " supported.");
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        Parent parent = fxmlLoader.load();
        controller = fxmlLoader.getController();
        return parent;
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch();
    }

    public static ExecutorService getThreadPool() {
        return executorService;
    }

    public static String getAppVersion() {
        String appVersionNumber = PropertyFile.getProperty(PropertyName.APP_VERSION_NUMBER);
        if (appVersionNumber == null || appVersionNumber.isBlank()) {
            LOGGER.log(Level.SEVERE, () -> "No entry for '" + PropertyName.APP_VERSION_NUMBER + "' or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException("No entry for '" + PropertyName.APP_VERSION_NUMBER + "' or is empty in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
        }
        return appVersionNumber;
    }

    public static String getCssFileName() {
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_FHD[0]) {
            return "/style/screen_fhd.css";
        }
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_HD[0]) {
            return "/style/screen_hd.css";
        }
        return "/style/base.css";
    }
}