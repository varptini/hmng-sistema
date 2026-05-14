package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.PedidoAlmacen

interface PedidoAlmacenRepository {
    fun getPedidos(estado: String? = null): Flow<List<PedidoAlmacen>>
    suspend fun getPedido(id: Int): Result<PedidoAlmacen>
    suspend fun crearPedido(detalles: List<DetalleRequest>): Result<PedidoAlmacen>
    suspend fun updateEstado(id: Int, estado: String, observacion: String?): Result<Unit>
}
