#!/bin/bash

echo "========================================="
echo "Docker Android APK 构建脚本"
echo "========================================="

# 检查项目结构
if [ ! -f "gluon-crossplatform/pom.xml" ]; then
    echo "错误: 未找到 gluon-crossplatform/pom.xml"
    echo "请确保项目已正确挂载到 /workspace"
    exit 1
fi

# 进入项目目录
cd gluon-crossplatform

echo "环境信息:"
echo "Java版本: $(java -version 2>&1 | head -1)"
echo "Maven版本: $(mvn -version | head -1)"
echo "GraalVM版本: $(/opt/graalvm/bin/java -version 2>&1 | head -1)"
echo "Native Image: $(/opt/graalvm/bin/native-image --version 2>&1 | head -1)"
echo "Android SDK: $(ls -la $ANDROID_HOME)"

echo ""
echo "开始构建Android APK..."
echo "这可能需要20-40分钟，请耐心等待..."

# 清理并构建
mvn clean
mvn gluonfx:build -Pandroid

# 检查构建结果
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ 构建成功！"
    echo "========================================="
    
    # 查找生成的APK文件
    find target -name "*.apk" -type f | while read apk; do
        size=$(du -h "$apk" | cut -f1)
        echo "📱 APK文件: $apk ($size)"
        
        # 复制到输出目录
        mkdir -p /workspace/output
        cp "$apk" "/workspace/output/TabletControl-Android.apk"
        echo "✅ 已复制到: /workspace/output/TabletControl-Android.apk"
    done
else
    echo ""
    echo "========================================="
    echo "❌ 构建失败"
    echo "========================================="
    echo "请检查上面的错误信息"
    exit 1
fi
