package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.hmng.app.data.dto.SalidaDto
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.domain.model.DetalleSalidaRequest
import mx.hmng.app.domain.model.Salida
import mx.hmng.app.domain.repository.SalidaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalidaRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : SalidaRepository {

    private val _cache = MutableStateFlow<List<Salida>>(emptyList())

    override fun getSalidas(): Flow<List<Salida>> = _cache.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _cache.value = api.getSalidas().bodyOrThrow().data.map { it.toDomain() }
    }

    override suspend fun crearSalida(
        servicioId: Int,
        observaciones: String?,
        detalles: List<DetalleSalidaRequest>
    ): Result<Int> = runCatching {
        val payload = buildMap<String, Any> {
            put("servicio_id", servicioId)
            if (!observaciones.isNullOrBlank()) put("observaciones", observaciones.trim())
            put("detalles", detalles.map { d ->
                mapOf("insumo_id" to d.insumoId, "cantidad" to d.cantidad)
            })
        }
        // Errores como "Stock insuficiente para: ..." llegan en el campo `error` y se propagan
        val created = api.createSalidaGeneral(payload).bodyOrThrow()
        refresh()
        created.id
    }

    private fun SalidaDto.toDomain() = Salida(
        id = id,
        observaciones = observaciones,
        fechaRegistro = fechaRegistro,
        servicioNombre = servicioNombre,
        usuarioNombre = usuarioNombre,
        totalItems = totalItems
    )
}
