package edu.lzjtu.environment.oildepot.data.repository

import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import edu.lzjtu.environment.oildepot.data.model.PredictionResult

class UnavailableProductionRepository : MonitoringRepository {
    override suspend fun loadDashboard(): DashboardSnapshot {
        error("生产数据接口尚未配置")
    }

    override suspend fun runPrediction(input: PredictionInput): PredictionResult {
        error("生产模型接口尚未配置")
    }
}

