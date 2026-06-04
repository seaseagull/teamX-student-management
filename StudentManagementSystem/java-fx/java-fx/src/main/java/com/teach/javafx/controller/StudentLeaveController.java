package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.LocalDateStringConverter;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 学生请假管理面板控制器，绑定 {@code student-leave-panel.fxml}。
 * <p>学生：填写/保存/提交请假，列表不展示教师/管理员审核意见列；教师：填写教师审核意见并通过或不通过；管理员：按状态筛选、填写管理员意见并审核。</p>
 * <p>{@code @FXML} 字段与 FXML 中 {@code fx:id} 对应；{@code onAction="#onXxxClick"} 绑定本类中带 {@code @FXML} 的按钮方法。</p>
 */
public class StudentLeaveController extends ToolController {

    /** 请假记录主表，行数据为后端返回的 {@code Map}（键与列字段一致）。 */
    @FXML
    private TableView<Map> dataTableView;
    /** 列表列：学号 */
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    /** 列表列：学生姓名 */
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    /** 列表列：指导老师（工号+姓名等展示串） */
    @FXML
    private TableColumn<Map, String> teacherNameColumn;
    /** 列表列：请假日期 */
    @FXML
    private TableColumn<Map, String> leaveDateColumn;
    /** 列表列：返校日期 */
    @FXML
    private TableColumn<Map, String> returnDateColumn;
    /** 列表列：请假理由 */
    @FXML
    private TableColumn<Map, String> reasonColumn;
    /** 列表列：审核状态（字典 SHZTM 文案） */
    @FXML
    private TableColumn<Map, String> stateNameColumn;
    /** 列表列：教师审核意见 */
    @FXML
    private TableColumn<Map, String> teacherCommentColumn;
    /** 列表列：管理员审核意见 */
    @FXML
    private TableColumn<Map, String> adminCommentColumn;

    /** 表单行标签：学号（学生端文案为「自动带出」） */
    @FXML
    private Label studentNumLabel;
    /** 表单行标签：姓名 */
    @FXML
    private Label studentNameLabel;
    /** 表单：学号（学生端只读展示，由 {@link #applyStudentIdentityFromJwt()} 填充，不禁用以避免灰显像空白） */
    @FXML
    private TextField studentNumField;
    /** 表单：姓名（学生端只读展示） */
    @FXML
    private TextField studentNameField;
    /** 表单：请假日期（日历选择，提交后端为 {@code yyyy-MM-dd} 字符串） */
    @FXML
    private DatePicker leaveDatePicker;
    /** 表单：返校日期（日历选择，提交后端为 {@code yyyy-MM-dd} 字符串） */
    @FXML
    private DatePicker returnDatePicker;
    /** 表单：请假理由 */
    @FXML
    private TextField reasonField;
    /** 表单行标签「老师意见」（学生端由 {@link #applyStudentSubmitFormLayout()} 隐藏） */
    @FXML
    private Label teacherCommentLabel;
    /** 表单行标签「管理员意见」 */
    @FXML
    private Label adminCommentLabel;
    /** 表单：教师审核意见（教师角色可编辑） */
    @FXML
    private TextField teacherCommentField;
    /** 表单：管理员审核意见（管理员角色可编辑） */
    @FXML
    private TextField adminCommentField;
    /** 表单：指导老师下拉，数据来自 {@code /api/studentLeave/getTeacherItemOptionList} */
    @FXML
    private ComboBox<OptionItem> teacherComboBox;
    /** 工具栏：审核状态筛选（字典 SHZTM，管理员可见） */
    @FXML
    private ComboBox<OptionItem> stateComboBox;

    /** 工具栏：关键字（学号/姓名等，管理员查询） */
    @FXML
    private TextField searchTextField;
    /** 工具栏：关键字标签 */
    @FXML
    private Label searchLabel;
    /** 工具栏：审核状态下拉标签 */
    @FXML
    private Label stateLabel;

    /** 学生：新建请假草稿 */
    @FXML
    private Button addButton;
    /** 学生：保存草稿（状态 0） */
    @FXML
    private Button saveButton;
    /** 学生：提交申请（状态 1） */
    @FXML
    private Button submitButton;
    /** 教师/管理员：审核通过 */
    @FXML
    private Button passButton;
    /** 教师/管理员：审核不通过 */
    @FXML
    private Button notPassButton;

    /** 右侧表单区域容器，用于控制显示/隐藏 */
    @FXML
    private VBox rightVBox;

    /** 当前编辑的请假记录主键，新建时为 {@code null} */
    private Integer studentLeaveId = null;
    /** 指导老师下拉选项列表 */
    private List<OptionItem> teacherList;
    /** 当前查询得到的请假记录列表 */
    private ArrayList<Map> studentLeaveList = new ArrayList<>();
    /** 审核状态下拉选项（字典 SHZTM） */
    private List<OptionItem> stateList;
    /** 绑定到 {@link #dataTableView} 的可观察行集合 */
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    /** 当前登录用户角色，如 {@code ROLE_STUDENT} */
    private String roleName;

