/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author root
 */
public class ListOfWorkerController implements Initializable {

    /**
     * Initializes the controller class.
     */

    @FXML
    TableView labourListTableView;

    @FXML
    TableColumn contractorId;

    @FXML
    TableColumn centerCode;

    @FXML
    TableColumn workerName;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO


        workerName.setCellValueFactory(new PropertyValueFactory<>("WorkerName"));

        centerCode.setCellValueFactory(new PropertyValueFactory<>("Centre Code"));

        contractorId.setCellValueFactory(new PropertyValueFactory<>("Contractor ID"));

//    labourListTableView.getColumns().add(column1);
//    labourListTableView.getColumns().add(column2);

        labourListTableView.getItems().add("John Doe");
        labourListTableView.getItems().add("Jane Deer");
        labourListTableView.getItems().add("Jane Tune");
    }

}
