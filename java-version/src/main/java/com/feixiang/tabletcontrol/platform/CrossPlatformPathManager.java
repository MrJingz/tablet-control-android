package com.feixiang.tabletcontrol.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨平台路径管理器
 * 负责管理不同平台下的USERDATA路径和文件操作
 */
public class CrossPlatformPathManager {
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformPathManager.class);
    
    // 平台类型枚举
    public enum Platform {
        WINDOWS, ANDROID, IOS, HARMONYOS, UNKNOWN
    }
    
    // USERDATA文件夹名称
    private static final String USERDATA_FOLDER = "USERDATA";
    private static final String APP_NAME = "TabletControl";
    
    private final Platform currentPlatform;
    private final Path userdataPath;
    
    /**
     * 构造函数 - 自动检测平台
     */
    public CrossPlatformPathManager() {
        this.currentPlatform = detectPlatform();
        this.userdataPath = determineUserdataPath();
        logger.info("平台检测: {}, USERDATA路径: {}", currentPlatform, userdataPath);
    }
    
    /**
     * 构造函数 - 指定路径
     */
    public CrossPlatformPathManager(String customPath) {
        this.currentPlatform = detectPlatform();
        this.userdataPath = Paths.get(customPath, USERDATA_FOLDER);
        logger.info("使用自定义路径: {}", userdataPath);
    }
    
    /**
     * 检测当前平台
     */
    private Platform detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String javaVendor = System.getProperty("java.vendor").toLowerCase();
        
        if (osName.contains("windows")) {
            return Platform.WINDOWS;
        } else if (osName.contains("android") || javaVendor.contains("android")) {
            return Platform.ANDROID;
        } else if (osName.contains("ios") || osName.contains("iphone")) {
            return Platform.IOS;
        } else if (javaVendor.contains("huawei") || osName.contains("harmonyos")) {
            return Platform.HARMONYOS;
        } else {
            logger.warn("未知平台: {}, 使用默认配置", osName);
            return Platform.UNKNOWN;
        }
    }
    
    /**
     * 确定USERDATA路径
     */
    private Path determineUserdataPath() {
        List<Path> candidatePaths = getPlatformPaths();
        
        // 查找现有的USERDATA文件夹
        for (Path path : candidatePaths) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                logger.info("找到现有USERDATA: {}", path);
                return path;
            }
        }
        
        // 选择最佳路径创建新的USERDATA
        Path bestPath = candidatePaths.get(0);
        logger.info("选择创建新USERDATA: {}", bestPath);
        return bestPath;
    }
    
    /**
     * 获取平台特定的候选路径
     */
    private List<Path> getPlatformPaths() {
        List<Path> paths = new ArrayList<>();
        
        switch (currentPlatform) {
            case WINDOWS:
                // Windows平台路径优先级
                String userHome = System.getProperty("user.home");
                String userDir = System.getProperty("user.dir");
                
                // 1. 用户文档目录
                paths.add(Paths.get(userHome, "Documents", APP_NAME, USERDATA_FOLDER));
                // 2. 应用程序目录
                paths.add(Paths.get(userDir, USERDATA_FOLDER));
                // 3. AppData目录
                String appData = System.getenv("APPDATA");
                if (appData != null) {
                    paths.add(Paths.get(appData, APP_NAME, USERDATA_FOLDER));
                }
                break;
                
            case ANDROID:
                // Android平台路径
                String androidData = System.getProperty("android.data.dir");
                if (androidData != null) {
                    paths.add(Paths.get(androidData, "files", USERDATA_FOLDER));
                }
                // 外部存储
                String externalStorage = System.getenv("EXTERNAL_STORAGE");
                if (externalStorage != null) {
                    paths.add(Paths.get(externalStorage, APP_NAME, USERDATA_FOLDER));
                }
                break;
                
            case IOS:
                // iOS平台路径
                String iosDocuments = System.getProperty("user.home");
                if (iosDocuments != null) {
                    paths.add(Paths.get(iosDocuments, "Documents", USERDATA_FOLDER));
                    paths.add(Paths.get(iosDocuments, "Library", "Application Support", APP_NAME, USERDATA_FOLDER));
                }
                break;
                
            case HARMONYOS:
                // HarmonyOS平台路径
                String harmonyData = System.getProperty("harmony.data.dir");
                if (harmonyData != null) {
                    paths.add(Paths.get(harmonyData, "files", USERDATA_FOLDER));
                }
                break;
                
            default:
                // 默认路径
                paths.add(Paths.get(System.getProperty("user.dir"), USERDATA_FOLDER));
                break;
        }
        
        return paths;
    }
    
    /**
     * 初始化USERDATA目录结构
     */
    public boolean initializeUserDataStructure() {
        try {
            // 创建主目录
            Files.createDirectories(userdataPath);
            
            // 创建标准子目录
            String[] subDirs = {
                "config", "data/database", "data/logs", "data/temp",
                "media/images/backgrounds", "media/images/icons", "media/images/ui", "media/images/content",
                "media/videos/backgrounds", "media/videos/content",
                "media/audio/effects", "media/audio/content",
                "media/fonts", "themes/default", "themes/dark", "themes/custom",
                "plugins/extensions", "plugins/scripts",
                "backup/auto", "backup/manual",
                "cache/thumbnails", "cache/processed", "cache/network"
            };
            
            for (String subDir : subDirs) {
                Path dirPath = userdataPath.resolve(subDir);
                Files.createDirectories(dirPath);
                logger.debug("创建目录: {}", dirPath);
            }
            
            // 创建元数据文件
            createMetadataFile();
            
            logger.info("USERDATA目录结构初始化完成: {}", userdataPath);
            return true;
            
        } catch (IOException e) {
            logger.error("初始化USERDATA目录失败", e);
            return false;
        }
    }
    
    /**
     * 创建元数据文件
     */
    private void createMetadataFile() throws IOException {
        Path metadataPath = userdataPath.resolve(".metadata");
        if (!Files.exists(metadataPath)) {
            String metadata = "{\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"platform\": \"cross-platform\",\n" +
                "  \"created\": \"" + java.time.Instant.now().toString() + "\",\n" +
                "  \"compatibility\": {\n" +
                "    \"minVersion\": \"1.0.0\",\n" +
                "    \"platforms\": [\"windows\", \"android\", \"ios\", \"harmonyos\"]\n" +
                "  }\n" +
                "}";
            Files.write(metadataPath, metadata.getBytes("UTF-8"));
            logger.info("创建元数据文件: {}", metadataPath);
        }
    }
    
    /**
     * 验证USERDATA目录完整性
     */
    public boolean validateUserDataIntegrity() {
        if (!Files.exists(userdataPath) || !Files.isDirectory(userdataPath)) {
            logger.warn("USERDATA目录不存在: {}", userdataPath);
            return false;
        }
        
        // 检查必要的子目录
        String[] requiredDirs = {"config", "data", "media"};
        for (String dir : requiredDirs) {
            Path dirPath = userdataPath.resolve(dir);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                logger.warn("缺少必要目录: {}", dirPath);
                return false;
            }
        }
        
        // 检查元数据文件
        Path metadataPath = userdataPath.resolve(".metadata");
        if (!Files.exists(metadataPath)) {
            logger.warn("缺少元数据文件: {}", metadataPath);
            return false;
        }
        
        logger.info("USERDATA目录完整性验证通过");
        return true;
    }
    
    // Getter方法
    public Platform getCurrentPlatform() { return currentPlatform; }
    public Path getUserdataPath() { return userdataPath; }
    public Path getConfigPath() { return userdataPath.resolve("config"); }
    public Path getDataPath() { return userdataPath.resolve("data"); }
    public Path getMediaPath() { return userdataPath.resolve("media"); }
    public Path getThemesPath() { return userdataPath.resolve("themes"); }
    public Path getPluginsPath() { return userdataPath.resolve("plugins"); }
    public Path getBackupPath() { return userdataPath.resolve("backup"); }
    public Path getCachePath() { return userdataPath.resolve("cache"); }
    
    /**
     * 获取相对路径（用于跨平台兼容）
     */
    public String getRelativePath(Path absolutePath) {
        try {
            return userdataPath.relativize(absolutePath).toString().replace('\\', '/');
        } catch (Exception e) {
            return absolutePath.toString().replace('\\', '/');
        }
    }
    
    /**
     * 解析相对路径为绝对路径
     */
    public Path resolveRelativePath(String relativePath) {
        return userdataPath.resolve(relativePath.replace('/', File.separatorChar));
    }
}
