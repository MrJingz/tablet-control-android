#!/bin/bash

echo "========================================"
echo "跨平台平板中控软件部署脚本 (Linux版)"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 错误处理函数
handle_error() {
    echo -e "${RED}错误: $1${NC}"
    echo "部署失败，退出脚本"
    exit 1
}

# 成功信息函数
success_msg() {
    echo -e "${GREEN}✓ $1${NC}"
}

# 警告信息函数
warning_msg() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# 信息函数
info_msg() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# 检查依赖函数
check_dependencies() {
    echo "检查构建依赖..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        handle_error "Java未安装，请先安装Java 11+"
    fi
    success_msg "Java已安装: $(java -version 2>&1 | head -1)"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        handle_error "Maven未安装，请先安装Apache Maven"
    fi
    success_msg "Maven已安装: $(mvn -version 2>&1 | head -1)"
    
    # 检查项目结构
    if [ ! -f "pom.xml" ]; then
        handle_error "未找到pom.xml，请在项目根目录运行此脚本"
    fi
    
    success_msg "依赖检查通过"
}

# 创建构建目录
setup_build_dirs() {
    echo ""
    echo "[1/8] 设置构建目录..."
    
    BUILD_DIR="build-$(date +%Y%m%d-%H%M%S)"
    DIST_DIR="dist"
    
    mkdir -p "$BUILD_DIR"/{jar,native-desktop,android,ios,docs}
    mkdir -p "$DIST_DIR/complete"
    
    success_msg "构建目录创建完成: $BUILD_DIR"
}

# 清理项目
clean_project() {
    echo ""
    echo "[2/8] 清理项目..."
    
    mvn clean
    if [ $? -ne 0 ]; then
        handle_error "项目清理失败"
    fi
    
    success_msg "项目清理完成"
}

# 编译项目
compile_project() {
    echo ""
    echo "[3/8] 编译项目..."
    
    mvn compile
    if [ $? -ne 0 ]; then
        handle_error "项目编译失败"
    fi
    
    success_msg "项目编译完成"
}

# 运行测试
run_tests() {
    echo ""
    echo "[4/8] 运行测试..."
    
    mvn test
    if [ $? -ne 0 ]; then
        warning_msg "测试失败，但继续构建"
    else
        success_msg "测试通过"
    fi
}

