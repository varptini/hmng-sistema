package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.hmng.app.data.dto.EmpleadoDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.data.remote.requireSuccess
import mx.hmng.app.domain.model.Empleado
import mx.hmng.app.domain.repository.EmpleadoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmpleadoRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : EmpleadoRepository {

    private val _cache = MutableStateFlow<List<Empleado>>(emptyList())

    override fun getEmpleados(): Flow<List<Empleado>> = _cache.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _cache.value = api.getEmpleados().bodyOrThrow().map { it.toDomain() }
    }

    override suspend fun createEmpleado(payload: Map<String, Any>): Result<Unit> = runCatching {
        val empleado = api.createEmpleado(payload).bodyOrThrow().toDomain()
        _cache.value = (_cache.value + empleado).sortedBy { it.nombre }
    }

    override suspend fun updateEmpleado(id: Int, payload: Map<String, Any>): Result<Unit> = runCatching {
        val empleado = api.updateEmpleado(id, payload).bodyOrThrow().toDomain()
        _cache.value = _cache.value.map { if (it.id == id) empleado else it }
    }

    override suspend fun deleteEmpleado(id: Int): Result<Unit> = runCatching {
        // Baja lógica en backend (activo = false); GET ya no lo devuelve
        api.deleteEmpleado(id).requireSuccess()
        _cache.value = _cache.value.filterNot { it.id == id }
    }

    private fun EmpleadoDto.toDomain() = Empleado(
        id = id,
        nombre = nombre,
        correo = correo,
        telefono = telefono,
        celular = celular,
        direccion = direccion,
        fechaNacimiento = fechaNacimiento
    )
}
