package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DormitoryStudentController 宿舍学生管理控制类 对应 dormitory-student-panel.fxml
 */
public class DormitoryStudentController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> studentIdColumn;
    @FXML
    private TableColumn<Map,String> bedNoColumn;
    @FXML
    private TableColumn<Map,String> checkInDateColumn;
    @FXML
    private TableColumn<Map,FlowPane> operateColumn;

    private Integer dormId;
    private DormitoryController parentController;
    private List<Map<String,Object>> studentList = new ArrayList<>();
    private final ObservableList<Map<String,Object>> observableList= FXCollections.observableArrayList();

    public void setDormId(Integer dormId) {
        this.dormId = dormId;
        loadStudentList();
    }

    public void setParentController(DormitoryController parentController) {
        this.parentController = parentController;
    }

    private void loadStudentList() {
        DataResponse res;
        DataRequest req =new DataRequest();
        req.add("dormId", dormId);
        res = HttpRequestUtil.request("/api/personnel/dormitory/studentList",req);
        if(res != null && res.getCode()== 0) {
            studentList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        Map<String,Object> map;
        FlowPane flowPane;
        Button checkoutButton;
        for (int j = 0; j < studentList.size(); j++) {
            map = studentList.get(j);
            flowPane = new FlowPane();
            flowPane.setHgap(10);
            flowPane.setAlignment(Pos.CENTER);

            checkoutButton = new Button("退宿");
            checkoutButton.setId("checkout"+j);
            checkoutButton.setOnAction(e-> checkoutItem(((Button)e.getSource()).getId()));

            flowPane.getChildren().add(checkoutButton);
            map.put("operate",flowPane);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    @FXML
    private void onAssignButtonClick(){
        // 用JavaFX原生输入框替代不存在的InputDialog
        TextInputDialog studentDialog = new TextInputDialog();
        studentDialog.setTitle("分配学生");
        studentDialog.setHeaderText("请输入学生ID：");
        Optional<String> studentResult = studentDialog.showAndWait();
        if(studentResult.isEmpty()) return;
        String studentIdStr = studentResult.get();

        TextInputDialog bedDialog = new TextInputDialog();
        bedDialog.setTitle("分配学生");
        bedDialog.setHeaderText("请输入床位号：");
        Optional<String> bedResult = bedDialog.showAndWait();
        if(bedResult.isEmpty()) return;
        String bedNo = bedResult.get();

        try {
            Integer studentId = Integer.parseInt(studentIdStr);
            DataRequest req = new DataRequest();
            req.add("studentId", studentId);
            req.add("dormId", dormId);
            req.add("bedNo", bedNo);
            DataResponse res = HttpRequestUtil.request("/api/personnel/dormitory/assign", req);
            if(res.getCode() == 0) {
                MessageDialog.showDialog("分配成功！");
                loadStudentList();
                parentController.onQueryButtonClick(); // 现在可以正常调用了
            }else {
                MessageDialog.showDialog(res.getMsg());
            }
        } catch (NumberFormatException e) {
            MessageDialog.showDialog("学生ID必须是数字");
        }
    }

    public void checkoutItem(String name){
        if(name == null) return;
        int j = Integer.parseInt(name.substring(8));
        Map<String,Object> data = studentList.get(j);
        DataRequest req = new DataRequest();
        req.add("studentDormId", data.get("id"));
        DataResponse res = HttpRequestUtil.request("/api/personnel/dormitory/checkout", req);
        if(res.getCode() == 0) {
            MessageDialog.showDialog("退宿成功！");
            loadStudentList();
            parentController.onQueryButtonClick();
        }else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
    /**
     * 打开宿舍学生管理弹窗
     * @param dorm 宿舍信息
     */
    public static void showDialog(Map<String, Object> dorm) {
        try {
            FXMLLoader loader = new FXMLLoader(DormitoryStudentController.class.getResource("/com/teach/javafx/dormitory-student-panel.fxml"));
            Stage stage = new Stage();
            stage.setTitle("宿舍学生管理 - " + dorm.get("buildingNo") + dorm.get("roomNo"));
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            DormitoryStudentController controller = loader.getController();
            controller.setDormId((Integer) dorm.get("id"));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        studentIdColumn.setCellValueFactory(new MapValueFactory<>("studentId"));
        bedNoColumn.setCellValueFactory(new MapValueFactory<>("bedNo"));
        checkInDateColumn.setCellValueFactory(new MapValueFactory<>("checkInDate"));
        operateColumn.setCellValueFactory(new MapValueFactory<>("operate"));
    }

}