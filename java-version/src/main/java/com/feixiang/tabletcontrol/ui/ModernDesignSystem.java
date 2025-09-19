package com.feixiang.tabletcontrol.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 现代化设计系统 - 统一管理配色方案、图标、样式等视觉元素
 * 采用Material Design和Fluent Design的设计理念
 */
public class ModernDesignSystem {
    
    // ==================== 配色方案 ====================
    
    // 主色调 - 蓝色系
    public static final Color PRIMARY_50 = new Color(239, 246, 255);   // 最浅蓝
    public static final Color PRIMARY_100 = new Color(219, 234, 254);  // 浅蓝
    public static final Color PRIMARY_200 = new Color(191, 219, 254);  // 
    public static final Color PRIMARY_300 = new Color(147, 197, 253);  // 
    public static final Color PRIMARY_400 = new Color(96, 165, 250);   // 
    public static final Color PRIMARY_500 = new Color(59, 130, 246);   // 主蓝色
    public static final Color PRIMARY_600 = new Color(37, 99, 235);    // 深蓝
    public static final Color PRIMARY_700 = new Color(29, 78, 216);    // 
    public static final Color PRIMARY_800 = new Color(30, 64, 175);    // 
    public static final Color PRIMARY_900 = new Color(30, 58, 138);    // 最深蓝
    
    // 中性色调 - 灰色系
    public static final Color GRAY_50 = new Color(249, 250, 251);      // 背景色
    public static final Color GRAY_100 = new Color(243, 244, 246);     // 浅灰背景
    public static final Color GRAY_200 = new Color(229, 231, 235);     // 边框色
    public static final Color GRAY_300 = new Color(209, 213, 219);     // 分割线
    public static final Color GRAY_400 = new Color(156, 163, 175);     // 禁用文字
    public static final Color GRAY_500 = new Color(107, 114, 128);     // 次要文字
    public static final Color GRAY_600 = new Color(75, 85, 99);        // 主要文字
    public static final Color GRAY_700 = new Color(55, 65, 81);        // 标题文字
    public static final Color GRAY_800 = new Color(31, 41, 55);        // 深色文字
    public static final Color GRAY_900 = new Color(17, 24, 39);        // 最深文字
    
    // 功能色调
    public static final Color SUCCESS_500 = new Color(34, 197, 94);    // 成功绿
    public static final Color WARNING_500 = new Color(245, 158, 11);   // 警告橙
    public static final Color ERROR_500 = new Color(239, 68, 68);      // 错误红
    public static final Color INFO_500 = new Color(59, 130, 246);      // 信息蓝
    
    // 成功色系
    public static final Color SUCCESS_50 = new Color(240, 253, 244);
    public static final Color SUCCESS_100 = new Color(220, 252, 231);
    public static final Color SUCCESS_600 = new Color(22, 163, 74);
    
    // 警告色系
    public static final Color WARNING_50 = new Color(255, 251, 235);
    public static final Color WARNING_100 = new Color(254, 243, 199);
    public static final Color WARNING_600 = new Color(217, 119, 6);
    
    // 错误色系
    public static final Color ERROR_50 = new Color(254, 242, 242);
    public static final Color ERROR_100 = new Color(254, 226, 226);
    public static final Color ERROR_600 = new Color(220, 38, 38);
    
    // ==================== 字体系统 ====================
    
    public static final Font FONT_TITLE = new Font("Microsoft YaHei", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Microsoft YaHei", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Microsoft YaHei", Font.PLAIN, 12);
    public static final Font FONT_CAPTION = new Font("Microsoft YaHei", Font.PLAIN, 10);
    public static final Font FONT_BUTTON = new Font("Microsoft YaHei", Font.BOLD, 11);
    
    // 支持Unicode图标的字体 - 按优先级排序
    public static final Font FONT_ICON = getUnicodeFont(16);
    
    /**
     * 获取支持Unicode图标的字体
     */
    public static Font getUnicodeFont(int size) {
        String[] fontNames = {
            "Segoe UI Emoji",           // Windows 10/11 emoji字体
            "Apple Color Emoji",        // macOS emoji字体
            "Noto Color Emoji",         // Google emoji字体
            "Segoe UI Symbol",          // Windows符号字体
            "Symbola",                  // 通用Unicode字体
            "DejaVu Sans",              // 开源字体
            "Arial Unicode MS",         // 微软Unicode字体
            "Lucida Grande",            // macOS系统字体
            "SansSerif"                 // 系统默认字体
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, size);
            if (font.getFamily().equals(fontName) || font.canDisplay('\u2764')) { // 使用Unicode码点代替emoji
                return font;
            }
        }
        
