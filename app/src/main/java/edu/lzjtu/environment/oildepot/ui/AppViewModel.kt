package edu.lzjtu.environment.oildepot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.lzjtu.environment.oildepot.BuildConfig
import edu.lzjtu.environment.oildepot.data.model.DepotId
import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import edu.lzjtu.environment.oildepot.data.repository.FakeMonitoringRepository
import edu.lzjtu.environment.oildepot.data.repository.MonitoringRepository
import edu.lzjtu.environment.oildepot.data.repository.UnavailableProductionRepository
import edu.lzjtu.environment.oildepot.domain.PredictionInputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: MonitoringRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun selectDepot(depotId: DepotId) {
        _uiState.update { it.copy(selectedDepot = depotId) }
    }

    fun selectSection(section: MainSection) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun updatePredictionInput(transform: (PredictionInput) -> PredictionInput) {
        _uiState.update {
            it.copy(
                predictionInput = transform(it.predictionInput),
                predictionErrors = emptyMap(),
                predictionResult = null,
            )
        }
    }

    fun runPrediction() {
        val input = _uiState.value.predictionInput
        val errors = PredictionInputValidator.validate(input)
        if (errors.isNotEmpty()) {
            _uiState.update { state -> state.copy(predictionErrors = errors.associate { it.field to it.message }) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(predictionRunning = true, predictionErrors = emptyMap()) }
            runCatching { repository.runPrediction(input) }
                .onSuccess { result ->
                    _uiState.update { it.copy(predictionRunning = false, predictionResult = result) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            predictionRunning = false,
                            predictionErrors = mapOf("request" to "模型任务提交失败，请稍后重试"),
                        )
                    }
                }
        }
    }

    fun acknowledgeAlert(alertId: String) {
        _uiState.update { it.copy(acknowledgedAlertIds = it.acknowledgedAlertIds + alertId) }
    }

    fun completeMeasure(measureId: String) {
        _uiState.update { it.copy(completedMeasureIds = it.completedMeasureIds + measureId) }
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            runCatching { repository.loadDashboard() }
                .onSuccess { dashboard ->
                    _uiState.update { it.copy(loading = false, dashboard = dashboard, loadError = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            loadError = error.message ?: "数据加载失败",
                        )
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = if (BuildConfig.USE_MOCK_DATA) {
                    FakeMonitoringRepository()
                } else {
                    UnavailableProductionRepository()
                }
                return AppViewModel(repository) as T
            }
        }
    }
}
