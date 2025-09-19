#!/bin/bash

echo "========================================"
echo "WSL Ubuntu Android APK构建脚本"
echo "========================================"

# 错误处理函数
handle_error() {
    echo "错误: $1"
    echo "构建失败，退出脚本"
    exit 1
}

# 预检查函数
pre_check() {
    echo "执行预检查..."

    # 检查是否在WSL环境中
    if ! grep -q Microsoft /proc/version 2>/dev/null; then
        echo "警告: 似乎不在WSL环境中运行"
    fi

    # 检查项目目录
    if [ ! -f "pom.xml" ]; then
        handle_error "未找到pom.xml文件，请确保在项目根目录运行此脚本"
    fi

    # 检查gluon-crossplatform目录
    if [ ! -d "gluon-crossplatform" ]; then
        handle_error "未找到gluon-crossplatform目录，请确保项目结构正确"
    fi

    echo "✓ 预检查通过"
}

# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export GRAALVM_HOME=/opt/graalvm
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$GRAALVM_HOME/bin:$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# 执行预检查
pre_check

echo "步骤1: 验证环境配置..."

# 验证Java
echo "检查Java..."
if ! java -version &> /dev/null; then
    handle_error "Java未安装或配置错误"
fi
echo "✓ Java版本："
java -version 2>&1 | head -1

# 验证Maven
echo "检查Maven..."
if ! mvn -version &> /dev/null; then
    handle_error "Maven未安装或配置错误"
fi
echo "✓ Maven版本："
mvn -version 2>&1 | head -1

# 验证GraalVM
echo "检查GraalVM..."
if [ ! -f "$GRAALVM_HOME/bin/java" ]; then
    handle_error "GraalVM未找到，路径: $GRAALVM_HOME"
fi
echo "✓ GraalVM版本："
$GRAALVM_HOME/bin/java -version 2>&1 | head -1

# 验证native-image
echo "检查native-image..."
if ! native-image --version &> /dev/null; then
    handle_error "native-image未安装或配置错误"
fi
echo "✓ native-image版本："
native-image --version 2>&1 | head -1

# 验证Android SDK
echo "检查Android SDK..."
if [ ! -d "$ANDROID_HOME" ]; then
    handle_error "Android SDK未找到，路径: $ANDROID_HOME"
fi
echo "✓ Android SDK路径: $ANDROID_HOME"
if [ -d "$ANDROID_HOME/platform-tools" ]; then
    echo "✓ platform-tools已安装"
else
    echo "⚠ platform-tools未找到"
fi

echo ""
echo "步骤2: 切换到gluon-crossplatform目录..."
cd gluon-crossplatform || handle_error "无法进入gluon-crossplatform目录"

echo ""
echo "步骤3: 清理之前的构建..."
mvn clean
if [ $? -ne 0 ]; then
    handle_error "Maven clean失败"
fi

echo ""
echo "步骤4: 编译项目..."
mvn compile
if [ $? -ne 0 ]; then
    handle_error "项目编译失败"
fi

echo ""
echo "步骤5: 构建Android APK..."
echo "开始构建，这可能需要10-30分钟..."
echo "构建命令: mvn gluonfx:build -Pandroid"
echo ""

# 设置构建超时（30分钟）
timeout 1800 mvn gluonfx:build -Pandroid
build_result=$?

if [ $build_result -eq 124 ]; then
    handle_error "构建超时（30分钟），请检查网络连接和系统资源"
elif [ $build_result -ne 0 ]; then
    echo "构建失败，退出码: $build_result"
    echo "请检查上面的错误信息"
    exit 1
fi

echo ""
echo "步骤6: 检查构建结果..."

# 检查构建目录
if [ ! -d "target/gluonfx" ]; then
    handle_error "构建目录 target/gluonfx 不存在，构建可能失败"
fi

echo "构建目录内容："
ls -la target/gluonfx/

# 查找APK文件
APK_FILES=($(find target/gluonfx -name "*.apk" 2>/dev/null))
AAB_FILES=($(find target/gluonfx -name "*.aab" 2>/dev/null))

if [ ${#APK_FILES[@]} -gt 0 ]; then
    echo ""
    echo "========================================"
    echo "🎉 构建成功！"
    echo "========================================"

    for apk in "${APK_FILES[@]}"; do
        echo "APK文件: $apk"
        echo "文件大小: $(du -h "$apk" | cut -f1)"
        echo "文件信息: $(file "$apk")"
        echo ""
    done

    # 自动复制到Windows可访问位置
    MAIN_APK="${APK_FILES[0]}"
    WINDOWS_PATH="/mnt/e/tablet-control-$(date +%Y%m%d-%H%M%S).apk"

    echo "传输APK到Windows..."
    if cp "$MAIN_APK" "$WINDOWS_PATH"; then
        echo "✓ APK已复制到: $WINDOWS_PATH"
        echo "Windows路径: E:\\tablet-control-$(date +%Y%m%d-%H%M%S).apk"
    else
        echo "⚠ 无法复制到Windows，请手动复制:"
        echo "cp \"$MAIN_APK\" /mnt/e/"
    fi

    echo ""
    echo "下一步操作："
    echo "1. 在Windows中找到APK文件"
    echo "2. 通过USB连接Android设备"
    echo "3. 启用开发者选项和USB调试"
    echo "4. 安装APK: adb install \"$MAIN_APK\""
    echo ""

elif [ ${#AAB_FILES[@]} -gt 0 ]; then
    echo ""
    echo "========================================"
    echo "✓ AAB构建成功！"
    echo "========================================"

    for aab in "${AAB_FILES[@]}"; do
        echo "AAB文件: $aab"
        echo "文件大小: $(du -h "$aab" | cut -f1)"
        echo ""
    done

    echo "注意: AAB文件需要通过Google Play Console发布"

else
    echo ""
    echo "========================================"
    echo "❌ 构建失败或APK/AAB文件未找到"
    echo "========================================"
    echo ""
    echo "故障排除："
    echo "1. 检查上面的构建日志中的错误信息"
    echo "2. 验证Android SDK组件是否完整安装"
    echo "3. 确认GraalVM native-image可用"
    echo "4. 检查项目依赖和配置"
    echo ""
    echo "构建目录详细内容："
    find target/gluonfx -type f 2>/dev/null | head -20

    exit 1
fi

echo ""
echo "========================================"
echo "构建流程完成！"
echo "========================================"
