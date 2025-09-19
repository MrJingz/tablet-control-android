package com.feixiang.tabletcontrol.ui;

import com.feixiang.tabletcontrol.core.model.ComponentData;
import com.feixiang.tabletcontrol.core.model.PageData;
import com.feixiang.tabletcontrol.core.model.ProjectData;
import com.feixiang.tabletcontrol.core.service.ProjectService;
import com.feixiang.tabletcontrol.platform.PlatformManager;
import com.feixiang.tabletcontrol.ui.theme.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * 主视图控制器 - 完整功能版本
 * 负责管理主界面的显示和交互，包括项目管理、页面管理、组件管理等完整功能
 */
public class MainViewController {

    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    private final ProjectService projectService;
    private final PlatformManager platformManager;
    private final ThemeManager themeManager;

    // UI组件 - 主要布局
    private BorderPane rootPane;
    private MenuBar menuBar;
    private ToolBar toolBar;
    private TabPane contentTabPane;
    private VBox statusBar;

    // UI组件 - 状态显示
    private Label titleLabel;
    private Label statusLabel;
    private Label platformLabel;
    private Label projectInfoLabel;
    private ProgressBar progressBar;

    // UI组件 - 按钮
    private Button newProjectButton;
    private Button loadProjectButton;
    private Button saveProjectButton;
    private Button exportProjectButton;
    private Button importProjectButton;
    private Button createPageButton;
    private Button deletePageButton;
    private Button renamePageButton;
    private Button settingsButton;

    // UI组件 - 页面管理
    private ListView<String> pageListView;
    private VBox pageContentArea;
    private ScrollPane pageScrollPane;
    private VBox contentArea;

    // UI组件 - 组件管理
    private ListView<ComponentData> componentListView;
    private VBox componentPropertiesArea;

    // 状态
    private ProjectData currentProject;
    private String currentPageName;
    private ComponentData selectedComponent;

    public MainViewController(ProjectService projectService, PlatformManager platformManager, ThemeManager themeManager) {
        this.projectService = projectService;
        this.platformManager = platformManager;
        this.themeManager = themeManager;

        initializeUI();
        setupEventHandlers();
        loadInitialData();
        updateUI();

        logger.info("主视图控制器初始化完成");
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        logger.debug("初始化完整UI组件");

        // 创建根面板
        rootPane = new BorderPane();
        rootPane.getStyleClass().add("main-view");

        // 创建菜单栏
        createMenuBar();

        // 创建工具栏
        createToolBar();

        // 创建中心内容区域
        createContentArea();

        // 创建状态栏
        createStatusBar();

        // 组装布局
        VBox topArea = new VBox();
        if (!platformManager.isMobile()) {
            topArea.getChildren().add(menuBar);
        }
        topArea.getChildren().add(toolBar);

        rootPane.setTop(topArea);
        rootPane.setCenter(contentTabPane);
        rootPane.setBottom(statusBar);

        logger.debug("UI组件初始化完成");
    }

    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        if (platformManager.isMobile()) {
            return; // 移动端不显示菜单栏
        }

        menuBar = new MenuBar();
        menuBar.getStyleClass().add("main-menu-bar");

        // 文件菜单
        Menu fileMenu = new Menu("文件");
        fileMenu.getItems().addAll(
            createMenuItem("新建项目", this::handleNewProject),
            createMenuItem("打开项目", this::handleLoadProject),
            new SeparatorMenuItem(),
            createMenuItem("保存项目", this::handleSaveProject),
            createMenuItem("另存为", this::handleSaveAsProject),
            new SeparatorMenuItem(),
            createMenuItem("导入项目", this::handleImportProject),
            createMenuItem("导出项目", this::handleExportProject),
            new SeparatorMenuItem(),
            createMenuItem("退出", this::handleExit)
        );

        // 编辑菜单
        Menu editMenu = new Menu("编辑");
        editMenu.getItems().addAll(
            createMenuItem("撤销", this::handleUndo),
            createMenuItem("重做", this::handleRedo),
            new SeparatorMenuItem(),
            createMenuItem("复制", this::handleCopy),
            createMenuItem("粘贴", this::handlePaste),
            createMenuItem("删除", this::handleDelete)
        );

