package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;

import java.util.List;
import java.util.Map;

public class DormInfoController {
    @FXML
    private TableView<Map<String,Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> buildingNoColumn;
    @FXML
    private TableColumn<Map,String> roomNoColumn;
    @FXML
    private TableColumn<Map,Number> bedCountColumn;
    @FXML
    private TableColumn<Map,Number> usedBedColumn;
    @FXML
    private TableColumn<Map,Number> emptyBedColumn;

    private ObservableList<Map<String,Object>> obsList = FXCollections.observableArrayList();

    @FXML
    public void onQueryButtonClick(){
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/dorm/emptyStat", req);
        if(res != null && res.getCode() == 0){
            List<Map<String,Object>> list = (List)res.getData();
            obsList.clear();
            obsList.addAll(list);
        }
    }

    @FXML
    public void initialize(){
        buildingNoColumn.setCellValueFactory(new MapValueFactory<>("buildingNo"));
        roomNoColumn.setCellValueFactory(new MapValueFactory<>("roomNo"));
        bedCountColumn.setCellValueFactory(new MapValueFactory<>("bedCount"));
        usedBedColumn.setCellValueFactory(new MapValueFactory<>("usedBed"));
        emptyBedColumn.setCellValueFactory(new MapValueFactory<>("emptyBed"));
        dataTableView.setItems(obsList);
        onQueryButtonClick();
    }
}