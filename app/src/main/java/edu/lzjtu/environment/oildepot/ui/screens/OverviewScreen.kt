package edu.lzjtu.environment.oildepot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.PollutantReading
import edu.lzjtu.environment.oildepot.ui.components.DepotSelector
import edu.lzjtu.environment.oildepot.ui.components.MetricTile
import edu.lzjtu.environment.oildepot.ui.components.QualityLabel
import edu.lzjtu.environment.oildepot.ui.components.SectionTitle

@Composable
fun OverviewScreen(
    dashboard: DashboardSnapshot,
    selectedDepot: DepotId,
    onDepotSelected: (DepotId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val points = dashboard.points.filter { it.depotId == selectedDepot }
    val pointIds = points.mapTo(mutableSetOf()) { it.id }
    val readings = dashboard.readings.filter { it.pointId in pointIds }
    val alerts = dashboard.alerts.filter { it.depotId == selectedDepot }
    val onlineCount = points.count { it.online }
    val warningCount = readings.count { it.thresholdRatio >= 1.0 }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DepotSelector(selected = selectedDepot, onSelected = onDepotSelected)
        }
        item {
            SectionTitle(
                title = "运行态势",
                supportingText = "监测数据、设备在线状态与活动预警汇总",
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "监测点在线",
                    value = "$onlineCount/${points.size}",
                    supportingText = "${points.size - onlineCount} 个离线",
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    label = "浓度超限",
                    value = warningCount.toString(),
                    supportingText = "${readings.size} 项指标",
                    accent = if (warningCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "活动预警",
                    value = alerts.size.toString(),
                    supportingText = alerts.firstOrNull()?.severity?.displayName ?: "暂无风险",
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    label = "最新模型",
                    value = "09:24",
                    supportingText = "任务已完成",
                    accent = Color(0xFF5367A5),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            SectionTitle(title = "污染物指标", supportingText = "按风险程度排序")
        }
        items(readings.sortedByDescending { it.thresholdRatio }, key = { it.pointId + it.pollutantName }) { reading ->
            ReadingCard(
                reading = reading,
                pointName = points.firstOrNull { it.id == reading.pointId }?.name ?: reading.pointId,
            )
        }
        item {
            Text(
                text = "演示数据仅用于界面和流程联调，不作为现场环境判断依据。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun ReadingCard(reading: PollutantReading, pointName: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reading.pollutantName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        pointName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "${formatReading(reading.value)} ${reading.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (reading.thresholdRatio >= 1.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "阈值 ${formatReading(reading.warningThreshold)} ${reading.unit}  ·  ${reading.sampledAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                QualityLabel(reading.quality)
            }
        }
    }
}

private fun formatReading(value: Double): String = if (value >= 100) {
    "%.0f".format(value)
} else {
    "%.2f".format(value)
}

