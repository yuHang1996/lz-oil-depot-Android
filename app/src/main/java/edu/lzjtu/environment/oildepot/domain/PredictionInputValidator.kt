package edu.lzjtu.environment.oildepot.domain

import edu.lzjtu.environment.oildepot.data.model.PredictionInput

data class ValidationError(
    val field: String,
    val message: String,
)

object PredictionInputValidator {
    fun validate(input: PredictionInput): List<ValidationError> = buildList {
        if (input.pollutantName.isBlank()) {
            add(ValidationError("pollutant", "请选择污染物"))
        }
        if (input.sourceStrengthKgPerHour <= 0.0 || input.sourceStrengthKgPerHour > 10_000.0) {
            add(ValidationError("sourceStrength", "源强应在 0 至 10000 kg/h 之间"))
        }
        if (input.releaseDurationMinutes !in 1..1_440) {
            add(ValidationError("duration", "释放时长应在 1 至 1440 分钟之间"))
        }
        if (input.windSpeedMetersPerSecond !in 0.0..75.0) {
            add(ValidationError("windSpeed", "风速应在 0 至 75 m/s 之间"))
        }
        if (input.predictionHours !in 1..48) {
            add(ValidationError("predictionHours", "预测时段应在 1 至 48 小时之间"))
        }
    }
}

