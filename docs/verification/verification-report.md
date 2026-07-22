# 基线验证报告

## 验证结论

兰州油库环境应急监控 Android App 第一阶段专属工程已完成构建与模拟器验证，可作为后续真实数据接口、GIS 服务和污染扩散模型接入的开发基线。

## 验证环境

- 验证日期：2026-07-22
- 构建变体：`demoDebug`
- 模拟设备：`LanzhouOilDepot_API36`
- Android 版本：API 36
- 设备分辨率：1080 × 2340
- 运行时：Android Studio JBR 21
- 构建字节码目标：JDK 17

## 自动化验证

- 单元测试：7 项通过，0 项失败
- Compose 界面测试：1 项通过，0 项失败
- Android Lint：0 个错误；8 个提醒均为依赖或工具版本更新提示
- 完整验证命令：`./gradlew connectedDemoDebugAndroidTest testDemoDebugUnitTest assembleDemoDebug lintDemoDebug`

## 安装包

- 文件：`app/build/outputs/apk/demo/debug/app-demo-debug.apk`
- SHA-256：`4ce809c5635463a1a76b94e40639a1369978e6f2ab1029011539657a9c3e68ce`

## 界面核验

- [监控态势首屏](overview-api36.png)
- [污染扩散预测页](prediction-api36.png)

已在 API 36 模拟设备上核验页面启动、底部导航、预测流程入口、中文文字显示及主要控件布局，未发现遮挡或明显错位。

## 适用边界

当前版本是可运行的第一阶段工程基线。`demo` 变体使用流程演示数据；`production` 变体不会回退到模拟数据，并在真实服务未配置时明确显示不可用状态。正式现场应用仍需接入建设单位提供的监测数据接口、GIS 服务、模型计算服务、预警规则、账号权限、推送证书及部署环境。污染扩散模型的技术路线、参数体系、适用条件和校验基准由环境专业技术团队确认。
