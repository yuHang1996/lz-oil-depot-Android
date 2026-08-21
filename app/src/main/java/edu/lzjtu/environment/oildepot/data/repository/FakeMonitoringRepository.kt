package edu.lzjtu.environment.oildepot.data.repository

import edu.lzjtu.environment.oildepot.data.model.AlertEvent
import edu.lzjtu.environment.oildepot.data.model.AlertSeverity
import edu.lzjtu.environment.oildepot.data.model.AlertStatus
import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.DataQuality
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.EmergencyMeasure
import edu.lzjtu.environment.oildepot.data.model.MeasureStatus
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.data.model.PointType
import edu.lzjtu.environment.oildepot.data.model.PollutantReading
import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import edu.lzjtu.environment.oildepot.data.model.PredictionResult
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlin.math.round

class FakeMonitoringRepository : MonitoringRepository {
    private var dashboardLoadCount = 0

    override suspend fun loadDashboard(): DashboardSnapshot {
        delay(120)
        val now = LocalDateTime.now()
        val tick = dashboardLoadCount++
        return DashboardSnapshot(
            points = points,
            readings = baseReadings.mapIndexed { index, reading ->
                if (reading.quality == DataQuality.OFFLINE) {
                    reading
                } else {
                    val offset = demoOffsets[(tick + index) % demoOffsets.size]
                    reading.copy(
                        value = round(
                            (reading.value + reading.warningThreshold * offset).coerceAtLeast(0.0) * 1_000,
                        ) / 1_000,
                        sampledAt = now.format(timeFormatter),
                    )
                }
            },
            alerts = alerts,
            measures = measures,
            updatedAt = now.format(dateTimeFormatter),
        )
    }

    override suspend fun runPrediction(input: PredictionInput): PredictionResult {
        delay(900)
        val scale = input.sourceStrengthKgPerHour / 12.0
        return PredictionResult(
            taskId = "TASK-20260722-0031",
            modelName = "大气扩散情景模型",
            modelVersion = "0.1.0-demo",
            maxConcentration = 3.42 * scale,
            concentrationUnit = "mg/m³",
            affectedAreaSquareKilometers = 1.86 * scale,
            affectedSensitivePoint = "黄河兰州段岸线 S-01",
            arrivalMinutes = (74 / scale.coerceAtLeast(0.25)).toInt(),
            windDirection = "东北偏东",
            calculatedAt = "2026-07-22 09:24",
        )
    }

    private val points = listOf(
        MonitoringPoint("SRC-C-01", DepotId.CRUDE, "原油罐区东侧", PointType.SOURCE, 103.8124, 36.0752, 0.76f, 0.58f, true),
        MonitoringPoint("AIR-C-01", DepotId.CRUDE, "厂界东北大气点", PointType.AIR, 103.8181, 36.0790, 0.78f, 0.20f, true),
        MonitoringPoint("GW-C-01", DepotId.CRUDE, "下游地下水井", PointType.GROUNDWATER, 103.8202, 36.0721, 0.86f, 0.82f, true),
        MonitoringPoint("SEN-C-01", DepotId.CRUDE, "黄河岸线 S-01", PointType.SENSITIVE, 103.8248, 36.0813, 0.93f, 0.10f, true),
        MonitoringPoint("SRC-R-01", DepotId.REFINED, "汽油装卸区", PointType.SOURCE, 103.7964, 36.0620, 0.37f, 0.52f, true),
        MonitoringPoint("AIR-R-01", DepotId.REFINED, "厂界东南大气点", PointType.AIR, 103.8012, 36.0577, 0.74f, 0.54f, true),
        MonitoringPoint("SOIL-R-01", DepotId.REFINED, "储罐区土壤点", PointType.SOIL, 103.7939, 36.0648, 0.56f, 0.35f, false),
        MonitoringPoint("SEN-R-01", DepotId.REFINED, "周边居民区 R-01", PointType.SENSITIVE, 103.8065, 36.0591, 0.83f, 0.65f, true),
    )

    private val baseReadings = listOf(
        PollutantReading("AIR-C-01", "苯", 2.62, "mg/m³", 2.0, "09:18", DataQuality.VALID),
        PollutantReading("GW-C-01", "石油类", 0.31, "mg/L", 0.5, "09:15", DataQuality.VALID),
        PollutantReading("SRC-C-01", "非甲烷总烃", 8.20, "mg/m³", 10.0, "09:19", DataQuality.VALID),
        PollutantReading("AIR-R-01", "甲苯", 0.84, "mg/m³", 1.0, "09:17", DataQuality.SUSPECT),
        PollutantReading("SOIL-R-01", "石油烃", 118.0, "mg/kg", 450.0, "昨日", DataQuality.OFFLINE),
        PollutantReading("SRC-R-01", "非甲烷总烃", 7.42, "mg/m³", 10.0, "09:19", DataQuality.VALID),
    )

    private val alerts = listOf(
        AlertEvent(
            id = "ENV-20260722-001",
            depotId = DepotId.CRUDE,
            title = "厂界东北大气点苯浓度超限",
            pollutantName = "苯",
            severity = AlertSeverity.WARNING,
            status = AlertStatus.NEW,
            triggerReason = "实测浓度为预警阈值的 1.31 倍",
            affectedTarget = "黄河兰州段岸线 S-01",
            expectedArrival = "预计 74 分钟后",
            createdAt = "09:18",
        ),
        AlertEvent(
            id = "ENV-20260722-002",
            depotId = DepotId.REFINED,
            title = "土壤监测点连续离线",
            pollutantName = "石油烃",
            severity = AlertSeverity.ATTENTION,
            status = AlertStatus.ACKNOWLEDGED,
            triggerReason = "连续 42 分钟未收到有效数据",
            affectedTarget = "储罐区土壤点",
            expectedArrival = "需现场复核",
            createdAt = "08:46",
        ),
    )

    private val measures = listOf(
        EmergencyMeasure("M-001", "ENV-20260722-001", "加密东北厂界与黄河岸线监测", "监测组", "10 分钟内", MeasureStatus.IN_PROGRESS, 1),
        EmergencyMeasure("M-002", "ENV-20260722-001", "复核原油罐区呼吸阀与密封状态", "现场处置组", "15 分钟内", MeasureStatus.PENDING, 2),
        EmergencyMeasure("M-003", "ENV-20260722-001", "准备围堵截流与吸附材料", "应急物资组", "20 分钟内", MeasureStatus.PENDING, 3),
        EmergencyMeasure("M-004", "ENV-20260722-002", "检查土壤监测设备供电与通信", "运维组", "30 分钟内", MeasureStatus.PENDING, 2),
    )

    companion object {
        private val demoOffsets = doubleArrayOf(-0.06, -0.02, 0.0, 0.03, 0.06)
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
