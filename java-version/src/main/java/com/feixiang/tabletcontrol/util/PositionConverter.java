package com.feixiang.tabletcontrol.util;

import com.feixiang.tabletcontrol.model.ComponentData;
import com.feixiang.tabletcontrol.model.RelativePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 位置转换工具类
 * 负责绝对坐标和相对定位之间的转换，以及跨分辨率的位置计算
 */
public class PositionConverter {
    private static final Logger logger = LoggerFactory.getLogger(PositionConverter.class);
    
    // 标准编辑分辨率
    public static final int STANDARD_EDIT_WIDTH = 1024;
    public static final int STANDARD_EDIT_HEIGHT = 768;
    
    /**
     * 将组件从绝对坐标模式转换为相对定位模式
     */
    public static void convertToRelativeMode(ComponentData component, int editWidth, int editHeight) {
        if (component.getPositionMode() == ComponentData.PositionMode.ABSOLUTE) {
            logger.debug("转换组件到相对定位模式: {}x{} -> {}x{}", 
                        component.getX(), component.getY(), editWidth, editHeight);
            
            component.convertToRelative(editWidth, editHeight);
            
            logger.debug("转换完成: {}", component.getRelativePosition());
        }
    }
    
    /**
     * 批量转换组件到相对定位模式
     */
    public static void convertAllToRelativeMode(List<ComponentData> components, int editWidth, int editHeight) {
        logger.info("批量转换{}个组件到相对定位模式，编辑分辨率: {}x{}", 
                   components.size(), editWidth, editHeight);
        
        for (ComponentData component : components) {
            convertToRelativeMode(component, editWidth, editHeight);
        }
        
        logger.info("批量转换完成");
    }
    
    /**
     * 计算组件在目标分辨率下的绝对位置
     */
    public static RelativePosition.AbsolutePosition calculateDisplayPosition(
            ComponentData component, int targetWidth, int targetHeight) {
        
        RelativePosition.AbsolutePosition position = component.getAbsolutePosition(targetWidth, targetHeight);
        
        logger.debug("计算显示位置: {} -> {} (目标分辨率: {}x{})", 
                    component.getPositionMode() == ComponentData.PositionMode.RELATIVE ? 
                        component.getRelativePosition() : 
                        String.format("abs(%d,%d,%d,%d)", component.getX(), component.getY(), component.getWidth(), component.getHeight()),
                    position, targetWidth, targetHeight);
        
        return position;
    }
    
    /**
     * 检测并自动转换旧格式数据
     */
    public static boolean autoConvertLegacyData(List<ComponentData> components, int editWidth, int editHeight) {
        boolean hasConverted = false;
        
        for (ComponentData component : components) {
            if (component.getPositionMode() == ComponentData.PositionMode.ABSOLUTE) {
                logger.info("检测到绝对坐标组件，自动转换为相对定位: ({},{}) -> 相对定位", 
                           component.getX(), component.getY());
                
                convertToRelativeMode(component, editWidth, editHeight);
                hasConverted = true;
            }
        }
        
        if (hasConverted) {
            logger.info("自动转换完成，建议保存项目以持久化更改");
        }
        
        return hasConverted;
    }
    
    /**
     * 验证相对位置的有效性
     */
    public static boolean validateRelativePosition(RelativePosition position) {
        if (position == null) {
            return false;
        }
        
        // 检查百分比范围
        if (position.getXPercent() < 0 || position.getXPercent() > 1 ||
            position.getYPercent() < 0 || position.getYPercent() > 1 ||
            position.getWidthPercent() <= 0 || position.getWidthPercent() > 1 ||
            position.getHeightPercent() <= 0 || position.getHeightPercent() > 1) {
            
            logger.warn("相对位置参数超出有效范围: {}", position);
            return false;
        }
        
        // 检查位置是否会超出边界
        if (position.getXPercent() + position.getWidthPercent() > 1 ||
            position.getYPercent() + position.getHeightPercent() > 1) {
            
            logger.warn("相对位置会超出容器边界: {}", position);
            return false;
        }
        
        return true;
    }
    
