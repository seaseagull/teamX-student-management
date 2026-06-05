package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuoteCountdownController {

    @FXML private Label currentDateLabel;
    @FXML private Label daysLeftLabel;
    @FXML private ProgressBar semesterProgressBar;
    @FXML private Label progressPercentLabel;
    @FXML private Label quoteTextLabel;
    @FXML private Label quoteSourceLabel;

    @FXML private TableView<Map> todoTableView;
    @FXML private TableColumn<Map, String> todoContentColumn;
    @FXML private TableColumn<Map, String> todoTimeColumn;
    @FXML private TextField todoInputField;
    @FXML private DatePicker todoDatePicker;

    private final LocalDate TERM_START = LocalDate.of(2026, 2, 23);
    private final LocalDate TERM_END = LocalDate.of(2026, 7, 12);
    private final ObservableList<Map> todoList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        currentDateLabel.setText("今天是 " + today.format(formatter));

        calculateCountdownAndProgress(today);
        loadDailyQuote();

        todoContentColumn.setCellValueFactory(new MapValueFactory<>("content"));
        todoTimeColumn.setCellValueFactory(new MapValueFactory<>("time"));
        todoTableView.setItems(todoList);

        // 【关键修复】启动时自动从数据库加载数据
        queryTodoList();
    }

    // --- 核心：从后端获取数据 ---
    private void queryTodoList() {
        DataRequest req = new DataRequest();
        req.add("personId", AppStore.getJwt().getId()); // 加这行
        Integer personId = AppStore.getJwt().getId();
        System.out.println("【DEBUG】当前用户 personId = " + personId);
        DataResponse res = HttpRequestUtil.request("/api/todo/getTodoList", req);

        if (res != null && res.getCode() == 0) {
            List<Map> dataList = (List<Map>) res.getData();
            todoList.clear();
            if (dataList != null) {
                todoList.addAll(dataList);
            }
        }
    }

    @FXML
    protected void onAddTodoClick() {
        // 管理员不能添加待办
        String role = AppStore.getJwt().getRole();
        if ("ROLE_ADMIN".equals(role)) {
            MessageDialog.showDialog("管理员账号不支持此功能！");
            return;
        }
        System.out.println("【DEBUG】开始执行保存逻辑"); // 调试用
        String content = todoInputField.getText().trim();
        LocalDate date = todoDatePicker.getValue();

        if (content.isEmpty()) {
            MessageDialog.showDialog("待办内容不能为空哦！");
            return;
        }

        String dateStr = (date != null) ? date.toString() : "无期限";

        // 【关键修复】封装数据发送给后端，不再直接 todoList.add
        DataRequest req = new DataRequest();
        req.add("personId", AppStore.getJwt().getId()); // 加这行
        req.add("content", content);
        req.add("time", dateStr);

        DataResponse res = HttpRequestUtil.request("/api/todo/todoSave", req);

        if (res != null && res.getCode() == 0) {
            todoInputField.clear();
            todoDatePicker.setValue(null);
            queryTodoList(); // 成功后刷新列表
        } else {
            MessageDialog.showDialog("保存失败，请检查后端服务是否启动");
        }
    }

    @FXML
    protected void onDeleteTodoClick() {
        Map selectedItem = todoTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            MessageDialog.showDialog("请先选中一条记录！");
            return;
        }

        // 【关键修复】发送 ID 给后端进行物理删除
        Integer todoId = CommonMethod.getInteger(selectedItem, "id");
        DataRequest req = new DataRequest();
        req.add("id", todoId);

        DataResponse res = HttpRequestUtil.request("/api/todo/todoDelete", req);

        if (res != null && res.getCode() == 0) {
            queryTodoList(); // 删除成功后重新加载
        } else {
            MessageDialog.showDialog("删除失败");
        }
    }

    // --- 原有逻辑保持不变 ---
    private void calculateCountdownAndProgress(LocalDate today) {
        // ... (保持原样即可)
        long daysLeft = ChronoUnit.DAYS.between(today, TERM_END);
        daysLeftLabel.setText(daysLeft < 0 ? "0" : String.valueOf(daysLeft));
        long totalDays = ChronoUnit.DAYS.between(TERM_START, TERM_END);
        long daysPassed = ChronoUnit.DAYS.between(TERM_START, today);
        double progress = 0.0;
        if (daysPassed > 0 && totalDays > 0) progress = (double) daysPassed / totalDays;
        if (progress > 1.0) progress = 1.0;
        if (progress < 0.0) progress = 0.0;
        semesterProgressBar.setProgress(progress);
        progressPercentLabel.setText(String.format("%.1f%%", progress * 100));
    }

    @FXML protected void onRefreshQuoteClick() { loadDailyQuote(); }

    private void loadDailyQuote() {
        new Thread(() -> {
            try {
                String urlStr = "https://v1.hitokoto.cn/?c=d&c=e&c=h&c=i";
                String result = httpGet(urlStr);
                String hitokoto = parseJsonField(result, "hitokoto");
                String fromSrc = parseJsonField(result, "from");
                String fromWho = parseJsonField(result, "from_who");
                String authorInfo = (fromWho == null || fromWho.isEmpty()) ? "《" + fromSrc + "》" : fromWho + " · " + "《" + fromSrc + "》";
                Platform.runLater(() -> {
                    quoteTextLabel.setText("“ " + hitokoto + " ”");
                    quoteSourceLabel.setText("—— " + authorInfo);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    quoteTextLabel.setText("今天也要元气满满哦！");
                    quoteSourceLabel.setText("—— 系统温馨提示");
                });
            }
        }).start();
    }

    private String parseJsonField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\":\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1);
        return "";
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}