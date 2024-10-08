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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.GENERIC_ERR_MSG;
import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;
import static com.cdac.enrollmentstation.util.Asn1CardTokenUtil.*;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */
public class TokenIssuanceController extends AbstractBaseController {
    private static final Logger LOGGER = ApplicationLog.getLogger(TokenIssuanceController.class);
    @FXML
    private BorderPane rootBorderPane;

    private ContractorCardInfo contractorCardInfo;


    @FXML
    private Button continueBtn;


    @FXML
    private Button backBtn;

    @FXML
    private Label messageLabel;

    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
    }

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
            updateUi("Fetching contracts from the server. Please wait.");
        } catch (NoReaderOrCardException | GenericException ex) {
            if ("Selected File deactivated.".equalsIgnoreCase(ex.getMessage())) {
                updateUi("The card is deactivated. Please activate it and try again.");
            } else {
                updateUi(ex.getMessage());
            }
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
                LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
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
        // restart EnrollmentStationServices if failed on the first WaitForConnect call.
        while (true) {
            counter--;
            LOGGER.log(Level.INFO, () -> "***Card: Calling deInitialize API.");
            Asn1CardTokenUtil.deInitialize();
            LOGGER.log(Level.INFO, () -> "***Card: Calling initialize API.");
            Asn1CardTokenUtil.initialize();
            try {
                LOGGER.log(Level.INFO, () -> "***Card: Sleeping for " + SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC + " milliseconds before waitForConnect API call.");
                Thread.sleep(SLEEP_TIME_BEFORE_WAIT_FOR_CONNECT_CALL_IN_MIL_SEC);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "****BeforeWaitSleep: Interrupted while sleeping.");
                Thread.currentThread().interrupt();
            }

            try {
                LOGGER.log(Level.INFO, () -> "***Card: Calling waitForConnect API.");
                crWaitForConnectResDto = Asn1CardTokenUtil.waitForConnect(MANTRA_CARD_READER_NAME);
                break;
            } catch (GenericException ex) { // don't handle NoReaderOrCardException
                // only restart API service when communication error happens
                if (counter == 0) {
                    LOGGER.log(Level.INFO, () -> "***Card: Communication error occurred. Restarting EnrollmentStationServices.");
                    if (restartApiService()) {
                        try {
                            LOGGER.log(Level.INFO, () -> "***Card: Sleeping for 2 seconds after restarting EnrollmentStationServices.");
                            Thread.sleep(2000); // needed to sleep after restarting
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue; // starts from DeInitialize again.
                    } // else exit code is not zero
                    else {
                        LOGGER.log(Level.INFO, () -> "***Card: Unable to restart EnrollmentStationServices.");
                    }
                }
                LOGGER.log(Level.INFO, () -> "***Card: Communication error occurred. Even after restarting EnrollmentStationServices.");
                throw new GenericException(ex.getMessage());
            }
        }
        int returnedErrorCode = crWaitForConnectResDto.getRetVal();
        LOGGER.log(Level.INFO, () -> "***Card: waitForConnectErrorCode: " + returnedErrorCode);

        if (crWaitForConnectResDto.getRetVal() != 0) {
            throw new GenericException("Kindly reconnect the reader and place card correctly.");
        }
        byte[] decodedHexCsn = Base64.getDecoder().decode(crWaitForConnectResDto.getCsn());
        if (decodedHexCsn.length != crWaitForConnectResDto.getCsnLength()) {
            LOGGER.log(Level.INFO, () -> "****Card: Decoded bytes size not matched with response length.");
            throw new GenericException("Decoded bytes size not matched with response length.");
        }
        contractorCardInfo.setCardChipSerialNo(Strings.fromByteArray(Hex.encode(decodedHexCsn)).toUpperCase()); // hexadecimal bytes to hex string.
        contractorCardInfo.setCardHandle(crWaitForConnectResDto.getHandle());

        LOGGER.log(Level.INFO, () -> "***Card: Calling selectApp API ");
        Asn1CardTokenUtil.selectApp(CARD_TYPE_NUMBER, crWaitForConnectResDto.getHandle());
        LOGGER.log(Level.INFO, () -> "***Card: Calling readData API ");
        return readBufferedData(crWaitForConnectResDto.getHandle(), CardTokenFileType.STATIC);
    }


    private boolean restartApiService() {
        try {
            Process pr = Runtime.getRuntime().exec(Asn1CardTokenUtil.CARD_API_SERVICE_RESTART_COMMAND);
            int exitCode = pr.waitFor();
            LOGGER.log(Level.INFO, () -> "****EnrollmentStationServices restart exit code: " + exitCode);
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
