package com.feixiang.tabletcontrol.model;

import java.io.Serializable;

/**
 * 相对位置数据模型
 * 使用百分比和锚点来定义组件的相对位置，解决跨分辨率兼容性问题
 */
public class RelativePosition implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 双锚点系统：左上角和右下角百分比位置 (0.0 - 1.0)
    private double topLeftX;      // 左上角X轴百分比位置
    private double topLeftY;      // 左上角Y轴百分比位置
    private double bottomRightX;  // 右下角X轴百分比位置
    private double bottomRightY;  // 右下角Y轴百分比位置

    // 向后兼容：保留原有的单锚点数据（已弃用，但保留用于数据迁移）
    @Deprecated
    private double xPercent;      // X轴百分比位置（已弃用）
    @Deprecated
    private double yPercent;      // Y轴百分比位置（已弃用）
    @Deprecated
    private double widthPercent;  // 宽度百分比（已弃用）
    @Deprecated
    private double heightPercent; // 高度百分比（已弃用）
    @Deprecated
    private AnchorType anchorType = AnchorType.TOP_LEFT; // 锚点类型（已弃用）
    
    // 最小/最大尺寸限制 (像素值)
    private int minWidth = 50;
    private int minHeight = 20;
    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;
    
    // 锚点类型枚举
    public enum AnchorType {
        TOP_LEFT,       // 左上角 (默认)
        TOP_CENTER,     // 上中
        TOP_RIGHT,      // 右上角
        CENTER_LEFT,    // 左中
        CENTER,         // 中心
        CENTER_RIGHT,   // 右中
        BOTTOM_LEFT,    // 左下角
        BOTTOM_CENTER,  // 下中
        BOTTOM_RIGHT    // 右下角
    }
    
    // 构造函数
    public RelativePosition() {
    }

    /**
     * 创建双锚点相对位置对象（推荐使用）
     */
    public static RelativePosition createDualAnchor(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY) {
        RelativePosition pos = new RelativePosition();
        pos.topLeftX = Math.max(0.0, Math.min(1.0, topLeftX));
        pos.topLeftY = Math.max(0.0, Math.min(1.0, topLeftY));
        pos.bottomRightX = Math.max(0.0, Math.min(1.0, bottomRightX));
        pos.bottomRightY = Math.max(0.0, Math.min(1.0, bottomRightY));

        // 确保右下角在左上角的右下方
        if (pos.bottomRightX < pos.topLeftX) {
            pos.bottomRightX = pos.topLeftX + 0.01; // 最小1%宽度
        }
        if (pos.bottomRightY < pos.topLeftY) {
            pos.bottomRightY = pos.topLeftY + 0.01; // 最小1%高度
        }

        // 更新遗留数据
        pos.updateLegacyData();
        return pos;
    }

    /**
     * 向后兼容的构造函数（已弃用）
     */
    @Deprecated
    public RelativePosition(double xPercent, double yPercent, double widthPercent, double heightPercent) {
        this.xPercent = Math.max(0.0, Math.min(1.0, xPercent));
        this.yPercent = Math.max(0.0, Math.min(1.0, yPercent));
        this.widthPercent = Math.max(0.0, Math.min(1.0, widthPercent));
        this.heightPercent = Math.max(0.0, Math.min(1.0, heightPercent));

        // 从单锚点数据计算双锚点数据
        convertFromSingleAnchor();
    }

    /**
     * 向后兼容的构造函数（已弃用）
     */
    @Deprecated
    public RelativePosition(double xPercent, double yPercent, double widthPercent, double heightPercent, AnchorType anchorType) {
        this.xPercent = Math.max(0.0, Math.min(1.0, xPercent));
        this.yPercent = Math.max(0.0, Math.min(1.0, yPercent));
        this.widthPercent = Math.max(0.0, Math.min(1.0, widthPercent));
        this.heightPercent = Math.max(0.0, Math.min(1.0, heightPercent));
        this.anchorType = anchorType;

        // 从单锚点数据计算双锚点数据
        convertFromSingleAnchor();
    }

    /**
     * 从单锚点数据转换为双锚点数据（向后兼容）
     */
    private void convertFromSingleAnchor() {
        if (anchorType == null) {
            anchorType = AnchorType.BOTTOM_RIGHT; // 默认值
        }

        // 根据锚点类型计算左上角和右下角位置
        switch (anchorType) {
            case TOP_LEFT:
                topLeftX = xPercent;
                topLeftY = yPercent;
                bottomRightX = xPercent + widthPercent;
                bottomRightY = yPercent + heightPercent;
                break;
            case BOTTOM_RIGHT:
                bottomRightX = xPercent;
                bottomRightY = yPercent;
                topLeftX = xPercent - widthPercent;
                topLeftY = yPercent - heightPercent;
                break;
            case CENTER:
                double halfWidth = widthPercent / 2;
                double halfHeight = heightPercent / 2;
                topLeftX = xPercent - halfWidth;
                topLeftY = yPercent - halfHeight;
                bottomRightX = xPercent + halfWidth;
                bottomRightY = yPercent + halfHeight;
                break;
            default:
                // 其他锚点类型，默认按TOP_LEFT处理
                topLeftX = xPercent;
                topLeftY = yPercent;
                bottomRightX = xPercent + widthPercent;
                bottomRightY = yPercent + heightPercent;
                break;
        }

        // 确保坐标在有效范围内
        topLeftX = Math.max(0.0, Math.min(1.0, topLeftX));
        topLeftY = Math.max(0.0, Math.min(1.0, topLeftY));
        bottomRightX = Math.max(0.0, Math.min(1.0, bottomRightX));
        bottomRightY = Math.max(0.0, Math.min(1.0, bottomRightY));
    }

    /**
     * 从绝对坐标创建相对位置（双锚点版本）
     */
    public static RelativePosition fromAbsoluteDualAnchor(int x, int y, int width, int height, int containerWidth, int containerHeight) {
        // 防止除零错误
        if (containerWidth <= 0 || containerHeight <= 0) {
            throw new IllegalArgumentException("容器尺寸必须大于0: " + containerWidth + "x" + containerHeight);
        }

        // 确保坐标和尺寸在有效范围内
        x = Math.max(0, Math.min(x, containerWidth));
        y = Math.max(0, Math.min(y, containerHeight));
        width = Math.max(1, Math.min(width, containerWidth - x));
        height = Math.max(1, Math.min(height, containerHeight - y));

        // 计算左上角和右下角的百分比位置
        double topLeftX = (double) x / containerWidth;
        double topLeftY = (double) y / containerHeight;
        double bottomRightX = (double) (x + width) / containerWidth;
        double bottomRightY = (double) (y + height) / containerHeight;

        // 确保最小尺寸：至少占容器的0.5%宽度和高度
        double minWidthPercent = 0.005;  // 0.5%
        double minHeightPercent = 0.005; // 0.5%

        double currentWidthPercent = bottomRightX - topLeftX;
        double currentHeightPercent = bottomRightY - topLeftY;

        if (currentWidthPercent < minWidthPercent) {
            // 保持中心位置，扩展到最小宽度
            double centerX = (topLeftX + bottomRightX) / 2;
            topLeftX = Math.max(0, centerX - minWidthPercent / 2);
            bottomRightX = Math.min(1.0, topLeftX + minWidthPercent);
            // 如果右边界超出，调整左边界
            if (bottomRightX >= 1.0) {
                bottomRightX = 1.0;
                topLeftX = bottomRightX - minWidthPercent;
            }
        }

        if (currentHeightPercent < minHeightPercent) {
            // 保持中心位置，扩展到最小高度
            double centerY = (topLeftY + bottomRightY) / 2;
            topLeftY = Math.max(0, centerY - minHeightPercent / 2);
            bottomRightY = Math.min(1.0, topLeftY + minHeightPercent);
            // 如果下边界超出，调整上边界
            if (bottomRightY >= 1.0) {
                bottomRightY = 1.0;
                topLeftY = bottomRightY - minHeightPercent;
            }
        }

        return createDualAnchor(topLeftX, topLeftY, bottomRightX, bottomRightY);
    }

    /**
     * 从绝对坐标创建相对位置（推荐使用双锚点版本）
     */
    public static RelativePosition fromAbsolute(int x, int y, int width, int height, int containerWidth, int containerHeight) {
        return fromAbsoluteDualAnchor(x, y, width, height, containerWidth, containerHeight);
    }

    /**
     * 从绝对坐标创建相对位置（向后兼容的单锚点版本）
     */
    @Deprecated
    public static RelativePosition fromAbsoluteLegacy(int x, int y, int width, int height, int containerWidth, int containerHeight) {
        // 防止除零错误
        if (containerWidth <= 0 || containerHeight <= 0) {
            throw new IllegalArgumentException("容器尺寸必须大于0: " + containerWidth + "x" + containerHeight);
        }

        // 确保坐标和尺寸在有效范围内
        x = Math.max(0, Math.min(x, containerWidth));
        y = Math.max(0, Math.min(y, containerHeight));
        width = Math.max(1, Math.min(width, containerWidth - x));
        height = Math.max(1, Math.min(height, containerHeight - y));

        double xPercent = (double) x / containerWidth;
        double yPercent = (double) y / containerHeight;
        double widthPercent = (double) width / containerWidth;
        double heightPercent = (double) height / containerHeight;

        // 确保百分比在有效范围内
        xPercent = Math.max(0.0, Math.min(1.0, xPercent));
        yPercent = Math.max(0.0, Math.min(1.0, yPercent));
        widthPercent = Math.max(0.001, Math.min(1.0, widthPercent));  // 最小0.1%
        heightPercent = Math.max(0.001, Math.min(1.0, heightPercent)); // 最小0.1%

        // 确保不会超出右边界和下边界
        if (xPercent + widthPercent > 1.0) {
            xPercent = Math.max(0.0, 1.0 - widthPercent);
        }
        if (yPercent + heightPercent > 1.0) {
            yPercent = Math.max(0.0, 1.0 - heightPercent);
        }

        return new RelativePosition(xPercent, yPercent, widthPercent, heightPercent);
    }
    
    /**
     * 转换为绝对坐标（双锚点版本）
     */
    public AbsolutePosition toAbsolute(int containerWidth, int containerHeight) {
        // 如果双锚点数据不完整，尝试从单锚点数据转换
        if (topLeftX == 0.0 && topLeftY == 0.0 && bottomRightX == 0.0 && bottomRightY == 0.0) {
            if (xPercent != 0.0 || yPercent != 0.0 || widthPercent != 0.0 || heightPercent != 0.0) {
                convertFromSingleAnchor();
            }
        }

        // 基于双锚点计算绝对位置和尺寸
        int x = (int) (topLeftX * containerWidth);
        int y = (int) (topLeftY * containerHeight);
        int right = (int) (bottomRightX * containerWidth);
        int bottom = (int) (bottomRightY * containerHeight);

        // 计算宽度和高度
        int width = Math.max(1, right - x);
        int height = Math.max(1, bottom - y);

        // 应用尺寸限制
        width = Math.max(minWidth, Math.min(maxWidth, width));
        height = Math.max(minHeight, Math.min(maxHeight, height));

        // 如果应用尺寸限制后尺寸发生变化，需要调整位置以保持锚点关系
        if (width != (right - x) || height != (bottom - y)) {
            // 保持左上角位置不变，调整右下角
            right = x + width;
            bottom = y + height;
        }

        // 确保不超出容器边界
        x = Math.max(0, Math.min(x, containerWidth - width));
        y = Math.max(0, Math.min(y, containerHeight - height));

        // 如果位置调整了，确保尺寸仍然有效
        width = Math.min(width, containerWidth - x);
        height = Math.min(height, containerHeight - y);

        return new AbsolutePosition(x, y, width, height);
    }

    /**
     * 转换为绝对坐标（向后兼容的单锚点版本）
     */
    @Deprecated
    public AbsolutePosition toAbsoluteLegacy(int containerWidth, int containerHeight) {
        int width = (int) (widthPercent * containerWidth);
        int height = (int) (heightPercent * containerHeight);

        // 应用尺寸限制
        width = Math.max(minWidth, Math.min(maxWidth, width));
        height = Math.max(minHeight, Math.min(maxHeight, height));

        // 计算基础位置
        int x = (int) (xPercent * containerWidth);
        int y = (int) (yPercent * containerHeight);

        // 根据锚点调整位置
        switch (anchorType) {
            case BOTTOM_RIGHT:
                x -= width;
                y -= height;
                break;
            case TOP_LEFT:
            default:
                // 不需要调整
                break;
        }

        // 确保不超出容器边界
        x = Math.max(0, Math.min(x, containerWidth - width));
        y = Math.max(0, Math.min(y, containerHeight - height));

        return new AbsolutePosition(x, y, width, height);
    }
    
    // 双锚点系统的Getter和Setter方法（推荐使用）
    public double getTopLeftX() { return topLeftX; }
    public void setTopLeftX(double topLeftX) {
        this.topLeftX = Math.max(0.0, Math.min(1.0, topLeftX));
        updateLegacyData();
    }

    public double getTopLeftY() { return topLeftY; }
    public void setTopLeftY(double topLeftY) {
        this.topLeftY = Math.max(0.0, Math.min(1.0, topLeftY));
        updateLegacyData();
    }

    public double getBottomRightX() { return bottomRightX; }
    public void setBottomRightX(double bottomRightX) {
        this.bottomRightX = Math.max(0.0, Math.min(1.0, bottomRightX));
        updateLegacyData();
    }

    public double getBottomRightY() { return bottomRightY; }
    public void setBottomRightY(double bottomRightY) {
        this.bottomRightY = Math.max(0.0, Math.min(1.0, bottomRightY));
        updateLegacyData();
    }

    /**
     * 更新遗留数据（向后兼容）
     */
    private void updateLegacyData() {
        this.xPercent = bottomRightX; // 使用右下角作为锚点
        this.yPercent = bottomRightY;
        this.widthPercent = Math.max(0.0, bottomRightX - topLeftX);
        this.heightPercent = Math.max(0.0, bottomRightY - topLeftY);
    }

    // 向后兼容的Getter和Setter方法（已弃用）
    @Deprecated
    public double getXPercent() { return xPercent; }
    @Deprecated
    public void setXPercent(double xPercent) {
        this.xPercent = Math.max(0.0, Math.min(1.0, xPercent));
    }

    @Deprecated
    public double getYPercent() { return yPercent; }
    @Deprecated
    public void setYPercent(double yPercent) {
        this.yPercent = Math.max(0.0, Math.min(1.0, yPercent));
    }

    /**
     * 计算当前宽度百分比（基于双锚点）
     */
    public double getWidthPercent() {
        return Math.max(0.0, bottomRightX - topLeftX);
    }

    @Deprecated
    public void setWidthPercent(double widthPercent) {
        this.widthPercent = Math.max(0.0, Math.min(1.0, widthPercent));
    }

    /**
     * 计算当前高度百分比（基于双锚点）
     */
    public double getHeightPercent() {
        return Math.max(0.0, bottomRightY - topLeftY);
    }

    @Deprecated
    public void setHeightPercent(double heightPercent) {
        this.heightPercent = Math.max(0.0, Math.min(1.0, heightPercent));
    }
    
    public AnchorType getAnchorType() { return anchorType; }
    public void setAnchorType(AnchorType anchorType) { this.anchorType = anchorType; }
    
    public int getMinWidth() { return minWidth; }
    public void setMinWidth(int minWidth) { this.minWidth = minWidth; }
    
    public int getMinHeight() { return minHeight; }
    public void setMinHeight(int minHeight) { this.minHeight = minHeight; }
    
    public int getMaxWidth() { return maxWidth; }
    public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }
    
    public int getMaxHeight() { return maxHeight; }
    public void setMaxHeight(int maxHeight) { this.maxHeight = maxHeight; }
    
    @Override
    public String toString() {
        return String.format("RelativePosition{topLeft=(%.3f%%, %.3f%%), bottomRight=(%.3f%%, %.3f%%), size=%.3f%%x%.3f%%}",
                           topLeftX * 100, topLeftY * 100, bottomRightX * 100, bottomRightY * 100,
                           getWidthPercent() * 100, getHeightPercent() * 100);
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
