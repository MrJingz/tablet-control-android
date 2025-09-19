package com.feixiang.tabletcontrol.view;

import com.feixiang.tabletcontrol.ui.ModernDesignSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

/**
 * 主界面视图
 * 负责创建和管理主界面的UI组件
 */
public class MainView {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);
    
    private JFrame mainFrame;
    private JPanel contentPanel;
    private JButton settingsButton;
    private JLabel statusLabel;
    private JLabel noContentLabel;
    
    // 界面状态
    private boolean isFullScreen = false;
    private Dimension screenSize;
    
    public MainView() {
        initializeComponents();
        logger.info("MainView初始化完成");
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        // 获取屏幕尺寸
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        logger.info("屏幕尺寸: {}x{}", screenSize.width, screenSize.height);
        
        // 创建主窗口
        createMainFrame();
        
        // 创建内容面板
        createContentPanel();
        
        // 创建设置按钮
        createSettingsButton();
        
        // 创建状态标签
        createStatusLabel();
        
        // 创建无内容提示标签
        createNoContentLabel();
        
        // 设置布局
        setupLayout();
    }
    
    /**
     * 创建主窗口
     */
    private void createMainFrame() {
        mainFrame = new JFrame("平板中控系统");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(screenSize);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setUndecorated(true);
        mainFrame.setBackground(Color.BLACK);
        
        // 设置窗口图标
        try {
            // 这里可以设置应用程序图标
            // mainFrame.setIconImage(iconImage);
        } catch (Exception e) {
            logger.warn("设置窗口图标失败: {}", e.getMessage());
        }
        
        logger.debug("主窗口创建完成");
    }
    
    /**
     * 创建内容面板
     */
    private void createContentPanel() {
        contentPanel = new JPanel();
        contentPanel.setLayout(null); // 使用绝对布局
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setOpaque(true);
        
        logger.debug("内容面板创建完成");
    }
    
    /**
     * 创建设置按钮（齿轮按钮）
     */
    private void createSettingsButton() {
        settingsButton = new JButton("⚙");
        settingsButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setBackground(new Color(60, 60, 60, 180));
        settingsButton.setBorder(BorderFactory.createEmptyBorder());
        settingsButton.setFocusPainted(false);
        settingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 设置按钮位置（右下角）
        int buttonSize = 30;
        int margin = 20;
        settingsButton.setBounds(
            screenSize.width - buttonSize - margin,
            screenSize.height - buttonSize - margin,
            buttonSize,
            buttonSize
        );
        
        logger.debug("设置按钮创建完成，位置: {}", settingsButton.getBounds());
    }
    
    /**
     * 创建状态标签
     */
    private void createStatusLabel() {
        statusLabel = new JLabel("系统就绪");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        // 设置状态标签位置（左下角）
        int margin = 20;
        statusLabel.setBounds(margin, screenSize.height - 30, 200, 20);
        
        logger.debug("状态标签创建完成");
    }
    
    /**
     * 创建无内容提示标签
     */
    private void createNoContentLabel() {
        noContentLabel = new JLabel("<html><div style='text-align: center;'>" +
                                   "<h2>欢迎使用平板中控系统</h2>" +
                                   "<p>点击右下角齿轮按钮开始配置</p>" +
                                   "</div></html>");
        noContentLabel.setForeground(Color.WHITE);
        noContentLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        noContentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noContentLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // 设置提示标签位置（居中）
        int labelWidth = 400;
        int labelHeight = 100;
        noContentLabel.setBounds(
            (screenSize.width - labelWidth) / 2,
            (screenSize.height - labelHeight) / 2,
            labelWidth,
            labelHeight
        );
        
        logger.debug("无内容提示标签创建完成");
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        // 添加组件到内容面板
        contentPanel.add(settingsButton);
        contentPanel.add(statusLabel);
        contentPanel.add(noContentLabel);
        
        // 设置主窗口内容
        mainFrame.setContentPane(contentPanel);
        
        logger.debug("布局设置完成");
    }
    
    // 公共方法
    
    /**
     * 显示主窗口
     */
    public void show() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setVisible(true);
            mainFrame.toFront();
            logger.info("主窗口已显示");
        });
    }
    
    /**
     * 隐藏主窗口
     */
    public void hide() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setVisible(false);
            logger.info("主窗口已隐藏");
        });
    }
    
    /**
     * 显示无内容提示
     */
    public void showNoContentMessage() {
        SwingUtilities.invokeLater(() -> {
            noContentLabel.setVisible(true);
            logger.debug("显示无内容提示");
        });
    }
    
    /**
     * 隐藏无内容提示
     */
    public void hideNoContentMessage() {
        SwingUtilities.invokeLater(() -> {
            noContentLabel.setVisible(false);
            logger.debug("隐藏无内容提示");
        });
    }
    
    /**
     * 更新状态文本
     */
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            logger.debug("状态更新: {}", status);
        });
    }
    
    /**
     * 清空内容面板
     */
    public void clearContent() {
        SwingUtilities.invokeLater(() -> {
            // 移除除了固定组件外的所有组件
            Component[] components = contentPanel.getComponents();
            for (Component component : components) {
                if (component != settingsButton && component != statusLabel && component != noContentLabel) {
                    contentPanel.remove(component);
                }
            }
            contentPanel.revalidate();
            contentPanel.repaint();
            logger.debug("内容面板已清空");
        });
    }
    
    /**
     * 添加组件到内容面板
     */
    public void addComponent(Component component) {
        SwingUtilities.invokeLater(() -> {
            contentPanel.add(component);
            contentPanel.revalidate();
            contentPanel.repaint();
            logger.debug("添加组件到内容面板: {}", component.getClass().getSimpleName());
        });
    }
    
    /**
     * 从内容面板移除组件
     */
    public void removeComponent(Component component) {
        SwingUtilities.invokeLater(() -> {
            contentPanel.remove(component);
            contentPanel.revalidate();
            contentPanel.repaint();
            logger.debug("从内容面板移除组件: {}", component.getClass().getSimpleName());
        });
    }
    
    // Getter方法
    
    public JFrame getMainFrame() { return mainFrame; }
    public JPanel getContentPanel() { return contentPanel; }
    public JButton getSettingsButton() { return settingsButton; }
    public JLabel getStatusLabel() { return statusLabel; }
    public Dimension getScreenSize() { return screenSize; }
    
    // 事件监听器设置方法
    
    /**
     * 设置设置按钮点击监听器
     */
    public void setSettingsButtonListener(ActionListener listener) {
        settingsButton.addActionListener(listener);
        logger.debug("设置按钮监听器已设置");
    }
    
    /**
     * 设置内容面板鼠标监听器
     */
    public void setContentPanelMouseListener(MouseListener listener) {
        contentPanel.addMouseListener(listener);
        logger.debug("内容面板鼠标监听器已设置");
    }
}
