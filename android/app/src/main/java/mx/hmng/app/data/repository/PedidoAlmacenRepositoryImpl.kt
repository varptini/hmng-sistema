package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import mx.hmng.app.data.dto.PedidoAlmacenDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.PedidoAlmacen
import mx.hmng.app.domain.repository.DetalleRequest
import mx.hmng.app.domain.repository.PedidoAlmacenRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoAlmacenRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : PedidoAlmacenRepository {

    private val _cache = MutableStateFlow<List<PedidoAlmacen>>(emptyList())

    override fun getPedidos(estado: String?): Flow<List<PedidoAlmacen>> =
        _cache.asStateFlow().map { list ->
            if (estado == null) list
            else list.filter { it.estado.equals(estado, ignoreCase = true) }
        }

    suspend fun refresh(estado: String?) {
        try {
            val response = api.getPedidosAlmacen(estado)
            if (response.isSuccessful) {
                _cache.value = response.body()?.map { it.toDomain() } ?: emptyList()
            }
        } catch (_: IOException) {
            // Use cached data
        }
    }

    override suspend fun getPedido(id: Int): Result<PedidoAlmacen> = runCatching {
        val response = api.getPedidoAlmacen(id)
        response.body()?.toDomain() ?: error("Pedido no encontrado")
    }

    override suspend fun crearPedido(detalles: List<DetalleRequest>): Result<PedidoAlmacen> =
        runCatching {
            val payload = mapOf(
                "detalles" to detalles.map { d ->
                    mapOf("insumo_id" to d.insumoId, "cantidad" to d.cantidad)
                }
            )
            val response = api.createPedidoAlmacen(payload)
            val dto = response.body() ?: error("Respuesta vacía del servidor")
            val pedido = dto.toDomain()
            _cache.value = _cache.value + pedido
            pedido
        }

    override suspend fun updateEstado(id: Int, estado: String, observacion: String?): Result<Unit> =
        runCatching {
            val payload: Map<String, Any?> = mapOf("estado" to estado, "observacion" to observacion)
            val response = api.updateEstadoPedidoAlmacen(id, payload)
            val dto = response.body() ?: error("Respuesta vacía del servidor")
            val updated = dto.toDomain()
            _cache.value = _cache.value.map { if (it.id == id) updated else it }
        }

    private fun PedidoAlmacenDto.toDomain() = PedidoAlmacen(
        id = id,
        estado = estado,
        observaciones = observaciones,
        servicioNombre = servicioNombre,
        solicitanteNombre = solicitanteNombre,
        totalItems = totalItems,
        fechaPedido = fechaPedido
    )
}
