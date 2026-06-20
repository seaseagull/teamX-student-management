package com.teach.javafx.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teach.javafx.models.Exam;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExamManagementController {

    @FXML private TableView<Exam> examTable;
    @FXML private TableColumn<Exam, Long> colExamId;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, String> colExamType;
    @FXML private TableColumn<Exam, LocalDateTime> colStartTime;
    @FXML private TableColumn<Exam, LocalDateTime> colEndTime;
    @FXML private TableColumn<Exam, Integer> colDuration;
    @FXML private TableColumn<Exam, String> colLocation;
    @FXML private TableColumn<Exam, String> colStatus;

    private ObservableList<Exam> examList = FXCollections.observableArrayList();
    private final Gson gson = new Gson();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        // 完全保留你原来的表格绑定代码
        colExamId.setCellValueFactory(new PropertyValueFactory<>("examId"));
        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colExamType.setCellValueFactory(new PropertyValueFactory<>("examType"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 只加这一行：格式化时间显示（不会抛异常）
        formatTableDateTime();
        examTable.setItems(examList);

        // 页面初始化完成后自动刷新一次，避免首次打开为空
        Platform.runLater(this::refreshExamList);
    }

    // ====================== 1. 刷新列表按钮（已修复）======================
    @FXML
    public void refreshExamList() {
        try {
            DataResponse res = HttpRequestUtil.request("/api/exam/getExamList", new DataRequest());
            if (isSuccess(res)) {
                Map<String, Object> resultData = gson.fromJson(gson.toJson(res.getData()), Map.class);
                List<Map<String, Object>> dataList = gson.fromJson(gson.toJson(resultData.get("dataList")), new TypeToken<List<Map<String, Object>>>(){}.getType());

                examList.clear();
                for (Map<String, Object> item : dataList) {
                    Exam exam = new Exam();
                    exam.setExamId(((Number) item.get("examId")).longValue());
                    exam.setExamName((String) item.get("examName"));
                    exam.setExamType((String) item.get("examType"));

                    String startTimeStr = (String) item.get("startTime");
                    if (startTimeStr != null) {
                        exam.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
                    }

                    String endTimeStr = (String) item.get("endTime");
                    if (endTimeStr != null) {
                        exam.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
                    }

                    exam.setDuration(((Number) item.get("duration")).intValue());
                    exam.setLocation((String) item.get("location"));
                    exam.setStatus((String) item.get("status"));

                    examList.add(exam);
                }

                showAlert(Alert.AlertType.INFORMATION, "刷新成功，共" + examList.size() + "条数据");
            } else {
                showAlert(Alert.AlertType.ERROR, getErrorMsg(res));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "刷新失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================== 2. 新增考试按钮 ======================
    @FXML
    public void addExam() {
        showExamDialog("新增考试", null);
    }

    // ====================== 3. 编辑考试按钮 ======================
    @FXML
    public void editExam() {
        Exam selected = examTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "请先选择一条考试数据");
            return;
        }
        showExamDialog("编辑考试", selected);
    }

    // ====================== 4. 查看详情按钮 ======================
    @FXML
    public void viewExamDetail() {
        Exam selected = examTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "请先选择一条考试数据");
            return;
        }

        try {
            DataRequest req = new DataRequest();
            req.add("examId", selected.getExamId());
            DataResponse res = HttpRequestUtil.request("/api/exam/getExamInfo", req);

            if (isSuccess(res)) {
                // 1. 先解析为 Map，避免 Gson 直接反序列化 LocalDateTime
                Map<String, Object> data = gson.fromJson(gson.toJson(res.getData()), Map.class);

                // 2. 直接从 Map 里取字段，格式化显示
                String detail = String.format(
                        "考试ID：%d\n考试名称：%s\n考试类型：%s\n开始时间：%s\n结束时间：%s\n时长：%d分钟\n地点：%s\n状态：%s",
                        ((Number) data.get("examId")).longValue(),
                        data.get("examName"),
                        data.get("examType"),
                        data.get("startTime"),  // 后端已经返回格式化好的字符串，直接用
                        data.get("endTime"),
                        ((Number) data.get("duration")).intValue(),
                        data.get("location"),
                        data.get("status")
                );
                showAlert(Alert.AlertType.INFORMATION, detail);
            } else {
                showAlert(Alert.AlertType.ERROR, getErrorMsg(res));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "查询详情失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    // ====================== 5. 删除考试按钮 ======================
    @FXML
    public void deleteExam() {
        Exam selected = examTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "请先选择一条考试数据");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定删除选中的考试吗？");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DataRequest req = new DataRequest();
                req.add("examId", selected.getExamId());
                DataResponse res = HttpRequestUtil.request("/api/exam/examDelete", req);

                if (isSuccess(res)) {
                    showAlert(Alert.AlertType.INFORMATION, "删除考试成功");
                    refreshExamList(); // 删除后自动刷新
                } else {
                    showAlert(Alert.AlertType.ERROR, getErrorMsg(res));
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "删除失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ====================== 通用：新增/编辑对话框（已修复）======================
    private void showExamDialog(String title, Exam exam) {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initOwner(examTable.getScene().getWindow());
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        VBox root = new VBox(16);
        root.getStyleClass().add("content-card");
        root.setPadding(new Insets(18));
        root.setPrefSize(680, 520);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        Label subtitleLabel = new Label("请补充考试基础信息后保存");
        subtitleLabel.getStyleClass().add("page-subtitle");
        VBox titleBox = new VBox(4, titleLabel, subtitleLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(e -> stage.close());
        header.getChildren().addAll(titleBox, closeButton);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHalignment(javafx.geometry.HPos.RIGHT);
        labelCol.setMinWidth(96);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setPrefWidth(460);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField txtName = new TextField();
        TextField txtType = new TextField("期中考试");
        TextField txtStartTime = new TextField(LocalDateTime.now().format(formatter));
        TextField txtEndTime = new TextField(LocalDateTime.now().plusHours(2).format(formatter));
        TextField txtDuration = new TextField("120");
        TextField txtLocation = new TextField("教学楼A101");
        TextField txtStatus = new TextField("未开始");
        List<TextField> allFields = List.of(txtName, txtType, txtStartTime, txtEndTime, txtDuration, txtLocation, txtStatus);
        allFields.forEach(field -> field.setPrefHeight(40));
        txtName.setPromptText("请输入考试名称");
        txtType.setPromptText("请输入考试类型");
        txtStartTime.setPromptText("yyyy-MM-dd HH:mm:ss");
        txtEndTime.setPromptText("yyyy-MM-dd HH:mm:ss");
        txtDuration.setPromptText("请输入分钟数");
        txtLocation.setPromptText("请输入考试地点");
        txtStatus.setPromptText("请输入状态");

        if (exam != null) {
            txtName.setText(exam.getExamName());
            txtType.setText(exam.getExamType());
            txtStartTime.setText(exam.getStartTime().format(formatter));
            txtEndTime.setText(exam.getEndTime().format(formatter));
            txtDuration.setText(exam.getDuration().toString());
            txtLocation.setText(exam.getLocation());
            txtStatus.setText(exam.getStatus());
        }

        grid.addRow(0, new Label("考试名称"), txtName);
        grid.addRow(1, new Label("考试类型"), txtType);
        grid.addRow(2, new Label("开始时间"), txtStartTime);
        grid.addRow(3, new Label("结束时间"), txtEndTime);
        grid.addRow(4, new Label("时长(分钟)"), txtDuration);
        grid.addRow(5, new Label("考试地点"), txtLocation);
        grid.addRow(6, new Label("状态"), txtStatus);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button cancelButton = new Button("取消");
        cancelButton.getStyleClass().add("secondary-button");
        Button saveButton = new Button("保存");
        saveButton.getStyleClass().add("primary-button");
        footer.getChildren().addAll(cancelButton, saveButton);

        root.getChildren().addAll(header, grid, footer);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
        stage.setScene(scene);

        Runnable submit = () -> {
            try {
                String name = txtName.getText().trim();
                String type = txtType.getText().trim();
                String startTimeText = txtStartTime.getText().trim();
                String endTimeText = txtEndTime.getText().trim();
                String durationText = txtDuration.getText().trim();
                String location = txtLocation.getText().trim();
                String status = txtStatus.getText().trim();

                if (name.isEmpty()) { txtName.requestFocus(); return; }
                if (type.isEmpty()) { txtType.requestFocus(); return; }
                if (location.isEmpty()) { txtLocation.requestFocus(); return; }
                if (status.isEmpty()) { txtStatus.requestFocus(); return; }

                LocalDateTime startTime = LocalDateTime.parse(startTimeText, formatter);
                LocalDateTime endTime = LocalDateTime.parse(endTimeText, formatter);
                int duration = Integer.parseInt(durationText);
                if (duration <= 0) {
                    txtDuration.requestFocus();
                    return;
                }
                if (!endTime.isAfter(startTime)) {
                    txtEndTime.requestFocus();
                    return;
                }

                Exam e = exam == null ? new Exam() : exam;
                e.setExamName(name);
                e.setExamType(type);
                e.setStartTime(startTime);
                e.setEndTime(endTime);
                e.setDuration(duration);
                e.setLocation(location);
                e.setStatus(status);

                DataRequest req = new DataRequest();
                Map<String, Object> form = new HashMap<>();
                form.put("examId", e.getExamId());
                form.put("examName", e.getExamName());
                form.put("examType", e.getExamType());
                form.put("startTime", e.getStartTime().format(formatter));
                form.put("endTime", e.getEndTime().format(formatter));
                form.put("duration", e.getDuration());
                form.put("location", e.getLocation());
                form.put("status", e.getStatus());
                req.add("form", form);

                DataResponse res = HttpRequestUtil.request("/api/exam/examEditSave", req);
                if (isSuccess(res)) {
                    showAlert(Alert.AlertType.INFORMATION, title + "成功");
                    stage.close();
                    refreshExamList();
                } else {
                    showAlert(Alert.AlertType.ERROR, getErrorMsg(res));
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, title + "失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        };

        saveButton.setOnAction(e -> submit.run());
        cancelButton.setOnAction(e -> stage.close());
        stage.getScene().getRoot().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
            }
        });
        stage.setOnShown(e -> Platform.runLater(txtName::requestFocus));
        stage.showAndWait();
    }

    // ====================== 工具方法（完全保留）======================
    private void formatTableDateTime() {
        colStartTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(formatter));
            }
        });

        colEndTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(formatter));
            }
        });
    }

    // 已确认：后端成功码为0
    private boolean isSuccess(DataResponse res) {
        return res != null && res.getCode() == 0;
    }

    private String getErrorMsg(DataResponse res) {
        return res != null ? res.getMsg() : "后端服务未响应";
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}