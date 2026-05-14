package mx.hmng.app.domain.repository

import mx.hmng.app.domain.model.InsumoInventario
import mx.hmng.app.domain.model.Movimiento

interface ReportesRepository {
    suspend fun getInventario(): Result<List<InsumoInventario>>
    suspend fun getMovimientos(desde: String, hasta: String, tipo: String?): Result<List<Movimiento>>
}
