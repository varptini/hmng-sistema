package mx.hmng.app.domain.model

/** Nombres de rol exactos tal como los define el backend (tabla `roles`). */
object Roles {
    const val ADMINISTRADOR = "Administrador"
    const val ABASTECEDOR = "Abastecedor"
    const val SUMINISTRADOR = "Suministrador"
    const val RESPONSABLE_SERVICIO = "Responsable de Servicio"
}

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val token: String
) {
    val esAdmin: Boolean get() = rol == Roles.ADMINISTRADOR
    val esAlmacenista: Boolean get() = rol == Roles.ABASTECEDOR
    val esSuministrador: Boolean get() = rol == Roles.SUMINISTRADOR
}
