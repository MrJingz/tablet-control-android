package com.feixiang.tabletcontrol.auth;

import com.feixiang.tabletcontrol.storage.AuthFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 基于文件存储的授权码生成器类
 * 实现基于HMAC-SHA256的授权码生成和验证逻辑
 * 从Python版本的auth_generator_file.py转换而来
 */
public class AuthCodeGeneratorFile {
    private static final Logger logger = LoggerFactory.getLogger(AuthCodeGeneratorFile.class);
    
    private static final String DEFAULT_SECRET_KEY = "TABLET_CONTROL_2024_DEFAULT";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SALT_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{8}$");
    
    private final String secretKey;
    private final AuthFileStorage authStorage;
    private final SecureRandom secureRandom;
    
    public AuthCodeGeneratorFile() {
        this(DEFAULT_SECRET_KEY);
    }
    
    public AuthCodeGeneratorFile(String secretKey) {
        this.secretKey = secretKey != null ? secretKey : DEFAULT_SECRET_KEY;
        this.authStorage = new AuthFileStorage();
        this.secureRandom = new SecureRandom();
        
        logger.info("授权码生成器初始化完成，使用文件存储");
    }
    
    /**
     * 生成指定长度的随机盐值
     * 
     * @param length 盐值长度，默认6位
     * @return 随机盐值
     */
    public String generateSalt(int length) {
        if (length <= 0) {
            length = 6;
        }
        
        StringBuilder salt = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(SALT_CHARACTERS.length());
            salt.append(SALT_CHARACTERS.charAt(index));
        }
        
