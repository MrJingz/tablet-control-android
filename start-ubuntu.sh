#!/bin/bash

echo "========================================"
echo "Ubuntu平板中控软件启动器"
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

# 检测环境
detect_environment() {
    echo "检测运行环境..."
    
    # 检测是否在WSL中
    if grep -q Microsoft /proc/version 2>/dev/null; then
        echo "检测到WSL环境"
        IS_WSL=true
    else
        echo "检测到原生Linux环境"
        IS_WSL=false
    fi
    
    # 检测桌面环境
    if [ -n "$XDG_CURRENT_DESKTOP" ]; then
        DESKTOP_ENV="$XDG_CURRENT_DESKTOP"
    elif [ -n "$DESKTOP_SESSION" ]; then
        DESKTOP_ENV="$DESKTOP_SESSION"
    else
        DESKTOP_ENV="unknown"
    fi
    
    echo "桌面环境: $DESKTOP_ENV"
    
    # 检测发行版
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO="$NAME"
        echo "发行版: $DISTRO"
    fi
}

# 检查依赖
check_dependencies() {
    echo ""
    echo "检查运行依赖..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        handle_error "Java未安装，请先安装Java 11+"
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    success_msg "Java版本: $JAVA_VERSION"
    
    # 检查显示环境
    if [ -z "$DISPLAY" ] && [ "$IS_WSL" = false ]; then
        warning_msg "DISPLAY环境变量未设置，可能无法显示GUI"
    fi
    
    # WSL特殊检查
    if [ "$IS_WSL" = true ]; then
        info_msg "WSL环境检测到，建议使用X11转发或VcXsrv"
        
        # 检查是否有Windows的X服务器
        if [ -z "$DISPLAY" ]; then
            export DISPLAY=:0
            warning_msg "设置DISPLAY=:0，请确保Windows上运行了X服务器"
        fi
    fi
}

# 查找项目
find_project() {
    echo ""
    echo "查找项目文件..."
    
    # 可能的项目位置
    POSSIBLE_PATHS=(
        "$(pwd)/gluon-crossplatform"
        "$(pwd)/java-version"
        "$HOME/projects/gluon-crossplatform"
        "$HOME/projects/java-version"
        "$(pwd)"
    )
    
    for path in "${POSSIBLE_PATHS[@]}"; do
        if [ -f "$path/pom.xml" ]; then
            PROJECT_PATH="$path"
            success_msg "找到项目: $PROJECT_PATH"
            return 0
        fi
    done
    
    handle_error "未找到项目文件，请确保在正确的目录运行此脚本"
}

# 选择启动方式
choose_startup_method() {
    echo ""
    echo "选择启动方式:"
    echo "1) 运行已编译的JAR包 (推荐)"
    echo "2) 使用Maven运行"
    echo "3) 运行原生应用 (如果已构建)"
    echo "4) 跨平台启动器"
    echo ""
    
    read -p "请选择 (1-4): " choice
    
    case $choice in
        1) run_jar ;;
        2) run_maven ;;
        3) run_native ;;
        4) run_crossplatform ;;
        *) 
            warning_msg "无效选择，使用默认方式 (JAR)"
            run_jar
            ;;
    esac
}

