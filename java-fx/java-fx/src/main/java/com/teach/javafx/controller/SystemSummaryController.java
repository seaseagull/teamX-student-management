package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.JwtResponse;
import com.teach.javafx.util.CommonMethod;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统首页仪表盘控制器。
 * <p>首页承担系统概览、数据摘要和快捷导航职责。</p>
 */
public class SystemSummaryController {
    private void debugLog(String hypothesisId, String location, String message, Map<String, Object> data) {
        try (FileWriter fw = new FileWriter("debug-d90a1c.log", true)) {
            fw.write("{\"sessionId\":\"d90a1c\",\"id\":\"sys_" + System.nanoTime() + "\",\"timestamp\":" + Instant.now().toEpochMilli() + ",\"runId\":\"run1\",\"hypothesisId\":\"" + hypothesisId + "\",\"location\":\"" + location + "\",\"message\":\"" + message.replace("\"", "'") + "\",\"data\":\"" + String.valueOf(data).replace("\"", "'") + "\"}\n");
        } catch (IOException ignored) {}
    }
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label daysLeftLabel;
    @FXML private Label progressPercentLabel;
    @FXML private Label quoteTextLabel;
    @FXML private Label quoteSourceLabel;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar semesterProgressBar;

    @FXML private TableView<Map> noticeTableView;
    @FXML private TableColumn<Map, String> noticeTitleColumn;
    @FXML private TableColumn<Map, String> noticeTimeColumn;

    @FXML private TableView<Map> leaveTableView;
    @FXML private TableColumn<Map, String> leaveNameColumn;
    @FXML private TableColumn<Map, String> leaveDateColumn;
    @FXML private TableColumn<Map, String> leaveStateColumn;

    @FXML private TableView<Map> todoTableView;
    @FXML private TableColumn<Map, String> todoContentColumn;
    @FXML private TableColumn<Map, String> todoTimeColumn;

    @FXML private PieChart featurePieChart;
    @FXML private BarChart<String, Number> usageBarChart;

    private String roleName = "GUEST";
    private final LocalDate TERM_START = LocalDate.of(2026, 2, 23);
    private final LocalDate TERM_END = LocalDate.of(2026, 7, 12);

    @FXML
    public void initialize() {
        JwtResponse jwt = AppStore.getJwt();
        roleName = jwt == null || jwt.getRole() == null ? "GUEST" : jwt.getRole();
        String userName = jwt == null ? "访客" : firstNonBlank(jwt.getPerName(), jwt.getUsername(), "访客");

        debugLog("H1", "SystemSummaryController.initialize:entry", "initialize entered", Map.of(
                "roleName", roleName,
                "userName", userName,
                "jwtNull", jwt == null,
                "titleLabelNull", titleLabel == null,
                "subtitleLabelNull", subtitleLabel == null,
                "featurePieChartNull", featurePieChart == null,
                "usageBarChartNull", usageBarChart == null));
        try {
            if (userLabel != null) {
                userLabel.setText(userName);
            }
            if (roleLabel != null) {
                roleLabel.setText(displayRole(roleName));
            }
            if (statusLabel != null) {
                statusLabel.setText("系统运行正常");
            }
            configureHeader();
            configureSemesterSummary();
            configureQuote();
            configureTables();
            configureCharts();
            loadNotices();
            loadLeaves();
            loadTodos();
            debugLog("H1", "SystemSummaryController.initialize:success", "initialize completed", Map.of(
                    "noticeCount", noticeTableView == null ? -1 : noticeTableView.getItems().size(),
                    "leaveCount", leaveTableView == null ? -1 : leaveTableView.getItems().size(),
                    "todoCount", todoTableView == null ? -1 : todoTableView.getItems().size()));
        } catch (Exception ex) {
            debugLog("H1", "SystemSummaryController.initialize:error", "initialize failed", Map.of(
                    "errorType", ex.getClass().getName(),
                    "message", ex.getMessage()));
            System.out.println("SystemSummaryController initialize failed: " + ex.getClass().getName() + " - " + ex.getMessage());
            throw ex;
        }
    }

