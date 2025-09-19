package com.feixiang.tabletcontrol.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 字体测试工具 - 帮助检测系统可用的Unicode字体
 */
public class FontTestUtil {
    
    private static final String[] TEST_EMOJIS = {
        "😀", "❤️", "⚙️", "📱", "🎮", "📺", "➕", "🗑️", "✏️", "📋",
        "📦", "💾", "🔄", "📁", "⌨️", "⏰", "❌", "✅", "⚠️", "ℹ️"
    };
    
    private static final String[] COMMON_FONTS = {
        "Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji",
        "Segoe UI Symbol", "Microsoft YaHei", "Arial Unicode MS",
        "DejaVu Sans", "Liberation Sans", "Symbola", "Code2000"
    };
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("字体测试工具 - Unicode支持检测");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 顶部控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton testButton = new JButton("检测系统字体");
        JButton refreshButton = new JButton("刷新显示");
        controlPanel.add(testButton);
        controlPanel.add(refreshButton);
        
        // 结果显示区域
        JTextArea resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        // 字体预览面板
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.setBorder(BorderFactory.createTitledBorder("字体预览"));
        JScrollPane previewScrollPane = new JScrollPane(previewPanel);
        previewScrollPane.setPreferredSize(new Dimension(400, 0));
        
        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, previewScrollPane);
        splitPane.setDividerLocation(400);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // 按钮事件
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = performFontTest();
                resultArea.setText(result);
                updatePreviewPanel(previewPanel);
                frame.revalidate();
                frame.repaint();
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePreviewPanel(previewPanel);
                frame.revalidate();
                frame.repaint();
            }
        });
        
        frame.add(mainPanel);
        frame.setVisible(true);
        
        // 自动执行一次测试
        SwingUtilities.invokeLater(() -> testButton.doClick());
    }
    
    private static String performFontTest() {
        StringBuilder result = new StringBuilder();
        result.append("=== 系统字体Unicode支持检测报告 ===\n\n");
        
        // 获取系统所有字体
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] allFonts = ge.getAvailableFontFamilyNames();
        
        result.append("系统总字体数量: ").append(allFonts.length).append("\n\n");
        
        // 测试常用字体
        result.append("=== 常用Unicode字体测试 ===\n");
        List<String> supportedFonts = new ArrayList<>();
        
        for (String fontName : COMMON_FONTS) {
            Font font = new Font(fontName, Font.PLAIN, 16);
            int supportedEmojis = 0;
            
            for (String emoji : TEST_EMOJIS) {
                if (canDisplayString(font, emoji)) {
                    supportedEmojis++;
                }
            }
            
            double supportRate = (double) supportedEmojis / TEST_EMOJIS.length * 100;
            String status = supportRate >= 50 ? "✅ 推荐" : supportRate >= 25 ? "⚠️ 部分支持" : "❌ 不推荐";
            
            result.append(String.format("%-20s | %s | 支持率: %.1f%% (%d/%d)\n", 
                fontName, status, supportRate, supportedEmojis, TEST_EMOJIS.length));
            
            if (supportRate >= 25) {
                supportedFonts.add(fontName);
            }
        }
        
        result.append("\n=== 推荐字体列表 ===\n");
        if (supportedFonts.isEmpty()) {
            result.append("未找到合适的Unicode字体，建议安装 Segoe UI Emoji 或 Noto Color Emoji\n");
        } else {
            for (int i = 0; i < supportedFonts.size(); i++) {
                result.append((i + 1)).append(". ").append(supportedFonts.get(i)).append("\n");
            }
        }
        
        result.append("\n=== 系统信息 ===\n");
        result.append("操作系统: ").append(System.getProperty("os.name")).append("\n");
        result.append("Java版本: ").append(System.getProperty("java.version")).append("\n");
        result.append("默认字体: ").append(new Font(Font.SANS_SERIF, Font.PLAIN, 12).getFamily()).append("\n");
        
        return result.toString();
    }
    
    private static void updatePreviewPanel(JPanel previewPanel) {
        previewPanel.removeAll();
        
        // 为每个推荐字体创建预览
        for (String fontName : COMMON_FONTS) {
            Font font = new Font(fontName, Font.PLAIN, 16);
            
            JPanel fontPanel = new JPanel(new BorderLayout());
            fontPanel.setBorder(BorderFactory.createTitledBorder(fontName));
            
            // 字体信息
            JLabel infoLabel = new JLabel("实际字体: " + font.getFamily());
            infoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            fontPanel.add(infoLabel, BorderLayout.NORTH);
            
            // emoji预览
            JPanel emojiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            for (String emoji : TEST_EMOJIS) {
                JLabel emojiLabel = new JLabel(emoji);
                emojiLabel.setFont(font);
                emojiLabel.setToolTipText(emoji + " - " + (canDisplayString(font, emoji) ? "支持" : "不支持"));
                emojiPanel.add(emojiLabel);
            }
            
            fontPanel.add(emojiPanel, BorderLayout.CENTER);
            previewPanel.add(fontPanel);
        }
    }
    
    private static boolean canDisplayString(Font font, String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!font.canDisplay(c)) {
                return false;
            }
        }
        
        return true;
    }
}
