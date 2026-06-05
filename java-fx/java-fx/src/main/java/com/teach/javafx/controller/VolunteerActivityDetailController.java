package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VolunteerActivityDetailController extends ToolController {

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

    private static Map activityData;
    private Map activity;

    private static String backPage = "volunteer-activity";
    private static String backTitle = "志愿活动";

    @FXML
    private Button startSignupButton, stopSignupButton, finishButton, signupButton, editActivityButton, deleteActivityButton;
    @FXML
    private Label statusTag, nameLabel, timeLabel, locationLabel, hoursLabel, recruitLabel, workLabel, requirementsLabel, notesLabel, signupTimeLabel, volunteerCountLabel;
    @FXML
    private VBox infoCard, requireCard, notesCard, volunteerCard;
    @FXML
    private TableView<Map> volunteerTable;
    @FXML
    private TableColumn<Map,String> volNameColumn, volNumColumn, volGenderColumn, volPhoneColumn;
    @FXML
    private TableColumn<Map, FlowPane> volOperateColumn;

    private String roleName;

    @Override
    public void doRefresh() {
        this.activity = activityData;  // 重新取数据
        renderDetail();                 // 重新渲染
    }

    public static void setActivityData(Map data) {
        activityData = data;
    }

    public static void setBackPage(String page, String title) {
        backPage = page;
        backTitle = title;
    }

    @FXML
    public void initialize() {
        this.activity = activityData;
        roleName = AppStore.getJwt().getRole();

        volNameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        volNumColumn.setCellValueFactory(new MapValueFactory<>("num"));
        volGenderColumn.setCellValueFactory(new MapValueFactory<>("gender"));
        volPhoneColumn.setCellValueFactory(new MapValueFactory<>("phone"));
        volOperateColumn.setCellValueFactory(new MapValueFactory<>("operate"));
        volOperateColumn.setCellFactory(col -> new TableCell<Map, FlowPane>(){
            @Override
            protected void updateItem(FlowPane item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else setGraphic(item);
            }
        });

        renderDetail();
    }

    private void renderDetail() {
        if (activity == null) return;

        volunteerTable.getItems().clear();

        String name = (String) activity.get("name");
        String location = (String) activity.get("location");
        String activityDate = (String) activity.get("activityDate");
        String startTime = (String) activity.get("startTime");
        String endTime = (String) activity.get("endTime");
        Object hoursObj = activity.get("volunteerHours");
        String hours = hoursObj != null ? String.valueOf(hoursObj) : "";
        Object recruitObj = activity.get("recruitCount");
        int recruit = recruitObj instanceof Double ? ((Double) recruitObj).intValue() : (Integer) recruitObj;
        Object signedObj = activity.get("signedCount");
        int signed = signedObj != null ? (signedObj instanceof Double ? ((Double) signedObj).intValue() : (Integer) signedObj) : 0;
        String work = (String) activity.get("workDescription");
        String requirements = (String) activity.get("requirements");
        String notes = (String) activity.get("notes");
        String signupStart = (String) activity.get("signupStart");
        String signupEnd = (String) activity.get("signupEnd");
        String status = (String) activity.get("status");
        if (status == null) {
            status = (String) activity.get("signupStatus");
        }

        nameLabel.setText(name);
        timeLabel.setText("📅 " + activityDate + "  " + startTime + " - " + endTime);
        locationLabel.setText("📍 " + location);
        hoursLabel.setText("⏱ 志愿时长：" + hours + " 小时");
        recruitLabel.setText("👥 已报名 " + signed + " / " + recruit + " 人");
        workLabel.setText(work != null ? work : "暂无");
        requirementsLabel.setText(requirements != null && !requirements.isEmpty() ? requirements : "暂无特殊要求");
        notesLabel.setText(notes != null && !notes.isEmpty() ? notes : "暂无特殊注意事项");
        signupTimeLabel.setText("报名时间：" + signupStart + " ~ " + signupEnd);
        setStatusTag(status);
        setupButtons(status, signed, recruit);

        if ("ROLE_ADMIN".equals(roleName)) {
            volunteerCard.setVisible(true);
            volunteerCard.setManaged(true);
            loadVolunteers();
        }
    }

    private void setStatusTag(String status) {
        if (status == null) {
            statusTag.setText("");
            return;
        }
        switch (status) {
            case "PENDING" -> {
                statusTag.setText("待开始");
                statusTag.setStyle("-fx-background-color: #FFF7E6; -fx-text-fill: #FA8C16; -fx-padding: 3 10; -fx-background-radius: 4;");
            }
            case "ONGOING" -> {
                statusTag.setText("进行中");
                statusTag.setStyle("-fx-background-color: #F0F5FF; -fx-text-fill: #1677FF; -fx-padding: 3 10; -fx-background-radius: 4;");
            }
            case "FINISHED" -> {
                statusTag.setText("已结束");
                statusTag.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #999; -fx-padding: 3 10; -fx-background-radius: 4;");
            }
            case "SIGNED" -> {
                statusTag.setText("已报名");
                statusTag.setStyle("-fx-background-color: #F0F5FF; -fx-text-fill: #1677FF; -fx-padding: 3 10; -fx-background-radius: 4;");
            }
            case "COMPLETED" -> {
                statusTag.setText("已完成");
                statusTag.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #999; -fx-padding: 3 10; -fx-background-radius: 4;");
            }
        }
    }

    private void setupButtons(String status, int signed, int recruit) {
        startSignupButton.setVisible(false);
        startSignupButton.setManaged(false);
        stopSignupButton.setVisible(false);
        stopSignupButton.setManaged(false);
        finishButton.setVisible(false);
        finishButton.setManaged(false);
        signupButton.setVisible(false);
        signupButton.setManaged(false);
        editActivityButton.setVisible(false);
        editActivityButton.setManaged(false);
        deleteActivityButton.setVisible(false);
        deleteActivityButton.setManaged(false);
        if ("ROLE_ADMIN".equals(roleName)) {
            startSignupButton.setVisible(true);
            startSignupButton.setManaged(true);
            stopSignupButton.setVisible(true);
            stopSignupButton.setManaged(true);
            finishButton.setVisible(true);
            finishButton.setManaged(true);
            editActivityButton.setVisible(true);
            editActivityButton.setManaged(true);
            deleteActivityButton.setVisible(true);
            deleteActivityButton.setManaged(true);
        } else if ("ROLE_STUDENT".equals(roleName)) {
            if ("ONGOING".equals(status) && signed < recruit) {
                signupButton.setVisible(true);
                signupButton.setManaged(true);

                checkIfSignedUp();
            }
        }
    }

    private void checkIfSignedUp() {
        DataRequest req = new DataRequest();
        Object idObj = activity.get("id");
        Integer activityId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;
        req.add("activityId", activityId);

        DataResponse res = HttpRequestUtil.request("/api/volunteer/checkSignup", req);
        if (res != null && res.getCode() == 1) {
            signupButton.setText("取消报名");
            signupButton.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6 16; -fx-background-radius: 4; -fx-cursor: hand;");
        }
    }

    private void loadVolunteers() {
        Object idObj = activity.get("id");
        Integer activityId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        DataRequest req = new DataRequest();
        req.add("activityId", activityId);
        DataResponse res = HttpRequestUtil.request("/api/volunteer/getVolunteers", req);

        if (res == null || res.getCode() != 0) return;

        List<Map> volunteers = (ArrayList<Map>) res.getData();
        if (volunteers == null) volunteers = new ArrayList<>();

        volunteerCountLabel.setText("共 " + volunteers.size() + " 人");

        ObservableList<Map> data = FXCollections.observableArrayList();
        for (Map v : volunteers) {
            FlowPane flowPane = new FlowPane();
            Button deleteButton = new Button("删除");
            deleteButton.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3;");
            deleteButton.setOnAction(e -> deleteVolunteer(v));
            flowPane.getChildren().add(deleteButton);
            v.put("operate", flowPane);
            data.add(v);
        }
        volunteerTable.setItems(data);
    }

    private void deleteVolunteer(Map volunteer) {
        Object signupIdObj = volunteer.get("id");
        Integer signupId = signupIdObj instanceof Double ? ((Double) signupIdObj).intValue() : (Integer) signupIdObj;

        DataRequest req = new DataRequest();
        req.add("signupId", signupId);
        DataResponse res = HttpRequestUtil.request("/api/volunteer/volunteerDelete", req);

        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            loadVolunteers();
        }
    }

    @FXML
    private void onStartSignupClick() {
        changeActivityStatus("ONGOING");
        statusTag.setText("进行中");
        statusTag.setStyle("-fx-background-color: #F0F5FF; -fx-text-fill: #1677FF; -fx-padding: 3 10; -fx-background-radius: 4;");
    }

    @FXML
    private void onStopSignupClick() {
        MessageDialog.showDialog("已截止报名！");
    }

    @FXML
    private void onFinishClick() {
        changeActivityStatus("FINISHED");
        statusTag.setText("已结束");
        statusTag.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #999; -fx-padding: 3 10; -fx-background-radius: 4;");
    }

    @FXML
    private void onSignUpClick() {
        Object idObj = activity.get("id");
        Integer activityId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        if ("取消报名".equals(signupButton.getText())) {
            // 取消报名
            DataRequest req = new DataRequest();
            req.add("activityId", activityId);
            DataResponse res = HttpRequestUtil.request("/api/volunteer/cancelSignup", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("已取消报名！");
                signupButton.setText("我要报名");
                signupButton.setStyle("-fx-background-color: #1677FF; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6 16; -fx-background-radius: 4; -fx-cursor: hand;");
                refreshActivityData(activityId);
            }
        } else {
            // 报名
            DataRequest req = new DataRequest();
            req.add("activityId", activityId);
            DataResponse res = HttpRequestUtil.request("/api/volunteer/signup", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("报名成功！");
                signupButton.setText("取消报名");
                signupButton.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6 16; -fx-background-radius: 4; -fx-cursor: hand;");
                refreshActivityData(activityId);
            } else {
                MessageDialog.showDialog(res != null ? res.getMsg() : "报名失败");
            }
        }

    }

    private void refreshActivityData(Integer activityId) {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/volunteer/getActivityList", req);
        if (res != null && res.getCode() == 0) {
            List<Map> activityList = (ArrayList<Map>) res.getData();
            if (activityList != null) {
                for (Map a : activityList) {
                    if (a.get("id") != null && activityId.equals(a.get("id") instanceof Double ?
                            ((Double) a.get("id")).intValue() : a.get("id"))) {
                        this.activity = a;
                        activityData = a;
                        renderDetail();
                        return;
                    }
                }
            }
        }
    }

    @FXML
    private void onEditActivityClick() {
        showEditDialog(activity);
    }

    private void showEditDialog(Map data) {
        Stage stage = new Stage();
        stage.setTitle("编辑志愿活动");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField((String) data.get("name"));
        nameField.setPrefWidth(350);

        TextField locationField = new TextField((String) data.get("location"));
        locationField.setPrefWidth(350);

        // 日期
        DatePicker datePicker = new DatePicker();
        String activityDate = (String) data.get("activityDate");
        if (activityDate != null && !activityDate.isEmpty()) {
            datePicker.setValue(java.time.LocalDate.parse(activityDate));
        }
        datePicker.setPrefWidth(350);

        // 时间
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        HBox startTimePicker = createTimePicker(
                startTime != null ? startTime.substring(0, 2) : "09",
                startTime != null ? startTime.substring(3, 5) : "00");
        HBox endTimePicker = createTimePicker(
                endTime != null ? endTime.substring(0, 2) : "12",
                endTime != null ? endTime.substring(3, 5) : "00");
        HBox timeBox = new HBox(10, new Label("开始:"), startTimePicker, new Label("结束:"), endTimePicker);

        TextArea workField = new TextArea((String) data.get("workDescription"));
        workField.setPrefHeight(60);
        workField.setWrapText(true);

        TextField recruitField = new TextField(String.valueOf(data.get("recruitCount")));
        TextField hoursField = new TextField(String.valueOf(data.get("volunteerHours")));
        HBox countBox = new HBox(10, recruitField, hoursField);

        TextArea requireField = new TextArea((String) data.get("requirements"));
        requireField.setPrefHeight(50);
        requireField.setWrapText(true);

        TextArea notesField = new TextArea((String) data.get("notes"));
        notesField.setPrefHeight(50);
        notesField.setWrapText(true);

        // 报名时间
        String signupStart = (String) data.get("signupStart");
        String signupEnd = (String) data.get("signupEnd");

        final DatePicker signupStartDate = new DatePicker();
        HBox signupStartTimePicker;
        if (signupStart != null && signupStart.contains(" ")) {
            String[] parts = signupStart.split(" ");
            signupStartDate.setValue(java.time.LocalDate.parse(parts[0]));
            signupStartTimePicker = createTimePicker(parts[1].substring(0, 2), parts[1].substring(3, 5));
        } else {
            signupStartTimePicker = createTimePicker("00", "00");
        }
        final HBox finalSignupStartTimePicker = signupStartTimePicker;

// 报名截止时间
        final DatePicker signupEndDate = new DatePicker();
        HBox signupEndTimePicker;
        if (signupEnd != null && signupEnd.contains(" ")) {
            String[] parts = signupEnd.split(" ");
            signupEndDate.setValue(java.time.LocalDate.parse(parts[0]));
            signupEndTimePicker = createTimePicker(parts[1].substring(0, 2), parts[1].substring(3, 5));
        } else {
            signupEndTimePicker = createTimePicker("23", "59");
        }
        final HBox finalSignupEndTimePicker = signupEndTimePicker;

        signupStartDate.setPrefWidth(150);
        signupEndDate.setPrefWidth(150);
        HBox signupStartBox = new HBox(5, new Label("开始:"), signupStartDate, signupStartTimePicker);
        HBox signupEndBox = new HBox(5, new Label("截止:"), signupEndDate, signupEndTimePicker);
        VBox signupBox = new VBox(5, signupStartBox, signupEndBox);

        grid.addRow(0, new Label("活动名称:"), nameField);
        grid.addRow(1, new Label("活动地点:"), locationField);
        grid.addRow(2, new Label("活动日期:"), datePicker);
        grid.addRow(3, new Label("时间:"), timeBox);
        grid.addRow(4, new Label("工作内容:"), workField);
        grid.addRow(5, new Label("招募/时长:"), countBox);
        grid.addRow(6, new Label("活动要求:"), requireField);
        grid.addRow(7, new Label("注意事项:"), notesField);
        grid.addRow(8, new Label("报名时间:"), signupBox);

        Button saveButton = ButtonFactory.createSaveButton("保存");
        Button cancelButton = ButtonFactory.createCancelButton("取消");

        HBox buttons = new HBox(10, new Pane(), cancelButton, saveButton);
        HBox.setHgrow(buttons.getChildren().get(0), Priority.ALWAYS);

        VBox root = new VBox(15, grid, new Separator(), buttons);
        root.setPadding(new Insets(10));

        Integer activityId = data.get("id") instanceof Double ?
                ((Double) data.get("id")).intValue() : (Integer) data.get("id");

        saveButton.setOnAction(e -> {
            DataRequest req = new DataRequest();
            req.add("activityId", activityId);
            req.add("name", nameField.getText());
            req.add("location", locationField.getText());
            req.add("activityDate", datePicker.getValue() != null ? datePicker.getValue().toString() : "");
            req.add("startTime", getTimeValue(startTimePicker));
            req.add("endTime", getTimeValue(endTimePicker));
            req.add("workDescription", workField.getText());
            req.add("recruitCount", Integer.parseInt(recruitField.getText()));
            req.add("volunteerHours", hoursField.getText());
            req.add("requirements", requireField.getText());
            req.add("notes", notesField.getText());
            req.add("signupStart", (signupStartDate.getValue() != null ? signupStartDate.getValue().toString() : "") + " " + getTimeValue(finalSignupStartTimePicker));
            req.add("signupEnd", (signupEndDate.getValue() != null ? signupEndDate.getValue().toString() : "") + " " + getTimeValue(finalSignupEndTimePicker));
            DataResponse res = HttpRequestUtil.request("/api/volunteer/activitySave", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("保存成功！");
                stage.close();
                refreshActivityData(activityId);
            }
            stage.close();
        });

        cancelButton.setOnAction(e -> stage.close());

        ScrollPane scrollPane = new ScrollPane(root);
        stage.setScene(new Scene(scrollPane, 700, 600));
        stage.showAndWait();
    }

    @FXML
    private void onDeleteActivityClick() {
        Object idObj = activity.get("id");
        Integer activityId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        DataRequest req = new DataRequest();
        req.add("activityId", activityId);
        DataResponse res = HttpRequestUtil.request("/api/volunteer/deleteActivity", req);

        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            goBack();
        }
    }

    private void changeActivityStatus(String newStatus) {
        Object idObj = activity.get("id");
        Integer activityId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        DataRequest req = new DataRequest();
        req.add("activityId", activityId);
        req.add("status", newStatus);
        DataResponse res = HttpRequestUtil.request("/api/volunteer/changeStatus", req);

        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("操作成功！");
        }
    }

    @FXML
    private void onBackClick() {
        goBack();
    }

    private void goBack() {
        MainFrameController mc = AppStore.getMainFrameController();
        if (mc != null) {
            mc.changeContent(backPage, backTitle);
            ToolController controller = mc.getToolController(backPage);
            if (controller instanceof VolunteerActivityController) {
                ((VolunteerActivityController) controller).doRefresh();
            }
            if (controller instanceof StudentVolunteerController) {
                ((StudentVolunteerController) controller).doRefresh();
            }
        }
    }
}
