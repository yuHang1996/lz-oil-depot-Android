package edu.lzjtu.environment.oildepot.data.repository

import edu.lzjtu.environment.oildepot.data.model.DashboardSnapshot
import edu.lzjtu.environment.oildepot.data.model.PredictionInput
import edu.lzjtu.environment.oildepot.data.model.PredictionResult

interface MonitoringRepository {
    suspend fun loadDashboard(): DashboardSnapshot
    suspend fun runPrediction(input: PredictionInput): PredictionResult
}

