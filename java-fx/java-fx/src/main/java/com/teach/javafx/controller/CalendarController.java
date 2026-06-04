package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarController {

    @FXML
    private VBox calendarContainer;

    // 固定渲染 2026年 3~9月（覆盖你第一张校历的所有月份）
    private static final int START_MONTH = 3;
    private static final int END_MONTH = 9;
    private static final int CURRENT_YEAR = 2026;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // ===================== 新增：所有特殊日期配置（和第一张校历完全一致） =====================
    private static final Map<String, SpecialDate> SPECIAL_DATES = new HashMap<>();

    static {
        // 节假日（黄色圆形 #ffc107）
        SPECIAL_DATES.put("2026-03-03", new SpecialDate("元宵节", "#ffc107", true));
        SPECIAL_DATES.put("2026-04-05", new SpecialDate("清明节", "#ffc107", true));
        SPECIAL_DATES.put("2026-05-01", new SpecialDate("劳动节", "#ffc107", true));
        SPECIAL_DATES.put("2026-05-04", new SpecialDate("青年节", "#ffc107", true));
        SPECIAL_DATES.put("2026-06-19", new SpecialDate("端午节", "#ffc107", true));

        // 学校事件（绿色圆形 #28a745）
        SPECIAL_DATES.put("2026-03-01", new SpecialDate("学生注册", "#28a745", true));
        SPECIAL_DATES.put("2026-03-02", new SpecialDate("学生上课", "#28a745", true));
        SPECIAL_DATES.put("2026-07-13", new SpecialDate("暑期学校开始", "#28a745", true));
        SPECIAL_DATES.put("2026-08-09", new SpecialDate("暑期学校结束", "#28a745", true));
        SPECIAL_DATES.put("2026-08-28", new SpecialDate("教工上班", "#28a745", true));
        SPECIAL_DATES.put("2026-08-30", new SpecialDate("学生注册", "#28a745", true));
        SPECIAL_DATES.put("2026-08-31", new SpecialDate("学生上课", "#28a745", true));

        // 体育大课堂（黄色背景 #fff3cd，非圆形）
        SPECIAL_DATES.put("2026-04-24", new SpecialDate("体育大课堂", "#fff3cd", false));
        SPECIAL_DATES.put("2026-04-25", new SpecialDate("体育大课堂", "#fff3cd", false));
    }

    // 特殊日期内部类
    private static class SpecialDate {
        String title;
        String color;
        boolean isCircle; // 是否显示为圆形标记

        SpecialDate(String title, String color, boolean isCircle) {
            this.title = title;
            this.color = color;
            this.isCircle = isCircle;
        }
    }

    @FXML
    public void initialize() {
        System.out.println("CalendarController初始化...");
        // 循环加载 3-9月 日历
        for (int month = START_MONTH; month <= END_MONTH; month++) {
            YearMonth monthObj = YearMonth.of(CURRENT_YEAR, month);
            loadSingleMonthCalendar(monthObj);
        }
    }

    private void loadSingleMonthCalendar(YearMonth month) {
        DataRequest req = new DataRequest();
        req.add("year", month.getYear());
        req.add("month", month.getMonthValue());

        DataResponse res = HttpRequestUtil.request("/api/calendar/getMonthEvents", req);
        if (res != null && res.getCode() == 0) {
            System.out.println(month + " 校历数据请求成功：" + res.getData());
            try {
                List<Map<String, Object>> events = (List<Map<String, Object>>) res.getData();
                drawSingleMonth(month, events);
            } catch (ClassCastException e) {
                System.err.println(month + " 数据格式错误：" + e.getMessage());
            }
        } else {
            System.err.println(month + " 数据请求失败：" + (res != null ? res.getMsg() : "服务器无响应"));
            // 即使后端请求失败，也渲染空日历（包含我们的特殊日期）
            drawSingleMonth(month, null);
        }
    }

    private void drawSingleMonth(YearMonth currentMonth, List<Map<String, Object>> events) {
        // 1. 创建当前月的外层容器
        VBox monthBox = new VBox(10);
        monthBox.setStyle("-fx-alignment:center; -fx-padding:10 0;");

        // 2. 月份标题（文字颜色改为深灰色）
        Label monthTitle = new Label(currentMonth.getYear() + "年" + currentMonth.getMonthValue() + "月");
        monthTitle.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#333333; -fx-padding:5;");
        monthBox.getChildren().add(monthTitle);

        // 3. 创建日历Grid
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(2);
        calendarGrid.setVgap(2);
        calendarGrid.setStyle("-fx-padding:10;");

        // 4. 绘制星期标题（文字颜色改为深灰色）
        String[] weekNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 0; i < 7; i++) {
            Label l = new Label(weekNames[i]);
            l.setStyle("-fx-font-weight:bold; -fx-padding:5; -fx-text-fill:#333333;");
            calendarGrid.add(l, i, 0);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startWeek = firstDay.getDayOfWeek().getValue();
        int days = currentMonth.lengthOfMonth();
        int row = 1;

        for (int day = 1; day <= days; day++) {
            int col = (startWeek + day - 2) % 7;
            if (col == 0 && day > 1) row++;

            VBox box = new VBox(2);
            box.setStyle("-fx-padding:4; -fx-border-color:#cccccc; -fx-min-width:80; -fx-min-height:60;");
            box.setAlignment(Pos.TOP_CENTER);

            LocalDate currentDate = currentMonth.atDay(day);
            String currentDateStr = currentDate.format(dateFormatter);

            // ===================== 新增：特殊日期标记逻辑 =====================
            Label dayLabel;
            if (SPECIAL_DATES.containsKey(currentDateStr)) {
                SpecialDate sd = SPECIAL_DATES.get(currentDateStr);
                if (sd.isCircle) {
                    // 圆形标记（和第一张校历完全一样）
                    StackPane circlePane = new StackPane();
                    circlePane.setStyle("-fx-background-color:" + sd.color + "; -fx-background-radius:50%; -fx-min-width:30; -fx-min-height:30;");
                    dayLabel = new Label(String.valueOf(day));
                    dayLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:white;");
                    circlePane.getChildren().add(dayLabel);
                    box.getChildren().add(circlePane);

                    // 添加事件名称
                    Label eventLabel = new Label(sd.title);
                    eventLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#333333;");
                    box.getChildren().add(eventLabel);
                } else {
                    // 背景标记（体育大课堂）
                    box.setStyle("-fx-padding:4; -fx-border-color:#cccccc; -fx-min-width:80; -fx-min-height:60; -fx-background-color:" + sd.color + ";");
                    dayLabel = new Label(String.valueOf(day));
                    dayLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#856404;");
                    box.getChildren().add(dayLabel);

                    // 添加事件名称
                    Label eventLabel = new Label(sd.title);
                    eventLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#856404;");
                    box.getChildren().add(eventLabel);
                }
            } else {
                // 普通日期
                dayLabel = new Label(String.valueOf(day));
                dayLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#333333;");
                box.getChildren().add(dayLabel);
            }

            // ===================== 保留原有的后端事件加载逻辑 =====================
            if (events != null) {
                for (Map<String, Object> e : events) {
                    String eventDateStr = (String) e.get("date");
                    if (currentDateStr.equals(eventDateStr)) {
                        Label eventLabel = new Label((String) e.get("title"));
                        eventLabel.setStyle("-fx-font-size:10px;");
                        eventLabel.setTextFill(Color.web((String) e.get("color")));
                        box.getChildren().add(eventLabel);
                    }
                }
            }

            calendarGrid.add(box, col, row);
        }

        // 5. 添加到主界面
        monthBox.getChildren().add(calendarGrid);
        calendarContainer.getChildren().add(monthBox);

        System.out.println(currentMonth + " 日历渲染完成");
    }
}