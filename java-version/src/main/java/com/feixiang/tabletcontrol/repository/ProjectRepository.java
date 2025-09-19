package com.feixiang.tabletcontrol.repository;

import com.feixiang.tabletcontrol.model.ProjectData;
import java.io.IOException;

/**
 * 项目数据访问接口
 * 定义项目数据的持久化操作
 */
public interface ProjectRepository {
    
    /**
     * 保存项目数据
     * @param projectData 项目数据
     * @throws IOException 保存失败时抛出异常
     */
    void save(ProjectData projectData) throws IOException;
    
    /**
     * 加载项目数据
     * @return 项目数据，如果不存在则返回null
     * @throws IOException 加载失败时抛出异常
     */
    ProjectData load() throws IOException;
    
    /**
     * 检查项目数据是否存在
     * @return 如果项目数据文件存在则返回true
     */
    boolean exists();
    
    /**
     * 删除项目数据
     * @return 如果删除成功则返回true
     */
    boolean delete();
    
    /**
     * 备份项目数据
     * @param backupName 备份名称
     * @throws IOException 备份失败时抛出异常
     */
    void backup(String backupName) throws IOException;
    
    /**
     * 从备份恢复项目数据
     * @param backupName 备份名称
     * @return 恢复的项目数据
     * @throws IOException 恢复失败时抛出异常
     */
    ProjectData restore(String backupName) throws IOException;
    
    /**
     * 获取项目数据文件路径
     * @return 文件路径
     */
    String getFilePath();
    
    /**
     * 获取项目数据文件大小
     * @return 文件大小（字节），如果文件不存在则返回-1
     */
    long getFileSize();
    
    /**
     * 获取项目数据文件最后修改时间
     * @return 最后修改时间戳，如果文件不存在则返回-1
     */
    long getLastModified();
}
