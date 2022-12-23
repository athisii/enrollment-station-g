//package com.cdac.enrollmentstation.controller;
//
//import com.cdac.enrollmentstation.App;
//import com.cdac.enrollmentstation.api.APIServerCheck;
//import com.cdac.enrollmentstation.dto.SaveEnrollmentResponse;
//import com.cdac.enrollmentstation.model.ARCDetails;
//import com.cdac.enrollmentstation.model.ARCDetailsHolder;
//import com.cdac.enrollmentstation.model.SaveEnrollmentDetails;
//import com.cdac.enrollmentstation.security.CryptoAES256;
//import com.cdac.enrollmentstation.security.HmacUtils;
//import com.cdac.enrollmentstation.security.PKIUtil;
//import com.cdac.enrollmentstation.util.Utils;
//import com.fasterxml.jackson.core.Base64Variants;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import org.apache.commons.io.FileUtils;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//import org.opencv.core.Scalar;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.videoio.VideoCapture;
//
//import javax.crypto.SecretKey;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.security.Key;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * The controller for our application, where the application logic is
// * implemented. It handles the button for starting/stopping the camera and the
// * acquired video stream.
// *
// * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
// * @author <a href="http://max-z.de">Maximilian Zuleger</a> (minor fixes)
// * @version 2.0 (2016-09-17)
// * @since 1.0 (2013-10-20)
// */
//public class FXHelloCVController_changedbyHari_140322 implements Initializable {
//    // the FXML button
//    @FXML
//    private Button button;
//    // the FXML image view
//    @FXML
//    private ImageView currentFrame;
//
//    @FXML
//    private ImageView msgicon;
//
//    @FXML
//    private ImageView croppedFrame;
//
//    @FXML
//    private ImageView iconFrame;
//
//    @FXML
//    private Label message;
//
//    @FXML
//    private Label labelarccam;
//
//    String finalBase64Img;
//
//    public static SecretKey skey;
//
//    @FXML
//    private Button showCaptureStatus;
//
//    public SaveEnrollmentResponse saveEnrollmentResponse;
//
//    public APIServerCheck apiServerCheck = new APIServerCheck();
//
//    // a timer for acquiring the video stream
//    private ScheduledExecutorService timer;
//    // the OpenCV object that realizes the video capture
//    private VideoCapture capture = new VideoCapture();
//    // a flag to change the button behavior
//    private boolean cameraActive = false;
//    // the id of the camera to be used
//    private static int cameraId = 0;
//    private int validflag = 0;
//
//    int fcount = 0;
//
//    /**
//     * The action triggered by pushing the button on the GUI
//     *
//     * @param event the push button event
//     */
//    @FXML
//    protected void startCamera(ActionEvent event) {
//        if (!this.cameraActive) {
//            // start the video capture
//            this.capture.open(cameraId);
//            //message.setText("Valid Image");
//            // is the video stream available?
//            if (this.capture.isOpened()) {
//                this.cameraActive = true;
//                this.fcount = 0;
//
//                // grab a frame every 33 ms (30 frames/sec)
//                Runnable frameGrabber = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // effectively grab and process a single frame
//                        Mat frame = grabFrame();
//                        // convert and show the frame
//                        Image imageToShow = Utils.mat2Image(frame);
//                        updateImageView(currentFrame, imageToShow);
//
//                        System.out.println("Called thread");
//                    }
//                };
//
//                this.timer = Executors.newSingleThreadScheduledExecutor();
//                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
//
//                // update the button content
//                this.button.setText("Stop Camera");
//                //message.setVisible(Boolean.FALSE);
//
//            } else {
//                // log the error
//
//                System.err.println("Impossible to open the camera connection...");
//            }
//        } else {
//            // the camera is not active at this point
//            this.cameraActive = false;
//            // update again the button content
//            this.button.setText("Start Camera");
//            //message.setText("Valid Image");
//            //message.setVisible(Boolean.TRUE);
//
//            // stop the timer
//            this.stopAcquisition();
//        }
//    }
//
//    /**
//     * Get a frame from the opened video stream (if any)
//     *
//     * @return the {@link Mat} to show
//     */
//    private Mat grabFrame() {
//        // init everything
//        Mat frame = new Mat();
//
//        // check if the capture is open
//        if (this.capture.isOpened()) {
//            try {
//                // read the current frame
//                this.capture.read(frame);
//
//                // if the frame is not empty, process it
//                if (!frame.empty()) {
//                    String file2 = "/usr/share/enrollment/images/input.jpg";
//                    Imgcodecs imageCodecs = new Imgcodecs();
//                    imageCodecs.imwrite(file2, frame);
//                    this.fcount++;
//                    System.out.println("Frame count=" + (this.fcount));
//                    //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
//                    if (this.fcount > 35) {
//                        Imgproc.rectangle(frame,                    //Matrix obj of the image
//                                new Point(150, 100),        //p1
//                                new Point(450, 450),       //p2
//                                new Scalar(0, 0, 255),     //Scalar object for color
//                                5                          //Thickness of the line
//                        );
//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                message.setText("Move your face to fit in REDBOX");
//                            }
//                        });
//                    }
//                                        /*PythonInterpreter interpreter = new PythonInterpreter();
//                                        interpreter.execfile("/home/boss/HeadPoseEstimation-WHENet/test.py");
//                                        String cmd = "hello()";
//                                        PyObject returnFromPython = interpreter.eval(cmd);
//                                        System.out.println("Return value from Python: " + returnFromPython);
//                                        */
//                                        /*interpreter.exec("import sys\nsys.path.append('/home/hari/Desktop/Capture')\nimport hellojy");
//                                        // execute a function that takes a string and returns a string
//                                        PyObject someFunc = interpreter.get("hellofun");
//                                        PyObject result = someFunc.__call__(new PyString("Hello"));
//                                        String realResult = (String) result.__tojava__(String.class);
//                                        System.out.println(realResult); */
//                    Runtime rt = Runtime.getRuntime();
//                    String cmdString;
//                    Process pr;
//                    if (this.fcount > 60) {
//                        //String cmdString0="/home/boss/HeadPoseEstimation-WHENet/WHENet.sh";
//                        //Process pr0 = rt.exec(cmdString0);
//                        //String[] cmdString ={"sh" , "WHENet.sh", "/home/boss/HeadPoseEstimation-WHENet"};       //exit value 127
//                        //String[] cmdString ={"sh" , "WHENet.sh"};       //exit value 2
//                        //+frame.toString();\
//                        //String[] cmdString ={"bash","-c","/home/boss/HeadPoseEstimation-WHENet/WHENet.sh"}; //exit value 2-working with exit value 126
//                        //String cmdString ="python /home/boss/HeadPoseEstimation-WHENet/demo_video_M1.py";  //exit value 1
//                        cmdString = "python3 /usr/share/enrollment/python/cap.py";
//
//                        System.out.println(cmdString);
//                        pr = rt.exec(cmdString);
//
//                    } else {
//                        String cmdString0 = "python3 /usr/share/enrollment/python/webcam.py";   //working
//                        System.out.println(cmdString0);
//                        pr = rt.exec(cmdString0);
//                    }
//
//
//                    BufferedReader input = new BufferedReader(new InputStreamReader(
//                            pr.getInputStream()));
//
//                    BufferedReader error = new BufferedReader(new InputStreamReader(
//                            pr.getErrorStream()));
//
//                    String line = null;
//                    String eline = null;
//                    while ((eline = error.readLine()) != null) {
//                        System.out.println(eline);
//                    }
//                    while ((line = input.readLine()) != null) {
//                        System.out.println(line);
//
//                        String croppedpath;
//                        if (line.contains("Path=")) {
//                            croppedpath = line.substring(line.indexOf("Path=") + 6, line.length());
//                            System.out.println("Cropped path=" + croppedpath);
//                        }
//                        String croppedm;
//                        if (line.contains("Message=")) {
//                            if (this.fcount < 35) {
//                                croppedm = line.substring(line.indexOf("Message=") + 9, line.length());
//                                System.out.println("Cropped Message=" + croppedm);
//                                Platform.runLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        message.setText(croppedm);
//
//                                        if (croppedm.contains("Face Going out of frame")) {
//                                            Image imageToShow_tick = new Image("/facecode/outofframecolor.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                        if (croppedm.contains("ROTATE Face CLOCK")) {
//                                            Image imageToShow_tick = new Image("/facecode/clockcolor.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                        if (croppedm.contains("ROTATE Face ANTI")) {
//                                            Image imageToShow_tick = new Image("/facecode/anticlockcolor.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                        if (croppedm.contains("ROTATE Face RIGHT")) {
//                                            Image imageToShow_tick = new Image("/facecode/rightrotatecolor.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                        if (croppedm.contains("ROTATE Face LEFT")) {
//                                            Image imageToShow_tick = new Image("/facecode/leftrotatecolor.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                        if (croppedm.contains("CHIN DOWN")) {
//                                            Image imageToShow_tick = new Image("/facecode/chindowncolored.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                        if (croppedm.contains("CHIN UP")) {
//                                            Image imageToShow_tick = new Image("/facecode/chinupcolor.png");
//                                            updateImageView(msgicon, imageToShow_tick);
//                                        }
//                                    }
//                                });
//                                if (croppedm.contains("Single")) {
//                                    this.stopAcquisition();
//                                }
//                            }
//
////                                                String filered = "/home/boss/NetBeansProjects/src/main/resources/facecode/camera.png";
////                                                Mat matrix_red = imageCodecs.imread(filered);
//                            //Image imageToShow_tick = Utils.mat2Image(matrix_red);
//                            Image imageToShow_tick = new Image("/facecode/camera.png");
//                            updateImageView(iconFrame, imageToShow_tick);
//                        }
//                        validflag = 0;
//                        if (line.contains("Valid")) {
//                            //this.updatemessage("Valid Image
//                            System.out.println("Valid flag set");
//                            validflag = 1;
//                            if (this.fcount < 60) {
//                                Platform.runLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        message.setText("Valid Image");
//                                    }
//                                });
////                                                String filetick = "/home/boss/NetBeansProjects/src/main/resources/facecode/tickgreen.jpg";
////                                                Mat matrix_tick = imageCodecs.imread(filetick);
////                                                Image imageToShow_tick = Utils.mat2Image(matrix_tick);
//                                Image imageToShow_tick = new Image("/facecode/tickgreen.jpg");
//
//                                updateImageView(iconFrame, imageToShow_tick);
//                                Platform.runLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showCaptureStatus.setDisable(false);
//                                    }
//                                });
//                            }// if valid
//                            else {
//                                validflag = 0;
//                                System.out.println("Valid flag reset");
//
//                                Platform.runLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        message.setText("Face cropped along RED Box. If not valid-repeat process");
//                                        //message.setText("Valid Image");
//                                    }
//                                });
////                                                String filebrown = "/home/boss/NetBeansProjects/src/main/resources/facecode/brownquestion.jpeg";
////                                                Mat matrix_brown = imageCodecs.imread(filebrown);
////                                                Image imageToShow_tick = Utils.mat2Image(matrix_brown);
//                                Image imageToShow_tick = new Image("/facecode/brownquestion.jpeg");
//                                //Image imageToShow_tick = new Image("/facecode/tickgreen.jpg");
//                                updateImageView(iconFrame, imageToShow_tick);
//                            }//else valid ends
//                            this.setClosed();
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    cameraActive = false;
//
//                                    button.setText("Start Camera");
//                                }
//                            });
//                        }
//                    }
//                    System.out.println("</OUTPUT>");
//                    int exitVal = pr.waitFor();
//                    System.out.println("Process exitValue: " + exitVal);
//                    if (exitVal == 0) {
//                        String file3;
//                        if (validflag == 1) {
//                            System.out.print("Valid == 1 if");
//                            file3 = "/usr/share/enrollment/croppedimg/sub.png";
//                        } else {
//                            file3 = "/usr/share/enrollment/croppedimg/out.png";
//                            System.out.print("Valid == 0 else");
//
//                        }
//                        //Changed for discarding background
//                        // String file2 = "/usr/share/enrollment/croppedimg/sub.png";
//                        System.out.println("Process exitValue if : " + exitVal + " File3=" + file3);
//                        Mat matrix_cropped = imageCodecs.imread(file3);
//                        System.out.print(matrix_cropped);
//                        Image imageToShow_crop = Utils.mat2Image(matrix_cropped);
//                        System.out.print(imageToShow_crop);
//                        updateImageView(croppedFrame, imageToShow_crop);
//                        //message.setText("Valid Image");
//                        updateImageView(croppedFrame, imageToShow_crop);
//
//
//                    }
//
//
//                }
//
//            } catch (Exception e) {
//                // log the error
//
//                System.err.println("Exception during the image elaboration: " + e);
//            }
//        }
//
//        return frame;
//    }
//
//    /**
//     * Stop the acquisition from the camera and release all the resources
//     */
//    private void stopAcquisition() {
//        if (this.timer != null && !this.timer.isShutdown()) {
//            try {
//                // stop the timer
//                this.timer.shutdown();
//                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
//            } catch (InterruptedException e) {
//                // log any exception
//                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
//            }
//        }
//
//        if (this.capture.isOpened()) {
//            // release the camera
//            this.capture.release();
//        }
//    }
//
//    /**
//     * Update the {@link ImageView} in the JavaFX main thread
//     *
//     * @param view  the {@link ImageView} to update
//     * @param image the {@link Image} to show
//     */
//    private void updateImageView(ImageView view, Image image) {
//        Utils.onFXThread(view.imageProperty(), image);
//    }
//
//    /**
//     * On application close, stop the acquisition from the camera
//     */
//    protected void setClosed() {
//        this.stopAcquisition();
//    }
//
//    @FXML
//    private void showIris() throws IOException {
//
//        App.setRoot("iris");
//    }
//
//    @FXML
//    private void showCaptureStatus() throws IOException {
//        System.out.println("show Capture Status ::");
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//        ARCDetails a = holder.getArcDetails();
//        SaveEnrollmentDetails saveEnrollment = holder.getSaveEnrollmentDetails();
//        System.out.println("details : " + saveEnrollment.getArcNo());
//        //String file1 = "/usr/share/enrollment/croppedimg/out.png";
//        String file1 = "/usr/share/enrollment/croppedimg/sub.png"; //changed from out.png to sub.png
//        File outFile = new File(file1);
//        //To check sub
//        //BufferedImage image = toBufferedImage(outFile);
//        //ImageIO.write(image, ".jpg", outFile);  // ignore returned boolean
//
//
//        if (outFile.exists()) {
//            FileInputStream outputFile = new FileInputStream(file1);
//            String fileCompressed = "/usr/share/enrollment/croppedimg/compressedsub.png";
//            String fileCompressed1 = "/usr/share/enrollment/croppedimg/compressedsubtmp.png";
//            File compressedFile = new File(fileCompressed);
//            if (compressedFile.exists()) {
//                // int cursor;
//                FileInputStream outputFileCompressed = new FileInputStream(fileCompressed);
//                //outputFileCompressed.
//               /* FileOutputStream outputFileCompressed1=new FileOutputStream(fileCompressed1);
//                while((cursor = outputFileCompressed.read())!=-1){
//                 outputFileCompressed1.write(cursor);
//                }*/
//                this.finalBase64Img = Base64.getEncoder().encodeToString(outputFile.readAllBytes());
//                saveEnrollment.setPhoto(finalBase64Img);
//                saveEnrollment.setPhotoCompressed(Base64.getEncoder().encodeToString(outputFileCompressed.readAllBytes()));
//            } else {
//
//                System.out.println("Problem reading compressed  file.");
//            }
//            //this.finalBase64Img = Base64.getEncoder().encodeToString(outputFile.readAllBytes());
//
//        } else {
//
//            System.out.println("Problem reading Out file.");
//        }
//        try (BufferedReader file = new BufferedReader(new FileReader("/etc/data.txt"))) {
//            String line = " ";
//            String input = " ";
//            while ((line = file.readLine()) != null) {
//                String[] tokens = line.split(",");
//                saveEnrollment.setEnrollmentStationID(tokens[2]);
//                saveEnrollment.setEnrollmentStationUnitID(tokens[0]);
//
//            }
//            file.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Problem reading file.");
//
//
//        }
//
//        //saveEnrollment.setEnrollmentStationID("StationID");
//        //saveEnrollment.setEnrollmentStationUnitID("UnitID");
//        saveEnrollment.setEnrollmentStatus("SUCCESS");
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date(System.currentTimeMillis());
//        saveEnrollment.setEnrollmentDate(formatter.format(date));
//        saveEnrollment.setArcStatus(a.getArcStatus());
//        saveEnrollment.setUniqueID(a.getApplicantID());//For ApplicantID
//        System.out.println("ARC STATUS::" + a.getArcStatus());
//        System.out.println("Apllicant ID::" + a.getApplicantID());
//
//        holder.setSaveEnrollmentDetails(saveEnrollment);
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.setBase64Variant(Base64Variants.MIME_NO_LINEFEEDS);
//
//
//        String postJson;
//        try {
//            postJson = mapper.writeValueAsString(saveEnrollment);
//            postJson = postJson.replace("\n", "");
//            int jsonHash = postJson.hashCode();
//            // System.out.println("post json final :"+ postJson);
////                 File json = new File("/home/cloud/postjson");
////
////                 FileOutputStream output = new FileOutputStream(json);
////                 output.write(postJson.getBytes());
////                 output.close();
//            String connurl = apiServerCheck.getEnrollmentSaveURL();
//            String testJson = "{\n" +
//                    "  \"ARCNo\": \"123ABC\",\n" +
//                    "  \"FP\": [\n" +
//                    "    {\n" +
//                    "      \"Position\": \"LT\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"LI\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"LM\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"LR\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"LL\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"RT\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"RI\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"RM\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"RR\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"RL\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    }\n" +
//                    "\n" +
//                    "  ],\n" +
//                    "  \"IRIS\": [\n" +
//                    "    {\n" +
//                    "      \"Position\": \"LI\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"Position\": \"RI\",\n" +
//                    "      \"Image\": \"aGVsbG8=\",\n" +
//                    "      \"Template\": \"aGVsbG8=\"\n" +
//                    "    }\n" +
//                    "  ],\n" +
//                    "  \"Photo\": \"aGVsbG8=\",\n" +
//                    "  \"PhotoCompressed\": \"aGVsbG8=\",\n" +
//                    "  \"EnrollmentStationID\": \"string\",\n" +
//                    "  \"EnrollmentStationUnitID\": \"string\",\n" +
//                    "  \"EnrollmentStatus\": \"string\",\n" +
//                    "  \"EnrollmentDate\": \" 2021-01-07 08:29:35\"\n" +
//                    "}";
//
//
//            //  String connectionStatus = apiServerCheck.checkGetEnrollmentSaveAPI(connurl, testJson);
//            //  System.out.println("connection status :"+connectionStatus);
//            //Uncomment Afterwards
////        if(!connectionStatus.contentEquals("connected")) {
////
////            ARCDetails arcDetail = new ARCDetails();
////            arcDetail.setDesc(connectionStatus);
////            holder.setARC(arcDetail);
////            App.setRoot("capturecomplete");
////        }
//            //else {
//            try {
//
//                CryptoAES256 aes256 = new CryptoAES256();
//                skey = aes256.getAESKey();
//                String getuuid = aes256.generateRandomUUID();
//                getuuid = getuuid.replace("-", "");
//                System.out.println("guid : " + getuuid.length());
//                Key strKey = aes256.generateKey32(getuuid);
//                String encstr = aes256.encryptString("test", strKey);
//                String dec = aes256.decryptStringSK(encstr, strKey);
//                System.out.println("dec string :" + dec);
//
//                // postJson = mapper.writeValueAsString(saveEnrollment);
//                postJson = postJson.replace("\n", "");
//                String encryptedJson = "";
//                encryptedJson = aes256.encryptString(postJson, strKey);
//                //System.out.println("POST JSON::"+postJson);
//                System.out.println("Encrypted JSON::" + encryptedJson);
//                FileUtils.writeStringToFile(new File("/home/enadmin/saveBio.txt"), encryptedJson);
//                FileUtils.writeStringToFile(new File("/home/enadmin/saveBiojson.txt"), postJson);
//
//                // TO be uncommented later
//                byte[] pkigetuuid = null;
//                PKIUtil pki = new PKIUtil();
//                pkigetuuid = pki.encrypt_test(getuuid);
//                System.out.println("PKI Get UUid" + pkigetuuid);
//                String encodedBase64getuuid = Base64.getEncoder().encodeToString(pkigetuuid);
//                System.out.println("getuuid:" + getuuid);
//                System.out.println("getuuidpkiencryptbase64 Bytes" + encodedBase64getuuid);
//
//
//                //Hashvalue for JSON
//                HmacUtils hm = new HmacUtils();
//                String messageDigestJson = hm.generateHmac256(encryptedJson, getuuid.getBytes());
//                System.out.println("messageDigestJson::" + messageDigestJson);
//
//            /*
//            String connectionStatus = apiServerCheck.checkGetARCNoAPI(connurl,"123abc");
//            System.out.println("connection status :"+connectionStatus);
//
//            if(!connectionStatus.contentEquals("connected")) {
//            message.setText("System not connected to network. Connect and try again");
//            return;
//             }else{*/
//                URL url = new URL(connurl);
//                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                con.setRequestMethod("POST");
//                con.setRequestProperty("Content-Type", "application/json; utf-8");
//                //con.setRequestProperty("SessionKey", getuuid);
//                //con.setRequestProperty("UniqueKey", getuuid);
//                con.setRequestProperty("UniqueKey", encodedBase64getuuid);
//                con.setRequestProperty("HashKey", messageDigestJson);
//                con.setRequestProperty("Accept", "application/json");
//                con.setDoOutput(true);
//
//
//                try (OutputStream os = con.getOutputStream()) {
//                    byte[] input = encryptedJson.getBytes("utf-8");
//                    os.write(input, 0, input.length);
//                }
//
//
//                try (BufferedReader br = new BufferedReader(
//                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
//                    StringBuilder response = new StringBuilder();
//                    String responseLine = null;
//                    while ((responseLine = br.readLine()) != null) {
//                        response.append(responseLine.trim());
//                        System.out.println("Response:::" + response);
//                    }
//
//                    System.out.println("Response:::" + response);
//
//                    Map<String, List<String>> map = con.getHeaderFields();
//                    Boolean isSessionKeyPresent = false;
//                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//                        if (entry.getKey() == null)
//                            continue;
//                        System.out.println("Key : " + entry.getKey() +
//                                " ,Value : " + entry.getValue());
//                        //if(entry.getKey().contains("SessionKey")){
//                        if (entry.getKey().contains("UniqueKey")) {
//                            isSessionKeyPresent = true;
//                        }
//                    }
//                    String secKey = "";
//                    if (isSessionKeyPresent) {
//                        //secKey = con.getHeaderField("SessionKey");
//                        secKey = con.getHeaderField("UniqueKey");
//                        System.out.println("Unique key :" + secKey);
//                    }
//
//                    //PKI Decrypt Session Key
//                    byte[] base64decodesessionkey = Base64.getDecoder().decode(secKey);
//                    String decodedString = new String(base64decodesessionkey);
//                    System.out.println("decodedString:::" + decodedString);
//
//                    String sessKey = "";
//                    sessKey = pki.decrypt(base64decodesessionkey);
//                    System.out.println("Decrypted PKI Session Key:::" + sessKey);
//
//                    //Pass Decrypted Session Key to AES Algo
//                    CryptoAES256 aesdec = new CryptoAES256(sessKey);
//                    //byte[] decodedKey = Base64.getDecoder().decode(secKey);
//                    // rebuild key using SecretKeySpec
//                    //SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//                    String decResponse = aesdec.decryptString(response.toString());
//                    System.out.println("response received :" + response.toString());
//                    System.out.println("dec response : " + decResponse);
//
//
//                    ObjectMapper objMapper = new ObjectMapper();
//                    saveEnrollmentResponse = objMapper.readValue(decResponse.toString(), SaveEnrollmentResponse.class);
//                    System.out.println(" save enrollment : " + saveEnrollmentResponse.toString());
//                    //ARCDetails arcDetail = new ARCDetails();
//                    a.setDesc(saveEnrollmentResponse.getDesc());
//                    holder.setArcDetails(a);
//                    System.out.println("ARC details :" + a.toString());
//                    App.setRoot("capturecomplete");
//
//                }
//            }//}
//            catch (Exception e) {
//                System.out.println("Exception block");
//                System.out.println(e);
//            }
//            // }
//        } catch (JsonProcessingException ex) {
//            // Logger.getLogger(FXHelloCVController_changedbyHari_140322.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        //  App.setRoot("capturecomplete");
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//        showCaptureStatus.setDisable(true);
//        ARCDetailsHolder holder = ARCDetailsHolder.getArcDetailsHolder();
//        ARCDetails a = holder.getArcDetails();
//        labelarccam.setText("ARC: " + a.getArcNo());
//    }
//
//    private BufferedImage toBufferedImage(File outFile) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//}
