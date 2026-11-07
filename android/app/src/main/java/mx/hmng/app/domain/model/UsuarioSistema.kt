package mx.hmng.app.domain.model

/** Cuenta de usuario para administración (`/usuarios`); el usuario en sesión es [Usuario]. */
data class UsuarioSistema(
    val id: Int,
    val nombreUsuario: String,
    val activo: Boolean,
    val ultimoAcceso: String?,
    val nombre: String?,
    val correo: String?,
    val rol: String?,
    val rolId: Int
)
