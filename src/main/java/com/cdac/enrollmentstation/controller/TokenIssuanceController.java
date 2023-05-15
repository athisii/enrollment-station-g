package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.LocalCardReaderApi;
import com.cdac.enrollmentstation.dto.*;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ContractorInfo;
import com.cdac.enrollmentstation.model.DetailsHolder;
import com.cdac.enrollmentstation.security.Asn1EncodedHexUtil;
import com.cdac.enrollmentstation.util.LocalCardReaderErrMsgUtil;
import com.cdac.enrollmentstation.util.Singleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;


/**
 * FXML Controller class
 *
 * @author root
 */
public class TokenIssuanceController {
    private static final Logger LOGGER = ApplicationLog.getLogger(TokenIssuanceController.class);
    private static final String MANTRA_CARD_READER_NAME = "Mantra Reader (1.00) 00 00";
    private static final byte CARD_TYPE = 4; // Naval ID/Contractor Card value is 4
    private static final byte STATIC_TYPE = 21; // Static file -> 21
    private static final byte FINGERPRINT_TYPE = 25; // Fingerprint file -> 25
    private static final int CARD_READER_MAX_BUFFER_SIZE = 1024; // Max bytes card can handle

    private int jniErrorCode;
    private static final String ERROR_MESSAGE = "Place a valid card type and try again.";

    private EnumMap<DataType, byte[]> asn1EncodedHexByteArrayMap; // GLOBAL data store.

    private enum DataType {
        STATIC(STATIC_TYPE),
        FINGERPRINT(FINGERPRINT_TYPE);
        private final byte value;

        DataType(byte val) {
            value = val;
        }

        private byte getValue() {
            return value;
        }
    }

    private ContractorInfo contractorInfo;

    public
    @FXML Button showContractDetails;

    public
    @FXML Button backBtn;

    public
    @FXML Label messageLabel;


    @FXML
    private void backBtnAction() throws IOException {
        App.setRoot("main_screen");
    }

    @FXML
    private void readCardDetails() {
        contractorInfo = new ContractorInfo();
        try {
            asn1EncodedHexByteArrayMap = startProcedureCall();
        } catch (GenericException ex) {
            messageLabel.setText(ex.getMessage());
            return;
        }
        if (asn1EncodedHexByteArrayMap == null) {
            LOGGER.log(Level.SEVERE, "Connection timeout. Card API service is not available.");
            messageLabel.setText("Something went wrong. Contact the system admin.");
            return;
        }
        byte[] asn1EncodedHexByteArray = asn1EncodedHexByteArrayMap.get(DataType.STATIC);
        if (asn1EncodedHexByteArray == null) {
            messageLabel.setText("No contractor details available in the card.");
            return;
        }
        String contractorName;
        String contractorId;
        try {
            contractorName = Asn1EncodedHexUtil.extractFromStaticAns1EncodedHex(asn1EncodedHexByteArray, Asn1EncodedHexUtil.CardDataIndex.NAME);
            contractorId = Asn1EncodedHexUtil.extractFromStaticAns1EncodedHex(asn1EncodedHexByteArray, Asn1EncodedHexUtil.CardDataIndex.UNIQUE_ID);
        } catch (GenericException ex) {
            messageLabel.setText("Both contractor name and id are required.");
            return;
        }
        if (contractorName.isEmpty() || contractorId.isEmpty()) {
            messageLabel.setText("No contractor details available in the card.");
            return;
        }
        contractorInfo.setContractorName(contractorName);
        contractorInfo.setContractorId(contractorId);

        DetailsHolder.getDetailsHolder().setContractorInfo(contractorInfo);
        try {
            App.setRoot("contract");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            messageLabel.setText("Something went wrong. Contact the system admin.");
        }

    }


    private EnumMap<DataType, byte[]> startProcedureCall() {
        // required to follow the procedure calls
        // deInitialize -> initialize ->[waitForConnect -> selectApp] -> readData
        CRDeInitializeResDto crDeInitializeResDto = LocalCardReaderApi.getDeInitialize();
        // api not configured properly timeout
        if (crDeInitializeResDto == null) {
            return null;
        }
        jniErrorCode = crDeInitializeResDto.getRetVal();
        // -1409286131 -> prerequisites failed error
        if (jniErrorCode != 0 && jniErrorCode != -1409286131) {
            LOGGER.log(Level.SEVERE, () -> "DeInitializeError: " + LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRInitializeResDto crInitializeResDto = LocalCardReaderApi.getInitialize();
        // api not configured properly
        if (crInitializeResDto == null) {
            return null;
        }
        jniErrorCode = crInitializeResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "InitializeError: " + LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
            throw new GenericException(GENERIC_ERR_MSG);
        }
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRWaitForConnectReqDto(MANTRA_CARD_READER_NAME));
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }

