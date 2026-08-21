# 点位地图 Design QA

## Comparison target

- Scope: Android `demoDebug` 的点位地图；仅限本机 POC，不是可发布的在线地图产品。
- Source visual truth:
  - `/private/tmp/amap-oil-depot-reference.jpg`（高德页面总览，1792 × 976 px）
  - `/private/tmp/amap-crude-crop.jpg`（原油库参考，837 × 620 px）
  - `/private/tmp/amap-refined-crop.jpg`（成品油库参考，700 × 520 px）
  - `/var/folders/91/hb8btl5j6957wvjjy4hs99l80000gn/T/codex-clipboard-db94d14e-88a7-4831-a46e-bfee95b29754.png`（仅用于办公区、罐区、辅助设施区、事故池等标注关系）
- Rendered implementation:
  - `/private/tmp/points-amap-crude-final.png`
  - `/private/tmp/points-amap-refined-final.png`
  - `/private/tmp/points-amap-refined-refresh.png`
- Focused comparison evidence:
  - `/private/tmp/qa-crude-final-comparison.png`
  - `/private/tmp/qa-refined-final-comparison.png`
- Device viewport: 1080 × 2340 px, 440 dpi；Android 报告逻辑尺寸约 393 × 851 dp，density 2.75。
- Map implementation crop: 992 × 735 px；源图和实现图均按同一 1.35:1 地图区域等比显示，无额外拉伸。
- State: 浅色主题、原油库/成品油库、全部点位；另验证成品油库 5 秒模拟刷新状态。

## Final visual review

- Fonts and typography: 沿用现有 Material 3 字体层级；地图标签调整为 10sp、半透明白底，卫星纹理上可读，无截断。
- Spacing and layout rhythm: 两张底图均完整铺满地图容器；标注使用地图宽高比例定位；48dp 点位热区不越界；图例和刷新说明分行展示。
- Colors and tokens: 点芯表达点位类型，外环表达正常/关注/预警/无数据/离线；事故池红、办公区蓝、库区黄，与资料图语义一致。
- Image quality and fidelity: App 与高德参考采用相同裁切和比例；原油罐区、成品油罐区及周边空间关系一致。当前截图分辨率足够本机 POC，放大查看仍有轻微软化，归为 P3。
- Copy and content: 明确写成“静态高德影像参考”“模拟监测、预警”“本机静态 Demo”和“演示数据 · 5 秒刷新”；不再把假数据或静态影像描述成真实实时系统。
- Accessibility and interaction: 标记热区 48dp，语义包含点名和状态；库区选择、类型筛选、地图标记、列表卡片和详情联动可操作。

## Comparison history

### Iteration 1 — blocked

- P0: 高德截图位于 `src/main/res`，会进入 production 包，且没有正式分发授权。
  - Fix: 移至 `app/src/demo/res/drawable-nodpi`；production 运行时显示“需配置已授权底图”。生产 APK 内容检查未发现两张 AMap 资源。
- P1: 原油源点落在事故池框内，成品油源点落在办公区内。
  - Fix: 将 `SRC-C-01` 调整到东侧罐列，将 `SRC-R-01` 调整到装卸/罐区边缘。
- P1: 空读数敏感点被绿色标成“正常”。
  - Fix: 新增 `NO_DATA/无数据` 状态与中性灰外环，并补充单元测试。
- P1: “实时监测”措辞和未校核经纬度制造了精确定位错觉。
  - Fix: 统一为静态影像与模拟监测措辞；详情改为“屏幕点位按资料图示意，坐标与现场边界待校核”。
- P1: 成品油罐区标签压住离线土壤点，底图鼠标指针可见。
  - Fix: 标签移到罐区上缘；办公区标签按区域比例定位并覆盖残留指针。

### Iteration 2 — blocked

- P2: 原油罐区标签与源点相碰，黄河方向标签与东北点位相碰。
  - Fix: 全部区域标签改用 `maxWidth/maxHeight` 比例锚点，并重新截图复核。
- P2: 加入“无数据”后状态图例与刷新说明横向截断。
  - Fix: 状态图例固定完整显示，刷新说明单独右对齐一行。
- Post-fix evidence: `/private/tmp/points-amap-crude-final.png`、`/private/tmp/points-amap-refined-final.png` 和两张 final comparison 图均未发现残留 P0/P1/P2。

## Interaction and functional evidence

- `connectedDemoDebugAndroidTest`: 进入点位地图、打开预警详情、切换成品油库、土壤筛选、离线点详情。
- `testDemoDebugUnitTest`: 正常/关注/预警/无数据/离线状态边界。
- 手工刷新证据: `/private/tmp/points-amap-refined-final.png` 到 `/private/tmp/points-amap-refined-refresh.png`，装卸点由 6.82 变为 8.02 mg/m³，状态从正常同步变为关注。
- `assembleDemoDebug` 与 `assembleProductionDebug` 均已构建成功。

## Residual constraints

- 高德截图只用于本机 Demo 验证，不能据此宣称免费长期商用，也不能发布为生产底图；正式上线必须替换为建设单位自有或书面授权的影像服务/正射影像。
- 点位位置来自资料图和目视校准，不具备测绘精度；接入真实接口前必须以统一坐标系和现场点位表复核。
- 成品油库源图为 700 × 520 px，较最终显示尺寸略有放大；本机 POC 可接受，正式版本需更高分辨率授权影像。

final result: passed
