# 网络配置和PyInstaller安装指南

## 问题描述
在某些网络环境下，pip可能无法正常连接到PyPI服务器安装PyInstaller，会出现代理连接错误。

## 解决方案

### 方案1：使用国内镜像源
```bash
# 使用清华大学镜像源
py -m pip install pyinstaller -i https://pypi.tuna.tsinghua.edu.cn/simple/

# 或使用阿里云镜像源
py -m pip install pyinstaller -i https://mirrors.aliyun.com/pypi/simple/

# 或使用豆瓣镜像源
py -m pip install pyinstaller -i https://pypi.douban.com/simple/
```

### 方案2：配置pip使用镜像源
创建或编辑pip配置文件：
- Windows: `%APPDATA%\pip\pip.ini`
- 内容：
```ini
[global]
index-url = https://pypi.tuna.tsinghua.edu.cn/simple/
trusted-host = pypi.tuna.tsinghua.edu.cn
```

### 方案3：离线安装
1. 在有网络的环境下载PyInstaller的whl文件
2. 将文件复制到目标机器
3. 使用本地安装：
```bash
py -m pip install pyinstaller-x.x.x-py3-none-any.whl
```

### 方案4：使用conda（如果已安装）
```bash
conda install pyinstaller
```

## 验证安装
安装完成后，验证PyInstaller是否正常工作：
```bash
py -m PyInstaller --version
```

## 打包应用程序
安装成功后，可以使用以下命令打包：
```bash
# 简单打包
py -m PyInstaller --onefile --windowed main.py

# 或运行我们提供的打包脚本
.\build.bat
```

## 注意事项
- 确保Python版本为3.7或更高
- 如果仍有网络问题，请联系网络管理员配置代理设置
- 生成的exe文件位于`dist`目录下