package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class DormAllotController {
    @FXML
    private TableView<Map<String,Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> studentNameColumn;
    @FXML
    private TableColumn<Map,String> dormNameColumn;
    @FXML
    private TableColumn<Map,Number> bedNoColumn;
    @FXML
    private TableColumn<Map,String> stateColumn;

    private ObservableList<Map<String,Object>> obsList = FXCollections.observableArrayList();

    @FXML
    public void onQueryButtonClick(){
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/dorm/allotList", req);
        if(res != null && res.getCode() == 0){
            List<Map<String,Object>> list = (List)res.getData();
            obsList.clear();
            obsList.addAll(list);
        }
    }

    @FXML
    public void onAddClick(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/allotAdd.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("新增入住");
            stage.showAndWait();
            onQueryButtonClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize(){
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("stuName"));
        dormNameColumn.setCellValueFactory(new MapValueFactory<>("dormName"));
        bedNoColumn.setCellValueFactory(new MapValueFactory<>("bedNo"));
        stateColumn.setCellValueFactory(new MapValueFactory<>("state"));
        dataTableView.setItems(obsList);
        onQueryButtonClick();
    }
}