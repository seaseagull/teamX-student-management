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

import java.util.List;
import java.util.Map;

public class NoticeController {

    @FXML private ListView<Map> noticeListView;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label timeLabel;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button submitButton;

    private ObservableList<Map> noticeList = FXCollections.observableArrayList();
    private Integer currentEditId = null;
    private String roleName;

    @FXML
    public void initialize() {
        roleName = AppStore.getJwt().getRole();

        // 非管理员隐藏添加、编辑、删除按钮
        boolean isAdmin = "ROLE_ADMIN".equals(roleName);
        addButton.setVisible(isAdmin);
        editButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);
        submitButton.setVisible(isAdmin);

        // 设置列表点击事件
        noticeListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                onNoticeSelected(selected);
                editButton.setDisable(!isAdmin);
                deleteButton.setDisable(!isAdmin);
            } else {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        loadData();
    }

    // 加载通知列表
    private void loadData() {
        DataResponse response = HttpRequestUtil.request("/api/notice/getList", new DataRequest());
        if (response != null && response.getCode() == 0) {
            List<Map> list = (List<Map>) response.getData();
            noticeList.clear();
            noticeList.addAll(list);
            noticeListView.setItems(noticeList);
            // 自定义显示方式：显示标题和发布时间
            noticeListView.setCellFactory(lv -> new ListCell<Map>() {
                @Override
                protected void updateItem(Map item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String title = (String) item.get("title");
                        String time = (String) item.get("createTime");
                        setText(title + "  (" + time + ")");
                    }
                }
            });
        }
    }

    // 点击通知时显示详情
    private void onNoticeSelected(Map notice) {
        currentEditId = (Integer) notice.get("id");
        titleField.setText((String) notice.get("title"));
        contentArea.setText((String) notice.get("content"));
        String time = (String) notice.get("createTime");
        timeLabel.setText("发布时间：" + (time != null ? time : ""));
    }

    // 新增通知
    @FXML
    private void onAddClick() {
        clearForm();
        currentEditId = null;
        titleField.requestFocus();
    }

    // 编辑通知
    @FXML
    private void onEditClick() {
        if (currentEditId == null) {
            MessageDialog.showDialog("请先选择要编辑的通知");
        }
    }

    // 删除通知
    @FXML
    private void onDeleteClick() {
        if (currentEditId == null) {
            MessageDialog.showDialog("请先选择要删除的通知");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除该通知吗？");
        if (ret != MessageDialog.CHOICE_YES) return;

        DataRequest request = new DataRequest();
        request.add("id", currentEditId);
        DataResponse response = HttpRequestUtil.request("/api/notice/delete", request);
        if (response != null && response.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            clearForm();
            loadData();
        } else {
            MessageDialog.showDialog(response != null ? response.getMsg() : "删除失败");
        }
    }

    // 保存通知
    @FXML
    private void onSubmitClick() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty()) {
            MessageDialog.showDialog("请输入公告标题");
            return;
        }
        if (content.isEmpty()) {
            MessageDialog.showDialog("请输入公告内容");
            return;
        }

        DataRequest request = new DataRequest();
        request.add("id", currentEditId);
        request.add("title", title);
        request.add("content", content);

        DataResponse response = HttpRequestUtil.request("/api/notice/save", request);
        if (response != null && response.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
            clearForm();
            loadData();
        } else {
            MessageDialog.showDialog(response != null ? response.getMsg() : "保存失败");
        }
    }

    // 刷新列表
    @FXML
    private void onRefreshClick() {
        loadData();
        clearForm();
    }

    // 取消编辑
    @FXML
    private void onCancelClick() {
        clearForm();
    }

    // 清空表单
    private void clearForm() {
        currentEditId = null;
        titleField.clear();
        contentArea.clear();
        timeLabel.setText("");
        noticeListView.getSelectionModel().clearSelection();
    }
}