package com.feixiang.tabletcontrol;

import com.feixiang.tabletcontrol.core.service.ProjectService;
import com.feixiang.tabletcontrol.core.service.impl.ProjectServiceImpl;
import com.feixiang.tabletcontrol.core.repository.ProjectRepository;
import com.feixiang.tabletcontrol.core.repository.impl.JsonProjectRepository;
import com.feixiang.tabletcontrol.core.model.ProjectData;
import com.feixiang.tabletcontrol.platform.PlatformManager;
import com.feixiang.tabletcontrol.platform.CrossPlatformPathManager;
import com.feixiang.tabletcontrol.ui.MainViewController;
import com.feixiang.tabletcontrol.ui.ResponsiveLayoutManager;
import com.feixiang.tabletcontrol.ui.theme.ThemeManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 跨平台主应用程序类
 * 支持 Windows、macOS、Linux、Android、iOS
 */
public class CrossPlatformApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformApp.class);
    
    // 核心组件
    private PlatformManager platformManager;
    private CrossPlatformPathManager pathManager;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private MainViewController mainViewController;
    private ResponsiveLayoutManager layoutManager;
    private ThemeManager themeManager;
    
    // 应用状态
    private boolean isInitialized = false;
    private long startTime;
    
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("user.language", "zh");
        System.setProperty("user.country", "CN");
        
        // 启动 JavaFX 应用
        launch(args);
    }
    
    @Override
    public void init() throws Exception {
        super.init();
        startTime = System.currentTimeMillis();
        logger.info("=== 跨平台中控应用程序初始化开始 ===");
        
        try {
            // 初始化平台管理器
            initializePlatformManager();
            
            // 初始化路径管理器
            initializePathManager();
            
            // 初始化数据层
            initializeDataLayer();
            
            // 初始化业务层
            initializeBusinessLayer();
            
            // 初始化UI管理器
            initializeUIManagers();
            
            this.isInitialized = true;
            long duration = System.currentTimeMillis() - startTime;
            logger.info("应用程序初始化完成，耗时: {}ms", duration);
            
        } catch (Exception e) {
            logger.error("应用程序初始化失败", e);
            throw e;
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!isInitialized) {
            showErrorAndExit("初始化失败", "应用程序初始化未完成，无法启动");
            return;
        }
        
        try {
            logger.info("启动跨平台用户界面");
            
            // 设置主舞台
            setupPrimaryStage(primaryStage);
            
            // 创建响应式场景
            Scene scene = createResponsiveScene(primaryStage);
            primaryStage.setScene(scene);
            
            // 显示主界面
            primaryStage.show();
            
            // 加载项目数据
            loadProjectData();
            
            // 记录启动完成
            long totalDuration = System.currentTimeMillis() - startTime;
            logger.info("=== 跨平台应用程序启动完成，总耗时: {}ms ===", totalDuration);
            
            // 记录平台信息
            logPlatformInfo();
            
        } catch (Exception e) {
            logger.error("应用程序启动失败", e);
            showErrorAndExit("启动失败", "应用程序启动时发生错误: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        logger.info("应用程序正在关闭...");
        
        try {
            // 保存当前状态
            if (projectService != null && projectService.hasUnsavedChanges()) {
                projectService.saveCurrentProject();
                logger.info("已保存未保存的更改");
            }
            
            // 清理资源
            cleanup();
            
            logger.info("应用程序关闭完成");
            
        } catch (Exception e) {
            logger.error("应用程序关闭时发生错误", e);
        } finally {
            super.stop();
        }
    }
    
    /**
     * 初始化平台管理器
     */
    private void initializePlatformManager() {
        logger.debug("初始化平台管理器");
        this.platformManager = new PlatformManager();
        logger.info("当前平台: {}", platformManager.getCurrentPlatform());
    }
    
    /**
     * 初始化路径管理器
     */
    private void initializePathManager() {
        logger.debug("初始化路径管理器");
        this.pathManager = new CrossPlatformPathManager(platformManager);
        
        // 确保用户数据目录存在
        String userdataPath = pathManager.getDataDirectory();
        File userdataDir = new File(userdataPath);
        if (!userdataDir.exists()) {
            boolean created = userdataDir.mkdirs();
            if (created) {
                logger.info("创建用户数据目录: {}", userdataPath);
            } else {
                logger.warn("无法创建用户数据目录: {}", userdataPath);
            }
        }
        
        logger.info("用户数据目录: {}", userdataPath);
    }
    
    /**
     * 初始化数据层
     */
    private void initializeDataLayer() {
        logger.debug("初始化数据访问层");
        
        this.projectRepository = new JsonProjectRepository(pathManager);
        
        logger.info("项目数据存储库初始化完成");
    }
    
    /**
     * 初始化业务层
     */
    private void initializeBusinessLayer() {
        logger.debug("初始化业务服务层");
        
        this.projectService = new ProjectServiceImpl(projectRepository);
        
        logger.info("业务服务层初始化完成");
    }
    
    /**
     * 初始化UI管理器
     */
    private void initializeUIManagers() {
        logger.debug("初始化UI管理器");
        
        this.layoutManager = new ResponsiveLayoutManager(platformManager);
        
        logger.info("UI管理器初始化完成");
    }
    
    /**
     * 设置主舞台
     */
    private void setupPrimaryStage(Stage primaryStage) {
        primaryStage.setTitle("平板中控系统 - 跨平台版");
        
        // 根据平台设置窗口属性
        if (platformManager.isMobile()) {
            // 移动端：全屏显示
            primaryStage.setMaximized(true);
            primaryStage.setResizable(false);
        } else {
            // 桌面端：可调整大小
            primaryStage.setWidth(1024);
            primaryStage.setHeight(768);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
        }
        
        // 设置关闭行为
        primaryStage.setOnCloseRequest(event -> {
            logger.info("用户请求关闭应用程序");
            Platform.exit();
        });
    }
    
    /**
     * 创建响应式场景
     */
    private Scene createResponsiveScene(Stage primaryStage) throws Exception {
        logger.debug("创建响应式用户界面");
        
        // 初始化主题管理器
        this.themeManager = new ThemeManager(platformManager);

        // 创建主视图控制器
        this.mainViewController = new MainViewController(projectService, platformManager, themeManager);
        
        // 获取根节点
        Parent rootNode = mainViewController.getRootNode();
        
        // 创建场景
        Scene scene = new Scene(rootNode);
        
        // 应用响应式布局
        layoutManager.applyResponsiveLayout(scene, primaryStage);
        
        // 加载样式表
        loadStylesheets(scene);
        
        return scene;
    }
    
    /**
     * 加载样式表
     */
    private void loadStylesheets(Scene scene) {
        try {
            // 加载通用样式
            String commonCss = getClass().getResource("/css/common.css").toExternalForm();
            scene.getStylesheets().add(commonCss);
            
            // 根据平台加载特定样式
            String platformCss;
            if (platformManager.isMobile()) {
                platformCss = getClass().getResource("/css/mobile.css").toExternalForm();
            } else {
                platformCss = getClass().getResource("/css/desktop.css").toExternalForm();
            }
            scene.getStylesheets().add(platformCss);
            
            logger.debug("样式表加载完成");
            
        } catch (Exception e) {
            logger.warn("加载样式表失败: {}", e.getMessage());
        }
    }
    
    /**
     * 加载项目数据
     */
    private void loadProjectData() {
        try {
            logger.info("加载项目数据");
            
            ProjectData projectData = projectService.loadProject();
            mainViewController.displayProject(projectData);
            
            logger.info("项目数据加载完成");
            
        } catch (Exception e) {
            logger.error("加载项目数据失败", e);
            showError("数据加载失败", "无法加载项目数据: " + e.getMessage());
        }
    }
    
    /**
     * 记录平台信息
     */
    private void logPlatformInfo() {
        logger.info("=== 平台信息 ===");
        logger.info("操作系统: {}", System.getProperty("os.name"));
        logger.info("Java版本: {}", System.getProperty("java.version"));
        logger.info("JavaFX版本: {}", System.getProperty("javafx.version", "内置"));
        logger.info("当前平台: {}", platformManager.getCurrentPlatform());
        logger.info("是否移动端: {}", platformManager.isMobile());
        logger.info("用户数据目录: {}", pathManager.getDataDirectory());
        logger.info("===============");
    }
    
    /**
     * 显示错误对话框并退出
     */
    private void showErrorAndExit(String title, String message) {
        logger.error("{}: {}", title, message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });
    }
    
    /**
     * 显示错误对话框
     */
    private void showError(String title, String message) {
        logger.error("{}: {}", title, message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        // 清理各种资源
        if (mainViewController != null) {
            mainViewController.cleanup();
        }
        
        // 其他清理工作...
    }
}
