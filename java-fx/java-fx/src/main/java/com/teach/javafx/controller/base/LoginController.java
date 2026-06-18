package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

/**
 * LoginController 登录交互控制类 对应 base/login-view.fxml
 *  @FXML  属性 对应fxml文件中的 fx:id 属性 如TextField usernameField 对应 fx:id="usernameField"
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性  如onLoginButtonClick() 对应onAction="#onLoginButtonClick"
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private BorderPane loginRoot;
    /**
     * 页面加载对象创建完成初始话方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */
    @FXML
    public void initialize() {
//        vbox.setId("min");  // id选择器 #
//        vbox.getStyleClass().add("min");  类选择器 .
        if (loginRoot != null) {
            loginRoot.getStyleClass().add("login-root");
        }
//        loginButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
    }

    /**
     *  点击登录按钮 执行onLoginButtonClick 方法 从面板上获取用户名和密码，请求后台登录服务，登录成功加载主框架，切换舞台到主框架，登录不成功，提示错误信息
     */
    @FXML
    protected void onAdminLoginButtonClick() {
        onLoginButtonClick("admin","123456");
    }
    @FXML
    protected void onStudentLoginButtonClick() {
        onLoginButtonClick("2022030001","123456");
    }
    @FXML
    protected void onTeacherLoginButtonClick() {
        onLoginButtonClick("T2022002","123456");  // 陈洪波
    }
    protected void onLoginButtonClick(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username,password);
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("debug-d90a1c.log"), "{\"sessionId\":\"d90a1c\",\"id\":\"login_button\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"LoginController.java:onLoginButtonClick\",\"message\":\"login button clicked\",\"data\":{\"username\":\"" + username + "\"}}\n", java.nio.charset.StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException ignored) {}
        String msg = HttpRequestUtil.login(loginRequest);
        if(msg != null) {
            MessageDialog.showDialog( msg);
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/main-frame.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            AppStore.setMainFrameController((MainFrameController) fxmlLoader.getController());
            MainApplication.resetStage("教学管理系统", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}