    /**
     * 修正超出边界的相对位置
     */
    public static RelativePosition fixBoundaryIssues(RelativePosition position) {
        if (position == null) {
            return new RelativePosition(0.1, 0.1, 0.2, 0.1); // 默认位置
        }
        
        RelativePosition fixed = new RelativePosition(
            Math.max(0, Math.min(1, position.getXPercent())),
            Math.max(0, Math.min(1, position.getYPercent())),
            Math.max(0.01, Math.min(1, position.getWidthPercent())),
            Math.max(0.01, Math.min(1, position.getHeightPercent()))
        );
        
        // 确保不超出右边界和下边界
        if (fixed.getXPercent() + fixed.getWidthPercent() > 1) {
            fixed.setXPercent(Math.max(0, 1 - fixed.getWidthPercent()));
        }
        
        if (fixed.getYPercent() + fixed.getHeightPercent() > 1) {
            fixed.setYPercent(Math.max(0, 1 - fixed.getHeightPercent()));
        }
        
        fixed.setAnchorType(position.getAnchorType());
        
        if (!position.equals(fixed)) {
            logger.info("修正了超出边界的相对位置: {} -> {}", position, fixed);
        }
        
        return fixed;
    }
    
    /**
     * 计算编辑界面中的显示位置
     * 将相对位置转换为编辑画布中的绝对坐标
     */
    public static RelativePosition.AbsolutePosition calculateEditPosition(
            ComponentData component, int canvasWidth, int canvasHeight, int editWidth, int editHeight) {
        
        // 首先获取在编辑分辨率下的绝对位置
        RelativePosition.AbsolutePosition editPos = component.getAbsolutePosition(editWidth, editHeight);
        
        // 然后缩放到画布显示尺寸
        double scaleX = (double) canvasWidth / editWidth;
        double scaleY = (double) canvasHeight / editHeight;
        
        int displayX = (int) (editPos.x * scaleX);
        int displayY = (int) (editPos.y * scaleY);
        int displayWidth = (int) (editPos.width * scaleX);
        int displayHeight = (int) (editPos.height * scaleY);
        
        RelativePosition.AbsolutePosition result = new RelativePosition.AbsolutePosition(
            displayX, displayY, displayWidth, displayHeight);
        
        logger.debug("计算编辑位置: 编辑分辨率{}x{} -> 画布{}x{}, {} -> {}", 
                    editWidth, editHeight, canvasWidth, canvasHeight, editPos, result);
        
        return result;
    }
    
    /**
     * 从编辑画布位置反向计算相对位置
     */
    public static RelativePosition calculateRelativeFromCanvas(
            int canvasX, int canvasY, int canvasWidth, int canvasHeight,
            int editCanvasWidth, int editCanvasHeight, int editWidth, int editHeight) {

        // 从画布坐标转换为编辑分辨率坐标
        double scaleX = (double) editWidth / editCanvasWidth;
        double scaleY = (double) editHeight / editCanvasHeight;

        int editX = (int) (canvasX * scaleX);
        int editY = (int) (canvasY * scaleY);
        int editW = (int) (canvasWidth * scaleX);
        int editH = (int) (canvasHeight * scaleY);

        logger.debug("坐标转换详情: 画布({},{},{},{}) 缩放比例({:.3f},{:.3f}) -> 编辑坐标({},{},{},{})",
                    canvasX, canvasY, canvasWidth, canvasHeight, scaleX, scaleY, editX, editY, editW, editH);

        // 确保坐标不超出编辑分辨率范围
        editX = Math.max(0, Math.min(editX, editWidth - editW));
        editY = Math.max(0, Math.min(editY, editHeight - editH));
        editW = Math.max(1, Math.min(editW, editWidth - editX));
        editH = Math.max(1, Math.min(editH, editHeight - editY));

        if (editW <= 2 || editH <= 2) {
            logger.warn("检测到极小尺寸组件: 编辑坐标({},{},{},{}) 可能导致显示问题", editX, editY, editW, editH);
        }

        // 转换为相对位置
        RelativePosition relative = RelativePosition.fromAbsolute(editX, editY, editW, editH, editWidth, editHeight);

        // 验证相对位置的有效性
        if (!validateRelativePosition(relative)) {
            logger.warn("计算出的相对位置无效，进行修正: {}", relative);
            relative = fixBoundaryIssues(relative);
        }

        logger.debug("从画布计算相对位置: 画布({},{},{},{}) -> 编辑({},{},{},{}) -> {}",
                    canvasX, canvasY, canvasWidth, canvasHeight, editX, editY, editW, editH, relative);

        return relative;
    }
}
