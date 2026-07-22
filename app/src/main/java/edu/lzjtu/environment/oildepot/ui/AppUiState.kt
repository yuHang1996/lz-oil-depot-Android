package edu.lzjtu.environment.oildepot.ui

import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import edu.lzjtu.environment.oildepot.data.model.PredictionResult

enum class MainSection(val title: String) {
    OVERVIEW("态势"),
    POINTS("点位"),
    PREDICTION("预测"),
    ALERTS("预警"),
    MEASURES("措施"),
}

data class AppUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val selectedDepot: DepotId = DepotId.CRUDE,
    val selectedSection: MainSection = MainSection.OVERVIEW,
    val dashboard: DashboardSnapshot? = null,
    val predictionInput: PredictionInput = PredictionInput(),
    val predictionErrors: Map<String, String> = emptyMap(),
    val predictionRunning: Boolean = false,
    val predictionResult: PredictionResult? = null,
    val acknowledgedAlertIds: Set<String> = emptySet(),
    val completedMeasureIds: Set<String> = emptySet(),
)
