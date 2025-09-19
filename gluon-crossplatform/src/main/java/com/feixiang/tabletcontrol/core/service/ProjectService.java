package com.feixiang.tabletcontrol.core.service;

import com.feixiang.tabletcontrol.core.model.ComponentData;
import com.feixiang.tabletcontrol.core.model.PageData;
import com.feixiang.tabletcontrol.core.model.ProjectData;

import java.io.IOException;
import java.util.List;

/**
 * 项目服务接口 - 跨平台版本
 * 定义项目管理的业务逻辑操作，支持跨平台数据管理
 */
public interface ProjectService {
    
    // 项目操作
    
    /**
     * 创建新项目
     * @return 新创建的项目数据
     */
    ProjectData createNewProject();
    
    /**
     * 加载项目
     * @return 加载的项目数据，如果不存在则返回新项目
     * @throws IOException 加载失败时抛出异常
     */
    ProjectData loadProject() throws IOException;
    
    /**
     * 保存项目
     * @param projectData 要保存的项目数据
     * @throws IOException 保存失败时抛出异常
     */
    void saveProject(ProjectData projectData) throws IOException;
    
    /**
     * 保存当前项目
     * @throws IOException 保存失败时抛出异常
     */
    void saveCurrentProject() throws IOException;
    
    /**
     * 获取当前项目
     * @return 当前项目数据
     */
    ProjectData getCurrentProject();
    
    /**
     * 设置当前项目
     * @param projectData 项目数据
     */
    void setCurrentProject(ProjectData projectData);
    
    // 页面操作
    
    /**
     * 创建新页面
     * @param pageName 页面名称
     * @return 创建的页面数据
     */
    PageData createPage(String pageName);
    
    /**
     * 删除页面
     * @param pageName 页面名称
     * @return 如果删除成功则返回true
     */
    boolean deletePage(String pageName);
    
    /**
     * 重命名页面
     * @param oldName 旧名称
     * @param newName 新名称
     * @return 如果重命名成功则返回true
     */
    boolean renamePage(String oldName, String newName);
    
    /**
     * 获取页面数据
     * @param pageName 页面名称
     * @return 页面数据，如果不存在则返回null
     */
    PageData getPage(String pageName);
    
    /**
     * 获取所有页面名称
     * @return 页面名称列表
     */
    List<String> getAllPageNames();
    
    /**
     * 设置当前页面
     * @param pageName 页面名称
     */
    void setCurrentPage(String pageName);
    
    /**
     * 获取当前页面名称
     * @return 当前页面名称
     */
    String getCurrentPageName();
    
    /**
     * 获取当前页面数据
     * @return 当前页面数据
     */
    PageData getCurrentPage();
    
    // 组件操作
    
    /**
     * 添加组件到页面
     * @param pageName 页面名称
     * @param component 组件数据
     */
    void addComponent(String pageName, ComponentData component);
    
    /**
     * 添加组件到当前页面
     * @param component 组件数据
     */
    void addComponentToCurrentPage(ComponentData component);
    
    /**
     * 从页面移除组件
     * @param pageName 页面名称
     * @param component 组件数据
     * @return 如果移除成功则返回true
     */
    boolean removeComponent(String pageName, ComponentData component);
    
    /**
     * 从当前页面移除组件
     * @param component 组件数据
     * @return 如果移除成功则返回true
     */
    boolean removeComponentFromCurrentPage(ComponentData component);
    
    /**
     * 更新组件
     * @param pageName 页面名称
     * @param oldComponent 旧组件数据
     * @param newComponent 新组件数据
     * @return 如果更新成功则返回true
     */
    boolean updateComponent(String pageName, ComponentData oldComponent, ComponentData newComponent);
    
    /**
     * 获取页面的所有组件
     * @param pageName 页面名称
     * @return 组件数据列表
     */
    List<ComponentData> getPageComponents(String pageName);
    
    /**
     * 获取当前页面的所有组件
     * @return 组件数据列表
     */
    List<ComponentData> getCurrentPageComponents();
    
    /**
     * 清空页面的所有组件
     * @param pageName 页面名称
     */
    void clearPageComponents(String pageName);
    
    /**
     * 清空当前页面的所有组件
     */
    void clearCurrentPageComponents();
    
    // 数据管理
    
    /**
     * 备份项目数据
     * @param backupName 备份名称
     * @throws IOException 备份失败时抛出异常
     */
    void backupProject(String backupName) throws IOException;
    
    /**
     * 从备份恢复项目数据
     * @param backupName 备份名称
     * @return 恢复的项目数据
     * @throws IOException 恢复失败时抛出异常
     */
    ProjectData restoreProject(String backupName) throws IOException;
    
    /**
     * 检查项目是否有未保存的更改
     * @return 如果有未保存的更改则返回true
     */
    boolean hasUnsavedChanges();
    
    /**
     * 标记项目已保存
     */
    void markAsSaved();
    
    /**
     * 标记项目已修改
     */
    void markAsModified();
    
    /**
     * 获取项目统计信息
     * @return 包含页面数量、组件数量等信息的字符串
     */
    String getProjectStatistics();
    
    // 跨平台扩展功能
    
    /**
     * 适配项目到指定分辨率
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     */
    void adaptProjectToResolution(int targetWidth, int targetHeight);
    
    /**
     * 验证项目数据完整性
     * @return 如果数据完整则返回true
     */
    boolean validateProjectIntegrity();
    
    /**
     * 修复项目数据完整性
     */
    void repairProjectIntegrity();
    
    /**
     * 导出项目数据
     * @param exportPath 导出路径
     * @throws IOException 导出失败时抛出异常
     */
    void exportProject(String exportPath) throws IOException;
    
    /**
     * 导入项目数据
     * @param importPath 导入路径
     * @return 导入的项目数据
     * @throws IOException 导入失败时抛出异常
     */
    ProjectData importProject(String importPath) throws IOException;
    
    /**
     * 获取最后保存时间
     * @return 最后保存时间戳
     */
    long getLastSavedTime();
    
    /**
     * 获取项目创建时间
     * @return 项目创建时间戳
     */
    long getProjectCreatedTime();
    
    /**
     * 获取项目最后修改时间
     * @return 最后修改时间戳
     */
    long getProjectLastModifiedTime();
}
