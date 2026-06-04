package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class BusScheduleController {

    @FXML
    private ComboBox<String> fromCampusComboBox;
    @FXML
    private ComboBox<String> toCampusComboBox;
    @FXML
    private ToggleGroup scheduleTypeGroup;
    @FXML
    private RadioButton weekdayRadio;
    @FXML
    private RadioButton weekendRadio;
    @FXML
    private ListView<String> resultListView;

    private List<String> campusList = Arrays.asList("中心校区", "软件园校区", "趵突泉校区", "洪家楼校区", "兴隆山校区", "千佛山校区");

    @FXML
    public void initialize() {
        fromCampusComboBox.setItems(FXCollections.observableArrayList(campusList));
        toCampusComboBox.setItems(FXCollections.observableArrayList(campusList));

        fromCampusComboBox.getSelectionModel().select(0);
        toCampusComboBox.getSelectionModel().select(1);
    }

    @FXML
    public void onQueryButtonClick() {
        String fromCampus = fromCampusComboBox.getSelectionModel().getSelectedItem();
        String toCampus = toCampusComboBox.getSelectionModel().getSelectedItem();

        if (fromCampus == null || toCampus == null) {
            MessageDialog.showDialog("请完整选择出发校区和到达校区");
            return;
        }

        if (fromCampus.equals(toCampus)) {
            MessageDialog.showDialog("出发校区和到达校区不能相同");
            return;
        }

        String scheduleType = weekdayRadio.isSelected() ? "weekday" : "weekend";

        DataRequest request = new DataRequest();
        request.add("fromCampus", fromCampus);
        request.add("toCampus", toCampus);
        request.add("scheduleType", scheduleType);

        DataResponse response = HttpRequestUtil.request("/api/bus/getSchedule", request);
        if (response != null && response.getCode() == 0) {
            Map<String, Object> data = (Map<String, Object>) response.getData();
            List<String> timeList = (List<String>) data.get("timeList");
            if (timeList != null && !timeList.isEmpty()) {
                resultListView.setItems(FXCollections.observableArrayList(timeList));
            } else {
                resultListView.setItems(FXCollections.observableArrayList());
                MessageDialog.showDialog("暂无该线路的校车信息");
            }
        } else {
            MessageDialog.showDialog(response != null ? response.getMsg() : "查询失败，请检查网络连接");
        }
    }

    @FXML
    public void onResetButtonClick() {
        fromCampusComboBox.getSelectionModel().select(0);
        toCampusComboBox.getSelectionModel().select(1);
        weekdayRadio.setSelected(true);
        resultListView.setItems(FXCollections.observableArrayList());
    }
}