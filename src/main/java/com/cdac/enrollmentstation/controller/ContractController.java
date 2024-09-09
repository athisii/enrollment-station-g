package com.cdac.enrollmentstation.controller;


import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.dto.Contract;
import com.cdac.enrollmentstation.dto.ContractResDto;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ContractorCardInfo;
import com.cdac.enrollmentstation.model.TokenDetailsHolder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cdac.enrollmentstation.constant.ApplicationConstant.SCENE_ROOT_ERR_MSG;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class ContractController extends AbstractBaseController {
    private static final int NUMBER_OF_ROWS_PER_PAGE = 8;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private Button homeBtn;
    @FXML
    private TableView<Contract> tableView;

    @FXML
    private Label messageLabel;

    @FXML
    private Label contractorIdLabel;

    @FXML
    private Label contractorNameLabel;


    List<Contract> contracts;

    @FXML
    private Pagination pagination;

    @FXML
    TextField searchBox;

    private static final Logger LOGGER = ApplicationLog.getLogger(ContractController.class);

    @FXML
    private void showHome() throws IOException {
        App.setRoot("main_screen");
    }

    private void fetchDetails() {
        contractorIdLabel.setText("CONTRACTOR ID: " + TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getContractorId());
        contractorNameLabel.setText("CONTRACTOR NAME: " + TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getContractorName());

        LOGGER.log(Level.INFO, () -> "***Fetching contract list from the server.");
        ContractResDto contractResDto;
        try {
            contractResDto = MafisServerApi.fetchContractList(TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getContractorId(), TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getCardChipSerialNo());
        } catch (GenericException ex) {
            messageLabel.setText(ex.getMessage());
            return;
        } catch (ConnectionTimeoutException ex) {
            messageLabel.setText("Connection timeout. Please try again.");
            return;
        }

        LOGGER.log(Level.INFO, () -> "***ServerResponseErrorCode: " + contractResDto.getErrorCode());
        if (contractResDto.getErrorCode() != 0) {
            LOGGER.log(Level.INFO, () -> "***ServerErrorDesc: " + contractResDto.getDesc());
            messageLabel.setText(contractResDto.getDesc());
            return;
        }

        if (contractResDto.getContracts() == null || contractResDto.getContracts().isEmpty()) {
            messageLabel.setText("No contract available.");
            return;
        }
        contracts = new ArrayList<>(contractResDto.getContracts());
        int extraPage;
        if (contracts.size() % NUMBER_OF_ROWS_PER_PAGE == 0) {
            extraPage = 0;
        } else {
            extraPage = 1;
        }
        int pageCount = contracts.size() / NUMBER_OF_ROWS_PER_PAGE + extraPage;

        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex > pageCount) {
                return null;
            }
            return createPage(pageIndex);
        });
        tableView.setRowFactory(tv -> {
            TableRow<Contract> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                LOGGER.log(Level.INFO, () -> "****Click count: " + event.getClickCount());
                if (!row.isEmpty() && event.getClickCount() >= 2) {
                    Contract element = row.getItem();
                    setContractIdInContractDetailHolder(element.getContractId());
                    try {
                        updateUi("Fetching labors from the server. Please wait.");
                        App.setRoot("labour");
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, SCENE_ROOT_ERR_MSG, ex);
                        messageLabel.setText(ex.getMessage());
                    }
                }
            });
            return row;

        });
        ObservableList<Contract> observablelist = FXCollections.observableArrayList(contracts);
        tableView.setItems(observablelist);
        tableView.refresh();
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> filterList(contracts, newValue));
    }


    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * NUMBER_OF_ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + NUMBER_OF_ROWS_PER_PAGE, contracts.size());
        tableView.setItems(FXCollections.observableArrayList(contracts.subList(fromIndex, toIndex)));
        tableView.refresh();
        return tableView;
    }


    public void initialize() {
        // disable 'enter key' on keyboard
        rootBorderPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
            }
        });
        fetchDetails();
    }

    private void filterList(List<Contract> contracts, String searchText) {
        List<Contract> contractList;
        if (searchText == null || searchText.isBlank()) {
            contractList = contracts;
        } else {
            List<Contract> filteredList = new ArrayList<>();
            for (Contract contract : contracts) {
                if (contract.getContractId().toLowerCase().contains(searchText.toLowerCase()) || (contract.getContractValidFrom().toLowerCase().contains(searchText.toLowerCase())) || (contract.getContractValidUpto().toLowerCase().contains(searchText.toLowerCase()))) {
                    filteredList.add(contract);
                }
            }
            contractList = filteredList;
        }
        int extraPage;
        if (contractList.size() % NUMBER_OF_ROWS_PER_PAGE == 0) {
            extraPage = 0;
        } else {
            extraPage = 1;
        }
        int pageCount = contractList.size() / NUMBER_OF_ROWS_PER_PAGE + extraPage;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        ObservableList<Contract> observablelist = FXCollections.observableArrayList(contractList);
        Platform.runLater(() -> {
            tableView.setItems(observablelist);
            tableView.refresh();
        });
    }

    public void setContractIdInContractDetailHolder(String contractId) {
        ContractorCardInfo contractorCardInfo = TokenDetailsHolder.getDetailsHolder().getContractorCardInfo();
        contractorCardInfo.setContractId(contractId);
        TokenDetailsHolder.getDetailsHolder().setContractorCardInfo(contractorCardInfo);
    }

    @Override
    public void onUncaughtException() {
        LOGGER.log(Level.INFO, "***Unhandled exception occurred.");
        homeBtn.setDisable(false);
        updateUi("Received an invalid data from the server.");
    }

    private void updateUi(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }
}