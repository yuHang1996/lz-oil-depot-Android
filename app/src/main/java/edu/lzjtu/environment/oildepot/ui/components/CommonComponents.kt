package edu.lzjtu.environment.oildepot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.lzjtu.environment.oildepot.data.model.AlertSeverity
import edu.lzjtu.environment.oildepot.data.model.DataQuality
import edu.lzjtu.environment.oildepot.data.model.DepotId

@Composable
fun DepotSelector(
    selected: DepotId,
    onSelected: (DepotId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DepotId.entries.forEach { depot ->
            FilterChip(
                selected = selected == depot,
                onClick = { onSelected(depot) },
                label = { Text(depot.shortName) },
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (supportingText != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    supportingText: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .background(accent, RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SeverityBadge(severity: AlertSeverity) {
    val background = when (severity) {
        AlertSeverity.NORMAL -> Color(0xFFDDEFE3)
        AlertSeverity.ATTENTION -> Color(0xFFFFE9C9)
        AlertSeverity.WARNING -> Color(0xFFFFD8B0)
        AlertSeverity.CRITICAL -> Color(0xFFFFDAD6)
    }
    val foreground = when (severity) {
        AlertSeverity.NORMAL -> Color(0xFF215E3D)
        AlertSeverity.ATTENTION -> Color(0xFF755100)
        AlertSeverity.WARNING -> Color(0xFF8B4A00)
        AlertSeverity.CRITICAL -> Color(0xFF93000A)
    }
    Text(
        text = severity.displayName,
        modifier = Modifier.background(background, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        color = foreground,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun QualityLabel(quality: DataQuality) {
    val color = when (quality) {
        DataQuality.VALID -> MaterialTheme.colorScheme.secondary
        DataQuality.SUSPECT -> MaterialTheme.colorScheme.tertiary
        DataQuality.OFFLINE -> MaterialTheme.colorScheme.error
    }
    Text(text = quality.displayName, color = color, style = MaterialTheme.typography.labelMedium)
}
