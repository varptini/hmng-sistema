package mx.hmng.app.domain.repository

import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.domain.model.Usuario

interface AuthRepository {
    suspend fun login(nombreUsuario: String, contrasena: String): NetworkResult<Usuario>
    suspend fun logout()
    fun getCurrentUser(): Usuario?
    fun isLoggedIn(): Boolean
    suspend fun cambiarContrasena(actual: String, nueva: String): NetworkResult<Unit>
}
