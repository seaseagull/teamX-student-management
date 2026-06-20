package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.OptionItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.text.Text;
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
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

/**
 * CourseController 登录交互控制类 对应 course-panel.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class CourseController {
    private static final String DEBUG_LOG_PATH = "debug-d90a1c.log";

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

    private void debugLog(String hypothesisId, String location, String message, Map<String, Object> data) {
        try (FileWriter fw = new FileWriter(DEBUG_LOG_PATH, true)) {
            fw.write("{\"sessionId\":\"d90a1c\",\"id\":\"course_" + System.nanoTime() + "\",\"timestamp\":" + Instant.now().toEpochMilli() + ",\"runId\":\"run1\",\"hypothesisId\":\"" + hypothesisId + "\",\"location\":\"" + location + "\",\"message\":\"" + message.replace("\"", "'") + "\",\"data\":\"" + String.valueOf(data).replace("\"", "'") + "\"}\n");
        } catch (IOException ignored) {}
    }

    @FXML
    public void initialize() {
        debugLog("H1", "CourseController.initialize:entry", "initialize invoked", Map.of("dataTableViewInjected", dataTableView != null, "numColumnInjected", numColumn != null, "nameColumnInjected", nameColumn != null, "teacherColumnInjected", teacherColumn != null));
        DataRequest req = new DataRequest();
        teacherList = HttpRequestUtil.requestOptionItemList("/api/studentCourse/getTeacherItemOptionList",req);
        preCourseList = HttpRequestUtil.requestOptionItemList(
                "/api/course/getPreCourseItemOptionList", req);
        debugLog("H2", "CourseController.initialize:options", "option lists loaded", Map.of("teacherListSize", teacherList == null ? -1 : teacherList.size(), "preCourseListSize", preCourseList == null ? -1 : preCourseList.size()));


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
        debugLog("H3", "CourseController.onQueryButtonClick:response", "course list request finished", Map.of("responseNull", res == null, "responseCode", res == null ? -1 : res.getCode(), "responseDataClass", res != null && res.getData() != null ? res.getData().getClass().getName() : "null"));
        if(res != null && res.getCode()== 0) {
            courseList = res.getData() == null ? new ArrayList<>() : new ArrayList<>((List<Map<String, Object>>) res.getData());
        }
        setTableViewData();
    }

    private void setTableViewData() {
       observableList.clear();
       debugLog("H4", "CourseController.setTableViewData:entry", "building table items", Map.of("courseListSize", courseList == null ? -1 : courseList.size()));
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
       debugLog("H4", "CourseController.setTableViewData:exit", "table items assigned", Map.of("observableSize", observableList.size(), "tableItemsNull", dataTableView.getItems() == null));
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
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.setTitle("添加课程");
        stage.initOwner(dataTableView.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        VBox root = new VBox(16);
        root.getStyleClass().add("content-card");
        root.setPadding(new Insets(18));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        Label headerTitle = new Label("添加课程");
        headerTitle.getStyleClass().add("page-title");
        Label headerSubtitle = new Label("补充课程基础信息并保存");
        headerSubtitle.getStyleClass().add("page-subtitle");
        VBox titleBox = new VBox(4, headerTitle, headerSubtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(e -> stage.close());
        header.getChildren().addAll(titleBox, closeButton);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(84);
        labelCol.setPrefWidth(84);
        labelCol.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setPrefWidth(360);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField numField = new TextField();
        numField.setPromptText("课程号");
        numField.setPrefWidth(360);
        TextField nameField = new TextField();
        nameField.setPromptText("课程名");
        nameField.setPrefWidth(360);
        TextField creditField = new TextField();
        creditField.setPromptText("学分");
        creditField.setPrefWidth(360);

        ComboBox<OptionItem> teacherBox = new ComboBox<>();
        teacherBox.getItems().setAll(teacherList == null ? List.of() : teacherList);
        teacherBox.setPrefWidth(360);
        teacherBox.setPromptText("请选择教师");

        ComboBox<OptionItem> preCourseBox = new ComboBox<>();
        preCourseBox.getItems().setAll(preCourseList == null ? List.of() : preCourseList);
        preCourseBox.setPrefWidth(360);
        preCourseBox.setPromptText("无");
        if (!preCourseBox.getItems().isEmpty()) {
            preCourseBox.getSelectionModel().selectFirst();
        }

        grid.addRow(0, new Label("课程号"), numField);
        grid.addRow(1, new Label("课程名"), nameField);
        grid.addRow(2, new Label("学分"), creditField);
        grid.addRow(3, new Label("教师"), teacherBox);
        grid.addRow(4, new Label("前序课"), preCourseBox);

        Button cancelButton = ButtonFactory.createCancelButton("取消");
        cancelButton.getStyleClass().add("secondary-button");
        Button saveButton = ButtonFactory.createSaveButton("保存");
        saveButton.getStyleClass().add("primary-button");

        HBox buttons = new HBox(10, cancelButton, saveButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, grid, new Separator(), buttons);

        saveButton.setOnAction(e -> {
            String num = numField.getText().trim();
            String name = nameField.getText().trim();
            String credit = creditField.getText().trim();

            if (num.isEmpty()) {
                MessageDialog.showDialog("课程号为必填项！");
                numField.requestFocus();
                return;
            }
            if (name.isEmpty()) {
                MessageDialog.showDialog("课程名为必填项！");
                nameField.requestFocus();
                return;
            }
            if (credit.isEmpty()) {
                MessageDialog.showDialog("学分为必填项！");
                creditField.requestFocus();
                return;
            }
            if (teacherBox.getSelectionModel().isEmpty()) {
                MessageDialog.showDialog("教师为必填项！");
                teacherBox.requestFocus();
                return;
            }

            int creditValue;
            try {
                creditValue = Integer.parseInt(credit);
            } catch (NumberFormatException ex) {
                MessageDialog.showDialog("学分必须是整数！");
                creditField.requestFocus();
                return;
            }

            DataRequest req = new DataRequest();
            req.add("num", num);
            req.add("name", name);
            req.add("credit", creditValue);

            OptionItem teacher = teacherBox.getSelectionModel().getSelectedItem();
            req.add("teacherId", Integer.parseInt(teacher.getValue()));

            OptionItem preCourse = preCourseBox.getSelectionModel().getSelectedItem();
            if (preCourse != null && preCourse.getValue() != null && !"0".equals(preCourse.getValue())) {
                req.add("preCourseId", Integer.parseInt(preCourse.getValue()));
            }

            DataResponse res = HttpRequestUtil.request("/api/course/courseSave", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("添加成功！");
                stage.close();
                onQueryButtonClick();
            } else {
                MessageDialog.showDialog("保存失败，请稍后重试！");
            }
        });

        cancelButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 620, 420);
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.showAndWait();
    }


}
