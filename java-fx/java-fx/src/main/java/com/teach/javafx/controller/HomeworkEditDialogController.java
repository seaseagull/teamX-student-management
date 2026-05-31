package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class HomeworkEditDialogController extends ToolController {
    @FXML private TextField homeworkNameField;
    @FXML private ComboBox<OptionItem> courseComboBox;
    @FXML private TextField deadlineField;
    @FXML private TextArea remarkField;
    @FXML private ComboBox<OptionItem> stateNameComboBox;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;

    private Stage stage;
    private Integer homeworkId;
    private boolean saved = false;
    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    public void initialize() {
        // 加载课程下拉框
        DataRequest req = new DataRequest();
        List<OptionItem> courseList = HttpRequestUtil.requestOptionItemList(
                "/api/homework/getCourseItemOptionList", req);
        courseComboBox.getItems().addAll(courseList);

        // 状态下拉框
        stateNameComboBox.getItems().addAll(
                new OptionItem(0, "0", "未完成"),
                new OptionItem(1, "1", "已完成")
        );
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // 新增模式
    public void setAddMode() {
        homeworkId = null;
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
    }

    // 编辑模式
    public void setEditMode(Map<String, Object> data) {
        // homeworkId 转 Integer
        Object idObj = data.get("homeworkId");
        homeworkId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;

        homeworkNameField.setText((String) data.get("homeworkName"));
        deadlineField.setText((String) data.get("deadline"));
        remarkField.setText((String) data.get("remark"));

        // courseId 转 String
        Object courseIdObj = data.get("courseId");
        String courseId = courseIdObj instanceof Double ?
                String.valueOf(((Double) courseIdObj).intValue()) : String.valueOf(courseIdObj);

        for (OptionItem item : courseComboBox.getItems()) {
            if (item.getValue().equals(courseId)) {
                courseComboBox.getSelectionModel().select(item);
                break;
            }
        }

        // state 转 int
        Object stateObj = data.get("state");
        int state;
        if (stateObj instanceof String) {
            state = Integer.parseInt((String) stateObj);
        } else if (stateObj instanceof Double) {
            state = ((Double) stateObj).intValue();
        } else {
            state = (Integer) stateObj;
        }
        stateNameComboBox.getSelectionModel().select(state == 1 ? 1 : 0);

        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
    }

    @FXML
    private void onSaveClick() {
        DataRequest req = new DataRequest();
        req.add("homeworkId", homeworkId);
        req.add("homeworkName", homeworkNameField.getText());
        req.add("deadline", deadlineField.getText());
        req.add("remark", remarkField.getText());

        OptionItem courseOp = courseComboBox.getSelectionModel().getSelectedItem();
        if (courseOp != null) {
            req.add("courseId", Integer.parseInt(courseOp.getValue()));
        }

        OptionItem stateOp = stateNameComboBox.getSelectionModel().getSelectedItem();
        if (stateOp != null) {
            req.add("state", Integer.parseInt(stateOp.getValue()));
        }

        DataResponse res = HttpRequestUtil.request("/api/homework/homeworkSave", req);
        if (res != null && res.getCode() == 0) {
            saved = true;
            MessageDialog.showDialog("保存成功！");
            if (onSaved != null) onSaved.run();
            stage.close();
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "保存失败");
        }
    }

    @FXML
    private void onDeleteClick() {
        DataRequest req = new DataRequest();
        req.add("homeworkId", homeworkId);
        DataResponse res = HttpRequestUtil.request("/api/homework/delete", req);
        if (res != null && res.getCode() == 0) {
            saved = true;
            MessageDialog.showDialog("删除成功！");
            if (onSaved != null) onSaved.run();
            stage.close();
        }
    }
}