package com.feixiang.tabletcontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目数据模型
 * 用于存储整个项目的完整信息
 */
public class ProjectData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<String> pages;
    private String currentPage;
    private Map<String, PageData> pageContents;
    private long createdTime;
    private long lastModifiedTime;
    private String version;
    
    // 默认构造函数
    public ProjectData() {
        this.pages = new ArrayList<>();
        this.pageContents = new HashMap<>();
        this.createdTime = System.currentTimeMillis();
        this.lastModifiedTime = this.createdTime;
        this.version = "1.0.0";
    }
    
    // Getter和Setter方法
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
     * 更新最后修改时间
     */
    private void updateLastModifiedTime() {
        this.lastModifiedTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "ProjectData{" +
                "pageCount=" + pages.size() +
                ", currentPage='" + currentPage + '\'' +
                ", version='" + version + '\'' +
                ", createdTime=" + new java.util.Date(createdTime) +
                ", lastModifiedTime=" + new java.util.Date(lastModifiedTime) +
                '}';
    }
}
