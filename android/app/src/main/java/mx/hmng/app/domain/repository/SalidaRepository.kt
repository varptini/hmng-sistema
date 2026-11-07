package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.DetalleSalidaRequest
import mx.hmng.app.domain.model.Salida

interface SalidaRepository {
    fun getSalidas(): Flow<List<Salida>>
    suspend fun refresh(): Result<Unit>
    suspend fun crearSalida(
        servicioId: Int,
        observaciones: String?,
        detalles: List<DetalleSalidaRequest>
    ): Result<Int>
}
