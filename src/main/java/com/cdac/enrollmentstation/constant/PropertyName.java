package com.cdac.enrollmentstation.constant;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */

// A utility class for mapping properties names in /etc/file.properties file.
public class PropertyName {
    //Suppress default constructor for noninstantiability
    private PropertyName() {
        throw new AssertionError("The PropertyName fields should be accessed statically");
    }

    //DEFAULT_PROPERTY_FILE = "/etc/file.properties"

    public static final String LOG_FILE = "logfile";
    public static final String INITIALIZE = "initialize";
    public static final String WAIT_FOR_CONNECT = "waitforconnect";
    public static final String SELECT_APP = "selectapp";
    public static final String READ_DATA = "readdata";
    public static final String STORE_DATA = "storedata";
    public static final String VERIFY_CERT = "verifycert";
    public static final String PKI_AUTH = "pkiauth";
    public static final String WAIT_FOR_REMOVAL = "waitforremoval";
    public static final String DE_INITIALIZE = "deinitialize";
    public static final String LIST_OF_READERS = "listofreaders";
    public static final String CERT_FILE = "certfile";
    public static final String PASSPHRASE = "passphrase";
    public static final String URL_DATA = "urldata";
    public static final String FP_QUALITY = "fpquality";
    public static final String ADMIN_PASSWD = "adminpasswd";
    public static final String INPUT_FILE = "inputfile";
    public static final String CAP_COMMAND = "capcommand";
    public static final String WEBCAM_COMMAND = "webcamcommand";
    public static final String OUTPUT_FILE = "outputfile";
    public static final String COMPRESS_FILE = "compressfile";
    public static final String IMPORT_JSON_FILE = "importjsonfile";
    public static final String EXPORT_FOLDER = "exportfolder";
    public static final String SAVE_ENROLLMENT = "saveenrollment";
    public static final String LOGFILE = "logfile";
    public static final String IRIS_FILE = "irisFile";
    public static final String SLAP_SCAN_FILE = "slapscanFile";
    public static final String CAMERA_FILE_PATH = "cameraFilePath";
    public static final String BARCODE_FILE_PATH = "barcodeFilePath";
    public static final String SLAP_LICENSE = "slaplicense";
    public static final String DOMAIN = "domain";
    public static final String LDA_PURL = "ldapurl";
    public static final String SERIAL_PORT_FILE = "serialportfile";
    public static final String IMPORT_JSON_FOLDER = "importjsonfolder";
    public static final String CUR_PES_ID = "curpesid";
    public static final String ENC_EXPORT_FOLDER = "encexportfolder";
    public static final String CAMERA_ID = "cameraid";
    public static final String PHOTO_CAPTURE_IMG = "photocaptureimg";
    public static final String SUB_FILE = "subfile";

}
