# JDK 8 升级到 JDK 11 完整指南

## 1. 下载JDK 11

### 推荐下载源
- **Oracle JDK 11**：https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
- **OpenJDK 11**：https://adoptium.net/temurin/releases/?version=11
- **Amazon Corretto 11**：https://aws.amazon.com/corretto/

### 推荐版本
- **OpenJDK 11.0.21** (免费，推荐)
- **Oracle JDK 11.0.21** (商业使用需要许可证)

## 2. 安装步骤

### Windows安装
1. 下载 `OpenJDK11U-jdk_x64_windows_hotspot_11.0.21_9.msi`
2. 双击运行安装程序
3. 选择安装路径，建议：`E:\JDK11`
4. 完成安装

## 3. 环境变量配置

### 方法一：系统环境变量（推荐）
1. 右键"此电脑" → "属性" → "高级系统设置"
2. 点击"环境变量"
3. 在"系统变量"中：

#### 新建JAVA_HOME
```
变量名：JAVA_HOME
变量值：E:\JDK11
```

#### 修改Path
在Path变量中添加：
```
%JAVA_HOME%\bin
```

#### 新建CLASSPATH（可选）
```
变量名：CLASSPATH
变量值：.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar
```

### 方法二：临时切换（测试用）
```cmd
set JAVA_HOME=E:\JDK11
set PATH=%JAVA_HOME%\bin;%PATH%
```

## 4. 验证安装

打开新的命令提示符窗口：
```cmd
java -version
javac -version
mvn -version
```

期望输出：
```
java version "11.0.21" 2023-10-17 LTS
Java(TM) SE Runtime Environment (build 11.0.21+9-LTS)
Java HotSpot(TM) 64-Bit Server VM (build 11.0.21+9-LTS, mixed mode)
```

## 5. Maven配置更新

### 确保Maven使用JDK 11
在 `%MAVEN_HOME%\conf\settings.xml` 中添加：
```xml
<profiles>
    <profile>
        <id>jdk-11</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <maven.compiler.source>11</maven.compiler.source>
            <maven.compiler.target>11</maven.compiler.target>
            <maven.compiler.compilerVersion>11</maven.compiler.compilerVersion>
        </properties>
    </profile>
</profiles>
```

## 6. IDE配置更新

### IntelliJ IDEA
1. File → Project Structure → Project Settings → Project
2. Project SDK: 选择JDK 11
3. Project language level: 11

### Eclipse
1. Window → Preferences → Java → Installed JREs
2. Add → Standard VM → 选择JDK 11目录
3. 设为默认

### VS Code
在 `.vscode/settings.json` 中：
```json
{
    "java.configuration.runtimes": [
        {
            "name": "JavaSE-11",
            "path": "E:\\JDK11"
        }
    ],
    "java.compile.nullAnalysis.mode": "automatic"
}
```

## 7. 项目代码兼容性检查

### 需要注意的变化

#### 模块系统（可选使用）
JDK 11引入了模块系统，但向后兼容：
```java
// module-info.java (可选)
module com.feixiang.tabletcontrol {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    
    exports com.feixiang.tabletcontrol;
}
```

#### JavaFX分离
JDK 11+不再内置JavaFX，需要外部依赖（已在pom.xml中配置）

#### 废弃API清理
检查并替换已废弃的API：
```java
// JDK 8
new Integer(123)  // 已废弃

// JDK 11+
Integer.valueOf(123)  // 推荐
```

## 8. 测试升级结果

### 编译测试
```cmd
cd E:\平板中控windows版\java-version
mvn clean compile
```

### 运行测试
```cmd
mvn javafx:run
```

### 打包测试
```cmd
mvn clean package
```

## 9. 常见问题解决

### 问题1：编译错误"无效的目标发行版"
**解决**：确保JAVA_HOME指向JDK 11，重启命令提示符

### 问题2：JavaFX运行时错误
**解决**：添加JVM参数：
```cmd
java --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml -jar app.jar
```

### 问题3：Maven仍使用JDK 8
**解决**：
1. 检查JAVA_HOME环境变量
2. 重启IDE和命令提示符
3. 运行 `mvn -version` 确认

## 10. 回滚方案

如果需要回滚到JDK 8：
1. 修改JAVA_HOME为：`E:\JDK1.8`
2. 恢复pom.xml中的版本配置
3. 重启开发环境

## 下一步

安装完JDK 11后，我们就可以：
1. ✅ 使用GluonFX插件
2. ✅ 生成Android APK
3. ✅ 支持跨平台部署
4. ✅ 享受JDK 11的新特性

请按照以上步骤安装JDK 11，然后告诉我安装结果！
