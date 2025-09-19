package com.feixiang.tabletcontrol.repository.impl;

import com.feixiang.tabletcontrol.model.ComponentData;
import com.feixiang.tabletcontrol.model.LabelData;
import com.feixiang.tabletcontrol.model.PageData;
import com.feixiang.tabletcontrol.model.ProjectData;
import com.feixiang.tabletcontrol.repository.ProjectRepository;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON格式的项目数据访问实现
 * 负责将项目数据以JSON格式持久化到文件系统
 */
public class JsonProjectRepository implements ProjectRepository {
    private static final Logger logger = LoggerFactory.getLogger(JsonProjectRepository.class);
    
    private final File dataFile;
    private final File backupDir;
    private final Gson gson;
    
    public JsonProjectRepository(String dataFilePath) {
        this.dataFile = new File(dataFilePath);
        this.backupDir = new File(dataFile.getParent(), "backups");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        
        // 确保目录存在
        ensureDirectoryExists(dataFile.getParentFile());
        ensureDirectoryExists(backupDir);
        
        logger.info("JsonProjectRepository初始化完成，数据文件路径: {}", dataFile.getAbsolutePath());
    }
    
    @Override
    public void save(ProjectData projectData) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("开始保存项目数据到文件: {}", dataFile.getAbsolutePath());
        
