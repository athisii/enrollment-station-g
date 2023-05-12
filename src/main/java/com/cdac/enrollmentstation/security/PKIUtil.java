/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.security;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.util.TestProp;
import com.cdac.enrollmentstation.logging.ApplicationLogOld;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author root
 */
public class PKIUtil {
    TestProp prop = new TestProp();

    //For Application Log
    ApplicationLogOld appLog = new ApplicationLogOld();
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    Handler handler;

    public PKIUtil() {
        //this.handler = appLog.getLogger();
        // LOGGER.addHandler(handler);
    }

    public String encrypt(String inputStr) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // for clarity, ignoring exceptions and failures
        String certfile = null;

        certfile = prop.getProp().getProperty("certfile");
        if (certfile.isBlank() || certfile.isEmpty() || certfile == null) {
            //System.out.println("The property 'certfile' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'certfile' is empty, Please add it in properties");
            return null;
        }
        //commented on 220422
        //InputStream keystoreStream = new FileInputStream("/usr/share/enrollment/jks/cacert.jks");
        InputStream keystoreStream = new FileInputStream(certfile);
        String encryptedSessionKey = "";
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreStream, "12qwaszx".toCharArray());
            if (!keystore.containsAlias("sample encryption test")) {
                throw new RuntimeException("Alias for key not found");
            }
            Key key = (PrivateKey) keystore.getKey("sample encryption test", "12qwaszx".toCharArray());
            java.security.cert.Certificate cert = keystore.getCertificate("sample encryption test");

            PublicKey pub = cert.getPublicKey();
            byte[] encodedCertKey = cert.getEncoded();
            byte[] encodedPublicKey = pub.getEncoded();
            String b64PublicKey = Base64.getMimeEncoder().encodeToString(encodedPublicKey);
            String b64CertKey = Base64.getMimeEncoder().encodeToString(encodedCertKey);
            System.out.println("public key : " + b64PublicKey);
            KeyPair kPair = new KeyPair(pub, (PrivateKey) key);
            Cipher cipher = Cipher.getInstance("RSA");


            //Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

            cipher.init(Cipher.ENCRYPT_MODE, kPair.getPublic());
            //byte[] textEncrypted = cipher.doFinal("hello world123".getBytes());
            byte[] textEncrypted = cipher.doFinal(inputStr.getBytes());
            System.out.println("encrypted: " + new String(textEncrypted));
            encryptedSessionKey = new String(textEncrypted);

            cipher.init(Cipher.DECRYPT_MODE, kPair.getPrivate());
            byte[] textDecrypted = cipher.doFinal(textEncrypted);
            System.out.println("decrypted: " + new String(textDecrypted));
            //keystoreStream.close();
            //return "";
            //return encryptedSessionKey;
        } catch (Exception e) {
            //System.out.println("Exception in Encrypt:"+e);
            LOGGER.log(Level.INFO, "Exception in Encrypt:" + e);
        } finally {
            keystoreStream.close();
        }
        return encryptedSessionKey;

    }

    public byte[] encrypt_test(String inputStr) {
        // for clarity, ignoring exceptions and failures
        String certfile = null;

        certfile = prop.getProp().getProperty("certfile");
        if (certfile.isBlank() || certfile.isEmpty() || certfile == null) {
            //System.out.println("The property 'certfile' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'certfile' is empty, Please add it in properties");
            return null;
        }
        //commented on 220422
        //InputStream keystoreStream = new FileInputStream("/usr/share/enrollment/jks/cacert.jks");
        InputStream keystoreStream = null;
        try {
            keystoreStream = new FileInputStream(certfile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PKIUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] textEncrypted = null;
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreStream, "12qwaszx".toCharArray());
            if (!keystore.containsAlias("sample encryption test")) {
                throw new RuntimeException("Alias for key not found");
            }
            Key key = (PrivateKey) keystore.getKey("sample encryption test", "12qwaszx".toCharArray());
            java.security.cert.Certificate cert = keystore.getCertificate("sample encryption test");

            PublicKey pub = cert.getPublicKey();
            byte[] encodedCertKey = cert.getEncoded();
            byte[] encodedPublicKey = pub.getEncoded();
            String b64PublicKey = Base64.getMimeEncoder().encodeToString(encodedPublicKey);
            String b64CertKey = Base64.getMimeEncoder().encodeToString(encodedCertKey);
            System.out.println("public key : " + b64PublicKey);
            KeyPair kPair = new KeyPair(pub, (PrivateKey) key);
            Cipher cipher = Cipher.getInstance("RSA");
            // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");


            cipher.init(Cipher.ENCRYPT_MODE, kPair.getPublic());
            //byte[] textEncrypted = cipher.doFinal("hello world123".getBytes());
            textEncrypted = cipher.doFinal(inputStr.getBytes());
            System.out.println("encrypted: " + new String(textEncrypted));
            String encryptedSessionKey = new String(textEncrypted);

            cipher.init(Cipher.DECRYPT_MODE, kPair.getPrivate());
            byte[] textDecrypted = cipher.doFinal(textEncrypted);
            System.out.println("decrypted: " + new String(textDecrypted));
            //keystoreStream.close();
            //return "";
            //return textEncrypted;
        } catch (Exception e) {
            //System.out.println("Exception in Encrypt:"+e);
            LOGGER.log(Level.INFO, "Exception :", e);
        } finally {
            try {
                keystoreStream.close();
            } catch (IOException ex) {
                Logger.getLogger(PKIUtil.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO, "IOException :" + ex);
            }
        }
        return textEncrypted;

    }

    public String decrypt_test(String inputStr) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // for clarity, ignoring exceptions and failures
        String certfile = null;

        certfile = prop.getProp().getProperty("certfile");
        if (certfile.isBlank() || certfile.isEmpty() || certfile == null) {
            //System.out.println("The property 'certfile' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'certfile' is empty, Please add it in properties");
            return null;
        }
        //commented on 220422
        //InputStream keystoreStream = new FileInputStream("/usr/share/enrollment/jks/cacert.jks");
        InputStream keystoreStream = new FileInputStream(certfile);
        String decryptedSessionKey = "";
        try {

            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreStream, "12qwaszx".toCharArray());
            if (!keystore.containsAlias("sample encryption test")) {
                throw new RuntimeException("Alias for key not found");
            }
            Key key = (PrivateKey) keystore.getKey("sample encryption test", "12qwaszx".toCharArray());
            java.security.cert.Certificate cert = keystore.getCertificate("sample encryption test");

            PublicKey pub = cert.getPublicKey();
            byte[] encodedCertKey = cert.getEncoded();
            byte[] encodedPublicKey = pub.getEncoded();
            String b64PublicKey = Base64.getMimeEncoder().encodeToString(encodedPublicKey);
            String b64CertKey = Base64.getMimeEncoder().encodeToString(encodedCertKey);
            System.out.println("public key : " + b64PublicKey);
            KeyPair kPair = new KeyPair(pub, (PrivateKey) key);
            Cipher cipher = Cipher.getInstance("RSA");
            //Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");


            // cipher.init(Cipher.ENCRYPT_MODE, kPair.getPublic());
            //byte[] textEncrypted = cipher.doFinal("hello world123".getBytes());
