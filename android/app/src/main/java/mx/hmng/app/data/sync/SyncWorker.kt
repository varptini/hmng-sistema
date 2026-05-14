package mx.hmng.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import mx.hmng.app.data.local.dao.DashboardCacheDao
import mx.hmng.app.data.local.dao.InsumoDao
import mx.hmng.app.data.local.dao.NotificacionDao
import mx.hmng.app.data.local.dao.PedidoSubalmacenDao
import mx.hmng.app.data.local.dao.PendingOperationDao
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: HmngApiService,
    private val insumoDao: InsumoDao,
    private val notificacionDao: NotificacionDao,
    private val dashboardCacheDao: DashboardCacheDao,
    private val pedidoSubalmacenDao: PedidoSubalmacenDao,
    private val pendingOperationDao: PendingOperationDao
) : CoroutineWorker(context, params) {

    private val gson = Gson()

    override suspend fun doWork(): Result {
        return try {
            refreshInsumos()
            refreshNotificaciones()
            refreshDashboard()
            refreshPedidos()
            processPendingOperations()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private suspend fun refreshInsumos() {
        val response = apiService.getInsumos()
        if (response.isSuccessful) {
            val entities = response.body()?.map { it.toEntity() } ?: return
            insumoDao.upsertAll(entities)
        }
    }

    private suspend fun refreshNotificaciones() {
        val response = apiService.getNotificaciones()
        if (response.isSuccessful) {
            val entities = response.body()?.map { it.toEntity() } ?: return
            notificacionDao.upsertAll(entities)
        }
    }

    private suspend fun refreshDashboard() {
        val response = apiService.getDashboard()
        if (response.isSuccessful) {
            val dto = response.body() ?: return
            dashboardCacheDao.upsertCache(dto.toEntity())
        }
    }

    private suspend fun refreshPedidos() {
        val response = apiService.getPedidosSubalmacen()
        if (response.isSuccessful) {
            val entities = response.body()?.map { it.toEntity() } ?: return
            pedidoSubalmacenDao.upsertAll(entities)
        }
    }

    private suspend fun processPendingOperations() {
        val pending = pendingOperationDao.getAll()
        for (op in pending) {
            if (op.retryCount >= MAX_OP_RETRIES) continue
            val success = executeOperation(op.type, op.payload)
            if (success) {
                pendingOperationDao.delete(op.id)
            } else {
                pendingOperationDao.incrementRetry(op.id)
            }
        }
    }

    private suspend fun executeOperation(type: String, payload: String): Boolean {
        return try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val body: Map<String, Any> = gson.fromJson(payload, mapType)
            when (type) {
                OP_CREATE_PEDIDO -> {
                    apiService.createPedido(body).isSuccessful
                }
                OP_CREATE_ENTRADA -> {
                    val insumoId = (body["insumo_id"] as? Double)?.toInt() ?: return false
                    apiService.createEntrada(insumoId, body).isSuccessful
                }
                OP_CREATE_SALIDA -> {
                    val insumoId = (body["insumo_id"] as? Double)?.toInt() ?: return false
                    apiService.createSalida(insumoId, body).isSuccessful
                }
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val MAX_OP_RETRIES = 5
        private const val WORK_NAME = "HmngSyncWorker"

        const val OP_CREATE_PEDIDO = "CREATE_PEDIDO"
        const val OP_CREATE_ENTRADA = "CREATE_ENTRADA"
        const val OP_CREATE_SALIDA = "CREATE_SALIDA"

        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
