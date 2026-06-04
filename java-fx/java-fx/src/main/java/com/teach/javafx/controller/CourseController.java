package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.OptionItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CourseController 登录交互控制类 对应 course-panel.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class CourseController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> numColumn;
    @FXML
    private TableColumn<Map,String> nameColumn;
    @FXML
    private TableColumn<Map,String> creditColumn;
    @FXML
    private TableColumn<Map,String> teacherColumn;
    @FXML
    private TableColumn<Map,String> preCourseColumn;
    @FXML
    private TableColumn<Map,FlowPane> operateColumn;

    private List<Map<String,Object>> courseList = new ArrayList<>();  // 学生信息列表数据
    private final ObservableList<Map<String,Object>> observableList= FXCollections.observableArrayList();  // TableView渲染列表
    private List<OptionItem> teacherList;
    private List<OptionItem> preCourseList;


    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        teacherList = HttpRequestUtil.requestOptionItemList("/api/studentCourse/getTeacherItemOptionList",req);
        preCourseList = HttpRequestUtil.requestOptionItemList(
                "/api/course/getPreCourseItemOptionList", req);


        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        numColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numColumn.setOnEditCommit(event -> {
            Map<String,Object> map = event.getRowValue();
            map.put("num", event.getNewValue());
            saveRow(map);
        });
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("name", event.getNewValue());
            saveRow(map);
        });
        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        creditColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        creditColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("credit", event.getNewValue());
            saveRow(map);
        });
        teacherColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        preCourseColumn.setCellValueFactory(new MapValueFactory<>("preCourse"));
        preCourseColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        preCourseColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("preCourse", event.getNewValue());
            saveRow(map);
        });
        operateColumn.setCellValueFactory(new MapValueFactory<>("operate"));
        operateColumn.setCellFactory(col -> {
            TableCell<Map, FlowPane> cell = new TableCell<>() {
                @Override
                protected void updateItem(FlowPane item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(item);
                    }
                }
            };
            return cell;
        });
        dataTableView.setEditable(true);
        onQueryButtonClick();
    }

    @FXML
    private void onQueryButtonClick(){
        DataResponse res;
        DataRequest req =new DataRequest();
        res = HttpRequestUtil.request("/api/course/getCourseList",req); //从后台获取所有学生信息列表集合
        if(res != null && res.getCode()== 0) {
            courseList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
       observableList.clear();
       for (int j = 0; j < courseList.size(); j++) {
                Map map = courseList.get(j);
                FlowPane flowPane = new FlowPane();
                flowPane.setHgap(10);
                flowPane.setAlignment(Pos.CENTER);
           Button deleteButton = ButtonFactory.createDeleteButton("删除");
                deleteButton.setId("delete"+j);
           final int index = j;
           deleteButton.setOnAction(e -> deleteCourse(index));
           flowPane.getChildren().add(deleteButton);
           map.put("operate", flowPane);
           observableList.add(map);
            }
       dataTableView.setItems(observableList);
    }



    private void saveRow(Map row) {
        DataRequest req = new DataRequest();

        Object idObj = row.get("courseId");
        Integer courseId;
        if (idObj instanceof String) {
            courseId = Integer.parseInt((String) idObj);
        } else if (idObj instanceof Double) {
            courseId = ((Double) idObj).intValue();
        } else {
            courseId = (Integer) idObj;
        }
        req.add("courseId", courseId);
        req.add("num", row.get("num"));
        req.add("name", row.get("name"));

        Object creditObj = row.get("credit");
        if (creditObj != null) {
            String creditStr = creditObj.toString();
            req.add("credit", Integer.parseInt(creditStr.contains(".") ? creditStr.substring(0, creditStr.indexOf(".")) : creditStr));
        }

        DataResponse res = HttpRequestUtil.request("/api/course/courseSave", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
        }
    }

    private void deleteCourse(int index) {
        Map data = courseList.get(index);
        Object idObj = data.get("courseId");
        Integer courseId;
        if (idObj instanceof String) {
            courseId = Integer.parseInt((String) idObj);
        } else if (idObj instanceof Double) {
            courseId = ((Double) idObj).intValue();
        } else {
            courseId = (Integer) idObj;
        }

        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        DataResponse res = HttpRequestUtil.request("/api/course/courseDelete", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            onQueryButtonClick();
        }
    }

    @FXML
    private void onAddButtonClick() {
        showAddDialog();
    }

    private void showAddDialog() {
        Stage stage = new Stage();
        stage.setTitle("添加课程");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField numField = new TextField();
        numField.setPromptText("课程号");
        numField.setPrefWidth(250);

        TextField nameField = new TextField();
        nameField.setPromptText("课程名");
        nameField.setPrefWidth(250);

        TextField creditField = new TextField();
        creditField.setPromptText("学分");
        creditField.setPrefWidth(250);

        ComboBox<OptionItem> teacherBox = new ComboBox<>();
        teacherBox.getItems().addAll(teacherList);
        teacherBox.setPrefWidth(250);

        ComboBox<OptionItem> preCourseBox = new ComboBox<>();
        preCourseBox.getItems().addAll(preCourseList);
        preCourseBox.getSelectionModel().selectFirst();
        preCourseBox.setPrefWidth(250);



        grid.addRow(0, new Label("课程号:"), numField);
        grid.addRow(1, new Label("课程名:"), nameField);
        grid.addRow(2, new Label("学分:"), creditField);
        grid.addRow(3, new Label("教师:"), teacherBox);
        grid.addRow(4, new Label("前序课:"), preCourseBox);

        Button saveButton = ButtonFactory.createSaveButton("保存");

        Button cancelButton = ButtonFactory.createCancelButton("取消");

        HBox buttons = new HBox(10, new Pane(), cancelButton, saveButton);
        HBox.setHgrow(buttons.getChildren().get(0), Priority.ALWAYS);

        VBox root = new VBox(15, grid, new Separator(), buttons);
        root.setPadding(new Insets(10));

        saveButton.setOnAction(e -> {
            String num = numField.getText().trim();
            String name = nameField.getText().trim();
            String credit = creditField.getText().trim();

            if (num.isEmpty()) {
                MessageDialog.showDialog("课程号为必填项！");
                return;
            }
            if (name.isEmpty()) {
                MessageDialog.showDialog("课程名为必填项！");
                return;
            }
            if (credit.isEmpty()) {
                MessageDialog.showDialog("学分为必填项！");
                return;
            }
            if (teacherBox.getSelectionModel().isEmpty()) {
                MessageDialog.showDialog("教师为必填项！");
                return;
            }

            DataRequest req = new DataRequest();
            req.add("num", num);
            req.add("name", name);
            req.add("credit", Integer.parseInt(credit));

            OptionItem teacher = teacherBox.getSelectionModel().getSelectedItem();
            req.add("teacherId", Integer.parseInt(teacher.getValue()));

            OptionItem preCourse = preCourseBox.getSelectionModel().getSelectedItem();
            if (preCourse != null && !"0".equals(preCourse.getValue())) {
                req.add("preCourseId", Integer.parseInt(preCourse.getValue()));
            }

            DataResponse res = HttpRequestUtil.request("/api/course/save", req);
            if(res != null && res.getCode() == 0) {
                MessageDialog.showDialog("添加成功！");
                stage.close();
                onQueryButtonClick();
            }
        });

        cancelButton.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 400, 320));
        stage.showAndWait();
    }


}
