package com.cdac.enrollmentstation;


import com.cdac.enrollmentstation.controller.AbstractBaseController;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.DisplayUtil;
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
import java.util.List;
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
    private static AbstractBaseController controller;
    private static final Logger LOGGER = ApplicationLog.getLogger(App.class);
    private static volatile boolean hostnameChanged = false;
    private static volatile String pno;
    private static List<String> enrollmentStationIds;
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
            System.exit(0);
        });

        scene = new Scene(loadFXML("main_screen"), DisplayUtil.SCREEN_WIDTH, DisplayUtil.SCREEN_HEIGHT);
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE, "**Uncaught Exception Error: ", throwable);
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

    public static void setHostnameChanged(boolean value) {
        App.hostnameChanged = value;
    }

    public static boolean getHostnameChanged() {
        return App.hostnameChanged;
    }

    public static void setPno(String pno) {
        App.pno = pno;
    }

    public static String getPno() {
        return App.pno;
    }

    public static void setEnrollmentStationIds(List<String> enrollmentStationIds) {
        App.enrollmentStationIds = enrollmentStationIds != null ? enrollmentStationIds.stream().sorted().toList() : null;
    }

    public static List<String> getEnrollmentStationIds() {
        return App.enrollmentStationIds;
    }

    public static ExecutorService getThreadPool() {
        return executorService;
    }

    public static String getCssFileName() {
        // if width >= 1600 and height >= 1200
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_1600X1200[0] && DisplayUtil.SCREEN_HEIGHT >= DisplayUtil.SCREEN_1600X1200[1]) {
            return "/style/screen_1600x1200.css";
        }// if width >= 1400 and height >= 1050
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_1400X1050[0] && DisplayUtil.SCREEN_HEIGHT >= DisplayUtil.SCREEN_1400X1050[1]) {
            return "/style/screen_1400x1050.css";
        }
        return "/style/base.css";
    }
}