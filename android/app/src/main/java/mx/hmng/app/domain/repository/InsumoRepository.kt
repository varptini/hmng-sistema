package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Insumo

interface InsumoRepository {
    fun getInsumos(query: String = "", alerta: String? = null): Flow<List<Insumo>>
    fun getCaducados(): Flow<List<Insumo>>
    fun getPorCaducar(): Flow<List<Insumo>>
    fun getStockBajo(): Flow<List<Insumo>>
    suspend fun createInsumo(payload: Map<String, Any>): Result<Insumo>
    suspend fun updateInsumo(id: Int, payload: Map<String, Any>): Result<Insumo>
    suspend fun deleteInsumo(id: Int): Result<Unit>
}
