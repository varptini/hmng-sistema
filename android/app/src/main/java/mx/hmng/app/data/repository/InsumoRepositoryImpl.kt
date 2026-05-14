package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.hmng.app.data.local.dao.InsumoDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

class InsumoRepositoryImpl @Inject constructor(
    private val api: HmngApiService,
    private val dao: InsumoDao
) : InsumoRepository {

    override fun getInsumos(query: String, alerta: String?): Flow<List<Insumo>> =
        dao.getAllFlow().map { entities ->
            entities
                .filter { e ->
                    query.isBlank() ||
                        e.nombre.contains(query, ignoreCase = true) ||
                        e.descripcion?.contains(query, ignoreCase = true) == true
                }
                .filter { e ->
                    when (alerta) {
                        "stock_bajo" -> e.existencia <= e.cantidadMinima
                        "por_caducar" -> e.estadoCaducidad == "POR_CADUCAR"
                        "caducado" -> e.estadoCaducidad == "CADUCADO"
                        else -> true
                    }
                }
                .map { it.toDomain() }
        }

    override fun getCaducados(): Flow<List<Insumo>> =
        dao.getAllFlow().map { entities ->
            entities.filter { it.estadoCaducidad == "CADUCADO" }.map { it.toDomain() }
        }

    override fun getPorCaducar(): Flow<List<Insumo>> =
        dao.getAllFlow().map { entities ->
            entities.filter { it.estadoCaducidad == "POR_CADUCAR" }.map { it.toDomain() }
        }

    override fun getStockBajo(): Flow<List<Insumo>> =
        dao.getAllFlow().map { entities ->
            entities.filter { it.existencia <= it.cantidadMinima }.map { it.toDomain() }
        }

    override suspend fun createInsumo(payload: Map<String, Any>): Result<Insumo> = runCatching {
        val response = api.createInsumo(payload)
        val dto = response.body() ?: error("Respuesta vacía")
        dao.upsert(dto.toEntity())
        dto.toEntity().toDomain()
    }

    override suspend fun updateInsumo(id: Int, payload: Map<String, Any>): Result<Insumo> = runCatching {
        val response = api.updateInsumo(id, payload)
        val dto = response.body() ?: error("Respuesta vacía")
        dao.upsert(dto.toEntity())
        dto.toEntity().toDomain()
    }

    override suspend fun deleteInsumo(id: Int): Result<Unit> = runCatching {
        api.deleteInsumo(id)
        dao.deleteById(id)
    }
}
