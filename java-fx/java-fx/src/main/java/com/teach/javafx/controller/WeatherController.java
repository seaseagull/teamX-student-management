package com.teach.javafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherController {

    // 💡 把你在 WeatherAPI.com 申请到的 Key 贴到这里
    private static final String API_KEY = "4911d70ced444cffb61141300260206";

    @FXML private TextField cityField;
    @FXML private Label cityLabel;
    @FXML private Label textLabel;
    @FXML private Label tempLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label humidityLabel;
    @FXML private Label windDirLabel;
    @FXML private Label windSpeedLabel;
    @FXML private Label updateTimeLabel;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    protected void onQueryButtonClick() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            errorLabel.setText("请输入城市名称！");
            return;
        }
        errorLabel.setText("查询中...");

        // 放到新线程里，避免卡 UI 界面
        new Thread(() -> {
            try {
                // ✨ 核心简化：直接传城市中文名，带上 lang=zh 即可一步到位拿到所有数据
                String urlStr = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY
                        + "&q=" + URLEncoder.encode(city, "UTF-8") + "&lang=zh";

                String result = httpGet(urlStr);
                JSONObject json = new JSONObject(result);

                // 捕获 API 自带的错误提示（比如输入了火星城市名）
                if (json.has("error")) {
                    String errorMsg = json.getJSONObject("error").getString("message");
                    Platform.runLater(() -> errorLabel.setText("查询失败: " + errorMsg));
                    return;
                }

                // 解析 WeatherAPI 的扁平 JSON 结构
                JSONObject location = json.getJSONObject("location");
                JSONObject current = json.getJSONObject("current");
                JSONObject condition = current.getJSONObject("condition");

                // 提取数据（转成 String 防止类型转换异常）
                String locationName = location.getString("name");
                String text = condition.getString("text");
                String temp = current.get("temp_c").toString();
                String feelsLike = current.get("feelslike_c").toString();
                String humidity = current.get("humidity").toString();
                String windDir = current.getString("wind_dir");
                String windSpeed = current.get("wind_kph").toString();
                String lastUpdated = current.getString("last_updated");

                // 回到 UI 线程刷新 JavaFX 界面
                Platform.runLater(() -> {
                    errorLabel.setText("");
                    cityLabel.setText(locationName); // 显示匹配到的城市
                    textLabel.setText(text);         // 阴晴状况
                    tempLabel.setText(temp + " °C");
                    feelsLikeLabel.setText(feelsLike + " °C");
                    humidityLabel.setText(humidity + " %");
                    windDirLabel.setText(windDir);    // 风向
                    windSpeedLabel.setText(windSpeed + " km/h");
                    updateTimeLabel.setText(lastUpdated); // 更新时间
                });

            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("请求失败：" + e.getMessage()));
            }
        }).start();
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        // 顺手补个 User-Agent，这是全栈开发的常规好习惯
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        // 兼容处理：发生错误时从 ErrorStream 读取返回的 JSON 错误信息
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode == 200) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}