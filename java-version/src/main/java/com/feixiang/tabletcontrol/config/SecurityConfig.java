package com.feixiang.tabletcontrol.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全配置类
 * 存储加密后的密码哈希值和安全相关配置
 * 从Python版本的security_config.py转换而来
 */
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    private static final String CONFIG_FILE = "security_config.json";
    // 默认密码"123"的SHA-256哈希值
    private static final String DEFAULT_PASSWORD_HASH = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";
    private static final int MAX_ATTEMPTS = 3;
    
    private String passwordHash;
    private int maxAttempts;
    private int currentAttempts;
    private final ObjectMapper objectMapper;
    
    public SecurityConfig() {
        this.objectMapper = new ObjectMapper();
        this.passwordHash = DEFAULT_PASSWORD_HASH;
        this.maxAttempts = MAX_ATTEMPTS;
        this.currentAttempts = 0;
        
        // 加载配置文件
        loadConfig();
    }
    
    /**
     * 使用SHA-256加密密码
     * 
     * @param password 原始密码
     * @return SHA-256哈希值
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256算法不可用", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证输入的密码
     * 
     * @param inputPassword 输入的密码
     * @return 是否验证成功
     */
    public boolean verifyPassword(String inputPassword) {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(this.passwordHash);
    }
    
    /**
     * 增加尝试次数
     * 
     * @return 当前尝试次数
     */
    public int incrementAttempts() {
        this.currentAttempts++;
        return this.currentAttempts;
    }
    
    /**
     * 重置尝试次数
     */
    public void resetAttempts() {
        this.currentAttempts = 0;
    }
    
    /**
     * 检查是否已达到最大尝试次数
     * 
     * @return 是否被锁定
     */
    public boolean isLocked() {
        return this.currentAttempts >= this.maxAttempts;
    }
    
    /**
     * 获取剩余尝试次数
     * 
     * @return 剩余尝试次数
     */
    public int getRemainingAttempts() {
        return Math.max(0, this.maxAttempts - this.currentAttempts);
    }
    
    /**
     * 从配置文件加载设置
     */
    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = objectMapper.readValue(configFile, Map.class);
                
                if (config.containsKey("password_hash")) {
                    this.passwordHash = (String) config.get("password_hash");
                    logger.info("从配置文件加载密码哈希：{}", this.passwordHash);
                }
                
                if (config.containsKey("max_attempts")) {
                    this.maxAttempts = (Integer) config.get("max_attempts");
                }
                
                if (config.containsKey("current_attempts")) {
                    this.currentAttempts = (Integer) config.get("current_attempts");
                }
                
            } catch (IOException e) {
                logger.warn("加载配置文件失败，使用默认配置: {}", e.getMessage());
            }
        } else {
            logger.info("配置文件不存在，使用默认配置");
            saveConfig();
        }
    }
    
    /**
     * 保存配置到文件
     */
    public void saveConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("password_hash", this.passwordHash);
            config.put("max_attempts", this.maxAttempts);
            config.put("current_attempts", this.currentAttempts);
            
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(new File(CONFIG_FILE), config);
            
            logger.info("配置已保存到文件: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("保存配置文件失败", e);
        }
    }
    
    // Getters and Setters
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public int getCurrentAttempts() {
        return currentAttempts;
    }
    
    public void setCurrentAttempts(int currentAttempts) {
        this.currentAttempts = currentAttempts;
    }
    
    /**
     * 更新密码
     * 
     * @param newPassword 新密码
     */
    public void updatePassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
        this.currentAttempts = 0; // 重置尝试次数
        saveConfig(); // 保存到配置文件
        logger.info("密码已更新并保存到配置文件");
    }
}