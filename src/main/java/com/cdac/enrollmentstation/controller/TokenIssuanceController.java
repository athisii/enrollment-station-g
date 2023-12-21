package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.dto.CRWaitForConnectResDto;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.exception.NoReaderOrCardException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ContractorCardInfo;
import com.cdac.enrollmentstation.model.TokenDetailsHolder;
import com.cdac.enrollmentstation.util.Asn1CardTokenUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;
import static com.cdac.enrollmentstation.util.Asn1CardTokenUtil.*;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */
public class TokenIssuanceController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(TokenIssuanceController.class);

    private ContractorCardInfo contractorCardInfo;


    @FXML
    private Button continueBtn;


    @FXML
    private Button backBtn;

    @FXML
    private Label messageLabel;


    @FXML
    private void backBtnAction() throws IOException {
        App.setRoot("main_screen");
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    private void readCardDetails() {
        contractorCardInfo = new ContractorCardInfo();
        byte[] asn1EncodedData;
        try {
            asn1EncodedData = startProcedureCall();
        } catch (NoReaderOrCardException | GenericException ex) {
            updateUi(ex.getMessage());
            enableControls(backBtn, continueBtn);
            return;
        } catch (ConnectionTimeoutException ex) {
            updateUi("Something went wrong. Kindly check Card API service.");
            enableControls(backBtn, continueBtn);
            return;
        }
        String contractorName;
        String contractorId;
        try {
            contractorName = new String(Asn1CardTokenUtil.extractFromAsn1EncodedStaticData(asn1EncodedData, CardStaticDataIndex.NAME.getValue()), StandardCharsets.UTF_8);
            contractorId = new String(Asn1CardTokenUtil.extractFromAsn1EncodedStaticData(asn1EncodedData, CardStaticDataIndex.UNIQUE_ID.getValue()), StandardCharsets.UTF_8);
        } catch (GenericException ex) {
            updateUi("Kindly place a valid card and try again.");
            enableControls(backBtn, continueBtn);
            return;
        }
        if (contractorName.isEmpty() || contractorId.isEmpty()) {
            updateUi("No contractor details available in the card.");
            enableControls(backBtn, continueBtn);
            return;
        }
        contractorCardInfo.setContractorName(contractorName);
        contractorCardInfo.setContractorId(contractorId);

        TokenDetailsHolder.getDetailsHolder().setContractorCardInfo(contractorCardInfo);
        Platform.runLater(() -> {
            try {
                App.setRoot("contract");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                messageLabel.setText("Something went wrong. Contact the system admin.");
                enableControls(backBtn, continueBtn);
            }
        });
    }

    @FXML
    private void continueBtnAction() {
        messageLabel.setText("Please wait...");
        disableControls(backBtn, continueBtn);
        App.getThreadPool().execute(this::readCardDetails);
    }

    private void disableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    private void enableControls(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }


    private byte[] startProcedureCall() {
        // required to follow the procedure calls
        // deInitialize -> initialize ->[waitForConnect -> selectApp] -> readData
        CRWaitForConnectResDto crWaitForConnectResDto;
        int counter = 1;
        // restart Naval_WebServices if failed on the first WaitForConnect call.
        while (true) {
            counter--;
            Asn1CardTokenUtil.deInitialize();
            Asn1CardTokenUtil.initialize();
            try {
                Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
                Thread.currentThread().interrupt();
            }

            try {
                crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_READER_NAME);
                byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
                if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
                    LOGGER.log(Level.INFO, () -> "****Decoded bytes size not matched with response length.");
                    throw new GenericException("Decoded bytes size not matched with response length.");
                }
                contractorCardInfo.setCardChipSerialNo(Strings.fromByteArray(Hex.encode(decodedHexCsn)).toUpperCase()); // hexadecimal bytes to hex string.
                contractorCardInfo.setCardHandle(crWaitForConnectResDto.getHandle());
                break;
            } catch (GenericException ex) {
                if (counter == 0) {
                    LOGGER.log(Level.INFO, () -> "****Communication error occurred. Restarting Naval_WebServices.");
                    if (restartApiService()) {
                        try {
                            Thread.sleep(2000); // needed to sleep after restarting
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue; // starts from DeInitialize again.
                    } // else exit code is not zero
                }
                LOGGER.log(Level.INFO, () -> "****Communication error occurred. Unable to restart Naval_WebServices.");
                throw new GenericException("Something went wrong. Please try again.");
            }
        }
        if (crWaitForConnectResDto.getRetVal() != 0) {
            throw new GenericException("Kindly reconnect the reader and place card correctly.");
        }
        Asn1CardTokenUtil.selectApp(CARD_TYPE_NUMBER, crWaitForConnectResDto.getHandle());
        return readBufferedData(crWaitForConnectResDto.getHandle(), CardTokenFileType.STATIC);
    }


    private boolean restartApiService() {
        try {
            Process pr = Runtime.getRuntime().exec(Asn1CardTokenUtil.CARD_API_SERVICE_RESTART_COMMAND);
            int exitCode = pr.waitFor();
            LOGGER.log(Level.INFO, () -> "****Naval_WebServices restart exit code: " + exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex::getMessage);
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new GenericException(GENERIC_ERR_MSG);
        }

    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        enableControls(backBtn);
        updateUi("Received an invalid data from the server.");
    }
}
