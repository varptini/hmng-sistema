package mx.hmng.app.domain.repository

import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.domain.model.Dashboard

interface DashboardRepository {
    suspend fun getDashboard(): NetworkResult<Dashboard>
}
