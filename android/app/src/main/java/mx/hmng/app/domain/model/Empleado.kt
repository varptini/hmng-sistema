package mx.hmng.app.domain.model

data class Empleado(
    val id: Int,
    val nombre: String,
    val correo: String?,
    val telefono: String?,
    val celular: String?,
    val direccion: String?,
    val fechaNacimiento: String?
)
