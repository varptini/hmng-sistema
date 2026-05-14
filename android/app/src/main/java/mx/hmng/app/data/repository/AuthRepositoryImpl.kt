package mx.hmng.app.data.repository

import android.content.Context
import androidx.work.WorkManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.hmng.app.core.security.TokenManager
import mx.hmng.app.core.util.NetworkResult
import mx.hmng.app.data.local.HmngDatabase
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: HmngApiService,
    private val tokenManager: TokenManager,
    private val database: HmngDatabase,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val gson = Gson()

    override suspend fun login(nombreUsuario: String, contrasena: String): NetworkResult<Usuario> {
        return try {
            val payload = mapOf("nombre_usuario" to nombreUsuario, "contrasena" to contrasena)
            val response = apiService.login(payload)
            if (response.isSuccessful) {
                val body = response.body() ?: return NetworkResult.Error("Respuesta vacía del servidor")
                val usuario = Usuario(
                    id = body.user.id,
                    nombre = body.user.nombre,
                    email = body.user.email,
                    rol = body.user.rol,
                    token = body.token
                )
                tokenManager.saveToken(body.token)
                tokenManager.saveUser(gson.toJson(usuario))
                NetworkResult.Success(usuario)
            } else {
                val msg = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    else -> "Error del servidor (${response.code()})"
                }
                NetworkResult.Error(msg, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión al servidor")
        }
    }

    override suspend fun logout() {
        try { apiService.logout() } catch (_: Exception) {}
        tokenManager.clearAll()
        WorkManager.getInstance(context).cancelAllWork()
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    override fun getCurrentUser(): Usuario? {
        val userJson = tokenManager.getUser() ?: return null
        return try {
            gson.fromJson(userJson, Usuario::class.java)
        } catch (_: Exception) {
            null
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override suspend fun cambiarContrasena(actual: String, nueva: String): NetworkResult<Unit> {
        return try {
            val payload = mapOf("actual" to actual, "nueva" to nueva)
            val response = apiService.cambiarContrasena(payload)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Error al cambiar contraseña (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sin conexión al servidor")
        }
    }
}
