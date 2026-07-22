package edu.lzjtu.environment.oildepot.domain

import edu.lzjtu.environment.oildepot.data.model.AlertSeverity

object AlertPolicy {
    fun classify(concentrationRatio: Double, arrivalMinutes: Int?): AlertSeverity = when {
        concentrationRatio >= 2.0 -> AlertSeverity.CRITICAL
        arrivalMinutes != null && arrivalMinutes <= 30 && concentrationRatio >= 1.0 -> AlertSeverity.CRITICAL
        concentrationRatio >= 1.0 -> AlertSeverity.WARNING
        arrivalMinutes != null && arrivalMinutes <= 120 -> AlertSeverity.ATTENTION
        concentrationRatio >= 0.8 -> AlertSeverity.ATTENTION
        else -> AlertSeverity.NORMAL
    }
}

