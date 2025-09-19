package com.feixiang.tabletcontrol.service.impl;

import com.feixiang.tabletcontrol.model.ComponentData;
import com.feixiang.tabletcontrol.model.PageData;
import com.feixiang.tabletcontrol.model.ProjectData;
import com.feixiang.tabletcontrol.repository.ProjectRepository;
import com.feixiang.tabletcontrol.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目服务实现类
 * 实现项目管理的业务逻辑
 */
public class ProjectServiceImpl implements ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    
    private final ProjectRepository projectRepository;
    private ProjectData currentProject;
    private boolean hasUnsavedChanges = false;
    private long lastSavedTime = 0;
    
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        logger.info("ProjectService初始化完成");
    }
    
    @Override
    public ProjectData createNewProject() {
        long startTime = System.currentTimeMillis();
        logger.info("创建新项目");
        
        ProjectData newProject = new ProjectData();
        this.currentProject = newProject;
        this.hasUnsavedChanges = true;
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("新项目创建完成，耗时: {}ms", duration);
        
        return newProject;
    }
    
    @Override
    public ProjectData loadProject() throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("开始加载项目数据");
        
        try {
            ProjectData loadedProject = projectRepository.load();
            
            if (loadedProject == null) {
                logger.info("项目数据不存在，创建新项目");
                loadedProject = createNewProject();
                this.hasUnsavedChanges = false; // 新项目不需要保存
            } else {
                this.currentProject = loadedProject;
                this.hasUnsavedChanges = false;
                this.lastSavedTime = System.currentTimeMillis();
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("项目数据加载完成，耗时: {}ms，页面数量: {}, 总组件数量: {}", 
                           duration, loadedProject.getPageCount(), getTotalComponentCount(loadedProject));
            }
            
            return loadedProject;
            
        } catch (IOException e) {
            logger.error("加载项目数据失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void saveProject(ProjectData projectData) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("开始保存项目数据，页面数量: {}", projectData.getPageCount());
        
        try {
            // 记录保存前的状态
            String beforeStats = getProjectStatistics();
            
            projectRepository.save(projectData);
            
            this.hasUnsavedChanges = false;
            this.lastSavedTime = System.currentTimeMillis();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("项目数据保存完成，耗时: {}ms", duration);
            logger.info("保存前状态: {}", beforeStats);
            logger.info("保存后文件大小: {}字节", projectRepository.getFileSize());
            
        } catch (IOException e) {
            logger.error("保存项目数据失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ProjectData getCurrentProject() {
        return currentProject;
    }
    
    @Override
    public void setCurrentProject(ProjectData projectData) {
        ProjectData oldProject = this.currentProject;
        this.currentProject = projectData;
        this.hasUnsavedChanges = true;
        
        logger.info("当前项目已更新，旧项目页面数: {}, 新项目页面数: {}", 
                   oldProject != null ? oldProject.getPageCount() : 0,
                   projectData != null ? projectData.getPageCount() : 0);
    }
    
    @Override
    public PageData createPage(String pageName) {
        long startTime = System.currentTimeMillis();
        logger.info("创建新页面: {}", pageName);
        
        if (currentProject == null) {
            currentProject = createNewProject();
        }
        
        if (currentProject.hasPage(pageName)) {
            logger.warn("页面已存在: {}", pageName);
            return currentProject.getPageData(pageName);
        }
        
        PageData newPage = new PageData(pageName);
        currentProject.addPage(newPage);
        this.hasUnsavedChanges = true;
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("页面创建完成: {}, 耗时: {}ms", pageName, duration);
        
        return newPage;
    }
    
    @Override
    public boolean deletePage(String pageName) {
        logger.info("删除页面: {}", pageName);
        
        if (currentProject == null) {
            logger.warn("当前项目为空，无法删除页面");
            return false;
        }
        
        if (!currentProject.hasPage(pageName)) {
            logger.warn("页面不存在: {}", pageName);
            return false;
        }
        
        // 记录删除前的组件数量
        PageData pageToDelete = currentProject.getPageData(pageName);
        int componentCount = pageToDelete != null ? pageToDelete.getComponentCount() : 0;
        
        boolean deleted = currentProject.removePage(pageName);
        if (deleted) {
            this.hasUnsavedChanges = true;
            logger.info("页面删除成功: {}, 删除了{}个组件", pageName, componentCount);
        }
        
        return deleted;
    }
    
    @Override
    public boolean renamePage(String oldName, String newName) {
        logger.info("重命名页面: {} -> {}", oldName, newName);
        
        if (currentProject == null) {
            logger.warn("当前项目为空，无法重命名页面");
            return false;
        }
        
        boolean renamed = currentProject.renamePage(oldName, newName);
        if (renamed) {
            this.hasUnsavedChanges = true;
            logger.info("页面重命名成功: {} -> {}", oldName, newName);
        } else {
            logger.warn("页面重命名失败: {} -> {}", oldName, newName);
        }
        
        return renamed;
    }
    
    @Override
    public PageData getPage(String pageName) {
        if (currentProject == null) {
            return null;
        }
        return currentProject.getPageData(pageName);
    }
    
    @Override
    public List<String> getAllPageNames() {
        if (currentProject == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(currentProject.getPages());
    }
    
    @Override
    public void setCurrentPage(String pageName) {
        logger.info("设置当前页面: {}", pageName);
        
        if (currentProject == null) {
            logger.warn("当前项目为空，无法设置当前页面");
            return;
        }
        
        String oldCurrentPage = currentProject.getCurrentPage();
        currentProject.setCurrentPage(pageName);
        this.hasUnsavedChanges = true;
        
        logger.info("当前页面已更新: {} -> {}", oldCurrentPage, pageName);
    }
    
    @Override
    public String getCurrentPageName() {
        return currentProject != null ? currentProject.getCurrentPage() : null;
    }
    
    @Override
    public PageData getCurrentPage() {
        return currentProject != null ? currentProject.getCurrentPageData() : null;
    }

    @Override
    public void addComponent(String pageName, ComponentData component) {
        logger.info("添加组件到页面: {}, 组件类型: {}, 位置: ({}, {})",
                   pageName, component.getFunctionType(), component.getX(), component.getY());

        if (currentProject == null) {
            logger.warn("当前项目为空，无法添加组件");
            return;
        }

        PageData page = currentProject.getPageData(pageName);
        if (page == null) {
            logger.info("页面不存在，创建新页面: {}", pageName);
            page = createPage(pageName);
        }

        page.addComponent(component);
        this.hasUnsavedChanges = true;

        logger.info("组件添加成功，页面{}现有{}个组件", pageName, page.getComponentCount());
    }

    @Override
    public boolean removeComponent(String pageName, ComponentData component) {
        logger.info("从页面移除组件: {}, 组件类型: {}", pageName, component.getFunctionType());

        if (currentProject == null) {
            logger.warn("当前项目为空，无法移除组件");
            return false;
        }

        PageData page = currentProject.getPageData(pageName);
        if (page == null) {
            logger.warn("页面不存在: {}", pageName);
            return false;
        }

        boolean removed = page.removeComponent(component);
        if (removed) {
            this.hasUnsavedChanges = true;
            logger.info("组件移除成功，页面{}现有{}个组件", pageName, page.getComponentCount());
        } else {
            logger.warn("组件移除失败，组件不存在于页面: {}", pageName);
        }

        return removed;
    }

    @Override
    public boolean updateComponent(String pageName, ComponentData oldComponent, ComponentData newComponent) {
        logger.info("更新页面组件: {}, 旧组件类型: {}, 新组件类型: {}",
                   pageName, oldComponent.getFunctionType(), newComponent.getFunctionType());

        if (currentProject == null) {
            logger.warn("当前项目为空，无法更新组件");
            return false;
        }

        PageData page = currentProject.getPageData(pageName);
        if (page == null) {
            logger.warn("页面不存在: {}", pageName);
            return false;
        }

        List<ComponentData> components = page.getComponents();
        int index = components.indexOf(oldComponent);
        if (index >= 0) {
            components.set(index, newComponent);
            this.hasUnsavedChanges = true;
            logger.info("组件更新成功: {}", pageName);
            return true;
        } else {
            logger.warn("组件更新失败，旧组件不存在于页面: {}", pageName);
            return false;
        }
    }

    @Override
    public List<ComponentData> getPageComponents(String pageName) {
        if (currentProject == null) {
            return new ArrayList<>();
        }

        PageData page = currentProject.getPageData(pageName);
        if (page == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(page.getComponents());
    }

    @Override
    public void clearPageComponents(String pageName) {
        logger.info("清空页面组件: {}", pageName);

        if (currentProject == null) {
            logger.warn("当前项目为空，无法清空组件");
            return;
        }

        PageData page = currentProject.getPageData(pageName);
        if (page == null) {
            logger.warn("页面不存在: {}", pageName);
            return;
        }

        int componentCount = page.getComponentCount();
        page.clearComponents();
        this.hasUnsavedChanges = true;

        logger.info("页面组件清空完成: {}, 清空了{}个组件", pageName, componentCount);
    }

    @Override
    public void backupProject(String backupName) throws IOException {
        logger.info("备份项目数据: {}", backupName);

        try {
            projectRepository.backup(backupName);
            logger.info("项目数据备份完成: {}", backupName);
        } catch (IOException e) {
            logger.error("项目数据备份失败: {}, 错误: {}", backupName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ProjectData restoreProject(String backupName) throws IOException {
        logger.info("从备份恢复项目数据: {}", backupName);

        try {
            ProjectData restoredProject = projectRepository.restore(backupName);
            this.currentProject = restoredProject;
            this.hasUnsavedChanges = false;
            this.lastSavedTime = System.currentTimeMillis();

            logger.info("项目数据恢复完成: {}, 页面数量: {}", backupName, restoredProject.getPageCount());
            return restoredProject;

        } catch (IOException e) {
            logger.error("项目数据恢复失败: {}, 错误: {}", backupName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    @Override
    public void markAsSaved() {
        this.hasUnsavedChanges = false;
        this.lastSavedTime = System.currentTimeMillis();
        logger.debug("项目标记为已保存");
    }

    @Override
    public void markAsModified() {
        this.hasUnsavedChanges = true;
        logger.debug("项目标记为已修改");
    }

    @Override
    public String getProjectStatistics() {
        if (currentProject == null) {
            return "项目统计: 无项目数据";
        }

        int totalComponents = getTotalComponentCount(currentProject);
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return String.format("项目统计: 页面数量=%d, 总组件数量=%d, 当前页面=%s, 未保存更改=%s, 内存使用=%.2fMB",
                           currentProject.getPageCount(),
                           totalComponents,
                           currentProject.getCurrentPage(),
                           hasUnsavedChanges ? "是" : "否",
                           memoryUsage / 1024.0 / 1024.0);
    }

    /**
     * 计算项目中的总组件数量
     */
    private int getTotalComponentCount(ProjectData projectData) {
        int total = 0;
        for (PageData page : projectData.getPageContents().values()) {
            total += page.getComponentCount();
        }
        return total;
    }
}
