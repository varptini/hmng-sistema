package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.hmng.app.data.dto.ServicioDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.domain.model.Servicio
import mx.hmng.app.domain.repository.ServicioRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServicioRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : ServicioRepository {

    private val _cache = MutableStateFlow<List<Servicio>>(emptyList())

    override fun getServicios(): Flow<List<Servicio>> = _cache.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _cache.value = api.getServicios().bodyOrThrow().map { it.toDomain() }
    }

    override suspend fun createServicio(payload: Map<String, Any>): Result<Servicio> = runCatching {
        val servicio = api.createServicio(payload).bodyOrThrow().toDomain()
        _cache.value = (_cache.value + servicio).sortedBy { it.nombre }
        servicio
    }

    override suspend fun updateServicio(id: Int, payload: Map<String, Any>): Result<Servicio> = runCatching {
        val servicio = api.updateServicio(id, payload).bodyOrThrow().toDomain()
        // GET /servicios solo lista activos: un servicio desactivado sale de la lista
        _cache.value = if (servicio.activo) {
            _cache.value.map { if (it.id == id) servicio else it }
        } else {
            _cache.value.filterNot { it.id == id }
        }
        servicio
    }

    private fun ServicioDto.toDomain() = Servicio(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        activo = activo
    )
}
