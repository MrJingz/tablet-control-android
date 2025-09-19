package com.feixiang.tabletcontrol.core.model;

import java.io.Serializable;

/**
 * 组件数据模型 - 跨平台版本
 * 用于存储页面中单个组件的完整信息，支持跨平台适配
 */
public class ComponentData implements Serializable {
    private static final long serialVersionUID = 2L;

    // 绝对坐标 (向后兼容)
    private int x;
    private int y;
    private int width;
    private int height;
    private int originalWidth;
    private int originalHeight;

    // 相对定位 (跨平台适配)
    private RelativePosition relativePosition;

    // 定位模式
    private PositionMode positionMode = PositionMode.ABSOLUTE; // 默认绝对定位

    private String functionType;
    private LabelData labelData;
    
    // 跨平台扩展属性
    private String componentId; // 组件唯一标识
    private boolean visible = true; // 是否可见
    private boolean enabled = true; // 是否启用
    private String tooltip; // 工具提示
    private String cssClass; // CSS样式类

    // 定位模式枚举
    public enum PositionMode {
        ABSOLUTE,   // 绝对坐标模式 (向后兼容)
        RELATIVE    // 相对定位模式 (跨平台推荐)
    }
    
    // 默认构造函数
    public ComponentData() {
        this.componentId = generateComponentId();
    }
    
    // 绝对坐标构造函数 (向后兼容)
    public ComponentData(int x, int y, int width, int height,
                        int originalWidth, int originalHeight,
                        String functionType, LabelData labelData) {
        this();
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

    // 相对定位构造函数 (跨平台推荐)
    public ComponentData(RelativePosition relativePosition, String functionType, LabelData labelData) {
        this();
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
    
    // 跨平台扩展属性
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getTooltip() { return tooltip; }
    public void setTooltip(String tooltip) { this.tooltip = tooltip; }
    
    public String getCssClass() { return cssClass; }
    public void setCssClass(String cssClass) { this.cssClass = cssClass; }

    /**
     * 获取在指定容器尺寸下的绝对位置
     */
    public AbsolutePosition getAbsolutePosition(int containerWidth, int containerHeight) {
        if (positionMode == PositionMode.RELATIVE && relativePosition != null) {
            RelativePosition.AbsolutePosition absPos = relativePosition.toAbsolute(containerWidth, containerHeight);
            return new AbsolutePosition(absPos.x, absPos.y, absPos.width, absPos.height);
        } else {
            // 使用绝对坐标
            return new AbsolutePosition(x, y, width, height);
        }
    }
    
    /**
     * 从绝对坐标更新相对位置
     */
    public void updateRelativePosition(int containerWidth, int containerHeight) {
        if (containerWidth > 0 && containerHeight > 0) {
            this.relativePosition = RelativePosition.fromAbsolute(x, y, width, height, containerWidth, containerHeight);
            this.positionMode = PositionMode.RELATIVE;
        }
    }
    
    /**
     * 从相对位置更新绝对坐标
     */
    public void updateAbsolutePosition(int containerWidth, int containerHeight) {
        if (relativePosition != null && containerWidth > 0 && containerHeight > 0) {
            RelativePosition.AbsolutePosition absPos = relativePosition.toAbsolute(containerWidth, containerHeight);
            this.x = absPos.x;
            this.y = absPos.y;
            this.width = absPos.width;
            this.height = absPos.height;
        }
    }
    
    /**
     * 生成组件唯一标识
     */
    private String generateComponentId() {
        return "comp_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 检查组件数据完整性
     */
    public boolean isValid() {
        if (functionType == null || functionType.trim().isEmpty()) {
            return false;
        }
        
        if (positionMode == PositionMode.RELATIVE) {
            return relativePosition != null && relativePosition.isValid();
        } else {
            return width > 0 && height > 0;
        }
    }
    
    /**
     * 修复组件数据完整性
     */
    public void repair() {
        if (functionType == null) {
            functionType = "未知组件";
        }
        
        if (componentId == null || componentId.trim().isEmpty()) {
            componentId = generateComponentId();
        }
        
        if (positionMode == PositionMode.RELATIVE && relativePosition != null) {
            relativePosition.repair();
        } else {
            // 修复绝对坐标
            x = Math.max(0, x);
            y = Math.max(0, y);
            width = Math.max(1, width);
            height = Math.max(1, height);
        }
        
        if (labelData == null) {
            labelData = new LabelData();
        }
    }
    
    /**
     * 获取组件摘要信息
     */
    public String getComponentSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("组件: ").append(functionType);
        
        if (labelData != null && labelData.hasText()) {
            sb.append(", 文本: \"").append(labelData.getText()).append("\"");
        }
        
        if (positionMode == PositionMode.RELATIVE && relativePosition != null) {
            sb.append(", 位置: ").append(relativePosition.toString());
        } else {
            sb.append(String.format(", 位置: (%d,%d) 尺寸: %dx%d", x, y, width, height));
        }
        
        return sb.toString();
    }
    
    /**
     * 创建副本
     */
    public ComponentData copy() {
        ComponentData copy = new ComponentData();
        copy.x = this.x;
        copy.y = this.y;
        copy.width = this.width;
        copy.height = this.height;
        copy.originalWidth = this.originalWidth;
        copy.originalHeight = this.originalHeight;
        copy.functionType = this.functionType;
        copy.positionMode = this.positionMode;
        copy.visible = this.visible;
        copy.enabled = this.enabled;
        copy.tooltip = this.tooltip;
        copy.cssClass = this.cssClass;
        
        if (this.labelData != null) {
            copy.labelData = this.labelData.copy();
        }
        
        if (this.relativePosition != null) {
            copy.relativePosition = this.relativePosition.copy();
        }
        
        // 生成新的组件ID
        copy.componentId = generateComponentId();
        
        return copy;
    }
    
    @Override
    public String toString() {
        return "ComponentData{" +
                "id='" + componentId + '\'' +
                ", type='" + functionType + '\'' +
                ", mode=" + positionMode +
                ", visible=" + visible +
                ", enabled=" + enabled +
                (positionMode == PositionMode.RELATIVE && relativePosition != null ? 
                    ", relPos=" + relativePosition : 
                    ", absPos=(" + x + "," + y + "," + width + "," + height + ")") +
                '}';
    }
    
    /**
     * 绝对位置数据类
     */
    public static class AbsolutePosition {
        public final int x, y, width, height;
        
        public AbsolutePosition(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        @Override
        public String toString() {
            return String.format("AbsolutePosition{x=%d, y=%d, w=%d, h=%d}", x, y, width, height);
        }
    }
}
