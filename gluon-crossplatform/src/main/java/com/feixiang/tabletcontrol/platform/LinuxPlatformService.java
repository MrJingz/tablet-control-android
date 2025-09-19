package com.feixiang.tabletcontrol.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Linux/Ubuntu平台特定服务
 * 提供Linux平台下的特定功能实现
 */
public class LinuxPlatformService {
    
    private static final Logger logger = LoggerFactory.getLogger(LinuxPlatformService.class);
    
    private final PlatformManager platformManager;
    private String desktopEnvironment;
    private boolean isWsl;
    
    public LinuxPlatformService(PlatformManager platformManager) {
        this.platformManager = platformManager;
        this.desktopEnvironment = detectDesktopEnvironment();
        this.isWsl = detectWSL();
        
        logger.info("Linux平台服务初始化完成 - 桌面环境: {}, WSL: {}", desktopEnvironment, isWsl);
    }
    
    /**
     * 检测桌面环境
     */
    private String detectDesktopEnvironment() {
        // 检查常见的桌面环境变量
        String[] envVars = {"XDG_CURRENT_DESKTOP", "DESKTOP_SESSION", "GDMSESSION"};
        
        for (String envVar : envVars) {
            String value = System.getenv(envVar);
            if (value != null && !value.isEmpty()) {
                logger.debug("检测到桌面环境变量 {}: {}", envVar, value);
                return value.toLowerCase();
            }
        }
        
        // 检查进程中是否有桌面环境
        try {
            String[] desktops = {"gnome", "kde", "xfce", "lxde", "mate", "cinnamon"};
            for (String desktop : desktops) {
                if (isProcessRunning(desktop)) {
                    logger.debug("检测到桌面环境进程: {}", desktop);
                    return desktop;
                }
            }
        } catch (Exception e) {
            logger.warn("检测桌面环境进程时出错", e);
        }
        
        return "unknown";
    }
    
    /**
     * 检测是否在WSL环境中运行
     */
    private boolean detectWSL() {
        try {
            // 检查 /proc/version 文件
            Path versionFile = Paths.get("/proc/version");
            if (Files.exists(versionFile)) {
                String content = Files.readString(versionFile);
                return content.toLowerCase().contains("microsoft") || 
                       content.toLowerCase().contains("wsl");
            }
        } catch (Exception e) {
            logger.debug("检测WSL环境时出错", e);
        }
        
        // 检查环境变量
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        return wslDistro != null && !wslDistro.isEmpty();
    }
    
