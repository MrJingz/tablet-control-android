package com.feixiang.tabletcontrol;

import com.feixiang.tabletcontrol.controller.MainController;
import com.feixiang.tabletcontrol.repository.ProjectRepository;
import com.feixiang.tabletcontrol.repository.impl.JsonProjectRepository;
import com.feixiang.tabletcontrol.service.ProjectService;
import com.feixiang.tabletcontrol.service.impl.ProjectServiceImpl;
import com.feixiang.tabletcontrol.view.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 重构后的平板中控应用程序主类
 * 采用Spring MVC架构模式，分离关注点
 */
public class TabletControlApplication {
    private static final Logger logger = LoggerFactory.getLogger(TabletControlApplication.class);
    
    // 应用程序组件
    private MainView mainView;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private MainController mainController;
    
    // 应用程序状态
    private boolean isRunning = false;
    private long startTime;
    
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("java.awt.headless", "false");
        System.setProperty("swing.aatext", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        
        // 设置Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("设置系统Look and Feel失败: {}", e.getMessage());
        }
        
        // 创建并启动应用程序
        SwingUtilities.invokeLater(() -> {
            try {
                TabletControlApplication app = new TabletControlApplication();
                app.start();
            } catch (Exception e) {
                logger.error("应用程序启动失败: {}", e.getMessage(), e);
                System.exit(1);
            }
        });
    }
    
    /**
     * 启动应用程序
     */
    public void start() {
        startTime = System.currentTimeMillis();
        logger.info("=== 平板中控应用程序启动 ===");
        
        try {
            // 初始化应用程序组件
            initializeComponents();
            
            // 启动应用程序
            startApplication();
            
            this.isRunning = true;
            long duration = System.currentTimeMillis() - startTime;
            logger.info("应用程序启动完成，耗时: {}ms", duration);
            
            // 记录系统信息
            logSystemInfo();
            
        } catch (Exception e) {
            logger.error("应用程序启动失败: {}", e.getMessage(), e);
            showErrorAndExit("启动失败", "应用程序启动时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 初始化应用程序组件
     */
    private void initializeComponents() {
        logger.info("初始化应用程序组件");
        
        // 1. 创建Repository层
        initializeRepository();
        
        // 2. 创建Service层
        initializeService();
        
        // 3. 创建View层
        initializeView();
        
        // 4. 创建Controller层
        initializeController();
        
        logger.info("应用程序组件初始化完成");
    }
    
    /**
     * 初始化Repository层
     */
    private void initializeRepository() {
        logger.debug("初始化Repository层");
        
        // 获取用户数据目录
        String userDataDir = getUserDataDir();
        String projectDataPath = userDataDir + File.separator + "project_data.json";
        
        // 创建JSON项目仓库
        this.projectRepository = new JsonProjectRepository(projectDataPath);
        
        logger.info("Repository层初始化完成，数据文件路径: {}", projectDataPath);
    }
    
    /**
     * 初始化Service层
     */
    private void initializeService() {
        logger.debug("初始化Service层");
        
        // 创建项目服务
        this.projectService = new ProjectServiceImpl(projectRepository);
        
        logger.info("Service层初始化完成");
    }
    
    /**
     * 初始化View层
     */
    private void initializeView() {
        logger.debug("初始化View层");
        
        // 创建主视图
        this.mainView = new MainView();
        
        logger.info("View层初始化完成");
    }
    
    /**
     * 初始化Controller层
     */
    private void initializeController() {
        logger.debug("初始化Controller层");
        
        // 创建主控制器
        this.mainController = new MainController(mainView, projectService);
        
        logger.info("Controller层初始化完成");
    }
    
    /**
     * 启动应用程序
     */
    private void startApplication() {
        logger.info("启动应用程序界面");
        
        // 显示主界面
        mainController.show();
        
        // 设置关闭钩子
        setupShutdownHook();
        
        logger.info("应用程序界面启动完成");
    }
    
    /**
     * 设置关闭钩子
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("应用程序正在关闭...");
            shutdown();
        }));
    }
    
    /**
     * 关闭应用程序
     */
    public void shutdown() {
        if (!isRunning) {
            return;
        }
        
        logger.info("开始关闭应用程序");
        long shutdownStart = System.currentTimeMillis();
        
        try {
            // 保存项目数据
            if (projectService.hasUnsavedChanges()) {
                logger.info("保存未保存的更改");
                projectService.saveProject(projectService.getCurrentProject());
            }
            
            // 隐藏界面
            if (mainController != null) {
                mainController.hide();
            }
            
            this.isRunning = false;
            long duration = System.currentTimeMillis() - shutdownStart;
            long totalRuntime = System.currentTimeMillis() - startTime;
            
            logger.info("应用程序关闭完成，关闭耗时: {}ms，总运行时间: {}ms", duration, totalRuntime);
            
        } catch (Exception e) {
            logger.error("应用程序关闭时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户数据目录
     */
    private String getUserDataDir() {
        String userDir = System.getProperty("user.dir");
        String userDataDir = userDir + File.separator + "USERDATA";
        
        // 确保目录存在
        File dir = new File(userDataDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            logger.info("创建用户数据目录: {}, 结果: {}", userDataDir, created);
        }
        
        return userDataDir;
    }
    
    /**
     * 记录系统信息
     */
    private void logSystemInfo() {
        logger.info("=== 系统信息 ===");
        logger.info("Java版本: {}", System.getProperty("java.version"));
        logger.info("操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        logger.info("用户目录: {}", System.getProperty("user.dir"));
        
        // 内存信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        logger.info("内存使用: {:.2f}MB / {:.2f}MB", 
                   usedMemory / 1024.0 / 1024.0, 
                   totalMemory / 1024.0 / 1024.0);
        
        // 屏幕信息
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        logger.info("屏幕尺寸: {}x{}", screenSize.width, screenSize.height);
        
        // 项目统计信息
        if (projectService != null) {
            logger.info("项目统计: {}", projectService.getProjectStatistics());
        }
        
        logger.info("=== 系统信息结束 ===");
    }
    
    /**
     * 显示错误消息并退出
     */
    private void showErrorAndExit(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        });
    }
    
    // Getter方法
    
    public MainView getMainView() { return mainView; }
    public ProjectRepository getProjectRepository() { return projectRepository; }
    public ProjectService getProjectService() { return projectService; }
    public MainController getMainController() { return mainController; }
    public boolean isRunning() { return isRunning; }
    public long getStartTime() { return startTime; }
}
