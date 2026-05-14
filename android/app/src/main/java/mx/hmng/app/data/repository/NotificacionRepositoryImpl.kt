package mx.hmng.app.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mx.hmng.app.data.local.dao.NotificacionDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.socket.SocketManager
import mx.hmng.app.domain.model.Notificacion
import mx.hmng.app.domain.repository.NotificacionRepository
import javax.inject.Inject

class NotificacionRepositoryImpl @Inject constructor(
    private val api: HmngApiService,
    private val dao: NotificacionDao,
    private val socketManager: SocketManager
) : NotificacionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            socketManager.notificacionFlow.collect { dto ->
                dao.upsertAll(listOf(dto.toEntity()))
            }
        }
    }

    override fun getNotificaciones(): Flow<List<Notificacion>> =
        dao.getAllFlow().map { entities -> entities.map { it.toDomain() } }

    override fun getUnreadCount(): Flow<Int> = dao.getNoLeidasCount()

    override suspend fun fetchAndStore(): Result<Unit> = runCatching {
        val response = api.getNotificaciones()
        val dtos = response.body() ?: error("Respuesta vacía")
        dao.upsertAll(dtos.map { it.toEntity() })
    }

    override suspend fun marcarLeidas(ids: List<Int>): Result<Unit> = runCatching {
        if (ids.isNotEmpty()) {
            api.marcarNotificacionesLeidas(mapOf<String, Any>("ids" to ids))
            dao.markReadByIds(ids)
        }
    }

    override suspend fun marcarTodasLeidas(): Result<Unit> = runCatching {
        api.marcarNotificacionesLeidas(emptyMap<String, Any>())
        dao.markAllRead()
    }
}
