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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/teach/javafx/homework-edit-dialog.fxml"));
            Parent root = loader.load();

            HomeworkEditDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(data == null ? "添加作业" : "编辑作业");
            stage.setScene(new Scene(root));
            controller.setStage(stage);

            if (data == null) {
                controller.setAddMode();
            } else {
                controller.setEditMode(data);
            }
            controller.setOnSaved(() -> onQueryButtonClick());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        showEditDialog(null);
    }
}