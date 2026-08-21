package edu.lzjtu.environment.oildepot.domain

import edu.lzjtu.environment.oildepot.data.model.DataQuality
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.data.model.PointType
import edu.lzjtu.environment.oildepot.data.model.PollutantReading
import org.junit.Assert.assertEquals
import org.junit.Test

class PointMonitoringStatusResolverTest {
    @Test
    fun offlinePoint_hasHighestPriority() {
        val point = point(online = false)
        val reading = reading(value = 2.0, threshold = 1.0)

        assertEquals(
            PointMonitoringStatus.OFFLINE,
            PointMonitoringStatusResolver.resolve(point, listOf(reading)),
        )
    }

    @Test
    fun thresholdBoundary_isAlarm() {
        assertEquals(
            PointMonitoringStatus.ALARM,
            PointMonitoringStatusResolver.resolve(point(), listOf(reading(value = 1.0, threshold = 1.0))),
        )
    }

    @Test
    fun eightyPercentBoundary_needsAttention() {
        assertEquals(
            PointMonitoringStatus.ATTENTION,
            PointMonitoringStatusResolver.resolve(point(), listOf(reading(value = 0.8, threshold = 1.0))),
        )
    }

    @Test
    fun suspectReading_needsAttentionEvenBelowThreshold() {
        assertEquals(
            PointMonitoringStatus.ATTENTION,
            PointMonitoringStatusResolver.resolve(
                point(),
                listOf(reading(value = 0.2, threshold = 1.0, quality = DataQuality.SUSPECT)),
            ),
        )
    }

    @Test
    fun onlinePointWithoutReading_isReportedAsNoData() {
        assertEquals(
            PointMonitoringStatus.NO_DATA,
            PointMonitoringStatusResolver.resolve(point(), emptyList()),
        )
    }

    private fun point(online: Boolean = true) = MonitoringPoint(
        id = "P-01",
        depotId = DepotId.CRUDE,
        name = "测试点",
        type = PointType.AIR,
        longitude = 103.8,
        latitude = 36.0,
        mapX = 0.5f,
        mapY = 0.5f,
        online = online,
    )

    private fun reading(
        value: Double,
        threshold: Double,
        quality: DataQuality = DataQuality.VALID,
    ) = PollutantReading(
        pointId = "P-01",
        pollutantName = "苯",
        value = value,
        unit = "mg/m³",
        warningThreshold = threshold,
        sampledAt = "10:00",
        quality = quality,
    )
}
