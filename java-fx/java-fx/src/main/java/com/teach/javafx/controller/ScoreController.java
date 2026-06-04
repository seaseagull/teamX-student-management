package com.teach.javafx.controller; // 注意：如果你的包路径不一样，请保留你原来的这行

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;

import java.util.List;
import java.util.Map;

public class ScoreController {

    @FXML
    private Button deleteButton;
    @FXML
    private Button importButton;

    @FXML
    private TextField numTextField; // 对应 FXML 里的学号框
    @FXML
    private TableView<Map> dataTableView; // 必须和 FXML 里的 fx:id 一致


    // ------ 核心修改：请在这里补上新列的声明 ------
    @FXML
    private TableColumn<Map, Object> person_idColumn;  // 开课学期
    @FXML
    private TableColumn<Map, Object> semesterColumn;  // 开课学期
    @FXML
    private TableColumn<Map, Object> nameColumn;  // 课程名
    @FXML
    private TableColumn<Map, Object> course_idColumn;  // 课程名称
    @FXML
    private TableColumn<Map, Object> markColumn;      // 百分制成绩
    @FXML
    private TableColumn<Map, Object> gpaColumn;      // 绩点
    @FXML
    private TableColumn<Map, Object> countColumn;   // 学分
    // --------------------------------------------

    @FXML
    private TextField rightStudentNumField;
    @FXML
    private TextField rightSemesterField;
    @FXML
    private TextField rightCourseNumField;
    @FXML
    private TextField rightCourseNameField;
    @FXML
    private TextField rightMarkField;
    @FXML
    private TextField rightGpaField;
    @FXML
    private TextField rightCreditField;

    @FXML
    private Button saveButton;

    // 这个方法在界面加载时会自动运行
    @FXML
    public void initialize() {

        System.out.println("成绩查询界面已初始化");
        // 1. 获取当前登录用户的角色
        String role = AppStore.getJwt().getRole();

        // 2. 权限控制：如果不是管理员，就隐藏掉那些“危险”按钮
        // 请确保 addButton(26.5.25注：我已经去掉这个按钮）, deleteButton, importButton 这三个变量你已经声明在类顶部了
        if (!"ROLE_ADMIN".equals(role)) {

            deleteButton.setVisible(false);
            deleteButton.setManaged(false);

            importButton.setVisible(false);
            importButton.setManaged(false);
        }
        // 原来只锁了部分字段，改成根据角色决定是否全锁

        if (!"ROLE_ADMIN".equals(role)) {
            // 学生端：右边所有字段全部变灰
            rightStudentNumField.setDisable(true);
            rightSemesterField.setDisable(true);
            rightCourseNumField.setDisable(true);
            rightCourseNameField.setDisable(true);
            rightMarkField.setDisable(true);   // ← 这个之前没锁
            rightGpaField.setDisable(true);
            rightCreditField.setDisable(true);
            saveButton.setVisible(false);
            saveButton.setManaged(false);
        } else {
            // 管理员端：只锁不该手动填的字段，mark 可以改
            rightStudentNumField.setDisable(true);
            rightSemesterField.setDisable(true);
            rightCourseNumField.setDisable(true);
            rightCourseNameField.setDisable(true);
            rightGpaField.setDisable(true);
            rightCreditField.setDisable(true);
        }



        // ------ 核心修改：建立前端列与数据库字段的绑定 ------
        // 括号里的字符串（如 "semester"）必须与你在 MySQL 里加的列名大小写完全一致！
        person_idColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        semesterColumn.setCellValueFactory(new MapValueFactory<>("semester"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        course_idColumn.setCellValueFactory(new MapValueFactory<>("courseNum"));
        markColumn.setCellValueFactory(new MapValueFactory<>("mark"));
        gpaColumn.setCellValueFactory(new MapValueFactory<>("gpa"));
        countColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        // -------------------------------------------------
        if ("ROLE_ADMIN".equals(role)) {
            onQueryButtonClick();//如果是管理员才自动加载一次数据
        }

        // 监听左侧表格点击，点击某一行，右边自动填满
        dataTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rightStudentNumField.setText(CommonMethod.getString(newValue, "studentNum"));
                rightSemesterField.setText(CommonMethod.getString(newValue, "semester"));
                rightCourseNumField.setText(CommonMethod.getString(newValue, "courseNum"));
                rightCourseNameField.setText(CommonMethod.getString(newValue, "courseName"));

                // 拿到当前最新的分数
                String currentMark = CommonMethod.getString(newValue, "mark");
                rightMarkField.setText(currentMark);

                // 📐 右侧的绩点框也通过分数现场算出来，绝对不穿帮！
                try {
                    int mark = Integer.parseInt(currentMark);
                    double calculatedGpa = mark >= 60 ? (mark - 50) / 10.0 : 0.0;
                    rightGpaField.setText(String.format("%.1f", calculatedGpa));
                } catch (Exception e) {
                    rightGpaField.setText("0.0");
                }

                rightCreditField.setText(CommonMethod.getString(newValue, "credit"));
            }
        });


