package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
        VBox box = new VBox(10);

        Label title = new Label(category);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333");

        FlowPane flowPane = new FlowPane(20, 15);
        flowPane.setPadding(new Insets(10, 10, 10, 10));
        flowPane.setPrefWrapLength(780);

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
        card.setPrefHeight(90);
        card.setPrefWidth(230);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #e8e8e8; -fx-border-radius: 8; -fx-background-color: #fafafa; -fx-background-radius: 8; -fx-cursor: hand");

        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #1890ff; -fx-border-radius: 8; -fx-background-color: #e6f7ff; -fx-background-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-border-color: #e8e8e8; -fx-border-radius: 8; -fx-background-color: #fafafa; -fx-background-radius: 8; -fx-cursor: hand;"));

        Label nameLabe = new Label(name);
        nameLabe.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333");

        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(30);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999");

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
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("网站名称");
        nameField.setPrefWidth(290);
        TextField urlField = new TextField();
        urlField.setPromptText("网址");
        urlField.setPrefWidth(290);
        TextArea descField = new TextArea();
        descField.setPromptText("描述");
        descField.setPrefWidth(290);
        descField.setPrefHeight(60);
        descField.setWrapText(true);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("综合服务", "学习相关", "生活服务");
        categoryBox.getSelectionModel().selectFirst();

        gridPane.addRow(0, new Label("名称:"), nameField);
        gridPane.addRow(1, new Label("网址:"), urlField );
        gridPane.addRow(2, new Label("描述:"), descField);
        gridPane.addRow(3, new Label("分类:"), categoryBox);

        Button saveButton = new Button("保存");
        saveButton.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");
        Button cancelButton = new Button("取消");
        HBox buttons = new HBox(10, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, gridPane, new Separator(), buttons);
        root.setPadding(new Insets(10));

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();
            String desc = descField.getText().trim();
            String category = categoryBox.getValue();

            if(name.isEmpty() || url.isEmpty()) {
                return;
            }

            doSave(name, url, desc, category);
            stage.close();
        });

        cancelButton.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 400, 300));
        stage.showAndWait();

        loadWebistes();
    }

    private void doSave(String name, String url, String desc, String category) {
        DataRequest req = new DataRequest();
        req.add("name", name);
        req.add("url", url);
        req.add("description", desc);
        req.add("category", category);

        DataResponse res = HttpRequestUtil.request("/api/website/saveWebsite", req);

    }

    private void showEditDialog(Map website) {
        Stage stage = new Stage();
        stage.setTitle("编辑网站");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        TextField nameField = new TextField((String) website.get("name"));
        TextField urlField = new TextField((String) website.get("url"));
        TextArea descField = new TextArea((String) website.get("description"));

        nameField.setPrefWidth(290);
        urlField.setPrefWidth(290);
        descField.setPrefWidth(290);
        descField.setPrefHeight(60);
        descField.setWrapText(true);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("综合服务", "学习相关", "生活服务");
        categoryBox.getSelectionModel().selectFirst();

        gridPane.addRow(0, new Label("名称:"), nameField);
        gridPane.addRow(1, new Label("网址:"), urlField );
        gridPane.addRow(2, new Label("描述:"), descField);
        gridPane.addRow(3, new Label("分类:"), categoryBox);

        Button saveButton = new Button("保存");
        saveButton.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");
        Button deleteButton = new Button("删除");
        deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        Button cancelButton = new Button("取消");
        HBox buttons = new HBox(10, new Pane(),saveButton, cancelButton, deleteButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, gridPane, new Separator(), buttons);
        root.setPadding(new Insets(10));

        Object idObj = website.get("id");
        Integer id = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();
            String desc = descField.getText().trim();
            String category = categoryBox.getValue();

            if(name.isEmpty() || url.isEmpty()) {
                return;
            }

            doSave(id, name, url, desc, category);
            stage.close();
        });

        deleteButton.setOnAction(e -> {
            doDelete(id);
            stage.close();
        });

        cancelButton.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 400, 300));
        stage.showAndWait();

        loadWebistes();
    }

    private void doSave(Integer id, String name, String url, String desc, String category) {
        DataRequest req = new DataRequest();
        req.add("id", id);
        req.add("name", name);
        req.add("url", url);
        req.add("description", desc);
        req.add("category", category);

        DataResponse res = HttpRequestUtil.request("/api/website/saveWebsite", req);
    }

    private void doDelete(Integer id) {
        DataRequest req = new DataRequest();
        req.add("id", id);
        HttpRequestUtil.request("/api/website/deleteWebsite", req);
    }
}
