package edu.lzjtu.environment.oildepot.domain

import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictionInputValidatorTest {

    @Test
    fun validInput_hasNoErrors() {
        val input = PredictionInput(
            pollutantName = "苯",
            sourceStrengthKgPerHour = 12.0,
            releaseDurationMinutes = 45,
            windSpeedMetersPerSecond = 2.8,
            predictionHours = 6,
        )

        assertTrue(PredictionInputValidator.validate(input).isEmpty())
    }

    @Test
    fun invalidInput_reportsEveryInvalidField() {
        val input = PredictionInput(
            pollutantName = "",
            sourceStrengthKgPerHour = 0.0,
            releaseDurationMinutes = 0,
            windSpeedMetersPerSecond = -1.0,
            predictionHours = 49,
        )

        assertEquals(
            setOf("pollutant", "sourceStrength", "duration", "windSpeed", "predictionHours"),
            PredictionInputValidator.validate(input).map { it.field }.toSet(),
        )
    }
}

