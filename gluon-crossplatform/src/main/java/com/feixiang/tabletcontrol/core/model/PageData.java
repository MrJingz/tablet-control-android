package com.feixiang.tabletcontrol.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 页面数据模型 - 跨平台版本
 * 用于存储单个页面的完整信息，支持跨平台适配
 */
public class PageData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<ComponentData> components;
    private long createdTime;
    private long lastModifiedTime;
    private String backgroundImage; // 背景图片路径
    private String backgroundColor; // 背景颜色
    
    // 默认构造函数
    public PageData() {
        this.components = new ArrayList<>();
        this.createdTime = System.currentTimeMillis();
        this.lastModifiedTime = this.createdTime;
    }
    
    // 构造函数
    public PageData(String name) {
        this();
        this.name = name;
    }
    
    // 构造函数
    public PageData(String name, List<ComponentData> components) {
        this(name);
        this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
    }
    
    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        updateLastModifiedTime();
    }
    
    public List<ComponentData> getComponents() { return components; }
    public void setComponents(List<ComponentData> components) { 
        this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
        updateLastModifiedTime();
    }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public long getLastModifiedTime() { return lastModifiedTime; }
    public void setLastModifiedTime(long lastModifiedTime) { this.lastModifiedTime = lastModifiedTime; }
    
    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { 
        this.backgroundImage = backgroundImage;
        updateLastModifiedTime();
    }
    
    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { 
        this.backgroundColor = backgroundColor;
        updateLastModifiedTime();
    }
    
    // 业务方法
    
    /**
     * 添加组件
     */
    public void addComponent(ComponentData component) {
        if (component != null) {
            this.components.add(component);
            updateLastModifiedTime();
        }
    }
    
    /**
     * 移除组件
     */
    public boolean removeComponent(ComponentData component) {
        boolean removed = this.components.remove(component);
        if (removed) {
            updateLastModifiedTime();
        }
        return removed;
    }
    
    /**
     * 根据索引移除组件
     */
    public ComponentData removeComponent(int index) {
        if (index >= 0 && index < components.size()) {
            ComponentData removed = components.remove(index);
            updateLastModifiedTime();
            return removed;
        }
        return null;
    }
    
    /**
     * 清空所有组件
     */
    public void clearComponents() {
        this.components.clear();
        updateLastModifiedTime();
    }
    
    /**
     * 获取组件数量
     */
    public int getComponentCount() {
        return components.size();
    }
    
    /**
     * 检查是否为空页面
     */
    public boolean isEmpty() {
        return components.isEmpty();
    }
    
    /**
     * 根据索引获取组件
     */
    public ComponentData getComponent(int index) {
        if (index >= 0 && index < components.size()) {
            return components.get(index);
        }
        return null;
    }
    
    /**
     * 查找组件索引
     */
    public int findComponentIndex(ComponentData component) {
        return components.indexOf(component);
    }
    
    /**
     * 替换组件
     */
    public boolean replaceComponent(int index, ComponentData newComponent) {
        if (index >= 0 && index < components.size() && newComponent != null) {
            components.set(index, newComponent);
            updateLastModifiedTime();
            return true;
        }
        return false;
    }
    
    /**
     * 替换组件
     */
    public boolean replaceComponent(ComponentData oldComponent, ComponentData newComponent) {
        int index = findComponentIndex(oldComponent);
        if (index >= 0) {
            return replaceComponent(index, newComponent);
        }
        return false;
    }
    
    /**
     * 移动组件位置
     */
    public boolean moveComponent(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < components.size() && 
            toIndex >= 0 && toIndex < components.size() && 
            fromIndex != toIndex) {
            
            ComponentData component = components.remove(fromIndex);
            components.add(toIndex, component);
            updateLastModifiedTime();
            return true;
        }
        return false;
    }
    
    /**
     * 复制组件
     */
    public ComponentData duplicateComponent(int index) {
        ComponentData original = getComponent(index);
        if (original != null) {
            try {
                // 简单的深拷贝实现
                ComponentData copy = new ComponentData();
                copy.setFunctionType(original.getFunctionType());
                copy.setX(original.getX() + 10); // 稍微偏移位置
                copy.setY(original.getY() + 10);
                copy.setWidth(original.getWidth());
                copy.setHeight(original.getHeight());
                copy.setOriginalWidth(original.getOriginalWidth());
                copy.setOriginalHeight(original.getOriginalHeight());
                
                // 复制标签数据
                if (original.getLabelData() != null) {
                    LabelData labelCopy = new LabelData();
                    labelCopy.setText(original.getLabelData().getText());
                    labelCopy.setFontName(original.getLabelData().getFontName());
                    labelCopy.setFontSize(original.getLabelData().getFontSize());
                    labelCopy.setFontStyle(original.getLabelData().getFontStyle());
                    labelCopy.setColorRGB(original.getLabelData().getColorRGB());
                    labelCopy.setIconPath(original.getLabelData().getIconPath());
                    copy.setLabelData(labelCopy);
                }
                
                // 复制相对位置信息
                if (original.getRelativePosition() != null) {
                    RelativePosition relPosCopy = new RelativePosition();
                    relPosCopy.setRelativeX(original.getRelativePosition().getRelativeX());
                    relPosCopy.setRelativeY(original.getRelativePosition().getRelativeY());
                    relPosCopy.setRelativeWidth(original.getRelativePosition().getRelativeWidth());
                    relPosCopy.setRelativeHeight(original.getRelativePosition().getRelativeHeight());
                    copy.setRelativePosition(relPosCopy);
                }
                
                copy.setPositionMode(original.getPositionMode());
                
                addComponent(copy);
                return copy;
                
            } catch (Exception e) {
                // 复制失败，返回null
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取页面摘要信息
     */
    public String getPageSummary() {
        return String.format("页面: %s, 组件数量: %d, 最后修改: %s", 
                            name, getComponentCount(), 
                            new java.util.Date(lastModifiedTime));
    }
    
    /**
     * 按功能类型统计组件
     */
    public java.util.Map<String, Integer> getComponentStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        for (ComponentData component : components) {
            String type = component.getFunctionType();
            if (type != null) {
                stats.put(type, stats.getOrDefault(type, 0) + 1);
            }
        }
        return stats;
    }
    
    /**
     * 验证页面数据完整性
     */
    public boolean validateIntegrity() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        if (components == null) {
            return false;
        }
        
        // 检查每个组件的完整性
        for (ComponentData component : components) {
            if (component == null || component.getFunctionType() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 修复页面数据完整性
     */
    public void repairIntegrity() {
        if (name == null) {
            name = "未命名页面";
        }
        
        if (components == null) {
            components = new ArrayList<>();
        }
        
        // 移除无效组件
        components.removeIf(component -> 
            component == null || component.getFunctionType() == null);
        
        updateLastModifiedTime();
    }
    
    /**
     * 更新最后修改时间
     */
    private void updateLastModifiedTime() {
        this.lastModifiedTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "PageData{" +
                "name='" + name + '\'' +
                ", componentCount=" + getComponentCount() +
                ", backgroundImage='" + backgroundImage + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", createdTime=" + new java.util.Date(createdTime) +
                ", lastModifiedTime=" + new java.util.Date(lastModifiedTime) +
                '}';
    }
}
