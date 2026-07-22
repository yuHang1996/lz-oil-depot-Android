# 兰州油库环境应急监控 Android App

兰州原油库、成品油库数字化监控与污染源扩散预测移动端工程。项目以投标技术方案为需求基线，覆盖监控态势、GIS 点位、扩散预测、敏感点预警、手机通知和应急措施反馈。

## 当前基线

- 包名：`edu.lzjtu.environment.oildepot`
- 语言与界面：Kotlin、Jetpack Compose、Material 3
- 最低系统：Android 8.0（API 26）
- 编译与目标版本：Android API 36
- 构建环境：JDK 17、Gradle 8.13、Android Gradle Plugin 8.13.2
- 构建变体：`demo` 使用模拟数据，`production` 预留真实服务

## 已实现

- 原油库与成品油库切换
- 监测点在线率、污染物浓度和活动预警态势
- 污染源、大气点、地下水井、土壤点和敏感点示意布局
- 突发污染情景参数配置、校验、模型任务与结果展示
- 分级预警确认、系统通知演示和深链接入口
- 应急措施责任组、时限、优先级和完成反馈
- 参数校验与预警分级单元测试

## 本地构建

```bash
./gradlew testDemoDebugUnitTest
./gradlew assembleDemoDebug
```

安装包生成在 `app/build/outputs/apk/demo/debug/`。生产构建在真实接口、签名材料和安全配置完成后启用。

- [Demo 安装包](app/build/outputs/apk/demo/debug/app-demo-debug.apk)
- [基线验证报告](docs/verification/verification-report.md)
- [API 36 监控态势截图](docs/verification/overview-api36.png)
- [API 36 扩散预测截图](docs/verification/prediction-api36.png)

## 重要边界

Android App 不直接连接生产数据库或监测设备。监测数据、模型任务、预警、权限和日志统一通过服务端接口接入。当前模型结果与地图坐标均为流程联调数据，不用于现场环境判断。

详细说明见：

- [架构说明](docs/architecture.md)
- [接口契约](docs/api-contracts.md)
- [安全与责任边界](docs/security-boundaries.md)
- [开发路线图](docs/development-roadmap.md)
- [需求追踪矩阵](docs/requirements-traceability.md)
- [投标版高清设计稿总览](docs/design-reference/投标版设计稿总览.png)
