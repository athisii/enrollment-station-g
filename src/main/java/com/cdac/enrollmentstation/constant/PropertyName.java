package com.cdac.enrollmentstation.constant;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */

// A utility class for mapping properties names in /etc/enrollment-app.properties file.
public class PropertyName {
    //Suppress default constructor for noninstantiability
    private PropertyName() {
        throw new AssertionError("The PropertyName fields must be accessed statically.");
    }

    //DEFAULT_PROPERTY_FILE = "/etc/enrollment-app.properties"

    public static final String LOG_FILE = "log.file";
    public static final String CARD_API_INITIALIZE = "card.api.initialize";
    public static final String CARD_API_WAIT_FOR_CONNECT = "card.api.wait.for.connect";
    public static final String CARD_API_SELECT_APP = "card.api.select.app";
    public static final String CARD_API_READ_DATA = "card.api.read.data";
    public static final String CARD_API_STORE_DATA = "card.api.store.data";
    public static final String CARD_API_VERIFY_CERT = "card.api.verify.cert";
    public static final String CARD_API_PKI_AUTH = "card.api.pki.auth";
    public static final String CARD_API_WAIT_FOR_REMOVAL = "card.api.wait.for.removal";
    public static final String CARD_API_DE_INITIALIZE = "card.api.de.initialize";
    public static final String CARD_API_LIST_OF_READERS = "card.api.list.of.readers";
    public static final String JKS_CERT_FILE = "jks.cert.file";
    public static final String JKS_PASSWORD = "jks.password";
    public static final String JKS_ALIAS_MANTRA = "jks.alias.mantra";
    public static final String JKS_ALIAS_TECHM = "jks.alias.techm";
    public static final String FP_MATCH_MIN_THRESHOLD = "fp.match.min.threshold";
    public static final String ADMIN_PASSWD = "admin.passwd";
    public static final String PYTHON_IMAGE_PROCESSOR_COMMAND = "python.image.processor.command";
    public static final String IMG_PHOTO_INPUT_FILE = "img.photo.input.file";
    public static final String IMG_PHOTO_COMPRESSED_FILE = "img.photo.compressed.file";
    public static final String IMG_PHOTO_FILE = "img.photo.file";
    public static final String IMG_SIGNATURE_FILE = "img.signature.file";
    public static final String IMG_SIGNATURE_COMPRESSED_FILE = "img.signature.compressed.file";

    public static final String SAVE_ENROLLMENT = "save.enrollment";
    public static final String BARCODE_FILE_PATH = "barcode.file.path";
    public static final String LDAP_DOMAIN = "ldap.domain";
    public static final String LDAP_URL = "ldap.url";
    public static final String SERIAL_PORT_FILE = "serial.port.file";
    public static final String EXPORT_FOLDER = "export.folder";
    public static final String IMPORT_JSON_FOLDER = "import.json.folder";
    public static final String CUR_PES_ID = "cur.pes.id";
    public static final String ENC_EXPORT_FOLDER = "enc.export.folder";
    public static final String CAMERA_ID = "camera.id";
    public static final String ENV = "env";
    public static final String LIC_IENGINE = "lic.iengine";
    public static final String MAFIS_API_URL = "mafis.api.url";
    public static final String ENROLLMENT_STATION_ID = "enrollment.station.id";
    public static final String ENROLLMENT_STATION_UNIT_ID = "enrollment.station.unit.id";
    public static final String FINGERPRINT_LIVENESS_MAX = "fingerprint.liveness.max";
    public static final String FINGERPRINT_LIVENESS_MIN = "fingerprint.liveness.min";
    public static final String FINGERPRINT_LIVENESS_VALUE = "fingerprint.liveness.value";
    public static final String TWO_FACTOR_AUTH_ENABLED = "two.factor.auth.enabled";
    public static final String APP_VERSION_NUMBER = "app.version.number";
    public static final String CARD_API_WHITELISTED_URL = "card.api.whitelisted.url";
    public static final String CARD_WHITELISTED_FILE = "card.whitelisted.file";
    public static final String FP_SEGMENT_WIDTH = "fp.segment.width";
    public static final String FP_SEGMENT_HEIGHT = "fp.segment.height";
    public static final String FP_NFIQ_VALUE = "fp.nfiq.value";
    public static final String CARD_API_CARD_READER_NAME = "card.api.card.reader.name";
    public static final String CARD_API_CARD_WRITER_NAME = "card.api.card.writer.name";
    public static final String CARD_API_SERVICE_RESTART_COMMAND = "card.api.service.restart.command";
    public static final String CARD_API_VERIFY_PIN = "card.api.verify.pin";
}
