package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.data.local.dao.NotificacionDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.Notificacion
import mx.hmng.app.domain.repository.NotificacionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacionRepositoryImpl @Inject constructor(
    private val apiService: HmngApiService,
    private val notificacionDao: NotificacionDao
) : NotificacionRepository {

    override fun getNotificaciones(): Flow<List<Notificacion>> =
        notificacionDao.getAllFlow().map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncNotificaciones(): NetworkResult<Unit> {
        return try {
            val response = apiService.getNotificaciones()
            if (response.isSuccessful) {
                response.body()?.let { dtos ->
                    notificacionDao.upsertAll(dtos.map { it.toEntity() })
                }
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun marcarLeidas(): NetworkResult<Unit> {
        return try {
            val response = apiService.marcarNotificacionesLeidas()
            if (response.isSuccessful) {
                notificacionDao.markAllRead()
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }
}