        CRWaitForConnectResDto crWaitForConnectResDto = LocalCardReaderApi.postWaitForConnect(reqData);
        // api not configured properly
        if (crWaitForConnectResDto == null) {
            return null;
        }
        jniErrorCode = crWaitForConnectResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "WaitForConnectError: " + LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
            throw new GenericException(ERROR_MESSAGE);
        }

        // get csn
        byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        String csn = DatatypeConverter.printHexBinary(decodedHexCsn);
        if (crWaitForConnectResDto.getCsnLength() != decodedHexCsn.length || csn == null || csn.isEmpty()) {
            LOGGER.log(Level.SEVERE, "WaitForConnectError: Decoded csn length not same with returned length or null or empty csn.");
            throw new GenericException(GENERIC_ERR_MSG);
        }
        // required for next page
        contractorInfo.setSerialNo(csn);
        contractorInfo.setCardReaderHandle(crWaitForConnectResDto.getHandle());

        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRSelectAppReqDto(CARD_TYPE, crWaitForConnectResDto.getHandle()));
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        CRSelectAppResDto crSelectAppResDto = LocalCardReaderApi.postSelectApp(reqData);
        // api not configured properly
        if (crSelectAppResDto == null) {
            return null;
        }
        jniErrorCode = crSelectAppResDto.getRetVal();
        if (jniErrorCode != 0) {
            LOGGER.log(Level.SEVERE, () -> "SelectAppError: " + LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
            throw new GenericException(ERROR_MESSAGE);
        }
        EnumMap<DataType, byte[]> ans1EncodedHexByteArrayMap = new EnumMap<>(DataType.class);
        ans1EncodedHexByteArrayMap.put(DataType.STATIC, readDataFromCard(crWaitForConnectResDto.getHandle(), DataType.STATIC));
        return ans1EncodedHexByteArrayMap;
    }

    // throws GenericException
    // Caller must handle the exception
    private byte[] readDataFromCard(int handle, DataType dataType) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // for reading multiple times
            boolean repeat = true;
            int offset = 0;
            while (repeat) {
                CRReadDataResDto crReadDataResDto = readBufferedDataFromCard(handle, dataType, offset, CARD_READER_MAX_BUFFER_SIZE);
                // api not configured properly
                if (crReadDataResDto == null) {
                    return null;
                }
                jniErrorCode = crReadDataResDto.getRetVal();
                // if first request failed throw exception
                if (offset == 0 && jniErrorCode != 0) {
                    LOGGER.log(Level.SEVERE, () -> "ReadDataError: " + LocalCardReaderErrMsgUtil.getMessage(jniErrorCode));
                    throw new GenericException(ERROR_MESSAGE);
                }
                // consider 1st request responseLen  = 1024 bytes
                // therefore, we assumed more data is left to be read,
                // so we make a 2nd request, but all data are already read.
                // in that case we get non-zero return value.
                if (offset != 0 && jniErrorCode != 0) {
                    break;
                }

                byte[] base64DecodedBytes = Base64.getDecoder().decode(crReadDataResDto.getResponse());
                // responseLen(in bytes)
                if (base64DecodedBytes.length != crReadDataResDto.getResponseLen()) {
                    LOGGER.log(Level.SEVERE, "ReadDataError: Number of decoded bytes and response length not equal.");
                    throw new GenericException(GENERIC_ERR_MSG);
                }
                // end the read request
                if (crReadDataResDto.getResponseLen() < CARD_READER_MAX_BUFFER_SIZE) {
                    repeat = false;
                }
                offset += CARD_READER_MAX_BUFFER_SIZE;
                byteArrayOutputStream.write(base64DecodedBytes);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            // throws if exception occurs while writing to byteOutputStream
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new GenericException(GENERIC_ERR_MSG);
        }
    }

    // throws GenericException
    // Caller must handle the exception
    private CRReadDataResDto readBufferedDataFromCard(int handle, DataType whichData, int offset, int requestLength) {
        String reqData;
        try {
            reqData = Singleton.getObjectMapper().writeValueAsString(new CRReadDataReqDto(handle, whichData.getValue(), offset, requestLength));
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            throw new GenericException(GENERIC_ERR_MSG);
        }
        return LocalCardReaderApi.postReadData(reqData);
    }

}
