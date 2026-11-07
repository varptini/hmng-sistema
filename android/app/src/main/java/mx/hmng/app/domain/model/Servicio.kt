package mx.hmng.app.domain.model

data class Servicio(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val activo: Boolean
)
