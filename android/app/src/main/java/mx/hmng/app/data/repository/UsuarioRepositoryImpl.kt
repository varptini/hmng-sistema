package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.hmng.app.data.dto.UsuarioSistemaDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.data.remote.requireSuccess
import mx.hmng.app.domain.model.UsuarioSistema
import mx.hmng.app.domain.repository.UsuarioRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : UsuarioRepository {

    private val _cache = MutableStateFlow<List<UsuarioSistema>>(emptyList())

    override fun getUsuarios(): Flow<List<UsuarioSistema>> = _cache.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _cache.value = api.getUsuarios().bodyOrThrow().map { it.toDomain() }
    }

    // POST/PUT/DELETE devuelven filas parciales (sin empleado ni rol): se recarga la lista completa
    override suspend fun createUsuario(payload: Map<String, Any>): Result<Unit> = runCatching {
        api.createUsuario(payload).requireSuccess()
        refresh().getOrThrow()
    }

    override suspend fun updateUsuario(id: Int, payload: Map<String, Any>): Result<Unit> = runCatching {
        api.updateUsuario(id, payload).requireSuccess()
        refresh().getOrThrow()
    }

    override suspend fun deleteUsuario(id: Int): Result<Unit> = runCatching {
        // Baja lógica (activo = false); el usuario sigue en la lista como inactivo
        api.deleteUsuario(id).requireSuccess()
        refresh().getOrThrow()
    }

    private fun UsuarioSistemaDto.toDomain() = UsuarioSistema(
        id = id,
        nombreUsuario = nombreUsuario,
        activo = activo,
        ultimoAcceso = ultimoAcceso,
        nombre = nombre,
        correo = correo,
        rol = rol,
        rolId = rolId
    )
}
