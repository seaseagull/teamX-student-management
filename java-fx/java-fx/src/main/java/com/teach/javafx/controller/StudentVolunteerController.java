package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentVolunteerController extends ToolController {
    @FXML
    private Label totalHoursLabel, activityCountLabel;
    @FXML
    private VBox studentActivityListBox;

    @FXML
    public void initialize() {
        loadSudentActivityList();
    }

    private void loadSudentActivityList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/volunteer/getStudentActivityList", req);

        if (res == null || res.getCode() != 0) return;

        Map<String, Object> result = (Map<String, Object>) res.getData();
        if (result == null) return;

        // 设置统计
        Object totalHoursObj = result.get("totalHours");
        double totalHours = totalHoursObj instanceof Double ? (Double) totalHoursObj : 0;
        totalHoursLabel.setText(String.format("%.1f", totalHours));

        Object countObj = result.get("activityCount");
        int count = countObj instanceof Double ? ((Double) countObj).intValue() : (Integer) countObj;
        activityCountLabel.setText(String.valueOf(count));

        List<Map> studentActivityList = (ArrayList<Map>) result.get("activities");
        if (studentActivityList == null) studentActivityList = new ArrayList<>();

        studentActivityListBox.getChildren().clear();
        for (Map sa : studentActivityList) {
            VBox card = createStudentActivityCard(sa);
            studentActivityListBox.getChildren().add(card);
        }
    }

    private VBox createStudentActivityCard(Map activity) {
        String name = (String) activity.get("name");
        String activityDate = (String) activity.get("activityDate");
        String location = (String) activity.get("location");
        Object hoursObj = activity.get("hoursEarned");
        String hours = hoursObj != null ? String.valueOf(hoursObj) : "0";
        String status = (String) activity.get("signupStatus");

        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setFocusTraversable(false);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #F0F0F0; -fx-border-radius: 8; -fx-cursor: hand;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 8; -fx-border-color: #1677FF; -fx-border-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #F0F0F0; -fx-border-radius: 8; -fx-cursor: hand;"));

        HBox row1 = new HBox(10);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A1A;");
        Label statusTag = new Label(getStatusText(status));
        statusTag.setStyle(getStatusStyle(status) + "-fx-padding: 2 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        row1.getChildren().addAll(nameLabel, statusTag);

        // 第二行：日期 + 地点
        Label infoLabel = new Label(activityDate + " | " + location);
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");

        // 第三行：志愿时长
        Label hoursLabel = new Label("志愿时长：" + hours + " 小时");
        hoursLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1677FF; -fx-font-weight: bold;");

        card.getChildren().addAll(row1, infoLabel, hoursLabel);
        card.setOnMouseClicked(e -> openDetail(activity));

        return card;
    }

    private void openDetail(Map activity) {
        MainFrameController mainFrameController = (MainFrameController) AppStore.getMainFrameController();
        VolunteerActivityDetailController.setBackPage("student-volunteer-panel", "志愿查询");
        VolunteerActivityDetailController.setActivityData(activity);

        mainFrameController.changeContent("volunteer-activity-detail-panel", "活动详情");
    }

    private String getStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "SIGNED" -> "已报名";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }

    private String getStatusStyle(String status) {
        if (status == null) return "";
        return switch (status) {
            case "SIGNED" -> "-fx-background-color: #F0F5FF; -fx-text-fill: #1677FF;";
            case "COMPLETED" -> "-fx-background-color: #F5F5F5; -fx-text-fill: #999;";
            case "CANCELLED" -> "-fx-background-color: #FFF1F0; -fx-text-fill: #FF4D4F;";
            default -> "";
        };
    }
}