    /** 请假日期传参、存库统一格式 */
    private static final DateTimeFormatter LEAVE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 将 {@link #studentLeaveList} 同步到 {@link #observableList} 并刷新 {@link #dataTableView}。
     */
    private void setTableViewData() {
        observableList.clear();
        for (Map map : studentLeaveList) {
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * FXML 加载完成后调用：拉取教师列表、绑定表格列、按角色显示/禁用控件并首次查询列表。
     */
    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        teacherList = HttpRequestUtil.requestOptionItemList("/api/studentLeave/getTeacherItemOptionList", req);
        if (teacherList == null) {
            teacherList = new ArrayList<>();
        }
        teacherComboBox.getItems().addAll(teacherList);
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        teacherNameColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        leaveDateColumn.setCellValueFactory(new MapValueFactory<>("leaveDate"));
        returnDateColumn.setCellValueFactory(new MapValueFactory<>("returnDate"));
        reasonColumn.setCellValueFactory(new MapValueFactory<>("reason"));
        stateNameColumn.setCellValueFactory(new MapValueFactory<>("stateName"));
        teacherCommentColumn.setCellValueFactory(new MapValueFactory<>("teacherComment"));
        adminCommentColumn.setCellValueFactory(new MapValueFactory<>("adminComment"));
        leaveDatePicker.setConverter(new LocalDateStringConverter());
        leaveDatePicker.setShowWeekNumbers(false);
        returnDatePicker.setConverter(new LocalDateStringConverter());
        returnDatePicker.setShowWeekNumbers(false);
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        setTableViewData();
        stateList = HttpRequestUtil.getDictionaryOptionItemList("SHZTM");
        if (stateList == null) {
            stateList = new ArrayList<>();
        }
        stateList.addFirst(new OptionItem(-1, "-1", "请选择..."));
        stateComboBox.getItems().addAll(stateList);
        this.roleName = AppStore.getJwt().getRole();
        switch (this.roleName) {
            case "ROLE_STUDENT" -> {
                stateLabel.setVisible(false);
                searchLabel.setVisible(false);
                stateComboBox.setVisible(false);
                searchTextField.setVisible(false);
                addButton.setVisible(true);
                saveButton.setVisible(true);
                submitButton.setVisible(true);
                passButton.setVisible(false);
                notPassButton.setVisible(false);
                teacherComboBox.setDisable(false);
                adminCommentField.setDisable(true);
                teacherCommentField.setDisable(true);
                applyStudentSubmitFormLayout();
                configureStudentIdentityFieldsForStudentRole();
                applyStudentIdentityFromJwt();
            }
            case "ROLE_TEACHER" -> {
                studentNumLabel.setText("学号");
                studentNameLabel.setText("姓名");
                studentNumField.setDisable(true);
                studentNameField.setDisable(true);
                studentNumField.setEditable(true);
                studentNameField.setEditable(true);
                stateLabel.setVisible(false);
                searchLabel.setVisible(false);
                stateComboBox.setVisible(false);
                searchTextField.setVisible(false);
                addButton.setVisible(false);
                saveButton.setVisible(false);
                submitButton.setVisible(false);
                passButton.setVisible(true);
                notPassButton.setVisible(true);
                teacherComboBox.setDisable(true);
                adminCommentField.setDisable(true);
                teacherCommentField.setDisable(false);
            }
            case "ROLE_ADMIN" -> {
                studentNumLabel.setText("学号");
                studentNameLabel.setText("姓名");
                studentNumField.setDisable(true);
                studentNameField.setDisable(true);
                studentNumField.setEditable(true);
                studentNameField.setEditable(true);
                stateLabel.setVisible(true);
                searchLabel.setVisible(true);
                stateComboBox.setVisible(true);
                searchTextField.setVisible(true);
                addButton.setVisible(false);
                saveButton.setVisible(false);
                submitButton.setVisible(false);
                passButton.setVisible(true);
                notPassButton.setVisible(true);
                teacherComboBox.setDisable(true);
                adminCommentField.setDisable(false);
                teacherCommentField.setDisable(true);
            }
            default -> {
                studentNumField.setDisable(true);
                studentNameField.setDisable(true);
            }
        }
        applyReviewCommentColumnsVisibility();
        configureLeaveDatePickerForRole();
        // 初始化时隐藏右侧表单区域
        rightVBox.setVisible(false);
        rightVBox.setManaged(false);
        onQueryButtonClick();
    }

    /**
     * 教师、管理员仅查看请假日期，不可改；学生可选择日期。
     */
    private void configureLeaveDatePickerForRole() {
        boolean student = "ROLE_STUDENT".equals(this.roleName);
        leaveDatePicker.setDisable(!student);
        returnDatePicker.setDisable(!student);
    }

    /**
     * 解析后端返回的请假日期字符串为 {@link LocalDate}，支持 {@code yyyy-MM-dd} 及带时间的 ISO 串。
     *
     * @param raw 列表或接口中的日期字段，可为空
     * @return 解析成功返回日期，否则 {@code null}
     */
    private LocalDate parseLeaveDateFromBackend(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s, LEAVE_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
        }
        if (s.length() >= 10) {
            try {
                return LocalDate.parse(s.substring(0, 10), LEAVE_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return new LocalDateStringConverter().fromString(s);
        } catch (DateTimeParseException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 解析后端返回的返校日期字符串为 {@link LocalDate}，支持 {@code yyyy-MM-dd} 及带时间的 ISO 串。
     *
     * @param raw 列表或接口中的日期字段，可为空
     * @return 解析成功返回日期，否则 {@code null}
     */
    private LocalDate parseReturnDateFromBackend(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s, LEAVE_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
        }
        if (s.length() >= 10) {
            try {
                return LocalDate.parse(s.substring(0, 10), LEAVE_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return new LocalDateStringConverter().fromString(s);
        } catch (DateTimeParseException | IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 控制列表中「老师审核意见」「管理员审核意见」列的显示：所有角色都可见。
     */
    private void applyReviewCommentColumnsVisibility() {
        teacherCommentColumn.setVisible(true);
        adminCommentColumn.setVisible(true);
    }

    /**
     * 学生端学号、姓名为「只读展示」：不禁用控件（避免部分主题下灰显难以辨认），禁止编辑。
     */
    private void configureStudentIdentityFieldsForStudentRole() {
        studentNumField.setDisable(false);
        studentNameField.setDisable(false);
        studentNumField.setEditable(false);
        studentNameField.setEditable(false);
        studentNumField.setTooltip(new Tooltip("与登录账号一致，无需填写"));
        studentNameField.setTooltip(new Tooltip("与档案姓名一致，无需填写"));
    }

    /**
     * 学生端隐藏表单中的教师/管理员意见区；意见在 {@link #dataTableView} 对应列查看。
     */
    private void applyStudentSubmitFormLayout() {
        boolean hide = "ROLE_STUDENT".equals(roleName);
        teacherCommentLabel.setVisible(!hide);
        teacherCommentLabel.setManaged(!hide);
        teacherCommentField.setVisible(!hide);
        teacherCommentField.setManaged(!hide);
        adminCommentLabel.setVisible(!hide);
        adminCommentLabel.setManaged(!hide);
        adminCommentField.setVisible(!hide);
        adminCommentField.setManaged(!hide);
    }

    /**
     * 学生端将学号、姓名设为当前登录用户的只读展示（{@link AppStore#getJwt()} 的 {@code username}、{@code perName}）。
     */
    private void applyStudentIdentityFromJwt() {
        if (!"ROLE_STUDENT".equals(roleName)) {
            return;
        }
        var jwt = AppStore.getJwt();
        if (jwt == null) {
            return;
        }
        String num = jwt.getUsername() != null ? jwt.getUsername() : "";
        studentNumField.setText(num);
        String name = jwt.getPerName();
        if (name == null || name.isBlank()) {
            name = num;
        }
        studentNameField.setText(name);
    }

    /**
     * 清空右侧表单；学生角色清空后重新填充学号、姓名。
     */
    public void clearPanel() {
        studentLeaveId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        teacherComboBox.getSelectionModel().clearSelection();
        leaveDatePicker.setValue(null);
        returnDatePicker.setValue(null);
        reasonField.setText("");
        teacherCommentField.setText("");
        adminCommentField.setText("");
        if ("ROLE_STUDENT".equals(roleName)) {
            applyStudentIdentityFromJwt();
        }
    }

    /**
     * 表格行选中变化时，将选中行映射到表单；学生端不写入审核意见输入框（以列表列展示为准）。
     */
    protected void changeStudentInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        studentLeaveId = CommonMethod.getInteger(form, "studentLeaveId");
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        teacherComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(teacherList, CommonMethod.getString(form, "teacherId")));
        leaveDatePicker.setValue(parseLeaveDateFromBackend(CommonMethod.getString(form, "leaveDate")));
        returnDatePicker.setValue(parseReturnDateFromBackend(CommonMethod.getString(form, "returnDate")));
        reasonField.setText(CommonMethod.getString(form, "reason"));
        if (!"ROLE_STUDENT".equals(roleName)) {
            teacherCommentField.setText(CommonMethod.getString(form, "teacherComment"));
            adminCommentField.setText(CommonMethod.getString(form, "adminComment"));
        }
    }

    /**
     * 表格选中索引变化监听，委托 {@link #changeStudentInfo()}。
     *
     * @param change 选中行索引变更事件
     */
    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeStudentInfo();
        // 如果选中了记录，显示右侧表单
        if (studentLeaveId != null) {
            rightVBox.setVisible(true);
            rightVBox.setManaged(true);
        }
    }

    /**
     * 按当前筛选条件请求请假列表并刷新表格，随后 {@link #clearPanel()}。
     */
    @FXML
    protected void onQueryButtonClick() {
        String search = searchTextField.getText();
        DataRequest req = new DataRequest();
        OptionItem op = stateComboBox.getSelectionModel().getSelectedItem();
        if (op != null) {
            req.add("state", Integer.parseInt(op.getValue()));
        }
        req.add("search", search);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/getStudentLeaveList", req);
        if (res != null && res.getCode() == 0) {
            studentLeaveList = (ArrayList<Map>) res.getData();
            setTableViewData();
            // 取消表格选中状态，隐藏右侧表单
            dataTableView.getSelectionModel().clearSelection();
            rightVBox.setVisible(false);
            rightVBox.setManaged(false);
        }
        clearPanel();
    }

    /** 学生：清空表单，准备新建请假。 */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        rightVBox.setVisible(true);
        rightVBox.setManaged(true);
    }

    /** 学生：保存请假草稿。 */
    @FXML
    protected void onSaveButtonClick() {
        doSave(0);
    }

    /** 学生：提交请假申请。 */
    @FXML
    protected void onSubmitButtonClick() {
        doSave(1);
    }

    /** 教师/管理员：审核通过。 */
    @FXML
    protected void onPassButtonClick() {
        doCheck(0);  // 0=通过
    }

    /** 教师/管理员：审核不通过。 */
    @FXML
    protected void onNotPassButtonClick() {
        doCheck(1);  // 1=不通过
    }

    /**
     * 调用后端 {@code /api/studentLeave/studentLeaveSave} 保存或提交请假。
     *
     * @param state 业务状态：0 草稿，1 已提交（与后端约定一致）
     */
    protected void doSave(Integer state) {
        if (studentLeaveId != null) {
            Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
            if (form != null) {
                Boolean teacherChecked = CommonMethod.getBoolean(form, "teacherChecked");
                Boolean adminChecked = CommonMethod.getBoolean(form, "adminChecked");
                if (Boolean.TRUE.equals(teacherChecked) || Boolean.TRUE.equals(adminChecked)) {
                    MessageDialog.showDialog("该请假申请已审核，无法修改！");
                    return;
                }
            }
        }
        DataRequest req = new DataRequest();
        OptionItem op = teacherComboBox.getSelectionModel().getSelectedItem();
        if ("ROLE_STUDENT".equals(roleName) && op == null) {
            MessageDialog.showDialog("请选择指导老师！");
            return;
        }
        LocalDate leaveDate = leaveDatePicker.getValue();
        if ("ROLE_STUDENT".equals(roleName) && leaveDate == null) {
            MessageDialog.showDialog("请选择离校日期！");
            return;
        }
        LocalDate returnDate = returnDatePicker.getValue();
        if ("ROLE_STUDENT".equals(roleName) && returnDate == null) {
            MessageDialog.showDialog("请选择返校日期！");
            return;
        }
        if ("ROLE_STUDENT".equals(roleName) && leaveDate != null && returnDate != null && returnDate.isBefore(leaveDate)) {
            MessageDialog.showDialog("返校日期不能早于离校日期！");
            return;
        }
        if (op != null) {
            req.add("teacherId", Integer.parseInt(op.getValue()));
        }
        req.add("studentLeaveId", studentLeaveId);
        req.add("leaveDate", leaveDate != null ? leaveDate.format(LEAVE_DATE_FORMAT) : "");
        req.add("returnDate", returnDate != null ? returnDate.format(LEAVE_DATE_FORMAT) : "");
        req.add("reason", reasonField.getText());
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveSave", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存提交成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "请求失败，请检查网络或服务。");
        }
    }

    /**
     * 调用后端 {@code /api/studentLeave/studentLeaveCheck} 提交审核结果及意见。
     *
     * @param state 审核结果状态码（与 {@code StudentLeaveService#studentLeaveCheck} 约定一致）
     */
    protected void doCheck(Integer state) {
        DataRequest req = new DataRequest();
        req.add("studentLeaveId", studentLeaveId);
        req.add("teacherComment", teacherCommentField.getText());
        req.add("adminComment", adminCommentField.getText());
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveCheck", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("审核成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "请求失败，请检查网络或服务。");
        }
    }
}
