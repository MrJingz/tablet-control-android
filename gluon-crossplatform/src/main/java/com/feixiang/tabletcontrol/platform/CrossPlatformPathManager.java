package com.feixiang.tabletcontrol.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 跨平台路径管理器
 * 处理不同平台的文件系统路径差异，提供统一的路径管理接口
 */
public class CrossPlatformPathManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformPathManager.class);
    
    private final PlatformManager platformManager;
    
    // 应用相关路径
    private final String appName = "TabletControl";
    private final String dataDirectoryName = "USERDATA";
    private final String configDirectoryName = "config";
    private final String logDirectoryName = "logs";
    private final String tempDirectoryName = "temp";
    private final String backupDirectoryName = "backups";
    
    // 缓存的路径
    private String dataDirectory;
    private String configDirectory;
    private String logDirectory;
    private String tempDirectory;
    private String backupDirectory;
    private String userHomeDirectory;
    private String appDataDirectory;
    
    public CrossPlatformPathManager(PlatformManager platformManager) {
        this.platformManager = platformManager;
        initializePaths();
        createDirectories();
        logger.info("跨平台路径管理器初始化完成");
    }
    
    /**
     * 初始化所有路径
     */
    private void initializePaths() {
        logger.info("初始化平台路径，当前平台: {}", platformManager.getCurrentPlatform());
        
        // 获取用户主目录
        userHomeDirectory = System.getProperty("user.home");
        logger.debug("用户主目录: {}", userHomeDirectory);
        
        // 根据平台确定应用数据目录
        appDataDirectory = determineAppDataDirectory();
        logger.debug("应用数据目录: {}", appDataDirectory);
        
        // 初始化各个子目录
        dataDirectory = appDataDirectory + File.separator + dataDirectoryName;
        configDirectory = appDataDirectory + File.separator + configDirectoryName;
        logDirectory = appDataDirectory + File.separator + logDirectoryName;
        tempDirectory = appDataDirectory + File.separator + tempDirectoryName;
        backupDirectory = dataDirectory + File.separator + backupDirectoryName;
        
        logger.info("路径初始化完成:");
        logger.info("  数据目录: {}", dataDirectory);
        logger.info("  配置目录: {}", configDirectory);
        logger.info("  日志目录: {}", logDirectory);
        logger.info("  临时目录: {}", tempDirectory);
        logger.info("  备份目录: {}", backupDirectory);
    }
    
    /**
     * 根据平台确定应用数据目录
     */
    private String determineAppDataDirectory() {
        switch (platformManager.getCurrentPlatform()) {
            case WINDOWS:
                // Windows: %APPDATA%\AppName 或 %USERPROFILE%\AppData\Roaming\AppName
                String appData = System.getenv("APPDATA");
                if (appData != null && !appData.isEmpty()) {
                    return appData + File.separator + appName;
                } else {
                    return userHomeDirectory + File.separator + "AppData" + File.separator + "Roaming" + File.separator + appName;
                }
                
            case MACOS:
                // macOS: ~/Library/Application Support/AppName
                return userHomeDirectory + File.separator + "Library" + File.separator + "Application Support" + File.separator + appName;
                
            case LINUX:
                // Linux: ~/.config/AppName 或 $XDG_CONFIG_HOME/AppName
                String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
                if (xdgConfigHome != null && !xdgConfigHome.isEmpty()) {
                    return xdgConfigHome + File.separator + appName;
                } else {
                    return userHomeDirectory + File.separator + ".config" + File.separator + appName;
                }
                
            case ANDROID:
                // Android: 使用应用私有目录
                // 注意：在实际Android环境中，这个路径会被Gluon Mobile框架处理
                return "/data/data/com.feixiang.tabletcontrol/files";
                
            case IOS:
                // iOS: 使用应用沙盒目录
                // 注意：在实际iOS环境中，这个路径会被Gluon Mobile框架处理
                return System.getProperty("user.home") + File.separator + "Documents";
                
            default:
                // 默认使用用户主目录下的隐藏文件夹
                return userHomeDirectory + File.separator + "." + appName.toLowerCase();
        }
    }
    
    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        logger.info("创建必要的目录结构");
        
        String[] directories = {
            appDataDirectory,
            dataDirectory,
            configDirectory,
            logDirectory,
            tempDirectory,
            backupDirectory
        };
        
        for (String directory : directories) {
            createDirectoryIfNotExists(directory);
        }
        
        logger.info("目录结构创建完成");
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.debug("创建目录: {}", directoryPath);
            } else {
                logger.debug("目录已存在: {}", directoryPath);
            }
        } catch (Exception e) {
            logger.error("创建目录失败: {}", directoryPath, e);
            throw new RuntimeException("无法创建目录: " + directoryPath, e);
        }
    }
    
    // 公共访问方法
    
    /**
     * 获取数据目录路径
     */
    public String getDataDirectory() {
        return dataDirectory;
    }
    
    /**
     * 获取配置目录路径
     */
    public String getConfigDirectory() {
        return configDirectory;
    }
    
    /**
     * 获取日志目录路径
     */
    public String getLogDirectory() {
        return logDirectory;
    }
    
    /**
     * 获取临时目录路径
     */
    public String getTempDirectory() {
        return tempDirectory;
    }
    
    /**
     * 获取备份目录路径
     */
    public String getBackupDirectory() {
        return backupDirectory;
    }
    
    /**
     * 获取用户主目录路径
     */
    public String getUserHomeDirectory() {
        return userHomeDirectory;
    }
    
    /**
     * 获取应用数据目录路径
     */
    public String getAppDataDirectory() {
        return appDataDirectory;
    }
    
    /**
     * 构建相对于数据目录的路径
     */
    public String getDataPath(String relativePath) {
        return dataDirectory + File.separator + relativePath;
    }
    
    /**
     * 构建相对于配置目录的路径
     */
    public String getConfigPath(String relativePath) {
        return configDirectory + File.separator + relativePath;
    }
    
    /**
     * 构建相对于日志目录的路径
     */
    public String getLogPath(String relativePath) {
        return logDirectory + File.separator + relativePath;
    }
    
    /**
     * 构建相对于临时目录的路径
     */
    public String getTempPath(String relativePath) {
        return tempDirectory + File.separator + relativePath;
    }
    
    /**
     * 构建相对于备份目录的路径
     */
    public String getBackupPath(String relativePath) {
        return backupDirectory + File.separator + relativePath;
    }
    
    /**
     * 规范化路径分隔符
     */
    public String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    }
    
    /**
     * 检查路径是否存在
     */
    public boolean pathExists(String path) {
        return Files.exists(Paths.get(path));
    }
    
    /**
     * 检查路径是否为目录
     */
    public boolean isDirectory(String path) {
        return Files.isDirectory(Paths.get(path));
    }
    
    /**
     * 检查路径是否为文件
     */
    public boolean isFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }
    
    /**
     * 获取文件大小
     */
    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (Exception e) {
            logger.warn("获取文件大小失败: {}", filePath, e);
            return -1;
        }
    }
    
    /**
     * 清理临时目录
     */
    public void cleanTempDirectory() {
        logger.info("清理临时目录: {}", tempDirectory);
        
        try {
            Path tempPath = Paths.get(tempDirectory);
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                            logger.debug("删除临时文件: {}", file);
                        } catch (Exception e) {
                            logger.warn("删除临时文件失败: {}", file, e);
                        }
                    });
            }
        } catch (Exception e) {
            logger.error("清理临时目录失败", e);
        }
    }
    
    /**
     * 获取路径信息摘要
     */
    public String getPathSummary() {
        return String.format("路径管理器 [平台: %s, 数据目录: %s, 配置目录: %s]",
                           platformManager.getCurrentPlatform().getDisplayName(),
                           dataDirectory,
                           configDirectory);
    }
}
