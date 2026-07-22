package edu.lzjtu.environment.oildepot.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import edu.lzjtu.environment.oildepot.data.model.PredictionResult
import edu.lzjtu.environment.oildepot.ui.components.SectionTitle
import kotlin.math.roundToInt

@Composable
fun PredictionScreen(
    input: PredictionInput,
    errors: Map<String, String>,
    running: Boolean,
    result: PredictionResult?,
    onInputChanged: ((PredictionInput) -> PredictionInput) -> Unit,
    onRunPrediction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle(title = "扩散预测", supportingText = "配置突发污染情景并提交模型任务") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("污染物", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("苯", "甲苯", "非甲烷总烃", "石油类").forEach { pollutant ->
                        FilterChip(
                            selected = input.pollutantName == pollutant,
                            onClick = { onInputChanged { it.copy(pollutantName = pollutant) } },
                            label = { Text(pollutant) },
                        )
                    }
                }
            }
        }
        item {
            ParameterSlider(
                label = "源强",
                valueLabel = "%.1f kg/h".format(input.sourceStrengthKgPerHour),
                value = input.sourceStrengthKgPerHour.toFloat(),
                valueRange = 1f..50f,
                steps = 48,
                error = errors["sourceStrength"],
                onValueChange = { value -> onInputChanged { it.copy(sourceStrengthKgPerHour = value.toDouble()) } },
            )
        }
        item {
            ParameterSlider(
                label = "持续时间",
                valueLabel = "${input.releaseDurationMinutes} 分钟",
                value = input.releaseDurationMinutes.toFloat(),
                valueRange = 15f..180f,
                steps = 10,
                error = errors["duration"],
                onValueChange = { value -> onInputChanged { it.copy(releaseDurationMinutes = value.roundToInt()) } },
            )
        }
        item {
            ParameterSlider(
                label = "风速",
                valueLabel = "%.1f m/s".format(input.windSpeedMetersPerSecond),
                value = input.windSpeedMetersPerSecond.toFloat(),
                valueRange = 0f..15f,
                steps = 29,
                error = errors["windSpeed"],
                onValueChange = { value -> onInputChanged { it.copy(windSpeedMetersPerSecond = value.toDouble()) } },
            )
        }
        item {
            ParameterSlider(
                label = "预测时段",
                valueLabel = "${input.predictionHours} 小时",
                value = input.predictionHours.toFloat(),
                valueRange = 1f..24f,
                steps = 22,
                error = errors["predictionHours"],
                onValueChange = { value -> onInputChanged { it.copy(predictionHours = value.roundToInt()) } },
            )
        }
        if (errors["request"] != null) {
            item { Text(errors.getValue("request"), color = MaterialTheme.colorScheme.error) }
        }
        item {
            Button(
                onClick = onRunPrediction,
                enabled = !running,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
            ) {
                if (running) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 10.dp).size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Text("模型计算中")
                } else {
                    androidx.compose.material3.Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Text("提交预测任务", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        if (result != null) {
            item { PredictionResultPanel(result) }
        } else {
            item {
                Text(
                    "当前为大气扩散演示流程；地下水迁移模型将在模型参数和校验基准确认后接入。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    error: String?,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun PredictionResultPanel(result: PredictionResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(
                title = "预测结果",
                supportingText = "${result.taskId}  ·  ${result.modelName} ${result.modelVersion}",
            )
            PlumeMap()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultMetric("最大浓度", "%.2f %s".format(result.maxConcentration, result.concentrationUnit), Modifier.weight(1f))
                ResultMetric("影响面积", "%.2f km²".format(result.affectedAreaSquareKilometers), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultMetric("敏感点到达", "${result.arrivalMinutes} 分钟", Modifier.weight(1f))
                ResultMetric("扩散方向", result.windDirection, Modifier.weight(1f))
            }
            Text(
                "影响对象：${result.affectedSensitivePoint}  ·  计算时间 ${result.calculatedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)).padding(10.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun PlumeMap() {
    val background = Color(0xFFE8F0EC)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.9f)
            .clip(RoundedCornerShape(6.dp))
            .background(background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val source = Offset(size.width * 0.23f, size.height * 0.58f)
            rotate(-12f, pivot = source) {
                drawOval(Color(0x55F2C14E), topLeft = Offset(source.x, source.y - size.height * 0.28f), size = Size(size.width * 0.62f, size.height * 0.55f))
                drawOval(Color(0x77E88932), topLeft = Offset(source.x, source.y - size.height * 0.18f), size = Size(size.width * 0.43f, size.height * 0.34f))
                drawOval(Color(0x99C7443E), topLeft = Offset(source.x, source.y - size.height * 0.09f), size = Size(size.width * 0.22f, size.height * 0.17f))
            }
            drawCircle(Color(0xFF0B5F66), radius = 10f, center = source)
            drawCircle(Color(0xFF37805A), radius = 10f, center = Offset(size.width * 0.82f, size.height * 0.27f))
        }
        Text("污染源", Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 8.dp), style = MaterialTheme.typography.bodySmall)
        Text("敏感点", Modifier.align(Alignment.TopEnd).padding(10.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFF215E3D))
    }
}
