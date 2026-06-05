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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DormEmptyStatController {
    @FXML
    private TableView<Map<String,Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> buildingNoColumn;
    @FXML
    private TableColumn<Map,String> roomNoColumn;
    @FXML
    private TableColumn<Map,String> totalBedColumn;
    @FXML
    private TableColumn<Map,String> usedBedColumn;
    @FXML
    private TableColumn<Map,String> emptyBedColumn;

    private List<Map<String,Object>> dataList = new ArrayList<>();
    private final ObservableList<Map<String,Object>> obsList = FXCollections.observableArrayList();

    @FXML
    public void onQueryButtonClick(){
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/personnel/dorm/emptyStat",req);
        if(res != null && res.getCode()==0){
            dataList = (List<Map<String, Object>>) res.getData();
        }
        setTableData();
    }

    private void setTableData(){
        obsList.clear();
        obsList.addAll(dataList);
        dataTableView.setItems(obsList);
    }

    @FXML
    public void initialize(){
        buildingNoColumn.setCellValueFactory(new MapValueFactory<>("buildingNo"));
        roomNoColumn.setCellValueFactory(new MapValueFactory<>("roomNo"));
        totalBedColumn.setCellValueFactory(new MapValueFactory<>("bedCount"));
        usedBedColumn.setCellValueFactory(new MapValueFactory<>("usedBed"));
        emptyBedColumn.setCellValueFactory(new MapValueFactory<>("emptyBed"));
        onQueryButtonClick();
    }
}