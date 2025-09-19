package com.feixiang.tabletcontrol.core.model;

import java.io.Serializable;

/**
 * 标签数据模型 - 跨平台版本
 * 用于存储组件的显示属性信息，支持跨平台字体和颜色适配
 */
public class LabelData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String text;
    private String fontName;
    private int fontSize;
    private int fontStyle;
    private int colorRGB;
    private String iconPath;
    private int originalFontSize;
    private int horizontalAlignment;
    private int verticalAlignment;
    private int horizontalTextPosition;
    private int verticalTextPosition;
    
    // 跨平台扩展属性
    private String fontFamily; // 跨平台字体族名
    private boolean autoScaleFont = true; // 是否自动缩放字体
    private double fontScaleFactor = 1.0; // 字体缩放因子
    
    // 默认构造函数
    public LabelData() {
        // 设置跨平台默认值
        this.fontName = "System"; // 系统默认字体
        this.fontSize = 12;
        this.fontStyle = 0; // PLAIN
        this.colorRGB = 0x000000; // 黑色
        this.horizontalAlignment = 0; // LEFT
        this.verticalAlignment = 0; // TOP
        this.horizontalTextPosition = 11; // TRAILING
        this.verticalTextPosition = 0; // CENTER
    }
    
    // 全参构造函数
    public LabelData(String text, String fontName, int fontSize, int fontStyle, 
                     int colorRGB, String iconPath, int originalFontSize,
                     int horizontalAlignment, int verticalAlignment,
                     int horizontalTextPosition, int verticalTextPosition) {
        this.text = text;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.colorRGB = colorRGB;
        this.iconPath = iconPath;
        this.originalFontSize = originalFontSize;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.horizontalTextPosition = horizontalTextPosition;
        this.verticalTextPosition = verticalTextPosition;
        
        // 设置跨平台字体族
        this.fontFamily = mapToSystemFont(fontName);
    }
    
    // Getter和Setter方法
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { 
        this.fontName = fontName;
        this.fontFamily = mapToSystemFont(fontName);
    }
    
    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }
    
    public int getFontStyle() { return fontStyle; }
    public void setFontStyle(int fontStyle) { this.fontStyle = fontStyle; }
    
    public int getColorRGB() { return colorRGB; }
    public void setColorRGB(int colorRGB) { this.colorRGB = colorRGB; }
    
    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
    
    public int getOriginalFontSize() { return originalFontSize; }
    public void setOriginalFontSize(int originalFontSize) { this.originalFontSize = originalFontSize; }
    
    public int getHorizontalAlignment() { return horizontalAlignment; }
    public void setHorizontalAlignment(int horizontalAlignment) { this.horizontalAlignment = horizontalAlignment; }
    
    public int getVerticalAlignment() { return verticalAlignment; }
    public void setVerticalAlignment(int verticalAlignment) { this.verticalAlignment = verticalAlignment; }
    
    public int getHorizontalTextPosition() { return horizontalTextPosition; }
    public void setHorizontalTextPosition(int horizontalTextPosition) { this.horizontalTextPosition = horizontalTextPosition; }
    
    public int getVerticalTextPosition() { return verticalTextPosition; }
    public void setVerticalTextPosition(int verticalTextPosition) { this.verticalTextPosition = verticalTextPosition; }
    
    // 跨平台扩展属性
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    
    public boolean isAutoScaleFont() { return autoScaleFont; }
    public void setAutoScaleFont(boolean autoScaleFont) { this.autoScaleFont = autoScaleFont; }
    
    public double getFontScaleFactor() { return fontScaleFactor; }
    public void setFontScaleFactor(double fontScaleFactor) { this.fontScaleFactor = fontScaleFactor; }
    
    // 跨平台适配方法
    
    /**
     * 将字体名称映射到系统字体族
     */
    private String mapToSystemFont(String fontName) {
        if (fontName == null) {
            return "System";
        }
        
        String lowerName = fontName.toLowerCase();
        
        // 常见字体映射
        if (lowerName.contains("dialog") || lowerName.contains("sans")) {
            return "System";
        } else if (lowerName.contains("serif")) {
            return "Serif";
        } else if (lowerName.contains("mono")) {
            return "Monospaced";
        } else {
            return fontName; // 保持原字体名
        }
    }
    
    /**
     * 获取适配后的字体大小
     */
    public int getAdaptedFontSize() {
        if (autoScaleFont) {
            return (int) Math.round(fontSize * fontScaleFactor);
        }
        return fontSize;
    }
    
    /**
     * 获取颜色的十六进制字符串表示
     */
    public String getColorHex() {
        return String.format("#%06X", colorRGB & 0xFFFFFF);
    }
    
    /**
     * 从十六进制字符串设置颜色
     */
    public void setColorFromHex(String hexColor) {
        if (hexColor != null && hexColor.startsWith("#") && hexColor.length() == 7) {
            try {
                this.colorRGB = Integer.parseInt(hexColor.substring(1), 16);
            } catch (NumberFormatException e) {
                // 解析失败，保持原颜色
            }
        }
    }
    
    /**
     * 获取颜色的RGB分量
     */
    public int[] getRGBComponents() {
        return new int[] {
            (colorRGB >> 16) & 0xFF, // Red
            (colorRGB >> 8) & 0xFF,  // Green
            colorRGB & 0xFF          // Blue
        };
    }
    
    /**
     * 从RGB分量设置颜色
     */
    public void setColorFromRGB(int red, int green, int blue) {
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));
        this.colorRGB = (red << 16) | (green << 8) | blue;
    }
    
    /**
     * 检查是否有图标
     */
    public boolean hasIcon() {
        return iconPath != null && !iconPath.trim().isEmpty();
    }
    
    /**
     * 检查是否有文本
     */
    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }
    
    /**
     * 获取显示内容摘要
     */
    public String getContentSummary() {
        StringBuilder sb = new StringBuilder();
        if (hasText()) {
            sb.append("文本: \"").append(text).append("\"");
        }
        if (hasIcon()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("图标: ").append(iconPath);
        }
        if (sb.length() == 0) {
            sb.append("无内容");
        }
        return sb.toString();
    }
    
    /**
     * 创建副本
     */
    public LabelData copy() {
        LabelData copy = new LabelData();
        copy.text = this.text;
        copy.fontName = this.fontName;
        copy.fontSize = this.fontSize;
        copy.fontStyle = this.fontStyle;
        copy.colorRGB = this.colorRGB;
        copy.iconPath = this.iconPath;
        copy.originalFontSize = this.originalFontSize;
        copy.horizontalAlignment = this.horizontalAlignment;
        copy.verticalAlignment = this.verticalAlignment;
        copy.horizontalTextPosition = this.horizontalTextPosition;
        copy.verticalTextPosition = this.verticalTextPosition;
        copy.fontFamily = this.fontFamily;
        copy.autoScaleFont = this.autoScaleFont;
        copy.fontScaleFactor = this.fontScaleFactor;
        return copy;
    }
    
    @Override
    public String toString() {
        return "LabelData{" +
                "text='" + text + '\'' +
                ", fontName='" + fontName + '\'' +
                ", fontSize=" + fontSize +
                ", fontStyle=" + fontStyle +
                ", colorRGB=" + String.format("0x%06X", colorRGB) +
                ", iconPath='" + iconPath + '\'' +
                ", fontFamily='" + fontFamily + '\'' +
                ", autoScale=" + autoScaleFont +
                ", scaleFactor=" + fontScaleFactor +
                '}';
    }
}
