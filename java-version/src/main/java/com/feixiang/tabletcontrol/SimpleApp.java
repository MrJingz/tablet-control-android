package com.feixiang.tabletcontrol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化版平板中控应用程序
 * 使用Swing实现，不依赖外部库
 * 用于验证Java转换的核心功能
 */
public class SimpleApp extends JFrame {
    private static final String APP_TITLE = "平板中控系统 - Java版";
    private static final String DEFAULT_PASSWORD_HASH = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";
    
    private JTextArea logArea;
    private JLabel statusLabel;
    private boolean isAuthorized = false;
    
    public SimpleApp() {
        initializeUI();
        appendLog("应用程序启动成功");
        appendLog("Java版本: " + System.getProperty("java.version"));
        appendLog("操作系统: " + System.getProperty("os.name"));
    }
    
    private void initializeUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建中心面板
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.LIGHT_GRAY);
        
        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        statusLabel = new JLabel("状态: " + (isAuthorized ? "已授权" : "未授权"));
        statusLabel.setForeground(isAuthorized ? Color.GREEN : Color.RED);
        
        panel.add(titleLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(statusLabel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel logLabel = new JLabel("系统日志:");
        logLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        logArea = new JTextArea(20, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(logLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton authButton = new JButton("授权管理");
        authButton.addActionListener(e -> showAuthDialog());
        
        JButton passwordButton = new JButton("密码验证");
        passwordButton.addActionListener(e -> showPasswordDialog());
        
        JButton infoButton = new JButton("系统信息");
        infoButton.addActionListener(e -> showSystemInfo());
        
        JButton exitButton = new JButton("退出");
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> exitApplication());
        
        panel.add(authButton);
        panel.add(passwordButton);
        panel.add(infoButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    private void showAuthDialog() {
        String[] options = {"生成授权码", "验证授权码", "取消"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "请选择授权操作:",
            "授权管理",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        switch (choice) {
            case 0: // 生成授权码
                generateAuthCode();
                break;
            case 1: // 验证授权码
                verifyAuthCode();
                break;
            default:
                break;
        }
    }
    
    private void generateAuthCode() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JTextField dateField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        JTextField userField = new JTextField("default_user");
        JCheckBox permanentBox = new JCheckBox("永久授权");
        
        panel.add(new JLabel("日期 (YYYYMMDD):"));
        panel.add(dateField);
        panel.add(new JLabel("用户ID:"));
        panel.add(userField);
        panel.add(new JLabel("永久授权:"));
        panel.add(permanentBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "生成授权码", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String dateStr = dateField.getText().trim();
                String userId = userField.getText().trim();
                boolean isPermanent = permanentBox.isSelected();
                
                String authCode = generateSimpleAuthCode(dateStr, userId, isPermanent);
                
                JOptionPane.showMessageDialog(this, "授权码生成成功:\n" + authCode, "成功", JOptionPane.INFORMATION_MESSAGE);
                appendLog("授权码生成成功: " + authCode + " (用户: " + userId + ", 永久: " + isPermanent + ")");
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "授权码生成失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                appendLog("授权码生成失败: " + e.getMessage());
            }
        }
    }
    
    private void verifyAuthCode() {
        String authCode = JOptionPane.showInputDialog(this, "请输入要验证的授权码:", "验证授权码", JOptionPane.QUESTION_MESSAGE);
        
        if (authCode != null && !authCode.trim().isEmpty()) {
            boolean isValid = verifySimpleAuthCode(authCode.trim());
            
            if (isValid) {
                JOptionPane.showMessageDialog(this, "授权码验证成功！", "验证结果", JOptionPane.INFORMATION_MESSAGE);
                appendLog("授权码验证成功: " + authCode);
                isAuthorized = true;
                updateStatus();
            } else {
                JOptionPane.showMessageDialog(this, "授权码验证失败！", "验证结果", JOptionPane.ERROR_MESSAGE);
                appendLog("授权码验证失败: " + authCode);
            }
        }
    }
    
    private void showPasswordDialog() {
        JPasswordField passwordField = new JPasswordField(20);
        int result = JOptionPane.showConfirmDialog(
            this,
            passwordField,
            "请输入密码 (默认: 123):",
            JOptionPane.OK_CANCEL_OPTION
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            boolean isValid = verifyPassword(password);
            
            if (isValid) {
                JOptionPane.showMessageDialog(this, "密码验证成功！", "验证结果", JOptionPane.INFORMATION_MESSAGE);
                appendLog("密码验证成功");
                isAuthorized = true;
                updateStatus();
            } else {
                JOptionPane.showMessageDialog(this, "密码验证失败！", "验证结果", JOptionPane.ERROR_MESSAGE);
                appendLog("密码验证失败");
            }
        }
    }
    
    private void showSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("系统信息:\n\n");
        info.append("应用程序: ").append(APP_TITLE).append("\n");
        info.append("Java版本: ").append(System.getProperty("java.version")).append("\n");
        info.append("操作系统: ").append(System.getProperty("os.name")).append("\n");
        info.append("系统架构: ").append(System.getProperty("os.arch")).append("\n");
        info.append("用户目录: ").append(System.getProperty("user.home")).append("\n");
        info.append("工作目录: ").append(System.getProperty("user.dir")).append("\n");
        info.append("授权状态: ").append(isAuthorized ? "已授权" : "未授权").append("\n");
        
        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "系统信息", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出应用程序吗?",
            "确认退出",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            appendLog("用户确认退出应用程序");
            System.exit(0);
        }
    }
    
    private String generateSimpleAuthCode(String dateStr, String userId, boolean isPermanent) {
        // 简化的授权码生成逻辑
        String salt = generateRandomString(6);
        String data = dateStr + "|" + userId + "|" + salt;
        String hash = sha256Hash(data).substring(0, 8).toUpperCase();
        
        return dateStr + "-" + salt + "-" + hash + (isPermanent ? "-PERM" : "");
    }
    
    private boolean verifySimpleAuthCode(String authCode) {
        // 简化的授权码验证逻辑
        if (authCode == null || authCode.length() < 15) {
            return false;
        }
        
        // 基本格式检查
        String[] parts = authCode.split("-");
        if (parts.length < 3) {
            return false;
        }
        
        // 检查日期格式
        String dateStr = parts[0];
        if (dateStr.length() != 8) {
            return false;
        }
        
        try {
            Integer.parseInt(dateStr);
            return true; // 简化验证，实际应该验证完整的哈希
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean verifyPassword(String password) {
        if (password == null) {
            return false;
        }
        
        String hash = sha256Hash(password);
        return DEFAULT_PASSWORD_HASH.equals(hash);
    }
    
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256哈希计算失败", e);
        }
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logEntry = String.format("[%s] %s\n", timestamp, message);
            logArea.append(logEntry);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void updateStatus() {
        statusLabel.setText("状态: " + (isAuthorized ? "已授权" : "未授权"));
        statusLabel.setForeground(isAuthorized ? Color.GREEN : Color.RED);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleApp().setVisible(true);
        });
    }
}