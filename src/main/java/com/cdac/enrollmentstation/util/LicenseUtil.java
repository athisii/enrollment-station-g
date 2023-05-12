package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.innovatrics.iengine.ansiiso.AnsiIso;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class LicenseUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(LicenseUtil.class);
    private static AnsiIso ansiIso;

    //Suppress default constructor for noninstantiability
    private LicenseUtil() {
        throw new AssertionError("The LicenseUtil methods should be accessed statically");
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static AnsiIso getAnsiIso() throws IOException {
        // lic --> /etc/licence/iengine.lic
        if (ansiIso == null) {
            byte[] bytes = Files.readAllBytes(Paths.get(PropertyFile.getProperty(PropertyName.SLAP_LICENSE)));
            LOGGER.log(Level.INFO, () -> "license content : " + Arrays.toString(bytes));
            LOGGER.log(Level.INFO, () -> "hardware ID : " + Arrays.toString(AnsiIso.getHardwareId()));
            ansiIso = new AnsiIso();
            ansiIso.setLicenseContent(bytes, bytes.length);
            ansiIso.init();
        }
        return ansiIso;
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception

    public static void checkFpScannerLicense() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkIrisLicense() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkBarcodeLicense() {
    }

    // TODO -- to be implemented
    // throws GenericRuntime Exception
    public static void checkCardReaderLicense() {
    }
}
