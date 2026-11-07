package mx.hmng.app.data.dto

data class ServicioDto(
    val id: Int = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val activo: Boolean = true
)
