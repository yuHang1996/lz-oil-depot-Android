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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.lzjtu.environment.oildepot.data.model.AlertEvent
import edu.lzjtu.environment.oildepot.data.model.AlertStatus
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.ui.components.DepotSelector
import edu.lzjtu.environment.oildepot.ui.components.SectionTitle
import edu.lzjtu.environment.oildepot.ui.components.SeverityBadge

@Composable
fun AlertsScreen(
    alerts: List<AlertEvent>,
    selectedDepot: DepotId,
    acknowledgedIds: Set<String>,
    onDepotSelected: (DepotId) -> Unit,
    onAcknowledge: (String) -> Unit,
    onSendDemoNotification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val depotAlerts = alerts.filter { it.depotId == selectedDepot }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { DepotSelector(selectedDepot, onDepotSelected) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle(
                    title = "预警中心",
                    modifier = Modifier.weight(1f),
                    supportingText = "监测超限、预测影响与处置状态",
                )
                OutlinedButton(onClick = onSendDemoNotification, shape = RoundedCornerShape(6.dp)) {
                    Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
                    Text("测试推送", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        items(depotAlerts, key = { it.id }) { alert ->
            AlertCard(
                alert = alert,
                acknowledged = alert.id in acknowledgedIds || alert.status != AlertStatus.NEW,
                onAcknowledge = { onAcknowledge(alert.id) },
            )
        }
        if (depotAlerts.isEmpty()) {
            item {
                Text(
                    "当前油库暂无活动预警",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AlertEvent, acknowledged: Boolean, onAcknowledge: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SeverityBadge(alert.severity)
                Text(
                    alert.id,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(alert.createdAt, style = MaterialTheme.typography.bodySmall)
            }
            Text(alert.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(alert.triggerReason, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Text(
                "影响对象：${alert.affectedTarget}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "预计影响：${alert.expectedArrival}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onAcknowledge,
                enabled = !acknowledged,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(if (acknowledged) "预警已确认" else "确认并进入处置")
            }
        }
    }
}
