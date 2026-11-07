package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Servicio

interface ServicioRepository {
    fun getServicios(): Flow<List<Servicio>>
    suspend fun refresh(): Result<Unit>
    suspend fun createServicio(payload: Map<String, Any>): Result<Servicio>
    suspend fun updateServicio(id: Int, payload: Map<String, Any>): Result<Servicio>
}
