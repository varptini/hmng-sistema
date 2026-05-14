package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.data.local.dao.InsumoDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsumoRepositoryImpl @Inject constructor(
    private val apiService: HmngApiService,
    private val insumoDao: InsumoDao
) : InsumoRepository {

    override fun getInsumos(search: String?, alerta: Boolean, page: Int): Flow<List<Insumo>> =
        channelFlow {
            launch {
                insumoDao.getAllFlow().collect { entities ->
                    val filtered = entities
                        .filter { e ->
                            val matchSearch = search.isNullOrBlank() ||
                                e.nombre.contains(search, ignoreCase = true) ||
                                e.descripcion?.contains(search, ignoreCase = true) == true
                            val matchAlerta = !alerta || e.existencia <= e.cantidadMinima
                            matchSearch && matchAlerta
                        }
                        .map { it.toDomain() }
                    send(filtered)
                }
            }
            try {
                val response = apiService.getInsumos(search, if (alerta) true else null, page)
                if (response.isSuccessful) {
                    response.body()?.let { dtos ->
                        insumoDao.upsertAll(dtos.map { it.toEntity() })
                    }
                }
            } catch (_: Exception) {}
        }

    override suspend fun getAlertas(): NetworkResult<List<Insumo>> {
        return try {
            val response = apiService.getInsumos(alerta = true)
            if (response.isSuccessful) {
                val insumos = response.body()?.map { dto ->
                    dto.toEntity().toDomain()
                } ?: emptyList()
                NetworkResult.Success(insumos)
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun createInsumo(payload: Map<String, Any>): NetworkResult<Insumo> {
        return try {
            val response = apiService.createInsumo(payload)
            if (response.isSuccessful) {
                val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                insumoDao.upsertAll(listOf(dto.toEntity()))
                NetworkResult.Success(dto.toEntity().toDomain())
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun updateInsumo(id: Int, payload: Map<String, Any>): NetworkResult<Insumo> {
        return try {
            val response = apiService.updateInsumo(id, payload)
            if (response.isSuccessful) {
                val dto = response.body() ?: return NetworkResult.Error("Respuesta vacía")
                insumoDao.upsertAll(listOf(dto.toEntity()))
                NetworkResult.Success(dto.toEntity().toDomain())
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    override suspend fun deleteInsumo(id: Int): NetworkResult<Unit> {
        return try {
            val response = apiService.deleteInsumo(id)
            if (response.isSuccessful) {
                refreshInsumosInRoom()
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión")
        }
    }

    private suspend fun refreshInsumosInRoom() {
        try {
            val response = apiService.getInsumos()
            if (response.isSuccessful) {
                response.body()?.let { dtos ->
                    insumoDao.upsertAll(dtos.map { it.toEntity() })
                }
            }
        } catch (_: Exception) {}
    }
}