    /**
     * 检查进程是否正在运行
     */
    private boolean isProcessRunning(String processName) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"pgrep", processName});
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取系统信息
     */
    public Map<String, String> getSystemInfo() {
        Map<String, String> info = new HashMap<>();
        
        info.put("platform", "Linux");
        info.put("desktop_environment", desktopEnvironment);
        info.put("is_wsl", String.valueOf(isWsl));
        
        // 获取发行版信息
        try {
            String distro = getLinuxDistribution();
            info.put("distribution", distro);
        } catch (Exception e) {
            info.put("distribution", "unknown");
        }
        
        // 获取内核版本
        try {
            String kernel = System.getProperty("os.version");
            info.put("kernel_version", kernel);
        } catch (Exception e) {
            info.put("kernel_version", "unknown");
        }
        
        return info;
    }
    
    /**
     * 获取Linux发行版信息
     */
    private String getLinuxDistribution() throws IOException {
        // 尝试读取 /etc/os-release
        Path osRelease = Paths.get("/etc/os-release");
        if (Files.exists(osRelease)) {
            List<String> lines = Files.readAllLines(osRelease);
            for (String line : lines) {
                if (line.startsWith("PRETTY_NAME=")) {
                    return line.substring(12).replaceAll("\"", "");
                }
            }
        }
        
        // 尝试读取 /etc/lsb-release
        Path lsbRelease = Paths.get("/etc/lsb-release");
        if (Files.exists(lsbRelease)) {
            List<String> lines = Files.readAllLines(lsbRelease);
            for (String line : lines) {
                if (line.startsWith("DISTRIB_DESCRIPTION=")) {
                    return line.substring(20).replaceAll("\"", "");
                }
            }
        }
        
        return "Unknown Linux";
    }
    
    /**
     * 打开文件管理器到指定路径
     */
    public CompletableFuture<Boolean> openFileManager(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String command;
                
                if (isWsl) {
                    // WSL环境下打开Windows文件管理器
                    String windowsPath = convertToWindowsPath(path);
                    command = "explorer.exe \"" + windowsPath + "\"";
                } else {
                    // 根据桌面环境选择文件管理器
                    switch (desktopEnvironment.toLowerCase()) {
                        case "gnome":
                        case "ubuntu":
                            command = "nautilus \"" + path + "\"";
                            break;
                        case "kde":
                            command = "dolphin \"" + path + "\"";
                            break;
                        case "xfce":
                            command = "thunar \"" + path + "\"";
                            break;
                        case "lxde":
                            command = "pcmanfm \"" + path + "\"";
                            break;
                        default:
                            // 尝试通用的xdg-open
                            command = "xdg-open \"" + path + "\"";
                            break;
                    }
                }
                
                logger.info("打开文件管理器: {}", command);
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();
                
                return exitCode == 0;
                
            } catch (Exception e) {
                logger.error("打开文件管理器失败", e);
                return false;
            }
        });
    }
    
    /**
     * 将WSL路径转换为Windows路径
     */
    private String convertToWindowsPath(String wslPath) {
        if (wslPath.startsWith("/mnt/")) {
            // /mnt/c/path -> C:\path
            String[] parts = wslPath.split("/");
            if (parts.length >= 3) {
                String drive = parts[2].toUpperCase();
                String remainingPath = wslPath.substring(6 + parts[2].length());
                return drive + ":" + remainingPath.replace("/", "\\");
            }
        }
        return wslPath;
    }
    
    /**
     * 创建桌面快捷方式
     */
    public CompletableFuture<Boolean> createDesktopShortcut(String name, String execPath, String iconPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String userHome = System.getProperty("user.home");
                Path desktopPath = Paths.get(userHome, "Desktop");
                
                // 如果Desktop目录不存在，尝试其他位置
                if (!Files.exists(desktopPath)) {
                    desktopPath = Paths.get(userHome, "桌面"); // 中文桌面
                }
                
                if (!Files.exists(desktopPath)) {
                    logger.warn("桌面目录不存在，跳过创建快捷方式");
                    return false;
                }
                
                Path shortcutPath = desktopPath.resolve(name + ".desktop");
                
                StringBuilder content = new StringBuilder();
                content.append("[Desktop Entry]\n");
                content.append("Version=1.0\n");
                content.append("Type=Application\n");
                content.append("Name=").append(name).append("\n");
                content.append("Comment=跨平台平板中控软件\n");
                content.append("Exec=").append(execPath).append("\n");
                content.append("Terminal=false\n");
                content.append("Categories=Utility;Development;\n");
                
                if (iconPath != null && !iconPath.isEmpty()) {
                    content.append("Icon=").append(iconPath).append("\n");
                }
                
                Files.write(shortcutPath, content.toString().getBytes());
                
                // 设置可执行权限
                shortcutPath.toFile().setExecutable(true);
                
                logger.info("桌面快捷方式创建成功: {}", shortcutPath);
                return true;
                
            } catch (Exception e) {
                logger.error("创建桌面快捷方式失败", e);
                return false;
            }
        });
    }
    
    /**
     * 获取系统主题信息
     */
    public String getSystemTheme() {
        try {
            // 检查GTK主题
            String gtkTheme = System.getenv("GTK_THEME");
            if (gtkTheme != null && !gtkTheme.isEmpty()) {
                return gtkTheme;
            }
            
            // 检查GNOME设置
            if (desktopEnvironment.contains("gnome")) {
                Process process = Runtime.getRuntime().exec(
                    new String[]{"gsettings", "get", "org.gnome.desktop.interface", "gtk-theme"});
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String theme = reader.readLine();
                    if (theme != null) {
                        return theme.replaceAll("'", "");
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("获取系统主题失败", e);
        }
        
        return "default";
    }
    
    /**
     * 检查是否支持系统通知
     */
    public boolean supportsNotifications() {
        try {
            // 检查是否有notify-send命令
            Process process = Runtime.getRuntime().exec(new String[]{"which", "notify-send"});
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 发送系统通知
     */
    public CompletableFuture<Boolean> sendNotification(String title, String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (!supportsNotifications()) {
                logger.warn("系统不支持通知");
                return false;
            }
            
            try {
                String[] command = {
                    "notify-send",
                    "--app-name=TabletControl",
                    "--urgency=normal",
                    title,
                    message
                };
                
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();
                
                return exitCode == 0;
                
            } catch (Exception e) {
                logger.error("发送通知失败", e);
                return false;
            }
        });
    }
    
    /**
     * 获取平台特定的文件对话框实现
     */
    public String getPreferredFileDialogType() {
        if (isWsl) {
            return "swing"; // WSL环境下使用Swing对话框
        }

        switch (desktopEnvironment.toLowerCase()) {
            case "gnome":
            case "ubuntu":
                return "gtk";
            case "kde":
                return "qt";
            default:
                return "javafx"; // 默认使用JavaFX对话框
        }
    }

    /**
     * 使用原生文件对话框选择文件
     */
    public CompletableFuture<String> showNativeFileDialog(String title, String initialDir, boolean isOpen) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String command;

                if (isWsl) {
                    // WSL环境下可能无法使用原生对话框
                    return null;
                }

                // 使用zenity (GNOME/GTK)
                if (desktopEnvironment.contains("gnome") || desktopEnvironment.contains("ubuntu")) {
                    if (isCommandAvailable("zenity")) {
                        List<String> cmd = new ArrayList<>();
                        cmd.add("zenity");
                        cmd.add("--file-selection");
                        cmd.add("--title=" + title);

                        if (initialDir != null && !initialDir.isEmpty()) {
                            cmd.add("--filename=" + initialDir + "/");
                        }

                        if (!isOpen) {
                            cmd.add("--save");
                        }

                        Process process = new ProcessBuilder(cmd).start();

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String result = reader.readLine();
                            int exitCode = process.waitFor();

                            if (exitCode == 0 && result != null && !result.trim().isEmpty()) {
                                return result.trim();
                            }
                        }
                    }
                }

                // 使用kdialog (KDE)
                if (desktopEnvironment.contains("kde")) {
                    if (isCommandAvailable("kdialog")) {
                        List<String> cmd = new ArrayList<>();
                        cmd.add("kdialog");

                        if (isOpen) {
                            cmd.add("--getopenfilename");
                        } else {
                            cmd.add("--getsavefilename");
                        }

                        cmd.add(initialDir != null ? initialDir : System.getProperty("user.home"));
                        cmd.add("--title");
                        cmd.add(title);

                        Process process = new ProcessBuilder(cmd).start();

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String result = reader.readLine();
                            int exitCode = process.waitFor();

                            if (exitCode == 0 && result != null && !result.trim().isEmpty()) {
                                return result.trim();
                            }
                        }
                    }
                }

                return null;

            } catch (Exception e) {
                logger.error("原生文件对话框失败", e);
                return null;
            }
        });
    }

    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", command});
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取系统字体配置
     */
    public Map<String, String> getSystemFonts() {
        Map<String, String> fonts = new HashMap<>();

        try {
            // 获取系统默认字体
            if (desktopEnvironment.contains("gnome")) {
                String[] fontSettings = {
                    "org.gnome.desktop.interface font-name",
                    "org.gnome.desktop.interface monospace-font-name",
                    "org.gnome.desktop.interface document-font-name"
                };

                for (String setting : fontSettings) {
                    try {
                        Process process = Runtime.getRuntime().exec(
                            new String[]{"gsettings", "get", setting.split(" ")[0], setting.split(" ")[1]});

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String font = reader.readLine();
                            if (font != null) {
                                String key = setting.split(" ")[1].replace("-", "_");
                                fonts.put(key, font.replaceAll("'", ""));
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("获取字体设置失败: " + setting, e);
                    }
                }
            }

        } catch (Exception e) {
            logger.debug("获取系统字体配置失败", e);
        }

        // 设置默认值
        fonts.putIfAbsent("font_name", "Ubuntu 11");
        fonts.putIfAbsent("monospace_font_name", "Ubuntu Mono 11");
        fonts.putIfAbsent("document_font_name", "Ubuntu 11");

        return fonts;
    }

    // Getter方法
    public String getDesktopEnvironment() {
        return desktopEnvironment;
    }

    public boolean isWsl() {
        return isWsl;
    }
}
