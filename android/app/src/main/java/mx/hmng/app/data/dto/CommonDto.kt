package mx.hmng.app.data.dto

/** Respuesta `{ id, message }` de los endpoints POST que no devuelven el registro completo. */
data class CreatedResponseDto(
    val id: Int = 0,
    val message: String? = null
)

/** Respuesta `{ message }` de endpoints de baja lógica (DELETE). */
data class MessageResponseDto(
    val message: String? = null
)
