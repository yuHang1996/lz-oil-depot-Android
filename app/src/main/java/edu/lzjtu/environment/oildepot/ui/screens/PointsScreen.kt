package edu.lzjtu.environment.oildepot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.data.model.PointType
import edu.lzjtu.environment.oildepot.data.model.PollutantReading
import edu.lzjtu.environment.oildepot.domain.PointMonitoringStatus
import edu.lzjtu.environment.oildepot.domain.PointMonitoringStatusResolver
import edu.lzjtu.environment.oildepot.ui.components.DepotSelector
import edu.lzjtu.environment.oildepot.ui.components.SectionTitle
import edu.lzjtu.environment.oildepot.ui.maps.MapLibreDepotMap

@Composable
fun PointsScreen(
    dashboard: DashboardSnapshot,
    selectedDepot: DepotId,
    onDepotSelected: (DepotId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by remember { mutableStateOf<PointType?>(null) }
    var selectedPointId by remember { mutableStateOf<String?>(null) }
    val allPoints = dashboard.points.filter { it.depotId == selectedDepot }
    val points = selectedType?.let { type -> allPoints.filter { it.type == type } } ?: allPoints
    val readingsByPoint = remember(dashboard.readings) { dashboard.readings.groupBy { it.pointId } }
    val statusByPoint = remember(allPoints, dashboard.readings) {
        allPoints.associate { point ->
            point.id to PointMonitoringStatusResolver.resolve(point, dashboard.readings)
        }
    }
    val selectedPoint = points.firstOrNull { it.id == selectedPointId }

    LaunchedEffect(selectedDepot) {
        selectedType = null
        selectedPointId = null
    }
    LaunchedEffect(points) {
        if (selectedPointId != null && points.none { it.id == selectedPointId }) {
            selectedPointId = null
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { DepotSelector(selectedDepot, onDepotSelected) }
        item { SectionTitle(title = "点位布局", supportingText = "本机静态 Demo 底图与模拟监测、预警状态叠加") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf<PointType?>(null).plus(PointType.entries).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(pointTypeFilterLabel(type)) },
                        modifier = Modifier.testTag("point-filter-${type?.name ?: "ALL"}"),
                    )
                }
            }
        }
        item {
            SchematicMap(
                depot = selectedDepot,
                points = points,
                statusByPoint = statusByPoint,
                selectedPointId = selectedPointId,
                onPointSelected = { selectedPointId = it },
            )
        }
        if (selectedPoint != null) {
            item {
                PointDetailCard(
                    point = selectedPoint,
                    readings = readingsByPoint[selectedPoint.id].orEmpty(),
                    status = statusByPoint.getValue(selectedPoint.id),
                )
            }
        }
        if (points.isEmpty()) {
            item {
                Text(
                    text = "当前库区暂无此类点位",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(points, key = { it.id }) { point ->
                PointCard(
                    point = point,
                    reading = readingsByPoint[point.id]?.firstOrNull(),
                    status = statusByPoint.getValue(point.id),
                    selected = point.id == selectedPointId,
                    onClick = { selectedPointId = point.id },
                )
            }
        }
    }
}

@Composable
private fun SchematicMap(
    depot: DepotId,
    points: List<MonitoringPoint>,
    statusByPoint: Map<String, PointMonitoringStatus>,
    selectedPointId: String?,
    onPointSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MapLibreDepotMap(
            depot = depot,
            points = points,
            statusByPoint = statusByPoint,
            selectedPointId = selectedPointId,
            onPointSelected = onPointSelected,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.35f),
        )
        StatusLegend()
    }
}

@Composable
private fun StatusLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("状态", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PointMonitoringStatus.entries.forEach { status ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(monitoringStatusColor(status), CircleShape),
                    )
                    Text(status.displayName, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(
            text = "演示数据 · 5 秒刷新",
            modifier = Modifier.align(Alignment.End),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PointDetailCard(
    point: MonitoringPoint,
    readings: List<PollutantReading>,
    status: PointMonitoringStatus,
) {
    val statusColor = monitoringStatusColor(status)
    Card(
        modifier = Modifier.fillMaxWidth().testTag("point-detail-card"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.65f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(point.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${point.id} · ${point.type.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = status.displayName,
                    modifier = Modifier
                        .testTag("point-detail-status")
                        .background(statusColor.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (readings.isEmpty()) {
                Text("暂无演示监测读数", style = MaterialTheme.typography.bodyMedium)
            } else {
                readings.forEach { reading ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${reading.pollutantName}  ${reading.value.formatReading()} ${reading.unit}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "阈值 ${reading.warningThreshold.formatReading()} ${reading.unit} · 当前 ${reading.thresholdRatio.formatRatio()} 倍",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "采样 ${reading.sampledAt} · 数据${reading.quality.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                "本机 Demo 底图为近似配准，点位及现场边界待测绘校核",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PointCard(
    point: MonitoringPoint,
    reading: PollutantReading?,
    status: PointMonitoringStatus,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = monitoringStatusColor(status)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("point-card-${point.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f) else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(pointColor(point.type))
                    .padding(horizontal = 7.dp, vertical = 5.dp),
            ) {
                Text(point.type.displayName, color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(point.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    reading?.let { "${point.id} · ${it.pollutantName} ${it.value.formatReading()} ${it.unit}" }
                        ?: "${point.id} · 暂无演示监测读数",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                status.displayName,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
            )
        }
    }
}

private fun pointTypeFilterLabel(type: PointType?): String = when (type) {
    null -> "全部"
    PointType.SOURCE -> "污染源"
    PointType.AIR -> "大气"
    PointType.GROUNDWATER -> "地下水井"
    PointType.SOIL -> "土壤"
    PointType.SENSITIVE -> "敏感点"
}

private fun pointColor(type: PointType): Color = when (type) {
    PointType.SOURCE -> Color(0xFFB54A42)
    PointType.AIR -> Color(0xFF596EAE)
    PointType.GROUNDWATER -> Color(0xFF287EA4)
    PointType.SOIL -> Color(0xFF8B6E41)
    PointType.SENSITIVE -> Color(0xFF37805A)
}

private fun monitoringStatusColor(status: PointMonitoringStatus): Color = when (status) {
    PointMonitoringStatus.NORMAL -> Color(0xFF2F7D50)
    PointMonitoringStatus.ATTENTION -> Color(0xFFB66A00)
    PointMonitoringStatus.ALARM -> Color(0xFFB3261E)
    PointMonitoringStatus.NO_DATA -> Color(0xFF7A746E)
    PointMonitoringStatus.OFFLINE -> Color(0xFF68736D)
}

private fun Double.formatReading(): String = if (this >= 100.0) "%.0f".format(this) else "%.2f".format(this)

private fun Double.formatRatio(): String = "%.2f".format(this)
