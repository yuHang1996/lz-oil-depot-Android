package edu.lzjtu.environment.oildepot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.lzjtu.environment.oildepot.data.model.EmergencyMeasure
import edu.lzjtu.environment.oildepot.data.model.MeasureStatus
import edu.lzjtu.environment.oildepot.ui.components.SectionTitle

@Composable
fun MeasuresScreen(
    measures: List<EmergencyMeasure>,
    completedIds: Set<String>,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedCount = measures.count { it.id in completedIds || it.status == MeasureStatus.COMPLETED }
    val progress = if (measures.isEmpty()) 0f else completedCount.toFloat() / measures.size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SectionTitle(title = "应急措施", supportingText = "预警关联任务、责任组与执行反馈") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("总体进度", style = MaterialTheme.typography.labelLarge)
                    Text("$completedCount/${measures.size}", style = MaterialTheme.typography.labelLarge)
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
        items(measures.sortedBy { it.priority }, key = { it.id }) { measure ->
            val completed = measure.id in completedIds || measure.status == MeasureStatus.COMPLETED
            MeasureCard(measure, completed) { onComplete(measure.id) }
        }
        item {
            Text(
                "措施内容用于技术辅助，不替代建设单位应急指挥决策。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun MeasureCard(measure: EmergencyMeasure, completed: Boolean, onComplete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "P${measure.priority}",
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = if (measure.priority == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    measure.title,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "责任组：${measure.owner}  ·  时限：${measure.deadline}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "关联预警：${measure.alertId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onComplete,
                enabled = !completed,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(if (completed) "已反馈完成" else "提交完成反馈")
            }
        }
    }
}
