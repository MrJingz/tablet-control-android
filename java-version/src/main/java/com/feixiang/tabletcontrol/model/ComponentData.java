package com.feixiang.tabletcontrol.model;

import java.io.Serializable;

/**
 * 组件数据模型
 * 用于存储页面中单个组件的完整信息
 * 支持绝对坐标和相对定位两种模式
 */
public class ComponentData implements Serializable {
    private static final long serialVersionUID = 2L; // 版本升级

    // 绝对坐标 (向后兼容)
    private int x;
    private int y;
    private int width;
    private int height;
    private int originalWidth;
    private int originalHeight;

    // 相对定位 (新增)
    private RelativePosition relativePosition;

    // 定位模式
    private PositionMode positionMode = PositionMode.ABSOLUTE; // 默认绝对定位

    private String functionType;
    private LabelData labelData;

    // 定位模式枚举
    public enum PositionMode {
        ABSOLUTE,   // 绝对坐标模式 (向后兼容)
        RELATIVE    // 相对定位模式 (新模式)
    }
    
    // 默认构造函数
    public ComponentData() {
    }
    
    // 绝对坐标构造函数 (向后兼容)
    public ComponentData(int x, int y, int width, int height,
                        int originalWidth, int originalHeight,
                        String functionType, LabelData labelData) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.functionType = functionType;
        this.labelData = labelData;
        this.positionMode = PositionMode.ABSOLUTE;
    }

    // 相对定位构造函数 (新增)
    public ComponentData(RelativePosition relativePosition, String functionType, LabelData labelData) {
        this.relativePosition = relativePosition;
        this.functionType = functionType;
        this.labelData = labelData;
        this.positionMode = PositionMode.RELATIVE;
    }
    
    // Getter和Setter方法
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public int getOriginalWidth() { return originalWidth; }
    public void setOriginalWidth(int originalWidth) { this.originalWidth = originalWidth; }
    
    public int getOriginalHeight() { return originalHeight; }
    public void setOriginalHeight(int originalHeight) { this.originalHeight = originalHeight; }
    
    public String getFunctionType() { return functionType; }
    public void setFunctionType(String functionType) { this.functionType = functionType; }

    public LabelData getLabelData() { return labelData; }
    public void setLabelData(LabelData labelData) { this.labelData = labelData; }

    // 相对定位相关方法
    public RelativePosition getRelativePosition() { return relativePosition; }
    public void setRelativePosition(RelativePosition relativePosition) {
        this.relativePosition = relativePosition;
        this.positionMode = PositionMode.RELATIVE;
    }

    public PositionMode getPositionMode() { return positionMode; }
    public void setPositionMode(PositionMode positionMode) { this.positionMode = positionMode; }

    /**
     * 获取在指定容器尺寸下的绝对位置
     */
    public RelativePosition.AbsolutePosition getAbsolutePosition(int containerWidth, int containerHeight) {
        if (positionMode == PositionMode.RELATIVE && relativePosition != null) {
            return relativePosition.toAbsolute(containerWidth, containerHeight);
        } else {
            // 绝对坐标模式或相对位置为空时，返回原始坐标
            return new RelativePosition.AbsolutePosition(x, y, width, height);
        }
    }

    /**
     * 从绝对坐标转换为相对定位
     */
    public void convertToRelative(int containerWidth, int containerHeight) {
        if (positionMode == PositionMode.ABSOLUTE) {
            this.relativePosition = RelativePosition.fromAbsolute(x, y, width, height, containerWidth, containerHeight);
            this.positionMode = PositionMode.RELATIVE;
        }
    }

    /**
     * 从相对定位转换为绝对坐标
     */
    public void convertToAbsolute(int containerWidth, int containerHeight) {
        if (positionMode == PositionMode.RELATIVE && relativePosition != null) {
            RelativePosition.AbsolutePosition abs = relativePosition.toAbsolute(containerWidth, containerHeight);
            this.x = abs.x;
            this.y = abs.y;
            this.width = abs.width;
            this.height = abs.height;
            this.positionMode = PositionMode.ABSOLUTE;
        }
    }
    
    /**
     * 获取组件的边界矩形
     */
    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle(x, y, width, height);
    }
    
    /**
     * 设置组件的边界矩形
     */
    public void setBounds(java.awt.Rectangle bounds) {
        this.x = bounds.x;
        this.y = bounds.y;
        this.width = bounds.width;
        this.height = bounds.height;
    }
    
    /**
     * 创建组件数据的深拷贝
     */
    public ComponentData copy() {
        LabelData labelDataCopy = null;
        if (this.labelData != null) {
            labelDataCopy = new LabelData(
                this.labelData.getText(),
                this.labelData.getFontName(),
                this.labelData.getFontSize(),
                this.labelData.getFontStyle(),
                this.labelData.getColorRGB(),
                this.labelData.getIconPath(),
                this.labelData.getOriginalFontSize(),
                this.labelData.getHorizontalAlignment(),
                this.labelData.getVerticalAlignment(),
                this.labelData.getHorizontalTextPosition(),
                this.labelData.getVerticalTextPosition()
            );
        }
        
        return new ComponentData(x, y, width, height, originalWidth, originalHeight, 
                               functionType, labelDataCopy);
    }
    
    @Override
    public String toString() {
        return "ComponentData{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", functionType='" + functionType + '\'' +
                ", labelData=" + labelData +
                '}';
    }
}
