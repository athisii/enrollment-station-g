package com.cdac.enrollmentstation.security;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii
 * @version 1.0
 * @since 11/1/24
 */

public class HashUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(HmacUtil.class);

    //Suppress default constructor for noninstantiability
    private HashUtil() {
        throw new AssertionError("The HashUtil methods  must be accessed statically.");
    }

    public static String hashPassword(String password) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public static boolean isSame(String base64EncodedString1, String base64EncodedString2) {
        return base64EncodedString1.equals(base64EncodedString2);
    }
}
