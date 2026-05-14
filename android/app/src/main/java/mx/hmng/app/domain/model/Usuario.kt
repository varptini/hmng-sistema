package mx.hmng.app.domain.model

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val token: String
) {
    val esAdmin: Boolean get() = rol == "ADMIN"
    val esAlmacenista: Boolean get() = rol == "ALMACENISTA"
}
