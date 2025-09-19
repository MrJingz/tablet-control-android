package com.feixiang.tabletcontrol;

import com.feixiang.tabletcontrol.auth.AuthCodeGeneratorFile;
import com.feixiang.tabletcontrol.config.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import javax.swing.DropMode;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

/**
 * 飞象中控应用程序主类 - Swing版本
 * 从Python版本的HelloWorldApp转换而来，实现完整的平板中控功能
 */
public class SwingTabletControlApp extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(SwingTabletControlApp.class);

    // 应用程序常量
    private static final String APP_TITLE = "飞象中控应用程序";
    private static final String APP_VERSION = "Java版 v1.0";

    // 应用程序状态
    private SecurityConfig securityConfig;
    private AuthCodeGeneratorFile authGenerator;
    private Timer authCheckTimer;
    private boolean isAuthorized = false;

    // 标记更新授权界面的来源
    private boolean isFromExpiredDialog = false;

    // UI组件
    private JPanel gearButton;
    private Dimension lastWindowSize;

    // 界面状态管理
    private boolean isEditMode = false;
    private JPanel mainContentPanel; // 主界面内容
    private JPanel editContentPanel; // 编辑界面内容
    private JFrame editWindow; // 独立的编辑窗口

    // 页面和实例管理
    private boolean hasCurrentPage = false; // 当前是否有页面存在
    private JPanel selectedInstance = null; // 当前选中的实例
    private boolean isUpdatingFromInstance = false; // 防止循环更新的标志
    private String currentPageName = null; // 当前页面名称
    private java.util.Map<String, java.util.List<Component>> pageContents = new java.util.LinkedHashMap<>(); // 每个页面的内容
    private java.util.Map<String, String> pageBackgrounds = new java.util.LinkedHashMap<>(); // 每个页面的背景图片路径
    private Point dragStartPoint = null; // 拖拽起始点

    // 属性面板控件引用
    private JTextField textField;
    private JComboBox<String> fontComboBox;
    private JComboBox<String> sizeComboBox;
    private JButton colorButton;
    private JTextField xField;
    private JTextField yField;
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private JButton styleButton;

    // 功能属性控件引用
    private JComboBox<String> functionTypeComboBox;
    private JPanel pageJumpPanel;
    private JPanel commandPanel;
    private JPanel instanceGroupPanel;
    private JComboBox<String> targetPageComboBox; // 目标页面下拉框引用

    // 编辑模式相关组件
    private JList<String> pageList;
    private DefaultListModel<String> pageListModel;
    private JLabel currentPageLabel;
    private JPanel editCanvas; // 编辑画布
    private JComboBox<String> resolutionComboBox; // 分辨率选择器

    // 任务栏自适应相关
    private Timer taskbarMonitorTimer;
    private Rectangle lastTaskbarBounds;
    private boolean taskbarAtBottom = false;

    // 编辑分辨率信息
    private String currentEditResolution = "1024x768"; // 默认编辑分辨率

    // 定时器功能相关
    private java.util.List<ScheduledTask> timerTasks = new java.util.ArrayList<>();
    private JDialog timerDialog;
    private DefaultListModel<ScheduledTask> timerListModel;
    private JList<ScheduledTask> timerList;

    // 属性面板引用
    private JPanel propertyBar;

    public SwingTabletControlApp() {
        System.out.println("=== 程序启动 " + new java.util.Date() + " ===");
        System.out.println("SwingTabletControlApp构造函数开始");
        initializeComponents();
        System.out.println("initializeComponents完成");
        setupWindow();
        System.out.println("setupWindow完成");
        createMainInterface();
        System.out.println("createMainInterface完成");

        // 生成测试授权码（仅在开发阶段）
        generateTestAuthCodesOnStartup();

        checkAuthorizationStatus();
        startPeriodicAuthCheck();
        loadAndApplySettings();

        // 启动时加载保存的数据并显示主界面
        SwingUtilities.invokeLater(() -> {
            // 确保所有组件都已初始化
            System.out.println("程序启动，开始加载界面...");
            System.out.println("检查pageListModel状态: " + (pageListModel != null ? "已初始化" : "未初始化"));
            loadInterface();
            System.out.println("界面加载完成");
        });
    }

    private void initializeComponents() {
        try {
            logger.info("正在初始化安全配置...");
            securityConfig = new SecurityConfig();

            logger.info("正在初始化授权文件存储...");
            authGenerator = new AuthCodeGeneratorFile();

            logger.info("正在初始化授权码生成器...");

            logger.info("应用程序组件初始化完成");
        } catch (Exception e) {
            logger.error("初始化组件时发生错误: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "应用程序初始化失败: " + e.getMessage(),
                                        "初始化错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 在启动时生成测试授权码
    private void generateTestAuthCodesOnStartup() {
        try {
            logger.info("开始生成测试授权码...");

            // 生成当前日期的授权码
            Map<String, Object> result1 = authGenerator.generateAuthCode("20250831", "default_user", false);
            if ((Boolean) result1.get("success")) {
                String authCode1 = (String) result1.get("auth_code");
                logger.info("生成当前日期授权码成功: {}", authCode1);
                System.out.println("✅ 当前日期授权码: " + authCode1);
            }

            // 生成未来日期的授权码
            Map<String, Object> result2 = authGenerator.generateAuthCode("20250901", "default_user", false);
            if ((Boolean) result2.get("success")) {
                String authCode2 = (String) result2.get("auth_code");
                logger.info("生成未来日期授权码成功: {}", authCode2);
                System.out.println("✅ 未来日期授权码: " + authCode2);
            }

            // 生成长期有效的授权码
            Map<String, Object> result3 = authGenerator.generateAuthCode("20251231", "default_user", false);
            if ((Boolean) result3.get("success")) {
                String authCode3 = (String) result3.get("auth_code");
                logger.info("生成长期授权码成功: {}", authCode3);
                System.out.println("✅ 长期有效授权码: " + authCode3);
            }

            // 生成永久授权码
            Map<String, Object> result4 = authGenerator.generateAuthCode("99991231", "default_user", true);
            if ((Boolean) result4.get("success")) {
                String authCode4 = (String) result4.get("auth_code");
                logger.info("生成永久授权码成功: {}", authCode4);
                System.out.println("✅ 永久授权码: " + authCode4);
            }

            logger.info("测试授权码生成完成");

        } catch (Exception e) {
            logger.error("生成测试授权码失败: {}", e.getMessage(), e);
        }
    }

    private void setupWindow() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 添加窗口关闭事件处理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupAndExit();
            }
        });

        // 获取屏幕分辨率并设置窗口大小
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // 设置为无边框全屏窗口
        setUndecorated(true);
        setResizable(false);

        // 使用真正的全屏模式
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);

        // 确保窗口在最前面
        setAlwaysOnTop(false); // 不要总是在最前面，避免影响其他应用

        System.out.println("主界面窗口设置: 屏幕尺寸=" + screenSize.width + "x" + screenSize.height +
                         ", 窗口状态=MAXIMIZED_BOTH, 无边框=true");

        // 创建主内容面板
        createMainContentPanel();

        // 设置最小尺寸为1x1像素，允许无限缩小
        setMinimumSize(new Dimension(1, 1));

        // 绑定快捷键
        setupKeyBindings();

        // 绑定窗口大小变化事件
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 窗口大小变化时更新齿轮按钮位置
                SwingUtilities.invokeLater(() -> {
                    updateGearButtonStyle();
                });
            }
        });
    }

    private void setupKeyBindings() {
        // 绑定ESC键退出全屏
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });

        // 绑定F11键切换全屏模式
        KeyStroke f11KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f11KeyStroke, "F11");
        getRootPane().getActionMap().put("F11", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });

        // 绑定F10键切换窗口大小（用于测试响应式效果）
        KeyStroke f10KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f10KeyStroke, "F10");
        getRootPane().getActionMap().put("F10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleWindowSize();
            }
        });
    }

    private void createMainInterface() {
        // 界面创建将在 setupWindow 中的 createMainContentPanel 中完成
        logger.info("应用程序界面创建完成");
    }

    private void createMainContentPanel() {
        // 创建主内容面板，消除留白
        mainContentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 242, 247),
                    0, getHeight(), new Color(248, 250, 252)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // 计算缩放因子，基于窗口尺寸
                double scaleFactor = Math.min(getWidth() / 800.0, getHeight() / 600.0);
                scaleFactor = Math.max(0.3, Math.min(3.0, scaleFactor)); // 限制缩放范围

                // 绘制中央logo或提示文字（自适应大小）
                g2d.setColor(new Color(108, 117, 125, 80));
                int mainFontSize = Math.max(20, (int)(48 * scaleFactor));
                g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, mainFontSize));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "平板中控系统";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);

                // 绘制副标题（自适应大小）
                int subFontSize = Math.max(10, (int)(16 * scaleFactor));
                g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, subFontSize));
                g2d.setColor(new Color(108, 117, 125, 120));
                fm = g2d.getFontMetrics();
                String subText = "点击左下角齿轮图标进入设置";
                textWidth = fm.stringWidth(subText);
                x = (getWidth() - textWidth) / 2;
                int subTextOffset = Math.max(30, (int)(60 * scaleFactor));
                y = y + subTextOffset;
                g2d.drawString(subText, x, y);

                g2d.dispose();
            }
        };

        mainContentPanel.setLayout(null); // 使用绝对定位以便齿轮按钮定位
        mainContentPanel.setOpaque(true); // 确保面板不透明，能正确绘制背景
        setContentPane(mainContentPanel);

        // 创建齿轮按钮
        createGearButton();

        // 强制重绘界面
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private void createGearButton() {
        // 直接创建齿轮按钮，无需延迟
        updateGearButtonStyle();

        // 确保按钮在窗口显示后再次更新位置
        SwingUtilities.invokeLater(() -> {
            if (gearButton != null) {
                updateGearButtonStyle();
            }
        });
    }

    private void updateGearButtonStyle() {
        // 获取窗口尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = screenSize.width;
        int windowHeight = screenSize.height;

        if (getWidth() > 1 && getHeight() > 1) {
            windowWidth = getWidth();
            windowHeight = getHeight();
        }

        // 销毁旧按钮
        if (gearButton != null) {
            Container contentPane = getContentPane();
            if (contentPane != null) {
                contentPane.remove(gearButton);
            }
        }

        // 创建带齿轮图标的按钮
        gearButton = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制背景
                g2d.setColor(new Color(108, 117, 125));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // 绘制齿轮图标
                drawGearIcon(g2d, getWidth(), getHeight());

                g2d.dispose();
            }
        };
        gearButton.setOpaque(false);
        gearButton.setPreferredSize(new Dimension(30, 30));
        gearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 绑定点击事件
        gearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSecurityDialog();
            }
        });

        // 绝对定位到左下角
        int buttonXOffset = 10;
        int buttonYOffset = 10;

        gearButton.setBounds(buttonXOffset, windowHeight - 30 - buttonYOffset, 30, 30);

        // 添加到内容面板并设置为最顶层
        Container contentPane = getContentPane();
        if (contentPane != null) {
            contentPane.add(gearButton);
            contentPane.setComponentZOrder(gearButton, 0);
            System.out.println("齿轮按钮已创建并设置为最顶层");
        }
        gearButton.setVisible(true);
        repaint();
    }

    private void drawGearIcon(Graphics2D g2d, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int outerRadius = Math.min(width, height) / 3;
        int innerRadius = outerRadius * 2 / 3;
        int centerRadius = outerRadius / 3;

        // 设置齿轮颜色为白色
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // 绘制8个齿
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            double toothAngle = Math.PI / 16;

            // 计算齿的顶点
            double angle1 = angle - toothAngle;
            double angle2 = angle + toothAngle;

            int x1 = centerX + (int)(innerRadius * Math.cos(angle1));
            int y1 = centerY + (int)(innerRadius * Math.sin(angle1));
            int x2 = centerX + (int)(outerRadius * Math.cos(angle1));
            int y2 = centerY + (int)(outerRadius * Math.sin(angle1));
            int x3 = centerX + (int)(outerRadius * Math.cos(angle2));
            int y3 = centerY + (int)(outerRadius * Math.sin(angle2));
            int x4 = centerX + (int)(innerRadius * Math.cos(angle2));
            int y4 = centerY + (int)(innerRadius * Math.sin(angle2));

            // 绘制齿的轮廓
            g2d.drawLine(x1, y1, x2, y2);
            g2d.drawLine(x2, y2, x3, y3);
            g2d.drawLine(x3, y3, x4, y4);
        }

        // 绘制内圆环
        g2d.drawOval(centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);

        // 绘制中心圆
        g2d.fillOval(centerX - centerRadius, centerY - centerRadius,
                    centerRadius * 2, centerRadius * 2);
    }

    private void showSecurityDialog() {
        System.out.println("齿轮按钮被点击了！");

        // 创建简洁的安全验证对话框 - 按照新UI设计
        JDialog securityDialog = new JDialog(this, "安全验证", true);
        securityDialog.setSize(400, 280);
        securityDialog.setResizable(false);
        securityDialog.setLocationRelativeTo(this);
        securityDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        securityDialog.getContentPane().setBackground(Color.WHITE);
        securityDialog.setLayout(new BorderLayout());

        // 主容器 - 调整边距使内容更居中
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 40, 40, 40)); // 增加顶部边距

        // 密码输入区域 - 按照新UI设计
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 密码标签
        JLabel passwordLabel = new JLabel("输入密码");
        passwordLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(51, 51, 51));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(Box.createVerticalStrut(8));

        // 简洁的密码输入框
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(new Color(51, 51, 51));
        passwordField.setPreferredSize(new Dimension(320, 35));
        passwordField.setMaximumSize(new Dimension(320, 35));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordPanel.add(passwordField);

        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        // 按钮区域 - 按照新UI设计，两个按钮居中排列
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        // 确认修改按钮 - 蓝色
        JButton okButton = new JButton("确认修改");
        okButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        okButton.setPreferredSize(new Dimension(120, 40));
        okButton.setBackground(new Color(52, 144, 220)); // 蓝色
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setOpaque(true);

        // 取消按钮 - 红色
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setBackground(new Color(220, 53, 69)); // 红色
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setOpaque(true);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        securityDialog.add(mainPanel, BorderLayout.CENTER);

        // 事件处理 - 保持原有的验证逻辑
        okButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(securityDialog, "请输入密码", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 使用SecurityConfig验证密码
            if (securityConfig.verifyPassword(password)) {
                securityDialog.dispose();
                showMainMenuDialog();
            } else {
                JOptionPane.showMessageDialog(securityDialog, "密码错误，请重新输入", "错误", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        });

        cancelButton.addActionListener(e -> securityDialog.dispose());

        // 快捷键绑定
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okButton.doClick();
                }
            }
        });

        securityDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    securityDialog.dispose();
                }
            }
        });

        // 设置焦点并显示对话框
        passwordField.requestFocus();
        securityDialog.setVisible(true);
    }

    private void showMainMenuDialog() {
        // 每次显示主菜单时都重新加载界面
        loadInterface();

        // 创建现代化主菜单对话框
        JDialog mainMenuDialog = new JDialog(this, "系统功能菜单", true);
        mainMenuDialog.setSize(540, 450);
        mainMenuDialog.setResizable(false);
        mainMenuDialog.setLocationRelativeTo(this);
        mainMenuDialog.getContentPane().setBackground(new Color(248, 250, 252));

        // 创建现代化主容器
        JPanel mainContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制微妙的渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(248, 250, 252)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        mainContainer.setLayout(new BorderLayout());
        mainContainer.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        // 现代化标题
        JLabel titleLabel = new JLabel("系统功能菜单", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 41, 59)); // Slate 800
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        mainContainer.add(titleLabel, BorderLayout.NORTH);

        // 现代化按钮容器 - 使用网格布局
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        buttonPanel.setOpaque(false); // 透明背景以显示渐变
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 创建功能按钮
        String[][] buttonData = {
            {"编辑模式", "52, 152, 219"},
            {"修改密码", "231, 76, 60"},
            {"更新日期授权", "243, 156, 18"},
            {"创建日期授权", "39, 174, 96"},
            {"系统设置", "155, 89, 182"},
            {"关闭程序", "149, 165, 166"}
        };

        for (String[] data : buttonData) {
            String text = data[0];
            String[] rgb = data[1].split(", ");
            Color bgColor = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));

            JButton button = createMenuButton(text, bgColor);

            // 绑定按钮事件
            switch (text) {
                case "编辑模式":
                    button.addActionListener(e -> {
                        mainMenuDialog.dispose();
                        showEditInterface();
                    });
                    break;
                case "修改密码":
                    button.addActionListener(e -> {
                        mainMenuDialog.dispose();
                        showChangePasswordDialog();
                    });
                    break;
                case "更新日期授权":
                    button.addActionListener(e -> {
                        mainMenuDialog.dispose();
                        isFromExpiredDialog = false; // 明确标记来源为菜单
                        showUpdateAuthDialog();
                    });
                    break;
                case "创建日期授权":
                    button.addActionListener(e -> {
                        // 验证开发者密码
                        String developerPassword = JOptionPane.showInputDialog(mainMenuDialog,
                            "请输入开发者密码：", "开发者验证", JOptionPane.PLAIN_MESSAGE);

                        if (developerPassword == null) {
                            // 用户取消了输入
                            return;
                        }

                        if (!"feixiangmoxing".equals(developerPassword)) {
                            // 密码错误
                            JOptionPane.showMessageDialog(mainMenuDialog,
                                "密码错误，无法执行此操作",
                                "验证失败", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // 密码正确，继续执行
                        mainMenuDialog.dispose();
                        showCreateAuthDialog();
                    });
                    break;
                case "系统设置":
                    button.addActionListener(e -> {
                        mainMenuDialog.dispose();
                        showSystemSettingsDialog();
                    });
                    break;
                case "关闭程序":
                    button.addActionListener(e -> {
                        mainMenuDialog.dispose();
                        System.exit(0);
                    });
                    break;
            }

            buttonPanel.add(button);
        }

        mainContainer.add(buttonPanel, BorderLayout.CENTER);

        // 添加到对话框
        mainMenuDialog.add(mainContainer);

        // 绑定ESC键关闭
        mainMenuDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        mainMenuDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenuDialog.dispose();
            }
        });

        mainMenuDialog.setVisible(true);
    }

    private JButton createMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
             @Override
             protected void paintComponent(Graphics g) {
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                 // 获取状态
                 boolean hovered = Boolean.TRUE.equals(getClientProperty("hovered"));
                 boolean pressed = Boolean.TRUE.equals(getClientProperty("pressed"));

                 // 计算颜色
                 Color currentColor = bgColor;
                 if (pressed) {
                     currentColor = darkenColor(bgColor, 0.7f);
                 } else if (hovered) {
                     currentColor = darkenColor(bgColor, 0.85f);
                 }

                // 绘制阴影
                 if (!pressed) {
                     g2d.setColor(new Color(0, 0, 0, 20));
                     g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                 }

                // 绘制按钮背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, currentColor,
                    0, getHeight(), darkenColor(currentColor, 0.9f)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);

                // 绘制边框
                g2d.setColor(darkenColor(currentColor, 0.8f));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 10, 10);

                // 绘制文本 - 使用简单可靠的文字渲染方式
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2d.drawString(text, textX, textY);

                g2d.dispose();
            }
        };

        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 50));
        button.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 现代化悬停和按下效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("hovered", true);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("hovered", false);
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("pressed", true);
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("pressed", false);
                button.repaint();
            }
        });

        return button;
    }

    private Color darkenColor(Color color) {
        return darkenColor(color, 0.8f);
    }

    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max(0, (int)(color.getRed() * factor)),
            Math.max(0, (int)(color.getGreen() * factor)),
            Math.max(0, (int)(color.getBlue() * factor))
        );
    }

    private void addButtonHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }

    /**
     * 创建编辑界面内容面板
     */
    private JPanel createEditContentPanel() {
        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.setBackground(new Color(240, 242, 245));

        // 顶部属性配置栏
        JPanel topPanel = createTopPropertyBar();
        editPanel.add(topPanel, BorderLayout.NORTH);

        // 主内容区域
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 左侧页面列表
        JPanel leftPanel = createLeftPageList();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 中央编辑区域
        JPanel centerPanel = createCenterEditArea();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 右侧功能按钮
        JPanel rightPanel = createRightFunctionButtons();
        mainPanel.add(rightPanel, BorderLayout.EAST);

        editPanel.add(mainPanel, BorderLayout.CENTER);

        return editPanel;
    }

    // 编辑模式界面 - 在当前窗口内切换
     private void showEditInterface() {
         if (isEditMode) {
             return; // 已经在编辑模式，无需重复切换
         }

         isEditMode = true;

         // 创建编辑界面内容面板（如果还没有创建）
         if (editContentPanel == null) {
             editContentPanel = createEditContentPanel();
         }

         // 创建独立的编辑窗口
         if (editWindow == null) {
             editWindow = new JFrame("编辑模式 - 中控系统");
             editWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

             // 设置为全屏显示
             editWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
             editWindow.setUndecorated(true); // 移除窗口装饰（标题栏等）

             // 获取屏幕尺寸并设置窗口大小
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             editWindow.setSize(screenSize);
             editWindow.setLocation(0, 0);

             // 添加窗口关闭监听器
             editWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                 @Override
                 public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                     exitEditMode();
                 }
             });
         }

         // 设置编辑窗口内容
         editWindow.setContentPane(editContentPanel);

         // 使用统一入口加载编辑界面
         loadInterface();

         // ESC键绑定已删除 - 不再支持ESC键退出编辑模式

         // 隐藏主窗口
         this.setVisible(false);

         // 显示编辑窗口
         editWindow.setVisible(true);
         editWindow.toFront();
         editWindow.requestFocus();
     }

     /**
      * 加载当前页面内容到编辑画布
      */
     private void loadCurrentPageToEditCanvas() {
         if (editCanvas == null) {
             System.out.println("编辑画布未创建，无法加载页面内容");
             return;
         }

        // 确保项目数据已加载
        System.out.println("检查项目数据加载状态");
        if (pageListModel == null || pageListModel.getSize() == 0 || pageContents.isEmpty()) {
            System.out.println("页面列表为空或页面内容为空，重新加载界面");
            System.out.println("pageListModel状态: " + (pageListModel != null ? pageListModel.getSize() + "个页面" : "null"));
            System.out.println("pageContents状态: " + pageContents.size() + "个页面内容");
            loadInterface();
        }

        System.out.println("页面列表状态: " + (pageListModel != null && pageListModel.getSize() > 0 ?
                          "有" + pageListModel.getSize() + "个页面" : "无页面"));

         // 清空编辑画布
         editCanvas.removeAll();

         // 确定当前页面
         String currentPage = null;
         if (pageListModel.getSize() > 0) {
             // 如果有选中的页面，使用选中的页面
             if (pageList != null && pageList.getSelectedIndex() >= 0) {
                 currentPage = pageListModel.getElementAt(pageList.getSelectedIndex());
             } else {
                 // 否则使用第一个页面
                 currentPage = pageListModel.getElementAt(0);
                 if (pageList != null) {
                     pageList.setSelectedIndex(0);
                 }
             }
         }

         if (currentPage != null) {
             // 首先加载页面背景（如果有）
             loadPageBackgroundInEditCanvas(currentPage);

             // 然后加载页面内容
             java.util.List<Component> pageComponents = pageContents.get(currentPage);
             if (pageComponents != null && !pageComponents.isEmpty()) {
                 for (Component comp : pageComponents) {
                     if (comp instanceof JPanel) {
                         JPanel instance = (JPanel) comp;

                         // 获取相对位置数据并重新计算坐标
                         com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                             (com.feixiang.tabletcontrol.model.RelativePosition) instance.getClientProperty("relativePosition");

                         if (relativePos != null && editCanvas != null) {
                             // 获取当前画布的实际显示尺寸
                             int canvasWidth = editCanvas.getWidth();
                             int canvasHeight = editCanvas.getHeight();

                             if (canvasWidth > 0 && canvasHeight > 0) {
                                 // 基于画布尺寸重新计算坐标
                                 com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition newPos =
                                     relativePos.toAbsolute(canvasWidth, canvasHeight);

                                 instance.setBounds(newPos.x, newPos.y, newPos.width, newPos.height);

                                 // 重要修复：重新加载实例样式，确保图标和字体正确显示
                                 updateInstanceComponentsForResize(instance, newPos.width, newPos.height);

                                 System.out.println("项目加载时重新计算坐标: " + relativePos +
                                                  " -> 画布(" + canvasWidth + "x" + canvasHeight +
                                                  ") -> 坐标(" + newPos.x + "," + newPos.y +
                                                  "," + newPos.width + "," + newPos.height + ")");
                             }
                         }

                         // 为编辑模式添加事件监听器
                         addInstanceMouseHandlers(instance);

                         // 添加到编辑画布
                         addInstanceToEditCanvas(instance);

                         System.out.println("加载实例到编辑画布: " + instance.getBounds());
                     }
                 }

                 // 更新当前页面名称
                 currentPageName = currentPage;
                 updateCurrentPageLabel(currentPage);

                 System.out.println("已加载页面到编辑画布: " + currentPage + ", 组件数量: " + pageComponents.size());
             } else {
                 System.out.println("页面无内容: " + currentPage);
             }
         } else {
             System.out.println("没有页面可加载");
         }

         // 刷新编辑画布
         editCanvas.revalidate();
         editCanvas.repaint();
     }

     /**
      * 在编辑画布中加载页面背景
      */
     private void loadPageBackgroundInEditCanvas(String pageName) {
         if (editCanvas == null || pageName == null) {
             return;
         }

         String backgroundPath = pageBackgrounds.get(pageName);
         if (backgroundPath != null && !backgroundPath.isEmpty()) {
             try {
                 File backgroundFile = new File(backgroundPath);
                 if (backgroundFile.exists()) {
                     System.out.println("在编辑画布加载页面背景: " + pageName + " -> " + backgroundPath);

                     // 读取背景图片
                     BufferedImage originalImage = ImageIO.read(backgroundFile);
                     if (originalImage != null) {
                         // 获取编辑画布尺寸（优先使用实际尺寸，其次使用首选尺寸）
                         int canvasWidth = editCanvas.getWidth();
                         int canvasHeight = editCanvas.getHeight();
                         if (canvasWidth <= 0 || canvasHeight <= 0) {
                             java.awt.Dimension preferred = editCanvas.getPreferredSize();
                             if (preferred != null && preferred.width > 0 && preferred.height > 0) {
                                 canvasWidth = preferred.width;
                                 canvasHeight = preferred.height;
                             }
                         }
                         // 仍未获得有效尺寸则回退到保守默认值
                         if (canvasWidth <= 0 || canvasHeight <= 0) {
                             canvasWidth = 600;
                             canvasHeight = 400;
                         }

                         // 缩放图片到画布大小
                         Image scaledImage = originalImage.getScaledInstance(canvasWidth, canvasHeight, Image.SCALE_SMOOTH);
                         ImageIcon backgroundIcon = new ImageIcon(scaledImage);

                         // 创建背景标签
                         JLabel backgroundLabel = new JLabel(backgroundIcon);
                         backgroundLabel.setBounds(0, 0, canvasWidth, canvasHeight);
                         backgroundLabel.setOpaque(false);
                         backgroundLabel.setHorizontalAlignment(SwingConstants.LEFT);
                         backgroundLabel.setVerticalAlignment(SwingConstants.TOP);
                         backgroundLabel.setIconTextGap(0);
                         backgroundLabel.putClientProperty("isBackground", true);
                         backgroundLabel.putClientProperty("backgroundFile", backgroundPath);

                         // 为背景标签添加右键菜单支持
                         addBackgroundLabelContextMenu(backgroundLabel);

                         // 先移除旧背景，避免多个背景叠加
                         removeAllBackgroundLabels();
                         // 添加背景到编辑画布
                         editCanvas.add(backgroundLabel);

                         // 确保背景在最底层
                         editCanvas.setComponentZOrder(backgroundLabel, editCanvas.getComponentCount() - 1);

                         System.out.println("编辑画布页面背景加载成功: " + pageName);
                     } else {
                         System.out.println("无法读取编辑画布背景图片: " + backgroundPath);
                     }
                 } else {
                     System.out.println("编辑画布背景文件不存在: " + backgroundPath);
                 }
             } catch (Exception e) {
                 System.out.println("在编辑画布加载页面背景失败: " + e.getMessage());
                 e.printStackTrace();
             }
         } else {
             System.out.println("页面 " + pageName + " 没有设置背景");
         }
     }

     /**
      * 添加实例到编辑画布，确保在背景之上
      */
     private void addInstanceToEditCanvas(JPanel instance) {
         if (editCanvas == null || instance == null) {
             return;
         }

         // 检查是否有背景
         boolean hasBackground = false;
         Component[] components = editCanvas.getComponents();
         for (Component comp : components) {
             if (comp instanceof JLabel) {
                 JLabel label = (JLabel) comp;
                 Boolean isBackground = (Boolean) label.getClientProperty("isBackground");
                 if (isBackground != null && isBackground) {
                     hasBackground = true;
                     break;
                 }
             }
         }

         // 添加实例到画布
         editCanvas.add(instance);

         // 重新整理组件层级，确保背景在底层，实例在上层
         reorganizeComponentLayers();

         System.out.println("实例已添加到画布" + (hasBackground ? "（有背景）" : "（无背景）"));
     }

     /**
      * 重新整理画布中所有组件的层级
      */
     private void reorganizeComponentLayers() {
         if (editCanvas == null) {
             return;
         }

         Component[] components = editCanvas.getComponents();
         JLabel backgroundComponent = null;
         java.util.List<JPanel> instanceComponents = new java.util.ArrayList<>();

         // 分类组件
         for (Component comp : components) {
             if (comp instanceof JLabel) {
                 JLabel label = (JLabel) comp;
                 Boolean isBackground = (Boolean) label.getClientProperty("isBackground");
                 if (isBackground != null && isBackground) {
                     backgroundComponent = label;
                 }
             } else if (comp instanceof JPanel) {
                 instanceComponents.add((JPanel) comp);
             }
         }

         // 重新设置层级：在Swing中，索引0是最前面（顶层），最大索引是最后面（底层）

         // 首先将所有实例设置在前面（顶层）
         for (int i = 0; i < instanceComponents.size(); i++) {
             editCanvas.setComponentZOrder(instanceComponents.get(i), i);
             System.out.println("实例 " + i + " 已设置为层级 " + i);
         }

         // 然后将背景设置在最底层（最大索引）
         if (backgroundComponent != null) {
             editCanvas.setComponentZOrder(backgroundComponent, editCanvas.getComponentCount() - 1);
             System.out.println("背景已设置为最底层，索引: " + (editCanvas.getComponentCount() - 1));
         }

         System.out.println("组件层级已重新整理: 背景=" + (backgroundComponent != null ? "存在" : "不存在") +
                          ", 实例数量=" + instanceComponents.size());
     }

    /**
     * 在布局完成后将背景图尺寸同步为当前画布尺寸
     */
    private void refreshBackgroundToCanvasSize() {
        if (editCanvas == null) return;

        int canvasWidth = editCanvas.getWidth();
        int canvasHeight = editCanvas.getHeight();
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            Dimension preferred = editCanvas.getPreferredSize();
            if (preferred != null && preferred.width > 0 && preferred.height > 0) {
                canvasWidth = preferred.width;
                canvasHeight = preferred.height;
            }
        }
        if (canvasWidth <= 0 || canvasHeight <= 0) return;

        int bgCount = 0;
        Component[] comps = editCanvas.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Boolean isBackground = (Boolean) label.getClientProperty("isBackground");
                if (isBackground != null && isBackground) {
                    bgCount++;
                    try {
                        String backgroundPath = (String) label.getClientProperty("backgroundFile");
                        Image newImg = null;
                        if (backgroundPath != null) {
                            File f = new File(backgroundPath);
                            if (f.exists()) {
                                BufferedImage img = ImageIO.read(f);
                                if (img != null) {
                                    newImg = createHighQualityScaledImage(img, canvasWidth, canvasHeight);
                                }
                            }
                        }
                        if (newImg == null && label.getIcon() instanceof ImageIcon) {
                            newImg = createHighQualityScaledImage(((ImageIcon) label.getIcon()).getImage(), canvasWidth, canvasHeight);
                        }
                        if (newImg != null) {
                            label.setIcon(new ImageIcon(newImg));
                        }
                        label.setBounds(0, 0, canvasWidth, canvasHeight);
                        label.revalidate();
                        label.repaint();
                        System.out.println("背景同步: " + canvasWidth + "x" + canvasHeight +
                                           ", icon=" + (label.getIcon() instanceof ImageIcon ?
                                                ((ImageIcon) label.getIcon()).getIconWidth() + "x" + ((ImageIcon) label.getIcon()).getIconHeight() : "n/a"));
                    } catch (Exception ex) {
                        System.out.println("同步背景尺寸失败: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    // 不break，确保清理异常状态下的多个背景
                }
            }
        }
        System.out.println("刷新背景完成，检测到背景数量: " + bgCount);
        reorganizeComponentLayers();
        editCanvas.revalidate();
        editCanvas.repaint();
    }


    /**
     * 移除编辑画布中所有背景标签，确保只保留一个背景
     */
    private void removeAllBackgroundLabels() {
        if (editCanvas == null) return;
        java.util.List<Component> toRemove = new java.util.ArrayList<>();
        for (Component comp : editCanvas.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Boolean isBackground = (Boolean) label.getClientProperty("isBackground");
                if (isBackground != null && isBackground) {
                    toRemove.add(label);
                }
            }
        }
        for (Component c : toRemove) {
            editCanvas.remove(c);
        }
        if (!toRemove.isEmpty()) {
            System.out.println("已移除旧背景数量: " + toRemove.size());
        }
    }

    /**
     * 给当前 editCanvas 绑定大小变化监听，变化后自动同步背景尺寸
     */
    private void attachBackgroundAutoFitListener() {
        if (editCanvas == null) return;
        Object attached = editCanvas.getClientProperty("bgAutoFitAttached");
        if (attached instanceof Boolean && (Boolean) attached) {
            return; // 已绑定
        }
        editCanvas.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // 在下一次事件循环中刷新，避免读取到中间尺寸
                SwingUtilities.invokeLater(() -> refreshBackgroundToCanvasSize());
            }
        });
        editCanvas.putClientProperty("bgAutoFitAttached", true);
        System.out.println("已绑定背景自适应监听器");
    }



     /**
      * 重新整理主界面中所有组件的层级
      */
     private void reorganizeMainInterfaceLayers() {
         Container contentPane = getContentPane();
         if (contentPane == null) {
             return;
         }

         Component[] components = contentPane.getComponents();
         JLabel backgroundComponent = null;
         java.util.List<JPanel> pageComponents = new java.util.ArrayList<>();
         JPanel gearButtonComponent = null;

         // 分类组件
         for (Component comp : components) {
             if (comp instanceof JLabel) {
                 JLabel label = (JLabel) comp;
                 Boolean isMainBackground = (Boolean) label.getClientProperty("isMainBackground");
                 if (isMainBackground != null && isMainBackground) {
                     backgroundComponent = label;
                 }
             } else if (comp instanceof JPanel) {
                 if (comp == gearButton) {
                     gearButtonComponent = (JPanel) comp;
                 } else {
                     pageComponents.add((JPanel) comp);
                 }
             }
         }

         // 重新设置层级：背景（底层）→ 页面组件 → 齿轮按钮（顶层）

         // 1. 背景在最底层
         if (backgroundComponent != null) {
             contentPane.setComponentZOrder(backgroundComponent, contentPane.getComponentCount() - 1);
             System.out.println("主界面背景已设置为最底层");
         }

         // 2. 页面组件在中间层
         for (int i = 0; i < pageComponents.size(); i++) {
             contentPane.setComponentZOrder(pageComponents.get(i), i + 1);
             System.out.println("页面组件 " + i + " 已设置为层级 " + (i + 1));
         }

         // 3. 齿轮按钮在最顶层
         if (gearButtonComponent != null) {
             contentPane.setComponentZOrder(gearButtonComponent, 0);
             System.out.println("齿轮按钮已设置为最顶层");
         }

         System.out.println("主界面组件层级已重新整理: 背景=" + (backgroundComponent != null ? "存在" : "不存在") +
                          ", 页面组件数量=" + pageComponents.size() +
                          ", 齿轮按钮=" + (gearButtonComponent != null ? "存在" : "不存在"));
     }

     /**
      * 退出编辑模式，回到主窗口
      */
     private void exitEditMode() {
         if (!isEditMode) {
             return; // 已经在主界面，无需切换
         }

         System.out.println("开始退出编辑模式");



         isEditMode = false;

         // 隐藏并销毁编辑窗口
         if (editWindow != null) {
             editWindow.setVisible(false);
             editWindow.dispose();
             editWindow = null;
         }

         // 显示主窗口
         setVisible(true);
         toFront();
         requestFocus();

         // 重新加载界面，确保主界面显示最新保存的内容
         try {
             loadInterface();
             System.out.println("界面已重新加载");
         } catch (Exception e) {
             System.out.println("重新加载界面时出错: " + e.getMessage());
             // 如果加载失败，使用默认的主界面设置
             setupMainInterface();
         }

         System.out.println("退出编辑模式完成");
     }



     /**
      * 设置主界面窗口属性（不处理页面内容显示）
      */
     private void setupMainInterfaceWindow() {
        System.out.println("开始设置主界面窗口属性");

        // 确保窗口是全屏无边框
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // 只有在窗口还未显示时才设置undecorated
        if (!isDisplayable()) {
            setUndecorated(true);
        }
        setResizable(false);

        // 获取屏幕尺寸并确保窗口完全覆盖屏幕
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);

        System.out.println("主界面窗口属性设置: 屏幕尺寸=" + screenSize.width + "x" + screenSize.height +
                         ", 窗口大小=" + getWidth() + "x" + getHeight() +
                         ", 窗口位置=" + getX() + "," + getY());

         // 完全清空主窗口内容，移除原来的mainContentPanel
         getContentPane().removeAll();

         // 重新设置内容面板为简单的JPanel，不绘制"平板中控系统"文字
         JPanel newContentPanel = new JPanel();
         newContentPanel.setLayout(null); // 使用绝对布局
         newContentPanel.setBackground(new Color(240, 242, 247));

         // 设置内容面板大小为屏幕大小
         newContentPanel.setSize(screenSize);
         newContentPanel.setPreferredSize(screenSize);

         // 设置新的内容面板
         setContentPane(newContentPanel);

         // 确保齿轮按钮存在，如果不存在则创建
         if (gearButton == null) {
             updateGearButtonStyle();
         }

         // 添加齿轮按钮到主界面
         addGearButtonToMain();

         // 检查是否有页面内容需要显示
         int pageCount = (pageListModel != null) ? pageListModel.getSize() : 0;
         System.out.println("setupMainInterfaceWindow - 页面数量: " + pageCount);
         System.out.println("setupMainInterfaceWindow - 当前页面: " + currentPageName);
         System.out.println("setupMainInterfaceWindow - 页面内容数量: " + pageContents.size());

         if (pageCount > 0) {
             // 有页面时显示当前页面内容（如果有的话），否则显示第一个页面
             if (currentPageName != null && pageContents.containsKey(currentPageName)) {
                 System.out.println("显示当前页面: " + currentPageName);
                 displayPageInMain(currentPageName);
             } else {
                 System.out.println("显示第一个页面");
                 displayFirstPageInMain();
             }
         } else {
             // 没有页面时显示提示信息
             System.out.println("显示无页面提示");
             displayNoPageMessage();
         }

         System.out.println("主界面窗口属性设置完成");
     }

     /**
      * 设置主界面（使用界面）
      */
     private void setupMainInterface() {
        System.out.println("开始设置主界面");

        // 确保窗口是全屏无边框
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // 只有在窗口还未显示时才设置undecorated
        if (!isDisplayable()) {
            setUndecorated(true);
        }
        setResizable(false);

        // 获取屏幕尺寸并确保窗口完全覆盖屏幕
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);

        System.out.println("主界面设置: 屏幕尺寸=" + screenSize.width + "x" + screenSize.height +
                         ", 窗口大小=" + getWidth() + "x" + getHeight() +
                         ", 窗口位置=" + getX() + "," + getY());

         // 完全清空主窗口内容，移除原来的mainContentPanel
         //getContentPane().removeAll();

         // 重新设置内容面板为简单的JPanel，不绘制"平板中控系统"文字
         JPanel newContentPanel = new JPanel();
         newContentPanel.setLayout(null); // 使用绝对布局
         newContentPanel.setBackground(new Color(240, 242, 247));

         // 设置内容面板大小为屏幕大小
         newContentPanel.setSize(screenSize);
         newContentPanel.setPreferredSize(screenSize);

         // 设置新的内容面板
         setContentPane(newContentPanel);

         // 检查是否有页面内容
         int pageCount = (pageListModel != null) ? pageListModel.getSize() : 0;
         System.out.println("setupMainInterface - 页面数量: " + pageCount);
         System.out.println("setupMainInterface - 当前页面: " + currentPageName);
         System.out.println("setupMainInterface - 页面内容数量: " + pageContents.size());

         if (pageCount > 0) {
             // 有页面时显示当前页面内容（如果有的话），否则显示第一个页面
             if (currentPageName != null && pageContents.containsKey(currentPageName)) {
                 System.out.println("显示当前页面: " + currentPageName);
                 displayPageInMain(currentPageName);
             } else {
                 System.out.println("显示第一个页面");
                 displayFirstPageInMain();
             }
         } else {
             // 没有页面时显示提示信息
             System.out.println("显示无页面提示");
             displayNoPageMessage();
         }

         // 创建并添加齿轮按钮（确保在最高层级）
         updateGearButtonStyle();
         addGearButtonToMain();

         // 刷新界面
         revalidate();
         repaint();

         System.out.println("主界面设置完成后，齿轮按钮状态: " + (gearButton != null ? "存在" : "不存在"));
         if (gearButton != null) {
             System.out.println("齿轮按钮位置: " + gearButton.getBounds());
             System.out.println("齿轮按钮可见: " + gearButton.isVisible());
         }

         System.out.println("主界面设置完成，屏幕尺寸: " + screenSize);
     }

     /**
      * 在主界面中显示第一个页面
      */
     private void displayFirstPageInMain() {
         System.out.println("开始显示第一个页面，页面数量: " + pageListModel.getSize());

         if (pageListModel.getSize() > 0) {
             String firstPageName = pageListModel.getElementAt(0);

             // 直接调用displayPageInMain方法，避免代码重复
             displayPageInMain(firstPageName);
         } else {
             System.out.println("没有页面可显示");
         }
     }

     /**
      * 移除编辑模式的事件监听器
      */
     private void removeEditModeListeners(JPanel instance) {
         // 移除所有鼠标监听器（编辑模式的拖拽、选择等）
         java.awt.event.MouseListener[] mouseListeners = instance.getMouseListeners();
         for (java.awt.event.MouseListener listener : mouseListeners) {
             instance.removeMouseListener(listener);
         }

         java.awt.event.MouseMotionListener[] motionListeners = instance.getMouseMotionListeners();
         for (java.awt.event.MouseMotionListener listener : motionListeners) {
             instance.removeMouseMotionListener(listener);
         }

         // 移除边框（编辑模式的选中效果）
         instance.setBorder(null);

         // 添加主界面的功能执行监听器
         addMainInterfaceListeners(instance);
     }

     /**
      * 添加主界面的功能执行监听器
      */
     private void addMainInterfaceListeners(JPanel instance) {
         instance.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 executeInstanceFunction(instance);
             }
         });
     }

     /**
      * 执行实例功能
      */
     private void executeInstanceFunction(JPanel instance) {
         // 首先检查是否为开关按钮
         String iconPath = getInstanceIconPath(instance);
         if (isSwitchButton(iconPath)) {
             executeSwitchButton(instance);
             return;
         }

         String functionType = (String) instance.getClientProperty("functionType");
         if (functionType == null) {
             functionType = "纯文本";
         }

         System.out.println("执行实例功能: " + functionType);

         switch (functionType) {
             case "页面跳转":
                 executePageJump(instance);
                 break;
             case "发送指令":
                 executeCommand(instance);
                 break;
             case "实例组":
                 executeInstanceGroup(instance);
                 break;
             case "纯文本":
             default:
                 System.out.println("纯文本实例，无功能执行");
                 break;
         }
     }

     /**
      * 获取实例的图标路径
      */
     private String getInstanceIconPath(JPanel instance) {
         if (instance == null) {
             System.out.println("调试：实例为null");
             return null;
         }

         // 从实例的标签数据中获取图标路径
         Object labelData = instance.getClientProperty("labelData");
         System.out.println("调试：labelData类型: " + (labelData != null ? labelData.getClass().getName() : "null"));

         if (labelData instanceof java.util.Map) {
             @SuppressWarnings("unchecked")
             java.util.Map<String, Object> labelMap = (java.util.Map<String, Object>) labelData;
             System.out.println("调试：labelMap内容: " + labelMap);
             String iconPath = (String) labelMap.get("iconPath");
             System.out.println("调试：从labelMap获取的iconPath: " + iconPath);
             return iconPath;
         } else {
             System.out.println("调试：labelData不是Map类型或为null");
         }

         return null;
     }

     /**
      * 执行开关按钮
      */
     private void executeSwitchButton(JPanel instance) {
         System.out.println("执行开关按钮");

         // 获取开关按钮数据
         SwitchButtonData switchData = getSwitchButtonData(instance);
         if (switchData == null) {
             System.out.println("✗ 开关按钮数据获取失败");
             return;
         }

         System.out.println("当前状态: " + (switchData.getCurrentState() ? "开" : "关"));

         // 获取要发送的指令（切换到下一个状态的指令）
         String commandToSend = switchData.getNextCommand();
         if (commandToSend == null || commandToSend.isEmpty()) {
             System.out.println("✗ 开关按钮指令为空");
             return;
         }

         System.out.println("发送指令: " + commandToSend);

         // 立即切换状态和更新UI（提供即时反馈）
         switchData.setCurrentState(!switchData.getCurrentState());
         updateSwitchButtonUI(instance, switchData);
         instance.putClientProperty("switchButtonData", switchData);

         System.out.println("✓ 开关按钮状态已立即切换为: " + (switchData.getCurrentState() ? "开" : "关"));

         // 在后台异步发送网络指令
         final String finalCommand = commandToSend;
         final SwitchButtonData finalSwitchData = switchData;
         final JPanel finalInstance = instance;

         new Thread(() -> {
             try {
                 System.out.println("→ 后台发送网络指令: " + finalCommand);
                 boolean success = sendNetworkCommand(finalCommand);

                 if (success) {
                     System.out.println("✓ 网络指令发送成功");
                 } else {
                     System.out.println("✗ 网络指令发送失败，但UI状态已更新");
                     // 注意：即使网络发送失败，我们也不回滚UI状态
                     // 这样可以避免UI闪烁，用户体验更好
                 }
             } catch (Exception e) {
                 System.out.println("✗ 网络指令发送异常: " + e.getMessage());
             }
         }).start();
     }

     /**
      * 获取开关按钮数据
      */
     private SwitchButtonData getSwitchButtonData(JPanel instance) {
         // 首先尝试从实例属性中获取
         Object existingData = instance.getClientProperty("switchButtonData");
         if (existingData instanceof SwitchButtonData) {
             return (SwitchButtonData) existingData;
         }

         // 如果没有现有数据，创建新的开关按钮数据
         String iconPath = getInstanceIconPath(instance);
         String buttonFolder = getSwitchButtonFolder(iconPath);
         if (buttonFolder == null) {
             return null;
         }

         // 创建开关按钮数据
         SwitchButtonData switchData = new SwitchButtonData(
             "switch_" + System.currentTimeMillis(),
             "开关按钮"
         );

         // 设置图片路径
         switchData.setOffImagePath(buttonFolder + "/btn1.png");
         switchData.setOnImagePath(buttonFolder + "/btn2.png");

         // 从commands属性中加载指令数据
         Object commands = instance.getClientProperty("commands");
         if (commands instanceof java.util.List) {
             @SuppressWarnings("unchecked")
             java.util.List<String> commandList = (java.util.List<String>) commands;

             // 如果有两条指令，根据指令名称正确分配
             if (commandList.size() >= 2) {
                 // 根据指令名称判断哪个是开启指令，哪个是关闭指令
                 String firstCommand = commandList.get(0);
                 String secondCommand = commandList.get(1);

                 // 检查第一条指令是否包含"开启"关键字
                 if (firstCommand.contains("开启") || firstCommand.contains("开")) {
                     switchData.setOnCommand(firstCommand);   // 开启指令 → 开状态指令
                     switchData.setOffCommand(secondCommand); // 关闭指令 → 关状态指令
                 } else {
                     switchData.setOffCommand(firstCommand);  // 关闭指令 → 关状态指令
                     switchData.setOnCommand(secondCommand);  // 开启指令 → 开状态指令
                 }

                 System.out.println("从commands加载指令:");
                 System.out.println("关状态指令(offCommand): " + switchData.getOffCommand());
                 System.out.println("开状态指令(onCommand): " + switchData.getOnCommand());
             } else {
                 // 设置默认指令
                 switchData.setOffCommand("关状态指令|TCP|127.0.0.1|8080|00");
                 switchData.setOnCommand("开状态指令|TCP|127.0.0.1|8080|01");
                 System.out.println("使用默认指令配置");
             }
         } else {
             // 设置默认指令
             switchData.setOffCommand("关状态指令|TCP|127.0.0.1|8080|00");
             switchData.setOnCommand("开状态指令|TCP|127.0.0.1|8080|01");
             System.out.println("commands属性为空，使用默认指令配置");
         }

         return switchData;
     }

     /**
      * 更新开关按钮UI
      */
     private void updateSwitchButtonUI(JPanel instance, SwitchButtonData switchData) {
         try {
             // 获取当前应该显示的图片路径
             String currentImagePath = switchData.getCurrentImagePath();
             System.out.println("调试：当前状态=" + switchData.getCurrentState() + ", 应显示图片=" + currentImagePath);

             // 查找实例中的JLabel组件
             Component[] components = instance.getComponents();
             System.out.println("调试：实例中组件数量=" + components.length);
             for (Component comp : components) {
                 if (comp instanceof JLabel) {
                     JLabel label = (JLabel) comp;
                     System.out.println("调试：找到JLabel组件，当前图标=" + (label.getIcon() != null ? "有图标" : "无图标"));

                     // 尝试加载新的图片
                     try {
                         File imageFile = new File(currentImagePath);
                         if (imageFile.exists()) {
                             ImageIcon icon = new ImageIcon(currentImagePath);

                             // 保持原有尺寸
                             int width = label.getWidth();
                             int height = label.getHeight();
                             if (width > 0 && height > 0) {
                                 Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                                 label.setIcon(new ImageIcon(scaledImage));
                             } else {
                                 label.setIcon(icon);
                             }

                             System.out.println("✓ 开关按钮图片已更新: " + currentImagePath);
                         } else {
                             // 图片文件不存在，使用默认样式
                             label.setIcon(null);
                             label.setText(switchData.getCurrentState() ? "开" : "关");
                             label.setBackground(switchData.getCurrentState() ? Color.GREEN : Color.GRAY);
                             label.setOpaque(true);
                             System.out.println("⚠ 图片文件不存在，使用默认样式: " + currentImagePath);
                         }
                     } catch (Exception e) {
                         // 图片加载失败，使用默认样式
                         label.setIcon(null);
                         label.setText(switchData.getCurrentState() ? "开" : "关");
                         label.setBackground(switchData.getCurrentState() ? Color.GREEN : Color.GRAY);
                         label.setOpaque(true);
                         System.out.println("⚠ 图片加载失败，使用默认样式: " + e.getMessage());
                     }

                     break; // 只更新第一个JLabel
                 }
             }

             // 刷新实例显示
             instance.revalidate();
             instance.repaint();

         } catch (Exception e) {
             System.out.println("✗ 更新开关按钮UI失败: " + e.getMessage());
         }
     }

     /**
      * 执行页面跳转
      */
     private void executePageJump(JPanel instance) {
         String targetPage = (String) instance.getClientProperty("targetPage");
         if (targetPage != null && !targetPage.equals("无可用页面")) {
             System.out.println("跳转到页面: " + targetPage);
             displayPageInMain(targetPage);
         } else {
             System.out.println("无效的目标页面: " + targetPage);
         }
     }

     /**
      * 在主界面中显示指定页面
      */
     private void displayPageInMain(String pageName) {
         System.out.println("=== displayPageInMain 开始 ===");
         System.out.println("切换主界面页面到: " + pageName);
         System.out.println("pageContents总数: " + pageContents.size());
         System.out.println("pageContents包含的页面: " + pageContents.keySet());

         // 清除当前页面内容（保留齿轮按钮）
         Component[] components = getContentPane().getComponents();
         System.out.println("当前内容面板组件数量: " + components.length);
         for (Component comp : components) {
             if (comp != gearButton) {
                 System.out.println("移除组件: " + comp.getClass().getSimpleName());
                 getContentPane().remove(comp);
             } else {
                 System.out.println("保留齿轮按钮: " + comp.getClass().getSimpleName());
             }
         }

         // 首先加载并显示页面背景（如果有）
         displayPageBackgroundInMain(pageName);

         // 显示新页面内容
         java.util.List<Component> pageComponents = pageContents.get(pageName);
         System.out.println("获取到的页面组件: " + (pageComponents != null ? pageComponents.size() : "null"));

         if (pageComponents != null) {
             System.out.println("开始添加页面组件...");

             // 获取屏幕尺寸和编辑界面分辨率
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

             // 解析编辑分辨率
             int editWidth = 1024;  // 默认宽度
             int editHeight = 768;  // 默认高度
             if (currentEditResolution != null) {
                 String[] parts = currentEditResolution.split("x");
                 if (parts.length == 2) {
                     try {
                         editWidth = Integer.parseInt(parts[0]);
                         editHeight = Integer.parseInt(parts[1]);
                     } catch (NumberFormatException e) {
                         System.out.println("解析编辑分辨率失败，使用默认值: " + e.getMessage());
                     }
                 }
             }

             System.out.println("=== 主界面显示计算 ===");
             System.out.println("屏幕尺寸: " + screenSize.width + "x" + screenSize.height);
             System.out.println("编辑分辨率: " + editWidth + "x" + editHeight);
             System.out.println("使用百分比相对定位，直接基于屏幕尺寸计算");

            // 计算页面级的基准画布尺寸（所有实例共用），避免每个实例用不同保存值导致偏移
            Integer pageBaseW = null, pageBaseH = null;
            if (pageComponents != null) {
                for (int i = 0; i < pageComponents.size(); i++) {
                    Component comp0 = pageComponents.get(i);
                    if (comp0 instanceof JPanel) {
                        JPanel p0 = (JPanel) comp0;
                        Integer cw = (Integer) p0.getClientProperty("canvasDisplayWidth");
                        Integer ch = (Integer) p0.getClientProperty("canvasDisplayHeight");
                        if (cw != null && cw > 0 && ch != null && ch > 0) {
                            pageBaseW = cw; pageBaseH = ch; break;
                        }
                    }
                }
            }
            if (pageBaseW == null || pageBaseH == null) {
                // 回退：使用编辑分辨率
                int fallbackW = editWidth;
                int fallbackH = editHeight;
                pageBaseW = fallbackW;
                pageBaseH = fallbackH;
            }
            System.out.println("[主界面] 页面基准画布尺寸: " + pageBaseW + "x" + pageBaseH);


             for (int i = 0; i < pageComponents.size(); i++) {
                 Component comp = pageComponents.get(i);
                 System.out.println("处理组件 " + i + ": " + comp.getClass().getSimpleName() + " (使用百分比相对定位)");
                 if (comp instanceof JPanel) {
                     JPanel panel = (JPanel) comp;

                     int scaledX, scaledY, scaledWidth, scaledHeight;

                     // 优先检查是否有编辑画布坐标数据
                     Integer editCanvasX = (Integer) panel.getClientProperty("editCanvasX");
                     Integer editCanvasY = (Integer) panel.getClientProperty("editCanvasY");
                     Integer editCanvasWidth = (Integer) panel.getClientProperty("editCanvasWidth");
                     Integer editCanvasHeight = (Integer) panel.getClientProperty("editCanvasHeight");
                     String savedEditResolution = (String) panel.getClientProperty("editResolution");
                     Integer savedCanvasDisplayWidth = (Integer) panel.getClientProperty("canvasDisplayWidth");
                     Integer savedCanvasDisplayHeight = (Integer) panel.getClientProperty("canvasDisplayHeight");

                     System.out.println("[主界面] 属性检测: editCanvasX=" + editCanvasX + ", editCanvasY=" + editCanvasY +
                                        ", editCanvasW=" + editCanvasWidth + ", editCanvasH=" + editCanvasHeight +
                                        ", savedEditResolution=" + savedEditResolution +
                                        ", canvasDisplayW=" + savedCanvasDisplayWidth + ", canvasDisplayH=" + savedCanvasDisplayHeight);

                     if (editCanvasX != null && editCanvasY != null && editCanvasWidth != null && editCanvasHeight != null &&
                         savedCanvasDisplayWidth != null && savedCanvasDisplayHeight != null) {

                         // 新格式：基于编辑画布坐标进行同比例转换
                         // 解析保存时的编辑分辨率
                         int savedEditWidth = editWidth, savedEditHeight = editHeight;
                         String[] parts = savedEditResolution.split("x");
                         if (parts.length == 2) {
                             try {
                                 savedEditWidth = Integer.parseInt(parts[0]);
                                 savedEditHeight = Integer.parseInt(parts[1]);
                             } catch (NumberFormatException e) {
                                 System.out.println("解析保存的编辑分辨率失败: " + e.getMessage());
                             }
                         }

                         // 正确的转换方法：基于相对位置转换
                         System.out.println("主界面坐标转换 - 原始编辑画布坐标: (" + editCanvasX + "," + editCanvasY + "," + editCanvasWidth + "," + editCanvasHeight + ")");
                         System.out.println("保存时的编辑分辨率: " + savedEditWidth + "x" + savedEditHeight);

                         // 第一步：基于编辑分辨率计算相对位置，确保宽高比例正确
                         // 先将编辑画布坐标转换为编辑分辨率坐标
                         double canvasToEditScaleX = (double) savedEditWidth / savedCanvasDisplayWidth;
                         double canvasToEditScaleY = (double) savedEditHeight / savedCanvasDisplayHeight;

                         int editResolutionX = (int) Math.round(editCanvasX * canvasToEditScaleX);
                         int editResolutionY = (int) Math.round(editCanvasY * canvasToEditScaleY);
                         int editResolutionWidth = (int) Math.round(editCanvasWidth * canvasToEditScaleX);
                         int editResolutionHeight = (int) Math.round(editCanvasHeight * canvasToEditScaleY);

                         // 基于编辑分辨率计算相对位置
                         double relativeX = savedEditWidth > 0 ? (double) editResolutionX / savedEditWidth : 0.0;
                         double relativeY = savedEditHeight > 0 ? (double) editResolutionY / savedEditHeight : 0.0;
                         double relativeWidth = savedEditWidth > 0 ? (double) editResolutionWidth / savedEditWidth : 0.0;
                         double relativeHeight = savedEditHeight > 0 ? (double) editResolutionHeight / savedEditHeight : 0.0;

                         System.out.println("相对位置(基于编辑分辨率) : X=" + String.format("%.4f", relativeX) +
                                          ", Y=" + String.format("%.4f", relativeY) +
                                          ", W=" + String.format("%.4f", relativeWidth) +
                                          ", H=" + String.format("%.4f", relativeHeight));

                         // 第二步：基于当前屏幕尺寸做等比缩放（保持形状不变），并居中留边
                         int screenW = screenSize.width;
                         int screenH = screenSize.height;
                         // 使用编辑分辨率作为基准，确保宽高比例正确
                         int baseW = savedEditWidth;
                         int baseH = savedEditHeight;
                         double uniformScale = Math.min((double) screenW / baseW, (double) screenH / baseH);
                         int scaledCanvasW = (int) Math.round(baseW * uniformScale);
                         int scaledCanvasH = (int) Math.round(baseH * uniformScale);
                         int offsetX = (screenW - scaledCanvasW) / 2;
                         int offsetY = (screenH - scaledCanvasH) / 2;

                         scaledX = offsetX + (int) Math.round(relativeX * scaledCanvasW);
                         scaledY = offsetY + (int) Math.round(relativeY * scaledCanvasH);
                         scaledWidth = (int) Math.round(relativeWidth * scaledCanvasW);
                         scaledHeight = (int) Math.round(relativeHeight * scaledCanvasH);

                         System.out.println("主界面最终坐标(等比缩放): (" + scaledX + "," + scaledY + "," + scaledWidth + "," + scaledHeight + ")" +
                                            ", uniformScale=" + String.format("%.4f", uniformScale) +
                                            ", offsets=(" + offsetX + "," + offsetY + ")");



                     } else {
                         // 向后兼容：使用相对位置数据
                         com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                             (com.feixiang.tabletcontrol.model.RelativePosition) panel.getClientProperty("relativePosition");

                         if (relativePos != null) {
                             // 先基于编辑分辨率计算绝对位置，然后缩放到屏幕尺寸
                             com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition editPos =
                                 relativePos.toAbsolute(editWidth, editHeight);

                             // 计算等比缩放（屏幕尺寸 / 编辑分辨率），保持形状不变并居中
                             int screenW2 = screenSize.width;
                             int screenH2 = screenSize.height;
                             // 使用页面统一的基准画布尺寸
                             int baseW2 = pageBaseW;
                             int baseH2 = pageBaseH;
                             double uniformScale2 = Math.min((double) screenW2 / baseW2, (double) screenH2 / baseH2);
                             int scaledCanvasW2 = (int) Math.round(baseW2 * uniformScale2);
                             int scaledCanvasH2 = (int) Math.round(baseH2 * uniformScale2);
                             int offsetX2 = (screenW2 - scaledCanvasW2) / 2;
                             int offsetY2 = (screenH2 - scaledCanvasH2) / 2;

                             // 缩放到屏幕尺寸（使用四舍五入提高精度）
                             scaledX = offsetX2 + (int) Math.round(editPos.x * uniformScale2);
                             scaledY = offsetY2 + (int) Math.round(editPos.y * uniformScale2);
                             scaledWidth = (int) Math.round(editPos.width * uniformScale2);
                             scaledHeight = (int) Math.round(editPos.height * uniformScale2);

                             System.out.println("相对位置转换(等比缩放): " + relativePos +
                                 " -> 屏幕(" + scaledX + "," + scaledY + "," + scaledWidth + "," + scaledHeight + ")" +
                                 ", uniformScale=" + String.format("%.4f", uniformScale2) +
                                 ", offsets=(" + offsetX2 + "," + offsetY2 + ")");
                         } else {
                             System.out.println("警告：组件缺少位置数据，跳过显示");
                             continue;
                         }
                     }

                     // 设置计算后的位置和尺寸
                     panel.setBounds(scaledX, scaledY, scaledWidth, scaledHeight);
                     System.out.println("屏幕显示坐标: " + panel.getBounds() + " (基于百分比计算)");

                     // 同步更新子组件（例如内部JLabel的图标与字体）以匹配容器新尺寸
                     updateInstanceComponentsForResize(panel, scaledWidth, scaledHeight);

                     // 确保组件可见
                     panel.setVisible(true);
                     panel.setEnabled(true);

                     // 确保子组件也可见
                     for (Component child : panel.getComponents()) {
                         child.setVisible(true);
                         if (child instanceof JLabel) {
                             child.setEnabled(true);
                         }
                     }

                     removeEditModeListeners(panel);
                     getContentPane().add(panel);
                     System.out.println("已添加JPanel组件 " + i + ", 子组件数量: " + panel.getComponentCount());
                 } else {
                     System.out.println("跳过非JPanel组件: " + comp.getClass().getSimpleName());
                 }
             }
             System.out.println("页面组件已添加，数量: " + pageComponents.size());
         } else {
             System.out.println("页面无内容: " + pageName);
             System.out.println("可用页面列表: " + pageContents.keySet());
         }

         // 刷新界面
         getContentPane().revalidate();
         getContentPane().repaint();

         // 输出最终状态
         // 确保齿轮按钮存在
         addGearButtonToMain();

         // 重新整理所有组件的层级：背景（底层）→ 页面组件 → 齿轮按钮（顶层）
         reorganizeMainInterfaceLayers();

         Component[] finalComponents = getContentPane().getComponents();
         System.out.println("最终内容面板组件数量: " + finalComponents.length);
         for (int i = 0; i < finalComponents.length; i++) {
             Component comp = finalComponents[i];
             System.out.println("最终组件 " + i + ": " + comp.getClass().getSimpleName() + ", bounds: " + comp.getBounds() + ", visible: " + comp.isVisible());
         }

         System.out.println("主界面切换到页面: " + pageName);
         System.out.println("=== displayPageInMain 结束 ===");
     }

     /**
      * 在主界面显示页面背景
      */
     private void displayPageBackgroundInMain(String pageName) {
         System.out.println("=== displayPageBackgroundInMain 开始 ===");
         System.out.println("页面名称: " + pageName);
         System.out.println("pageBackgrounds总数: " + pageBackgrounds.size());
         System.out.println("pageBackgrounds内容: " + pageBackgrounds);

         if (pageName == null) {
             System.out.println("页面名称为null，退出背景显示");
             return;
         }

         String backgroundPath = pageBackgrounds.get(pageName);
         System.out.println("获取到的背景路径: " + backgroundPath);
         if (backgroundPath != null && !backgroundPath.isEmpty()) {
             try {
                 File backgroundFile = new File(backgroundPath);
                 if (backgroundFile.exists()) {
                     System.out.println("在主界面加载页面背景: " + pageName + " -> " + backgroundPath);

                     // 读取背景图片
                     BufferedImage originalImage = ImageIO.read(backgroundFile);
                     if (originalImage != null) {
                         // 获取屏幕尺寸
                         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                         int screenWidth = screenSize.width;
                         int screenHeight = screenSize.height;

                         // 统一与实例相同的等比缩放逻辑：优先使用保存时的画布显示尺寸，其次使用编辑分辨率
                         int baseW = -1, baseH = -1;
                         java.util.List<Component> comps = pageContents.get(pageName);
                         if (comps != null) {
                             for (Component c : comps) {
                                 if (c instanceof JPanel) {
                                     Integer w = (Integer) ((JPanel) c).getClientProperty("canvasDisplayWidth");
                                     Integer h = (Integer) ((JPanel) c).getClientProperty("canvasDisplayHeight");
                                     if (w != null && w > 0 && h != null && h > 0) {
                                         baseW = w; baseH = h; break;
                                     }
                                 }
                             }
                         }
                         if (baseW <= 0 || baseH <= 0) {
                             int editW = 1024, editH = 768;
                             if (currentEditResolution != null) {
                                 String[] parts = currentEditResolution.split("x");
                                 if (parts.length == 2) {
                                     try { editW = Integer.parseInt(parts[0]); editH = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                                 }
                             }
                             baseW = editW; baseH = editH;
                         }

                         double uniformScale = Math.min((double) screenWidth / baseW, (double) screenHeight / baseH);
                         int scaledCanvasW = (int) Math.round(baseW * uniformScale);
                         int scaledCanvasH = (int) Math.round(baseH * uniformScale);
                         int offsetX = (screenWidth - scaledCanvasW) / 2;
                         int offsetY = (screenHeight - scaledCanvasH) / 2;

                         // 高质量缩放背景图到等比画布区域
                         Image scaledImage = createHighQualityScaledImage(originalImage, scaledCanvasW, scaledCanvasH);
                         ImageIcon backgroundIcon = new ImageIcon(scaledImage);

                         // 创建背景标签（放在等比画布区域内，居中）
                         JLabel backgroundLabel = new JLabel(backgroundIcon);
                         backgroundLabel.setBounds(offsetX, offsetY, scaledCanvasW, scaledCanvasH);
                         backgroundLabel.setOpaque(false);
                         backgroundLabel.putClientProperty("isMainBackground", true);

                         // 添加背景到主界面
                         getContentPane().add(backgroundLabel);

                         // 立即刷新界面
                         getContentPane().revalidate();
                         getContentPane().repaint();

                         System.out.println("主界面页面背景显示成功(等比缩放): " + pageName +
                                            ", scaledCanvas=" + scaledCanvasW + "x" + scaledCanvasH +
                                            ", offsets=(" + offsetX + "," + offsetY + ")");
                     } else {
                         System.out.println("无法读取主界面背景图片: " + backgroundPath);
                     }
                 } else {
                     System.out.println("主界面背景文件不存在: " + backgroundPath);
                 }
             } catch (Exception e) {
                 System.out.println("在主界面加载页面背景失败: " + e.getMessage());
                 e.printStackTrace();
             }
         } else {
             System.out.println("页面 " + pageName + " 没有设置背景");
         }
     }

     /**
      * 缩放子组件（主要是JLabel）
      */
     private void scaleChildComponents(JPanel panel, double scaleX, double scaleY) {
         for (Component child : panel.getComponents()) {
             if (child instanceof JLabel) {
                 JLabel label = (JLabel) child;

                 // 缩放子组件的位置和尺寸
                 Rectangle childBounds = child.getBounds();
                 int scaledX = (int) (childBounds.x * scaleX);
                 int scaledY = (int) (childBounds.y * scaleY);
                 int scaledWidth = (int) (childBounds.width * scaleX);
                 int scaledHeight = (int) (childBounds.height * scaleY);
                 child.setBounds(scaledX, scaledY, scaledWidth, scaledHeight);

                 // 缩放字体大小
                 Font currentFont = label.getFont();
                 if (currentFont != null) {
                     // 使用较小的缩放比例来缩放字体，避免字体过大
                     double fontScale = Math.min(scaleX, scaleY);
                     int newFontSize = Math.max(8, (int) (currentFont.getSize() * fontScale));
                     Font scaledFont = new Font(currentFont.getName(), currentFont.getStyle(), newFontSize);
                     label.setFont(scaledFont);
                     System.out.println("字体缩放: " + currentFont.getSize() + " -> " + newFontSize + " (scale=" + fontScale + ")");
                 }

                 // 缩放图标
                 if (label.getIcon() != null && label.getIcon() instanceof ImageIcon) {
                     ImageIcon originalIcon = (ImageIcon) label.getClientProperty("originalIcon");
                     if (originalIcon != null) {
                         try {
                             Image originalImage = originalIcon.getImage();
                             int iconWidth = (int) (originalIcon.getIconWidth() * scaleX);
                             int iconHeight = (int) (originalIcon.getIconHeight() * scaleY);
                             Image scaledImage = originalImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
                             label.setIcon(new ImageIcon(scaledImage));
                             System.out.println("图标缩放: " + originalIcon.getIconWidth() + "x" + originalIcon.getIconHeight() +
                                              " -> " + iconWidth + "x" + iconHeight);
                         } catch (Exception e) {
                             System.out.println("图标缩放失败: " + e.getMessage());
                         }
                     }
                 }
             }
         }
     }

     /**
      * 显示无页面提示信息
      */
     private void displayNoPageMessage() {
         // 获取屏幕尺寸
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

         JLabel messageLabel = new JLabel("暂无页面内容");
         messageLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
         messageLabel.setForeground(new Color(108, 117, 125));
         messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
         messageLabel.setBounds(0, 0, screenSize.width, screenSize.height);
         getContentPane().add(messageLabel);
         System.out.println("显示无页面提示信息，屏幕尺寸: " + screenSize);
     }



     /**
      * 执行发送指令
      */
     private void executeCommand(JPanel instance) {
         Object commands = instance.getClientProperty("commands");
         if (commands instanceof java.util.List) {
             @SuppressWarnings("unchecked")
             java.util.List<String> commandList = (java.util.List<String>) commands;
             System.out.println("=== 开始执行指令列表，指令数量: " + commandList.size() + " ===");

             int successCount = 0;
             int failCount = 0;

             for (int i = 0; i < commandList.size(); i++) {
                 String cmd = commandList.get(i);
                 System.out.println("执行第 " + (i + 1) + " 条指令: " + cmd);

                 // 处理每条指令的延时（包括第一条指令）
                 double delay = getCommandDelay(cmd);
                 if (delay > 0) {
                     System.out.println("执行前延时: " + delay + " 秒");
                     try {
                         Thread.sleep((long) (delay * 1000));
                     } catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                         break;
                     }
                 } else if (i > 0) {
                     // 非第一条指令且没有设置延时时，默认间隔100ms
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                         break;
                     }
                 }

                 try {
                     boolean success = sendNetworkCommand(cmd);
                     if (success) {
                         successCount++;
                         System.out.println("✓ 第 " + (i + 1) + " 条指令执行成功");
                     } else {
                         failCount++;
                         System.out.println("✗ 第 " + (i + 1) + " 条指令执行失败");
                     }
                 } catch (Exception e) {
                     failCount++;
                     System.out.println("✗ 第 " + (i + 1) + " 条指令执行异常: " + e.getMessage());
                 }
             }

             System.out.println("=== 指令执行完成，成功: " + successCount + " 条，失败: " + failCount + " 条 ===");

            // 所有指令发送完成后，统一等待3秒
            if (commandList.size() > 0) {
                System.out.println("所有指令发送完成，等待3秒...");
                try {
                    Thread.sleep(3000);
                    System.out.println("等待完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("等待被中断");
                }
            }
         } else {
             System.out.println("无指令可执行");
         }
     }

     /**
      * 发送网络指令
      * @param commandData 指令数据，格式：指令名称|协议类型|IP地址|端口|指令内容|延时（可选）
      * @return 是否发送成功
      */
     private boolean sendNetworkCommand(String commandData) {
         try {
             // 解析指令数据，支持5字段（旧格式）和6字段（新格式）
             String[] parts = commandData.split("\\|");
             if (parts.length < 5 || parts.length > 6) {
                 System.out.println("✗ 指令格式错误，应为5-6个部分，实际: " + parts.length + " 部分");
                 return false;
             }

             String name = parts[0].trim();
             String protocol = parts[1].trim().toUpperCase();
             String ip = parts[2].trim();
             String portStr = parts[3].trim();
             String hexCommand = parts[4].trim();

             // 验证协议类型
             if (!"TCP".equals(protocol) && !"UDP".equals(protocol)) {
                 System.out.println("✗ 不支持的协议类型: " + protocol + "，仅支持TCP/UDP");
                 return false;
             }

             // 验证IP地址格式
             if (!isValidIPAddress(ip)) {
                 System.out.println("✗ IP地址格式错误: " + ip);
                 return false;
             }

             // 验证端口
             int port;
             try {
                 port = Integer.parseInt(portStr);
                 if (port < 0 || port > 65535) {
                     System.out.println("✗ 端口超出范围 (0-65535): " + port);
                     return false;
                 }
             } catch (NumberFormatException e) {
                 System.out.println("✗ 端口格式错误: " + portStr);
                 return false;
             }

             // 转换十六进制指令为字节数组
             byte[] commandBytes = hexStringToBytes(hexCommand);
             if (commandBytes == null) {
                 System.out.println("✗ 十六进制指令格式错误: " + hexCommand);
                 return false;
             }

             System.out.println("→ 发送指令: " + name + " [" + protocol + " " + ip + ":" + port + "] 数据长度: " + commandBytes.length + " 字节");
             System.out.println("  十六进制数据: " + bytesToHexString(commandBytes));

             // 根据协议类型发送
             if ("TCP".equals(protocol)) {
                 return sendTCPCommand(ip, port, commandBytes, name);
             } else {
                 return sendUDPCommand(ip, port, commandBytes, name);
             }

         } catch (Exception e) {
             System.out.println("✗ 发送指令异常: " + e.getMessage());
             return false;
         }
     }

     /**
      * 发送TCP指令
      */
     private boolean sendTCPCommand(String ip, int port, byte[] data, String commandName) {
         Socket socket = null;
         try {
             System.out.println("  → 建立TCP连接到 " + ip + ":" + port);
             socket = new Socket();
             socket.connect(new InetSocketAddress(ip, port), 3000); // 3秒连接超时
             socket.setSoTimeout(3000); // 3秒读取超时

             System.out.println("  ✓ TCP连接已建立");

             // 发送数据
             OutputStream out = socket.getOutputStream();
             out.write(data);
             out.flush();

             System.out.println("  ✓ TCP数据发送完成，已发送 " + data.length + " 字节");

             // 快速发送模式：不等待响应，立即返回
             // 注释掉响应读取逻辑，避免每个指令都等待3秒
             /*
             try {
                 InputStream in = socket.getInputStream();
                 byte[] buffer = new byte[1024];
                 int bytesRead = in.read(buffer);
                 if (bytesRead > 0) {
                     byte[] response = new byte[bytesRead];
                     System.arraycopy(buffer, 0, response, 0, bytesRead);
                     System.out.println("  ← 收到TCP响应: " + bytesToHexString(response) + " (" + bytesRead + " 字节)");
                 }
             } catch (SocketTimeoutException e) {
                 System.out.println("  ⚠ TCP读取超时，无响应数据");
             }
             */
             System.out.println("  ✓ TCP指令发送完成（快速模式，不等待响应）");

             return true;

         } catch (ConnectException e) {
             System.out.println("  ✗ TCP连接失败: 目标主机拒绝连接 " + ip + ":" + port);
             return false;
         } catch (SocketTimeoutException e) {
             System.out.println("  ✗ TCP连接超时: " + ip + ":" + port);
             return false;
         } catch (UnknownHostException e) {
             System.out.println("  ✗ TCP连接失败: 未知主机 " + ip);
             return false;
         } catch (Exception e) {
             System.out.println("  ✗ TCP发送异常: " + e.getMessage());
             return false;
         } finally {
             if (socket != null) {
                 try {
                     socket.close();
                     System.out.println("  ✓ TCP连接已关闭");
                 } catch (Exception e) {
                     System.out.println("  ⚠ TCP连接关闭异常: " + e.getMessage());
                 }
             }
         }
     }

     /**
      * 发送UDP指令
      */
     private boolean sendUDPCommand(String ip, int port, byte[] data, String commandName) {
         DatagramSocket socket = null;
         try {
             System.out.println("  → 创建UDP套接字发送到 " + ip + ":" + port);
             socket = new DatagramSocket();
             socket.setSoTimeout(3000); // 3秒接收超时

             // 创建数据包
             InetAddress address = InetAddress.getByName(ip);
             DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

             // 发送数据包
             socket.send(packet);
             System.out.println("  ✓ UDP数据发送完成，已发送 " + data.length + " 字节");

             // 可选：等待响应
             try {
                 byte[] buffer = new byte[1024];
                 DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                 socket.receive(responsePacket);

                 byte[] response = new byte[responsePacket.getLength()];
                 System.arraycopy(responsePacket.getData(), 0, response, 0, responsePacket.getLength());
                 System.out.println("  ← 收到UDP响应: " + bytesToHexString(response) + " (" + response.length + " 字节)");
                 System.out.println("    响应来源: " + responsePacket.getAddress().getHostAddress() + ":" + responsePacket.getPort());

             } catch (SocketTimeoutException e) {
                 System.out.println("  ⚠ UDP接收超时，无响应数据");
             }

             return true;

         } catch (UnknownHostException e) {
             System.out.println("  ✗ UDP发送失败: 未知主机 " + ip);
             return false;
         } catch (Exception e) {
             System.out.println("  ✗ UDP发送异常: " + e.getMessage());
             return false;
         } finally {
             if (socket != null) {
                 socket.close();
                 System.out.println("  ✓ UDP套接字已关闭");
             }
         }
     }

     /**
      * 验证IP地址格式
      */
     private boolean isValidIPAddress(String ip) {
         if (ip == null || ip.isEmpty()) {
             return false;
         }

         String[] parts = ip.split("\\.");
         if (parts.length != 4) {
             return false;
         }

         try {
             for (String part : parts) {
                 int num = Integer.parseInt(part);
                 if (num < 0 || num > 255) {
                     return false;
                 }
             }
             return true;
         } catch (NumberFormatException e) {
             return false;
         }
     }

     /**
      * 十六进制字符串转字节数组
      */
     private byte[] hexStringToBytes(String hexString) {
         if (hexString == null || hexString.isEmpty()) {
             return new byte[0];
         }

         // 去除空格和其他分隔符
         hexString = hexString.replaceAll("\\s+", "").replaceAll("[^0-9A-Fa-f]", "");

         // 验证只包含有效的十六进制字符
         if (!hexString.matches("[0-9A-Fa-f]*")) {
             System.out.println("  ✗ 十六进制字符串包含无效字符，仅支持0-9、A-F");
             return null;
         }

         // 确保长度为偶数
         if (hexString.length() % 2 != 0) {
             System.out.println("  ⚠ 十六进制字符串长度为奇数，自动在前面补0");
             hexString = "0" + hexString;
         }

         try {
             byte[] bytes = new byte[hexString.length() / 2];
             for (int i = 0; i < bytes.length; i++) {
                 int index = i * 2;
                 bytes[i] = (byte) Integer.parseInt(hexString.substring(index, index + 2), 16);
             }
             return bytes;
         } catch (NumberFormatException e) {
             System.out.println("  ✗ 十六进制字符串转换失败: " + e.getMessage());
             return null;
         }
     }

     /**
      * 字节数组转十六进制字符串
      */
     private String bytesToHexString(byte[] bytes) {
         if (bytes == null || bytes.length == 0) {
             return "";
         }

         StringBuilder sb = new StringBuilder();
         for (byte b : bytes) {
             sb.append(String.format("%02X ", b & 0xFF));
         }
         return sb.toString().trim();
     }

     /**
      * 执行实例组
      */
     private void executeInstanceGroup(JPanel instance) {
         executeInstanceGroup(instance, true); // 默认为顶层实例组，需要统一等待
     }

     /**
      * 执行实例组（支持嵌套控制）
      * @param instance 实例组面板
      * @param isTopLevel 是否为顶层实例组（控制是否需要统一等待3秒）
      */
     private void executeInstanceGroup(JPanel instance, boolean isTopLevel) {
         String groupName = (String) instance.getClientProperty("instanceGroupName");
         Object groupCommands = instance.getClientProperty("instanceGroupCommands");
         Object groupDelays = instance.getClientProperty("instanceGroupDelays");

         if (groupName == null || groupCommands == null) {
             System.out.println("无实例组可执行");
             return;
         }

         if (!(groupCommands instanceof java.util.List)) {
             System.out.println("实例组指令格式错误");
             return;
         }

         @SuppressWarnings("unchecked")
         java.util.List<String> commandNames = (java.util.List<String>) groupCommands;

         @SuppressWarnings("unchecked")
         java.util.Map<String, String> delayMap = (groupDelays instanceof java.util.Map) ?
             (java.util.Map<String, String>) groupDelays : new java.util.HashMap<>();

         System.out.println("=== 开始执行实例组: " + groupName + ", 包含 " + commandNames.size() + " 条指令 ===");

         int successCount = 0;
         int failCount = 0;

         for (int i = 0; i < commandNames.size(); i++) {
             String commandData = commandNames.get(i);
             System.out.println("执行第 " + (i + 1) + " 条指令: " + commandData);

             // 处理每条指令的延时（包括第一条指令）
             String[] parts = commandData.split("\\|");
             String commandKey = parts.length > 0 ? parts[0] : commandData;
             String delayStr = delayMap.get(commandKey);

             double delay = 0.0;
             if (delayStr != null && !delayStr.isEmpty()) {
                 try {
                     delay = Double.parseDouble(delayStr);
                 } catch (NumberFormatException e) {
                     delay = 0.0;
                 }
             }

             if (delay > 0) {
                 System.out.println("执行前延时: " + delay + " 秒 (指令: " + commandKey + ")");
                 try {
                     Thread.sleep((long) (delay * 1000));
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     break;
                 }
             } else if (i > 0) {
                 // 非第一条指令且没有设置延时时，默认间隔100ms
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     break;
                 }
             }

             try {
                 boolean success = false;

                 // 检查是否是嵌套的实例组（复用之前的parts变量）
                 if (parts.length >= 2 && "GROUP".equals(parts[1])) {
                     // 这是一个嵌套的实例组，需要递归执行
                     String nestedGroupName = parts[0];
                     if (nestedGroupName.endsWith(" (实例组)")) {
                         nestedGroupName = nestedGroupName.substring(0, nestedGroupName.length() - 5);
                     }
                     System.out.println("  → 执行嵌套实例组: " + nestedGroupName);
                     success = executeNestedInstanceGroup(nestedGroupName);
                 } else {
                     // 普通的网络指令，直接发送
                     success = sendNetworkCommand(commandData);
                 }

                 if (success) {
                     successCount++;
                     System.out.println("✓ 第 " + (i + 1) + " 条指令执行成功");
                 } else {
                     failCount++;
                     System.out.println("✗ 第 " + (i + 1) + " 条指令执行失败");
                 }
             } catch (Exception e) {
                 failCount++;
                 System.out.println("✗ 第 " + (i + 1) + " 条指令执行异常: " + e.getMessage());
             }
         }

         System.out.println("=== 实例组执行完成，成功: " + successCount + " 条，失败: " + failCount + " 条 ===");

         // 只有顶层实例组才需要统一等待3秒
         if (isTopLevel && commandNames.size() > 0) {
             System.out.println("顶层实例组所有指令发送完成，等待3秒...");
             try {
                 Thread.sleep(3000);
                 System.out.println("等待完成");
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 System.out.println("等待被中断");
             }
         } else if (!isTopLevel) {
             System.out.println("嵌套实例组执行完成，不等待");
         }
     }

     /**
      * 执行嵌套的实例组
      */
     private boolean executeNestedInstanceGroup(String groupName) {
         System.out.println("  → 开始查找嵌套实例组: " + groupName);

         // 在所有页面中查找对应的实例组
         if (pageContents != null) {
             System.out.println("  → 总页面数: " + pageContents.size());

             for (String pageName : pageContents.keySet()) {
                 java.util.List<Component> pageContent = pageContents.get(pageName);
                 System.out.println("  → 检查页面: " + pageName + ", 组件数: " + (pageContent != null ? pageContent.size() : 0));

                 if (pageContent != null) {
                     for (int i = 0; i < pageContent.size(); i++) {
                         Component component = pageContent.get(i);
                         if (component instanceof JPanel) {
                             JPanel instance = (JPanel) component;
                             String functionType = (String) instance.getClientProperty("functionType");
                             String instanceGroupName = (String) instance.getClientProperty("instanceGroupName");

                             System.out.println("    → 组件 " + i + ": functionType=" + functionType + ", instanceGroupName=" + instanceGroupName);

                             // 详细的条件检查
                             boolean functionTypeMatch = "实例组".equals(functionType);

                             // 清理字符串，去除可能的空格和不可见字符
                             String cleanGroupName = groupName != null ? groupName.trim() : "";
                             String cleanInstanceGroupName = instanceGroupName != null ? instanceGroupName.trim() : "";
                             boolean groupNameMatch = cleanGroupName.equals(cleanInstanceGroupName);

                             System.out.println("      → functionType匹配: " + functionTypeMatch + " (期望: 实例组, 实际: " + functionType + ")");
                             System.out.println("      → groupName匹配: " + groupNameMatch + " (期望: '" + cleanGroupName + "', 实际: '" + cleanInstanceGroupName + "')");
                             System.out.println("      → groupName长度: 期望=" + cleanGroupName.length() + ", 实际=" + cleanInstanceGroupName.length());

                             if (functionTypeMatch && groupNameMatch) {
                                 System.out.println("  ✓ 找到嵌套实例组: " + groupName);
                                 // 递归执行实例组（嵌套实例组不需要统一等待）
                                 executeInstanceGroup(instance, false);
                                 return true;
                             }
                         }
                     }
                 }
             }
         } else {
             System.out.println("  ✗ pageContents 为 null");
         }

         System.out.println("  ✗ 未找到嵌套实例组: " + groupName);
         return false;
     }

     /**
      * 执行实例组中的单个指令
      */
     private boolean executeInstanceGroupCommand(String commandName) {
         // 去除可能的后缀标识
         String actualCommandName = commandName;
         boolean isInstanceGroup = false;

         if (commandName.endsWith(" (实例组)")) {
             actualCommandName = commandName.substring(0, commandName.length() - 5);
             isInstanceGroup = true;
         }

         // 在所有页面中查找对应的指令或实例组
         if (pageContents != null) {
             for (String pageName : pageContents.keySet()) {
                 java.util.List<Component> pageContent = pageContents.get(pageName);
                 if (pageContent != null) {
                     for (Component component : pageContent) {
                         if (component instanceof JPanel) {
                             JPanel instance = (JPanel) component;

                             String functionType = (String) instance.getClientProperty("functionType");

                             if (isInstanceGroup && "实例组".equals(functionType)) {
                                 // 执行实例组
                                 String groupName = (String) instance.getClientProperty("instanceGroupName");
                                 if (actualCommandName.equals(groupName)) {
                                     System.out.println("  → 找到实例组: " + actualCommandName);
                                     // 递归执行实例组（需要防止循环引用）
                                     Object groupCommands = instance.getClientProperty("instanceGroupCommands");
                                     if (groupCommands instanceof java.util.List) {
                                         @SuppressWarnings("unchecked")
                                         java.util.List<String> subCommands = (java.util.List<String>) groupCommands;
                                         for (String subCommand : subCommands) {
                                             executeInstanceGroupCommand(subCommand);
                                         }
                                     }
                                     return true;
                                 }
                             } else if (!isInstanceGroup && "发送指令".equals(functionType)) {
                                 // 执行发送指令
                                 Object commands = instance.getClientProperty("commands");
                                 if (commands instanceof java.util.List) {
                                     @SuppressWarnings("unchecked")
                                     java.util.List<String> commandList = (java.util.List<String>) commands;
                                     for (String cmd : commandList) {
                                         String cmdName = null;
                                         String fullCommandData = null;

                                         // 解析指令数据
                                         String[] parts = cmd.split("\\|");
                                         if (parts.length >= 5) {
                                             // 新格式：完整的指令数据
                                             cmdName = parts[0];
                                             fullCommandData = cmd;
                                         } else {
                                             // 旧格式：只有指令名称
                                             cmdName = cmd;
                                             fullCommandData = cmd; // 可能需要补充默认值
                                         }

                                         if (actualCommandName.equals(cmdName) && fullCommandData != null) {
                                             System.out.println("  → 找到指令: " + actualCommandName);
                                             return sendNetworkCommand(fullCommandData);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }

         System.out.println("  ✗ 未找到指令: " + actualCommandName);
         return false;
     }

     /**
      * 添加齿轮按钮到主界面
      */
     private void addGearButtonToMain() {
         if (gearButton != null) {
             System.out.println("添加齿轮按钮到主界面");

             // 如果齿轮按钮已经在容器中，先移除
             if (gearButton.getParent() != null) {
                 gearButton.getParent().remove(gearButton);
                 System.out.println("移除了已存在的齿轮按钮");
             }

             // 确保齿轮按钮在最高层级
             getContentPane().add(gearButton);
             getContentPane().setComponentZOrder(gearButton, 0);
             gearButton.setVisible(true);

             // 更新齿轮按钮位置
             updateGearButtonPosition();

             System.out.println("齿轮按钮位置: " + gearButton.getBounds());
             System.out.println("齿轮按钮可见性: " + gearButton.isVisible());
         } else {
             System.out.println("齿轮按钮为null");
         }
     }

     /**
      * 更新齿轮按钮位置
      */
     private void updateGearButtonPosition() {
         if (gearButton != null) {
             // 获取屏幕尺寸（全屏模式下）
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

             // 将齿轮按钮放在左下角
             int x = 20;
             int y = screenSize.height - gearButton.getHeight() - 20;
             gearButton.setLocation(x, y);
             System.out.println("齿轮按钮位置更新: (" + x + ", " + y + "), 屏幕高度: " + screenSize.height);
         }
     }

     private JPanel createTopPropertyBar() {
         propertyBar = new JPanel();
         propertyBar.setLayout(new BorderLayout());
         propertyBar.setBackground(Color.WHITE);
         propertyBar.setPreferredSize(new Dimension(0, 213)); // 保持增加后的高度
         propertyBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

         // 标题区域
         JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         titlePanel.setBackground(Color.WHITE);

         JLabel titleLabel = new JLabel("属性配置面板");
         titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
         titleLabel.setForeground(new Color(44, 62, 80));
         titlePanel.add(titleLabel);

         propertyBar.add(titlePanel, BorderLayout.NORTH);

         // 主要内容区域
         JPanel mainContent = new JPanel(new BorderLayout());
         mainContent.setBackground(Color.WHITE);

         // 属性卡片区域
         JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
         cardsPanel.setBackground(Color.WHITE);

         // 1. 文本属性卡片
         JPanel textCard = createTextPropertyCard();
         cardsPanel.add(textCard);

         // 2. 位置属性卡片
         JPanel positionCard = createPositionPropertyCard();
         cardsPanel.add(positionCard);

         // 3. 显示属性卡片
         JPanel displayCard = createDisplayPropertyCard();
         cardsPanel.add(displayCard);

         // 4. 功能属性卡片
         JPanel functionCard = createFunctionPropertyCard();
         cardsPanel.add(functionCard);

         // 5. 接口配置卡片
         JPanel interfaceCard = createInterfacePropertyCard();
         cardsPanel.add(interfaceCard);

         // 6. 操作按钮卡片
         JPanel operationCard = createOperationCard();
         cardsPanel.add(operationCard);

         mainContent.add(cardsPanel, BorderLayout.CENTER);

         propertyBar.add(mainContent, BorderLayout.CENTER);

         return propertyBar;
     }

     private JPanel createPropertyCard(String title, String[][] fields) {
         JPanel card = new JPanel();
         card.setLayout(new BorderLayout());
         card.setBackground(new Color(248, 249, 250));
         card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
         card.setPreferredSize(new Dimension(180, 133)); // 增加三分之一高度 (100 * 1.33)

         // 卡片标题
         JPanel header = new JPanel();
         header.setBackground(new Color(233, 236, 239));
         header.setPreferredSize(new Dimension(0, 26));

         JLabel headerLabel = new JLabel(title);
         headerLabel.setFont(getUnicodeFont(11)); // 增加字体大小
         headerLabel.setForeground(new Color(73, 80, 87));
         header.add(headerLabel);

         card.add(header, BorderLayout.NORTH);

         // 卡片内容
         JPanel content = new JPanel(new GridLayout(fields.length, 2, 3, 3));
         content.setBackground(new Color(248, 249, 250));
         content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

         for (String[] field : fields) {
             JLabel label = new JLabel(field[0]);
             label.setFont(getUnicodeFont(10)); // 增加字体大小
             label.setForeground(new Color(73, 80, 87));
             content.add(label);

             JTextField textField = new JTextField(field[1]);
             textField.setFont(getUnicodeFont(10)); // 增加字体大小
             textField.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(new Color(206, 212, 218)),
                 BorderFactory.createEmptyBorder(2, 4, 2, 4)
             ));
             content.add(textField);
         }

         card.add(content, BorderLayout.CENTER);

         return card;
     }

    private JPanel createLeftPageList() {
         JPanel leftPanel = new JPanel(new BorderLayout());
         leftPanel.setBackground(Color.WHITE);
         leftPanel.setPreferredSize(new Dimension(200, 0)); // 调整为适合20个字符的宽度
         leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

         // 标题区域 - 优化视觉效果
         JPanel titleArea = new JPanel(new BorderLayout());
         titleArea.setBackground(new Color(248, 249, 250)); // 浅灰背景，与列表区域形成对比
         titleArea.setPreferredSize(new Dimension(0, 52));
         titleArea.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(233, 236, 239)), // 底部分割线
             BorderFactory.createEmptyBorder(12, 15, 12, 15) // 内边距
         ));

         JLabel titleLabel = new JLabel("页面管理");
         titleLabel.setFont(getUnicodeFont(15));
         titleLabel.setForeground(new Color(44, 62, 80));
         titleArea.add(titleLabel, BorderLayout.WEST);

         JButton newPageBtn = new JButton("新建");
         newPageBtn.setFont(getUnicodeFont(12));
         newPageBtn.setBackground(new Color(0, 123, 255));
         newPageBtn.setForeground(Color.WHITE);
         newPageBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
         newPageBtn.setFocusPainted(false);
         addButtonHoverEffect(newPageBtn, new Color(0, 123, 255), new Color(0, 86, 179));

         // 添加新建页面点击事件
         newPageBtn.addActionListener(e -> showAddPageDialog());

         titleArea.add(newPageBtn, BorderLayout.EAST);

         leftPanel.add(titleArea, BorderLayout.NORTH);

         // 页面列表区域 - 美化视觉效果
         JPanel listArea = new JPanel(new BorderLayout());
         listArea.setBackground(new Color(250, 251, 252)); // 更柔和的背景色
         listArea.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8)); // 添加内边距

         // 初始化页面列表模型（使用成员变量）- 初始为空
         pageListModel = new DefaultListModel<>();

         pageList = new CustomJList<>(pageListModel);
         pageList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
         pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

         // 使用默认的选中模型
         pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

         // 初始列表为空，不设置选中项

         // 统一的鼠标事件处理器，处理左键点击和右键菜单
         pageList.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 handlePageListClick(e, false); // 处理点击事件
             }

             @Override
             public void mousePressed(MouseEvent e) {
                 if (e.isPopupTrigger()) {
                     handlePageListClick(e, true); // 处理右键菜单
                 }
             }

             @Override
             public void mouseReleased(MouseEvent e) {
                 if (e.isPopupTrigger()) {
                     handlePageListClick(e, true); // 处理右键菜单
                 }
             }
         });

         // 添加选中状态监听器，用于调试和监控
         pageList.addListSelectionListener(e -> {
             if (!e.getValueIsAdjusting()) {
                 int selectedIndex = pageList.getSelectedIndex();
                 if (selectedIndex >= 0 && selectedIndex < pageListModel.getSize()) {
                     String selectedPage = pageListModel.getElementAt(selectedIndex);
                     System.out.println("页面列表选中状态变化: " + selectedPage + " (索引: " + selectedIndex + ")" +
                                      " - 调用栈: " + Thread.currentThread().getStackTrace()[2].getMethodName());
                 } else {
                     System.out.println("页面列表选中状态已清除 - 调用栈: " +
                                      Thread.currentThread().getStackTrace()[2].getMethodName());
                 }
             } else {
                 System.out.println("页面列表选中状态调整中... (valueIsAdjusting=true)");
             }
         });

         // 添加右键菜单
         addPageListContextMenu(pageList);

         // 添加快捷键绑定
         addPageListKeyBindings(pageList);
         pageList.setCellRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                     boolean isSelected, boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                 // 优化边距和间距
                 setBorder(BorderFactory.createCompoundBorder(
                     BorderFactory.createEmptyBorder(2, 8, 2, 8), // 外边距
                     BorderFactory.createEmptyBorder(6, 8, 6, 8)  // 内边距
                 ));

                 setFont(getUnicodeFont(12)); // 使用普通字体

                 if (isSelected) {
                     // 选中状态：更明显的蓝色背景和白色文字
                     setBackground(new Color(0, 123, 255));
                     setForeground(Color.WHITE);
                     setOpaque(true);
                 } else {
                     // 未选中状态：白色背景，深灰色文字，添加细微边框
                     setBackground(Color.WHITE);
                     setForeground(new Color(52, 58, 64));
                     setOpaque(true);
                     setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createLineBorder(new Color(233, 236, 239), 1), // 细边框
                         BorderFactory.createEmptyBorder(6, 8, 6, 8)  // 内边距
                     ));
                 }
                 return this;
             }
         });

         JScrollPane scrollPane = new JScrollPane(pageList);
         // 添加圆角边框，让列表区域更加清晰
         scrollPane.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(206, 212, 218), 1), // 外边框
             BorderFactory.createEmptyBorder(2, 2, 2, 2) // 内边距
         ));
         scrollPane.getVerticalScrollBar().setUnitIncrement(16);
         scrollPane.setBackground(Color.WHITE);

         // 设置滚动条策略，只支持纵向滚动
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

         // 缩小滚动条宽度
         scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); // 从默认16像素缩小到8像素

         listArea.add(scrollPane, BorderLayout.CENTER);
         leftPanel.add(listArea, BorderLayout.CENTER);

         // 底部批量删除按钮区域
         JPanel bottomPanel = new JPanel(new FlowLayout());
         bottomPanel.setBackground(new Color(248, 249, 250));
         bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

         JButton batchDeleteBtn = new JButton("删除页面");
         batchDeleteBtn.setFont(getUnicodeFont(12));
         batchDeleteBtn.setBackground(new Color(220, 53, 69));
         batchDeleteBtn.setForeground(Color.WHITE);
         batchDeleteBtn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
         batchDeleteBtn.setFocusPainted(false);
         batchDeleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
         batchDeleteBtn.addActionListener(e -> showMultiDeleteDialog());

         // 添加悬停效果
         addButtonHoverEffect(batchDeleteBtn, new Color(220, 53, 69), new Color(200, 35, 51));

         bottomPanel.add(batchDeleteBtn);
         leftPanel.add(bottomPanel, BorderLayout.SOUTH);

         return leftPanel;
     }

     private JPanel createCenterEditArea() {
         JPanel centerPanel = new JPanel(new BorderLayout());
         centerPanel.setBackground(new Color(248, 249, 250));

         // 标题卡片
         JPanel titleCard = new JPanel(new BorderLayout());
         titleCard.setBackground(Color.WHITE);
         titleCard.setPreferredSize(new Dimension(0, 50));
         titleCard.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

         JPanel titleContent = new JPanel(new BorderLayout());
         titleContent.setBackground(Color.WHITE);
         titleContent.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

         // 左侧：当前页面标签
         currentPageLabel = new JLabel("当前页面: 无");
         currentPageLabel.setFont(getUnicodeFont(15));
         currentPageLabel.setForeground(new Color(44, 62, 80));
         titleContent.add(currentPageLabel, BorderLayout.WEST);

         // 右侧：分辨率选择器
         JPanel resolutionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
         resolutionPanel.setBackground(Color.WHITE);

         // 添加顶部负内边距使区域上移（数值越大上移越多）
         resolutionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
         // 最后一个参数20是右边距，避免贴边

         JLabel resolutionLabel = new JLabel("分辨率:");
         resolutionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
         resolutionLabel.setForeground(new Color(73, 80, 87));
         resolutionPanel.add(resolutionLabel);

         resolutionPanel.add(Box.createHorizontalStrut(8));

         // 常用平板分辨率选项，默认1024x768
         resolutionComboBox = new JComboBox<>(new String[]{
             "1024x768", "1280x800", "1366x768", "1600x1200",
             "1920x1080", "1920x1200", "2048x1536", "2224x1668",
             "2360x1640", "2732x2048"
         });
         resolutionComboBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
         resolutionComboBox.setPreferredSize(new Dimension(130, 28));
         resolutionComboBox.setSelectedIndex(0); // 默认选择1024x768

         // 添加分辨率变化监听器
         resolutionComboBox.addActionListener(e -> {
             String newResolution = (String) resolutionComboBox.getSelectedItem();
             System.out.println("分辨率选择器事件触发: " + newResolution);

             // 保存旧的分辨率
             String oldResolution = currentEditResolution;

             // 更新当前编辑分辨率
             if (newResolution != null) {
                 currentEditResolution = newResolution;
                 System.out.println("编辑分辨率已更新为: " + currentEditResolution);
             }

             // 更新画布尺寸
             updatePageDisplayArea();

             // 重新计算现有实例的位置
             // updateInstancesForResolutionChange(oldResolution, newResolution);
         });

         resolutionPanel.add(resolutionComboBox);

         titleContent.add(resolutionPanel, BorderLayout.EAST);

         titleCard.add(titleContent, BorderLayout.CENTER);
         centerPanel.add(titleCard, BorderLayout.NORTH);

         // 页面显示区域（中间剩余的所有空间）
         createPageDisplayArea(centerPanel);

         // 初始化页面显示区域（确保editCanvas已经创建）
         SwingUtilities.invokeLater(() -> {
            updatePageDisplayArea();
            // 编辑模式启动，等待用户添加页面
            System.out.println("=== 编辑模式已启动，等待用户添加页面 ===");
            System.out.println("editCanvas: " + (editCanvas != null ? "已创建" : "未创建"));
            System.out.println("页面列表状态: " + (pageListModel != null && !pageListModel.isEmpty() ? "有页面" : "无页面"));
            System.out.println("属性面板控件状态:");
            System.out.println("  textField: " + (textField != null ? "已创建" : "未创建"));
            System.out.println("  xField: " + (xField != null ? "已创建" : "未创建"));
            System.out.println("  yField: " + (yField != null ? "已创建" : "未创建"));

            // 属性面板已在创建时设置了默认值
        });

         return centerPanel;
     }

     private JPanel createRightFunctionButtons() {
         JPanel rightPanel = new JPanel(new BorderLayout());
         rightPanel.setBackground(Color.WHITE);
         rightPanel.setPreferredSize(new Dimension(200, 0));
         rightPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));

         // 标题装饰条
         JPanel titleAccent = new JPanel();
         titleAccent.setBackground(new Color(40, 167, 69));
         titleAccent.setPreferredSize(new Dimension(0, 4));
         rightPanel.add(titleAccent, BorderLayout.NORTH);

         // 标题区域
         JPanel titleArea = new JPanel();
         titleArea.setBackground(Color.WHITE);
         titleArea.setPreferredSize(new Dimension(0, 50));
         titleArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

         JLabel titleLabel = new JLabel("快捷操作");
         titleLabel.setFont(getUnicodeFont(14));
         titleLabel.setForeground(new Color(44, 62, 80));
         titleArea.add(titleLabel);

         rightPanel.add(titleArea, BorderLayout.NORTH);

         // 功能按钮区域
         JPanel buttonArea = new JPanel();
         buttonArea.setLayout(new BoxLayout(buttonArea, BoxLayout.Y_AXIS));
         buttonArea.setBackground(Color.WHITE);
         buttonArea.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

         // 创建功能按钮
         String[][] buttons = {
             {"添加背景", "#6c757d"},
             {"添加图片", "#007bff"},
             {"添加文本", "#17a2b8"},
             {"添加视频", "#6f42c1"},
             {"添加实例", "#28a745"},
             {"数据同步", "#17a2b8"},
             {"键盘映射", "#fd7e14"},
             {"定时器", "#e83e8c"},
             {"保存", "#20c997"},
             {"退出编辑", "#dc3545"}
         };

         for (String[] btnInfo : buttons) {
             JButton btn = new JButton(btnInfo[0]);
             btn.setFont(getUnicodeFont(12));
             btn.setBackground(Color.decode(btnInfo[1]));
             btn.setForeground(Color.WHITE);
             btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
             btn.setFocusPainted(false);
             btn.setAlignmentX(Component.CENTER_ALIGNMENT);
             btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

             // 添加按钮事件处理
             String buttonText = btnInfo[0];
             if ("退出编辑".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     // 显示确认对话框
                     int result = JOptionPane.showConfirmDialog(
                         editWindow,
                         "确定要退出编辑模式吗？\n未保存的更改可能会丢失。",
                         "确认退出",
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE
                     );

                     if (result == JOptionPane.YES_OPTION) {
                         // 用户确认退出，执行退出操作
                         exitEditMode();
                     }
                     // 如果用户选择"否"，则不执行任何操作，继续编辑
                 });
             } else if ("添加背景".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     if (validatePageExists()) {
                         addBackgroundToCanvas();
                     }
                 });
             } else if ("添加图片".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     if (validatePageExists()) {
                         addImageToCanvas();
                     }
                 });
             } else if ("添加文本".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     if (validatePageExists()) {
                         addTextToCanvas();
                     }
                 });
             } else if ("添加视频".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     if (validatePageExists()) {
                         addVideoToCanvas();
                     }
                 });
             } else if ("添加实例".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     if (validatePageExists()) {
                         addInstanceToCanvas();
                     }
                 });
             } else if ("保存".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     saveProjectData();
                 });
             } else if ("定时器".equals(buttonText)) {
                 btn.addActionListener(e -> {
                     openTimerDialog();
                 });
             }

             // 添加悬停效果
             Color originalColor = Color.decode(btnInfo[1]);
             Color hoverColor = originalColor.darker();
             addButtonHoverEffect(btn, originalColor, hoverColor);

             buttonArea.add(btn);
             buttonArea.add(Box.createVerticalStrut(8));
         }

         JScrollPane scrollPane = new JScrollPane(buttonArea);
         scrollPane.setBorder(null);
         scrollPane.getVerticalScrollBar().setUnitIncrement(16);
         rightPanel.add(scrollPane, BorderLayout.CENTER);

         return rightPanel;
     }

     private void showChangePasswordDialog() {
         // 创建修改密码对话框 - 修复层级问题
         JDialog changePasswordDialog = new JDialog(this, "修改密码", true);
         changePasswordDialog.setSize(580, 720);
         changePasswordDialog.setLocationRelativeTo(this);
         changePasswordDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         changePasswordDialog.setResizable(false);
         changePasswordDialog.getContentPane().setBackground(new Color(245, 247, 250));
         changePasswordDialog.setLayout(new BorderLayout());

         // 确保对话框始终在最前面
         changePasswordDialog.setAlwaysOnTop(true);
         changePasswordDialog.toFront();
         changePasswordDialog.requestFocus();

         // 创建主面板
         JPanel mainPanel = new JPanel();
         mainPanel.setLayout(new BorderLayout());
         mainPanel.setBackground(new Color(245, 247, 250));

         // 顶部渐变装饰条
         JPanel topGradient = new JPanel() {
             @Override
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                 Graphics2D g2d = (Graphics2D) g;
                 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 GradientPaint gradient = new GradientPaint(0, 0, new Color(74, 144, 226), getWidth(), 0, new Color(80, 200, 120));
                 g2d.setPaint(gradient);
                 g2d.fillRect(0, 0, getWidth(), getHeight());
             }
         };
         topGradient.setPreferredSize(new Dimension(0, 6));
         mainPanel.add(topGradient, BorderLayout.NORTH);

         // 中心内容面板
         JPanel centerPanel = new JPanel();
         centerPanel.setLayout(new BorderLayout());
         centerPanel.setBackground(Color.WHITE);
         centerPanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(220, 225, 230)),
             BorderFactory.createEmptyBorder(40, 50, 40, 50)
         ));

         // 内容容器
         JPanel contentContainer = new JPanel();
         contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
         contentContainer.setBackground(Color.WHITE);

         // 标题区域
         JPanel headerPanel = new JPanel();
         headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
         headerPanel.setBackground(Color.WHITE);
         headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

         // 移除授权图标
         headerPanel.add(Box.createVerticalStrut(15));

         // 主标题
         JLabel titleLabel = new JLabel("修改密码");
         titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
         titleLabel.setForeground(new Color(33, 37, 41));
         titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         headerPanel.add(titleLabel);
         headerPanel.add(Box.createVerticalStrut(8));

         // 副标题
         JLabel subtitleLabel = new JLabel("为了您的账户安全，请设置新的密码");
         subtitleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         subtitleLabel.setForeground(new Color(108, 117, 125));
         subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         headerPanel.add(subtitleLabel);

         contentContainer.add(headerPanel);
         contentContainer.add(Box.createVerticalStrut(40));

         // 密码要求提示卡片 - 取消红色边框，降低高度三分之一
         JPanel requirementCard = new JPanel();
         requirementCard.setLayout(new BorderLayout());
         requirementCard.setBackground(new Color(248, 249, 250));
         requirementCard.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(220, 225, 230), 1), // 保持原来的灰色边框
             BorderFactory.createEmptyBorder(3, 15, 3, 15) // 大幅减少垂直内边距，降低高度约三分之一
         ));

         JLabel requirementText = new JLabel("密码要求：3-16位数字和字母组合");
         requirementText.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
         requirementText.setForeground(new Color(73, 80, 87));
         requirementCard.add(requirementText, BorderLayout.WEST);

         contentContainer.add(requirementCard);
         contentContainer.add(Box.createVerticalStrut(20));

         // 输入区域
         JPanel inputArea = new JPanel();
         inputArea.setLayout(new BoxLayout(inputArea, BoxLayout.Y_AXIS));
         inputArea.setBackground(Color.WHITE);

         // 新密码输入组
         JPanel[] newPasswordComponents = createPasswordInputGroup("新密码", "请输入新密码");
         JPanel newPasswordGroup = newPasswordComponents[0];
         JPasswordField newPasswordField = (JPasswordField) newPasswordGroup.getClientProperty("passwordField");
         JLabel newPasswordStatus = (JLabel) newPasswordGroup.getClientProperty("statusIcon");
         JLabel newPasswordHint = (JLabel) newPasswordGroup.getClientProperty("hintLabel");

         inputArea.add(newPasswordGroup);
         inputArea.add(Box.createVerticalStrut(25));


         // 确认密码输入组
         JPanel[] confirmPasswordComponents = createPasswordInputGroup("确认密码", "请再次输入新密码");
         JPanel confirmPasswordGroup = confirmPasswordComponents[0];
         JPasswordField confirmPasswordField = (JPasswordField) confirmPasswordGroup.getClientProperty("passwordField");
         JLabel confirmPasswordStatus = (JLabel) confirmPasswordGroup.getClientProperty("statusIcon");
         JLabel confirmPasswordHint = (JLabel) confirmPasswordGroup.getClientProperty("hintLabel");

         // 初始状态下确认密码输入框禁用
         confirmPasswordField.setEnabled(false);
         confirmPasswordField.setBackground(new Color(248, 249, 250));
         confirmPasswordField.setForeground(new Color(108, 117, 125));

         inputArea.add(confirmPasswordGroup);
         contentContainer.add(inputArea);
         contentContainer.add(Box.createVerticalStrut(40));

         // 按钮区域 - 增加按钮间距并交换位置
         JPanel buttonArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0)); // 增加按钮间距从15到30
         buttonArea.setBackground(Color.WHITE);

         // 确认按钮 - 移到左侧，优化外观设计
         JButton confirmButton = createStyledButton("确认修改", new Color(74, 144, 226), Color.WHITE, 140, 45);
         confirmButton.setEnabled(false);

         // 取消按钮 - 移到右侧，优化外观设计
         JButton cancelButton = createStyledButton("取消", new Color(220, 53, 69), Color.WHITE, 140, 45);

         buttonArea.add(confirmButton); // 确认按钮在左侧
         buttonArea.add(cancelButton);  // 取消按钮在右侧
         contentContainer.add(buttonArea);

         centerPanel.add(contentContainer, BorderLayout.CENTER);
         mainPanel.add(centerPanel, BorderLayout.CENTER);
         changePasswordDialog.add(mainPanel, BorderLayout.CENTER);

         // 密码验证逻辑
         final boolean[] newPasswordValid = {false};
         final boolean[] confirmPasswordValid = {false};

         // 新密码验证
         DocumentListener newPasswordListener = new DocumentListener() {
             public void insertUpdate(DocumentEvent e) { validateNewPassword(); }
             public void removeUpdate(DocumentEvent e) { validateNewPassword(); }
             public void changedUpdate(DocumentEvent e) { validateNewPassword(); }

             private void validateNewPassword() {
                 String password = new String(newPasswordField.getPassword());

                 if (password.isEmpty()) {
                     newPasswordStatus.setText("");
                     newPasswordHint.setText("");
                     newPasswordValid[0] = false;
                     confirmPasswordField.setEnabled(false);
                     confirmPasswordField.setBackground(new Color(248, 249, 250));
                     confirmPasswordField.setForeground(new Color(108, 117, 125));
                     confirmPasswordField.setText("");
                     confirmPasswordStatus.setText("");
                     confirmPasswordHint.setText("");
                     updateConfirmButtonState();
                     return;
                 }

                 // 密码格式验证：3-16位数字和字母
                 if (password.matches("^[a-zA-Z0-9]{3,16}$")) {
                     // 检查是否与当前密码相同
                     if (securityConfig.verifyPassword(password)) {
                         newPasswordStatus.setText("❌");
                         newPasswordStatus.setForeground(new Color(220, 53, 69));
                         newPasswordHint.setText("新密码与原密码相同，请重新输入不同的密码");
                         newPasswordValid[0] = false;
                         confirmPasswordField.setEnabled(false);
                         confirmPasswordField.setBackground(new Color(248, 249, 250));
                         confirmPasswordField.setForeground(new Color(108, 117, 125));
                         confirmPasswordField.setText("");
                         confirmPasswordStatus.setText("");
                         confirmPasswordHint.setText("");
                         updateConfirmButtonState();
                     } else {
                         newPasswordStatus.setText("✅");
                         newPasswordStatus.setForeground(new Color(40, 167, 69));
                         newPasswordHint.setText("");
                         newPasswordValid[0] = true;
                         confirmPasswordField.setEnabled(true);
                         confirmPasswordField.setBackground(Color.WHITE);
                         confirmPasswordField.setForeground(new Color(73, 80, 87));
                         // 重新验证确认密码
                         validateConfirmPassword();
                     }
                 } else {
                     newPasswordStatus.setText("❌");
                     newPasswordStatus.setForeground(new Color(220, 53, 69));
                     newPasswordHint.setText("格式不正确，请输入3-16位数字和字母组合");
                     newPasswordValid[0] = false;
                     confirmPasswordField.setEnabled(false);
                     confirmPasswordField.setBackground(new Color(248, 249, 250));
                     confirmPasswordField.setForeground(new Color(108, 117, 125));
                     confirmPasswordField.setText("");
                     confirmPasswordStatus.setText("");
                     confirmPasswordHint.setText("");
                     updateConfirmButtonState();
                 }
             }

             private void validateConfirmPassword() {
                 if (!newPasswordValid[0]) return;

                 String newPassword = new String(newPasswordField.getPassword());
                 String confirmPassword = new String(confirmPasswordField.getPassword());

                 if (confirmPassword.isEmpty()) {
                     confirmPasswordStatus.setText("");
                     confirmPasswordHint.setText("");
                     confirmPasswordValid[0] = false;
                     updateConfirmButtonState();
                     return;
                 }

                 if (newPassword.equals(confirmPassword)) {
                     confirmPasswordStatus.setText("✅");
                     confirmPasswordStatus.setForeground(new Color(40, 167, 69));
                     confirmPasswordHint.setText("");
                     confirmPasswordValid[0] = true;
                     updateConfirmButtonState();
                 } else {
                     confirmPasswordStatus.setText("❌");
                     confirmPasswordStatus.setForeground(new Color(220, 53, 69));
                     confirmPasswordHint.setText("新密码和确认密码不一致");
                     confirmPasswordValid[0] = false;
                     updateConfirmButtonState();
                 }
             }

             private void updateConfirmButtonState() {
                 if (newPasswordValid[0] && confirmPasswordValid[0]) {
                     confirmButton.setEnabled(true);
                     confirmButton.setBackground(new Color(74, 144, 226));
                     confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                 } else {
                     confirmButton.setEnabled(false);
                     confirmButton.setBackground(new Color(108, 117, 125));
                     confirmButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                 }
             }
         };

         newPasswordField.getDocument().addDocumentListener(newPasswordListener);

         // 确认密码验证
         DocumentListener confirmPasswordListener = new DocumentListener() {
             public void insertUpdate(DocumentEvent e) { validateConfirmPassword(); }
             public void removeUpdate(DocumentEvent e) { validateConfirmPassword(); }
             public void changedUpdate(DocumentEvent e) { validateConfirmPassword(); }

             private void validateConfirmPassword() {
                 if (!newPasswordValid[0]) return;

                 String newPassword = new String(newPasswordField.getPassword());
                 String confirmPassword = new String(confirmPasswordField.getPassword());

                 if (confirmPassword.isEmpty()) {
                     confirmPasswordStatus.setText("");
                     confirmPasswordHint.setText("");
                     confirmPasswordValid[0] = false;
                     updateConfirmButtonState();
                     return;
                 }

                 if (newPassword.equals(confirmPassword)) {
                     confirmPasswordStatus.setText("✅");
                     confirmPasswordStatus.setForeground(new Color(40, 167, 69));
                     confirmPasswordHint.setText("");
                     confirmPasswordValid[0] = true;
                     updateConfirmButtonState();
                 } else {
                     confirmPasswordStatus.setText("❌");
                     confirmPasswordStatus.setForeground(new Color(220, 53, 69));
                     confirmPasswordHint.setText("新密码和确认密码不一致");
                     confirmPasswordValid[0] = false;
                     updateConfirmButtonState();
                 }
             }

             private void updateConfirmButtonState() {
                 if (newPasswordValid[0] && confirmPasswordValid[0]) {
                     confirmButton.setEnabled(true);
                     confirmButton.setBackground(new Color(74, 144, 226));
                     confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                 } else {
                     confirmButton.setEnabled(false);
                     confirmButton.setBackground(new Color(108, 117, 125));
                     confirmButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                 }
             }
         };

         confirmPasswordField.getDocument().addDocumentListener(confirmPasswordListener);

         // 按钮事件处理
         confirmButton.addActionListener(e -> {
             if (!newPasswordValid[0] || !confirmPasswordValid[0]) {
                 return;
             }

             String newPassword = new String(newPasswordField.getPassword());

             // 显示加载状态
             confirmButton.setEnabled(false);
             confirmButton.setText("修改中...");
             confirmButton.setBackground(new Color(108, 117, 125));

             // 使用 SwingWorker 在后台执行密码更新
             SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                 @Override
                 protected Boolean doInBackground() throws Exception {
                     try {
                         securityConfig.updatePassword(newPassword);
                         return true;
                     } catch (Exception ex) {
                         logger.error("密码修改错误：", ex);
                         throw ex;
                     }
                 }

                 @Override
                 protected void done() {
                     try {
                         if (get()) {
                             // 显示成功消息
                             JOptionPane.showMessageDialog(changePasswordDialog,
                                 "✅ 密码修改成功！\n\n新密码已生效，请妥善保管。",
                                 "修改成功",
                                 JOptionPane.INFORMATION_MESSAGE);
                             changePasswordDialog.dispose();
                             showMainMenuDialog();
                         }
                     } catch (Exception ex) {
                         // 恢复按钮状态
                         confirmButton.setText("确认修改");
                         if (newPasswordValid[0] && confirmPasswordValid[0]) {
                             confirmButton.setEnabled(true);
                             confirmButton.setBackground(new Color(74, 144, 226));
                         }

                         // 显示错误消息
                         JOptionPane.showMessageDialog(changePasswordDialog,
                             "❌ 密码修改失败\n\n错误信息：" + ex.getMessage(),
                             "修改失败",
                             JOptionPane.ERROR_MESSAGE);
                     }
                 }
             };
             worker.execute();
         });

         cancelButton.addActionListener(e -> {
             changePasswordDialog.dispose();
             showMainMenuDialog();
         });

         // 快捷键绑定
         changePasswordDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
         changePasswordDialog.getRootPane().getActionMap().put("cancel", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 changePasswordDialog.dispose();
                 showMainMenuDialog();
             }
         });

         // Enter键提交
         changePasswordDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
         changePasswordDialog.getRootPane().getActionMap().put("submit", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (confirmButton.isEnabled()) {
                     confirmButton.doClick();
                 }
             }
         });

         // 关闭事件 - 参照取消按钮逻辑修改
         changePasswordDialog.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 changePasswordDialog.dispose();
                 showMainMenuDialog();
             }
         });

         // 设置焦点并显示 - 确保对话框在最前面
         SwingUtilities.invokeLater(() -> {
             changePasswordDialog.toFront();
             changePasswordDialog.requestFocus();
             newPasswordField.requestFocus();
         });
         changePasswordDialog.setVisible(true);
     }

     // 创建密码输入组的辅助方法
     private JPanel[] createPasswordInputGroup(String labelText, String placeholder) {
         JPanel group = new JPanel();
         group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
         group.setBackground(Color.WHITE);

         // 标签 - 调整对齐方式使其与输入框文字垂直一致
         JLabel label = new JLabel(labelText);
         label.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
         label.setForeground(new Color(52, 58, 64));
         label.setAlignmentX(Component.LEFT_ALIGNMENT);
         // 设置标签容器，确保与输入框左对齐
         JPanel labelContainer = new JPanel(new BorderLayout());
         labelContainer.setBackground(Color.WHITE);
         labelContainer.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0)); // 1像素左边距对齐边框
         labelContainer.add(label, BorderLayout.WEST);
         group.add(labelContainer);
         group.add(Box.createVerticalStrut(8));

         // 输入框容器
         JPanel inputContainer = new JPanel(new BorderLayout());
         inputContainer.setBackground(Color.WHITE);
         inputContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

         // 密码输入框
         JPasswordField passwordField = new JPasswordField();
         passwordField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         passwordField.setBackground(Color.WHITE);
         passwordField.setForeground(new Color(73, 80, 87));
         passwordField.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
             BorderFactory.createEmptyBorder(12, 15, 12, 15)
         ));
         passwordField.setPreferredSize(new Dimension(0, 50));

         // 添加焦点效果
         passwordField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent e) {
                 passwordField.setBorder(BorderFactory.createCompoundBorder(
                     BorderFactory.createLineBorder(new Color(74, 144, 226), 2),
                     BorderFactory.createEmptyBorder(11, 14, 11, 14)
                 ));
             }

             @Override
             public void focusLost(FocusEvent e) {
                 passwordField.setBorder(BorderFactory.createCompoundBorder(
                     BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                     BorderFactory.createEmptyBorder(12, 15, 12, 15)
                 ));
             }
         });

         inputContainer.add(passwordField, BorderLayout.CENTER);

         // 状态图标
         JLabel statusIcon = new JLabel("");
         statusIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
         statusIcon.setPreferredSize(new Dimension(40, 50));
         statusIcon.setHorizontalAlignment(SwingConstants.CENTER);
         statusIcon.setBackground(Color.WHITE);
         statusIcon.setOpaque(true);
         inputContainer.add(statusIcon, BorderLayout.EAST);

         group.add(inputContainer);
         group.add(Box.createVerticalStrut(8));

         // 提示信息
         JLabel hintLabel = new JLabel("");
         hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
         hintLabel.setForeground(new Color(220, 53, 69));
         hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
         hintLabel.setMinimumSize(new Dimension(0, 20));
         hintLabel.setPreferredSize(new Dimension(0, 20));
         group.add(hintLabel);

         // 返回包含组件的数组：[group, passwordField, statusIcon, hintLabel]
         JPanel[] components = new JPanel[4];
         components[0] = group;
         components[1] = new JPanel(); // 占位符，实际返回passwordField
         components[2] = new JPanel(); // 占位符，实际返回statusIcon
         components[3] = new JPanel(); // 占位符，实际返回hintLabel

         // 使用putClientProperty存储组件引用
         group.putClientProperty("passwordField", passwordField);
         group.putClientProperty("statusIcon", statusIcon);
         group.putClientProperty("hintLabel", hintLabel);

         return new JPanel[]{group};
     }

     // 创建样式化按钮的辅助方法 - 优化外观设计
     private JButton createStyledButton(String text, Color bgColor, Color fgColor, int width, int height) {
         JButton button = new JButton(text) {
             @Override
             protected void paintComponent(Graphics g) {
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                 // 绘制阴影效果
                 if (isEnabled()) {
                     g2d.setColor(new Color(0, 0, 0, 20));
                     g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 8, 8);
                 }

                 // 绘制按钮背景（圆角）
                 g2d.setColor(getBackground());
                 g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);

                 // 绘制文本
                 g2d.setColor(getForeground());
                 g2d.setFont(getFont());
                 FontMetrics fm = g2d.getFontMetrics();
                 int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                 int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                 g2d.drawString(getText(), textX, textY);

                 g2d.dispose();
             }
         };

         button.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
         button.setPreferredSize(new Dimension(width, height));
         button.setBackground(bgColor);
         button.setForeground(fgColor);
         button.setFocusPainted(false);
         button.setBorderPainted(false);
         button.setContentAreaFilled(false); // 禁用默认背景绘制
         button.setCursor(new Cursor(Cursor.HAND_CURSOR));
         button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

         // 添加悬停效果
         button.addMouseListener(new MouseAdapter() {
             Color originalBg = bgColor;
             Color hoverBg = darkenColor(bgColor, 0.85f); // 调整悬停效果

             @Override
             public void mouseEntered(MouseEvent e) {
                 if (button.isEnabled()) {
                     button.setBackground(hoverBg);
                     button.repaint();
                 }
             }

             @Override
             public void mouseExited(MouseEvent e) {
                 if (button.isEnabled()) {
                     button.setBackground(originalBg);
                     button.repaint();
                 }
             }
         });

         return button;
     }

    private void showUpdateAuthDialog() {
         // 创建更新授权对话框 - 修复界面显示不完整问题
         JDialog updateAuthDialog = new JDialog(this, "更新日期授权", true);
         updateAuthDialog.setSize(450, 280); // 调整为更紧凑的对话框大小
         updateAuthDialog.setLocationRelativeTo(this);
         updateAuthDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         updateAuthDialog.getContentPane().setBackground(new Color(248, 249, 250));
         updateAuthDialog.setLayout(new BorderLayout());
         updateAuthDialog.setResizable(false); // 禁止调整大小

         // 确保对话框始终在最前面
         updateAuthDialog.setAlwaysOnTop(true);
         updateAuthDialog.toFront();

         // 顶部装饰条
         JPanel topAccent = new JPanel();
         topAccent.setBackground(new Color(0, 123, 255));
         topAccent.setPreferredSize(new Dimension(0, 4));
         updateAuthDialog.add(topAccent, BorderLayout.NORTH);

         // 主容器 - 调整内边距为合理大小
         JPanel container = new JPanel();
         container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
         container.setBackground(Color.WHITE);
         container.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25)); // 调整为合理的内边距

         // 标题区域 - 删除图标，简化布局
         JPanel titlePanel = new JPanel();
         titlePanel.setBackground(Color.WHITE);
         titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

         JLabel titleLabel = new JLabel("更新日期授权");
         titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
         titleLabel.setForeground(new Color(44, 62, 80));
         titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         titlePanel.add(titleLabel);

         container.add(titlePanel);
         container.add(Box.createVerticalStrut(20)); // 调整标题与输入区域的间距

         // 输入区域
         JPanel inputPanel = new JPanel(new BorderLayout());
         inputPanel.setBackground(Color.WHITE);

         JLabel promptLabel = new JLabel("请输入授权码：");
         promptLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
         promptLabel.setForeground(new Color(44, 62, 80));
         inputPanel.add(promptLabel, BorderLayout.NORTH);

         // 输入框和确认按钮容器 - 调整间距
         JPanel inputContainer = new JPanel(new BorderLayout());
         inputContainer.setBackground(Color.WHITE);
         inputContainer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)); // 调整上边距

         JTextField authCodeField = new JTextField();
         authCodeField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
         authCodeField.setPreferredSize(new Dimension(250, 28)); // 调整为更小的输入框
         authCodeField.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(206, 212, 218)),
             BorderFactory.createEmptyBorder(4, 8, 4, 8)
         ));
         inputContainer.add(authCodeField, BorderLayout.CENTER);

         JButton confirmBtn = new JButton("确认");
         confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
         confirmBtn.setBackground(new Color(0, 123, 255));
         confirmBtn.setForeground(Color.WHITE);
         confirmBtn.setPreferredSize(new Dimension(60, 28)); // 调整按钮大小与输入框匹配
         confirmBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
         confirmBtn.setFocusPainted(false);
         confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
         confirmBtn.setOpaque(true); // 确保背景色显示
         confirmBtn.setBorderPainted(false); // 去掉边框
         addButtonHoverEffect(confirmBtn, new Color(0, 123, 255), new Color(0, 86, 179));

         confirmBtn.addActionListener(e -> {
             String authCode = authCodeField.getText().trim();
             if (authCode.isEmpty()) {
                 JOptionPane.showMessageDialog(updateAuthDialog, "请输入授权码", "警告", JOptionPane.WARNING_MESSAGE);
                 return;
             }

             // 真正的授权码验证逻辑，包含过期校验
             try {
                 boolean isValid = validateAuthCode(authCode);
                 if (isValid) {
                     // 更新本地授权码
                     updateLocalAuthCode(authCode);

                     // 重新检查授权状态
                     checkAuthorizationStatus();

                     JOptionPane.showMessageDialog(updateAuthDialog,
                         "授权码验证成功！\n\n授权码：" + authCode + "\n\n授权状态：已更新",
                         "验证成功", JOptionPane.INFORMATION_MESSAGE);
                     updateAuthDialog.dispose();

                     // 跳转到系统功能菜单
                     showMainMenuDialog();
                 } else {
                     JOptionPane.showMessageDialog(updateAuthDialog, "授权码无效或已过期，请重新输入", "验证失败", JOptionPane.ERROR_MESSAGE);
                     authCodeField.setText("");
                 }
             } catch (Exception ex) {
                 logger.error("授权码验证失败: {}", ex.getMessage(), ex);
                 JOptionPane.showMessageDialog(updateAuthDialog, "授权码验证失败：" + ex.getMessage(), "验证错误", JOptionPane.ERROR_MESSAGE);
                 authCodeField.setText("");
             }
         });

         // 创建按钮面板，包含间距和确认按钮
         JPanel buttonPanel = new JPanel(new BorderLayout());
         buttonPanel.setBackground(Color.WHITE);
         buttonPanel.add(Box.createHorizontalStrut(8), BorderLayout.WEST);
         buttonPanel.add(confirmBtn, BorderLayout.CENTER);
         inputContainer.add(buttonPanel, BorderLayout.EAST);

         inputPanel.add(inputContainer, BorderLayout.CENTER);

         JLabel helpLabel = new JLabel("请输入授权码更新使用期限");
         helpLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10)); // 调整字体大小
         helpLabel.setForeground(new Color(108, 117, 125));
         helpLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // 调整上边距
         inputPanel.add(helpLabel, BorderLayout.SOUTH);

         container.add(inputPanel);

         updateAuthDialog.add(container, BorderLayout.CENTER);

         // 绑定回车键
         authCodeField.addActionListener(e -> confirmBtn.doClick());

         // 绑定ESC键关闭 - 根据来源决定行为
         updateAuthDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
         updateAuthDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 updateAuthDialog.dispose();
                 if (isFromExpiredDialog) {
                     showAuthExpiredDialog(); // 从过期界面来的，返回过期界面
                 } else {
                     showMainMenuDialog(); // 从菜单来的，返回主菜单
                 }
                 isFromExpiredDialog = false; // 重置标记
             }
         });

         // 绑定关闭事件 - 根据来源决定行为
         updateAuthDialog.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 updateAuthDialog.dispose();
                 if (isFromExpiredDialog) {
                     showAuthExpiredDialog(); // 从过期界面来的，返回过期界面
                 } else {
                     showMainMenuDialog(); // 从菜单来的，返回主菜单
                 }
                 isFromExpiredDialog = false; // 重置标记
             }
         });

         updateAuthDialog.setVisible(true);
         authCodeField.requestFocus();
     }

     private void showCreateAuthDialog() {
         // 创建生成授权码对话框 - 增加高度确保所有按钮显示
         JDialog createAuthDialog = new JDialog(this, "生成日期授权码", true);
         createAuthDialog.setSize(500, 700); // 增加高度从600到700
         createAuthDialog.setLocationRelativeTo(this);
         createAuthDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         createAuthDialog.getContentPane().setBackground(new Color(248, 249, 250));
         createAuthDialog.setLayout(new BorderLayout());

         // 顶部装饰条
         JPanel topAccent = new JPanel();
         topAccent.setBackground(new Color(0, 123, 255));
         topAccent.setPreferredSize(new Dimension(0, 4));
         createAuthDialog.add(topAccent, BorderLayout.NORTH);

         // 主容器
         JPanel container = new JPanel();
         container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
         container.setBackground(Color.WHITE);
         container.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

         // 标题区域 - 重新设计，删除图标
         JPanel titlePanel = new JPanel();
         titlePanel.setBackground(Color.WHITE);
         titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

         // 移除删除图标

         // 删除图标相关代码

         JLabel titleLabel = new JLabel("生成日期授权码");
         titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
         titleLabel.setForeground(new Color(44, 62, 80));
         titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         titlePanel.add(titleLabel);

         container.add(titlePanel);
         container.add(Box.createVerticalStrut(25));

         // 日期输入区域
         JPanel datePanel = new JPanel(new BorderLayout());
         datePanel.setBackground(Color.WHITE);

         JLabel datePromptLabel = new JLabel("日期（格式YYYYMMDD，例如20201231）");
         datePromptLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
         datePromptLabel.setForeground(new Color(44, 62, 80));
         datePanel.add(datePromptLabel, BorderLayout.NORTH);

         // 日期输入容器
         JPanel dateInputContainer = new JPanel(new BorderLayout());
         dateInputContainer.setBackground(Color.WHITE);
         dateInputContainer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

         JTextField dateField = new JTextField();
         dateField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
         dateField.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(206, 212, 218)),
             BorderFactory.createEmptyBorder(8, 8, 8, 8)
         ));
         dateInputContainer.add(dateField, BorderLayout.CENTER);

         JButton confirmDateBtn = new JButton("确认");
         confirmDateBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
         confirmDateBtn.setBackground(new Color(108, 117, 125));
         confirmDateBtn.setForeground(Color.WHITE);
         confirmDateBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
         confirmDateBtn.setFocusPainted(false);
         confirmDateBtn.setOpaque(true); // 确保背景色显示
         confirmDateBtn.setBorderPainted(false); // 去掉边框
         confirmDateBtn.setEnabled(false);

         // 创建按钮容器，确保按钮正确显示
         JPanel buttonWrapper = new JPanel(new BorderLayout());
         buttonWrapper.setBackground(Color.WHITE);
         buttonWrapper.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
         buttonWrapper.add(confirmDateBtn, BorderLayout.CENTER);
         dateInputContainer.add(buttonWrapper, BorderLayout.EAST);

         datePanel.add(dateInputContainer, BorderLayout.CENTER);

         // 永久授权码按钮
         JButton permanentBtn = new JButton("生成永久授权码");
         permanentBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
         permanentBtn.setBackground(new Color(23, 162, 184));
         permanentBtn.setForeground(Color.WHITE);
         permanentBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
         permanentBtn.setFocusPainted(false);
         permanentBtn.setOpaque(true); // 确保背景色显示
         permanentBtn.setBorderPainted(false); // 去掉边框
         permanentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
         permanentBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
         addButtonHoverEffect(permanentBtn, new Color(23, 162, 184), new Color(19, 132, 150));

         // 需要在这里声明变量以便在lambda中使用
         final JTextArea[] authCodeDisplayRef = new JTextArea[1];
         final JButton[] copyBtnRef = new JButton[1];

         permanentBtn.addActionListener(e -> {
             try {
                 // 使用AuthCodeGeneratorFile生成永久授权码
                 // 使用一个远期日期作为永久授权码的标识
                 String permanentDate = "99991231"; // 9999年12月31日
                 Map<String, Object> result = authGenerator.generateAuthCode(permanentDate, "default_user", true);

                 if ((Boolean) result.get("success")) {
                     String authCode = (String) result.get("auth_code");

                     authCodeDisplayRef[0].setText("永久授权码：" + authCode + "\n\n" +
                         "此授权码永久有效，请妥善保管。\n" +
                         "授权码类型：永久授权\n" +
                         "生成时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                     authCodeDisplayRef[0].setCaretPosition(0);
                     copyBtnRef[0].setEnabled(true);
                     copyBtnRef[0].setBackground(new Color(0, 123, 255)); // 启用时改为蓝色
                 } else {
                     String errorMsg = (String) result.get("error_message");
                     JOptionPane.showMessageDialog(createAuthDialog, "生成永久授权码失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (Exception ex) {
                 logger.error("生成永久授权码失败: {}", ex.getMessage(), ex);
                 JOptionPane.showMessageDialog(createAuthDialog, "生成永久授权码失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
             }
         });

         // 创建永久授权码按钮容器
         JPanel permanentBtnPanel = new JPanel();
         permanentBtnPanel.setBackground(Color.WHITE);
         permanentBtnPanel.setLayout(new BoxLayout(permanentBtnPanel, BoxLayout.Y_AXIS));
         permanentBtnPanel.add(Box.createVerticalStrut(15));
         permanentBtnPanel.add(permanentBtn);
         datePanel.add(permanentBtnPanel, BorderLayout.SOUTH);

         container.add(datePanel);
         container.add(Box.createVerticalStrut(20));

         // 分隔线
         JSeparator separator = new JSeparator();
         separator.setForeground(new Color(222, 226, 230));
         container.add(separator);
         container.add(Box.createVerticalStrut(20));

         // 授权码显示区域
         JPanel authDisplayPanel = new JPanel(new BorderLayout());
         authDisplayPanel.setBackground(Color.WHITE);

         JLabel authPromptLabel = new JLabel("授权码");
         authPromptLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
         authPromptLabel.setForeground(new Color(44, 62, 80));
         authDisplayPanel.add(authPromptLabel, BorderLayout.NORTH);

         JTextArea authCodeDisplay = new JTextArea();
         authCodeDisplay.setRows(3);
         authCodeDisplay.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12)); // 使用支持中文的字体
         authCodeDisplay.setBackground(new Color(248, 249, 250));
         authCodeDisplay.setForeground(new Color(73, 80, 87));
         authCodeDisplay.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         authCodeDisplay.setEditable(false);
         authCodeDisplay.setLineWrap(true);
         authCodeDisplay.setWrapStyleWord(true);
         authCodeDisplayRef[0] = authCodeDisplay;

         JScrollPane scrollPane = new JScrollPane(authCodeDisplay);
         scrollPane.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218)));
         scrollPane.setPreferredSize(new Dimension(0, 80));

         authDisplayPanel.add(scrollPane, BorderLayout.CENTER);

         // 复制并更新授权码按钮
         JButton copyBtn = new JButton("复制并更新授权码");
         copyBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
         copyBtn.setBackground(new Color(108, 117, 125));
         copyBtn.setForeground(Color.WHITE);
         copyBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
         copyBtn.setFocusPainted(false);
         copyBtn.setOpaque(true); // 确保背景色显示
         copyBtn.setBorderPainted(false); // 去掉边框
         copyBtn.setEnabled(false);
         copyBtnRef[0] = copyBtn;

         copyBtn.addActionListener(e -> {
             if (!authCodeDisplayRef[0].getText().trim().isEmpty()) {
                 String authCodeText = authCodeDisplayRef[0].getText();

                 // 1. 复制到剪贴板
                 StringSelection stringSelection = new StringSelection(authCodeText);
                 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                 // 2. 提取授权码并更新本地存储
                 String authCode = extractAuthCodeFromText(authCodeText);
                 if (authCode != null && !authCode.isEmpty()) {
                     // 先验证授权码是否有效（未过期）
                     boolean isValid = validateAuthCode(authCode);

                     if (!isValid) {
                         // 授权码已过期，显示提示并保持在当前界面
                         JOptionPane.showMessageDialog(createAuthDialog,
                             "此授权码已过期，请重新生成",
                             "授权码过期", JOptionPane.WARNING_MESSAGE);
                         return; // 保持在当前界面
                     }

                     // 授权码有效，更新本地存储
                     updateLocalAuthCode(authCode);

                     // 重新检查授权状态
                     checkAuthorizationStatus();

                     // 直接关闭对话框并跳转到系统功能菜单，不显示提示框
                     createAuthDialog.dispose();
                     showMainMenuDialog();
                 } else {
                     JOptionPane.showMessageDialog(createAuthDialog, "授权码已复制到剪贴板", "复制成功", JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         });

         authDisplayPanel.add(copyBtn, BorderLayout.SOUTH);
         authDisplayPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

         container.add(authDisplayPanel);

         createAuthDialog.add(container, BorderLayout.CENTER);

         // 日期输入验证和授权码生成逻辑
         dateField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
             public void changedUpdate(javax.swing.event.DocumentEvent e) { validateDate(); }
             public void removeUpdate(javax.swing.event.DocumentEvent e) { validateDate(); }
             public void insertUpdate(javax.swing.event.DocumentEvent e) { validateDate(); }

             private void validateDate() {
                 String dateText = dateField.getText().trim();
                 if (dateText.matches("\\d{8}")) {
                     confirmDateBtn.setEnabled(true);
                     confirmDateBtn.setBackground(new Color(0, 123, 255));
                 } else {
                     confirmDateBtn.setEnabled(false);
                     confirmDateBtn.setBackground(new Color(108, 117, 125));
                 }
             }
         });

         confirmDateBtn.addActionListener(e -> {
             String dateText = dateField.getText().trim();
             if (dateText.matches("\\d{8}")) {
                 try {
                     // 使用AuthCodeGeneratorFile生成复杂授权码
                     Map<String, Object> result = authGenerator.generateAuthCode(dateText, "default_user", false);

                     if ((Boolean) result.get("success")) {
                         String authCode = (String) result.get("auth_code");
                         String expiresAt = (String) result.get("expires_at");

                         authCodeDisplayRef[0].setText("日期授权码：" + authCode + "\n\n" +
                             "有效日期：" + dateText + "\n" +
                             "过期时间：" + (expiresAt != null ? expiresAt : "永久有效") + "\n" +
                             "授权码类型：日期授权\n" +
                             "生成时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                         authCodeDisplayRef[0].setCaretPosition(0);
                         copyBtnRef[0].setEnabled(true);
                         copyBtnRef[0].setBackground(new Color(0, 123, 255)); // 启用时改为蓝色
                     } else {
                         String errorMsg = (String) result.get("error_message");
                         JOptionPane.showMessageDialog(createAuthDialog, "生成授权码失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                     }
                 } catch (Exception ex) {
                     logger.error("生成授权码失败: {}", ex.getMessage(), ex);
                     JOptionPane.showMessageDialog(createAuthDialog, "生成授权码失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                 }
             }
         });

         // 绑定ESC键关闭 - 修复返回逻辑
         createAuthDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
         createAuthDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 createAuthDialog.dispose();
                 showMainMenuDialog(); // 返回主菜单
             }
         });

         // 绑定关闭事件 - 点击右上角关闭按钮时返回主菜单
         createAuthDialog.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 createAuthDialog.dispose();
                 showMainMenuDialog();
             }
         });

         createAuthDialog.setVisible(true);
     }

    private void showSystemSettingsDialog() {
         // 创建系统设置对话框
         JDialog settingsDialog = new JDialog(this, "系统设置", true);
         settingsDialog.setSize(500, 400);
         settingsDialog.setLocationRelativeTo(this);
         settingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         settingsDialog.getContentPane().setBackground(new Color(248, 249, 250));
         settingsDialog.setLayout(new BorderLayout());

         // 顶部装饰条
         JPanel topAccent = new JPanel();
         topAccent.setBackground(new Color(0, 123, 255));
         topAccent.setPreferredSize(new Dimension(0, 4));
         settingsDialog.add(topAccent, BorderLayout.NORTH);

         // 主容器
         JPanel container = new JPanel();
         container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
         container.setBackground(Color.WHITE);
         container.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

         // 标题区域
         JPanel titlePanel = new JPanel();
         titlePanel.setBackground(Color.WHITE);
         titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

         // 删除图标，直接显示标题

         JLabel titleLabel = new JLabel("系统设置");
         titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
         titleLabel.setForeground(new Color(44, 62, 80));
         titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         titlePanel.add(titleLabel);

         container.add(titlePanel);
         container.add(Box.createVerticalStrut(25));

         // 设置内容区域
         JPanel contentPanel = new JPanel();
         contentPanel.setBackground(Color.WHITE);
         contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

         // 隐藏图标设置区域
         JPanel hideIconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
         hideIconPanel.setBackground(Color.WHITE);

         JCheckBox hideIconCheckbox = new JCheckBox("是否隐藏左下角设置图标");
         hideIconCheckbox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
         hideIconCheckbox.setBackground(Color.WHITE);
         hideIconCheckbox.setForeground(new Color(73, 80, 87));
         hideIconCheckbox.setFocusPainted(false);

         // 从设置文件加载当前状态
         hideIconCheckbox.setSelected(loadHideIconSetting());

         hideIconPanel.add(hideIconCheckbox);
         contentPanel.add(hideIconPanel);

         contentPanel.add(Box.createVerticalStrut(10));

         // 备注文字区域
         JPanel notePanel = new JPanel();
         notePanel.setBackground(new Color(232, 244, 253));
         notePanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(232, 244, 253)),
             BorderFactory.createEmptyBorder(12, 15, 12, 15)
         ));
         notePanel.setLayout(new BorderLayout());

         JLabel noteLabel = new JLabel("<html>隐藏设置按钮后可以继续点击相同位置打开系统功能菜单</html>");
         noteLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
         noteLabel.setForeground(new Color(73, 80, 87));
         notePanel.add(noteLabel, BorderLayout.CENTER);

         contentPanel.add(notePanel);

         container.add(contentPanel);
         container.add(Box.createVerticalStrut(25));

         // 按钮区域 - 使用绝对定位控制按钮大小
         JPanel buttonPanel = new JPanel(null); // 使用null布局进行绝对定位
         buttonPanel.setBackground(Color.WHITE);
         buttonPanel.setPreferredSize(new Dimension(400, 40)); // 增加面板高度适应新按钮

         // 确认按钮 - 使用自定义绘制降低高度
         JButton confirmBtn = new JButton("确认") {
             @Override
             public void paintComponent(Graphics g) {
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                 // 绘制背景
                 g2d.setColor(getBackground());
                 g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

                 // 绘制文字
                 g2d.setColor(getForeground());
                 g2d.setFont(getFont());
                 FontMetrics fm = g2d.getFontMetrics();
                 int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                 int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                 g2d.drawString(getText(), textX, textY);

                 g2d.dispose();
             }

             @Override
             public Dimension getPreferredSize() {
                 return new Dimension(120, 27); // 同比例加大一半：宽度80→120，高度18→27
             }

             @Override
             public Dimension getMinimumSize() {
                 return getPreferredSize();
             }

             @Override
             public Dimension getMaximumSize() {
                 return getPreferredSize();
             }
         };
         confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 10));
         confirmBtn.setBackground(new Color(0, 123, 255));
         confirmBtn.setForeground(Color.WHITE);
         confirmBtn.setBorder(null);
         confirmBtn.setContentAreaFilled(false);
         confirmBtn.setFocusPainted(false);
         confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
         addButtonHoverEffect(confirmBtn, new Color(0, 123, 255), new Color(0, 86, 179));

         confirmBtn.addActionListener(e -> {
             // 保存设置状态
             boolean hideIcon = hideIconCheckbox.isSelected();
             saveHideIconSetting(hideIcon);

             // 应用图标可见性设置
             applyIconVisibility(hideIcon);

             // 关闭设置对话框
             settingsDialog.dispose();

             // 显示主功能菜单对话框
             showMainMenuDialog();
         });

         // 取消按钮 - 使用自定义绘制降低高度
         JButton cancelBtn = new JButton("取消") {
             @Override
             public void paintComponent(Graphics g) {
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                 // 绘制背景
                 g2d.setColor(getBackground());
                 g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

                 // 绘制文字
                 g2d.setColor(getForeground());
                 g2d.setFont(getFont());
                 FontMetrics fm = g2d.getFontMetrics();
                 int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                 int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                 g2d.drawString(getText(), textX, textY);

                 g2d.dispose();
             }

             @Override
             public Dimension getPreferredSize() {
                 return new Dimension(120, 27); // 同比例加大一半：宽度80→120，高度18→27
             }

             @Override
             public Dimension getMinimumSize() {
                 return getPreferredSize();
             }

             @Override
             public Dimension getMaximumSize() {
                 return getPreferredSize();
             }
         };
         cancelBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 10));
         cancelBtn.setBackground(new Color(108, 117, 125));
         cancelBtn.setForeground(Color.WHITE);
         cancelBtn.setBorder(null);
         cancelBtn.setContentAreaFilled(false);
         cancelBtn.setFocusPainted(false);
         cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
         addButtonHoverEffect(cancelBtn, new Color(108, 117, 125), new Color(84, 91, 97));

         cancelBtn.addActionListener(e -> {
             settingsDialog.dispose();
             showMainMenuDialog();
         });

         // 使用绝对定位设置按钮位置和大小 - 加大间距和尺寸
         confirmBtn.setBounds(30, 6, 120, 27);  // x, y, width, height - 加大尺寸和间距
         cancelBtn.setBounds(270, 6, 120, 27);  // x, y, width, height - 增加间距

         buttonPanel.add(confirmBtn);
         buttonPanel.add(cancelBtn);

         container.add(buttonPanel);

         settingsDialog.add(container, BorderLayout.CENTER);

         // 绑定ESC键关闭并返回主菜单
         settingsDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
         settingsDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 settingsDialog.dispose();
                 showMainMenuDialog();
             }
         });

         // 绑定关闭事件 - 点击右上角关闭按钮时返回主菜单
         settingsDialog.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 settingsDialog.dispose();
                 showMainMenuDialog();
             }
         });

         settingsDialog.setVisible(true);
     }

    private void checkAuthorizationStatus() {
        try {
            // 读取本地当前使用的授权码
            String currentAuthCode = getCurrentAuthCode();

            if (currentAuthCode == null || currentAuthCode.trim().isEmpty()) {
                isAuthorized = false;
                logger.info("未找到本地授权码，需要设置授权");
                showAuthExpiredDialog(); // 显示授权过期对话框
                return;
            }

            // 验证当前授权码是否有效
            boolean isValid = validateAuthCode(currentAuthCode);

            if (isValid) {
                isAuthorized = true;
                logger.info("授权验证通过，当前授权码: {}", currentAuthCode);
            } else {
                isAuthorized = false;
                logger.info("当前授权码已过期或无效: {}", currentAuthCode);
                showAuthExpiredDialog(); // 显示授权过期对话框
            }

        } catch (Exception e) {
            logger.error("授权状态检查失败: {}", e.getMessage(), e);
            isAuthorized = false;
            showAuthExpiredDialog();
        }
    }

    private void startPeriodicAuthCheck() {
        // 启动定时授权码检测（12小时 = 43200000毫秒）
        authCheckTimer = new Timer(true);
        authCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    logger.info("执行定期授权检查...");
                    checkAuthorizationStatus();
                });
            }
        }, 43200000, 43200000); // 12小时间隔
    }

    private void toggleFullscreen() {
        // 实现全屏显示或最小化到任务栏的二选一模式
        if (getExtendedState() == JFrame.ICONIFIED) {
            // 如果当前是最小化状态，则恢复到全屏
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            toFront();
            logger.info("窗口已从最小化状态恢复到全屏");
        } else {
            // 如果当前是显示状态，则最小化到任务栏
            setExtendedState(JFrame.ICONIFIED);
            logger.info("窗口已最小化到任务栏");
        }
    }

    private void toggleWindowSize() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            setSize(800, 600);
            setLocationRelativeTo(null);
        } else {
            // 使用任务栏自适应的最大化
            maximizeWithTaskbarAwareness();
        }
    }

    /**
     * 简化的最大化模式
     */
    private void maximizeWithTaskbarAwareness() {
        // 使用标准最大化
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        logger.info("窗口已设置为最大化模式");

        // 窗口大小调整后，更新齿轮按钮位置
        SwingUtilities.invokeLater(() -> {
            updateGearButtonStyle();
        });
    }

    /**
     * 打开定时器主界面
     */
    private void openTimerDialog() {
        if (timerDialog != null) {
            timerDialog.dispose();
        }

        timerDialog = new JDialog(this, "定时器", true);
        timerDialog.setSize(600, 500);
        timerDialog.setLocationRelativeTo(this);
        timerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        timerDialog.setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 顶部面板 - 当前时间显示
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel timeLabel = new JLabel();
        timeLabel.setFont(getUnicodeFont(14));
        timeLabel.setForeground(new Color(73, 80, 87));
        topPanel.add(timeLabel);

        // 创建时间更新定时器
        javax.swing.Timer timeUpdateTimer = new javax.swing.Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日-EEEE-HH:mm:ss");
            timeLabel.setText(sdf.format(new java.util.Date()));
        });
        timeUpdateTimer.start();
        // 立即更新一次时间
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日-EEEE-HH:mm:ss");
        timeLabel.setText(sdf.format(new java.util.Date()));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 定时任务列表
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 初始化定时任务列表
        if (timerListModel == null) {
            timerListModel = new DefaultListModel<>();
        }
        timerList = new JList<>(timerListModel);
        timerList.setFont(getUnicodeFont(12));
        timerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(timerList);
        scrollPane.setPreferredSize(new Dimension(550, 300));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部面板 - 按钮区域
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JButton addTimerButton = new JButton("添加定时");
        addTimerButton.setFont(getUnicodeFont(12));
        addTimerButton.setBackground(new Color(0, 123, 255));
        addTimerButton.setForeground(Color.WHITE);
        addTimerButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addTimerButton.setFocusPainted(false);
        addTimerButton.addActionListener(e -> openAddTimerDialog());

        JButton closeButton = new JButton("×");
        closeButton.setFont(getUnicodeFont(16));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> {
            timeUpdateTimer.stop();
            timerDialog.dispose();
        });

        bottomPanel.add(addTimerButton);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(closeButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        timerDialog.add(mainPanel);
        timerDialog.setVisible(true);
    }

    /**
     * 打开添加定时对话框
     */
    private void openAddTimerDialog() {
        JDialog addDialog = new JDialog(timerDialog, "添加定时", true);
        addDialog.setSize(500, 450);
        addDialog.setLocationRelativeTo(timerDialog);
        addDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addDialog.setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 顶部面板 - 当前时间显示
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel timeLabel = new JLabel();
        timeLabel.setFont(getUnicodeFont(14));
        timeLabel.setForeground(new Color(73, 80, 87));
        topPanel.add(timeLabel);

        // 创建时间更新定时器
        javax.swing.Timer timeUpdateTimer = new javax.swing.Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日-EEEE-HH:mm:ss");
            timeLabel.setText(sdf.format(new java.util.Date()));
        });
        timeUpdateTimer.start();
        // 立即更新一次时间
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日-EEEE-HH:mm:ss");
        timeLabel.setText(sdf.format(new java.util.Date()));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 表单内容
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 标题输入
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titleLabel = new JLabel("标题:");
        titleLabel.setFont(getUnicodeFont(12));
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField titleField = new JTextField();
        titleField.setFont(getUnicodeFont(11));
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(titleField, gbc);

        // 执行输入
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel executeLabel = new JLabel("执行:");
        executeLabel.setFont(getUnicodeFont(12));
        formPanel.add(executeLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField executeField = new JTextField();
        executeField.setFont(getUnicodeFont(11));
        executeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(executeField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel executeHintLabel = new JLabel("此处填写要执行按钮的第三方接口");
        executeHintLabel.setFont(getUnicodeFont(10));
        executeHintLabel.setForeground(new Color(108, 117, 125));
        formPanel.add(executeHintLabel, gbc);

        // 时间输入
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel timeInputLabel = new JLabel("时间:");
        timeInputLabel.setFont(getUnicodeFont(12));
        formPanel.add(timeInputLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField timeField = new JTextField("08:00:00");
        timeField.setFont(getUnicodeFont(11));
        timeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(timeField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel timeHintLabel = new JLabel("24小时制，例如13:29:01");
        timeHintLabel.setFont(getUnicodeFont(10));
        timeHintLabel.setForeground(new Color(108, 117, 125));
        formPanel.add(timeHintLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 继续添加重复设置和按钮...
        addRepeatSettingsAndButtons(mainPanel, addDialog, timeUpdateTimer, titleField, executeField, timeField);

        addDialog.add(mainPanel);
        addDialog.setVisible(true);
    }

    /**
     * 添加重复设置和按钮到添加定时对话框
     */
    private void addRepeatSettingsAndButtons(JPanel mainPanel, JDialog addDialog, javax.swing.Timer timeUpdateTimer,
                                           JTextField titleField, JTextField executeField, JTextField timeField) {
        // 重复设置面板
        JPanel repeatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        repeatPanel.setBackground(Color.WHITE);
        repeatPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        JLabel repeatLabel = new JLabel("重复:");
        repeatLabel.setFont(getUnicodeFont(12));
        repeatPanel.add(repeatLabel);

        // 创建7个复选框对应一周七天
        String[] dayNames = {"一", "二", "三", "四", "五", "六", "日"};
        JCheckBox[] dayCheckBoxes = new JCheckBox[7];

        for (int i = 0; i < 7; i++) {
            dayCheckBoxes[i] = new JCheckBox(dayNames[i]);
            dayCheckBoxes[i].setFont(getUnicodeFont(11));
            dayCheckBoxes[i].setBackground(Color.WHITE);
            dayCheckBoxes[i].setFocusPainted(false);

            // 自定义复选框样式，选中时显示√
            dayCheckBoxes[i].addItemListener(e -> {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    cb.setText("√" + cb.getActionCommand());
                } else {
                    cb.setText(cb.getActionCommand());
                }
            });
            dayCheckBoxes[i].setActionCommand(dayNames[i]);

            repeatPanel.add(dayCheckBoxes[i]);
            repeatPanel.add(Box.createHorizontalStrut(5));
        }

        mainPanel.add(repeatPanel, BorderLayout.SOUTH);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(getUnicodeFont(12));
        confirmButton.setBackground(new Color(40, 167, 69));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        confirmButton.setFocusPainted(false);

        confirmButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String executeInterface = executeField.getText().trim();
            String time = timeField.getText().trim();

            // 验证输入
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "请输入标题", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (executeInterface.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "请输入执行接口", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (time.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "请输入时间", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 检查标题重复
            for (int i = 0; i < timerListModel.getSize(); i++) {
                if (timerListModel.getElementAt(i).getTitle().equals(title)) {
                    JOptionPane.showMessageDialog(addDialog, "标题命名重复，请重新命名", "标题重复", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 收集重复设置
            boolean[] repeatDays = new boolean[7];
            for (int i = 0; i < 7; i++) {
                repeatDays[i] = dayCheckBoxes[i].isSelected();
            }

            // 创建定时任务
            ScheduledTask newTask = new ScheduledTask(title, executeInterface, time, repeatDays);
            timerTasks.add(newTask);
            timerListModel.addElement(newTask);

            // 关闭对话框
            timeUpdateTimer.stop();
            addDialog.dispose();
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(getUnicodeFont(12));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            timeUpdateTimer.stop();
            addDialog.dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);

        // 将按钮面板添加到重复面板下方
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.add(repeatPanel, BorderLayout.NORTH);
        bottomContainer.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
    }

    /**
     * 清理资源并退出应用程序
     */
    private void cleanupAndExit() {
        try {
            // 停止任务栏监测定时器
            if (taskbarMonitorTimer != null) {
                taskbarMonitorTimer.cancel();
                taskbarMonitorTimer = null;
                logger.info("任务栏监测定时器已停止");
            }

            // 停止授权检查定时器
            if (authCheckTimer != null) {
                authCheckTimer.cancel();
                authCheckTimer = null;
                logger.info("授权检查定时器已停止");
            }

            logger.info("应用程序正在退出...");
        } catch (Exception e) {
            logger.error("清理资源时发生错误: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    // 系统设置相关方法
    private boolean loadHideIconSetting() {
        try {
            File settingsFile = new File("settings.json");
            if (!settingsFile.exists()) {
                return false; // 默认不隐藏
            }

            FileReader reader = new FileReader(settingsFile);
            JsonObject settings = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            return settings.has("hide_icon") && settings.get("hide_icon").getAsBoolean();
        } catch (Exception e) {
            logger.error("加载设置失败", e);
            return false;
        }
    }

    private void saveHideIconSetting(boolean hideIcon) {
        try {
            JsonObject settings = new JsonObject();

            // 如果设置文件已存在，先读取现有设置
            File settingsFile = new File("settings.json");
            if (settingsFile.exists()) {
                FileReader reader = new FileReader(settingsFile);
                settings = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();
            }

            // 更新隐藏图标设置
            settings.addProperty("hide_icon", hideIcon);

            // 保存到文件
            FileWriter writer = new FileWriter(settingsFile);
            Gson gson = new Gson();
            gson.toJson(settings, writer);
            writer.close();

            logger.info("设置已保存: hide_icon = {}", hideIcon);
        } catch (Exception e) {
            logger.error("保存设置失败", e);
        }
    }

    private void applyIconVisibility(boolean hideIcon) {
        if (gearButton != null) {
            if (hideIcon) {
                // 隐藏齿轮图标，但保持点击区域可用
                gearButton.setVisible(false);

                // 创建透明的可点击区域
                JPanel invisibleClickArea = new JPanel();
                invisibleClickArea.setOpaque(false);
                invisibleClickArea.setBackground(new Color(0, 0, 0, 0)); // 完全透明
                invisibleClickArea.setBounds(10, getHeight() - 40, 30, 30); // 与齿轮按钮相同位置
                invisibleClickArea.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // 添加点击事件
                invisibleClickArea.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showMainMenuDialog(); // 直接打开系统功能菜单
                    }
                });

                // 添加到内容面板
                Container contentPane = getContentPane();
                if (contentPane != null) {
                    contentPane.add(invisibleClickArea);
                    contentPane.setComponentZOrder(invisibleClickArea, 0); // 置于最前
                }
            } else {
                // 显示齿轮图标，移除透明点击区域
                gearButton.setVisible(true);

                // 移除透明点击区域（但不移除齿轮按钮）
                Container contentPane = getContentPane();
                if (contentPane != null) {
                    Component[] components = contentPane.getComponents();
                    for (Component comp : components) {
                        // 只移除透明的点击区域，不移除齿轮按钮
                        if (comp instanceof JPanel && !((JPanel) comp).isOpaque() && comp != gearButton) {
                            contentPane.remove(comp);
                        }
                    }
                }
            }
            repaint();
            logger.info("设置图标可见性: {}", !hideIcon);
        }
    }

    private void loadAndApplySettings() {
        try {
            boolean hideIcon = loadHideIconSetting();
            applyIconVisibility(hideIcon);
            logger.info("系统设置已加载并应用");
        } catch (Exception e) {
            logger.error("加载系统设置失败", e);
        }
    }

    // 从授权码文本中提取授权码
    private String extractAuthCodeFromText(String authCodeText) {
        try {
            // 查找授权码模式：日期授权码：DATEXXXXXX 或 永久授权码：PERMXXXX
            if (authCodeText.contains("日期授权码：")) {
                int startIndex = authCodeText.indexOf("日期授权码：") + "日期授权码：".length();
                int endIndex = authCodeText.indexOf("\n", startIndex);
                if (endIndex == -1) endIndex = authCodeText.length();
                return authCodeText.substring(startIndex, endIndex).trim();
            } else if (authCodeText.contains("永久授权码：")) {
                int startIndex = authCodeText.indexOf("永久授权码：") + "永久授权码：".length();
                int endIndex = authCodeText.indexOf("\n", startIndex);
                if (endIndex == -1) endIndex = authCodeText.length();
                return authCodeText.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            logger.error("提取授权码失败: {}", e.getMessage(), e);
        }
        return null;
    }

    // 验证授权码有效性和过期状态
    private boolean validateAuthCode(String authCode) {
        try {
            // 验证授权码格式和有效性
            if (authCode == null || authCode.trim().isEmpty()) {
                return false;
            }

            // 检查是否为预设的测试授权码
            if ("AUTH2024".equalsIgnoreCase(authCode)) {
                return true; // 预设测试授权码始终有效
            }

            // 检查是否为测试用的复杂授权码格式
            if (authCode.contains("default_user") && authCode.contains("TABLET_CONTROL_2024_DEFAULT")) {
                // 这是一个测试授权码，检查日期部分
                try {
                    String dateStr = authCode.substring(0, 8);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    Date authDate = sdf.parse(dateStr);
                    Date currentDate = new Date();

                    boolean isValid = !currentDate.after(authDate);
                    logger.info("测试授权码验证: {} -> 日期: {}, 当前: {}, 有效: {}",
                        authCode, authDate, currentDate, isValid);
                    return isValid;
                } catch (Exception ex) {
                    logger.error("解析测试授权码日期失败: {}", ex.getMessage());
                    return false;
                }
            }

            // 使用AuthCodeGeneratorFile进行验证
            Map<String, Object> verifyResult = authGenerator.verifyAuthCode(authCode);
            boolean isValid = (Boolean) verifyResult.get("valid");

            if (isValid) {
                logger.info("授权码验证成功: {}", authCode);
                return true;
            } else {
                String errorCode = (String) verifyResult.get("error_code");
                String errorMessage = (String) verifyResult.get("error_message");
                logger.warn("授权码验证失败: {} - {} ({})", authCode, errorMessage, errorCode);
                return false;
            }

        } catch (Exception e) {
            logger.error("验证授权码失败: {}", e.getMessage(), e);
            return false;
        }
    }

    // 更新本地授权码存储
    private void updateLocalAuthCode(String authCode) {
        try {
            // 将当前使用的授权码存储到配置文件中
            File configFile = new File("current_auth.json");
            JsonObject authConfig = new JsonObject();
            authConfig.addProperty("current_auth_code", authCode);
            authConfig.addProperty("updated_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            try (FileWriter writer = new FileWriter(configFile)) {
                Gson gson = new Gson();
                gson.toJson(authConfig, writer);
                logger.info("本地授权码已更新: {}", authCode);
            }
        } catch (Exception e) {
            logger.error("更新本地授权码失败: {}", e.getMessage(), e);
        }
    }

    // 读取本地当前使用的授权码
    private String getCurrentAuthCode() {
        try {
            File configFile = new File("current_auth.json");
            if (!configFile.exists()) {
                return null;
            }

            try (FileReader reader = new FileReader(configFile)) {
                JsonObject authConfig = JsonParser.parseReader(reader).getAsJsonObject();
                return authConfig.get("current_auth_code").getAsString();
            }
        } catch (Exception e) {
            logger.error("读取本地授权码失败: {}", e.getMessage(), e);
            return null;
        }
    }

    // 显示授权过期对话框
    private void showAuthExpiredDialog() {
        SwingUtilities.invokeLater(() -> {
            JDialog expiredDialog = new JDialog(this, "授权已过期", true);
            expiredDialog.setSize(400, 300); // 增加高度从250到300
            expiredDialog.setLocationRelativeTo(this);
            expiredDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            expiredDialog.getContentPane().setBackground(Color.WHITE);
            expiredDialog.setLayout(new BorderLayout());

            // 主容器
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setBackground(Color.WHITE);
            container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            // 警告图标和标题
            JLabel titleLabel = new JLabel("授权已过期");
            titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
            titleLabel.setForeground(new Color(220, 53, 69));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(titleLabel);

            container.add(Box.createVerticalStrut(20));

            // 提示信息 - 使用多行标签实现居中
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setBackground(Color.WHITE);

            JLabel line1 = new JLabel("您的授权码已过期，请更新授权码后继续使用。");
            line1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            line1.setForeground(new Color(73, 80, 87));
            line1.setAlignmentX(Component.CENTER_ALIGNMENT);
            line1.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel line2 = new JLabel("点击更新授权按钮输入新的授权码。");
            line2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            line2.setForeground(new Color(73, 80, 87));
            line2.setAlignmentX(Component.CENTER_ALIGNMENT);
            line2.setHorizontalAlignment(SwingConstants.CENTER);

            messagePanel.add(line1);
            messagePanel.add(Box.createVerticalStrut(10));
            messagePanel.add(line2);

            container.add(messagePanel);

            container.add(Box.createVerticalStrut(30));

            // 按钮区域
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            buttonPanel.setBackground(Color.WHITE);

            // 更新授权按钮
            JButton updateBtn = new JButton("更新授权");
            updateBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
            updateBtn.setPreferredSize(new Dimension(120, 40));
            updateBtn.setBackground(new Color(0, 123, 255));
            updateBtn.setForeground(Color.WHITE);
            updateBtn.setFocusPainted(false);
            updateBtn.setBorderPainted(false);
            updateBtn.setOpaque(true);
            updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            updateBtn.addActionListener(e -> {
                expiredDialog.dispose();
                isFromExpiredDialog = true; // 标记来源
                showUpdateAuthDialog();
            });

            // 退出程序按钮
            JButton exitBtn = new JButton("退出程序");
            exitBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
            exitBtn.setPreferredSize(new Dimension(120, 40));
            exitBtn.setBackground(new Color(108, 117, 125));
            exitBtn.setForeground(Color.WHITE);
            exitBtn.setFocusPainted(false);
            exitBtn.setBorderPainted(false);
            exitBtn.setOpaque(true);
            exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            exitBtn.addActionListener(e -> {
                expiredDialog.dispose();
                System.exit(0);
            });

            buttonPanel.add(updateBtn);
            buttonPanel.add(exitBtn);
            container.add(buttonPanel);

            // 添加底部边距，确保按钮完整显示
            container.add(Box.createVerticalStrut(20));

            // 绑定ESC键和关闭按钮事件 - 直接退出程序
            expiredDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitProgram");
            expiredDialog.getRootPane().getActionMap().put("exitProgram", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            expiredDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0); // 直接退出程序
                }
            });

            expiredDialog.add(container, BorderLayout.CENTER);
            expiredDialog.setVisible(true);
        });
    }


    public static void main(String[] args) {
        System.out.println("=== 程序启动 ===");

        // 检查是否为测试模式
        if (args.length > 0 && "test-auth".equals(args[0])) {
            generateTestAuthCodes();
            return;
        }

        SwingUtilities.invokeLater(() -> {



            try {
                System.out.println("创建应用程序实例...");
                SwingTabletControlApp app = new SwingTabletControlApp();
                System.out.println("显示应用程序窗口...");
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    // 生成测试授权码的静态方法
    private static void generateTestAuthCodes() {
        try {
            System.out.println("=== 授权码生成测试 ===");

            AuthCodeGeneratorFile generator = new AuthCodeGeneratorFile();

            // 生成当前日期的授权码
            System.out.println("\n1. 生成当前日期授权码 (20250831):");
            Map<String, Object> result1 = generator.generateAuthCode("20250831", "default_user", false);
            printTestResult(result1);

            // 生成未来日期的授权码
            System.out.println("\n2. 生成未来日期授权码 (20250901):");
            Map<String, Object> result2 = generator.generateAuthCode("20250901", "default_user", false);
            printTestResult(result2);

            // 生成长期有效的授权码
            System.out.println("\n3. 生成长期有效授权码 (20251231):");
            Map<String, Object> result3 = generator.generateAuthCode("20251231", "default_user", false);
            printTestResult(result3);

            // 生成永久授权码
            System.out.println("\n4. 生成永久授权码:");
            Map<String, Object> result4 = generator.generateAuthCode("99991231", "default_user", true);
            printTestResult(result4);

            // 获取存储信息
            System.out.println("\n=== 存储信息 ===");
            Map<String, Object> storageInfo = generator.getStorageInfo();
            System.out.println("存储信息: " + storageInfo);

        } catch (Exception e) {
            System.err.println("生成授权码失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printTestResult(Map<String, Object> result) {
        Boolean success = (Boolean) result.get("success");
        if (success) {
            String authCode = (String) result.get("auth_code");
            String expiresAt = (String) result.get("expires_at");
            Boolean isPermanent = (Boolean) result.get("is_permanent");

            System.out.println("✅ 生成成功!");
            System.out.println("授权码: " + authCode);
            System.out.println("过期时间: " + (expiresAt != null ? expiresAt : "永久有效"));
            System.out.println("是否永久: " + isPermanent);
        } else {
            String errorCode = (String) result.get("error_code");
            String errorMessage = (String) result.get("error_message");
            System.out.println("❌ 生成失败!");
            System.out.println("错误代码: " + errorCode);
            System.out.println("错误信息: " + errorMessage);
        }
    }

    private JPanel createBasicPropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(200, 160)); // 增加三分之一高度 (120 * 1.33)

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("基础属性");
        headerLabel.setFont(getUnicodeFont(11)); // 增加字体大小
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 属性内容区域
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 标签文字
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel labelTextLabel = new JLabel("标签文字:");
        labelTextLabel.setFont(getUnicodeFont(10)); // 增加字体大小
        labelTextLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(labelTextLabel, gbc);

        gbc.gridx = 1;
        JTextField labelTextField = new JTextField("${label_text}", 12);
        labelTextField.setFont(getUnicodeFont(10)); // 增加字体大小
        labelTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        contentPanel.add(labelTextField, gbc);

        // 字体类型
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel fontTypeLabel = new JLabel("字体:");
        fontTypeLabel.setFont(getUnicodeFont(10)); // 增加字体大小
        fontTypeLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(fontTypeLabel, gbc);

        gbc.gridx = 1;
        String[] fonts = {"Microsoft YaHei", "SimSun", "Arial", "Times New Roman"};
        JComboBox<String> fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setFont(getUnicodeFont(10)); // 增加字体大小
        fontComboBox.setBackground(Color.WHITE);
        contentPanel.add(fontComboBox, gbc);

        // 字号
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel fontSizeLabel = new JLabel("字号:");
        fontSizeLabel.setFont(getUnicodeFont(10)); // 增加字体大小
        fontSizeLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(fontSizeLabel, gbc);

        gbc.gridx = 1;
        JTextField fontSizeField = new JTextField("${font_size}", 12);
        fontSizeField.setFont(getUnicodeFont(10)); // 增加字体大小
        fontSizeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        contentPanel.add(fontSizeField, gbc);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createColorPropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(200, 160)); // 增加三分之一高度 (120 * 1.33)

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("颜色属性");
        headerLabel.setFont(getUnicodeFont(11)); // 增加字体大小
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 属性内容区域
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 文字颜色
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel textColorLabel = new JLabel("文字颜色:");
        textColorLabel.setFont(getUnicodeFont(10)); // 增加字体大小
        textColorLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(textColorLabel, gbc);

        gbc.gridx = 1;
        JButton textColorBtn = new JButton();
        textColorBtn.setBackground(Color.BLACK);
        textColorBtn.setPreferredSize(new Dimension(60, 20));
        textColorBtn.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
        textColorBtn.addActionListener(e -> {
            Color color = JColorChooser.showDialog(card, "选择文字颜色", Color.BLACK);
            if (color != null) {
                textColorBtn.setBackground(color);
            }
        });
        contentPanel.add(textColorBtn, gbc);

        // 背景颜色
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel bgColorLabel = new JLabel("背景颜色:");
        bgColorLabel.setFont(getUnicodeFont(10)); // 增加字体大小
        bgColorLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(bgColorLabel, gbc);

        gbc.gridx = 1;
        JButton bgColorBtn = new JButton();
        bgColorBtn.setBackground(Color.WHITE);
        bgColorBtn.setPreferredSize(new Dimension(60, 20));
        bgColorBtn.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
        bgColorBtn.addActionListener(e -> {
            Color color = JColorChooser.showDialog(card, "选择背景颜色", Color.WHITE);
            if (color != null) {
                bgColorBtn.setBackground(color);
            }
        });
        contentPanel.add(bgColorBtn, gbc);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDisplayPropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(240, 160)); // 增加宽度以容纳滑块和按钮

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("显示属性");
        headerLabel.setFont(getUnicodeFont(13)); // 增大标题字体
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 属性内容区域
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 缩放比例滑块
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        scaleLabel = new JLabel("缩放比例: 100%");
        scaleLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        scaleLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(scaleLabel, gbc);

        gbc.gridy = 1;
        scaleSlider = new JSlider(10, 300, 100); // 10%-300%，默认100%
        scaleSlider.setBackground(new Color(248, 249, 250));
        scaleSlider.addChangeListener(e -> {
            int value = scaleSlider.getValue();
            scaleLabel.setText("缩放比例: " + value + "%");
            System.out.println("缩放比例滑块ChangeListener触发: " + value + "%");
            // 实时更新实例缩放
            updateInstanceScale(value);
        });
        contentPanel.add(scaleSlider, gbc);

        // 样式功能
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel styleLabel = new JLabel("样式:");
        styleLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        styleLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(styleLabel, gbc);

        gbc.gridx = 1;
        styleButton = new JButton("选择");
        styleButton.setFont(getUnicodeFont(12)); // 增大按钮字体
        styleButton.setBackground(new Color(248, 249, 250));
        styleButton.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
        styleButton.addActionListener(e -> {
            // 确保USERDATA/appearance文件夹存在
            File appearanceDir = new File("USERDATA/appearance");
            if (!appearanceDir.exists()) {
                boolean created = appearanceDir.mkdirs();
                if (created) {
                    System.out.println("自动创建了USERDATA/appearance文件夹");
                } else {
                    System.out.println("创建USERDATA/appearance文件夹失败");
                }
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(appearanceDir);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "gif", "bmp"));
            int result = fileChooser.showOpenDialog(card);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // 检查文件是否在USERDATA/appearance文件夹下
                try {
                    String selectedPath = selectedFile.getCanonicalPath();
                    String appearancePath = appearanceDir.getCanonicalPath();
                    if (selectedPath.startsWith(appearancePath)) {
                        styleButton.setText(selectedFile.getName());
                        System.out.println("样式按钮ActionListener触发: " + selectedFile.getName());
                        // 实时更新实例样式
                        updateInstanceStyle(selectedFile);
                    } else {
                        JOptionPane.showMessageDialog(card, "只能选择USERDATA/appearance文件夹下的图片文件",
                                                    "路径限制", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(card, "路径检查失败: " + ex.getMessage(),
                                                "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        contentPanel.add(styleButton, gbc);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private void addPageListContextMenu(JList<String> pageList) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(Color.WHITE);
        contextMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 4, 8, 4) // 增加内边距
        ));

        // 重命名菜单项
        JMenuItem renameItem = new JMenuItem("重命名 (F2)");
        renameItem.setFont(getUnicodeFont(12));
        renameItem.setForeground(new Color(73, 80, 87));
        renameItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 增加菜单项内边距
        renameItem.addActionListener(e -> showRenamePageDialog(pageList));
        contextMenu.add(renameItem);

        // 复制菜单项
        JMenuItem copyItem = new JMenuItem("复制 (Ctrl+C)");
        copyItem.setFont(getUnicodeFont(12));
        copyItem.setForeground(new Color(73, 80, 87));
        copyItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 增加菜单项内边距
        copyItem.addActionListener(e -> copySelectedPage(pageList));
        contextMenu.add(copyItem);

        // 移动菜单项
        JMenuItem moveItem = new JMenuItem("移动 (Ctrl+M)");
        moveItem.setFont(getUnicodeFont(12));
        moveItem.setForeground(new Color(73, 80, 87));
        moveItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 增加菜单项内边距
        moveItem.addActionListener(e -> showMovePageDialog(pageList));
        contextMenu.add(moveItem);

        contextMenu.addSeparator();

        // 删除菜单项
        JMenuItem deleteItem = new JMenuItem("删除 (Delete)");
        deleteItem.setFont(getUnicodeFont(12));
        deleteItem.setForeground(new Color(220, 53, 69));
        deleteItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 增加菜单项内边距
        deleteItem.addActionListener(e -> showDeleteConfirmation(pageList));
        contextMenu.add(deleteItem);

        // 右键事件现在由统一的鼠标监听器处理
    }

    /**
     * 统一处理页面列表的点击事件（左键点击和右键菜单）
     */
    private void handlePageListClick(MouseEvent e, boolean isRightClick) {
        int clickedIndex = pageList.locationToIndex(e.getPoint());

        // 检查是否点击在有效的列表项上
        if (clickedIndex >= 0 && clickedIndex < pageListModel.getSize()) {
            Rectangle cellBounds = pageList.getCellBounds(clickedIndex, clickedIndex);
            if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                // 点击在有效项上
                String selectedPage = pageListModel.getElementAt(clickedIndex);

                if (isRightClick) {
                    // 右键点击：设置选中状态并显示上下文菜单
                    pageList.setSelectedIndex(clickedIndex);
                    JPopupMenu contextMenu = createPageListContextMenu();
                    contextMenu.show(pageList, e.getX(), e.getY());
                    System.out.println("用户右键点击页面项: " + selectedPage + " (索引: " + clickedIndex + ")");
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    // 左键点击：切换到该页面
                    switchToPage(selectedPage);
                    System.out.println("用户左键点击页面项: " + selectedPage + " (索引: " + clickedIndex + ")");
                }
            } else {
                // 点击在空白区域，不执行任何操作
                System.out.println("点击页面列表空白区域，忽略操作");
            }
        } else {
            // 点击在空白区域，不执行任何操作
            System.out.println("点击页面列表空白区域，忽略操作");
        }
    }

    /**
     * 创建页面列表的上下文菜单
     */
    private JPopupMenu createPageListContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(Color.WHITE);
        contextMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 4, 8, 4) // 增加内边距
        ));

        // 重命名菜单项
        JMenuItem renameItem = new JMenuItem("重命名 (F2)");
        renameItem.setFont(getUnicodeFont(12));
        renameItem.setForeground(new Color(73, 80, 87));
        renameItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        renameItem.addActionListener(e -> showRenamePageDialog(pageList));
        contextMenu.add(renameItem);

        // 复制菜单项
        JMenuItem copyItem = new JMenuItem("复制 (Ctrl+C)");
        copyItem.setFont(getUnicodeFont(12));
        copyItem.setForeground(new Color(73, 80, 87));
        copyItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        copyItem.addActionListener(e -> copySelectedPage(pageList));
        contextMenu.add(copyItem);

        // 移动菜单项
        JMenuItem moveItem = new JMenuItem("移动 (Ctrl+M)");
        moveItem.setFont(getUnicodeFont(12));
        moveItem.setForeground(new Color(73, 80, 87));
        moveItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        moveItem.addActionListener(e -> showMovePageDialog(pageList));
        contextMenu.add(moveItem);

        contextMenu.addSeparator();

        // 删除菜单项
        JMenuItem deleteItem = new JMenuItem("删除 (Delete)");
        deleteItem.setFont(getUnicodeFont(12));
        deleteItem.setForeground(new Color(220, 53, 69));
        deleteItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        deleteItem.addActionListener(e -> showDeleteConfirmation(pageList));
        contextMenu.add(deleteItem);

        return contextMenu;
    }

    private void showRenamePageDialog(JList<String> pageList) {
        int selectedIndex = pageList.getSelectedIndex();
        if (selectedIndex == -1) {
            showErrorMessage("请先选择要重命名的页面！");
            return;
        }

        String currentPageName = pageListModel.getElementAt(selectedIndex);
        String currentName = currentPageName; // 直接使用页面名称

        // 创建重命名对话框
        JDialog renameDialog = new JDialog(this, "重命名页面", true);
        renameDialog.setSize(400, 250); // 增大高度确保输入框完整显示
        renameDialog.setLocationRelativeTo(this);
        renameDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 主容器
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题区域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        // 移除图标，只保留文字标题

        JLabel titleLabel = new JLabel("重命名页面");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(55, 65, 81));
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 输入区域
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;

        // 页面名称标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("新页面名称:");
        nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(107, 114, 128));
        inputPanel.add(nameLabel, gbc);

        // 页面名称输入框
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(currentName, 20);
        nameField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        inputPanel.add(nameField, gbc);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 取消按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(107, 114, 128));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> renameDialog.dispose());

        // 确认按钮
        JButton confirmBtn = new JButton("重命名");
        confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        confirmBtn.setBackground(new Color(59, 130, 246));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();

            if (validatePageNameForRename(newName, selectedIndex)) {
                pageListModel.setElementAt(newName, selectedIndex);
                updateCurrentPageLabel(newName);
                renameDialog.dispose();
            }
        });

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        renameDialog.add(mainPanel);

        // 设置焦点到输入框并选中所有文本
        SwingUtilities.invokeLater(() -> {
            nameField.requestFocus();
            nameField.selectAll();
        });

        renameDialog.setVisible(true);
    }

    private void copySelectedPage(JList<String> pageList) {
        int selectedIndex = pageList.getSelectedIndex();
        if (selectedIndex == -1) {
            showErrorMessage("请先选择要复制的页面！");
            return;
        }

        String originalPageName = pageListModel.getElementAt(selectedIndex);

        // 生成副本名称
        String copyName = generateCopyName(originalPageName);

        // 复制页面内容
        copyPageContent(originalPageName, copyName);

        // 添加到列表
        pageListModel.addElement(copyName);

        // 切换到新复制的页面（这会自动同步页面列表选中状态）
        switchToPage(copyName);

        // 检查复制的页面名称是否超过20个字符
        if (copyName.length() > 20) {
            // 显示错误提示并自动打开重命名对话框
            SwingUtilities.invokeLater(() -> {
                showErrorMessage("命名超过最大位数20位，请重新命名");
                // 错误提示关闭后，自动打开重命名对话框
                SwingUtilities.invokeLater(() -> {
                    showRenamePageDialog(pageList);
                });
            });
        }
    }

    private void showMovePageDialog(JList<String> pageList) {
        int selectedIndex = pageList.getSelectedIndex();
        if (selectedIndex == -1) {
            showErrorMessage("请先选择要移动的页面！");
            return;
        }

        String currentPageName = pageListModel.getElementAt(selectedIndex);

        // 创建移动页面对话框
        JDialog moveDialog = new JDialog(this, "移动页面", true);
        moveDialog.setSize(450, 350);
        moveDialog.setLocationRelativeTo(this);
        moveDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 主容器
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题区域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("移动页面到指定位置");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(55, 65, 81));
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 内容区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 当前页面信息
        JLabel currentPageLabel = new JLabel("当前选中页面: " + currentPageName);
        currentPageLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        currentPageLabel.setForeground(new Color(107, 114, 128));
        contentPanel.add(currentPageLabel, BorderLayout.NORTH);

        // 目标位置选择
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JLabel instructionLabel = new JLabel("选择目标位置（将移动到选中页面的上方）:");
        instructionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        instructionLabel.setForeground(new Color(107, 114, 128));
        selectionPanel.add(instructionLabel, BorderLayout.NORTH);

        // 创建页面列表（排除当前页面）
        DefaultListModel<String> moveListModel = new DefaultListModel<>();
        for (int i = 0; i < pageListModel.getSize(); i++) {
            if (i != selectedIndex) {
                moveListModel.addElement(pageListModel.getElementAt(i));
            }
        }

        JList<String> moveList = new JList<>(moveListModel);
        moveList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moveList.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));

        JScrollPane scrollPane = new JScrollPane(moveList);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        selectionPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(selectionPanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 确认按钮
        JButton confirmBtn = new JButton("移动");
        confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        confirmBtn.setBackground(new Color(59, 130, 246));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            int targetIndex = moveList.getSelectedIndex();
            if (targetIndex == -1) {
                showErrorMessage("请选择目标位置！");
                return;
            }

            // 执行移动操作
            boolean moveSuccess = performPageMove(selectedIndex, targetIndex, currentPageName);
            if (moveSuccess) {
                moveDialog.dispose(); // 只有移动成功时才关闭对话框
            }
        });

        // 取消按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(107, 114, 128));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> moveDialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        moveDialog.add(mainPanel);

        // 绑定ESC键关闭对话框
        moveDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        moveDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDialog.dispose();
            }
        });

        moveDialog.setVisible(true);
    }

    private boolean performPageMove(int sourceIndex, int targetIndex, String pageName) {
        // 计算实际的目标位置（因为移动列表中排除了当前页面）
        int actualTargetIndex = targetIndex;
        if (targetIndex >= sourceIndex) {
            actualTargetIndex = targetIndex + 1; // 如果目标位置在源位置之后，需要+1
        }

        // 检查是否真的需要移动
        if (actualTargetIndex == sourceIndex || actualTargetIndex == sourceIndex + 1) {
            showErrorMessage("目标位置与当前位置相同，无需移动");
            return false; // 返回false表示移动失败，不关闭对话框
        }

        // 从原位置移除页面
        pageListModel.removeElementAt(sourceIndex);

        // 调整目标位置（因为移除了一个元素，后面的索引会前移）
        int finalTargetIndex = actualTargetIndex;
        if (actualTargetIndex > sourceIndex) {
            finalTargetIndex = actualTargetIndex - 1;
        }

        // 在新位置插入页面
        pageListModel.insertElementAt(pageName, finalTargetIndex);

        // 更新选中项到新位置
        pageList.setSelectedIndex(finalTargetIndex);
        updateCurrentPageLabel(pageName);

        showSuccessMessage("页面移动成功！");
        return true; // 返回true表示移动成功
    }

    private void showDeleteConfirmation(JList<String> pageList) {
        int selectedIndex = pageList.getSelectedIndex();
        if (selectedIndex == -1) {
            showErrorMessage("请先选择要删除的页面！");
            return;
        }

        String selectedPageName = pageListModel.getElementAt(selectedIndex);
        String pageName = selectedPageName; // 直接使用页面名称，无需去除图标

        // 创建确认对话框
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要删除页面 \"" + pageName + "\" 吗？\n\n此操作不可撤销！",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            // 执行完整的页面删除操作
            deletePageCompletely(pageName, selectedIndex);
        }
    }

    /**
     * 完整删除页面（包括画布内容和数据）
     */
    private void deletePageCompletely(String pageName, int selectedIndex) {
        System.out.println("=== 开始删除页面: " + pageName + " ===");

        // 1. 如果删除的是当前正在编辑的页面，先保存当前页面内容
        if (pageName.equals(currentPageName)) {
            System.out.println("删除的是当前页面，先保存内容");
            saveCurrentPageContent();
        }

        // 2. 清空画布（如果删除的是当前页面）
        if (pageName.equals(currentPageName) && editCanvas != null) {
            System.out.println("清空当前页面的画布内容，组件数量: " + editCanvas.getComponentCount());
            editCanvas.removeAll();
            editCanvas.revalidate();
            editCanvas.repaint();
            selectedInstance = null; // 清除选中的实例
            resetPropertyPanel(); // 重置属性面板
        }

        // 3. 从数据结构中删除页面内容
        if (pageContents.containsKey(pageName)) {
            java.util.List<Component> removedComponents = pageContents.remove(pageName);
            System.out.println("从pageContents中删除页面: " + pageName + ", 移除组件数量: " +
                             (removedComponents != null ? removedComponents.size() : 0));
        }

        // 4. 从背景信息中删除页面背景
        if (pageBackgrounds.containsKey(pageName)) {
            String removedBackground = pageBackgrounds.remove(pageName);
            System.out.println("从pageBackgrounds中删除页面背景: " + pageName + " -> " + removedBackground);
        }

        // 4. 从页面列表模型中删除
        pageListModel.removeElementAt(selectedIndex);
        System.out.println("从页面列表中删除: " + pageName);

        // 5. 调整选中项
        int newSelectedIndex;
        if (selectedIndex >= pageListModel.getSize()) {
            newSelectedIndex = pageListModel.getSize() - 1;
        } else {
            newSelectedIndex = selectedIndex;
        }

        // 6. 更新当前页面
        if (newSelectedIndex >= 0) {
            String newSelectedPage = pageListModel.getElementAt(newSelectedIndex);
            pageList.setSelectedIndex(newSelectedIndex);
            updateCurrentPageLabel(newSelectedPage);

            // 如果删除的是当前页面，需要加载新的当前页面
            if (pageName.equals(currentPageName)) {
                currentPageName = newSelectedPage;
                loadPageContent(newSelectedPage);
                System.out.println("切换到新页面: " + newSelectedPage);
            }
        } else {
            // 没有页面了
            updateCurrentPageLabel("无");
            currentPageName = null;
            if (editCanvas != null) {
                editCanvas.removeAll();
                editCanvas.revalidate();
                editCanvas.repaint();
            }
            System.out.println("所有页面已删除");
        }

        // 刷新目标页面下拉框，排除当前页面
        refreshTargetPageComboBox(currentPageName);

        System.out.println("=== 页面删除完成: " + pageName + " ===");
        showSuccessMessage("页面 \"" + pageName + "\" 删除成功！");
    }

    private void showMultiDeleteDialog() {

        // 创建批量删除对话框
        JDialog multiDeleteDialog = new JDialog(this, "批量删除页面", true);
        multiDeleteDialog.setSize(500, 450);
        multiDeleteDialog.setLocationRelativeTo(this);
        multiDeleteDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题区域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("批量删除页面");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(220, 53, 69));
        titlePanel.add(titleLabel);

        mainContainer.add(titlePanel, BorderLayout.NORTH);

        // 内容区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 说明文字
        JLabel instructionLabel = new JLabel("请选择要删除的页面:");
        instructionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        instructionLabel.setForeground(new Color(107, 114, 128));
        contentPanel.add(instructionLabel, BorderLayout.NORTH);

        // 页面选择区域
        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.setBackground(Color.WHITE);
        selectPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // 全选复选框
        JCheckBox selectAllCheckBox = new JCheckBox("全选");
        selectAllCheckBox.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        selectAllCheckBox.setBackground(Color.WHITE);
        selectAllCheckBox.setForeground(new Color(59, 130, 246));
        selectPanel.add(selectAllCheckBox, BorderLayout.NORTH);

        // 页面复选框列表
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setBackground(Color.WHITE);

        // 创建页面复选框列表
        java.util.List<JCheckBox> pageCheckBoxes = new java.util.ArrayList<>();
        for (int i = 0; i < pageListModel.getSize(); i++) {
            String pageName = pageListModel.getElementAt(i);
            JCheckBox checkBox = new JCheckBox(pageName);
            checkBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            checkBox.setBackground(Color.WHITE);
            checkBox.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            pageCheckBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }

        // 全选复选框事件
        selectAllCheckBox.addActionListener(e -> {
            boolean selected = selectAllCheckBox.isSelected();
            for (JCheckBox checkBox : pageCheckBoxes) {
                checkBox.setSelected(selected);
            }
        });

        // 滚动面板
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setBackground(Color.WHITE);
        selectPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(selectPanel, BorderLayout.CENTER);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 确认删除按钮
        JButton confirmBtn = new JButton("确认删除");
        confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        confirmBtn.setBackground(new Color(220, 53, 69));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            performBatchDelete(pageCheckBoxes, multiDeleteDialog);
        });

        // 取消按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> multiDeleteDialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        multiDeleteDialog.add(mainContainer);

        // 绑定ESC键关闭对话框
        multiDeleteDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        multiDeleteDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                multiDeleteDialog.dispose();
            }
        });

        multiDeleteDialog.setVisible(true);
    }

    private void performBatchDelete(java.util.List<JCheckBox> pageCheckBoxes, JDialog parentDialog) {
        // 收集选中的页面
        java.util.List<String> selectedPages = new java.util.ArrayList<>();
        for (JCheckBox checkBox : pageCheckBoxes) {
            if (checkBox.isSelected()) {
                selectedPages.add(checkBox.getText());
            }
        }

        // 检查是否有选中的页面
        if (selectedPages.isEmpty()) {
            showErrorMessage("请至少选择一个要删除的页面！");
            return;
        }

        // 显示二次确认对话框
        showBatchDeleteConfirmation(selectedPages, parentDialog);
    }

    private void showBatchDeleteConfirmation(java.util.List<String> selectedPages, JDialog parentDialog) {
        // 创建确认对话框
        JDialog confirmDialog = new JDialog(parentDialog, "确认批量删除", true);
        confirmDialog.setSize(450, 350);
        confirmDialog.setLocationRelativeTo(parentDialog);
        confirmDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 主容器
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题区域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("确认删除操作");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(220, 53, 69));
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 内容区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 警告信息
        JLabel warningLabel = new JLabel("即将删除以下 " + selectedPages.size() + " 个页面，此操作不可撤销！");
        warningLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        warningLabel.setForeground(new Color(220, 53, 69));
        contentPanel.add(warningLabel, BorderLayout.NORTH);

        // 页面列表
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        for (String pageName : selectedPages) {
            JLabel pageLabel = new JLabel("• " + pageName);
            pageLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            pageLabel.setForeground(new Color(107, 114, 128));
            pageLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            listPanel.add(pageLabel);
        }

        // 滚动面板（如果页面太多）
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setBackground(Color.WHITE);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 确认删除按钮
        JButton confirmBtn = new JButton("确认删除");
        confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        confirmBtn.setBackground(new Color(220, 53, 69));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            executeBatchDelete(selectedPages);
            confirmDialog.dispose();
            parentDialog.dispose();
        });

        // 取消按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        confirmDialog.add(mainPanel);

        // 绑定ESC键关闭对话框
        confirmDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        confirmDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmDialog.dispose();
            }
        });

        confirmDialog.setVisible(true);
    }

    private void executeBatchDelete(java.util.List<String> selectedPages) {
        System.out.println("=== 开始批量删除页面: " + selectedPages + " ===");

        // 先保存当前页面内容
        if (currentPageName != null) {
            saveCurrentPageContent();
        }

        // 从后往前删除，避免索引变化问题
        for (int i = pageListModel.getSize() - 1; i >= 0; i--) {
            String pageName = pageListModel.getElementAt(i);
            if (selectedPages.contains(pageName)) {
                System.out.println("批量删除页面: " + pageName);

                // 从数据结构中删除页面内容
                if (pageContents.containsKey(pageName)) {
                    java.util.List<Component> removedComponents = pageContents.remove(pageName);
                    System.out.println("从pageContents中删除页面: " + pageName + ", 移除组件数量: " +
                                     (removedComponents != null ? removedComponents.size() : 0));
                }

                // 从背景信息中删除页面背景
                if (pageBackgrounds.containsKey(pageName)) {
                    String removedBackground = pageBackgrounds.remove(pageName);
                    System.out.println("从pageBackgrounds中删除页面背景: " + pageName + " -> " + removedBackground);
                }

                // 从页面列表模型中删除
                pageListModel.removeElementAt(i);
            }
        }

        // 清空画布（如果当前页面被删除了）
        if (selectedPages.contains(currentPageName)) {
            if (editCanvas != null) {
                System.out.println("当前页面被删除，清空画布");
                editCanvas.removeAll();
                editCanvas.revalidate();
                editCanvas.repaint();
                selectedInstance = null;
                resetPropertyPanel();
            }
        }

        // 更新选中项
        if (pageListModel.getSize() > 0) {
            int newSelectedIndex = Math.min(pageList.getSelectedIndex(), pageListModel.getSize() - 1);
            if (newSelectedIndex < 0) newSelectedIndex = 0;
            pageList.setSelectedIndex(newSelectedIndex);
            String newSelectedPage = pageListModel.getElementAt(newSelectedIndex);
            updateCurrentPageLabel(newSelectedPage);

            // 如果当前页面被删除，切换到新页面
            if (selectedPages.contains(currentPageName)) {
                currentPageName = newSelectedPage;
                loadPageContent(newSelectedPage);
                System.out.println("切换到新页面: " + newSelectedPage);
            }
        } else {
            updateCurrentPageLabel("无");
            currentPageName = null;
            if (editCanvas != null) {
                editCanvas.removeAll();
                editCanvas.revalidate();
                editCanvas.repaint();
            }
            System.out.println("所有页面已删除");
        }

        // 刷新目标页面下拉框，排除当前页面
        refreshTargetPageComboBox(currentPageName);

        System.out.println("=== 批量删除完成 ===");
        showSuccessMessage("成功删除 " + selectedPages.size() + " 个页面！");
    }

    private void showAddPageDialog() {
        // 创建现代化添加页面对话框
        JDialog addPageDialog = new JDialog(this, "新建页面", true);
        addPageDialog.setSize(400, 250);
        addPageDialog.setLocationRelativeTo(this);
        addPageDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 主容器
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // 标题区域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        // 移除图标，只保留文字标题

        JLabel titleLabel = new JLabel("新建页面");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setForeground(new Color(55, 65, 81));
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 输入区域
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;

        // 页面名称标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("页面名称:");
        nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(107, 114, 128));
        inputPanel.add(nameLabel, gbc);

        // 页面名称输入框
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField("", 20);
        nameField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        inputPanel.add(nameField, gbc);

        // 移除图标选择功能

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // 取消按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(107, 114, 128));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> addPageDialog.dispose());

        // 确认按钮
        JButton confirmBtn = new JButton("添加");
        confirmBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        confirmBtn.setBackground(new Color(59, 130, 246));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            String pageName = nameField.getText().trim();

            if (validatePageName(pageName)) {
                pageListModel.addElement(pageName);

                // 为新页面创建空的内容列表
                pageContents.put(pageName, new java.util.ArrayList<>());
                System.out.println("新建页面: " + pageName + ", 创建空内容列表");

                // 切换到新页面（这会自动同步页面列表选中状态）
                switchToPage(pageName);

                addPageDialog.dispose();
                showSuccessMessage("页面 \"" + pageName + "\" 创建成功！");
            }
        });

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        addPageDialog.add(mainPanel);

        // 绑定ESC键关闭对话框
        addPageDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        addPageDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPageDialog.dispose();
            }
        });

        // 设置焦点到输入框
        SwingUtilities.invokeLater(() -> nameField.requestFocus());

        addPageDialog.setVisible(true);
    }

    private boolean validatePageName(String pageName) {
        // 检查页面名称是否为空
        if (pageName.isEmpty()) {
            showErrorMessage("页面名称不能为空！");
            return false;
        }

        // 检查页面名称长度
        if (pageName.length() > 20) {
            showErrorMessage("页面名称不能超过20个字符！");
            return false;
        }

        // 检查是否包含特殊字符
        if (!pageName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$")) {
            showErrorMessage("页面名称只能包含中文、英文、数字和空格！");
            return false;
        }

        // 检查是否重名
        for (int i = 0; i < pageListModel.getSize(); i++) {
            String existingPage = pageListModel.getElementAt(i);
            if (existingPage.equals(pageName)) {
                showErrorMessage("名称已存在，请重新命名！");
                return false;
            }
        }

        return true;
    }

    private void updateCurrentPageLabel(String pageName) {
        if (currentPageLabel != null) {
            currentPageLabel.setText("当前页面: " + pageName);
        }
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this,
            message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
            message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private boolean validatePageNameForRename(String pageName, int excludeIndex) {
        // 检查页面名称是否为空
        if (pageName.isEmpty()) {
            showErrorMessage("页面名称不能为空！");
            return false;
        }

        // 检查是否与当前页面名称一致
        String currentPageName = pageListModel.getElementAt(excludeIndex);
        if (pageName.equals(currentPageName)) {
            showErrorMessage("与当前页面命名一致，请重新命名");
            return false;
        }

        // 检查页面名称长度
        if (pageName.length() > 20) {
            showErrorMessage("超过最大位数，请限制在20位之内");
            return false;
        }

        // 检查是否包含特殊字符
        if (!pageName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$")) {
            showErrorMessage("页面名称只能包含中文、英文、数字和空格！");
            return false;
        }

        // 检查是否重名（排除当前页面）
        for (int i = 0; i < pageListModel.getSize(); i++) {
            if (i == excludeIndex) continue; // 跳过当前页面

            String existingPage = pageListModel.getElementAt(i);
            if (existingPage.equals(pageName)) {
                showErrorMessage("页面名称已存在，请使用其他名称！");
                return false;
            }
        }

        return true;
    }

    private String generateCopyName(String originalName) {
        String baseName = originalName;
        int copyNumber = 1;

        // 如果原名称已经是副本，提取基础名称
        if (originalName.matches(".*副本\\d*$")) {
            int lastIndex = originalName.lastIndexOf("副本");
            baseName = originalName.substring(0, lastIndex).trim();
        }

        // 生成不重复的副本名称
        String copyName;
        do {
            if (copyNumber == 1) {
                copyName = baseName + "副本";
            } else {
                copyName = baseName + "副本" + copyNumber;
            }
            copyNumber++;
        } while (isPageNameExists(copyName));

        return copyName;
    }

    private boolean isPageNameExists(String pageName) {
        for (int i = 0; i < pageListModel.getSize(); i++) {
            String existingPage = pageListModel.getElementAt(i);
            if (existingPage.equals(pageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取标准字体 - 确保文字正常显示
     */
    private static Font getUnicodeFont(int size) {
        // 使用系统默认字体，确保最佳兼容性
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    /**
     * 为页面列表添加快捷键绑定
     */
    private void addPageListKeyBindings(JList<String> pageList) {
        // 获取输入映射和动作映射
        InputMap inputMap = pageList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = pageList.getActionMap();

        // F2键 - 重命名
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "rename");
        actionMap.put("rename", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRenamePageDialog(pageList);
            }
        });

        // Ctrl+C键 - 复制
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedPage(pageList);
            }
        });

        // Ctrl+M键 - 移动
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "move");
        actionMap.put("move", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMovePageDialog(pageList);
            }
        });

        // Delete键 - 删除
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDeleteConfirmation(pageList);
            }
        });
    }

    /**
     * 分辨率切换时更新现有实例的位置
     */
    private void updateInstancesForResolutionChange(String oldResolution, String newResolution) {
        if (editCanvas == null || oldResolution == null || newResolution == null || oldResolution.equals(newResolution)) {
            return;
        }

        System.out.println("=== 分辨率切换：更新实例位置 ===");
        System.out.println("从 " + oldResolution + " 切换到 " + newResolution);

        // 解析旧分辨率和新分辨率
        int[] oldRes = parseResolution(oldResolution);
        int[] newRes = parseResolution(newResolution);

        if (oldRes == null || newRes == null) {
            System.out.println("分辨率解析失败，跳过实例位置更新");
            return;
        }

        int oldWidth = oldRes[0], oldHeight = oldRes[1];
        int newWidth = newRes[0], newHeight = newRes[1];

        // 获取当前画布中的所有实例
        Component[] components = editCanvas.getComponents();
        int updatedCount = 0;

        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel instance = (JPanel) comp;

                // 获取实例的编辑画布坐标
                Integer editCanvasX = (Integer) instance.getClientProperty("editCanvasX");
                Integer editCanvasY = (Integer) instance.getClientProperty("editCanvasY");
                Integer editCanvasWidth = (Integer) instance.getClientProperty("editCanvasWidth");
                Integer editCanvasHeight = (Integer) instance.getClientProperty("editCanvasHeight");

                if (editCanvasX != null && editCanvasY != null && editCanvasWidth != null && editCanvasHeight != null) {
                    // 使用保存的编辑画布坐标，基于新分辨率重新计算位置
                    updateInstancePositionForNewResolution(instance, editCanvasX, editCanvasY,
                                                          editCanvasWidth, editCanvasHeight,
                                                          oldWidth, oldHeight, newWidth, newHeight);
                    updatedCount++;
                } else {
                    // 如果没有编辑画布坐标，尝试使用相对位置
                    updateInstancePositionUsingRelativePosition(instance, oldWidth, oldHeight, newWidth, newHeight);
                    updatedCount++;
                }
            }
        }

        System.out.println("已更新 " + updatedCount + " 个实例的位置");

        // 刷新画布
        editCanvas.revalidate();
        editCanvas.repaint();

        System.out.println("=== 实例位置更新完成 ===");
    }

    /**
     * 解析分辨率字符串
     */
    private int[] parseResolution(String resolution) {
        if (resolution == null) return null;

        String[] parts = resolution.split("x");
        if (parts.length == 2) {
            try {
                return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
            } catch (NumberFormatException e) {
                System.out.println("解析分辨率失败: " + resolution);
            }
        }
        return null;
    }

    /**
     * 基于编辑画布坐标更新实例位置（用于新的坐标格式）
     */
    private void updateInstancePositionForNewResolution(JPanel instance, int editCanvasX, int editCanvasY,
                                                       int editCanvasWidth, int editCanvasHeight,
                                                       int oldResWidth, int oldResHeight,
                                                       int newResWidth, int newResHeight) {

        System.out.println("=== 分辨率切换位置更新 ===");
        System.out.println("原始编辑画布坐标: (" + editCanvasX + "," + editCanvasY + "," + editCanvasWidth + "," + editCanvasHeight + ")");
        System.out.println("分辨率变化: " + oldResWidth + "x" + oldResHeight + " -> " + newResWidth + "x" + newResHeight);

        // 获取当前画布的实际显示尺寸
        int currentCanvasWidth = editCanvas.getWidth();
        int currentCanvasHeight = editCanvas.getHeight();

        System.out.println("当前画布显示尺寸: " + currentCanvasWidth + "x" + currentCanvasHeight);

        // 基于“保存时的画布显示尺寸”来换算（优先使用画布显示尺寸，回退到旧分辨率）
        int baseW = oldResWidth; // 默认使用旧分辨率
        int baseH = oldResHeight;
        Integer savedCanvasDisplayWidth = (Integer) instance.getClientProperty("canvasDisplayWidth");
        Integer savedCanvasDisplayHeight = (Integer) instance.getClientProperty("canvasDisplayHeight");
        if (savedCanvasDisplayWidth != null && savedCanvasDisplayWidth > 0) baseW = savedCanvasDisplayWidth;
        if (savedCanvasDisplayHeight != null && savedCanvasDisplayHeight > 0) baseH = savedCanvasDisplayHeight;

        // 第一步：计算在“保存基准（画布显示尺寸或旧分辨率）”下的相对位置
        double relativeX = baseW > 0 ? (double) editCanvasX / baseW : 0.0;
        double relativeY = baseH > 0 ? (double) editCanvasY / baseH : 0.0;
        double relativeWidth = baseW > 0 ? (double) editCanvasWidth / baseW : 0.0;
        double relativeHeight = baseH > 0 ? (double) editCanvasHeight / baseH : 0.0;

        System.out.println("相对位置(基于" + (baseW == oldResWidth ? "旧分辨率" : "保存时画布显示尺寸") + ") : X=" + String.format("%.4f", relativeX) +
                         ", Y=" + String.format("%.4f", relativeY) +
                         ", W=" + String.format("%.4f", relativeWidth) +
                         ", H=" + String.format("%.4f", relativeHeight));

        // 第二步：基于新分辨率计算新的编辑画布坐标
        int newEditCanvasX = (int) Math.round(relativeX * newResWidth);
        int newEditCanvasY = (int) Math.round(relativeY * newResHeight);
        int newEditCanvasWidth = (int) Math.round(relativeWidth * newResWidth);
        int newEditCanvasHeight = (int) Math.round(relativeHeight * newResHeight);

        System.out.println("新编辑画布坐标: (" + newEditCanvasX + "," + newEditCanvasY + "," + newEditCanvasWidth + "," + newEditCanvasHeight + ")");

        // 第三步：将新的编辑画布坐标转换为当前画布的显示坐标
        if (currentCanvasWidth > 0 && currentCanvasHeight > 0) {
            double scaleX = (double) currentCanvasWidth / newResWidth;
            double scaleY = (double) currentCanvasHeight / newResHeight;

            int newDisplayX = (int) Math.round(newEditCanvasX * scaleX);
            int newDisplayY = (int) Math.round(newEditCanvasY * scaleY);
            int newDisplayWidth = (int) Math.round(newEditCanvasWidth * scaleX);
            int newDisplayHeight = (int) Math.round(newEditCanvasHeight * scaleY);

            System.out.println("画布缩放比例: X=" + String.format("%.4f", scaleX) + ", Y=" + String.format("%.4f", scaleY));
            System.out.println("最终显示坐标: (" + newDisplayX + "," + newDisplayY + "," + newDisplayWidth + "," + newDisplayHeight + ")");

            // 更新实例位置
            instance.setBounds(newDisplayX, newDisplayY, newDisplayWidth, newDisplayHeight);
        } else {
            // 如果画布尺寸无效，直接使用新的编辑画布坐标
            instance.setBounds(newEditCanvasX, newEditCanvasY, newEditCanvasWidth, newEditCanvasHeight);
            System.out.println("画布尺寸无效，直接使用编辑画布坐标");
        }

        // 更新保存的编辑画布坐标
        instance.putClientProperty("editCanvasX", newEditCanvasX);
        instance.putClientProperty("editCanvasY", newEditCanvasY);
        instance.putClientProperty("editCanvasWidth", newEditCanvasWidth);
        instance.putClientProperty("editCanvasHeight", newEditCanvasHeight);

        System.out.println("=== 位置更新完成 ===");
    }

    /**
     * 基于相对位置更新实例位置（用于旧的相对位置格式）
     */
    private void updateInstancePositionUsingRelativePosition(JPanel instance, int oldResWidth, int oldResHeight,
                                                           int newResWidth, int newResHeight) {

        com.feixiang.tabletcontrol.model.RelativePosition relativePos =
            (com.feixiang.tabletcontrol.model.RelativePosition) instance.getClientProperty("relativePosition");

        if (relativePos != null) {
            // 基于新分辨率计算新的画布坐标
            int currentCanvasWidth = editCanvas.getWidth();
            int currentCanvasHeight = editCanvas.getHeight();

            double canvasScaleX = (double) currentCanvasWidth / newResWidth;
            double canvasScaleY = (double) currentCanvasHeight / newResHeight;

            // 先转换为新分辨率的绝对坐标，再转换为画布坐标
            com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition newResPos =
                relativePos.toAbsolute(newResWidth, newResHeight);

            int newDisplayX = (int) Math.round(newResPos.x * canvasScaleX);
            int newDisplayY = (int) Math.round(newResPos.y * canvasScaleY);
            int newDisplayWidth = (int) Math.round(newResPos.width * canvasScaleX);
            int newDisplayHeight = (int) Math.round(newResPos.height * canvasScaleY);

            // 更新实例位置
            instance.setBounds(newDisplayX, newDisplayY, newDisplayWidth, newDisplayHeight);

            System.out.println("实例位置更新（相对位置）: " + relativePos +
                             " -> 显示坐标 (" + newDisplayX + "," + newDisplayY + "," + newDisplayWidth + "," + newDisplayHeight + ")");
        }
    }

    /**
     * 更新画布分辨率
     */
    private void updateCanvasResolution() {
        if (resolutionComboBox == null || editCanvas == null) {
            return;
        }

        String selectedResolution = (String) resolutionComboBox.getSelectedItem();
        if (selectedResolution != null) {
            // 解析分辨率字符串
            String[] parts = selectedResolution.split("x");
            if (parts.length == 2) {
                try {
                    int width = Integer.parseInt(parts[0]);
                    int height = Integer.parseInt(parts[1]);

                    // 按比例缩放到合适的显示尺寸（最大600x400）
                    double scale = Math.min(600.0 / width, 400.0 / height);
                    int displayWidth = (int) (width * scale);
                    int displayHeight = (int) (height * scale);

                    // 更新画布尺寸
                    editCanvas.setPreferredSize(new Dimension(displayWidth, displayHeight));
                    editCanvas.setMinimumSize(new Dimension(displayWidth, displayHeight));
                    editCanvas.setMaximumSize(new Dimension(displayWidth, displayHeight));

                    // 保持边框为空，不占用内部空间
                    editCanvas.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

                    // 更新画布包装器的尺寸
                    updateCanvasWrapperSize(displayWidth, displayHeight);

                    editCanvas.revalidate();
                    editCanvas.repaint();
                } catch (NumberFormatException e) {
                    // 解析失败时使用默认尺寸
                    editCanvas.setPreferredSize(new Dimension(600, 400));
                }
            }
        }
    }

    /**
     * 创建页面显示区域
     */
    private void createPageDisplayArea(JPanel parentPanel) {
        // 页面显示区域容器
        JPanel displayArea = new JPanel(new BorderLayout());
        displayArea.setBackground(new Color(248, 249, 250));
        displayArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 页面显示画布容器（用于居中显示）
        JPanel canvasContainer = new JPanel(new GridBagLayout());
        canvasContainer.setBackground(new Color(248, 249, 250));

        // 在容器上添加边框效果，不影响画布内部空间
        canvasContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 页面显示画布
        editCanvas = new JPanel();
        editCanvas.setBackground(Color.WHITE);
        editCanvas.setLayout(null); // 使用绝对布局，确保元素约束在边界内

        // 设置初始尺寸（1024x768的缩放版本）
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxCanvasWidth = screenSize.width - 500;  // 左右各预留250px
        int maxCanvasHeight = screenSize.height - 400; // 顶部213px + 底部100px + 其他边距
        double scale = Math.min((double)maxCanvasWidth / 1024, (double)maxCanvasHeight / 768);
        int initialWidth = (int) (1024 * scale);
        int initialHeight = (int) (768 * scale);

        editCanvas.setPreferredSize(new Dimension(initialWidth, initialHeight));
        editCanvas.setMinimumSize(new Dimension(initialWidth, initialHeight));
        editCanvas.setMaximumSize(new Dimension(initialWidth, initialHeight));
        // 创建不占用内部空间的边框效果
        // 使用EmptyBorder确保边框不占用画布内部空间
        editCanvas.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 添加画布右键菜单支持
        addCanvasContextMenu(editCanvas);

        // 创建画布包装器来显示边框效果
        canvasWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制边框效果
                g2d.setColor(new Color(206, 212, 218));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);

                g2d.setColor(new Color(173, 181, 189));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2d.dispose();
            }
        };
        canvasWrapper.setLayout(new BorderLayout());
        canvasWrapper.setOpaque(false);

        // 设置包装器的尺寸与画布相同
        canvasWrapper.setPreferredSize(editCanvas.getPreferredSize());
        canvasWrapper.setMinimumSize(editCanvas.getMinimumSize());
        canvasWrapper.setMaximumSize(editCanvas.getMaximumSize());

        canvasWrapper.add(editCanvas, BorderLayout.CENTER);

        // 使用GridBagLayout居中显示画布包装器
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        canvasContainer.add(canvasWrapper, gbc);

        displayArea.add(canvasContainer, BorderLayout.CENTER);
        parentPanel.add(displayArea, BorderLayout.CENTER);
    }

    // 保存画布包装器的引用
    private JPanel canvasWrapper;

    /**
     * 更新画布包装器尺寸
     */
    private void updateCanvasWrapperSize(int width, int height) {
        if (canvasWrapper != null) {
            Dimension newSize = new Dimension(width, height);
            canvasWrapper.setPreferredSize(newSize);
            canvasWrapper.setMinimumSize(newSize);
            canvasWrapper.setMaximumSize(newSize);
            canvasWrapper.revalidate();
            canvasWrapper.repaint();
            System.out.println("更新画布包装器尺寸: " + width + "x" + height);
        }
    }

    /**
     * 更新页面显示区域尺寸
     */
    private void updatePageDisplayArea() {
        if (resolutionComboBox == null) {
            System.out.println("updatePageDisplayArea: resolutionComboBox为null");
            return;
        }

        String selectedResolution = (String) resolutionComboBox.getSelectedItem();
        System.out.println("updatePageDisplayArea: 选中的分辨率 = " + selectedResolution);

        if (selectedResolution != null && editCanvas != null) {
            // 解析分辨率字符串
            String[] parts = selectedResolution.split("x");
            if (parts.length == 2) {
                try {
                    int width = Integer.parseInt(parts[0]);
                    int height = Integer.parseInt(parts[1]);

                    // 计算合适的显示尺寸
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    int maxCanvasWidth = screenSize.width - 500;  // 左右各预留250px
                    int maxCanvasHeight = screenSize.height - 400; // 顶部213px + 底部100px + 其他边距
                    double scale = Math.min((double)maxCanvasWidth / width, (double)maxCanvasHeight / height);
                    int displayWidth = (int) (width * scale);
                    int displayHeight = (int) (height * scale);

                    System.out.println("updatePageDisplayArea: 计算尺寸 = " + displayWidth + "x" + displayHeight);

                    // 重新创建画布以确保尺寸变化
                    recreateCanvas(displayWidth, displayHeight);

                } catch (NumberFormatException e) {
                    System.out.println("updatePageDisplayArea: 解析分辨率失败，使用默认尺寸");
                    recreateCanvas(600, 450); // 默认尺寸
                }
            }
        }
    }

    /**
     * 重新创建画布（保留现有实例）
     */
    private void recreateCanvas(int width, int height) {
        if (editCanvas == null) {
            System.out.println("recreateCanvas: editCanvas为null，无法重新创建");
            return;
        }

        Container canvasContainer = editCanvas.getParent();
        if (canvasContainer == null) {
            System.out.println("recreateCanvas: canvasContainer为null，无法重新创建");
            return;
        }

        // 保存现有实例
        java.util.List<JPanel> existingInstances = new java.util.ArrayList<>();
        Component[] components = editCanvas.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                existingInstances.add((JPanel) comp);
            }
        }



        // 移除旧的画布
        canvasContainer.remove(editCanvas);

        // 创建新的画布
        editCanvas = new JPanel();
        editCanvas.setBackground(Color.WHITE);
        editCanvas.setLayout(null);
        editCanvas.setPreferredSize(new Dimension(width, height));
        editCanvas.setMinimumSize(new Dimension(width, height));
        editCanvas.setMaximumSize(new Dimension(width, height));
        // 不设置边框，保持内部空间完整
        editCanvas.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 添加画布右键菜单支持
        addCanvasContextMenu(editCanvas);

        // 首先加载当前页面的背景（如果有）

        // 新画布创建后再绑定背景自适应监听
        attachBackgroundAutoFitListener();

        if (currentPageName != null) {
            loadPageBackgroundInEditCanvas(currentPageName);
            System.out.println("recreateCanvas: 先加载页面背景 - " + currentPageName);

        // 画布重建完成后，异步同步背景尺寸以避免布局时尺寸为0的问题
            // 背景添加后再异步同步一次，确保与最终布局一致
            SwingUtilities.invokeLater(this::refreshBackgroundToCanvasSize);

        SwingUtilities.invokeLater(this::refreshBackgroundToCanvasSize);

        }

        // 然后恢复现有实例到新画布，并重新计算位置和大小
        for (JPanel instance : existingInstances) {
            // 获取实例的相对位置数据
            com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                (com.feixiang.tabletcontrol.model.RelativePosition) instance.getClientProperty("relativePosition");

            if (relativePos != null) {
                // 基于新画布尺寸重新计算位置和大小
                com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition newPos =
                    relativePos.toAbsolute(width, height);

                instance.setBounds(newPos.x, newPos.y, newPos.width, newPos.height);

                // 重新缩放实例内部的图片和文字
                updateInstanceComponentsForResize(instance, newPos.width, newPos.height);

                System.out.println("画布重建时重新计算实例位置: " + relativePos +
                                 " -> 新画布(" + width + "x" + height +
                                 ") -> 新位置(" + newPos.x + "," + newPos.y +
                                 "," + newPos.width + "," + newPos.height + ")");
            } else {
                System.out.println("警告：实例缺少相对位置数据，保持原位置: " + instance.getBounds());
            }

            addInstanceToEditCanvas(instance);
        }

        // 更新画布包装器
        if (canvasWrapper != null) {
            // 移除旧画布
            canvasWrapper.removeAll();
            // 添加新画布
            canvasWrapper.add(editCanvas, BorderLayout.CENTER);
            // 更新包装器尺寸
            updateCanvasWrapperSize(width, height);
        } else {
            // 如果没有包装器，直接添加到容器（向后兼容）
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            canvasContainer.add(editCanvas, gbc);
        }

        // 强制更新布局
        canvasContainer.revalidate();
        canvasContainer.repaint();

        // 更新父容器
        Container parent = canvasContainer.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }

        // 更新编辑窗口

        if (editWindow != null && editWindow.isVisible()) {
            editWindow.revalidate();
            editWindow.repaint();
        }

        System.out.println("recreateCanvas: 画布重新创建完成，尺寸 = " + width + "x" + height +
                         ", 恢复了 " + existingInstances.size() + " 个实例");
    }

    /**
     * 创建文本属性卡片（两列布局）
     */
    private JPanel createTextPropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(280, 160)); // 增加宽度以容纳两列布局

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("文本属性");
        headerLabel.setFont(getUnicodeFont(13)); // 增大标题字体
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 属性内容区域 - 两列布局
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 允许控件水平填充

        // 第1项：文本内容（左列第1行）
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel textLabel = new JLabel("文本:");
        textLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        textLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(textLabel, gbc);

        gbc.gridx = 1;
        textField = new JTextField("文本", 8); // 增加列数从6到8
        textField.setFont(getUnicodeFont(12)); // 增大输入框字体
        textField.setPreferredSize(new Dimension(100, 25)); // 设置明确的尺寸
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        // 添加文本变化监听器（实时更新）
        textField.addActionListener(e -> {
            System.out.println("文本输入框ActionListener触发: " + textField.getText());
            updateInstanceFromPropertyPanel();
        });
        textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("文本输入框DocumentListener触发: " + textField.getText());
                updateInstanceFromPropertyPanel();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("文本输入框DocumentListener触发: " + textField.getText());
                updateInstanceFromPropertyPanel();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("文本输入框DocumentListener触发: " + textField.getText());
                updateInstanceFromPropertyPanel();
            }
        });
        contentPanel.add(textField, gbc);

        // 第2项：字体选择（右列第1行）
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel fontLabel = new JLabel("字体:");
        fontLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        fontLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(fontLabel, gbc);

        gbc.gridx = 3;
        String[] fonts = {"微软雅黑", "宋体", "楷体", "行书", "黑体", "仿宋", "隶书",
                         "Arial", "Times New Roman", "Calibri", "Verdana", "Tahoma",
                         "Georgia", "Comic Sans MS", "Impact"};
        fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setFont(getUnicodeFont(11)); // 增大下拉框字体
        fontComboBox.setBackground(Color.WHITE);
        fontComboBox.setSelectedIndex(0); // 默认微软雅黑
        // 添加字体变化监听器（实时更新）
        fontComboBox.addActionListener(e -> {
            System.out.println("字体下拉框ActionListener触发: " + fontComboBox.getSelectedItem());
            updateInstanceFromPropertyPanel();
        });
        contentPanel.add(fontComboBox, gbc);

        // 第3项：字号设置（左列第2行）
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel sizeLabel = new JLabel("字号:");
        sizeLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        sizeLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        String[] sizes = new String[47]; // 2-48
        for (int i = 0; i < 47; i++) {
            sizes[i] = String.valueOf(i + 2);
        }
        sizeComboBox = new JComboBox<>(sizes);
        sizeComboBox.setFont(getUnicodeFont(11)); // 增大下拉框字体
        sizeComboBox.setBackground(Color.WHITE);
        sizeComboBox.setSelectedIndex(28); // 默认30号字体
        // 设置字号下拉框的宽度，由于滚动条变窄了，可以加宽字号框
        // 从50像素增加到70像素，给更多空间显示字号
        sizeComboBox.setPreferredSize(new Dimension(70, sizeComboBox.getPreferredSize().height));
        sizeComboBox.setMinimumSize(new Dimension(70, sizeComboBox.getPreferredSize().height));
        sizeComboBox.setMaximumSize(new Dimension(70, sizeComboBox.getPreferredSize().height));
        // 添加字号变化监听器（实时更新）
        sizeComboBox.addActionListener(e -> {
            System.out.println("字号下拉框ActionListener触发: " + sizeComboBox.getSelectedItem());
            updateInstanceFromPropertyPanel();
        });
        contentPanel.add(sizeComboBox, gbc);

        // 第4项：颜色选择（右列第2行）
        gbc.gridx = 2; gbc.gridy = 1;
        JLabel colorLabel = new JLabel("颜色:");
        colorLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        colorLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(colorLabel, gbc);

        gbc.gridx = 3;
        colorButton = new JButton();
        colorButton.setBackground(Color.BLACK); // 默认黑色
        colorButton.setPreferredSize(new Dimension(70, 25)); // 增加宽度和高度
        colorButton.setMinimumSize(new Dimension(70, 25)); // 设置最小尺寸
        colorButton.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
        colorButton.setOpaque(true); // 确保背景色可见
        colorButton.setText(""); // 确保没有文本
        colorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(card, "选择文字颜色", Color.BLACK);
            if (color != null) {
                colorButton.setBackground(color);
                // 实时更新实例颜色
                updateInstanceFromPropertyPanel();
            }
        });
        contentPanel.add(colorButton, gbc);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * 创建位置属性卡片
     */
    private JPanel createPositionPropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(200, 160)); // 调整宽度保持一致性

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("位置属性");
        headerLabel.setFont(getUnicodeFont(13)); // 增大标题字体
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 属性内容区域
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // X坐标
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel xLabel = new JLabel("X坐标:");
        xLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        xLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(xLabel, gbc);

        gbc.gridx = 1;
        xField = new JTextField("0", 8);
        xField.setFont(getUnicodeFont(12)); // 增大输入框字体
        xField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        // 添加X坐标变化监听器（多种事件确保实时更新）
        xField.addActionListener(e -> {
            System.out.println("X坐标输入框ActionListener触发: " + xField.getText());
            updateInstanceFromPropertyPanel();
        });
        xField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                System.out.println("X坐标输入框FocusListener触发: " + xField.getText());
                updateInstanceFromPropertyPanel();
            }
        });
        // 添加DocumentListener实现真正的实时更新
        xField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("X坐标输入框DocumentListener触发: " + xField.getText());
                updateInstanceFromPropertyPanel();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("X坐标输入框DocumentListener触发: " + xField.getText());
                updateInstanceFromPropertyPanel();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("X坐标输入框DocumentListener触发: " + xField.getText());
                updateInstanceFromPropertyPanel();
            }
        });
        contentPanel.add(xField, gbc);

        // Y坐标
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel yLabel = new JLabel("Y坐标:");
        yLabel.setFont(getUnicodeFont(12)); // 增大标签字体
        yLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(yLabel, gbc);

        gbc.gridx = 1;
        yField = new JTextField("0", 8);
        yField.setFont(getUnicodeFont(12)); // 增大输入框字体
        yField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        // 添加Y坐标变化监听器
        yField.addActionListener(e -> updateInstanceFromPropertyPanel());
        // 添加Y坐标变化监听器（多种事件确保实时更新）
        yField.addActionListener(e -> {
            System.out.println("Y坐标输入框ActionListener触发: " + yField.getText());
            updateInstanceFromPropertyPanel();
        });
        yField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                System.out.println("Y坐标输入框FocusListener触发: " + yField.getText());
                updateInstanceFromPropertyPanel();
            }
        });
        // 添加DocumentListener实现真正的实时更新
        yField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("Y坐标输入框DocumentListener触发: " + yField.getText());
                updateInstanceFromPropertyPanel();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("Y坐标输入框DocumentListener触发: " + yField.getText());
                updateInstanceFromPropertyPanel();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                System.out.println("Y坐标输入框DocumentListener触发: " + yField.getText());
                updateInstanceFromPropertyPanel();
            }
        });
        contentPanel.add(yField, gbc);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * 创建功能属性卡片（支持动态显示）
     */
    private JPanel createFunctionPropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(200, 160)); // 与其他属性卡片保持一致的高度

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("功能属性");
        headerLabel.setFont(getUnicodeFont(13)); // 增大标题字体
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 主内容区域（使用CardLayout支持动态切换）
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(new Color(248, 249, 250));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // 类型选择区域（始终显示）
        JPanel typeSelectionPanel = createTypeSelectionPanel();
        mainContentPanel.add(typeSelectionPanel, BorderLayout.NORTH);

        // 动态功能配置区域
        JPanel dynamicContentPanel = new JPanel(new CardLayout());
        dynamicContentPanel.setBackground(new Color(248, 249, 250));

        // 创建各种功能配置面板
        createFunctionConfigPanels(dynamicContentPanel);

        mainContentPanel.add(dynamicContentPanel, BorderLayout.CENTER);
        card.add(mainContentPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * 创建类型选择面板
     */
    private JPanel createTypeSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 3, 3, 3);
        //gbc.anchor = GridBagConstraints.WEST;
        gbc.anchor = GridBagConstraints.CENTER;

        // 类型标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel typeLabel = new JLabel("类型:");
        typeLabel.setFont(getUnicodeFont(12));
        typeLabel.setForeground(new Color(73, 80, 87));
        panel.add(typeLabel, gbc);

        // 类型下拉框
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String[] functionTypes = {"纯文本", "页面跳转", "发送指令", "实例组"};
        functionTypeComboBox = new JComboBox<>(functionTypes);
        functionTypeComboBox.setFont(getUnicodeFont(11));
        functionTypeComboBox.setBackground(Color.WHITE);
        functionTypeComboBox.setSelectedIndex(0); // 默认选择"纯文本"

        // 添加类型变化监听器
        functionTypeComboBox.addActionListener(e -> {
            String selectedType = (String) functionTypeComboBox.getSelectedItem();
            System.out.println("功能类型变化: " + selectedType);

            // 保存功能类型到当前选中的实例
            if (selectedInstance != null) {
                selectedInstance.putClientProperty("functionType", selectedType);
                System.out.println("功能类型已保存到实例: " + selectedType);
            }

            // 更新功能配置显示
            updateFunctionConfigDisplay(selectedType);

            // 如果切换到页面跳转功能，刷新目标页面下拉框并排除当前页面
            if ("页面跳转".equals(selectedType)) {
                refreshTargetPageComboBox(currentPageName);
            }
        });

        panel.add(functionTypeComboBox, gbc);

        return panel;
    }

    /**
     * 创建各种功能配置面板
     */
    private void createFunctionConfigPanels(JPanel dynamicContentPanel) {
        CardLayout cardLayout = (CardLayout) dynamicContentPanel.getLayout();

        // 1. 纯文本面板（空面板）
        JPanel pureTextPanel = new JPanel();
        pureTextPanel.setBackground(new Color(248, 249, 250));
        dynamicContentPanel.add(pureTextPanel, "纯文本");

        // 2. 页面跳转面板
        pageJumpPanel = createPageJumpPanel();
        dynamicContentPanel.add(pageJumpPanel, "页面跳转");

        // 3. 发送指令面板
        commandPanel = createCommandPanel();
        dynamicContentPanel.add(commandPanel, "发送指令");

        // 4. 实例组面板
        instanceGroupPanel = createInstanceGroupPanel();
        dynamicContentPanel.add(instanceGroupPanel, "实例组");

        // 默认显示纯文本面板
        cardLayout.show(dynamicContentPanel, "纯文本");
    }

    /**
     * 创建页面跳转配置面板
     */
    private JPanel createPageJumpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 目标页面标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel targetPageLabel = new JLabel("目标页面:");
        targetPageLabel.setFont(getUnicodeFont(12));
        targetPageLabel.setForeground(new Color(73, 80, 87));
        panel.add(targetPageLabel, gbc);

        // 目标页面下拉框
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 动态获取页面列表，排除当前页面
        String[] pages = getAvailablePages(currentPageName);
        targetPageComboBox = new JComboBox<>(pages);
        targetPageComboBox.setFont(getUnicodeFont(11));
        targetPageComboBox.setBackground(Color.WHITE);

        // 添加页面选择变化监听器
        targetPageComboBox.addActionListener(e -> {
            String selectedPage = (String) targetPageComboBox.getSelectedItem();
            if (selectedInstance != null && selectedPage != null && !selectedPage.equals("无可用页面")) {
                selectedInstance.putClientProperty("targetPage", selectedPage);
                System.out.println("目标页面已保存: " + selectedPage);
            }
        });

        panel.add(targetPageComboBox, gbc);

        return panel;
    }

    /**
     * 获取可用页面列表
     */
    private String[] getAvailablePages() {
        return getAvailablePages(null);
    }

    /**
     * 获取可用页面列表，排除指定页面
     * @param excludePage 要排除的页面名称，如果为null则不排除任何页面
     */
    private String[] getAvailablePages(String excludePage) {
        if (pageListModel == null || pageListModel.isEmpty()) {
            return new String[]{"无可用页面"};
        }

        java.util.List<String> availablePages = new java.util.ArrayList<>();
        for (int i = 0; i < pageListModel.size(); i++) {
            String pageName = pageListModel.getElementAt(i);
            if (excludePage == null || !pageName.equals(excludePage)) {
                availablePages.add(pageName);
            }
        }

        if (availablePages.isEmpty()) {
            return new String[]{"无可用页面"};
        }

        return availablePages.toArray(new String[0]);
    }

    /**
     * 刷新目标页面下拉框
     */
    private void refreshTargetPageComboBox() {
        refreshTargetPageComboBox(null);
    }

    /**
     * 刷新目标页面下拉框，排除指定页面
     * @param excludePage 要排除的页面名称，如果为null则不排除任何页面
     */
    private void refreshTargetPageComboBox(String excludePage) {
        if (targetPageComboBox != null) {
            String[] pages = getAvailablePages(excludePage);
            targetPageComboBox.removeAllItems();
            for (String page : pages) {
                targetPageComboBox.addItem(page);
            }
            System.out.println("目标页面下拉框已刷新，页面数量: " + pages.length +
                             (excludePage != null ? "，排除页面: " + excludePage : ""));
        }
    }

    /**
     * 创建发送指令配置面板
     */
    private JPanel createCommandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 指令标签和编辑按钮
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel commandLabel = new JLabel("指令:");
        commandLabel.setFont(getUnicodeFont(12));
        commandLabel.setForeground(new Color(73, 80, 87));
        panel.add(commandLabel, gbc);

        // 编辑按钮（优化样式）
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JButton editButton = new JButton("编辑");
        editButton.setFont(getUnicodeFont(13));
        editButton.setPreferredSize(new Dimension(60, 28));
        editButton.setBackground(new Color(0, 123, 255));
        editButton.setForeground(Color.WHITE);
        editButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        editButton.setFocusPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                editButton.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                editButton.setBackground(new Color(0, 123, 255));
            }
        });

        // 创建一个隐藏的文本框用于存储指令信息（不显示给用户）
        JTextField commandField = new JTextField();
        commandField.setVisible(false);

        editButton.addActionListener(e -> openCommandListDialogWithDelay(commandField));
        panel.add(editButton, gbc);

        return panel;
    }

    /**
     * 创建实例组配置面板
     */
    private JPanel createInstanceGroupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 实例组标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel groupLabel = new JLabel("实例组:");
        groupLabel.setFont(getUnicodeFont(12));
        groupLabel.setForeground(new Color(73, 80, 87));
        panel.add(groupLabel, gbc);

        // 编辑按钮（参照发送指令功能的设计）
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JButton editButton = new JButton("编辑");
        editButton.setFont(getUnicodeFont(13));
        editButton.setPreferredSize(new Dimension(60, 28));
        editButton.setBackground(new Color(0, 123, 255));
        editButton.setForeground(Color.WHITE);
        editButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 86, 179), 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        editButton.setFocusPainted(false);

        // 添加悬停效果
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                editButton.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                editButton.setBackground(new Color(0, 123, 255));
            }
        });

        // 创建一个隐藏的文本框用于存储实例组信息（不显示给用户）
        JTextField groupField = new JTextField();
        groupField.setVisible(false);

        editButton.addActionListener(e -> openInstanceGroupDialog(groupField));
        panel.add(editButton, gbc);

        return panel;
    }

    /**
     * 创建实例组配置对话框标题面板
     */
    private JPanel createInstanceGroupTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(248, 250, 252));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题标签
        JLabel titleLabel = new JLabel("实例组配置");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 58, 64));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // 描述标签
        JLabel descLabel = new JLabel("配置实例组的名称和包含的指令");
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        descLabel.setForeground(new Color(108, 117, 125));
        titlePanel.add(descLabel, BorderLayout.SOUTH);

        return titlePanel;
    }

    /**
     * 创建实例组名称输入面板
     */
    private JPanel createInstanceGroupNamePanel() {
        JPanel namePanel = new JPanel(new GridBagLayout());
        namePanel.setBackground(Color.WHITE);
        namePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // 名称标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("实例组名称:");
        nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        nameLabel.setForeground(new Color(73, 80, 87));
        namePanel.add(nameLabel, gbc);

        // 名称输入框
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(25);
        nameField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        nameField.setBackground(new Color(248, 250, 252));

        // 添加焦点效果
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(59, 130, 246), 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
                nameField.setBackground(Color.WHITE);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                nameField.setBackground(new Color(248, 250, 252));
            }
        });

        namePanel.add(nameField, gbc);
        return namePanel;
    }

    /**
     * 在面板中查找名称输入框
     */
    private JTextField findNameFieldInPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            }
        }
        return null;
    }

    /**
     * 智能截断文本以适应指定宽度
     */
    private String truncateTextToFit(String text, JComponent component, int availableWidth) {
        if (availableWidth <= 0) {
            return text; // 如果宽度无效，返回原文本
        }

        FontMetrics fm = component.getFontMetrics(component.getFont());
        if (fm.stringWidth(text) <= availableWidth) {
            return text; // 文本适合，不需要截断
        }

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);

        // 二分查找最佳截断位置
        int left = 0;
        int right = text.length();
        String result = text;

        while (left < right) {
            int mid = (left + right + 1) / 2;
            String candidate = text.substring(0, mid) + ellipsis;

            if (fm.stringWidth(candidate) <= availableWidth) {
                result = candidate;
                left = mid;
            } else {
                right = mid - 1;
            }
        }

        return result;
    }

    /**
     * 解析指令数据，支持延时字段
     * 格式：name|protocol|ip|port|command|delay（delay可选）
     */
    private String[] parseCommandData(String commandData) {
        String[] parts = commandData.split("\\|");
        if (parts.length >= 5) {
            // 确保有6个字段（包括延时）
            String[] result = new String[6];
            System.arraycopy(parts, 0, result, 0, Math.min(parts.length, 6));
            if (parts.length < 6) {
                result[5] = "0"; // 默认无延时
            }
            return result;
        }
        return parts;
    }

    /**
     * 构建指令数据字符串，包含延时字段
     */
    private String buildCommandData(String name, String protocol, String ip, String port, String command, String delay) {
        if (delay == null || delay.trim().isEmpty()) {
            delay = "0";
        }
        return String.format("%s|%s|%s|%s|%s|%s", name, protocol, ip, port, command, delay);
    }

    /**
     * 获取指令的延时设置
     */
    private double getCommandDelay(String commandData) {
        String[] parts = parseCommandData(commandData);
        if (parts.length >= 6) {
            try {
                return Double.parseDouble(parts[5]);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * 打开延时设置对话框
     */
    private void openDelaySettingDialog(String commandData, java.util.function.Consumer<String> onDelaySet) {
        JDialog dialog = new JDialog(this, "延时设置", true);
        dialog.setSize(400, 300); // 从250增加到300
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(new Color(248, 250, 252));
        dialog.setResizable(false);

        // 标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(248, 250, 252));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JLabel titleLabel = new JLabel("指令延时设置");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 58, 64));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JLabel descLabel = new JLabel("设置指令执行完成后的等待时间");
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        descLabel.setForeground(new Color(108, 117, 125));
        titlePanel.add(descLabel, BorderLayout.SOUTH);

        dialog.add(titlePanel, BorderLayout.NORTH);

        // 内容面板
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30) // 增加内边距
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 15); // 增加组件间距
        gbc.anchor = GridBagConstraints.WEST;

        // 延时标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel delayLabel = new JLabel("延时时间:");
        delayLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        delayLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(delayLabel, gbc);

        // 延时输入框
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField delayField = new JTextField(10);
        delayField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        delayField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        delayField.setBackground(new Color(248, 250, 252));

        // 设置当前延时值
        double currentDelay = getCommandDelay(commandData);
        if (currentDelay > 0) {
            delayField.setText(String.valueOf(currentDelay));
        }

        contentPanel.add(delayField, gbc);

        // 单位标签
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel unitLabel = new JLabel("秒");
        unitLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        unitLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(unitLabel, gbc);

        // 说明标签
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel noteLabel = new JLabel("支持小数，如 0.1, 0.5, 1.0 等，0表示无延时");
        noteLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        noteLabel.setForeground(new Color(108, 117, 125));
        contentPanel.add(noteLabel, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = createDelayDialogButtonPanel(dialog, delayField, commandData, onDelaySet);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 创建延时对话框按钮面板
     */
    private JPanel createDelayDialogButtonPanel(JDialog dialog, JTextField delayField, String commandData, java.util.function.Consumer<String> onDelaySet) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        // 确定按钮
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        confirmButton.setPreferredSize(new Dimension(100, 40));
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setFocusPainted(false);

        // 取消按钮
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        cancelButton.setFocusPainted(false);

        // 添加悬停效果
        confirmButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                confirmButton.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                confirmButton.setBackground(new Color(0, 123, 255));
            }
        });

        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(90, 98, 104));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(108, 117, 125));
            }
        });

        // 确定按钮事件
        confirmButton.addActionListener(e -> {
            String delayText = delayField.getText().trim();

            // 验证输入
            if (!delayText.isEmpty()) {
                try {
                    double delay = Double.parseDouble(delayText);
                    if (delay < 0) {
                        JOptionPane.showMessageDialog(dialog, "延时时间不能为负数", "输入错误", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "输入错误", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 更新指令数据
            String[] parts = parseCommandData(commandData);
            if (parts.length >= 5) {
                String delay = delayText.isEmpty() ? "0" : delayText;
                String updatedCommand = buildCommandData(parts[0], parts[1], parts[2], parts[3], parts[4], delay);
                onDelaySet.accept(updatedCommand);
            }

            dialog.dispose();
        });

        // 取消按钮事件
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    /**
     * 创建实例组指令列表面板
     */
    private JPanel createInstanceGroupListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 列表标题和全选区域
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel listLabel = new JLabel("选择要包含的指令:");
        listLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        listLabel.setForeground(new Color(73, 80, 87));
        headerPanel.add(listLabel, BorderLayout.WEST);

        JCheckBox selectAllCheckBox = new JCheckBox("全选");
        selectAllCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        selectAllCheckBox.setForeground(new Color(108, 117, 125));
        selectAllCheckBox.setBackground(Color.WHITE);
        headerPanel.add(selectAllCheckBox, BorderLayout.EAST);

        listPanel.add(headerPanel, BorderLayout.NORTH);

        // 指令列表
        DefaultListModel<CheckBoxItem> listModel = new DefaultListModel<>();
        collectAvailableCommandsForInstanceGroup(listModel);

        JList<CheckBoxItem> commandList = new JList<>(listModel);
        commandList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        commandList.setCellRenderer(new CheckBoxListCellRenderer());
        commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandList.setBackground(Color.WHITE);

        // 点击切换选中状态
        commandList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = commandList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    CheckBoxItem item = listModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    commandList.repaint();
                }
            }
        });

        // 全选功能
        selectAllCheckBox.addActionListener(e -> {
            boolean selectAll = selectAllCheckBox.isSelected();
            for (int i = 0; i < listModel.size(); i++) {
                listModel.getElementAt(i).setSelected(selectAll);
            }
            commandList.repaint();
        });

        JScrollPane scrollPane = new JScrollPane(commandList);
        scrollPane.setPreferredSize(new Dimension(580, 240));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        listPanel.add(scrollPane, BorderLayout.CENTER);

        // 存储组件引用以便后续使用
        listPanel.putClientProperty("listModel", listModel);
        listPanel.putClientProperty("selectAllCheckBox", selectAllCheckBox);

        return listPanel;
    }

    /**
     * 创建带延时功能的实例组指令列表面板
     */
    private JPanel createInstanceGroupListPanelWithDelay() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // 列表标题和全选区域
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(Color.WHITE);

        JLabel listLabel = new JLabel("选择要包含的指令:");
        listLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        listLabel.setForeground(new Color(73, 80, 87));
        leftPanel.add(listLabel);

        JLabel dragHintLabel = new JLabel("  (可拖拽调整顺序)");
        dragHintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        dragHintLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(dragHintLabel);

        headerPanel.add(leftPanel, BorderLayout.WEST);

        JCheckBox selectAllCheckBox = new JCheckBox("全选");
        selectAllCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        selectAllCheckBox.setBackground(Color.WHITE);
        selectAllCheckBox.setForeground(new Color(73, 80, 87));
        headerPanel.add(selectAllCheckBox, BorderLayout.EAST);

        listPanel.add(headerPanel, BorderLayout.NORTH);

        // 创建指令列表
        DefaultListModel<CheckBoxItemWithDelay> listModel = new DefaultListModel<>();
        collectAvailableCommandsForInstanceGroupWithDelay(listModel);

        // 创建支持拖拽重排序的列表
        DraggableJList<CheckBoxItemWithDelay> commandList = new DraggableJList<>(listModel);
        commandList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        commandList.setCellRenderer(new CheckBoxWithDelayListCellRenderer());
        commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandList.setBackground(Color.WHITE);

        // 点击切换选中状态或打开延时设置（需要与拖拽功能协调）
        commandList.addMouseListener(new java.awt.event.MouseAdapter() {
            private boolean wasDragging = false;

            @Override
            public void mousePressed(MouseEvent e) {
                wasDragging = false;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 如果刚刚完成拖拽，不处理点击事件
                if (commandList.isDragging) {
                    wasDragging = true;
                    return;
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 如果刚刚完成拖拽，不处理点击事件
                if (wasDragging) {
                    wasDragging = false;
                    return;
                }

                int index = commandList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    CheckBoxItemWithDelay item = listModel.getElementAt(index);
                    Rectangle cellBounds = commandList.getCellBounds(index, index);

                    // 检查是否点击了延时按钮区域（右侧80像素）
                    if (e.getX() > cellBounds.width - 80) {
                        // 点击了延时按钮
                        openDelaySettingDialog(item.getCommandData(), updatedCommand -> {
                            item.setCommandData(updatedCommand);
                            commandList.repaint();
                        });
                    } else {
                        // 点击了复选框区域
                        item.setSelected(!item.isSelected());
                        commandList.repaint();
                    }
                }
            }
        });

        // 全选功能
        selectAllCheckBox.addActionListener(e -> {
            boolean selectAll = selectAllCheckBox.isSelected();
            for (int i = 0; i < listModel.size(); i++) {
                listModel.getElementAt(i).setSelected(selectAll);
            }
            commandList.repaint();
        });

        JScrollPane scrollPane = new JScrollPane(commandList);
        scrollPane.setPreferredSize(new Dimension(580, 240));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        listPanel.add(scrollPane, BorderLayout.CENTER);

        // 存储组件引用以便后续使用
        listPanel.putClientProperty("listModel", listModel);
        listPanel.putClientProperty("selectAllCheckBox", selectAllCheckBox);

        return listPanel;
    }

    /**
     * 恢复实例组指令选择状态和顺序
     */
    private void restoreInstanceGroupCommandSelection(JPanel listPanel, java.util.List<String> savedCommands) {
        @SuppressWarnings("unchecked")
        DefaultListModel<CheckBoxItemWithDelay> listModel = (DefaultListModel<CheckBoxItemWithDelay>) listPanel.getClientProperty("listModel");

        if (listModel != null && savedCommands != null) {
            System.out.println("开始恢复实例组指令选择状态和顺序，保存的指令数量: " + savedCommands.size());
            System.out.println("当前列表中的指令数量: " + listModel.size());

            // 按照保存的顺序重新排列列表
            restoreInstanceGroupCommandOrder(listModel, savedCommands);

            // 强制刷新列表显示
            if (listPanel.getComponentCount() > 1) {
                Component listComponent = listPanel.getComponent(1);
                if (listComponent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) listComponent;
                    Component viewport = scrollPane.getViewport().getView();
                    if (viewport instanceof JList) {
                        ((JList<?>) viewport).repaint();
                        System.out.println("✓ 刷新列表显示");
                    }
                }
            }
        }
    }

    /**
     * 按照保存的顺序重新排列实例组指令列表
     */
    private void restoreInstanceGroupCommandOrder(DefaultListModel<CheckBoxItemWithDelay> listModel, java.util.List<String> savedCommands) {
        System.out.println("开始按保存顺序重新排列指令列表");

        // 创建一个临时列表存储原始的所有指令项
        java.util.List<CheckBoxItemWithDelay> allItems = new java.util.ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            allItems.add(listModel.getElementAt(i));
        }

        // 清空当前列表
        listModel.clear();

        // 首先按照保存的顺序添加已选中的指令
        for (String savedCommand : savedCommands) {
            String savedName = savedCommand.split("\\|")[0];
            System.out.println("按顺序添加已保存指令: " + savedName);

            // 在原始列表中查找匹配的指令
            CheckBoxItemWithDelay matchedItem = null;
            for (CheckBoxItemWithDelay item : allItems) {
                String itemName = item.getCommandData().split("\\|")[0];
                if (itemName.equals(savedName)) {
                    matchedItem = item;
                    break;
                }
            }

            if (matchedItem != null) {
                // 设置为选中状态
                matchedItem.setSelected(true);

                // 恢复实例组专用的延时设置
                if (savedCommand.split("\\|").length >= 6) {
                    matchedItem.setCommandData(savedCommand);
                    String delay = savedCommand.split("\\|")[5];
                    if (!"0".equals(delay)) {
                        System.out.println("✓ 恢复延时设置: " + savedName + " -> " + delay + "秒");
                    }
                }

                // 添加到新列表
                listModel.addElement(matchedItem);
                allItems.remove(matchedItem);
                System.out.println("✓ 已添加指令: " + savedName);
            } else {
                System.out.println("✗ 未找到匹配的指令: " + savedName);
            }
        }

        // 然后添加剩余的未选中指令
        for (CheckBoxItemWithDelay item : allItems) {
            item.setSelected(false);
            listModel.addElement(item);
            String itemName = item.getCommandData().split("\\|")[0];
            System.out.println("添加未选中指令: " + itemName);
        }

        System.out.println("指令列表重新排列完成，总数: " + listModel.size());
    }

    /**
     * 更新功能配置显示
     */
    private void updateFunctionConfigDisplay(String selectedType) {
        // 获取动态内容面板
        Container parent = functionTypeComboBox.getParent();
        while (parent != null && !(parent.getLayout() instanceof BorderLayout)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            Component centerComponent = ((BorderLayout) parent.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JPanel) {
                JPanel dynamicPanel = (JPanel) centerComponent;
                CardLayout cardLayout = (CardLayout) dynamicPanel.getLayout();
                cardLayout.show(dynamicPanel, selectedType);

                // 刷新界面
                parent.revalidate();
                parent.repaint();

                System.out.println("功能配置面板已切换到: " + selectedType);
            }
        }
    }

    /**
     * 创建接口配置卡片
     */
    private JPanel createInterfacePropertyCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(200, 160));

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("接口");
        headerLabel.setFont(getUnicodeFont(13));
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 内容区域
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 第三方接口标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel interfaceLabel = new JLabel("第三方接口");
        interfaceLabel.setFont(getUnicodeFont(13));
        interfaceLabel.setForeground(new Color(73, 80, 87));
        contentPanel.add(interfaceLabel, gbc);

        // 接口地址输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField interfaceField = new JTextField();
        interfaceField.setFont(getUnicodeFont(10));
        interfaceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        interfaceField.setBackground(Color.WHITE);
        contentPanel.add(interfaceField, gbc);

        // 存储接口输入框的引用
        card.putClientProperty("interfaceField", interfaceField);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * 创建操作按钮卡片
     */
    private JPanel createOperationCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));
        card.setPreferredSize(new Dimension(200, 160));

        // 卡片标题
        JPanel header = new JPanel();
        header.setBackground(new Color(233, 236, 239));
        header.setPreferredSize(new Dimension(0, 26));
        JLabel headerLabel = new JLabel("操作按钮");
        headerLabel.setFont(getUnicodeFont(13)); // 增大标题字体
        headerLabel.setForeground(new Color(73, 80, 87));
        header.add(headerLabel);
        card.add(header, BorderLayout.NORTH);

        // 按钮内容区域
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        // 复制实例按钮
        gbc.gridx = 0; gbc.gridy = 0;
        JButton copyButton = new JButton("复制实例");
        copyButton.setFont(getUnicodeFont(12));
        copyButton.setBackground(new Color(40, 167, 69)); // 绿色
        copyButton.setForeground(Color.WHITE);
        copyButton.setPreferredSize(new Dimension(120, 30));
        copyButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        copyButton.setFocusPainted(false);
        copyButton.addActionListener(e -> {
            if (validatePageExists() && validateInstanceSelected()) {
                copySelectedInstance();
            }
        });
        contentPanel.add(copyButton, gbc);

        // 删除实例按钮
        gbc.gridy = 1;
        JButton deleteButton = new JButton("删除实例");
        deleteButton.setFont(getUnicodeFont(12));
        deleteButton.setBackground(new Color(220, 53, 69)); // 红色
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setPreferredSize(new Dimension(120, 30));
        deleteButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> {
            if (validatePageExists() && validateInstanceSelected()) {
                int result = JOptionPane.showConfirmDialog(deleteButton,
                    "确定要删除当前选中的元素实例吗？", "确认删除",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    deleteSelectedInstance();
                }
            }
        });
        contentPanel.add(deleteButton, gbc);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * 验证页面是否存在
     */
    private boolean validatePageExists() {
        // 检查页面列表是否有页面
        if (pageListModel == null || pageListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先创建页面",
                                        "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * 验证是否有选中的实例
     */
    private boolean validateInstanceSelected() {
        if (selectedInstance == null) {
            JOptionPane.showMessageDialog(this, "当前没有选中实例，请选中后操作",
                                        "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * 添加实例到画布
     */
    private void addInstanceToCanvas() {
        if (editCanvas == null) {
            return;
        }

        // 检查btn1.png文件是否存在
        File btn1File = new File("USERDATA/appearance/btn1.png");
        if (!btn1File.exists()) {
            JOptionPane.showMessageDialog(this, "文件不存在: USERDATA/appearance/btn1.png",
                                        "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 创建实例容器
            JPanel instance = new JPanel();
            instance.setLayout(null); // 使用绝对布局支持图层
            instance.setOpaque(false);
            // 先不设置边框，等选中时再设置

            // 确保实例可以接收鼠标事件
            instance.setEnabled(true);
            instance.setFocusable(true);

            // 获取图片的原始尺寸并自动计算合适的显示尺寸
            ImageIcon originalIcon = new ImageIcon(btn1File.getAbsolutePath());
            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();

            // 计算宽高比
            double aspectRatio = (double) originalWidth / originalHeight;

            // 设置目标显示区域的最大尺寸
            int maxDisplayWidth = 120;
            int maxDisplayHeight = 80;

            // 根据宽高比和最大尺寸限制计算实际显示尺寸
            int instanceWidth, instanceHeight;

            if (aspectRatio > (double) maxDisplayWidth / maxDisplayHeight) {
                // 图片比较宽，以宽度为准
                instanceWidth = maxDisplayWidth;
                instanceHeight = (int) (maxDisplayWidth / aspectRatio);
            } else {
                // 图片比较高，以高度为准
                instanceHeight = maxDisplayHeight;
                instanceWidth = (int) (maxDisplayHeight * aspectRatio);
            }

            // 确保最小尺寸
            if (instanceWidth < 40) {
                instanceWidth = 40;
                instanceHeight = (int) (40 / aspectRatio);
            }
            if (instanceHeight < 20) {
                instanceHeight = 20;
                instanceWidth = (int) (20 * aspectRatio);
            }

            instance.setSize(instanceWidth, instanceHeight);
            System.out.println("图片原始尺寸: " + originalWidth + "x" + originalHeight +
                             ", 宽高比: " + String.format("%.2f", aspectRatio) +
                             ", 实例尺寸: " + instanceWidth + "x" + instanceHeight);

            // 创建一个同时包含图片和文本的标签
            ImageIcon icon = new ImageIcon(btn1File.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(instanceWidth, instanceHeight, Image.SCALE_SMOOTH);

            // 创建单个JLabel，同时设置图片和文本
            JLabel combinedLabel = new JLabel("文本", new ImageIcon(img), SwingConstants.CENTER);
            combinedLabel.setFont(getUnicodeFont(16));
            combinedLabel.setForeground(Color.WHITE);
            combinedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            combinedLabel.setVerticalAlignment(SwingConstants.CENTER);
            combinedLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            combinedLabel.setVerticalTextPosition(SwingConstants.CENTER);
            combinedLabel.setBounds(0, 0, instanceWidth, instanceHeight);
            combinedLabel.setOpaque(false);

            // 确保标签不会阻止鼠标事件传播
            combinedLabel.setEnabled(true);
            combinedLabel.setFocusable(false); // 标签不需要焦点

            // 保存原始图标用于高质量缩放，并设置描述为文件路径
            ImageIcon savedOriginalIcon = new ImageIcon(btn1File.getAbsolutePath());
            savedOriginalIcon.setDescription(btn1File.getAbsolutePath());
            combinedLabel.putClientProperty("originalIcon", savedOriginalIcon);

            // 保存原始字体大小用于缩放
            combinedLabel.putClientProperty("originalFontSize", combinedLabel.getFont().getSize());

            instance.add(combinedLabel);

            // 注意：不再保存originalWidth/originalHeight，完全使用百分比定位

            System.out.println("创建实例 - 图片: " + btn1File.getName() + ", 文本: '" + combinedLabel.getText() +
                             "', 文本颜色: " + combinedLabel.getForeground() + ", 字体大小: " + combinedLabel.getFont().getSize());

            // 设置实例位置和尺寸（画布中心）
            int x = Math.max(0, (editCanvas.getWidth() - instanceWidth) / 2);
            int y = Math.max(0, (editCanvas.getHeight() - instanceHeight) / 2);
            instance.setBounds(x, y, instanceWidth, instanceHeight);

            // 计算并保存相对位置（基于编辑分辨率）
            int editWidth = 1024, editHeight = 768;
            if (currentEditResolution != null) {
                String[] parts = currentEditResolution.split("x");
                if (parts.length == 2) {
                    try {
                        editWidth = Integer.parseInt(parts[0]);
                        editHeight = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("解析编辑分辨率失败: " + e.getMessage());
                    }
                }
            }

            // 基于编辑分辨率计算双锚点相对位置，确保跨分辨率一致性
            // 首先将画布坐标转换为编辑分辨率坐标
            int canvasWidth = editCanvas.getWidth();
            int canvasHeight = editCanvas.getHeight();

            // 计算缩放比例
            double scaleX = (double) editWidth / canvasWidth;
            double scaleY = (double) editHeight / canvasHeight;

            // 转换为编辑分辨率坐标（使用四舍五入提高精度）
            int editX = (int) Math.round(x * scaleX);
            int editY = (int) Math.round(y * scaleY);
            int editInstanceWidth = (int) Math.round(instanceWidth * scaleX);
            int editInstanceHeight = (int) Math.round(instanceHeight * scaleY);

            // 基于编辑分辨率计算相对位置
            com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                com.feixiang.tabletcontrol.model.RelativePosition.fromAbsolute(
                    editX, editY, editInstanceWidth, editInstanceHeight,
                    editWidth, editHeight);

            System.out.println("新建实例坐标转换: 画布坐标(" + x + "," + y + "," + instanceWidth + "," + instanceHeight +
                             ") -> 编辑坐标(" + editX + "," + editY + "," + editInstanceWidth + "," + editInstanceHeight +
                             ") 编辑分辨率(" + editWidth + "x" + editHeight + ")");

            // 保存相对位置到客户端属性
            instance.putClientProperty("positionMode", com.feixiang.tabletcontrol.model.ComponentData.PositionMode.RELATIVE);
            instance.putClientProperty("relativePosition", relativePos);

            // 保存基础尺寸百分比（100%缩放时的尺寸），用于缩放比例计算
            double baseWidthPercent = relativePos.getWidthPercent();
            double baseHeightPercent = relativePos.getHeightPercent();
            instance.putClientProperty("baseWidthPercent", baseWidthPercent);
            instance.putClientProperty("baseHeightPercent", baseHeightPercent);

            System.out.println("新建实例相对位置: " + relativePos);
            System.out.println("新建实例基础尺寸百分比: " + String.format("%.2f%%x%.2f%%", baseWidthPercent*100, baseHeightPercent*100));

            // 在所有子组件添加完成后，添加鼠标事件处理
            addInstanceMouseHandlers(instance);

            // 添加到画布
            addInstanceToEditCanvas(instance);

            // 添加到当前页面内容
            addToCurrentPageContent(instance);

            editCanvas.revalidate();
            editCanvas.repaint();

            // 设置为当前选中实例
            selectInstance(instance);

            System.out.println("添加实例成功，位置: " + x + "," + y + ", 尺寸: " + instanceWidth + "x" + instanceHeight);

            // 不再需要测试代码

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加实例失败: " + e.getMessage(),
                                        "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * 为实例添加鼠标事件处理
     */
    private void addInstanceMouseHandlers(JPanel instance) {
        // 为实例容器添加鼠标事件
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectInstance(instance);
                System.out.println("实例被点击选中");

                // 检查是否为开关按钮的双击编辑
                if (e.getClickCount() == 2) {
                    String iconPath = getInstanceIconPath(instance);
                    if (isSwitchButton(iconPath)) {
                        System.out.println("双击开关按钮，打开编辑对话框");
                        openSwitchButtonEditDialog(instance);
                    }
                }
            }
        };

        final Point[] dragStart = {null};
        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 将鼠标坐标转换为相对于实例的坐标
                Point relativePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), instance);
                dragStart[0] = relativePoint;
                selectInstance(instance);
                System.out.println("开始拖拽，起始点: " + dragStart[0] + ", 实例位置: " + instance.getLocation() +
                                 ", 事件源: " + e.getComponent().getClass().getSimpleName());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStart[0] != null) {
                    System.out.println("拖拽结束，最终位置: " + instance.getLocation());
                    // 确保最终位置正确更新到属性面板
                    SwingUtilities.invokeLater(() -> updatePropertyPanelFromInstance(instance));
                }
                dragStart[0] = null;
            }
        };

        MouseMotionAdapter motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null) {
                    // 将鼠标坐标转换为相对于实例的坐标
                    Point currentPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), instance);

                    Point currentLocation = instance.getLocation();
                    int deltaX = currentPoint.x - dragStart[0].x;
                    int deltaY = currentPoint.y - dragStart[0].y;
                    int newX = currentLocation.x + deltaX;
                    int newY = currentLocation.y + deltaY;

                    System.out.println("拖拽计算: 当前位置(" + currentLocation.x + "," + currentLocation.y +
                                     ") + 偏移(" + deltaX + "," + deltaY + ") = 新位置(" + newX + "," + newY + ")" +
                                     ", 事件源: " + e.getComponent().getClass().getSimpleName());

                    // 确保实例不会拖出画布边界
                    if (editCanvas != null) {
                        int boundedX = Math.max(0, Math.min(newX, editCanvas.getWidth() - instance.getWidth()));
                        int boundedY = Math.max(0, Math.min(newY, editCanvas.getHeight() - instance.getHeight()));

                        if (boundedX != newX || boundedY != newY) {
                            System.out.println("边界限制: (" + newX + "," + newY + ") -> (" + boundedX + "," + boundedY + ")");
                        }

                        newX = boundedX;
                        newY = boundedY;
                    }

                    // 设置新位置
                    instance.setLocation(newX, newY);

                    // 同步更新相对位置数据
                    updateRelativePositionFromBounds(instance);

                    // 强制重绘
                    instance.repaint();
                    if (editCanvas != null) {
                        editCanvas.repaint();
                    }

                    // 注意：在拖拽过程中不更新属性面板，避免性能问题和潜在的冲突

                    System.out.println("拖拽完成: 最终位置(" + newX + "," + newY + "), 实际位置(" +
                                     instance.getX() + "," + instance.getY() + ")");
                }
            }
        };

        // 为实例容器添加事件
        instance.addMouseListener(clickListener);
        instance.addMouseListener(dragListener);
        instance.addMouseMotionListener(motionListener);

        // 为所有子组件也添加相同的事件处理
        Component[] components = instance.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                comp.addMouseListener(clickListener);
                comp.addMouseListener(dragListener);
                comp.addMouseMotionListener(motionListener);
            }
        }
    }

    /**
     * 根据组件的当前bounds更新相对位置数据
     */
    private void updateRelativePositionFromBounds(JPanel instance) {
        try {
            if (editCanvas == null) return;

            int canvasWidth = editCanvas.getWidth();
            int canvasHeight = editCanvas.getHeight();

            if (canvasWidth <= 0 || canvasHeight <= 0) return;

            // 获取当前的bounds（画布坐标）
            Rectangle bounds = instance.getBounds();

            // 获取编辑分辨率
            int editWidth = 1024, editHeight = 768;
            if (currentEditResolution != null) {
                String[] parts = currentEditResolution.split("x");
                if (parts.length == 2) {
                    try {
                        editWidth = Integer.parseInt(parts[0]);
                        editHeight = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("解析编辑分辨率失败: " + e.getMessage());
                    }
                }
            }

            // 计算缩放比例
            double scaleX = (double) editWidth / canvasWidth;
            double scaleY = (double) editHeight / canvasHeight;

            // 转换为编辑分辨率坐标（使用四舍五入提高精度）
            int editX = (int) Math.round(bounds.x * scaleX);
            int editY = (int) Math.round(bounds.y * scaleY);
            int editW = (int) Math.round(bounds.width * scaleX);
            int editH = (int) Math.round(bounds.height * scaleY);

            // 基于编辑分辨率创建新的双锚点相对位置
            com.feixiang.tabletcontrol.model.RelativePosition newRelativePos =
                com.feixiang.tabletcontrol.model.RelativePosition.fromAbsolute(
                    editX, editY, editW, editH,
                    editWidth, editHeight);

            // 更新实例的相对位置数据
            instance.putClientProperty("relativePosition", newRelativePos);

            System.out.println("同步更新相对位置: 画布bounds=" + bounds +
                             " -> 编辑坐标(" + editX + "," + editY + "," + editW + "," + editH +
                             ") -> 双锚点=" + newRelativePos);

        } catch (Exception e) {
            System.out.println("更新相对位置失败: " + e.getMessage());
        }
    }

    /**
     * 选中实例
     */
    private void selectInstance(JPanel instance) {
        System.out.println("=== selectInstance被调用 ===");
        System.out.println("新选中的实例: " + instance);
        System.out.println("之前选中的实例: " + selectedInstance);

        // 清除之前选中实例的边框
        if (selectedInstance != null) {
            selectedInstance.setBorder(null);
            System.out.println("清除了之前实例的边框");
        }

        // 设置新选中实例
        selectedInstance = instance;
        selectedInstance.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        System.out.println("设置了新实例的蓝色边框");

        // 更新属性面板
        System.out.println("开始更新属性面板...");
        updatePropertyPanelFromInstance(instance);

        editCanvas.revalidate();
        editCanvas.repaint();
        System.out.println("=== selectInstance完成 ===");
    }

    /**
     * 从实例更新属性面板
     */
    private void updatePropertyPanelFromInstance(JPanel instance) {
        if (instance == null) {
            return;
        }

        // 防止循环更新
        if (isUpdatingFromInstance) {
            System.out.println("正在更新中，跳过此次更新避免循环");
            return;
        }

        try {
            isUpdatingFromInstance = true;
            System.out.println("updatePropertyPanelFromInstance: 开始更新，实例位置(" + instance.getX() + "," + instance.getY() + ")");

            // 更新位置信息
            if (xField != null) {
                xField.setText(String.valueOf(instance.getX()));
            }
            if (yField != null) {
                yField.setText(String.valueOf(instance.getY()));
            }

            // 更新文本内容（新的单标签结构）
            Component[] components = instance.getComponents();
            JLabel combinedLabel = null;

            // 找到组合标签
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    combinedLabel = (JLabel) comp;
                    break; // 现在只有一个标签
                }
            }

            if (combinedLabel != null) {
                // 更新文本内容
                if (textField != null && combinedLabel.getText() != null) {
                    textField.setText(combinedLabel.getText());
                }

                // 更新字体
                if (fontComboBox != null && combinedLabel.getFont() != null) {
                    fontComboBox.setSelectedItem(combinedLabel.getFont().getName());
                }

                // 更新字号
                if (sizeComboBox != null && combinedLabel.getFont() != null) {
                    sizeComboBox.setSelectedItem(String.valueOf(combinedLabel.getFont().getSize()));
                }

                // 更新颜色
                if (colorButton != null && combinedLabel.getForeground() != null) {
                    colorButton.setBackground(combinedLabel.getForeground());
                }

                System.out.println("文本属性已更新: " + combinedLabel.getText() +
                                 ", 字体: " + combinedLabel.getFont().getName() +
                                 ", 字号: " + combinedLabel.getFont().getSize());
            }

            // 更新缩放比例
            updateScaleSliderFromInstance(instance);

            // 更新样式按钮显示
            updateStyleButtonFromInstance(instance);

            // 更新功能类型显示
            updateFunctionTypeFromInstance(instance);

            // 更新接口配置显示
            updateInterfaceConfigFromInstance(instance);

            System.out.println("属性面板已更新: X=" + instance.getX() + ", Y=" + instance.getY());

        } catch (Exception e) {
            System.out.println("更新属性面板失败: " + e.getMessage());
        } finally {
            isUpdatingFromInstance = false;
        }
    }

    /**
     * 从实例更新接口配置显示
     */
    private void updateInterfaceConfigFromInstance(JPanel instance) {
        if (instance == null) return;

        // 查找接口配置面板中的输入框
        JTextField interfaceField = findInterfaceField();
        if (interfaceField != null) {
            String interfaceConfig = (String) instance.getClientProperty("interfaceConfig");
            interfaceField.setText(interfaceConfig != null ? interfaceConfig : "");

            // 添加文档监听器，实时保存接口配置
            interfaceField.getDocument().removeDocumentListener(interfaceDocumentListener);
            interfaceField.getDocument().addDocumentListener(interfaceDocumentListener);
        }
    }

    /**
     * 查找接口配置输入框
     */
    private JTextField findInterfaceField() {
        if (propertyBar != null) {
            return findInterfaceFieldInContainer(propertyBar);
        }
        return null;
    }

    /**
     * 在容器中递归查找接口配置输入框
     */
    private JTextField findInterfaceFieldInContainer(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // 检查是否是接口配置面板
                JTextField interfaceField = (JTextField) panel.getClientProperty("interfaceField");
                if (interfaceField != null) {
                    return interfaceField;
                }
                // 递归查找
                JTextField found = findInterfaceFieldInContainer(panel);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // 接口配置文档监听器
    private javax.swing.event.DocumentListener interfaceDocumentListener = new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            updateInterfaceConfig();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            updateInterfaceConfig();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            updateInterfaceConfig();
        }

        private void updateInterfaceConfig() {
            if (selectedInstance != null && !isUpdatingFromInstance) {
                JTextField interfaceField = findInterfaceField();
                if (interfaceField != null) {
                    String interfaceConfig = interfaceField.getText();
                    selectedInstance.putClientProperty("interfaceConfig", interfaceConfig);
                    System.out.println("接口配置已更新: " + interfaceConfig);
                }
            }
        }
    };

    /**
     * 添加背景到画布
     */
    private void addBackgroundToCanvas() {
        try {
            // 指定背景文件路径
            String backgroundPath = "E:\\平板中控windows版\\java-version\\USERDATA\\background";
            File backgroundDir = new File(backgroundPath);

            // 确保背景目录存在
            if (!backgroundDir.exists()) {
                backgroundDir.mkdirs();
                System.out.println("创建背景目录: " + backgroundPath);
            }

            // 创建文件选择器，限制只能选择指定目录下的文件
            JFileChooser fileChooser = new JFileChooser(backgroundDir);
            fileChooser.setDialogTitle("选择背景文件");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // 设置文件过滤器，只显示图片和视频文件
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    String name = file.getName().toLowerCase();
                    return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                           name.endsWith(".png") || name.endsWith(".gif") ||
                           name.endsWith(".bmp") || name.endsWith(".mp4") ||
                           name.endsWith(".avi") || name.endsWith(".mov") ||
                           name.endsWith(".wmv") || name.endsWith(".flv");
                }

                @Override
                public String getDescription() {
                    return "图片和视频文件 (*.jpg, *.png, *.gif, *.mp4, *.avi, etc.)";
                }
            });

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // 验证文件是否在指定目录下
                if (!isFileInDirectory(selectedFile, backgroundDir)) {
                    JOptionPane.showMessageDialog(this,
                        "只能选择 " + backgroundPath + " 目录下的文件！",
                        "路径限制", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 添加背景到画布
                addBackgroundToCanvas(selectedFile);
            }

        } catch (Exception e) {
            System.out.println("添加背景失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "添加背景失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 验证文件是否在指定目录下
     */
    private boolean isFileInDirectory(File file, File directory) {
        try {
            String filePath = file.getCanonicalPath();
            String dirPath = directory.getCanonicalPath();
            return filePath.startsWith(dirPath);
        } catch (Exception e) {
            System.out.println("验证文件路径失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 添加背景文件到画布（实际实现）
     */
    private void addBackgroundToCanvas(File backgroundFile) {
        try {
            if (editCanvas == null) {
                JOptionPane.showMessageDialog(this, "画布未初始化", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fileName = backgroundFile.getName().toLowerCase();
            boolean isVideo = fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                             fileName.endsWith(".mov") || fileName.endsWith(".wmv") ||
                             fileName.endsWith(".flv");

            if (isVideo) {
                // 视频背景处理
                addVideoBackground(backgroundFile);
            } else {
                // 图片背景处理
                addImageBackground(backgroundFile);
            }

            System.out.println("背景添加成功: " + backgroundFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("添加背景文件失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "添加背景失败: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 添加图片背景
     */
    private void addImageBackground(File imageFile) {
        try {
            // 读取图片
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                throw new IOException("无法读取图片文件");
            }

            // 获取画布尺寸
            int canvasWidth = editCanvas.getWidth();
            int canvasHeight = editCanvas.getHeight();

            // 缩放图片到画布大小
            Image scaledImage = originalImage.getScaledInstance(canvasWidth, canvasHeight, Image.SCALE_SMOOTH);
            ImageIcon backgroundIcon = new ImageIcon(scaledImage);

            // 创建背景标签
            JLabel backgroundLabel = new JLabel(backgroundIcon);
            backgroundLabel.setBounds(0, 0, canvasWidth, canvasHeight);
            backgroundLabel.setOpaque(true);  // 设置为不透明，确保背景显示
            backgroundLabel.setBackground(Color.BLACK);  // 设置背景色

            // 移除现有背景（如果有）
            removeExistingBackground();

            // 设置背景属性
            backgroundLabel.putClientProperty("isBackground", true);
            backgroundLabel.putClientProperty("backgroundFile", imageFile.getAbsolutePath());

            // 为背景标签添加右键菜单支持
            addBackgroundLabelContextMenu(backgroundLabel);

            // 获取现有的所有实例组件
            java.util.List<JPanel> existingInstances = new java.util.ArrayList<>();
            Component[] components = editCanvas.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    existingInstances.add((JPanel) comp);
                }
            }

            // 添加背景到画布
            editCanvas.add(backgroundLabel);

            // 确保背景在最底层
            editCanvas.setComponentZOrder(backgroundLabel, editCanvas.getComponentCount() - 1);

            // 确保所有现有实例在背景之上
            for (int i = 0; i < existingInstances.size(); i++) {
                editCanvas.setComponentZOrder(existingInstances.get(i), i);
            }

            System.out.println("背景已添加，层级已设置：背景在底层，" + existingInstances.size() + " 个实例在上层");

            // 保存背景信息到当前页面
            if (currentPageName != null) {
                pageBackgrounds.put(currentPageName, imageFile.getAbsolutePath());
                System.out.println("保存页面背景信息: " + currentPageName + " -> " + imageFile.getAbsolutePath());
            }

            // 刷新画布
            editCanvas.revalidate();
            editCanvas.repaint();

        } catch (Exception e) {
            throw new RuntimeException("添加图片背景失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加视频背景（暂时显示提示信息）
     */
    private void addVideoBackground(File videoFile) {
        // 视频背景功能较复杂，暂时显示提示
        JOptionPane.showMessageDialog(this,
            "视频背景功能正在开发中，当前版本暂不支持。\n选择的文件: " + videoFile.getName(),
            "功能提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 移除现有背景
     */
    private void removeExistingBackground() {
        if (editCanvas == null) return;

        Component[] components = editCanvas.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Boolean isBackground = (Boolean) label.getClientProperty("isBackground");
                if (isBackground != null && isBackground) {
                    editCanvas.remove(comp);
                    System.out.println("移除了现有背景");

                    // 从背景信息中移除
                    if (currentPageName != null) {
                        pageBackgrounds.remove(currentPageName);
                        System.out.println("从背景信息中移除: " + currentPageName);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 为编辑画布添加右键菜单支持
     */
    private void addCanvasContextMenu(JPanel canvas) {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showCanvasContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showCanvasContextMenu(e);
                }
            }

            private void showCanvasContextMenu(MouseEvent e) {
                // 检查点击位置是否在背景图片上
                Component clickedComponent = canvas.getComponentAt(e.getPoint());
                if (isBackgroundComponent(clickedComponent)) {
                    showBackgroundContextMenu(e);
                }
            }
        });
    }

    /**
     * 检查组件是否为背景组件
     */
    private boolean isBackgroundComponent(Component component) {
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            Boolean isBackground = (Boolean) label.getClientProperty("isBackground");
            return isBackground != null && isBackground;
        }
        return false;
    }

    /**
     * 显示背景图片右键菜单
     */
    private void showBackgroundContextMenu(MouseEvent e) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(Color.WHITE);
        contextMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 4, 8, 4)
        ));

        // 重新选择背景图片菜单项
        JMenuItem changeBackgroundItem = new JMenuItem("重新选择背景图片");
        changeBackgroundItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        changeBackgroundItem.setForeground(new Color(55, 65, 81));
        changeBackgroundItem.setBackground(Color.WHITE);
        changeBackgroundItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        changeBackgroundItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        changeBackgroundItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                changeBackgroundItem.setBackground(new Color(243, 244, 246));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeBackgroundItem.setBackground(Color.WHITE);
            }
        });

        // 添加点击事件
        changeBackgroundItem.addActionListener(e1 -> {
            contextMenu.setVisible(false);
            // 复用现有的添加背景逻辑
            addBackgroundToCanvas();
        });

        contextMenu.add(changeBackgroundItem);

        // 显示菜单
        contextMenu.show(editCanvas, e.getX(), e.getY());
    }

    /**
     * 为背景标签添加右键菜单支持
     */
    private void addBackgroundLabelContextMenu(JLabel backgroundLabel) {
        backgroundLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showBackgroundLabelContextMenu(e, backgroundLabel);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showBackgroundLabelContextMenu(e, backgroundLabel);
                }
            }
        });
    }

    /**
     * 显示背景标签右键菜单
     */
    private void showBackgroundLabelContextMenu(MouseEvent e, JLabel backgroundLabel) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(Color.WHITE);
        contextMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 4, 8, 4)
        ));

        // 重新选择背景图片菜单项
        JMenuItem changeBackgroundItem = new JMenuItem("重新选择背景图片");
        changeBackgroundItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        changeBackgroundItem.setForeground(new Color(55, 65, 81));
        changeBackgroundItem.setBackground(Color.WHITE);
        changeBackgroundItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        changeBackgroundItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        changeBackgroundItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                changeBackgroundItem.setBackground(new Color(243, 244, 246));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeBackgroundItem.setBackground(Color.WHITE);
            }
        });

        // 添加点击事件
        changeBackgroundItem.addActionListener(e1 -> {
            contextMenu.setVisible(false);
            // 复用现有的添加背景逻辑
            addBackgroundToCanvas();
        });

        contextMenu.add(changeBackgroundItem);

        // 显示菜单
        contextMenu.show(backgroundLabel, e.getX(), e.getY());
    }

    /**
     * 添加图片到画布
     */
    private void addImageToCanvas() {
        try {
            // 指定图片文件路径
            String picturePath = System.getProperty("user.dir") + File.separator + "USERDATA" + File.separator + "picture";
            File pictureDir = new File(picturePath);

            // 确保图片目录存在
            if (!pictureDir.exists()) {
                pictureDir.mkdirs();
                System.out.println("创建图片目录: " + picturePath);
            }

            // 创建文件选择器，限制只能选择指定目录下的文件
            JFileChooser fileChooser = new JFileChooser(pictureDir);
            fileChooser.setDialogTitle("选择图片文件");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // 设置文件过滤器，只显示图片文件
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    String name = file.getName().toLowerCase();
                    return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                           name.endsWith(".png") || name.endsWith(".gif") ||
                           name.endsWith(".bmp") || name.endsWith(".webp");
                }

                @Override
                public String getDescription() {
                    return "图片文件 (*.jpg, *.png, *.gif, *.bmp, *.webp)";
                }
            });

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // 验证文件是否在指定目录下
                if (!isFileInDirectory(selectedFile, pictureDir)) {
                    JOptionPane.showMessageDialog(this,
                        "只能选择 USERDATA/picture 目录下的图片文件",
                        "路径限制",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 创建图片组件并添加到画布
                createImageComponent(selectedFile);

                System.out.println("成功添加图片到画布: " + selectedFile.getName());
                JOptionPane.showMessageDialog(this,
                    "图片已添加到画布: " + selectedFile.getName(),
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            System.err.println("添加图片到画布失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "添加图片失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 创建图片组件并添加到画布
     */
    private void createImageComponent(File imageFile) {
        try {
            // 验证当前页面
            if (currentPageName == null || currentPageName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先创建或选择一个页面", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 创建图片实例容器
            JPanel imageInstance = new JPanel();
            imageInstance.setLayout(new BorderLayout());
            imageInstance.setOpaque(false);
            imageInstance.setEnabled(true);
            imageInstance.setFocusable(true);

            // 读取图片并获取原始尺寸
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            originalIcon.setDescription(imageFile.getAbsolutePath()); // 设置描述为文件路径，用于保存
            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();

            // 计算宽高比
            double aspectRatio = (double) originalWidth / originalHeight;

            // 设置目标显示区域的最大尺寸（图片组件可以比按钮大一些）
            int maxDisplayWidth = 200;
            int maxDisplayHeight = 150;

            // 根据宽高比和最大尺寸限制计算实际显示尺寸
            int instanceWidth, instanceHeight;

            if (aspectRatio > (double) maxDisplayWidth / maxDisplayHeight) {
                // 图片比较宽，以宽度为准
                instanceWidth = maxDisplayWidth;
                instanceHeight = (int) (maxDisplayWidth / aspectRatio);
            } else {
                // 图片比较高，以高度为准
                instanceHeight = maxDisplayHeight;
                instanceWidth = (int) (maxDisplayHeight * aspectRatio);
            }

            // 确保最小尺寸
            if (instanceWidth < 60) {
                instanceWidth = 60;
                instanceHeight = (int) (60 / aspectRatio);
            }
            if (instanceHeight < 40) {
                instanceHeight = 40;
                instanceWidth = (int) (40 * aspectRatio);
            }

            imageInstance.setSize(instanceWidth, instanceHeight);
            System.out.println("图片原始尺寸: " + originalWidth + "x" + originalHeight +
                             ", 宽高比: " + String.format("%.2f", aspectRatio) +
                             ", 实例尺寸: " + instanceWidth + "x" + instanceHeight);

            // 创建缩放后的图片
            Image scaledImg = originalIcon.getImage().getScaledInstance(instanceWidth, instanceHeight, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImg);

            // 创建图片标签
            JLabel imageLabel = new JLabel(scaledIcon);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            imageLabel.setBounds(0, 0, instanceWidth, instanceHeight);

            // 设置图片标签的属性，用于保存
            imageLabel.putClientProperty("originalIcon", originalIcon);
            imageLabel.setText(""); // 确保没有文本，这是纯图片组件

            // 将图片标签添加到实例容器
            imageInstance.add(imageLabel, BorderLayout.CENTER);

            // 设置实例位置（画布中心）
            int canvasWidth = editCanvas.getWidth();
            int canvasHeight = editCanvas.getHeight();
            int x = (canvasWidth - instanceWidth) / 2;
            int y = (canvasHeight - instanceHeight) / 2;
            imageInstance.setBounds(x, y, instanceWidth, instanceHeight);

            // 保存原始图片信息到实例属性
            imageInstance.putClientProperty("originalIcon", originalIcon);
            imageInstance.putClientProperty("originalWidth", instanceWidth);
            imageInstance.putClientProperty("originalHeight", instanceHeight);
            imageInstance.putClientProperty("imageFile", imageFile);
            imageInstance.putClientProperty("componentType", "图片");
            imageInstance.putClientProperty("functionType", "图片"); // 设置功能类型为图片

            // 创建相对位置数据
            com.feixiang.tabletcontrol.model.RelativePosition relativePos = createRelativePosition(x, y, instanceWidth, instanceHeight);
            imageInstance.putClientProperty("relativePosition", relativePos);

            // 添加鼠标事件监听器
            addInstanceMouseListeners(imageInstance);

            // 添加到画布
            editCanvas.add(imageInstance);
            editCanvas.setComponentZOrder(imageInstance, 0); // 设置为最顶层

            // 刷新画布
            editCanvas.revalidate();
            editCanvas.repaint();

            System.out.println("成功创建图片组件: " + imageFile.getName() +
                             ", 位置: (" + x + ", " + y + "), 尺寸: " + instanceWidth + "x" + instanceHeight);

        } catch (Exception e) {
            System.err.println("创建图片组件失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建图片组件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建相对位置数据
     */
    private com.feixiang.tabletcontrol.model.RelativePosition createRelativePosition(int x, int y, int width, int height) {
        // 获取编辑画布尺寸
        int canvasWidth = editCanvas.getWidth();
        int canvasHeight = editCanvas.getHeight();

        // 使用标准编辑分辨率
        int editWidth = 1024;
        int editHeight = 768;

        // 计算编辑分辨率下的坐标
        double scaleX = (double) editWidth / canvasWidth;
        double scaleY = (double) editHeight / canvasHeight;

        int editX = (int) (x * scaleX);
        int editY = (int) (y * scaleY);
        int editW = (int) (width * scaleX);
        int editH = (int) (height * scaleY);

        // 创建相对位置
        return com.feixiang.tabletcontrol.model.RelativePosition.fromAbsolute(
            editX, editY, editW, editH, editWidth, editHeight);
    }

    /**
     * 为实例添加鼠标监听器
     */
    private void addInstanceMouseListeners(JPanel instance) {
        // 创建鼠标监听器
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 选中实例
                selectInstance(instance);
                System.out.println("图片实例被点击");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // 开始拖拽
                if (SwingUtilities.isLeftMouseButton(e)) {
                    selectedInstance = instance;
                    dragStartPoint = e.getPoint();
                    SwingUtilities.convertPointToScreen(dragStartPoint, instance);
                    SwingUtilities.convertPointFromScreen(dragStartPoint, editCanvas);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 结束拖拽
                dragStartPoint = null;
            }
        };

        MouseMotionAdapter motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 处理拖拽
                if (dragStartPoint != null && selectedInstance == instance) {
                    Point currentPoint = e.getPoint();
                    SwingUtilities.convertPointToScreen(currentPoint, instance);
                    SwingUtilities.convertPointFromScreen(currentPoint, editCanvas);

                    int deltaX = currentPoint.x - dragStartPoint.x;
                    int deltaY = currentPoint.y - dragStartPoint.y;

                    Rectangle bounds = instance.getBounds();
                    int newX = Math.max(0, Math.min(editCanvas.getWidth() - bounds.width, bounds.x + deltaX));
                    int newY = Math.max(0, Math.min(editCanvas.getHeight() - bounds.height, bounds.y + deltaY));

                    instance.setLocation(newX, newY);
                    dragStartPoint = currentPoint;

                    // 更新相对位置
                    com.feixiang.tabletcontrol.model.RelativePosition newRelativePos =
                        createRelativePosition(newX, newY, bounds.width, bounds.height);
                    instance.putClientProperty("relativePosition", newRelativePos);

                    editCanvas.repaint();
                }
            }
        };

        // 添加监听器到实例
        instance.addMouseListener(mouseListener);
        instance.addMouseMotionListener(motionListener);

        // 为实例内的组件也添加监听器
        Component[] components = instance.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                comp.addMouseListener(mouseListener);
                comp.addMouseMotionListener(motionListener);
            }
        }
    }

    /**
     * 添加文本到画布
     */
    private void addTextToCanvas() {
        // TODO: 实现添加文本功能
        JOptionPane.showMessageDialog(this, "添加文本功能待实现", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 添加视频到画布
     */
    private void addVideoToCanvas() {
        // TODO: 实现添加视频功能
        JOptionPane.showMessageDialog(this, "添加视频功能待实现", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 复制选中的实例
     */
    private void copySelectedInstance() {
        if (selectedInstance == null || editCanvas == null) {
            return;
        }

        try {
            // 创建新实例面板，使用与原实例相同的布局
            JPanel newInstance = new JPanel();
            newInstance.setLayout(null); // 使用绝对布局
            newInstance.setOpaque(false);
            newInstance.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));

            // 确保新实例可以接收鼠标事件
            newInstance.setEnabled(true);
            newInstance.setFocusable(true);

            // 设置新实例的尺寸（与原实例相同）
            newInstance.setSize(selectedInstance.getSize());

            // 复制相对位置数据并稍微偏移位置（避免完全重叠）
            com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                (com.feixiang.tabletcontrol.model.RelativePosition) selectedInstance.getClientProperty("relativePosition");
            if (relativePos != null) {
                // 使用双锚点系统，稍微偏移位置（2%的偏移）
                double offsetPercent = 0.02; // 2%偏移
                double newTopLeftX = Math.min(relativePos.getTopLeftX() + offsetPercent, 1.0 - relativePos.getWidthPercent());
                double newTopLeftY = Math.min(relativePos.getTopLeftY() + offsetPercent, 1.0 - relativePos.getHeightPercent());
                double newBottomRightX = newTopLeftX + relativePos.getWidthPercent();
                double newBottomRightY = newTopLeftY + relativePos.getHeightPercent();

                // 确保不超出边界
                if (newBottomRightX > 1.0) {
                    newBottomRightX = 1.0;
                    newTopLeftX = newBottomRightX - relativePos.getWidthPercent();
                }
                if (newBottomRightY > 1.0) {
                    newBottomRightY = 1.0;
                    newTopLeftY = newBottomRightY - relativePos.getHeightPercent();
                }

                com.feixiang.tabletcontrol.model.RelativePosition newRelativePos =
                    com.feixiang.tabletcontrol.model.RelativePosition.createDualAnchor(
                        newTopLeftX, newTopLeftY, newBottomRightX, newBottomRightY);
                newInstance.putClientProperty("relativePosition", newRelativePos);

                // 基于新的百分比位置设置实际坐标
                if (editCanvas != null) {
                    int canvasWidth = editCanvas.getWidth();
                    int canvasHeight = editCanvas.getHeight();
                    if (canvasWidth > 0 && canvasHeight > 0) {
                        com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition pos =
                            newRelativePos.toAbsolute(canvasWidth, canvasHeight);
                        newInstance.setBounds(pos.x, pos.y, pos.width, pos.height);
                        System.out.println("复制实例位置(百分比偏移): " + newRelativePos + " -> 画布坐标: " +
                                         "(" + pos.x + "," + pos.y + "," + pos.width + "," + pos.height + ")");
                    }
                }
            }

            // 复制所有子组件（保持图层顺序和位置）
            Component[] components = selectedInstance.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel originalLabel = (JLabel) comp;
                    JLabel newLabel = new JLabel();

                    // 复制图标
                    if (originalLabel.getIcon() != null) {
                        newLabel.setIcon(originalLabel.getIcon());
                    }

                    // 复制文本
                    if (originalLabel.getText() != null) {
                        newLabel.setText(originalLabel.getText());
                    }

                    // 复制字体和颜色
                    newLabel.setFont(originalLabel.getFont());
                    newLabel.setForeground(originalLabel.getForeground());

                    // 复制所有对齐方式
                    newLabel.setHorizontalAlignment(originalLabel.getHorizontalAlignment());
                    newLabel.setVerticalAlignment(originalLabel.getVerticalAlignment());
                    newLabel.setHorizontalTextPosition(originalLabel.getHorizontalTextPosition());
                    newLabel.setVerticalTextPosition(originalLabel.getVerticalTextPosition());

                    // 复制透明度设置
                    newLabel.setOpaque(originalLabel.isOpaque());

                    // 复制位置和尺寸（绝对布局需要）
                    newLabel.setBounds(originalLabel.getBounds());

                    // 复制客户端属性（原始图标等）
                    ImageIcon originalIcon = (ImageIcon) originalLabel.getClientProperty("originalIcon");
                    if (originalIcon != null) {
                        newLabel.putClientProperty("originalIcon", originalIcon);
                    }

                    // 复制原始字体大小
                    Integer originalFontSize = (Integer) originalLabel.getClientProperty("originalFontSize");
                    if (originalFontSize != null) {
                        newLabel.putClientProperty("originalFontSize", originalFontSize);
                    } else {
                        // 如果原标签没有保存原始字体大小，使用当前字体大小
                        newLabel.putClientProperty("originalFontSize", originalLabel.getFont().getSize());
                    }

                    // 确保标签不会阻止鼠标事件传播
                    newLabel.setEnabled(true);
                    newLabel.setFocusable(false);

                    newInstance.add(newLabel);
                }
            }

            // 位置已通过百分比相对定位设置，不需要额外的setLocation

            // 添加鼠标事件处理
            addInstanceMouseHandlers(newInstance);

            // 添加到画布
            addInstanceToEditCanvas(newInstance);

            // 添加到当前页面内容
            addToCurrentPageContent(newInstance);

            editCanvas.revalidate();
            editCanvas.repaint();

            // 选中新创建的实例
            selectInstance(newInstance);

            System.out.println("复制实例成功");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "复制实例失败: " + e.getMessage(),
                                        "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 删除选中的实例
     */
    private void deleteSelectedInstance() {
        if (selectedInstance == null || editCanvas == null) {
            return;
        }

        try {
            // 从画布中移除实例
            editCanvas.remove(selectedInstance);
            editCanvas.revalidate();
            editCanvas.repaint();

            // 从页面内容数据中移除实例（强制更新页面内容）
            if (currentPageName != null) {
                // 移除旧的页面内容，强制重新保存
                pageContents.remove(currentPageName);
                // 重新保存当前页面内容（基于更新后的画布）
                saveCurrentPageContent();
                System.out.println("已更新页面内容数据，移除删除的实例");
            }

            // 清除选中状态
            selectedInstance = null;

            // 重置属性面板为默认状态
            resetPropertyPanel();

            System.out.println("删除实例成功");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除实例失败: " + e.getMessage(),
                                        "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 重置属性面板为默认状态
     */
    private void resetPropertyPanel() {
        try {
            if (textField != null) textField.setText("文本");
            if (fontComboBox != null) fontComboBox.setSelectedIndex(0);
            if (sizeComboBox != null) sizeComboBox.setSelectedItem("12");
            if (colorButton != null) colorButton.setBackground(Color.BLACK);
            if (xField != null) xField.setText("0");
            if (yField != null) yField.setText("0");
            if (scaleSlider != null) {
                scaleSlider.setValue(100);
                if (scaleLabel != null) scaleLabel.setText("缩放比例: 100%");
            }
            if (styleButton != null) styleButton.setText("选择");

            System.out.println("属性面板已重置为默认状态");
        } catch (Exception e) {
            System.out.println("重置属性面板失败: " + e.getMessage());
        }
    }

    /**
     * 从属性面板更新实例
     */
    private void updateInstanceFromPropertyPanel() {
        System.out.println("updateInstanceFromPropertyPanel被调用");
        System.out.println("selectedInstance: " + (selectedInstance != null ? "存在" : "null"));

        if (selectedInstance == null) {
            System.out.println("selectedInstance为null，无法更新");
            return;
        }

        // 防止循环更新
        if (isUpdatingFromInstance) {
            System.out.println("正在从实例更新属性面板，跳过此次属性面板更新避免循环");
            return;
        }

        try {
            // 更新位置
            if (xField != null && yField != null) {
                try {
                    int x = Integer.parseInt(xField.getText());
                    int y = Integer.parseInt(yField.getText());

                    System.out.println("尝试设置实例位置: " + x + ", " + y);
                    selectedInstance.setLocation(x, y);

                    // 强制刷新画布
                    if (editCanvas != null) {
                        editCanvas.revalidate();
                        editCanvas.repaint();
                    }

                    System.out.println("实例位置已更新为: " + selectedInstance.getX() + ", " + selectedInstance.getY());
                } catch (NumberFormatException e) {
                    System.out.println("无效的坐标输入: " + e.getMessage());
                }
            }

            // 更新文本内容和样式（新的单标签结构）
            Component[] components = selectedInstance.getComponents();
            JLabel combinedLabel = null;

            // 找到组合标签（有图标和文本的标签）
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    combinedLabel = (JLabel) comp;
                    break; // 现在只有一个标签
                }
            }

            if (combinedLabel != null) {
                // 更新文本内容
                if (textField != null) {
                    combinedLabel.setText(textField.getText());
                }

                // 更新字体
                if (fontComboBox != null && sizeComboBox != null) {
                    String fontName = (String) fontComboBox.getSelectedItem();
                    String sizeStr = (String) sizeComboBox.getSelectedItem();
                    try {
                        int fontSize = Integer.parseInt(sizeStr);
                        Font newFont = new Font(fontName, Font.PLAIN, fontSize);
                        combinedLabel.setFont(newFont);
                    } catch (NumberFormatException e) {
                        // 忽略无效的字号
                    }
                }

                // 更新颜色
                if (colorButton != null) {
                    combinedLabel.setForeground(colorButton.getBackground());
                }

                System.out.println("文本属性已更新: " + combinedLabel.getText() +
                                 ", 字体: " + combinedLabel.getFont().getName() +
                                 ", 字号: " + combinedLabel.getFont().getSize());
            }

            // 刷新画布
            if (editCanvas != null) {
                editCanvas.revalidate();
                editCanvas.repaint();
            }

            System.out.println("实例已根据属性面板更新");

        } catch (Exception e) {
            System.out.println("从属性面板更新实例失败: " + e.getMessage());
        }
    }

    /**
     * 测试属性面板控件
     */
    private void testPropertyPanelControls() {
        System.out.println("=== 测试属性面板控件 ===");
        System.out.println("textField: " + (textField != null ? "存在" : "null"));
        System.out.println("fontComboBox: " + (fontComboBox != null ? "存在" : "null"));
        System.out.println("sizeComboBox: " + (sizeComboBox != null ? "存在" : "null"));
        System.out.println("colorButton: " + (colorButton != null ? "存在" : "null"));
        System.out.println("xField: " + (xField != null ? "存在" : "null"));
        System.out.println("yField: " + (yField != null ? "存在" : "null"));

        if (textField != null) {
            System.out.println("textField当前值: " + textField.getText());
        }
        if (xField != null) {
            System.out.println("xField当前值: " + xField.getText());
        }
        if (yField != null) {
            System.out.println("yField当前值: " + yField.getText());
        }

        // 测试手动设置值
        if (textField != null) {
            System.out.println("手动设置textField为'测试文本'");
            textField.setText("测试文本");
        }
        if (xField != null) {
            System.out.println("手动设置xField为'100'");
            xField.setText("100");
        }
        if (yField != null) {
            System.out.println("手动设置yField为'50'");
            yField.setText("50");
        }

        System.out.println("=== 属性面板控件测试完成 ===");
    }

    /**
     * 更新实例缩放（保持中心点不变，同步缩放图片和文本）
     */
    private void updateInstanceScale(int scalePercent) {
        if (selectedInstance == null) {
            System.out.println("updateInstanceScale: selectedInstance为null");
            return;
        }

        try {
            // 计算缩放比例
            double scale = scalePercent / 100.0;

            // 获取当前画布尺寸
            int canvasWidth = editCanvas != null ? editCanvas.getWidth() : 600;
            int canvasHeight = editCanvas != null ? editCanvas.getHeight() : 400;

            // 获取或初始化基准尺寸（100%时的像素尺寸）
            Integer baseWidth = (Integer) selectedInstance.getClientProperty("originalWidth");
            Integer baseHeight = (Integer) selectedInstance.getClientProperty("originalHeight");

            if (baseWidth == null || baseHeight == null) {
                // 如果没有基准尺寸，使用当前尺寸作为基准
                Rectangle currentBounds = selectedInstance.getBounds();
                baseWidth = currentBounds.width;
                baseHeight = currentBounds.height;
                selectedInstance.putClientProperty("originalWidth", baseWidth);
                selectedInstance.putClientProperty("originalHeight", baseHeight);
                System.out.println("初始化基准尺寸: " + baseWidth + "x" + baseHeight);
            }

            // 计算新的像素尺寸（保持宽高比）
            int newWidth = (int) Math.round(baseWidth * scale);
            int newHeight = (int) Math.round(baseHeight * scale);

            // 确保最小尺寸
            newWidth = Math.max(10, newWidth);
            newHeight = Math.max(10, newHeight);

            // 获取当前中心点
            Rectangle currentBounds = selectedInstance.getBounds();
            int centerX = currentBounds.x + currentBounds.width / 2;
            int centerY = currentBounds.y + currentBounds.height / 2;

            // 计算新位置（保持中心点不变）
            int newX = centerX - newWidth / 2;
            int newY = centerY - newHeight / 2;

            // 确保不超出画布边界
            newX = Math.max(0, Math.min(newX, canvasWidth - newWidth));
            newY = Math.max(0, Math.min(newY, canvasHeight - newHeight));

            // 更新实例位置和尺寸
            selectedInstance.setBounds(newX, newY, newWidth, newHeight);

            // 同步更新实例内部组件（图片和文本）
            updateInstanceComponentsSync(newWidth, newHeight, scale);

            // 更新相对位置数据
            updateRelativePositionFromBounds(selectedInstance);

            // 刷新画布
            if (editCanvas != null) {
                editCanvas.revalidate();
                editCanvas.repaint();
            }

            System.out.println("实例缩放已更新: " + scalePercent + "%, 新尺寸: " + newWidth + "x" + newHeight +
                             ", 新位置: " + newX + "," + newY);

        } catch (Exception e) {
            System.out.println("更新实例缩放失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 同步更新实例内部组件（图片和文本同时缩放）
     */
    private void updateInstanceComponentsSync(int newWidth, int newHeight, double scale) {
        if (selectedInstance == null) return;

        Component[] components = selectedInstance.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;

                // 更新标签尺寸
                label.setBounds(0, 0, newWidth, newHeight);

                // 同步更新图片
                if (label.getIcon() != null) {
                    ImageIcon originalIcon = getOriginalIcon(label);
                    if (originalIcon != null) {
                        // 使用高质量缩放创建新图片
                        Image scaledImg = createHighQualityScaledImage(originalIcon.getImage(), newWidth, newHeight);
                        label.setIcon(new ImageIcon(scaledImg));
                        System.out.println("同步缩放图片: " + newWidth + "x" + newHeight);
                    }
                }

                // 同步更新字体
                Integer originalFontSize = (Integer) label.getClientProperty("originalFontSize");
                if (originalFontSize != null) {
                    // 基于缩放比例计算新字体大小
                    int newFontSize = (int) Math.round(originalFontSize * scale);
                    newFontSize = Math.max(8, newFontSize); // 最小字体大小

                    Font currentFont = label.getFont();
                    if (currentFont != null) {
                        Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), newFontSize);
                        label.setFont(newFont);
                        System.out.println("同步缩放字体: " + originalFontSize + " -> " + newFontSize + " (scale=" + String.format("%.2f", scale) + ")");
                    }
                } else {
                    // 如果没有保存原始字体大小，保存当前字体大小作为基准
                    Font currentFont = label.getFont();
                    if (currentFont != null) {
                        label.putClientProperty("originalFontSize", currentFont.getSize());
                    }
                }
            }
        }
    }



    /**
     * 更新实例样式（保持当前缩放比例）
     */
    private void updateInstanceStyle(File styleFile) {
        if (selectedInstance == null) {
            System.out.println("updateInstanceStyle: selectedInstance为null");
            return;
        }

        try {
            // 获取实例当前的中心点位置
            int currentX = selectedInstance.getX();
            int currentY = selectedInstance.getY();
            int currentWidth = selectedInstance.getWidth();
            int currentHeight = selectedInstance.getHeight();
            int centerX = currentX + currentWidth / 2;
            int centerY = currentY + currentHeight / 2;

            // 获取当前的缩放比例
            int currentScalePercent = 100; // 默认100%
            if (scaleSlider != null) {
                currentScalePercent = scaleSlider.getValue();
            }
            double currentScale = currentScalePercent / 100.0;

            // 加载新的样式图片并获取其原始尺寸
            ImageIcon newOriginalIcon = new ImageIcon(styleFile.getAbsolutePath());
            int originalWidth = newOriginalIcon.getIconWidth();
            int originalHeight = newOriginalIcon.getIconHeight();

            // 使用与addInstanceToCanvas相同的自适应尺寸算法计算基础尺寸（100%时的尺寸）
            double aspectRatio = (double) originalWidth / originalHeight;
            int maxDisplayWidth = 120;
            int maxDisplayHeight = 80;

            int baseWidth, baseHeight;
            if (aspectRatio > (double) maxDisplayWidth / maxDisplayHeight) {
                // 图片比较宽，以宽度为准
                baseWidth = maxDisplayWidth;
                baseHeight = (int) (maxDisplayWidth / aspectRatio);
            } else {
                // 图片比较高，以高度为准
                baseHeight = maxDisplayHeight;
                baseWidth = (int) (maxDisplayHeight * aspectRatio);
            }

            // 确保最小尺寸
            if (baseWidth < 40) {
                baseWidth = 40;
                baseHeight = (int) (40 / aspectRatio);
            }
            if (baseHeight < 20) {
                baseHeight = 20;
                baseWidth = (int) (20 * aspectRatio);
            }

            // 应用当前的缩放比例
            int newWidth = (int) (baseWidth * currentScale);
            int newHeight = (int) (baseHeight * currentScale);

            // 计算新位置（保持中心点不变）
            int newX = centerX - newWidth / 2;
            int newY = centerY - newHeight / 2;

            // 确保不会超出画布边界
            if (editCanvas != null) {
                newX = Math.max(0, Math.min(newX, editCanvas.getWidth() - newWidth));
                newY = Math.max(0, Math.min(newY, editCanvas.getHeight() - newHeight));
            }

            // 更新实例位置和尺寸
            selectedInstance.setBounds(newX, newY, newWidth, newHeight);

            // 更新相对位置数据
            updateRelativePositionFromBounds(selectedInstance);

            // 重新计算并保存基础尺寸百分比（基于新的基础尺寸）
            if (editCanvas != null && editCanvas.getWidth() > 0 && editCanvas.getHeight() > 0) {
                double newBaseWidthPercent = (double) baseWidth / editCanvas.getWidth();
                double newBaseHeightPercent = (double) baseHeight / editCanvas.getHeight();
                selectedInstance.putClientProperty("baseWidthPercent", newBaseWidthPercent);
                selectedInstance.putClientProperty("baseHeightPercent", newBaseHeightPercent);
                System.out.println("更新样式后重新计算基础尺寸百分比: " +
                                 String.format("%.2f%%x%.2f%%", newBaseWidthPercent*100, newBaseHeightPercent*100));
            }

            // 更新实例的原始尺寸属性（保存100%时的尺寸）
            selectedInstance.putClientProperty("originalWidth", baseWidth);
            selectedInstance.putClientProperty("originalHeight", baseHeight);

            // 创建缩放后的图片
            Image scaledImg = createHighQualityScaledImage(newOriginalIcon.getImage(), newWidth, newHeight);

            // 更新实例内部组件
            Component[] components = selectedInstance.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    // 更新标签的尺寸和图标
                    label.setBounds(0, 0, newWidth, newHeight);
                    label.setIcon(new ImageIcon(scaledImg));

                    // 保存新的原始图标，并设置描述为文件路径
                    newOriginalIcon.setDescription(styleFile.getAbsolutePath());
                    label.putClientProperty("originalIcon", newOriginalIcon);

                    // 更新labelData中的iconPath
                    Object existingLabelData = selectedInstance.getClientProperty("labelData");
                    java.util.Map<String, Object> labelDataMap;
                    if (existingLabelData instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> existing = (java.util.Map<String, Object>) existingLabelData;
                        labelDataMap = existing;
                    } else {
                        labelDataMap = new java.util.HashMap<>();
                    }

                    // 更新图标路径（转换为相对路径）
                    String relativePath = toRelativeIfUnderUserData(styleFile.getAbsolutePath());
                    labelDataMap.put("iconPath", relativePath);
                    selectedInstance.putClientProperty("labelData", labelDataMap);

                    System.out.println("调试：更新labelData中的iconPath为: " + relativePath);
                    break;
                }
            }

            // 刷新画布
            if (editCanvas != null) {
                editCanvas.revalidate();
                editCanvas.repaint();
            }

            // 更新属性面板显示
            updatePropertyPanelFromInstance(selectedInstance);

            System.out.println("实例样式已更新: " + styleFile.getName() +
                             ", 原始尺寸: " + originalWidth + "x" + originalHeight +
                             ", 基础显示尺寸: " + baseWidth + "x" + baseHeight +
                             ", 当前缩放: " + currentScalePercent + "%" +
                             ", 最终尺寸: " + newWidth + "x" + newHeight +
                             ", 宽高比: " + String.format("%.2f", aspectRatio));

        } catch (Exception e) {
            System.out.println("更新实例样式失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新实例内部组件的尺寸和字体
     */
    private void updateInstanceComponents(int newWidth, int newHeight) {
        if (selectedInstance == null) return;

        // 计算当前缩放比例
        double currentScale = 1.0;
        if (scaleSlider != null) {
            currentScale = scaleSlider.getValue() / 100.0;
        }

        Component[] components = selectedInstance.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setBounds(0, 0, newWidth, newHeight);

                // 如果有图标，重新缩放图标（使用高质量缩放）
                if (label.getIcon() != null) {
                    // 获取原始图片
                    ImageIcon originalIcon = getOriginalIcon(label);
                    if (originalIcon != null) {
                        Image scaledImg = createHighQualityScaledImage(originalIcon.getImage(), newWidth, newHeight);
                        label.setIcon(new ImageIcon(scaledImg));
                    }
                }

                // 缩放字体
                updateLabelFont(label, currentScale);
            }
        }
    }

    /**
     * 为画布重建时更新实例内部组件的尺寸（不依赖selectedInstance）
     */
    private void updateInstanceComponentsForResize(JPanel instance, int newWidth, int newHeight) {
        if (instance == null) return;

        Component[] components = instance.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setBounds(0, 0, newWidth, newHeight);

                // 如果有图标，重新缩放图标（使用高质量缩放）
                if (label.getIcon() != null) {
                    // 获取原始图片
                    ImageIcon originalIcon = getOriginalIcon(label);
                    if (originalIcon != null) {
                        Image scaledImg = createHighQualityScaledImage(originalIcon.getImage(), newWidth, newHeight);
                        label.setIcon(new ImageIcon(scaledImg));
                        System.out.println("画布重建时重新缩放图片: " + newWidth + "x" + newHeight);
                    }
                }

                // 缩放字体 - 保持原有的字体大小比例
                Integer originalFontSize = (Integer) label.getClientProperty("originalFontSize");
                if (originalFontSize != null) {
                    // 根据实例尺寸变化计算字体缩放比例
                    // 这里可以根据需要调整字体缩放策略
                    Font currentFont = label.getFont();
                    if (currentFont != null) {
                        // 保持字体大小不变，或者根据实例大小适当调整
                        // 暂时保持原有字体大小
                        System.out.println("画布重建时保持字体大小: " + currentFont.getSize());
                    }
                } else {
                    // 如果没有保存原始字体大小，保存当前字体大小
                    Font currentFont = label.getFont();
                    if (currentFont != null) {
                        label.putClientProperty("originalFontSize", currentFont.getSize());
                    }
                }
            }
        }
    }

    /**
     * 更新标签字体大小（根据缩放比例）
     */
    private void updateLabelFont(JLabel label, double scale) {
        if (label == null) return;

        try {
            // 获取或保存原始字体大小
            Integer originalFontSize = (Integer) label.getClientProperty("originalFontSize");
            if (originalFontSize == null) {
                // 如果没有保存原始字体大小，使用当前字体大小作为基准
                originalFontSize = label.getFont().getSize();
                label.putClientProperty("originalFontSize", originalFontSize);
            }

            // 计算新的字体大小
            int newFontSize = (int) Math.round(originalFontSize * scale);
            // 确保字体大小不会太小
            newFontSize = Math.max(8, newFontSize);

            // 创建新字体
            Font currentFont = label.getFont();
            Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), newFontSize);
            label.setFont(newFont);

            System.out.println("字体已缩放: 原始大小=" + originalFontSize + ", 缩放比例=" +
                             String.format("%.2f", scale) + ", 新大小=" + newFontSize);

        } catch (Exception e) {
            System.out.println("更新字体失败: " + e.getMessage());
        }
    }

    /**
     * 获取标签的原始图标
     */
    private ImageIcon getOriginalIcon(JLabel label) {
        // 尝试从客户端属性中获取原始图标
        ImageIcon originalIcon = (ImageIcon) label.getClientProperty("originalIcon");
        if (originalIcon == null) {
            // 如果没有保存原始图标，使用当前图标
            originalIcon = (ImageIcon) label.getIcon();
            if (originalIcon != null) {
                label.putClientProperty("originalIcon", originalIcon);
            }
        }
        return originalIcon;
    }

    /**
     * 创建高质量缩放图片
     */
    private Image createHighQualityScaledImage(Image originalImage, int targetWidth, int targetHeight) {
        if (originalImage == null) return null;

        int currentWidth = originalImage.getWidth(null);
        int currentHeight = originalImage.getHeight(null);

        // 如果目标尺寸与当前尺寸相同，直接返回
        if (currentWidth == targetWidth && currentHeight == targetHeight) {
            return originalImage;
        }

        // 对于大幅缩小的情况，使用多步缩放获得更好的质量
        if (targetWidth < currentWidth / 2 || targetHeight < currentHeight / 2) {
            return createMultiStepScaledImage(originalImage, targetWidth, targetHeight);
        }

        // 使用SCALE_SMOOTH获得最佳质量
        return originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    }

    /**
     * 多步缩放图片（用于大幅缩小时保持质量）
     */
    private Image createMultiStepScaledImage(Image originalImage, int targetWidth, int targetHeight) {
        int currentWidth = originalImage.getWidth(null);
        int currentHeight = originalImage.getHeight(null);

        Image currentImage = originalImage;

        // 每次缩放不超过50%，逐步缩放到目标尺寸
        while (currentWidth > targetWidth * 2 || currentHeight > targetHeight * 2) {
            currentWidth = Math.max(targetWidth, currentWidth / 2);
            currentHeight = Math.max(targetHeight, currentHeight / 2);
            currentImage = currentImage.getScaledInstance(currentWidth, currentHeight, Image.SCALE_SMOOTH);
        }

        // 最后一步缩放到精确的目标尺寸
        if (currentWidth != targetWidth || currentHeight != targetHeight) {
            currentImage = currentImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        }

        return currentImage;
    }

    /**
     * 根据实例更新缩放滑块
     */
    private void updateScaleSliderFromInstance(JPanel instance) {
        if (scaleSlider == null || scaleLabel == null) return;

        try {
            // 获取相对位置数据
            com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                (com.feixiang.tabletcontrol.model.RelativePosition) instance.getClientProperty("relativePosition");

            if (relativePos != null) {
                // 获取当前尺寸百分比，转换为缩放百分比显示
                double widthPercent = relativePos.getWidthPercent();
                double heightPercent = relativePos.getHeightPercent();

                // 尝试从实例属性中获取基础尺寸百分比（100%时的尺寸）
                Double baseWidthPercent = (Double) instance.getClientProperty("baseWidthPercent");
                Double baseHeightPercent = (Double) instance.getClientProperty("baseHeightPercent");

                int scalePercent = 100; // 默认100%

                if (baseWidthPercent != null && baseWidthPercent > 0) {
                    // 基于保存的基础尺寸计算缩放比例
                    double currentScale = widthPercent / baseWidthPercent;
                    double rawScalePercent = currentScale * 100;

                    // 添加容差处理：如果计算结果接近100%（在98%-102%范围内），则显示为100%
                    if (rawScalePercent >= 98.0 && rawScalePercent <= 102.0) {
                        scalePercent = 100;
                        System.out.println("基于保存的基础尺寸计算缩放: 当前宽度=" + String.format("%.2f%%", widthPercent*100) +
                                         ", 基础宽度=" + String.format("%.2f%%", baseWidthPercent*100) +
                                         ", 原始计算=" + String.format("%.1f%%", rawScalePercent) +
                                         ", 容差修正为=100%");
                    } else {
                        scalePercent = (int) Math.round(rawScalePercent);
                        System.out.println("基于保存的基础尺寸计算缩放: 当前宽度=" + String.format("%.2f%%", widthPercent*100) +
                                         ", 基础宽度=" + String.format("%.2f%%", baseWidthPercent*100) +
                                         ", 缩放比例=" + scalePercent + "%");
                    }
                } else {
                    // 如果没有保存基础尺寸，则认为当前尺寸就是100%
                    scalePercent = 100;
                    // 保存当前尺寸作为基础尺寸
                    instance.putClientProperty("baseWidthPercent", widthPercent);
                    instance.putClientProperty("baseHeightPercent", heightPercent);
                    System.out.println("没有基础尺寸数据，将当前尺寸设为100%基准: " +
                                     String.format("%.2f%%x%.2f%%", widthPercent*100, heightPercent*100));
                }

                // 限制缩放范围
                scalePercent = Math.max(50, Math.min(200, scalePercent));

                // 更新滑块和标签（暂时移除监听器避免循环触发）
                javax.swing.event.ChangeListener[] listeners = scaleSlider.getChangeListeners();
                for (javax.swing.event.ChangeListener listener : listeners) {
                    scaleSlider.removeChangeListener(listener);
                }

                scaleSlider.setValue(scalePercent);
                scaleLabel.setText("缩放比例: " + scalePercent + "%");

                // 重新添加监听器
                for (javax.swing.event.ChangeListener listener : listeners) {
                    scaleSlider.addChangeListener(listener);
                }

                System.out.println("缩放滑块已更新: " + scalePercent + "% (尺寸百分比: " +
                                 String.format("%.1f%%x%.1f%%", widthPercent*100, heightPercent*100) + ")");
            } else {
                // 如果没有相对位置信息，默认设置为100%（暂时移除监听器避免循环触发）
                javax.swing.event.ChangeListener[] listeners = scaleSlider.getChangeListeners();
                for (javax.swing.event.ChangeListener listener : listeners) {
                    scaleSlider.removeChangeListener(listener);
                }

                scaleSlider.setValue(100);
                scaleLabel.setText("缩放比例: 100%");

                // 重新添加监听器
                for (javax.swing.event.ChangeListener listener : listeners) {
                    scaleSlider.addChangeListener(listener);
                }

                System.out.println("缩放滑块设置为默认100% (缺少相对位置数据)");
            }
        } catch (Exception e) {
            System.out.println("更新缩放滑块失败: " + e.getMessage());
            // 异常情况下也要避免触发监听器
            javax.swing.event.ChangeListener[] listeners = scaleSlider.getChangeListeners();
            for (javax.swing.event.ChangeListener listener : listeners) {
                scaleSlider.removeChangeListener(listener);
            }

            scaleSlider.setValue(100);
            scaleLabel.setText("缩放比例: 100%");

            // 重新添加监听器
            for (javax.swing.event.ChangeListener listener : listeners) {
                scaleSlider.addChangeListener(listener);
            }
        }
    }

    /**
     * 根据实例更新样式按钮显示
     */
    private void updateStyleButtonFromInstance(JPanel instance) {
        if (styleButton == null) return;

        try {
            // 查找实例中的标签组件
            Component[] components = instance.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;

                    // 尝试从客户端属性中获取原始图标的文件名
                    ImageIcon originalIcon = (ImageIcon) label.getClientProperty("originalIcon");
                    if (originalIcon != null) {
                        // 从图标的描述或URL中提取文件名
                        String description = originalIcon.getDescription();
                        if (description != null && description.contains("\\")) {
                            String fileName = description.substring(description.lastIndexOf("\\") + 1);
                            styleButton.setText(fileName);
                            System.out.println("样式按钮已更新: " + fileName);
                            return;
                        } else if (description != null && description.contains("/")) {
                            String fileName = description.substring(description.lastIndexOf("/") + 1);
                            styleButton.setText(fileName);
                            System.out.println("样式按钮已更新: " + fileName);
                            return;
                        }
                    }

                    // 如果无法获取文件名，显示默认文本
                    styleButton.setText("btn1.png");
                    System.out.println("样式按钮设置为默认: btn1.png");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("更新样式按钮失败: " + e.getMessage());
            styleButton.setText("选择");
        }
    }

    /**
     * 根据实例更新功能类型显示
     */
    private void updateFunctionTypeFromInstance(JPanel instance) {
        if (functionTypeComboBox == null) return;

        try {
            // 从实例的客户端属性中获取功能类型
            String functionType = (String) instance.getClientProperty("functionType");
            if (functionType == null) {
                functionType = "纯文本"; // 默认类型
                instance.putClientProperty("functionType", functionType);
            }

            // 更新下拉框选择
            functionTypeComboBox.setSelectedItem(functionType);

            // 更新功能配置显示
            updateFunctionConfigDisplay(functionType);

            // 如果是页面跳转功能，刷新目标页面下拉框并排除当前页面
            if ("页面跳转".equals(functionType)) {
                refreshTargetPageComboBox(currentPageName);

                // 恢复之前选择的目标页面
                String targetPage = (String) instance.getClientProperty("targetPage");
                if (targetPage != null && targetPageComboBox != null) {
                    targetPageComboBox.setSelectedItem(targetPage);
                }
            }

            System.out.println("功能类型已更新: " + functionType);

        } catch (Exception e) {
            System.out.println("更新功能类型失败: " + e.getMessage());
            functionTypeComboBox.setSelectedItem("纯文本");
        }
    }

    /**
     * 打开带延时功能的指令列表对话框
     */
    private void openCommandListDialogWithDelay(JTextField commandField) {
        // 检查是否为开关按钮
        boolean isSwitchButton = false;
        if (selectedInstance != null) {
            String iconPath = getInstanceIconPath(selectedInstance);
            System.out.println("调试：获取到的图标路径: " + iconPath);
            isSwitchButton = isSwitchButton(iconPath);
            System.out.println("调试：是否为开关按钮: " + isSwitchButton);
        }

        // 如果是开关按钮，直接打开开关按钮编辑对话框
        if (isSwitchButton) {
            System.out.println("调试：检测到开关按钮，打开开关按钮编辑对话框");
            openSwitchButtonEditDialog(selectedInstance);
            return;
        }

        JDialog dialog = new JDialog(this, "指令列表管理", true);
        dialog.setSize(700, 500); // 增加宽度以容纳延时按钮
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(Color.WHITE);

        // 添加标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(248, 249, 250));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("指令列表管理");
        titleLabel.setFont(getUnicodeFont(16));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        dialog.add(titlePanel, BorderLayout.NORTH);

        // 指令列表面板
        DefaultListModel<String> listModel = new DefaultListModel<>();

        // 从实例属性中加载现有指令，确保包含延时字段
        if (selectedInstance != null) {
            Object commands = selectedInstance.getClientProperty("commands");
            if (commands instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> commandList = (java.util.List<String>) commands;
                for (String cmd : commandList) {
                    // 确保指令包含延时字段
                    String[] parts = parseCommandData(cmd);
                    if (parts.length >= 5) {
                        String commandWithDelay = buildCommandData(parts[0], parts[1], parts[2], parts[3], parts[4],
                                                                 parts.length >= 6 ? parts[5] : "0");
                        listModel.addElement(commandWithDelay);
                    } else {
                        listModel.addElement(cmd);
                    }
                }
            }
        }

        JList<String> commandList = new JList<>(listModel);
        commandList.setFont(getUnicodeFont(12));
        commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandList.setBackground(Color.WHITE);
        commandList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 使用带延时按钮的渲染器
        commandList.setCellRenderer(new CommandListWithDelayRenderer());

        // 添加鼠标点击事件处理延时按钮
        commandList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = commandList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle cellBounds = commandList.getCellBounds(index, index);

                    // 检查是否点击了延时按钮区域（右侧80像素）
                    if (e.getX() > cellBounds.width - 80) {
                        // 点击了延时按钮
                        String currentCommand = listModel.getElementAt(index);
                        openDelaySettingDialog(currentCommand, updatedCommand -> {
                            listModel.setElementAt(updatedCommand, index);
                            commandList.repaint();
                        });
                    } else if (e.getClickCount() == 2) {
                        // 双击编辑指令
                        String selectedCommand = listModel.getElementAt(index);
                        openEditCommandDialog(listModel, index, selectedCommand);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(commandList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.setBackground(Color.WHITE);

        dialog.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("添加指令");
        addButton.setFont(getUnicodeFont(12));
        addButton.setBackground(new Color(0, 123, 255));
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addButton.setFocusPainted(false);

        JButton deleteButton = new JButton("删除指令");
        deleteButton.setFont(getUnicodeFont(12));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        deleteButton.setFocusPainted(false);

        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(getUnicodeFont(12));
        confirmButton.setBackground(new Color(40, 167, 69));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmButton.setFocusPainted(false);

        // 添加指令按钮事件
        addButton.addActionListener(e -> openEditCommandDialog(listModel, -1, null));

        // 删除指令按钮事件
        deleteButton.addActionListener(e -> {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.removeElementAt(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(dialog, "请先选择要删除的指令", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 确定按钮事件
        confirmButton.addActionListener(e -> {
            // 保存指令列表到实例属性
            java.util.List<String> commands = new java.util.ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) {
                commands.add(listModel.getElementAt(i));
            }

            if (selectedInstance != null) {
                selectedInstance.putClientProperty("commands", commands);
                System.out.println("指令列表已保存，共 " + commands.size() + " 条指令");
                for (String cmd : commands) {
                    System.out.println("  - " + cmd);
                }
            }
            dialog.dispose();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(confirmButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 打开指令列表对话框
     */
    private void openCommandListDialog(JTextField commandField) {
        // 检查是否为开关按钮
        boolean isSwitchButton = false;
        if (selectedInstance != null) {
            String iconPath = getInstanceIconPath(selectedInstance);
            System.out.println("调试：获取到的图标路径: " + iconPath);
            isSwitchButton = isSwitchButton(iconPath);
            System.out.println("调试：是否为开关按钮: " + isSwitchButton);
        }

        // 如果是开关按钮，直接打开开关按钮编辑对话框
        if (isSwitchButton) {
            System.out.println("调试：检测到开关按钮，打开开关按钮编辑对话框");
            openSwitchButtonEditDialog(selectedInstance);
            return;
        }

        JDialog dialog = new JDialog(this, "指令列表管理", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(Color.WHITE);

        // 添加标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(248, 249, 250));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("指令列表管理");
        titleLabel.setFont(getUnicodeFont(16));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        dialog.add(titlePanel, BorderLayout.NORTH);

        // 指令列表面板
        DefaultListModel<String> listModel = new DefaultListModel<>();

        // 从实例属性中加载现有指令
        if (selectedInstance != null) {
            Object commands = selectedInstance.getClientProperty("commands");
            if (commands instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> commandList = (java.util.List<String>) commands;
                for (String cmd : commandList) {
                    listModel.addElement(cmd);
                }
            }
        }

        JList<String> commandList = new JList<>(listModel);
        commandList.setFont(getUnicodeFont(12));
        commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandList.setBackground(Color.WHITE);
        commandList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 优化列表渲染
        commandList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // 增加内边距，改善视觉效果
                setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
                setFont(getUnicodeFont(13)); // 增大字体

                if (isSelected) {
                    setBackground(new Color(0, 123, 255));
                    setForeground(Color.WHITE);
                    // 添加选中状态的圆角效果
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 86, 179), 2),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                    ));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(52, 58, 64));
                    // 添加悬停效果的边框
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                        BorderFactory.createEmptyBorder(11, 15, 11, 15)
                    ));
                }

                // 显示完整指令信息
                String displayText = value.toString();
                String[] parts = displayText.split("\\|");
                if (parts.length >= 5) {
                    // 格式化显示：指令名称 | 协议 | IP:端口 | 指令内容
                    String name = parts[0];
                    String protocol = parts[1];
                    String ip = parts[2];
                    String port = parts[3];
                    String command = parts[4];

                    displayText = String.format("%s | %s | %s:%s | %s", name, protocol, ip, port, command);
                } else {
                    // 如果格式不正确，显示原始内容
                    displayText = value.toString();
                }

                // 智能截断文本
                String finalText = "📋 " + truncateTextToFit(displayText, this, getWidth() - 40);
                setText(finalText);

                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(commandList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 优化滚动速度

        // 优化滚动条样式
        scrollPane.getVerticalScrollBar().setBackground(new Color(248, 249, 250));
        scrollPane.getHorizontalScrollBar().setBackground(new Color(248, 249, 250));

        // 右键菜单（优化样式）
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(Color.WHITE);
        popupMenu.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));

        JMenuItem editItem = new JMenuItem("编辑");
        editItem.setFont(getUnicodeFont(12));
        editItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        editItem.addActionListener(e -> {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedCommand = listModel.getElementAt(selectedIndex);
                openEditCommandDialog(listModel, selectedIndex, selectedCommand);
            }
        });

        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.setFont(getUnicodeFont(12));
        deleteItem.setForeground(new Color(220, 53, 69));
        deleteItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        deleteItem.addActionListener(e -> {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex != -1) {
                int result = JOptionPane.showConfirmDialog(dialog,
                    "确定要删除选中的指令吗？", "确认删除",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    listModel.remove(selectedIndex);
                }
            }
        });

        popupMenu.add(editItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);
        commandList.setComponentPopupMenu(popupMenu);

        // 添加双击编辑功能
        commandList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedIndex = commandList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedCommand = listModel.getElementAt(selectedIndex);
                        openEditCommandDialog(listModel, selectedIndex, selectedCommand);
                    }
                }
            }
        });

        // 中间面板：列表 + 排序按钮（优化布局）
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // 排序控制按钮面板
        JPanel sortPanel = new JPanel(new GridBagLayout());
        sortPanel.setBackground(new Color(248, 249, 250));
        sortPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints sortGbc = new GridBagConstraints();
        sortGbc.insets = new Insets(8, 5, 8, 5);

        // 上移按钮（优化样式）
        JButton upButton = new JButton("↑");
        upButton.setPreferredSize(new Dimension(45, 35));
        upButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        upButton.setBackground(new Color(40, 167, 69));
        upButton.setForeground(Color.WHITE);
        upButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        upButton.setFocusPainted(false);
        upButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        upButton.setToolTipText("上移选中项");

        // 下移按钮（优化样式）
        JButton downButton = new JButton("↓");
        downButton.setPreferredSize(new Dimension(45, 35));
        downButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        downButton.setBackground(new Color(220, 53, 69));
        downButton.setForeground(Color.WHITE);
        downButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        downButton.setFocusPainted(false);
        downButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downButton.setToolTipText("下移选中项");

        // 添加悬停效果
        upButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                upButton.setBackground(new Color(33, 136, 56));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                upButton.setBackground(new Color(40, 167, 69));
            }
        });

        downButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                downButton.setBackground(new Color(200, 35, 51));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                downButton.setBackground(new Color(220, 53, 69));
            }
        });

        // 上移按钮
        upButton.addActionListener(e -> {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex > 0) {
                String item = listModel.remove(selectedIndex);
                listModel.add(selectedIndex - 1, item);
                commandList.setSelectedIndex(selectedIndex - 1);
            }
        });

        // 下移按钮
        downButton.addActionListener(e -> {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < listModel.size() - 1) {
                String item = listModel.remove(selectedIndex);
                listModel.add(selectedIndex + 1, item);
                commandList.setSelectedIndex(selectedIndex + 1);
            }
        });

        sortGbc.gridx = 0; sortGbc.gridy = 0;
        sortPanel.add(upButton, sortGbc);
        sortGbc.gridy = 1;
        sortPanel.add(downButton, sortGbc);

        centerPanel.add(sortPanel, BorderLayout.EAST);
        dialog.add(centerPanel, BorderLayout.CENTER);

        // 底部按钮面板（优化样式）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        JButton newButton = new JButton("新增指令");
        newButton.setFont(getUnicodeFont(12));
        newButton.setPreferredSize(new Dimension(100, 35));
        newButton.setBackground(new Color(40, 167, 69));
        newButton.setForeground(Color.WHITE);
        newButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        newButton.setFocusPainted(false);
        newButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(getUnicodeFont(12));
        confirmButton.setPreferredSize(new Dimension(80, 35));
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmButton.setFocusPainted(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        newButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                newButton.setBackground(new Color(33, 136, 56));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newButton.setBackground(new Color(40, 167, 69));
            }
        });

        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                confirmButton.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                confirmButton.setBackground(new Color(0, 123, 255));
            }
        });

        newButton.addActionListener(e -> openNewCommandDialog(listModel));
        confirmButton.addActionListener(e -> {
            // 保存指令列表到实例属性
            if (selectedInstance != null) {
                java.util.List<String> commands = new java.util.ArrayList<>();
                for (int i = 0; i < listModel.size(); i++) {
                    commands.add(listModel.getElementAt(i));
                }
                selectedInstance.putClientProperty("commands", commands);
            }
            dialog.dispose();
        });

        buttonPanel.add(newButton);
        buttonPanel.add(confirmButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 打开新增指令对话框
     */
    private void openNewCommandDialog(DefaultListModel<String> listModel) {
        openEditCommandDialog(listModel, -1, null);
    }

    /**
     * 打开编辑指令对话框（新增或编辑）
     */
    private void openEditCommandDialog(DefaultListModel<String> listModel, int editIndex, String existingCommand) {
        boolean isEdit = editIndex >= 0;
        JDialog dialog = new JDialog(this, isEdit ? "编辑指令" : "新增指令", true);
        dialog.setSize(520, 520); // 进一步增大对话框高度
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(Color.WHITE);

        // 删除标题面板，节省空间，标题已经在对话框标题栏显示

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25)); // 减少上下边距
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8); // 适度减少垂直间距
        gbc.anchor = GridBagConstraints.WEST;

        // 协议类型
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel protocolLabel = new JLabel("协议类型:");
        protocolLabel.setFont(getUnicodeFont(13));
        protocolLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(protocolLabel, gbc);

        gbc.gridy = 1;
        ButtonGroup protocolGroup = new ButtonGroup();
        JRadioButton tcpRadio = new JRadioButton("TCP");
        JRadioButton udpRadio = new JRadioButton("UDP");

        // 优化单选按钮样式
        tcpRadio.setFont(getUnicodeFont(12));
        tcpRadio.setBackground(Color.WHITE);
        tcpRadio.setForeground(new Color(52, 58, 64));
        tcpRadio.setSelected(true); // 默认选择TCP

        udpRadio.setFont(getUnicodeFont(12));
        udpRadio.setBackground(Color.WHITE);
        udpRadio.setForeground(new Color(52, 58, 64));

        protocolGroup.add(tcpRadio);
        protocolGroup.add(udpRadio);

        JPanel protocolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        protocolPanel.setBackground(Color.WHITE);
        protocolPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        protocolPanel.add(tcpRadio);
        protocolPanel.add(udpRadio);
        mainPanel.add(protocolPanel, gbc);

        // 指令名称
        gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("指令名称:");
        nameLabel.setFont(getUnicodeFont(13));
        nameLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        nameField.setFont(getUnicodeFont(13)); // 增大字体
        nameField.setPreferredSize(new Dimension(280, 36)); // 增大尺寸
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) // 增加内边距
        ));

        // 添加焦点效果
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        if (isEdit && existingCommand != null) {
            nameField.setText(existingCommand);
        }
        mainPanel.add(nameField, gbc);

        // IP地址
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel ipLabel = new JLabel("IP地址:");
        ipLabel.setFont(getUnicodeFont(13));
        ipLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(ipLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField ipField = new JTextField(20);
        ipField.setFont(getUnicodeFont(13));
        ipField.setPreferredSize(new Dimension(280, 36));
        ipField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // 添加焦点效果
        ipField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                ipField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                ipField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        ipField.setText("192.168.1.100"); // 默认IP
        mainPanel.add(ipField, gbc);

        // 端口
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel portLabel = new JLabel("端口:");
        portLabel.setFont(getUnicodeFont(13));
        portLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(portLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField portField = new JTextField(20);
        portField.setFont(getUnicodeFont(13));
        portField.setPreferredSize(new Dimension(280, 36));
        portField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // 添加焦点效果
        portField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                portField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                portField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        portField.setText("8080"); // 默认端口
        mainPanel.add(portField, gbc);

        // 指令内容
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel commandLabel = new JLabel("指令内容:");
        commandLabel.setFont(getUnicodeFont(13));
        commandLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(commandLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField commandField = new JTextField(20);
        commandField.setFont(new Font("Consolas", Font.PLAIN, 12)); // 使用等宽字体
        commandField.setPreferredSize(new Dimension(280, 36));
        commandField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // 添加焦点效果
        commandField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                commandField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                commandField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        commandField.setToolTipText("支持十六进制格式，如：01 05 00 01 00 04 9D C9");
        commandField.setBackground(new Color(248, 249, 250)); // 浅灰背景突出显示
        mainPanel.add(commandField, gbc);

        dialog.add(mainPanel, BorderLayout.CENTER);

        // 编辑模式时回填数据（在所有字段定义完成后）
        if (isEdit && existingCommand != null) {
            String[] parts = existingCommand.split("\\|");
            if (parts.length >= 5) {
                nameField.setText(parts[0]); // 指令名称
                // 设置协议类型
                if ("UDP".equals(parts[1])) {
                    udpRadio.setSelected(true);
                } else {
                    tcpRadio.setSelected(true);
                }
                ipField.setText(parts[2]); // IP地址
                portField.setText(parts[3]); // 端口
                commandField.setText(parts[4]); // 指令内容
            } else {
                // 兼容旧格式（只有指令名称）
                nameField.setText(existingCommand);
            }
        }

        // 按钮面板（优化样式）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 12, 20)); // 减少上下边距

        JButton confirmButton = new JButton(isEdit ? "保存" : "确认");
        confirmButton.setFont(getUnicodeFont(12));
        confirmButton.setPreferredSize(new Dimension(80, 35));
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmButton.setFocusPainted(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(getUnicodeFont(12));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                confirmButton.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                confirmButton.setBackground(new Color(0, 123, 255));
            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(new Color(90, 98, 104));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(new Color(108, 117, 125));
            }
        });

        confirmButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String ip = ipField.getText().trim();
            String port = portField.getText().trim();
            String command = commandField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入指令名称", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入IP地址", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (port.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入端口", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 创建完整的指令对象
            String protocol = tcpRadio.isSelected() ? "TCP" : "UDP";
            String commandData = String.format("%s|%s|%s|%s|%s", name, protocol, ip, port, command);

            if (isEdit) {
                // 编辑模式：更新现有项
                listModel.setElementAt(commandData, editIndex);
            } else {
                // 新增模式：添加到列表
                listModel.addElement(commandData);
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 打开实例组配置对话框
     */
    private void openInstanceGroupDialog(JTextField groupField) {
        System.out.println("调试：打开实例组配置对话框");

        try {
            JDialog dialog = new JDialog(this, "实例组配置", true);
        dialog.setSize(650, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(new Color(248, 250, 252));
        dialog.setResizable(false);

        // 创建标题面板
        JPanel titlePanel = createInstanceGroupTitlePanel();
        dialog.add(titlePanel, BorderLayout.NORTH);

        // 创建内容面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        // 指令名称输入区域
        JPanel namePanel = createInstanceGroupNamePanel();
        System.out.println("调试：创建名称面板，组件数量: " + namePanel.getComponentCount());

        JTextField nameField = findNameFieldInPanel(namePanel);
        if (nameField == null) {
            System.out.println("错误：无法找到名称输入框");
            System.out.println("调试：名称面板中的组件类型:");
            for (Component comp : namePanel.getComponents()) {
                System.out.println("  - " + comp.getClass().getSimpleName());
            }
            return;
        }
        System.out.println("调试：成功找到名称输入框");

        // 恢复实例组名称
        if (selectedInstance != null) {
            String savedName = (String) selectedInstance.getClientProperty("instanceGroupName");
            if (savedName != null && !savedName.isEmpty()) {
                nameField.setText(savedName);
                System.out.println("恢复实例组名称: " + savedName);
            }
        }

        contentPanel.add(namePanel, BorderLayout.NORTH);

        // 指令列表区域
        System.out.println("调试：开始创建指令列表面板");
        JPanel listPanel = createInstanceGroupListPanelWithDelay();
        System.out.println("调试：指令列表面板创建完成");

        // 恢复指令选择状态
        if (selectedInstance != null) {
            @SuppressWarnings("unchecked")
            java.util.List<String> savedCommands = (java.util.List<String>) selectedInstance.getClientProperty("instanceGroupCommands");
            if (savedCommands != null && !savedCommands.isEmpty()) {
                System.out.println("调试：开始恢复指令选择状态");
                restoreInstanceGroupCommandSelection(listPanel, savedCommands);
                System.out.println("恢复指令选择状态，共 " + savedCommands.size() + " 条指令");
            } else {
                System.out.println("调试：没有保存的指令需要恢复");
            }
        } else {
            System.out.println("调试：selectedInstance 为 null");
        }

        contentPanel.add(listPanel, BorderLayout.CENTER);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // 创建按钮面板
        System.out.println("调试：开始创建按钮面板");
        JPanel buttonPanel = createInstanceGroupButtonPanel(dialog, nameField, listPanel);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        System.out.println("调试：按钮面板创建完成");

        System.out.println("调试：准备显示对话框");
        dialog.setVisible(true);
        System.out.println("调试：对话框显示完成");

        } catch (Exception e) {
            System.out.println("错误：实例组配置对话框创建失败");
            e.printStackTrace();
        }
    }

    /**
     * 创建实例组配置对话框按钮面板
     */
    private JPanel createInstanceGroupButtonPanel(JDialog dialog, JTextField nameField, JPanel listPanel) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        // 确定按钮
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        confirmButton.setPreferredSize(new Dimension(100, 40));
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setFocusPainted(false);

        // 取消按钮
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        cancelButton.setFocusPainted(false);

        // 添加悬停效果
        confirmButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                confirmButton.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                confirmButton.setBackground(new Color(0, 123, 255));
            }
        });

        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(90, 98, 104));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(108, 117, 125));
            }
        });

        // 确定按钮事件
        confirmButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入实例组名称", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取选中的指令（支持延时功能）
            @SuppressWarnings("unchecked")
            DefaultListModel<CheckBoxItemWithDelay> listModel = (DefaultListModel<CheckBoxItemWithDelay>) listPanel.getClientProperty("listModel");
            java.util.List<String> selectedCommands = new java.util.ArrayList<>();
            java.util.Map<String, String> instanceGroupDelays = new java.util.HashMap<>();

            if (listModel != null) {
                for (int i = 0; i < listModel.size(); i++) {
                    CheckBoxItemWithDelay item = listModel.getElementAt(i);
                    if (item.isSelected()) {
                        String commandData = item.getCommandData();
                        selectedCommands.add(commandData); // 保存完整的指令数据（包括延时）

                        // 保存实例组专用的延时信息
                        String[] parts = commandData.split("\\|");
                        if (parts.length >= 6) {
                            String commandKey = parts[0]; // 使用指令名称作为键
                            String delay = parts[5];
                            if (!"0".equals(delay)) {
                                instanceGroupDelays.put(commandKey, delay);
                                System.out.println("保存实例组延时: " + commandKey + " -> " + delay + "秒");
                            }
                        }
                    }
                }
            }

            if (selectedCommands.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请至少选择一条指令", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 保存配置到实例属性
            if (selectedInstance != null) {
                selectedInstance.putClientProperty("instanceGroupName", name);
                selectedInstance.putClientProperty("instanceGroupCommands", selectedCommands);
                selectedInstance.putClientProperty("instanceGroupDelays", instanceGroupDelays);

                System.out.println("实例组配置已保存: " + name + ", 包含 " + selectedCommands.size() + " 条指令");
                for (String cmd : selectedCommands) {
                    System.out.println("  - " + cmd);
                }

                // 刷新所有相关的实例组列表显示
                refreshInstanceGroupDisplays();
            }
            dialog.dispose();
        });

        // 取消按钮事件
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    /**
     * 刷新所有实例组显示
     */
    private void refreshInstanceGroupDisplays() {
        System.out.println("刷新实例组显示");

        // 刷新当前页面中所有实例组配置对话框的列表
        if (pageContents != null) {
            for (String pageName : pageContents.keySet()) {
                java.util.List<Component> pageContent = pageContents.get(pageName);
                if (pageContent != null) {
                    for (Component component : pageContent) {
                        if (component instanceof JPanel) {
                            JPanel instance = (JPanel) component;
                            String functionType = (String) instance.getClientProperty("functionType");

                            // 如果是实例组类型，可能需要更新其显示
                            if ("实例组".equals(functionType)) {
                                // 这里可以添加更多的刷新逻辑
                                System.out.println("发现实例组实例，功能类型: " + functionType);
                            }
                        }
                    }
                }
            }
        }

        // 如果有打开的实例组配置对话框，刷新其列表
        // 注意：由于对话框是模态的，这里主要是为了将来扩展
        System.out.println("实例组显示刷新完成");
    }

    /**
     * 收集所有可用的指令和实例组（带延时功能）- 实例组专用
     */
    private void collectAvailableCommandsForInstanceGroupWithDelay(DefaultListModel<CheckBoxItemWithDelay> listModel) {
        // 收集所有页面中的指令
        if (pageContents != null) {
            for (String pageName : pageContents.keySet()) {
                java.util.List<Component> pageContent = pageContents.get(pageName);
                if (pageContent != null) {
                    for (Component component : pageContent) {
                        if (component instanceof JPanel) {
                            JPanel instance = (JPanel) component;

                            String functionType = (String) instance.getClientProperty("functionType");
                            if ("发送指令".equals(functionType)) {
                                // 添加发送指令类型的实例
                                Object commands = instance.getClientProperty("commands");
                                if (commands instanceof java.util.List) {
                                    @SuppressWarnings("unchecked")
                                    java.util.List<String> commandList = (java.util.List<String>) commands;
                                    for (String cmd : commandList) {
                                        String commandName = null;
                                        String displayText = null;

                                        // 解析指令数据并格式化显示
                                        String[] parts = cmd.split("\\|");

                                        if (parts.length >= 5) {
                                            // 新格式：完整的指令数据
                                            String name = parts[0];
                                            String protocol = parts[1];
                                            String ip = parts[2];
                                            String port = parts[3];
                                            String command = parts[4];

                                            // 为实例组配置创建独立的指令数据（不包含指令列表管理的延时）
                                            String commandWithDelay = buildCommandData(name, protocol, ip, port, command, "0");

                                            commandName = name; // 用于去重检查
                                        } else {
                                            // 旧格式：只有指令名称
                                            commandName = cmd;
                                            displayText = cmd;
                                        }

                                        if (commandName != null && !commandName.isEmpty()) {
                                            // 检查是否已存在（基于指令名称去重）
                                            boolean exists = false;
                                            for (int i = 0; i < listModel.size(); i++) {
                                                CheckBoxItemWithDelay existingItem = listModel.getElementAt(i);
                                                String existingName = existingItem.getCommandData().split("\\|")[0].trim();
                                                if (existingName.equals(commandName)) {
                                                    exists = true;
                                                    break;
                                                }
                                            }
                                            if (!exists) {
                                                // 为实例组配置创建独立的指令数据（默认延时为0）
                                                String commandWithDelay;
                                                if (parts.length >= 5) {
                                                    // 新格式：完整的指令数据
                                                    commandWithDelay = buildCommandData(parts[0], parts[1], parts[2], parts[3], parts[4], "0");
                                                    System.out.println("添加新格式指令到列表: " + commandName + " -> " + commandWithDelay);
                                                } else {
                                                    // 旧格式：只有指令名称，创建默认格式
                                                    commandWithDelay = buildCommandData(commandName, "TCP", "127.0.0.1", "8080", "00 01", "0");
                                                    System.out.println("添加旧格式指令到列表: " + commandName + " -> " + commandWithDelay);
                                                }
                                                listModel.addElement(new CheckBoxItemWithDelay(commandWithDelay, false));
                                            } else {
                                                System.out.println("指令已存在，跳过: " + commandName);
                                            }
                                        }
                                    }
                                }
                            } else if ("实例组".equals(functionType)) {
                                // 添加实例组类型的实例
                                String groupName = (String) instance.getClientProperty("instanceGroupName");
                                if (groupName != null && !groupName.isEmpty()) {
                                    // 检查是否已存在（基于实例组名称去重）
                                    boolean exists = false;
                                    for (int i = 0; i < listModel.size(); i++) {
                                        CheckBoxItemWithDelay existingItem = listModel.getElementAt(i);
                                        String existingDisplayText = existingItem.getDisplayText();
                                        if (existingDisplayText.contains(groupName + " (实例组)")) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        // 创建实例组的显示数据
                                        String groupDisplayData = buildCommandData(groupName + " (实例组)", "GROUP", "", "", "", "0");
                                        listModel.addElement(new CheckBoxItemWithDelay(groupDisplayData, false));
                                        System.out.println("添加实例组到列表: " + groupName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 收集所有可用的指令和实例组
     */
    private void collectAvailableCommandsForInstanceGroup(DefaultListModel<CheckBoxItem> listModel) {
        // 收集所有页面中的指令
        if (pageContents != null) {
            for (String pageName : pageContents.keySet()) {
                java.util.List<Component> pageContent = pageContents.get(pageName);
                if (pageContent != null) {
                    for (Component component : pageContent) {
                        if (component instanceof JPanel) {
                            JPanel instance = (JPanel) component;

                            String functionType = (String) instance.getClientProperty("functionType");
                            if ("发送指令".equals(functionType)) {
                                // 添加发送指令类型的实例
                                Object commands = instance.getClientProperty("commands");
                                if (commands instanceof java.util.List) {
                                    @SuppressWarnings("unchecked")
                                    java.util.List<String> commandList = (java.util.List<String>) commands;
                                    for (String cmd : commandList) {
                                        String commandName = null;

                                        // 解析指令数据并格式化显示
                                        String[] parts = cmd.split("\\|");
                                        String displayText = null;

                                        if (parts.length >= 5) {
                                            // 新格式：完整的指令数据
                                            String name = parts[0];
                                            String protocol = parts[1];
                                            String ip = parts[2];
                                            String port = parts[3];
                                            String command = parts[4];

                                            // 不在这里截断，让UI组件根据实际宽度处理
                                            displayText = String.format("%s | %s | %s:%s | %s", name, protocol, ip, port, command);
                                            commandName = name; // 用于去重检查
                                        } else {
                                            // 旧格式：只有指令名称
                                            commandName = cmd;
                                            displayText = cmd;
                                        }

                                        if (commandName != null && !commandName.isEmpty()) {
                                            // 检查是否已存在（基于指令名称去重）
                                            boolean exists = false;
                                            for (int i = 0; i < listModel.size(); i++) {
                                                CheckBoxItem existingItem = listModel.getElementAt(i);
                                                String existingName = existingItem.getText().split("\\|")[0].trim();
                                                if (existingName.equals(commandName)) {
                                                    exists = true;
                                                    break;
                                                }
                                            }
                                            if (!exists) {
                                                listModel.addElement(new CheckBoxItem(displayText, false));
                                            }
                                        }
                                    }
                                }
                            } else if ("实例组".equals(functionType)) {
                                // 添加实例组类型的实例
                                String groupName = (String) instance.getClientProperty("instanceGroupName");
                                if (groupName != null && !groupName.isEmpty()) {
                                    // 检查是否已存在
                                    boolean exists = false;
                                    for (int i = 0; i < listModel.size(); i++) {
                                        if (listModel.getElementAt(i).getText().equals(groupName + " (实例组)")) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        listModel.addElement(new CheckBoxItem(groupName + " (实例组)", false));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 如果没有找到任何指令，添加提示信息
        if (listModel.isEmpty()) {
            listModel.addElement(new CheckBoxItem("暂无可用指令", false));
        }
    }

    /**
     * 检查是否为开关按钮
     */
    private boolean isSwitchButton(String iconPath) {
        if (iconPath == null || iconPath.isEmpty()) {
            System.out.println("调试：图标路径为空，不是开关按钮");
            return false;
        }

        // 检查路径是否在USERDATA/appearance/button目录下
        String normalizedPath = iconPath.replace("\\", "/");
        System.out.println("调试：标准化路径: " + normalizedPath);

        boolean isSwitch = normalizedPath.contains("USERDATA/appearance/button/") ||
                          normalizedPath.contains("appearance/button/");
        System.out.println("调试：路径检查结果: " + isSwitch);

        return isSwitch;
    }

    /**
     * 获取开关按钮的文件夹路径
     */
    private String getSwitchButtonFolder(String iconPath) {
        if (!isSwitchButton(iconPath)) {
            return null;
        }

        String normalizedPath = iconPath.replace("\\", "/");
        int buttonIndex = normalizedPath.indexOf("appearance/button/");
        if (buttonIndex == -1) {
            return null;
        }

        String afterButton = normalizedPath.substring(buttonIndex + "appearance/button/".length());
        int slashIndex = afterButton.indexOf("/");
        if (slashIndex == -1) {
            return null;
        }

        String buttonFolder = afterButton.substring(0, slashIndex);
        return "USERDATA/appearance/button/" + buttonFolder;
    }

    /**
     * 开关按钮数据类
     */
    private static class SwitchButtonData {
        private String id;
        private String displayName;
        private String offImagePath;
        private String onImagePath;
        private String offCommand;  // 格式：指令名称|协议类型|IP地址|端口|十六进制内容
        private String onCommand;   // 格式：指令名称|协议类型|IP地址|端口|十六进制内容
        private boolean currentState; // true=开，false=关

        public SwitchButtonData(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
            this.currentState = false; // 默认关状态
        }

        // Getter和Setter方法
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getOffImagePath() { return offImagePath; }
        public void setOffImagePath(String offImagePath) { this.offImagePath = offImagePath; }

        public String getOnImagePath() { return onImagePath; }
        public void setOnImagePath(String onImagePath) { this.onImagePath = onImagePath; }

        public String getOffCommand() { return offCommand; }
        public void setOffCommand(String offCommand) { this.offCommand = offCommand; }

        public String getOnCommand() { return onCommand; }
        public void setOnCommand(String onCommand) { this.onCommand = onCommand; }

        public boolean getCurrentState() { return currentState; }
        public void setCurrentState(boolean currentState) { this.currentState = currentState; }

        public String getCurrentImagePath() {
            return currentState ? onImagePath : offImagePath;
        }

        public String getCurrentCommand() {
            return currentState ? offCommand : onCommand; // 注意：当前是开状态时，点击应该发送关指令
        }

        public String getNextCommand() {
            return currentState ? offCommand : onCommand; // 下一个状态的指令
        }
    }

    /**
     * 打开开关按钮编辑对话框
     */
    private void openSwitchButtonEditDialog(JPanel instance) {
        JDialog dialog = new JDialog(this, "开关按钮配置", true);
        dialog.setSize(680, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(new Color(248, 250, 252));
        dialog.setResizable(false);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建内容面板
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(30, 25, 30, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 获取现有的开关按钮数据
        final SwitchButtonData existingData = getSwitchButtonData(instance);
        final SwitchButtonData finalData = (existingData != null) ? existingData :
            new SwitchButtonData("switch_" + System.currentTimeMillis(), "开关按钮");

        // 关状态配置区域
        final JPanel offStatePanel = createStateConfigPanel("关状态配置", "●", new Color(239, 68, 68), finalData.getOffCommand(), "关状态指令");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        contentPanel.add(offStatePanel, gbc);

        // 开状态配置区域
        final JPanel onStatePanel = createStateConfigPanel("开状态配置", "●", new Color(34, 197, 94), finalData.getOnCommand(), "开状态指令");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        contentPanel.add(onStatePanel, gbc);

        // 当前状态显示区域
        JPanel statusPanel = createStatusPanel(finalData);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(30, 10, 20, 10);
        contentPanel.add(statusPanel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        dialog.add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 25, 30));

        // 获取文本字段引用（用于确定按钮的事件处理）
        final JTextField[] fieldRefs = getTextFieldsFromPanels(offStatePanel, onStatePanel);
        final JTextField offCommandField = fieldRefs[0];
        final JTextField onCommandField = fieldRefs[1];

        JButton confirmButton = createStyledButton("确定", new Color(34, 197, 94), Color.WHITE);
        confirmButton.setPreferredSize(new Dimension(100, 42));

        confirmButton.addActionListener(e -> {
            // 获取输入的指令
            String offCommand = offCommandField.getText().trim();
            String onCommand = onCommandField.getText().trim();

            // 校验：开关按钮必须配置两条指令
            if (offCommand.isEmpty()) {
                showStyledErrorDialog(dialog, "请配置关状态指令", "校验失败");
                return;
            }

            if (onCommand.isEmpty()) {
                showStyledErrorDialog(dialog, "请配置开状态指令", "校验失败");
                return;
            }

            // 校验指令格式（应该包含5个部分：名称|协议|IP|端口|指令内容）
            String[] offParts = offCommand.split("\\|");
            String[] onParts = onCommand.split("\\|");

            if (offParts.length < 5) {
                showStyledErrorDialog(dialog,
                    "关状态指令格式错误，应为：指令名称|协议类型|IP地址|端口|指令内容",
                    "校验失败");
                return;
            }

            if (onParts.length < 5) {
                showStyledErrorDialog(dialog,
                    "开状态指令格式错误，应为：指令名称|协议类型|IP地址|端口|指令内容",
                    "校验失败");
                return;
            }

            // 保存配置
            finalData.setOffCommand(offCommand);
            finalData.setOnCommand(onCommand);

            // 同时保存到commands属性（兼容现有系统）
            java.util.List<String> commands = new java.util.ArrayList<>();
            commands.add(offCommand);
            commands.add(onCommand);
            instance.putClientProperty("commands", commands);

            // 保存开关按钮数据
            instance.putClientProperty("switchButtonData", finalData);

            System.out.println("开关按钮配置已保存");
            System.out.println("关状态指令: " + finalData.getOffCommand());
            System.out.println("开状态指令: " + finalData.getOnCommand());

            dialog.dispose();
        });

        JButton cancelButton = createStyledButton("取消", new Color(148, 163, 184), Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 42));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(248, 250, 252));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("🔧 开关按钮配置");
        titleLabel.setFont(getUnicodeFont(18));
        titleLabel.setForeground(new Color(30, 41, 59));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("配置开关按钮的两种状态指令");
        subtitleLabel.setFont(getUnicodeFont(12));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        return titlePanel;
    }

    /**
     * 创建状态配置面板
     */
    private JPanel createStateConfigPanel(String title, String icon, Color iconColor, String command, String commandType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // 标题区域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 18));
        iconLabel.setForeground(iconColor);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(getUnicodeFont(15));
        titleLabel.setForeground(new Color(30, 41, 59));

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // 内容区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JTextField commandField = createStyledTextField(command);
        commandField.putClientProperty("commandType", commandType);
        contentPanel.add(commandField, BorderLayout.CENTER);

        JButton editButton = createStyledButton("编辑", new Color(59, 130, 246), Color.WHITE);
        editButton.setPreferredSize(new Dimension(75, 40));
        editButton.addActionListener(e -> {
            String editedCommand = editSwitchCommand(commandType, commandField.getText());
            if (editedCommand != null) {
                commandField.setText(editedCommand);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(editButton);
        contentPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建状态显示面板
     */
    private JPanel createStatusPanel(SwitchButtonData data) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel statusLabel = new JLabel("当前状态：");
        statusLabel.setFont(getUnicodeFont(13));
        statusLabel.setForeground(new Color(71, 85, 105));

        String stateText = data.getCurrentState() ? "开启" : "关闭";
        String stateIcon = data.getCurrentState() ? "●" : "●";
        Color stateColor = data.getCurrentState() ? new Color(34, 197, 94) : new Color(239, 68, 68);

        JLabel stateIconLabel = new JLabel(stateIcon);
        stateIconLabel.setFont(new Font("Arial", Font.BOLD, 16));
        stateIconLabel.setForeground(stateColor);
        stateIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 6));

        JLabel stateTextLabel = new JLabel(stateText);
        stateTextLabel.setFont(getUnicodeFont(13));
        stateTextLabel.setForeground(stateColor);

        panel.add(statusLabel);
        panel.add(stateIconLabel);
        panel.add(stateTextLabel);

        return panel;
    }

    /**
     * 创建样式化的文本框
     */
    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(getUnicodeFont(12));
        field.setPreferredSize(new Dimension(400, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(new Color(248, 250, 252));

        // 添加焦点效果
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(59, 130, 246), 2),
                    BorderFactory.createEmptyBorder(9, 11, 9, 11)
                ));
                field.setBackground(Color.WHITE);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
                field.setBackground(new Color(248, 250, 252));
            }
        });

        return field;
    }

    /**
     * 创建样式化的按钮
     */
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(getUnicodeFont(12));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // 添加悬停效果
        Color originalBg = bgColor;
        Color hoverBg = new Color(
            Math.max(0, originalBg.getRed() - 20),
            Math.max(0, originalBg.getGreen() - 20),
            Math.max(0, originalBg.getBlue() - 20)
        );

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalBg);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(
                    Math.max(0, originalBg.getRed() - 40),
                    Math.max(0, originalBg.getGreen() - 40),
                    Math.max(0, originalBg.getBlue() - 40)
                ));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBg);
            }
        });

        return button;
    }

    /**
     * 从面板中获取文本框引用
     */
    private JTextField[] getTextFieldsFromPanels(JPanel offPanel, JPanel onPanel) {
        JTextField offField = findTextFieldInPanel(offPanel);
        JTextField onField = findTextFieldInPanel(onPanel);
        return new JTextField[]{offField, onField};
    }

    /**
     * 在面板中查找文本框
     */
    private JTextField findTextFieldInPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JTextField field = findTextFieldInPanel((JPanel) comp);
                if (field != null) return field;
            } else if (comp instanceof JTextField) {
                return (JTextField) comp;
            }
        }
        return null;
    }

    /**
     * 显示样式化的错误对话框
     */
    private void showStyledErrorDialog(JDialog parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 编辑开关指令
     */
    private String editSwitchCommand(String title, String existingCommand) {
        final String[] result = {existingCommand};

        JDialog editDialog = new JDialog(this, "编辑" + title, true);
        editDialog.setSize(520, 480);
        editDialog.setLocationRelativeTo(this);
        editDialog.setLayout(new BorderLayout());
        editDialog.setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 解析现有指令
        String name = "", protocol = "TCP", ip = "127.0.0.1", port = "8080", command = "";
        if (existingCommand != null && !existingCommand.isEmpty()) {
            String[] parts = existingCommand.split("\\|");
            if (parts.length >= 5) {
                name = parts[0];
                protocol = parts[1];
                ip = parts[2];
                port = parts[3];
                command = parts[4];
            }
        }

        // 协议类型选择
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel protocolLabel = new JLabel("协议类型:");
        protocolLabel.setFont(getUnicodeFont(13));
        protocolLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(protocolLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel protocolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        protocolPanel.setBackground(Color.WHITE);
        protocolPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        ButtonGroup protocolGroup = new ButtonGroup();
        JRadioButton tcpRadio = new JRadioButton("TCP", "TCP".equals(protocol));
        JRadioButton udpRadio = new JRadioButton("UDP", "UDP".equals(protocol));
        tcpRadio.setFont(getUnicodeFont(12));
        udpRadio.setFont(getUnicodeFont(12));
        tcpRadio.setBackground(Color.WHITE);
        udpRadio.setBackground(Color.WHITE);
        protocolGroup.add(tcpRadio);
        protocolGroup.add(udpRadio);
        protocolPanel.add(tcpRadio);
        protocolPanel.add(udpRadio);
        mainPanel.add(protocolPanel, gbc);

        // 指令名称
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("指令名称:");
        nameLabel.setFont(getUnicodeFont(13));
        nameLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField nameField = new JTextField(name, 20);
        nameField.setFont(getUnicodeFont(13));
        nameField.setPreferredSize(new Dimension(280, 36));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        mainPanel.add(nameField, gbc);

        // IP地址
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel ipLabel = new JLabel("IP地址:");
        ipLabel.setFont(getUnicodeFont(13));
        ipLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(ipLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField ipField = new JTextField(ip, 20);
        ipField.setFont(getUnicodeFont(13));
        ipField.setPreferredSize(new Dimension(280, 36));
        ipField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        mainPanel.add(ipField, gbc);

        // 端口
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel portLabel = new JLabel("端口:");
        portLabel.setFont(getUnicodeFont(13));
        portLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(portLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField portField = new JTextField(port, 20);
        portField.setFont(getUnicodeFont(13));
        portField.setPreferredSize(new Dimension(280, 36));
        portField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        mainPanel.add(portField, gbc);

        // 指令内容
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel commandLabel = new JLabel("指令内容:");
        commandLabel.setFont(getUnicodeFont(13));
        commandLabel.setForeground(new Color(73, 80, 87));
        mainPanel.add(commandLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField commandField = new JTextField(command, 20);
        commandField.setFont(new Font("Consolas", Font.PLAIN, 13));
        commandField.setPreferredSize(new Dimension(280, 36));
        commandField.setBackground(new Color(248, 249, 250));
        commandField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        mainPanel.add(commandField, gbc);

        editDialog.add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(getUnicodeFont(12));
        confirmButton.setPreferredSize(new Dimension(80, 35));
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmButton.setFocusPainted(false);

        confirmButton.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newProtocol = tcpRadio.isSelected() ? "TCP" : "UDP";
            String newIp = ipField.getText().trim();
            String newPort = portField.getText().trim();
            String newCommand = commandField.getText().trim();

            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "请输入指令名称", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newIp.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "请输入IP地址", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newPort.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "请输入端口", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 构建完整的指令字符串
            result[0] = String.format("%s|%s|%s|%s|%s", newName, newProtocol, newIp, newPort, newCommand);
            editDialog.dispose();
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(getUnicodeFont(12));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> editDialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        editDialog.setVisible(true);

        return result[0];
    }

    /**
     * 复选框项目类
     */
    private static class CheckBoxItem {
        private String text;
        private boolean selected;

        public CheckBoxItem(String text, boolean selected) {
            this.text = text;
            this.selected = selected;
        }

        public String getText() {
            return text;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * 带延时功能的复选框项目类
     */
    private static class CheckBoxItemWithDelay {
        private String commandData;
        private boolean selected;

        public CheckBoxItemWithDelay(String commandData, boolean selected) {
            this.commandData = commandData;
            this.selected = selected;
        }

        public String getCommandData() {
            return commandData;
        }

        public void setCommandData(String commandData) {
            this.commandData = commandData;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getDisplayText() {
            String[] parts = commandData.split("\\|");
            if (parts.length >= 5) {
                String name = parts[0];
                String protocol = parts[1];
                String ip = parts[2];
                String port = parts[3];
                String command = parts[4];

                // 特殊处理实例组显示
                if ("GROUP".equals(protocol)) {
                    return name; // 直接显示实例组名称
                }

                return String.format("%s | %s | %s:%s | %s", name, protocol, ip, port, command);
            }
            return commandData;
        }

        public double getDelay() {
            String[] parts = commandData.split("\\|");
            if (parts.length >= 6) {
                try {
                    return Double.parseDouble(parts[5]);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return 0.0;
        }

        @Override
        public String toString() {
            return getDisplayText();
        }
    }

    /**
     * 带复选框的列表单元格渲染器
     */
    private class CheckBoxListCellRenderer extends JCheckBox implements ListCellRenderer<CheckBoxItem> {
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckBoxItem> list, CheckBoxItem value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // 智能截断文本以适应列表宽度
            String originalText = value.getText();
            String displayText = truncateTextToFit(originalText, this, list.getWidth() - 60); // 预留复选框和边距空间

            setText(displayText);
            setSelected(value.isSelected());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    /**
     * 带延时按钮的复选框列表单元格渲染器
     */
    private class CheckBoxWithDelayListCellRenderer extends JPanel implements ListCellRenderer<CheckBoxItemWithDelay> {
        private JCheckBox checkBox;
        private JButton delayButton;

        public CheckBoxWithDelayListCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);

            // 复选框
            checkBox = new JCheckBox();
            checkBox.setOpaque(false);
            add(checkBox, BorderLayout.WEST);

            // 延时按钮
            delayButton = new JButton("延时");
            delayButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            delayButton.setPreferredSize(new Dimension(60, 25));
            delayButton.setBackground(new Color(108, 117, 125));
            delayButton.setForeground(Color.WHITE);
            delayButton.setBorder(BorderFactory.createEmptyBorder());
            delayButton.setFocusPainted(false);

            // 使用GridBagLayout实现垂直居中
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;
            buttonPanel.add(delayButton, gbc);

            add(buttonPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CheckBoxItemWithDelay> list, CheckBoxItemWithDelay value,
                int index, boolean isSelected, boolean cellHasFocus) {

            // 设置背景色
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                checkBox.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                checkBox.setForeground(list.getForeground());
            }

            // 智能截断文本以适应列表宽度
            String originalText = value.getDisplayText();
            String displayText = truncateTextToFit(originalText, checkBox, list.getWidth() - 120); // 预留复选框和延时按钮空间

            checkBox.setText(displayText);
            checkBox.setSelected(value.isSelected());
            checkBox.setEnabled(list.isEnabled());
            checkBox.setFont(list.getFont());

            // 更新延时按钮显示
            double delay = value.getDelay();
            if (delay > 0) {
                delayButton.setText(delay + "s");
                delayButton.setBackground(new Color(40, 167, 69)); // 绿色表示已设置延时
            } else {
                delayButton.setText("延时");
                delayButton.setBackground(new Color(108, 117, 125)); // 灰色表示未设置延时
            }

            return this;
        }
    }

    /**
     * 指令列表的延时渲染器
     */
    private class CommandListWithDelayRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel textLabel;
        private JButton delayButton;

        public CommandListWithDelayRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);

            // 文本标签
            textLabel = new JLabel();
            textLabel.setOpaque(false);
            textLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            add(textLabel, BorderLayout.CENTER);

            // 延时按钮
            delayButton = new JButton("延时");
            delayButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            delayButton.setPreferredSize(new Dimension(60, 25));
            delayButton.setBackground(new Color(108, 117, 125));
            delayButton.setForeground(Color.WHITE);
            delayButton.setBorder(BorderFactory.createEmptyBorder());
            delayButton.setFocusPainted(false);

            // 使用GridBagLayout实现垂直居中
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;
            buttonPanel.add(delayButton, gbc);

            add(buttonPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {

            // 设置背景色
            if (isSelected) {
                setBackground(new Color(0, 123, 255));
                textLabel.setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                textLabel.setForeground(new Color(52, 58, 64));
            }

            // 显示完整指令信息
            String displayText = value.toString();
            String[] parts = displayText.split("\\|");
            if (parts.length >= 5) {
                // 格式化显示：指令名称 | 协议 | IP:端口 | 指令内容
                String name = parts[0];
                String protocol = parts[1];
                String ip = parts[2];
                String port = parts[3];
                String command = parts[4];

                displayText = String.format("%s | %s | %s:%s | %s", name, protocol, ip, port, command);
            }

            // 智能截断文本以适应列表宽度
            String finalText = "📋 " + truncateTextToFit(displayText, textLabel, list.getWidth() - 120);
            textLabel.setText(finalText);
            textLabel.setFont(getUnicodeFont(13));

            // 更新延时按钮显示
            double delay = getCommandDelay(value);
            if (delay > 0) {
                delayButton.setText(delay + "s");
                delayButton.setBackground(new Color(40, 167, 69)); // 绿色表示已设置延时
            } else {
                delayButton.setText("延时");
                delayButton.setBackground(new Color(108, 117, 125)); // 灰色表示未设置延时
            }

            return this;
        }
    }

    /**
     * 切换到指定页面
     */
    private void switchToPage(String pageName) {
        System.out.println("切换到页面: " + pageName);

        // 只有当前页面有实际内容时才保存
        if (currentPageName != null && editCanvas != null && editCanvas.getComponentCount() > 0) {
            System.out.println("保存当前页面内容: " + currentPageName + ", 组件数量: " + editCanvas.getComponentCount());
            saveCurrentPageContent();
        } else {
            System.out.println("跳过保存当前页面内容 - 页面为空或无画布");
        }

        // 加载新页面内容
        loadPageContent(pageName);

        // 更新当前页面
        currentPageName = pageName;
        updateCurrentPageLabel(pageName);

        // 同步页面列表的选中状态
        syncPageListSelection(pageName);

        // 刷新目标页面下拉框，排除新的当前页面
        refreshTargetPageComboBox(currentPageName);

        // 清除选中的实例
        selectedInstance = null;

        System.out.println("页面切换完成: " + pageName);
    }

    /**
     * 同步页面列表的选中状态
     */
    private void syncPageListSelection(String pageName) {
        if (pageList != null && pageListModel != null && pageName != null) {
            int pageIndex = findPageIndex(pageName);
            if (pageIndex >= 0) {
                // 找到匹配的页面，设置选中状态
                if (pageList.getSelectedIndex() != pageIndex) {
                    pageList.setSelectedIndex(pageIndex);
                    System.out.println("同步页面列表选中状态: " + pageName + " (索引: " + pageIndex + ")");
                }
            } else {
                // 如果没有找到匹配的页面，清除选中状态
                pageList.clearSelection();
                System.out.println("页面 " + pageName + " 不在列表中，清除选中状态");
            }
        }
    }

    /**
     * 查找页面在列表中的索引
     */
    private int findPageIndex(String pageName) {
        if (pageListModel != null && pageName != null) {
            for (int i = 0; i < pageListModel.getSize(); i++) {
                if (pageName.equals(pageListModel.getElementAt(i))) {
                    return i;
                }
            }
        }
        return -1; // 未找到
    }

    /**
     * 保存当前页面内容
     */
    private void saveCurrentPageContent() {
        if (currentPageName != null) {
            // 总是基于当前画布状态保存页面内容，确保数据同步
            if (editCanvas != null && editCanvas.getComponentCount() > 0) {
                java.util.List<Component> components = new java.util.ArrayList<>();
                for (Component comp : editCanvas.getComponents()) {
                    components.add(comp);
                }
                pageContents.put(currentPageName, components);
                System.out.println("从editCanvas保存页面内容: " + currentPageName + ", 组件数量: " + components.size());
            } else {
                // 如果editCanvas为空，创建空的组件列表
                pageContents.put(currentPageName, new java.util.ArrayList<>());
                System.out.println("创建空页面内容: " + currentPageName);
            }
        } else {
            System.out.println("无法保存页面内容 - currentPageName为null");
        }
    }


    /**
     * 加载页面内容
     */
    private void loadPageContent(String pageName) {
        if (editCanvas != null) {
            // 先清空editCanvas中的所有组件
            editCanvas.removeAll();
            System.out.println("已清空editCanvas");

            java.util.List<Component> components = pageContents.get(pageName);

            if (components != null) {
                // 恢复页面内容，重新计算坐标
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;

                        // 获取相对位置数据
                        com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                            (com.feixiang.tabletcontrol.model.RelativePosition) panel.getClientProperty("relativePosition");

                        if (relativePos != null) {
                            // 获取当前画布的实际显示尺寸
                            int canvasWidth = editCanvas.getWidth();
                            int canvasHeight = editCanvas.getHeight();

                            if (canvasWidth > 0 && canvasHeight > 0) {
                                // 基于画布尺寸重新计算坐标
                                com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition newPos =
                                    relativePos.toAbsolute(canvasWidth, canvasHeight);

                                panel.setBounds(newPos.x, newPos.y, newPos.width, newPos.height);

                                System.out.println("重新计算组件坐标: " + relativePos +
                                                 " -> 画布(" + canvasWidth + "x" + canvasHeight +
                                                 ") -> 坐标(" + newPos.x + "," + newPos.y +
                                                 "," + newPos.width + "," + newPos.height + ")");
                            } else {
                                System.out.println("画布尺寸无效，使用原始坐标: " + panel.getBounds());
                            }
                        } else {
                            System.out.println("组件缺少相对位置数据，使用原始坐标: " + panel.getBounds());
                        }
                    }

                    if (comp instanceof JPanel) {
                        addInstanceToEditCanvas((JPanel) comp);
                    } else {
                        editCanvas.add(comp);
                    }
                }
                System.out.println("已加载页面内容: " + pageName + ", 组件数量: " + components.size());
            } else {
                // 新页面，创建空的内容列表
                pageContents.put(pageName, new java.util.ArrayList<>());
                System.out.println("创建新页面: " + pageName);
            }

            // 加载页面背景（如果有）
            loadPageBackgroundInEditCanvas(pageName);

            editCanvas.revalidate();
            editCanvas.repaint();
        }
    }

    /**
     * 加载页面背景
     */
    private void loadPageBackground(String pageName) {
        if (editCanvas == null || pageName == null) {
            return;
        }

        String backgroundPath = pageBackgrounds.get(pageName);
        if (backgroundPath != null && !backgroundPath.isEmpty()) {
            try {
                File backgroundFile = new File(backgroundPath);
                if (backgroundFile.exists()) {
                    System.out.println("加载页面背景: " + pageName + " -> " + backgroundPath);

                    // 读取背景图片
                    BufferedImage originalImage = ImageIO.read(backgroundFile);
                    if (originalImage != null) {
                        // 获取画布尺寸
                        int canvasWidth = editCanvas.getWidth();
                        int canvasHeight = editCanvas.getHeight();

                        // 缩放图片到画布大小
                        Image scaledImage = originalImage.getScaledInstance(canvasWidth, canvasHeight, Image.SCALE_SMOOTH);
                        ImageIcon backgroundIcon = new ImageIcon(scaledImage);

                        // 创建背景标签
                        JLabel backgroundLabel = new JLabel(backgroundIcon);
                        backgroundLabel.setBounds(0, 0, canvasWidth, canvasHeight);
                        backgroundLabel.setOpaque(false);
                        backgroundLabel.putClientProperty("isBackground", true);
                        backgroundLabel.putClientProperty("backgroundFile", backgroundPath);

                        // 添加背景到画布最底层
                        editCanvas.add(backgroundLabel, 0);

                        System.out.println("页面背景加载成功: " + pageName);
                    } else {
                        System.out.println("无法读取背景图片: " + backgroundPath);
                    }
                } else {
                    System.out.println("背景文件不存在: " + backgroundPath);
                    // 从背景信息中移除无效的路径
                    pageBackgrounds.remove(pageName);
                }
            } catch (Exception e) {
                System.out.println("加载页面背景失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加组件到当前页面内容
     */
    private void addToCurrentPageContent(Component component) {
        if (currentPageName != null) {
            java.util.List<Component> components = pageContents.get(currentPageName);
            if (components == null) {
                components = new java.util.ArrayList<>();
                pageContents.put(currentPageName, components);
            }
            components.add(component);
            System.out.println("组件已添加到页面: " + currentPageName + ", 当前组件数量: " + components.size());
        }
    }

    /**
     * 从当前页面内容中移除组件
     */
    private void removeFromCurrentPageContent(Component component) {
        if (currentPageName != null) {
            java.util.List<Component> components = pageContents.get(currentPageName);
            if (components != null) {
                components.remove(component);
                System.out.println("组件已从页面移除: " + currentPageName + ", 当前组件数量: " + components.size());
            }
        }
    }

    /**
     * 复制页面内容
     */
    private void copyPageContent(String originalPageName, String copyPageName) {
        java.util.List<Component> originalComponents = pageContents.get(originalPageName);
        if (originalComponents == null || originalComponents.isEmpty()) {
            // 原页面没有内容，创建空的内容列表
            pageContents.put(copyPageName, new java.util.ArrayList<>());
            System.out.println("原页面无内容，创建空的复制页面: " + copyPageName);
            return;
        }

        java.util.List<Component> copiedComponents = new java.util.ArrayList<>();

        for (Component originalComp : originalComponents) {
            if (originalComp instanceof JPanel) {
                // 复制实例
                JPanel copiedInstance = copyInstanceWithResetFunction((JPanel) originalComp);
                if (copiedInstance != null) {
                    copiedComponents.add(copiedInstance);
                }
            } else {
                // 复制其他类型的组件（图片、文本、视频等）
                Component copiedComp = copyComponent(originalComp);
                if (copiedComp != null) {
                    copiedComponents.add(copiedComp);
                }
            }
        }

        pageContents.put(copyPageName, copiedComponents);
        System.out.println("页面内容复制完成: " + originalPageName + " → " + copyPageName +
                         ", 组件数量: " + copiedComponents.size());
    }

    /**
     * 复制实例并重置功能配置
     */
    private JPanel copyInstanceWithResetFunction(JPanel originalInstance) {
        try {
            // 创建新实例容器
            JPanel newInstance = new JPanel();
            newInstance.setLayout(null); // 绝对布局
            newInstance.setOpaque(false);

            // 复制相对位置数据并基于百分比设置位置
            com.feixiang.tabletcontrol.model.RelativePosition relativePos =
                (com.feixiang.tabletcontrol.model.RelativePosition) originalInstance.getClientProperty("relativePosition");
            if (relativePos != null) {
                // 创建新的双锚点相对位置对象（避免引用同一个对象）
                com.feixiang.tabletcontrol.model.RelativePosition newRelativePos =
                    com.feixiang.tabletcontrol.model.RelativePosition.createDualAnchor(
                        relativePos.getTopLeftX(), relativePos.getTopLeftY(),
                        relativePos.getBottomRightX(), relativePos.getBottomRightY());
                newInstance.putClientProperty("relativePosition", newRelativePos);

                // 基于百分比和当前画布尺寸设置位置
                if (editCanvas != null) {
                    int canvasWidth = editCanvas.getWidth();
                    int canvasHeight = editCanvas.getHeight();
                    if (canvasWidth > 0 && canvasHeight > 0) {
                        com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition pos =
                            newRelativePos.toAbsolute(canvasWidth, canvasHeight);
                        newInstance.setBounds(pos.x, pos.y, pos.width, pos.height);
                        System.out.println("复制实例位置(百分比): " + newRelativePos + " -> 画布坐标: " +
                                         "(" + pos.x + "," + pos.y + "," + pos.width + "," + pos.height + ")");
                    } else {
                        // 画布尺寸无效时，使用原实例的bounds作为备用
                        newInstance.setBounds(originalInstance.getBounds());
                        System.out.println("画布尺寸无效，使用原实例bounds作为备用");
                    }
                } else {
                    // editCanvas为null时，使用原实例的bounds作为备用
                    newInstance.setBounds(originalInstance.getBounds());
                    System.out.println("editCanvas为null，使用原实例bounds作为备用");
                }
            } else {
                // 没有相对位置数据时，使用原实例的bounds作为备用
                newInstance.setBounds(originalInstance.getBounds());
                System.out.println("缺少相对位置数据，使用原实例bounds作为备用");
            }

            // 重置功能类型为"纯文本"
            newInstance.putClientProperty("functionType", "纯文本");

            // 清除功能配置
            newInstance.putClientProperty("targetPage", null);
            newInstance.putClientProperty("commands", null);
            newInstance.putClientProperty("instanceGroupName", null);

            // 复制所有子组件
            Component[] components = originalInstance.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel originalLabel = (JLabel) comp;
                    JLabel newLabel = new JLabel();

                    // 复制图标
                    if (originalLabel.getIcon() != null) {
                        newLabel.setIcon(originalLabel.getIcon());
                    }

                    // 复制文本
                    if (originalLabel.getText() != null) {
                        newLabel.setText(originalLabel.getText());
                    }

                    // 复制字体和颜色
                    newLabel.setFont(originalLabel.getFont());
                    newLabel.setForeground(originalLabel.getForeground());

                    // 复制对齐方式
                    newLabel.setHorizontalAlignment(originalLabel.getHorizontalAlignment());
                    newLabel.setVerticalAlignment(originalLabel.getVerticalAlignment());
                    newLabel.setHorizontalTextPosition(originalLabel.getHorizontalTextPosition());
                    newLabel.setVerticalTextPosition(originalLabel.getVerticalTextPosition());

                    // 复制透明度设置
                    newLabel.setOpaque(originalLabel.isOpaque());

                    // 复制位置和尺寸
                    newLabel.setBounds(originalLabel.getBounds());

                    // 复制客户端属性
                    ImageIcon originalIcon = (ImageIcon) originalLabel.getClientProperty("originalIcon");
                    if (originalIcon != null) {
                        newLabel.putClientProperty("originalIcon", originalIcon);
                    }

                    Integer originalFontSize = (Integer) originalLabel.getClientProperty("originalFontSize");
                    if (originalFontSize != null) {
                        newLabel.putClientProperty("originalFontSize", originalFontSize);
                    } else {
                        newLabel.putClientProperty("originalFontSize", originalLabel.getFont().getSize());
                    }

                    // 设置标签属性
                    newLabel.setEnabled(true);
                    newLabel.setFocusable(false);

                    newInstance.add(newLabel);
                }
            }

            // 为复制的实例添加鼠标事件处理
            addInstanceMouseHandlers(newInstance);

            return newInstance;

        } catch (Exception e) {
            System.out.println("复制实例失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 复制其他类型的组件
     */
    private Component copyComponent(Component originalComp) {
        // 这里可以根据需要扩展复制其他类型的组件
        // 目前主要处理实例，其他组件类型可以后续添加
        System.out.println("暂不支持复制此类型组件: " + originalComp.getClass().getSimpleName());
        return null;
    }

    // 动态获取并确保 USERDATA 目录存在
    private File getUserDataDir() {
        String userDir = System.getProperty("user.dir");
        System.out.println("当前工作目录: " + userDir);

        File dir = new File(userDir, "USERDATA");
        System.out.println("尝试USERDATA路径: " + dir.getAbsolutePath());
        System.out.println("USERDATA目录存在: " + dir.exists());

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("创建USERDATA目录: " + created);
        }

        // 列出目录内容
        if (dir.exists()) {
            File[] files = dir.listFiles();
            System.out.println("USERDATA目录内容:");
            if (files != null) {
                for (File file : files) {
                    System.out.println("  - " + file.getName() + " (存在: " + file.exists() + ")");
                }
            }
        }

        return dir;
    }

    // 若图标路径位于 USERDATA 下，则转为相对路径
    private String toRelativeIfUnderUserData(String path) {
        if (path == null || path.isEmpty()) return path;
        File abs = new File(path);
        File user = getUserDataDir().getAbsoluteFile();
        String up = user.getAbsolutePath().replace('\\','/');
        String ap = abs.getAbsolutePath().replace('\\','/');
        if (ap.startsWith(up + "/")) return ap.substring(up.length() + 1);
        return path;
    }

    // 解析保存的图标路径，返回可用绝对路径
    private String resolveIconPath(String saved) {
        if (saved == null || saved.isEmpty()) return null;
        File f = new File(saved);
        if (f.isAbsolute() && f.exists()) return f.getAbsolutePath();
        File user = getUserDataDir();
        File rel = new File(user, saved);
        if (rel.exists()) return rel.getAbsolutePath();
        String norm = saved.replace('\\','/');
        rel = new File(user, norm);
        if (rel.exists()) return rel.getAbsolutePath();
        String savedNorm = f.getAbsolutePath().replace('\\','/');
        int idx = savedNorm.indexOf("/USERDATA/");
        if (idx != -1) {
            String relative = savedNorm.substring(idx + "/USERDATA/".length());
            File rel2 = new File(user, relative);
            if (rel2.exists()) return rel2.getAbsolutePath();
        }
        return null;
    }

    /**
     * 保存项目数据（显示提示）
     */
    private void saveProjectData() {
        saveProjectData(true);
    }

    /**
     * 保存项目数据
     * @param showMessage 是否显示保存成功提示
     */
    private void saveProjectData(boolean showMessage) {
        try {
            // 保存当前页面内容
            saveCurrentPageContent();

            // 创建保存目录
            File userDataDir = getUserDataDir();

            // 使用固定文件名，替换原文件
            String fileName = "project_data.json";
            File saveFile = new File(userDataDir, fileName);

            // 创建项目数据JSON对象
            JsonObject projectData = new JsonObject();

            // 保存页面列表
            com.google.gson.JsonArray pagesArray = new com.google.gson.JsonArray();
            for (int i = 0; i < pageListModel.getSize(); i++) {
                pagesArray.add(pageListModel.getElementAt(i));
            }
            projectData.add("pages", pagesArray);

            // 保存当前页面
            if (currentPageName != null) {
                projectData.addProperty("currentPage", currentPageName);
            }

            // 保存编辑分辨率信息
            if (resolutionComboBox != null) {
                String selectedResolution = (String) resolutionComboBox.getSelectedItem();
                if (selectedResolution != null) {
                    projectData.addProperty("editResolution", selectedResolution);
                }
            }

            // 保存每个页面的内容
            JsonObject pageContentsJson = new JsonObject();
            for (String pageName : pageContents.keySet()) {
                com.google.gson.JsonArray pageComponentsArray = new com.google.gson.JsonArray();
                java.util.List<Component> components = pageContents.get(pageName);

                if (components != null) {
                    for (Component comp : components) {
                        if (comp instanceof JPanel) {
                            JsonObject instanceData = saveInstanceData((JPanel) comp);
                            if (instanceData != null) {
                                pageComponentsArray.add(instanceData);
                            }
                        }
                        // 可以扩展保存其他类型的组件
                    }
                }

                pageContentsJson.add(pageName, pageComponentsArray);
            }
            projectData.add("pageContents", pageContentsJson);

            // 保存每个页面的背景信息
            JsonObject pageBackgroundsJson = new JsonObject();
            for (String pageName : pageBackgrounds.keySet()) {
                String backgroundPath = pageBackgrounds.get(pageName);
                if (backgroundPath != null && !backgroundPath.isEmpty()) {
                    pageBackgroundsJson.addProperty(pageName, backgroundPath);
                }
            }
            projectData.add("pageBackgrounds", pageBackgroundsJson);

            // 保存定时器数据
            if (timerTasks != null && !timerTasks.isEmpty()) {
                com.google.gson.JsonArray timerArray = new com.google.gson.JsonArray();
                for (ScheduledTask task : timerTasks) {
                    JsonObject taskJson = new JsonObject();
                    taskJson.addProperty("title", task.getTitle());
                    taskJson.addProperty("executeInterface", task.getExecuteInterface());
                    taskJson.addProperty("time", task.getTime());

                    // 保存重复设置
                    com.google.gson.JsonArray repeatArray = new com.google.gson.JsonArray();
                    boolean[] repeatDays = task.getRepeatDays();
                    for (boolean day : repeatDays) {
                        repeatArray.add(day);
                    }
                    taskJson.add("repeatDays", repeatArray);

                    timerArray.add(taskJson);
                }
                projectData.add("timerTasks", timerArray);
            }

            // 写入文件（使用UTF-8编码）
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(saveFile),
                java.nio.charset.StandardCharsets.UTF_8
            )) {
                Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                gson.toJson(projectData, writer);
                writer.flush(); // 确保数据写入
            }

            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                    "项目数据保存成功！\n文件位置: " + saveFile.getAbsolutePath(),
                    "保存成功", JOptionPane.INFORMATION_MESSAGE);
            }

            System.out.println("项目数据已保存到: " + saveFile.getAbsolutePath());

        } catch (Exception e) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                    "保存失败: " + e.getMessage(),
                    "保存错误", JOptionPane.ERROR_MESSAGE);
            }
            System.out.println("保存项目数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存实例数据到JSON (直接保存编辑画布坐标)
     */
    private JsonObject saveInstanceData(JPanel instance) {
        try {
            JsonObject instanceData = new JsonObject();

            // 直接保存编辑画布中的像素坐标
            Rectangle bounds = instance.getBounds();
            instanceData.addProperty("editCanvasX", bounds.x);
            instanceData.addProperty("editCanvasY", bounds.y);
            instanceData.addProperty("editCanvasWidth", bounds.width);
            instanceData.addProperty("editCanvasHeight", bounds.height);

            // 保存编辑分辨率和画布尺寸作为参考
            String editResolution = currentEditResolution != null ? currentEditResolution : "1024x768";
            instanceData.addProperty("editResolution", editResolution);
            instanceData.addProperty("canvasDisplayWidth", editCanvas.getWidth());
            instanceData.addProperty("canvasDisplayHeight", editCanvas.getHeight());

            // 同时保存到客户端属性，供主界面使用
            instance.putClientProperty("editCanvasX", bounds.x);
            instance.putClientProperty("editCanvasY", bounds.y);
            instance.putClientProperty("editCanvasWidth", bounds.width);
            instance.putClientProperty("editCanvasHeight", bounds.height);
            instance.putClientProperty("editResolution", editResolution);
            instance.putClientProperty("canvasDisplayWidth", editCanvas.getWidth());
            instance.putClientProperty("canvasDisplayHeight", editCanvas.getHeight());

            System.out.println("保存编辑画布坐标: (" + bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height +
                             ") 编辑分辨率: " + editResolution +
                             " 画布尺寸: " + editCanvas.getWidth() + "x" + editCanvas.getHeight());



            // 保存功能配置
            String functionType = (String) instance.getClientProperty("functionType");
            if (functionType != null) instanceData.addProperty("functionType", functionType);

            String targetPage = (String) instance.getClientProperty("targetPage");
            if (targetPage != null) instanceData.addProperty("targetPage", targetPage);

            String instanceGroupName = (String) instance.getClientProperty("instanceGroupName");
            if (instanceGroupName != null) instanceData.addProperty("instanceGroupName", instanceGroupName);

            // 保存实例组指令列表
            Object instanceGroupCommands = instance.getClientProperty("instanceGroupCommands");
            if (instanceGroupCommands instanceof java.util.List) {
                com.google.gson.JsonArray groupCommandsArray = new com.google.gson.JsonArray();
                @SuppressWarnings("unchecked")
                java.util.List<String> groupCommandList = (java.util.List<String>) instanceGroupCommands;
                for (String cmd : groupCommandList) {
                    groupCommandsArray.add(cmd);
                }
                instanceData.add("instanceGroupCommands", groupCommandsArray);
                System.out.println("保存实例组指令列表，共 " + groupCommandList.size() + " 条指令");
            }

            // 保存实例组延时设置
            Object instanceGroupDelays = instance.getClientProperty("instanceGroupDelays");
            if (instanceGroupDelays instanceof java.util.Map) {
                JsonObject delaysObj = new JsonObject();
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> delayMap = (java.util.Map<String, String>) instanceGroupDelays;
                for (java.util.Map.Entry<String, String> entry : delayMap.entrySet()) {
                    delaysObj.addProperty(entry.getKey(), entry.getValue());
                }
                instanceData.add("instanceGroupDelays", delaysObj);
                System.out.println("保存实例组延时设置，共 " + delayMap.size() + " 条延时");
            }

            // 保存接口配置
            String interfaceConfig = (String) instance.getClientProperty("interfaceConfig");
            if (interfaceConfig != null && !interfaceConfig.isEmpty()) {
                instanceData.addProperty("interfaceConfig", interfaceConfig);
                System.out.println("保存接口配置: " + interfaceConfig);
            }

            // 保存指令列表（完整的指令数据）
            Object commands = instance.getClientProperty("commands");
            if (commands instanceof java.util.List) {
                com.google.gson.JsonArray commandsArray = new com.google.gson.JsonArray();
                @SuppressWarnings("unchecked")
                java.util.List<String> commandList = (java.util.List<String>) commands;
                for (String cmd : commandList) {
                    // 检查是否是新格式的完整指令数据
                    String[] parts = cmd.split("\\|");
                    if (parts.length >= 5) {
                        // 新格式：保存完整的指令对象（包含延时）
                        JsonObject commandObj = new JsonObject();
                        commandObj.addProperty("name", parts[0]);
                        commandObj.addProperty("protocol", parts[1]);
                        commandObj.addProperty("ip", parts[2]);
                        commandObj.addProperty("port", parts[3]);
                        commandObj.addProperty("command", parts[4]);
                        // 保存延时字段
                        if (parts.length >= 6) {
                            commandObj.addProperty("delay", parts[5]);
                        } else {
                            commandObj.addProperty("delay", "0");
                        }
                        commandsArray.add(commandObj);
                    } else {
                        // 旧格式：只有指令名称，创建默认的指令对象
                        JsonObject commandObj = new JsonObject();
                        commandObj.addProperty("name", cmd);
                        commandObj.addProperty("protocol", "TCP");
                        commandObj.addProperty("ip", "192.168.1.100");
                        commandObj.addProperty("port", "8080");
                        commandObj.addProperty("command", "");
                        commandObj.addProperty("delay", "0");
                        commandsArray.add(commandObj);
                    }
                }
                instanceData.add("commands", commandsArray);
            }

            // 保存标签信息
            Component[] components = instance.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    JsonObject labelData = new JsonObject();

                    // 保存文本
                    if (label.getText() != null) {
                        labelData.addProperty("text", label.getText());
                    }

                    // 保存字体信息
                    Font font = label.getFont();
                    if (font != null) {
                        labelData.addProperty("fontName", font.getName());
                        labelData.addProperty("fontSize", font.getSize());
                        labelData.addProperty("fontStyle", font.getStyle());
                    }

                    // 保存颜色
                    Color color = label.getForeground();
                    if (color != null) {
                        labelData.addProperty("colorRGB", color.getRGB());
                    }

                    // 保存图标路径
                    ImageIcon originalIcon = (ImageIcon) label.getClientProperty("originalIcon");
                    if (originalIcon != null && originalIcon.getDescription() != null) {
                        String stored = toRelativeIfUnderUserData(originalIcon.getDescription());
                        labelData.addProperty("iconPath", stored);
                    }

                    // 保存原始字体大小
                    Integer originalFontSize = (Integer) label.getClientProperty("originalFontSize");
                    if (originalFontSize != null) {
                        labelData.addProperty("originalFontSize", originalFontSize);
                    }

                    // 保存对齐方式
                    labelData.addProperty("horizontalAlignment", label.getHorizontalAlignment());
                    labelData.addProperty("verticalAlignment", label.getVerticalAlignment());
                    labelData.addProperty("horizontalTextPosition", label.getHorizontalTextPosition());
                    labelData.addProperty("verticalTextPosition", label.getVerticalTextPosition());

                    instanceData.add("labelData", labelData);
                    break; // 目前只处理第一个标签
                }
            }

            return instanceData;

        } catch (Exception e) {
            System.out.println("保存实例数据失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 统一的界面加载入口
     * 根据当前模式（主界面/编辑界面）加载相应的内容
     */
    private void loadInterface() {
        System.out.println("=== loadInterface 开始 ===");
        System.out.println("当前模式: " + (isEditMode ? "编辑模式" : "主界面模式"));

        try {
            // 首先加载项目数据
            loadProjectData();

            // 根据当前模式加载相应界面
            if (isEditMode) {
                // 编辑模式：加载到编辑画布
                loadCurrentPageToEditCanvas();

                // 确保页面列表选中状态正确
                if (currentPageName != null) {
                    SwingUtilities.invokeLater(() -> {
                        syncPageListSelection(currentPageName);
                    });
                }

                System.out.println("编辑界面加载完成");
            } else {
                // 主界面模式：显示在主界面
                setupMainInterfaceWindow();
                System.out.println("主界面加载完成");
            }

        } catch (Exception e) {
            System.out.println("界面加载失败: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== loadInterface 结束 ===");
    }

    /**
     * 加载项目数据
     */
    private void loadProjectData() {
        System.out.println("=== loadProjectData 开始 ===");
        try {
            File userDataDir = getUserDataDir();
            System.out.println("USERDATA目录: " + userDataDir.getAbsolutePath());
            if (!userDataDir.exists()) {
                System.out.println("USERDATA目录不存在，跳过加载");
                return;
            }

            // 查找项目数据文件
            File projectFile = new File(userDataDir, "project_data.json");
            System.out.println("项目文件路径: " + projectFile.getAbsolutePath());
            System.out.println("项目文件存在: " + projectFile.exists());
            System.out.println("项目文件可读: " + projectFile.canRead());
            if (projectFile.exists()) {
                System.out.println("项目文件大小: " + projectFile.length() + " 字节");
            }

            if (!projectFile.exists()) {
                System.out.println("没有找到项目文件，创建默认页面");
                createDefaultPage();
                return;
            }

            System.out.println("加载项目文件: " + projectFile.getAbsolutePath());

            // 读取JSON文件（使用UTF-8编码）
            java.io.InputStreamReader reader = new java.io.InputStreamReader(
                new java.io.FileInputStream(projectFile),
                java.nio.charset.StandardCharsets.UTF_8
            );
            Gson gson = new Gson();
            JsonObject projectData = gson.fromJson(reader, JsonObject.class);
            reader.close();

            // 加载页面列表
            if (projectData.has("pages")) {
                if (pageListModel != null) {
                    pageListModel.clear();
                    com.google.gson.JsonArray pagesArray = projectData.getAsJsonArray("pages");
                    for (int i = 0; i < pagesArray.size(); i++) {
                        String pageName = pagesArray.get(i).getAsString();
                        pageListModel.addElement(pageName);
                    }
                    System.out.println("已加载页面列表，页面数量: " + pageListModel.getSize());
                } else {
                    System.out.println("pageListModel为null，初始化pageListModel");
                    // 如果pageListModel为null，先初始化它
                    pageListModel = new javax.swing.DefaultListModel<>();
                    com.google.gson.JsonArray pagesArray = projectData.getAsJsonArray("pages");
                    for (int i = 0; i < pagesArray.size(); i++) {
                        String pageName = pagesArray.get(i).getAsString();
                        pageListModel.addElement(pageName);
                    }
                    System.out.println("已初始化并加载页面列表，页面数量: " + pageListModel.getSize());
                }
            }

            // 加载当前页面
            if (projectData.has("currentPage")) {
                currentPageName = projectData.get("currentPage").getAsString();
                System.out.println("当前页面: " + currentPageName);
            }

            // 加载编辑分辨率
            currentEditResolution = "1024x768"; // 默认分辨率
            if (projectData.has("editResolution")) {
                currentEditResolution = projectData.get("editResolution").getAsString();
                System.out.println("编辑分辨率: " + currentEditResolution);
            } else {
                System.out.println("使用默认编辑分辨率: " + currentEditResolution);
            }

            // 加载页面内容
            System.out.println("开始加载页面内容...");
            if (projectData.has("pageContents")) {
                pageContents.clear();
                JsonObject pageContentsJson = projectData.getAsJsonObject("pageContents");

                if (pageContentsJson != null) {
                    System.out.println("JSON中的页面数量: " + pageContentsJson.keySet().size());

                    // 按照页面列表的顺序加载页面内容，确保顺序一致
                    if (pageListModel != null) {
                        for (int i = 0; i < pageListModel.getSize(); i++) {
                            String pageName = pageListModel.getElementAt(i);
                            if (pageContentsJson.has(pageName)) {
                                System.out.println("加载页面: " + pageName + " (顺序: " + i + ")");
                                com.google.gson.JsonArray pageComponentsArray = pageContentsJson.getAsJsonArray(pageName);
                                System.out.println("页面 " + pageName + " 的组件数量: " + pageComponentsArray.size());
                                java.util.List<Component> components = new java.util.ArrayList<>();

                                for (int j = 0; j < pageComponentsArray.size(); j++) {
                                    JsonObject instanceData = pageComponentsArray.get(j).getAsJsonObject();
                                    System.out.println("加载组件 " + j + ": " + instanceData);
                                    JPanel instance = loadInstanceFromData(instanceData);
                                    if (instance != null) {
                                        components.add(instance);
                                        System.out.println("成功创建组件 " + j + ", bounds: " + instance.getBounds());
                                    } else {
                                        System.out.println("创建组件 " + j + " 失败");
                                    }
                                }

                                pageContents.put(pageName, components);
                                System.out.println("已加载页面内容: " + pageName + ", 组件数量: " + components.size());
                            } else {
                                // 如果JSON中没有该页面的内容，创建空列表
                                pageContents.put(pageName, new java.util.ArrayList<>());
                                System.out.println("页面 " + pageName + " 在JSON中无内容，创建空列表");
                            }
                        }
                    }
                    System.out.println("页面内容加载完成，总页面数: " + pageContents.size());
                } else {
                    System.out.println("pageContentsJson为null");
                }
            } else {
                System.out.println("JSON中没有pageContents字段");
            }

            // 加载页面背景信息
            System.out.println("开始加载页面背景信息...");
            if (projectData.has("pageBackgrounds")) {
                pageBackgrounds.clear();
                JsonObject pageBackgroundsJson = projectData.getAsJsonObject("pageBackgrounds");

                if (pageBackgroundsJson != null) {
                    System.out.println("JSON中的背景页面数量: " + pageBackgroundsJson.keySet().size());

                    for (String pageName : pageBackgroundsJson.keySet()) {
                        String backgroundPath = pageBackgroundsJson.get(pageName).getAsString();
                        pageBackgrounds.put(pageName, backgroundPath);
                        System.out.println("加载页面背景: " + pageName + " -> " + backgroundPath);
                    }
                    System.out.println("页面背景信息加载完成，总背景数: " + pageBackgrounds.size());
                } else {
                    System.out.println("pageBackgroundsJson为null");
                }
            } else {
                System.out.println("JSON中没有pageBackgrounds字段");
            }

            // 加载定时器数据
            System.out.println("开始加载定时器数据...");
            if (projectData.has("timerTasks")) {
                timerTasks.clear();
                if (timerListModel != null) {
                    timerListModel.clear();
                }

                com.google.gson.JsonArray timerArray = projectData.getAsJsonArray("timerTasks");
                if (timerArray != null) {
                    System.out.println("JSON中的定时任务数量: " + timerArray.size());

                    for (int i = 0; i < timerArray.size(); i++) {
                        JsonObject taskJson = timerArray.get(i).getAsJsonObject();

                        String title = taskJson.has("title") ? taskJson.get("title").getAsString() : "";
                        String executeInterface = taskJson.has("executeInterface") ? taskJson.get("executeInterface").getAsString() : "";
                        String time = taskJson.has("time") ? taskJson.get("time").getAsString() : "08:00:00";

                        // 加载重复设置
                        boolean[] repeatDays = new boolean[7];
                        if (taskJson.has("repeatDays")) {
                            com.google.gson.JsonArray repeatArray = taskJson.getAsJsonArray("repeatDays");
                            for (int j = 0; j < Math.min(7, repeatArray.size()); j++) {
                                repeatDays[j] = repeatArray.get(j).getAsBoolean();
                            }
                        }

                        ScheduledTask task = new ScheduledTask(title, executeInterface, time, repeatDays);
                        timerTasks.add(task);

                        if (timerListModel != null) {
                            timerListModel.addElement(task);
                        }

                        System.out.println("加载定时任务: " + title + " - " + time + " - " + executeInterface);
                    }
                    System.out.println("定时器数据加载完成，总任务数: " + timerTasks.size());
                } else {
                    System.out.println("timerArray为null");
                }
            } else {
                System.out.println("JSON中没有timerTasks字段");
            }

            // 刷新目标页面下拉框，排除当前页面
            refreshTargetPageComboBox(currentPageName);

            // 同步页面列表的选中状态
            if (currentPageName != null) {
                syncPageListSelection(currentPageName);
            }

            // 加载完成后刷新主界面显示（仅在非编辑模式下）
            if (!isEditMode) {
                if (currentPageName != null && pageContents.containsKey(currentPageName)) {
                    displayPageInMain(currentPageName);
                } else if (pageListModel.getSize() > 0) {
                    displayFirstPageInMain();
                }
            }

            System.out.println("项目数据加载完成");

        } catch (Exception e) {
            System.out.println("加载项目数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从JSON数据加载实例 (支持相对定位)
     */
    private JPanel loadInstanceFromData(JsonObject instanceData) {
        try {
            // 创建实例容器
            JPanel instance = new JPanel();
            instance.setLayout(null);
            instance.setOpaque(false);
            instance.setVisible(true);
            instance.setEnabled(true);

            // 声明变量
            com.feixiang.tabletcontrol.model.ComponentData.PositionMode positionMode;
            com.feixiang.tabletcontrol.model.RelativePosition relativePos = null;

            // 优先使用新的编辑画布坐标格式
            if (instanceData.has("editCanvasX") && instanceData.has("editCanvasY") &&
                instanceData.has("editCanvasWidth") && instanceData.has("editCanvasHeight")) {

                // 新格式：基于编辑画布坐标创建相对位置，然后统一使用百分比转换
                int editCanvasX = instanceData.get("editCanvasX").getAsInt();
                int editCanvasY = instanceData.get("editCanvasY").getAsInt();
                int editCanvasWidth = instanceData.get("editCanvasWidth").getAsInt();
                int editCanvasHeight = instanceData.get("editCanvasHeight").getAsInt();

                // 获取保存时的编辑分辨率
                String savedEditResolution = instanceData.has("editResolution") ?
                    instanceData.get("editResolution").getAsString() : "1024x768";

                // 第一步：将编辑画布坐标转换为基于编辑分辨率的相对位置（百分比）
                int[] savedRes = parseResolution(savedEditResolution);
                if (savedRes != null) {
                    int savedEditWidth = savedRes[0];
                    int savedEditHeight = savedRes[1];

                    // 先将编辑画布坐标转换为编辑分辨率坐标
                    int savedCanvasDisplayWidth = instanceData.has("canvasDisplayWidth") ?
                        instanceData.get("canvasDisplayWidth").getAsInt() : 600;
                    int savedCanvasDisplayHeight = instanceData.has("canvasDisplayHeight") ?
                        instanceData.get("canvasDisplayHeight").getAsInt() : 400;

                    double canvasToEditScaleX = (double) savedEditWidth / savedCanvasDisplayWidth;
                    double canvasToEditScaleY = (double) savedEditHeight / savedCanvasDisplayHeight;

                    int editResolutionX = (int) Math.round(editCanvasX * canvasToEditScaleX);
                    int editResolutionY = (int) Math.round(editCanvasY * canvasToEditScaleY);
                    int editResolutionWidth = (int) Math.round(editCanvasWidth * canvasToEditScaleX);
                    int editResolutionHeight = (int) Math.round(editCanvasHeight * canvasToEditScaleY);

                    // 基于编辑分辨率创建相对位置（双锚点百分比）
                    relativePos = com.feixiang.tabletcontrol.model.RelativePosition.fromAbsolute(
                        editResolutionX, editResolutionY, editResolutionWidth, editResolutionHeight,
                        savedEditWidth, savedEditHeight);

                    System.out.println("编辑画布坐标转换: 画布坐标(" + editCanvasX + "," + editCanvasY + "," + editCanvasWidth + "," + editCanvasHeight +
                                     ") -> 编辑分辨率坐标(" + editResolutionX + "," + editResolutionY + "," + editResolutionWidth + "," + editResolutionHeight +
                                     ") -> 相对位置: " + relativePos);
                } else {
                    System.out.println("无法解析编辑分辨率，跳过坐标转换");
                    return null;
                }

                // 保存原始数据到客户端属性（用于保存时的向后兼容）
                instance.putClientProperty("editCanvasX", editCanvasX);
                instance.putClientProperty("editCanvasY", editCanvasY);
                instance.putClientProperty("editCanvasWidth", editCanvasWidth);
                instance.putClientProperty("editCanvasHeight", editCanvasHeight);
                instance.putClientProperty("editResolution", savedEditResolution);
                if (instanceData.has("canvasDisplayWidth")) {
                    instance.putClientProperty("canvasDisplayWidth", instanceData.get("canvasDisplayWidth").getAsInt());
                }
                if (instanceData.has("canvasDisplayHeight")) {
                    instance.putClientProperty("canvasDisplayHeight", instanceData.get("canvasDisplayHeight").getAsInt());
                }

                // 设置位置模式为相对定位
                positionMode = com.feixiang.tabletcontrol.model.ComponentData.PositionMode.RELATIVE;

            } else if (instanceData.has("relativePosition")) {
                // 向后兼容：使用相对定位
                // 加载相对位置数据
                JsonObject relativeData = instanceData.getAsJsonObject("relativePosition");

                // 优先使用双锚点数据
                if (relativeData.has("topLeftX") && relativeData.has("topLeftY") &&
                    relativeData.has("bottomRightX") && relativeData.has("bottomRightY")) {
                    // 加载双锚点数据
                    relativePos = com.feixiang.tabletcontrol.model.RelativePosition.createDualAnchor(
                        relativeData.get("topLeftX").getAsDouble(),
                        relativeData.get("topLeftY").getAsDouble(),
                        relativeData.get("bottomRightX").getAsDouble(),
                        relativeData.get("bottomRightY").getAsDouble()
                    );
                    System.out.println("加载双锚点位置数据: " + relativePos);
                } else {
                    // 向后兼容：使用单锚点数据
                    relativePos = new com.feixiang.tabletcontrol.model.RelativePosition(
                        relativeData.get("xPercent").getAsDouble(),
                        relativeData.get("yPercent").getAsDouble(),
                        relativeData.get("widthPercent").getAsDouble(),
                        relativeData.get("heightPercent").getAsDouble()
                    );

                    if (relativeData.has("anchorType")) {
                        try {
                            relativePos.setAnchorType(com.feixiang.tabletcontrol.model.RelativePosition.AnchorType.valueOf(
                                relativeData.get("anchorType").getAsString()));
                        } catch (IllegalArgumentException e) {
                            System.out.println("无效的锚点类型，使用默认值: " + e.getMessage());
                        }
                    }
                    System.out.println("加载单锚点位置数据（向后兼容）: " + relativePos);
                }

                positionMode = com.feixiang.tabletcontrol.model.ComponentData.PositionMode.RELATIVE;

            } else if (instanceData.has("x") && instanceData.has("y")) {
                // 向后兼容：使用绝对坐标，转换为相对位置
                positionMode = com.feixiang.tabletcontrol.model.ComponentData.PositionMode.ABSOLUTE;
                System.out.println("使用绝对坐标模式 (向后兼容)");

                int x = instanceData.get("x").getAsInt();
                int y = instanceData.get("y").getAsInt();
                int width = instanceData.get("width").getAsInt();
                int height = instanceData.get("height").getAsInt();

                // 将绝对坐标转换为相对位置（假设基于1024x768分辨率）
                int baseWidth = 1024, baseHeight = 768;
                if (currentEditResolution != null) {
                    int[] res = parseResolution(currentEditResolution);
                    if (res != null) {
                        baseWidth = res[0];
                        baseHeight = res[1];
                    }
                }

                relativePos = com.feixiang.tabletcontrol.model.RelativePosition.fromAbsolute(
                    x, y, width, height, baseWidth, baseHeight);

                System.out.println("绝对坐标转换为相对位置: (" + x + "," + y + "," + width + "," + height +
                                 ") 基于分辨率(" + baseWidth + "x" + baseHeight + ") -> " + relativePos);

            } else {
                System.out.println("错误：组件缺少位置信息！");
                return null;
            }

            // 统一的坐标转换逻辑：基于相对位置和当前画布尺寸计算最终坐标
            if (relativePos != null) {
                // 获取当前画布尺寸
                int currentCanvasWidth = 600;  // 默认值
                int currentCanvasHeight = 400; // 默认值

                if (editCanvas != null && editCanvas.getWidth() > 0 && editCanvas.getHeight() > 0) {
                    currentCanvasWidth = editCanvas.getWidth();
                    currentCanvasHeight = editCanvas.getHeight();
                    System.out.println("使用实际画布尺寸: " + currentCanvasWidth + "x" + currentCanvasHeight);
                } else {
                    // 如果画布还没有初始化，根据编辑分辨率计算默认画布尺寸
                    int editWidth = 1024, editHeight = 768;
                    if (currentEditResolution != null) {
                        int[] res = parseResolution(currentEditResolution);
                        if (res != null) {
                            editWidth = res[0];
                            editHeight = res[1];
                        }
                    }

                    // 计算缩放后的画布尺寸（最大600x400）
                    double scale = Math.min(600.0 / editWidth, 400.0 / editHeight);
                    currentCanvasWidth = (int) (editWidth * scale);
                    currentCanvasHeight = (int) (editHeight * scale);

                    System.out.println("画布未初始化，计算默认尺寸: 编辑分辨率" + editWidth + "x" + editHeight +
                                     " -> 画布尺寸" + currentCanvasWidth + "x" + currentCanvasHeight);
                }

                // 使用相对位置转换为当前画布的绝对坐标
                com.feixiang.tabletcontrol.model.RelativePosition.AbsolutePosition absPos =
                    relativePos.toAbsolute(currentCanvasWidth, currentCanvasHeight);
                instance.setBounds(absPos.x, absPos.y, absPos.width, absPos.height);

                System.out.println("统一坐标转换: " + relativePos +
                                 " -> 画布(" + currentCanvasWidth + "x" + currentCanvasHeight +
                                 ") -> 最终坐标(" + absPos.x + "," + absPos.y + "," + absPos.width + "," + absPos.height + ")");

                // 保存相对位置数据到客户端属性
                instance.putClientProperty("relativePosition", relativePos);
                instance.putClientProperty("positionMode", com.feixiang.tabletcontrol.model.ComponentData.PositionMode.RELATIVE);

                // 设置基础尺寸百分比
                double baseWidthPercent = relativePos.getWidthPercent();
                double baseHeightPercent = relativePos.getHeightPercent();
                instance.putClientProperty("baseWidthPercent", baseWidthPercent);
                instance.putClientProperty("baseHeightPercent", baseHeightPercent);
                System.out.println("设置基础尺寸百分比: " + String.format("%.2f%%x%.2f%%", baseWidthPercent*100, baseHeightPercent*100));
            }

            // 注意：不再处理originalWidth/originalHeight，完全使用百分比定位

            // 设置功能配置
            if (instanceData.has("functionType")) {
                instance.putClientProperty("functionType", instanceData.get("functionType").getAsString());
            }
            if (instanceData.has("targetPage")) {
                instance.putClientProperty("targetPage", instanceData.get("targetPage").getAsString());
            }
            if (instanceData.has("instanceGroupName")) {
                instance.putClientProperty("instanceGroupName", instanceData.get("instanceGroupName").getAsString());
            }

            // 加载指令列表
            if (instanceData.has("commands")) {
                com.google.gson.JsonArray commandsArray = instanceData.getAsJsonArray("commands");
                java.util.List<String> commands = new java.util.ArrayList<>();
                for (int i = 0; i < commandsArray.size(); i++) {
                    com.google.gson.JsonElement element = commandsArray.get(i);
                    if (element.isJsonObject()) {
                        // 新格式：完整的指令对象（包含延时）
                        JsonObject commandObj = element.getAsJsonObject();
                        String name = commandObj.has("name") ? commandObj.get("name").getAsString() : "";
                        String protocol = commandObj.has("protocol") ? commandObj.get("protocol").getAsString() : "TCP";
                        String ip = commandObj.has("ip") ? commandObj.get("ip").getAsString() : "192.168.1.100";
                        String port = commandObj.has("port") ? commandObj.get("port").getAsString() : "8080";
                        String command = commandObj.has("command") ? commandObj.get("command").getAsString() : "";
                        String delay = commandObj.has("delay") ? commandObj.get("delay").getAsString() : "0";

                        // 组合成完整的指令数据字符串（包含延时）
                        String commandData = String.format("%s|%s|%s|%s|%s|%s", name, protocol, ip, port, command, delay);
                        commands.add(commandData);
                        System.out.println("加载指令: " + name + ", 延时: " + delay + "秒");
                    } else {
                        // 旧格式：只有指令名称
                        String commandName = element.getAsString();
                        // 转换为新格式，使用默认值（包含延时）
                        String commandData = String.format("%s|TCP|192.168.1.100|8080||0", commandName);
                        commands.add(commandData);
                        System.out.println("加载旧格式指令: " + commandName + ", 使用默认延时: 0秒");
                    }
                }
                instance.putClientProperty("commands", commands);
            }

            // 加载实例组指令列表
            if (instanceData.has("instanceGroupCommands")) {
                com.google.gson.JsonArray groupCommandsArray = instanceData.getAsJsonArray("instanceGroupCommands");
                java.util.List<String> groupCommands = new java.util.ArrayList<>();
                for (int i = 0; i < groupCommandsArray.size(); i++) {
                    String cmd = groupCommandsArray.get(i).getAsString();
                    groupCommands.add(cmd);
                }
                instance.putClientProperty("instanceGroupCommands", groupCommands);
                System.out.println("加载实例组指令列表，共 " + groupCommands.size() + " 条指令");
            }

            // 加载实例组延时设置
            if (instanceData.has("instanceGroupDelays")) {
                JsonObject delaysObj = instanceData.getAsJsonObject("instanceGroupDelays");
                java.util.Map<String, String> delayMap = new java.util.HashMap<>();
                for (String key : delaysObj.keySet()) {
                    String value = delaysObj.get(key).getAsString();
                    delayMap.put(key, value);
                }
                instance.putClientProperty("instanceGroupDelays", delayMap);
                System.out.println("加载实例组延时设置，共 " + delayMap.size() + " 条延时");
            }

            // 加载接口配置
            if (instanceData.has("interfaceConfig")) {
                String interfaceConfig = instanceData.get("interfaceConfig").getAsString();
                instance.putClientProperty("interfaceConfig", interfaceConfig);
                System.out.println("加载接口配置: " + interfaceConfig);
            }

            // 加载标签
            if (instanceData.has("labelData")) {
                JsonObject labelData = instanceData.getAsJsonObject("labelData");
                JLabel label = new JLabel();

                // 设置文本
                if (labelData.has("text")) {
                    label.setText(labelData.get("text").getAsString());
                }

                // 设置字体
                if (labelData.has("fontName") && labelData.has("fontSize") && labelData.has("fontStyle")) {
                    String fontName = labelData.get("fontName").getAsString();
                    int fontSize = labelData.get("fontSize").getAsInt();
                    int fontStyle = labelData.get("fontStyle").getAsInt();
                    label.setFont(new Font(fontName, fontStyle, fontSize));
                }

                // 设置颜色
                if (labelData.has("colorRGB")) {
                    int colorRGB = labelData.get("colorRGB").getAsInt();
                    Color textColor = new Color(colorRGB);
                    label.setForeground(textColor);
                    System.out.println("设置文本颜色: " + colorRGB + " -> " + textColor);


                }

                // 加载图标
                if (labelData.has("iconPath")) {
                    String iconPath = labelData.get("iconPath").getAsString();
                    System.out.println("尝试加载图标: " + iconPath);

                    // 尝试多个可能的路径
                    File iconFile = new File(iconPath);
                    if (!iconFile.exists()) {
                        // 尝试USERDATA目录下的路径
                        iconFile = new File(getUserDataDir(), iconPath);
                        System.out.println("尝试USERDATA路径: " + iconFile.getAbsolutePath());
                    }
                    if (!iconFile.exists()) {
                        // 尝试相对于工作目录的路径
                        iconFile = new File("USERDATA/" + iconPath);
                        System.out.println("尝试工作目录路径: " + iconFile.getAbsolutePath());
                    }

                    if (iconFile.exists()) {
                        try {
                            ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                            label.setIcon(icon);
                            System.out.println("成功加载图标: " + iconFile.getAbsolutePath());

                            // 保存原始图标
                            ImageIcon originalIcon = new ImageIcon(iconFile.getAbsolutePath());
                            originalIcon.setDescription(iconFile.getAbsolutePath());
                            label.putClientProperty("originalIcon", originalIcon);
                        } catch (Exception e) {
                            System.out.println("加载图标失败: " + e.getMessage());
                        }
                    } else {
                        System.out.println("图标文件不存在: " + iconPath);
                    }
                }

                // 设置原始字体大小
                if (labelData.has("originalFontSize")) {
                    label.putClientProperty("originalFontSize", labelData.get("originalFontSize").getAsInt());
                }

                // 设置对齐方式
                if (labelData.has("horizontalAlignment")) {
                    label.setHorizontalAlignment(labelData.get("horizontalAlignment").getAsInt());
                }
                if (labelData.has("verticalAlignment")) {
                    label.setVerticalAlignment(labelData.get("verticalAlignment").getAsInt());
                }
                if (labelData.has("horizontalTextPosition")) {
                    label.setHorizontalTextPosition(labelData.get("horizontalTextPosition").getAsInt());
                }
                if (labelData.has("verticalTextPosition")) {
                    label.setVerticalTextPosition(labelData.get("verticalTextPosition").getAsInt());
                }

                // 设置标签属性
                label.setBounds(0, 0, instance.getWidth(), instance.getHeight());
                label.setEnabled(true);
                label.setFocusable(false);

                instance.add(label);

                // 将labelData保存到实例属性中，供开关按钮检测使用
                java.util.Map<String, Object> labelDataMap = new java.util.HashMap<>();
                if (labelData.has("iconPath")) {
                    labelDataMap.put("iconPath", labelData.get("iconPath").getAsString());
                }
                if (labelData.has("text")) {
                    labelDataMap.put("text", labelData.get("text").getAsString());
                }
                instance.putClientProperty("labelData", labelDataMap);
            }

            // 为加载的实例添加主界面事件监听器
            addMainInterfaceListeners(instance);

            System.out.println("成功创建实例: bounds=" + instance.getBounds() +
                             ", visible=" + instance.isVisible() +
                             ", enabled=" + instance.isEnabled() +
                             ", 子组件数量=" + instance.getComponentCount());

            return instance;

        } catch (Exception e) {
            System.out.println("加载实例失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 创建默认页面
     */
    private void createDefaultPage() {
        if (pageListModel.getSize() == 0) {
            pageListModel.addElement("页面1");
            pageContents.put("页面1", new java.util.ArrayList<>());
            currentPageName = "页面1";
            System.out.println("已创建默认页面: 页面1");

            // 同步页面列表的选中状态
            syncPageListSelection(currentPageName);
            updateCurrentPageLabel(currentPageName);
        }
    }



    /**
     * 自定义的JList，阻止点击空白区域时的选中行为
     */
    private class CustomJList<E> extends JList<E> {
        private boolean allowSelectionChange = true;
        private javax.swing.Timer restoreTimer;

        public CustomJList(ListModel<E> dataModel) {
            super(dataModel);
            // 使用自定义的选中模型来完全控制选中行为
            setSelectionModel(new CustomListSelectionModel());
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            // 在任何处理之前，先检查是否应该阻止选中变化
            if (e.getID() == MouseEvent.MOUSE_PRESSED && !isClickOnValidItem(e)) {
                System.out.println("MOUSE_PRESSED在空白区域，立即阻止选中变化");
                allowSelectionChange = false;

                // 停止之前的定时器
                if (restoreTimer != null && restoreTimer.isRunning()) {
                    restoreTimer.stop();
                }

                // 设置新的恢复定时器
                restoreTimer = new javax.swing.Timer(300, evt -> {
                    allowSelectionChange = true;
                    System.out.println("定时器恢复allowSelectionChange=true");
                });
                restoreTimer.setRepeats(false);
                restoreTimer.start();
            }

            // 检查是否点击在有效的列表项上
            if (!isClickOnValidItem(e)) {
                // 点击在空白区域，阻止选中变化并消费事件
                System.out.println("阻止空白区域鼠标事件: " + getMouseEventName(e.getID()) +
                                   (e.getClickCount() > 1 ? "(双击)" : "") +
                                   " - 当前allowSelectionChange=" + allowSelectionChange);
                e.consume();
                return;
            }

            // 点击在有效项上，允许选中变化
            allowSelectionChange = true;
            super.processMouseEvent(e);
        }

        private class CustomListSelectionModel extends DefaultListSelectionModel {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (allowSelectionChange && index0 >= 0 && index0 < getModel().getSize()
                    && index1 >= 0 && index1 < getModel().getSize()) {
                    System.out.println("CustomListSelectionModel允许选中变化: " + index0 + " -> " + index1);
                    super.setSelectionInterval(index0, index1);
                } else {
                    System.out.println("CustomListSelectionModel阻止选中变化: " + index0 + " -> " + index1 +
                                     " (allowSelectionChange=" + allowSelectionChange + ")");
                }
                // 其他情况忽略，不改变选中状态
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                if (allowSelectionChange && index0 >= 0 && index0 < getModel().getSize()
                    && index1 >= 0 && index1 < getModel().getSize()) {
                    System.out.println("CustomListSelectionModel允许添加选中: " + index0 + " -> " + index1);
                    super.addSelectionInterval(index0, index1);
                } else {
                    System.out.println("CustomListSelectionModel阻止添加选中: " + index0 + " -> " + index1 +
                                     " (allowSelectionChange=" + allowSelectionChange + ")");
                }
                // 其他情况忽略
            }

            @Override
            public void removeSelectionInterval(int index0, int index1) {
                if (allowSelectionChange) {
                    System.out.println("CustomListSelectionModel允许移除选中: " + index0 + " -> " + index1);
                    super.removeSelectionInterval(index0, index1);
                } else {
                    System.out.println("CustomListSelectionModel阻止移除选中: " + index0 + " -> " + index1);
                }
            }

            @Override
            public void clearSelection() {
                if (allowSelectionChange) {
                    System.out.println("CustomListSelectionModel允许清除选中");
                    super.clearSelection();
                } else {
                    System.out.println("CustomListSelectionModel阻止清除选中");
                }
            }
        }

        // processMouseEvent方法已经整合到上面的版本中

        // 这些方法现在由CustomListSelectionModel处理，不需要在这里重写

        @Override
        public void setSelectedIndex(int index) {
            // 只有在有效范围内才允许设置选中
            if (index >= 0 && index < getModel().getSize()) {
                super.setSelectedIndex(index);
            } else if (index == -1) {
                // 允许清除选中状态
                super.setSelectedIndex(index);
            }
            // 其他情况忽略
        }

        @Override
        public int locationToIndex(Point location) {
            int index = super.locationToIndex(location);
            if (index >= 0 && index < getModel().getSize()) {
                Rectangle cellBounds = getCellBounds(index, index);
                if (cellBounds != null && !cellBounds.contains(location)) {
                    // 点击在最后一项下方或空白位置，返回 -1，避免误选最后一项
                    return -1;
                }
            }
            return index;
        }

        private boolean isClickOnValidItem(MouseEvent e) {
            int clickedIndex = super.locationToIndex(e.getPoint());
            if (clickedIndex >= 0 && clickedIndex < getModel().getSize()) {
                Rectangle cellBounds = getCellBounds(clickedIndex, clickedIndex);
                return cellBounds != null && cellBounds.contains(e.getPoint());
            }
            return false;
        }

        private String getMouseEventName(int eventId) {
            switch (eventId) {
                case MouseEvent.MOUSE_PRESSED: return "MOUSE_PRESSED";
                case MouseEvent.MOUSE_RELEASED: return "MOUSE_RELEASED";
                case MouseEvent.MOUSE_CLICKED: return "MOUSE_CLICKED";
                default: return "UNKNOWN(" + eventId + ")";
            }
        }
    }

    /**
     * 支持拖拽重排序的JList
     */
    private class DraggableJList<E> extends JList<E> {
        private int dragSourceIndex = -1;
        private int dropTargetIndex = -1;
        public boolean isDragging = false;

        public DraggableJList(ListModel<E> dataModel) {
            super(dataModel);
            setDragEnabled(true);
            setDropMode(DropMode.INSERT);

            // 添加鼠标监听器处理拖拽开始
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        int index = locationToIndex(e.getPoint());
                        if (index >= 0) {
                            dragSourceIndex = index;
                            setSelectedIndex(index);
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        isDragging = false;
                        dragSourceIndex = -1;
                        dropTargetIndex = -1;
                        repaint();
                    }
                }
            });

            // 添加鼠标移动监听器处理拖拽过程
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragSourceIndex >= 0) {
                        isDragging = true;
                        int currentIndex = locationToIndex(e.getPoint());
                        if (currentIndex >= 0 && currentIndex != dropTargetIndex) {
                            dropTargetIndex = currentIndex;
                            repaint();
                        }
                    }
                }
            });

            // 设置传输处理器
            setTransferHandler(new ListItemTransferHandler());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 绘制拖拽目标位置的视觉反馈
            if (isDragging && dropTargetIndex >= 0) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 123, 255, 100));
                g2d.setStroke(new BasicStroke(2));

                Rectangle bounds = getCellBounds(dropTargetIndex, dropTargetIndex);
                if (bounds != null) {
                    // 绘制插入位置的指示线
                    int y = bounds.y;
                    if (dropTargetIndex > dragSourceIndex) {
                        y = bounds.y + bounds.height;
                    }
                    g2d.drawLine(bounds.x, y, bounds.x + bounds.width, y);
                }
                g2d.dispose();
            }
        }
    }

    /**
     * 列表项传输处理器，支持拖拽重排序
     */
    private class ListItemTransferHandler extends TransferHandler {
        private int sourceIndex = -1;

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            @SuppressWarnings("unchecked")
            DraggableJList<CheckBoxItemWithDelay> list = (DraggableJList<CheckBoxItemWithDelay>) c;
            sourceIndex = list.getSelectedIndex();
            if (sourceIndex >= 0) {
                CheckBoxItemWithDelay item = list.getModel().getElementAt(sourceIndex);
                return new StringSelection(item.toString());
            }
            return null;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor) &&
                   support.getComponent() instanceof DraggableJList;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            DraggableJList<CheckBoxItemWithDelay> list = (DraggableJList<CheckBoxItemWithDelay>) support.getComponent();
            DefaultListModel<CheckBoxItemWithDelay> model = (DefaultListModel<CheckBoxItemWithDelay>) list.getModel();

            JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
            int dropIndex = dropLocation.getIndex();

            if (sourceIndex >= 0 && dropIndex >= 0 && sourceIndex != dropIndex) {
                // 执行重排序
                CheckBoxItemWithDelay item = model.getElementAt(sourceIndex);
                model.removeElementAt(sourceIndex);

                // 调整插入位置
                if (dropIndex > sourceIndex) {
                    dropIndex--;
                }

                model.insertElementAt(item, dropIndex);
                list.setSelectedIndex(dropIndex);

                System.out.println("指令重排序: 从位置 " + sourceIndex + " 移动到位置 " + dropIndex);
                return true;
            }

            return false;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            sourceIndex = -1;
        }
    }

    /**
     * 定时器任务数据类
     */
    public static class ScheduledTask {
        private String title;
        private String executeInterface;
        private String time; // HH:MM:SS格式
        private boolean[] repeatDays; // 7个元素，对应周一到周日

        public ScheduledTask() {
            this.repeatDays = new boolean[7];
        }

        public ScheduledTask(String title, String executeInterface, String time, boolean[] repeatDays) {
            this.title = title;
            this.executeInterface = executeInterface;
            this.time = time;
            this.repeatDays = repeatDays != null ? repeatDays.clone() : new boolean[7];
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getExecuteInterface() { return executeInterface; }
        public void setExecuteInterface(String executeInterface) { this.executeInterface = executeInterface; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public boolean[] getRepeatDays() { return repeatDays; }
        public void setRepeatDays(boolean[] repeatDays) {
            this.repeatDays = repeatDays != null ? repeatDays.clone() : new boolean[7];
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(title).append(" - ").append(time).append(" - ").append(executeInterface);

            // 添加重复设置显示
            StringBuilder repeatStr = new StringBuilder();
            String[] dayNames = {"一", "二", "三", "四", "五", "六", "日"};
            for (int i = 0; i < repeatDays.length; i++) {
                if (repeatDays[i]) {
                    if (repeatStr.length() > 0) repeatStr.append(",");
                    repeatStr.append(dayNames[i]);
                }
            }
            if (repeatStr.length() > 0) {
                sb.append(" (").append(repeatStr.toString()).append(")");
            }

            return sb.toString();
        }
    }

}