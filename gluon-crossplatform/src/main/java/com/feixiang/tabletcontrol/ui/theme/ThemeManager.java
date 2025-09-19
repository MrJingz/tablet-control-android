package com.feixiang.tabletcontrol.ui.theme;

import com.feixiang.tabletcontrol.platform.PlatformManager;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主题管理器
 * 负责管理应用的主题样式，支持跨平台主题适配
 */
public class ThemeManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);
    
    private final PlatformManager platformManager;
    
    // 主题定义
    private Theme currentTheme;
    private final Map<String, Theme> availableThemes;
    
    // 样式表缓存
    private final Map<String, String> stylesheetCache;
    
    public ThemeManager(PlatformManager platformManager) {
        this.platformManager = platformManager;
        this.availableThemes = new HashMap<>();
        this.stylesheetCache = new HashMap<>();
        
        initializeThemes();
        setDefaultTheme();
        
        logger.info("主题管理器初始化完成，当前主题: {}", currentTheme.getName());
    }
    
    /**
     * 初始化可用主题
     */
    private void initializeThemes() {
        logger.info("初始化主题系统");
        
        // 默认浅色主题
        Theme lightTheme = new Theme("light", "浅色主题")
            .setPrimaryColor("#2196F3")
            .setSecondaryColor("#FFC107")
            .setBackgroundColor("#FFFFFF")
            .setSurfaceColor("#F5F5F5")
            .setTextColor("#212121")
            .setTextSecondaryColor("#757575")
            .setAccentColor("#FF5722")
            .setErrorColor("#F44336")
            .setSuccessColor("#4CAF50")
            .setWarningColor("#FF9800");
        
        // 默认深色主题
        Theme darkTheme = new Theme("dark", "深色主题")
            .setPrimaryColor("#1976D2")
            .setSecondaryColor("#FFA000")
            .setBackgroundColor("#121212")
            .setSurfaceColor("#1E1E1E")
            .setTextColor("#FFFFFF")
            .setTextSecondaryColor("#B0B0B0")
            .setAccentColor("#FF5722")
            .setErrorColor("#CF6679")
            .setSuccessColor("#81C784")
            .setWarningColor("#FFB74D");
        
        // 高对比度主题
        Theme highContrastTheme = new Theme("high-contrast", "高对比度主题")
            .setPrimaryColor("#0000FF")
            .setSecondaryColor("#FFFF00")
            .setBackgroundColor("#FFFFFF")
            .setSurfaceColor("#F0F0F0")
            .setTextColor("#000000")
            .setTextSecondaryColor("#333333")
            .setAccentColor("#FF0000")
            .setErrorColor("#FF0000")
            .setSuccessColor("#00FF00")
            .setWarningColor("#FFFF00");
        
        // 移动端优化主题
        Theme mobileTheme = new Theme("mobile", "移动端主题")
            .setPrimaryColor("#2196F3")
            .setSecondaryColor("#FFC107")
            .setBackgroundColor("#FAFAFA")
            .setSurfaceColor("#FFFFFF")
            .setTextColor("#212121")
            .setTextSecondaryColor("#757575")
            .setAccentColor("#FF5722")
            .setErrorColor("#F44336")
            .setSuccessColor("#4CAF50")
            .setWarningColor("#FF9800")
            .setFontScale(1.2) // 移动端字体稍大
            .setSpacingScale(1.5); // 移动端间距更大
        
        availableThemes.put(lightTheme.getId(), lightTheme);
        availableThemes.put(darkTheme.getId(), darkTheme);
        availableThemes.put(highContrastTheme.getId(), highContrastTheme);
        availableThemes.put(mobileTheme.getId(), mobileTheme);
        
        logger.info("主题初始化完成，可用主题数量: {}", availableThemes.size());
    }
    
    /**
     * 设置默认主题
     */
    private void setDefaultTheme() {
        // 根据平台选择默认主题
        String defaultThemeId;
        if (platformManager.isMobile()) {
            defaultThemeId = "mobile";
        } else {
            // 检查系统是否为深色模式
            if (isSystemDarkMode()) {
                defaultThemeId = "dark";
            } else {
                defaultThemeId = "light";
            }
        }
        
        currentTheme = availableThemes.get(defaultThemeId);
        logger.info("设置默认主题: {}", currentTheme.getName());
    }
    
    /**
     * 检查系统是否为深色模式
     */
    private boolean isSystemDarkMode() {
        // 简单的系统深色模式检测
        // 在实际应用中，可以使用更复杂的检测逻辑
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                // Windows 10/11 深色模式检测
                // 这里可以通过注册表或其他方式检测
                return false; // 默认浅色
            } else if (osName.contains("mac")) {
                // macOS 深色模式检测
                // 可以通过系统命令检测
                return false; // 默认浅色
            } else {
                // Linux 等其他系统
                return false; // 默认浅色
            }
        } catch (Exception e) {
            logger.warn("检测系统深色模式失败", e);
            return false;
        }
    }
    
    /**
     * 应用主题到场景
     */
    public void applyTheme(Scene scene) {
        if (scene == null || currentTheme == null) {
            logger.warn("场景或主题为null，无法应用主题");
            return;
        }
        
        logger.info("应用主题到场景: {}", currentTheme.getName());
        
        // 清除现有样式类
        scene.getRoot().getStyleClass().removeIf(styleClass -> 
            styleClass.startsWith("theme-"));
        
        // 添加主题样式类
        scene.getRoot().getStyleClass().add("theme-" + currentTheme.getId());
        
        // 生成并应用CSS样式
        String css = generateThemeCSS(currentTheme);
        applyCSSToScene(scene, css);
        
        // 应用平台特定的样式调整
        applyPlatformSpecificStyles(scene);
        
        logger.info("主题应用完成");
    }
    
    /**
     * 生成主题CSS样式
     */
    private String generateThemeCSS(Theme theme) {
        String cacheKey = theme.getId() + "_" + platformManager.getCurrentPlatform().name();
        
        if (stylesheetCache.containsKey(cacheKey)) {
            return stylesheetCache.get(cacheKey);
        }
        
        StringBuilder css = new StringBuilder();
        
        // 根节点样式
        css.append(".root {\n");
        css.append("    -fx-base: ").append(theme.getBackgroundColor()).append(";\n");
        css.append("    -fx-background: ").append(theme.getBackgroundColor()).append(";\n");
        css.append("    -fx-control-inner-background: ").append(theme.getSurfaceColor()).append(";\n");
        css.append("    -fx-text-fill: ").append(theme.getTextColor()).append(";\n");
        css.append("    -fx-font-size: ").append(theme.getFontScale()).append("em;\n");
        css.append("}\n\n");
        
        // 按钮样式
        css.append(".button {\n");
        css.append("    -fx-background-color: ").append(theme.getPrimaryColor()).append(";\n");
        css.append("    -fx-text-fill: white;\n");
        css.append("    -fx-background-radius: 4px;\n");
        css.append("    -fx-padding: ").append(8 * theme.getSpacingScale()).append("px ").append(16 * theme.getSpacingScale()).append("px;\n");
        css.append("}\n\n");
        
        css.append(".button:hover {\n");
        css.append("    -fx-background-color: derive(").append(theme.getPrimaryColor()).append(", -10%);\n");
        css.append("}\n\n");
        
        css.append(".button:pressed {\n");
        css.append("    -fx-background-color: derive(").append(theme.getPrimaryColor()).append(", -20%);\n");
        css.append("}\n\n");
        
        // 标签样式
        css.append(".label {\n");
        css.append("    -fx-text-fill: ").append(theme.getTextColor()).append(";\n");
        css.append("}\n\n");
        
        css.append(".label.secondary {\n");
        css.append("    -fx-text-fill: ").append(theme.getTextSecondaryColor()).append(";\n");
        css.append("}\n\n");
        
        // 标题样式
        css.append(".title-label {\n");
        css.append("    -fx-font-size: ").append(1.5 * theme.getFontScale()).append("em;\n");
        css.append("    -fx-font-weight: bold;\n");
        css.append("    -fx-text-fill: ").append(theme.getPrimaryColor()).append(";\n");
        css.append("}\n\n");
        
        // 面板样式
        css.append(".content-area {\n");
        css.append("    -fx-background-color: ").append(theme.getSurfaceColor()).append(";\n");
        css.append("    -fx-background-radius: 8px;\n");
        css.append("    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);\n");
        css.append("}\n\n");
        
        // 状态样式
        css.append(".success {\n");
        css.append("    -fx-text-fill: ").append(theme.getSuccessColor()).append(";\n");
        css.append("}\n\n");
        
        css.append(".error {\n");
        css.append("    -fx-text-fill: ").append(theme.getErrorColor()).append(";\n");
        css.append("}\n\n");
        
        css.append(".warning {\n");
        css.append("    -fx-text-fill: ").append(theme.getWarningColor()).append(";\n");
        css.append("}\n\n");
        
        // 移动端特定样式
        if (platformManager.isMobile()) {
            css.append(".button {\n");
            css.append("    -fx-min-height: 44px;\n"); // iOS HIG 推荐的最小触控区域
            css.append("    -fx-min-width: 44px;\n");
            css.append("}\n\n");
            
            css.append(".scroll-pane {\n");
            css.append("    -fx-fit-to-width: true;\n");
            css.append("}\n\n");
        }
        
        String cssString = css.toString();
        stylesheetCache.put(cacheKey, cssString);
        
        return cssString;
    }
    
    /**
     * 应用CSS到场景
     */
    private void applyCSSToScene(Scene scene, String css) {
        // 移除旧的内联样式
        scene.getRoot().setStyle("");
        
        // 应用新的内联样式
        scene.getRoot().setStyle(css);
    }
    
    /**
     * 应用平台特定样式
     */
    private void applyPlatformSpecificStyles(Scene scene) {
        // 添加平台特定的样式类
        scene.getRoot().getStyleClass().add("platform-" + platformManager.getCurrentPlatform().name().toLowerCase());
        
        if (platformManager.isMobile()) {
            scene.getRoot().getStyleClass().add("mobile-platform");
        } else {
            scene.getRoot().getStyleClass().add("desktop-platform");
        }
    }
    
    /**
     * 切换主题
     */
    public void setTheme(String themeId) {
        Theme newTheme = availableThemes.get(themeId);
        if (newTheme == null) {
            logger.warn("主题不存在: {}", themeId);
            return;
        }
        
        logger.info("切换主题: {} -> {}", currentTheme.getName(), newTheme.getName());
        currentTheme = newTheme;
        
        // 清除样式表缓存以强制重新生成
        stylesheetCache.clear();
    }
    
    /**
     * 获取当前主题
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * 获取可用主题列表
     */
    public List<Theme> getAvailableThemes() {
        return new ArrayList<>(availableThemes.values());
    }
    
    /**
     * 检查主题是否存在
     */
    public boolean hasTheme(String themeId) {
        return availableThemes.containsKey(themeId);
    }
    
    /**
     * 注册自定义主题
     */
    public void registerTheme(Theme theme) {
        if (theme == null || theme.getId() == null) {
            throw new IllegalArgumentException("主题或主题ID不能为null");
        }
        
        availableThemes.put(theme.getId(), theme);
        logger.info("注册自定义主题: {}", theme.getName());
    }
    
    /**
     * 清除样式表缓存
     */
    public void clearCache() {
        stylesheetCache.clear();
        logger.info("样式表缓存已清除");
    }
}
