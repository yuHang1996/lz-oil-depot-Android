package edu.lzjtu.environment.oildepot.data.model

enum class DepotId(val displayName: String, val shortName: String) {
    CRUDE("兰州原油库", "原油库2"),
    REFINED("兰州成品油库", "成品油库2"),
}

enum class PointType(val displayName: String) {
    SOURCE("污染源"),
    AIR("大气监测点"),
    GROUNDWATER("地下水监测井"),
    SOIL("土壤监测点"),
    SENSITIVE("敏感点"),
}

enum class DataQuality(val displayName: String) {
    VALID("有效"),
    SUSPECT("待复核"),
    OFFLINE("离线"),
}

enum class AlertSeverity(val displayName: String) {
    NORMAL("正常"),
    ATTENTION("关注"),
    WARNING("预警"),
    CRITICAL("严重"),
}

enum class AlertStatus(val displayName: String) {
    NEW("待确认"),
    ACKNOWLEDGED("已确认"),
    IN_PROGRESS("处置中"),
    CLOSED("已解除"),
}

enum class MeasureStatus(val displayName: String) {
    PENDING("待执行"),
    IN_PROGRESS("执行中"),
    COMPLETED("已完成"),
}

data class MonitoringPoint(
    val id: String,
    val depotId: DepotId,
    val name: String,
    val type: PointType,
    val longitude: Double,
    val latitude: Double,
    val mapX: Float,
    val mapY: Float,
    val online: Boolean,
)

data class PollutantReading(
    val pointId: String,
    val pollutantName: String,
    val value: Double,
    val unit: String,
    val warningThreshold: Double,
    val sampledAt: String,
    val quality: DataQuality,
) {
    val thresholdRatio: Double
        get() = if (warningThreshold == 0.0) 0.0 else value / warningThreshold
}

data class PredictionInput(
    val pollutantName: String = "苯",
    val sourceStrengthKgPerHour: Double = 12.0,
    val releaseDurationMinutes: Int = 45,
    val windSpeedMetersPerSecond: Double = 2.8,
    val predictionHours: Int = 6,
)

data class PredictionResult(
    val taskId: String,
    val modelName: String,
    val modelVersion: String,
    val maxConcentration: Double,
    val concentrationUnit: String,
    val affectedAreaSquareKilometers: Double,
    val affectedSensitivePoint: String,
    val arrivalMinutes: Int,
    val windDirection: String,
    val calculatedAt: String,
)

data class AlertEvent(
    val id: String,
    val depotId: DepotId,
    val title: String,
    val pollutantName: String,
    val severity: AlertSeverity,
    val status: AlertStatus,
    val triggerReason: String,
    val affectedTarget: String,
    val expectedArrival: String,
    val createdAt: String,
)

data class EmergencyMeasure(
    val id: String,
    val alertId: String,
    val title: String,
    val owner: String,
    val deadline: String,
    val status: MeasureStatus,
    val priority: Int,
)

data class DashboardSnapshot(
    val points: List<MonitoringPoint>,
    val readings: List<PollutantReading>,
    val alerts: List<AlertEvent>,
    val measures: List<EmergencyMeasure>,
    val updatedAt: String,
)

