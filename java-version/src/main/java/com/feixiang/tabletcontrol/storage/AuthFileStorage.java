package com.feixiang.tabletcontrol.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 基于文件的授权码存储类
 * 从Python版本的auth_file_storage.py转换而来
 */
public class AuthFileStorage {
    private static final Logger logger = LoggerFactory.getLogger(AuthFileStorage.class);
    
    private static final String AUTH_CODES_FILE = "auth_codes.json";
    private static final String BACKUP_FILE = "auth_codes.json.backup";
    
    private final ObjectMapper objectMapper;
    private Map<String, Map<String, Object>> authCodes;
    
    public AuthFileStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.authCodes = new HashMap<>();
        
        // 加载现有的授权码数据
        loadAuthCodes();
    }
    
    /**
     * 保存授权码
     * 
     * @param authCode 授权码
     * @param data 授权码数据
     * @return 是否保存成功
     */
    public boolean saveAuthCode(String authCode, Map<String, Object> data) {
        try {
            // 添加时间戳
            data.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 保存到内存
            this.authCodes.put(authCode, new HashMap<>(data));
            
            // 持久化到文件
            return saveToFile();
            
        } catch (Exception e) {
            logger.error("保存授权码失败: {}", authCode, e);
            return false;
        }
    }
    
    /**
     * 获取授权码数据
     * 
     * @param authCode 授权码
     * @return 授权码数据，如果不存在返回null
     */
    public Map<String, Object> getAuthCode(String authCode) {
        Map<String, Object> data = this.authCodes.get(authCode);
        return data != null ? new HashMap<>(data) : null;
    }
    
    /**
     * 删除授权码
     * 
     * @param authCode 授权码
     * @return 是否删除成功
     */
    public boolean deleteAuthCode(String authCode) {
        try {
            boolean existed = this.authCodes.containsKey(authCode);
            this.authCodes.remove(authCode);
            
            if (existed) {
                saveToFile();
                logger.info("授权码已删除: {}", authCode);
            }
            
            return existed;
            
        } catch (Exception e) {
            logger.error("删除授权码失败: {}", authCode, e);
            return false;
        }
    }
    
    /**
     * 获取所有授权码
     * 
     * @return 所有授权码的副本
     */
    public Map<String, Map<String, Object>> getAllAuthCodes() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : this.authCodes.entrySet()) {
            result.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return result;
    }
    
    /**
     * 清理过期的授权码
     * 
     * @return 清理的数量
     */
    public int cleanupExpiredCodes() {
        int cleanedCount = 0;
        LocalDateTime now = LocalDateTime.now();
        
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = this.authCodes.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            Map<String, Object> data = entry.getValue();
            
            // 检查是否为永久授权码
            Boolean isPermanent = (Boolean) data.get("is_permanent");
            if (isPermanent != null && isPermanent) {
                continue; // 跳过永久授权码
            }
            
            // 检查过期时间
            String expiresAtStr = (String) data.get("expires_at");
            if (expiresAtStr != null) {
                try {
                    LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    if (now.isAfter(expiresAt)) {
                        iterator.remove();
                        cleanedCount++;
                        logger.info("清理过期授权码: {}", entry.getKey());
                    }
                } catch (Exception e) {
                    logger.warn("解析过期时间失败，删除异常授权码: {}", entry.getKey());
                    iterator.remove();
                    cleanedCount++;
                }
            }
        }
        
        if (cleanedCount > 0) {
            saveToFile();
        }
        
        return cleanedCount;
    }
    
    /**
     * 获取存储信息
     * 
     * @return 存储统计信息
     */
    public Map<String, Object> getStorageInfo() {
        Map<String, Object> info = new HashMap<>();
        
        int totalCodes = this.authCodes.size();
        int activeCodes = 0;
        int permanentCodes = 0;
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Map<String, Object> data : this.authCodes.values()) {
            Boolean isPermanent = (Boolean) data.get("is_permanent");
            if (isPermanent != null && isPermanent) {
                permanentCodes++;
                activeCodes++; // 永久授权码总是活跃的
            } else {
                // 检查是否过期
                String expiresAtStr = (String) data.get("expires_at");
                if (expiresAtStr != null) {
                    try {
                        LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        if (now.isBefore(expiresAt) || now.isEqual(expiresAt)) {
                            activeCodes++;
                        }
                    } catch (Exception e) {
                        // 解析失败，认为已过期
                    }
                }
            }
        }
        
        info.put("total_codes", totalCodes);
        info.put("active_codes", activeCodes);
        info.put("permanent_codes", permanentCodes);
        info.put("expired_codes", totalCodes - activeCodes);
        info.put("storage_file", AUTH_CODES_FILE);
        
        return info;
    }
    
    /**
     * 从文件加载授权码数据
     */
    private void loadAuthCodes() {
        File authFile = new File(AUTH_CODES_FILE);
        
        if (authFile.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> loadedCodes = objectMapper.readValue(authFile, Map.class);
                this.authCodes = loadedCodes != null ? loadedCodes : new HashMap<>();
                
                logger.info("从文件加载授权码数据: {} 条记录", this.authCodes.size());
                
            } catch (IOException e) {
                logger.error("加载授权码文件失败，尝试从备份文件恢复", e);
                loadFromBackup();
            }
        } else {
            logger.info("授权码文件不存在，创建新的存储");
            this.authCodes = new HashMap<>();
        }
    }
    
    /**
     * 从备份文件加载数据
     */
    private void loadFromBackup() {
        File backupFile = new File(BACKUP_FILE);
        
        if (backupFile.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> loadedCodes = objectMapper.readValue(backupFile, Map.class);
                this.authCodes = loadedCodes != null ? loadedCodes : new HashMap<>();
                
                logger.info("从备份文件恢复授权码数据: {} 条记录", this.authCodes.size());
                
                // 恢复后立即保存到主文件
                saveToFile();
                
            } catch (IOException e) {
                logger.error("从备份文件恢复失败，使用空数据", e);
                this.authCodes = new HashMap<>();
            }
        } else {
            logger.warn("备份文件也不存在，使用空数据");
            this.authCodes = new HashMap<>();
        }
    }
    
    /**
     * 保存数据到文件
     * 
     * @return 是否保存成功
     */
    private boolean saveToFile() {
        try {
            // 先创建备份
            File authFile = new File(AUTH_CODES_FILE);
            if (authFile.exists()) {
                File backupFile = new File(BACKUP_FILE);
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                authFile.renameTo(backupFile);
            }
            
            // 保存到主文件
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(new File(AUTH_CODES_FILE), this.authCodes);
            
            logger.debug("授权码数据已保存到文件: {} 条记录", this.authCodes.size());
            return true;
            
        } catch (IOException e) {
            logger.error("保存授权码文件失败", e);
            return false;
        }
    }
}