        return salt.toString();
    }
    
    /**
     * 验证日期字符串格式
     * 
     * @param dateStr 日期字符串，格式应为YYYYMMDD
     * @return 格式是否正确
     */
    private boolean verifyDateFormat(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return false;
        }
        
        if (!DATE_PATTERN.matcher(dateStr).matches()) {
            return false;
        }
        
        try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            
            // 基本范围检查
            if (year < 1900 || year > 9999) {
                return false;
            }
            if (month < 1 || month > 12) {
                return false;
            }
            if (day < 1 || day > 31) {
                return false;
            }
            
            // 尝试创建日期对象进行更严格的验证
            LocalDate.of(year, month, day);
            return true;
            
        } catch (NumberFormatException | DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * 生成授权码
     * 
     * @param dateStr 日期字符串，格式为YYYYMMDD
     * @param userId 关联主体ID，默认为"default_user"
     * @param isPermanent 是否为永久授权码，默认false
     * @return 生成结果，包含授权码和相关信息
     */
    public Map<String, Object> generateAuthCode(String dateStr, String userId, boolean isPermanent) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 验证日期格式
            if (!verifyDateFormat(dateStr)) {
                result.put("success", false);
                result.put("error_code", "INVALID_DATE_FORMAT");
                result.put("error_message", "日期格式不正确，必须为YYYYMMDD格式");
                return result;
            }
            
            // 2. 验证用户ID
            if (userId == null || userId.trim().length() < 3) {
                result.put("success", false);
                result.put("error_code", "INVALID_USER_ID");
                result.put("error_message", "用户ID长度不能少于3个字符");
                return result;
            }
            
            // 3. 生成盐值
            String salt = generateSalt(6);
            
            // 4. 构建签名数据
            String signatureData = dateStr + "|" + userId.trim() + "|" + salt;
            
            // 5. 生成HMAC-SHA256签名
            String signature = generateHmacSignature(signatureData);
            
            // 6. 构建最终授权码
            String authCode = dateStr + "-" + salt + "-" + signature.substring(0, 8).toUpperCase();
            
            // 7. 计算过期时间（如果不是永久授权码）
            LocalDateTime expiresAt = null;
            if (!isPermanent) {
                // 解析日期并设置为当天23:59:59过期
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                expiresAt = LocalDate.of(year, month, day).atTime(23, 59, 59);
            }
            
            // 8. 准备存储数据
            Map<String, Object> authData = new HashMap<>();
            authData.put("auth_code", authCode);
            authData.put("date_str", dateStr);
            authData.put("user_id", userId.trim());
            authData.put("salt", salt);
            authData.put("signature", signature);
            authData.put("is_permanent", isPermanent);
            authData.put("generated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            if (expiresAt != null) {
                authData.put("expires_at", expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            // 9. 保存到存储
            boolean saved = authStorage.saveAuthCode(authCode, authData);
            
            if (saved) {
                result.put("success", true);
                result.put("auth_code", authCode);
                result.put("expires_at", expiresAt != null ? expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                result.put("is_permanent", isPermanent);
                result.put("user_id", userId.trim());
                
                logger.info("授权码生成成功: {} (用户: {}, 永久: {})", authCode, userId.trim(), isPermanent);
            } else {
                result.put("success", false);
                result.put("error_code", "STORAGE_ERROR");
                result.put("error_message", "授权码保存失败");
            }
            
        } catch (Exception e) {
            logger.error("生成授权码时发生错误", e);
            result.put("success", false);
            result.put("error_code", "GENERATION_ERROR");
            result.put("error_message", "授权码生成过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证授权码
     * 
     * @param authCode 要验证的授权码
     * @return 验证结果
     */
    public Map<String, Object> verifyAuthCode(String authCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (authCode == null || authCode.trim().isEmpty()) {
                result.put("valid", false);
                result.put("error_code", "INVALID_AUTH_CODE_FORMAT");
                result.put("error_message", "授权码不能为空");
                return result;
            }
            
            // 从存储中获取授权码数据
            Map<String, Object> authData = authStorage.getAuthCode(authCode.trim());
            
            if (authData == null) {
                result.put("valid", false);
                result.put("error_code", "AUTH_NOT_FOUND");
                result.put("error_message", "授权码不存在或已失效");
                return result;
            }
            
            // 检查是否为永久授权码
            Boolean isPermanent = (Boolean) authData.get("is_permanent");
            if (isPermanent != null && isPermanent) {
                result.put("valid", true);
                result.put("is_permanent", true);
                result.put("user_id", authData.get("user_id"));
                return result;
            }
            
            // 检查过期时间
            String expiresAtStr = (String) authData.get("expires_at");
            if (expiresAtStr != null) {
                try {
                    LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    LocalDateTime now = LocalDateTime.now();
                    
                    if (now.isAfter(expiresAt)) {
                        result.put("valid", false);
                        result.put("error_code", "AUTH_EXPIRED");
                        result.put("error_message", "授权码已过期");
                        return result;
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("解析授权码过期时间失败: {}", expiresAtStr);
                    result.put("valid", false);
                    result.put("error_code", "INVALID_EXPIRY_TIME");
                    result.put("error_message", "授权码过期时间格式错误");
                    return result;
                }
            }
            
            // 验证成功
            result.put("valid", true);
            result.put("is_permanent", false);
            result.put("user_id", authData.get("user_id"));
            result.put("expires_at", expiresAtStr);
            
        } catch (Exception e) {
            logger.error("验证授权码时发生错误: {}", authCode, e);
            result.put("valid", false);
            result.put("error_code", "VERIFICATION_ERROR");
            result.put("error_message", "授权码验证过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 清理过期的授权码
     * 
     * @return 清理的数量
     */
    public int cleanupExpiredCodes() {
        return authStorage.cleanupExpiredCodes();
    }
    
    /**
     * 获取存储信息
     * 
     * @return 存储统计信息
     */
    public Map<String, Object> getStorageInfo() {
        return authStorage.getStorageInfo();
    }
    
    /**
     * 生成HMAC-SHA256签名
     * 
     * @param data 要签名的数据
     * @return 十六进制签名字符串
     */
    private String generateHmacSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // 转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : signature) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}