package com.feixiang.tabletcontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 页面数据模型
 * 用于存储单个页面的完整信息
 */
public class PageData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<ComponentData> components;
    private long createdTime;
    private long lastModifiedTime;
    
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
     * 更新最后修改时间
     */
    private void updateLastModifiedTime() {
        this.lastModifiedTime = System.currentTimeMillis();
    }
    
    /**
     * 创建页面数据的深拷贝
     */
    public PageData copy() {
        PageData copy = new PageData(this.name);
        copy.createdTime = this.createdTime;
        copy.lastModifiedTime = this.lastModifiedTime;
        
        for (ComponentData component : this.components) {
            copy.components.add(component.copy());
        }
        
        return copy;
    }
    
    @Override
    public String toString() {
        return "PageData{" +
                "name='" + name + '\'' +
                ", componentCount=" + components.size() +
                ", createdTime=" + new java.util.Date(createdTime) +
                ", lastModifiedTime=" + new java.util.Date(lastModifiedTime) +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PageData pageData = (PageData) obj;
        return name != null ? name.equals(pageData.name) : pageData.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
