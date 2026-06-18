package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentController 登录交互控制类 对应 student_panel.fxml  对应于学生管理的后台业务处理的控制器，主要获取数据和保存数据的方法不同
 *
 * @FXML 属性 对应fxml文件中的
 * @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class HomeworkController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;  //作业信息表
    @FXML
    private TableColumn<Map, String> homeworkNameColumn; //作业信息表 名称列
    @FXML
    private TableColumn<Map, String> courseNameColumn; //作业信息表 名称列
    @FXML
    private TableColumn<Map, String> deadlineColumn;  //作业信息表 截止日期列
    @FXML
    private TableColumn<Map, String> remarkColumn; //作业信息表 备注列
    @FXML
    private TableColumn<Map, String> stateNameColumn; //作业信息表 状态列

    @FXML
    private Label searchLabel;
    @FXML
    private TextField searchTextField;
    @FXML
    private Label stateFilterLabel;
    @FXML
    private ComboBox<OptionItem> stateFilterComboBox;
    @FXML
    private Label courseFilterLabel;
    @FXML
    private ComboBox<OptionItem> courseFilterComboBox;

    @FXML
    private Button addButton;

    private Integer homeworkId = null;  //当前编辑修改的学生的主键

    private ArrayList<Map> homeworkList = new ArrayList();  // 学生信息列表数据
    private List<OptionItem> courseList;
    private List<OptionItem> stateNameList;
    private List<OptionItem> stateFilterList;
    private List<OptionItem> courseFilterList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表


    private HBox createTimePicker(String defaultHour, String defaultMinute) {
        ComboBox<String> hourBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) hourBox.getItems().add(String.format("%02d", i));
        hourBox.setValue(defaultHour != null ? defaultHour : "23");
        hourBox.setPrefWidth(70);

        ComboBox<String> minuteBox = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) minuteBox.getItems().add(String.format("%02d", i));
        minuteBox.setValue(defaultMinute != null ? defaultMinute : "59");
        minuteBox.setPrefWidth(70);

        return new HBox(5, hourBox, new Label(":"), minuteBox);
    }

    private String getTimeValue(HBox timePicker) {
        ComboBox<String> hourBox = (ComboBox<String>) timePicker.getChildren().get(0);
        ComboBox<String> minuteBox = (ComboBox<String>) timePicker.getChildren().get(2);
        return hourBox.getValue() + ":" + minuteBox.getValue();
    }

    /**
     * 将学生数据集合设置到面板上显示
     */
    private void setTableViewData() {
        dataTableView.getSelectionModel().clearSelection();
        observableList.clear();
        for (Map map : homeworkList) {
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * 页面加载对象创建完成初始化方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        courseList = HttpRequestUtil.requestOptionItemList("/api/homework/getCourseItemOptionList", req);


        //作业列表
        homeworkNameColumn.setCellValueFactory(new MapValueFactory<>("homeworkName"));
        courseNameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        deadlineColumn.setCellValueFactory(new MapValueFactory<>("deadline"));
        remarkColumn.setCellValueFactory(new MapValueFactory<>("remark"));
        stateNameColumn.setCellValueFactory(new MapValueFactory<>("stateName"));
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        setTableViewData();

        //作业列表筛选
        stateFilterList = new ArrayList<>();
        stateFilterList.add(new OptionItem(-1, "-1", "全部"));
        stateFilterList.add(new OptionItem(0, "0", "未完成"));
        stateFilterList.add(new OptionItem(1, "1", "已完成"));
        stateFilterComboBox.getItems().addAll(stateFilterList);
        stateFilterComboBox.getSelectionModel().selectFirst();

        courseFilterList = new ArrayList<>();
        courseFilterList.add(new OptionItem(-1, "-1", "全部课程"));
        courseFilterList.addAll(courseList);
        courseFilterComboBox.getItems().addAll(courseFilterList);
        courseFilterComboBox.getSelectionModel().selectFirst();

        //作业表单


        stateFilterLabel.setVisible(true);
        searchLabel.setVisible(true);
        stateFilterComboBox.setVisible(true);
        searchTextField.setVisible(true);
        addButton.setVisible(true);


        onQueryButtonClick();
    }




    protected void changeHomeworkInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            return;
        }
        showEditDialog(form);
    }

    private void showEditDialog(Map<String, Object> data) {
        showHomeworkDialog(data);
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeHomeworkInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        String search = searchTextField.getText();
        DataRequest req = new DataRequest();

        //状态筛选
        OptionItem stateOp;
        stateOp = stateFilterComboBox.getSelectionModel().getSelectedItem();
        if (stateOp != null) {
            req.add("state", Integer.parseInt(stateOp.getValue()));
        }

        //课程筛选
        OptionItem courseOp;
        courseOp = courseFilterComboBox.getSelectionModel().getSelectedItem();
        if(courseOp != null) {
            req.add("courseId", Integer.parseInt(courseOp.getValue()));
        }
        req.add("search", search);
        DataResponse res = HttpRequestUtil.request("/api/homework/getHomeworkList", req);
        if (res != null && res.getCode() == 0) {
            homeworkList = (ArrayList<Map>) res.getData();
            if (homeworkList == null) homeworkList = new ArrayList<>();
            setTableViewData();
        }

    }


    @FXML
    protected void onAddButtonClick() {
        showHomeworkDialog(null);
    }

    private void showHomeworkDialog(Map<String, Object> data) {
        Stage stage = new Stage();
        stage.setTitle(data == null ? "添加作业" : "编辑作业");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // 作业名称
        TextField nameField = new TextField(data != null ? (String) data.get("homeworkName") : "");
        nameField.setPrefWidth(300);
        nameField.getStyleClass().add("login-input");

        // 课程
        ComboBox<OptionItem> courseBox = new ComboBox<>();
        courseBox.getItems().addAll(courseList);
        courseBox.setPrefWidth(300);
        if (data != null) {
            Object cidObj = data.get("courseId");
            String cid = cidObj instanceof Double ? String.valueOf(((Double) cidObj).intValue()) : String.valueOf(cidObj);
            for (OptionItem item : courseList) {
                if (item.getValue().equals(cid)) { courseBox.setValue(item); break; }
            }
        }

        // 截止日期
        DatePicker datePicker = new DatePicker();
        HBox timePicker = createTimePicker("23", "59");
        HBox deadlineBox = new HBox(10, datePicker, timePicker);
        if (data != null) {
            String deadline = (String) data.get("deadline");
            if (deadline != null && deadline.contains(" ")) {
                String[] parts = deadline.split(" ");
                datePicker.setValue(LocalDate.parse(parts[0]));
                timePicker = createTimePicker(parts[1].substring(0, 2), parts[1].substring(3, 5));
                deadlineBox.getChildren().set(1, timePicker);
            }
        }
        final HBox finalTimePicker = timePicker;

        // 备注
        TextArea remarkField = new TextArea(data != null ? (String) data.get("remark") : "");
        remarkField.setPrefHeight(60);
        remarkField.setWrapText(true);
        remarkField.getStyleClass().add("login-input");

        // 完成状态
        ComboBox<OptionItem> stateBox = new ComboBox<>();
        stateBox.getItems().addAll(new OptionItem(0, "0", "未完成"), new OptionItem(1, "1", "已完成"));
        stateBox.setValue(stateBox.getItems().get(0));
        if (data != null) {
            Object sObj = data.get("state");
            int s = sObj instanceof String ? Integer.parseInt((String) sObj) : sObj instanceof Double ? ((Double) sObj).intValue() : (Integer) sObj;
            stateBox.setValue(stateBox.getItems().get(s == 1 ? 1 : 0));
        }

        grid.addRow(0, new Label("作业名称:"), nameField);
        grid.addRow(1, new Label("课程:"), courseBox);
        grid.addRow(2, new Label("截止日期:"), deadlineBox);
        grid.addRow(3, new Label("备注:"), remarkField);
        grid.addRow(4, new Label("完成状态:"), stateBox);

        Button saveBtn = new Button("保存");

        Button deleteBtn = new Button("删除");
        if (data == null) { deleteBtn.setVisible(false); deleteBtn.setManaged(false); }

        Button cancelBtn = new Button("取消");

        saveBtn.getStyleClass().add("primary-button");
        deleteBtn.getStyleClass().add("danger-button");
        cancelBtn.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10, deleteBtn, new Pane(), cancelBtn, saveBtn);
        HBox.setHgrow(buttons.getChildren().get(1), Priority.ALWAYS);

        VBox pane = new VBox(15, grid, buttons);
        VBox root = new VBox(pane);
        pane.getStyleClass().add("login-panel");
        pane.setPadding(new Insets(20, 20, 30, 20));

        // 保存
        Integer homeworkId = null;
        if (data != null) {
            Object idObj = data.get("homeworkId");
            homeworkId = idObj instanceof Double ? ((Double) idObj).intValue() : (Integer) idObj;
        }
        final Integer finalHomeworkId = homeworkId;

        saveBtn.setOnAction(e -> {
            DataRequest req = new DataRequest();
            req.add("homeworkId", finalHomeworkId);
            req.add("homeworkName", nameField.getText());
            req.add("courseId", Integer.parseInt(courseBox.getValue().getValue()));
            req.add("deadline", (datePicker.getValue() != null ? datePicker.getValue().toString() : "") + " " + getTimeValue(finalTimePicker));
            req.add("remark", remarkField.getText());
            req.add("state", Integer.parseInt(stateBox.getValue().getValue()));

            DataResponse res = HttpRequestUtil.request("/api/homework/homeworkSave", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("保存成功！");
                stage.close();
                onQueryButtonClick();
            } else {
                MessageDialog.showDialog(res != null ? res.getMsg() : "保存失败");
            }
        });

        // 删除
        deleteBtn.setOnAction(e -> {
            DataRequest req = new DataRequest();
            req.add("homeworkId", finalHomeworkId);
            DataResponse res = HttpRequestUtil.request("/api/homework/delete", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                stage.close();
                onQueryButtonClick();
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 800, 420);
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/component.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/login-modern.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/teach/javafx/css/page-modern.css").toExternalForm());
        stage.setScene(scene);
        root.setStyle("-fx-background-color: rgba(255,255,255,0.82); -fx-background-radius: 30; -fx-background-image: url('" + getClass().getResource("/com/teach/javafx/picture/login-bg.png").toExternalForm() + "'); -fx-background-size: cover;");
        stage.showAndWait();
    }
}