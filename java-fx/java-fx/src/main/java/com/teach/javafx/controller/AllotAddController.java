package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AllotAddController {

    @FXML private TextField tfStuName;
    @FXML private TextField tfClassName;
    @FXML private TextField tfDormName;
    @FXML private TextField tfBedNo;
    @FXML private ComboBox<String> cbState;

    @FXML
    public void save() {
        DataRequest req = new DataRequest();
        req.add("stuName", tfStuName.getText());
        req.add("className", tfClassName.getText());
        req.add("dormName", tfDormName.getText());
        req.add("bedNo", tfBedNo.getText());
        req.add("state", cbState.getValue());

        HttpRequestUtil.request("/api/dorm/allotSave", req);

        Stage stage = (Stage) tfStuName.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancel() {
        Stage stage = (Stage) tfStuName.getScene().getWindow();
        stage.close();
    }
}