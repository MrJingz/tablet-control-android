package com.feixiang.tabletcontrol.controller;

import com.feixiang.tabletcontrol.model.ComponentData;
import com.feixiang.tabletcontrol.model.PageData;
import com.feixiang.tabletcontrol.model.ProjectData;
import com.feixiang.tabletcontrol.service.ProjectService;
import com.feixiang.tabletcontrol.view.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

/**
 * 主控制器
 * 负责协调主界面视图和项目服务之间的交互
 */
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    private final MainView mainView;
    private final ProjectService projectService;
    
    // 应用状态
    private boolean isInitialized = false;
    private long lastOperationTime = 0;
    
    public MainController(MainView mainView, ProjectService projectService) {
        this.mainView = mainView;
        this.projectService = projectService;
        
        initializeController();
        logger.info("MainController初始化完成");
    }
    
    /**
     * 初始化控制器
     */
    private void initializeController() {
        long startTime = System.currentTimeMillis();
        logger.info("开始初始化主控制器");
        
        try {
            // 设置事件监听器
            setupEventListeners();
            
            // 加载项目数据
            loadProjectData();
            
            // 更新界面显示
            updateMainView();
            
            this.isInitialized = true;
            long duration = System.currentTimeMillis() - startTime;
            logger.info("主控制器初始化完成，耗时: {}ms", duration);
            
        } catch (Exception e) {
            logger.error("主控制器初始化失败: {}", e.getMessage(), e);
            showErrorMessage("系统初始化失败", e.getMessage());
        }
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        logger.debug("设置事件监听器");
        
        // 设置按钮点击监听器
        mainView.setSettingsButtonListener(new SettingsButtonListener());
        
        // 内容面板鼠标监听器
        mainView.setContentPanelMouseListener(new ContentPanelMouseListener());
        
        logger.debug("事件监听器设置完成");
    }
    
    /**
     * 加载项目数据
     */
    private void loadProjectData() {
        long startTime = System.currentTimeMillis();
        logger.info("开始加载项目数据");
        
        try {
            ProjectData projectData = projectService.loadProject();
            
            if (projectData != null) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("项目数据加载成功，耗时: {}ms，{}", duration, projectService.getProjectStatistics());
                
                mainView.updateStatus("项目加载完成 - " + projectData.getPageCount() + "个页面");
            } else {
                logger.info("未找到项目数据，创建新项目");
                mainView.updateStatus("新项目已创建");
            }
            
        } catch (IOException e) {
            logger.error("加载项目数据失败: {}", e.getMessage(), e);
            mainView.updateStatus("项目加载失败");
            showErrorMessage("加载项目失败", e.getMessage());
        }
    }
    
    /**
     * 更新主界面显示
     */
    private void updateMainView() {
        logger.debug("更新主界面显示");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // 清空当前内容
                mainView.clearContent();
                
                // 获取当前项目和页面
                ProjectData currentProject = projectService.getCurrentProject();
                if (currentProject == null || currentProject.isEmpty()) {
                    // 显示无内容提示
                    mainView.showNoContentMessage();
                    logger.debug("显示无内容提示");
                    return;
                }
                
                // 隐藏无内容提示
                mainView.hideNoContentMessage();
                
                // 显示当前页面内容
                displayCurrentPage();
                
                logger.debug("主界面更新完成");
                
            } catch (Exception e) {
                logger.error("更新主界面失败: {}", e.getMessage(), e);
                showErrorMessage("界面更新失败", e.getMessage());
            }
        });
    }
    
    /**
     * 显示当前页面内容
     */
    private void displayCurrentPage() {
        PageData currentPage = projectService.getCurrentPage();
        if (currentPage == null) {
            logger.debug("当前页面为空");
            return;
        }
        
        String pageName = currentPage.getName();
        List<ComponentData> components = currentPage.getComponents();
        
        logger.info("显示页面内容: {}, 组件数量: {}", pageName, components.size());
        
        // 创建并显示页面组件
        for (ComponentData componentData : components) {
            try {
                Component uiComponent = createUIComponent(componentData);
                if (uiComponent != null) {
                    mainView.addComponent(uiComponent);
                    logger.debug("添加UI组件: {} at ({}, {})", 
                               componentData.getFunctionType(), 
                               componentData.getX(), 
                               componentData.getY());
                }
            } catch (Exception e) {
                logger.error("创建UI组件失败: {}", e.getMessage(), e);
            }
        }
        
        // 更新状态
        mainView.updateStatus(String.format("页面: %s (%d个组件)", pageName, components.size()));
    }
    
    /**
     * 根据组件数据创建UI组件
     */
    private Component createUIComponent(ComponentData componentData) {
        if (componentData == null || componentData.getLabelData() == null) {
            logger.warn("组件数据或标签数据为空");
            return null;
        }
        
        try {
            // 创建JPanel作为组件容器
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBounds(componentData.getBounds());
            panel.setOpaque(true);
            
            // 创建标签
            JLabel label = new JLabel(componentData.getLabelData().getText());
            
            // 设置字体
            String fontName = componentData.getLabelData().getFontName();
            int fontSize = componentData.getLabelData().getFontSize();
            int fontStyle = componentData.getLabelData().getFontStyle();
            label.setFont(new Font(fontName, fontStyle, fontSize));
            
            // 设置颜色
            int colorRGB = componentData.getLabelData().getColorRGB();
            if (colorRGB == -1 || colorRGB == 0xFFFFFF) {
                // 白色文本改为黑色以便在白色背景上可见
                label.setForeground(Color.BLACK);
            } else {
                label.setForeground(new Color(colorRGB));
            }
            
            // 设置对齐方式
            label.setHorizontalAlignment(componentData.getLabelData().getHorizontalAlignment());
            label.setVerticalAlignment(componentData.getLabelData().getVerticalAlignment());
            
            // 设置背景
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            
            // 添加标签到面板
            panel.add(label, BorderLayout.CENTER);
            
            // 设置可见性
            panel.setVisible(true);
            label.setVisible(true);
            
            return panel;
            
        } catch (Exception e) {
            logger.error("创建UI组件失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 保存项目数据
     */
    public void saveProject() {
        long startTime = System.currentTimeMillis();
        logger.info("开始保存项目");
        
        try {
            ProjectData currentProject = projectService.getCurrentProject();
            if (currentProject != null) {
                projectService.saveProject(currentProject);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("项目保存成功，耗时: {}ms", duration);
                
                mainView.updateStatus("项目已保存");
                showInfoMessage("保存成功", "项目数据已保存");
            } else {
                logger.warn("当前项目为空，无法保存");
                mainView.updateStatus("无项目数据可保存");
            }
            
        } catch (IOException e) {
            logger.error("保存项目失败: {}", e.getMessage(), e);
            mainView.updateStatus("项目保存失败");
            showErrorMessage("保存失败", e.getMessage());
        }
    }
    
    /**
     * 刷新界面
     */
    public void refreshView() {
        logger.info("刷新界面");
        updateMainView();
    }
    
    /**
     * 显示应用程序
     */
    public void show() {
        mainView.show();
        logger.info("应用程序界面已显示");
    }
    
    /**
     * 隐藏应用程序
     */
    public void hide() {
        mainView.hide();
        logger.info("应用程序界面已隐藏");
    }
    
    /**
     * 获取项目统计信息
     */
    public String getProjectStatistics() {
        return projectService.getProjectStatistics();
    }
    
    /**
     * 显示错误消息
     */
    private void showErrorMessage(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                mainView.getMainFrame(),
                message,
                title,
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * 显示信息消息
     */
    private void showInfoMessage(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                mainView.getMainFrame(),
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    // 内部事件监听器类

    /**
     * 设置按钮点击监听器
     */
    private class SettingsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            long startTime = System.currentTimeMillis();
            logger.info("设置按钮被点击");

            try {
                // 记录操作时间
                lastOperationTime = System.currentTimeMillis();

                // 显示设置对话框或进入编辑模式
                showSettingsDialog();

                long duration = System.currentTimeMillis() - startTime;
                logger.info("设置按钮处理完成，耗时: {}ms", duration);

            } catch (Exception ex) {
                logger.error("处理设置按钮点击失败: {}", ex.getMessage(), ex);
                showErrorMessage("操作失败", "处理设置按钮点击时发生错误");
            }
        }
    }

    /**
     * 内容面板鼠标监听器
     */
    private class ContentPanelMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            logger.debug("内容面板鼠标点击: ({}, {})", e.getX(), e.getY());

            // 记录操作时间
            lastOperationTime = System.currentTimeMillis();

            // 可以在这里添加其他鼠标点击处理逻辑
        }

        @Override
        public void mousePressed(MouseEvent e) {
            logger.debug("内容面板鼠标按下: ({}, {})", e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            logger.debug("内容面板鼠标释放: ({}, {})", e.getX(), e.getY());
        }
    }

    /**
     * 显示设置对话框
     */
    private void showSettingsDialog() {
        logger.info("显示设置对话框");

        SwingUtilities.invokeLater(() -> {
            // 创建简单的设置对话框
            JDialog settingsDialog = new JDialog(mainView.getMainFrame(), "系统设置", true);
            settingsDialog.setSize(400, 300);
            settingsDialog.setLocationRelativeTo(mainView.getMainFrame());

            // 创建对话框内容
            JPanel dialogPanel = new JPanel(new BorderLayout());

            // 标题
            JLabel titleLabel = new JLabel("平板中控系统设置", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            dialogPanel.add(titleLabel, BorderLayout.NORTH);

            // 内容区域
            JPanel contentPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // 项目信息
            JLabel projectInfoLabel = new JLabel("项目信息: " + projectService.getProjectStatistics());
            projectInfoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            contentPanel.add(projectInfoLabel);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout());

            // 保存按钮
            JButton saveButton = new JButton("保存项目");
            saveButton.addActionListener(evt -> {
                saveProject();
                settingsDialog.dispose();
            });
            buttonPanel.add(saveButton);

            // 刷新按钮
            JButton refreshButton = new JButton("刷新界面");
            refreshButton.addActionListener(evt -> {
                refreshView();
                settingsDialog.dispose();
            });
            buttonPanel.add(refreshButton);

            // 关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(evt -> settingsDialog.dispose());
            buttonPanel.add(closeButton);

            contentPanel.add(buttonPanel);
            dialogPanel.add(contentPanel, BorderLayout.CENTER);

            settingsDialog.setContentPane(dialogPanel);
            settingsDialog.setVisible(true);

            logger.debug("设置对话框已显示");
        });
    }

    // Getter方法

    public boolean isInitialized() {
        return isInitialized;
    }

    public long getLastOperationTime() {
        return lastOperationTime;
    }

    public MainView getMainView() {
        return mainView;
    }

    public ProjectService getProjectService() {
        return projectService;
    }
}
