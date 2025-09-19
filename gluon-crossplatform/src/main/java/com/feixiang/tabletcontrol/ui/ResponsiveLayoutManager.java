package com.feixiang.tabletcontrol.ui;

import com.feixiang.tabletcontrol.platform.PlatformManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 响应式布局管理器
 * 负责根据平台和屏幕尺寸调整UI布局
 */
public class ResponsiveLayoutManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponsiveLayoutManager.class);
    
    private final PlatformManager platformManager;
    
    // 断点定义
    private static final int MOBILE_BREAKPOINT = 768;
    private static final int TABLET_BREAKPOINT = 1024;
    private static final int DESKTOP_BREAKPOINT = 1200;
    
    public ResponsiveLayoutManager(PlatformManager platformManager) {
        this.platformManager = platformManager;
    }
    
    /**
     * 应用响应式布局
     */
    public void applyResponsiveLayout(Scene scene, Stage stage) {
        logger.info("应用响应式布局，平台: {}", platformManager.getCurrentPlatform());
        
        // 获取屏幕尺寸
        double screenWidth = stage.getWidth();
        double screenHeight = stage.getHeight();
        
        // 确定设备类型
        DeviceType deviceType = determineDeviceType(screenWidth, screenHeight);
        logger.info("设备类型: {}, 屏幕尺寸: {}x{}", deviceType, screenWidth, screenHeight);
        
        // 应用设备特定的样式
        applyDeviceSpecificStyles(scene, deviceType);
        
        // 设置字体缩放
        applyFontScaling(scene, deviceType);
        
        // 设置间距调整
        applySpacingAdjustments(scene, deviceType);
        
        logger.info("响应式布局应用完成");
    }
    
    /**
     * 确定设备类型
     */
    private DeviceType determineDeviceType(double width, double height) {
        if (platformManager.isMobile()) {
            if (width < MOBILE_BREAKPOINT) {
                return DeviceType.MOBILE_PHONE;
            } else {
                return DeviceType.MOBILE_TABLET;
            }
        } else {
            if (width < TABLET_BREAKPOINT) {
                return DeviceType.SMALL_DESKTOP;
            } else if (width < DESKTOP_BREAKPOINT) {
                return DeviceType.MEDIUM_DESKTOP;
            } else {
                return DeviceType.LARGE_DESKTOP;
            }
        }
    }
    
    /**
     * 应用设备特定样式
     */
    private void applyDeviceSpecificStyles(Scene scene, DeviceType deviceType) {
        // 移除现有的设备样式类
        scene.getRoot().getStyleClass().removeIf(styleClass -> 
            styleClass.startsWith("device-") || styleClass.startsWith("platform-"));
        
        // 添加平台样式类
        if (platformManager.isMobile()) {
            scene.getRoot().getStyleClass().add("platform-mobile");
        } else {
            scene.getRoot().getStyleClass().add("platform-desktop");
        }
        
        // 添加设备类型样式类
        switch (deviceType) {
            case MOBILE_PHONE:
                scene.getRoot().getStyleClass().add("device-mobile-phone");
                break;
            case MOBILE_TABLET:
                scene.getRoot().getStyleClass().add("device-mobile-tablet");
                break;
            case SMALL_DESKTOP:
                scene.getRoot().getStyleClass().add("device-small-desktop");
                break;
            case MEDIUM_DESKTOP:
                scene.getRoot().getStyleClass().add("device-medium-desktop");
                break;
            case LARGE_DESKTOP:
                scene.getRoot().getStyleClass().add("device-large-desktop");
                break;
        }
        
        logger.debug("应用设备样式: {}", deviceType);
    }
    
    /**
     * 应用字体缩放
     */
    private void applyFontScaling(Scene scene, DeviceType deviceType) {
        double fontScale = getFontScaleFactor(deviceType);
        
        // 设置根节点的字体缩放
        scene.getRoot().setStyle(scene.getRoot().getStyle() + 
            String.format("; -fx-font-size: %.1fem;", fontScale));
        
        logger.debug("应用字体缩放: {}em", fontScale);
    }
    
    /**
     * 获取字体缩放因子
     */
    private double getFontScaleFactor(DeviceType deviceType) {
        switch (deviceType) {
            case MOBILE_PHONE:
                return 1.2; // 移动端字体稍大
            case MOBILE_TABLET:
                return 1.1;
            case SMALL_DESKTOP:
                return 0.9;
            case MEDIUM_DESKTOP:
                return 1.0; // 标准大小
            case LARGE_DESKTOP:
                return 1.1;
            default:
                return 1.0;
        }
    }
    
    /**
     * 应用间距调整
     */
    private void applySpacingAdjustments(Scene scene, DeviceType deviceType) {
        double spacingScale = getSpacingScaleFactor(deviceType);
        
        // 设置根节点的间距缩放
        scene.getRoot().setStyle(scene.getRoot().getStyle() + 
            String.format("; -fx-spacing: %.1fem;", spacingScale));
        
        logger.debug("应用间距缩放: {}em", spacingScale);
    }
    
    /**
     * 获取间距缩放因子
     */
    private double getSpacingScaleFactor(DeviceType deviceType) {
        switch (deviceType) {
            case MOBILE_PHONE:
                return 1.5; // 移动端间距更大
            case MOBILE_TABLET:
                return 1.3;
            case SMALL_DESKTOP:
                return 0.8;
            case MEDIUM_DESKTOP:
                return 1.0; // 标准间距
            case LARGE_DESKTOP:
                return 1.2;
            default:
                return 1.0;
        }
    }
    
    /**
     * 获取推荐的触控区域最小尺寸
     */
    public double getMinTouchTargetSize(DeviceType deviceType) {
        if (platformManager.isMobile()) {
            switch (deviceType) {
                case MOBILE_PHONE:
                    return 48.0; // iOS HIG 推荐
                case MOBILE_TABLET:
                    return 44.0;
                default:
                    return 44.0;
            }
        } else {
            return 24.0; // 桌面端较小的点击区域
        }
    }
    
    /**
     * 检查是否需要紧凑布局
     */
    public boolean shouldUseCompactLayout(double width, double height) {
        return width < MOBILE_BREAKPOINT || height < 600;
    }
    
    /**
     * 检查是否应该隐藏次要UI元素
     */
    public boolean shouldHideSecondaryUI(double width) {
        return width < MOBILE_BREAKPOINT;
    }
    
    /**
     * 获取设备类型信息
     */
    public DeviceType getCurrentDeviceType(Stage stage) {
        return determineDeviceType(stage.getWidth(), stage.getHeight());
    }
    
    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        MOBILE_PHONE("手机"),
        MOBILE_TABLET("平板"),
        SMALL_DESKTOP("小屏桌面"),
        MEDIUM_DESKTOP("中屏桌面"),
        LARGE_DESKTOP("大屏桌面");
        
        private final String displayName;
        
        DeviceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}
