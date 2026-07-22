package edu.lzjtu.environment.oildepot.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.MonitoringPoint
import edu.lzjtu.environment.oildepot.data.model.PointType
import edu.lzjtu.environment.oildepot.ui.components.DepotSelector
import edu.lzjtu.environment.oildepot.ui.components.SectionTitle

@Composable
fun PointsScreen(
    dashboard: DashboardSnapshot,
    selectedDepot: DepotId,
    onDepotSelected: (DepotId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by remember { mutableStateOf<PointType?>(null) }
    val allPoints = dashboard.points.filter { it.depotId == selectedDepot }
    val points = selectedType?.let { type -> allPoints.filter { it.type == type } } ?: allPoints

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { DepotSelector(selectedDepot, onDepotSelected) }
        item { SectionTitle(title = "点位布局", supportingText = "污染源、监测点与典型敏感点空间关系") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(null, PointType.SOURCE, PointType.AIR, PointType.GROUNDWATER, PointType.SENSITIVE).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type?.displayName?.replace("监测点", "") ?: "全部") },
                    )
                }
            }
        }
        item { SchematicMap(points = points) }
        items(points, key = { it.id }) { point -> PointCard(point) }
    }
}

@Composable
private fun SchematicMap(points: List<MonitoringPoint>) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val riverColor = Color(0xFF3F83B6)
    val background = Color(0xFFEAF1EC)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.35f)
            .clip(RoundedCornerShape(8.dp))
            .background(background),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (step in 1..5) {
                drawLine(gridColor, Offset(size.width * step / 6f, 0f), Offset(size.width * step / 6f, size.height), 1f)
                drawLine(gridColor, Offset(0f, size.height * step / 6f), Offset(size.width, size.height * step / 6f), 1f)
            }

            val river = Path().apply {
                moveTo(0f, size.height * 0.16f)
                cubicTo(
                    size.width * 0.28f,
                    size.height * 0.05f,
                    size.width * 0.55f,
                    size.height * 0.29f,
                    size.width,
                    size.height * 0.12f,
                )
            }
            drawPath(river, riverColor, style = Stroke(width = 12f))

            points.forEach { point ->
                val center = Offset(size.width * point.mapX, size.height * point.mapY)
                val color = pointColor(point.type)
                drawCircle(Color.White, radius = 13f, center = center)
                drawCircle(color, radius = 9f, center = center)
                if (!point.online) drawCircle(Color(0xFFB3261E), radius = 13f, center = center, style = Stroke(width = 3f))
            }
        }
        Text(
            text = "黄河",
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            style = MaterialTheme.typography.labelMedium,
            color = riverColor,
        )
        Text(
            text = "GIS 接口联调前使用示意坐标",
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PointCard(point: MonitoringPoint) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
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
                Text(point.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${point.id}  ·  %.4f, %.4f".format(point.longitude, point.latitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                if (point.online) "在线" else "离线",
                style = MaterialTheme.typography.labelMedium,
                color = if (point.online) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun pointColor(type: PointType): Color = when (type) {
    PointType.SOURCE -> Color(0xFFB54A42)
    PointType.AIR -> Color(0xFF596EAE)
    PointType.GROUNDWATER -> Color(0xFF287EA4)
    PointType.SOIL -> Color(0xFF8B6E41)
    PointType.SENSITIVE -> Color(0xFF37805A)
}
