package com.cdac.enrollmentstation.util;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.dto.CRWaitForConnectResDto;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import org.bouncycastle.asn1.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.util.Asn1CardTokenUtil.*;

// test util for  writing photo file to token
public class TestUtil {
    private static final Logger LOGGER = ApplicationLog.getLogger(TestUtil.class);
    public static final int SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC = 500;


    public static void startProceduralCall() throws Exception {

        LOGGER.log(Level.SEVERE, () -> "**** Reader 1 name: " + MANTRA_CARD_READER_NAME);
        LOGGER.log(Level.SEVERE, () -> "**** Reader 2 name: " + MANTRA_CARD_WRITER_NAME);


//        getBase64EncodedStringAsn1File("/Photo.txt");
        /*
            DeInitialize
            Initialize
            waitForConnect - card
            selectApp - card
            waitForConnect - token
            selectApp - token
            read data(static) - token
            read cert - card
            verify cert - token handle
            pki auth - (token handle, card handle)
            token write:
                  1. photo

         */

        LOGGER.log(Level.SEVERE, () -> "**** Calling deInitialize API");
        Asn1CardTokenUtil.deInitialize();
        LOGGER.log(Level.SEVERE, () -> "**** Calling initialize API");
        Asn1CardTokenUtil.initialize();
        // setup reader; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Card: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitFocConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.INFO, () -> "***Card: Calling waitForConnect API.");
        CRWaitForConnectResDto crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_READER_NAME);
        int cardHandle = crWaitForConnectResDto.getHandle();
        LOGGER.log(Level.INFO, () -> "***Card: Calling selectApp API.");
        Asn1CardTokenUtil.selectApp(CARD_TYPE_NUMBER, cardHandle);

        // setup writer; need to add a delay for some milliseconds
        try {
            LOGGER.log(Level.INFO, () -> "***Token: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitFocConnect API call.");
            Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
            Thread.currentThread().interrupt();
        }

        LOGGER.log(Level.INFO, () -> "***Token: Calling waitForConnect API.");
        crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_WRITER_NAME);
        int tokenHandle = crWaitForConnectResDto.getHandle();
        LOGGER.log(Level.INFO, () -> "***Token: Calling selectApp API.");
        Asn1CardTokenUtil.selectApp(TOKEN_TYPE_NUMBER, tokenHandle);

        LOGGER.log(Level.INFO, () -> "***Token: Calling readData API to read static data to get the token number.");
        byte[] asn1EncodedTokenStaticData = Asn1CardTokenUtil.readBufferedData(tokenHandle, CardTokenFileType.STATIC);
        // not needed now but needed in production
        String tokenNumber = new String(extractFromAsn1EncodedStaticData(asn1EncodedTokenStaticData, 1), StandardCharsets.UTF_8); // not needed now
        LOGGER.log(Level.INFO, () -> "***Token: Token number: " + tokenNumber);

        // read cert now
        LOGGER.log(Level.INFO, () -> "***Card: Calling readData API for reading system certificate.");
        byte[] systemCertificate = Asn1CardTokenUtil.readBufferedData(cardHandle, CardTokenFileType.SYSTEM_CERTIFICATE);

        LOGGER.log(Level.INFO, () -> "***Token: Calling verifyCertificate API: handle1=token, handle2=card");
        Asn1CardTokenUtil.verifyCertificate(tokenHandle, WHICH_TRUST, WHICH_CERTIFICATE, systemCertificate);

        LOGGER.log(Level.INFO, () -> "***Token: Calling pkiAuth API: handle1=token, handle2=card");
        Asn1CardTokenUtil.pkiAuth(tokenHandle, cardHandle);


        LOGGER.log(Level.INFO, () -> "***Token: Writing photo file to token as Sequence (priyanka image).");
        storeAsn1SequenceFromImageFile(tokenHandle, "/image.png");
        LOGGER.log(Level.INFO, () -> "***Photo file successfully written to token.");

        LOGGER.log(Level.INFO, () -> "***Token: Writing photo file to token as OctetString (priyanka image).");
        Asn1CardTokenUtil.encodeAndStorePhotoFile(tokenHandle, Base64.getEncoder().encodeToString(loadImageFile("/image.png")));
        LOGGER.log(Level.INFO, () -> "***Photo file successfully written to token.");


    }


    public static String getBase64EncodedStringAsn1File(String filename) throws IOException {
        InputStream resourceAsStream = App.class.getResourceAsStream(filename);
        byte[] bytes = resourceAsStream.readAllBytes();
        LOGGER.log(Level.INFO, () -> "****Loaded File Byte size: " + bytes.length);
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1Primitive asn1Primitive = asn1InputStream.readObject();
        if (asn1Primitive instanceof ASN1Sequence) {
            ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(asn1Primitive);
            ASN1Encodable asn1SequenceObject = asn1Sequence.getObjectAt(0);
            if (asn1SequenceObject instanceof ASN1OctetString) {
                LOGGER.log(Level.INFO, "****ExtractFromAsn1EncodedStaticData: OctetString type parsed.");
                byte[] imagBytes = ((ASN1OctetString) asn1SequenceObject).getOctets();// encoded in hex
                Files.write(Path.of("cdac.png"), imagBytes);
            }

            return Base64.getEncoder().encodeToString(bytes);
        }
        return Base64.getEncoder().encodeToString("photo files".getBytes());
    }

    public static byte[] loadImageFile(String filename) throws IOException {
        InputStream resourceAsStream = App.class.getResourceAsStream(filename);
        return resourceAsStream.readAllBytes();
    }

    public static void storeAsn1SequenceFromImageFile(int handle, String filename) throws IOException {
        InputStream resourceAsStream = App.class.getResourceAsStream(filename);
        byte[] imageBytes = resourceAsStream.readAllBytes();
        DEROctetString octetString = new DEROctetString(imageBytes);
        DERSequence sequence = new DERSequence(octetString);
        byte[] bytes = sequence.getEncoded();
        if (imageBytes.length > MAX_PHOTO_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStorePhotoFileError: Photo size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The photo file size exceeded the allowed limit.");
        }
        LOGGER.log(Level.INFO, () -> "***writing PhotoFile to token.");
        Asn1CardTokenUtil.storeBufferedData(handle, CardTokenFileType.PHOTO_FILE, bytes);
    }
}
