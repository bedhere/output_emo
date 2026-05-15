# 情绪颜文字输入法

基于 JavaFX 的 Windows 桌面输入法应用，可根据用户输入的文字自动检测情绪并推荐对应的颜文字。

## 功能特性

- **全局键盘监听**：使用 JNA 调用 Windows API，实时捕获用户输入
- **情绪识别**：支持 7 种情绪检测（开心、难过、生气、惊讶、害羞、困倦、疑问）
- **智能推荐**：每种情绪对应多个颜文字，随机推荐 3 个供选择
- **剪贴板复制**：一键复制选中的颜文字到剪贴板
- **系统托盘**：最小化到系统托盘，双击恢复显示
- **快捷键支持**：Ctrl+E 全局清除输入

## 技术栈

- **Java 11+**
- **JavaFX 17.0.2**：UI 框架
- **JNA 5.13.0**：调用 Windows 原生 API
- **Maven**：项目构建

## 项目结构

```
src/main/java/com/emotionime/
├── Main.java                          # 应用入口
├── ui/
│   └── AppUI.java                     # 主界面 UI
├── service/
│   ├── EmotionService.java            # 情绪检测服务
│   └── EmojiService.java              # 颜文字服务
├── repository/
│   └── EmojiRepository.java           # 颜文字数据仓库
└── util/
    ├── GlobalKeyListener.java         # 全局热键监听
    └── WindowsGlobalKeyboardListener.java  # Windows 键盘钩子
```

## 运行方式

### 方式 1：Maven 命令（推荐）

```bash
mvn clean javafx:run
```

### 方式 2：运行脚本

```bash
# 双击 run.bat 或在终端执行
run.bat
```

### 方式 3：IDEA 运行

1. 打开 IDEA 右侧 Maven 工具窗口
2. 展开 Plugins > javafx
3. 双击 javafx:run

### 方式 4：打包 JAR

```bash
mvn clean package
java -jar target/output_emo.jar
```

## 使用说明

1. 启动后在屏幕右下角显示输入法窗口
2. 在任意应用中输入文字，输入法会自动检测情绪
3. 点击颜文字按钮或按数字键 1/2/3 选择颜文字
4. 选中的颜文字会自动复制到剪贴板
5. 按 Enter 清除输入，按 Esc 隐藏窗口

## 快捷键

- **1/2/3**：选择对应位置的颜文字
- **Enter**：清除输入框
- **Esc**：隐藏窗口
- **Ctrl+E**：全局清除输入

## 注意事项

- 仅支持 Windows 系统（依赖 Win32 API）
- 需要 Java 11 或更高版本
- 全局键盘监听需要管理员权限
