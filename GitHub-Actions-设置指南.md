# GitHub Actions Android APK 自动构建设置指南

## 🎯 **完整设置步骤**

### **步骤1: 安装Git（如果未安装）**

**下载Git：**
- 访问：https://git-scm.com/download/win
- 下载并安装Git for Windows
- 安装时选择默认选项即可

### **步骤2: 创建GitHub仓库**

1. **登录GitHub**：https://github.com
2. **创建新仓库**：
   - 点击右上角 "+" → "New repository"
   - 仓库名称：`tablet-control-crossplatform`
   - 设置为Public（免费用户）或Private（付费用户）
   - 不要初始化README、.gitignore或license
   - 点击"Create repository"

### **步骤3: 本地Git配置**

在项目目录中打开PowerShell，执行：

```powershell
# 初始化Git仓库
git init

# 配置用户信息（替换为您的信息）
git config user.name "您的用户名"
git config user.email "您的邮箱@example.com"

# 添加远程仓库（替换为您的仓库地址）
git remote add origin https://github.com/您的用户名/tablet-control-crossplatform.git
```

### **步骤4: 准备项目文件**

确保以下文件存在并正确配置：

**✅ 已创建的文件：**
- `.github/workflows/build-android.yml` - GitHub Actions工作流
- `gluon-crossplatform/pom.xml` - Maven配置
- `gluon-crossplatform/src/` - 源代码目录

**📝 需要创建.gitignore：**
```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# GluonFX
gluonfx/

# Logs
*.log

# Temporary files
*.tmp
*.temp
```

### **步骤5: 提交并推送代码**

```powershell
# 创建.gitignore文件
@"
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# GluonFX
gluonfx/

# Logs
*.log

# Temporary files
*.tmp
*.temp
"@ | Out-File -FilePath ".gitignore" -Encoding UTF8

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: 平板中控跨平台应用"

# 推送到GitHub
git push -u origin main
```

### **步骤6: 验证GitHub Actions**

1. **访问您的GitHub仓库**
2. **点击"Actions"标签页**
3. **查看构建状态**：
   - 🟡 黄色圆圈：正在构建
   - ✅ 绿色勾号：构建成功
   - ❌ 红色叉号：构建失败

### **步骤7: 下载APK文件**

**构建成功后：**
1. 在Actions页面点击最新的构建
2. 滚动到底部找到"Artifacts"部分
3. 点击"tablet-control-android"下载APK
4. 解压下载的zip文件获得APK

## 🔧 **GitHub Actions工作流说明**

**我已为您创建的工作流文件包含：**

```yaml
# 触发条件
on:
  push:
    branches: [ main, develop ]  # 推送到main或develop分支时触发
  workflow_dispatch:            # 手动触发

# 构建环境
runs-on: ubuntu-latest         # 使用最新Ubuntu环境

# 构建步骤
steps:
  - 检出代码
  - 设置JDK 11
  - 设置Android SDK
  - 安装GraalVM + Native Image
  - 缓存Maven依赖
  - 构建Android APK
  - 上传APK文件
  - 创建Release（仅main分支）
```

## 📱 **预期结果**

**构建时间：** 15-30分钟
**生成文件：** `TabletControl-Android.apk`
**文件大小：** 约15-30MB
**支持设备：** Android 7.0+

## 🚨 **故障排除**

### **常见问题：**

1. **构建失败 - 依赖问题**
   ```bash
   # 检查pom.xml中的依赖版本
   # 确保JavaFX版本为11.0.2
   ```

2. **构建失败 - 内存不足**
   ```yaml
   # 在workflow中添加：
   env:
     MAVEN_OPTS: "-Xmx2048m"
   ```

3. **APK无法安装**
   ```bash
   # 检查Android设备设置
   # 启用"未知来源"应用安装
   ```

## 🎉 **成功标志**

看到以下信息表示成功：
- ✅ Actions页面显示绿色勾号
- 📱 Artifacts中有APK文件可下载
- 🚀 APK可以在Android设备上安装运行

## 📞 **需要帮助？**

如果遇到问题，请提供：
1. GitHub Actions构建日志
2. 错误信息截图
3. 具体的失败步骤

立即开始设置您的GitHub仓库吧！
