package mx.hmng.app.data.repository

import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.data.local.dao.DashboardCacheDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toDto
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.Dashboard
import mx.hmng.app.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_MAX_AGE_MS = 5 * 60 * 1000L

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: HmngApiService,
    private val dashboardCacheDao: DashboardCacheDao
) : DashboardRepository {

    override suspend fun getDashboard(): NetworkResult<Dashboard> {
        if (!dashboardCacheDao.isExpired(CACHE_MAX_AGE_MS)) {
            val cached = dashboardCacheDao.getCache()?.toDto()?.toDomain()
            if (cached != null) return NetworkResult.Success(cached)
        }
        return fetchFromNetwork()
    }

    private suspend fun fetchFromNetwork(): NetworkResult<Dashboard> {
        return try {
            val response = apiService.getDashboard()
            if (response.isSuccessful) {
                val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                dashboardCacheDao.upsertCache(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                val cached = dashboardCacheDao.getCache()?.toDto()?.toDomain()
                if (cached != null) {
                    NetworkResult.Success(cached)
                } else {
                    NetworkResult.Error("Error ${response.code()}", response.code())
                }
            }
        } catch (e: Exception) {
            val cached = dashboardCacheDao.getCache()?.toDto()?.toDomain()
            if (cached != null) {
                NetworkResult.Success(cached)
            } else {
                NetworkResult.Error(e.message ?: "Sin conexión")
            }
        }
    }
}
