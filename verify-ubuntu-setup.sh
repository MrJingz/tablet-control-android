#!/bin/bash

echo "========================================"
echo "Ubuntu环境项目验证脚本"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 计数器
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# 检查函数
check_item() {
    local description="$1"
    local command="$2"
    local expected_result="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    echo -n "检查 $description... "
    
    if eval "$command" &> /dev/null; then
        echo -e "${GREEN}✓ 通过${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}✗ 失败${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        if [ -n "$expected_result" ]; then
            echo -e "${YELLOW}  预期: $expected_result${NC}"
        fi
        return 1
    fi
}

# 详细检查函数
detailed_check() {
    local description="$1"
    local command="$2"
    
    echo -e "${BLUE}=== $description ===${NC}"
    eval "$command"
    echo ""
}

echo "开始验证Ubuntu开发环境..."
echo ""

# 1. 系统环境检查
echo -e "${BLUE}[1] 系统环境检查${NC}"
check_item "WSL环境" "grep -q Microsoft /proc/version"
check_item "Ubuntu版本" "lsb_release -a"
detailed_check "系统信息" "uname -a && lsb_release -a"

# 2. Java环境检查
echo -e "${BLUE}[2] Java环境检查${NC}"
check_item "Java安装" "command -v java"
check_item "Java版本(11+)" "java -version 2>&1 | grep -E '(11|17|21)'"
check_item "JAVA_HOME设置" "[ -n \"\$JAVA_HOME\" ]"
detailed_check "Java详细信息" "java -version && echo \"JAVA_HOME: \$JAVA_HOME\""

# 3. Maven环境检查
echo -e "${BLUE}[3] Maven环境检查${NC}"
check_item "Maven安装" "command -v mvn"
check_item "Maven版本" "mvn -version"
detailed_check "Maven详细信息" "mvn -version"

# 4. GraalVM环境检查
echo -e "${BLUE}[4] GraalVM环境检查${NC}"
check_item "GraalVM安装" "[ -d \"/opt/graalvm\" ]"
check_item "GraalVM Java" "[ -f \"/opt/graalvm/bin/java\" ]"
check_item "native-image" "command -v native-image"
check_item "GRAALVM_HOME设置" "[ -n \"\$GRAALVM_HOME\" ]"
detailed_check "GraalVM详细信息" "/opt/graalvm/bin/java -version && native-image --version"

# 5. Android SDK检查
echo -e "${BLUE}[5] Android SDK环境检查${NC}"
check_item "Android SDK目录" "[ -d \"\$HOME/android-sdk\" ]"
check_item "cmdline-tools" "[ -d \"\$HOME/android-sdk/cmdline-tools/latest\" ]"
check_item "platform-tools" "[ -d \"\$HOME/android-sdk/platform-tools\" ]"
check_item "sdkmanager" "command -v sdkmanager"
check_item "ANDROID_HOME设置" "[ -n \"\$ANDROID_HOME\" ]"
detailed_check "Android SDK详细信息" "echo \"ANDROID_HOME: \$ANDROID_HOME\" && ls -la \$HOME/android-sdk/ 2>/dev/null || echo 'Android SDK目录不存在'"

# 6. 项目结构检查
echo -e "${BLUE}[6] 项目结构检查${NC}"
check_item "项目目录存在" "[ -d \"\$HOME/projects\" ]"
check_item "gluon-crossplatform项目" "[ -d \"\$HOME/projects/gluon-crossplatform\" ]"
check_item "pom.xml文件" "[ -f \"\$HOME/projects/gluon-crossplatform/pom.xml\" ]"
check_item "src目录" "[ -d \"\$HOME/projects/gluon-crossplatform/src\" ]"
detailed_check "项目文件结构" "ls -la \$HOME/projects/gluon-crossplatform/ 2>/dev/null || echo '项目目录不存在'"

# 7. 环境变量检查
echo -e "${BLUE}[7] 环境变量检查${NC}"
echo "当前环境变量:"
echo "JAVA_HOME: $JAVA_HOME"
echo "GRAALVM_HOME: $GRAALVM_HOME"
echo "ANDROID_HOME: $ANDROID_HOME"
echo "PATH: $PATH"
echo ""

# 8. 网络连接检查
echo -e "${BLUE}[8] 网络连接检查${NC}"
check_item "互联网连接" "ping -c 1 google.com"
check_item "Maven仓库连接" "curl -s --head https://repo.maven.apache.org/maven2/ | head -1"
check_item "Android SDK连接" "curl -s --head https://dl.google.com/android/repository/ | head -1"

# 9. 构建测试
echo -e "${BLUE}[9] 构建测试${NC}"
if [ -d "$HOME/projects/gluon-crossplatform" ]; then
    cd "$HOME/projects/gluon-crossplatform"
    check_item "Maven依赖解析" "mvn dependency:resolve -q"
    check_item "项目编译" "mvn compile -q"
else
    echo -e "${YELLOW}跳过构建测试 - 项目目录不存在${NC}"
fi

# 10. 生成验证报告
echo ""
echo "========================================"
echo "验证结果汇总"
echo "========================================"
echo "总检查项: $TOTAL_CHECKS"
echo -e "通过: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "失败: ${RED}$FAILED_CHECKS${NC}"

if [ $FAILED_CHECKS -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 所有检查通过！Ubuntu环境配置完成${NC}"
    echo ""
    echo "下一步操作:"
    echo "1. cd ~/projects/gluon-crossplatform"
    echo "2. ./build-android-wsl.sh  # 构建Android APK"
    echo "3. ./deploy-crossplatform.sh  # 跨平台构建"
    
    # 创建快捷脚本
    cat > "$HOME/build-android.sh" << 'EOF'
#!/bin/bash
cd ~/projects/gluon-crossplatform
./build-android-wsl.sh
EOF
    chmod +x "$HOME/build-android.sh"
    echo ""
    echo "快捷命令已创建:"
    echo "~/build-android.sh  # 快速构建Android APK"
    
else
    echo ""
    echo -e "${YELLOW}⚠ 发现 $FAILED_CHECKS 个问题，请根据上面的检查结果进行修复${NC}"
    echo ""
    echo "常见问题解决方案:"
    echo "1. 重新运行环境配置: ./setup-ubuntu-android.sh"
    echo "2. 重新加载环境变量: source ~/.bashrc"
    echo "3. 检查网络连接和防火墙设置"
    echo "4. 确保项目文件已正确复制到Ubuntu"
fi

echo ""
echo "验证完成！"

# 保存验证报告
REPORT_FILE="$HOME/ubuntu-verification-report-$(date +%Y%m%d-%H%M%S).txt"
{
    echo "Ubuntu环境验证报告"
    echo "===================="
    echo "验证时间: $(date)"
    echo "系统信息: $(uname -a)"
    echo "Ubuntu版本: $(lsb_release -d | cut -f2)"
    echo ""
    echo "验证结果:"
    echo "总检查项: $TOTAL_CHECKS"
    echo "通过: $PASSED_CHECKS"
    echo "失败: $FAILED_CHECKS"
    echo ""
    echo "环境变量:"
    echo "JAVA_HOME: $JAVA_HOME"
    echo "GRAALVM_HOME: $GRAALVM_HOME"
    echo "ANDROID_HOME: $ANDROID_HOME"
} > "$REPORT_FILE"

echo "验证报告已保存到: $REPORT_FILE"
