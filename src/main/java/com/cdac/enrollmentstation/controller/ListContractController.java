/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.model.FileData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
public class ListContractController implements Initializable {
    @FXML
    TableView contractTableView;

    // @FXML TableColumn contractIdList;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        contractTableView = new TableView<FileData>();
        final ObservableList<FileData> data = FXCollections.observableArrayList(
                // new FileData("file1", "D:\\myFiles\\file1.txt"),
                // new FileData("file2", "D:\\myFiles\\file2.txt"),
                // new FileData("file3", "D:\\myFiles\\file3.txt"),
                // new FileData("file4", "D:\\myFiles\\file4.txt")
        );
        //Creating columns
        TableColumn fileNameCol = new TableColumn("File Name");
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));
        TableColumn pathCol = new TableColumn("Path");
        pathCol.setCellValueFactory(new PropertyValueFactory("path"));
        //Adding data to the table
        ObservableList<String> list = FXCollections.observableArrayList();
        contractTableView.setItems(data);
//    TableColumn<String, String> column1 = new TableColumn<>("Contract ID Date");
//    
//
//
//    
//
//        System.out.println("contract get columns :"+ contractTableView.getColumns() + " : "+ contractTableView.getItems());
//    contractTableView.getColumns().add(column1);
//    
//    contractTableView.getItems().add("Contract ID1");
//    contractTableView.getItems().add("Contract ID2");

    }

}
