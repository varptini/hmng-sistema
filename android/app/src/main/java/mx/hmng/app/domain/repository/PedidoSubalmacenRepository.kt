package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.PedidoSubalmacen

data class DetalleRequest(val insumoId: Int, val cantidad: Double)
data class DetalleRespuesta(val insumoId: Int, val cantidadEntregada: Double)

interface PedidoSubalmacenRepository {
    fun getPedidos(estado: String? = null): Flow<List<PedidoSubalmacen>>
    suspend fun crearPedido(detalles: List<DetalleRequest>): Result<PedidoSubalmacen>
    suspend fun atenderPedido(id: Int, detalles: List<DetalleRespuesta>): Result<Unit>
    suspend fun cancelarPedido(id: Int): Result<Unit>
}
