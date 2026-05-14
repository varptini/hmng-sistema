package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.domain.model.Insumo

interface InsumoRepository {
    fun getInsumos(search: String? = null, alerta: Boolean = false, page: Int = 1): Flow<List<Insumo>>
    suspend fun getAlertas(): NetworkResult<List<Insumo>>
    suspend fun createInsumo(payload: Map<String, Any>): NetworkResult<Insumo>
    suspend fun updateInsumo(id: Int, payload: Map<String, Any>): NetworkResult<Insumo>
    suspend fun deleteInsumo(id: Int): NetworkResult<Unit>
}
