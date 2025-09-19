package com.feixiang.tabletcontrol;

import com.feixiang.tabletcontrol.core.model.ComponentData;
import com.feixiang.tabletcontrol.core.model.LabelData;
import com.feixiang.tabletcontrol.core.model.PageData;
import com.feixiang.tabletcontrol.core.model.ProjectData;
import com.feixiang.tabletcontrol.core.model.RelativePosition;
import com.feixiang.tabletcontrol.core.repository.ProjectRepository;
import com.feixiang.tabletcontrol.core.repository.impl.JsonProjectRepository;
import com.feixiang.tabletcontrol.core.service.ProjectService;
import com.feixiang.tabletcontrol.core.service.impl.ProjectServiceImpl;
import com.feixiang.tabletcontrol.platform.CrossPlatformPathManager;
import com.feixiang.tabletcontrol.platform.PlatformManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 跨平台集成测试
 * 测试完整的跨平台功能，包括数据持久化、业务逻辑、平台适配等
 */
public class CrossPlatformIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformIntegrationTest.class);
    
    @TempDir
    Path tempDir;
    
    private PlatformManager platformManager;
    private CrossPlatformPathManager pathManager;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    
    @BeforeEach
    void setUp() {
        logger.info("设置跨平台集成测试环境");
        
        // 初始化平台管理器
        platformManager = new PlatformManager();
        
        // 创建测试用的路径管理器
        pathManager = new TestPathManager(tempDir.toString());
        
        // 初始化存储库和服务
        projectRepository = new JsonProjectRepository(pathManager);
        projectService = new ProjectServiceImpl(projectRepository);
        
        logger.info("测试环境设置完成，平台: {}", platformManager.getCurrentPlatform());
    }
    
    @Test
    void testCompleteProjectLifecycle() throws IOException {
        logger.info("测试完整项目生命周期");
        
        // 1. 创建新项目
        ProjectData project = projectService.createNewProject();
        assertNotNull(project);
        assertEquals("新项目", project.getName());
        assertTrue(project.getPageCount() > 0);
        
        // 2. 添加页面
        PageData page1 = projectService.createPage("测试页面1");
        assertNotNull(page1);
        assertEquals("测试页面1", page1.getName());
        
        PageData page2 = projectService.createPage("测试页面2");
        assertNotNull(page2);
        assertEquals("测试页面2", page2.getName());
        
        // 3. 添加组件到页面
        ComponentData component1 = createTestComponent("按钮1", 100, 100, 80, 30);
        projectService.addComponent("测试页面1", component1);
        
        ComponentData component2 = createTestComponent("标签1", 200, 150, 120, 25);
        projectService.addComponent("测试页面1", component2);
        
        // 4. 验证组件添加
        List<ComponentData> components = projectService.getPageComponents("测试页面1");
        assertEquals(2, components.size());
        
        // 5. 保存项目
        projectService.saveProject(project);
        assertFalse(projectService.hasUnsavedChanges());
        
        // 6. 重新加载项目
        ProjectData loadedProject = projectService.loadProject();
        assertNotNull(loadedProject);
        assertEquals(project.getName(), loadedProject.getName());
        assertEquals(project.getPageCount(), loadedProject.getPageCount());
        
        // 7. 验证数据完整性
        assertTrue(loadedProject.validateIntegrity());
        
        logger.info("完整项目生命周期测试通过");
    }
    
    @Test
    void testCrossPlatformDataCompatibility() throws IOException {
        logger.info("测试跨平台数据兼容性");
        
        // 创建包含各种数据类型的项目
        ProjectData project = projectService.createNewProject();
        project.setName("跨平台兼容性测试项目");
        project.setDescription("测试不同平台间的数据兼容性");
        
        // 创建页面
        PageData page = projectService.createPage("兼容性测试页面");
        
        // 添加不同类型的组件
        ComponentData buttonComponent = createTestComponent("跨平台按钮", 50, 50, 100, 40);
        buttonComponent.setTooltip("这是一个跨平台按钮");
        buttonComponent.setCssClass("cross-platform-button");
        
        ComponentData labelComponent = createTestComponent("跨平台标签", 200, 100, 150, 30);
        labelComponent.getLabelData().setColorFromHex("#FF5722");
        labelComponent.getLabelData().setAutoScaleFont(true);
        labelComponent.getLabelData().setFontScaleFactor(1.2);
        
        // 使用相对定位的组件
        RelativePosition relativePos = RelativePosition.fromAbsolute(300, 200, 120, 50, 800, 600);
        ComponentData relativeComponent = new ComponentData(relativePos, "相对定位组件", new LabelData());
        relativeComponent.getLabelData().setText("相对定位测试");
        
        projectService.addComponent("兼容性测试页面", buttonComponent);
        projectService.addComponent("兼容性测试页面", labelComponent);
        projectService.addComponent("兼容性测试页面", relativeComponent);
        
        // 保存项目
        projectService.saveProject(project);
        
        // 重新加载并验证
        ProjectData reloadedProject = projectService.loadProject();
        assertNotNull(reloadedProject);
        
        List<ComponentData> reloadedComponents = projectService.getPageComponents("兼容性测试页面");
        assertEquals(3, reloadedComponents.size());
        
        // 验证组件属性保持完整
        ComponentData reloadedButton = reloadedComponents.stream()
            .filter(c -> "跨平台按钮".equals(c.getLabelData().getText()))
            .findFirst()
            .orElse(null);
        assertNotNull(reloadedButton);
        assertEquals("这是一个跨平台按钮", reloadedButton.getTooltip());
        assertEquals("cross-platform-button", reloadedButton.getCssClass());
        
        // 验证相对定位组件
        ComponentData reloadedRelative = reloadedComponents.stream()
            .filter(c -> c.getPositionMode() == ComponentData.PositionMode.RELATIVE)
            .findFirst()
            .orElse(null);
        assertNotNull(reloadedRelative);
        assertNotNull(reloadedRelative.getRelativePosition());
        
        logger.info("跨平台数据兼容性测试通过");
    }
    
    @Test
    void testPlatformSpecificAdaptation() {
        logger.info("测试平台特定适配");
        
        // 测试平台检测
        assertNotNull(platformManager.getCurrentPlatform());
        logger.info("当前平台: {}", platformManager.getCurrentPlatform());
        
        // 测试移动端检测
        boolean isMobile = platformManager.isMobile();
        logger.info("是否移动平台: {}", isMobile);
        
        // 测试平台特定配置
        double minTouchSize = platformManager.getMinTouchTargetSize();
        assertTrue(minTouchSize > 0);
        logger.info("最小触控区域尺寸: {}", minTouchSize);
        
        // 测试路径管理
        assertNotNull(pathManager.getDataDirectory());
        assertNotNull(pathManager.getConfigDirectory());
        assertNotNull(pathManager.getLogDirectory());
        
        logger.info("数据目录: {}", pathManager.getDataDirectory());
        logger.info("配置目录: {}", pathManager.getConfigDirectory());
        
        // 验证目录存在
        assertTrue(pathManager.pathExists(pathManager.getDataDirectory()));
        assertTrue(pathManager.isDirectory(pathManager.getDataDirectory()));
        
        logger.info("平台特定适配测试通过");
    }
    
    @Test
    void testDataIntegrityAndRepair() throws IOException {
        logger.info("测试数据完整性和修复");
        
        // 创建项目
        ProjectData project = projectService.createNewProject();
        PageData page = projectService.createPage("完整性测试页面");
        
        // 添加正常组件
        ComponentData validComponent = createTestComponent("正常组件", 100, 100, 80, 30);
        projectService.addComponent("完整性测试页面", validComponent);
        
        // 创建有问题的组件（模拟数据损坏）
        ComponentData invalidComponent = new ComponentData();
        invalidComponent.setFunctionType(null); // 无效的功能类型
        invalidComponent.setWidth(-10); // 无效的宽度
        invalidComponent.setHeight(0); // 无效的高度
        
        page.addComponent(invalidComponent);
        
        // 验证数据完整性检查
        assertFalse(project.validateIntegrity());
        
        // 执行数据修复
        project.repairIntegrity();
        
        // 验证修复后的完整性
        assertTrue(project.validateIntegrity());
        
        // 保存和重新加载
        projectService.saveProject(project);
        ProjectData reloadedProject = projectService.loadProject();
        
        // 验证修复后的数据
        assertTrue(reloadedProject.validateIntegrity());
        
        logger.info("数据完整性和修复测试通过");
    }
    
    @Test
    void testBackupAndRestore() throws IOException {
        logger.info("测试备份和恢复功能");
        
        // 创建测试项目
        ProjectData originalProject = projectService.createNewProject();
        originalProject.setName("备份测试项目");
        
        PageData page = projectService.createPage("备份测试页面");
        ComponentData component = createTestComponent("备份测试组件", 150, 150, 100, 50);
        projectService.addComponent("备份测试页面", component);
        
        // 保存项目
        projectService.saveProject(originalProject);
        
        // 创建备份
        projectService.backupProject("测试备份");
        
        // 修改项目
        originalProject.setName("修改后的项目");
        projectService.saveProject(originalProject);
        
        // 从备份恢复
        ProjectData restoredProject = projectService.restoreProject("测试备份");
        assertNotNull(restoredProject);
        assertEquals("备份测试项目", restoredProject.getName());
        
        // 验证恢复的数据完整性
        assertTrue(restoredProject.validateIntegrity());
        
        logger.info("备份和恢复功能测试通过");
    }
    
    /**
     * 创建测试组件
     */
    private ComponentData createTestComponent(String text, int x, int y, int width, int height) {
        LabelData labelData = new LabelData();
        labelData.setText(text);
        labelData.setFontName("System");
        labelData.setFontSize(12);
        labelData.setColorRGB(0x000000);
        
        ComponentData component = new ComponentData(x, y, width, height, width, height, "测试组件", labelData);
        component.setVisible(true);
        component.setEnabled(true);
        
        return component;
    }
    
    /**
     * 测试用路径管理器
     */
    private static class TestPathManager extends CrossPlatformPathManager {
        private final String testBaseDir;
        
        public TestPathManager(String testBaseDir) {
            super(new PlatformManager());
            this.testBaseDir = testBaseDir;
        }
        
        @Override
        public String getDataDirectory() {
            return testBaseDir + "/USERDATA";
        }
        
        @Override
        public String getConfigDirectory() {
            return testBaseDir + "/config";
        }
        
        @Override
        public String getLogDirectory() {
            return testBaseDir + "/logs";
        }
        
        @Override
        public String getTempDirectory() {
            return testBaseDir + "/temp";
        }
        
        @Override
        public String getBackupDirectory() {
            return testBaseDir + "/USERDATA/backups";
        }
    }
}
