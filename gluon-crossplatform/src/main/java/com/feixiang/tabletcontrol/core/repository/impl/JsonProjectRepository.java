package com.feixiang.tabletcontrol.core.repository.impl;

import com.feixiang.tabletcontrol.core.model.ProjectData;
import com.feixiang.tabletcontrol.core.repository.ProjectRepository;
import com.feixiang.tabletcontrol.platform.CrossPlatformPathManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON格式项目数据存储库实现
 * 使用JSON格式存储项目数据，支持跨平台文件系统
 */
public class JsonProjectRepository implements ProjectRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonProjectRepository.class);
    
    private static final String PROJECT_FILE_NAME = "project_data.json";
    private static final String BACKUP_DIR_NAME = "backups";
    private static final String EXPORT_FILE_EXTENSION = ".json";
    
    private final CrossPlatformPathManager pathManager;
    private final Gson gson;
    private final String projectFilePath;
    private final String backupDirectoryPath;
    
    public JsonProjectRepository(CrossPlatformPathManager pathManager) {
        this.pathManager = pathManager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        
        // 初始化路径
        this.projectFilePath = pathManager.getDataDirectory() + File.separator + PROJECT_FILE_NAME;
        this.backupDirectoryPath = pathManager.getDataDirectory() + File.separator + BACKUP_DIR_NAME;
        
        // 确保目录存在
        ensureDirectoriesExist();
        
        logger.info("JSON项目存储库初始化完成");
        logger.info("项目文件路径: {}", projectFilePath);
        logger.info("备份目录路径: {}", backupDirectoryPath);
    }
    
    /**
     * 确保必要的目录存在
     */
    private void ensureDirectoriesExist() {
        try {
            // 确保数据目录存在
            Path dataDir = Paths.get(pathManager.getDataDirectory());
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                logger.info("创建数据目录: {}", dataDir);
            }
            
            // 确保备份目录存在
            Path backupDir = Paths.get(backupDirectoryPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                logger.info("创建备份目录: {}", backupDir);
            }
            
        } catch (IOException e) {
            logger.error("创建目录失败", e);
            throw new RuntimeException("无法创建必要的目录", e);
        }
    }
    
    @Override
    public ProjectData loadProject() throws IOException {
        logger.info("加载项目数据: {}", projectFilePath);
        
        Path projectFile = Paths.get(projectFilePath);
        if (!Files.exists(projectFile)) {
            logger.info("项目文件不存在: {}", projectFilePath);
            return null;
        }
        
        try (Reader reader = Files.newBufferedReader(projectFile, StandardCharsets.UTF_8)) {
            ProjectData projectData = gson.fromJson(reader, ProjectData.class);
            
            if (projectData == null) {
                logger.warn("项目文件为空或格式错误: {}", projectFilePath);
                return null;
            }
            
            // 验证和修复数据完整性
            if (!projectData.validateIntegrity()) {
                logger.warn("项目数据完整性检查失败，正在修复");
                projectData.repairIntegrity();
            }
            
            logger.info("项目数据加载完成: {}", projectData.getProjectSummary());
            return projectData;
            
        } catch (Exception e) {
            logger.error("加载项目数据失败: {}", projectFilePath, e);
            throw new IOException("加载项目数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void saveProject(ProjectData projectData) throws IOException {
        if (projectData == null) {
            throw new IllegalArgumentException("项目数据不能为null");
        }
        
        logger.info("保存项目数据: {}", projectFilePath);
        
        // 验证数据完整性
        if (!projectData.validateIntegrity()) {
            logger.warn("项目数据完整性检查失败，正在修复");
            projectData.repairIntegrity();
        }
        
        // 创建临时文件
        Path projectFile = Paths.get(projectFilePath);
        Path tempFile = Paths.get(projectFilePath + ".tmp");
        
        try {
            // 写入临时文件
            try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                gson.toJson(projectData, writer);
                writer.flush();
            }
            
            // 原子性替换
            Files.move(tempFile, projectFile, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("项目数据保存完成: {}", projectData.getProjectSummary());
            
        } catch (Exception e) {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException cleanupException) {
                logger.warn("清理临时文件失败", cleanupException);
            }
            
            logger.error("保存项目数据失败: {}", projectFilePath, e);
            throw new IOException("保存项目数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean projectExists() {
        return Files.exists(Paths.get(projectFilePath));
    }
    
    @Override
    public void deleteProject() throws IOException {
        logger.info("删除项目文件: {}", projectFilePath);
        
        Path projectFile = Paths.get(projectFilePath);
        if (Files.exists(projectFile)) {
            Files.delete(projectFile);
            logger.info("项目文件删除完成");
        } else {
            logger.info("项目文件不存在，无需删除");
        }
    }
    
    @Override
    public void backupProject(ProjectData projectData, String backupName) throws IOException {
        if (projectData == null) {
            throw new IllegalArgumentException("项目数据不能为null");
        }
        if (backupName == null || backupName.trim().isEmpty()) {
            throw new IllegalArgumentException("备份名称不能为空");
        }
        
        logger.info("备份项目数据: {}", backupName);
        
        // 生成备份文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = String.format("%s_%s.json", backupName, timestamp);
        String backupFilePath = backupDirectoryPath + File.separator + backupFileName;
        
        // 保存备份
        Path backupFile = Paths.get(backupFilePath);
        try (Writer writer = Files.newBufferedWriter(backupFile, StandardCharsets.UTF_8)) {
            gson.toJson(projectData, writer);
            writer.flush();
        }
        
        logger.info("项目备份完成: {}", backupFilePath);
    }
    
    @Override
    public ProjectData restoreProject(String backupName) throws IOException {
        if (backupName == null || backupName.trim().isEmpty()) {
            throw new IllegalArgumentException("备份名称不能为空");
        }
        
        logger.info("从备份恢复项目数据: {}", backupName);
        
        // 查找备份文件
        List<String> backups = listBackups();
        String matchingBackup = backups.stream()
                .filter(backup -> backup.startsWith(backupName))
                .findFirst()
                .orElse(null);
        
        if (matchingBackup == null) {
            throw new IOException("备份不存在: " + backupName);
        }
        
        String backupFilePath = backupDirectoryPath + File.separator + matchingBackup;
        Path backupFile = Paths.get(backupFilePath);
        
        try (Reader reader = Files.newBufferedReader(backupFile, StandardCharsets.UTF_8)) {
            ProjectData projectData = gson.fromJson(reader, ProjectData.class);
            
            if (projectData == null) {
                throw new IOException("备份文件格式错误: " + backupFilePath);
            }
            
            // 验证和修复数据完整性
            if (!projectData.validateIntegrity()) {
                logger.warn("备份数据完整性检查失败，正在修复");
                projectData.repairIntegrity();
            }
            
            logger.info("项目数据恢复完成: {}", projectData.getProjectSummary());
            return projectData;
            
        } catch (Exception e) {
            logger.error("恢复项目数据失败: {}", backupFilePath, e);
            throw new IOException("恢复项目数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> listBackups() throws IOException {
        logger.debug("获取备份列表: {}", backupDirectoryPath);
        
        Path backupDir = Paths.get(backupDirectoryPath);
        if (!Files.exists(backupDir)) {
            return new ArrayList<>();
        }
        
        try {
            return Files.list(backupDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
                    
        } catch (IOException e) {
            logger.error("获取备份列表失败", e);
            throw new IOException("获取备份列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteBackup(String backupName) throws IOException {
        if (backupName == null || backupName.trim().isEmpty()) {
            throw new IllegalArgumentException("备份名称不能为空");
        }
        
        logger.info("删除备份: {}", backupName);
        
        String backupFilePath = backupDirectoryPath + File.separator + backupName;
        Path backupFile = Paths.get(backupFilePath);
        
        if (Files.exists(backupFile)) {
            Files.delete(backupFile);
            logger.info("备份删除完成: {}", backupName);
        } else {
            logger.warn("备份文件不存在: {}", backupName);
        }
    }
    
    @Override
    public void exportProject(ProjectData projectData, String exportPath) throws IOException {
        if (projectData == null) {
            throw new IllegalArgumentException("项目数据不能为null");
        }
        if (exportPath == null || exportPath.trim().isEmpty()) {
            throw new IllegalArgumentException("导出路径不能为空");
        }
        
        logger.info("导出项目数据到: {}", exportPath);
        
        // 确保导出路径以.json结尾
        String finalExportPath = exportPath.endsWith(EXPORT_FILE_EXTENSION) ? 
                                exportPath : exportPath + EXPORT_FILE_EXTENSION;
        
        Path exportFile = Paths.get(finalExportPath);
        
        // 确保父目录存在
        Path parentDir = exportFile.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        try (Writer writer = Files.newBufferedWriter(exportFile, StandardCharsets.UTF_8)) {
            gson.toJson(projectData, writer);
            writer.flush();
        }
        
        logger.info("项目数据导出完成: {}", finalExportPath);
    }
    
    @Override
    public ProjectData importProject(String importPath) throws IOException {
        if (importPath == null || importPath.trim().isEmpty()) {
            throw new IllegalArgumentException("导入路径不能为空");
        }
        
        logger.info("从文件导入项目数据: {}", importPath);
        
        Path importFile = Paths.get(importPath);
        if (!Files.exists(importFile)) {
            throw new IOException("导入文件不存在: " + importPath);
        }
        
        try (Reader reader = Files.newBufferedReader(importFile, StandardCharsets.UTF_8)) {
            ProjectData projectData = gson.fromJson(reader, ProjectData.class);
            
            if (projectData == null) {
                throw new IOException("导入文件格式错误: " + importPath);
            }
            
            // 验证和修复数据完整性
            if (!projectData.validateIntegrity()) {
                logger.warn("导入数据完整性检查失败，正在修复");
                projectData.repairIntegrity();
            }
            
            logger.info("项目数据导入完成: {}", projectData.getProjectSummary());
            return projectData;
            
        } catch (Exception e) {
            logger.error("导入项目数据失败: {}", importPath, e);
            throw new IOException("导入项目数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getProjectFilePath() {
        return projectFilePath;
    }
    
    @Override
    public String getBackupDirectoryPath() {
        return backupDirectoryPath;
    }
}
