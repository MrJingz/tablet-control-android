package com.feixiang.tabletcontrol.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 跨平台管理器
 * 负责检测当前平台并提供平台特定的功能
 */
public class PlatformManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PlatformManager.class);
    
    /**
     * 支持的平台枚举
     */
    public enum Platform {
        WINDOWS("Windows"),
        MACOS("macOS"),
        LINUX("Linux"),
        ANDROID("Android"),
        IOS("iOS"),
        UNKNOWN("Unknown");
        
        private final String displayName;
        
        Platform(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final Platform currentPlatform;
    private final boolean isMobile;
    private final boolean isDesktop;
    
    public PlatformManager() {
        this.currentPlatform = detectPlatform();
        this.isMobile = (currentPlatform == Platform.ANDROID || currentPlatform == Platform.IOS);
        this.isDesktop = !isMobile;
        
        logger.info("平台检测完成: {}, 移动端: {}", currentPlatform.getDisplayName(), isMobile);
    }
    
    /**
     * 检测当前运行平台
     */
    private Platform detectPlatform() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        String javaVendor = System.getProperty("java.vendor", "").toLowerCase();
        String javaRuntimeName = System.getProperty("java.runtime.name", "").toLowerCase();
        
        logger.debug("系统信息 - OS: {}, Vendor: {}, Runtime: {}", osName, javaVendor, javaRuntimeName);
        
        // Android 检测
        if (osName.contains("android") || 
            javaVendor.contains("android") || 
            javaRuntimeName.contains("android") ||
            System.getProperty("java.vm.name", "").toLowerCase().contains("dalvik")) {
            return Platform.ANDROID;
        }
        
        // iOS 检测
        if (osName.contains("ios") || 
            osName.contains("iphone") || 
            osName.contains("ipad") ||
            javaVendor.contains("robovm")) {
            return Platform.IOS;
        }
        
        // 桌面平台检测
        if (osName.contains("windows")) {
            return Platform.WINDOWS;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return Platform.MACOS;
        } else if (osName.contains("linux") || osName.contains("unix")) {
            return Platform.LINUX;
        }
        
        logger.warn("无法识别的平台: {}", osName);
        return Platform.UNKNOWN;
    }
    
    /**
     * 获取当前平台
     */
    public Platform getCurrentPlatform() {
        return currentPlatform;
    }
    
    /**
     * 是否为移动端平台
     */
    public boolean isMobile() {
        return isMobile;
    }
    
    /**
     * 是否为桌面端平台
     */
    public boolean isDesktop() {
        return isDesktop;
    }
    
    /**
     * 是否为 Windows 平台
     */
    public boolean isWindows() {
        return currentPlatform == Platform.WINDOWS;
    }
    
    /**
     * 是否为 macOS 平台
     */
    public boolean isMacOS() {
        return currentPlatform == Platform.MACOS;
    }
    
    /**
     * 是否为 Linux 平台
     */
    public boolean isLinux() {
        return currentPlatform == Platform.LINUX;
    }
    
    /**
     * 是否为 Android 平台
     */
    public boolean isAndroid() {
        return currentPlatform == Platform.ANDROID;
    }
    
    /**
     * 是否为 iOS 平台
     */
    public boolean isIOS() {
        return currentPlatform == Platform.IOS;
    }
    
    /**
     * 获取平台特定的文件分隔符
     */
    public String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    /**
     * 获取平台特定的路径分隔符
     */
    public String getPathSeparator() {
        return System.getProperty("path.separator");
    }
    
    /**
     * 获取平台特定的换行符
     */
    public String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    /**
     * 获取推荐的触控区域最小尺寸
     */
    public double getMinTouchTargetSize() {
        if (isMobile()) {
            return 44.0; // iOS HIG 推荐的最小触控区域
        } else {
            return 24.0; // 桌面端较小的点击区域
        }
    }
    
    /**
     * 获取推荐的字体大小
     */
    public double getRecommendedFontSize() {
        if (isMobile()) {
            return 16.0; // 移动端较大字体
        } else {
            return 12.0; // 桌面端标准字体
        }
    }
    
    /**
     * 获取推荐的组件间距
     */
    public double getRecommendedSpacing() {
        if (isMobile()) {
            return 16.0; // 移动端较大间距
        } else {
            return 8.0; // 桌面端较小间距
        }
    }
    
    /**
     * 是否支持文件系统访问
     */
    public boolean supportsFileSystemAccess() {
        // 移动端可能有文件访问限制
        return isDesktop() || isAndroid(); // iOS 通常有更严格的文件访问限制
    }
    
    /**
     * 是否支持多窗口
     */
    public boolean supportsMultipleWindows() {
        return isDesktop(); // 移动端通常不支持多窗口
    }
    
    /**
     * 是否支持系统托盘
     */
    public boolean supportsSystemTray() {
        return isDesktop() && !isMacOS(); // macOS 使用 Dock，不是传统的系统托盘
    }
    
    /**
     * 获取平台特定的用户数据目录名称
     */
    public String getUserDataDirectoryName() {
        switch (currentPlatform) {
            case WINDOWS:
                return "TabletControl";
            case MACOS:
                return "TabletControl";
            case LINUX:
                return ".tabletcontrol";
            case ANDROID:
                return "TabletControl";
            case IOS:
                return "TabletControl";
            default:
                return "TabletControl";
        }
    }
    
    /**
     * 获取平台信息摘要
     */
    public String getPlatformSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Platform: ").append(currentPlatform.getDisplayName());
        sb.append(", Type: ").append(isMobile() ? "Mobile" : "Desktop");
        sb.append(", OS: ").append(System.getProperty("os.name"));
        sb.append(", Version: ").append(System.getProperty("os.version"));
        sb.append(", Arch: ").append(System.getProperty("os.arch"));
        return sb.toString();
    }
    
    /**
     * 记录详细的平台信息
     */
    public void logPlatformDetails() {
        logger.info("=== 详细平台信息 ===");
        logger.info("当前平台: {}", currentPlatform.getDisplayName());
        logger.info("平台类型: {}", isMobile() ? "移动端" : "桌面端");
        logger.info("操作系统: {}", System.getProperty("os.name"));
        logger.info("系统版本: {}", System.getProperty("os.version"));
        logger.info("系统架构: {}", System.getProperty("os.arch"));
        logger.info("Java版本: {}", System.getProperty("java.version"));
        logger.info("Java供应商: {}", System.getProperty("java.vendor"));
        logger.info("Java运行时: {}", System.getProperty("java.runtime.name"));
        logger.info("用户目录: {}", System.getProperty("user.home"));
        logger.info("工作目录: {}", System.getProperty("user.dir"));
        logger.info("文件分隔符: {}", getFileSeparator());
        logger.info("推荐字体大小: {}", getRecommendedFontSize());
        logger.info("推荐触控区域: {}", getMinTouchTargetSize());
        logger.info("支持文件系统: {}", supportsFileSystemAccess());
        logger.info("支持多窗口: {}", supportsMultipleWindows());
        logger.info("支持系统托盘: {}", supportsSystemTray());
        logger.info("==================");
    }
}
