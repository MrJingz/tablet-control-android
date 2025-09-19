# 跨平台适配指南

## 🌐 平台特定配置

### Windows 平台

#### 路径配置
```json
{
  "platform": "windows",
  "paths": {
    "primary": "%USERPROFILE%\\Documents\\TabletControl\\USERDATA",
    "fallback": [
      "%APPDATA%\\TabletControl\\USERDATA",
      ".\\USERDATA"
    ]
  },
  "permissions": {
    "fileSystem": "full",
    "registry": false
  },
  "features": {
    "autoReload": true,
    "fileWatcher": true,
    "encryption": true
  }
}
```

#### 部署说明
1. **安装包配置**：
   - 使用NSIS或Inno Setup创建安装程序
   - 自动创建用户文档目录下的USERDATA文件夹
   - 注册文件关联（.userdata扩展名）

2. **权限要求**：
   - 用户文档目录读写权限
   - 临时文件夹访问权限
   - 网络访问权限（如需要）

### Android 平台

#### 路径配置
```json
{
  "platform": "android",
  "paths": {
    "internal": "/data/data/com.feixiang.tabletcontrol/files/USERDATA",
    "external": "/storage/emulated/0/TabletControl/USERDATA",
    "cache": "/data/data/com.feixiang.tabletcontrol/cache"
  },
  "permissions": [
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_EXTERNAL_STORAGE"
  ],
  "features": {
    "autoReload": true,
    "fileWatcher": false,
    "encryption": true
  }
}
```

#### AndroidManifest.xml 配置
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<application
    android:requestLegacyExternalStorage="true"
    android:preserveLegacyExternalStorage="true">
    
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="com.feixiang.tabletcontrol.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>
```

#### 文件导入/导出
```java
// Android特定的文件操作
public class AndroidFileManager {
    public void importUserData(Uri sourceUri) {
        // 使用DocumentFile API导入USERDATA
    }
    
    public void exportUserData(Uri targetUri) {
        // 导出USERDATA到用户选择的位置
    }
}
```

### iOS 平台

#### 路径配置
```json
{
  "platform": "ios",
  "paths": {
    "documents": "Documents/USERDATA",
    "library": "Library/Application Support/TabletControl/USERDATA",
    "shared": "Shared/USERDATA"
  },
  "capabilities": [
    "com.apple.security.files.user-selected.read-write",
    "com.apple.security.files.downloads.read-write"
  ],
  "features": {
    "autoReload": false,
    "fileWatcher": false,
    "encryption": true,
    "iCloudSync": true
  }
}
```

#### Info.plist 配置
```xml
<key>UIFileSharingEnabled</key>
<true/>
<key>LSSupportsOpeningDocumentsInPlace</key>
<true/>
<key>UTExportedTypeDeclarations</key>
<array>
    <dict>
        <key>UTTypeIdentifier</key>
        <string>com.feixiang.tabletcontrol.userdata</string>
        <key>UTTypeDescription</key>
        <string>TabletControl Project Package</string>
        <key>UTTypeConformsTo</key>
        <array>
            <string>public.folder</string>
        </array>
        <key>UTTypeTagSpecification</key>
        <dict>
            <key>public.filename-extension</key>
            <array>
                <string>userdata</string>
            </array>
        </dict>
    </dict>
</array>
```

#### Swift文件操作
```swift
// iOS特定的文件操作
class IOSFileManager {
    func importUserData(from url: URL) {
        // 使用Document Picker导入USERDATA
    }
    
    func exportUserData(to url: URL) {
        // 导出USERDATA到Files应用
    }
    
    func enableiCloudSync() {
        // 启用iCloud同步
    }
}
```

### HarmonyOS 平台

#### 路径配置
```json
{
  "platform": "harmonyos",
  "paths": {
    "internal": "/data/storage/el2/base/haps/entry/files/USERDATA",
    "external": "/storage/media/100/local/files/TabletControl/USERDATA",
    "distributed": "/data/storage/el2/distributedfiles/USERDATA"
  },
  "permissions": [
    "ohos.permission.READ_MEDIA",
    "ohos.permission.WRITE_MEDIA",
    "ohos.permission.DISTRIBUTED_DATASYNC"
  ],
  "features": {
    "autoReload": true,
    "fileWatcher": true,
    "encryption": true,
    "distributedSync": true
  }
}
```

#### config.json 配置
```json
{
  "module": {
    "reqPermissions": [
      {
        "name": "ohos.permission.READ_MEDIA",
        "reason": "访问媒体文件"
      },
      {
        "name": "ohos.permission.WRITE_MEDIA",
        "reason": "保存项目数据"
      },
      {
        "name": "ohos.permission.DISTRIBUTED_DATASYNC",
        "reason": "跨设备同步"
      }
    ]
  }
}
```

#### ArkTS文件操作
```typescript
// HarmonyOS特定的文件操作
class HarmonyFileManager {
  async importUserData(uri: string): Promise<boolean> {
    // 使用picker API导入USERDATA
  }
  
  async exportUserData(uri: string): Promise<boolean> {
    // 导出USERDATA
  }
  
  async enableDistributedSync(): Promise<void> {
    // 启用分布式同步
  }
}
```

## 🔄 跨平台兼容性处理

### 文件路径标准化
```java
public class PathNormalizer {
    public static String normalize(String path) {
        // 统一使用正斜杠
        return path.replace('\\', '/');
    }
    
    public static String toPlatformPath(String normalizedPath) {
        // 转换为平台特定路径
        return normalizedPath.replace('/', File.separatorChar);
    }
}
```

### 文件名兼容性
```java
public class FileNameValidator {
    private static final String INVALID_CHARS = "<>:\"|?*";
    
    public static String sanitizeFileName(String fileName) {
        // 移除或替换不兼容字符
        for (char c : INVALID_CHARS.toCharArray()) {
            fileName = fileName.replace(c, '_');
        }
        return fileName;
    }
}
```

### 权限管理
```java
public class PermissionManager {
    public static boolean requestPermissions(Platform platform) {
        switch (platform) {
            case ANDROID:
                return requestAndroidPermissions();
            case IOS:
                return requestIOSPermissions();
            case HARMONYOS:
                return requestHarmonyPermissions();
            default:
                return true;
        }
    }
}
```

## 📱 平台特定功能

### 文件分享
- **Windows**: 文件资源管理器集成
- **Android**: Intent分享，支持其他应用导入
- **iOS**: Files应用集成，AirDrop支持
- **HarmonyOS**: 分布式文件系统，跨设备同步

### 备份策略
- **Windows**: 本地备份 + 云存储同步
- **Android**: Google Drive集成
- **iOS**: iCloud自动备份
- **HarmonyOS**: 华为云空间同步

### 安全特性
- **所有平台**: AES-256加密
- **Android**: Android Keystore
- **iOS**: Keychain Services
- **HarmonyOS**: HUKS (HarmonyOS Universal KeyStore)

## 🛠️ 开发工具

### 跨平台构建
```bash
# Windows
./gradlew buildWindows

# Android
./gradlew assembleRelease

# iOS (需要macOS)
xcodebuild -project TabletControl.xcodeproj

# HarmonyOS
hvigor assembleHap
```

### 测试脚本
```bash
# 跨平台兼容性测试
./scripts/test-cross-platform.sh

# USERDATA结构验证
./scripts/validate-userdata.sh

# 文件替换测试
./scripts/test-package-replacement.sh
```
