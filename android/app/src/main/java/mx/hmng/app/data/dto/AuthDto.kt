package mx.hmng.app.data.dto

data class AuthResponseDto(
    val token: String = "",
    val user: UsuarioDto = UsuarioDto()
)

data class UsuarioDto(
    val id: Int = 0,
    val nombre: String = "",
    val email: String = "",
    val rol: String = ""
)
