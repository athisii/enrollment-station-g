package com.cdac.enrollmentstation.util;


import com.cdac.enrollmentstation.api.LocalNavalWebServiceApi;
import com.cdac.enrollmentstation.constant.ApplicationConstant;
import com.cdac.enrollmentstation.constant.PropertyName;
import com.cdac.enrollmentstation.dto.*;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.exception.NoReaderException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.DefaultValidityFile;
import com.cdac.enrollmentstation.model.DynamicFile;
import com.cdac.enrollmentstation.model.LabourFp;
import com.cdac.enrollmentstation.model.SpecialAccessFile;
import org.bouncycastle.asn1.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;

public class Asn1CardTokenUtil {
    /*
      -------------------------------------------------------------------------------------------------------------------------
      -                                                  ASN1 DATA TYPES                                                      -
      -------------------------------------------------------------------------------------------------------------------------
      -  Sr.No   Data Type           Class   Form       Tag Number(binary)  Tag Number(dec)    Byte(binary)    Byte(decimal)  -
      -------------------------------------------------------------- ----------------------------------------------------------
      -   1       EOC(marker)          00     0             00000                  0             00000000            0        -
      -   2       BOOLEAN              00     0             00001                  1             00000001            1        -
      -   3       INTEGER              00     0             00010                  2             00000010            2        -
      -   4       BIT STRING           00     0             00011                  3             00000011            3        -
      -   5       OCTET STRING         00     0             00100                  4             00000100            4        -
      -   6       NULL                 00     0             00101                  5             00000101            5        -
      -   7       OBJECT IDENTIFIER    00     0             00110                  6             00000110            6        -
      -   8       REAL                 00     0             00111                  7             00000111            7        -
      -   9       SEQUENCE             00     1             10000                  16            00110000            48       -
      -   10      SET                  00     1             10001                  17            00110001            49       -
      -   11      PrintableString      00     0             10011                  19            00010011            19       -
      -   12      IA5String            00     0             10110                  22            00010110            22       -
      -   -        -                    -     -               -                    -                 -               -        -
      -   -        -                    -     -               -                    -                 -               -        -
      -   -        -                    -     -               -                    -                 -               -        -
      -----------------------------------------------------------------------------------------------------------------------------


                                NOIDA'S API CONTRACT
      .............................................................................
      . Input Type           Acronym                         Value     Max Byte   .
      .............................................................................
      . card-type            Naval ID Card                     4                  .
      .                      Token                             5                  .
      .............................................................................
      . which-data           Static File                       21       500       .
      .                      Default Access Validity           22       28        .
      .                      Special Access Permission File    24       15000     .
      .                      Fingerprint File                  25       5164      .
      .                      System Certificate                32       2806      .
      .                      Signature File1                   33       1655      .
      .                      Signature File2                   34                 .
      .                      Signature File3                   35       1655      .
      .                      Dynamic File                      36       500       .
      .                      CSN                               42                 .
      .                      Photo File                        43       10250     .
      .............................................................................
      . which-certificate    System certificate                14                 .
      .............................................................................
      . which-trust          AFSAC                             11                 .
      .............................................................................
      .                                                           Total: 37,358   .
      .............................................................................
     */

    private static final Logger LOGGER = ApplicationLog.getLogger(Asn1CardTokenUtil.class);
    private static int jniErrorCode;
    public static final int SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC = 500;

    public static final int CARD_TYPE_NUMBER = 4; // Naval ID/Contractor Card
    public static final int TOKEN_TYPE_NUMBER = 5; // Token 5
    public static final int WHICH_TRUST = 11; // AFSAC
    public static final int WHICH_CERTIFICATE = 14; // System certificate
    public static final int MAX_BUFFER_SIZE = 1024; // Max bytes card can handle
    public static final int MAX_DYNAMIC_FILE_SIZE = 500;
    public static final int MAX_DEFAULT_ACCESS_VALIDITY_SIZE = 28;
    public static final int MAX_SPECIAL_ACCESS_PERMISSION_FILE_SIZE = 15000;
    public static final int MAX_SYSTEM_CERTIFICATE_SIZE = 2806;
    public static final int MAX_SIGNATURE_FILE_SIZE = 1655;
    public static final int MAX_FINGERPRINT_FILE_SIZE = 5164;
    public static final int MAX_PHOTO_FILE_SIZE = 10250;
    public static final String MANTRA_CARD_READER_NAME;
    public static final String MANTRA_CARD_WRITER_NAME;
    public static final String CARD_API_SERVICE_RESTART_COMMAND;

