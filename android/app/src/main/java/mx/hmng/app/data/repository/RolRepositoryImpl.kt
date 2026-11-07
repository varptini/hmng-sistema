package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.hmng.app.data.dto.RolDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.data.remote.requireSuccess
import mx.hmng.app.domain.model.Rol
import mx.hmng.app.domain.repository.RolRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RolRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : RolRepository {

    private val _cache = MutableStateFlow<List<Rol>>(emptyList())

    override fun getRoles(): Flow<List<Rol>> = _cache.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _cache.value = api.getRoles().bodyOrThrow().map { it.toDomain() }
    }

    override suspend fun createRol(payload: Map<String, Any>): Result<Unit> = runCatching {
        val rol = api.createRol(payload).bodyOrThrow().toDomain()
        _cache.value = (_cache.value + rol).sortedBy { it.id }
    }

    override suspend fun updateRol(id: Int, payload: Map<String, Any>): Result<Unit> = runCatching {
        val rol = api.updateRol(id, payload).bodyOrThrow().toDomain()
        _cache.value = _cache.value.map { if (it.id == id) rol else it }
    }

    override suspend fun deleteRol(id: Int): Result<Unit> = runCatching {
        // 204 sin cuerpo; 409 si el rol está asignado a usuarios (mensaje del backend)
        api.deleteRol(id).requireSuccess()
        _cache.value = _cache.value.filterNot { it.id == id }
    }

    private fun RolDto.toDomain() = Rol(
        id = id,
        nombre = nombre,
        descripcion = descripcion
    )
}
