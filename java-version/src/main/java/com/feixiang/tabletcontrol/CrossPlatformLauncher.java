package com.feixiang.tabletcontrol;

import com.feixiang.tabletcontrol.platform.CrossPlatformBootstrap;
import com.feixiang.tabletcontrol.service.UnifiedDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * 跨平台启动器
 * 使用新的跨平台管理系统启动应用程序
 */
public class CrossPlatformLauncher {
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformLauncher.class);
    
    public static void main(String[] args) {
        try {
            logger.info("启动跨平台平板中控应用程序...");
            
            // 解析命令行参数
            String password = null;
            String customPath = null;
            boolean useOldLauncher = false;
            
            for (String arg : args) {
                if (arg.startsWith("--password=")) {
                    password = arg.substring("--password=".length());
                } else if (arg.startsWith("--path=")) {
                    customPath = arg.substring("--path=".length());
                } else if ("--legacy".equals(arg)) {
                    useOldLauncher = true;
                }
            }
            
            // 如果指定使用旧启动器，直接调用原始main方法
            if (useOldLauncher) {
                logger.info("使用传统启动方式");
                SwingTabletControlApp.main(args);
                return;
            }
            
            // 创建跨平台启动器
            CrossPlatformBootstrap bootstrap = customPath != null ? 
                new CrossPlatformBootstrap(customPath) : 
                new CrossPlatformBootstrap();
            
            // 执行跨平台启动流程
            boolean bootstrapSuccess = bootstrap.bootstrap().get();
            if (!bootstrapSuccess) {
                logger.error("跨平台启动流程失败，尝试使用传统启动方式");
                SwingTabletControlApp.main(args);
                return;
            }
            
            // 获取数据服务
            UnifiedDataService dataService = bootstrap.getDataService();
            if (dataService == null) {
                logger.error("数据服务未初始化，使用传统启动方式");
                SwingTabletControlApp.main(args);
                return;
            }
            
            // 如果提供了密码，设置密码
            if (password != null) {
                dataService.setPassword(password);
            }
            
            // 启动应用程序
            SwingUtilities.invokeLater(() -> {
                try {
                    // 设置系统外观
                    try {
                        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        // 忽略外观设置错误
                    }
                    
                    // 创建应用程序实例
                    SwingTabletControlApp app = new SwingTabletControlApp();
                    
                    // 显示应用程序
                    app.setVisible(true);
                    
                    logger.info("跨平台应用程序启动完成");

                    // 启动成功信息已禁用
                    // 如需显示启动信息，可取消注释以下代码：
                    /*
                    javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
                        showStartupInfo(bootstrap);
                    });
                    timer.setRepeats(false);
                    timer.start();
                    */
                    
                } catch (Exception e) {
                    logger.error("应用程序界面启动失败", e);
                    JOptionPane.showMessageDialog(null, 
                        "应用程序界面启动失败: " + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
        } catch (Exception e) {
            logger.error("跨平台启动失败", e);
            
            // 显示错误信息并提供回退选项
            int choice = JOptionPane.showConfirmDialog(null,
                "跨平台启动失败: " + e.getMessage() + "\n\n是否尝试使用传统启动方式？",
                "启动失败",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                logger.info("使用传统启动方式作为回退");
                SwingTabletControlApp.main(args);
            } else {
                System.exit(1);
            }
        }
    }
    
    /**
     * 显示启动信息
     */
    private static void showStartupInfo(CrossPlatformBootstrap bootstrap) {
        SwingUtilities.invokeLater(() -> {
            String platform = bootstrap.getPathManager().getCurrentPlatform().toString();
            String userdataPath = bootstrap.getPathManager().getUserdataPath().toString();
            
            String message = String.format(
                "🎉 跨平台启动成功！\n\n" +
                "📱 检测平台: %s\n" +
                "📁 数据路径: %s\n" +
                "🔒 加密状态: %s\n\n" +
                "现在您可以通过替换USERDATA文件夹来切换项目！",
                platform,
                userdataPath,
                bootstrap.getDataService().isEncryptionEnabled() ? "已启用" : "未启用"
            );
            
            JOptionPane.showMessageDialog(null, message, 
                "跨平台启动成功", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
