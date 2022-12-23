/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.security;

/**
 * @author root
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HmacUtils {

    public String generateHmac256(String message, byte[] key) {
        byte[] bytes = null;
        bytes = hmac("HmacSHA256", key, message.getBytes());
        return bytesToHex(bytes);
    }

    byte[] hmac(String algorithm, byte[] key, byte[] message) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HmacUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            mac.init(new SecretKeySpec(key, algorithm));
        } catch (InvalidKeyException ex) {
            Logger.getLogger(HmacUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mac.doFinal(message);
    }

    String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
//        String valueToDigest = "The quick brown fox jumps over the lazy dog";
//        byte[] key = "secret".getBytes();

        HmacUtils hm = new HmacUtils();
        //String valueToDigest = new String(Files.readAllBytes(Paths.get("/home/boss/details_biometric/encryptedjson.txt")));
        //String valueToDigest = new String(Files.readAllBytes(Paths.get("/home/boss/test.txt")));

        //String valueToDigest = "ImYJPEZGBZySiy9G/5ZuUHZ7bLTyJYO6SQdrpC+4iG+Td4zbmlo2r8x1eHOaW2H/4DQuwDtKWyBrtjmiI700eUrgOh6DifytTmLaByZBAWk=";
        //byte[] key = "733fc4d86fba457b95daa2877d523a7a".getBytes();
        //String messageDigest = hm.generateHmac256(valueToDigest, key);
        //System.out.println(messageDigest);
    }
}

