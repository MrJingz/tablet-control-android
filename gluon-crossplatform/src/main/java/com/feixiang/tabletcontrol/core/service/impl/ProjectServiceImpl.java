package com.feixiang.tabletcontrol.core.service.impl;

import com.feixiang.tabletcontrol.core.model.ComponentData;
import com.feixiang.tabletcontrol.core.model.PageData;
import com.feixiang.tabletcontrol.core.model.ProjectData;
import com.feixiang.tabletcontrol.core.repository.ProjectRepository;
import com.feixiang.tabletcontrol.core.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 项目服务实现类 - 完整功能版本
 * 提供完整的项目管理功能，包括数据持久化、缓存管理、并发控制等
 */
public class ProjectServiceImpl implements ProjectService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    
    private final ProjectRepository projectRepository;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 当前状态
    private ProjectData currentProject;
    private String currentPageName;
    private boolean hasUnsavedChanges = false;
    private long lastSavedTime = 0;
    
    // 缓存
    private final Object cacheLock = new Object();
    
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        logger.info("项目服务初始化完成");
    }
    
    // 项目操作
    
    @Override
    public ProjectData createNewProject() {
        lock.writeLock().lock();
        try {
            logger.info("创建新项目");
            
            ProjectData newProject = new ProjectData();
            newProject.setName("新项目");
            newProject.setDescription("跨平台中控项目");
            
            // 创建默认页面
            PageData defaultPage = new PageData("主页面");
            newProject.addPage(defaultPage);
            newProject.setCurrentPage("主页面");
            
            this.currentProject = newProject;
            this.currentPageName = "主页面";
            this.hasUnsavedChanges = true;
            
            logger.info("新项目创建完成: {}", newProject.getProjectSummary());
            return newProject;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public ProjectData loadProject() throws IOException {
        lock.writeLock().lock();
        try {
            logger.info("加载项目");
            
            ProjectData project = projectRepository.loadProject();
            if (project == null) {
                logger.info("项目文件不存在，创建新项目");
                project = createNewProject();
            } else {
                // 验证和修复项目数据
                if (!project.validateIntegrity()) {
                    logger.warn("项目数据完整性检查失败，正在修复");
                    project.repairIntegrity();
                }
                
                this.currentProject = project;
                this.currentPageName = project.getCurrentPage();
                this.hasUnsavedChanges = false;
                this.lastSavedTime = System.currentTimeMillis();
                
                logger.info("项目加载完成: {}", project.getProjectSummary());
            }
            
            return project;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void saveProject(ProjectData projectData) throws IOException {
        lock.readLock().lock();
        try {
            logger.info("保存项目: {}", projectData.getProjectSummary());
            
            // 验证项目数据
            if (!projectData.validateIntegrity()) {
                logger.warn("项目数据完整性检查失败，正在修复");
                projectData.repairIntegrity();
            }
            
            // 更新时间戳
            projectData.setLastModifiedTime(System.currentTimeMillis());
            
            // 保存到存储库
            projectRepository.saveProject(projectData);
            
            // 更新状态
            if (projectData == this.currentProject) {
                this.hasUnsavedChanges = false;
                this.lastSavedTime = System.currentTimeMillis();
            }
            
            logger.info("项目保存完成");
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void saveCurrentProject() throws IOException {
        if (currentProject != null) {
            saveProject(currentProject);
        } else {
            logger.warn("没有当前项目需要保存");
        }
    }
    
    @Override
    public ProjectData getCurrentProject() {
        lock.readLock().lock();
        try {
            return currentProject;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void setCurrentProject(ProjectData projectData) {
        lock.writeLock().lock();
        try {
            this.currentProject = projectData;
            if (projectData != null) {
                this.currentPageName = projectData.getCurrentPage();
            }
            this.hasUnsavedChanges = true;
            logger.info("设置当前项目: {}", projectData != null ? projectData.getProjectSummary() : "null");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // 页面操作
    
    @Override
    public PageData createPage(String pageName) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                currentProject = createNewProject();
            }
            
            logger.info("创建页面: {}", pageName);
            
            // 检查页面是否已存在
            if (currentProject.hasPage(pageName)) {
                throw new IllegalArgumentException("页面已存在: " + pageName);
            }
            
            PageData newPage = new PageData(pageName);
            currentProject.addPage(newPage);
            this.hasUnsavedChanges = true;
            
            logger.info("页面创建完成: {}", pageName);
            return newPage;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean deletePage(String pageName) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                return false;
            }
            
            logger.info("删除页面: {}", pageName);
            
            // 不能删除最后一个页面
            if (currentProject.getPageCount() <= 1) {
                logger.warn("不能删除最后一个页面");
                return false;
            }
            
            boolean removed = currentProject.removePage(pageName);
            if (removed) {
                // 如果删除的是当前页面，切换到第一个页面
                if (pageName.equals(currentPageName)) {
                    List<String> pages = currentProject.getPages();
                    if (!pages.isEmpty()) {
                        setCurrentPage(pages.get(0));
                    }
                }
                this.hasUnsavedChanges = true;
                logger.info("页面删除完成: {}", pageName);
            }
            
            return removed;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean renamePage(String oldName, String newName) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                return false;
            }
            
            logger.info("重命名页面: {} -> {}", oldName, newName);
            
            // 检查新名称是否已存在
            if (currentProject.hasPage(newName)) {
                logger.warn("页面名称已存在: {}", newName);
                return false;
            }
            
            boolean renamed = currentProject.renamePage(oldName, newName);
            if (renamed) {
                // 更新当前页面名称
                if (oldName.equals(currentPageName)) {
                    this.currentPageName = newName;
                }
                this.hasUnsavedChanges = true;
                logger.info("页面重命名完成: {} -> {}", oldName, newName);
            }
            
            return renamed;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public PageData getPage(String pageName) {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                return null;
            }
            return currentProject.getPage(pageName);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<String> getAllPageNames() {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(currentProject.getPages());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void setCurrentPage(String pageName) {
        lock.writeLock().lock();
        try {
            if (currentProject != null && currentProject.hasPage(pageName)) {
                this.currentPageName = pageName;
                currentProject.setCurrentPage(pageName);
                logger.info("切换到页面: {}", pageName);
            } else {
                logger.warn("页面不存在: {}", pageName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public String getCurrentPageName() {
        lock.readLock().lock();
        try {
            return currentPageName;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public PageData getCurrentPage() {
        lock.readLock().lock();
        try {
            if (currentProject != null && currentPageName != null) {
                return currentProject.getPage(currentPageName);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    // 组件操作

    @Override
    public void addComponent(String pageName, ComponentData component) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                currentProject = createNewProject();
            }

            PageData page = currentProject.getPage(pageName);
            if (page != null) {
                page.addComponent(component);
                this.hasUnsavedChanges = true;
                logger.info("添加组件到页面 {}: {}", pageName, component.getComponentSummary());
            } else {
                logger.warn("页面不存在: {}", pageName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void addComponentToCurrentPage(ComponentData component) {
        if (currentPageName != null) {
            addComponent(currentPageName, component);
        } else {
            logger.warn("没有当前页面");
        }
    }

    @Override
    public boolean removeComponent(String pageName, ComponentData component) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                return false;
            }

            PageData page = currentProject.getPage(pageName);
            if (page != null) {
                boolean removed = page.removeComponent(component);
                if (removed) {
                    this.hasUnsavedChanges = true;
                    logger.info("从页面 {} 移除组件: {}", pageName, component.getComponentSummary());
                }
                return removed;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeComponentFromCurrentPage(ComponentData component) {
        if (currentPageName != null) {
            return removeComponent(currentPageName, component);
        }
        return false;
    }

    @Override
    public boolean updateComponent(String pageName, ComponentData oldComponent, ComponentData newComponent) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                return false;
            }

            PageData page = currentProject.getPage(pageName);
            if (page != null) {
                boolean updated = page.replaceComponent(oldComponent, newComponent);
                if (updated) {
                    this.hasUnsavedChanges = true;
                    logger.info("更新页面 {} 的组件: {} -> {}", pageName,
                              oldComponent.getComponentSummary(), newComponent.getComponentSummary());
                }
                return updated;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<ComponentData> getPageComponents(String pageName) {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                return new ArrayList<>();
            }

            PageData page = currentProject.getPage(pageName);
            if (page != null) {
                return new ArrayList<>(page.getComponents());
            }
            return new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ComponentData> getCurrentPageComponents() {
        if (currentPageName != null) {
            return getPageComponents(currentPageName);
        }
        return new ArrayList<>();
    }

    @Override
    public void clearPageComponents(String pageName) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                return;
            }

            PageData page = currentProject.getPage(pageName);
            if (page != null) {
                page.clearComponents();
                this.hasUnsavedChanges = true;
                logger.info("清空页面 {} 的所有组件", pageName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clearCurrentPageComponents() {
        if (currentPageName != null) {
            clearPageComponents(currentPageName);
        }
    }

    // 数据管理

    @Override
    public void backupProject(String backupName) throws IOException {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                throw new IllegalStateException("没有项目需要备份");
            }

            logger.info("备份项目: {}", backupName);
            projectRepository.backupProject(currentProject, backupName);
            logger.info("项目备份完成: {}", backupName);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ProjectData restoreProject(String backupName) throws IOException {
        lock.writeLock().lock();
        try {
            logger.info("从备份恢复项目: {}", backupName);
            ProjectData restoredProject = projectRepository.restoreProject(backupName);

            if (restoredProject != null) {
                this.currentProject = restoredProject;
                this.currentPageName = restoredProject.getCurrentPage();
                this.hasUnsavedChanges = true;
                logger.info("项目恢复完成: {}", restoredProject.getProjectSummary());
            }

            return restoredProject;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean hasUnsavedChanges() {
        lock.readLock().lock();
        try {
            return hasUnsavedChanges;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void markAsSaved() {
        lock.writeLock().lock();
        try {
            this.hasUnsavedChanges = false;
            this.lastSavedTime = System.currentTimeMillis();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void markAsModified() {
        lock.writeLock().lock();
        try {
            this.hasUnsavedChanges = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String getProjectStatistics() {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                return "无项目数据";
            }

            int pageCount = currentProject.getPageCount();
            int totalComponents = 0;

            for (String pageName : currentProject.getPages()) {
                PageData page = currentProject.getPage(pageName);
                if (page != null) {
                    totalComponents += page.getComponentCount();
                }
            }

            return String.format("项目统计: %d个页面, %d个组件, 最后修改: %s",
                               pageCount, totalComponents,
                               new java.util.Date(currentProject.getLastModifiedTime()));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void adaptProjectToResolution(int targetWidth, int targetHeight) {
        lock.writeLock().lock();
        try {
            if (currentProject == null) {
                return;
            }

            logger.info("适配项目到分辨率: {}x{}", targetWidth, targetHeight);

            for (String pageName : currentProject.getPages()) {
                PageData page = currentProject.getPage(pageName);
                if (page != null) {
                    for (ComponentData component : page.getComponents()) {
                        // 更新相对位置
                        component.updateRelativePosition(targetWidth, targetHeight);
                    }
                }
            }

            this.hasUnsavedChanges = true;
            logger.info("项目分辨率适配完成");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean validateProjectIntegrity() {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                return false;
            }
            return currentProject.validateIntegrity();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void repairProjectIntegrity() {
        lock.writeLock().lock();
        try {
            if (currentProject != null) {
                logger.info("修复项目数据完整性");
                currentProject.repairIntegrity();
                this.hasUnsavedChanges = true;
                logger.info("项目数据完整性修复完成");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void exportProject(String exportPath) throws IOException {
        lock.readLock().lock();
        try {
            if (currentProject == null) {
                throw new IllegalStateException("没有项目需要导出");
            }

            logger.info("导出项目到: {}", exportPath);
            projectRepository.exportProject(currentProject, exportPath);
            logger.info("项目导出完成");
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ProjectData importProject(String importPath) throws IOException {
        lock.writeLock().lock();
        try {
            logger.info("从文件导入项目: {}", importPath);
            ProjectData importedProject = projectRepository.importProject(importPath);

            if (importedProject != null) {
                this.currentProject = importedProject;
                this.currentPageName = importedProject.getCurrentPage();
                this.hasUnsavedChanges = true;
                logger.info("项目导入完成: {}", importedProject.getProjectSummary());
            }

            return importedProject;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long getLastSavedTime() {
        lock.readLock().lock();
        try {
            return lastSavedTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long getProjectCreatedTime() {
        lock.readLock().lock();
        try {
            if (currentProject != null) {
                return currentProject.getCreatedTime();
            }
            return 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long getProjectLastModifiedTime() {
        lock.readLock().lock();
        try {
            if (currentProject != null) {
                return currentProject.getLastModifiedTime();
            }
            return 0;
        } finally {
            lock.readLock().unlock();
        }
    }
}
