package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.DetalleEntradaRequest
import mx.hmng.app.domain.model.Entrada

interface EntradaRepository {
    fun getEntradas(): Flow<List<Entrada>>
    suspend fun refresh(): Result<Unit>
    suspend fun crearEntrada(observaciones: String?, detalles: List<DetalleEntradaRequest>): Result<Int>
}
