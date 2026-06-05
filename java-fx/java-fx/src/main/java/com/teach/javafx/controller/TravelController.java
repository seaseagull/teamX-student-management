package com.teach.javafx.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TravelController {
    @FXML private Label driveLabel;    // 驾车出行
    @FXML private Label tourLabel;     // 运动建议
    @FXML private Label trafficLabel;  // 穿衣+路况
    @FXML private StackPane rootStackPane; // 绑定页面最外层面板

    private static final String API_KEY = "4911d70ced444cffb61141300260206";
    private static final String CITY = "Jinan";
    private static final String SCHOOL_NAME = "山东大学软件园校区";

    // 两套渐变背景：雨天蓝、晴天暖黄
    private final String RAIN_BG = "-fx-background-color: linear-gradient(to bottom right, #e0f7fa, #b2ebf2, #81d4fa);-fx-padding:35;";
    private final String SUN_BG = "-fx-background-color: linear-gradient(to bottom right, #fff9e0, #ffe8b9, #ffd38c);-fx-padding:35;";

    @FXML
    public void initialize() {
        new Thread(this::loadAllAdvice).start();
    }

    private void loadAllAdvice() {
        String url = String.format(
                "https://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=1&aqi=no&alerts=no",
                API_KEY, CITY
        );

        String result;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            result = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                driveLabel.setText("🚙 驾车出行：网络请求失败");
                tourLabel.setText("🏃 运动建议：网络请求失败");
                trafficLabel.setText("👕 穿衣建议：网络请求失败");
            });
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(result).getAsJsonObject();
            if (root.has("error")) {
                String errMsg = root.getAsJsonObject("error").get("message").getAsString();
                Platform.runLater(() -> {
                    driveLabel.setText("🚙 错误：" + errMsg);
                    tourLabel.setText("🏃 错误：" + errMsg);
                    trafficLabel.setText("👕 错误：" + errMsg);
                });
                return;
            }

            JsonObject dayData = root.getAsJsonObject("forecast")
                    .getAsJsonArray("forecastday")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("day");

            int avgTemp = dayData.get("avgtemp_c").getAsInt();
            int willRain = dayData.get("daily_will_it_rain").getAsInt();
            int uvIndex = dayData.get("uv").getAsInt();

            String driveTip = willRain == 1 ? "降雨天气，路面湿滑谨慎驾车" : "天气晴好，适宜驾车出行";
            String sportTip;
            if(willRain == 1){
                sportTip = "有降雨，建议室内锻炼";
            }else if(uvIndex >6){
                sportTip = "紫外线偏高，避开正午户外运动";
            }else{
                sportTip = "气候舒适，适合户外运动";
            }
            String dressTip;
            if(avgTemp>25) dressTip = "短袖夏装";
            else if(avgTemp>15) dressTip = "薄长袖+外套";
            else dressTip = "厚防寒外套";
            String roadTip = willRain ==1 ? "雨天减速慢行" : "路况良好正常通行";

            // 动态切换背景：下雨蓝色、晴天暖黄色
            String finalDriveTip = driveTip;
            String finalSportTip = sportTip;
            String finalDressTip = dressTip;
            String finalRoadTip = roadTip;
            int finalWillRain = willRain;
            Platform.runLater(() -> {
                driveLabel.setText("🚙 驾车出行：" + finalDriveTip);
                tourLabel.setText("🏃 运动建议：" + finalSportTip);
                trafficLabel.setText("👕 穿衣建议：平均气温"+avgTemp+"℃，推荐"+finalDressTip+" | 🚦 "+finalRoadTip);
                // 修改面板背景
                if(finalWillRain ==1){
                    rootStackPane.setStyle(RAIN_BG);
                }else{
                    rootStackPane.setStyle(SUN_BG);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                driveLabel.setText("🚙 驾车出行：数据解析失败");
                tourLabel.setText("🏃 运动建议：数据解析失败");
                trafficLabel.setText("👕 穿衣建议：数据解析失败");
            });
        }
    }
}