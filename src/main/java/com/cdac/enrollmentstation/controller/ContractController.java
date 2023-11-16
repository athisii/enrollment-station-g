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
import javafx.scene.input.MouseButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author athisii, CDAC
 * Created on 26/12/22
 */

public class ContractController extends AbstractBaseController {
    private static final int NUMBER_OF_ROWS_PER_PAGE = 8;
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

        if (contractResDto.getErrorCode() != 0) {
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
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    Contract element = row.getItem();
                    setContractIdInContractDetailHolder(element.getContractId());
                    try {
                        App.setRoot("labour");
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage());
                        messageLabel.setText(ex.getMessage());
                    }
                }
            });
            return row;

        });
        ObservableList<Contract> observablelist = FXCollections.observableArrayList(contracts);
        tableView.setItems(observablelist);
        tableView.refresh();
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> tableView.setItems(filterList(contracts, newValue)));
    }


    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * NUMBER_OF_ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + NUMBER_OF_ROWS_PER_PAGE, contracts.size());
        tableView.setItems(FXCollections.observableArrayList(contracts.subList(fromIndex, toIndex)));
        return tableView;
    }


    public void initialize() {
        test();
        fetchDetails();
    }

    private void test() {
        ContractorCardInfo contractorCardInfo = new ContractorCardInfo();
        contractorCardInfo.setContractorName("Athisii Ekhe");
        contractorCardInfo.setContractorId("0000012");
        TokenDetailsHolder.getDetailsHolder().setContractorCardInfo(contractorCardInfo);
    }

    private ObservableList<Contract> filterList(List<Contract> list, String searchText) {
        List<Contract> filteredList = new ArrayList<>();
        for (Contract contract : list) {
            if (contract.getContractId().toLowerCase().contains(searchText.toLowerCase()) || (contract.getContractValidFrom().toLowerCase().contains(searchText.toLowerCase())) || (contract.getContractValidUpto().toLowerCase().contains(searchText.toLowerCase()))) {
                filteredList.add(contract);
            }
        }
        return FXCollections.observableList(filteredList);
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