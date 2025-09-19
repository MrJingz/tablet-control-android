package com.feixiang.tabletcontrol.core.repository;

import com.feixiang.tabletcontrol.core.model.ProjectData;

import java.io.IOException;
import java.util.List;

/**
 * 项目数据存储库接口
 * 定义项目数据的持久化操作
 */
public interface ProjectRepository {
    
    /**
     * 加载项目数据
     * @return 项目数据，如果不存在则返回null
     * @throws IOException 加载失败时抛出异常
     */
    ProjectData loadProject() throws IOException;
    
    /**
     * 保存项目数据
     * @param projectData 要保存的项目数据
     * @throws IOException 保存失败时抛出异常
     */
    void saveProject(ProjectData projectData) throws IOException;
    
    /**
     * 检查项目文件是否存在
     * @return 如果项目文件存在则返回true
     */
    boolean projectExists();
    
    /**
     * 删除项目文件
     * @throws IOException 删除失败时抛出异常
     */
    void deleteProject() throws IOException;
    
    /**
     * 备份项目数据
     * @param projectData 要备份的项目数据
     * @param backupName 备份名称
     * @throws IOException 备份失败时抛出异常
     */
    void backupProject(ProjectData projectData, String backupName) throws IOException;
    
    /**
     * 从备份恢复项目数据
     * @param backupName 备份名称
     * @return 恢复的项目数据
     * @throws IOException 恢复失败时抛出异常
     */
    ProjectData restoreProject(String backupName) throws IOException;
    
    /**
     * 获取所有备份列表
     * @return 备份名称列表
     * @throws IOException 获取失败时抛出异常
     */
    List<String> listBackups() throws IOException;
    
    /**
     * 删除备份
     * @param backupName 备份名称
     * @throws IOException 删除失败时抛出异常
     */
    void deleteBackup(String backupName) throws IOException;
    
    /**
     * 导出项目数据到指定路径
     * @param projectData 要导出的项目数据
     * @param exportPath 导出路径
     * @throws IOException 导出失败时抛出异常
     */
    void exportProject(ProjectData projectData, String exportPath) throws IOException;
    
    /**
     * 从指定路径导入项目数据
     * @param importPath 导入路径
     * @return 导入的项目数据
     * @throws IOException 导入失败时抛出异常
     */
    ProjectData importProject(String importPath) throws IOException;
    
    /**
     * 获取项目文件路径
     * @return 项目文件路径
     */
    String getProjectFilePath();
    
    /**
     * 获取备份目录路径
     * @return 备份目录路径
     */
    String getBackupDirectoryPath();
}
