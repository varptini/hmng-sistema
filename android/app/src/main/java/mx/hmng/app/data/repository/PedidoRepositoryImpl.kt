package mx.hmng.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.data.local.dao.PedidoSubalmacenDao
import mx.hmng.app.data.local.dao.PendingOperationDao
import mx.hmng.app.data.local.entity.PendingOperationEntity
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.sync.SyncWorker
import mx.hmng.app.domain.model.PedidoSubalmacen
import mx.hmng.app.domain.repository.PedidoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoRepositoryImpl @Inject constructor(
    private val apiService: HmngApiService,
    private val pedidoSubalmacenDao: PedidoSubalmacenDao,
    private val pendingOperationDao: PendingOperationDao,
    @ApplicationContext private val context: Context
) : PedidoRepository {

    private val gson = Gson()

    override fun getPedidosSubalmacen(estado: String?, servicioId: Int?): Flow<List<PedidoSubalmacen>> =
        channelFlow {
            launch {
                pedidoSubalmacenDao.getAllFlow().collect { entities ->
                    val filtered = entities
                        .filter { e -> estado == null || e.estado == estado }
                        .map { it.toDomain() }
                    send(filtered)
                }
            }
            try {
                val response = apiService.getPedidosSubalmacen()
                if (response.isSuccessful) {
                    response.body()?.let { dtos ->
                        pedidoSubalmacenDao.upsertAll(dtos.map { it.toEntity() })
                    }
                }
            } catch (_: Exception) {}
        }

    override suspend fun createPedidoSubalmacen(payload: Map<String, Any>): NetworkResult<PedidoSubalmacen> {
        return if (isNetworkAvailable()) {
            try {
                val response = apiService.createPedido(payload)
                if (response.isSuccessful) {
                    val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                    pedidoSubalmacenDao.upsertAll(listOf(dto.toEntity()))
                    NetworkResult.Success(dto.toEntity().toDomain())
                } else {
                    NetworkResult.Error("Error ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                queuePendingPedido(payload)
            }
        } else {
            queuePendingPedido(payload)
        }
    }

    private suspend fun queuePendingPedido(payload: Map<String, Any>): NetworkResult<PedidoSubalmacen> {
        pendingOperationDao.insert(
            PendingOperationEntity(
                type = SyncWorker.OP_CREATE_PEDIDO,
                payload = gson.toJson(payload)
            )
        )
        return NetworkResult.Error("Sin conexión: pedido guardado para sincronización")
    }

    override suspend fun atenderPedido(id: Int): NetworkResult<PedidoSubalmacen> {
        return try {
            val response = apiService.atenderPedido(id)
            if (response.isSuccessful) {
                val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                pedidoSubalmacenDao.upsertAll(listOf(dto.toEntity()))
                NetworkResult.Success(dto.toEntity().toDomain())
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun cancelarPedido(id: Int): NetworkResult<PedidoSubalmacen> {
        return try {
            val response = apiService.cancelarPedido(id)
            if (response.isSuccessful) {
                val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                pedidoSubalmacenDao.upsertAll(listOf(dto.toEntity()))
                NetworkResult.Success(dto.toEntity().toDomain())
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun getPedidosAlmacen(): NetworkResult<List<PedidoSubalmacen>> {
        return try {
            val response = apiService.getPedidosAlmacen()
            if (response.isSuccessful) {
                val pedidos = response.body()?.map { it.toEntity().toDomain() } ?: emptyList()
                NetworkResult.Success(pedidos)
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun updateEstadoPedidoAlmacen(id: Int, estado: String): NetworkResult<PedidoSubalmacen> {
        return try {
            val response = apiService.updateEstadoPedidoAlmacen(id, mapOf("estado" to estado))
            if (response.isSuccessful) {
                val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                NetworkResult.Success(dto.toEntity().toDomain())
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
