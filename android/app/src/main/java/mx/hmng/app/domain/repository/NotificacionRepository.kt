package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Notificacion

interface NotificacionRepository {
    fun getNotificaciones(): Flow<List<Notificacion>>
    fun getUnreadCount(): Flow<Int>
    suspend fun fetchAndStore(): Result<Unit>
    suspend fun marcarLeidas(ids: List<Int>): Result<Unit>
    suspend fun marcarTodasLeidas(): Result<Unit>
}
