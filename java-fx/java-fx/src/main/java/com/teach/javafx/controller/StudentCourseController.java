package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentCourseController extends ToolController {
    @FXML
    private FlowPane cardFlowPane;

    public void initialize() {
        loadCourses();
    }

    private void loadCourses() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/studentCourse/getStudentCourses", req);

        if (res == null || res.getCode() != 0) return;

        List<Map> courses = (ArrayList<Map>) res.getData();
        if (courses == null || courses.isEmpty()) return;

        renderCards(courses);
    }

    private void renderCards(List<Map> courses) {
        cardFlowPane.getChildren().clear();

        for (Map c : courses) {
            VBox card = createCourseCard(c);
            cardFlowPane.getChildren().add(card);
        }
    }

    private VBox createCourseCard(Map course) {
        String num = (String) course.get("num");
        String name = (String) course.get("name");
        Object creditObj = course.get("credit");
        String credit = creditObj != null ? String.valueOf(creditObj) : "";
        String teacher = (String) course.get("teacherName");

        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setPrefHeight(130);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #F0F0F0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);"
        );

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #FAFAFA;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #1677FF;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 2, 6);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #F0F0F0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);"
        ));

        Label numLabel = new Label(num);
        numLabel.setStyle("-fx-font-size: 11px;-fx-text-fill: #999");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A1A");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #F5F5F5;");

        HBox infoRow = new HBox(20);

        VBox creditBox = new VBox(3);
        Label creditValue = new Label(credit);
        creditValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1677FF;");
        Label creditLabel = new Label("学分");
        creditLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        creditBox.getChildren().addAll(creditLabel, creditValue);

        VBox teacherBox = new VBox(3);
        Label teacherValue = new Label(teacher != null ? teacher : "");
        teacherValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        Label teacherLabel = new Label("开课教师");
        teacherLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        teacherBox.getChildren().addAll(teacherLabel, teacherValue);

        infoRow.getChildren().addAll(creditBox, teacherBox);
        card.getChildren().addAll(numLabel, nameLabel, sep, infoRow);

        return card;
    }
}