//            byte[] textEncrypted = cipher.doFinal(inputStr.getBytes());
//            System.out.println("encrypted: "+new String(textEncrypted));
//            String encryptedSessionKey = new String(textEncrypted);

            cipher.init(Cipher.DECRYPT_MODE, kPair.getPrivate());
            byte[] textDecrypted = cipher.doFinal(inputStr.getBytes());
            System.out.println("decrypted: " + new String(textDecrypted));
            decryptedSessionKey = new String(textDecrypted);
            //keystoreStream.close();
            //return "";
            //return decryptedSessionKey;
        } catch (Exception e) {
            //System.out.println("Exception in decrypt:"+e);
            LOGGER.log(Level.INFO, "Exception in decrypt: :" + e);

        } finally {
            keystoreStream.close();
        }
        return decryptedSessionKey;
    }

    public String decrypt(byte[] inputStr) {
        // for clarity, ignoring exceptions and failures
        String certfile = null;

        certfile = prop.getProp().getProperty("certfile");
        if (certfile.isBlank() || certfile.isEmpty() || certfile == null) {
            //System.out.println("The property 'certfile' is empty, Please add it in properties");
            LOGGER.log(Level.INFO, "The property 'certfile' is empty, Please add it in properties");
            return null;
        }
        //commented on 220422
        //InputStream keystoreStream = new FileInputStream("/usr/share/enrollment/jks/cacert.jks");
        InputStream keystoreStream = null;
        try {
            keystoreStream = new FileInputStream(certfile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PKIUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        String decryptedSessionKey = "";
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreStream, "12qwaszx".toCharArray());
            if (!keystore.containsAlias("sample encryption test")) {
                throw new RuntimeException("Alias for key not found");
            }
            Key key = (PrivateKey) keystore.getKey("sample encryption test", "12qwaszx".toCharArray());
            java.security.cert.Certificate cert = keystore.getCertificate("sample encryption test");

            PublicKey pub = cert.getPublicKey();
            byte[] encodedCertKey = cert.getEncoded();
            byte[] encodedPublicKey = pub.getEncoded();
            String b64PublicKey = Base64.getMimeEncoder().encodeToString(encodedPublicKey);
            String b64CertKey = Base64.getMimeEncoder().encodeToString(encodedCertKey);
            System.out.println("public key : " + b64PublicKey);
            KeyPair kPair = new KeyPair(pub, (PrivateKey) key);
            Cipher cipher = Cipher.getInstance("RSA");
            //Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            //Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");


            cipher.init(Cipher.ENCRYPT_MODE, kPair.getPublic());

            byte[] textEncrypted = cipher.doFinal("hello world123".getBytes());
            System.out.println("encrypted: " + new String(textEncrypted));


            cipher.init(Cipher.DECRYPT_MODE, kPair.getPrivate());
//            byte[] textDecrypted = cipher.doFinal(textEncrypted);
//            System.out.println("decrypted: "+new String(textDecrypted));
            byte[] textDecrypted = cipher.doFinal(inputStr);
            System.out.println("decrypted Session Key: " + new String(textDecrypted));
            decryptedSessionKey = new String(textDecrypted);
            //keystoreStream.close();
            // return decryptedSessionKey;
        } catch (Exception e) {
            //System.out.println("Exception in decrypt:"+e);
            LOGGER.log(Level.INFO, "Exception in decrypt: :", e);
        } finally {
            try {
                keystoreStream.close();
            } catch (IOException ex) {
                Logger.getLogger(PKIUtil.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.INFO, "IOException in decrypt: :" + ex);
            }
        }
        return decryptedSessionKey;
    }

}
