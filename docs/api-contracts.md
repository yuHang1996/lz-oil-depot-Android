# 接口契约基线

生产环境接口由建设单位或总体平台提供，Android App 不直接访问数据库、采集设备或模型文件系统。所有接口使用 HTTPS、统一身份认证、版本化路径和服务端权限校验。

## 接口清单

| 能力 | 建议接口 | App 主要用途 |
| --- | --- | --- |
| 油库与点位 | `GET /api/v1/depots/{id}/points` | 地图图层、点位详情与检索 |
| 监测指标 | `GET /api/v1/readings` | 最新浓度、阈值、单位、质量和趋势 |
| 情景管理 | `POST /api/v1/scenarios` | 保存污染源、污染物和环境参数 |
| 模型任务 | `POST /api/v1/model-tasks` | 提交大气或地下水计算任务 |
| 任务状态 | `GET /api/v1/model-tasks/{id}` | 查询排队、运行、完成或失败状态 |
| 模型结果 | `GET /api/v1/model-tasks/{id}/result` | 获取扩散范围、浓度分区和敏感点到达时间 |
| 影响校验 | `POST /api/v1/validations` | 记录对比数据、误差指标和校验结论 |
| 预警事件 | `GET /api/v1/alerts` | 查询分级预警和处置状态 |
| 预警确认 | `POST /api/v1/alerts/{id}/acknowledgements` | 记录确认人员和时间 |
| 应急措施 | `GET /api/v1/alerts/{id}/measures` | 获取建议措施、责任组和时限 |
| 措施反馈 | `POST /api/v1/measures/{id}/feedback` | 记录执行状态和现场反馈 |
| 消息注册 | `POST /api/v1/devices` | 注册推送令牌和授权用户 |

## 通用要求

- 时间统一使用带时区的 ISO 8601 格式，服务端保存 UTC，客户端展示北京时间。
- 数值必须同时提供单位，污染物和点位使用稳定编码。
- 列表接口支持分页、时间范围和油库过滤。
- 写接口使用幂等键，模型任务避免重复提交。
- 错误响应至少包含 `code`、`message`、`traceId` 和可否重试标识。
- 模型结果必须包含模型名称、版本、参数摘要、计算时间和数据版本。
- 坐标必须声明坐标参考系，禁止仅传无说明的经纬度。

## 接入替换点

当前 `FakeMonitoringRepository` 实现演示流程。生产接口明确后新增 `RemoteMonitoringRepository`，并通过应用级依赖容器按构建环境选择实现，页面与 ViewModel 不直接感知网络库。

