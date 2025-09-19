# 平板中控系统 - Java版本

## 项目简介

这是从Python版本转换而来的Java实现，使用JavaFX构建GUI界面，保持了原有的所有功能和业务逻辑。

## 技术栈

- **Java 11+**: 核心开发语言
- **JavaFX**: GUI框架
- **Maven**: 项目构建和依赖管理
- **Jackson**: JSON数据处理
- **SLF4J + Logback**: 日志框架
- **JUnit 5**: 单元测试框架

## 项目结构

```
java-version/
├── pom.xml                                    # Maven配置文件
├── README.md                                  # 项目说明文档
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java              # Java模块配置
│   │   │   └── com/feixiang/tabletcontrol/
│   │   │       ├── HelloWorldApp.java        # 主应用程序类
│   │   │       ├── package-info.java         # 包信息
│   │   │       ├── auth/                     # 认证模块
│   │   │       │   └── AuthCodeGeneratorFile.java
│   │   │       ├── config/                   # 配置模块
│   │   │       │   └── SecurityConfig.java
│   │   │       └── storage/                  # 存储模块
│   │   │           └── AuthFileStorage.java
│   │   └── resources/
│   │       ├── application.properties         # 应用配置
│   │       └── logback.xml                   # 日志配置
│   └── test/
│       └── java/                             # 测试代码目录
├── logs/                                     # 日志文件目录
└── data/                                     # 数据文件目录
```

## 核心功能

### 1. 用户界面
- 基于JavaFX的现代化GUI界面
- 支持全屏模式切换 (F11)
- 实时日志显示
- 响应式布局设计

### 2. 授权管理
- HMAC-SHA256授权码生成和验证
- 支持临时和永久授权码
- 自动清理过期授权码
- 文件存储授权数据

### 3. 安全配置
- SHA-256密码哈希
- 登录尝试次数限制
- 配置文件加密存储
- 安全状态监控

### 4. 数据存储
- JSON格式数据持久化
- 自动备份机制
- 数据完整性验证
- 存储统计信息

## 环境要求

- **Java**: JDK 11 或更高版本
- **Maven**: 3.6.0 或更高版本
- **操作系统**: Windows 10/11, macOS 10.14+, Linux (Ubuntu 18.04+)

## 安装和运行

### 1. 克隆项目
```bash
cd java-version
```

### 2. 编译项目
```bash
mvn clean compile
```

### 3. 运行应用
```bash
mvn javafx:run
```

### 4. 打包应用
```bash
mvn clean package
```

### 5. 运行打包后的应用
```bash
java -jar target/tablet-control-java-1.0.jar
```

## 开发指南

### 项目配置

应用程序的配置信息存储在 `src/main/resources/application.properties` 文件中，包括：

- 窗口尺寸和行为设置
- 安全配置参数
- 授权码生成设置
- 日志级别配置
- UI主题设置

### 日志配置

日志配置位于 `src/main/resources/logback.xml`，支持：

- 控制台和文件双重输出
- 按日期和大小滚动
- 不同模块的日志级别控制
- UTF-8编码支持

### 数据存储

应用程序使用JSON文件存储数据：

- `data/security_config.json`: 安全配置数据
- `data/auth_codes.json`: 授权码数据
- `logs/`: 应用程序日志文件

## API文档

### AuthCodeGeneratorFile类

主要方法：
- `generateAuthCode(String dateStr, String userId, boolean isPermanent)`: 生成授权码
- `verifyAuthCode(String authCode)`: 验证授权码
- `cleanupExpiredCodes()`: 清理过期授权码
- `getStorageInfo()`: 获取存储统计信息

### SecurityConfig类

主要方法：
- `verifyPassword(String password)`: 验证密码
- `hashPassword(String password)`: 生成密码哈希
- `incrementLoginAttempts()`: 增加登录尝试次数
- `resetLoginAttempts()`: 重置登录尝试次数

### AuthFileStorage类

主要方法：
- `saveAuthCode(String authCode, Map<String, Object> authData)`: 保存授权码
- `getAuthCode(String authCode)`: 获取授权码数据
- `deleteAuthCode(String authCode)`: 删除授权码
- `cleanupExpiredCodes()`: 清理过期授权码

## 测试

### 运行单元测试
```bash
mvn test
```

### 运行集成测试
```bash
mvn verify
```

### 生成测试报告
```bash
mvn surefire-report:report
```

## 部署

### 创建可执行JAR
```bash
mvn clean package -P executable
```

### 创建Windows安装包
```bash
mvn clean package -P windows-installer
```

### 创建跨平台分发包
```bash
mvn clean package -P cross-platform
```

## 故障排除

### 常见问题

1. **JavaFX运行时错误**
   - 确保使用包含JavaFX的JDK版本
   - 或者单独安装JavaFX SDK

2. **模块路径问题**
   - 检查module-info.java中的依赖声明
   - 确保所有依赖都在模块路径中

3. **日志文件权限错误**
   - 确保应用程序有写入logs目录的权限
   - 检查磁盘空间是否充足

4. **配置文件加载失败**
   - 验证配置文件格式是否正确
   - 检查文件编码是否为UTF-8

### 调试模式

启用调试模式：
```bash
mvn javafx:run -Ddeveloper.mode=true -Ddebug.enabled=true
```

## 版本历史

- **v1.0** (2024-01-20)
  - 初始Java版本发布
  - 完整的Python功能移植
  - JavaFX GUI实现
  - Maven构建支持

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: [您的姓名]
- 邮箱: [您的邮箱]
- 项目链接: [项目仓库地址]

## 致谢

- 感谢原Python版本的开发团队
- 感谢JavaFX社区的支持
- 感谢所有贡献者的努力