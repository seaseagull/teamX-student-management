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
 * <p>学生：列表不展示审核意见；时间与意见在右侧详情区查看。教师/管理员：审核已提交请假。</p>
 */
public class StudentLeaveController extends ToolController {

    private static final int REASON_MAX_LENGTH = 100;
    /** 字典 SHZTM 中「草稿」项 value，仅学生端筛选使用 */
    private static final String DRAFT_FILTER_VALUE = "4";

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
    private TextArea reasonField;
    /** 理由字数提示 */
    @FXML
    private Label reasonLengthLabel;
    /** 详情：申请时间标签与值 */
    @FXML
    private Label applyTimeLabel;
    @FXML
    private Label applyTimeValueLabel;
    @FXML
    private Label teacherTimeLabel;
    @FXML
    private Label teacherTimeValueLabel;
    @FXML
    private Label adminTimeLabel;
    @FXML
    private Label adminTimeValueLabel;
    /** 表单行标签「老师意见」 */
    @FXML
    private Label teacherCommentLabel;
    /** 表单行标签「管理员意见」 */
    @FXML
    private Label adminCommentLabel;
    /** 表单：教师审核意见 */
    @FXML
    private TextArea teacherCommentField;
    /** 表单：管理员审核意见 */
    @FXML
    private TextArea adminCommentField;
    /** 表单：指导老师姓名（只读展示，列表/详情通用） */
    @FXML
    private TextField teacherNameField;
    /** 表单：指导老师下拉（仅学生新建/未审可编辑时使用） */
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
    /** 学生：删除未审核的请假记录 */
    @FXML
    private Button deleteButton;
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
    /** 查询刷新前待恢复的选中记录主键 */
    private Integer pendingReselectLeaveId = null;

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
        reasonField.textProperty().addListener((obs, oldVal, newVal) -> updateReasonLengthLabel(newVal));
        updateReasonLengthLabel(reasonField.getText());
        leaveDatePicker.setConverter(new LocalDateStringConverter());
        leaveDatePicker.setShowWeekNumbers(false);
        returnDatePicker.setConverter(new LocalDateStringConverter());
        returnDatePicker.setShowWeekNumbers(false);
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        setTableViewData();
        this.roleName = AppStore.getJwt().getRole();
        initStateFilterComboBox();
        switch (this.roleName) {
            case "ROLE_STUDENT" -> {
                stateLabel.setVisible(true);
                stateLabel.setManaged(true);
                stateComboBox.setVisible(true);
                stateComboBox.setManaged(true);
                searchLabel.setVisible(false);
                searchLabel.setManaged(false);
                searchTextField.setVisible(false);
                searchTextField.setManaged(false);
                addButton.setVisible(true);
                deleteButton.setVisible(true);
                saveButton.setVisible(true);
                submitButton.setVisible(true);
                passButton.setVisible(false);
                notPassButton.setVisible(false);
                teacherComboBox.setDisable(false);
                adminCommentField.setDisable(true);
                teacherCommentField.setDisable(true);
                applyTeacherFieldMode(true);
                configureStudentDetailFieldsForStudentRole();
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
                deleteButton.setVisible(false);
                saveButton.setVisible(false);
                submitButton.setVisible(false);
                passButton.setVisible(true);
                notPassButton.setVisible(true);
                teacherComboBox.setDisable(true);
                adminCommentField.setDisable(true);
                teacherCommentField.setDisable(false);
                applyTeacherFieldMode(false);
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
                deleteButton.setVisible(false);
                saveButton.setVisible(false);
                submitButton.setVisible(false);
                passButton.setVisible(true);
                notPassButton.setVisible(true);
                teacherComboBox.setDisable(true);
                adminCommentField.setDisable(false);
                teacherCommentField.setDisable(true);
                applyTeacherFieldMode(false);
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

    private void initStateFilterComboBox() {
        stateList = HttpRequestUtil.getDictionaryOptionItemList("SHZTM");
        if (stateList == null) {
            stateList = new ArrayList<>();
        }
        List<OptionItem> filterItems = new ArrayList<>();
        if ("ROLE_STUDENT".equals(roleName)) {
            filterItems.add(new OptionItem(-1, "-1", "全部"));
            filterItems.addAll(stateList);
        } else if ("ROLE_ADMIN".equals(roleName) || "ROLE_TEACHER".equals(roleName)) {
            filterItems.add(new OptionItem(-1, "-1", "请选择..."));
            for (OptionItem item : stateList) {
                if (!DRAFT_FILTER_VALUE.equals(item.getValue())) {
                    filterItems.add(item);
                }
            }
        } else {
            filterItems.add(new OptionItem(-1, "-1", "请选择..."));
            filterItems.addAll(stateList);
        }
        stateComboBox.getItems().addAll(filterItems);
        if (!filterItems.isEmpty()) {
            stateComboBox.getSelectionModel().select(0);
        }
    }

    private void updateReasonLengthLabel(String text) {
        int len = text != null ? text.length() : 0;
        reasonLengthLabel.setText(len + "/" + REASON_MAX_LENGTH);
    }

    /**
     * 教师、管理员仅查看请假日期，不可改；学生是否可编辑由 {@link #updateActionButtonVisibility(Map)} 控制。
     */
    private void configureLeaveDatePickerForRole() {
        if ("ROLE_STUDENT".equals(this.roleName)) {
            configureStudentFormEditable(true);
            return;
        }
        leaveDatePicker.setDisable(true);
        returnDatePicker.setDisable(true);
    }

    private void configureStudentFormEditable(boolean editable) {
        leaveDatePicker.setDisable(!editable);
        returnDatePicker.setDisable(!editable);
        teacherComboBox.setDisable(!editable);
        reasonField.setEditable(editable);
    }

    /**
     * 指导老师控件切换：可编辑时显示下拉供学生选择，否则用只读文本展示姓名。
     */
    private void applyTeacherFieldMode(boolean selectionMode) {
        teacherComboBox.setVisible(selectionMode);
        teacherComboBox.setManaged(selectionMode);
        teacherNameField.setVisible(!selectionMode);
        teacherNameField.setManaged(!selectionMode);
        if (!selectionMode) {
            teacherNameField.setEditable(false);
            teacherNameField.setDisable(false);
        }
    }

    private void fillTeacherFieldFromForm(Map<String, Object> form) {
        if (form == null) {
            teacherNameField.setText("");
            teacherComboBox.getSelectionModel().clearSelection();
            return;
        }
        teacherNameField.setText(displayOrDash(CommonMethod.getString(form, "teacherName")));
        int teacherIdx = CommonMethod.getOptionItemIndexById(teacherList, CommonMethod.getInteger(form, "teacherId"));
        if (teacherIdx >= 0) {
            teacherComboBox.getSelectionModel().select(teacherIdx);
        } else {
            teacherComboBox.getSelectionModel().clearSelection();
        }
    }

    private void applyTeacherFieldForCurrentRole(Map<String, Object> form) {
        if (!"ROLE_STUDENT".equals(roleName)) {
            applyTeacherFieldMode(false);
            fillTeacherFieldFromForm(form);
            return;
        }
        boolean reviewed = form != null && (
                Boolean.TRUE.equals(CommonMethod.getBoolean(form, "teacherChecked"))
                        || Boolean.TRUE.equals(CommonMethod.getBoolean(form, "adminChecked")));
        boolean selectionMode = studentLeaveId == null || !reviewed;
        applyTeacherFieldMode(selectionMode);
        fillTeacherFieldFromForm(form);
    }

    private void setButtonManaged(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    /**
     * 按当前选中记录与角色，动态显示保存/提交/删除或审核按钮。
     */
    private void updateActionButtonVisibility(Map<String, Object> form) {
        if ("ROLE_STUDENT".equals(roleName)) {
            updateStudentActionButtons(form);
            return;
        }
        if ("ROLE_TEACHER".equals(roleName)) {
            updateTeacherActionButtons(form);
            return;
        }
        if ("ROLE_ADMIN".equals(roleName)) {
            updateAdminActionButtons(form);
        }
    }

    private void updateStudentActionButtons(Map<String, Object> form) {
        if (studentLeaveId == null) {
            setButtonManaged(saveButton, true);
            setButtonManaged(submitButton, true);
            setButtonManaged(deleteButton, false);
            configureStudentFormEditable(true);
            return;
        }
        boolean reviewed = form != null && (
                Boolean.TRUE.equals(CommonMethod.getBoolean(form, "teacherChecked"))
                        || Boolean.TRUE.equals(CommonMethod.getBoolean(form, "adminChecked")));
        if (reviewed) {
            setButtonManaged(saveButton, false);
            setButtonManaged(submitButton, false);
            setButtonManaged(deleteButton, false);
            configureStudentFormEditable(false);
            return;
        }
        boolean submitted = form != null && Integer.valueOf(1).equals(CommonMethod.getInteger(form, "submitState"));
        setButtonManaged(saveButton, !submitted);
        setButtonManaged(submitButton, true);
        setButtonManaged(deleteButton, true);
        configureStudentFormEditable(true);
    }

    private void updateTeacherActionButtons(Map<String, Object> form) {
        boolean canReview = canTeacherReview(form);
        setButtonManaged(passButton, canReview);
        setButtonManaged(notPassButton, canReview);
        teacherCommentField.setDisable(!canReview);
    }

    private void updateAdminActionButtons(Map<String, Object> form) {
        boolean canReview = canAdminReview(form);
        setButtonManaged(passButton, canReview);
        setButtonManaged(notPassButton, canReview);
        adminCommentField.setDisable(!canReview);
    }

    private boolean canTeacherReview(Map<String, Object> form) {
        if (form == null || studentLeaveId == null) {
            return false;
        }
        Integer submitState = CommonMethod.getInteger(form, "submitState");
        if (submitState == null || submitState != 1) {
            return false;
        }
        return !Boolean.TRUE.equals(CommonMethod.getBoolean(form, "teacherChecked"));
    }

    private boolean canAdminReview(Map<String, Object> form) {
        if (form == null || studentLeaveId == null) {
            return false;
        }
        Integer submitState = CommonMethod.getInteger(form, "submitState");
        if (submitState == null || submitState != 1) {
            return false;
        }
        if (Boolean.TRUE.equals(CommonMethod.getBoolean(form, "adminChecked"))) {
            return false;
        }
        if (!Boolean.TRUE.equals(CommonMethod.getBoolean(form, "teacherChecked"))) {
            return false;
        }
        return Boolean.TRUE.equals(CommonMethod.getBoolean(form, "teacherPass"));
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
        boolean student = "ROLE_STUDENT".equals(roleName);
        teacherCommentColumn.setVisible(!student);
        adminCommentColumn.setVisible(!student);
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
     * 学生端：详情区时间与意见只读展示；新建时尚未产生时间与意见。
     */
    private void configureStudentDetailFieldsForStudentRole() {
        applyTimeValueLabel.setText("—");
        teacherTimeValueLabel.setText("—");
        adminTimeValueLabel.setText("—");
        teacherCommentField.setText("");
        adminCommentField.setText("");
        teacherCommentField.setEditable(false);
        adminCommentField.setEditable(false);
        teacherCommentField.setDisable(false);
        adminCommentField.setDisable(false);
    }

    private void fillDetailFieldsFromForm(Map<String, Object> form) {
        if (form == null) {
            applyTimeValueLabel.setText("—");
            teacherTimeValueLabel.setText("—");
            adminTimeValueLabel.setText("—");
            if ("ROLE_STUDENT".equals(roleName)) {
                teacherCommentField.setText("");
                adminCommentField.setText("");
            }
            return;
        }
        applyTimeValueLabel.setText(displayOrDash(CommonMethod.getString(form, "applyTime")));
        teacherTimeValueLabel.setText(displayOrDash(CommonMethod.getString(form, "teacherTime")));
        adminTimeValueLabel.setText(displayOrDash(CommonMethod.getString(form, "adminTime")));
        if ("ROLE_STUDENT".equals(roleName)) {
            teacherCommentField.setText(displayOrDash(CommonMethod.getString(form, "teacherComment")));
            adminCommentField.setText(displayOrDash(CommonMethod.getString(form, "adminComment")));
        } else {
            teacherCommentField.setText(CommonMethod.getString(form, "teacherComment"));
            adminCommentField.setText(CommonMethod.getString(form, "adminComment"));
        }
    }

    private static String displayOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value;
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
        teacherNameField.setText("");
        teacherComboBox.getSelectionModel().clearSelection();
        leaveDatePicker.setValue(null);
        returnDatePicker.setValue(null);
        reasonField.setText("");
        teacherCommentField.setText("");
        adminCommentField.setText("");
        fillDetailFieldsFromForm(null);
        if ("ROLE_STUDENT".equals(roleName)) {
            applyStudentIdentityFromJwt();
            configureStudentDetailFieldsForStudentRole();
        }
        applyTeacherFieldForCurrentRole(null);
        updateActionButtonVisibility(null);
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
        fillTeacherFieldFromForm(form);
        leaveDatePicker.setValue(parseLeaveDateFromBackend(CommonMethod.getString(form, "leaveDate")));
        returnDatePicker.setValue(parseReturnDateFromBackend(CommonMethod.getString(form, "returnDate")));
        reasonField.setText(CommonMethod.getString(form, "reason"));
        updateReasonLengthLabel(reasonField.getText());
        fillDetailFieldsFromForm(form);
    }

    /**
     * 表格选中索引变化监听，委托 {@link #changeStudentInfo()}。
     *
     * @param change 选中行索引变更事件
     */
    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        changeStudentInfo();
        if (studentLeaveId != null) {
            rightVBox.setVisible(true);
            rightVBox.setManaged(true);
        }
        updateActionButtonVisibility(form);
        applyTeacherFieldForCurrentRole(form);
    }

    /**
     * 按当前筛选条件请求请假列表并刷新表格，随后 {@link #clearPanel()}。
     */
    @FXML
    protected void onQueryButtonClick() {
        onQueryButtonClick(null);
    }

    private void onQueryButtonClick(Integer reselectLeaveId) {
        pendingReselectLeaveId = reselectLeaveId;
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
            if (!restoreTableSelection(pendingReselectLeaveId)) {
                dataTableView.getSelectionModel().clearSelection();
                rightVBox.setVisible(false);
                rightVBox.setManaged(false);
                clearPanel();
            }
        } else if (pendingReselectLeaveId == null) {
            clearPanel();
        }
        pendingReselectLeaveId = null;
    }

    private boolean restoreTableSelection(Integer leaveId) {
        if (leaveId == null || studentLeaveList == null) {
            return false;
        }
        for (int i = 0; i < studentLeaveList.size(); i++) {
            Map row = studentLeaveList.get(i);
            if (leaveId.equals(CommonMethod.getInteger(row, "studentLeaveId"))) {
                dataTableView.getSelectionModel().select(i);
                rightVBox.setVisible(true);
                rightVBox.setManaged(true);
                return true;
            }
        }
        return false;
    }

    /** 学生：清空表单，准备新建请假。 */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        rightVBox.setVisible(true);
        rightVBox.setManaged(true);
        updateActionButtonVisibility(null);
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

    /** 学生：删除选中的未审核请假记录。 */
    @FXML
    protected void onDeleteButtonClick() {
        if (studentLeaveId == null) {
            MessageDialog.showDialog("请选择要删除的请假记录！");
            return;
        }
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form != null) {
            Boolean teacherChecked = CommonMethod.getBoolean(form, "teacherChecked");
            Boolean adminChecked = CommonMethod.getBoolean(form, "adminChecked");
            if (Boolean.TRUE.equals(teacherChecked) || Boolean.TRUE.equals(adminChecked)) {
                MessageDialog.showDialog("该请假申请已审核，无法删除！");
                return;
            }
        }
        int ret = MessageDialog.choiceDialog("确认要删除该请假记录吗？");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        DataRequest req = new DataRequest();
        req.add("studentLeaveId", studentLeaveId);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveDelete", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "请求失败，请检查网络或服务。");
        }
    }

    @Override
    public void doDelete() {
        onDeleteButtonClick();
    }

    @Override
    public void doNew() {
        onAddButtonClick();
    }

    @Override
    public void doSave() {
        if (!saveButton.isVisible()) {
            MessageDialog.showDialog("当前记录不可保存为草稿，请使用提交！");
            return;
        }
        onSaveButtonClick();
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
        boolean isSubmit = state != null && state == 1;
        if ("ROLE_STUDENT".equals(roleName) && isSubmit && op == null) {
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
        String reason = reasonField.getText();
        if ("ROLE_STUDENT".equals(roleName) && isSubmit && (reason == null || reason.trim().isEmpty())) {
            MessageDialog.showDialog("请填写请假理由！");
            return;
        }
        if (reason != null && reason.length() > REASON_MAX_LENGTH) {
            MessageDialog.showDialog("请假理由不能超过100字！");
            return;
        }
        if (op != null) {
            req.add("teacherId", Integer.parseInt(op.getValue()));
        }
        req.add("studentLeaveId", studentLeaveId);
        req.add("leaveDate", leaveDate != null ? leaveDate.format(LEAVE_DATE_FORMAT) : "");
        req.add("returnDate", returnDate != null ? returnDate.format(LEAVE_DATE_FORMAT) : "");
        req.add("reason", reason);
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveSave", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存提交成功！");
            onQueryButtonClick(studentLeaveId);
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
        if (studentLeaveId == null) {
            MessageDialog.showDialog("请选择要审核的请假记录！");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("studentLeaveId", studentLeaveId);
        req.add("teacherComment", teacherCommentField.getText());
        req.add("adminComment", adminCommentField.getText());
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveCheck", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("审核成功！");
            onQueryButtonClick(studentLeaveId);
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "请求失败，请检查网络或服务。");
        }
    }
}
