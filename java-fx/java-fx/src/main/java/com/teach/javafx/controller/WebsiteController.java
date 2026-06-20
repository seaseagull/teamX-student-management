package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class WebsiteController extends ToolController {
    @FXML
    private VBox contentVBox;
    @FXML
    private Button addButton;


    @FXML
    public void initialize() {
        String roleName = AppStore.getJwt().getRole();
        if ("ROLE_ADMIN".equals(roleName)) {
            addButton.setVisible(true);
            addButton.setManaged(true);
        }
        loadWebistes();
    }

    private void loadWebistes() {
        contentVBox.getChildren().clear();
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/website/getWebsites", req);

        if (res == null || res.getCode() != 0) return;

        List<Map> websites = (ArrayList<Map>) res.getData();
        if(websites == null || websites.isEmpty()) return;

        Map<String, List<Map>> categoryMap = new LinkedHashMap<>();
        for (Map w : websites) {
            String category = (String) w.get("category");
            if (category == null) category = "其他";
            categoryMap.computeIfAbsent(category, k->new ArrayList<>()).add(w);
        }

        for (Map.Entry<String, List<Map>> entry : categoryMap.entrySet()) {
            VBox categoryBox = createCategoryBox(entry.getKey(), entry.getValue());

            contentVBox.getChildren().add(categoryBox);
        }
    }

    private VBox createCategoryBox(String category, List<Map> websites) {
        VBox box = new VBox(12);
        box.getStyleClass().add("content-card");

        Label title = new Label(category);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #102a43;");

        FlowPane flowPane = new FlowPane(16, 16);
        flowPane.setPrefWrapLength(760);

        for (Map w : websites) {
            VBox card = createCard(w);
            flowPane.getChildren().add(card);
        }

        box.getChildren().addAll(title, flowPane);
        return box;
    }

    private VBox createCard(Map website) {
        String name = (String) website.get("name");
        String url = (String) website.get("url");
        String desc = (String) website.get("description");

        VBox card = new VBox(8);
        card.setFocusTraversable(false);
        card.setPrefHeight(96);
        card.setPrefWidth(230);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("website-card");
        card.setStyle("-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 14; -fx-border-color: rgba(226,232,240,0.95); -fx-border-radius: 14; -fx-cursor: hand;");

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(22,119,255,0.08); -fx-background-radius: 14; -fx-border-color: rgba(22,119,255,0.28); -fx-border-radius: 14; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 14; -fx-border-color: rgba(226,232,240,0.95); -fx-border-radius: 14; -fx-cursor: hand;"));

        Label nameLabe = new Label(name);
        nameLabe.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #1f2937;");

        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(30);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #667085;");

        card.getChildren().addAll(nameLabe, descLabel);
        card.setOnMouseClicked(e -> {
            String roleName = AppStore.getJwt().getRole();
            if ("ROLE_ADMIN".equals(roleName)) {
                showEditDialog(website);
            } else {
                openUrl(url);
            }
        });

        return card;
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddButtonClick() {
        showAddDialog();
    }

    private void showAddDialog() {
        Stage stage = new Stage();
        stage.setTitle("添加网站");
        stage.initOwner(contentVBox.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        VBox root = new VBox(16);
        root.getStyleClass().add("content-card");
        root.setPadding(new Insets(18));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        VBox titleBox = new VBox(4);
        Label title = new Label("添加网站");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("补充网站基础信息后保存");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(e -> stage.close());
        header.getChildren().addAll(titleBox, closeButton);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(14);
        gridPane.setVgap(14);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(76);
        labelCol.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField();
        nameField.setPromptText("网站名称");
        TextField urlField = new TextField();
        urlField.setPromptText("网址");
        TextArea descField = new TextArea();
        descField.setPromptText("描述");
        descField.setPrefRowCount(3);
        descField.setWrapText(true);
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("综合服务", "学习相关", "生活服务");
        categoryBox.getSelectionModel().selectFirst();

        nameField.setPrefHeight(40);
        urlField.setPrefHeight(40);
        categoryBox.setPrefHeight(40);
        descField.setPrefHeight(92);

        gridPane.addRow(0, new Label("名称"), nameField);
        gridPane.addRow(1, new Label("网址"), urlField);
        gridPane.addRow(2, new Label("描述"), descField);
        gridPane.addRow(3, new Label("分类"), categoryBox);

        Button cancelButton = new Button("取消");
        cancelButton.getStyleClass().add("secondary-button");
        Button saveButton = new Button("保存");
        saveButton.getStyleClass().add("primary-button");
        HBox buttons = new HBox(10, cancelButton, saveButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, gridPane, buttons);
        Scene scene = new Scene(root, 520, 380);
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
        stage.setScene(scene);

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();
            String desc = descField.getText().trim();
            String category = categoryBox.getValue();
            if (name.isEmpty() || url.isEmpty()) {
                return;
            }
            doSave(name, url, desc, category);
            stage.close();
            loadWebistes();
        });
        cancelButton.setOnAction(e -> stage.close());
        stage.showAndWait();
    }

    private void doSave(String name, String url, String desc, String category) {
        DataRequest req = new DataRequest();
        req.add("name", name);
        req.add("url", url);
        req.add("description", desc);
        req.add("category", category);

        HttpRequestUtil.request("/api/website/saveWebsite", req);
    }

    private void showEditDialog(Map website) {
        Stage stage = new Stage();
        stage.setTitle("编辑网站");
        stage.initOwner(contentVBox.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        VBox root = new VBox(16);
        root.getStyleClass().add("content-card");
        root.setPadding(new Insets(18));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        VBox titleBox = new VBox(4);
        Label title = new Label("编辑网站");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("可修改信息或删除该网站");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(e -> stage.close());
        header.getChildren().addAll(titleBox, closeButton);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(14);
        gridPane.setVgap(14);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(76);
        labelCol.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField((String) website.get("name"));
        TextField urlField = new TextField((String) website.get("url"));
        TextArea descField = new TextArea((String) website.get("description"));
        descField.setPrefRowCount(3);
        descField.setWrapText(true);
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("综合服务", "学习相关", "生活服务");
        categoryBox.getSelectionModel().select((String) website.getOrDefault("category", "综合服务"));

        gridPane.addRow(0, new Label("名称"), nameField);
        gridPane.addRow(1, new Label("网址"), urlField);
        gridPane.addRow(2, new Label("描述"), descField);
        gridPane.addRow(3, new Label("分类"), categoryBox);

        Button cancelButton = new Button("取消");
        cancelButton.getStyleClass().add("secondary-button");
        Button saveButton = new Button("保存");
        saveButton.getStyleClass().add("primary-button");
        Button deleteButton = new Button("删除");
        deleteButton.getStyleClass().add("danger-button");
        HBox buttons = new HBox(10, cancelButton, saveButton, deleteButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, gridPane, buttons);
        Scene scene = new Scene(root, 540, 400);
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
        stage.setScene(scene);

        Object idObj = website.get("id");
        Integer id = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();
            String desc = descField.getText().trim();
            String category = categoryBox.getValue();
            if (name.isEmpty() || url.isEmpty()) {
                return;
            }
            doSave(id, name, url, desc, category);
            stage.close();
            loadWebistes();
        });
        deleteButton.setOnAction(e -> {
            doDelete(id);
            stage.close();
            loadWebistes();
        });
        cancelButton.setOnAction(e -> stage.close());
        stage.showAndWait();
    }

    private void doSave(Integer id, String name, String url, String desc, String category) {
        DataRequest req = new DataRequest();
        req.add("id", id);
        req.add("name", name);
        req.add("url", url);
        req.add("description", desc);
        req.add("category", category);

        HttpRequestUtil.request("/api/website/saveWebsite", req);
    }

    private void doDelete(Integer id) {
        DataRequest req = new DataRequest();
        req.add("id", id);
        HttpRequestUtil.request("/api/website/deleteWebsite", req);
    }
}
