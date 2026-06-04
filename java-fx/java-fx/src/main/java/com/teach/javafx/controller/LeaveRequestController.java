package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LeaveRequestController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> startHourCombo;
    @FXML private ComboBox<String> startMinuteCombo;
    @FXML private ComboBox<String> endHourCombo;
    @FXML private ComboBox<String> endMinuteCombo;
    @FXML private TextArea reasonArea;
    @FXML private TextArea commentArea;
    @FXML private Button submitButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Label roleLabel;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> idColumn;
    @FXML private TableColumn<Map, String> studentNumColumn;
    @FXML private TableColumn<Map, String> studentNameColumn;
    @FXML private TableColumn<Map, String> startTimeColumn;
    @FXML private TableColumn<Map, String> endTimeColumn;
    @FXML private TableColumn<Map, String> reasonColumn;
    @FXML private TableColumn<Map, String> statusColumn;
    @FXML private TableColumn<Map, String> adminCommentColumn;

    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private String roleName;
    private Integer currentEditId = null;
    private Map<String, Object> currentSelected = null;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // 初始化时间下拉框
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        List<String> minutes = Arrays.asList("00", "30");

        startHourCombo.setItems(FXCollections.observableArrayList(hours));
        startMinuteCombo.setItems(FXCollections.observableArrayList(minutes));
        endHourCombo.setItems(FXCollections.observableArrayList(hours));
        endMinuteCombo.setItems(FXCollections.observableArrayList(minutes));

        startHourCombo.getSelectionModel().select(8);
        startMinuteCombo.getSelectionModel().select(0);
        endHourCombo.getSelectionModel().select(17);
        endMinuteCombo.getSelectionModel().select(0);

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        // 初始化表格列
        idColumn.setCellValueFactory(new MapValueFactory<>("id"));
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        startTimeColumn.setCellValueFactory(new MapValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new MapValueFactory<>("endTime"));
        reasonColumn.setCellValueFactory(new MapValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusName"));
        adminCommentColumn.setCellValueFactory(new MapValueFactory<>("adminComment"));

        dataTableView.setItems(observableList);

        // 状态筛选下拉框
        statusFilterCombo.setItems(FXCollections.observableArrayList("全部", "待审批", "已通过", "已拒绝"));
        statusFilterCombo.getSelectionModel().select(0);

        // 获取角色
        roleName = AppStore.getJwt().getRole();

        // 根据角色显示不同的UI
        if ("ROLE_STUDENT".equals(roleName)) {
            roleLabel.setText("学生视角 - 您可以提交离校申请");
            approveButton.setVisible(false);
            rejectButton.setVisible(false);
            commentArea.setEditable(false);
        } else if ("ROLE_TEACHER".equals(roleName) || "ROLE_ADMIN".equals(roleName)) {
            roleLabel.setText("管理视角 - 您可以审批申请");
            submitButton.setVisible(false);
            commentArea.setEditable(true);
            approveButton.setVisible(true);
            rejectButton.setVisible(true);
        }

        // 表格选中事件
        dataTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                onTableRowSelect(selected);
            }
        });

        loadData();
    }

    private void onTableRowSelect(Map<String, Object> row) {
        currentSelected = row;
        currentEditId = (Integer) row.get("id");

        // 填充审批意见区域
        String comment = (String) row.get("adminComment");
        commentArea.setText(comment != null ? comment : "");

        // 如果是待审批状态，管理员/教师可以审批
        Integer status = (Integer) row.get("status");
        if (("ROLE_ADMIN".equals(roleName) || "ROLE_TEACHER".equals(roleName)) && status == 0) {
            approveButton.setDisable(false);
            rejectButton.setDisable(false);
            commentArea.setEditable(true);
        } else {
            approveButton.setDisable(true);
            rejectButton.setDisable(true);
            commentArea.setEditable(false);
        }
    }

    @FXML
    public void onSubmitClick() {
        if (reasonArea.getText().trim().isEmpty()) {
            MessageDialog.showDialog("请填写请假原因");
            return;
        }

        DataRequest request = new DataRequest();
        request.add("id", currentEditId);

        LocalDate startDate = startDatePicker.getValue();
        String startDateTime = startDate + " " + startHourCombo.getValue() + ":" + startMinuteCombo.getValue();
        request.add("startTime", startDateTime);

        LocalDate endDate = endDatePicker.getValue();
        String endDateTime = endDate + " " + endHourCombo.getValue() + ":" + endMinuteCombo.getValue();
        request.add("endTime", endDateTime);

        request.add("reason", reasonArea.getText());

        DataResponse response = HttpRequestUtil.request("/api/leave/save", request);
        if (response != null && response.getCode() == 0) {
            MessageDialog.showDialog("提交成功！");
            onResetClick();
            loadData();
        } else {
            MessageDialog.showDialog(response != null ? response.getMsg() : "提交失败");
        }
    }

    @FXML
    public void onApproveClick() {
        doApprove(1, "通过");
    }

    @FXML
    public void onRejectClick() {
        doApprove(2, "拒绝");
    }

    private void doApprove(int status, String msg) {
        if (currentSelected == null) {
            MessageDialog.showDialog("请先选择一条申请");
            return;
        }

        int ret = MessageDialog.choiceDialog("确认要" + msg + "这条申请吗？");
        if (ret != MessageDialog.CHOICE_YES) return;

        DataRequest request = new DataRequest();
        request.add("id", currentEditId);
        request.add("status", status);
        request.add("comment", commentArea.getText());

        DataResponse response = HttpRequestUtil.request("/api/leave/approve", request);
        if (response != null && response.getCode() == 0) {
            MessageDialog.showDialog("审批成功！");
            loadData();
            commentArea.clear();
        } else {
            MessageDialog.showDialog(response != null ? response.getMsg() : "审批失败");
        }
    }

    @FXML
    public void onResetClick() {
        currentEditId = null;
        currentSelected = null;
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        startHourCombo.getSelectionModel().select(8);
        startMinuteCombo.getSelectionModel().select(0);
        endHourCombo.getSelectionModel().select(17);
        endMinuteCombo.getSelectionModel().select(0);
        reasonArea.clear();
        commentArea.clear();
        approveButton.setDisable(false);
        rejectButton.setDisable(false);
    }

    @FXML
    public void onRefreshClick() {
        loadData();
    }

    @FXML
    public void onFilterChange() {
        loadData();
    }

    private void loadData() {
        DataRequest request = new DataRequest();

        String filter = statusFilterCombo.getSelectionModel().getSelectedItem();
        if ("待审批".equals(filter)) {
            request.add("status", 0);
        } else if ("已通过".equals(filter)) {
            request.add("status", 1);
        } else if ("已拒绝".equals(filter)) {
            request.add("status", 2);
        }

        DataResponse response = HttpRequestUtil.request("/api/leave/getList", request);
        if (response != null && response.getCode() == 0) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) response.getData();
            observableList.clear();
            for (Map<String, Object> item : list) {
                Integer status = (Integer) item.get("status");
                if (status == 0) {
                    item.put("statusName", "⏳ 待审批");
                } else if (status == 1) {
                    item.put("statusName", "✅ 已通过");
                } else {
                    item.put("statusName", "❌ 已拒绝");
                }
                observableList.add(item);
            }
        }
    }
}