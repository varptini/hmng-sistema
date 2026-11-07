package mx.hmng.app.data.dto

import com.google.gson.annotations.SerializedName

/** Fila de `GET /usuarios` (administración de cuentas, distinto del usuario en sesión). */
data class UsuarioSistemaDto(
    val id: Int = 0,
    @SerializedName("nombre_usuario") val nombreUsuario: String = "",
    val activo: Boolean = true,
    @SerializedName("ultimo_acceso") val ultimoAcceso: String? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val rol: String? = null,
    @SerializedName("rol_id") val rolId: Int = 0
)
