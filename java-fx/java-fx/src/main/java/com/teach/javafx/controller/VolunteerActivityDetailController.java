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
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VolunteerActivityDetailController extends ToolController {

    private static Map activityData;
    private Map activity;

    private static String backPage = "volunteer-activity";
    private static String backTitle = "志愿活动";

    @FXML
    private Button startSignupButton, stopSignupButton, finishButton, signupButton;
    @FXML
    private Label statusTag, nameLabel, timeLabel, locationLabel, hoursLabel, recruitLabel, workLabel, requirementsLabel, notesLabel, signupTimeLabel, volunteerCountLabel, titleLabel;
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
        String activityDate = (String) activity.get("activityData");
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
        }
    }

    private void setupButtons(String status, int signed, int recruit) {
        if ("ROLE_ADMIN".equals(roleName)) {
            startSignupButton.setVisible(true);
            startSignupButton.setManaged(true);
            stopSignupButton.setVisible(true);
            stopSignupButton.setManaged(true);
            finishButton.setVisible(true);
            finishButton.setManaged(true);
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
        if (res != null && res.getCode() == 0) {
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
    }

    @FXML
    private void onStopSignupClick() {
        MessageDialog.showDialog("已截止报名！");
    }

    @FXML
    private void onFinishClick() {
        changeActivityStatus("FINISHED");
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
            } else {
                MessageDialog.showDialog(res != null ? res.getMsg() : "报名失败");
            }
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
            goBack();
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
        }
    }
}
