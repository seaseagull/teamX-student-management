package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DormitoryController 宿舍管理控制类 对应 dormitory-panel.fxml
 */
public class DormitoryController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> buildingNoColumn;
    @FXML
    private TableColumn<Map,String> roomNoColumn;
    @FXML
    private TableColumn<Map,String> bedCountColumn;
    @FXML
    private TableColumn<Map,String> usedBedColumn;
    @FXML
    private TableColumn<Map,FlowPane> operateColumn;

    // 新增：新增宿舍按钮
    @FXML
    private Button addDormBtn;

    private List<Map<String,Object>> dormitoryList = new ArrayList<>();
    private final ObservableList<Map<String,Object>> observableList= FXCollections.observableArrayList();

    // 当前登录用户类型 1=管理员 2=学生 3=教师
    private Integer loginUserType = 1;

    @FXML
    public void onQueryButtonClick(){
        DataResponse res;
        DataRequest req =new DataRequest();
        res = HttpRequestUtil.request("/api/personnel/dormitory/list",req);
        if(res != null && res.getCode()== 0) {
            dormitoryList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    // 新增：新增宿舍按钮点击事件
    @FXML
    public void onAddDormClick(){
        onQueryButtonClick();
    }

    private void setTableViewData() {
        observableList.clear();
        Map<String,Object> map;
        FlowPane flowPane;
        Button saveButton, deleteButton;
        for (int j = 0; j < dormitoryList.size(); j++) {
            map = dormitoryList.get(j);
            flowPane = new FlowPane();
            flowPane.setHgap(5);
            flowPane.setAlignment(Pos.CENTER);

            saveButton = new Button("修改保存");
            saveButton.setId("save"+j);
            saveButton.setOnAction(e-> saveItem(((Button)e.getSource()).getId()));

            deleteButton = new Button("删除");
            deleteButton.setId("delete"+j);
            deleteButton.setOnAction(e-> deleteItem(((Button)e.getSource()).getId()));

            // 权限控制：非管理员禁用修改/删除按钮
            if(!loginUserType.equals(1)){
                saveButton.setDisable(true);
                deleteButton.setDisable(true);
            }

            flowPane.getChildren().addAll(saveButton, deleteButton);
            map.put("operate",flowPane);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    public void saveItem(String name){
        if(name == null) return;
        int j = Integer.parseInt(name.substring(4));
        Map<String,Object> data = dormitoryList.get(j);
        DataRequest req = new DataRequest();
        req.add("id", data.get("id"));
        req.add("buildingNo", data.get("buildingNo"));
        req.add("roomNo", data.get("roomNo"));
        req.add("bedCount", data.get("bedCount"));
        DataResponse res = HttpRequestUtil.request("/api/personnel/dormitory/save", req);
        if(res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
        }else {
            MessageDialog.showDialog(res.getMsg());
        }
        onQueryButtonClick();
    }

    public void deleteItem(String name){
        if(name == null) return;
        int j = Integer.parseInt(name.substring(5));
        Map<String,Object> data = dormitoryList.get(j);
        DataRequest req = new DataRequest();
        req.add("id", data.get("id"));
        DataResponse res = HttpRequestUtil.request("/api/personnel/dormitory/delete", req);
        if(res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
        }else {
            MessageDialog.showDialog(res.getMsg());
        }
        onQueryButtonClick();
    }

    @FXML
    public void initialize() {
        buildingNoColumn.setCellValueFactory(new MapValueFactory<>("buildingNo"));
        buildingNoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        buildingNoColumn.setOnEditCommit(event -> {
            Map<String,Object> map = event.getRowValue();
            map.put("buildingNo", event.getNewValue());
        });

        roomNoColumn.setCellValueFactory(new MapValueFactory<>("roomNo"));
        roomNoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        roomNoColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("roomNo", event.getNewValue());
        });

        bedCountColumn.setCellValueFactory(new MapValueFactory<>("bedCount"));
        bedCountColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        bedCountColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("bedCount", event.getNewValue());
        });

        usedBedColumn.setCellValueFactory(new MapValueFactory<>("usedBed"));
        operateColumn.setCellValueFactory(new MapValueFactory<>("operate"));

        dataTableView.setEditable(true);

        // ===================== 权限控制 =====================
        try {
            DataRequest req = new DataRequest();
            DataResponse userRes = HttpRequestUtil.request("/api/user/getCurrentUser", req);
            if (userRes != null && userRes.getCode() == 0 && userRes.getData() != null) {
                Map<String, Object> userInfo = (Map<String, Object>) userRes.getData();
                loginUserType = Integer.parseInt(userInfo.get("user_type_id").toString());
            }
        } catch (Exception e) {
            loginUserType = 1;
        }

        // 非管理员隐藏新增宿舍按钮
        if(addDormBtn != null && !loginUserType.equals(1)){
            addDormBtn.setVisible(false);
        }
        // ====================================================

        onQueryButtonClick();
    }
}