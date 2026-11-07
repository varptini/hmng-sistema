package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Rol

interface RolRepository {
    fun getRoles(): Flow<List<Rol>>
    suspend fun refresh(): Result<Unit>
    suspend fun createRol(payload: Map<String, Any>): Result<Unit>
    suspend fun updateRol(id: Int, payload: Map<String, Any>): Result<Unit>
    suspend fun deleteRol(id: Int): Result<Unit>
}
