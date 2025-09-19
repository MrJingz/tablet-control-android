package com.feixiang.tabletcontrol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * 统一数据服务
 * 提供跨平台数据管理功能
 */
public class UnifiedDataService {
    private static final Logger logger = LoggerFactory.getLogger(UnifiedDataService.class);
    
    private final String basePath;
    private boolean encryptionEnabled = false;
    private String currentPassword = null;
    
    public UnifiedDataService(String basePath) {
        this.basePath = basePath;
        logger.info("统一数据服务已创建，基础路径: {}", basePath);
    }
    
    /**
     * 异步初始化服务
     */
    public CompletableFuture<Void> initializeAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("开始异步初始化数据服务");
                
                // 确保基础目录存在
                Path baseDir = Paths.get(basePath);
                if (!Files.exists(baseDir)) {
                    Files.createDirectories(baseDir);
                }
                
                // 检查USERDATA目录
                Path userdataDir = baseDir.resolve("USERDATA");
                if (!Files.exists(userdataDir)) {
                    Files.createDirectories(userdataDir);
                    logger.info("创建USERDATA目录: {}", userdataDir);
                }
                
                // 检查加密状态
                checkEncryptionStatus();
                
                logger.info("数据服务初始化成功");
                
            } catch (Exception e) {
                logger.error("数据服务初始化失败", e);
                throw new RuntimeException("数据服务初始化失败", e);
            }
        });
    }
    
    /**
     * 检查加密状态
     */
    private void checkEncryptionStatus() {
        try {
            Path userdataDir = Paths.get(basePath, "USERDATA");
            Path projectDataFile = userdataDir.resolve("project_data.json");
            
            if (Files.exists(projectDataFile)) {
                // 读取文件前几个字节检查是否加密
                byte[] header = Files.readAllBytes(projectDataFile);
                if (header.length > 8) {
                    String headerStr = new String(header, 0, Math.min(8, header.length));
                    // 简单检查：如果不是JSON格式开头，可能是加密的
                    if (!headerStr.trim().startsWith("{")) {
                        encryptionEnabled = true;
                        logger.info("检测到加密的项目数据文件");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("检查加密状态时发生错误", e);
        }
    }
    
    /**
     * 设置密码
     */
    public boolean setPassword(String password) {
        try {
            if (password == null || password.trim().isEmpty()) {
                // 禁用加密
                this.currentPassword = null;
                this.encryptionEnabled = false;
                logger.info("加密已禁用");
                return true;
            } else {
                // 启用加密
                this.currentPassword = password;
                this.encryptionEnabled = true;
                logger.info("加密已启用");
                return true;
            }
        } catch (Exception e) {
            logger.error("设置密码失败", e);
            return false;
        }
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String password) {
        if (!encryptionEnabled) {
            return true; // 未启用加密时总是返回true
        }
        
        return currentPassword != null && currentPassword.equals(password);
    }
    
    /**
     * 检查是否启用加密
     */
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    /**
     * 获取基础路径
     */
    public String getBasePath() {
        return basePath;
    }
    
    /**
     * 获取USERDATA路径
     */
    public String getUserDataPath() {
        return Paths.get(basePath, "USERDATA").toString();
    }
    
    /**
     * 读取配置文件
     */
    public String readConfigFile(String fileName) throws IOException {
        Path configFile = Paths.get(basePath, "USERDATA", "config", fileName);
        if (!Files.exists(configFile)) {
            return null;
        }
        
        byte[] content = Files.readAllBytes(configFile);
        
        if (encryptionEnabled && isEncryptedFile(content)) {
            // 这里应该解密文件，简化版本直接返回
            logger.warn("文件已加密，需要解密: {}", fileName);
            return new String(content, "UTF-8");
        } else {
            return new String(content, "UTF-8");
        }
    }
    
    /**
     * 写入配置文件
     */
    public void writeConfigFile(String fileName, String content) throws IOException {
        Path configDir = Paths.get(basePath, "USERDATA", "config");
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
        
        Path configFile = configDir.resolve(fileName);
        
        if (encryptionEnabled) {
            // 这里应该加密文件，简化版本直接写入
            logger.warn("应该加密文件: {}", fileName);
        }
        
        Files.write(configFile, content.getBytes("UTF-8"));
    }
    
    /**
     * 检查文件是否已加密
     */
    private boolean isEncryptedFile(byte[] content) {
        if (content.length < 8) {
            return false;
        }
        
        // 简单检查：如果前8个字节不是可打印字符，可能是加密的
        String header = new String(content, 0, 8);
        return !header.trim().startsWith("{") && !header.trim().startsWith("[");
    }
    
    /**
     * 获取项目数据
     */
    public String getProjectData() {
        try {
            return readConfigFile("project_data.json");
        } catch (IOException e) {
            logger.error("读取项目数据失败", e);
            return null;
        }
    }
    
    /**
     * 保存项目数据
     */
    public boolean saveProjectData(String data) {
        try {
            writeConfigFile("project_data.json", data);
            return true;
        } catch (IOException e) {
            logger.error("保存项目数据失败", e);
            return false;
        }
    }
    
    /**
     * 获取应用设置
     */
    public String getSettings() {
        try {
            return readConfigFile("settings.json");
        } catch (IOException e) {
            logger.error("读取应用设置失败", e);
            return null;
        }
    }
    
    /**
     * 保存应用设置
     */
    public boolean saveSettings(String settings) {
        try {
            writeConfigFile("settings.json", settings);
            return true;
        } catch (IOException e) {
            logger.error("保存应用设置失败", e);
            return false;
        }
    }
    
    /**
     * 检查服务是否就绪
     */
    public boolean isReady() {
        Path userdataDir = Paths.get(basePath, "USERDATA");
        return Files.exists(userdataDir) && Files.isDirectory(userdataDir);
    }
    
    /**
     * 关闭服务
     */
    public void shutdown() {
        logger.info("统一数据服务正在关闭");
        // 清理资源
        currentPassword = null;
    }
}