# 打包JAR
package_jar() {
    echo ""
    echo "[5/8] 打包JAR文件..."
    
    mvn package -DskipTests
    if [ $? -ne 0 ]; then
        handle_error "JAR打包失败"
    fi
    
    # 复制JAR文件
    if ls target/*.jar 1> /dev/null 2>&1; then
        cp target/*.jar "$BUILD_DIR/jar/"
        success_msg "JAR包构建成功"
    else
        warning_msg "未找到JAR文件"
    fi
}

# 构建原生桌面应用
build_native_desktop() {
    echo ""
    echo "[6/8] 构建原生桌面应用..."
    
    info_msg "正在构建当前平台的原生应用..."
    mvn gluonfx:build
    
    if [ $? -eq 0 ]; then
        success_msg "原生桌面应用构建完成"
        if [ -d "target/gluonfx" ]; then
            cp -r target/gluonfx/* "$BUILD_DIR/native-desktop/" 2>/dev/null || true
        fi
    else
        warning_msg "原生桌面应用构建失败，但继续执行"
    fi
}

# 构建移动端应用
build_mobile_apps() {
    echo ""
    echo "[7/8] 构建移动端应用..."
    
    # Android构建
    info_msg "尝试构建Android应用..."
    mvn gluonfx:build -Pandroid
    if [ $? -eq 0 ]; then
        success_msg "Android应用构建完成"
        if [ -d "target/gluonfx" ]; then
            find target/gluonfx -name "*.apk" -exec cp {} "$BUILD_DIR/android/" \; 2>/dev/null || true
        fi
    else
        info_msg "Android构建不可用或失败"
    fi
    
    # iOS构建（仅在macOS上可用）
    if [[ "$OSTYPE" == "darwin"* ]]; then
        info_msg "尝试构建iOS应用..."
        mvn gluonfx:build -Pios
        if [ $? -eq 0 ]; then
            success_msg "iOS应用构建完成"
            if [ -d "target/gluonfx" ]; then
                find target/gluonfx -name "*.ipa" -exec cp {} "$BUILD_DIR/ios/" \; 2>/dev/null || true
            fi
        else
            info_msg "iOS构建失败"
        fi
    else
        info_msg "iOS构建仅在macOS上可用"
    fi
}

# 创建运行脚本
create_run_scripts() {
    echo ""
    echo "创建运行脚本..."
    
    # Linux/Unix运行脚本
    cat > "$DIST_DIR/complete/run-linux.sh" << 'EOF'
#!/bin/bash
echo "启动跨平台平板中控软件..."

if [ -f native-desktop/TabletControl ]; then
    echo "运行原生版本..."
    ./native-desktop/TabletControl
elif [ -f jar/*.jar ]; then
    echo "运行JAR版本..."
    java -jar jar/*.jar
else
    echo "错误: 未找到可执行文件"
    exit 1
fi
EOF
    
    chmod +x "$DIST_DIR/complete/run-linux.sh"
    
    # Windows运行脚本
    cat > "$DIST_DIR/complete/run-windows.bat" << 'EOF'
@echo off
echo 启动跨平台平板中控软件...

if exist "native-desktop\TabletControl.exe" (
    echo 运行原生版本...
    native-desktop\TabletControl.exe
) else if exist "jar\*.jar" (
    echo 运行JAR版本...
    for %%f in (jar\*.jar) do java -jar "%%f"
) else (
    echo 错误: 未找到可执行文件
    pause
)
EOF
    
    success_msg "运行脚本创建完成"
}

# 生成部署报告
generate_report() {
    echo ""
    echo "[8/8] 生成部署报告..."
    
    REPORT_FILE="$DIST_DIR/complete/deployment-report.txt"
    
    cat > "$REPORT_FILE" << EOF
跨平台平板中控软件部署报告
========================================
构建时间: $(date)
构建平台: $(uname -a)
Java版本: $(java -version 2>&1 | head -1)
Maven版本: $(mvn -version 2>&1 | head -1)

构建结果:
========================================
EOF
    
    # 检查各平台构建结果
    if ls "$BUILD_DIR/jar"/*.jar 1> /dev/null 2>&1; then
        echo "✓ JAR包: $(ls "$BUILD_DIR/jar"/*.jar | wc -l) 个文件" >> "$REPORT_FILE"
    else
        echo "✗ JAR包: 未构建" >> "$REPORT_FILE"
    fi
    
    if ls "$BUILD_DIR/native-desktop"/* 1> /dev/null 2>&1; then
        echo "✓ 原生桌面应用: 已构建" >> "$REPORT_FILE"
    else
        echo "✗ 原生桌面应用: 未构建" >> "$REPORT_FILE"
    fi
    
    if ls "$BUILD_DIR/android"/*.apk 1> /dev/null 2>&1; then
        echo "✓ Android APK: $(ls "$BUILD_DIR/android"/*.apk | wc -l) 个文件" >> "$REPORT_FILE"
    else
        echo "✗ Android APK: 未构建" >> "$REPORT_FILE"
    fi
    
    if ls "$BUILD_DIR/ios"/*.ipa 1> /dev/null 2>&1; then
        echo "✓ iOS IPA: $(ls "$BUILD_DIR/ios"/*.ipa | wc -l) 个文件" >> "$REPORT_FILE"
    else
        echo "✗ iOS IPA: 未构建" >> "$REPORT_FILE"
    fi
    
    success_msg "部署报告生成完成: $REPORT_FILE"
}

# 主函数
main() {
    check_dependencies
    setup_build_dirs
    clean_project
    compile_project
    run_tests
    package_jar
    build_native_desktop
    build_mobile_apps
    
    # 复制构建结果到分发目录
    cp -r "$BUILD_DIR"/* "$DIST_DIR/complete/" 2>/dev/null || true
    
    # 复制文档
    [ -f "README.md" ] && cp "README.md" "$DIST_DIR/complete/"
    [ -f "跨平台迁移技术方案.md" ] && cp "跨平台迁移技术方案.md" "$DIST_DIR/complete/"
    
    create_run_scripts
    generate_report
    
    echo ""
    echo "========================================"
    echo -e "${GREEN}🎉 部署完成！${NC}"
    echo "========================================"
    echo "构建目录: $BUILD_DIR"
    echo "分发目录: $DIST_DIR/complete"
    echo ""
    echo "使用说明:"
    echo "1. 桌面版: 运行 run-linux.sh 或 run-windows.bat"
    echo "2. Android版: 安装 android/*.apk 文件"
    echo "3. iOS版: 通过Xcode或TestFlight安装 ios/*.ipa"
    echo ""
    echo "详细信息请查看: $DIST_DIR/complete/deployment-report.txt"
}

# 执行主函数
main "$@"
