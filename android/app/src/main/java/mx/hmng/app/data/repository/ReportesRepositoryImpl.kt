package mx.hmng.app.data.repository

import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.InsumoInventario
import mx.hmng.app.domain.model.Movimiento
import mx.hmng.app.domain.repository.ReportesRepository
import javax.inject.Inject

class ReportesRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : ReportesRepository {

    override suspend fun getInventario(): Result<List<InsumoInventario>> = runCatching {
        val response = api.getInventario()
        val dtos = response.body() ?: error("Respuesta vacía")
        dtos.map { dto ->
            InsumoInventario(
                id = dto.id,
                nombre = dto.nombre,
                unidadMedida = dto.unidadMedida,
                stockActual = dto.stockActual,
                stockMinimo = dto.stockMinimo,
                estadoStock = dto.estadoStock
            )
        }
    }

    override suspend fun getMovimientos(
        desde: String,
        hasta: String,
        tipo: String?
    ): Result<List<Movimiento>> = runCatching {
        val response = api.getMovimientos(desde = desde, hasta = hasta, tipo = tipo)
        val dtos = response.body() ?: error("Respuesta vacía")
        dtos.map { dto ->
            Movimiento(
                id = dto.id,
                tipo = dto.tipo,
                insumo = dto.insumo,
                cantidad = dto.cantidad,
                fecha = dto.fecha,
                usuario = dto.usuario
            )
        }
    }
}
