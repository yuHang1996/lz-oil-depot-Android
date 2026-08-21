package edu.lzjtu.environment.oildepot.ui.maps

import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.data.model.PointType
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoMapRegistrationTest {
    @Test
    fun crudeRegistration_mapsNormalizedPointToItsImageCenter() {
        val registration = DemoMapRegistration.forDepot(DepotId.CRUDE)

        val coordinate = registration.coordinateFor(testPoint(mapX = 0.5f, mapY = 0.5f))

        assertEquals(36.0769, coordinate.latitude, 0.000001)
        assertEquals(103.818875, coordinate.longitude, 0.000001)
    }

    @Test
    fun refinedRegistration_boundsContainAllImageCorners() {
        val registration = DemoMapRegistration.forDepot(DepotId.REFINED)
        val bounds = registration.bounds()

        assertEquals(36.0659, bounds.latitudeNorth, 0.000001)
        assertEquals(36.0559, bounds.latitudeSouth, 0.000001)
        assertEquals(103.807825, bounds.longitudeEast, 0.000001)
        assertEquals(103.791175, bounds.longitudeWest, 0.000001)
    }

    private fun testPoint(mapX: Float, mapY: Float) = MonitoringPoint(
        id = "test",
        depotId = DepotId.CRUDE,
        name = "测试点",
        type = PointType.AIR,
        longitude = 0.0,
        latitude = 0.0,
        mapX = mapX,
        mapY = mapY,
        online = true,
    )
}
