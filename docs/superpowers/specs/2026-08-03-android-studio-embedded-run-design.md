# Android Studio 内嵌运行设计

## 目标

让 `LanzhouOilDepotMonitor` 在当前 Android Studio 窗口的 `Running Devices` 工具窗中运行，代码修改后可通过 Android Studio 的 Run 或 Apply Changes 直接查看效果，不再额外打开独立展示窗口。

## 当前状态与根因

- 当前打开的是仓库根 Gradle 项目，`:app` 模块已被正确识别。
- API 36 模拟器 `LanzhouOilDepot_API36` 已在线。
- Android Studio 的 `Running Devices` 工具窗已经启用。
- 当前默认运行配置误选为 `androidTest`，不是应用配置 `app`。
- 项目采用 Jetpack Compose，但本次范围不引入 Compose Preview；完整 App 通过内嵌模拟器展示。

## 实施方案

1. 使用仓库根目录作为唯一 Android Studio 项目入口，不单独打开 `app/` 子目录。
2. 将当前 IDE 默认运行配置切换为 Android App 的 `app` 配置。
3. 选择 `demoDebug` 构建变体，以项目内置模拟数据运行完整功能。
4. 运行 Gradle 构建以复现并修复构建错误；修复仅针对已确认的根因，不改无关业务代码。
5. 安装并启动 `edu.lzjtu.environment.oildepot.demo` 到当前 API 36 模拟器。

## 验证标准

- `./gradlew testDemoDebugUnitTest assembleDemoDebug` 成功。
- APK 成功安装到当前在线的 API 36 模拟器。
- App 主 Activity 成功启动。
- Android Studio 当前项目仍使用根目录，默认运行配置为 `app`。
- `Running Devices` 工具窗保持启用，运行效果显示在当前 Studio 窗口内。

## 约束与回退

- 保留用户已有的 `README.md`、`settings.gradle.kts`、`DomainModels.kt` 及设计资料改动。
- 不删除 `app/.idea` 等历史目录；如果确认其影响当前根项目，再单独提出清理建议。
- IDE 工作区配置属于本机状态，不作为产品源码提交内容。
