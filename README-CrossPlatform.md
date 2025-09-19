# 平板中控跨平台应用

一个基于JavaFX和GluonFX的跨平台平板控制应用，支持Windows、Android、iOS等多个平台。

## 🚀 特性

- **跨平台支持**：Windows桌面版 + Android移动版
- **现代UI**：基于JavaFX的响应式界面设计
- **主题系统**：支持浅色/深色主题切换
- **项目管理**：完整的项目创建和管理功能
- **数据持久化**：JSON格式的本地数据存储
- **国际化**：支持中文界面

## 📱 支持平台

- ✅ **Windows 10/11** (JDK 11+)
- ✅ **Android 7.0+** (API Level 24+)
- 🔄 **iOS** (计划中)
- 🔄 **macOS** (计划中)

## 🛠️ 技术栈

- **Java 11** - 核心开发语言
- **JavaFX 11.0.2** - UI框架
- **GluonFX** - 跨平台移动开发
- **Maven** - 项目构建管理
- **GraalVM Native Image** - 原生编译
- **Jackson** - JSON数据处理
- **Logback** - 日志系统

## 📦 自动构建

本项目使用GitHub Actions自动构建Android APK：

1. 推送代码到`main`或`develop`分支
2. GitHub Actions自动触发构建
3. 构建完成后在Actions页面下载APK

### 手动触发构建

在GitHub仓库页面：
1. 点击"Actions"标签
2. 选择"Build Android APK"工作流
3. 点击"Run workflow"

## 🏗️ 本地开发

### 环境要求

- JDK 11+
- Maven 3.6+
- Android SDK (仅Android构建)
- GraalVM (仅原生构建)

### 运行桌面版

```bash
cd gluon-crossplatform
mvn javafx:run
```

### 构建Android APK

```bash
cd gluon-crossplatform
mvn gluonfx:build -Pandroid
```

## 📁 项目结构

```
├── gluon-crossplatform/          # 跨平台项目
│   ├── src/main/java/           # Java源代码
│   ├── src/main/resources/      # 资源文件
│   └── pom.xml                  # Maven配置
├── java-version/                # 传统JavaFX版本
├── .github/workflows/           # GitHub Actions工作流
└── README.md                    # 项目说明
```

## 🎯 核心功能

### 平台管理
- 自动检测运行平台
- 平台特定的服务实现
- 响应式布局适配

### 项目管理
- 创建和管理项目
- 页面和组件管理
- 项目数据持久化

### 用户界面
- 现代化Material Design风格
- 主题切换支持
- 多语言界面支持

## 📄 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📞 联系

如有问题或建议，请创建Issue或联系开发团队。
