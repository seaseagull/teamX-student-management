package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class StudentCourseSelectController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map,String> numColumn;
    @FXML
    private TableColumn<Map,String> nameColumn;
    @FXML
    private TableColumn<Map,String> teacherNameColumn;
    @FXML
    private TableColumn<Map,String> creditColumn;
    @FXML
    private TableColumn<Map,String> preCourseColumn;
    @FXML
    private TableColumn<Map, FlowPane> operateColumn;


    @FXML
    private ComboBox<OptionItem> teacherFilterComboBox;
    @FXML
    private ComboBox<OptionItem> creditFilterComboBox;
    @FXML
    private ComboBox<OptionItem> stateFilterComboBox;
    @FXML
    private Label searchLabel;
    @FXML
    private TextField searchTextField;

    private List<Map> courseList = new ArrayList();  // 学生信息列表数据
    private final ObservableList<Map> observableList= FXCollections.observableArrayList();
    private List<OptionItem> teacherFilterList;
    private List<OptionItem> creditFilterList;
    private List<OptionItem> stateFilterList;



    private void setTableViewData() {
        observableList.clear();
        Map<String,Object> map;
        FlowPane flowPane;
        Button selectButton;
        for (int j = 0; j < courseList.size(); j++) {
            map = courseList.get(j);
            flowPane = new FlowPane();
            flowPane.setHgap(10);
            flowPane.setAlignment(Pos.CENTER);
            selectButton = new Button();
            Object isSelectedObj = map.get("isSelected");
            boolean isSelected = isSelectedObj instanceof Boolean ? (Boolean) isSelectedObj :false;
            if (isSelected) {
                selectButton.setText("取消选课");
            } else {
                selectButton.setText("选课");
            }
            selectButton.setId("select"+j);
            selectButton.setOnAction(e->{
                selectItem(((Button)e.getSource()).getId());
            });
            flowPane.getChildren().addAll(selectButton);
            map.put("operate",flowPane);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        teacherFilterList = HttpRequestUtil.requestOptionItemList("/api/studentCourse/getTeacherItemOptionList", req); //从后台获取所有学生信息列表集合
        teacherFilterList.add(new OptionItem(-1,"-1","全部"));
        teacherFilterComboBox.getItems().addAll(teacherFilterList);

        creditFilterList = new ArrayList<>();
        creditFilterList.add(new OptionItem(-1,"-1","全部"));
        creditFilterList.add(new OptionItem(1,"1","1"));
        creditFilterList.add(new OptionItem(2,"2","2"));
        creditFilterList.add(new OptionItem(3,"2","3"));
        creditFilterComboBox.getItems().addAll(creditFilterList);
        creditFilterComboBox.getSelectionModel().selectFirst();

        stateFilterList = new ArrayList<>();
        stateFilterList.add(new OptionItem(-1, "-1", "全部"));
        stateFilterList.add(new OptionItem(0, "0", "已选课"));
        stateFilterList.add(new OptionItem(1, "1", "未选课"));
        stateFilterComboBox.getItems().addAll(stateFilterList);
        stateFilterComboBox.getSelectionModel().selectFirst();

        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        teacherNameColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        preCourseColumn.setCellValueFactory(new MapValueFactory<>("preCourse"));
        operateColumn.setCellValueFactory(new MapValueFactory<>("operate"));
        setTableViewData();

        onQueryButtonClick();
    }

    @FXML
    private void onQueryButtonClick(){
        String search = searchTextField.getText();
        DataRequest req = new DataRequest();

        //状态筛选
        OptionItem stateOp;
        stateOp = stateFilterComboBox.getSelectionModel().getSelectedItem();
        if (stateOp != null) {
            req.add("state", Integer.parseInt(stateOp.getValue()));
        }

        //课程筛选
        OptionItem teacherOp;
        teacherOp = teacherFilterComboBox.getSelectionModel().getSelectedItem();
        if(teacherOp != null) {
            req.add("teacherId", Integer.parseInt(teacherOp.getValue()));
        }

        OptionItem creditOp;
        creditOp = creditFilterComboBox.getSelectionModel().getSelectedItem();
        if(creditOp != null) {
            req.add("credit", Integer.parseInt(creditOp.getValue()));
        }

        req.add("search", search);
        DataResponse res = HttpRequestUtil.request("/api/studentCourse/getCourseList", req);
        if (res != null && res.getCode() == 0) {
            courseList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    private void selectItem(String buttonId) {
        int index = Integer.parseInt(buttonId.replace("select",""));
        Map <String,Object> map = courseList.get(index);
        Object courseIdObj = map.get("courseId");
        Integer courseId = courseIdObj instanceof Double ?
                ((Double) courseIdObj).intValue() : (Integer) courseIdObj;

        FlowPane flowPane = (FlowPane) map.get("operate");
        Button button = (Button) flowPane.getChildren().get(0);

        if("选课".equals(button.getText())){
            doSelectCourse(courseId);
            button.setText("取消选课");
        } else {
            doCancelCourse(courseId);
            button.setText("选课");
        }
    }

    private void doSelectCourse(Integer courseId) {
        DataRequest req = new DataRequest();
        req.add("courseId",courseId);
        DataResponse res = HttpRequestUtil.request("/api/studentCourse/courseSelect",req);
        if(res != null && res.getCode() == 0) {
            MessageDialog.showDialog("选课成功！");
        }
    }

    private void doCancelCourse(Integer courseId) {
        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        DataResponse res = HttpRequestUtil.request("/api/studentCourse/courseCancel", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("取消选课成功！");
        }
    }
}
