package com.cdac.enrollmentstation.security;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 21/03/23
 */
public class Aes256Util {
    private static final Logger LOGGER = ApplicationLog.getLogger(Aes256Util.class);

    //Suppress default constructor for noninstantiability
    private Aes256Util() {
        throw new AssertionError("The AES256Util methods must be accessed statically.");
    }

    private static final SecureRandom random = new SecureRandom();
    private static final int IV_SIZE = 16;
    private static final ThreadLocal<Cipher> CIPHER_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        try {
            return Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    });

    public static String genUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static Key genKey(String secretKey) {
        return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

    }

    // returns encrypted bytes
    public static byte[] encrypt(String data, Key key) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] ivBytes = new byte[IV_SIZE];
            random.nextBytes(ivBytes);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            CIPHER_THREAD_LOCAL.get().init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            byte[] encryptedData = CIPHER_THREAD_LOCAL.get().doFinal(data.getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.write(ivBytes);
            byteArrayOutputStream.write(encryptedData);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    public static String decrypt(byte[] ivData, Key key) {
        try {
            byte[] ivBytes = Arrays.copyOfRange(ivData, 0, IV_SIZE);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            CIPHER_THREAD_LOCAL.get().init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            byte[] actualData = Arrays.copyOfRange(ivData, IV_SIZE, ivData.length);
            byte[] decryptedData = CIPHER_THREAD_LOCAL.get().doFinal(actualData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }
}
