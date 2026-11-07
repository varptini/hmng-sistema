package mx.hmng.app.data.dto

/** Respuesta paginada `{ data, total, page, limit }` (usada por GET /insumos). */
data class InsumosPageDto(
    val data: List<InsumoDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20
)

/** Respuesta `{ id, message }` de los endpoints POST que no devuelven el registro completo. */
data class CreatedResponseDto(
    val id: Int = 0,
    val message: String? = null
)

/** Respuesta `{ message }` de endpoints de baja lógica (DELETE). */
data class MessageResponseDto(
    val message: String? = null
)
