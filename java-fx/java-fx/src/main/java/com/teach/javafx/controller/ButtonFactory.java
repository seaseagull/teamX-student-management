package com.teach.javafx.controller;

import javafx.scene.control.Button;

public class ButtonFactory {

    // 主按钮 - 蓝
    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #1677FF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 6 16;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #4096FF;" +
                        "-fx-text-fill: white; -fx-font-size:13px; -fx-padding:6 16; -fx-background-radius:4; -fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #1677FF;" +
                        "-fx-text-fill: white; -fx-font-size:13px; -fx-padding:6 16; -fx-background-radius:4; -fx-cursor:hand;"));
        return btn;
    }

    // 危险按钮 - 红
    public static Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #FF4D4F;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 6 16;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #FF7875;" +
                        "-fx-text-fill: white; -fx-font-size:13px; -fx-padding:6 16; -fx-background-radius:4; -fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #FF4D4F;" +
                        "-fx-text-fill: white; -fx-font-size:13px; -fx-padding:6 16; -fx-background-radius:4; -fx-cursor:hand;"));
        return btn;
    }

    // 默认按钮 - 白底灰边
    public static Button createDefaultButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #333;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 6 16;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #D9D9D9;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #FAFAFA; -fx-text-fill:#1677FF;" +
                        "-fx-font-size:13px; -fx-padding:6 16; -fx-background-radius:4;" +
                        "-fx-border-color: #1677FF; -fx-border-radius:4; -fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: white; -fx-text-fill:#333;" +
                        "-fx-font-size:13px; -fx-padding:6 16; -fx-background-radius:4;" +
                        "-fx-border-color:#D9D9D9; -fx-border-radius:4; -fx-cursor:hand;"));
        return btn;
    }

    // 保存按钮
    public static Button createSaveButton(String text) {
        return createPrimaryButton(text);
    }

    // 删除按钮
    public static Button createDeleteButton(String text) {
        return createDangerButton(text);
    }

    // 取消按钮
    public static Button createCancelButton(String text) {
        return createDefaultButton(text);
    }
}