package edu.lzjtu.environment.oildepot.domain

import edu.lzjtu.environment.oildepot.data.model.AlertSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertPolicyTest {

    @Test
    fun classifiesCritical_whenConcentrationIsDoubleThreshold() {
        assertEquals(
            AlertSeverity.CRITICAL,
            AlertPolicy.classify(concentrationRatio = 2.1, arrivalMinutes = 90),
        )
    }

    @Test
    fun classifiesCritical_whenSensitivePointWillBeReachedSoon() {
        assertEquals(
            AlertSeverity.CRITICAL,
            AlertPolicy.classify(concentrationRatio = 1.1, arrivalMinutes = 20),
        )
    }

    @Test
    fun classifiesWarning_forThresholdExceedance() {
        assertEquals(
            AlertSeverity.WARNING,
            AlertPolicy.classify(concentrationRatio = 1.25, arrivalMinutes = 180),
        )
    }

    @Test
    fun classifiesNormal_whenNoRiskRuleIsMet() {
        assertEquals(
            AlertSeverity.NORMAL,
            AlertPolicy.classify(concentrationRatio = 0.72, arrivalMinutes = null),
        )
    }

    @Test
    fun classifiesAttention_whenConcentrationApproachesThreshold() {
        assertEquals(
            AlertSeverity.ATTENTION,
            AlertPolicy.classify(concentrationRatio = 0.82, arrivalMinutes = null),
        )
    }
}
