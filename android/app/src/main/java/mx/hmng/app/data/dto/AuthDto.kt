package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    val token: String = "",
    val usuario: UsuarioDto = UsuarioDto()
)

data class UsuarioDto(
    val id: Int = 0,
    @SerializedName("nombre_usuario") val nombreUsuario: String = "",
    val nombre: String? = null,
    val correo: String? = null,
    val rol: String = "",
    @SerializedName("rol_id") val rolId: Int = 0
)