# 运行JAR包
run_jar() {
    echo ""
    info_msg "查找JAR文件..."
    
    cd "$PROJECT_PATH"
    
    # 查找JAR文件
    JAR_FILE=""
    if [ -f "target/*.jar" ]; then
        JAR_FILE=$(ls target/*.jar | head -1)
    elif [ -f "*.jar" ]; then
        JAR_FILE=$(ls *.jar | head -1)
    fi
    
    if [ -z "$JAR_FILE" ]; then
        warning_msg "未找到JAR文件，尝试编译..."
        mvn clean package -DskipTests
        
        if [ -f "target/*.jar" ]; then
            JAR_FILE=$(ls target/*.jar | head -1)
        else
            handle_error "编译失败，无法生成JAR文件"
        fi
    fi
    
    success_msg "找到JAR文件: $JAR_FILE"
    
    echo ""
    info_msg "启动应用程序..."
    
    # 设置JVM参数
    JVM_ARGS="-Xmx1g"
    
    # WSL特殊设置
    if [ "$IS_WSL" = true ]; then
        JVM_ARGS="$JVM_ARGS -Djava.awt.headless=false"
    fi
    
    # 启动应用
    java $JVM_ARGS -jar "$JAR_FILE"
}

# 使用Maven运行
run_maven() {
    echo ""
    info_msg "使用Maven运行应用..."
    
    cd "$PROJECT_PATH"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        handle_error "Maven未安装"
    fi
    
    # 运行应用
    mvn javafx:run
}

# 运行原生应用
run_native() {
    echo ""
    info_msg "查找原生应用..."
    
    cd "$PROJECT_PATH"
    
    # 查找原生可执行文件
    NATIVE_APP=""
    if [ -f "target/gluonfx/TabletControl" ]; then
        NATIVE_APP="target/gluonfx/TabletControl"
    elif [ -f "TabletControl" ]; then
        NATIVE_APP="TabletControl"
    fi
    
    if [ -z "$NATIVE_APP" ]; then
        warning_msg "未找到原生应用，尝试构建..."
        mvn gluonfx:build
        
        if [ -f "target/gluonfx/TabletControl" ]; then
            NATIVE_APP="target/gluonfx/TabletControl"
        else
            handle_error "原生应用构建失败"
        fi
    fi
    
    success_msg "找到原生应用: $NATIVE_APP"
    
    # 设置执行权限
    chmod +x "$NATIVE_APP"
    
    echo ""
    info_msg "启动原生应用..."
    "./$NATIVE_APP"
}

# 运行跨平台启动器
run_crossplatform() {
    echo ""
    info_msg "使用跨平台启动器..."
    
    cd "$PROJECT_PATH"
    
    # 查找跨平台启动器
    if [ -f "src/main/java/com/feixiang/tabletcontrol/CrossPlatformLauncher.java" ]; then
        mvn exec:java -Dexec.mainClass="com.feixiang.tabletcontrol.CrossPlatformLauncher"
    elif [ -f "src/main/java/com/feixiang/tabletcontrol/platform/CrossPlatformBootstrap.java" ]; then
        mvn exec:java -Dexec.mainClass="com.feixiang.tabletcontrol.platform.CrossPlatformBootstrap"
    else
        warning_msg "未找到跨平台启动器，回退到JAR方式"
        run_jar
    fi
}

# 创建桌面快捷方式
create_desktop_shortcut() {
    echo ""
    read -p "是否创建桌面快捷方式? (y/n): " create_shortcut
    
    if [ "$create_shortcut" = "y" ] || [ "$create_shortcut" = "Y" ]; then
        DESKTOP_DIR="$HOME/Desktop"
        if [ ! -d "$DESKTOP_DIR" ]; then
            DESKTOP_DIR="$HOME/桌面"
        fi
        
        if [ -d "$DESKTOP_DIR" ]; then
            SHORTCUT_FILE="$DESKTOP_DIR/TabletControl.desktop"
            
            cat > "$SHORTCUT_FILE" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=平板中控软件
Comment=跨平台平板中控软件
Exec=$PROJECT_PATH/start-ubuntu.sh
Icon=$PROJECT_PATH/icon.png
Terminal=false
Categories=Utility;Development;
EOF
            
            chmod +x "$SHORTCUT_FILE"
            success_msg "桌面快捷方式创建成功: $SHORTCUT_FILE"
        else
            warning_msg "未找到桌面目录，跳过快捷方式创建"
        fi
    fi
}

# 主函数
main() {
    detect_environment
    check_dependencies
    find_project
    choose_startup_method
    
    echo ""
    echo "========================================"
    echo -e "${GREEN}应用程序已退出${NC}"
    echo "========================================"
    
    create_desktop_shortcut
    
    echo ""
    echo "感谢使用平板中控软件！"
}

# 执行主函数
main "$@"
