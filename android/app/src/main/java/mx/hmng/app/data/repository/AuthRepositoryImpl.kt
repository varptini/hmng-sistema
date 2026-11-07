package mx.hmng.app.data.repository

import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.data.remote.bodyOrThrow
import mx.hmng.app.data.session.SessionManager
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: HmngApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(usuario: String, contrasena: String): Result<Usuario> = runCatching {
        val payload = mapOf("nombre_usuario" to usuario, "contrasena" to contrasena)
        val dto = api.login(payload).bodyOrThrow()
        val user = Usuario(
            id = dto.usuario.id,
            nombre = dto.usuario.nombre ?: dto.usuario.nombreUsuario,
            email = dto.usuario.correo ?: "",
            rol = dto.usuario.rol,
            token = dto.token
        )
        sessionManager.saveUser(user)
        user
    }
}
