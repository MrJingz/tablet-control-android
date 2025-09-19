package com.feixiang.tabletcontrol.model;

import java.io.Serializable;

/**
 * 标签数据模型
 * 用于存储组件的显示属性信息
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
    
    // 默认构造函数
    public LabelData() {
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
    }
    
    // Getter和Setter方法
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { this.fontName = fontName; }
    
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
    
    @Override
    public String toString() {
        return "LabelData{" +
                "text='" + text + '\'' +
                ", fontName='" + fontName + '\'' +
                ", fontSize=" + fontSize +
                ", fontStyle=" + fontStyle +
                ", colorRGB=" + colorRGB +
                ", iconPath='" + iconPath + '\'' +
                '}';
    }
}
