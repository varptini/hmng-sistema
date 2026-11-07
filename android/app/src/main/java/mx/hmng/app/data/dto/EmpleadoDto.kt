package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class EmpleadoDto(
    val id: Int = 0,
    val nombre: String = "",
    val correo: String? = null,
    val telefono: String? = null,
    val celular: String? = null,
    val direccion: String? = null,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String? = null
)
