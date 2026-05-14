package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.domain.model.Notificacion

interface NotificacionRepository {
    fun getNotificaciones(): Flow<List<Notificacion>>
    suspend fun syncNotificaciones(): NetworkResult<Unit>
    suspend fun marcarLeidas(): NetworkResult<Unit>
}
