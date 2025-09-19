#!/bin/bash

echo "========================================"
echo "Ubuntu Android开发环境配置脚本"
echo "========================================"

# 更新系统
echo "步骤1: 更新系统包..."
sudo apt update && sudo apt upgrade -y

# 安装基础工具
echo "步骤2: 安装基础开发工具..."
sudo apt install -y curl wget unzip git build-essential

# 安装Java 11
echo "步骤3: 安装OpenJDK 11..."
sudo apt install -y openjdk-11-jdk
echo "Java版本："
java -version

# 设置JAVA_HOME
echo "步骤4: 配置JAVA_HOME..."
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 安装Maven
echo "步骤5: 安装Maven..."
sudo apt install -y maven
echo "Maven版本："
mvn -version

# 下载并安装GraalVM
echo "步骤6: 下载并安装GraalVM..."
cd /tmp
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.3/graalvm-ce-java11-linux-amd64-22.3.3.tar.gz
sudo mkdir -p /opt/graalvm
sudo tar -xzf graalvm-ce-java11-linux-amd64-22.3.3.tar.gz -C /opt/graalvm --strip-components=1
sudo chown -R $USER:$USER /opt/graalvm

# 配置GraalVM环境变量
echo "步骤7: 配置GraalVM环境变量..."
echo 'export GRAALVM_HOME=/opt/graalvm' >> ~/.bashrc
echo 'export PATH=$GRAALVM_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 安装native-image
echo "步骤8: 安装native-image..."
/opt/graalvm/bin/gu install native-image

# 验证GraalVM安装
echo "步骤9: 验证GraalVM安装..."
/opt/graalvm/bin/java -version
/opt/graalvm/bin/native-image --version

# 下载Android SDK
echo "步骤10: 下载Android SDK..."
cd ~
mkdir -p android-sdk
cd android-sdk

# 检查是否已经下载
if [ ! -f "commandlinetools-linux-9477386_latest.zip" ]; then
    echo "正在下载Android SDK命令行工具..."
    wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
    if [ $? -ne 0 ]; then
        echo "错误: Android SDK下载失败"
        exit 1
    fi
fi

# 解压并设置目录结构
if [ ! -d "cmdline-tools/latest" ]; then
    echo "正在解压Android SDK..."
    unzip -q commandlinetools-linux-9477386_latest.zip
    mkdir -p cmdline-tools/latest
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
    # 清理空目录
    rmdir cmdline-tools/cmdline-tools 2>/dev/null || true
fi

# 配置Android SDK环境变量
echo "步骤11: 配置Android SDK环境变量..."
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export ANDROID_SDK_ROOT=$ANDROID_HOME' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/platform-tools:$PATH' >> ~/.bashrc
source ~/.bashrc

# 安装Android SDK组件
echo "步骤12: 安装Android SDK组件..."
export ANDROID_HOME=$HOME/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# 验证sdkmanager是否可用
if ! command -v sdkmanager &> /dev/null; then
    echo "错误: sdkmanager未找到，请检查Android SDK安装"
    exit 1
fi

echo "接受Android SDK许可证..."
yes | sdkmanager --licenses

echo "安装Android SDK组件..."
sdkmanager "platform-tools" "platforms;android-30" "build-tools;30.0.3"
if [ $? -ne 0 ]; then
    echo "警告: 部分Android SDK组件安装失败，但继续执行"
fi

# 创建项目目录并复制项目
echo "步骤13: 准备项目目录..."
mkdir -p ~/projects
echo "请将Windows项目复制到 ~/projects/ 目录"

# 最终验证函数
verify_installation() {
    echo ""
    echo "========================================"
    echo "验证安装结果..."
    echo "========================================"

    local errors=0

    # 验证Java
    echo "检查Java安装..."
    if java -version &> /dev/null; then
        echo "✓ Java安装成功"
        java -version 2>&1 | head -1
    else
        echo "✗ Java安装失败"
        errors=$((errors + 1))
    fi

    # 验证Maven
    echo "检查Maven安装..."
    if mvn -version &> /dev/null; then
        echo "✓ Maven安装成功"
        mvn -version 2>&1 | head -1
    else
        echo "✗ Maven安装失败"
        errors=$((errors + 1))
    fi

    # 验证GraalVM
    echo "检查GraalVM安装..."
    if /opt/graalvm/bin/java -version &> /dev/null; then
        echo "✓ GraalVM安装成功"
        /opt/graalvm/bin/java -version 2>&1 | head -1
    else
        echo "✗ GraalVM安装失败"
        errors=$((errors + 1))
    fi

    # 验证native-image
    echo "检查native-image安装..."
    if /opt/graalvm/bin/native-image --version &> /dev/null; then
        echo "✓ native-image安装成功"
    else
        echo "✗ native-image安装失败"
        errors=$((errors + 1))
    fi

    # 验证Android SDK
    echo "检查Android SDK安装..."
    if [ -d "$HOME/android-sdk/cmdline-tools/latest" ]; then
        echo "✓ Android SDK目录结构正确"
    else
        echo "✗ Android SDK目录结构错误"
        errors=$((errors + 1))
    fi

    return $errors
}

# 执行验证
verify_installation
verification_result=$?

echo ""
echo "========================================"
if [ $verification_result -eq 0 ]; then
    echo "✓ 安装完成！所有组件验证通过"
else
    echo "⚠ 安装完成，但有 $verification_result 个组件验证失败"
fi
echo "========================================"
echo "环境变量配置："
echo "JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
echo "GRAALVM_HOME=/opt/graalvm"
echo "ANDROID_HOME=$HOME/android-sdk"
echo ""
echo "下一步："
echo "1. 重新加载环境变量: source ~/.bashrc"
echo "2. 复制项目到 ~/projects/ 目录"
echo "3. 运行构建命令: mvn gluonfx:build -Pandroid"
echo ""
echo "手动验证命令："
echo "java -version"
echo "mvn -version"
echo "/opt/graalvm/bin/native-image --version"
echo "sdkmanager --list"