        // 🔥 完美的动态绩点计算逻辑（已修复泛型报错）
        gpaColumn.setCellValueFactory(cellData -> {
            Map rowData = cellData.getValue();
            if (rowData != null) {
                Object markObj = rowData.get("mark");
                if (markObj != null) {
                    try {
                        int mark = Integer.parseInt(markObj.toString());
                        // 按照高校公式现场计算绩点
                        double calculatedGpa = mark >= 60 ? (mark - 50) / 10.0 : 0.0;

                        // ✨ 核心修复：改用 SimpleObjectProperty<Object>
                        return new javafx.beans.property.SimpleObjectProperty<Object>(String.format("%.1f", calculatedGpa));
                    } catch (Exception e) {
                        return new javafx.beans.property.SimpleObjectProperty<Object>("0.0");
                    }
                }
            }
            return new javafx.beans.property.SimpleObjectProperty<Object>("");
        });

    }

    @FXML
    protected void onQueryButtonClick() {
        String role = AppStore.getJwt().getRole();
        String num;

        if ("ROLE_ADMIN".equals(role)) {
            // 管理员：用输入框里的值查询
            num = numTextField.getText().trim();
        } else {
            // 学生端：输入框为空则清空表格不查询
            String inputNum = numTextField.getText().trim();
            if (inputNum.isEmpty()) {
                dataTableView.setItems(FXCollections.observableArrayList());
                return;
            }
            // 只允许查自己的学号
            String selfNum = AppStore.getJwt().getUsername();
            if (!inputNum.equals(selfNum)) {
                MessageDialog.showDialog("只能查询自己的成绩！");
                numTextField.setText("");
                return;
            }
            num = selfNum;
        }

        DataRequest req = new DataRequest();
        req.add("num", num);
        req.add("name", "");
        DataResponse res = HttpRequestUtil.request("/api/score/getScoreList", req);
        if (res != null && res.getCode() == 0) {
            List<Map> dataList = (List<Map>) res.getData();
            dataTableView.setItems(FXCollections.observableArrayList(dataList));
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        // 1. 获取表格中当前选中的那一行数据
        Map selectedItem = dataTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            MessageDialog.showDialog("请先在表格中选中一条要删除的成绩记录！");
            return;
        }

        // 2. 弹窗让老师确认，防止手抖误删（使用项目自带的确认弹窗）
        int ret = MessageDialog.choiceDialog("确认要删除该条成绩记录吗？");
        if (ret != MessageDialog.CHOICE_YES) {
            return; // 如果老师点了取消，就直接结束，什么都不做
        }

        // 3. 从选中行里提取后端需要的真实主键 scoreId
        Integer scoreId = CommonMethod.getInteger(selectedItem, "scoreId");

        // 4. 组装请求，调用后端的删除接口
        DataRequest req = new DataRequest();
        req.add("scoreId", scoreId);
        DataResponse res = HttpRequestUtil.request("/api/score/scoreDelete", req);

        // 5. 根据后端返回的结果进行提示
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            onQueryButtonClick(); // 🎉 极其关键：删除成功后自动调用查询，刷新表格！
        } else {
            MessageDialog.showDialog("删除失败：" + (res != null ? res.getMsg() : "未知错误"));
        }
    }

    @FXML
    protected void onImportButtonClick() {
        // 当用户点击导入按钮时，直接弹出提示框，不让程序没有反应
        MessageDialog.showDialog("功能暂未开通");
    }

    @FXML
    protected void onSaveButtonClick() {
        // 1. 获取选中的行
        Map selectedItem = dataTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            MessageDialog.showDialog("请先在左侧表格中选择要修改的成绩记录！");
            return;
        }
        Integer scoreId = CommonMethod.getInteger(selectedItem, "scoreId");

        // 2. 获取修改后的百分制成绩
        String markStr = rightMarkField.getText().trim();
        if (markStr.isEmpty()) {
            MessageDialog.showDialog("百分制成绩不能为空！");
            return;
        }

        // 3. 组装请求对象
        DataRequest req = new DataRequest();
        req.add("scoreId", scoreId);
        try {
            req.add("mark", Integer.parseInt(markStr));
        } catch (NumberFormatException e) {
            MessageDialog.showDialog("请输入正确的百分制成绩（必须为整数）！");
            return;
        }

        // 4. 发送给后端保存
        DataResponse res = HttpRequestUtil.request("/api/score/scoreSave", req);

        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("成绩修改成功！");
            onQueryButtonClick(); // 刷新表格

            // 5. 成功后重置表格选中并清空 7 个输入框
            dataTableView.getSelectionModel().clearSelection();
            rightStudentNumField.clear();
            rightSemesterField.clear();
            rightCourseNumField.clear();
            rightCourseNameField.clear();
            rightMarkField.clear();
            rightGpaField.clear();
            rightCreditField.clear();
        } else {
            MessageDialog.showDialog("修改失败：" + (res != null ? res.getMsg() : "未知错误"));
        }
    }

}
