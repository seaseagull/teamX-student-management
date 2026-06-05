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

public class DormElectricFeeController {
    @FXML
    private TableView<Map<String,Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> buildingNoColumn;
    @FXML
    private TableColumn<Map,String> roomNoColumn;
    @FXML
    private TableColumn<Map,Number> moneyColumn;
    @FXML
    private TableColumn<Map,String> dayColumn;

    private ObservableList<Map<String,Object>> obsList = FXCollections.observableArrayList();

    @FXML
    public void onQueryButtonClick(){
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/dorm/feeList", req);
        if(res != null && res.getCode() == 0){
            List<Map<String,Object>> list = (List)res.getData();
            obsList.clear();
            obsList.addAll(list);
        }
    }

    @FXML
    public void onPaySaveClick(){
        Map<String,Object> row = dataTableView.getSelectionModel().getSelectedItem();
        if(row == null) return;
        DataRequest req = new DataRequest();
        req.add("id", row.get("id"));
        HttpRequestUtil.request("/api/dorm/feePay", req);
        onQueryButtonClick();
    }

    @FXML
    public void initialize(){
        buildingNoColumn.setCellValueFactory(new MapValueFactory<>("buildingNo"));
        roomNoColumn.setCellValueFactory(new MapValueFactory<>("roomNo"));
        moneyColumn.setCellValueFactory(new MapValueFactory<>("money"));
        dayColumn.setCellValueFactory(new MapValueFactory<>("day"));
        dataTableView.setItems(obsList);
        onQueryButtonClick();
    }
}