package com.cdac.enrollmentstation.security;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 21/03/23
 */
public class AesFileUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(AesFileUtil.class);

    //Suppress default constructor for noninstantiability
    private AesFileUtil() {
        throw new AssertionError("The AesFileUtil methods must be accessed statically.");
    }

    private static final String PASSWORD = "P0rt@b1eEnr011ment";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int SALT_SIZE = 8;
    private static final int IV_SIZE = 16;

    private static final ThreadLocal<Cipher> CIPHER_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        try {
            return Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    });

    public static void encrypt(String jsonData, Path encOutputPath) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] saltBytes = new byte[SALT_SIZE];
            secureRandom.nextBytes(saltBytes);
            SecretKey secretKey = getSecretKey(PASSWORD, saltBytes);
            byte[] ivBytes = new byte[IV_SIZE];
            secureRandom.nextBytes(ivBytes);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            CIPHER_THREAD_LOCAL.get().init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encryptedData = CIPHER_THREAD_LOCAL.get().doFinal(jsonData.getBytes(StandardCharsets.UTF_8));
            // writes salt and iv to the file
            // must follow this order while decrypting
            byteArrayOutputStream.write(saltBytes);
            byteArrayOutputStream.write(ivBytes);
            byteArrayOutputStream.write(encryptedData);
            //saves to file
            Files.write(encOutputPath, byteArrayOutputStream.toByteArray());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }


    public static String decrypt(Path path) {
        try {
            byte[] readBytes = Files.readAllBytes(path);
            // read order followed as when encrypting
            byte[] saltBytes = Arrays.copyOfRange(readBytes, 0, SALT_SIZE);
            byte[] ivBytes = Arrays.copyOfRange(readBytes, SALT_SIZE, SALT_SIZE + IV_SIZE);
            byte[] actualData = Arrays.copyOfRange(readBytes, SALT_SIZE + IV_SIZE, readBytes.length);

            SecretKey secretKey = getSecretKey(PASSWORD, saltBytes);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            CIPHER_THREAD_LOCAL.get().init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            byte[] decryptedBytes = CIPHER_THREAD_LOCAL.get().doFinal(actualData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }

    }

    public static SecretKey getSecretKey(String password, byte[] salt) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            return new SecretKeySpec(secretKey.getEncoded(), "AES");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }
}