    static {
        try {
            MANTRA_CARD_READER_NAME = PropertyFile.getProperty(PropertyName.CARD_API_CARD_READER_NAME).trim();
            MANTRA_CARD_WRITER_NAME = PropertyFile.getProperty(PropertyName.CARD_API_CARD_WRITER_NAME).trim();
            CARD_API_SERVICE_RESTART_COMMAND = PropertyFile.getProperty(PropertyName.CARD_API_SERVICE_RESTART_COMMAND).trim();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "No entry for '" + PropertyName.CARD_API_CARD_READER_NAME + "/" + PropertyName.CARD_API_CARD_WRITER_NAME + "/" + PropertyName.CARD_API_SERVICE_RESTART_COMMAND + "' in " + ApplicationConstant.DEFAULT_PROPERTY_FILE);
            throw new GenericException(ex.getMessage());
        }
    }

    public enum CardTokenFileType {
        STATIC(21),
        DEFAULT_ACCESS_VALIDITY(22),
        SPECIAL_ACCESS_PERMISSION_FILE(24),
        FINGERPRINT_FILE(25),
        SYSTEM_CERTIFICATE(32),
        SIGNATURE_FILE_1(33),
        SIGNATURE_FILE_2(34),
        SIGNATURE_FILE_3(35),
        DYNAMIC_FILE(36),
        CSN(42),
        PHOTO_FILE(43);
        private final int value;

        CardTokenFileType(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }

    public enum TokenStaticDataIndex {
        CHIP_SERIAL_NO(0),
        TOKEN_NO(1),
        CARD_TYPE_ID(2),
        DATE_ISSUED(3);
        private final int value;

        TokenStaticDataIndex(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }

    public enum CardStaticDataIndex {
        CHIP_SERIAL_NUMBER(0),
        CARD_NUMBER(1),
        CARD_TYPE_ID(2),
        USER_CATEGORY_ID(3),
        NAME(4),
        SERVICE(5),
        PN(6),
        UNIQUE_ID(7), // used for civilian
        RANK(8),
        DESIGNATION(9),
        GROUP(10),
        DATE_OF_BIRTH(11),
        UNIT(12),
        ZONE_ACCESS(13),
        DATE_ISSUED(14),
        PLACE_ISSUED(15),
        BLOOD_GROUP(16),
        NATIONALITY(17),
        ISSUED_BY(18),
        FIRMS_NAME(19),
        GENDER(20),
        SPONSOR_PHONE_NUMBER(21),
        SPONSOR_NAME(22),
        SPONSOR_RANK(23),
        SPONSORS_UNIT(24),
        RELATION(25);
        private final int value;

        CardStaticDataIndex(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }

    //Suppress default constructor for non-instantiability
    private Asn1CardTokenUtil() {
        throw new AssertionError("The Asn1CardTokenUtil methods must be accessed statically.");
    }

    /**
     * Utility to decode ASN1 encoded static data. Caller must handle the exception
     *
     * @param bytes - byte array of ASN1 encoded data.
     * @param index - index of a component in ASN1Sequence
     * @return the string representation of data.
     * @throws GenericException - on null, io
     */
    public static byte[] extractFromAsn1EncodedStaticData(byte[] bytes, int index) {
        try {
            ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(bytes));
            ASN1Primitive asn1Primitive = asn1InputStream.readObject();
            if (asn1Primitive instanceof ASN1Sequence) {
                ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(asn1Primitive);
                ASN1Encodable asn1SequenceObject = asn1Sequence.getObjectAt(index);
                if (asn1SequenceObject instanceof ASN1OctetString) {
                    LOGGER.log(Level.INFO, "****ExtractFromAsn1EncodedStaticData: OctetString type parsed.");
                    return ((ASN1OctetString) asn1SequenceObject).getOctets(); // encoded in hex
                }
                return asn1SequenceObject.toString().getBytes();
            }
            if (asn1Primitive instanceof ASN1OctetString) {
                LOGGER.log(Level.INFO, "****ExtractFromAsn1EncodedStaticData: OctetString type parsed.");
                return ((ASN1OctetString) asn1Primitive).getOctets();
            }
            return asn1Primitive.toString().getBytes();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(ApplicationConstant.GENERIC_ERR_MSG);
        }
    }


    /**
     * Utility to encode and store DynamicFile. Caller must handle the exception
     *
     * @param handle      - handle of the token
     * @param dynamicFile - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStoreDynamicFile(int handle, DynamicFile dynamicFile) {
        if (dynamicFile == null) {
            throw new GenericException("Received invalid data from server.");
        }

        /*
         **************************************** ORDER MUST BE FOLLOWED ****************************************
         * Index    ASN1_Data_Type           Field_Name                         DynamicFile_Member_Field        *
         * ******************************************************************************************************
         *   0.      U.P.INTEGER           - User Category ID                     userCategoryId                *
         *   1.      U.P.PrintableString   - Name                                 labourName                    *
         *   2.      U.P.INTEGER           - Gender                               genderId                      *
         *   3.      U.P.IA5String         - Date of Birth                        dateOfBirth                   *
         *   4.      U.P.INTEGER           - Blood Group                          bloodGroupId                  *
         *   5.      U.P.INTEGER           - Nationality                          nationalityId                 *
         *   6.      U.P.IA5String         - Unit (Issuance unit)                 issuanceUnit                  *
         *   7.      U.P.IA5String         - Unique ID - To identify the person   labourId                      *
         *   8.      U.P.IA5String         - Contractor ID                        contractorId                  *
         ********************************************************************************************************
         */
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            // ORDER MUST BE FOLLOWED
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(dynamicFile.getUserCategoryId()).trim())));
            derSequenceGenerator.addObject(new DERPrintableString(nonNull(dynamicFile.getLabourName())));
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(dynamicFile.getLabourGenderId()).trim())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(dynamicFile.getLabourDateOfBirth())));
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(dynamicFile.getLabourBloodGroupId()).trim())));
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(dynamicFile.getLabourNationalityId()).trim())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(dynamicFile.getIssuanceUnit())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(dynamicFile.getLabourId())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(dynamicFile.getContractorId())));
            derSequenceGenerator.close(); // must close it
            bytes = byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreDynamicFileError: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding dynamic file.");
        }
        if (bytes.length > MAX_DYNAMIC_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreDynamicFileError: DynamicFile size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The dynamic file size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.DYNAMIC_FILE, bytes);
    }

    private static String nonNull(String string) {
        if (string == null) {
            LOGGER.log(Level.SEVERE, "Received null data from server.");
            throw new GenericException("Received invalid data from server.");
        }
        return string;
    }

    /**
     * Utility to encode and store DefaultValidityFile. Caller must handle the exception
     *
     * @param handle              - handle of the token
     * @param defaultValidityFile - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStoreDefaultValidityFile(int handle, DefaultValidityFile defaultValidityFile) {
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            // ORDER MUST BE FOLLOWED
            derSequenceGenerator.addObject(new DERIA5String(nonNull(defaultValidityFile.getValidFrom())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(defaultValidityFile.getValidTo())));
            derSequenceGenerator.close(); // must close it
            bytes = byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreDefaultValidityFileError: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding default validity file.");
        }
        if (bytes.length > MAX_DEFAULT_ACCESS_VALIDITY_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreDefaultValidityFileError: DefaultValidityFile size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The default validity file size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.DEFAULT_ACCESS_VALIDITY, bytes);
    }

    /**
     * Utility to encode and store FingerprintFile. Caller must handle the exception
     *
     * @param handle - handle of the token
     * @param fps    - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStoreFingerprintFile(int handle, List<LabourFp> fps) {
        byte[] bytes;
        try {
            ASN1EncodableVector set = new ASN1EncodableVector();
            for (LabourFp fp : fps) {
                ASN1EncodableVector asn1EncodableVector = new ASN1EncodableVector();
                asn1EncodableVector.add(new DERIA5String(nonNull(fp.getFpPos())));
                asn1EncodableVector.add(new DEROctetString(Base64.getDecoder().decode(nonNull(fp.getFpData()))));
                DERSequence sequence = new DERSequence(asn1EncodableVector);
                set.add(sequence);
            }
            bytes = new DERSet(set).getEncoded();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreFingerprintFileError: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding fingerprint file.");
        }
        if (bytes.length > MAX_FINGERPRINT_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreFingerprintFileError: FingerprintFile size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The fingerprint file size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.FINGERPRINT_FILE, bytes);
    }


    /**
     * Utility to encode and store photo file. Caller must handle the exception
     *
     * @param handle             - handle of the token
     * @param base64EncodedPhoto - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStorePhotoFile(int handle, String base64EncodedPhoto) {
        byte[] bytes;
        try {
            bytes = new DEROctetString(Base64.getDecoder().decode(nonNull(base64EncodedPhoto))).getEncoded();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStorePhotoFileError: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding photo file.");
        }

        if (bytes.length > MAX_PHOTO_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStorePhotoFileError: Photo size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The photo file size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.PHOTO_FILE, bytes);
    }

    /**
     * Utility to encode and store SignatureFile1. Caller must handle the exception
     *
     * @param handle    - handle of the token
     * @param signFile1 - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStoreSignFile1(int handle, String signFile1) {
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            derSequenceGenerator.addObject(new DERIA5String(nonNull(signFile1)));
            derSequenceGenerator.close(); // must close it
            bytes = byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreSignFile1Error: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding signature file1.");
        }
        if (bytes.length > MAX_SIGNATURE_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreSignFile1Error: SignFile1 size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The signature file1 size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.SIGNATURE_FILE_1, bytes);
    }

    /**
     * Utility to encode and store SignatureFile3. Caller must handle the exception
     *
     * @param handle    - handle of the token
     * @param signFile3 - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStoreSignFile3(int handle, String signFile3) {
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            derSequenceGenerator.addObject(new DERIA5String(nonNull(signFile3)));
            derSequenceGenerator.close(); // must close it
            bytes = byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreSignFile3Error: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding signature file3.");
        }
        if (bytes.length > MAX_SIGNATURE_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreSignFile3Error: SignFile3 size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The signature file3 size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.SIGNATURE_FILE_3, bytes);
    }

    /**
     * Utility to encode and store SpecialAccessFile. Caller must handle the exception
     *
     * @param handle            - handle of the token
     * @param specialAccessFile - object to be encoded
     * @throws GenericException - on exception
     */
    public static void encodeAndStoreSpecialAccessFile(int handle, SpecialAccessFile specialAccessFile) {
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            // ORDER MUST BE FOLLOWED
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(specialAccessFile.getUnitCode()).trim())));
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(specialAccessFile.getZoneId()).trim())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(specialAccessFile.getFromDate())));
            derSequenceGenerator.addObject(new DERIA5String(nonNull(specialAccessFile.getToDate())));
            derSequenceGenerator.addObject(new ASN1Integer(Integer.parseUnsignedInt(nonNull(specialAccessFile.getWorkingHourCode().trim()))));
            derSequenceGenerator.close(); // must close it
            bytes = byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreSpecialAccessFileError: " + ex.getMessage());
            throw new GenericException("Error occurred while encoding special access file.");
        }
        if (bytes.length > MAX_SPECIAL_ACCESS_PERMISSION_FILE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****EncodeAndStoreSpecialAccessFileError: SpecialAccessFile size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("The special access file size exceeded the allowed limit.");
        }
        storeBufferedData(handle, CardTokenFileType.SPECIAL_ACCESS_PERMISSION_FILE, bytes);
    }


    /**
     * Disconnects the initialized card
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static void deInitialize() {
        CRApiResDto deInitializeResDto = LocalNavalWebServiceApi.getDeInitialize();
        jniErrorCode = deInitializeResDto.getRetVal();
        // -1409286131 -> prerequisites failed error
        if (jniErrorCode != 0 && jniErrorCode != -1409286131) {
            LOGGER.log(Level.SEVERE, () -> "****DeInitializeErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }

    /**
     * prerequisite of all other APIs
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static void initialize() {
        CRApiResDto crInitializeResDto = LocalNavalWebServiceApi.getInitialize();
        jniErrorCode = crInitializeResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****InitializeErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }

    /**
     * Gets the handle id, csn
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static CRWaitForConnectResDto waitForConnect(String readerName) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRWaitForConnectReqDto(readerName));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRWaitForConnectResDto crWaitForConnectResDto = LocalNavalWebServiceApi.postWaitForConnect(reqData);
        jniErrorCode = crWaitForConnectResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****WaitForConnectErrorCode: " + jniErrorCode);
            if (jniErrorCode == -1090519029) { // custom message when press 'login' without reader connected
                LOGGER.log(Level.SEVERE, () -> "****WaitForConnectErrorCode: No card reader detected or unsupported reader name.");
                throw new NoReaderException("No card reader detected or unsupported reader name.");
            }
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
        return crWaitForConnectResDto;
    }

    /**
     * select card/token application
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static void selectApp(int cardType, int handle) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRSelectAppReqDto(cardType, handle));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRApiResDto crSelectAppResDto = LocalNavalWebServiceApi.postSelectApp(reqData);
        jniErrorCode = crSelectAppResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****SelectAppErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }

    /**
     * returns Object[], must be type cast by caller
     * array[0] -> int (ASN1 Tag),
     * array[1] --> byte[] (ASN1 encoded bytes)
     * reads all data and saves it in buffer
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static byte[] readBufferedData(int handle, CardTokenFileType cardTokenFileType) {
        //  4000
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // read 1024 bytes for the first time as we don't know the exact size to be read.
            byte[] base64DecodedBytes = readData(handle, cardTokenFileType.getValue(), 0, MAX_BUFFER_SIZE);
            // check first byte (TAG)
            // the first byte specifies ASN1 data type.
            byte asn1TypeByte = base64DecodedBytes[0];
            if (asn1TypeByte <= 0) {
                LOGGER.log(Level.SEVERE, () -> "****Error: Read unknown ASN1 type. Tag value: " + asn1TypeByte);
                throw new GenericException("Read unknown ASN1 type.");
            }
            // check second byte (LENGTH)
            // the second byte specifies data length or how many bytes are used
            // to encode LENGTH based on MSB.
            byte asn1LengthByte = base64DecodedBytes[1];
            if (asn1LengthByte == 0) {
                LOGGER.log(Level.SEVERE, () -> "****Error: No data is available on the card/token.");
                throw new GenericException("No data is available on the card/token.");
            }
            int totalByteCount;
            // check if only one byte is used to encode actual data length
            // if second byte's MSB is 0, then it a positive number,
            // and only a single byte is used to encode data length
            if (asn1LengthByte > 0) {
                // now data length is less than 128 bytes.
                // 01111111 --> 127
                totalByteCount = asn1LengthByte + 2; //first byte + second byte
                if (base64DecodedBytes.length < totalByteCount) {
                    LOGGER.log(Level.SEVERE, () -> "****Error: Read corrupted data.");
                    throw new GenericException("Read corrupted data from the card/token.");
                }
                return Arrays.copyOfRange(base64DecodedBytes, 0, totalByteCount);
            }

            /*
                 now LENGTH(second byte) has 1xxxxxxx form.
                 the MSB is 1, so, rest 7 bits are used to find the number of bytes used to encode LENGTH
                 Example:
                     byte[] =      [00010110, 10000001, 11111111, 01000001, 01000001, 01000001 .... 01000001]
                            Index ->   0,         1,        2,       3,         4,        5,    ....   256

                          TAG(IA5String=22) byte                               = 00010110 (first byte)
                          LENGTH byte                                          = 10000001 (second byte)
                          LENGTH byte MSB                                      = 1
                          LENGTH byte 7 LS bits(LENGTH Encoding Byte Number)   = 0000001
                          LENGTH Encoding Byte Number in decimal               = 1
                          LENGTH Encoding Byte array                           = [11111111]
                          Decimal value of LENGTH Encoding Byte array          = 255 (binary string converted to dec)
                          VALUE Array                                          = [01000001, 01000001 ....]255 times
                          Binary String of character A                         = 01000001

                LENGTH Encoding Byte Number in decimal:
                          10000001 (LENGTH)
                        & 01111111 (0x7f) masking MSB
                        ------------------------
                          00000001 -> 1 (decimal)

                Decimal value of LENGTH Encoding Byte array:
                            1. array = [11111111] -> 255
                            // if array.size > 1, then Big Endian is used for converting binary string to decimal.
                            2. e.g. array = [000000001, 11111111] -> 511 [0000000111111111] binary string concatenated
            */
            // get number of bytes for encoding length
            int byteCountForEncodingLength = asn1LengthByte & 0x7F; // removes the MSB
            int metaDataByteCount = 2 + byteCountForEncodingLength; // number of bytes used by ASN1 information.
            // now LENGTH encoding byte starts from 3rd byte (index 2) + number of bytes used to encode LENGTH
            byte[] bytesForEncodingLength = Arrays.copyOfRange(base64DecodedBytes, 2, metaDataByteCount);
            int dataByteCount = calculateDataLength(bytesForEncodingLength); // number of bytes for actual data
            totalByteCount = metaDataByteCount + dataByteCount;
            if (totalByteCount <= base64DecodedBytes.length) {
                return Arrays.copyOfRange(base64DecodedBytes, 0, totalByteCount);
            }
            /*
                 more bytes need to be read.
                 dataByteCount > (base64DecodedBytes.length - metaDataByteCount)
                     e.g. 1200 > (1024 - 4)
                 remaining required bytes = 180 ( 1200 - ( 1024 - 4 ))
             */

            int moreByteToReadSize = dataByteCount - (base64DecodedBytes.length - metaDataByteCount);
            byteArrayOutputStream.write(base64DecodedBytes);
            int offset = base64DecodedBytes.length;
            while (moreByteToReadSize > 0) {
                if (moreByteToReadSize < MAX_BUFFER_SIZE) {
                    base64DecodedBytes = readData(handle, cardTokenFileType.getValue(), offset, moreByteToReadSize);
                    if (moreByteToReadSize != base64DecodedBytes.length) {
                        LOGGER.log(Level.SEVERE, () -> "****Error: Failed to read required bytes.");
                        throw new GenericException("Failed to read required bytes from card/token.");
                    }
                    byteArrayOutputStream.write(base64DecodedBytes);
                    break;
                }
                base64DecodedBytes = readData(handle, cardTokenFileType.getValue(), offset, MAX_BUFFER_SIZE);
                byteArrayOutputStream.write(base64DecodedBytes);
                moreByteToReadSize -= base64DecodedBytes.length;
                offset += base64DecodedBytes.length;
            }
            byte[] allReadBytes = byteArrayOutputStream.toByteArray();
            if (allReadBytes.length != totalByteCount) {
                LOGGER.log(Level.SEVERE, () -> "****Error: Required byte length and read byte length are not matched.");
                throw new GenericException("The required byte length and read byte length are not matched.");
            }
            return allReadBytes;
        } catch (Exception ex) {
            throw new GenericException(ex.getMessage());
        }
    }

    public static int calculateDataLength(byte[] byteArray) {
        StringBuilder binaryString = new StringBuilder(byteArray.length * 8);
        for (byte b : byteArray) {
            for (int i = 7; i >= 0; i--) {
                binaryString.append((b >> i) & 1);
            }
        }
        return Integer.parseInt(binaryString.toString(), 2);
    }


    /**
     * decodes base64 encoded data and give actual data.
     * read max 1024 bytes
     * Caller must handle the exception.
     *
     * @return actual data in byte[]
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    private static byte[] readData(int handle, int whichData, int offset, int requestLength) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRReadDataReqDto(handle, whichData, offset, requestLength));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRReadDataResDto crReadDataResDto = LocalNavalWebServiceApi.postReadData(reqData);
        jniErrorCode = crReadDataResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****ReadDataErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
        return Base64.getDecoder().decode(crReadDataResDto.getResponse());
    }

    /**
     * stores buffered data
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static void storeBufferedData(int handle, CardTokenFileType cardTokenFileType, byte[] bytes) {
        int offset = 0;
        // for writing multiple times
        int times = bytes.length / MAX_BUFFER_SIZE;
        int extraBytes = bytes.length % MAX_BUFFER_SIZE;
        if (times < 1) {
            storeData(handle, cardTokenFileType.getValue(), offset, bytes);
            return;
        }
        for (int i = 0; i < times; i++) {
            byte[] temp = Arrays.copyOfRange(bytes, offset, offset + MAX_BUFFER_SIZE);
            storeData(handle, cardTokenFileType.getValue(), offset, temp);
            offset += MAX_BUFFER_SIZE;
        }
        if (extraBytes > 0) {
            byte[] temp = Arrays.copyOfRange(bytes, offset, offset + extraBytes);
            storeData(handle, cardTokenFileType.getValue(), offset, temp);
        }
    }


    /**
     * private API.
     * Should not be used by client.
     * store max 1024-byte
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    private static void storeData(int handle, int whichData, int offset, byte[] bytes) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRStoreDataReqDto(handle, whichData, offset, Base64.getEncoder().encodeToString(bytes), bytes.length));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRApiResDto crApiResDto = LocalNavalWebServiceApi.postStoreData(reqData);
        jniErrorCode = crApiResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****StoreDataErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }

    /**
     * verifies Certificate chain
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */

    public static void verifyCertificate(int handle, int whichTrust, int whichCertificate, byte[] bytes) {
        if (bytes.length > MAX_SYSTEM_CERTIFICATE_SIZE) {
            LOGGER.log(Level.SEVERE, () -> "****VerifyCertificateError: Certificate size exceeded the allowed limit. Length: " + bytes.length);
            throw new GenericException("Certificate size exceeded the allowed limit.");
        }
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRVerifyCertificateReqDto(handle, whichTrust, whichCertificate, Base64.getEncoder().encodeToString(bytes), bytes.length));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRApiResDto crApiResDto = LocalNavalWebServiceApi.postVerifyCertificate(reqData);
        jniErrorCode = crApiResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****VerifyCertificateErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }

    /**
     * authenticate two cards
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static void pkiAuth(int handle1, int handle2) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRPkiAuthReqDto(handle1, handle2));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRApiResDto crApiResDto = LocalNavalWebServiceApi.postPkiAuth(reqData);
        jniErrorCode = crApiResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****PkiAuthErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }

    /**
     * disconnect the incoming card
     * Caller must handle the exception.
     *
     * @throws ConnectionTimeoutException - on timeout or response status code not 200
     * @throws GenericException           - on Exception
     */
    public static void waitForRemoval(int handle) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRWaitForRemovalReqDto(handle));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRApiResDto crApiResDto = LocalNavalWebServiceApi.postWaitForRemoval(reqData);
        jniErrorCode = crApiResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "****WaitForRemovalErrorCode: " + jniErrorCode);
            throw new GenericException(LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
        }
    }
}
