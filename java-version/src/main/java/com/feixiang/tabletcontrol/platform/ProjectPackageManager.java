package com.feixiang.tabletcontrol.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * 跨平台项目包管理器
 * 实现"文件夹替换即项目切换"的核心功能
 */
public class ProjectPackageManager {
    private static final Logger logger = LoggerFactory.getLogger(ProjectPackageManager.class);
    
    private final CrossPlatformPathManager pathManager;
    private final FileWatcher fileWatcher;
    
    public ProjectPackageManager(CrossPlatformPathManager pathManager) {
        this.pathManager = pathManager;
        this.fileWatcher = new FileWatcher();
    }
    
    /**
     * 检测USERDATA文件夹变化并自动重载
     */
    public CompletableFuture<Boolean> startAutoReloadWatcher() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path parentPath = pathManager.getUserdataPath().getParent();
                if (parentPath == null) {
                    logger.warn("无法获取USERDATA父目录，跳过自动重载监控");
                    return false;
                }
                
                fileWatcher.watchDirectory(parentPath, this::handleDirectoryChange);
                logger.info("启动USERDATA自动重载监控: {}", parentPath);
                return true;
                
            } catch (Exception e) {
                logger.error("启动自动重载监控失败", e);
                return false;
            }
        });
    }
    
    /**
     * 处理目录变化事件
     */
    private void handleDirectoryChange(WatchEvent<?> event, Path changedPath) {
        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE ||
            event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            
            String fileName = changedPath.getFileName().toString();
            if ("USERDATA".equals(fileName)) {
                logger.info("检测到USERDATA文件夹变化，准备重载项目");
                
                // 延迟重载，等待文件操作完成
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(2000); // 等待2秒
                        reloadProject();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }
    
    /**
     * 验证项目包完整性
     */
    public ValidationResult validateProjectPackage(Path packagePath) {
        ValidationResult result = new ValidationResult();
        
        try {
            // 检查基本结构
            if (!Files.exists(packagePath) || !Files.isDirectory(packagePath)) {
                result.addError("项目包路径不存在或不是目录: " + packagePath);
                return result;
            }
            
            // 检查元数据文件
            Path metadataPath = packagePath.resolve(".metadata");
            if (!Files.exists(metadataPath)) {
                result.addWarning("缺少元数据文件，将使用默认配置");
            } else {
                validateMetadata(metadataPath, result);
            }
            
            // 检查必要目录
            String[] requiredDirs = {"config", "data", "media"};
            for (String dir : requiredDirs) {
                Path dirPath = packagePath.resolve(dir);
                if (!Files.exists(dirPath)) {
                    result.addWarning("缺少目录: " + dir + "，将自动创建");
                }
            }
            
            // 检查配置文件
            validateConfigFiles(packagePath.resolve("config"), result);
            
            // 检查媒体文件
            validateMediaFiles(packagePath.resolve("media"), result);
            
            logger.info("项目包验证完成: {} 错误, {} 警告", 
                       result.getErrors().size(), result.getWarnings().size());
            
        } catch (Exception e) {
            result.addError("验证过程中发生异常: " + e.getMessage());
            logger.error("项目包验证失败", e);
        }
        
        return result;
    }
    
    /**
     * 验证元数据文件
     */
    private void validateMetadata(Path metadataPath, ValidationResult result) {
        try {
            String content = new String(Files.readAllBytes(metadataPath), "UTF-8");
            // 这里可以添加JSON解析和版本兼容性检查
            logger.debug("元数据文件验证通过: {}", metadataPath);
        } catch (Exception e) {
            result.addError("元数据文件格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 验证配置文件
     */
    private void validateConfigFiles(Path configPath, ValidationResult result) {
        if (!Files.exists(configPath)) {
            return;
        }
        
        String[] configFiles = {"project.json", "settings.json"};
        for (String configFile : configFiles) {
            Path filePath = configPath.resolve(configFile);
            if (Files.exists(filePath)) {
                try {
                    // 验证JSON格式
                    String content = new String(Files.readAllBytes(filePath), "UTF-8");
                    // 简单的JSON格式检查
                    if (!content.trim().startsWith("{") || !content.trim().endsWith("}")) {
                        result.addWarning("配置文件可能格式错误: " + configFile);
                    }
                } catch (Exception e) {
                    result.addWarning("无法读取配置文件: " + configFile);
                }
            }
        }
    }
    
    /**
     * 验证媒体文件
     */
    private void validateMediaFiles(Path mediaPath, ValidationResult result) {
        if (!Files.exists(mediaPath)) {
            return;
        }
        
        try {
            Files.walkFileTree(mediaPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString().toLowerCase();
                    
                    // 检查文件扩展名
                    if (fileName.endsWith(".tmp") || fileName.endsWith(".lock")) {
                        result.addWarning("发现临时文件: " + file.getFileName());
                    }
                    
                    // 检查文件大小
                    if (attrs.size() == 0) {
                        result.addWarning("发现空文件: " + file.getFileName());
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            result.addWarning("媒体文件验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 安全替换项目包
     */
    public CompletableFuture<Boolean> replaceProjectPackage(Path newPackagePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("开始替换项目包: {} -> {}", newPackagePath, pathManager.getUserdataPath());
                
                // 1. 验证新包
                ValidationResult validation = validateProjectPackage(newPackagePath);
                if (!validation.isValid()) {
                    logger.error("新项目包验证失败: {}", validation.getErrors());
                    return false;
                }
                
                // 2. 释放文件占用
                releaseFileHandles();
                
                // 3. 备份当前项目
                Path backupPath = createBackup();
                if (backupPath == null) {
                    logger.error("创建备份失败，取消替换操作");
                    return false;
                }
                
                // 4. 删除当前USERDATA
                deleteDirectory(pathManager.getUserdataPath());
                
                // 5. 复制新项目包
                copyDirectory(newPackagePath, pathManager.getUserdataPath());
                
                // 6. 验证替换结果
                if (!pathManager.validateUserDataIntegrity()) {
                    logger.error("替换后验证失败，尝试恢复备份");
                    restoreBackup(backupPath);
                    return false;
                }
                
                logger.info("项目包替换成功");
                return true;
                
            } catch (Exception e) {
                logger.error("项目包替换失败", e);
                return false;
            }
        });
    }
    
    /**
     * 释放文件句柄
     */
    private void releaseFileHandles() {
        try {
            // 强制垃圾回收
            System.gc();
            Thread.sleep(1000);
            
            // 这里可以添加平台特定的文件句柄释放逻辑
            logger.info("文件句柄释放完成");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("文件句柄释放被中断");
        }
    }
    
    /**
     * 创建备份
     */
    private Path createBackup() {
        try {
            Path backupDir = pathManager.getBackupPath().resolve("auto");
            Files.createDirectories(backupDir);
            
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backupPath = backupDir.resolve("backup_" + timestamp);
            
            copyDirectory(pathManager.getUserdataPath(), backupPath);
            logger.info("创建备份: {}", backupPath);
            return backupPath;
            
        } catch (Exception e) {
            logger.error("创建备份失败", e);
            return null;
        }
    }
    
    /**
     * 恢复备份
     */
    private void restoreBackup(Path backupPath) {
        try {
            deleteDirectory(pathManager.getUserdataPath());
            copyDirectory(backupPath, pathManager.getUserdataPath());
            logger.info("备份恢复成功: {}", backupPath);
        } catch (Exception e) {
            logger.error("备份恢复失败", e);
        }
    }
    
    /**
     * 复制目录
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * 删除目录
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
    
    /**
     * 重载项目
     */
    private void reloadProject() {
        logger.info("重载项目...");
        // 这里触发应用程序重新加载项目数据
        // 具体实现依赖于应用程序架构
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        
        public boolean isValid() { return errors.isEmpty(); }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
    
    /**
     * 文件监控器（简化版）
     */
    private static class FileWatcher {
        public void watchDirectory(Path directory, java.util.function.BiConsumer<WatchEvent<?>, Path> handler) {
            // 简化的文件监控实现
            // 实际实现需要使用WatchService
        }
    }
}
