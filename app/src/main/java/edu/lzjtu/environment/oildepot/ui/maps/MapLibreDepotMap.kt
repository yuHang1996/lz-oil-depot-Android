package edu.lzjtu.environment.oildepot.ui.maps

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import edu.lzjtu.environment.oildepot.BuildConfig
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.domain.PointMonitoringStatus
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.textIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.ImageSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

@Composable
fun MapLibreDepotMap(
    depot: DepotId,
    points: List<MonitoringPoint>,
    statusByPoint: Map<String, PointMonitoringStatus>,
    selectedPointId: String?,
    onPointSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onPointSelectedLatest by rememberUpdatedState(onPointSelected)
    val registration = remember(depot) {
        if (BuildConfig.USE_MOCK_DATA) DemoMapRegistration.forDepot(depot) else null
    }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    var mapReady by remember { mutableStateOf(false) }

    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).also { it.onCreate(null) }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var destroyed = false
        fun destroyMapView() {
            if (!destroyed) {
                destroyed = true
                mapView.onDestroy()
            }
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> destroyMapView()
                else -> Unit
            }
        }
        val memoryCallbacks = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun onLowMemory() {
                mapView.onLowMemory()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        context.applicationContext.registerComponentCallbacks(memoryCallbacks)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            context.applicationContext.unregisterComponentCallbacks(memoryCallbacks)
            mapView.onPause()
            mapView.onStop()
            destroyMapView()
        }
    }

    DisposableEffect(map) {
        val currentMap = map
        if (currentMap == null) {
            onDispose { }
        } else {
            val listener = MapLibreMap.OnMapClickListener { clickedCoordinate ->
                val clickedPoint = currentMap.projection.toScreenLocation(clickedCoordinate)
                val touchBounds = RectF(
                    clickedPoint.x - MARKER_TOUCH_RADIUS_PX,
                    clickedPoint.y - MARKER_TOUCH_RADIUS_PX,
                    clickedPoint.x + MARKER_TOUCH_RADIUS_PX,
                    clickedPoint.y + MARKER_TOUCH_RADIUS_PX,
                )
                val pointId = currentMap
                    .queryRenderedFeatures(touchBounds, POINT_CORE_LAYER_ID)
                    .firstOrNull()
                    ?.getStringProperty(POINT_ID_PROPERTY)
                if (pointId == null) {
                    false
                } else {
                    onPointSelectedLatest(pointId)
                    true
                }
            }
            currentMap.addOnMapClickListener(listener)
            onDispose { currentMap.removeOnMapClickListener(listener) }
        }
    }

    LaunchedEffect(map, depot, registration) {
        val currentMap = map ?: return@LaunchedEffect
        style = null
        mapReady = false
        currentMap.setStyle(Style.Builder().fromJson(LOCAL_STYLE_JSON)) { loadedStyle ->
            installBaseLayers(loadedStyle, context, registration)
            installPointLayers(loadedStyle, points, statusByPoint, selectedPointId, registration)
            if (registration != null) {
                currentMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(registration.bounds(), MAP_EDGE_PADDING_PX),
                )
            } else {
                currentMap.cameraPosition = CameraPosition.Builder()
                    .target(pointCenter(points))
                    .zoom(PRODUCTION_DEFAULT_ZOOM)
                    .build()
            }
            style = loadedStyle
            mapReady = true
        }
    }

    LaunchedEffect(style, points, statusByPoint, selectedPointId, registration) {
        val pointSource = style?.getSourceAs<GeoJsonSource>(POINT_SOURCE_ID) ?: return@LaunchedEffect
        pointSource.setGeoJson(pointFeatureCollection(points, statusByPoint, selectedPointId, registration))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("point-map-container"),
    ) {
        AndroidView(
            factory = { viewContext ->
                mapView.getMapAsync { loadedMap -> map = loadedMap }
                mapView
            },
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = when {
                !mapReady -> "正在加载本机底图…"
                registration != null -> "MapLibre · 本机静态 Demo · 近似配准"
                else -> "MapLibre · 未配置生产底图"
            },
            modifier = Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
                .padding(horizontal = 7.dp, vertical = 4.dp)
                .then(if (mapReady) Modifier.testTag("point-map-ready") else Modifier),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (registration != null) {
            Text(
                text = "点位及边界未经测绘校核，仅用于 Demo 技术验证",
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomStart)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun installBaseLayers(
    style: Style,
    context: Context,
    registration: DepotImageRegistration?,
) {
    registration?.let { imageRegistration ->
        val resourceId = context.resources.getIdentifier(
            imageRegistration.imageResourceName,
            "drawable",
            context.packageName,
        )
        if (resourceId != 0) {
            style.addSource(ImageSource(BASEMAP_SOURCE_ID, imageRegistration.imageQuad(), resourceId))
            style.addLayer(RasterLayer(BASEMAP_LAYER_ID, BASEMAP_SOURCE_ID))
        }
        addAreaAnnotations(style, imageRegistration)
        addLabelAnnotations(style, imageRegistration)
    }
}

private fun installPointLayers(
    style: Style,
    points: List<MonitoringPoint>,
    statusByPoint: Map<String, PointMonitoringStatus>,
    selectedPointId: String?,
    registration: DepotImageRegistration?,
) {
    style.addSource(
        GeoJsonSource(
            POINT_SOURCE_ID,
            pointFeatureCollection(points, statusByPoint, selectedPointId, registration),
        ),
    )
    style.addLayer(
        CircleLayer(POINT_SELECTED_RING_LAYER_ID, POINT_SOURCE_ID).withProperties(
            circleRadius(17f),
            circleColor("rgba(0,0,0,0)"),
            circleStrokeColor(Expression.get(SELECTED_COLOR_PROPERTY)),
            circleStrokeWidth(Expression.get(SELECTED_STROKE_WIDTH_PROPERTY)),
        ),
    )
    style.addLayer(
        CircleLayer(POINT_STATUS_RING_LAYER_ID, POINT_SOURCE_ID).withProperties(
            circleRadius(12f),
            circleColor("rgba(0,0,0,0)"),
            circleStrokeColor(Expression.get(STATUS_COLOR_PROPERTY)),
            circleStrokeWidth(3.2f),
        ),
    )
    style.addLayer(
        CircleLayer(POINT_CORE_LAYER_ID, POINT_SOURCE_ID).withProperties(
            circleRadius(6.5f),
            circleColor(Expression.get(TYPE_COLOR_PROPERTY)),
            circleStrokeColor("#FFFFFF"),
            circleStrokeWidth(2.2f),
        ),
    )
}

private fun addAreaAnnotations(style: Style, registration: DepotImageRegistration) {
    if (registration.areas.isEmpty()) return
    style.addSource(
        GeoJsonSource(
            AREA_SOURCE_ID,
            FeatureCollection.fromFeatures(registration.areas.map { area ->
                val vertices = area.vertices.map { coordinate ->
                    registration.coordinateAt(coordinate.x, coordinate.y).toGeoJsonPoint()
                }
                Feature.fromGeometry(
                    Polygon.fromLngLats(listOf(vertices + vertices.first())),
                ).apply { addStringProperty(AREA_COLOR_PROPERTY, area.color) }
            }),
        ),
    )
    style.addLayer(
        FillLayer(AREA_FILL_LAYER_ID, AREA_SOURCE_ID).withProperties(
            fillColor(Expression.get(AREA_COLOR_PROPERTY)),
            fillOpacity(0.12f),
        ),
    )
    style.addLayer(
        LineLayer(AREA_LINE_LAYER_ID, AREA_SOURCE_ID).withProperties(
            lineColor(Expression.get(AREA_COLOR_PROPERTY)),
            lineWidth(2f),
        ),
    )
}

private fun addLabelAnnotations(style: Style, registration: DepotImageRegistration) {
    if (registration.labels.isEmpty()) return
    style.addSource(
        GeoJsonSource(
            LABEL_SOURCE_ID,
            FeatureCollection.fromFeatures(registration.labels.map { label ->
                Feature.fromGeometry(registration.coordinateAt(label.x, label.y).toGeoJsonPoint()).apply {
                    addStringProperty(LABEL_TEXT_PROPERTY, label.text)
                    addStringProperty(LABEL_COLOR_PROPERTY, label.color)
                }
            }),
        ),
    )
    style.addLayer(
        SymbolLayer(LABEL_LAYER_ID, LABEL_SOURCE_ID).withProperties(
            textField(Expression.get(LABEL_TEXT_PROPERTY)),
            textSize(11f),
            textColor(Expression.get(LABEL_COLOR_PROPERTY)),
            textHaloColor("#FFFFFF"),
            textHaloWidth(1.25f),
            textAllowOverlap(true),
            textIgnorePlacement(true),
        ),
    )
}

private fun pointFeatureCollection(
    points: List<MonitoringPoint>,
    statusByPoint: Map<String, PointMonitoringStatus>,
    selectedPointId: String?,
    registration: DepotImageRegistration?,
): FeatureCollection = FeatureCollection.fromFeatures(points.map { point ->
    val coordinate = registration?.coordinateFor(point) ?: LatLng(point.latitude, point.longitude)
    Feature.fromGeometry(coordinate.toGeoJsonPoint()).apply {
        addStringProperty(POINT_ID_PROPERTY, point.id)
        addStringProperty(TYPE_COLOR_PROPERTY, point.type.mapColor())
        addStringProperty(STATUS_COLOR_PROPERTY, statusByPoint.getValue(point.id).mapColor())
        addStringProperty(SELECTED_COLOR_PROPERTY, SELECTED_COLOR)
        addNumberProperty(
            SELECTED_STROKE_WIDTH_PROPERTY,
            if (point.id == selectedPointId) 2.6 else 0.0,
        )
    }
})

private fun pointCenter(points: List<MonitoringPoint>): LatLng {
    if (points.isEmpty()) return LatLng(36.0700, 103.8100)
    return LatLng(
        points.map { it.latitude }.average(),
        points.map { it.longitude }.average(),
    )
}

private fun LatLng.toGeoJsonPoint(): Point = Point.fromLngLat(longitude, latitude)

private fun edu.lzjtu.environment.oildepot.data.model.PointType.mapColor(): String = when (this) {
    edu.lzjtu.environment.oildepot.data.model.PointType.SOURCE -> "#B54A42"
    edu.lzjtu.environment.oildepot.data.model.PointType.AIR -> "#596EAE"
    edu.lzjtu.environment.oildepot.data.model.PointType.GROUNDWATER -> "#287EA4"
    edu.lzjtu.environment.oildepot.data.model.PointType.SOIL -> "#8B6E41"
    edu.lzjtu.environment.oildepot.data.model.PointType.SENSITIVE -> "#37805A"
}

private fun PointMonitoringStatus.mapColor(): String = when (this) {
    PointMonitoringStatus.NORMAL -> "#2F7D50"
    PointMonitoringStatus.ATTENTION -> "#B66A00"
    PointMonitoringStatus.ALARM -> "#B3261E"
    PointMonitoringStatus.NO_DATA -> "#7A746E"
    PointMonitoringStatus.OFFLINE -> "#68736D"
}

private const val LOCAL_STYLE_JSON = """
    {
      "version": 8,
      "sources": {},
      "layers": [
        {
          "id": "local-background",
          "type": "background",
          "paint": { "background-color": "#1D2622" }
        }
      ]
    }
"""

private const val BASEMAP_SOURCE_ID = "depot-basemap-source"
private const val BASEMAP_LAYER_ID = "depot-basemap-layer"
private const val AREA_SOURCE_ID = "depot-area-source"
private const val AREA_FILL_LAYER_ID = "depot-area-fill-layer"
private const val AREA_LINE_LAYER_ID = "depot-area-line-layer"
private const val LABEL_SOURCE_ID = "depot-label-source"
private const val LABEL_LAYER_ID = "depot-label-layer"
private const val POINT_SOURCE_ID = "monitoring-point-source"
private const val POINT_SELECTED_RING_LAYER_ID = "monitoring-point-selected-ring-layer"
private const val POINT_STATUS_RING_LAYER_ID = "monitoring-point-status-ring-layer"
private const val POINT_CORE_LAYER_ID = "monitoring-point-core-layer"
private const val POINT_ID_PROPERTY = "pointId"
private const val TYPE_COLOR_PROPERTY = "typeColor"
private const val STATUS_COLOR_PROPERTY = "statusColor"
private const val SELECTED_COLOR_PROPERTY = "selectedColor"
private const val SELECTED_STROKE_WIDTH_PROPERTY = "selectedStrokeWidth"
private const val AREA_COLOR_PROPERTY = "areaColor"
private const val LABEL_TEXT_PROPERTY = "label"
private const val LABEL_COLOR_PROPERTY = "labelColor"
private const val SELECTED_COLOR = "#006D5B"
private const val PRODUCTION_DEFAULT_ZOOM = 14.5
private const val MARKER_TOUCH_RADIUS_PX = 28f
private const val MAP_EDGE_PADDING_PX = 24
