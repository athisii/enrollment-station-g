package com.cdac.enrollmentstation.security;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptoAES256 {
    private static final String ALGO = "AES";
    private byte[] keyValue;
    private String imgExt = "JPG";


    public CryptoAES256() {

    }

    public CryptoAES256(final String strKey) {
        this.imgExt = "JPG";
        try {
            this.keyValue = strKey.getBytes("UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Key generateKey() {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] digest = sha.digest(this.keyValue);
        digest = Arrays.copyOf(digest, 16);
        final Key key = new SecretKeySpec(digest, "AES");
        return key;
    }


    public void encryptImageStream(final BufferedImage image, final File file) {
        FileOutputStream output = null;
        CipherOutputStream cos = null;
        try {
            final Key key = this.generateKey();
            Cipher cpr = null;
            try {
                cpr = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchPaddingException ex) {
                Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                cpr.init(1, key);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                output = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
            }
            cos = new CipherOutputStream(output, cpr);
            try {
                ImageIO.write(image, "JPG", cos);
            } catch (IOException ex) {
                Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (cos != null) {
                try {
                    cos.close();
                } catch (IOException ex) {
                    Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void encryptImg(final String inFilePath, final String outFilePath) throws Exception {
        FileOutputStream output = null;
        CipherOutputStream cos = null;
        try {
            final Key key = this.generateKey();
            final File imgPath = new File(inFilePath);
            final BufferedImage bufferedImageIn = ImageIO.read(imgPath);
            final Cipher cpr = Cipher.getInstance("AES");
            cpr.init(1, key);
            output = new FileOutputStream(outFilePath);
            cos = new CipherOutputStream(output, cpr);
            ImageIO.write(bufferedImageIn, "JPG", cos);
        } finally {
            if (output != null) {
                output.close();
            }
            if (cos != null) {
                cos.close();
            }
        }
    }

    public void decryptImg(final String inFilePath, final String outFilePath) throws Exception {
        FileInputStream fileinput = null;
        CipherInputStream cis = null;
        try {
            final Key key = this.generateKey();
            final Cipher cpr = Cipher.getInstance("AES");
            cpr.init(2, key);
            fileinput = new FileInputStream(inFilePath);
            cis = new CipherInputStream(fileinput, cpr);
            final BufferedImage input = ImageIO.read(cis);
            ImageIO.write(input, "JPG", new File(outFilePath));
        } finally {
            if (fileinput != null) {
                fileinput.close();
            }
            if (cis != null) {
                cis.close();
            }
        }
    }

    public boolean encryptFile(final String inFilePath, final String outFilePath) throws Exception {
        FileInputStream fileinput = null;
        CipherInputStream cis = null;
        FileOutputStream fos = null;
        try {
            final Key key = this.generateKey();
            final Cipher cpr = Cipher.getInstance("AES");
            cpr.init(1, key);
            fileinput = new FileInputStream(inFilePath);
            cis = new CipherInputStream(fileinput, cpr);
            fos = new FileOutputStream(outFilePath);
            this.writeData(cis, fos);
            return true;
        } finally {
            if (fileinput != null) {
                fileinput.close();
            }
            if (cis != null) {
                cis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public boolean decryptFile(final String inFilePath, final String outFilePath) throws Exception {
        FileInputStream fileinput = null;
        CipherInputStream cis = null;
        FileOutputStream fos = null;
        try {
            final Key key = this.generateKey();
            final Cipher cpr = Cipher.getInstance("AES");
            cpr.init(2, key);
            fileinput = new FileInputStream(inFilePath);
            cis = new CipherInputStream(fileinput, cpr);
            fos = new FileOutputStream(outFilePath);
            this.writeData(cis, fos);
            return true;
        } finally {
            if (fileinput != null) {
                fileinput.close();
            }
            if (cis != null) {
                cis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public boolean encryptFile(final File inFilePath, final File outFilePath) throws Exception {
        FileInputStream fileinput = null;
        CipherInputStream cis = null;
        FileOutputStream fos = null;
        try {
            final Key key = this.generateKey();
            final Cipher cpr = Cipher.getInstance("AES");
            cpr.init(1, key);
            fileinput = new FileInputStream(inFilePath);
            cis = new CipherInputStream(fileinput, cpr);
            fos = new FileOutputStream(outFilePath);
            this.writeData(cis, fos);
            return true;
        } finally {
            if (fileinput != null) {
                fileinput.close();
            }
            if (cis != null) {
                cis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public boolean decryptFile(final File inFilePath, final File outFilePath) throws Exception {
        FileInputStream fileinput = null;
        CipherInputStream cis = null;
        FileOutputStream fos = null;
        try {
            final Key key = this.generateKey();
            final Cipher cpr = Cipher.getInstance("AES");
            cpr.init(2, key);
            fileinput = new FileInputStream(inFilePath);
            cis = new CipherInputStream(fileinput, cpr);
            fos = new FileOutputStream(outFilePath);
            this.writeData(cis, fos);
            return true;
        } finally {
            if (fileinput != null) {
                fileinput.close();
            }
            if (cis != null) {
                cis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public SecretKey getAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        byte[] key = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        keyGen.init(256, random);

        return keyGen.generateKey();
    }

    public String generateRandomUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    public static byte[] getRandomNonce() {
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public String encryptString(final String jsonData, Key key) {
        //final Key key = this.generateKey();
        Cipher c = null;
        try {
            c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            c.init(1, key, ivSpec);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("before enc");
        //final byte[] decodedValue = Base64.decodeBase64(encryptedData);
        byte[] encValue = null;
        try {
            encValue = c.doFinal(jsonData.getBytes());
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(iv);
        } catch (IOException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            baos.write(encValue);
        } catch (IOException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }
        final byte[] encValueWithIV = baos.toByteArray();
        System.out.println("After enc");
        final String encryptedValue = java.util.Base64.getEncoder().encodeToString(encValueWithIV);
        return encryptedValue;
    }

    public String decryptString(final String jsonData) {
        System.out.println("keyvalue :" + keyValue + " " + ALGO);
        Key key = new SecretKeySpec(keyValue, "AES");
        System.out.println("After key init");
        final Cipher c;
        final byte[] decValue;
        try {
            final byte[] decodedValue = Base64.decodeBase64(jsonData);
            byte[] iv = Arrays.copyOfRange(decodedValue, 0, 16);
            System.out.println("iv length :" + iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            System.out.println("ivspec length :" + ivSpec.getIV().length);
            c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(2, key, ivSpec);
            System.out.println("before decrypt" + jsonData);

            byte[] actualdata = Arrays.copyOfRange(decodedValue, 16, decodedValue.length);
            System.out.println("decoded bytes " + decodedValue.toString());
            decValue = c.doFinal(actualdata);

            // final byte[] decodedValue = Base64.decodeBase64(jsonData);

            System.out.println("After dec");
            final String decryptedValue = new String(decValue);
            return decryptedValue;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String decryptStringSK(final String jsonData, Key key) {
        System.out.println("keyvalue :" + keyValue + " " + ALGO);

        final Cipher c;
        final byte[] decValue;
        try {
            final byte[] decodedValue = Base64.decodeBase64(jsonData);
            byte[] iv = Arrays.copyOfRange(decodedValue, 0, 16);
            System.out.println("iv length :" + iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            System.out.println("ivspec length :" + ivSpec.getIV().length);
            c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(2, key, ivSpec);
            System.out.println("before decrypt" + jsonData);

            byte[] actualdata = Arrays.copyOfRange(decodedValue, 16, decodedValue.length);
            System.out.println("decoded bytes " + decodedValue.toString());
            decValue = c.doFinal(actualdata);

            // final byte[] decodedValue = Base64.decodeBase64(jsonData);

            System.out.println("After dec");
            final String decryptedValue = new String(decValue);
            return decryptedValue;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CryptoAES256.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }


    private void writeData(final InputStream is, final OutputStream os) throws IOException {
        try {
            final byte[] buffer = new byte[64];
            //Changed on 260522 for code review
            int numBytes;
            // int numBytes = is.read(buffer);
            while ((numBytes = is.read(buffer)) != -1) {
                //  while ((numBytes) != -1) {
                os.write(buffer, 0, numBytes);
            }
            //os.write(is.readAllBytes()); 
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    public Key generateKey32(String key) {


        Key aeskey = new SecretKeySpec(key.getBytes(), "AES");
        return aeskey;
    }


    public static void main(final String[] args) throws Exception {
        String crypt = "CAA8F9CB2FC642CA";
        final CryptoAES256 algo = new CryptoAES256(crypt);
    }
}
