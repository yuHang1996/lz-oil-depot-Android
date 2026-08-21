package edu.lzjtu.environment.oildepot.ui.maps

import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.geometry.LatLngQuad

/**
 * Local Demo images have no surveyed control points. These registrations only keep the
 * image, annotations, and mock point positions in one replaceable map coordinate system.
 */
internal data class DepotImageRegistration(
    val imageResourceName: String,
    val topLeft: LatLng,
    val topRight: LatLng,
    val bottomRight: LatLng,
    val bottomLeft: LatLng,
    val labels: List<DemoMapLabel>,
    val areas: List<DemoMapArea>,
) {
    fun imageQuad(): LatLngQuad = LatLngQuad(topLeft, topRight, bottomRight, bottomLeft)

    fun coordinateFor(point: MonitoringPoint): LatLng = coordinateAt(point.mapX, point.mapY)

    fun coordinateAt(x: Float, y: Float): LatLng {
        val horizontal = x.coerceIn(0f, 1f).toDouble()
        val vertical = y.coerceIn(0f, 1f).toDouble()
        val top = interpolate(topLeft, topRight, horizontal)
        val bottom = interpolate(bottomLeft, bottomRight, horizontal)
        return interpolate(top, bottom, vertical)
    }

    fun bounds(): LatLngBounds {
        val corners = listOf(topLeft, topRight, bottomRight, bottomLeft)
        return LatLngBounds.from(
            corners.maxOf { it.latitude },
            corners.maxOf { it.longitude },
            corners.minOf { it.latitude },
            corners.minOf { it.longitude },
        )
    }

    private fun interpolate(start: LatLng, end: LatLng, factor: Double): LatLng = LatLng(
        start.latitude + (end.latitude - start.latitude) * factor,
        start.longitude + (end.longitude - start.longitude) * factor,
    )
}

internal data class DemoMapLabel(
    val text: String,
    val x: Float,
    val y: Float,
    val color: String = "#3F4D4C",
)

internal data class DemoMapArea(
    val vertices: List<DemoMapCoordinate>,
    val color: String,
)

internal data class DemoMapCoordinate(
    val x: Float,
    val y: Float,
)

internal object DemoMapRegistration {
    fun forDepot(depot: DepotId): DepotImageRegistration = when (depot) {
        DepotId.CRUDE -> crude
        DepotId.REFINED -> refined
    }

    private val crude = DepotImageRegistration(
        imageResourceName = "amap_crude_demo",
        topLeft = LatLng(36.081900, 103.810523),
        topRight = LatLng(36.081900, 103.827227),
        bottomRight = LatLng(36.071900, 103.827227),
        bottomLeft = LatLng(36.071900, 103.810523),
        labels = listOf(
            DemoMapLabel("辅助设施区", 0.23f, 0.55f),
            DemoMapLabel("原油储罐区", 0.52f, 0.52f),
            DemoMapLabel("事故池", 0.85f, 0.35f, "#B3261E"),
            DemoMapLabel("黄河岸线方向", 0.29f, 0.11f, "#155F8A"),
        ),
        areas = listOf(
            DemoMapArea(
                vertices = listOf(
                    DemoMapCoordinate(0.49f, 0.40f),
                    DemoMapCoordinate(0.57f, 0.20f),
                    DemoMapCoordinate(0.76f, 0.15f),
                    DemoMapCoordinate(0.88f, 0.53f),
                    DemoMapCoordinate(0.76f, 0.91f),
                    DemoMapCoordinate(0.42f, 0.84f),
                    DemoMapCoordinate(0.37f, 0.62f),
                ),
                color = "#FFD54F",
            ),
            rectangle(0.38f, 0.47f, 0.18f, 0.23f, "#4F7FC5"),
            rectangle(0.82f, 0.48f, 0.06f, 0.18f, "#E0463B"),
        ),
    )

    private val refined = DepotImageRegistration(
        imageResourceName = "amap_refined_demo",
        topLeft = LatLng(36.065900, 103.791175),
        topRight = LatLng(36.065900, 103.807825),
        bottomRight = LatLng(36.055900, 103.807825),
        bottomLeft = LatLng(36.055900, 103.791175),
        labels = listOf(
            DemoMapLabel("办公区", 0.30f, 0.34f),
            DemoMapLabel("成品油罐区", 0.48f, 0.19f),
            DemoMapLabel("辅助设施区", 0.51f, 0.59f),
            DemoMapLabel("事故池", 0.76f, 0.20f, "#B3261E"),
            DemoMapLabel("居民方向", 0.85f, 0.47f),
        ),
        areas = listOf(
            rectangle(0.39f, 0.25f, 0.34f, 0.25f, "#FFD54F"),
            rectangle(0.25f, 0.26f, 0.14f, 0.24f, "#4F7FC5"),
            rectangle(0.39f, 0.50f, 0.34f, 0.10f, "#FFFFFF"),
            rectangle(0.73f, 0.18f, 0.10f, 0.13f, "#E0463B"),
        ),
    )

    private fun rectangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: String,
    ): DemoMapArea = DemoMapArea(
        vertices = listOf(
            DemoMapCoordinate(x, y),
            DemoMapCoordinate(x + width, y),
            DemoMapCoordinate(x + width, y + height),
            DemoMapCoordinate(x, y + height),
        ),
        color = color,
    )
}
