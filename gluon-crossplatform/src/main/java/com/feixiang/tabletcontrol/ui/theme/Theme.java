package com.feixiang.tabletcontrol.ui.theme;

/**
 * 主题数据类
 * 定义主题的颜色、字体、间距等样式属性
 */
public class Theme {
    
    private final String id;
    private final String name;
    
    // 颜色定义
    private String primaryColor = "#2196F3";
    private String secondaryColor = "#FFC107";
    private String backgroundColor = "#FFFFFF";
    private String surfaceColor = "#F5F5F5";
    private String textColor = "#212121";
    private String textSecondaryColor = "#757575";
    private String accentColor = "#FF5722";
    private String errorColor = "#F44336";
    private String successColor = "#4CAF50";
    private String warningColor = "#FF9800";
    
    // 尺寸和缩放
    private double fontScale = 1.0;
    private double spacingScale = 1.0;
    private double borderRadius = 4.0;
    
    // 阴影和效果
    private boolean enableShadows = true;
    private double shadowOpacity = 0.1;
    
    public Theme(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getter方法
    public String getId() { return id; }
    public String getName() { return name; }
    
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public String getBackgroundColor() { return backgroundColor; }
    public String getSurfaceColor() { return surfaceColor; }
    public String getTextColor() { return textColor; }
    public String getTextSecondaryColor() { return textSecondaryColor; }
    public String getAccentColor() { return accentColor; }
    public String getErrorColor() { return errorColor; }
    public String getSuccessColor() { return successColor; }
    public String getWarningColor() { return warningColor; }
    
    public double getFontScale() { return fontScale; }
    public double getSpacingScale() { return spacingScale; }
    public double getBorderRadius() { return borderRadius; }
    
    public boolean isEnableShadows() { return enableShadows; }
    public double getShadowOpacity() { return shadowOpacity; }
    
    // 流式设置方法
    public Theme setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
        return this;
    }
    
    public Theme setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
        return this;
    }
    
    public Theme setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }
    
    public Theme setSurfaceColor(String surfaceColor) {
        this.surfaceColor = surfaceColor;
        return this;
    }
    
    public Theme setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }
    
    public Theme setTextSecondaryColor(String textSecondaryColor) {
        this.textSecondaryColor = textSecondaryColor;
        return this;
    }
    
    public Theme setAccentColor(String accentColor) {
        this.accentColor = accentColor;
        return this;
    }
    
    public Theme setErrorColor(String errorColor) {
        this.errorColor = errorColor;
        return this;
    }
    
    public Theme setSuccessColor(String successColor) {
        this.successColor = successColor;
        return this;
    }
    
    public Theme setWarningColor(String warningColor) {
        this.warningColor = warningColor;
        return this;
    }
    
    public Theme setFontScale(double fontScale) {
        this.fontScale = fontScale;
        return this;
    }
    
    public Theme setSpacingScale(double spacingScale) {
        this.spacingScale = spacingScale;
        return this;
    }
    
    public Theme setBorderRadius(double borderRadius) {
        this.borderRadius = borderRadius;
        return this;
    }
    
    public Theme setEnableShadows(boolean enableShadows) {
        this.enableShadows = enableShadows;
        return this;
    }
    
    public Theme setShadowOpacity(double shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
        return this;
    }
    
    @Override
    public String toString() {
        return "Theme{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", primaryColor='" + primaryColor + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", fontScale=" + fontScale +
                ", spacingScale=" + spacingScale +
                '}';
    }
}
