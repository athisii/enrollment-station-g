package com.cdac.enrollmentstation.security;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.util.PropertyFile;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 21/03/23
 */
public class PkiUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(PkiUtil.class);

    //Suppress default constructor for noninstantiability
    private PkiUtil() {
        throw new AssertionError("The PkiUtil methods must be accessed statically.");
    }

    private static final KeyStore keyStore;
    private static final InputStream inputStream;
    private static final String PASSWORD;
    private static final String ALIAS;
    private static final KeyPair keyPair;

    private static final ThreadLocal<Cipher> CIPHER_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        try {
            return Cipher.getInstance("RSA");
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    });

    static {
        try {
            PASSWORD = PropertyFile.getProperty(PropertyName.JKS_PASSWORD);
            ALIAS = PropertyFile.getProperty(PropertyName.JKS_ALIAS);
            inputStream = new FileInputStream(PropertyFile.getProperty(PropertyName.JKS_CERT_FILE));
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inputStream, PASSWORD.toCharArray());
            if (!keyStore.containsAlias(ALIAS)) {
                throw new GenericException("Not found for key with alias: " + ALIAS);
            }
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(ALIAS, PASSWORD.toCharArray());
            Certificate certificate = keyStore.getCertificate(ALIAS);
            PublicKey publicKey = certificate.getPublicKey();
            keyPair = new KeyPair(publicKey, privateKey);
        } catch (GeneralSecurityException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }


    // return encrypted bytes
    public static synchronized byte[] encrypt(String data) {
        try {
            CIPHER_THREAD_LOCAL.get().init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            return CIPHER_THREAD_LOCAL.get().doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }

    }

    // return decrypted String
    public static synchronized String decrypt(byte[] encryptedData) {
        try {
            CIPHER_THREAD_LOCAL.get().init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedInput = CIPHER_THREAD_LOCAL.get().doFinal(encryptedData);
            return new String(decryptedInput, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }
}
