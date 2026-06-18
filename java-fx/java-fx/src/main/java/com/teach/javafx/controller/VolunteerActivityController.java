package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VolunteerActivityController extends ToolController {
    @FXML
    private HBox tabBox;
    @FXML
    private Label allTab;
    @FXML
    private Label pendingTab;
    @FXML
    private Label ongoingTab;
    @FXML
    private Label finishedTab;
    @FXML
    private VBox activityListBox;
    @FXML
    private VBox studentActivityBox;
    @FXML
    private VBox studentActivityListBox;

    @FXML
    private Button addButton;

    private List<Map> activityList = new ArrayList<>();
    private String currentTab = "ALL";
    private String roleName;

    @FXML
    public void initialize() {
        roleName = AppStore.getJwt().getRole();

        if ("ROLE_ADMIN".equals(roleName)) {
            addButton.setVisible(true);
            addButton.setManaged(true);
        }

        if ("ROLE_STUDENT".equals(roleName)) {
            studentActivityBox.setVisible(true);
            studentActivityBox.setManaged(true);
        }

        setupTabs();
        loadActivityList();
    }

    private void setupTabs() {
        allTab.setOnMouseClicked(e -> switchTab("ALL", allTab));
        pendingTab.setOnMouseClicked(e -> switchTab("PENDING", pendingTab));
        ongoingTab.setOnMouseClicked(e -> switchTab("ONGOING", ongoingTab));
        finishedTab.setOnMouseClicked(e -> switchTab("FINISHED", finishedTab));

        highlightTab(allTab);
    }

    private void switchTab(String status, Label tab) {
        currentTab = status;
        highlightTab(tab);
        renderActivityList();
    }

    private void highlightTab(Label selected) {
        Label[] tabs = {allTab, pendingTab, ongoingTab, finishedTab};
        for (Label tab : tabs) {
            if(tab == selected) {
                tab.setStyle("-fx-padding: 6 20; -fx-cursor: hand; -fx-background-radius: 4; -fx-background-color: white; -fx-font-weight: bold; -fx-text-fill: #1677FF;");
            } else {
                tab.setStyle("-fx-padding: 6 20; -fx-cursor: hand; -fx-background-radius: 4; -fx-background-color: transparent; -fx-text-fill: #666;");
            }
        }
    }

    private void loadActivityList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/volunteer/getActivityList", req);

        if(res != null && res.getCode() == 0) {
            activityList = (ArrayList<Map>) res.getData();
        }
        if (activityList == null) activityList = new ArrayList<>();

        renderActivityList();

        if("ROLE_STUDENT".equals(roleName)) {
            loadStudentActivityList();
        }
    }

    private void loadStudentActivityList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/volunteer/getStudentActivityList", req);

        studentActivityListBox.getChildren().clear();
        if(res != null && res.getCode() == 0) {
            Map<String, Object> result = (Map<String, Object>) res.getData();
            if (result == null) return;
            List<Map> studentActivityList = (ArrayList<Map>) result.get("activities");
            if (studentActivityList != null) {
                for (Map sa : studentActivityList) {
                    HBox item = createStudentActivityItem(sa);
                    studentActivityListBox.getChildren().add(item);
                }
            }
        }
    }

    private void renderActivityList() {
        activityListBox.getChildren().clear();

        for (Map activity : activityList) {
            String status = (String) activity.get("status");
            if (!"ALL".equals(currentTab) && !currentTab.equals(status)) continue;
            VBox card = createActivityCard(activity);
            activityListBox.getChildren().add(card);
        }
    }

    private VBox createActivityCard(Map activity) {
        String name = (String) activity.get("name");
        String location = (String) activity.get("location");
        String activityDate = (String) activity.get("activityDate");
        Object recruitObj = activity.get("recruitCount");
        int recruit = recruitObj instanceof Double ? ((Double) recruitObj).intValue() : (Integer) recruitObj;
        Object signedObj = activity.get("signedCount");
        int signed = signedObj != null ? (signedObj instanceof Double ? ((Double) signedObj).intValue() : (Integer) signedObj) : 0;
        String status = (String) activity.get("status");

        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setFocusTraversable(false);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #F0F0F0; -fx-border-radius: 8; -fx-cursor: hand;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 8; -fx-border-color: #1677FF; -fx-border-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #F0F0F0; -fx-border-radius: 8; -fx-cursor: hand;"));

        HBox row1 = new HBox(10);
        Label statusTag = new Label(getStatusText(status));
        statusTag.setStyle(getStatusStyle(status) + "-fx-padding: 2 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A1A;");
        row1.getChildren().addAll(nameLabel, new Region(), statusTag);
        HBox.setHgrow(row1.getChildren().get(1), Priority.ALWAYS);

        HBox row2 = new HBox(10);
        Label infoLabel = new Label(activityDate + "|" + location);
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");
        Label recruitLabel = new Label("已报名 " + signed + "/" + recruit + "人");
        recruitLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (signed >= recruit ? "#FF4D4F" : "#1677FF") + "; -fx-font-weight: bold;");
        row2.getChildren().addAll(infoLabel, new Region(), recruitLabel);
        HBox.setHgrow(row2.getChildren().get(1), Priority.ALWAYS);

        card.getChildren().addAll(row1, row2);
        card.setOnMouseClicked(e -> openDetail(activity));

        return card;
    }

    private HBox createStudentActivityItem(Map activity) {
        String name = (String) activity.get("name");
        String status = (String) activity.get("status");

        HBox item = new HBox(5);
        item.setPadding(new Insets(5));
        item.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 6; -fx-cursor: hand;");
        item.setFocusTraversable(false);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);

        Label statusLabel = new Label(getStatusText(status));
        statusLabel.setStyle(getStatusStyle(status) + "-fx-font-size: 10px;");

        item.getChildren().addAll(nameLabel,new Region(), statusLabel);
        HBox.setHgrow(item.getChildren().get(1), Priority.ALWAYS);
        item.setOnMouseClicked(e -> openDetail(activity));

        return item;
    }

    private String getStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "待开始";
            case "ONGOING" -> "进行中";
            case "FINISHED" -> "已结束";
            default -> status;
        };
    }

    private String getStatusStyle(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "-fx-background-color: #FFF7E6; -fx-text-fill: #FA8C16;";
            case "ONGOING" -> "-fx-background-color: #F0F5FF; -fx-text-fill: #1677FF;";
            case "FINISHED" -> "-fx-background-color: #F5F5F5; -fx-text-fill: #999;";
            default -> "";
        };
    }

    private void openDetail(Map activity) {
        MainFrameController mainFrameController = (MainFrameController) AppStore.getMainFrameController();
        VolunteerActivityDetailController.setBackPage("volunteer-activity-panel", "志愿活动");
        VolunteerActivityDetailController.setActivityData(activity);

        mainFrameController.changeContent("volunteer-activity-detail-panel", "活动详情");
    }

    @FXML
    private void onAddButtonClick() {
        showAddDialog();
    }

    private void showAddDialog() {
        Stage stage = new Stage();
        stage.setTitle("添加志愿活动");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(16);
        root.getStyleClass().add("page-root");
        root.setPrefWidth(860);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(250,252,255,0.98), rgba(236,242,250,0.94));");
        root.setPadding(new Insets(18));

        VBox header = new VBox(4);
        Label titleLabel = new Label("添加志愿活动");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #102a43;");
        Label subtitleLabel = new Label("完善活动信息后保存，界面风格与系统保持一致");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #667085;");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        VBox card = new VBox(14);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.94); -fx-background-radius: 22; -fx-border-color: rgba(226, 232, 240, 0.94); -fx-border-radius: 22; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.14), 20, 0.16, 0, 6);");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(2, 2, 2, 2));

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(110);
        labelColumn.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        TextField nameField = new TextField();
        nameField.setPrefWidth(420);
        nameField.setPromptText("请输入活动名称，例如：春季校园清洁活动");

        TextField locationField = new TextField();
        locationField.setPrefWidth(420);
        locationField.setPromptText("请输入活动地点，例如：图书馆东门");

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(420);
        datePicker.setPromptText("请选择活动日期");

        HBox startTimePicker = createTimePicker("09", "00");
        HBox endTimePicker = createTimePicker("12", "00");

        HBox timeBox = new HBox(10,
                createInlineFieldTitle("开始时间"), startTimePicker,
                createInlineFieldTitle("结束时间"), endTimePicker);
        timeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextArea workField = new TextArea();
        workField.setPrefRowCount(3);
        workField.setWrapText(true);
        workField.setPromptText("请输入志愿工作内容，例如：引导签到、秩序维护、物资整理");

        TextField recruitField = new TextField();
        recruitField.setPromptText("请输入招募人数，例如：20");
        TextField hoursField = new TextField();
        hoursField.setPromptText("请输入志愿时长，例如：2.5");

        HBox countBox = new HBox(12,
                createInlineFieldTitle("招募人数"), recruitField,
                createInlineFieldTitle("志愿时长"), hoursField);
        HBox.setHgrow(recruitField, Priority.ALWAYS);
        HBox.setHgrow(hoursField, Priority.ALWAYS);

        TextArea requirementsField = new TextArea();
        requirementsField.setPrefRowCount(3);
        requirementsField.setWrapText(true);
        requirementsField.setPromptText("请输入活动要求，例如：准时到场、服从安排、穿着志愿马甲");

        TextArea notesField = new TextArea();
        notesField.setPrefRowCount(3);
        notesField.setWrapText(true);
        notesField.setPromptText("请输入注意事项，例如：活动当天请携带学生证，雨天活动顺延");

        DatePicker signupStartDate = new DatePicker();
        signupStartDate.setPrefWidth(180);
        signupStartDate.setPromptText("开始日期");
        HBox signupStartTimePicker = createTimePicker("00", "00");
        HBox signupStartBox = new HBox(8, createInlineFieldTitle("开始时间"), signupStartDate, signupStartTimePicker);

        DatePicker signupEndDate = new DatePicker();
        signupEndDate.setPrefWidth(180);
        signupEndDate.setPromptText("截止日期");
        HBox signupEndTimePicker = createTimePicker("23", "59");
        HBox signupEndBox = new HBox(8, createInlineFieldTitle("截止时间"), signupEndDate, signupEndTimePicker);

        VBox signupBox = new VBox(8, signupStartBox, signupEndBox);

        grid.addRow(0, createFieldTitle("活动名称", "请输入活动名称，例如：春季校园清洁活动"), nameField);
        grid.addRow(1, createFieldTitle("活动地点", "请输入活动地点，例如：图书馆东门"), locationField);
        grid.addRow(2, createFieldTitle("活动日期", "请选择志愿活动举行的日期"), datePicker);
        grid.addRow(3, createFieldTitle("活动时间", "设置开始和结束时间"), timeBox);
        grid.addRow(4, createFieldTitle("工作内容", "简要说明志愿者需要做什么"), workField);
        grid.addRow(5, createFieldTitle("招募与时长", "填写招募人数与志愿服务时长"), countBox);
        grid.addRow(6, createFieldTitle("活动要求", "如报名条件、着装要求等"), requirementsField);
        grid.addRow(7, createFieldTitle("注意事项", "如集合地点、签到说明等"), notesField);
        grid.addRow(8, createFieldTitle("报名时间", "设置可报名的起止时间"), signupBox);

        Button saveButton = ButtonFactory.createSaveButton("保存");
        Button cancelButton = ButtonFactory.createCancelButton("取消");

        HBox buttons = new HBox(10, new Pane(), cancelButton, saveButton);
        HBox.setHgrow(buttons.getChildren().get(0), Priority.ALWAYS);

        card.getChildren().addAll(grid, new Separator(), buttons);
        root.getChildren().addAll(header, card);

        saveButton.setOnAction(e -> {
            if (nameField.getText().isEmpty()) {
                MessageDialog.showDialog("活动名称为必填项！");
                return;
            }
            if (locationField.getText().isEmpty()) {
                MessageDialog.showDialog("活动地点为必填项！");
                return;
            }
            if (workField.getText().isEmpty()) {
                MessageDialog.showDialog("志愿工作内容为必填项！");
                return;
            }
            if (recruitField.getText().isEmpty()) {
                MessageDialog.showDialog("招募人数为必填项！");
                return;
            }
            if (hoursField.getText().isEmpty()) {
                MessageDialog.showDialog("志愿时长为必填项！");
                return;
            }
            if (signupStartDate.getValue() == null) {
                MessageDialog.showDialog("报名开始时间为必填项！");
                return;
            }

            DataRequest req = new DataRequest();
            req.add("name", nameField.getText());
            req.add("location", locationField.getText());
            String activityDate = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
            req.add("activityDate", activityDate);
            req.add("startTime", getTimeValue(startTimePicker));
            req.add("endTime", getTimeValue(endTimePicker));
            req.add("workDescription", workField.getText());
            req.add("recruitCount", Integer.parseInt(recruitField.getText()));
            req.add("volunteerHours", hoursField.getText());
            req.add("requirements", requirementsField.getText());
            req.add("notes", notesField.getText());
            String signupStart = (signupStartDate.getValue() != null ? signupStartDate.getValue().toString() : "")
                    + " " + getTimeValue(signupStartTimePicker);
            String signupEnd = (signupEndDate.getValue() != null ? signupEndDate.getValue().toString() : "")
                    + " " + getTimeValue(signupEndTimePicker);

            req.add("signupStart", signupStart);
            req.add("signupEnd", signupEnd);

            DataResponse res = HttpRequestUtil.request("/api/volunteer/activitySave", req);

            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("添加成功！");
                stage.close();
                loadActivityList();
            }
            stage.close();
        });

        cancelButton.setOnAction(e -> stage.close());
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setFillWidth(true);

        stage.setScene(new Scene(scrollPane, 900, 760));
        if (stage.getScene() != null) {
            stage.getScene().getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
        }
        stage.showAndWait();
    }

    @Override
    public void doRefresh() {
        loadActivityList();  // 重新加载
    }

    private VBox createFieldTitle(String title, String hint) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #344054;");
        Label hintLabel = new Label(hint);
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #667085;");
        hintLabel.setWrapText(true);
        VBox box = new VBox(2, titleLabel, hintLabel);
        box.setMinWidth(150);
        return box;
    }

    private Label createInlineFieldTitle(String title) {
        Label label = new Label(title + ":");
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #475467;");
        return label;
    }

    private void styleDatePicker(DatePicker datePicker) {
        datePicker.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(208, 213, 221, 0.92); -fx-background-color: rgba(255, 255, 255, 0.92);");
        datePicker.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                datePicker.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #1677ff; -fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(22, 119, 255, 0.18), 10, 0.12, 0, 0);");
            } else {
                datePicker.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(208, 213, 221, 0.92); -fx-background-color: rgba(255, 255, 255, 0.92);");
            }
        });
    }

    private TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPrefWidth(420);
        field.setPromptText(promptText);
        field.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                field.setStyle("-fx-background-color: white; -fx-border-color: #1677ff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(22, 119, 255, 0.18), 10, 0.12, 0, 0);");
            } else {
                field.setStyle("");
            }
        });
        return field;
    }

    private TextArea createStyledTextArea(String promptText) {
        TextArea area = new TextArea();
        area.setPrefRowCount(3);
        area.setWrapText(true);
        area.setPromptText(promptText);
        area.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                area.setStyle("-fx-background-color: white; -fx-border-color: #1677ff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(22, 119, 255, 0.18), 10, 0.12, 0, 0);");
            } else {
                area.setStyle("");
            }
        });
        return area;
    }

    // 时间选择器工具方法
    private HBox createTimePicker(String defaultHour, String defaultMinute) {
        ComboBox<String> hourBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(String.format("%02d", i));
        }
        hourBox.setValue(defaultHour != null ? defaultHour : "09");
        hourBox.setPrefWidth(80);

        ComboBox<String> minuteBox = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            minuteBox.getItems().add(String.format("%02d", i));
        }
        minuteBox.setValue(defaultMinute != null ? defaultMinute : "00");
        minuteBox.setPrefWidth(80);

        hourBox.setId("hour");
        minuteBox.setId("minute");

        hourBox.getStyleClass().add("combo-box");
        minuteBox.getStyleClass().add("combo-box");

        HBox timePicker = new HBox(6, hourBox, new Label(":"), minuteBox);
        timePicker.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return timePicker;
    }

    // 获取时间值
    private String getTimeValue(HBox timePicker) {
        ComboBox<String> hourBox = (ComboBox<String>) timePicker.getChildren().get(0);
        ComboBox<String> minuteBox = (ComboBox<String>) timePicker.getChildren().get(2);
        return hourBox.getValue() + ":" + minuteBox.getValue();
    }
}