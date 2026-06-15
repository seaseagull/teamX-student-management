package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.JwtResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 主框架控制器：负责顶部工具栏、角色菜单、页面切换、主题切换与状态提示。
 */
public class MainFrameController {
    private static final String LIGHT_THEME = "/com/teach/javafx/css/theme-light.css";
    private static final String DARK_THEME = "/com/teach/javafx/css/theme-dark.css";
    private static final String COMPONENT_THEME = "/com/teach/javafx/css/component.css";
    private static final String LAYOUT_THEME = "/com/teach/javafx/css/layout.css";

    @FXML private Label systemPrompt;
    @FXML private Label pagePrompt;
    @FXML private Label roleLabel;
    @FXML private Label userNameLabel;
    @FXML private BorderPane rootPane;
    @FXML private Label sidebarHintLabel;
    @FXML private TextField searchField;
    @FXML private Button themeToggleButton;
    @FXML private VBox menuContainer;
    @FXML private StackPane contentArea;

    private final Map<String, Parent> contentCache = new java.util.HashMap<>();
    private final Map<String, ToolController> toolControllerCache = new java.util.HashMap<>();
    private final Map<String, Button> menuButtonMap = new java.util.HashMap<>();
    private String currentPageName;
    private String currentTheme = LIGHT_THEME;
    private List<MenuEntry> currentMenuEntries = List.of();

    @FXML
    public void initialize() {
        JwtResponse jwt = AppStore.getJwt();
        String role = jwt == null || jwt.getRole() == null ? "GUEST" : jwt.getRole().replace("ROLE_", "");
        String userName = jwt == null ? "访客" : firstNonBlank(jwt.getPerName(), jwt.getUsername(), "访客");

        roleLabel.setText("角色：" + role);
        userNameLabel.setText(userName);
        if (sidebarHintLabel != null) {
            sidebarHintLabel.setText("按角色动态生成菜单");
        }
        systemPrompt.setText("系统已就绪，正在加载页面资源...");
        pagePrompt.setText("工作台");
        searchField.setPromptText("搜索功能、菜单或页面");

        applyThemeToScene(currentTheme);
        currentMenuEntries = buildFallbackMenus(role);
        renderMenu(currentMenuEntries);
        MenuEntry firstPage = findFirstPage(currentMenuEntries);
        if (firstPage != null) {
            openPage(firstPage);
        }
        loadServerInfoAsync();
        loadMenusAsync(role);
    }

