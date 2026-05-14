package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Dashboard

interface DashboardRepository {
    fun getDashboard(): Flow<Dashboard>
    suspend fun refreshDashboard(): Result<Dashboard>
}