    private void configureHeader() {
        debugLog("H1", "SystemSummaryController.configureHeader", "configuring header", Map.of("roleName", roleName));
        if ("ROLE_STUDENT".equals(roleName)) {
            titleLabel.setText("学生首页");
            subtitleLabel.setText("查看学习进度、生活服务和常用功能入口");
        } else if ("ROLE_TEACHER".equals(roleName)) {
            titleLabel.setText("教师首页");
            subtitleLabel.setText("查看课程教学、作业成绩与请假审批数据");
        } else if ("ROLE_ADMIN".equals(roleName)) {
            titleLabel.setText("管理首页");
            subtitleLabel.setText("总览学生管理、教学事务和校园服务运行情况");
        } else {
            titleLabel.setText("系统首页");
            subtitleLabel.setText("通过数据概览和快捷入口快速访问常用功能");
        }
    }

    private void configureSemesterSummary() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        currentDateLabel.setText("今天是 " + today.format(formatter));
        long totalDays = Math.max(1, ChronoUnit.DAYS.between(TERM_START, TERM_END));
        long passedDays = Math.max(0, ChronoUnit.DAYS.between(TERM_START, today));
        double progress = Math.min(1.0, Math.max(0.0, (double) passedDays / totalDays));
        semesterProgressBar.setProgress(progress);
        progressPercentLabel.setText(String.format("%.1f%%", progress * 100));
        long daysLeft = ChronoUnit.DAYS.between(today, TERM_END);
        daysLeftLabel.setText(String.valueOf(Math.max(0, daysLeft)));
        debugLog("H1", "SystemSummaryController.configureSemesterSummary", "semester summary configured", Map.of(
                "today", today.toString(),
                "progress", progress,
                "daysLeft", Math.max(0, daysLeft)));
    }

    private void configureQuote() {
        new Thread(() -> {
            try {
                String result = httpGet("https://v1.hitokoto.cn/?c=d&c=e&c=h&c=i");
                String hitokoto = parseJsonField(result, "hitokoto");
                String fromSrc = parseJsonField(result, "from");
                String fromWho = parseJsonField(result, "from_who");
                String authorInfo = (fromWho == null || fromWho.isEmpty()) ? "《" + fromSrc + "》" : fromWho + " · 《" + fromSrc + "》";
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
        }, "home-quote-loader").start();
    }

    private void configureTables() {
        debugLog("H1", "SystemSummaryController.configureTables:entry", "configuring tables", Map.of(
                "noticeTableViewNull", noticeTableView == null,
                "leaveTableViewNull", leaveTableView == null,
                "todoTableViewNull", todoTableView == null));
        noticeTitleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        noticeTimeColumn.setCellValueFactory(new MapValueFactory<>("createTime"));
        leaveNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        leaveDateColumn.setCellValueFactory(new MapValueFactory<>("leaveDate"));
        leaveStateColumn.setCellValueFactory(new MapValueFactory<>("stateName"));
        todoContentColumn.setCellValueFactory(new MapValueFactory<>("content"));
        todoTimeColumn.setCellValueFactory(new MapValueFactory<>("time"));

        if (todoTableView != null) {
            todoTableView.setFixedCellSize(28);
        }
        if (noticeTableView != null) {
            noticeTableView.setFixedCellSize(28);
        }
        if (leaveTableView != null) {
            leaveTableView.setFixedCellSize(28);
        }
        debugLog("H1", "SystemSummaryController.configureTables:success", "tables configured", Map.of("ok", true));
    }

    private void configureCharts() {
        debugLog("H1", "SystemSummaryController.configureCharts:entry", "configuring charts", Map.of(
                "featurePieChartNull", featurePieChart == null,
                "usageBarChartNull", usageBarChart == null));
        featurePieChart.setLegendVisible(true);
        featurePieChart.setLabelsVisible(true);
        usageBarChart.setLegendVisible(false);

        if ("ROLE_STUDENT".equals(roleName)) {
            featurePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("课程作业", 38),
                    new PieChart.Data("成绩考试", 20),
                    new PieChart.Data("请假事务", 18),
                    new PieChart.Data("生活服务", 24)
            ));
            setBarData("学生热点", new String[]{"课程", "作业", "成绩", "请假", "服务"}, new int[]{8, 7, 6, 5, 7});
        } else if ("ROLE_TEACHER".equals(roleName)) {
            featurePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("课程教学", 34),
                    new PieChart.Data("作业成绩", 28),
                    new PieChart.Data("请假审批", 22),
                    new PieChart.Data("校园服务", 16)
            ));
            setBarData("教师热点", new String[]{"课程", "作业", "成绩", "审批", "考试"}, new int[]{8, 9, 6, 7, 5});
        } else {
            featurePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("学生管理", 30),
                    new PieChart.Data("教学管理", 28),
                    new PieChart.Data("事务审批", 22),
                    new PieChart.Data("系统服务", 20)
            ));
            setBarData("管理热点", new String[]{"学生", "教师", "课程", "审批", "服务"}, new int[]{9, 6, 8, 5, 7});
        }
    }

    private void setBarData(String seriesName, String[] categories, int[] values) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        for (int i = 0; i < categories.length; i++) {
            series.getData().add(new XYChart.Data<>(categories[i], values[i]));
        }
        usageBarChart.getData().setAll(series);
    }

    private void loadNotices() {
        try {
            DataResponse response = HttpRequestUtil.request("/api/notice/getList", new DataRequest());
            if (response != null && response.getCode() == 0) {
                List<Map> list = asMapList(response.getData());
                List<Map> limited = list.stream().limit(5).toList();
                noticeTableView.getItems().setAll(limited);
            }
        } catch (Exception ignored) {
            noticeTableView.getItems().clear();
        }
    }

    private void loadLeaves() {
        try {
            DataRequest req = new DataRequest();
            req.add("search", "");
            DataResponse response = HttpRequestUtil.request("/api/studentLeave/getStudentLeaveList", req);
            if (response != null && response.getCode() == 0) {
                List<Map> list = asMapList(response.getData());
                List<Map> limited = list.stream().limit(5).toList();
                leaveTableView.getItems().setAll(limited);
            }
        } catch (Exception ignored) {
            leaveTableView.getItems().clear();
        }
    }

    private void loadTodos() {
        try {
            JwtResponse jwt = AppStore.getJwt();
            if (jwt == null) {
                return;
            }
            DataRequest req = new DataRequest();
            req.add("personId", jwt.getId());
            DataResponse res = HttpRequestUtil.request("/api/todo/getTodoList", req);
            if (res != null && res.getCode() == 0) {
                List<Map> list = asMapList(res.getData());
                List<Map> limited = list.stream().limit(6).toList();
                todoTableView.getItems().setAll(limited);
            }
        } catch (Exception ignored) {
            todoTableView.getItems().clear();
        }
    }

    private List<Map> asMapList(Object data) {
        if (!(data instanceof List<?> rawList)) {
            return List.of();
        }
        List<Map> result = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map map) {
                result.add(map);
            }
        }
        return result;
    }

    @FXML private void onWorkBenchClick() { navigate("system_summary_panel", "工作台"); }
    @FXML private void onStudentManageClick() { navigate("student-panel", "学生管理"); }
    @FXML private void onCourseManageClick() { navigate("course-panel", "课程管理"); }
    @FXML private void onLeaveClick() { navigate("ROLE_STUDENT".equals(roleName) ? "student-leave-panel" : "leave-request-panel", "请假管理"); }
    @FXML private void onCalendarClick() { navigate("calendar-view", "校历查询"); }
    @FXML private void onWebsiteClick() { navigate("website-panel", "校园服务"); }
    @FXML private void onScoreClick() { navigate("score-panel", "成绩查询"); }
    @FXML private void onVolunteerClick() { navigate("ROLE_STUDENT".equals(roleName) ? "student-volunteer-panel" : "volunteer-activity-panel", "志愿活动"); }
    @FXML private void onWeatherClick() { navigate("weather-panel", "天气查询"); }
    @FXML private void onBusClick() { navigate("bus-schedule-panel", "校车时刻"); }
    @FXML private void onMapClick() { navigate("sdu-map-panel", "校园地图"); }
    @FXML private void onNoticeClick() { navigate("notice-panel", "通知公告"); }
    @FXML private void onHomeworkClick() { navigate("homework-panel", "作业管理"); }
    @FXML private void onExamClick() { navigate("exam_panel", "考试安排"); }
    @FXML private void onRefreshTodoClick() { loadTodos(); }

    private void navigate(String pageName, String title) {
        MainFrameController controller = AppStore.getMainFrameController();
        if (controller != null) {
            controller.changeContent(pageName, title);
        }
    }

    private String displayRole(String role) {
        return switch (role) {
            case "ROLE_STUDENT" -> "学生";
            case "ROLE_TEACHER" -> "教师";
            case "ROLE_ADMIN" -> "管理员";
            default -> "访客";
        };
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "访客";
    }

    private String parseJsonField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\":\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1);
        return "";
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = URI.create(urlStr).toURL();
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
