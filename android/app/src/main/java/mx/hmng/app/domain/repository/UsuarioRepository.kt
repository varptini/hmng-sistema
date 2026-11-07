package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.UsuarioSistema

interface UsuarioRepository {
    fun getUsuarios(): Flow<List<UsuarioSistema>>
    suspend fun refresh(): Result<Unit>
    suspend fun createUsuario(payload: Map<String, Any>): Result<Unit>
    suspend fun updateUsuario(id: Int, payload: Map<String, Any>): Result<Unit>
    suspend fun deleteUsuario(id: Int): Result<Unit>
}