        try {
            // 创建备份
            if (exists()) {
                backup("auto_backup_" + System.currentTimeMillis());
            }
            
            // 转换为JSON格式
            JsonObject jsonObject = convertToJson(projectData);
            
            // 写入文件
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
                gson.toJson(jsonObject, writer);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("项目数据保存成功，耗时: {}ms，文件大小: {}字节", duration, dataFile.length());
            
        } catch (Exception e) {
            logger.error("保存项目数据失败: {}", e.getMessage(), e);
            throw new IOException("保存项目数据失败", e);
        }
    }
    
    @Override
    public ProjectData load() throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("开始从文件加载项目数据: {}", dataFile.getAbsolutePath());
        
        if (!exists()) {
            logger.warn("项目数据文件不存在: {}", dataFile.getAbsolutePath());
            return null;
        }
        
        try {
            // 读取JSON文件
            JsonObject jsonObject;
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
                jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            // 转换为ProjectData对象
            ProjectData projectData = convertFromJson(jsonObject);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("项目数据加载成功，耗时: {}ms，页面数量: {}", 
                       duration, projectData.getPageCount());
            
            return projectData;
            
        } catch (Exception e) {
            logger.error("加载项目数据失败: {}", e.getMessage(), e);
            throw new IOException("加载项目数据失败", e);
        }
    }
    
    @Override
    public boolean exists() {
        return dataFile.exists() && dataFile.isFile();
    }
    
    @Override
    public boolean delete() {
        if (exists()) {
            boolean deleted = dataFile.delete();
            logger.info("删除项目数据文件: {}, 结果: {}", dataFile.getAbsolutePath(), deleted);
            return deleted;
        }
        return true;
    }
    
    @Override
    public void backup(String backupName) throws IOException {
        if (!exists()) {
            throw new IOException("项目数据文件不存在，无法备份");
        }
        
        File backupFile = new File(backupDir, backupName + ".json");
        Path sourcePath = dataFile.toPath();
        Path targetPath = backupFile.toPath();
        
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("项目数据备份完成: {} -> {}", dataFile.getAbsolutePath(), backupFile.getAbsolutePath());
    }
    
    @Override
    public ProjectData restore(String backupName) throws IOException {
        File backupFile = new File(backupDir, backupName + ".json");
        if (!backupFile.exists()) {
            throw new IOException("备份文件不存在: " + backupFile.getAbsolutePath());
        }
        
        // 临时保存当前文件
        File tempFile = new File(dataFile.getAbsolutePath() + ".temp");
        if (exists()) {
            Files.copy(dataFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        try {
            // 恢复备份文件
            Files.copy(backupFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // 加载恢复的数据
            ProjectData restoredData = load();
            
            // 删除临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
            
            logger.info("项目数据恢复完成: {}", backupFile.getAbsolutePath());
            return restoredData;
            
        } catch (Exception e) {
            // 恢复失败，还原原文件
            if (tempFile.exists()) {
                Files.copy(tempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFile.delete();
            }
            throw new IOException("恢复项目数据失败", e);
        }
    }
    
    @Override
    public String getFilePath() {
        return dataFile.getAbsolutePath();
    }
    
    @Override
    public long getFileSize() {
        return exists() ? dataFile.length() : -1;
    }
    
    @Override
    public long getLastModified() {
        return exists() ? dataFile.lastModified() : -1;
    }
    
    /**
     * 将ProjectData转换为JSON对象
     */
    private JsonObject convertToJson(ProjectData projectData) {
        JsonObject jsonObject = new JsonObject();
        
        // 页面列表
        JsonArray pagesArray = new JsonArray();
        for (String pageName : projectData.getPages()) {
            pagesArray.add(pageName);
        }
        jsonObject.add("pages", pagesArray);
        
        // 当前页面
        if (projectData.getCurrentPage() != null) {
            jsonObject.addProperty("currentPage", projectData.getCurrentPage());
        }
        
        // 页面内容
        JsonObject pageContentsObject = new JsonObject();
        for (Map.Entry<String, PageData> entry : projectData.getPageContents().entrySet()) {
            String pageName = entry.getKey();
            PageData pageData = entry.getValue();
            
            JsonArray componentsArray = new JsonArray();
            for (ComponentData component : pageData.getComponents()) {
                JsonObject componentObject = convertComponentToJson(component);
                componentsArray.add(componentObject);
            }
            
            pageContentsObject.add(pageName, componentsArray);
        }
        jsonObject.add("pageContents", pageContentsObject);
        
        // 元数据
        jsonObject.addProperty("version", projectData.getVersion());
        jsonObject.addProperty("createdTime", projectData.getCreatedTime());
        jsonObject.addProperty("lastModifiedTime", projectData.getLastModifiedTime());
        
        return jsonObject;
    }

    /**
     * 将组件数据转换为JSON对象
     */
    private JsonObject convertComponentToJson(ComponentData component) {
        JsonObject componentObject = new JsonObject();

        componentObject.addProperty("x", component.getX());
        componentObject.addProperty("y", component.getY());
        componentObject.addProperty("width", component.getWidth());
        componentObject.addProperty("height", component.getHeight());
        componentObject.addProperty("originalWidth", component.getOriginalWidth());
        componentObject.addProperty("originalHeight", component.getOriginalHeight());
        componentObject.addProperty("functionType", component.getFunctionType());

        // 标签数据
        if (component.getLabelData() != null) {
            JsonObject labelDataObject = convertLabelDataToJson(component.getLabelData());
            componentObject.add("labelData", labelDataObject);
        }

        return componentObject;
    }

    /**
     * 将标签数据转换为JSON对象
     */
    private JsonObject convertLabelDataToJson(LabelData labelData) {
        JsonObject labelDataObject = new JsonObject();

        labelDataObject.addProperty("text", labelData.getText());
        labelDataObject.addProperty("fontName", labelData.getFontName());
        labelDataObject.addProperty("fontSize", labelData.getFontSize());
        labelDataObject.addProperty("fontStyle", labelData.getFontStyle());
        labelDataObject.addProperty("colorRGB", labelData.getColorRGB());
        labelDataObject.addProperty("iconPath", labelData.getIconPath());
        labelDataObject.addProperty("originalFontSize", labelData.getOriginalFontSize());
        labelDataObject.addProperty("horizontalAlignment", labelData.getHorizontalAlignment());
        labelDataObject.addProperty("verticalAlignment", labelData.getVerticalAlignment());
        labelDataObject.addProperty("horizontalTextPosition", labelData.getHorizontalTextPosition());
        labelDataObject.addProperty("verticalTextPosition", labelData.getVerticalTextPosition());

        return labelDataObject;
    }

    /**
     * 从JSON对象转换为ProjectData
     */
    private ProjectData convertFromJson(JsonObject jsonObject) {
        ProjectData projectData = new ProjectData();

        // 页面列表
        if (jsonObject.has("pages")) {
            JsonArray pagesArray = jsonObject.getAsJsonArray("pages");
            List<String> pages = new ArrayList<>();
            for (JsonElement element : pagesArray) {
                pages.add(element.getAsString());
            }
            projectData.setPages(pages);
        }

        // 当前页面
        if (jsonObject.has("currentPage")) {
            projectData.setCurrentPage(jsonObject.get("currentPage").getAsString());
        }

        // 页面内容
        if (jsonObject.has("pageContents")) {
            JsonObject pageContentsObject = jsonObject.getAsJsonObject("pageContents");
            Map<String, PageData> pageContents = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : pageContentsObject.entrySet()) {
                String pageName = entry.getKey();
                JsonArray componentsArray = entry.getValue().getAsJsonArray();

                PageData pageData = new PageData(pageName);
                List<ComponentData> components = new ArrayList<>();

                for (JsonElement componentElement : componentsArray) {
                    JsonObject componentObject = componentElement.getAsJsonObject();
                    ComponentData component = convertComponentFromJson(componentObject);
                    components.add(component);
                }

                pageData.setComponents(components);
                pageContents.put(pageName, pageData);
            }

            projectData.setPageContents(pageContents);
        }

        // 元数据
        if (jsonObject.has("version")) {
            projectData.setVersion(jsonObject.get("version").getAsString());
        }
        if (jsonObject.has("createdTime")) {
            projectData.setCreatedTime(jsonObject.get("createdTime").getAsLong());
        }
        if (jsonObject.has("lastModifiedTime")) {
            projectData.setLastModifiedTime(jsonObject.get("lastModifiedTime").getAsLong());
        }

        return projectData;
    }

    /**
     * 从JSON对象转换为组件数据
     */
    private ComponentData convertComponentFromJson(JsonObject componentObject) {
        ComponentData component = new ComponentData();

        component.setX(componentObject.get("x").getAsInt());
        component.setY(componentObject.get("y").getAsInt());
        component.setWidth(componentObject.get("width").getAsInt());
        component.setHeight(componentObject.get("height").getAsInt());
        component.setOriginalWidth(componentObject.get("originalWidth").getAsInt());
        component.setOriginalHeight(componentObject.get("originalHeight").getAsInt());
        component.setFunctionType(componentObject.get("functionType").getAsString());

        // 标签数据
        if (componentObject.has("labelData")) {
            JsonObject labelDataObject = componentObject.getAsJsonObject("labelData");
            LabelData labelData = convertLabelDataFromJson(labelDataObject);
            component.setLabelData(labelData);
        }

        return component;
    }

    /**
     * 从JSON对象转换为标签数据
     */
    private LabelData convertLabelDataFromJson(JsonObject labelDataObject) {
        LabelData labelData = new LabelData();

        labelData.setText(getStringValue(labelDataObject, "text"));
        labelData.setFontName(getStringValue(labelDataObject, "fontName"));
        labelData.setFontSize(getIntValue(labelDataObject, "fontSize"));
        labelData.setFontStyle(getIntValue(labelDataObject, "fontStyle"));
        labelData.setColorRGB(getIntValue(labelDataObject, "colorRGB"));
        labelData.setIconPath(getStringValue(labelDataObject, "iconPath"));
        labelData.setOriginalFontSize(getIntValue(labelDataObject, "originalFontSize"));
        labelData.setHorizontalAlignment(getIntValue(labelDataObject, "horizontalAlignment"));
        labelData.setVerticalAlignment(getIntValue(labelDataObject, "verticalAlignment"));
        labelData.setHorizontalTextPosition(getIntValue(labelDataObject, "horizontalTextPosition"));
        labelData.setVerticalTextPosition(getIntValue(labelDataObject, "verticalTextPosition"));

        return labelData;
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ?
               object.get(key).getAsString() : null;
    }

    /**
     * 安全获取整数值
     */
    private int getIntValue(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ?
               object.get(key).getAsInt() : 0;
    }

    /**
     * 确保目录存在
     */
    private void ensureDirectoryExists(File directory) {
        if (directory != null && !directory.exists()) {
            boolean created = directory.mkdirs();
            logger.info("创建目录: {}, 结果: {}", directory.getAbsolutePath(), created);
        }
    }
}
