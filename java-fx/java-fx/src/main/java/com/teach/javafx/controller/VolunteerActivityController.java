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

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPrefWidth(350);

        TextField locationField = new TextField();
        locationField.setPrefWidth(350);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(350);
        datePicker.setPromptText("选择日期");

        HBox startTimePicker = createTimePicker("09", "00");
        HBox endTimePicker = createTimePicker("12", "00");

        HBox timeBox = new HBox(10,
                new Label("开始:"), startTimePicker,
                new Label("结束:"), endTimePicker);

        TextArea workField = new TextArea();
        workField.setPrefHeight(60);
        workField.setWrapText(true);

        TextField recruitField = new TextField();
        TextField hoursField = new TextField();

        HBox countBox = new HBox(10, recruitField, hoursField);

        TextArea requirementsField = new TextArea();
        requirementsField.setPrefHeight(50);
        requirementsField.setWrapText(true);

        TextArea notesField = new TextArea();
        notesField.setPrefHeight(50);
        notesField.setWrapText(true);

        DatePicker signupStartDate = new DatePicker();
        signupStartDate.setPrefWidth(150);
        HBox signupStartTimePicker = createTimePicker("00", "00");
        HBox signupStartBox = new HBox(5, new Label("开始:"), signupStartDate, signupStartTimePicker);

        // 报名截止时间
        DatePicker signupEndDate = new DatePicker();
        signupEndDate.setPrefWidth(150);
        HBox signupEndTimePicker = createTimePicker("23", "59");
        HBox signupEndBox = new HBox(5, new Label("截止:"), signupEndDate, signupEndTimePicker);

        VBox signupBox = new VBox(5, signupStartBox, signupEndBox);

        grid.addRow(0, new Label("活动名称:"), nameField);
        grid.addRow(1, new Label("活动地点:"), locationField);
        grid.addRow(2, new Label("活动日期:"), datePicker);
        grid.addRow(3, new Label("时间:"), timeBox);
        grid.addRow(4, new Label("工作内容:"), workField);
        grid.addRow(5, new Label("招募/时长:"), countBox);
        grid.addRow(6, new Label("活动要求:"), requirementsField);
        grid.addRow(7, new Label("注意事项:"), notesField);
        grid.addRow(8, new Label("报名时间:"), signupBox);;

        Button saveButton = ButtonFactory.createSaveButton("保存");
        Button cancelButton = ButtonFactory.createCancelButton("取消");

        HBox buttons = new HBox(10, new Pane(), cancelButton, saveButton);
        HBox.setHgrow(buttons.getChildren().get(0), Priority.ALWAYS);

        VBox root = new VBox(15, grid, new Separator(), buttons);
        root.setPadding(new Insets(10));

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
        stage.setScene(new Scene(scrollPane, 700, 600));
        stage.showAndWait();
    }

    @Override
    public void doRefresh() {
        loadActivityList();  // 重新加载
    }

    // 时间选择器工具方法
    private HBox createTimePicker(String defaultHour, String defaultMinute) {
        ComboBox<String> hourBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(String.format("%02d", i));
        }
        hourBox.setValue(defaultHour != null ? defaultHour : "09");
        hourBox.setPrefWidth(70);

        ComboBox<String> minuteBox = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            minuteBox.getItems().add(String.format("%02d", i));
        }
        minuteBox.setValue(defaultMinute != null ? defaultMinute : "00");
        minuteBox.setPrefWidth(70);

        hourBox.setId("hour");
        minuteBox.setId("minute");

        HBox timePicker = new HBox(5, hourBox, new Label(":"), minuteBox);
        return timePicker;
    }

    // 获取时间值
    private String getTimeValue(HBox timePicker) {
        ComboBox<String> hourBox = (ComboBox<String>) timePicker.getChildren().get(0);
        ComboBox<String> minuteBox = (ComboBox<String>) timePicker.getChildren().get(2);
        return hourBox.getValue() + ":" + minuteBox.getValue();
    }
}