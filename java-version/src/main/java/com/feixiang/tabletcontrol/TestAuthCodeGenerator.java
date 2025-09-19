package com.feixiang.tabletcontrol;

import com.feixiang.tabletcontrol.auth.AuthCodeGeneratorFile;
import java.util.Map;

/**
 * 测试授权码生成器，用于生成真实的授权码
 */
public class TestAuthCodeGenerator {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 授权码生成测试 ===");
            
            AuthCodeGeneratorFile generator = new AuthCodeGeneratorFile();
            
            // 生成当前日期的授权码
            System.out.println("\n1. 生成当前日期授权码 (20250831):");
            Map<String, Object> result1 = generator.generateAuthCode("20250831", "default_user", false);
            printResult(result1);
            
            // 生成未来日期的授权码
            System.out.println("\n2. 生成未来日期授权码 (20250901):");
            Map<String, Object> result2 = generator.generateAuthCode("20250901", "default_user", false);
            printResult(result2);
            
            // 生成长期有效的授权码
            System.out.println("\n3. 生成长期有效授权码 (20251231):");
            Map<String, Object> result3 = generator.generateAuthCode("20251231", "default_user", false);
            printResult(result3);
            
            // 生成永久授权码
            System.out.println("\n4. 生成永久授权码:");
            Map<String, Object> result4 = generator.generateAuthCode("99991231", "default_user", true);
            printResult(result4);
            
            // 获取存储信息
            System.out.println("\n=== 存储信息 ===");
            Map<String, Object> storageInfo = generator.getStorageInfo();
            System.out.println("存储信息: " + storageInfo);
            
        } catch (Exception e) {
            System.err.println("生成授权码失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printResult(Map<String, Object> result) {
        Boolean success = (Boolean) result.get("success");
        if (success) {
            String authCode = (String) result.get("auth_code");
            String expiresAt = (String) result.get("expires_at");
            Boolean isPermanent = (Boolean) result.get("is_permanent");
            
            System.out.println("✅ 生成成功!");
            System.out.println("授权码: " + authCode);
            System.out.println("过期时间: " + (expiresAt != null ? expiresAt : "永久有效"));
            System.out.println("是否永久: " + isPermanent);
        } else {
            String errorCode = (String) result.get("error_code");
            String errorMessage = (String) result.get("error_message");
            System.out.println("❌ 生成失败!");
            System.out.println("错误代码: " + errorCode);
            System.out.println("错误信息: " + errorMessage);
        }
    }
}
