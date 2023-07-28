package com.cdac.enrollmentstation.controller;


import com.cdac.enrollmentstation.App;
import com.cdac.enrollmentstation.api.MafisServerApi;
import com.cdac.enrollmentstation.dto.ContractResDto;
import com.cdac.enrollmentstation.exception.ConnectionTimeoutException;
import com.cdac.enrollmentstation.exception.GenericException;
import com.cdac.enrollmentstation.logging.ApplicationLog;
import com.cdac.enrollmentstation.model.ContractDetail;
import com.cdac.enrollmentstation.model.ContractorCardInfo;
import com.cdac.enrollmentstation.model.TokenDetailsHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContractController {
    private static final int NUMBER_OF_ROWS_PER_PAGE = 8;
    @FXML
    private TableView<ContractDetail> tableView;

    @FXML
    private Label messageLabel;

    @FXML
    private Label contractorIdLabel;

    @FXML
    private Label contractorNameLabel;

    @FXML
    private TableColumn<ContractDetail, String> contractIdTableColumn;

    @FXML
    private TableColumn<ContractDetail, String> contractValidFromTableColumn;

    @FXML
    private TableColumn<ContractDetail, String> contractValidUptoTableColumn;


    List<ContractDetail> contractDetails;

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
        contractorIdLabel.setText(TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getContractorId());
        contractorNameLabel.setText(TokenDetailsHolder.getDetailsHolder().getContractorCardInfo().getContractorName());

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

        if (!"0".equals(contractResDto.getErrorCode())) {
            messageLabel.setText(contractResDto.getDesc());
            return;
        }

        if (contractResDto.getContractDetails().isEmpty()) {
            messageLabel.setText("No contract available.");
            return;
        }
        contractDetails = new ArrayList<>(contractResDto.getContractDetails());

        //for property bindings
        contractIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("contractId"));
        contractValidFromTableColumn.setCellValueFactory(new PropertyValueFactory<>("contractValidFrom"));
        contractValidUptoTableColumn.setCellValueFactory(new PropertyValueFactory<>("contractValidUpto"));

        int extraPage;
        if (contractDetails.size() % NUMBER_OF_ROWS_PER_PAGE == 0) {
            extraPage = 0;
        } else {
            extraPage = 1;
        }
        int pageCount = contractDetails.size() / NUMBER_OF_ROWS_PER_PAGE + extraPage;

        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex > pageCount) {
                return null;
            }
            return createPage(pageIndex);
        });
        tableView.setRowFactory(tv -> {
            TableRow<ContractDetail> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    ContractDetail element = row.getItem();
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
        tableView.setFixedCellSize(35.0);
        ObservableList<ContractDetail> observablelist = FXCollections.observableArrayList(contractDetails);
        tableView.setItems(observablelist);
        tableView.refresh();
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> tableView.setItems(filterList(contractDetails, newValue)));
    }


    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * NUMBER_OF_ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + NUMBER_OF_ROWS_PER_PAGE, contractDetails.size());
        tableView.setItems(FXCollections.observableArrayList(contractDetails.subList(fromIndex, toIndex)));
        return tableView;
    }


    public void initialize() {
        fetchDetails();
    }

    private ObservableList<ContractDetail> filterList(List<ContractDetail> list, String searchText) {
        List<ContractDetail> filteredList = new ArrayList<>();
        for (ContractDetail contract : list) {
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
}