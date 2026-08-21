package edu.lzjtu.environment.oildepot.domain

import edu.lzjtu.environment.oildepot.data.model.AlertSeverity
import edu.lzjtu.environment.oildepot.data.model.DataQuality
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.data.model.PollutantReading

enum class PointMonitoringStatus(val displayName: String) {
    NORMAL("正常"),
    ATTENTION("关注"),
    ALARM("预警"),
    NO_DATA("无数据"),
    OFFLINE("离线"),
}

object PointMonitoringStatusResolver {
    fun resolve(
        point: MonitoringPoint,
        readings: List<PollutantReading>,
    ): PointMonitoringStatus {
        val pointReadings = readings.filter { it.pointId == point.id }
        if (!point.online || pointReadings.any { it.quality == DataQuality.OFFLINE }) {
            return PointMonitoringStatus.OFFLINE
        }
        if (pointReadings.isEmpty()) {
            return PointMonitoringStatus.NO_DATA
        }

        val severities = pointReadings.map { AlertPolicy.classify(it.thresholdRatio, arrivalMinutes = null) }
        return when {
            severities.any { it == AlertSeverity.WARNING || it == AlertSeverity.CRITICAL } -> PointMonitoringStatus.ALARM
            pointReadings.any { it.quality == DataQuality.SUSPECT } ||
                severities.any { it == AlertSeverity.ATTENTION } -> PointMonitoringStatus.ATTENTION
            else -> PointMonitoringStatus.NORMAL
        }
    }
}