        // 如果都不支持，返回默认字体
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }
    
    // ==================== 间距系统 ====================
    
    public static final int SPACING_XS = 4;   // 极小间距
    public static final int SPACING_SM = 8;   // 小间距
    public static final int SPACING_MD = 12;  // 中等间距
    public static final int SPACING_LG = 16;  // 大间距
    public static final int SPACING_XL = 24;  // 极大间距
    public static final int SPACING_2XL = 32; // 超大间距
    
    // ==================== 圆角系统 ====================
    
    public static final int RADIUS_SM = 4;    // 小圆角
    public static final int RADIUS_MD = 8;    // 中等圆角
    public static final int RADIUS_LG = 12;   // 大圆角
    public static final int RADIUS_XL = 16;   // 极大圆角
    
    // ==================== 阴影系统 ====================
    
    public static final Border SHADOW_SM = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0, 0, 0, 10), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    );
    
    public static final Border SHADOW_MD = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0, 0, 0, 15), 1),
        BorderFactory.createEmptyBorder(4, 4, 4, 4)
    );
    
    // ==================== 图标系统 ====================
    
    public static final Map<String, String> ICONS = new HashMap<String, String>() {{
        put("settings", "⚙️");
        put("page", "📄");
        put("add", "➕");
        put("delete", "🗑️");
        put("edit", "✏️");
        put("copy", "📋");
        put("move", "📦");
        put("save", "💾");
        put("color", "🎨");
        put("font", "🔤");
        put("scale", "🔍");
        put("position", "📍");
        put("function", "⚙️");
        put("canvas", "🖼️");
        put("grid", "⊞");
        put("fullscreen", "⛶");
        put("close", "✖️");
        put("menu", "☰");
        put("check", "✓");
        put("warning", "⚠️");
        put("info", "ℹ️");
        put("success", "✅");
        put("error", "❌");
    }};
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建现代化按钮
     */
    public static JButton createModernButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setBorder(BorderFactory.createEmptyBorder(SPACING_SM, SPACING_LG, SPACING_SM, SPACING_LG));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        addButtonHoverEffect(button, bgColor);
        
        return button;
    }
    
    /**
     * 创建现代化卡片面板
     */
    public static JPanel createModernCard(String title, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRAY_200, 1),
            BorderFactory.createEmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD)
        ));
        
        // 卡片标题
        if (title != null && !title.isEmpty()) {
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            headerPanel.setBackground(Color.WHITE);
            
            if (icon != null && !icon.isEmpty()) {
                JLabel iconLabel = new JLabel(icon);
                iconLabel.setFont(FONT_ICON);
                iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, SPACING_SM));
                headerPanel.add(iconLabel);
            }
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_SUBTITLE);
            titleLabel.setForeground(GRAY_700);
            headerPanel.add(titleLabel);
            
            card.add(headerPanel, BorderLayout.NORTH);
        }
        
        return card;
    }
    
    /**
     * 添加现代化按钮悬停效果（带动画）
     */
    public static void addButtonHoverEffect(JButton button, Color originalColor) {
        Color hoverColor = darkenColor(originalColor, 0.1f);
        Color pressedColor = darkenColor(originalColor, 0.2f);
        
        // 现代化悬停效果实现
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateColorTransition(button, button.getBackground(), hoverColor, 150);
                // 添加轻微的缩放效果
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(lightenColor(originalColor, 0.3f), 1),
                    button.getBorder()
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                animateColorTransition(button, button.getBackground(), originalColor, 150);
                // 恢复原始边框
                if (button.getBorder() instanceof javax.swing.border.CompoundBorder) {
                    javax.swing.border.CompoundBorder compound = (javax.swing.border.CompoundBorder) button.getBorder();
                    button.setBorder(compound.getInsideBorder());
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(pressedColor);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.contains(e.getPoint())) {
                    button.setBackground(hoverColor);
                } else {
                    button.setBackground(originalColor);
                }
            }
        });
    }
    
    /**
     * 颜色过渡动画
     */
    private static void animateColorTransition(JButton button, Color fromColor, Color toColor, int duration) {
        Timer timer = new Timer(16, null); // 60 FPS
        long startTime = System.currentTimeMillis();
        
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / duration);
            
            // 使用缓动函数（ease-out）
            progress = 1 - (1 - progress) * (1 - progress);
            
            Color currentColor = interpolateColor(fromColor, toColor, progress);
            button.setBackground(currentColor);
            
            if (progress >= 1.0f) {
                timer.stop();
            }
        });
        
        timer.start();
    }
    
    /**
     * 颜色插值
     */
    private static Color interpolateColor(Color from, Color to, float progress) {
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * progress);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * progress);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * progress);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * progress);
        
        return new Color(Math.max(0, Math.min(255, r)), 
                        Math.max(0, Math.min(255, g)), 
                        Math.max(0, Math.min(255, b)), 
                        Math.max(0, Math.min(255, a)));
    }
    
    /**
     * 颜色加深工具方法
     */
    public static Color darkenColor(Color color, float factor) {
        int r = Math.max(0, (int)(color.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(color.getBlue() * (1 - factor)));
        return new Color(r, g, b, color.getAlpha());
    }
    
    /**
     * 颜色变浅工具方法
     */
    public static Color lightenColor(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b, color.getAlpha());
    }
}