    private void loadServerInfoAsync() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                DataResponse response = HttpRequestUtil.request("/api/base/getDataBaseUserName", new DataRequest());
                Object data = response == null ? null : response.getData();
                return data == null ? "未知" : String.valueOf(data);
            }
        };
        task.setOnSucceeded(event -> systemPrompt.setText("服务器：" + HttpRequestUtil.serverUrl + " 数据库：" + task.getValue()));
        task.setOnFailed(event -> systemPrompt.setText("服务器：" + HttpRequestUtil.serverUrl + " 数据库：获取失败"));
        Thread thread = new Thread(task, "mainframe-db-info");
        thread.setDaemon(true);
        thread.start();
    }

    private void loadMenusAsync(String role) {
        Task<List<Map>> task = new Task<>() {
            @Override
            protected List<Map> call() {
                DataResponse response = HttpRequestUtil.request("/api/base/getMenuList", new DataRequest());
                Object data = response == null ? null : response.getData();
                if (data instanceof List) {
                    return (List<Map>) data;
                }
                return new ArrayList<>();
            }
        };
        task.setOnSucceeded(event -> {
            List<MenuEntry> entries = buildMenuEntries(role, task.getValue());
            if (!entries.isEmpty()) {
                currentMenuEntries = entries;
                renderMenu(entries);
                highlightCurrentMenu();
            }
        });
        task.setOnFailed(event -> {
            currentMenuEntries = buildFallbackMenus(role);
            renderMenu(currentMenuEntries);
            if (!currentMenuEntries.isEmpty()) {
                openPage(currentMenuEntries.get(0));
            }
        });
        Thread thread = new Thread(task, "mainframe-menu-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void renderMenu(List<MenuEntry> entries) {
        menuContainer.getChildren().clear();
        menuButtonMap.clear();
        for (MenuEntry entry : orderAccountMenusLast(entries)) {
            if (entry.hasChildren()) {
                Label groupLabel = new Label(entry.title());
                groupLabel.getStyleClass().add("nav-group-title");
                menuContainer.getChildren().add(groupLabel);
                for (MenuEntry child : orderAccountMenusLast(entry.children())) {
                    addMenuButton(child, true);
                }
            } else {
                addMenuButton(entry, false);
            }
        }
    }

    private void addMenuButton(MenuEntry entry, boolean childMenu) {
        String pageName = normalizePageName(entry.name(), entry.title());
        Button button = new Button(childMenu ? "  " + entry.title() : entry.title());
        button.getStyleClass().addAll("nav-item", "nav-btn");
        if (childMenu) {
            button.getStyleClass().add("nav-child-item");
        }
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> openPage(entry));
        menuButtonMap.put(pageName, button);
        menuContainer.getChildren().add(button);
    }

    private void openPage(MenuEntry entry) {
        if (entry == null || entry.name() == null || entry.name().isBlank()) {
            return;
        }
        String name = normalizePageName(entry.name(), entry.title());
        Parent page = contentCache.get(name);
        if (page == null) {
            page = loadPage(name);
            if (page == null) {
                return;
            }
            contentCache.put(name, page);
        }
        contentArea.getChildren().setAll(page);
        currentPageName = name;
        pagePrompt.setText(entry.title());
        highlightCurrentMenu();
        ToolController controller = toolControllerCache.get(name);
        if (controller != null) {
            controller.doRefresh();
        }
    }

    private void highlightCurrentMenu() {
        for (Map.Entry<String, Button> entry : menuButtonMap.entrySet()) {
            Button button = entry.getValue();
            button.getStyleClass().remove("nav-item-active");
            if (entry.getKey().equals(currentPageName)) {
                button.getStyleClass().add("nav-item-active");
            }
        }
    }

    private Parent loadPage(String name) {
        try {
            URL pageUrl = resolveFxmlUrl(name);
            if (pageUrl == null) {
                new Alert(Alert.AlertType.ERROR, "打开页面失败：" + name + "\n未找到对应的 FXML 文件").show();
                return null;
            }
            FXMLLoader loader = new FXMLLoader(pageUrl);
            Parent root = loader.load();
            applySceneStyles(root);
            Object controller = loader.getController();
            if (controller instanceof ToolController toolController) {
                toolControllerCache.put(name, toolController);
                toolController.doRefresh();
            }
            return root;
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "打开页面失败：" + name + "\n" + ex.getMessage()).show();
            return null;
        }
    }

    private URL resolveFxmlUrl(String name) throws MalformedURLException {
        URL url = MainApplication.class.getResource(name + ".fxml");
        if (url != null) {
            return url;
        }
        url = MainApplication.class.getResource("base/" + name + ".fxml");
        if (url != null) {
            return url;
        }
        File resourceFile = new File("java-fx/java-fx/src/main/resources/com/teach/javafx/" + name + ".fxml");
        if (resourceFile.exists()) {
            return resourceFile.toURI().toURL();
        }
        File baseResourceFile = new File("java-fx/java-fx/src/main/resources/com/teach/javafx/base/" + name + ".fxml");
        if (baseResourceFile.exists()) {
            return baseResourceFile.toURI().toURL();
        }
        return null;
    }

    private void applySceneStyles(Parent root) {
        removeThemeStylesheets(root.getStylesheets());
        addStylesheetIfMissing(root.getStylesheets(), COMPONENT_THEME);
        addStylesheetIfMissing(root.getStylesheets(), LAYOUT_THEME);
        addStylesheetIfMissing(root.getStylesheets(), currentTheme);
    }

    private void applyThemeToScene(String theme) {
        applyThemeToRoot(rootPane, theme);
        for (Parent cachedPage : contentCache.values()) {
            applyThemeToRoot(cachedPage, theme);
        }

        Scene scene = rootScene();
        if (scene != null) {
            removeThemeStylesheets(scene.getStylesheets());
            addStylesheetIfMissing(scene.getStylesheets(), COMPONENT_THEME);
            addStylesheetIfMissing(scene.getStylesheets(), LAYOUT_THEME);
            addStylesheetIfMissing(scene.getStylesheets(), theme);
        }

        if (rootPane != null) {
            rootPane.applyCss();
            rootPane.layout();
        }
        if (contentArea != null) {
            contentArea.applyCss();
            contentArea.layout();
        }
    }

    private void applyThemeToRoot(Parent root, String theme) {
        if (root == null) {
            return;
        }
        removeThemeStylesheets(root.getStylesheets());
        addStylesheetIfMissing(root.getStylesheets(), COMPONENT_THEME);
        addStylesheetIfMissing(root.getStylesheets(), LAYOUT_THEME);
        addStylesheetIfMissing(root.getStylesheets(), theme);
        root.applyCss();
    }

    private void removeThemeStylesheets(List<String> stylesheets) {
        stylesheets.removeIf(sheet -> sheet.endsWith("theme-light.css")
                || sheet.endsWith("theme-dark.css")
                || sheet.endsWith("component.css")
                || sheet.endsWith("layout.css"));
    }

    private void addStylesheetIfMissing(List<String> stylesheets, String resourcePath) {
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return;
        }
        String url = resource.toExternalForm();
        if (!stylesheets.contains(url)) {
            stylesheets.add(url);
        }
    }

    private Scene rootScene() {
        if (rootPane != null && rootPane.getScene() != null) {
            return rootPane.getScene();
        }
        return contentArea == null ? null : contentArea.getScene();
    }

    @FXML
    public void onToggleTheme(ActionEvent event) {
        currentTheme = LIGHT_THEME.equals(currentTheme) ? DARK_THEME : LIGHT_THEME;
        if (themeToggleButton != null) {
            themeToggleButton.setText(LIGHT_THEME.equals(currentTheme) ? "深色模式" : "浅色模式");
        }
        applyThemeToScene(currentTheme);
    }

    @FXML
    public void onRefreshCurrentPage(ActionEvent event) {
        if (currentPageName == null) {
            if (!currentMenuEntries.isEmpty()) {
                openPage(currentMenuEntries.get(0));
            }
            return;
        }
        contentCache.remove(currentPageName);
        toolControllerCache.remove(currentPageName);
        for (MenuEntry entry : currentMenuEntries) {
            if (currentPageName.equals(normalizePageName(entry.name(), entry.title()))) {
                openPage(entry);
                return;
            }
        }
    }

    @FXML
    public void onLogoutAction(ActionEvent event) {
        logout();
    }

    protected void logout() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);
            MainApplication.loginStage("Login", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AppStore.setJwt(null);
    }

    public void changeContent(String name, String title) {
        openPage(new MenuEntry(name, title));
    }

    public void changeContent(ActionEvent ae) {
        if (ae.getSource() instanceof Button button) {
            changeContent(button.getId(), button.getText());
        }
    }

    private String normalizePageName(String name, String title) {
        String safeName = name == null ? "" : name.trim();
        String safeTitle = title == null ? "" : title.trim();
        String normalizedTitle = safeTitle.toLowerCase(Locale.ROOT);

        if (normalizedTitle.contains("成绩管理") || normalizedTitle.contains("成绩查询") || "score".equals(safeName)) {
            return "score-panel";
        }
        if (normalizedTitle.contains("校历") || "calendar".equals(safeName)) {
            return "calendar-view";
        }
        if (normalizedTitle.contains("课程") && !normalizedTitle.contains("选课")) {
            return "course-panel";
        }
        if (normalizedTitle.contains("选课")) {
            return "student-course-select-panel";
        }
        if (normalizedTitle.contains("学生") && normalizedTitle.contains("管理")) {
            return "student-panel";
        }
        if (normalizedTitle.contains("考试")) {
            return "exam_panel";
        }
        if (normalizedTitle.contains("请假") || normalizedTitle.contains("审批")) {
            return normalizedTitle.contains("申请") ? "student-leave-panel" : "leave-request-panel";
        }
        if (normalizedTitle.contains("志愿")) {
            return normalizedTitle.contains("学生") || normalizedTitle.contains("我的") ? "student-volunteer-panel" : "volunteer-activity-panel";
        }
        if (normalizedTitle.contains("作业")) {
            return "homework-panel";
        }
        if (normalizedTitle.contains("网站") || normalizedTitle.contains("校园服务") || normalizedTitle.contains("生活服务")) {
            return "website-panel";
        }
        if (normalizedTitle.contains("天气")) {
            return "weather-panel";
        }
        if (normalizedTitle.contains("地图")) {
            return "sdu-map-panel";
        }
        if (normalizedTitle.contains("班车") || normalizedTitle.contains("校车")) {
            return "bus-schedule-panel";
        }
        if (normalizedTitle.contains("便签") || normalizedTitle.contains("倒计时")) {
            return "quote-countdown-panel";
        }
        if (safeName.endsWith(".fxml")) {
            return safeName.substring(0, safeName.length() - 5);
        }
        return safeName;
    }

    private List<MenuEntry> buildMenuEntries(String role, List<Map> menuList) {
        List<MenuEntry> entries = new ArrayList<>();
        if (menuList != null) {
            for (Map item : menuList) {
                MenuEntry entry = toMenuEntry(item);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        if (entries.isEmpty()) {
            entries = buildFallbackMenus(role);
        }
        return entries;
    }

    private MenuEntry toMenuEntry(Map item) {
        Object name = item.get("name");
        Object title = item.get("title");
        if (name == null && title == null) {
            return null;
        }

        List<MenuEntry> children = new ArrayList<>();
        Object childMenus = item.get("sList");
        if (childMenus instanceof List<?> childList) {
            for (Object child : childList) {
                if (child instanceof Map childMap) {
                    MenuEntry childEntry = toMenuEntry(childMap);
                    if (childEntry != null) {
                        children.add(childEntry);
                    }
                }
            }
        }

        return new MenuEntry(
                name == null ? "" : String.valueOf(name),
                title == null ? "未命名菜单" : String.valueOf(title),
                children
        );
    }

    private List<MenuEntry> orderAccountMenusLast(List<MenuEntry> entries) {
        List<MenuEntry> normalMenus = new ArrayList<>();
        List<MenuEntry> accountMenus = new ArrayList<>();
        for (MenuEntry entry : entries) {
            if (isAccountMenu(entry)) {
                accountMenus.add(entry);
            } else {
                normalMenus.add(entry);
            }
        }
        normalMenus.addAll(accountMenus);
        return normalMenus;
    }

    private boolean isAccountMenu(MenuEntry entry) {
        String text = (entry.name() + " " + entry.title()).toLowerCase(Locale.ROOT);
        return text.contains("password")
                || text.contains("logout")
                || text.contains("退出")
                || text.contains("密码")
                || text.contains("账号")
                || text.contains("账户")
                || text.contains("个人中心");
    }

    private MenuEntry findFirstPage(List<MenuEntry> entries) {
        for (MenuEntry entry : orderAccountMenusLast(entries)) {
            if (entry.hasChildren()) {
                MenuEntry childPage = findFirstPage(entry.children());
                if (childPage != null) {
                    return childPage;
                }
            } else if (!isAccountMenu(entry)) {
                return entry;
            }
        }
        return entries.isEmpty() ? null : entries.get(0);
    }

    private List<MenuEntry> buildFallbackMenus(String role) {
        Map<String, List<MenuEntry>> defaults = new LinkedHashMap<>();
        defaults.put("ADMIN", List.of(
                new MenuEntry("system_summary_panel", "工作台"),
                new MenuEntry("student-panel", "学生管理"),
                new MenuEntry("teacher-panel", "教师管理"),
                new MenuEntry("course-panel", "课程管理"),
                new MenuEntry("exam_panel", "考试管理"),
                new MenuEntry("leave-request-panel", "请假审批"),
                new MenuEntry("volunteer-activity-panel", "志愿活动"),
                new MenuEntry("calendar-view", "校历管理"),
                new MenuEntry("website-panel", "校园服务"),
                new MenuEntry("system_summary_panel", "系统设置")
        ));
        defaults.put("TEACHER", List.of(
                new MenuEntry("system_summary_panel", "工作台"),
                new MenuEntry("course-panel", "我的课程"),
                new MenuEntry("score-table-panel", "成绩录入"),
                new MenuEntry("homework-panel", "作业管理"),
                new MenuEntry("leave-request-panel", "审批中心"),
                new MenuEntry("exam_panel", "考试安排"),
                new MenuEntry("website-panel", "校园服务")
        ));
        defaults.put("STUDENT", List.of(
                new MenuEntry("system_summary_panel", "工作台"),
                new MenuEntry("student-panel", "我的信息"),
                new MenuEntry("student-course-panel", "学习中心"),
                new MenuEntry("score-panel", "成绩查询"),
                new MenuEntry("exam_panel", "考试安排"),
                new MenuEntry("student-leave-panel", "请假申请"),
                new MenuEntry("student-volunteer-panel", "志愿活动"),
                new MenuEntry("website-panel", "校园服务"),
                new MenuEntry("quote-countdown-panel", "学期便签")
        ));
        return new ArrayList<>(defaults.getOrDefault(role, List.of(new MenuEntry("system_summary_panel", "工作台"))));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "访客";
    }

    public ToolController getToolController(String name) {
        if (name == null) {
            return null;
        }
        return toolControllerCache.get(name);
    }

    private record MenuEntry(String name, String title, List<MenuEntry> children) {
        private MenuEntry(String name, String title) {
            this(name, title, List.of());
        }

        private boolean hasChildren() {
            return children != null && !children.isEmpty();
        }
    }
}
