package mx.hmng.app.domain.repository

import mx.hmng.app.domain.model.Usuario

interface AuthRepository {
    suspend fun login(usuario: String, contrasena: String): Result<Usuario>
}
