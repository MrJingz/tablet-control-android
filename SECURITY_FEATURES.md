# 安全验证功能说明

## 功能概述

本应用程序已集成安全验证功能，在主界面左下角添加了"安全验证"按钮，提供密码保护的访问控制。

## 功能特性

### 1. 安全验证按钮
- **位置**: 固定在主窗口左下角
- **样式**: 符合整体UI设计规范的ttk按钮
- **功能**: 点击后弹出安全验证模态对话框

### 2. 模态验证对话框
- **标题**: "安全验证"
- **组件**: 
  - 密码输入框（password类型，显示为*号）
  - "确认"和"取消"按钮
  - 错误提示区域
- **交互**: 支持回车键确认，ESC键取消

### 3. 密码安全机制
- **默认密码**: "123"
- **加密方式**: SHA-256哈希算法
- **存储位置**: `security_config.py`文件中
- **哈希值**: `a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3`

### 4. 验证流程

#### 验证成功时:
- 重置失败尝试次数
- 销毁模态对话框DOM元素
- 显示成功提示消息
- 在默认浏览器中打开`about:blank`页面
- 清除所有相关临时数据

#### 验证失败时:
- 保持对话框开启状态
- 在密码框下方显示红色错误提示
- 自动清空已输入密码
- 记录失败尝试次数
- 显示剩余尝试机会

### 5. 安全保护机制
- **最大尝试次数**: 3次
- **锁定机制**: 超过最大尝试次数后拒绝访问
- **密码加密**: 使用SHA-256算法，不存储明文密码
- **会话管理**: 验证成功后重置尝试计数器

## 技术实现

### 文件结构
```
├── main.py                 # 主应用程序文件
├── security_config.py      # 安全配置和密码管理
└── SECURITY_FEATURES.md    # 本说明文档
```

### 核心类和方法

#### SecurityConfig类
- `hash_password()`: 密码SHA-256加密
- `verify_password()`: 密码验证
- `increment_attempts()`: 增加尝试次数
- `reset_attempts()`: 重置尝试次数
- `is_locked()`: 检查锁定状态

#### HelloWorldApp类新增方法
- `create_security_button()`: 创建安全验证按钮
- `show_security_dialog()`: 显示验证对话框
- `create_dialog_content()`: 创建对话框内容
- `verify_password()`: 执行密码验证
- `on_verification_success()`: 处理验证成功

## 使用说明

1. **启动应用程序**:
   ```bash
   py main.py
   ```

2. **进行安全验证**:
   - 点击左下角"安全验证"按钮
   - 在弹出对话框中输入密码"123"
   - 点击"确认"或按回车键

3. **验证结果**:
   - 成功：显示成功消息并跳转到浏览器
   - 失败：显示错误提示，可重新尝试
   - 锁定：超过3次失败后拒绝访问

## 安全注意事项

1. **密码修改**: 如需修改默认密码，请更新`security_config.py`中的`password_hash`值
2. **哈希计算**: 使用Python的hashlib库计算新密码的SHA-256值
3. **文件保护**: 确保`security_config.py`文件的访问权限设置合理
4. **网络安全**: 在生产环境中建议使用HTTPS协议

## 示例：修改默认密码

```python
import hashlib

# 计算新密码的哈希值
new_password = "your_new_password"
hash_value = hashlib.sha256(new_password.encode('utf-8')).hexdigest()
print(f"新密码的哈希值: {hash_value}")

# 将此哈希值更新到security_config.py中的password_hash变量
```

## 版本信息

- **版本**: 1.1.0
- **更新日期**: 2024年1月
- **新增功能**: 安全验证模块
- **兼容性**: Windows 10及以上版本