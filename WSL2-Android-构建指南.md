# WSL2 + Ubuntu Android APK构建完整指南

## 🎯 方案概述

使用WSL2 + Ubuntu环境构建Android APK，解决Windows上GraalVM交叉编译限制问题。

## 📋 准备工作

### 系统要求
- Windows 10 版本 2004 及更高版本，或 Windows 11
- 启用虚拟化功能（在BIOS中启用）
- 至少8GB RAM（推荐16GB）
- 至少20GB可用磁盘空间

## 🚀 安装步骤

### 第一阶段：安装WSL2和Ubuntu

1. **以管理员身份运行PowerShell**
   ```powershell
   .\setup-wsl2-android.ps1
   ```

2. **重启计算机**
   - 脚本会提示重启，选择Y重启

3. **启动Ubuntu并设置用户**
   - 重启后，从开始菜单启动"Ubuntu 20.04"
   - 设置用户名和密码（建议使用简单的用户名，如：dev）

### 第二阶段：配置Ubuntu开发环境

1. **在Ubuntu中运行配置脚本**
   ```bash
   # 首先复制脚本到Ubuntu
   cp /mnt/e/平板中控windows版/setup-ubuntu-android.sh ~/
   chmod +x ~/setup-ubuntu-android.sh
   ./setup-ubuntu-android.sh
   ```

2. **重新加载环境变量**
   ```bash
   source ~/.bashrc
   ```

### 第三阶段：迁移项目

1. **在Windows PowerShell中运行**
   ```powershell
   .\migrate-project-to-wsl.ps1
   ```

2. **或手动复制项目**
   ```bash
   # 在Ubuntu中运行
   mkdir -p ~/projects
   cp -r /mnt/e/平板中控windows版/gluon-crossplatform ~/projects/
   ```

### 第四阶段：构建Android APK

1. **进入项目目录**
   ```bash
   cd ~/projects/gluon-crossplatform
   ```

2. **运行构建脚本**
   ```bash
   chmod +x build-android-wsl.sh
   ./build-android-wsl.sh
   ```

3. **或手动构建**
   ```bash
   # 设置环境变量
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
   export GRAALVM_HOME=/opt/graalvm
   export ANDROID_HOME=$HOME/android-sdk
   export PATH=$GRAALVM_HOME/bin:$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
   
   # 构建APK
   mvn gluonfx:build -Pandroid
   ```

## 📱 APK部署和测试

### 传输APK到Windows
```bash
# 将APK复制到Windows E盘
cp target/gluonfx/aarch64-android/gvm/Tablet\ Control\ -\ Cross\ Platform.apk /mnt/e/tablet-control.apk
```

### 安装到Android设备

1. **启用开发者选项**
   - 设置 → 关于手机 → 连续点击"版本号"7次
   - 设置 → 开发者选项 → 启用"USB调试"

2. **使用ADB安装**
   ```bash
   # 在Ubuntu中
   adb devices  # 确认设备连接
   adb install target/gluonfx/aarch64-android/gvm/Tablet\ Control\ -\ Cross\ Platform.apk
   ```

3. **或手动安装**
   - 将APK文件传输到Android设备
   - 在设备上点击APK文件安装

## 🔧 故障排除

### 常见问题

1. **WSL2安装失败**
   - 确保Windows版本支持WSL2
   - 在BIOS中启用虚拟化功能
   - 运行Windows更新

2. **Ubuntu启动失败**
   - 重新安装Ubuntu：`wsl --unregister Ubuntu-20.04`
   - 从Microsoft Store重新安装

3. **GraalVM下载失败**
   - 检查网络连接
   - 手动下载并解压到/opt/graalvm

4. **Android SDK安装失败**
   - 检查网络连接
   - 手动下载Android SDK

5. **构建失败**
   - 检查所有环境变量是否正确设置
   - 确认所有依赖都已安装
   - 查看详细错误日志

### 验证命令
```bash
# 验证Java
java -version

# 验证Maven
mvn -version

# 验证GraalVM
/opt/graalvm/bin/java -version
native-image --version

# 验证Android SDK
sdkmanager --list
```

## 📊 预期结果

- **构建时间**：首次构建约15-30分钟
- **APK大小**：约50-100MB
- **支持架构**：ARM64 (aarch64)
- **最低Android版本**：Android 7.0 (API 24)

## 🎉 成功标志

构建成功后，您将看到：
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX:XX min
```

APK文件位置：
```
target/gluonfx/aarch64-android/gvm/Tablet Control - Cross Platform.apk
```

## 📞 技术支持

如果遇到问题，请提供：
1. 错误日志的完整输出
2. 环境验证命令的输出
3. 具体的错误步骤描述
