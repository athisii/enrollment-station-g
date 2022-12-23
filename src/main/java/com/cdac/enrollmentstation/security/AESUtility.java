package com.cdac.enrollmentstation.security;


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AESUtility {
    private CryptoAES256 cryptoAES256;
    private Boolean cryptoMode256bit;

    public AESUtility(final String key) {
        this.cryptoMode256bit = true;
        this.cryptoAES256 = new CryptoAES256(key);

    }

    public void encryptImageStream(final BufferedImage image, final File file) {
        if (this.cryptoMode256bit) {
            try {
                this.cryptoAES256.encryptImageStream(image, file);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void encryptImg(final String inFilePath, final String outFilePath) {
        if (this.cryptoMode256bit) {
            try {
                this.cryptoAES256.encryptImg(inFilePath, outFilePath);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void decryptImg(final String inFilePath, final String outFilePath) {
        System.out.println("outFilePath :" + outFilePath);
        if (this.cryptoMode256bit) {
            try {
                this.cryptoAES256.decryptImg(inFilePath, outFilePath);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public Boolean encryptFile256(final String inFilePath, final String outFilePath) {
        if (this.cryptoMode256bit) {
            try {
                return this.cryptoAES256.encryptFile(inFilePath, outFilePath);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Boolean decryptFile256(final String inFilePath, final String outFilePath) {
        if (this.cryptoMode256bit) {
            try {
                return this.cryptoAES256.decryptFile(inFilePath, outFilePath);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Boolean encryptFile256(final File inFilePath, final File outFilePath) {
        if (this.cryptoMode256bit) {
            try {
                return this.cryptoAES256.encryptFile(inFilePath, outFilePath);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Boolean decryptFile256(final File inFilePath, final File outFilePath) {
        if (this.cryptoMode256bit) {
            try {
                return this.cryptoAES256.decryptFile(inFilePath, outFilePath);
            } catch (Exception ex) {
                Logger.getLogger(AESUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

//    public String decryptString256(final String encryptedData) throws Exception {
//        if (this.cryptoMode256bit) {
//            return this.cryptoAES256.decryptString(encryptedData);
//        }
//        return null;
//    }


}
