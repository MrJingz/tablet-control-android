package com.feixiang.tabletcontrol.core.model;

import java.io.Serializable;

/**
 * 相对位置数据模型 - 跨平台版本
 * 使用百分比定义组件的相对位置，解决跨分辨率兼容性问题
 */
public class RelativePosition implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 相对位置和尺寸 (0.0 - 1.0)
    private double relativeX;      // X轴百分比位置
    private double relativeY;      // Y轴百分比位置
    private double relativeWidth;  // 宽度百分比
    private double relativeHeight; // 高度百分比
    
    // 最小/最大尺寸限制 (像素值)
    private int minWidth = 50;
    private int minHeight = 20;
    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;
    
    // 构造函数
    public RelativePosition() {
    }
    
    public RelativePosition(double relativeX, double relativeY, double relativeWidth, double relativeHeight) {
        this.relativeX = Math.max(0.0, Math.min(1.0, relativeX));
        this.relativeY = Math.max(0.0, Math.min(1.0, relativeY));
        this.relativeWidth = Math.max(0.0, Math.min(1.0, relativeWidth));
        this.relativeHeight = Math.max(0.0, Math.min(1.0, relativeHeight));
    }
    
    /**
     * 从绝对坐标创建相对位置
     */
    public static RelativePosition fromAbsolute(int x, int y, int width, int height, int containerWidth, int containerHeight) {
        // 防止除零错误
        if (containerWidth <= 0 || containerHeight <= 0) {
            throw new IllegalArgumentException("容器尺寸必须大于0: " + containerWidth + "x" + containerHeight);
        }
        
        // 确保坐标和尺寸在有效范围内
        x = Math.max(0, Math.min(x, containerWidth));
        y = Math.max(0, Math.min(y, containerHeight));
        width = Math.max(1, Math.min(width, containerWidth - x));
        height = Math.max(1, Math.min(height, containerHeight - y));
        
        double relativeX = (double) x / containerWidth;
        double relativeY = (double) y / containerHeight;
        double relativeWidth = (double) width / containerWidth;
        double relativeHeight = (double) height / containerHeight;
        
        // 确保百分比在有效范围内
        relativeX = Math.max(0.0, Math.min(1.0, relativeX));
        relativeY = Math.max(0.0, Math.min(1.0, relativeY));
        relativeWidth = Math.max(0.001, Math.min(1.0, relativeWidth));  // 最小0.1%
        relativeHeight = Math.max(0.001, Math.min(1.0, relativeHeight)); // 最小0.1%
        
        // 确保不会超出右边界和下边界
        if (relativeX + relativeWidth > 1.0) {
            relativeX = Math.max(0.0, 1.0 - relativeWidth);
        }
        if (relativeY + relativeHeight > 1.0) {
            relativeY = Math.max(0.0, 1.0 - relativeHeight);
        }
        
        return new RelativePosition(relativeX, relativeY, relativeWidth, relativeHeight);
    }
    
    /**
     * 转换为绝对坐标
     */
    public AbsolutePosition toAbsolute(int containerWidth, int containerHeight) {
        int x = (int) (relativeX * containerWidth);
        int y = (int) (relativeY * containerHeight);
        int width = (int) (relativeWidth * containerWidth);
        int height = (int) (relativeHeight * containerHeight);
        
        // 应用尺寸限制
        width = Math.max(minWidth, Math.min(maxWidth, width));
        height = Math.max(minHeight, Math.min(maxHeight, height));
        
        // 确保不超出容器边界
        x = Math.max(0, Math.min(x, containerWidth - width));
        y = Math.max(0, Math.min(y, containerHeight - height));
        
        // 如果位置调整了，确保尺寸仍然有效
        width = Math.min(width, containerWidth - x);
        height = Math.min(height, containerHeight - y);
        
        return new AbsolutePosition(x, y, width, height);
    }
    
    // Getter和Setter方法
    public double getRelativeX() { return relativeX; }
    public void setRelativeX(double relativeX) {
        this.relativeX = Math.max(0.0, Math.min(1.0, relativeX));
    }
    
    public double getRelativeY() { return relativeY; }
    public void setRelativeY(double relativeY) {
        this.relativeY = Math.max(0.0, Math.min(1.0, relativeY));
    }
    
    public double getRelativeWidth() { return relativeWidth; }
    public void setRelativeWidth(double relativeWidth) {
        this.relativeWidth = Math.max(0.0, Math.min(1.0, relativeWidth));
    }
    
    public double getRelativeHeight() { return relativeHeight; }
    public void setRelativeHeight(double relativeHeight) {
        this.relativeHeight = Math.max(0.0, Math.min(1.0, relativeHeight));
    }
    
    public int getMinWidth() { return minWidth; }
    public void setMinWidth(int minWidth) { this.minWidth = minWidth; }
    
    public int getMinHeight() { return minHeight; }
    public void setMinHeight(int minHeight) { this.minHeight = minHeight; }
    
    public int getMaxWidth() { return maxWidth; }
    public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }
    
    public int getMaxHeight() { return maxHeight; }
    public void setMaxHeight(int maxHeight) { this.maxHeight = maxHeight; }
    
    /**
     * 检查位置是否有效
     */
    public boolean isValid() {
        return relativeX >= 0.0 && relativeX <= 1.0 &&
               relativeY >= 0.0 && relativeY <= 1.0 &&
               relativeWidth > 0.0 && relativeWidth <= 1.0 &&
               relativeHeight > 0.0 && relativeHeight <= 1.0 &&
               (relativeX + relativeWidth) <= 1.0 &&
               (relativeY + relativeHeight) <= 1.0;
    }
    
    /**
     * 修复无效的位置数据
     */
    public void repair() {
        relativeX = Math.max(0.0, Math.min(1.0, relativeX));
        relativeY = Math.max(0.0, Math.min(1.0, relativeY));
        relativeWidth = Math.max(0.001, Math.min(1.0, relativeWidth));
        relativeHeight = Math.max(0.001, Math.min(1.0, relativeHeight));
        
        // 确保不超出边界
        if (relativeX + relativeWidth > 1.0) {
            relativeX = Math.max(0.0, 1.0 - relativeWidth);
        }
        if (relativeY + relativeHeight > 1.0) {
            relativeY = Math.max(0.0, 1.0 - relativeHeight);
        }
    }
    
    /**
     * 创建副本
     */
    public RelativePosition copy() {
        RelativePosition copy = new RelativePosition();
        copy.relativeX = this.relativeX;
        copy.relativeY = this.relativeY;
        copy.relativeWidth = this.relativeWidth;
        copy.relativeHeight = this.relativeHeight;
        copy.minWidth = this.minWidth;
        copy.minHeight = this.minHeight;
        copy.maxWidth = this.maxWidth;
        copy.maxHeight = this.maxHeight;
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("RelativePosition{pos=(%.3f%%, %.3f%%), size=(%.3f%%, %.3f%%)}",
                           relativeX * 100, relativeY * 100, relativeWidth * 100, relativeHeight * 100);
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
