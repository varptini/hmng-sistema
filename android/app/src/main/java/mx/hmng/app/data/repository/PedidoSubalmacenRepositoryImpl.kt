package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mx.hmng.app.data.local.dao.PedidoSubalmacenDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.PedidoSubalmacen
import mx.hmng.app.domain.repository.DetalleRequest
import mx.hmng.app.domain.repository.DetalleRespuesta
import mx.hmng.app.domain.repository.PedidoSubalmacenRepository
import java.io.IOException
import javax.inject.Inject

class PedidoSubalmacenRepositoryImpl @Inject constructor(
    private val api: HmngApiService,
    private val dao: PedidoSubalmacenDao
) : PedidoSubalmacenRepository {

    override fun getPedidos(estado: String?): Flow<List<PedidoSubalmacen>> = channelFlow {
        launch {
            dao.getAllFlow()
                .map { entities ->
                    entities
                        .filter { e -> estado == null || e.estado.equals(estado, ignoreCase = true) }
                        .map { it.toDomain() }
                }
                .collect { send(it) }
        }
        try {
            val response = api.getPedidosSubalmacen(estado)
            if (response.isSuccessful) {
                response.body()?.let { dtos ->
                    dao.upsertAll(dtos.map { it.toEntity() })
                }
            }
        } catch (_: IOException) {
            // Offline - Room data is served above
        } catch (_: Exception) {
            // Other network errors - Room data is served above
        }
    }

    override suspend fun crearPedido(detalles: List<DetalleRequest>): Result<PedidoSubalmacen> =
        runCatching {
            val payload = mapOf(
                "detalles" to detalles.map { d ->
                    mapOf("insumo_id" to d.insumoId, "cantidad" to d.cantidad)
                }
            )
            val response = api.createPedidoSubalmacen(payload)
            val dto = response.body() ?: error("Respuesta vacía del servidor")
            dao.upsertAll(listOf(dto.toEntity()))
            dto.toEntity().toDomain()
        }

    override suspend fun atenderPedido(id: Int, detalles: List<DetalleRespuesta>): Result<Unit> =
        runCatching {
            val payload = mapOf(
                "detalles" to detalles.map { d ->
                    mapOf("insumo_id" to d.insumoId, "cantidad_entregada" to d.cantidadEntregada)
                }
            )
            val response = api.atenderPedido(id, payload)
            val dto = response.body() ?: error("Respuesta vacía del servidor")
            dao.upsertAll(listOf(dto.toEntity()))
        }

    override suspend fun cancelarPedido(id: Int): Result<Unit> = runCatching {
        val response = api.cancelarPedido(id)
        val dto = response.body() ?: error("Respuesta vacía del servidor")
        dao.upsertAll(listOf(dto.toEntity()))
    }
}
