# USERDATA 跨平台标准目录结构设计

## 📁 标准目录结构

```
USERDATA/
├── .metadata                    # 项目元数据文件
├── config/                      # 配置文件目录
│   ├── project.json            # 项目主配置
│   ├── settings.json           # 应用设置
│   ├── layout.json             # 界面布局配置
│   ├── auth.json               # 认证配置
│   └── platform.json           # 平台特定配置
├── data/                        # 数据文件目录
│   ├── database/               # 数据库文件
│   │   ├── main.db            # 主数据库
│   │   └── cache.db           # 缓存数据库
│   ├── logs/                   # 日志文件
│   │   ├── app.log            # 应用日志
│   │   ├── error.log          # 错误日志
│   │   └── access.log         # 访问日志
│   └── temp/                   # 临时文件
├── media/                       # 媒体资源目录
│   ├── images/                 # 图片资源
│   │   ├── backgrounds/       # 背景图片
│   │   ├── icons/             # 图标文件
│   │   ├── ui/                # UI元素图片
│   │   └── content/           # 内容图片
│   ├── videos/                 # 视频资源
│   │   ├── backgrounds/       # 背景视频
│   │   └── content/           # 内容视频
│   ├── audio/                  # 音频资源
│   │   ├── effects/           # 音效文件
│   │   └── content/           # 内容音频
│   └── fonts/                  # 字体文件
├── themes/                      # 主题资源目录
│   ├── default/               # 默认主题
│   ├── dark/                  # 深色主题
│   └── custom/                # 自定义主题
├── plugins/                     # 插件目录
│   ├── extensions/            # 扩展插件
│   └── scripts/               # 脚本文件
├── backup/                      # 备份目录
│   ├── auto/                  # 自动备份
│   └── manual/                # 手动备份
└── cache/                       # 缓存目录
    ├── thumbnails/            # 缩略图缓存
    ├── processed/             # 处理后的文件
    └── network/               # 网络缓存
```

## 🔧 平台路径适配

### Windows 平台
- **标准路径**: `%USERPROFILE%\Documents\TabletControl\USERDATA`
- **备选路径**: `软件安装目录\USERDATA`
- **权限要求**: 读写权限，支持文件锁定检测

### Android 平台
- **内部存储**: `/Android/data/com.feixiang.tabletcontrol/files/USERDATA`
- **外部存储**: `/storage/emulated/0/TabletControl/USERDATA`
- **权限要求**: WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE

### iOS 平台
- **应用沙盒**: `Documents/USERDATA`
- **共享目录**: `Shared/USERDATA` (支持文件应用访问)
- **权限要求**: 文件访问权限，支持iTunes文件共享

### HarmonyOS 平台
- **应用目录**: `/data/storage/el2/base/haps/entry/files/USERDATA`
- **外部存储**: `/storage/media/100/local/files/TabletControl/USERDATA`
- **权限要求**: ohos.permission.READ_MEDIA, ohos.permission.WRITE_MEDIA

## 📋 文件格式规范

### 配置文件格式
- **编码**: UTF-8
- **格式**: JSON
- **压缩**: 可选GZIP压缩
- **加密**: AES-256-CBC（敏感配置）

### 媒体文件格式
- **图片**: PNG, JPG, WEBP, SVG
- **视频**: MP4, WEBM, MOV
- **音频**: MP3, AAC, OGG, WAV
- **字体**: TTF, OTF, WOFF2

### 元数据文件格式
```json
{
  "version": "1.0.0",
  "platform": "cross-platform",
  "created": "2024-01-01T00:00:00Z",
  "modified": "2024-01-01T00:00:00Z",
  "compatibility": {
    "minVersion": "1.0.0",
    "platforms": ["windows", "android", "ios", "harmonyos"]
  },
  "encryption": {
    "enabled": false,
    "algorithm": "AES-256-CBC",
    "files": ["config/auth.json", "config/settings.json"]
  },
  "integrity": {
    "checksum": "sha256:...",
    "files": {
      "config/project.json": "sha256:...",
      "media/images/logo.png": "sha256:..."
    }
  }
}
```

## 🔄 版本兼容性

### 向后兼容策略
1. **版本检测**: 启动时检查.metadata版本
2. **自动升级**: 低版本自动升级到当前版本
3. **降级保护**: 高版本提示不兼容
4. **备份机制**: 升级前自动备份

### 平台差异处理
1. **路径分隔符**: 统一使用'/'，运行时转换
2. **文件名限制**: 避免平台特殊字符
3. **大小写敏感**: 统一使用小写文件名
4. **文件权限**: 各平台适配相应权限模型
