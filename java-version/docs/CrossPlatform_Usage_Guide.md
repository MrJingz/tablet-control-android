# 跨平台中控软件项目内容管理使用指南

## 🌟 核心特性

### ✨ **文件夹替换即项目切换**
- 只需替换整个`USERDATA`文件夹，重新打开软件即可切换项目
- 无需额外配置，自动加载新项目的所有内容
- 支持跨平台项目包共享

### 🔒 **跨平台加密保护**
- AES-256-CBC加密算法
- 支持Windows、Android、iOS、HarmonyOS
- 密码保护敏感配置文件

### 📁 **统一数据管理**
- 所有项目数据统一存储在`USERDATA`文件夹
- 标准化目录结构，确保跨平台兼容
- 自动检测和修复文件夹结构

## 🚀 快速开始

### **1. 启动应用程序**

#### Windows平台
```bash
# 跨平台启动（推荐）
start_crossplatform.bat

# 传统启动
start_app.bat

# 命令行启动
cd java-version
java -cp "target\classes;target\lib\*" com.feixiang.tabletcontrol.CrossPlatformLauncher
```

#### 其他平台
```bash
# 编译项目
mvn compile

# 启动跨平台版本
java -cp "target/classes:target/lib/*" com.feixiang.tabletcontrol.CrossPlatformLauncher

# 启动传统版本
java -cp "target/classes:target/lib/*" com.feixiang.tabletcontrol.SwingTabletControlApp
```

### **2. 首次启动**

应用程序会自动：
1. 检测当前平台（Windows/Android/iOS/HarmonyOS）
2. 确定最佳的USERDATA存储路径
3. 检查USERDATA文件夹完整性
4. 如果不存在，提示创建或导入项目包

## 📂 USERDATA文件夹结构

```
USERDATA/
├── .metadata                    # 项目元数据
├── config/                      # 配置文件
│   ├── project.json            # 项目主配置
│   ├── settings.json           # 应用设置
│   └── auth.json               # 认证配置（加密）
├── data/                        # 数据文件
│   ├── database/               # 数据库文件
│   └── logs/                   # 日志文件
├── media/                       # 媒体资源
│   ├── images/                 # 图片资源
│   ├── videos/                 # 视频资源
│   └── audio/                  # 音频资源
├── themes/                      # 主题资源
├── plugins/                     # 插件目录
├── backup/                      # 备份目录
└── cache/                       # 缓存目录
```

## 🔄 项目切换操作

### **方法一：手动替换文件夹**
1. 关闭应用程序
2. 备份当前USERDATA文件夹（可选）
3. 删除或重命名当前USERDATA文件夹
4. 将新的项目包重命名为USERDATA
5. 重新启动应用程序

### **方法二：使用项目包管理工具**
```bash
# 启动项目包管理工具
cd java-version
java -cp "target\classes;target\lib\*" ProjectPackageTool
```

工具功能：
- 🔍 **验证项目包**：检查当前USERDATA完整性
- 📁 **创建新项目**：初始化标准项目结构
- 📥 **导入项目包**：从其他位置导入项目
- 📤 **导出项目包**：备份当前项目到指定位置

### **方法三：使用应用程序内置功能**
启动时如果检测到USERDATA问题，应用程序会提供：
- 创建新项目选项
- 导入现有项目选项
- 修复损坏项目选项

## 🔐 加密功能使用

### **设置加密密码**
```bash
# 使用密码设置工具
cd java-version
java -cp "target\classes;target\lib\*;." SetPassword

# 或使用批处理文件
set_password.bat
```

### **启动时输入密码**
```bash
# 命令行指定密码
java -cp "target\classes;target\lib\*" com.feixiang.tabletcontrol.CrossPlatformLauncher --password=您的密码

# 启动时交互输入
java -cp "target\classes;target\lib\*" com.feixiang.tabletcontrol.CrossPlatformLauncher
```

### **加密文件管理**
- 敏感配置文件自动加密
- 支持密码更改
- 忘记密码时可重置（会丢失加密数据）

## 🌐 跨平台使用

### **Windows平台**
- **推荐路径**：`%USERPROFILE%\Documents\TabletControl\USERDATA`
- **备选路径**：应用程序安装目录下
- **特性**：完整功能支持，文件监控，自动重载

### **Android平台**
- **内部存储**：`/Android/data/com.feixiang.tabletcontrol/files/USERDATA`
- **外部存储**：`/storage/emulated/0/TabletControl/USERDATA`
- **特性**：支持文件分享，Google Drive同步

### **iOS平台**
- **应用沙盒**：`Documents/USERDATA`
- **共享目录**：支持Files应用访问
- **特性**：iCloud自动同步，iTunes文件共享

### **HarmonyOS平台**
- **应用目录**：`/data/storage/el2/base/haps/entry/files/USERDATA`
- **外部存储**：`/storage/media/100/local/files/TabletControl/USERDATA`
- **特性**：分布式文件系统，跨设备同步

## 📋 最佳实践

### **项目管理**
1. **定期备份**：使用导出功能备份重要项目
2. **版本控制**：为不同版本的项目包命名
3. **测试验证**：切换项目前先验证项目包完整性

### **跨平台共享**
1. **统一命名**：使用一致的文件命名规范
2. **路径兼容**：避免使用平台特定的路径分隔符
3. **文件格式**：使用跨平台兼容的文件格式

### **安全管理**
1. **强密码**：使用复杂密码保护敏感数据
2. **定期更换**：定期更换加密密码
3. **安全备份**：加密备份存储在安全位置

## 🛠️ 故障排除

### **常见问题**

#### **Q: USERDATA文件夹不存在**
A: 应用程序会自动提示创建或导入，也可以手动创建标准结构

#### **Q: 项目包验证失败**
A: 使用项目包管理工具检查具体错误，通常是文件夹结构不完整

#### **Q: 加密文件无法读取**
A: 检查密码是否正确，或尝试重置加密（会丢失数据）

#### **Q: 跨平台启动失败**
A: 使用`--legacy`参数启动传统版本，或检查Java环境

### **日志查看**
- 应用程序日志：`USERDATA/data/logs/app.log`
- 错误日志：`USERDATA/data/logs/error.log`
- 控制台输出：启动时的命令行窗口

### **重置应用程序**
如果遇到严重问题：
1. 备份USERDATA文件夹
2. 删除USERDATA文件夹
3. 重新启动应用程序
4. 选择创建新项目或导入备份

## 📞 技术支持

### **获取帮助**
- 查看日志文件了解详细错误信息
- 使用项目包管理工具诊断问题
- 尝试传统启动方式作为备选方案

### **报告问题**
提供以下信息：
- 操作系统和版本
- Java版本
- 错误日志内容
- 操作步骤重现

---

🎉 **恭喜！您现在可以享受跨平台项目内容管理的便利了！**

只需替换USERDATA文件夹，即可在不同项目间自由切换，真正实现"一个文件夹，一个项目"的理念！
