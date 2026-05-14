package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mx.hmng.app.data.local.dao.DashboardCacheDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toDto
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.Dashboard
import mx.hmng.app.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val api: HmngApiService,
    private val cacheDao: DashboardCacheDao
) : DashboardRepository {

    override fun getDashboard(): Flow<Dashboard> = flow {
        cacheDao.getCache()?.toDto()?.toDomain()?.let { emit(it) }
        runCatching { api.getDashboard() }
            .onSuccess { response ->
                response.body()?.let { dto ->
                    cacheDao.upsertCache(dto.toEntity())
                    emit(dto.toDomain())
                }
            }
    }

    override suspend fun refreshDashboard(): Result<Dashboard> = runCatching {
        val response = api.getDashboard()
        val dto = response.body() ?: error("Respuesta vacía")
        cacheDao.upsertCache(dto.toEntity())
        dto.toDomain()
    }
}
