package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.hmng.app.data.dto.EntradaDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.domain.model.DetalleEntradaRequest
import mx.hmng.app.domain.model.Entrada
import mx.hmng.app.domain.repository.EntradaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntradaRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : EntradaRepository {

    private val _cache = MutableStateFlow<List<Entrada>>(emptyList())

    override fun getEntradas(): Flow<List<Entrada>> = _cache.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _cache.value = api.getEntradas().bodyOrThrow().data.map { it.toDomain() }
    }

    override suspend fun crearEntrada(
        observaciones: String?,
        detalles: List<DetalleEntradaRequest>
    ): Result<Int> = runCatching {
        val payload = buildMap<String, Any> {
            if (!observaciones.isNullOrBlank()) put("observaciones", observaciones.trim())
            put("detalles", detalles.map { d ->
                buildMap<String, Any> {
                    put("insumo_id", d.insumoId)
                    put("cantidad", d.cantidad)
                    d.lote?.takeIf { it.isNotBlank() }?.let { put("lote", it.trim()) }
                    d.fechaCaducidad?.takeIf { it.isNotBlank() }?.let { put("fecha_caducidad", it.trim()) }
                    d.precioUnitario?.let { put("precio_unitario", it) }
                }
            })
        }
        val created = api.createEntradaGeneral(payload).bodyOrThrow()
        // POST solo devuelve { id, message }: recargar la lista para tener la fila completa
        refresh()
        created.id
    }

    private fun EntradaDto.toDomain() = Entrada(
        id = id,
        observaciones = observaciones,
        fechaRegistro = fechaRegistro,
        nombreUsuario = nombreUsuario,
        usuarioNombre = usuarioNombre,
        almacenNombre = almacenNombre,
        totalItems = totalItems
    )
}
