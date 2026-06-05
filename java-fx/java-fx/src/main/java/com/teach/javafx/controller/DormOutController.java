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

public class DormOutController {
    @FXML
    private TableView<Map<String,Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> studentNameColumn;
    @FXML
    private TableColumn<Map,String> buildingNoColumn;
    @FXML
    private TableColumn<Map,String> roomNoColumn;
    @FXML
    private TableColumn<Map,String> statusColumn;

    private ObservableList<Map<String,Object>> obsList = FXCollections.observableArrayList();

    @FXML
    public void onQueryButtonClick(){
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/dorm/outList", req);
        if(res != null && res.getCode() == 0){
            List<Map<String,Object>> list = (List)res.getData();
            obsList.clear();
            obsList.addAll(list);
        }
    }

    @FXML
    public void onOutSaveClick(){
        Map<String,Object> row = dataTableView.getSelectionModel().getSelectedItem();
        if(row == null) return;
        DataRequest req = new DataRequest();
        req.add("id", row.get("id"));
        HttpRequestUtil.request("/api/dorm/outSave", req);
        onQueryButtonClick();
    }

    @FXML
    public void initialize(){
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        buildingNoColumn.setCellValueFactory(new MapValueFactory<>("buildingNo"));
        roomNoColumn.setCellValueFactory(new MapValueFactory<>("roomNo"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("status"));
        dataTableView.setItems(obsList);
        onQueryButtonClick();
    }
}