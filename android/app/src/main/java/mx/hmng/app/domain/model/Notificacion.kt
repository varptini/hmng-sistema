package mx.hmng.app.domain.model

data class Notificacion(
    val id: Int,
    val tipo: String,
    val mensaje: String,
    val referenciaId: Int?,
    val leida: Boolean,
    val createdAt: String
)
