# 兰州油库环境应急监控 Android App

兰州原油库、成品油库数字化监控与污染源扩散预测移动端工程。项目以投标技术方案为需求基线，覆盖监控态势、GIS 点位、扩散预测、敏感点预警、手机通知和应急措施反馈。

## 当前基线

- 包名：`edu.lzjtu.environment.oildepot`
- 语言与界面：Kotlin、Jetpack Compose、Material 3
- 最低系统：Android 8.0（API 26）
- 编译与目标版本：Android API 36
- 构建环境：JDK 17、Gradle 8.13、Android Gradle Plugin 8.13.2
- 构建变体：`demo` 使用模拟数据，`production` 预留真实服务


## 本地构建

```bash
./gradlew testDemoDebugUnitTest
./gradlew assembleDemoDebug
```