        // 页面菜单
        Menu pageMenu = new Menu("页面");
        pageMenu.getItems().addAll(
            createMenuItem("新建页面", this::handleCreatePage),
            createMenuItem("删除页面", this::handleDeletePage),
            createMenuItem("重命名页面", this::handleRenamePage),
            new SeparatorMenuItem(),
            createMenuItem("复制页面", this::handleCopyPage)
        );

        // 视图菜单
        Menu viewMenu = new Menu("视图");
        viewMenu.getItems().addAll(
            createMenuItem("刷新", this::handleRefresh),
            new SeparatorMenuItem(),
            createMenuItem("全屏", this::handleToggleFullscreen),
            createMenuItem("缩放", this::handleZoom)
        );

        // 工具菜单
        Menu toolsMenu = new Menu("工具");
        toolsMenu.getItems().addAll(
            createMenuItem("设置", this::handleSettings),
            createMenuItem("主题", this::handleThemeSettings),
            new SeparatorMenuItem(),
            createMenuItem("备份管理", this::handleBackupManager),
            createMenuItem("数据验证", this::handleDataValidation)
        );

        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        helpMenu.getItems().addAll(
            createMenuItem("用户手册", this::handleUserManual),
            createMenuItem("快捷键", this::handleShortcuts),
            new SeparatorMenuItem(),
            createMenuItem("关于", this::handleAbout)
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, pageMenu, viewMenu, toolsMenu, helpMenu);
    }

    /**
     * 创建菜单项
     */
    private MenuItem createMenuItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
    }

    /**
     * 创建工具栏
     */
    private void createToolBar() {
        toolBar = new ToolBar();
        toolBar.getStyleClass().add("main-toolbar");

        // 项目操作按钮
        newProjectButton = createToolButton("新建", this::handleNewProject);
        loadProjectButton = createToolButton("打开", this::handleLoadProject);
        saveProjectButton = createToolButton("保存", this::handleSaveProject);

        // 分隔符
        Separator separator1 = new Separator();

        // 页面操作按钮
        createPageButton = createToolButton("新建页面", this::handleCreatePage);
        deletePageButton = createToolButton("删除页面", this::handleDeletePage);
        renamePageButton = createToolButton("重命名页面", this::handleRenamePage);

        // 分隔符
        Separator separator2 = new Separator();

        // 导入导出按钮
        importProjectButton = createToolButton("导入", this::handleImportProject);
        exportProjectButton = createToolButton("导出", this::handleExportProject);

        // 弹性空间
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 设置按钮
        settingsButton = createToolButton("设置", this::handleSettings);

        toolBar.getItems().addAll(
            newProjectButton, loadProjectButton, saveProjectButton,
            separator1,
            createPageButton, deletePageButton, renamePageButton,
            separator2,
            importProjectButton, exportProjectButton,
            spacer,
            settingsButton
        );
    }

    /**
     * 创建工具栏按钮
     */
    private Button createToolButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("tool-button");
        button.setOnAction(e -> action.run());

        // 根据平台调整按钮大小
        if (platformManager.isMobile()) {
            button.setPrefHeight(44); // 移动端触控友好尺寸
            button.setPrefWidth(80);
        } else {
            button.setPrefHeight(32);
            button.setPrefWidth(60);
        }

        return button;
    }

    /**
     * 创建内容区域
     */
    private void createContentArea() {
        contentTabPane = new TabPane();
        contentTabPane.getStyleClass().add("main-content-tabs");

        // 项目概览标签页
        Tab overviewTab = new Tab("项目概览");
        overviewTab.setClosable(false);
        VBox overviewContent = createOverviewContent();
        overviewTab.setContent(overviewContent);

        // 页面管理标签页
        Tab pagesTab = new Tab("页面管理");
        pagesTab.setClosable(false);
        VBox pagesContent = createPagesContent();
        pagesTab.setContent(pagesContent);

        // 组件管理标签页
        Tab componentsTab = new Tab("组件管理");
        componentsTab.setClosable(false);
        VBox componentsContent = createComponentsContent();
        componentsTab.setContent(componentsContent);

        contentTabPane.getTabs().addAll(overviewTab, pagesTab, componentsTab);
    }

    /**
     * 创建项目概览内容
     */
    private VBox createOverviewContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("overview-content");

        // 项目信息区域
        projectInfoLabel = new Label("无项目数据");
        projectInfoLabel.getStyleClass().add("project-info");

        // 统计信息区域
        VBox statsArea = new VBox(10);
        statsArea.getStyleClass().add("stats-area");

        Label statsTitle = new Label("项目统计");
        statsTitle.getStyleClass().add("section-title");

        statsArea.getChildren().add(statsTitle);

        content.getChildren().addAll(projectInfoLabel, statsArea);
        return content;
    }

    /**
     * 创建页面管理内容
     */
    private VBox createPagesContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("pages-content");

        // 页面列表
        pageListView = new ListView<>();
        pageListView.getStyleClass().add("page-list");
        pageListView.setPrefHeight(200);

        // 页面内容区域
        pageContentArea = new VBox(10);
        pageContentArea.getStyleClass().add("page-content-area");

        pageScrollPane = new ScrollPane(pageContentArea);
        pageScrollPane.setFitToWidth(true);
        pageScrollPane.getStyleClass().add("page-scroll-pane");

        content.getChildren().addAll(
            new Label("页面列表:"),
            pageListView,
            new Label("页面内容:"),
            pageScrollPane
        );

        return content;
    }

    /**
     * 创建组件管理内容
     */
    private VBox createComponentsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("components-content");

        // 组件列表
        componentListView = new ListView<>();
        componentListView.getStyleClass().add("component-list");
        componentListView.setPrefHeight(200);

        // 组件属性区域
        componentPropertiesArea = new VBox(10);
        componentPropertiesArea.getStyleClass().add("component-properties-area");

        content.getChildren().addAll(
            new Label("组件列表:"),
            componentListView,
            new Label("组件属性:"),
            componentPropertiesArea
        );

        return content;
    }

    /**
     * 创建状态栏
     */
    private void createStatusBar() {
        statusBar = new VBox(5);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.getStyleClass().add("status-bar");

        // 状态信息行
        HBox statusRow = new HBox(20);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("就绪");
        statusLabel.getStyleClass().add("status-label");

        // 弹性空间
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 平台信息
        platformLabel = new Label("平台: " + platformManager.getCurrentPlatform().getDisplayName());
        platformLabel.getStyleClass().add("platform-label");

        statusRow.getChildren().addAll(statusLabel, spacer, platformLabel);

        // 进度条
        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.getStyleClass().add("status-progress");

        statusBar.getChildren().addAll(statusRow, progressBar);
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        logger.debug("设置事件处理器");

        // 页面列表选择事件
        if (pageListView != null) {
            pageListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentPageName = newValue;
                        projectService.setCurrentPage(newValue);
                        updatePageContent();
                    }
                });
        }

        // 组件列表选择事件
        if (componentListView != null) {
            componentListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedComponent = newValue;
                    updateComponentProperties();
                });
        }
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        logger.info("加载初始数据");
        try {
            currentProject = projectService.loadProject();
            if (currentProject != null) {
                currentPageName = currentProject.getCurrentPage();
            }
        } catch (Exception e) {
            logger.warn("加载初始数据失败", e);
            // 创建新项目作为默认
            currentProject = projectService.createNewProject();
            currentPageName = currentProject.getCurrentPage();
        }
    }

    /**
     * 更新UI状态
     */
    private void updateUI() {
        // 根据项目状态更新按钮状态
        boolean hasProject = currentProject != null;

        if (saveProjectButton != null) saveProjectButton.setDisable(!hasProject);
        if (exportProjectButton != null) exportProjectButton.setDisable(!hasProject);
        if (createPageButton != null) createPageButton.setDisable(!hasProject);
        if (deletePageButton != null) deletePageButton.setDisable(!hasProject || currentPageName == null);
        if (renamePageButton != null) renamePageButton.setDisable(!hasProject || currentPageName == null);
    }

    /**
     * 显示项目数据
     */
    public void displayProject(ProjectData projectData) {
        logger.info("显示项目数据: {}", projectData != null ? projectData.getProjectSummary() : "null");

        this.currentProject = projectData;
        if (projectData != null) {
            this.currentPageName = projectData.getCurrentPage();
        }

        updateProjectDisplay();
        updateUI();
    }

    /**
     * 更新项目显示
     */
    private void updateProjectDisplay() {
        // 更新项目信息
        if (projectInfoLabel != null) {
            if (currentProject != null) {
                projectInfoLabel.setText(currentProject.getProjectSummary());
            } else {
                projectInfoLabel.setText("无项目数据");
            }
        }

        // 更新页面列表
        if (pageListView != null) {
            pageListView.getItems().clear();
            if (currentProject != null) {
                pageListView.getItems().addAll(currentProject.getPages());
                if (currentPageName != null) {
                    pageListView.getSelectionModel().select(currentPageName);
                }
            }
        }

        // 更新页面内容
        updatePageContent();
    }

    /**
     * 更新页面内容
     */
    private void updatePageContent() {
        if (pageContentArea == null) return;

        pageContentArea.getChildren().clear();

        if (currentProject != null && currentPageName != null) {
            PageData page = currentProject.getPage(currentPageName);
            if (page != null) {
                Label pageTitle = new Label("页面: " + page.getName());
                pageTitle.getStyleClass().add("page-title");

                Label componentCount = new Label("组件数量: " + page.getComponentCount());
                componentCount.getStyleClass().add("component-count");

                pageContentArea.getChildren().addAll(pageTitle, componentCount);

                // 更新组件列表
                if (componentListView != null) {
                    componentListView.getItems().clear();
                    componentListView.getItems().addAll(page.getComponents());
                }
            }
        }
    }

    /**
     * 更新组件属性
     */
    private void updateComponentProperties() {
        if (componentPropertiesArea == null) return;

        componentPropertiesArea.getChildren().clear();

        if (selectedComponent != null) {
            Label componentTitle = new Label("组件: " + selectedComponent.getFunctionType());
            componentTitle.getStyleClass().add("component-title");

            Label componentSummary = new Label(selectedComponent.getComponentSummary());
            componentSummary.getStyleClass().add("component-summary");

            componentPropertiesArea.getChildren().addAll(componentTitle, componentSummary);
        }
    }

    /**
     * 更新状态显示
     */
    private void updateStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
        logger.debug("状态更新: {}", status);
    }

    /**
     * 获取根节点
     */
    public Parent getRootNode() {
        return rootPane;
    }
    
    /**
     * 创建顶部区域
     */
    private Node createTopArea() {
        VBox topArea = new VBox(10);
        topArea.setPadding(new Insets(20));
        topArea.getStyleClass().add("top-area");
        
        // 标题
        titleLabel = new Label("平板中控系统 - 跨平台版");
        titleLabel.getStyleClass().add("title-label");
        
        // 平台信息
        platformLabel = new Label("平台: " + platformManager.getCurrentPlatform().getDisplayName());
        platformLabel.getStyleClass().add("platform-label");
        
        // 按钮区域
        HBox buttonArea = createButtonArea();
        
        topArea.getChildren().addAll(titleLabel, platformLabel, buttonArea);
        return topArea;
    }
    
    /**
     * 创建按钮区域
     */
    private HBox createButtonArea() {
        HBox buttonArea = new HBox(10);
        buttonArea.setAlignment(Pos.CENTER_LEFT);
        buttonArea.getStyleClass().add("button-area");
        
        // 加载项目按钮
        loadProjectButton = new Button("加载项目");
        loadProjectButton.getStyleClass().add("action-button");
        
        // 保存项目按钮
        saveProjectButton = new Button("保存项目");
        saveProjectButton.getStyleClass().add("action-button");
        
        // 创建页面按钮
        createPageButton = new Button("创建页面");
        createPageButton.getStyleClass().add("action-button");
        
        // 根据平台调整按钮大小
        double buttonHeight = platformManager.getMinTouchTargetSize();
        loadProjectButton.setPrefHeight(buttonHeight);
        saveProjectButton.setPrefHeight(buttonHeight);
        createPageButton.setPrefHeight(buttonHeight);
        
        buttonArea.getChildren().addAll(loadProjectButton, saveProjectButton, createPageButton);
        return buttonArea;
    }






    
    // 事件处理方法

    private void handleNewProject() {
        logger.info("处理新建项目");
        try {
            currentProject = projectService.createNewProject();
            updateProjectDisplay();
            updateStatus("新项目创建完成");
        } catch (Exception e) {
            logger.error("创建新项目失败", e);
            showErrorDialog("创建失败", "创建新项目时发生错误: " + e.getMessage());
        }
    }

    private void handleLoadProject() {
        logger.info("处理加载项目");
        try {
            updateStatus("正在加载项目...");
            ProjectData project = projectService.loadProject();
            displayProject(project);
            updateStatus("项目加载完成");
        } catch (Exception e) {
            logger.error("加载项目失败", e);
            updateStatus("加载项目失败: " + e.getMessage());
            showErrorDialog("加载失败", "加载项目时发生错误: " + e.getMessage());
        }
    }

    private void handleSaveProject() {
        logger.info("处理保存项目");
        try {
            if (currentProject != null) {
                updateStatus("正在保存项目...");
                projectService.saveProject(currentProject);
                updateStatus("项目保存完成");
            } else {
                updateStatus("没有项目需要保存");
            }
        } catch (Exception e) {
            logger.error("保存项目失败", e);
            updateStatus("保存项目失败: " + e.getMessage());
            showErrorDialog("保存失败", "保存项目时发生错误: " + e.getMessage());
        }
    }

    private void handleSaveAsProject() {
        logger.info("处理另存为项目");
        // 实现另存为逻辑
        showInfoDialog("功能提示", "另存为功能正在开发中");
    }

    private void handleImportProject() {
        logger.info("处理导入项目");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导入项目");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON文件", "*.json"));

            File selectedFile = fileChooser.showOpenDialog(getStage());
            if (selectedFile != null) {
                updateStatus("正在导入项目...");
                ProjectData importedProject = projectService.importProject(selectedFile.getAbsolutePath());
                displayProject(importedProject);
                updateStatus("项目导入完成");
            }
        } catch (Exception e) {
            logger.error("导入项目失败", e);
            updateStatus("导入项目失败: " + e.getMessage());
            showErrorDialog("导入失败", "导入项目时发生错误: " + e.getMessage());
        }
    }

    private void handleExportProject() {
        logger.info("处理导出项目");
        try {
            if (currentProject == null) {
                showWarningDialog("导出失败", "没有项目需要导出");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导出项目");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON文件", "*.json"));

            File selectedFile = fileChooser.showSaveDialog(getStage());
            if (selectedFile != null) {
                updateStatus("正在导出项目...");
                projectService.exportProject(selectedFile.getAbsolutePath());
                updateStatus("项目导出完成");
            }
        } catch (Exception e) {
            logger.error("导出项目失败", e);
            updateStatus("导出项目失败: " + e.getMessage());
            showErrorDialog("导出失败", "导出项目时发生错误: " + e.getMessage());
        }
    }

    private void handleCreatePage() {
        logger.info("处理创建页面");
        try {
            String pageName = showInputDialog("创建页面", "请输入页面名称:", "新页面");
            if (pageName != null && !pageName.trim().isEmpty()) {
                projectService.createPage(pageName.trim());
                updateProjectDisplay();
                updateStatus("页面创建完成: " + pageName);
            }
        } catch (Exception e) {
            logger.error("创建页面失败", e);
            updateStatus("创建页面失败: " + e.getMessage());
            showErrorDialog("创建失败", "创建页面时发生错误: " + e.getMessage());
        }
    }

    private void handleDeletePage() {
        logger.info("处理删除页面");
        try {
            if (currentPageName != null) {
                boolean confirmed = showConfirmDialog("删除页面",
                    "确定要删除页面 \"" + currentPageName + "\" 吗？\n此操作不可撤销。");
                if (confirmed) {
                    boolean deleted = projectService.deletePage(currentPageName);
                    if (deleted) {
                        updateProjectDisplay();
                        updateStatus("页面删除完成: " + currentPageName);
                    } else {
                        showWarningDialog("删除失败", "无法删除页面，可能是最后一个页面");
                    }
                }
            } else {
                showWarningDialog("删除失败", "没有选中的页面");
            }
        } catch (Exception e) {
            logger.error("删除页面失败", e);
            showErrorDialog("删除失败", "删除页面时发生错误: " + e.getMessage());
        }
    }

    private void handleRenamePage() {
        logger.info("处理重命名页面");
        try {
            if (currentPageName != null) {
                String newName = showInputDialog("重命名页面", "请输入新的页面名称:", currentPageName);
                if (newName != null && !newName.trim().isEmpty() && !newName.equals(currentPageName)) {
                    boolean renamed = projectService.renamePage(currentPageName, newName.trim());
                    if (renamed) {
                        currentPageName = newName.trim();
                        updateProjectDisplay();
                        updateStatus("页面重命名完成: " + newName);
                    } else {
                        showWarningDialog("重命名失败", "页面名称已存在或重命名失败");
                    }
                }
            } else {
                showWarningDialog("重命名失败", "没有选中的页面");
            }
        } catch (Exception e) {
            logger.error("重命名页面失败", e);
            showErrorDialog("重命名失败", "重命名页面时发生错误: " + e.getMessage());
        }
    }

    // 其他事件处理方法的简单实现
    private void handleExit() { Platform.exit(); }
    private void handleUndo() { showInfoDialog("功能提示", "撤销功能正在开发中"); }
    private void handleRedo() { showInfoDialog("功能提示", "重做功能正在开发中"); }
    private void handleCopy() { showInfoDialog("功能提示", "复制功能正在开发中"); }
    private void handlePaste() { showInfoDialog("功能提示", "粘贴功能正在开发中"); }
    private void handleDelete() { showInfoDialog("功能提示", "删除功能正在开发中"); }
    private void handleCopyPage() { showInfoDialog("功能提示", "复制页面功能正在开发中"); }
    private void handleRefresh() { updateProjectDisplay(); updateStatus("界面已刷新"); }
    private void handleToggleFullscreen() { showInfoDialog("功能提示", "全屏功能正在开发中"); }
    private void handleZoom() { showInfoDialog("功能提示", "缩放功能正在开发中"); }
    private void handleSettings() { showInfoDialog("功能提示", "设置功能正在开发中"); }
    private void handleThemeSettings() { showInfoDialog("功能提示", "主题设置功能正在开发中"); }
    private void handleBackupManager() { showInfoDialog("功能提示", "备份管理功能正在开发中"); }
    private void handleDataValidation() {
        boolean valid = projectService.validateProjectIntegrity();
        showInfoDialog("数据验证", valid ? "项目数据完整性验证通过" : "项目数据存在问题，建议修复");
    }
    private void handleUserManual() { showInfoDialog("功能提示", "用户手册功能正在开发中"); }
    private void handleShortcuts() { showInfoDialog("功能提示", "快捷键功能正在开发中"); }
    private void handleAbout() {
        showInfoDialog("关于", "跨平台中控系统 v1.0.0\n基于JavaFX + Gluon Mobile\n支持Windows、macOS、Linux、Android、iOS");
    }

    // 工具方法

    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private String showInputDialog(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        logger.info("清理主视图控制器资源");
        // 清理资源...
    }
}
