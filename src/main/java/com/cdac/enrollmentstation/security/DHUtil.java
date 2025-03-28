package com.cdac.enrollmentstation.security;

import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DHUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(DHUtil.class);

    private DHUtil() {
        throw new AssertionError("The DHUtil methods must be accessed statically.");
    }

    public static void test() {
        KeyPair keyPair = generateKeyPair("EC", "secp256r1");
        byte[] sharedSecretBytes = generateSharedSecretBytes(keyPair.getPrivate(), keyPair.getPublic(), "ECDH");
        byte[] aesKeyBytes = Arrays.copyOfRange(sharedSecretBytes, 0, 16);
    }

    public static KeyPair generateKeyPair(String algorithm, String standardName) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(standardName);
            keyPairGenerator.initialize(ecSpec);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Failed to generate shared secret. " + ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    public static byte[] generateSharedSecretBytes(PrivateKey privateKey, PublicKey publicKey, String algorithm) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance(algorithm);
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            byte[] sharedSecretBytes = keyAgreement.generateSecret();
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(sharedSecretBytes);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Failed to generate shared secret. " + ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    public static PrivateKey generatePrivateKey(byte[] bytes, String algorithm) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Failed to generate privateKey. " + ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    public static PublicKey generatePublicKey(byte[] bytes, String algorithm) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, () -> "***Failed to generate public Key. " + ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    public static String toUnsignedNumbers(byte[] bytes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bytes.length; i++) {
            int unsignedValue = bytes[i] & 0xFF; // Convert to unsigned (0–255) if needed
            if (i == bytes.length - 1) {
                sb.append(unsignedValue).append("]");
            } else {
                sb.append(unsignedValue).append(", ");
            }
        }
        return sb.toString();
    }
}
