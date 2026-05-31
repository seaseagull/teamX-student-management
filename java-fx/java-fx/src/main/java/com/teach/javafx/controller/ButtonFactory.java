package com.teach.javafx.controller;

import javafx.scene.control.Button;

public class ButtonFactory {

    // 浅蓝保存按钮
    public static Button createSaveButton(String text) {
        Button btn = new Button(text);
        String normal =
                "-fx-background-color: linear-gradient(#E8F4FD, #D0E8FB);" +
                        "-fx-text-fill: #2B7BD6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 5 14;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #B8D4F0;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;";
        String hover =
                "-fx-background-color: linear-gradient(#D0E8FB, #B8D8F5);" +
                        "-fx-text-fill: #1E5EA8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 5 14;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #9AC0E8;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    // 浅粉删除按钮
    public static Button createDeleteButton(String text) {
        Button btn = new Button(text);
        String normal =
                "-fx-background-color: linear-gradient(#FDE8E8, #FBD0D0);" +
                        "-fx-text-fill: #D64545;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 5 14;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #F0B8B8;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;";
        String hover =
                "-fx-background-color: linear-gradient(#FBD0D0, #F5B0B0);" +
                        "-fx-text-fill: #B83535;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 5 14;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #E89898;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    // 浅灰取消按钮
    public static Button createCancelButton(String text) {
        Button btn = new Button(text);
        String normal =
                "-fx-background-color: linear-gradient(#F5F5F5, #E8E8E8);" +
                        "-fx-text-fill: #777;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 5 14;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #D8D8D8;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;";
        String hover =
                "-fx-background-color: linear-gradient(#E8E8E8, #D8D8D8);" +
                        "-fx-text-fill: #555;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 5 14;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #C0C0C0;" +
                        "-fx-border-radius: 4;" +
                        "-fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }
}