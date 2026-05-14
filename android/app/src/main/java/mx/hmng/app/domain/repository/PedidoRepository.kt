package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.domain.model.PedidoSubalmacen

interface PedidoRepository {
    fun getPedidosSubalmacen(estado: String? = null, servicioId: Int? = null): Flow<List<PedidoSubalmacen>>
    suspend fun createPedidoSubalmacen(payload: Map<String, Any>): NetworkResult<PedidoSubalmacen>
    suspend fun atenderPedido(id: Int): NetworkResult<PedidoSubalmacen>
    suspend fun cancelarPedido(id: Int): NetworkResult<PedidoSubalmacen>
    suspend fun getPedidosAlmacen(): NetworkResult<List<PedidoSubalmacen>>
    suspend fun updateEstadoPedidoAlmacen(id: Int, estado: String): NetworkResult<PedidoSubalmacen>
}
