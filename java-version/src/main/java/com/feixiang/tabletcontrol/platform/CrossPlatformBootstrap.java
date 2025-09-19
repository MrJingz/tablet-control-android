package com.feixiang.tabletcontrol.platform;

import com.feixiang.tabletcontrol.service.UnifiedDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 跨平台启动管理器
 * 负责应用程序的跨平台启动和USERDATA检测
 */
public class CrossPlatformBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformBootstrap.class);
    
    private final CrossPlatformPathManager pathManager;
    private final ProjectPackageManager packageManager;
    private UnifiedDataService dataService;
    
    public CrossPlatformBootstrap() {
        this.pathManager = new CrossPlatformPathManager();
        this.packageManager = new ProjectPackageManager(pathManager);
    }
    
    public CrossPlatformBootstrap(String customPath) {
        this.pathManager = new CrossPlatformPathManager(customPath);
        this.packageManager = new ProjectPackageManager(pathManager);
    }
    
    /**
     * 启动应用程序
     */
    public CompletableFuture<Boolean> bootstrap() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("开始跨平台启动流程...");
                
                // 1. 检测USERDATA完整性
                if (!checkUserdataIntegrity()) {
                    return false;
                }
                
                // 2. 初始化数据服务
                if (!initializeDataService()) {
                    return false;
                }
                
                // 3. 启动自动重载监控
                packageManager.startAutoReloadWatcher();
                
                // 4. 平台特定初始化
                if (!performPlatformSpecificInitialization()) {
                    return false;
                }
                
                logger.info("跨平台启动完成");
                return true;
                
            } catch (Exception e) {
                logger.error("启动过程中发生异常", e);
                showErrorDialog("启动失败", "应用程序启动过程中发生错误: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * 检查USERDATA完整性
     */
    private boolean checkUserdataIntegrity() {
        logger.info("检查USERDATA完整性...");
        
        Path userdataPath = pathManager.getUserdataPath();
        
        // 检查USERDATA是否存在
        if (!Files.exists(userdataPath)) {
            logger.warn("USERDATA不存在: {}", userdataPath);
            return handleMissingUserdata();
        }
        
        // 验证完整性
        if (!pathManager.validateUserDataIntegrity()) {
            logger.warn("USERDATA完整性验证失败");
            return handleCorruptedUserdata();
        }
        
        // 验证项目包
        ProjectPackageManager.ValidationResult validation = 
            packageManager.validateProjectPackage(userdataPath);
        
        if (!validation.isValid()) {
            logger.warn("项目包验证失败: {}", validation.getErrors());
            return handleInvalidProjectPackage(validation);
        }
        
        if (!validation.getWarnings().isEmpty()) {
            logger.info("项目包验证警告: {}", validation.getWarnings());
            showWarningDialog("项目包警告", String.join("\n", validation.getWarnings()));
        }
        
        logger.info("USERDATA完整性检查通过");
        return true;
    }
    
    /**
     * 处理缺失的USERDATA
     */
    private boolean handleMissingUserdata() {
        logger.info("处理缺失的USERDATA...");
        
        // 根据平台显示不同的处理方式
        switch (pathManager.getCurrentPlatform()) {
            case WINDOWS:
                return handleMissingUserdataWindows();
            case ANDROID:
                return handleMissingUserdataAndroid();
            case IOS:
                return handleMissingUserdataIOS();
            case HARMONYOS:
                return handleMissingUserdataHarmonyOS();
            default:
                return createDefaultUserdata();
        }
    }
    
    /**
     * Windows平台处理缺失USERDATA
     */
    private boolean handleMissingUserdataWindows() {
        String[] options = {"创建新项目", "导入现有项目", "退出"};
        int choice = showOptionDialog(
            "USERDATA不存在",
            "未找到项目数据文件夹，请选择操作：",
            options
        );
        
        switch (choice) {
            case 0: // 创建新项目
                return createDefaultUserdata();
            case 1: // 导入现有项目
                return importExistingProject();
            default: // 退出
                return false;
        }
    }
    
    /**
     * Android平台处理缺失USERDATA
     */
    private boolean handleMissingUserdataAndroid() {
        // Android平台的特殊处理逻辑
        logger.info("Android平台：尝试从外部存储导入或创建新项目");
        
        // 检查外部存储是否有USERDATA
        // 这里需要Android特定的API调用
        
        return createDefaultUserdata();
    }
    
    /**
     * iOS平台处理缺失USERDATA
     */
    private boolean handleMissingUserdataIOS() {
        // iOS平台的特殊处理逻辑
        logger.info("iOS平台：检查iTunes文件共享或iCloud同步");
        
        return createDefaultUserdata();
    }
    
    /**
     * HarmonyOS平台处理缺失USERDATA
     */
    private boolean handleMissingUserdataHarmonyOS() {
        // HarmonyOS平台的特殊处理逻辑
        logger.info("HarmonyOS平台：检查分布式文件系统");
        
        return createDefaultUserdata();
    }
    
    /**
     * 创建默认USERDATA
     */
    private boolean createDefaultUserdata() {
        logger.info("创建默认USERDATA结构...");
        
        if (pathManager.initializeUserDataStructure()) {
            showInfoDialog("项目创建成功", "已创建新的项目数据文件夹");
            return true;
        } else {
            showErrorDialog("创建失败", "无法创建项目数据文件夹");
            return false;
        }
    }
    
    /**
     * 导入现有项目
     */
    private boolean importExistingProject() {
        logger.info("导入现有项目...");
        
        // 显示文件选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择USERDATA文件夹");
        
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path selectedPath = fileChooser.getSelectedFile().toPath();
            
            // 验证选择的文件夹
            ProjectPackageManager.ValidationResult validation = 
                packageManager.validateProjectPackage(selectedPath);
            
            if (!validation.isValid()) {
                showErrorDialog("导入失败", "选择的文件夹不是有效的项目包:\n" + 
                               String.join("\n", validation.getErrors()));
                return false;
            }
            
            // 复制项目包
            CompletableFuture<Boolean> importResult = 
                packageManager.replaceProjectPackage(selectedPath);
            
            try {
                if (importResult.get()) {
                    showInfoDialog("导入成功", "项目包导入完成");
                    return true;
                } else {
                    showErrorDialog("导入失败", "项目包导入过程中发生错误");
                    return false;
                }
            } catch (Exception e) {
                logger.error("导入项目包失败", e);
                showErrorDialog("导入失败", "导入过程中发生异常: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 处理损坏的USERDATA
     */
    private boolean handleCorruptedUserdata() {
        String[] options = {"尝试修复", "重新创建", "退出"};
        int choice = showOptionDialog(
            "项目数据损坏",
            "项目数据文件夹结构不完整，请选择操作：",
            options
        );
        
        switch (choice) {
            case 0: // 尝试修复
                return repairUserdata();
            case 1: // 重新创建
                return recreateUserdata();
            default: // 退出
                return false;
        }
    }
    
    /**
     * 修复USERDATA
     */
    private boolean repairUserdata() {
        logger.info("尝试修复USERDATA...");
        
        if (pathManager.initializeUserDataStructure()) {
            showInfoDialog("修复成功", "项目数据结构已修复");
            return true;
        } else {
            showErrorDialog("修复失败", "无法修复项目数据结构");
            return false;
        }
    }
    
    /**
     * 重新创建USERDATA
     */
    private boolean recreateUserdata() {
        logger.info("重新创建USERDATA...");
        
        try {
            // 备份现有数据
            Path backupPath = pathManager.getBackupPath().resolve("corrupted_backup_" + 
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Files.createDirectories(backupPath.getParent());
            Files.move(pathManager.getUserdataPath(), backupPath);
            
            // 创建新的USERDATA
            if (pathManager.initializeUserDataStructure()) {
                showInfoDialog("重建成功", 
                    "项目数据已重新创建\n原数据已备份到: " + backupPath);
                return true;
            } else {
                showErrorDialog("重建失败", "无法重新创建项目数据");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("重新创建USERDATA失败", e);
            showErrorDialog("重建失败", "重建过程中发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 处理无效的项目包
     */
    private boolean handleInvalidProjectPackage(ProjectPackageManager.ValidationResult validation) {
        String errorMessage = "项目包验证失败:\n" + String.join("\n", validation.getErrors());
        
        String[] options = {"尝试修复", "重新导入", "退出"};
        int choice = showOptionDialog("项目包无效", errorMessage, options);
        
        switch (choice) {
            case 0: // 尝试修复
                return repairUserdata();
            case 1: // 重新导入
                return importExistingProject();
            default: // 退出
                return false;
        }
    }
    
    /**
     * 初始化数据服务
     */
    private boolean initializeDataService() {
        try {
            logger.info("初始化统一数据服务...");
            
            dataService = new UnifiedDataService(pathManager.getUserdataPath().toString());
            dataService.initializeAsync().get();
            
            logger.info("数据服务初始化完成");
            return true;
            
        } catch (Exception e) {
            logger.error("数据服务初始化失败", e);
            showErrorDialog("初始化失败", "数据服务初始化失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 平台特定初始化
     */
    private boolean performPlatformSpecificInitialization() {
        logger.info("执行平台特定初始化: {}", pathManager.getCurrentPlatform());
        
        switch (pathManager.getCurrentPlatform()) {
            case WINDOWS:
                return initializeWindows();
            case ANDROID:
                return initializeAndroid();
            case IOS:
                return initializeIOS();
            case HARMONYOS:
                return initializeHarmonyOS();
            default:
                return true;
        }
    }
    
    private boolean initializeWindows() {
        // Windows特定初始化
        logger.info("Windows平台初始化完成");
        return true;
    }
    
    private boolean initializeAndroid() {
        // Android特定初始化
        logger.info("Android平台初始化完成");
        return true;
    }
    
    private boolean initializeIOS() {
        // iOS特定初始化
        logger.info("iOS平台初始化完成");
        return true;
    }
    
    private boolean initializeHarmonyOS() {
        // HarmonyOS特定初始化
        logger.info("HarmonyOS平台初始化完成");
        return true;
    }
    
    // UI辅助方法
    private void showErrorDialog(String title, String message) {
        if (GraphicsEnvironment.isHeadless()) {
            logger.error("{}: {}", title, message);
        } else {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showWarningDialog(String title, String message) {
        if (GraphicsEnvironment.isHeadless()) {
            logger.warn("{}: {}", title, message);
        } else {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showInfoDialog(String title, String message) {
        if (GraphicsEnvironment.isHeadless()) {
            logger.info("{}: {}", title, message);
        } else {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private int showOptionDialog(String title, String message, String[] options) {
        if (GraphicsEnvironment.isHeadless()) {
            logger.info("{}: {} (自动选择第一个选项)", title, message);
            return 0;
        } else {
            return JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        }
    }
    
    // Getter方法
    public CrossPlatformPathManager getPathManager() { return pathManager; }
    public ProjectPackageManager getPackageManager() { return packageManager; }
    public UnifiedDataService getDataService() { return dataService; }
}
