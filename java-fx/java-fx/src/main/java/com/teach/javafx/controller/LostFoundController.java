package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LostFoundController {
    @FXML
    private TableView<Map<String,Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> titleColumn;
    @FXML
    private TableColumn<Map,String> contentColumn;
    @FXML
    private TableColumn<Map,String> timeColumn;

    private List<Map<String,Object>> dataList = new ArrayList<>();
    private final ObservableList<Map<String,Object>> obsList = FXCollections.observableArrayList();

    @FXML
    public void onQueryButtonClick(){
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/dorm/lostList",req);
        if(res != null && res.getCode()==0){
            dataList = (List<Map<String, Object>>) res.getData();
        }
        setTableData();
        // ✅ 这就是强制刷新表格（加这一行）
        dataTableView.refresh();    }

    private void setTableData(){
        obsList.clear();
        obsList.addAll(dataList);
        dataTableView.setItems(obsList);
    }

    @FXML
    public void initialize(){
        // 标题列：null时显示"无标题"
        titleColumn.setCellValueFactory(p -> {
            Object value = p.getValue().get("title");
            return new SimpleStringProperty(value != null ? value.toString() : "无标题");
        });

        // 详情列：null时显示"无详情"
        contentColumn.setCellValueFactory(p -> {
            Object value = p.getValue().get("content");
            return new SimpleStringProperty(value != null ? value.toString() : "无详情");
        });

        // 时间列：保持不变（后端保证有值）
        timeColumn.setCellValueFactory(p ->
                new SimpleStringProperty(String.valueOf(p.getValue().get("createTime")))
        );

        onQueryButtonClick();
    }

    @FXML

    public void onAddClick(){
        //第一步：输入标题
        TextInputDialog titleDia = new TextInputDialog("");
        titleDia.setTitle("新增失物招领");
        titleDia.setHeaderText("填写物品标题");
        Optional<String> titleOpt = titleDia.showAndWait();
        if(titleOpt.isEmpty()) return;
        String title = titleOpt.get();

        //第二步：输入物品详情
        TextInputDialog contentDia = new TextInputDialog("");
        contentDia.setHeaderText("填写物品详情");
        Optional<String> contentOpt = contentDia.showAndWait();
        if(contentOpt.isEmpty()) return;
        String content = contentOpt.get();

        //调用后端新增接口 /api/dorm/lostSave
        DataRequest req = new DataRequest();
        req.add("title",title);
        req.add("content",content);
        HttpRequestUtil.request("/api/dorm/lostSave",req);

        //新增完毕自动刷新表格
        onQueryButtonClick();
    }
}