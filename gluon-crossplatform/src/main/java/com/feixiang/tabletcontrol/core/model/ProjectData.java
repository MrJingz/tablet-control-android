package com.feixiang.tabletcontrol.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目数据模型 - 跨平台版本
 * 用于存储整个项目的完整信息，支持所有平台
 */
public class ProjectData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private List<String> pages;
    private String currentPage;
    private Map<String, PageData> pageContents;
    private long createdTime;
    private long lastModifiedTime;
    private String version;
    private String editResolution; // 编辑时的分辨率，用于跨平台适配
    
    // 默认构造函数
    public ProjectData() {
        this.name = "新建项目";
        this.description = "";
        this.pages = new ArrayList<>();
        this.pageContents = new HashMap<>();
        this.createdTime = System.currentTimeMillis();
        this.lastModifiedTime = this.createdTime;
        this.version = "1.0.0";
        this.editResolution = "1366x768"; // 默认分辨率
    }
    
    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        updateLastModifiedTime();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        updateLastModifiedTime();
    }

    public List<String> getPages() { return pages; }
    public void setPages(List<String> pages) { 
        this.pages = pages != null ? new ArrayList<>(pages) : new ArrayList<>();
        updateLastModifiedTime();
    }
    
    public String getCurrentPage() { return currentPage; }
    public void setCurrentPage(String currentPage) { 
        this.currentPage = currentPage; 
        updateLastModifiedTime();
    }
    
    public Map<String, PageData> getPageContents() { return pageContents; }
    public void setPageContents(Map<String, PageData> pageContents) { 
        this.pageContents = pageContents != null ? new HashMap<>(pageContents) : new HashMap<>();
        updateLastModifiedTime();
    }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public long getLastModifiedTime() { return lastModifiedTime; }
    public void setLastModifiedTime(long lastModifiedTime) { this.lastModifiedTime = lastModifiedTime; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getEditResolution() { return editResolution; }
    public void setEditResolution(String editResolution) { 
        this.editResolution = editResolution;
        updateLastModifiedTime();
    }
    
    // 业务方法
    
    /**
     * 添加页面
     */
    public void addPage(String pageName) {
        if (pageName != null && !pages.contains(pageName)) {
            pages.add(pageName);
            pageContents.put(pageName, new PageData(pageName));
            if (currentPage == null) {
                currentPage = pageName;
            }
            updateLastModifiedTime();
        }
    }
    
    /**
     * 添加页面数据
     */
    public void addPage(PageData pageData) {
        if (pageData != null && pageData.getName() != null) {
            String pageName = pageData.getName();
            if (!pages.contains(pageName)) {
                pages.add(pageName);
            }
            pageContents.put(pageName, pageData);
            if (currentPage == null) {
                currentPage = pageName;
            }
            updateLastModifiedTime();
        }
    }
    
    /**
     * 移除页面
     */
    public boolean removePage(String pageName) {
        if (pageName != null && pages.contains(pageName)) {
            pages.remove(pageName);
            pageContents.remove(pageName);
            
            // 如果删除的是当前页面，切换到第一个页面
            if (pageName.equals(currentPage)) {
                currentPage = pages.isEmpty() ? null : pages.get(0);
            }
            
            updateLastModifiedTime();
            return true;
        }
        return false;
    }
    
    /**
     * 获取页面数据
     */
    public PageData getPageData(String pageName) {
        return pageContents.get(pageName);
    }

    /**
     * 获取页面数据（别名方法，用于兼容）
     */
    public PageData getPage(String pageName) {
        return getPageData(pageName);
    }
    
    /**
     * 获取当前页面数据
     */
    public PageData getCurrentPageData() {
        return currentPage != null ? pageContents.get(currentPage) : null;
    }
    
    /**
     * 检查页面是否存在
     */
    public boolean hasPage(String pageName) {
        return pages.contains(pageName);
    }
    
    /**
     * 获取页面数量
     */
    public int getPageCount() {
        return pages.size();
    }
    
    /**
     * 检查是否为空项目
     */
    public boolean isEmpty() {
        return pages.isEmpty();
    }
    
    /**
     * 重命名页面
     */
    public boolean renamePage(String oldName, String newName) {
        if (oldName != null && newName != null && pages.contains(oldName) && !pages.contains(newName)) {
            int index = pages.indexOf(oldName);
            pages.set(index, newName);
            
            PageData pageData = pageContents.remove(oldName);
            if (pageData != null) {
                pageData.setName(newName);
                pageContents.put(newName, pageData);
            }
            
            if (oldName.equals(currentPage)) {
                currentPage = newName;
            }
            
            updateLastModifiedTime();
            return true;
        }
        return false;
    }
    
    /**
     * 获取总组件数量（跨平台统计）
     */
    public int getTotalComponentCount() {
        int count = 0;
        for (PageData pageData : pageContents.values()) {
            if (pageData != null && pageData.getComponents() != null) {
                count += pageData.getComponents().size();
            }
        }
        return count;
    }
    
    /**
     * 获取项目摘要信息
     */
    public String getProjectSummary() {
        return String.format("项目: %d页面, %d组件, 版本: %s, 编辑分辨率: %s", 
                            getPageCount(), getTotalComponentCount(), version, editResolution);
    }
    
    /**
     * 检查项目数据完整性
     */
    public boolean validateIntegrity() {
        // 检查基本数据完整性
        if (pages == null || pageContents == null) {
            return false;
        }
        
        // 检查页面数据一致性
        for (String pageName : pages) {
            if (!pageContents.containsKey(pageName)) {
                return false;
            }
        }
        
        // 检查当前页面是否有效
        if (currentPage != null && !pages.contains(currentPage)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 修复项目数据完整性
     */
    public void repairIntegrity() {
        if (pages == null) {
            pages = new ArrayList<>();
        }
        if (pageContents == null) {
            pageContents = new HashMap<>();
        }
        
        // 移除无效的页面引用
        pages.removeIf(pageName -> !pageContents.containsKey(pageName));
        
        // 移除无效的页面数据
        pageContents.entrySet().removeIf(entry -> !pages.contains(entry.getKey()));
        
        // 修复当前页面
        if (currentPage != null && !pages.contains(currentPage)) {
            currentPage = pages.isEmpty() ? null : pages.get(0);
        }
        
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
        return "ProjectData{" +
                "pageCount=" + pages.size() +
                ", componentCount=" + getTotalComponentCount() +
                ", currentPage='" + currentPage + '\'' +
                ", version='" + version + '\'' +
                ", editResolution='" + editResolution + '\'' +
                ", createdTime=" + new java.util.Date(createdTime) +
                ", lastModifiedTime=" + new java.util.Date(lastModifiedTime) +
                '}';
    }
}
